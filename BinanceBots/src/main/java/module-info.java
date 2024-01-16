module BinanceBots {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.botFromSpot.guiApp to javafx.fxml;
    exports org.botFromSpot.guiApp;
}