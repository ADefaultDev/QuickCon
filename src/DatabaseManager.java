import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

class DatabaseManager {

    private Properties props;
    private static Connection connection;
    private String dbName="";


    DatabaseManager(String dbName) {
        try {
            readDatabaseProperties();
            this.dbName=dbName;
            connection = getConnectionToDataBase(dbName);
            connection.setAutoCommit(false);
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (SQLException ex) {
            for(Throwable t: ex)
                t.printStackTrace();
        }
    }

    private void readDatabaseProperties() throws IOException {
        props = new Properties();
        try {
            try (InputStream in = Files.newInputStream(Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("database.properties")).toURI()))) {
                props.load(in);
            }
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }
    }

    private Connection getConnectionToDataBase(String dbName) throws SQLException {
        String url;
        this.dbName=dbName;
        if(dbName.equals("")){
            url = props.getProperty("jdbc.url");
        }
        else {
            url = "jdbc:mysql://127.0.0.1:3306/" + dbName;
        }
        String username = props.getProperty("jdbc.username");
        String password = props.getProperty("jdbc.password");
        return DriverManager.getConnection(url, username, password);
    }

    static Connection getConnection() {
        return connection;
    }

    void reconnection() {
        try {
            readDatabaseProperties();
            connection = getConnectionToDataBase(dbName);
            connection.setAutoCommit(false);
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (SQLException ex) {
            for(Throwable t: ex)
                t.printStackTrace();
        }
    }

    String getDbName(){
        return dbName;
    }

    String[] getAllDatabases(){return props.getProperty("jdbc.databases").split(",");}


}
