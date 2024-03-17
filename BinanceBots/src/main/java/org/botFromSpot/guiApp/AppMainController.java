package org.botFromSpot.guiApp;

import com.binance.connector.client.exceptions.BinanceClientException;
import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.botFromSpot.guiApp.model.*;
import org.botFromSpot.guiApp.services.*;
import org.botFromSpot.guiApp.utils.Constants;
import org.botFromSpot.guiApp.utils.CryptoUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class AppMainController {
    private Stage stage;
    private ContextMenu contextMenu;
    private ScheduledExecutorService scheduledExecutorService;
    @FXML
    private AnchorPane rootAnchorPane;
    @FXML
    public TextField testTextOutput;
    //Переменные связанные с таблицей запущенных пар
    @FXML
    public TableView<PairPriceInfo> cashTable;
    @FXML
    public TableColumn<PairPriceInfo,String> pairNamesColumn;
    @FXML
    public TableColumn<PairPriceInfo, Double> pairPriceColumn;
    @FXML
    public TableColumn<PairPriceInfo,Double> pairProfitColumn;
    public ObservableList<PairPriceInfo> pairPriceInfoObservableList = FXCollections.observableArrayList();

    //Переменные связанные с кнопкой "Загрузить валютную пару"
    @FXML
    private Button loadPairButton;
    @FXML
    public ListView<BinancePair> selectedPairsList;
    //Переменные связанные с балансом
    @FXML
    public Label balanceAcc;
    @FXML
    public Label reservedBalance;
    private double reservedBalance_var = 0;
    //--------------------------------------------------------
    //Переменные связанные с токенами биржи
    @FXML
    private Button ApplyTokensButton;
    @FXML
    private PasswordField apiKey;
    @FXML
    private PasswordField secretKey;
    @FXML
    private RadioButton binanceRadioButton;

    @FXML
    private RadioButton testNetBinanceRadioButton;

    @FXML
    private RadioButton byBitRadioButton;
    private ToggleGroup toggleGroup;
    public String selectedStock;
    //--------------------------------------------------------
    //Переменные связанные с вкладкой информация
    @FXML
    private ListView<String> informationListView;
    //-------------------------------------------------------
    //Переменные связанные с Spring
    //private ApplyConfigService applyConfigService;
    private BinancePairDAO binancePairDAO;
    private BinanceApiMethods binanceApiMethods;
    private LoadPairController loadPairController;
    private PairSettingController pairSettingController;
    private BotProvider botProvider;

    private StrategyAveragingFromSpotProvider strategyProvider;

    public void setBinancePairDAO(BinancePairDAO binancePairDAO) {
        this.binancePairDAO = binancePairDAO;
    }
    public void setBinanceApiMethods(BinanceApiMethods binanceApiMethods) {this.binanceApiMethods = binanceApiMethods;}

    public void setLoadPairController(LoadPairController loadPairController) {this.loadPairController = loadPairController;}

    public void setPairSettingController(PairSettingController pairSettingController) {this.pairSettingController = pairSettingController;}

    public void setBotProvider(BotProvider botProvider) {this.botProvider = botProvider;}

    public void setStrategyProvider(StrategyAveragingFromSpotProvider strategyProvider) {this.strategyProvider = strategyProvider;}

    //-----------------------------------------------------
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    @FXML
    private void initialize() {
        informationListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        //informationListView.setStyle("-fx-control-inner-background: darkgray; -fx-base: darkgray;");
        toggleGroup = new ToggleGroup();
        binanceRadioButton.setToggleGroup(toggleGroup);
        testNetBinanceRadioButton.setToggleGroup(toggleGroup);
        byBitRadioButton.setToggleGroup(toggleGroup);

        testTextOutput.setText("Loaded");
        balanceAcc.setText("-");
        reservedBalance.setText("-");
        addLogEntry("Приложение запущено");
        //Добавляем пары из базы данных
        ObservableList<BinancePair> items = selectedPairsList.getItems();
        List<BinancePair> allPairs = binancePairDAO.getAllPairs();
        for (BinancePair pair : allPairs) {
            items.add(pair);
        }
        selectedPairsList.setStyle("-fx-control-inner-background: darkgray; -fx-base: darkgray;");

        //Записываем токены биржи из базы
        BinanceTokens tokens = binancePairDAO.getTokens();
        if(tokens != null){
            addLogEntry("Api Key = " + tokens.getApiKey() + "\nSecret key = " + tokens.getSecretKey());
            //System.out.println("Api Key = " + tokens.getApiKey() + "\nSecret key = " + tokens.getSecretKey());

            apiKey.setText(tokens.getApiKey());
            secretKey.setText(tokens.getSecretKey());
            selectedStock = binancePairDAO.getStock(CryptoUtils.encrypt(apiKey.getText()));
            if(selectedStock.equals("TestNetBinance")){
                testNetBinanceRadioButton.fire();
                BinanceApiMethods.setBaseURL(Constants.TESTNET_BINANCE_BASE_URL);
                balanceAcc.setText(String.valueOf(binanceApiMethods.getAccountBalanceForTestNet(tokens)));
            }

        }
        // Добавляем фабрики для столбцов в таблице cashTable
        pairNamesColumn.setCellValueFactory(new PropertyValueFactory<>("symbol"));
        pairPriceColumn.setCellValueFactory(new PropertyValueFactory<>("bidPrice"));
        pairProfitColumn.setCellValueFactory(new PropertyValueFactory<>("profit"));

        cashTable.setItems(pairPriceInfoObservableList);

        //Создаём поток для работы стратегии
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(this::updatePricesInBackground, 0, Constants.API_UPDATE_TIMEOUT, TimeUnit.MILLISECONDS);

    }

    @FXML
    private void loadPairButtonAction(ActionEvent event) {
        // Новое окно после нажатия на кнопку "Загрузить валютную пару
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

    //Методы для работы после нажатия правой кнопкой мыши в списке торговых пар
    private void showContextMenu(MouseEvent event) {
        BinancePair selectedPair = selectedPairsList.getSelectionModel().getSelectedItem();

        // только одно открытие contextMenu
        if (selectedPair != null) {
            if (contextMenu == null) {

                contextMenu = new ContextMenu();
                if(!containsRunningPair(selectedPair.getPairName())){ // Если бот не запущен
                    MenuItem startBot = new MenuItem("Запустить");
                    startBot.setOnAction(e -> startBotAction());
                    contextMenu.getItems().add(startBot);

                }
                else { //Если бот запущен
                    MenuItem autoDrying = new MenuItem("Автосушка");
                    autoDrying.setOnAction(e -> autoDryingAction());
                    contextMenu.getItems().add(autoDrying);

                    MenuItem stopBot = new MenuItem("Остановить");
                    stopBot.setOnAction(e-> stopBotAction());
                    contextMenu.getItems().add(stopBot);
                }


                MenuItem editItem = new MenuItem("Изменить");
                editItem.setOnAction(e -> editItemAction());

                MenuItem deleteItem = new MenuItem("Удалить");
                deleteItem.setOnAction(e -> deleteItemAction());

                contextMenu.getItems().addAll(editItem, deleteItem);
                // Добавляем слушатель для скрытия меню
                contextMenu.setOnHidden(e -> {
                    //selectedPairsList.getSelectionModel().clearSelection();
                    contextMenu = null;
                });

                //Ниже просто код для быстрых действий с аккаунтом по клику правой кнопки мыши (НЕ ЛОГИКА БОТА)
                //String walletBalance = binanceApiMethods.getWalletInfoForTestNet(binancePairDAO.getTokens());
                //System.out.println(walletBalance);
                //binanceApiMethods.closeOrder(binancePairDAO.getTokens(), "ETHUSDT", 1.00790000);
            }

            contextMenu.show(selectedPairsList, event.getScreenX(), event.getScreenY());
        }
    }
    @FXML
    private void listViewMouseClicked(MouseEvent event) {

        if (event.getButton() == MouseButton.SECONDARY && contextMenu == null) {
            // Если элемент выбран, открываем контекстное меню
            if (selectedPairsList.getSelectionModel().getSelectedItem() != null) {
                showContextMenu(event);
            }

        } else if (event.getButton() == MouseButton.PRIMARY) {
            // Скрываем контекстное меню при нажатии левой кнопки мыши
            if (contextMenu != null) {
                selectedPairsList.getSelectionModel().clearSelection();
                contextMenu.hide();
                contextMenu = null;
            }
        } else if (contextMenu != null) {
            contextMenu.hide();
            contextMenu = null;
        }
    }
    @FXML
    private void startBotAction() {
        BinancePair pair = binancePairDAO.getBinancePairByPairName(selectedPairsList
                .getSelectionModel()
                .getSelectedItem()
                .getPairName());
        PairConfiguration pairConfig = binancePairDAO.getConfigurationForPair(pair.getId());

        BotConfiguration botRequest = new BotConfiguration(pair,pairConfig);

        //BotProvider botProvider = new BotProvider();
        try {
            botProvider.createBot(botRequest);
            //botProvider.stopBot(botRequest);
        } catch (IllegalArgumentException e) {
            Alert informationAlert = new Alert(Alert.AlertType.WARNING);
            informationAlert.setTitle("Недостаточный баланс");
            informationAlert.setHeaderText("Вашего свободного баланса недостаточно для запуска этой пары");
            informationAlert.showAndWait();
            e.printStackTrace();
        }
    }
    @FXML
    private void autoDryingAction() {
        System.out.println("Здесь включается автосушка");
        BinancePair selectedPair = selectedPairsList.getSelectionModel().getSelectedItem();
        System.out.println("selectedPair = " + selectedPair);
        System.out.println("selectedPairID = " + selectedPair.getId());
        if (selectedPair == null || binancePairDAO.getAutoDryingInTradeInfoForPair(selectedPair.getId())) {
            return;
        }
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Подтверждение включения автосушки");
        confirmationAlert.setHeaderText(null);
        confirmationAlert.setContentText("Вы уверены, что хотите включить автосушку для пары " + selectedPair.getPairName() + "? \n" +
                "Открытые сделки по этой паре будут закрыты по тейк-профиту и бот закончит работу с этой парой");

        confirmationAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        ButtonType userResponse = confirmationAlert.showAndWait().orElse(ButtonType.NO);


        if (userResponse == ButtonType.YES) {

            PairConfiguration pairConfig = binancePairDAO.getConfigurationForPair(binancePairDAO.getPairIdByPairName(selectedPair.getPairName()));
            System.out.println("В методе autoDryingAction AppMainController: " + pairConfig);
            BotConfiguration botRequest = new BotConfiguration(selectedPair, pairConfig);
            botProvider.autoDryingBot(botRequest);

        }

    }
    @FXML
    private void stopBotAction() {
        BinancePair selectedPair = selectedPairsList.getSelectionModel().getSelectedItem();

        if (selectedPair == null) {
            return;
        }
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Подтверждение удаления");
        confirmationAlert.setHeaderText(null);
        confirmationAlert.setContentText("Вы уверены, что хотите остановить пару " + selectedPair.getPairName() + "? \n" +
                "Открытые сделки по этой паре будут принудительно закрыты по текущей цене");

        confirmationAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        ButtonType userResponse = confirmationAlert.showAndWait().orElse(ButtonType.NO);


        if (userResponse == ButtonType.YES) {

            PairConfiguration pairConfig = binancePairDAO.getConfigurationForPair(binancePairDAO.getPairIdByPairName(selectedPair.getPairName()));
            BotConfiguration botRequest = new BotConfiguration(selectedPair, pairConfig);
            botProvider.stopBot(botRequest);
        }



    }
    @FXML
    private void editItemAction() {
        BinancePair pair = binancePairDAO.getBinancePairByPairName(selectedPairsList
                .getSelectionModel()
                .getSelectedItem()
                .getPairName());
        PairConfiguration editPair = binancePairDAO.getConfigurationForPair(pair.getId());
        System.out.println(editPair);

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
    private void deleteItemAction() {
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
    //Метод для кнопки подтверждения токенов
    @FXML
    private void applyTokensBtnAction(ActionEvent event) {
        System.out.println(event + ": Кнопка подтверждения токенов");
        try {
            String encryptedApiKey = CryptoUtils.encrypt(apiKey.getText());
            String encryptedSecretKey = CryptoUtils.encrypt(secretKey.getText());
            if(selectedStock != null){
                binancePairDAO.saveTokens(encryptedApiKey,encryptedSecretKey, selectedStock);
            }
            else {
                throw new IllegalArgumentException("Не выбрана биржа");
            }

            BinanceTokens tokens = new BinanceTokens(getApiKey(), getSecretKey());
            System.out.println("Api Key = " + tokens.getApiKey() + "\nSecret key = " + tokens.getSecretKey()); //Потом нужно будет убрать эту строчку
            if (selectedStock.equals("TestNetBinance")){
                balanceAcc.setText(String.valueOf(binanceApiMethods.getAccountBalanceForTestNet(tokens)));
            }


        } catch (IllegalArgumentException | BinanceClientException e) {
            System.err.println("An error occurred after clicking the 'Confirm' button:" + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка ввода");
            alert.setHeaderText("Пожалуйста, проверьте правильность введенных данных, а также выбрана ли биржа");
            alert.getButtonTypes().setAll(ButtonType.OK);
            alert.showAndWait();
        }

    }
    //Метод для выбора биржи
    @FXML
    private void handleRadioButtonAction() {
        List<String> activePairs = botProvider.getPairNameInActiveBots();
        if (activePairs.isEmpty()){
            if (binanceRadioButton.isSelected()) {
                selectedStock = "Binance";
                BinanceApiMethods.setBaseURL(Constants.BINANCE_BASE_URL);
                // Действия при выборе Binance
            } else if (testNetBinanceRadioButton.isSelected()) {
                selectedStock = "TestNetBinance";
                BinanceApiMethods.setBaseURL(Constants.TESTNET_BINANCE_BASE_URL);

            } else if (byBitRadioButton.isSelected()) {
                // Действия при выборе ByBit
                selectedStock = "ByBit";
                BinanceApiMethods.setBaseURL(Constants.BYBIT_BASE_URL);
            }
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Невозможно поменять биржу");
            alert.setHeaderText("Невозможно поменять биржу, если в работе находится хотя бы одна пара");
            alert.getButtonTypes().setAll(ButtonType.OK);
            alert.showAndWait();
        }

    }
    //Методы для работы с потоком стратегии.
    private void updatePricesInBackground() {
        // Получение списка активных торговых пар из BotProvider
        List<String> activePairs = botProvider.getPairNameInActiveBots();

        if(!activePairs.isEmpty()){

            // Вызов метода для получения актуальных цен из BinanceApiMethods
            List<PairPriceInfo> pairsPriceInfoList = binanceApiMethods.getActualPriceForPairs(binancePairDAO.getTokens(), activePairs);

            //binanceApiMethods.getAvgBuyPrice(binancePairDAO.getTokens(),pairsPriceInfoList);
            for (PairPriceInfo pairPriceInfo : pairsPriceInfoList){

                BinancePair tradingPair = binancePairDAO.getBinancePairByPairName(pairPriceInfo.getSymbol());
                PairConfiguration tradingPairConfig = binancePairDAO.getConfigurationForPair(tradingPair.getId());
                BotConfiguration botConfig = new BotConfiguration(tradingPair,tradingPairConfig);


                StrategyAveragingForSpot averagingSpotStrategy = strategyProvider.getStrategyAveragingForSpot(
                        botConfig,
                        pairSettingController.appMainController, //Костыль надо поправлять
                        binancePairDAO,
                        binanceApiMethods);
                averagingSpotStrategy.setBotProvider(botProvider);
                //averagingSpotStrategy.setBinancePairDAO(binancePairDAO);
                //averagingSpotStrategy.setBinanceApiMethods(binanceApiMethods);

                updateOrAddRow(pairPriceInfo);

                averagingSpotStrategy.update(pairPriceInfo);

            }
        }

        stage.setOnHidden(event -> stopBackgroundTasks());
    }

    private void stopBackgroundTasks() {
        if (scheduledExecutorService != null && !scheduledExecutorService.isShutdown()) {
            scheduledExecutorService.shutdown();
        }
    }

    // Метод для добавления или обновления информации в таблице
    private void updateOrAddRow(PairPriceInfo pairPriceInfo) {
        String pairName = pairPriceInfo.getSymbol();
        double bidPrice = pairPriceInfo.getBidPrice();

        //double profit = calculateProfit(pairPriceInfo); // Рассчитываем профит (замените на ваш расчет)

        // Проверяем, есть ли уже такая торговая пара в таблице
        boolean isPairExists = false;
        for (PairPriceInfo existingPair : pairPriceInfoObservableList) {
            if (existingPair.getSymbol().equals(pairName)) {
                if(existingPair.getBidPrice() != pairPriceInfo.getBidPrice() ||
                existingPair.getProfit() != pairPriceInfo.getProfit()){
                    // Обновляем существующую строку
                    existingPair.setBidPrice(bidPrice);
                    existingPair.setProfit(binancePairDAO.getProfitInTradeInfoForPair(binancePairDAO.getPairIdByPairName(existingPair.getSymbol())));
                    cashTable.refresh();

                }
                isPairExists = true;
                break;
            }
        }
        List<String> activeBotsList = botProvider.getPairNameInActiveBots();
        // Если нет, то добавляем новую строку
        if (!isPairExists && activeBotsList.stream().anyMatch(pair -> pair.equals(pairName))) {
            pairPriceInfo.setProfit(binancePairDAO.getProfitInTradeInfoForPair(binancePairDAO.getPairIdByPairName(pairPriceInfo.getSymbol())));
            pairPriceInfoObservableList.add(pairPriceInfo);
            cashTable.setStyle("-fx-control-inner-background: darkgray; -fx-base: darkgray;");
        }
    }

    // ***** ВРЕМЕННАЯ ЗАГЛУШКА ***** Метод для расчета профита
    private double calculateProfit(PairPriceInfo pairPriceInfo) {
        TradeInfo pairTradeInfo = binancePairDAO.getTradeInfoForPair(binancePairDAO.getPairIdByPairName(pairPriceInfo.getSymbol()));

        BigDecimal bdProfit = BigDecimal.valueOf((pairPriceInfo.getBidPrice() * pairTradeInfo.getLotSize()) -
                (pairTradeInfo.getBuyPrice()*pairTradeInfo.getLotSize()));
        bdProfit = bdProfit.setScale(
                5,
                RoundingMode.HALF_UP);
        return bdProfit.doubleValue();
    }
    //----------------------Служебные публичные методы-----------------------------
    public void updateSelectedPairsList(BinancePair newPair) {
        selectedPairsList.getItems().add(newPair);
        selectedPairsList.setStyle("-fx-control-inner-background: darkgray; -fx-base: darkgray;");
    }

    public ListView<BinancePair> getSelectedPairsList() {
        return selectedPairsList;
    }

    public void selectPairInPairsList(BinancePair pairForSelect){
        MultipleSelectionModel<BinancePair> selectionModel = selectedPairsList.getSelectionModel();
        selectionModel.select(pairForSelect);
    }
    public void removeRunningPair(BinancePair pair) {
        pairPriceInfoObservableList.removeIf(pairRemove -> pairRemove.getSymbol().equals(pair.getPairName()));
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

    // Метод для добавления новой записи в лог и Separator после нее
    public void addLogEntry(String entryText) {
        informationListView.getItems().add(entryText);
        informationListView.setStyle("-fx-control-inner-background: darkgray; -fx-base: darkgray;");
    }
    //----------------------Служебные приватные методы-----------------------------
    private boolean containsRunningPair(String pairName) {
        return pairPriceInfoObservableList.stream()
                .map(PairPriceInfo::getSymbol) // Преобразование в поток названий пар
                .anyMatch(pair -> pair.equals(pairName));
    }


}
