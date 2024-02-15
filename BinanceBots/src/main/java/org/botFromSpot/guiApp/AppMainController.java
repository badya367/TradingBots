package org.botFromSpot.guiApp;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.botFromSpot.guiApp.model.BinancePair;
import org.botFromSpot.guiApp.model.BinanceTokens;
import org.botFromSpot.guiApp.services.*;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Collections;
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

    //--------------------------------------------------------
    //Переменные связанные с токенами биржи
    @FXML
    public Button ApplyTokensButton;
    @FXML
    public TextField apiKey;
    @FXML
    public TextField secretKey;
    //-------------------------------------------------------
    //Переменные связанные с Spring
    //private ApplyConfigService applyConfigService;
    private BinancePairDAO binancePairDAO;
    private BinanceApiMethods binanceApiMethods;
    private LoadPairController loadPairController;
    private PairSettingController pairSettingController;
    //public void setApplyConfigService(ApplyConfigService applyConfigService) {this.applyConfigService = applyConfigService;}
    public void setBinancePairDAO(BinancePairDAO binancePairDAO) {
        this.binancePairDAO = binancePairDAO;
    }
    public void setBinanceApiMethods(BinanceApiMethods binanceApiMethods) {this.binanceApiMethods = binanceApiMethods;}

    public void setLoadPairController(LoadPairController loadPairController) {this.loadPairController = loadPairController;}

    public void setPairSettingController(PairSettingController pairSettingController) {this.pairSettingController = pairSettingController;}

    //-----------------------------------------------------
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    @FXML
    public void initialize() {
        testTextOutput.setText("Loaded");
        ObservableList<BinancePair> items = selectedPairsList.getItems();
        List<BinancePair> allPairs = binancePairDAO.getAllPairs();
        for (BinancePair pair : allPairs) {
            items.add(pair);
        }
    }

    @FXML
    public void loadPairButtonAction(ActionEvent event) {
        // Новое окно после нажатия на кнопку
        try {
            FXMLLoader loader = new FXMLLoader();
            URL xmlUrl = getClass().getResource("/loadPairWindow.fxml");
            loader.setController(loadPairController);
            loader.setLocation(xmlUrl);
            Parent root = loader.load();
            Stage loadPair = new Stage();
            loadPair.setTitle("Загрузка валютной пары");
            loadPair.setScene(new Scene(root));
            loadPair.show();
        } catch (Exception e) {
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

                MenuItem editItem = new MenuItem("Изменить");
                editItem.setOnAction(e -> editItemAction());

                MenuItem deleteItem = new MenuItem("Удалить");
                deleteItem.setOnAction(e -> deleteItemAction());

                contextMenu.getItems().addAll(editItem, deleteItem);

                // Добавляем слушатель для скрытия меню
                contextMenu.setOnHidden(e -> contextMenu = null);
            }

            contextMenu.show(selectedPairsList, event.getScreenX(), event.getScreenY());
        }
    }
    @FXML
    public void editItemAction() {
        BinanceBotConfiguration editPair = binancePairDAO
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
        binancePairDAO.deleteBotConfiguration(binancePairDAO
                .getPairIdByPairName(selectedPairsList
                        .getSelectionModel()
                        .getSelectedItem()
                        .getPairName()));
        binancePairDAO.deletePair(binancePairDAO
                .getPairIdByPairName(selectedPairsList
                        .getSelectionModel()
                        .getSelectedItem()
                        .getPairName()));
        selectedPairsList.getItems().remove(selectedPairsList.getSelectionModel().getSelectedItem());
    }
    @FXML
    public void emptyFieldsForConfig() {

    }
    @FXML
    public void applyTokensBtnAction(ActionEvent event) {
        System.out.println(event + ": Кнопка подтверждения токенов");
        try {
            BinanceTokens tokens = new BinanceTokens(apiKey.getText(), secretKey.getText());
            System.out.println("Api Key = " + tokens.getApiKey() + "\nSecret key = " + tokens.getSecretKey()); //Потом нужно будет убрать эту строчку
            binanceApiMethods.connectBinance(tokens);
        } catch (IllegalArgumentException e) {
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
}
