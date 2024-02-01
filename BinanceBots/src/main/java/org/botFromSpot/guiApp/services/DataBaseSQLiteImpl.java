package org.botFromSpot.guiApp.services;

import org.botFromSpot.guiApp.model.BinancePair;
import org.botFromSpot.guiApp.utils.Constants;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class DataBaseSQLiteImpl implements DataBaseService {
    private static DataBaseSQLiteImpl instance;
    private String url;

    public void setUrl(String url) {
        this.url = url;
    }

    private Connection connection;

    /* -------------------------------------------------------------------------
    // Создание экземпляра базы данных с помощью паттерна Singleton
    --------------------------------------------------------------------------*/
    private void init() {
        try {
            connection = DriverManager.getConnection(url);
            System.out.println("Соединение с базой данных установлено");
            String getAllTablesQueryCheck = SqlQueryLoader.loadSql(Constants.GET_ALL_TABLES_SQL);
            PreparedStatement preparedStatement = connection.prepareStatement(getAllTablesQueryCheck);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                System.out.println("table name: " + resultSet.getString(1));
            }
        } catch (SQLException e) {
            System.err.println("Подключение не установлено: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /* -------------------------------------------------------------------------
    // Подключение к БД
    --------------------------------------------------------------------------*/
    @Override
    public Connection connectionDB() {
        return connection;
    }

    /* -------------------------------------------------------------------------
    // Создание таблиц в БД
    --------------------------------------------------------------------------*/
    @Override
    public void createDB() {
        try {
            Statement statement = connection.createStatement();
            // Создание таблицы для торговых пар
            String createPairsTableQuery = "CREATE TABLE IF NOT EXISTS pairs ("
                    + "id Number PRIMARY KEY AUTOINCREMENT,"
                    + "pairName TEXT NOT NULL);";

            statement.executeUpdate(createPairsTableQuery);

            // Создание таблицы для настроек
            String createConfigurationTableQuery = "CREATE TABLE IF NOT EXISTS botConfiguration ("
                    + "pairId Number REFERENCES pairs(id) PRIMARY KEY,"
                    + "takeProfit DOUBLE NOT NULL,"
                    + "averagingStep DOUBLE NOT NULL,"
                    + "multiplier DOUBLE NOT NULL),"
                    + "quantityOrders Number NOT NULL),"
                    + "averagingTimer Number NOT NULL),"
                    + "sumToTrade DOUBLE NOT NULL),"
                    + "startingLotVolume DOUBLE NOT NULL),"
                    + "tradingRange DOUBLE NOT NULL;";

            statement.executeUpdate(createConfigurationTableQuery);

            System.out.println("таблицы созданы успешно.");
        } catch (SQLException e) {
            System.err.println("Ошибка при создании таблиц: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /* -------------------------------------------------------------------------
    // Вставка в таблицу botConfiguration конфигурации для торговой пары
    --------------------------------------------------------------------------*/
    @Override
    public void insertBotConfiguration(BinanceBotConfiguration botConfiguration) {
        try {
            String query = "INSERT INTO settings (pairId, " +
                    "takeProfit, " +
                    "averagingStep, " +
                    "multiplier, " +
                    "quantityOrders" +
                    "averagingTimer" +
                    "sumToTrade" +
                    "startingLotVolume" +
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
    // Получаем все конфигурации из таблицы botConfiguration
    --------------------------------------------------------------------------*/
    @Override
    public List<BinanceBotConfiguration> readAllConfiguration() {
        List<BinanceBotConfiguration> settingsList = new ArrayList<>();

        try {
            String query = "SELECT * FROM botConfiguration;";
            try (PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    int pairId = resultSet.getInt("pairId");
                    int takeProfit = resultSet.getInt("takeProfit");
                    double averagingStep = resultSet.getDouble("averagingStep");
                    double multiplier = resultSet.getDouble("multiplier");
                    int quantityOrders = resultSet.getInt("quantityOrders");
                    int averagingTimer = resultSet.getInt("averagingTimer");
                    double sumToTrade = resultSet.getDouble("sumToTrade");
                    double startingLotVolume = resultSet.getDouble("startingLotVolume");
                    double tradingRange = resultSet.getDouble("tradingRange");

                    BinanceBotConfiguration botConfiguration = new BinanceBotConfiguration();
                    botConfiguration.setPairId(pairId);
                    botConfiguration.setTakeProfit(takeProfit);
                    botConfiguration.setAveragingStep(averagingStep);
                    botConfiguration.setMultiplier(multiplier);
                    botConfiguration.setQuantityOrders(quantityOrders);
                    botConfiguration.setAveragingTimer(averagingTimer);
                    botConfiguration.setSumToTrade(sumToTrade);
                    botConfiguration.setStartingLotVolume(startingLotVolume);
                    botConfiguration.setTradingRange(tradingRange);

                    settingsList.add(botConfiguration);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при считывании данных из таблицы botConfiguration: " + e.getMessage());
        }

        return settingsList;
    }

    /* -------------------------------------------------------------------------
    // Получаем конкретную конфигурацию по pairId
    --------------------------------------------------------------------------*/
    @Override
    public BinanceBotConfiguration readConfigurationForPair(int pairId) {
        BinanceBotConfiguration botConfiguration = null;

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

                        botConfiguration = new BinanceBotConfiguration();
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
    // Вставка в таблицу pairs валютную пару
    --------------------------------------------------------------------------*/
    @Override
    public void insertPair(String pairName) {
        try {
            String query = "INSERT INTO pairs (pairName) VALUES (?);";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, pairName);

                statement.executeUpdate();
                System.out.println("Торговая пара " + pairName + " добавлена в таблицу pairs.");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при добавлении торговой пары в таблицу pairs: " + e.getMessage());
        }
    }

    /* -------------------------------------------------------------------------
    Получаем все торговые пары из таблицы pairs
    --------------------------------------------------------------------------*/
    @Override
    public List<BinancePair> readAllPairs() {
        List<BinancePair> pairs = new ArrayList<>();

        try {
            String query = "SELECT * FROM pairs;";
            try (PreparedStatement statement = connection.prepareStatement(query);
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String pairName = resultSet.getString("pairName");

                    BinancePair pair = new BinancePair();
                    pair.setId(id);
                    pair.setPairName(pairName);

                    pairs.add(pair);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при считывании данных из таблицы pairs: " + e.getMessage());
        }

        return pairs;
    }

    /* -------------------------------------------------------------------------
    // Получаем id для торговой пары по имени пары
    --------------------------------------------------------------------------*/
    @Override
    public int getPairIdByPairName(String pairName) {
        int pairId = -1; // Если запись не найдена

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
    // Закрытие соединения с БД
    --------------------------------------------------------------------------*/
    @Override
    public void closeDB() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Соединение с базой данных закрыто");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при закрытии соединения с базой данных: " + e.getMessage());
        }
    }
}
