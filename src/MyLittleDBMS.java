import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import javax.sql.RowSet;

import javafx.application.*;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;


/**
 * @version 1.0 2021-10-09
 * @author Vsevolod Batyrov
 */

public class MyLittleDBMS extends Application{
    public static void main(String[] args) {
        launch(args);
    }

    private Properties props;
    private Connection conn;
    private ComboBox<String> tableNames;

    /**
     *
     * default javafx window creation
     * using BorderPane layout to place components
     *
     */
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
        Button nextButton = new Button("Next");
        Button deleteButton = new Button("Delete");
        Button saveButton = new Button("Save");
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


/**
 * defines better version of GridPane
 * using to show databases table values
 * and to change them
 */
class DataPane extends GridPane{
    private ArrayList<TextField> fields;

    DataPane() {
        super();
        fields = new ArrayList<TextField>();
        TextField tf = new TextField("dol");
        fields.add(tf);
        for (int i=0;i<fields.size();i++){
            this.add(new Label("First"),0,0);
            this.add(fields.get(i),1,0);
        }
    }
}