package org.botFromSpot.guiApp.model;
//Тестовая сеть
//bwKtUIBVFBkRktAieumDnDwb7YP9DTOACR9SOgz4yP7k7DOrqOSAgjI6fB3Emjkv
//Qvu0Gfdn0m4OXm5RKI9vqPf8XAe3TIBrxqN8yvZgKoVShAmpd9qrFTP3pJ1o9TmA
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
