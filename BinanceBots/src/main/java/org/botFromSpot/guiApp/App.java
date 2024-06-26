package org.botFromSpot.guiApp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.support.GenericXmlApplicationContext;

import java.io.IOException;
import java.net.URL;

public class App extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        GenericXmlApplicationContext genericXmlApplicationContext =
                new GenericXmlApplicationContext("classpath:applicationContext.xml");
        FXMLLoader loader = new FXMLLoader();
        AppMainController appMainController = genericXmlApplicationContext.getBean(AppMainController.class,
                "appMainController");
        loader.setController(appMainController);
        appMainController.setStage(primaryStage);
        URL xmlUrl = getClass().getResource("/app.fxml");
        //System.out.println(xmlUrl);
        loader.setLocation(xmlUrl);
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Got AS God");
        primaryStage.show();

    }
}
