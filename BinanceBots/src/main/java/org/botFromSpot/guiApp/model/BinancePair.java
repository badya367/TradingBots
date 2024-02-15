package org.botFromSpot.guiApp.model;

public class BinancePair {
    private int id;
    private String pairName;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
//---------------------------------------------------------------------------------//
    public String getPairName() {
        return pairName;
    }
    public void setPairName(String pairName) {
        this.pairName = pairName;
    }

    @Override
    public String toString() {
        return pairName;
    }
}
