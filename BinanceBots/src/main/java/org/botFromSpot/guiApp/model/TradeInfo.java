package org.botFromSpot.guiApp.model;

import org.botFromSpot.guiApp.services.PairConfiguration;

import java.sql.Time;

public class TradeInfo {

    private int pairId;
    private double buyPrice;
    private double lotSize;
    private int openedOrders;
    private long transactTime;
    private boolean isTradeAllowed;

    public TradeInfo(){
    }


    public int getPairId() {
        return pairId;
    }

    public void setPairId(int pairId) {
        this.pairId = pairId;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(double buyPrice) {
        this.buyPrice = buyPrice;
    }

    public double getLotSize() {
        return lotSize;
    }

    public void setLotSize(double lotSize) {
        this.lotSize = lotSize;
    }

    public int getOpenedOrders() {
        return openedOrders;
    }

    public void setOpenedOrders(int openedOrders) {
        this.openedOrders = openedOrders;
    }

    public long getTransactTime() {return transactTime;}

    public void setTransactTime(long transactTime) {
        this.transactTime = transactTime;
    }

    public boolean isTradeAllowed() {return isTradeAllowed;}

    public void setTradeAllowed(boolean tradeAllowed) {isTradeAllowed = tradeAllowed;}

    @Override
    public String toString() {
        return "TradeInfo{" +
                "pairId=" + pairId +
                ", buyPrice=" + buyPrice +
                ", lotSize=" + lotSize +
                ", openedOrders=" + openedOrders +
                ", transactTime=" + transactTime +
                ", isTradeAllowed=" + isTradeAllowed +
                '}';
    }
}
