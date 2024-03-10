package org.botFromSpot.guiApp.services;

import org.botFromSpot.guiApp.AppMainController;
import org.botFromSpot.guiApp.model.BinancePair;
import org.botFromSpot.guiApp.model.BinanceTokens;
import org.botFromSpot.guiApp.model.BotConfiguration;
import org.botFromSpot.guiApp.model.StrategyAveragingForSpot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BotProvider {
    private AppMainController appMainController;
    private BinanceApiMethods binanceApiMethods;
    private BinancePairDAO binancePairDAO;
    private StrategyAveragingFromSpotProvider strategyProvider;
    public void setAppMainController(AppMainController appMainController) {this.appMainController = appMainController;}
    public void setBinanceApiMethods(BinanceApiMethods binanceApiMethods) {this.binanceApiMethods = binanceApiMethods;}
    public void setBinancePairDAO(BinancePairDAO binancePairDAO) {this.binancePairDAO = binancePairDAO;}

    public void setStrategyProvider(StrategyAveragingFromSpotProvider strategyProvider) {this.strategyProvider = strategyProvider;}

    private List<BotConfiguration> activeBots;

    public BotProvider() {
        this.activeBots = new ArrayList<>();
    }

    public void createBot(BotConfiguration botConfiguration){
        if(!activeBots.contains(botConfiguration)){  //Если конфигурации нет в списке созданных ботов.
            BinanceTokens tokens = new BinanceTokens(appMainController.getApiKey(), appMainController.getSecretKey());

            BinancePair pair = botConfiguration.getPair();
            PairConfiguration pairConfiguration = botConfiguration.getConfiguration();

            double sumForTrading = pairConfiguration.getSumToTrade();
            double balanceAcc = binanceApiMethods.getAccountBalanceForTestNet(tokens);
            double reservedBalance = appMainController.getReservedBalance_var();
            if (balanceAcc - sumForTrading > 0 && balanceAcc-reservedBalance >= sumForTrading){ //Если хватает средств с учётом зарезервированных средств
                System.out.println("Создаём бота");
                //Резервируем баланс для выбранной торговой пары
                appMainController.setReservedBalance_var(reservedBalance+sumForTrading);
                appMainController.reservedBalance.setText(String.valueOf(appMainController.getReservedBalance_var()));
                activeBots.add(botConfiguration);
                //Добавляем бот в список активных и запускаем бот с выбранной стратегией
                StrategyAveragingForSpot strategy = strategyProvider.getStrategyAveragingForSpot(
                        botConfiguration,
                        appMainController,
                        binancePairDAO,
                        binanceApiMethods);


                strategy.start();
            } else {
                throw new IllegalArgumentException("Недостаточно баланса для открытия пары");
            }
        }
        else {
            throw new IllegalArgumentException("Такой бот уже запущен");
        }
    }

    public void stopBot(BotConfiguration botConfiguration){
        Iterator<BotConfiguration> iterator = activeBots.iterator();
        while (iterator.hasNext()) {
            BotConfiguration someConfig = iterator.next();
            if (someConfig.equals(botConfiguration)) {
                StrategyAveragingForSpot strategy = strategyProvider.getStrategyAveragingForSpot(
                        botConfiguration,
                        appMainController,
                        binancePairDAO,
                        binanceApiMethods);

                strategy.stop();
                iterator.remove();
                BinancePair pair = botConfiguration.getPair();
                System.out.println("Cтоп для " + pair.getPairName());
                PairConfiguration pairConfiguration = botConfiguration.getConfiguration();
                double sumForTrading = pairConfiguration.getSumToTrade();
                double reservedBalance = appMainController.getReservedBalance_var();
                appMainController.setReservedBalance_var(reservedBalance - sumForTrading);
                appMainController.reservedBalance.setText(String.valueOf(appMainController.getReservedBalance_var()));
                appMainController.removeRunningPair(pair);
                return;
            }
        }
        System.err.println("Такой бот не был запущен");
    }



    public List<String> getPairNameInActiveBots() {
        List<String> pairNamesInActiveBots = new ArrayList<>();
        for(BotConfiguration config: activeBots){
            String pairName = config.getPair().getPairName();
            pairNamesInActiveBots.add(pairName);
        }
        return pairNamesInActiveBots;
    }


    //Принимает на вход конфигурацию (CreateAlgorithmRequest)
    //Проверяет что он единственный
    //createBot
    //Создать бота, остановить бота

}
