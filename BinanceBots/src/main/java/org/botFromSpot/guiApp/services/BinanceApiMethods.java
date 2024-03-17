package org.botFromSpot.guiApp.services;

import com.binance.connector.client.SpotClient;
import com.binance.connector.client.exceptions.BinanceClientException;
import com.binance.connector.client.exceptions.BinanceConnectorException;
import com.binance.connector.client.impl.SpotClientImpl;
import com.binance.connector.client.impl.spot.Market;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import org.botFromSpot.guiApp.AppMainController;
import org.botFromSpot.guiApp.model.BinanceTokens;
import org.botFromSpot.guiApp.model.PairPriceInfo;
import org.botFromSpot.guiApp.services.binanceTestNetServices.TestNetSpotClient;
import org.botFromSpot.guiApp.services.binanceTestNetServices.TestNetSpotClientImpl;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.*;
import java.util.*;


public class BinanceApiMethods {
    private BinancePairDAO binancePairDAO;
    private AppMainController appMainController;
    private static String baseURL;

    public static void setBaseURL(String baseURL) {BinanceApiMethods.baseURL = baseURL;}

    public void setBinancePairDAO(BinancePairDAO binancePairDAO) {
        this.binancePairDAO = binancePairDAO;
    }

    public void setAppMainController(AppMainController appMainController) {this.appMainController = appMainController;}

