import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

class ConfigWindow{
    private HBox subContainer;
    private VBox container;
    private Properties props;

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
        createUsernameEditing();
        createPasswordEditing();
        container.getChildren().add(button);

        newWindow.show();
    }

    private void createURLEditing(){
        subContainer = new HBox();
        Label username = new Label("Username:  ");
        TextField usernameField = new TextField(props.getProperty("jdbc.username"));
        subContainer.getChildren().addAll(username,usernameField);
        container.getChildren().add(subContainer);
    }

    private void createUsernameEditing(){
        subContainer = new HBox();
        Label URL = new Label("URL:  ");
        TextField urlField = new TextField(props.getProperty("jdbc.url"));
        subContainer.getChildren().addAll(URL, urlField);
        container.getChildren().add(subContainer);
    }

    private void createPasswordEditing(){
        subContainer = new HBox();
        Label password = new Label("Password:  ");
        TextField pasField = new TextField(props.getProperty("jdbc.password"));
        subContainer.getChildren().addAll(password,pasField);
        container.getChildren().add(subContainer);

    }




}
