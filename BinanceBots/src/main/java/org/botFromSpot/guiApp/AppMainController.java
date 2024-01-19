package org.botFromSpot.guiApp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.botFromSpot.guiApp.model.BinanceBotConfiguration;

public class AppMainController {
    @FXML
    public TextField testTextOutput;
    @FXML
    public Button applyButton;
    @FXML
    public TextField takeProfit;

    @FXML
    public void initialize() {
        System.out.println(testTextOutput);
        testTextOutput.setText("Loaded");
    }

    @FXML
    public void applyButtonAction(ActionEvent event) {
        System.out.println(event);
        System.out.println(takeProfit);
        BinanceBotConfiguration binanceBotConfiguration = new BinanceBotConfiguration();
        binanceBotConfiguration.setTakeProfit(Double.valueOf(takeProfit.getText()));
    }

}
