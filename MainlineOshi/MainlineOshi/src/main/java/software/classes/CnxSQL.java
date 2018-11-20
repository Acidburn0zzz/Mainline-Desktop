package software.classes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class CnxSQL {

    private final JSlack jslack = new JSlack();
    private final DadosOshi oshi = new DadosOshi();
    private final Usuario user = new Usuario();
    private final String idAtivo = oshi.getAtivoID(); // id do Ativo

    // Variaveis de Cnx
    protected final String url = String.format("jdbc:sqlserver://lol-2018.database.windows.net:1433;database=ADS 2018;user=jessicasantos@lol-2018;password=Corinthians11;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;");
    protected Connection cnx = null;
    protected Statement stm = null;

    public boolean autenticaUsuario(String email, String senha) {
        try {
            // Abre conexão
            cnx = DriverManager.getConnection(url);// A classe DriverManager tentará carregar as classes de driver referenciadas na propriedade de sistema "jdbc.drivers". Isso permite que um usuário personalize os drivers JDBC usados por seus aplicativos;
            stm = cnx.createStatement();// Cria um objeto Statement para enviar instruções SQL para o BD;
            //String select = "SELECT * FROM usuario";
            String select = "SELECT * FROM usuario WHERE email = '" + email + "' and senha = '" + senha + "'";// Faz select
            ResultSet rs = stm.executeQuery(select);// Executa a instrução SQL fornecida, que retorna um objeto ResultSet;

            if (rs.next()) { // Le os dados no BD;
                // if (rs.getString("email").equals(email) && rs.getString("senha").equals(senha)) {
                user.setNome(rs.getString("nome"));
                jslack.usuarioLogado(user.getNome());
                verificaAtivoID();
                return true; // if email e senha fornecidos pelo usuario existirem no BD, retorna true;
                //}
            }
            cnx.close();

        } catch (SQLException ex) {
            Logger.getLogger(CnxSQL.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    public void verificaAtivoID() {// Verifica se ativo já existe no BD;
        try {
            cnx = DriverManager.getConnection(url);
            stm = cnx.createStatement();
            String select = "SELECT * FROM ativo WHERE idAtivo = '" + idAtivo + "'";
            ResultSet rs = stm.executeQuery(select);
            if (rs.next()) {
            } else {
                String insert = "INSERT INTO ativo (idAtivo) VALUES ('" + idAtivo + "')";
                stm.executeUpdate(insert);// Executa a instrução SQL fornecida, que pode ser uma instrução INSERT, UPDATE ou DELETE;
            }

            cnx.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, ex, "Erro!", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(CnxSQL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void insertComponente(String tabela, String coluna, String nomeComponente, float valorComponente, long sleep) throws InterruptedException {
        try {
            cnx = DriverManager.getConnection(url);
            stm = cnx.createStatement();
            String insert = "INSERT INTO " + tabela + " (" + coluna + ", idAtivo) VALUES (" + valorComponente + ", '" + idAtivo + "')";
            stm.executeUpdate(insert);

            cnx.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "CnxSQL Componente " + ex, "Erro!", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(CnxSQL.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (valorComponente > 80) {
            jslack.alertaComponente(nomeComponente);
        }
        Thread.sleep(sleep);
    }

    public void insertRede() throws InterruptedException {
        float download = oshi.getDownload();
        float upload = oshi.getUpload();
        try {
            cnx = DriverManager.getConnection(url);
            stm = cnx.createStatement();
            String insert = "INSERT INTO infoRede (upload, download, idAtivo) VALUES (" + upload + "," + download + ",'" + idAtivo + "')";
            stm.executeUpdate(insert);

            cnx.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "CnxSQL Rede " + ex, "Erro!", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(CnxSQL.class.getName()).log(Level.SEVERE, null, ex);
        }
        Thread.sleep(20000);
    }
}
