package org.botFromSpot.guiApp.services;

import org.botFromSpot.guiApp.model.BinanceBotConfiguration;

public class AppleConfigServiceImpl implements ApplyConfigService{

    @Override
    public void applyConfig(BinanceBotConfiguration binanceBotConfiguration) {

    }

    @Override
    public BinanceBotConfiguration loadConfig() {
        BinanceBotConfiguration testBotConfiguration = new BinanceBotConfiguration();
        testBotConfiguration.setTakeProfit(10);
        return testBotConfiguration;
    }
}
