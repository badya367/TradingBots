package org.botFromSpot.guiApp.model;

import org.botFromSpot.guiApp.services.PairConfiguration;

import java.util.Objects;

public class BotConfiguration {
    private BinancePair pair;
    private PairConfiguration configuration;

    public BotConfiguration() {
    }

    public BotConfiguration(BinancePair pair, PairConfiguration configuration) {
        this.pair = pair;
        this.configuration = configuration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BotConfiguration that = (BotConfiguration) o;
        return Objects.equals(pair.getPairName(), that.pair.getPairName()) && Objects.equals(configuration.getPairId(), that.configuration.getPairId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(pair.getPairName(), configuration.getPairId());
    }

    @Override
    public String toString() {
        return "BotConfiguration" + "\n"
                +"{" + "\n"
                + "pair=" + pair + ", " + "\n"
                + "configuration=" + configuration + "\n" +
                '}';
    }
}
