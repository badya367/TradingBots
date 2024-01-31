package org.botFromSpot.guiApp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.botFromSpot.guiApp.model.BinanceBotConfiguration;
import org.botFromSpot.guiApp.model.BinancePair;
import org.botFromSpot.guiApp.model.BinanceTokens;
import org.botFromSpot.guiApp.services.BinanceApiMethods;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class AppMainController {
    @FXML
    public Button loadPairButton;
    @FXML
    public TextField testTextOutput;
    @FXML
    public Button applySettingsButton;
    @FXML
    public Button saveConfigBtn;
    @FXML
    public Button loadConfigBtn;
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
    public Button ApplyTokensButton;
    @FXML
    public TextField apiKey;
    @FXML
    public TextField secretKey;

    private Stage stage;

    public void setStage(Stage stage) {this.stage = stage;}

    @FXML
    public void initialize() {
        testTextOutput.setText("Loaded");
    }
    @FXML
    public void loadPairButtonAction(ActionEvent event) {
        // Новое окно после нажатия на кнопку
        Stage stage = new Stage();
        stage.setTitle("Выберите торговую пару");
        // ComboBox для списка торговых пар
        ComboBox<String> pairComboBox = BinanceApiMethods.allPairs();
        // Здесь нужно добавить логику для загрузки всех возможных торговых пар с биржи Binance
//        pairComboBox.getItems().add("BTCUSDT");
//        pairComboBox.getItems().add("ETCUSDT");
//        pairComboBox.getItems().add("SOLUSDT");

        // Кнопка для подтверждения выбора
        Button confirmButton = new Button("Подтвердить");
        confirmButton.setOnAction(e -> {
            // Получаем выбранную торговую пару
            String selectedPair = pairComboBox.getValue();
            BinancePair pair = new BinancePair();
            pair.setPairName(selectedPair);

            // закрываем окно
            stage.close();
        });
        // VBox для компоновки элементов управления
        VBox vbox = new VBox(pairComboBox, confirmButton);
        // Создаем сцену и устанавливаем ее для окна
        Scene scene = new Scene(vbox, 300, 150);
        stage.setScene(scene);
        // Показываем окно
        stage.show();
    }
    @FXML
    public void applySettingsButtonAction(ActionEvent event) {
        try {
            startingLotVolume.setText(String.valueOf(BinanceBotConfiguration.calculateStartingLotVolume(sumToTrade, multiplier, quantityOrders)));
            tradingRange.setText(String.valueOf(BinanceBotConfiguration.calculateTradingRange(averagingStep, quantityOrders)));
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
            binanceBotConfiguration.setStartingLotVolume(BinanceBotConfiguration.calculateStartingLotVolume(sumToTrade, multiplier, quantityOrders));
            binanceBotConfiguration.setTradingRange(BinanceBotConfiguration.calculateTradingRange(averagingStep, quantityOrders));
        } catch (RuntimeException e) {
            System.err.println("Произошла ошибка после нажатия кнопки 'Применить': " + e.getMessage());
            // Показываем диалоговое окно с сообщением об ошибке
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка ввода");
            alert.setHeaderText("Пожалуйста, проверьте правильность введенных данных");
            // Добавляем кнопку "OK" для закрытия диалогового окна
            alert.getButtonTypes().setAll(ButtonType.OK);
            // Отображаем диалоговое окно и ждем, пока пользователь его закроет
            alert.showAndWait();
        }
    }
    @FXML
    public void emptyFieldsForConfig() {

    }
    @FXML
    public void saveConfigBtnAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select directory for save");
        fileChooser.setInitialFileName("configForPair-");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("txt", "*.txt"));
        File file = fileChooser.showSaveDialog(stage);
        System.out.println("ПОКА ЧТО ЗАГЛУШКА: " + event); //Файл не создается
    }
    @FXML
    public void loadConfigBtnAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);
        System.out.println("ПОКА ЧТО ЗАГЛУШКА: " + event);  //Файл не открывается
    }

    @FXML
    public void applyTokensBtnAction(ActionEvent event) {
        System.out.println(event + ": Кнопка подтверждения токенов");
        try {
            BinanceTokens tokens = new BinanceTokens(apiKey.getText(), secretKey.getText());

            System.out.println("Api Key = " + tokens.getApiKey() + "\nSecret key = " + tokens.getSecretKey()); //Потом нужно будет убрать эту строчку
            BinanceApiMethods.connectBinance(tokens);
        } catch (IllegalArgumentException e) {
            System.err.println("Произошла ошибка после нажатия кнопки 'Подтвердить': " + e.getMessage());
            // Показываем диалоговое окно с сообщением об ошибке
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка ввода");
            alert.setHeaderText("Пожалуйста, проверьте правильность введенных данных");
            // Добавляем кнопку "OK" для закрытия диалогового окна
            alert.getButtonTypes().setAll(ButtonType.OK);
            // Отображаем диалоговое окно и ждем, пока пользователь его закроет
            alert.showAndWait();
        }

    }






}
