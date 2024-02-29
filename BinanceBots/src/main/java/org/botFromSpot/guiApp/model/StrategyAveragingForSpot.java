package org.botFromSpot.guiApp.model;

public class StrategyAveragingForSpot {
    private BotConfiguration botConfiguration;


    public StrategyAveragingForSpot(BotConfiguration botConfiguration) {
        this.botConfiguration = botConfiguration;
    }

    public void start(){
        System.out.println("Бот запущен для конфигурации: " + botConfiguration.toString());
    }
    public void update(){


        System.out.println("Бот обновлен для конфигурации:  " + botConfiguration.toString());
    }
    public void stop(){
        System.out.println("Бот остановлен для конфигурации: " + botConfiguration.toString());
    }
}
