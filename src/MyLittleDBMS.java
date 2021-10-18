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

    private static final String version = "0.1";
    private static final String title = "QuickCon " + version;
    private static final int width = 1440; // 960 // 1440
    private static final int height = 810; // 540 // 810

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

        try(Statement stat = DatabaseManager.getConnection().createStatement(); ResultSet result = stat.executeQuery("SHOW TABLES")) {
            while (result.next()) {
                tableNames.getItems().add(result.getString(1));
            }
        } catch (SQLException ex){
            ex.printStackTrace();
        }

        //If we choose combobox item, it will show table from DB
        tableNames.setOnAction((actionEvent -> mlFunctions.showTable(tableNames.getSelectionModel().getSelectedItem(), DatabaseManager.getConnection(), rootNode)));

        //Create buttons
        HBox buttonBox = new HBox();

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(actionEvent -> {
            mlFunctions.delete();
            mlFunctions.showTable(tableNames.getSelectionModel().getSelectedItem(), DatabaseManager.getConnection(), rootNode);
        });

        Button saveButton = new Button("Save");
        //saveButton.setOnAction(actionEvent -> mlFunctions.save());

        buttonBox.getChildren().addAll(deleteButton, saveButton);
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

        stage.show();
    }

    @Override
    public void stop(){
        try{
            if(DatabaseManager.getConnection() != null) DatabaseManager.getConnection().close();
        } catch (SQLException ex){
            for(Throwable t:ex)
                t.printStackTrace();
        } finally {
            System.out.println("The application has been stopped");
        }
    }

}

