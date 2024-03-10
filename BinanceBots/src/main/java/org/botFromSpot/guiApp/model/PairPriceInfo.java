package org.botFromSpot.guiApp.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class PairPriceInfo {
    private String symbol;
    private double bidPrice;
    private double bidQty;
    private double askPrice;
    private double askQty;
    private double avgBuyPrice; //Цена покупки пары
    private double profit;

    public PairPriceInfo(String symbol, double bidPrice, double bidQty, double askPrice, double askQty) {
        this.symbol = symbol;
        this.bidPrice = bidPrice;
        this.bidQty = bidQty;
        this.askPrice = askPrice;
        this.askQty = askQty;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getBidPrice() {
        return bidPrice;
    }

    public double getBidQty() {
        return bidQty;
    }

    public double getAskPrice() {
        return askPrice;
    }

    public double getAskQty() {
        return askQty;
    }

    public double getProfit() {
        return profit;
    }

    public double getAvgBuyPrice() {return avgBuyPrice;}

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setBidPrice(double bidPrice) {
        this.bidPrice = bidPrice;
    }

    public void setBidQty(double bidQty) {
        this.bidQty = bidQty;
    }

    public void setAskPrice(double askPrice) {
        this.askPrice = askPrice;
    }

    public void setAskQty(double askQty) {
        this.askQty = askQty;
    }

    public void setProfit(double profit) {
        this.profit = profit;
    }

    public void setAvgBuyPrice(double avgBuyPrice) {this.avgBuyPrice = avgBuyPrice;}

    @Override
    public String toString() {
        return "PairPriceInfo{" +
                "symbol='" + symbol + '\'' +
                ", bidPrice=" + bidPrice +
                ", profit=" + profit +
                '}';
    }
}
