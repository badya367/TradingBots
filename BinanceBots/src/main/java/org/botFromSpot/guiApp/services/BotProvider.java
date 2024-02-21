package org.botFromSpot.guiApp.services;

import org.botFromSpot.guiApp.model.BotConfiguration;
import org.botFromSpot.guiApp.model.StrategyAveragingForSpot;

import java.util.ArrayList;
import java.util.List;

public class BotProvider {
    private List<BotConfiguration> activeBots;

    public BotProvider() {
        this.activeBots = new ArrayList<>();
    }

    public void createBot(BotConfiguration botConfiguration){
        if(!activeBots.contains(botConfiguration)){
            StrategyAveragingForSpot strategy = new StrategyAveragingForSpot(botConfiguration);
            activeBots.add(botConfiguration);
            strategy.start();
        }
        else {
            System.err.println("Такой бот уже запущен");
        }
    }

    public void stopBot(BotConfiguration botConfiguration){
        for(BotConfiguration someConfig: activeBots){
            if(someConfig.equals(botConfiguration)){
                StrategyAveragingForSpot strategy = new StrategyAveragingForSpot(someConfig);
                strategy.stop();
                activeBots.remove(someConfig);
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
