package org.botFromSpot.guiApp.services;

public interface ApplyConfigService {
    void applyConfig(BinanceBotConfiguration binanceBotConfiguration);
    BinanceBotConfiguration loadConfig();
    BinanceBotConfiguration loadDefaultConfig();
}
