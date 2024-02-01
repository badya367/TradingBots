package org.botFromSpot.guiApp.services;

public class ApplyConfigServiceImpl implements ApplyConfigService {
    private double takeProfit;
    private double averagingStep;
    private double multiplier;
    private int quantityOrders;
    private int averagingTimer;
    private double sumToTrade;
    private double startingLotVolume;
    private double tradingRange;

    public void setTakeProfit(double takeProfit) {
        this.takeProfit = takeProfit;
    }

    public void setAveragingStep(double averagingStep) {
        this.averagingStep = averagingStep;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public void setQuantityOrders(int quantityOrders) {
        this.quantityOrders = quantityOrders;
    }

    public void setAveragingTimer(int averagingTimer) {
        this.averagingTimer = averagingTimer;
    }

    public void setSumToTrade(double sumToTrade) {
        this.sumToTrade = sumToTrade;
    }

    public void setStartingLotVolume(double startingLotVolume) {
        this.startingLotVolume = startingLotVolume;
    }

    public void setTradingRange(double tradingRange) {
        this.tradingRange = tradingRange;
    }

    public ApplyConfigServiceImpl() {
    }

    DataBaseService dataBaseService;

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
        System.out.println(takeProfit);
        testBotConfiguration.setTakeProfit(takeProfit);
        testBotConfiguration.setAveragingStep(averagingStep);
        testBotConfiguration.setMultiplier(multiplier);
        testBotConfiguration.setQuantityOrders(quantityOrders);
        testBotConfiguration.setAveragingTimer(averagingTimer);
        testBotConfiguration.setSumToTrade(sumToTrade);
        testBotConfiguration.setStartingLotVolume(startingLotVolume);
        testBotConfiguration.setTradingRange(tradingRange);
        return testBotConfiguration;

    }

    public void setDataBaseService(DataBaseService dataBaseService) {
        this.dataBaseService = dataBaseService;
    }
}
