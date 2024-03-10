package org.botFromSpot.guiApp.services;

import org.botFromSpot.guiApp.AppMainController;
import org.botFromSpot.guiApp.model.BotConfiguration;
import org.botFromSpot.guiApp.model.StrategyAveragingForSpot;

public class StrategyAveragingFromSpotProvider {

    public StrategyAveragingForSpot getStrategyAveragingForSpot(BotConfiguration botConfiguration,
                                                                AppMainController appMainController,
                                                                BinancePairDAO binancePairDAO,
                                                                BinanceApiMethods binanceApiMethods) {
        //System.out.println("Проверка передачи бина: " + appMainController.balanceAcc.getText());
        return new StrategyAveragingForSpot(botConfiguration, appMainController, binancePairDAO, binanceApiMethods);
    }
    //
}
