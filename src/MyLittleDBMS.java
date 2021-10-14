import java.sql.*;
import javax.sql.rowset.CachedRowSet;

import javafx.application.*;
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

    private MLFunctions mlFunctions;
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

        mlFunctions = new MLFunctions();
        databaseManager = new DatabaseManager();
        ComboBox<String> tableNames = new ComboBox<>();

        try{
            DatabaseMetaData meta = DatabaseManager.getConnection().getMetaData();
            try(ResultSet mrs = meta.getTables(null, null, null, new String[] {"TABLE"})) {
                while (mrs.next()) tableNames.getItems().add(mrs.getString(3));
            }
        } catch (SQLException ex){
            for(Throwable t: ex)
                t.printStackTrace();
        }

        //If we choose combobox item, it will show table from DB
//        tableNames.setOnAction((actionEvent -> System.out.println(tableNames.getSelectionModel().getSelectedItem())));
        tableNames.setOnAction((actionEvent -> mlFunctions.showTable( (String) tableNames.getSelectionModel().getSelectedItem(), DatabaseManager.getConnection(), rootNode)));


        //Create buttons
        HBox buttonBox = new HBox();

        Button previousButton = new Button("Previous");
        previousButton.setOnAction(actionEvent -> mlFunctions.previousAction(cachedRowSet, dataPane));

        Button nextButton = new Button("Next");
        nextButton.setOnAction(actionEvent -> mlFunctions.nextAction(cachedRowSet, dataPane));

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(actionEvent -> mlFunctions.delete());

        Button saveButton = new Button("Save");
        saveButton.setOnAction(actionEvent -> mlFunctions.save());

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


        //Create changeable table !!! NOW INITIALIZED IN MLFunctions:28
//        DataPane dataPane = new DataPane(cachedRowSet);
//        dataPane.setAlignment(Pos.CENTER);
//        rootNode.setCenter(dataPane);

        stage.show();

    }

    @Override
    public void stop(){
        try{
            if(databaseManager.getConnection() != null) databaseManager.getConnection().close();
        } catch (SQLException ex){
            for(Throwable t:ex)
                t.printStackTrace();
        } finally {
            System.out.println("The application has been stopped");
        }
    }

}

