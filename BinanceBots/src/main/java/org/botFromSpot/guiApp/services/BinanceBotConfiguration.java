package org.botFromSpot.guiApp.services;

import javafx.scene.control.TextField;

public class BinanceBotConfiguration {
    private int pairId;
    private double takeProfit;
    private double averagingStep;  
    private double multiplier;
    private int quantityOrders;
    private int averagingTimer;
    private double sumToTrade;
    private double startingLotVolume;
    private double tradingRange;
    //todo Add lombok Library


    public BinanceBotConfiguration() {

    }

    public int getPairId() {
        return pairId;
    }
    public void setPairId(int pairId) {
        this.pairId = pairId;
    }
//---------------------------------------------------------------------------------//
    public double getTakeProfit() {
        return takeProfit;
    }
    public void setTakeProfit(double takeProfit) {
        this.takeProfit = takeProfit;
    }
//---------------------------------------------------------------------------------//
    public double getAveragingStep() {
        return averagingStep;
    }
    public void setAveragingStep(double averagingStep) {
        this.averagingStep = averagingStep;
    }
//---------------------------------------------------------------------------------//
    public double getMultiplier() {
        return multiplier;
    }
    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }
//---------------------------------------------------------------------------------//
    public int getQuantityOrders() {
        return quantityOrders;
    }
    public void setQuantityOrders(int quantityOrders) {
        this.quantityOrders = quantityOrders;
    }
//---------------------------------------------------------------------------------//
    public int getAveragingTimer() {
        return averagingTimer;
    }
    public void setAveragingTimer(int averagingTimer) {
        this.averagingTimer = averagingTimer;
    }
//---------------------------------------------------------------------------------//
    public double getSumToTrade() {
        return sumToTrade;
    }
    public void setSumToTrade(double sumToTrade) {
        this.sumToTrade = sumToTrade;
    }
//---------------------------------------------------------------------------------//
    public double getStartingLotVolume() {
        return startingLotVolume;
    }
    public void setStartingLotVolume(double startingLotVolume) {
        this.startingLotVolume = startingLotVolume;
    }
//---------------------------------------------------------------------------------//
    public double getTradingRange() {
        return tradingRange;
    }
    public void setTradingRange(double tradingRange) {
        this.tradingRange = tradingRange;
    }

    public static double calculateTradingRange(TextField averagingStep, TextField quantityOrders){
        return Double.parseDouble(averagingStep.getText()) * Integer.parseInt(quantityOrders.getText());
    }

    public static double calculateStartingLotVolume(TextField sumToTrade, TextField multiplier, TextField quantityOrders){
        double sum = Double.parseDouble(sumToTrade.getText());
        double multi = Double.parseDouble(multiplier.getText());
        double quantity = Double.parseDouble(quantityOrders.getText());
        double startingLotVolume;
        for (int i = 0; i < quantity; i++){
            sum = sum/multi;
        }
        startingLotVolume = sum;
        return startingLotVolume;
    }
}
