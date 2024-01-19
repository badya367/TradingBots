package org.botFromSpot.guiApp;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class AppMainController {
    @FXML
    public TextField testTextOutput;
    @FXML
    public void initialize() {
        System.out.println(testTextOutput);
        testTextOutput.setText("Loaded");
    }

}
