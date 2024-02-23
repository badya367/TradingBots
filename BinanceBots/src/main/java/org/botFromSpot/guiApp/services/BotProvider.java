package org.botFromSpot.guiApp.services;

import org.botFromSpot.guiApp.AppMainController;
import org.botFromSpot.guiApp.model.BinancePair;
import org.botFromSpot.guiApp.model.BinanceTokens;
import org.botFromSpot.guiApp.model.BotConfiguration;
import org.botFromSpot.guiApp.model.StrategyAveragingForSpot;

import java.util.ArrayList;
import java.util.List;

public class BotProvider {
    private AppMainController appMainController;
    private BinanceApiMethods binanceApiMethods;
    private BinancePairDAO binancePairDAO;
    public void setAppMainController(AppMainController appMainController) {this.appMainController = appMainController;}
    public void setBinanceApiMethods(BinanceApiMethods binanceApiMethods) {this.binanceApiMethods = binanceApiMethods;}
    public void setBinancePairDAO(BinancePairDAO binancePairDAO) {this.binancePairDAO = binancePairDAO;}
    private List<BotConfiguration> activeBots;

    public BotProvider() {
        this.activeBots = new ArrayList<>();
    }

    public void createBot(BotConfiguration botConfiguration){
        if(!activeBots.contains(botConfiguration)){
            BinancePair pair = botConfiguration.getPair();
            PairConfiguration pairConfiguration = botConfiguration.getConfiguration();
            BinanceTokens tokens = new BinanceTokens(appMainController.getApiKey(), appMainController.getSecretKey());
            double sumForTrading = pairConfiguration.getSumToTrade();
            double balanceAcc = binanceApiMethods.getAccountBalanceForTestNet(tokens);
            double reservedBalance = appMainController.getReservedBalance_var();

            if (balanceAcc - sumForTrading > 0 && balanceAcc-reservedBalance >= sumForTrading){
                if(binanceApiMethods.getOpenOrders(tokens, pair.getPairName())) {
                    System.out.println("Создаём бота");
                    appMainController.setReservedBalance_var(reservedBalance+sumForTrading);
                    appMainController.reservedBalance.setText(String.valueOf(appMainController.getReservedBalance_var()));

                    StrategyAveragingForSpot strategy = new StrategyAveragingForSpot(botConfiguration);
                    activeBots.add(botConfiguration);
                    strategy.start();
                } else {
                    throw new IllegalArgumentException("По данной паре существуют открытые сделки");
                }
            } else {
                throw new IllegalArgumentException("Недостаточно баланса для открытия пары");
            }
        }
        else {
            throw new IllegalArgumentException("Такой бот уже запущен");
        }
    }

    public void stopBot(BotConfiguration botConfiguration){
        for(BotConfiguration someConfig: activeBots){
            if(someConfig.equals(botConfiguration)){
                StrategyAveragingForSpot strategy = new StrategyAveragingForSpot(someConfig);
                strategy.stop();
                activeBots.remove(someConfig);
                BinancePair pair = botConfiguration.getPair();
                PairConfiguration pairConfiguration = botConfiguration.getConfiguration();
                double sumForTrading = pairConfiguration.getSumToTrade();
                double reservedBalance = appMainController.getReservedBalance_var();
                appMainController.setReservedBalance_var(reservedBalance-sumForTrading);
                appMainController.reservedBalance.setText(String.valueOf(appMainController.getReservedBalance_var()));
                return;
            }
            System.err.println("Такой бот не был запущен");
        }
    }
    //Принимает на вход конфигурацию (CreateAlgorithmRequest)
    //Проверяет что он единственный
    //createBot
    //Создать бота, остановить бота

}
