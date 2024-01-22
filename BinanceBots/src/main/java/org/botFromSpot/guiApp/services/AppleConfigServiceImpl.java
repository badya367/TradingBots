package org.botFromSpot.guiApp.services;

import org.botFromSpot.guiApp.model.BinanceBotConfiguration;

public class AppleConfigServiceImpl implements ApplyConfigService{

    @Override
    public void applyConfig(BinanceBotConfiguration binanceBotConfiguration) {

    }

    @Override
    public BinanceBotConfiguration loadConfig() {
        return null;
    }

    @Override
    public BinanceBotConfiguration loadDefaultConfig() {
        BinanceBotConfiguration testBotConfiguration = new BinanceBotConfiguration();
        testBotConfiguration.setTakeProfit(3);
        testBotConfiguration.setAveragingStep(1);
        testBotConfiguration.setMultiplier(2);
        testBotConfiguration.setQuantityOrders(5);
        testBotConfiguration.setAveragingTimer(1);
        testBotConfiguration.setSumToTrade(100);
        return testBotConfiguration;

    }
}
