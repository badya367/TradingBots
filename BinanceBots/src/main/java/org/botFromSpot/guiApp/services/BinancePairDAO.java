package org.botFromSpot.guiApp.services;

import org.botFromSpot.guiApp.model.BinancePair;
import org.botFromSpot.guiApp.model.BinanceTokens;
import org.botFromSpot.guiApp.model.TradeInfo;
import org.botFromSpot.guiApp.utils.CryptoUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BinancePairDAO {
    private DataBaseService dataBaseService;

    public void setDataBaseService(DataBaseService dataBaseService) {
        this.dataBaseService = dataBaseService;
    }
    public void saveTokens(String encryptedApiKey, String encryptedSecretKey, String stock) {
        Connection connection = dataBaseService.connectionDB();
        try {
            String query = "INSERT INTO tokens (api_key, secret_key, stock) VALUES (?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, encryptedApiKey);
                preparedStatement.setString(2, encryptedSecretKey);
                preparedStatement.setString(3, stock);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public BinanceTokens getTokens() {
        Connection connection = dataBaseService.connectionDB();
        try {
            String query = "SELECT api_key, secret_key FROM tokens ORDER BY id DESC LIMIT 1";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String encryptedApiKey = resultSet.getString("api_key");
                        String encryptedSecretKey = resultSet.getString("secret_key");

                        // Расшифровка ключей, используя ваш CryptoUtils.decrypt
                        String apiKey = CryptoUtils.decrypt(encryptedApiKey);
                        String secretKey = CryptoUtils.decrypt(encryptedSecretKey);

                        return new BinanceTokens(apiKey, secretKey);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
    public String getStock(String apiKey) {
        Connection connection = dataBaseService.connectionDB();
        String stock = "";
        try {
            String query = "SELECT stock FROM tokens WHERE api_key = ? ORDER BY id DESC LIMIT 1";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, apiKey);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        stock = resultSet.getString("stock");
                        return stock;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return stock;
    }
    /* -------------------------------------------------------------------------
    // Вставка в таблицу pairs торговой пары
    --------------------------------------------------------------------------*/
    public void addPair(BinancePair pair){
        String name = pair.getPairName();
        Connection connection = dataBaseService.connectionDB();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO pairs (pairName) VALUES (?);");
            preparedStatement.setString(1,name);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
    /* -------------------------------------------------------------------------
    // Вставка в таблицу botConfiguration конфигурации для торговой пары
    --------------------------------------------------------------------------*/
    public void addBotConfiguration(PairConfiguration botConfiguration) {
        Connection connection = dataBaseService.connectionDB();
        try {
            String query = "INSERT INTO settings (pairId, " +
                    "takeProfit, " +
                    "averagingStep, " +
                    "multiplier, " +
                    "quantityOrders, " +
                    "averagingTimer, " +
                    "sumToTrade, " +
                    "startingLotVolume, " +
                    "tradingRange, " +
            "isChanged) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, botConfiguration.getPairId());
                statement.setDouble(2, botConfiguration.getTakeProfit());
                statement.setDouble(3, botConfiguration.getAveragingStep());
                statement.setDouble(4, botConfiguration.getMultiplier());
                statement.setInt(5, botConfiguration.getQuantityOrders());
                statement.setInt(6, botConfiguration.getAveragingTimer());
                statement.setDouble(7, botConfiguration.getSumToTrade());
                statement.setDouble(8, botConfiguration.getStartingLotVolume());
                statement.setDouble(9, botConfiguration.getTradingRange());
                statement.setBoolean(10, botConfiguration.isChanged());

                statement.executeUpdate();
                System.out.println("Конфигурация для pairId " + botConfiguration.getPairId() + " добавлена в таблицу settings.");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении конфигурации в таблицу settings: " + e.getMessage());
        }
    }
    /* -------------------------------------------------------------------------
    // Вставка в таблицу tradesInfo информации о торговой паре
    --------------------------------------------------------------------------*/
    public void addTradeInfo(TradeInfo tradeInfo) {
        Connection connection = dataBaseService.connectionDB();
        try {
            String query = "INSERT INTO tradesInfo (pairId, " +
                    "buyPrice, " +
                    "lotSize," +
                    "openedOrders," +
                    "transactTime," +
                    "isTradeAllowed," +
                    "profit," +
                    "isAutoDrying) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, tradeInfo.getPairId());
                statement.setDouble(2, tradeInfo.getBuyPrice());
                statement.setDouble(3, tradeInfo.getLotSize());
                statement.setInt(4, tradeInfo.getOpenedOrders());
                statement.setLong(5, tradeInfo.getTransactTime());
                statement.setBoolean(6, tradeInfo.isTradeAllowed());
                statement.setDouble(7, tradeInfo.getProfit());
                statement.setBoolean(8, tradeInfo.isAutoDrying());


                statement.executeUpdate();
                System.out.println("Конфигурация для pairId " + tradeInfo.getPairId() + " добавлена в таблицу tradesInfo.");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении конфигурации в таблицу settings: " + e.getMessage());
        }
    }

    /* -------------------------------------------------------------------------
    // Изменение botConfiguration конфигурации для торговой пары по pairId
    --------------------------------------------------------------------------*/
    public void updateBotConfiguration(PairConfiguration botConfiguration) {
        Connection connection = dataBaseService.connectionDB();
        try {
            String query = "UPDATE settings SET takeProfit=?, averagingStep=?, multiplier=?, " +
                    "quantityOrders=?, averagingTimer=?, sumToTrade=?, startingLotVolume=?, " +
                    "tradingRange=?, isChanged=? WHERE pairId=?;";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setDouble(1, botConfiguration.getTakeProfit());
                statement.setDouble(2, botConfiguration.getAveragingStep());
                statement.setDouble(3, botConfiguration.getMultiplier());
                statement.setInt(4, botConfiguration.getQuantityOrders());
                statement.setInt(5, botConfiguration.getAveragingTimer());
                statement.setDouble(6, botConfiguration.getSumToTrade());
                statement.setDouble(7, botConfiguration.getStartingLotVolume());
                statement.setDouble(8, botConfiguration.getTradingRange());
                statement.setBoolean(9, botConfiguration.isChanged());
                statement.setInt(10, botConfiguration.getPairId());

                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Конфигурация для pairId " + botConfiguration.getPairId() + " обновлена в таблице settings.");
                } else {
                    System.out.println("Конфигурация для pairId " + botConfiguration.getPairId() + " не найдена в таблице settings.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении конфигурации в таблице settings: " + e.getMessage());
        }
    }

    /* -------------------------------------------------------------------------
    // Изменение информации по последнему трейду торговой пары по pairId в tradesInfo
    --------------------------------------------------------------------------*/
    public void updateTradeInfo(TradeInfo tradeInfo) {
        Connection connection = dataBaseService.connectionDB();
        try {
            String query = "UPDATE tradesInfo SET buyPrice=?, lotSize=?, openedOrders=?, " +
                    "transactTime=?, isTradeAllowed=? WHERE pairId=?;";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setDouble(1, tradeInfo.getBuyPrice());
                statement.setDouble(2, tradeInfo.getLotSize());
                statement.setInt(3, tradeInfo.getOpenedOrders());
                statement.setLong(4, tradeInfo.getTransactTime());
                statement.setBoolean(5, tradeInfo.isTradeAllowed());
                statement.setInt(6, tradeInfo.getPairId());

                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Конфигурация для pairId " + tradeInfo.getPairId() + " обновлена в таблице tradesInfo.");
                } else {
                    System.out.println("Конфигурация для pairId " + tradeInfo.getPairId() + " не найдена в таблице tradesInfo.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении конфигурации в таблице tradesInfo: " + e.getMessage());
        }
    }
    /* -------------------------------------------------------------------------
    // Изменение профита торговой пары по pairId в tradesInfo
    --------------------------------------------------------------------------*/
    public void updateProfitInTradeInfo(TradeInfo tradeInfo) {
        Connection connection = dataBaseService.connectionDB();
        try {
            String query = "UPDATE tradesInfo SET profit=? WHERE pairId=?;";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setDouble(1, tradeInfo.getProfit());
                statement.setInt(2, tradeInfo.getPairId());

                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Профит для pairId " + tradeInfo.getPairId() + " обновлена в таблице tradesInfo.");
                } else {
                    System.out.println("Профит для pairId " + tradeInfo.getPairId() + " не найдена в таблице tradesInfo.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении профита в таблице tradesInfo: " + e.getMessage());
        }
    }
    /* -------------------------------------------------------------------------
    // Изменение поля "автосушка" для торговой пары по pairId в tradesInfo
    --------------------------------------------------------------------------*/
    public void updateAutoDryingInTradeInfo(TradeInfo tradeInfo) {
        Connection connection = dataBaseService.connectionDB();
        try {
            String query = "UPDATE tradesInfo SET isAutoDrying=? WHERE pairId=?;";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setBoolean(1, tradeInfo.isAutoDrying());
                statement.setInt(2, tradeInfo.getPairId());

                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Поле \"Автосушка\" для pairId " + tradeInfo.getPairId() + " обновлена в таблице tradesInfo.");
                } else {
                    System.out.println("Поле \"Автосушка\" для pairId " + tradeInfo.getPairId() + " не найдена в таблице tradesInfo.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении поля \"Автосушка\" в таблице tradesInfo: " + e.getMessage());
        }
    }
    /* -------------------------------------------------------------------------
    // Получаем список всех имен валютных пар
    --------------------------------------------------------------------------*/
    public List<String> getAllPairsNames(){
        List<String> allPairs = new ArrayList<>();
        Connection connection = dataBaseService.connectionDB();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT pairName FROM pairs;");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                allPairs.add(resultSet.getString(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return allPairs;
    }
    /* -------------------------------------------------------------------------
    // Получаем список из объектов всех пар
    --------------------------------------------------------------------------*/
    public List<BinancePair> getAllPairs(){
        List<BinancePair> allPairs = new ArrayList<>();
        Connection connection = dataBaseService.connectionDB();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT id, pairName FROM pairs;");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                BinancePair binancePair = new BinancePair();
                binancePair.setId(resultSet.getInt(1));
                binancePair.setPairName(resultSet.getString(2));
                allPairs.add(binancePair);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return allPairs;
    }
    /* -------------------------------------------------------------------------
    // Получаем конкретную BinancePair по имени пары
    --------------------------------------------------------------------------*/
    public BinancePair getBinancePairByPairName(String pairName) {
        BinancePair pair = new BinancePair();
        Connection connection = dataBaseService.connectionDB();
        try {
            String query = "SELECT * FROM pairs WHERE pairName = ?;";
            try (PreparedStatement statement = connection.prepareStatement(query)){
                statement.setString(1, pairName);
                try (ResultSet resultSet = statement.executeQuery()){
                    if(resultSet.next()) {

                        pair.setId(resultSet.getInt("id"));
                        pair.setPairName(resultSet.getString("pairName"));
                    }
                }
            }
        } catch (SQLException e){
            System.err.println("Ошибка при получении BinancePair по имени" + e.getMessage());
        }
        return pair;
    }
    /* -------------------------------------------------------------------------
    // Получаем id для торговой пары по имени пары
    --------------------------------------------------------------------------*/
    public int getPairIdByPairName(String pairName) {
        int pairId = -1; // Если запись не найдена
        Connection connection = dataBaseService.connectionDB();
        try {
            String query = "SELECT id FROM pairs WHERE pairName = ?;";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, pairName);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        pairId = resultSet.getInt("id");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении pairId по PairName: " + e.getMessage());
        }
        return pairId;
    }

    /* -------------------------------------------------------------------------
    // Получаем конкретную конфигурацию по pairId
    --------------------------------------------------------------------------*/
    public PairConfiguration getConfigurationForPair(int pairId) {
        PairConfiguration botConfiguration = null;
        Connection connection = dataBaseService.connectionDB();
        try {
            String query = "SELECT * FROM settings WHERE pairId = ?;";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, pairId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        double takeProfit = resultSet.getDouble("takeProfit");
                        double averagingStep = resultSet.getDouble("averagingStep");
                        double multiplier = resultSet.getDouble("multiplier");
                        int quantityOrders = resultSet.getInt("quantityOrders");
                        int averagingTimer = resultSet.getInt("averagingTimer");
                        double sumToTrade = resultSet.getDouble("sumToTrade");
                        double startingLotVolume = resultSet.getDouble("startingLotVolume");
                        double tradingRange = resultSet.getDouble("tradingRange");
                        boolean isChanged = resultSet.getBoolean("isChanged");
                        botConfiguration = new PairConfiguration();
                        botConfiguration.setPairId(pairId);
                        botConfiguration.setTakeProfit(takeProfit);
                        botConfiguration.setAveragingStep(averagingStep);
                        botConfiguration.setMultiplier(multiplier);
                        botConfiguration.setQuantityOrders(quantityOrders);
                        botConfiguration.setAveragingTimer(averagingTimer);
                        botConfiguration.setSumToTrade(sumToTrade);
                        botConfiguration.setStartingLotVolume(startingLotVolume);
                        botConfiguration.setTradingRange(tradingRange);
                        botConfiguration.setChanged(isChanged);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при чтении данных из таблицы settings: " + e.getMessage());
        }

        return botConfiguration;
    }

    /* -------------------------------------------------------------------------
    // Получаем конкретную информацию по трейдам по pairId
    --------------------------------------------------------------------------*/
    public TradeInfo getTradeInfoForPair(int pairId) {
        TradeInfo tradeInfo = null;
        Connection connection = dataBaseService.connectionDB();
        try {
            String query = "SELECT * FROM tradesInfo WHERE pairId = ?;";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, pairId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        double buyPrice = resultSet.getDouble("buyPrice");
                        double lotSize = resultSet.getDouble("lotSize");
                        int openedOrders = resultSet.getInt("openedOrders");
                        long transactTime = resultSet.getLong("transactTime");
                        boolean isTradeAllowed = resultSet.getBoolean("isTradeAllowed");
                        boolean isAutoDrying = resultSet.getBoolean("isAutoDrying");

                        tradeInfo = new TradeInfo();
                        tradeInfo.setPairId(pairId);
                        tradeInfo.setBuyPrice(buyPrice);
                        tradeInfo.setLotSize(lotSize);
                        tradeInfo.setOpenedOrders(openedOrders);
                        tradeInfo.setTransactTime(transactTime);
                        tradeInfo.setTradeAllowed(isTradeAllowed);
                        tradeInfo.setAutoDrying(isAutoDrying);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при чтении данных из таблицы tradesInfo: " + e.getMessage());
        }

        return tradeInfo;
    }
    /* -------------------------------------------------------------------------
    // Получаем профит пары по pairId
    --------------------------------------------------------------------------*/
    public double getProfitInTradeInfoForPair(int pairId) {
        double profit = 0;
        Connection connection = dataBaseService.connectionDB();
        try {
            String query = "SELECT profit FROM tradesInfo WHERE pairId = ?;";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, pairId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        profit = resultSet.getDouble("profit");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при чтении профита из таблицы tradesInfo: " + e.getMessage());
        }

        return profit;
    }
    /* -------------------------------------------------------------------------
    // Получаем значение поля "Автосушка" для пары по pairId
    --------------------------------------------------------------------------*/
    public boolean getAutoDryingInTradeInfoForPair(int pairId) {
        boolean isAutoDrying = false;
        Connection connection = dataBaseService.connectionDB();
        try {
            String query = "SELECT isAutoDrying FROM tradesInfo WHERE pairId = ?;";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, pairId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        isAutoDrying = resultSet.getBoolean("isAutoDrying");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при чтении поля \"Автосушка\" из таблицы tradesInfo: " + e.getMessage());
        }

        return isAutoDrying;
    }
    /* -------------------------------------------------------------------------
    // Удаляем конкретную конфигурацию по pairId
    --------------------------------------------------------------------------*/
    public void deleteBotConfiguration(int pairId) {
        Connection connection = dataBaseService.connectionDB();
        try {
            String query = "DELETE FROM settings WHERE pairId=?;";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, pairId);

                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Конфигурация для pairId " + pairId + " удалена из таблицы settings.");
                } else {
                    System.out.println("Конфигурация для pairId " + pairId + " не найдена в таблице settings.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении конфигурации из таблицы settings: " + e.getMessage());
        }
    }
    /* -------------------------------------------------------------------------
    // Удаляем конкретную пару по pairId
    --------------------------------------------------------------------------*/
    public void deletePair(int pairId) {
        Connection connection = dataBaseService.connectionDB();
        try {
            String query = "DELETE FROM pairs WHERE id=?;";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, pairId);

                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Пара с pairId " + pairId + " удалена из таблицы pairs.");
                } else {
                    System.out.println("Пара pairId " + pairId + " не найдена в таблице pairs.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении пары из таблицы pairs: " + e.getMessage());
        }
    }
    /* -------------------------------------------------------------------------
    // Удаляем конкретную информацию из tradesInfo по pairId
    --------------------------------------------------------------------------*/
    public void deleteTradesInfo(int pairId) {
        Connection connection = dataBaseService.connectionDB();
        try {
            String query = "DELETE FROM tradesInfo WHERE pairId=?;";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, pairId);

                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Конфигурация для pairId " + pairId + " удалена из таблицы tradesInfo.");
                } else {
                    System.out.println("Конфигурация для pairId " + pairId + " не найдена в таблице tradesInfo.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при удалении конфигурации из таблицы tradesInfo: " + e.getMessage());
        }
    }

}