    public void connectBinance(BinanceTokens tokens) {
        try {
            URL url = new URL("https://testnet.binance.vision/api/v3/ping");

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Устанавливаем метод запроса
            connection.setRequestMethod("GET");

            // Устанавливаем заголовки для аутентификации с использованием apiKey и secretKey
            connection.setRequestProperty("X-MBX-APIKEY", tokens.getApiKey());

            // Получаем ответ от сервера
            int responseCode = connection.getResponseCode();

            // Читаем ответ
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            // Закрываем соединение
            connection.disconnect();

            // Печатаем результат
            System.out.println("Response Code: " + responseCode);
            System.out.println("Response: " + response);

            // Проверяем успешность подключения
            if (responseCode == 200) {
                System.out.println("Successfully connected to Binance API.");
            } else {
                System.out.println("Failed to connect to Binance API.");
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
    public double getAccountBalanceForTestNet(BinanceTokens tokens){
        Map<String, Object> parameters = new LinkedHashMap<>();
        //SpotClient client = new SpotClientImpl(tokens.getApiKey(),tokens.getSecretKey());
        //System.out.println(client.createWallet().walletBalance(parameters));
        //Закоменчено для получения баланса НЕ тестовой сети
        TestNetSpotClient client = new TestNetSpotClientImpl(tokens.getApiKey(),tokens.getSecretKey());
        String jsonAnswerAccountInfo = client.createWallet().walletBalance(parameters);
        BigDecimal bdBalance = BigDecimal.valueOf(
                client.createWallet().getAvailableBalanceInUSDT(jsonAnswerAccountInfo,"USDT"));
        bdBalance = bdBalance.setScale(
                2,
                RoundingMode.HALF_DOWN);

        return bdBalance.doubleValue();
    }

    public String getWalletInfoForTestNet(BinanceTokens tokens){
        Map<String, Object> parameters = new LinkedHashMap<>();
        //SpotClient client = new SpotClientImpl(tokens.getApiKey(),tokens.getSecretKey());
        //System.out.println(client.createWallet().walletBalance(parameters));
        //Закоменчено для получения баланса НЕ тестовой сети
        TestNetSpotClient client = new TestNetSpotClientImpl(tokens.getApiKey(),tokens.getSecretKey());
        String jsonAnswerAccountInfo = client.createWallet().walletBalance(parameters);

        return jsonAnswerAccountInfo;
    }
    public ComboBox<String> allPairs() {
        ComboBox<String> comboBox = new ComboBox<>();
        try {
            // Формируем URL для получения информации о торговых парах
            URL url = new URL(baseURL + "/api/v3/exchangeInfo");

            // Открываем соединение
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Устанавливаем метод запроса
            connection.setRequestMethod("GET");

            // Получаем ответ от сервера
            int responseCode = connection.getResponseCode();

            // Читаем ответ
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            // Закрываем соединение
            connection.disconnect();

            // Проверяем успешность запроса
            if (responseCode == 200) {
                // Парсим JSON и извлекаем информацию о торговых парах
                ObservableList<String> pairs = parseTradingPairs(response.toString());

                // Заполняем ComboBox
                comboBox.setItems(pairs);
            } else {
                System.out.println("Failed to fetch trading pairs. Response Code: " + responseCode);
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        return comboBox;
    }
    public double getMinLotSizeForBuy(String pairName) {
        double minLotSize = -1;
        SpotClient client = new SpotClientImpl();
        Market market = client.createMarket();
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", pairName);

        String result = market.exchangeInfo(parameters);
        System.out.println(result);
        minLotSize = parseMinLotSizeResponse(result);

        return minLotSize;
    }

    public int getPrecisionSizeForTicker(String pairName, boolean isBaseAsset) {
        int precisionSize;
        SpotClient client = new SpotClientImpl();
        Market market = client.createMarket();
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", pairName);

        String result = market.exchangeInfo(parameters);

        precisionSize = parsePrecisionSizeResponse(result, isBaseAsset);
        return precisionSize;
    }
    public boolean getOpenOrders(BinanceTokens tokens, String pairName){
        Map<String, Object> parameters = new LinkedHashMap<>();
        SpotClient spotClient = new SpotClientImpl(tokens.getApiKey(),tokens.getSecretKey(), baseURL);
        parameters.put("symbol", pairName);
        try {
            String result = spotClient.createTrade().getOrders(parameters);
            System.out.println(result);
            if(!result.isEmpty()){
                return true;
            }
            else {return false;}
        } catch (BinanceConnectorException e) {
            System.err.println((String) String.format("fullErrMessage: %s", e.getMessage()));
        } catch (BinanceClientException e) {
            System.err.println((String) String.format("fullErrMessage: %s \nerrMessage: %s \nerrCode: %d \nHTTPStatusCode: %d",
                    e.getMessage(), e.getErrMsg(), e.getErrorCode(), e.getHttpStatusCode()));
        }
        return false;
    }
    public String openOrder(BinanceTokens tokens, String symbol, double quantity) throws BinanceConnectorException, BinanceClientException{
        Map<String, Object> parameters = new LinkedHashMap<>();

        SpotClient client = new SpotClientImpl(tokens.getApiKey(), tokens.getSecretKey(), baseURL);

        parameters.put("symbol", symbol);
        parameters.put("side", "BUY");
        parameters.put("type", "MARKET");
        parameters.put("quoteOrderQty", quantity);

        return client.createTrade().newOrder(parameters);
//        try {
//            return client.createTrade().newOrder(parameters);
//        } catch (BinanceConnectorException e) {
//            System.err.printf("fullErrMessage: %s%n", e.getMessage());
//        } catch (BinanceClientException e) {
//            System.err.printf("fullErrMessage: %s \nerrMessage: %s \nerrCode: %d \nHTTPStatusCode: %d%n",
//                    e.getMessage(), e.getErrMsg(), e.getErrorCode(), e.getHttpStatusCode());
//        }
//
//        return null;
    }

    public String closeOrder(BinanceTokens tokens, String symbol, double quantity){
        Map<String, Object> parameters = new LinkedHashMap<>();

        SpotClient client = new SpotClientImpl(tokens.getApiKey(), tokens.getSecretKey(), baseURL);

        parameters.put("symbol", symbol);
        parameters.put("side", "SELL");
        parameters.put("type", "MARKET");
        parameters.put("quantity", quantity);

        try {
            return client.createTrade().newOrder(parameters);
        } catch (BinanceConnectorException e) {
            System.err.println((String) String.format("fullErrMessage: %s", e.getMessage()));
        } catch (BinanceClientException e) {
            System.err.println((String) String.format("fullErrMessage: %s \nerrMessage: %s \nerrCode: %d \nHTTPStatusCode: %d",
                    e.getMessage(), e.getErrMsg(), e.getErrorCode(), e.getHttpStatusCode()));
        }
        return null;
    }
    public List<PairPriceInfo> getActualPriceForPairs(BinanceTokens tokens, List<String> pairs) {
        SpotClient spotClient = new SpotClientImpl(tokens.getApiKey(),tokens.getSecretKey(), baseURL);

        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbols", pairs);

        String jsonResponse = spotClient.createMarket().bookTicker(parameters);
        return parsePairPriceInfo(jsonResponse);
    }
    public double getActualBidPriceForPair(String symbol) {
        BinanceTokens tokens = binancePairDAO.getTokens();
        SpotClient spotClient = new SpotClientImpl(tokens.getApiKey(), tokens.getSecretKey(), baseURL);
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);

        String jsonResponse = spotClient.createMarket().bookTicker(parameters);

        JSONObject json = new JSONObject(jsonResponse);


        return json.getDouble("bidPrice");
    }

    public int getOrderIdByPairName(BinanceTokens tokens, String symbol){
        Map<String, Object> parameters = new LinkedHashMap<>();
        int orderID = 0;
        SpotClient client = new SpotClientImpl(tokens.getApiKey(), tokens.getSecretKey(), baseURL);

        parameters.put("symbol", symbol);

        try {
            String result = client.createTrade().myTrades(parameters);
            System.out.println(result);
            orderID = parseOrderIdResponse(result);
            System.out.println(orderID);

        } catch (BinanceConnectorException e) {
            System.err.println((String) String.format("fullErrMessage: %s", e.getMessage()));
        } catch (BinanceClientException e) {
            System.err.println((String) String.format("fullErrMessage: %s \nerrMessage: %s \nerrCode: %d \nHTTPStatusCode: %d",
                    e.getMessage(), e.getErrMsg(), e.getErrorCode(), e.getHttpStatusCode()));
        }

        return orderID;
    }
    public void getAvgBuyPrice(BinanceTokens tokens, List<PairPriceInfo> pairs) {
        Map<String, Object> parameters = new LinkedHashMap<>();

        SpotClient client = new SpotClientImpl(tokens.getApiKey(), tokens.getSecretKey(), baseURL);
        for (PairPriceInfo pair: pairs){
            parameters.put("symbols", pair.getSymbol());
        }
        try {
            String result = client.createTrade().getOpenOrders(parameters);
            System.out.println(result);
        } catch (BinanceConnectorException e) {
            System.err.println((String) String.format("fullErrMessage: %s", e.getMessage()));
        } catch (BinanceClientException e) {
            System.err.println((String) String.format("fullErrMessage: %s \nerrMessage: %s \nerrCode: %d \nHTTPStatusCode: %d",
                    e.getMessage(), e.getErrMsg(), e.getErrorCode(), e.getHttpStatusCode()));
        }
    }

    //-----------------------------------------------------------
    //Ниже необходимые приватные методы для работы этого класса  |
    //-----------------------------------------------------------
    //метод для парсинга доступных торговых пар для торговли на бирже
    private ObservableList<String> parseTradingPairs(String jsonResponse) {
        // Код для парсинга JSON и извлечения торговых пар
        ObservableList<String> pairs = FXCollections.observableArrayList();
        List<String> allPairs = binancePairDAO.getAllPairsNames();
        try {
            // Преобразуем строку JSON в объект
            JSONObject json = new JSONObject(jsonResponse);

            // Извлекаем массив символов (торговых пар)
            JSONArray symbols = json.getJSONArray("symbols");

            // Перебираем торговые пары и извлекаем их названия
            for (int i = 0; i < symbols.length(); i++) {
                JSONObject symbol = symbols.getJSONObject(i);
                String pairName = symbol.getString("symbol");
                if (!allPairs.contains(pairName)) {
                    pairs.add(pairName);
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        return pairs;
    }
    //Метод для парсинга ответа от биржи с информацией о цене пары.
    private List<PairPriceInfo> parsePairPriceInfo(String jsonResponse) {
        List<PairPriceInfo> pairPriceInfoList = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject pairObject = jsonArray.getJSONObject(i);

                PairPriceInfo pairPriceInfo = new PairPriceInfo(
                        pairObject.getString("symbol"),
                        pairObject.getDouble("bidPrice"),
                        pairObject.getDouble("bidQty"),
                        pairObject.getDouble("askPrice"),
                        pairObject.getDouble("askQty")
                );

                pairPriceInfoList.add(pairPriceInfo);
            }
        } catch (JSONException e) {
            e.printStackTrace(); // Обработайте исключение по вашему усмотрению
        }

        return pairPriceInfoList;
    }
    //Метод для парсинга orderId
    private int parseOrderIdResponse(String jsonResponse) {
        int orderId = 0;
        try {
            JSONArray jsonArray = new JSONArray(jsonResponse);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject pairObject = jsonArray.getJSONObject(i);

                orderId = pairObject.getInt("orderId");
            }
        } catch (JSONException e) {
            e.printStackTrace(); // Обработайте исключение по вашему усмотрению
        }

        return orderId;
    }
    //Метод для парсинга минимального лота для пары
    private double parseMinLotSizeResponse(String jsonResponse) {
        double minLotSize = -1;
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray symbolsArray = jsonObject.getJSONArray("symbols");
        if (!symbolsArray.isEmpty()) {
            JSONObject symbolArray = symbolsArray.getJSONObject(0);
            JSONArray filtersArray = symbolArray.getJSONArray("filters");

            // Ищем фильтр LOT_SIZE
            for (int i = 0; i < filtersArray.length(); i++) {
                JSONObject filter = filtersArray.getJSONObject(i);
                if ("LOT_SIZE".equals(filter.getString("filterType"))) {
                    String minQty = filter.getString("minQty");
                    minLotSize = Double.parseDouble(minQty);
                }
            }
        }
        return minLotSize;
    }
    //Метод для парсинга размера лота (StepSize) для пары
    private int parsePrecisionSizeResponse(String jsonResponse, boolean isBaseAsset) {
        int stepSize = -1;
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray symbolsArray = jsonObject.getJSONArray("symbols");
            for (int i = 0; i < symbolsArray.length(); i++) {
                JSONObject symbolObject = symbolsArray.getJSONObject(i);
                if(isBaseAsset){
                    stepSize = symbolObject.getInt("baseAssetPrecision");
                }
                else {
                    stepSize = symbolObject.getInt("quoteAssetPrecision");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return stepSize;
    }

    //-----------------------------------------------------------
    //Ниже необходимые публичные методы для работы этого класса  |
    //-----------------------------------------------------------

    public double convertingInBaseAsset(double quoteAsset, String symbol){
        double baseAssetSize;

        if (quoteAsset < 1) { baseAssetSize = quoteAsset * getActualBidPriceForPair(symbol);}
        else { baseAssetSize = quoteAsset / getActualBidPriceForPair(symbol);}

        return baseAssetSize;
    }
}
