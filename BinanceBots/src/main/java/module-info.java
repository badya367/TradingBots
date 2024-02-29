module BinanceBots {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires org.json;
    requires spring.context;
    requires spring.core;
    requires binance.connector.java;
    requires org.slf4j;
    opens org.botFromSpot.guiApp to javafx.fxml;
    exports org.botFromSpot.guiApp;
    opens org.botFromSpot.guiApp.services to spring.core;
    exports org.botFromSpot.guiApp.services;
    exports org.botFromSpot.guiApp.services.binanceTestNetServices;
    opens org.botFromSpot.guiApp.services.binanceTestNetServices to spring.core;
    opens org.botFromSpot.guiApp.model to javafx.base;
}