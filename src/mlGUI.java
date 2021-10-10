import javafx.application.*;
import javafx.scene.*;
import javafx.stage.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.event.*;

class JFXDemo extends Application {

    Label response;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage myStage) {
        myStage.setTitle("JavaFX Skeleton");
        BorderPane rootNode = new BorderPane();

        Scene myScene = new Scene(rootNode,300,400);
        myStage.setScene(myScene);

        response = new Label("Push a button");

        MenuBar mb = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem open = new MenuItem("Open");
        MenuItem close = new MenuItem("Close");
        MenuItem save = new MenuItem ("Save");
        MenuItem exit = new MenuItem ("Exit");
        fileMenu.getItems().addAll(open,close,save,new SeparatorMenuItem(),exit);
        mb.getMenus().add(fileMenu);
        Menu optionsMenu = new Menu("Options");

        Menu colorsMenu = new Menu("Colors");
        MenuItem red = new MenuItem("Red");
        MenuItem green = new MenuItem("Green");
        MenuItem blue = new MenuItem("Blue");

        colorsMenu.getItems().addAll(red,green,blue);
        optionsMenu.getItems().add(colorsMenu);

        Menu priorityMenu = new Menu("Priority");
        MenuItem high = new MenuItem("High");
        MenuItem low = new MenuItem("Low");
        priorityMenu.getItems().addAll(high, low);
        optionsMenu.getItems().add(priorityMenu);

        optionsMenu.getItems().add(new SeparatorMenuItem());

        MenuItem reset = new MenuItem("Reset");
        optionsMenu.getItems().add(reset);

        mb.getMenus().add(optionsMenu);

        Menu helpMenu = new Menu("Help");
        MenuItem about = new MenuItem("About");
        helpMenu.getItems().add(about);

        mb.getMenus().add(helpMenu);

        EventHandler<ActionEvent> MEHandler =
                actionEvent -> {
                    String name = ((MenuItem)actionEvent.getTarget()).getText();
                    if(name.equals("Exit")) Platform.exit();
                    response.setText(name + " selected");
                };
        Button but1 = new Button("Eded");

        open.setOnAction(MEHandler);
        close.setOnAction(MEHandler);
        save.setOnAction(MEHandler);
        exit.setOnAction(MEHandler);
        red.setOnAction(MEHandler);
        green.setOnAction(MEHandler);
        blue.setOnAction(MEHandler);
        high.setOnAction(MEHandler);
        low.setOnAction(MEHandler);
        reset.setOnAction(MEHandler);
        about.setOnAction(MEHandler);

        rootNode.setTop(mb);
        rootNode.setCenter(response);
        rootNode.setBottom(but1);

        myStage.show();
    }
}
