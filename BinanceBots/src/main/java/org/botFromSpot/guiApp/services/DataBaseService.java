package org.botFromSpot.guiApp.services;

import org.botFromSpot.guiApp.model.BinancePair;

import java.sql.Connection;
import java.util.List;

public interface DataBaseService {
    Connection connectionDB();

    void createDB();

    void insertBotConfiguration(BinanceBotConfiguration botConfiguration);

    List<BinanceBotConfiguration> readAllConfiguration();

    BinanceBotConfiguration readConfigurationForPair(int pairId);

    void insertPair(String pairName);

    List<BinancePair> readAllPairs();

    int getPairIdByPairName(String pairName);

    void closeDB();

}
