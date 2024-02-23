package org.botFromSpot.guiApp;

import com.binance.connector.client.exceptions.BinanceClientException;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.botFromSpot.guiApp.model.BinancePair;
import org.botFromSpot.guiApp.model.BinanceTokens;
import org.botFromSpot.guiApp.model.BotConfiguration;
import org.botFromSpot.guiApp.services.*;
import org.botFromSpot.guiApp.utils.CryptoUtils;

import java.io.IOException;
import java.net.URL;
import java.util.List;



public class AppMainController {
    private Stage stage;
    private ContextMenu contextMenu;
    @FXML
    public TextField testTextOutput;
    //Переменные связанные с кнопкой "Загрузить валютную пару"
    @FXML
    public Button loadPairButton;
    @FXML
    public ListView<BinancePair> selectedPairsList;

    @FXML
    public Label balanceAcc;
    @FXML
    public Label reservedBalance;
    private double reservedBalance_var = 0;
    //--------------------------------------------------------
    //Переменные связанные с токенами биржи
    @FXML
    public Button ApplyTokensButton;
    @FXML
    public PasswordField apiKey;
    @FXML
    public PasswordField secretKey;
    //-------------------------------------------------------
    //Переменные связанные с Spring
    //private ApplyConfigService applyConfigService;
    private BinancePairDAO binancePairDAO;
    private BinanceApiMethods binanceApiMethods;
    private LoadPairController loadPairController;
    private PairSettingController pairSettingController;
    private BotProvider botProvider;

    //public void setApplyConfigService(ApplyConfigService applyConfigService) {this.applyConfigService = applyConfigService;}
    public void setBinancePairDAO(BinancePairDAO binancePairDAO) {
        this.binancePairDAO = binancePairDAO;
    }
    public void setBinanceApiMethods(BinanceApiMethods binanceApiMethods) {this.binanceApiMethods = binanceApiMethods;}

    public void setLoadPairController(LoadPairController loadPairController) {this.loadPairController = loadPairController;}

    public void setPairSettingController(PairSettingController pairSettingController) {this.pairSettingController = pairSettingController;}

    public void setBotProvider(BotProvider botProvider) {this.botProvider = botProvider;}

    //-----------------------------------------------------
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    @FXML
    public void initialize() {
        testTextOutput.setText("Loaded");
        balanceAcc.setText("-");

        ObservableList<BinancePair> items = selectedPairsList.getItems();
        List<BinancePair> allPairs = binancePairDAO.getAllPairs();
        for (BinancePair pair : allPairs) {
            items.add(pair);
        }

        BinanceTokens tokens = binancePairDAO.getTokens();
        if(tokens != null){
            System.out.println("Api Key = " + tokens.getApiKey() + "\nSecret key = " + tokens.getSecretKey());
            apiKey.setText(tokens.getApiKey());
            secretKey.setText(tokens.getSecretKey());
            balanceAcc.setText(String.valueOf(binanceApiMethods.getAccountBalanceForTestNet(tokens)));
        }
    }

