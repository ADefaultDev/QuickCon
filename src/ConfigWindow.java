import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

class ConfigWindow{
    private HBox subContainer;
    private VBox container;
    private Properties props;
    private TextField usernameField, urlField, pasField, databasesField;

    ConfigWindow(){

    }

    void createWindow(){
        Stage newWindow = new Stage();
        newWindow.setTitle("Configuration");

        Button button = new Button("OK");
        button.setOnAction(event -> newWindow.close());

        container = new VBox();
        subContainer = new HBox();
        container.setSpacing(15);
        container.setPadding(new Insets(25));
        container.setAlignment(Pos.CENTER);
        newWindow.setScene(new Scene(container));

        props = new Properties();
        try {
            try (InputStream in = Files.newInputStream(Paths.get(Objects.requireNonNull(this.getClass().getClassLoader().getResource("database.properties")).toURI()))) {
                props.load(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }

        createURLEditing();
        createDBEditing();
        createUsernameEditing();
        createPasswordEditing();
        container.getChildren().add(button);
        newWindow.show();

        button.setOnAction(actionEvent -> {
            System.out.println(Objects.requireNonNull(this.getClass().getClassLoader().getResource("database.properties")).toString());
            String filePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("database.properties")).toString();
            filePath = filePath.substring(5);
            try (PrintWriter out = new PrintWriter(new BufferedWriter(
                    new FileWriter(filePath, false)))) {
                out.println("jdbc.url=" + urlField.getText());
                out.println("jdbc.databases=" + databasesField.getText());
                out.println("jdbc.username=" + usernameField.getText());
                out.println("jdbc.password=" + pasField.getText());
                newWindow.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

        });

    }

    private void createURLEditing(){
        subContainer = new HBox();
        Label username = new Label("Username:  ");
        usernameField = new TextField(props.getProperty("jdbc.username"));
        subContainer.getChildren().addAll(username,usernameField);
        subContainer.setAlignment(Pos.CENTER_LEFT);
        container.getChildren().add(subContainer);
    }

    private void createDBEditing(){
        subContainer = new HBox();
        Label databases = new Label("Databases:  ");
        databasesField = new TextField(props.getProperty("jdbc.databases"));
        subContainer.getChildren().addAll(databases,databasesField);
        subContainer.setAlignment(Pos.CENTER_LEFT);
        container.getChildren().add(subContainer);
    }

    private void createUsernameEditing(){
        subContainer = new HBox();
        Label URL = new Label("URL:  ");
        urlField = new TextField(props.getProperty("jdbc.url"));
        subContainer.getChildren().addAll(URL, urlField);
        subContainer.setAlignment(Pos.CENTER_LEFT);
        container.getChildren().add(subContainer);
    }

    private void createPasswordEditing(){
        subContainer = new HBox();
        Label password = new Label("Password:  ");
        pasField = new TextField(props.getProperty("jdbc.password"));
        subContainer.getChildren().addAll(password,pasField);
        subContainer.setAlignment(Pos.CENTER_LEFT);
        container.getChildren().add(subContainer);

    }
}
