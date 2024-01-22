package org.botFromSpot.guiApp.services;

import org.botFromSpot.guiApp.model.BinanceBotConfiguration;

public interface ApplyConfigService {
    void applyConfig(BinanceBotConfiguration binanceBotConfiguration);
    BinanceBotConfiguration loadConfig();
    BinanceBotConfiguration loadDefaultConfig();
}