    @FXML
    public void loadPairButtonAction(ActionEvent event) {
        // Новое окно после нажатия на кнопку
        try {
            if(apiKey.getText().isEmpty() || secretKey.getText().isEmpty()){
                throw new IllegalArgumentException("Пустые значения ключей");
            }
            pairSettingController.setUpdatingConfig(false);
            FXMLLoader loader = new FXMLLoader();
            URL xmlUrl = getClass().getResource("/loadPairWindow.fxml");
            loader.setController(loadPairController);
            loader.setLocation(xmlUrl);
            Parent root = loader.load();
            Stage loadPair = new Stage();
            loadPair.setTitle("Загрузка валютной пары");
            loadPair.setScene(new Scene(root));
            loadPair.show();
        } catch (IllegalArgumentException e){
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка запроса");
            alert.setHeaderText("Пожалуйста укажите Api и Secret Key");
            alert.setContentText("Invalid values entered: " + e.getMessage());
            alert.getButtonTypes().setAll(ButtonType.OK);
            alert.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void listViewMouseClicked(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            showContextMenu(event);
        } else if (event.getButton() == MouseButton.PRIMARY) {
            // Скрываем контекстное меню при нажатии левой кнопки мыши
            if (contextMenu != null) {
                contextMenu.hide();
                contextMenu = null;
            }
        }
    }
    private void showContextMenu(MouseEvent event) {
        BinancePair selectedPair = selectedPairsList.getSelectionModel().getSelectedItem();

        // только одно открытие contextMenu
        if (selectedPair != null) {
            if (contextMenu == null) {
                contextMenu = new ContextMenu();
                MenuItem startBot = new MenuItem("Запустить");
                startBot.setOnAction(e -> startBotAction());

                MenuItem editItem = new MenuItem("Изменить");
                editItem.setOnAction(e -> editItemAction());

                MenuItem deleteItem = new MenuItem("Удалить");
                deleteItem.setOnAction(e -> deleteItemAction());

                contextMenu.getItems().addAll(startBot, editItem, deleteItem);

                // Добавляем слушатель для скрытия меню
                contextMenu.setOnHidden(e -> contextMenu = null);
            }

            contextMenu.show(selectedPairsList, event.getScreenX(), event.getScreenY());
        }
    }
    @FXML
    public void startBotAction() {
        BinancePair pair = binancePairDAO.getBinancePairByPairName(selectedPairsList
                .getSelectionModel()
                .getSelectedItem()
                .getPairName());

        PairConfiguration pairConfig = binancePairDAO.getConfigurationForPair(pair.getId());

        BotConfiguration botRequest = new BotConfiguration(pair,pairConfig);
        //BotProvider botProvider = new BotProvider();
        try {
            botProvider.createBot(botRequest);
            botProvider.stopBot(botRequest);
        } catch (IllegalArgumentException e) {
            System.out.println("В блоке catch");
            e.getMessage();
        }




    }
    @FXML
    public void editItemAction() {
        PairConfiguration editPair = binancePairDAO
                .getConfigurationForPair(binancePairDAO
                .getPairIdByPairName(selectedPairsList
                        .getSelectionModel()
                        .getSelectedItem()
                        .getPairName()));

        try {
            FXMLLoader loader = new FXMLLoader();
            URL xmlUrl = getClass().getResource("/pairSettingWindow.fxml");
            loader.setController(pairSettingController);
            loader.setLocation(xmlUrl);
            Parent root = loader.load();
            Stage loadPair = new Stage();
            loadPair.setTitle("Настройки валютной пары");
            loadPair.setScene(new Scene(root));
            loadPair.show();
            pairSettingController.setUpdatingConfig(true);
            pairSettingController.fillFields(editPair);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    @FXML
    public void deleteItemAction() {
        // Получение выбранной пары
        BinancePair selectedPair = selectedPairsList.getSelectionModel().getSelectedItem();

        if (selectedPair == null) {
            return;
        }
        // Создание всплывающего окна с подтверждением
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Подтверждение удаления");
        confirmationAlert.setHeaderText(null);
        confirmationAlert.setContentText("Вы уверены, что хотите удалить пару " + selectedPair.getPairName() + "?");

        confirmationAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        ButtonType userResponse = confirmationAlert.showAndWait().orElse(ButtonType.NO);


        if (userResponse == ButtonType.YES) {

            int pairId = binancePairDAO.getPairIdByPairName(selectedPair.getPairName());

            binancePairDAO.deleteBotConfiguration(pairId);
            binancePairDAO.deletePair(pairId);

            selectedPairsList.getItems().remove(selectedPair);
        }
    }
    @FXML
    public void emptyFieldsForConfig() {

    }
    @FXML
    public void applyTokensBtnAction(ActionEvent event) {
        System.out.println(event + ": Кнопка подтверждения токенов");
        try {
            String encryptedApiKey = CryptoUtils.encrypt(apiKey.getText());
            String encryptedSecretKey = CryptoUtils.encrypt(secretKey.getText());

            binancePairDAO.saveTokens(encryptedApiKey,encryptedSecretKey);
            BinanceTokens tokens = new BinanceTokens(getApiKey(), getSecretKey());
            System.out.println("Api Key = " + tokens.getApiKey() + "\nSecret key = " + tokens.getSecretKey()); //Потом нужно будет убрать эту строчку
            binanceApiMethods.connectBinance(tokens);
            balanceAcc.setText(String.valueOf(binanceApiMethods.getAccountBalanceForTestNet(tokens)));

        } catch (IllegalArgumentException | BinanceClientException e) {
            System.err.println("An error occurred after clicking the 'Confirm' button:" + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка ввода");
            alert.setHeaderText("Пожалуйста, проверьте правильность введенных данных");
            alert.getButtonTypes().setAll(ButtonType.OK);
            alert.showAndWait();
        }

    }
    public void updateSelectedPairsList(BinancePair newPair) {
        selectedPairsList.getItems().add(newPair);
    }

    public ListView<BinancePair> getSelectedPairsList() {
        return selectedPairsList;
    }

    public void selectPairInPairsList(BinancePair pairForSelect){
        MultipleSelectionModel<BinancePair> selectionModel = selectedPairsList.getSelectionModel();
        selectionModel.select(pairForSelect);
    }

    public double getReservedBalance_var() {
        return reservedBalance_var;
    }

    public void setReservedBalance_var(double reservedBalance_var) {
        this.reservedBalance_var = reservedBalance_var;
    }

    public String getApiKey() {
        return String.valueOf(apiKey.getText());
    }

    public String getSecretKey() {
        return String.valueOf(secretKey.getText());
    }
}
