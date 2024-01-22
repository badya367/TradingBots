package org.botFromSpot.guiApp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.botFromSpot.guiApp.model.BinanceBotConfiguration;

public class AppMainController {
    @FXML
    public TextField testTextOutput;
    @FXML
    public Button applySettingsButton;
    @FXML
    public TextField takeProfit;
    @FXML
    public TextField averagingStep;
    @FXML
    public TextField multiplier;
    @FXML
    public TextField quantityOrders;
    @FXML
    public TextField averagingTimer;
    @FXML
    public TextField sumToTrade;
    @FXML
    public Label startingLotVolume;
    @FXML
    public Label tradingRange;


    @FXML
    public void initialize() {
        testTextOutput.setText("Loaded");
    }

    @FXML
    public void applySettingsButtonAction(ActionEvent event) {
        startingLotVolume.setText(String.valueOf(calculateStartingLotVolume(sumToTrade,multiplier,quantityOrders)));
        tradingRange.setText(String.valueOf(calculateTradingRange(averagingStep,quantityOrders)));

        System.out.println(event);
        System.out.println(takeProfit + ". Значение: " + takeProfit.getText());
        System.out.println(averagingStep + ". Значение: " + averagingStep.getText());
        System.out.println(multiplier + ". Значение: " + multiplier.getText());
        System.out.println(quantityOrders + ". Значение: " + quantityOrders.getText());
        System.out.println(averagingTimer + ". Значение: " + averagingTimer.getText());
        System.out.println(sumToTrade + ". Значение: " + sumToTrade.getText());
        System.out.println(startingLotVolume + ". Значение: " + startingLotVolume.getText());
        System.out.println(tradingRange + ". Значение: " + tradingRange.getText());




        BinanceBotConfiguration binanceBotConfiguration = new BinanceBotConfiguration();

        binanceBotConfiguration.setTakeProfit(Double.parseDouble(takeProfit.getText()));
        binanceBotConfiguration.setAveragingStep(Double.parseDouble(averagingStep.getText()));
        binanceBotConfiguration.setMultiplier(Double.parseDouble(multiplier.getText()));
        binanceBotConfiguration.setQuantityOrders(Integer.parseInt(quantityOrders.getText()));
        binanceBotConfiguration.setAveragingTimer(Integer.parseInt(averagingTimer.getText()));
        binanceBotConfiguration.setSumToTrade(Double.parseDouble(sumToTrade.getText()));
        binanceBotConfiguration.setStartingLotVolume(calculateStartingLotVolume(sumToTrade,multiplier,quantityOrders));
        binanceBotConfiguration.setTradingRange(calculateTradingRange(averagingStep,quantityOrders));

    }


    public static double calculateStartingLotVolume(TextField sumToTrade, TextField multiplier, TextField quantityOrders){
        double sum = Double.parseDouble(sumToTrade.getText());
        double multi = Double.parseDouble(multiplier.getText());
        double quantity = Double.parseDouble(quantityOrders.getText());
        double startingLotVolume;
        for (int i = 0; i < quantity; i++){
            sum = sum/multi;
        }
        startingLotVolume = sum;
        return startingLotVolume;
    }

    public static double calculateTradingRange(TextField averagingStep, TextField quantityOrders){
        return Double.parseDouble(averagingStep.getText()) * Integer.parseInt(quantityOrders.getText());
    }

}
