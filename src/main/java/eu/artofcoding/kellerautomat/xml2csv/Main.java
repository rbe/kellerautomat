package eu.artofcoding.kellerautomat.xml2csv;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL resource = getClass().getResource("KellerGUI.fxml");
        FXMLLoader loader = new FXMLLoader(resource);
        Parent root = loader.load();
        primaryStage.setTitle("KellerAutomat");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
