package org.botFromSpot.guiApp.services.binanceTestNetServices;

import com.binance.connector.client.utils.ProxyAuth;
import com.binance.connector.client.utils.signaturegenerator.HmacSignatureGenerator;
import com.binance.connector.client.utils.signaturegenerator.SignatureGenerator;

public class TestNetSpotClientImpl implements TestNetSpotClient {
    private final String apiKey;
    private final SignatureGenerator signatureGenerator;
    private final String baseUrl;
    private boolean showLimitUsage;
    private ProxyAuth proxy;
    public TestNetSpotClientImpl() {
        this("https://testnet.binance.vision");
    }

    public TestNetSpotClientImpl(String baseUrl) {
        this("", (SignatureGenerator)null, baseUrl);
    }

    public TestNetSpotClientImpl(String baseUrl, boolean showLimitUsage) {
        this(baseUrl);
        this.showLimitUsage = showLimitUsage;
    }

    public TestNetSpotClientImpl(String apiKey, String secretKey) {
        this(apiKey, secretKey, "https://testnet.binance.vision");
    }

    public TestNetSpotClientImpl(String apiKey, String secretKey, String baseUrl) {
        this(apiKey, (SignatureGenerator)(new HmacSignatureGenerator(secretKey)), baseUrl);
    }

    public TestNetSpotClientImpl(String apiKey, SignatureGenerator signatureGenerator, String baseUrl) {
        this.showLimitUsage = false;
        this.proxy = null;
        this.apiKey = apiKey;
        this.signatureGenerator = signatureGenerator;
        this.baseUrl = baseUrl;
    }

    @Override
    public TestNetWallet createWallet() {
        return new TestNetWallet(this.baseUrl, this.apiKey, this.signatureGenerator, this.showLimitUsage, this.proxy);
    }
}
