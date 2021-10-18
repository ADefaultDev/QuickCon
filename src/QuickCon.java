import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.util.Callback;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;



public class QuickCon  extends Application {

    private static final String version = "0.1";
    private static final String title = "QuickCon " + version;
    private static final int width = 1440; // 960 // 1440
    private static final int height = 810; // 540 // 810

    private BorderPane rootNode;
    private DatabaseManager databaseManager;
    private TreeView<String> treeView;
    private ObservableList<ObservableList> data;
    private TableView tableview;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle(title);

        rootNode = new BorderPane();
        databaseManager = new DatabaseManager();

        //get names of databases and their tables
        ArrayList<String> databaseNames = new ArrayList<>();
        ArrayList<String> tableNames = new ArrayList<>();
        try(Statement stat = DatabaseManager.getConnection().createStatement(); ResultSet result = stat.executeQuery("SHOW DATABASES")) {
            while (result.next()) databaseNames.add(result.getString(1));
        } catch (SQLException ex){
            ex.printStackTrace();
        }
        try(Statement stat = DatabaseManager.getConnection().createStatement(); ResultSet result = stat.executeQuery("SHOW TABLES")) {
            while (result.next())                 tableNames.add(result.getString(1));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        //Root tree
        TreeItem<String> root = new TreeItem<>();
        //DBMS next tree
        TreeItem<String> DBMS = makeBranches("MySQL - @localhost", root);
        DBMS.setExpanded(true);
        //Create tree for databases and their tables
        for (String nameDatabase: databaseNames) {
            TreeItem<String> database = makeBranches(nameDatabase, DBMS);
            database.setExpanded(true);
            String currentDatabase = database.getValue();
            if (currentDatabase.toString().equals(databaseManager.getDatabase())) {
                TreeItem<String> nodeTable = makeBranches("tables", database); // should be one block higher
                nodeTable.setExpanded(true);
                for (String nameTable: tableNames) {
                    TreeItem<String> tableDB = makeBranches(nameTable, nodeTable);
                    tableDB.setExpanded(true);
                }
            }
        }
        treeView = new TreeView<>(root);
        treeView.setShowRoot(false);
        treeView.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getClickCount() == 2)
            {
                TreeItem<String> item = treeView.getSelectionModel().getSelectedItem();
                if (item.getValue() != null)
                    if (item.getParent().getValue().equals("tables"))
                        System.out.println(item.getValue());
                        createTable(item.getValue());
            }
        });
        rootNode.setLeft(treeView);


        Scene myScene = new Scene(rootNode, width, height);
        stage.setScene(myScene);
        stage.show();
    }

    //Create branches
    public TreeItem<String> makeBranches(String title, TreeItem<String> parent) {
        TreeItem<String> item = new TreeItem<>(title);
        item.setExpanded(false);
        parent.getChildren().add(item);
        return item;
    }

    public void createTable(String tableName){
        tableview = new TableView();
        tableview.setEditable(true);

        data = FXCollections.observableArrayList();
        try (Statement stat = DatabaseManager.getConnection().createStatement(); ResultSet result = stat.executeQuery("SELECT * FROM " + tableName)) {
            for(int i = 0 ; i < result.getMetaData().getColumnCount(); i++){
                final int j = i;
                TableColumn col = new TableColumn(result.getMetaData().getColumnName(1 + i));
                col.setCellValueFactory((Callback<CellDataFeatures<ObservableList, String>, ObservableValue<String>>) param ->
                        new SimpleStringProperty(param.getValue().get(j).toString()));
                tableview.getColumns().addAll(col);
            }
            while(result.next()){
                ObservableList<String> row = FXCollections.observableArrayList();
                for(int i=1 ; i<=result.getMetaData().getColumnCount(); i++)
                    row.add(result.getString(i));
                data.add(row);
            }
            tableview.setItems(data);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        rootNode.setCenter(tableview);
    }


    @Override
    public void stop(){
    }



}
