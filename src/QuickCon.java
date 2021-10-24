import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.util.Callback;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.util.Duration;

import java.sql.*;
import java.util.ArrayList;
import java.util.TreeMap;

public class QuickCon extends Application {

    private static final String version = "0.2";
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
    private ArrayList<String> queries;
    private ArrayList<Integer> deletedIndex;

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

    public void createTable(String tableName){
        tableview = new TableView();
        columnNames = new ArrayList<>();
        dataForQueries = new TreeMap<>();
        queries = new ArrayList<>();
        deletedIndex = new ArrayList<>();

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

        tableview.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableview.setEditable(true);
        rootNode.setCenter(tableview);
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
        TreeItem<String> DBMS = makeBranch("MySQL - @localhost", root);
        DBMS.setExpanded(true);
        //Create tree for databases and their tables
        for (String nameDatabase: databaseNames) {
            TreeItem<String> database = makeBranch(nameDatabase, DBMS);
            database.setExpanded(true);
            String currentDatabase = database.getValue();
            if (currentDatabase.toString().equals(databaseManager.getDatabase())) {
                TreeItem<String> nodeTable = makeBranch("tables", database); // should be one block higher
                nodeTable.setExpanded(true);
                for (String nameTable: tableNames) {
                    TreeItem<String> tableDB = makeBranch(nameTable, nodeTable);
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

    public void createButtons() {
        HBox buttonBox = new HBox();

        Button reload = makeButton("Reload", true, "Reload Page", 0.5f);
        reload.setOnAction(actionEvent -> {
            reloadTable();
        });

        Button delete = makeButton("-", true, "Delete Row", 0.5f);
        delete.setOnAction(actionEvent -> {
            deleteRow();
        });

        Button submit = makeButton("Submit", true, "Submit", 0.5f);
        submit.setOnAction(actionEvent -> {
            submitChanges();
        });

        buttonBox.getChildren().addAll(reload, delete, submit);
        buttonBox.setSpacing(50);
        buttonBox.setAlignment(Pos.CENTER);
        rootNode.setTop(buttonBox);
    }

    public void submitChanges() {
        try(Statement statement = DatabaseManager.getConnection().createStatement()) {
//            String updateQ = "UPDATE wp_terms SET name='r1r', slug='z1z', term_group=5 WHERE term_id=2;";
//            String insertQ = "INSERT INTO wp_terms (name, slug, term_group) VALUES ('test1', 'test2', '5');";

//            create a UPDATE request and add it to the array
            String start = "UPDATE " + tableN + " SET ";
            for (String key: dataForQueries.keySet()) {
                StringBuilder arg = new StringBuilder();
                arg.append(start);
                for (String key2: dataForQueries.get(key).keySet()) {
                    arg.append(key2).append("=").append("'").append(dataForQueries.get(key).get(key2)).append("'");
                    if (!dataForQueries.get(key).lastKey().equals(key2)) {
                        arg.append(", ");
                    }
                }
                arg.append(" WHERE ").append(columnNames.get(0)).append("=").append("'").append(key).append("'");
                queries.add(String.valueOf(arg));
            }
//            System.out.println(dataForQueries);
            dataForQueries.clear();

            for (String query: queries) {
                int count = statement.executeUpdate(query);
                if (count > 0) {
                    System.out.println("Successful query: " + query);
                }
            }
            queries.clear();

            DatabaseManager.getConnection().commit();
            createTable(tableN);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void deleteRow() {
//        try {
//            DatabaseMetaData metaData = DatabaseManager.getConnection().getMetaData();
//            ResultSet rs = metaData.getPrimaryKeys(databaseManager.getDatabase(), null, tableN);
//            while (rs.next()){
//                System.out.println("Column name: " + rs.getString("COLUMN_NAME"));
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }

        ObservableList o = (ObservableList) tableview.getSelectionModel().getSelectedItem();
        int indexRow = tableview.getSelectionModel().getSelectedIndex();
        if (o != null && !deletedIndex.contains(indexRow)) {
            tableview.setStyle("-fx-selection-bar-non-focused: salmon;");
            String deleteQ = "DELETE FROM " + tableN + " WHERE " + columnNames.get(0) + "='" + o.get(0) + "'";
            queries.add(deleteQ);
        }
        deletedIndex.add(indexRow);
    }

    public void reloadTable() {
        databaseManager.reconnection();
        getDatabasesAndTables();
        createTree();
        createTable(tableN);
    }

    public TreeItem<String> makeBranch(String title, TreeItem<String> parent) {
        TreeItem<String> item = new TreeItem<>(title);
        item.setExpanded(false);
        parent.getChildren().add(item);
        return item;
    }

    public Button makeButton(String nameButton, boolean isTooltip, String nameTooltip, float tooltipDuration) {
        Button button = new Button(nameButton);
        if (isTooltip) {
            Tooltip tooltip = new Tooltip(nameTooltip);
            tooltip.setShowDelay(Duration.seconds(tooltipDuration));
            button.setTooltip(tooltip);
        }
        return button;
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
