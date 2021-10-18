import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class QuickCon  extends Application {

    private static final String version = "0.1";
    private static final String title = "QuickCon " + version;
    private static final int width = 1440; // 960 // 1440
    private static final int height = 810; // 540 // 810

    private DatabaseManager databaseManager;

    private TreeView<String> treeView;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle(title);
        BorderPane rootNode = new BorderPane();

        databaseManager = new DatabaseManager();


        //Root
        TreeItem<String> root = new TreeItem<>();
        root.setExpanded(true);

        //DBMS
        TreeItem<String> DBMS = makeBranches("MySQL - @localhost", root);

        try(Statement stat = DatabaseManager.getConnection().createStatement(); ResultSet result = stat.executeQuery("SHOW DATABASES")) {
            while (result.next()) {
                TreeItem<String> item = makeBranches(result.getString(1), DBMS);
                TreeItem<String> tables = makeBranches("tables", item);
            }
        } catch (SQLException ex){
            ex.printStackTrace();
        }


        //Create tree
        treeView = new TreeView<>(root);
        treeView.setShowRoot(false);


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

    @Override
    public void stop(){
    }



}
