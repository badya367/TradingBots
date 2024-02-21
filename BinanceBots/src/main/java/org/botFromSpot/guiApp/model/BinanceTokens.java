package org.botFromSpot.guiApp.model;
//Тестовая сеть
//TLD4kVp4z7X0dMP94MKW7pzLSo2D8wCt3Ga9KvvqBZZUf8tjBPeO75EDsKtAkGYf
//koQMdHB23EKkaQsJYtOVM8WG9TVMYSVZ00DVYUBQqXYSj9LHxUgRGgayXPLZ2Hmn
public class BinanceTokens {
    private final String apiKey;
    private final String secretKey;

    public BinanceTokens(String apiKey, String secretKey){
        if (apiKey.isEmpty() || secretKey.isEmpty()) {
            throw new IllegalArgumentException("API ключи не могут быть пустыми");
        }
        this.apiKey = apiKey;
        this.secretKey = secretKey;
    }
    public String getApiKey() {
        return apiKey;
    }

    public String getSecretKey() {
        return secretKey;
    }
}
