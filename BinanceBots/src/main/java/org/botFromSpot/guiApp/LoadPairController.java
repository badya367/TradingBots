package org.botFromSpot.guiApp;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.botFromSpot.guiApp.model.BinancePair;
import org.botFromSpot.guiApp.services.BinanceApiMethods;
import org.botFromSpot.guiApp.services.BinancePairDAO;

import java.io.IOException;
import java.net.URL;

public class LoadPairController{

    @FXML
    private ComboBox<String> pairsComboBox;
    @FXML
    private Button goToSettingsPairBtn;
    private ListView<BinancePair> selectedPairsList;
    private BinanceApiMethods binanceApiMethods;
    private BinancePairDAO binancePairDAO;
    private PairSettingController pairSettingController;
    private AppMainController appMainController;
    public void setBinanceApiMethods(BinanceApiMethods binanceApiMethods) {this.binanceApiMethods = binanceApiMethods;}
    public void setBinancePairDAO(BinancePairDAO binancePairDAO) {this.binancePairDAO = binancePairDAO;}
    public void setPairSettingController(PairSettingController pairSettingController) {this.pairSettingController = pairSettingController;}
    public void setAppMainController(AppMainController appMainController) {this.appMainController = appMainController;}

    @FXML
    public void initialize() {

        ComboBox<String> pairsComboBoxFromMethod = binanceApiMethods.allPairs();
        ObservableList<String> allPairs = pairsComboBoxFromMethod.getItems();
        pairsComboBox.getItems().addAll(allPairs);

    }
    @FXML
    public void selectSettingPair(ActionEvent event) {
        try {
            actionWithSelectedPair();

            createSceneWithSettingsPair();

            ((Stage) pairsComboBox.getScene().getWindow()).close();
            // закрываем окно
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void actionWithSelectedPair(){
        String selectedPair = pairsComboBox.getValue();
        BinancePair binancePair = new BinancePair();
        binancePair.setPairName(selectedPair);
        binancePairDAO.addPair(binancePair);
        appMainController.updateSelectedPairsList(binancePair);
        appMainController.selectPairInPairsList(binancePair);
    }
    private void createSceneWithSettingsPair() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        URL xmlUrl = getClass().getResource("/pairSettingWindow.fxml");
        loader.setController(pairSettingController);
        loader.setLocation(xmlUrl);
        Parent root = loader.load();
        Stage loadPair = new Stage();
        loadPair.setTitle("Настройки валютной пары");
        loadPair.setScene(new Scene(root));
        loadPair.show();

    }
}
