package org.botFromSpot.guiApp.services;

import org.botFromSpot.guiApp.model.BinancePair;

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
                    "tradingRange) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
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

                statement.executeUpdate();
                System.out.println("Конфигурация для pairId " + botConfiguration.getPairId() + " добавлена в таблицу settings.");
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
                    "tradingRange=? WHERE pairId=?;";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setDouble(1, botConfiguration.getTakeProfit());
                statement.setDouble(2, botConfiguration.getAveragingStep());
                statement.setDouble(3, botConfiguration.getMultiplier());
                statement.setInt(4, botConfiguration.getQuantityOrders());
                statement.setInt(5, botConfiguration.getAveragingTimer());
                statement.setDouble(6, botConfiguration.getSumToTrade());
                statement.setDouble(7, botConfiguration.getStartingLotVolume());
                statement.setDouble(8, botConfiguration.getTradingRange());
                statement.setInt(9, botConfiguration.getPairId());

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
                        int takeProfit = resultSet.getInt("takeProfit");
                        double averagingStep = resultSet.getDouble("averagingStep");
                        double multiplier = resultSet.getDouble("multiplier");
                        int quantityOrders = resultSet.getInt("quantityOrders");
                        int averagingTimer = resultSet.getInt("averagingTimer");
                        double sumToTrade = resultSet.getDouble("sumToTrade");
                        double startingLotVolume = resultSet.getDouble("startingLotVolume");
                        double tradingRange = resultSet.getDouble("tradingRange");

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
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при чтении данных из таблицы settings: " + e.getMessage());
        }

        return botConfiguration;
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

}
