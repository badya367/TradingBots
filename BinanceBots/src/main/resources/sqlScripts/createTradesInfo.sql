CREATE TABLE IF NOT EXISTS tradesInfo (
    trade_id INTEGER PRIMARY KEY AUTOINCREMENT,
    pairId INTEGER REFERENCES settings(pairId),
    buyPrice DOUBLE NOT NULL,
    lotSize DOUBLE NOT NULL,
    openedOrders INTEGER NOT NULL,
    transactTime INTEGER NOT NULL,
    isTradeAllowed BOOLEAN NOT NULL DEFAULT FALSE
    );