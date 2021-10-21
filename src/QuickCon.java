import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.control.TableColumn.CellEditEvent;

import java.sql.*;
import java.util.ArrayList;
import java.util.TreeMap;


public class QuickCon  extends Application {

    private static final String version = "0.1";
    private static final String title = "QuickCon " + version;
    private static final int width = 1440; // 960 // 1440
    private static final int height = 810; // 540 // 810

    private BorderPane rootNode;
    private DatabaseManager databaseManager;
    private TreeView<String> treeView;
    private TableView tableview;

    private String tableN;
    private ArrayList<String> databaseNames, tableNames;
    private ArrayList<String> columnNames;

    private TreeMap<String, TreeMap<String, String>> dataForQueries;
    private ArrayList<String> queries = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle(title);

        rootNode = new BorderPane();
        databaseManager = new DatabaseManager();

        getDatabasesAndTables();
        createTree();
        createButtons();

        Scene myScene = new Scene(rootNode, width, height);
        stage.setScene(myScene);
        stage.show();
    }

    public void getDatabasesAndTables() {
        //get names of databases and their tables
        databaseNames = new ArrayList<>();
        tableNames = new ArrayList<>();
        try(Statement stat = DatabaseManager.getConnection().createStatement(); ResultSet result = stat.executeQuery("SHOW DATABASES")) {
            while (result.next()) databaseNames.add(result.getString(1));
        } catch (SQLException ex){
            ex.printStackTrace();
        }
        try(Statement stat = DatabaseManager.getConnection().createStatement(); ResultSet result = stat.executeQuery("SHOW TABLES")) {
            while (result.next()) tableNames.add(result.getString(1));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void createTree() {
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
                    if (item.getParent().getValue().equals("tables")) {
                        tableN = item.getValue();
                        createTable(tableN);
                    }
            }
        });
        rootNode.setLeft(treeView);
    }

    public void createTable(String tableName){
        tableview = new TableView();
        columnNames = new ArrayList<>();
        dataForQueries = new TreeMap<>();

        ObservableList<ObservableList> data = FXCollections.observableArrayList();
        try (Statement stat = DatabaseManager.getConnection().createStatement(); ResultSet result = stat.executeQuery("SELECT * FROM " + tableName)) {
            for(int i = 0 ; i < result.getMetaData().getColumnCount(); i++){
                final int j = i;
                TableColumn col = new TableColumn(result.getMetaData().getColumnName(1 + i));
                columnNames.add(result.getMetaData().getColumnName(1 + i));
                col.setCellValueFactory((Callback<CellDataFeatures<ObservableList, String>, ObservableValue<String>>) param -> {
                    return new SimpleStringProperty(param.getValue().get(j).toString());
                });
                //Accept changes in the table and overwrites them
                col.setCellFactory(TextFieldTableCell.forTableColumn());
                col.setOnEditCommit((EventHandler<CellEditEvent<ObservableList, String>>) t -> {
                    String oldValue = t.getOldValue();
                    String newValue = t.getNewValue();
                    if (!oldValue.equals(newValue)) {
                        int column = t.getTablePosition().getColumn();
                        int row = t.getTablePosition().getRow();
                        t.getTableView().getItems().get(row).set(column, newValue);

                        String key = (String) t.getTableView().getItems().get(row).get(0);
                        TreeMap<String, String> argument;
                        if (dataForQueries.containsKey(key)) {
                            argument = dataForQueries.get(key);
                        } else {
                            argument = new TreeMap<>();
                        }
                        argument.put(columnNames.get(column), newValue);
                        dataForQueries.put(key, argument);
                    }
                });
                col.setSortable(false);
                tableview.getColumns().add(col);
            }
            while(result.next()){
                ObservableList<String> row = FXCollections.observableArrayList();
                for(int i=1 ; i <= result.getMetaData().getColumnCount(); i++) {
                    row.add(result.getString(i));
                }
                data.add(row);
            }
            tableview.setItems(data);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        tableview.setEditable(true);
        rootNode.setCenter(tableview);
    }

    public void createButtons() {
        HBox buttonBox = new HBox();

        Button reload = new Button("Reload");
        reload.setOnAction(actionEvent -> {
            reloadTable();
        });

        Button submit = new Button("Submit");
        submit.setOnAction(actionEvent -> {
            submitChanges();
        });

        buttonBox.getChildren().addAll(reload, submit);
        buttonBox.setSpacing(50);
        buttonBox.setAlignment(Pos.CENTER);
        rootNode.setTop(buttonBox);
    }


    public TreeItem<String> makeBranches(String title, TreeItem<String> parent) {
        TreeItem<String> item = new TreeItem<>(title);
        item.setExpanded(false);
        parent.getChildren().add(item);
        return item;
    }

    public void submitChanges() {
        try(Statement statement = DatabaseManager.getConnection().createStatement()) {
            //create a request and add it to the array
            String start = "UPDATE " + tableN + " SET ";
            for (String key: dataForQueries.keySet()) {
                StringBuilder arg = new StringBuilder();
                arg.append(start);
                for (String key2: dataForQueries.get(key).keySet()) {
                    arg.append(key2).append("=").append(dataForQueries.get(key).get(key2));
                    if (!dataForQueries.get(key).lastKey().equals(key2)) {
                        arg.append(", ");
                    }
                }
                arg.append(" WHERE ").append(columnNames.get(0)).append("=").append(key);
                queries.add(String.valueOf(arg));
            }
            System.out.println(queries);

            for (String query: queries) {
                int count = statement.executeUpdate(query);
                System.out.println("Updated queries: "+count);
            }
//            String updateQ = "UPDATE wp_terms SET name='r1r', slug='z1z', term_group=5 WHERE term_id=2;";
//            String insertQ = "INSERT INTO wp_terms (name, slug, term_group) VALUES ('khm', 'khm', 0);";
//            int count = statement.executeUpdate(insertQ);
//            System.out.println("Updated queries: " + count);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

//        System.out.println(dataForQueries);

    }

    public void reloadTable() {
        databaseManager.reconnection();
        getDatabasesAndTables();
        createTree();
        createTable(tableN);
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
