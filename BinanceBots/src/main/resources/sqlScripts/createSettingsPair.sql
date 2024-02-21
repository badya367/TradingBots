CREATE TABLE IF NOT EXISTS settings (
    settings_id INTEGER PRIMARY KEY AUTOINCREMENT,
    pairId INTEGER REFERENCES pairs(id),
    takeProfit DOUBLE NOT NULL,
    averagingStep DOUBLE NOT NULL,
    multiplier DOUBLE NOT NULL,
    quantityOrders INTEGER NOT NULL,
    averagingTimer INTEGER NOT NULL,
    sumToTrade DOUBLE NOT NULL,
    startingLotVolume DOUBLE NOT NULL,
    tradingRange DOUBLE NOT NULL
    );