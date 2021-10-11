import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import javax.sql.RowSet;
import javax.sql.rowset.CachedRowSet;

import javafx.application.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;


/**
 * @version 1.0 2021-10-09
 * @author ADefaultDev Vsevolod Batyrov
 * @author Rauf-ID Rauf Agaguliev
 */

public class MyLittleDBMS extends Application{
    public static void main(String[] args) {
        launch(args);
    }

    private Properties props;
    private Connection conn;
    private ComboBox<String> tableNames;
    private DataPane dataPane;
    private CachedRowSet cachedRowSet;

    @Override
    public void start(Stage stage){
        stage.setTitle("MyLittleDBMS");
        BorderPane rootNode = new BorderPane();
        Scene myScene = new Scene(rootNode,400,400);
        stage.setScene(myScene);

        tableNames = new ComboBox<>();
        try{
            readDatabaseProperties();
            conn = getConnection();
            DatabaseMetaData meta = conn.getMetaData();
            try(ResultSet mrs = meta.getTables(null, null, null,
                    new String[] {"Table"})) {
                while (mrs.next())
                    tableNames.getItems().add(mrs.getString(3));
            }
        }
        catch (SQLException ex){
            for(Throwable t: ex)
                t.printStackTrace();
        }
        catch (IOException ex){
            ex.printStackTrace();
        }


        //Create buttons
        HBox buttonBox = new HBox();
        Button previousButton = new Button("Previous");
        previousButton.setOnAction(actionEvent -> MLFunctions.previousAction(cachedRowSet,dataPane));

        Button nextButton = new Button("Next");
        nextButton.setOnAction(actionEvent -> MLFunctions.nextAction(cachedRowSet,dataPane));

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(actionEvent -> MLFunctions.delete());

        Button saveButton = new Button("Save");
        saveButton.setOnAction(actionEvent -> MLFunctions.save());

        buttonBox.getChildren().addAll(previousButton,nextButton,deleteButton,saveButton);

        buttonBox.setSpacing(50);
        buttonBox.setAlignment(Pos.CENTER);
        rootNode.setBottom(buttonBox);

        //Create table names
        HBox tableNamesBox = new HBox();
        Label label = new Label("List of table titles:");
        tableNamesBox.getChildren().addAll(label, tableNames);

        tableNamesBox.setSpacing(50);
        tableNamesBox.setAlignment(Pos.CENTER);
        rootNode.setTop(tableNamesBox);

        //Create changeable table
        DataPane dataPane = new DataPane();
        dataPane.setAlignment(Pos.CENTER);
        rootNode.setCenter(dataPane);

        stage.show();

    }

    /**
     * default javafx function override
     * to close connection even if application
     * stops because of error
     */
    @Override
    public void stop(){
        try{
            if(conn != null) conn.close();
        }
        catch (SQLException ex){
            for(Throwable t:ex)
                t.printStackTrace();
        }
        finally {
            System.out.println("The application has been stopped");
        }
    }

    /**
     * reading database properties to connect
     * from file database.properties
     *
     * @throws IOException
     */
    private void readDatabaseProperties() throws IOException {
        props = new Properties();
        try {
            try (InputStream in = Files.newInputStream(Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("database.properties")).toURI())))
            {
                props.load(in);
            }
        }

        catch (URISyntaxException ex){
            ex.printStackTrace();
        }
    }

    private Connection getConnection() throws SQLException{
        String url = props.getProperty("jdbc.url");
        String username = props.getProperty("jdbc.username");
        String password = props.getProperty("jdbc.password");
        return DriverManager.getConnection(url,username,password);
    }
}

