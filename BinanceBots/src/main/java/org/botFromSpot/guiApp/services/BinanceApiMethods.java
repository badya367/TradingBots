package org.botFromSpot.guiApp.services;

import com.binance.connector.client.SpotClient;
import com.binance.connector.client.exceptions.BinanceClientException;
import com.binance.connector.client.exceptions.BinanceConnectorException;
import com.binance.connector.client.impl.SpotClientImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import org.botFromSpot.guiApp.model.BinanceTokens;
import org.botFromSpot.guiApp.services.binanceTestNetServices.TestNetSpotClient;
import org.botFromSpot.guiApp.services.binanceTestNetServices.TestNetSpotClientImpl;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class BinanceApiMethods {
    private BinancePairDAO binancePairDAO;

    public void setBinancePairDAO(BinancePairDAO binancePairDAO) {
        this.binancePairDAO = binancePairDAO;
    }

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
        double balance = client.createWallet().getAvailableBalanceInUSDT(jsonAnswerAccountInfo,"USDT");
        return balance;
    }
    public ComboBox<String> allPairs() {
        ComboBox<String> comboBox = new ComboBox<>();

        try {
            // Формируем URL для получения информации о торговых парах
            URL url = new URL("https://testnet.binance.vision/api/v3/exchangeInfo");

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

    public boolean getOpenOrders(BinanceTokens tokens, String pairName){
        Map<String, Object> parameters = new LinkedHashMap<>();
        SpotClient spotClient = new SpotClientImpl(tokens.getApiKey(),tokens.getSecretKey(), "https://testnet.binance.vision");
        parameters.put("symbol", pairName);
        try {
            String result = spotClient.createTrade().getOpenOrders(parameters);
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
    //-----------------------------------------------------------
    //Ниже необходимые приватные методы для работы этого класса  |
    //-----------------------------------------------------------
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
}
