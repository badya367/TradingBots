package org.botFromSpot.guiApp.services.binanceTestNetServices;

import com.binance.connector.client.enums.HttpMethod;
import com.binance.connector.client.utils.ProxyAuth;
import com.binance.connector.client.utils.RequestHandler;
import com.binance.connector.client.utils.signaturegenerator.HmacSignatureGenerator;
import com.binance.connector.client.utils.signaturegenerator.SignatureGenerator;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

public class TestNetWallet {
    private final String baseUrl;
    private final RequestHandler requestHandler;
    private final boolean showLimitUsage;
    private final String WALLET_BALANCE = "/api/v3/account";
    public TestNetWallet(String baseUrl, String apiKey, String secretKey, boolean showLimitUsage, ProxyAuth proxy) {
        this.baseUrl = baseUrl;
        this.requestHandler = new RequestHandler(apiKey, new HmacSignatureGenerator(secretKey), proxy);
        this.showLimitUsage = showLimitUsage;
    }

    public TestNetWallet(String baseUrl, String apiKey, SignatureGenerator signatureGenerator, boolean showLimitUsage, ProxyAuth proxy) {
        this.baseUrl = baseUrl;
        this.requestHandler = new RequestHandler(apiKey, signatureGenerator, proxy);
        this.showLimitUsage = showLimitUsage;
    }
    public String walletBalance(Map<String, Object> parameters) {
        return this.requestHandler.sendSignedRequest(this.baseUrl, "/api/v3/account", parameters, HttpMethod.GET, this.showLimitUsage);
    }

    public double getAvailableBalanceInUSDT(String jsonResponse, String coin) {
        // Преобразование строки JSON в объект JSONObject
        JSONObject jsonObject = new JSONObject(jsonResponse);

        // Получение массива балансов
        JSONArray balances = jsonObject.getJSONArray("balances");

        // Инициализация переменной для хранения баланса в BTC
        double usdtBalance = 0.0;

        // Поиск баланса по символу "coin"
        for (int i = 0; i < balances.length(); i++) {
            JSONObject balance = balances.getJSONObject(i);
            String asset = balance.getString("asset");

            if (coin.equals(asset)) {
                // Получение свободного баланса в BTC
                usdtBalance = balance.getDouble("free");
                break;
            }
        }

        return usdtBalance;
    }
}
