package org.botFromSpot.guiApp.services;

public interface ApplyConfigService {
    void applyConfig(PairConfiguration pairConfiguration);
    PairConfiguration loadConfig();
    PairConfiguration loadDefaultConfig();
}
