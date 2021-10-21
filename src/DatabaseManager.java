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

    DatabaseManager() {
        try {
            readDatabaseProperties();
            connection = getConnectionToDataBase();
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

    private Connection getConnectionToDataBase() throws SQLException {
        String url = props.getProperty("jdbc.url");
        String username = props.getProperty("jdbc.username");
        String password = props.getProperty("jdbc.password");
        return DriverManager.getConnection(url, username, password);
    }

    public String getDatabase() {
        return props.getProperty("jdbc.database");
    }

    static Connection getConnection() {
        return connection;
    }

    public void reconnection() {
        try {
            readDatabaseProperties();
            connection = getConnectionToDataBase();
            connection.setAutoCommit(false);
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (SQLException ex) {
            for(Throwable t: ex)
                t.printStackTrace();
        }
    }
}
