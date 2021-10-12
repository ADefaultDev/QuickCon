import java.sql.*;
import javax.sql.rowset.CachedRowSet;

import javafx.application.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;


/**
 * @version 1.0 2021-10-12
 * @author ADefaultDev Vsevolod Batyrov
 * @author Rauf-ID Rauf Agaguliev
 */

public class MyLittleDBMS extends Application {

    private static final String version = "0.0.0";
    private static final String title = "MyLittleDBMS " + version;
    private static final int width = 960;
    private static final int height = 540;

    private DataPane dataPane;
    private CachedRowSet cachedRowSet;
    private ComboBox<String> tableNames;
    private DatabaseManager databaseManager;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage){
        stage.setTitle(title);
        BorderPane rootNode = new BorderPane();
        Scene myScene = new Scene(rootNode, width, height);
        stage.setScene(myScene);

        databaseManager = new DatabaseManager();
        tableNames = new ComboBox<>();

        try{
            DatabaseMetaData meta = databaseManager.getConnection().getMetaData();
            try(ResultSet mrs = meta.getTables(null, null, null, new String[] {"Table"})) {
                while (mrs.next()) {
                    tableNames.getItems().add(mrs.getString(3));
                }
            }
        } catch (SQLException ex){
            for(Throwable t: ex)
                t.printStackTrace();
        }

        //If we choose combobox item, it will show table from DB
        tableNames.setOnAction((actionEvent -> System.out.println(tableNames.getSelectionModel().getSelectedItem())));

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
        DataPane dataPane = new DataPane(cachedRowSet);
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
            if(databaseManager.getConnection() != null) databaseManager.getConnection().close();
        }
        catch (SQLException ex){
            for(Throwable t:ex)
                t.printStackTrace();
        }
        finally {
            System.out.println("The application has been stopped");
        }
    }


}

