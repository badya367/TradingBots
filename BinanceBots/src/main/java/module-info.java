module BinanceBots {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires org.json;
    requires spring.context;
    requires spring.core;
    opens org.botFromSpot.guiApp to javafx.fxml;
    exports org.botFromSpot.guiApp;
    opens org.botFromSpot.guiApp.services to spring.core;
    exports org.botFromSpot.guiApp.services;
}