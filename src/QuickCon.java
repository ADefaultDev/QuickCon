import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
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

    private static final String version = "0.3";
    private static final String title = "QuickCon " + version;
    private static final int width = 1440; // 960 // 1440
    private static final int height = 810; // 540 // 810

    private Stage stage;
    private BorderPane rootNode;
    private VBox vBoxLeft, vBoxCenter, vBoxRight, vBoxTop, vBoxBottom;

    private DatabaseManager databaseManager;
    private TreeView<String> treeView;
    private TableView tableView;

    private String tableN;
    private ArrayList<String> tableNames;
    private ArrayList<String> columnNames;

    private TreeMap<String, TreeMap<String, String>> dataForQueries;
    private ArrayList<String> queries;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage none) {
        stage = none;
        stage.setTitle(title);
        databaseManager = new DatabaseManager("");

        rootNode = new BorderPane();
        vBoxLeft = new VBox();
        vBoxCenter = new VBox();
        vBoxTop = new VBox();

        getDatabasesAndTables();
        createToolBar();
        createTree();
        createMenuBar();

        rootNode.setLeft(vBoxLeft);
        rootNode.setTop(vBoxTop);

        Scene myScene = new Scene(rootNode, width, height);
        stage.setScene(myScene);
        stage.show();
    }

    private void createTable(String tableName){
        vBoxCenter.getChildren().clear();
        createButtons();
        columnNames = new ArrayList<>();
        dataForQueries = new TreeMap<>();
        queries = new ArrayList<>();
        tableView = new TableView<String>();

        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tableView.setEditable(true);
        tableView.setMinHeight(height);


        ObservableList<ObservableList> data = FXCollections.observableArrayList();
        try (Statement stat = DatabaseManager.getConnection().createStatement(); ResultSet result = stat.executeQuery("SELECT * FROM " + tableName)) {
            for(int i = 0 ; i < result.getMetaData().getColumnCount(); i++){
                final int j = i;
                TableColumn col = new TableColumn(result.getMetaData().getColumnName(1 + i));
                columnNames.add(result.getMetaData().getColumnName(1 + i));
                col.setCellValueFactory((Callback<CellDataFeatures<ObservableList, String>, ObservableValue<String>>) param ->
                        new SimpleStringProperty(param.getValue().get(j).toString()));
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
                tableView.getColumns().add(col);
            }
            while(result.next()){
                ObservableList<String> row = FXCollections.observableArrayList();
                for(int i = 1 ; i <= result.getMetaData().getColumnCount(); i++) {
                    row.add(result.getString(i));
                }
                data.add(row);
            }

            tableView.setItems(data);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        vBoxCenter.getChildren().add(tableView);
        rootNode.setCenter(vBoxCenter);
    }

    private void getDatabasesAndTables() {
        //get names of databases and their tables

        tableNames = new ArrayList<>();
        try(Statement stat = DatabaseManager.getConnection().createStatement(); ResultSet result = stat.executeQuery("SHOW TABLES")) {
            while (result.next()) tableNames.add(result.getString(1));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void createMenuBar() {
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem exitItem = new MenuItem("Exit");
        fileMenu.getItems().add(exitItem);
        fileMenu.setOnAction(actionEvent -> Platform.exit());

        Menu editMenu = new Menu("Edit");

        menuBar.getMenus().addAll(fileMenu, editMenu);

        vBoxTop.getChildren().addAll(menuBar);
    }

    private void createTree() {
        //Root tree
        TreeItem<String> root = new TreeItem<>();
        //DBMS next tree
        TreeItem<String> DBMS = makeBranch("MySQL - @localhost", root);
        DBMS.setExpanded(true);
        //Create tree for databases and their tables
        String[] allDB = databaseManager.getAllDatabases();
        for (String db:
                allDB) {
            TreeItem<String> database = makeBranch(db, DBMS);
            database.setExpanded(true);
            String currentDatabase = database.getValue();
            if (currentDatabase.equals(db) && databaseManager.getDbName().equals(db)) {
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
        treeView.setMinHeight(height);
        treeView.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getClickCount() == 2)
            {
                updateTree();
            }
        });

        vBoxLeft.getChildren().add(treeView);
    }

    private void updateTree(){
        TreeItem<String> item = treeView.getSelectionModel().getSelectedItem();
        try {
            if (item.getValue() != null) {
                //If item == table, show table
                if (item.getParent().getValue().equals("tables")) {
                    tableN = item.getValue();
                    createTable(tableN);
                }
                //If item == Database, show table
                else if (item.getParent().getValue().equals("MySQL - @localhost")) {
                    System.out.println("Selected database:" + item.getValue());
                    try {
                        DatabaseManager.getConnection().close();
                    }
                    catch (SQLException ignored){}
                    databaseManager = new DatabaseManager(item.getValue());
                    getDatabasesAndTables();
                    vBoxLeft.getChildren().clear();
                    vBoxCenter.getChildren().clear();
                    createToolBar();
                    createTree();
                }
            }
        }catch (NullPointerException ignored){ }
    }

    private void createButtons() {
        HBox buttonBox = new HBox();

        Button reload = makeButton("Reload", true, "Reload Page", 0.5f);
        reload.setOnAction(actionEvent -> {
            reloadTable();
        });

        Separator separator = new Separator(Orientation.VERTICAL);

        Button add = makeButton("+", true, "Add Row", 0.5f);
        add.setOnAction(actionEvent -> {
            addRow();
        });

        Button delete = makeButton("-", true, "Delete Row", 0.5f);
        delete.setOnAction(actionEvent -> {
            deleteRow();
        });

        Button submit = makeButton("Submit", true, "Submit", 0.5f);
        submit.setOnAction(actionEvent -> {
            submitChanges();
        });

        buttonBox.getChildren().addAll(reload, separator, add, delete, submit);
        buttonBox.setSpacing(30);
        vBoxCenter.getChildren().add(buttonBox);
    }

    private void createToolBar() {
        MenuButton newSys = new MenuButton("+");
        Tooltip tooltip = new Tooltip("New connections to DBMS");
        tooltip.setShowDelay(Duration.seconds(0.5f));
        newSys.setTooltip(tooltip);

        Menu menu = new Menu("Data Source");

        MenuItem mySQL = new MenuItem("MySQL");
        mySQL.setOnAction(event -> {
            Stage newWindow = new Stage();
            newWindow.setTitle("Data Source");

            Label labelHost = new Label("Host:");
            TextField textFieldHost = new TextField();
            textFieldHost.setMinWidth(300);

            Region p = new Region();
            p.setPrefSize(500, 0.0);

            Label labelPort = new Label("Port:");
            TextField textFieldPort = new TextField();
            textFieldPort.setMinWidth(300);

            FlowPane flowPane = new FlowPane();
            flowPane.setHgap(40);
            FlowPane.setMargin(labelHost, new Insets(10, 0, 10, 20));
            FlowPane.setMargin(labelPort, new Insets(10, 0, 10, 20));
            flowPane.getChildren().addAll(labelHost, textFieldHost, p, labelPort, textFieldPort);

            TabPane tabPane = new TabPane();
            tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            Tab tab1 = new Tab("General");
            tabPane.getTabs().addAll(tab1);

            VBox vBox = new VBox();
            vBox.getChildren().addAll(flowPane, tabPane);

            Scene secondScene = new Scene(vBox, 960, 540);
            newWindow.initOwner(stage);
            newWindow.setScene(secondScene);
            newWindow.show();
        });
        MenuItem postgreSQL = new MenuItem("PostgreSQL");

        menu.getItems().addAll(mySQL, postgreSQL);
        newSys.getItems().addAll(menu);

        ToolBar toolBar = new ToolBar();
        toolBar.getItems().addAll(newSys);
        vBoxLeft.getChildren().add(toolBar);
    }

    private void submitChanges() {
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

    private void addRow() {
        ObservableList<String> row = FXCollections.observableArrayList();
        row.addAll(columnNames);
        tableView.getItems().add(row);
        String insertQ = "INSERT INTO wp_terms (name, slug, term_group) VALUES ('test1', 'test2', '5');";
        queries.add(insertQ);

    }

    private void deleteRow() {
//        try {
//            DatabaseMetaData metaData = DatabaseManager.getConnection().getMetaData();
//            ResultSet rs = metaData.getPrimaryKeys(databaseManager.getDatabase(), null, tableN);
//            while (rs.next()){
//                System.out.println("Column name: " + rs.getString("COLUMN_NAME"));
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }

        ObservableList selectedItems = tableView.getSelectionModel().getSelectedItems();
        for (Object selectedItem : selectedItems) {
            ObservableList selectedIts = (ObservableList) selectedItem;
            tableView.setStyle("-fx-selection-bar-non-focused: salmon;");
            String deleteQ = "DELETE FROM " + tableN + " WHERE " + columnNames.get(0) + "='" + selectedIts.get(0) + "'";
            queries.add(deleteQ);
        }
    }

    private void reloadTable() {
        vBoxLeft.getChildren().clear();
        vBoxCenter.getChildren().clear();
        databaseManager.reconnection();
        getDatabasesAndTables();
        createToolBar();
        createTree();
        createTable(tableN);
    }

    private TreeItem<String> makeBranch(String title, TreeItem<String> parent) {
        TreeItem<String> item = new TreeItem<>(title);
        item.setExpanded(false);
        parent.getChildren().add(item);
        return item;
    }

    private Button makeButton(String nameButton, boolean isTooltip, String nameTooltip, float tooltipDuration) {
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
