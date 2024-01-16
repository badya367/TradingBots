package org.botFromSpot.guiApp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class App extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Label label = new Label("ЗАРАБОТАЛО!!!");
        Scene scene = new Scene(label, 300, 200);

        primaryStage.setTitle("BBFS");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
