module BinanceBots {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    opens org.botFromSpot.guiApp to javafx.fxml;
    exports org.botFromSpot.guiApp;

}