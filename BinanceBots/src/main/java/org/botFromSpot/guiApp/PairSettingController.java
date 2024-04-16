package org.botFromSpot.guiApp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.botFromSpot.guiApp.model.BinancePair;
import org.botFromSpot.guiApp.model.BotConfiguration;
import org.botFromSpot.guiApp.services.BinanceApiMethods;
import org.botFromSpot.guiApp.services.BotProvider;
import org.botFromSpot.guiApp.services.PairConfiguration;
import org.botFromSpot.guiApp.services.BinancePairDAO;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class PairSettingController {
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

    private Stage stage;
    private boolean isUpdatingConfig = false;

    public void setUpdatingConfig(boolean updatingConfig) {isUpdatingConfig = updatingConfig;}

    public AppMainController appMainController;
    private BinanceApiMethods binanceApiMethods;
    private BinancePairDAO binancePairDAO;
    private BotProvider botProvider;
    public void setBinanceApiMethods(BinanceApiMethods binanceApiMethods) {this.binanceApiMethods = binanceApiMethods;}
    public void setBinancePairDAO(BinancePairDAO binancePairDAO) {this.binancePairDAO = binancePairDAO;}

    public void setAppMainController(AppMainController appMainController) {this.appMainController = appMainController;}

    public void setBotProvider(BotProvider botProvider) {this.botProvider = botProvider;}

    @FXML
    public void applySettingsButtonAction(ActionEvent event) {
        try {
            ListView<BinancePair> selectedPairsList = readSelectedPairsList();
            if(takeProfit.getText().isEmpty() ||
                    averagingStep.getText().isEmpty() ||
                    multiplier.getText().isEmpty() ||
                    quantityOrders.getText().isEmpty() ||
                    averagingTimer.getText().isEmpty() ||
                    sumToTrade.getText().isEmpty()) {
                throw new IllegalArgumentException("Missing input field");
            }
            if(Double.parseDouble(takeProfit.getText()) <= 0 ||
                    Double.parseDouble(averagingStep.getText()) <= 0 ||
                    Double.parseDouble(multiplier.getText()) < 1 ||
                    Double.parseDouble(quantityOrders.getText()) < 1 ||
                    Double.parseDouble(averagingTimer.getText()) < 0 ||
                    Double.parseDouble(sumToTrade.getText()) <= 0) {
                throw new IllegalArgumentException("The field has an incorrect value");
            }
            BinancePair selectedPair = selectedPairsList.getSelectionModel().getSelectedItem();
            BigDecimal bdStartinLotQuoteAsset = new BigDecimal(PairConfiguration.calculateStartingLotVolume(sumToTrade,multiplier,quantityOrders));
            bdStartinLotQuoteAsset = bdStartinLotQuoteAsset.setScale(
                    binanceApiMethods.getPrecisionSizeForTicker(selectedPair.getPairName(), false),
                    RoundingMode.HALF_UP);
            double checkStartingLotQuoteAsset = bdStartinLotQuoteAsset.doubleValue();
            if(checkStartingLotQuoteAsset < binanceApiMethods.getMinLotSizeForBuy(selectedPair.getPairName())) {
                throw new IllegalArgumentException("Объём стартового лота слишком мал для запуска бота, \n" +
                        "увеличьте объем для бота или уменьшите количество ордеров/множитель. \n" +
                        "Минимальный объём в сделке = " + binanceApiMethods.getMinLotSizeForBuy(selectedPair.getPairName()) +
                        "$");
            }

            PairConfiguration pairConfiguration = new PairConfiguration();
            System.out.println(selectedPairsList.getSelectionModel().getSelectedItem());

            pairConfiguration.setPairId(binancePairDAO.getPairIdByPairName(selectedPair.getPairName()));
            pairConfiguration.setTakeProfit(Double.parseDouble(takeProfit.getText()));
            pairConfiguration.setAveragingStep(Double.parseDouble(averagingStep.getText()));
            pairConfiguration.setMultiplier(Double.parseDouble(multiplier.getText()));
            pairConfiguration.setQuantityOrders(Integer.parseInt(quantityOrders.getText()));
            pairConfiguration.setAveragingTimer(Integer.parseInt(averagingTimer.getText()));
            pairConfiguration.setSumToTrade(Double.parseDouble(sumToTrade.getText()));
            pairConfiguration.setStartingLotVolume(checkStartingLotQuoteAsset);
            pairConfiguration.setTradingRange(PairConfiguration.calculateTradingRange(averagingStep, quantityOrders));

            DecimalFormat df = new DecimalFormat("#.###");
            startingLotVolume.setText(String.valueOf(checkStartingLotQuoteAsset));
            tradingRange.setText(String.valueOf(df.format(pairConfiguration.getTradingRange())));
            System.out.println(event);
            System.out.println(pairConfiguration);

            if (isUpdatingConfig) {
                // Вызывается из AppMainController, обновляем существующую запись
                pairConfiguration.setChanged(true);
                binancePairDAO.updateBotConfiguration(pairConfiguration);

                BotConfiguration botRequest = new BotConfiguration(selectedPair, pairConfiguration);
                botProvider.autoDryingBot(botRequest);
            } else {
                // Вызывается из LoadPairController, создаем новую запись
                pairConfiguration.setChanged(false);
                binancePairDAO.addBotConfiguration(pairConfiguration);
            }
            ((Stage) takeProfit.getScene().getWindow()).close();


        } catch (IllegalArgumentException e) {
            System.err.println("An error occurred after clicking the 'Apply' button: " + e.getMessage());
            // Показываем диалоговое окно с сообщением об ошибке
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка ввода");
            alert.setHeaderText("Пожалуйста, проверьте правильность введенных данных");
            alert.setContentText("Invalid values entered: " + e.getMessage());
            // Добавляем кнопку "OK" для закрытия диалогового окна
            alert.getButtonTypes().setAll(ButtonType.OK);
            // Отображаем диалоговое окно и ждем, пока пользователь его закроет
            alert.showAndWait();
        }
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
    public void fillFields(PairConfiguration botConfiguration){
        if (botConfiguration != null){
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
            DecimalFormat decimalFormat = new DecimalFormat("#.##########", symbols);

            takeProfit.setText(decimalFormat.format(botConfiguration.getTakeProfit()));
            averagingStep.setText(decimalFormat.format(botConfiguration.getAveragingStep()));
            multiplier.setText(String.valueOf(botConfiguration.getMultiplier()));
            quantityOrders.setText(String.valueOf(botConfiguration.getQuantityOrders()));
            averagingTimer.setText(String.valueOf(botConfiguration.getAveragingTimer()));
            sumToTrade.setText(String.valueOf(botConfiguration.getSumToTrade()));
            startingLotVolume.setText(decimalFormat.format(botConfiguration.getStartingLotVolume()));
            tradingRange.setText(decimalFormat.format(botConfiguration.getTradingRange()));
        }

    }
    private ListView<BinancePair> readSelectedPairsList(){
        return appMainController.getSelectedPairsList();
    }

}
