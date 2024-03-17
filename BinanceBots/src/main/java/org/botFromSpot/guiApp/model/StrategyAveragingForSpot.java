package org.botFromSpot.guiApp.model;

import com.binance.connector.client.exceptions.BinanceClientException;
import com.binance.connector.client.exceptions.BinanceConnectorException;
import javafx.application.Platform;
import org.botFromSpot.guiApp.AppMainController;
import org.botFromSpot.guiApp.services.BinanceApiMethods;
import org.botFromSpot.guiApp.services.BinancePairDAO;
import org.botFromSpot.guiApp.services.BotProvider;
import org.botFromSpot.guiApp.services.PairConfiguration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class StrategyAveragingForSpot {
    private final BotConfiguration botConfiguration;
    private BinanceApiMethods binanceApiMethods;
    private BinancePairDAO binancePairDAO;
    private AppMainController appMainController;
    private BotProvider botProvider;
    public void setBinanceApiMethods(BinanceApiMethods binanceApiMethods) {this.binanceApiMethods = binanceApiMethods;}
    public void setBinancePairDAO(BinancePairDAO binancePairDAO) {this.binancePairDAO = binancePairDAO;}
    public void setAppMainController(AppMainController appMainController) {this.appMainController = appMainController;}
    public void setBotProvider(BotProvider botProvider) {this.botProvider = botProvider;}

    public StrategyAveragingForSpot(BotConfiguration botConfiguration,
                                    AppMainController appMainController,
                                    BinancePairDAO binancePairDAO,
                                    BinanceApiMethods binanceApiMethods) {
        this.botConfiguration = botConfiguration;
        this.appMainController = appMainController;
        this.binancePairDAO = binancePairDAO;
        this.binanceApiMethods = binanceApiMethods;
    }

    public void start(){
        BinancePair pair = botConfiguration.getPair();
        PairConfiguration pairConfiguration = botConfiguration.getConfiguration();
        System.out.println("Должен открыть ордер объёмом: " + pairConfiguration.getStartingLotVolume());

        TradeInfo tradeInfo = callOpenOrder(pair,
                pairConfiguration.getStartingLotVolume(),
                1,
                true);

        if (tradeInfo != null){
            tradeInfo.setProfit(0);
            binancePairDAO.addTradeInfo(tradeInfo);

            callAddLogMethod(String.format("Бот открыл ордер для: %s. По цене: %f. Объём: %f",
                    pair.getPairName(),
                    tradeInfo.getBuyPrice(),
                    calculateLotSizeInQuoteAsset(tradeInfo)));
            System.out.printf("Бот открыл ордер для: %s. По цене: %f. Объём: %f%n",
                    pair.getPairName(),
                    tradeInfo.getBuyPrice(),
                    calculateLotSizeInQuoteAsset(tradeInfo));
        }
        else {
            callAddLogMethod("ОШИБКА!!! ПЕРВЫЙ ОРДЕР ПО НОВОЙ ПАРЕ НЕ ОТКРЫЛСЯ");
            System.out.println("ОШИБКА!!! ПЕРВЫЙ ОРДЕР ПО НОВОЙ ПАРЕ НЕ ОТКРЫЛСЯ");
        }


    }
    public void update(PairPriceInfo pairPriceInfo){
        BinancePair pair = botConfiguration.getPair();
        PairConfiguration pairConfiguration = botConfiguration.getConfiguration();
        TradeInfo pairTradeInfo = binancePairDAO.getTradeInfoForPair(pair.getId());

        //Проверка, что был получен объект с информацией о паре из БД
        if(pairTradeInfo!=null) {
            System.out.println("В методе update: \n" + pairTradeInfo + "\n" + pairConfiguration);
            //В случае тейк-профита
            if (pairPriceInfo.getBidPrice() >= pairTradeInfo.getBuyPrice() + pairConfiguration.getTakeProfit()) {
                //Закрытие ордера
                String result;
                try {
                    result = binanceApiMethods.closeOrder(
                            binancePairDAO.getTokens(),
                            pairPriceInfo.getSymbol(),
                            pairTradeInfo.getLotSize());
                } catch (BinanceConnectorException | BinanceClientException e) {
                    result = null;
                    callAddLogMethod(e.getMessage());
                    e.printStackTrace();
                }
                //В случае успешного закрытия ордера
                if (result != null) {
                    //Подсчёт профита по сделке
                    BigDecimal bdProfit = BigDecimal.valueOf((pairPriceInfo.getBidPrice() * pairTradeInfo.getLotSize()) -
                            (pairTradeInfo.getBuyPrice()*pairTradeInfo.getLotSize()));
                    bdProfit = bdProfit.setScale(
                            5,
                            RoundingMode.HALF_UP);
                    double profitInThisTrade = bdProfit.doubleValue();
                    //Обновление общего профита
                    double oldProfit = binancePairDAO.getProfitInTradeInfoForPair(pairTradeInfo.getPairId());
                    pairTradeInfo.setProfit(oldProfit+profitInThisTrade);
                    binancePairDAO.updateProfitInTradeInfo(pairTradeInfo);
                    //Добавление информации в лог
                    callAddLogMethod("Тейк профит в паре " + pairPriceInfo.getSymbol() + "(" +
                            profitInThisTrade + ")");
                    System.out.println("Тейк профит по сделке с " + pairPriceInfo.getSymbol());
                    //Если автосушка не включена
                    if (!binancePairDAO.getAutoDryingInTradeInfoForPair(pairTradeInfo.getPairId())){
                        //Открываем новую сетку
                        TradeInfo newTradeInfo = callOpenOrder(pair,
                                pairConfiguration.getStartingLotVolume(),
                                1,
                                true);
                        if(newTradeInfo != null){
                            binancePairDAO.updateTradeInfo(newTradeInfo);
                            //Добавляем запись в лог
                            callAddLogMethod(String.format("Бот открыл новую сетку для: %s. По цене: %f. Объём: %f",
                                    pair.getPairName(),
                                    newTradeInfo.getBuyPrice(),
                                    calculateLotSizeInQuoteAsset(newTradeInfo)));
                            System.out.printf("Бот открыл новую сетку для: %s. По цене: %f. Объём: %f%n",
                                    pair.getPairName(),
                                    newTradeInfo.getBuyPrice(),
                                    calculateLotSizeInQuoteAsset(newTradeInfo));
                        }
                        //В случае ошибки при открытии новой сетки
                        else {
                            callAddLogMethod("С ОТКРЫТИЕМ НОВОЙ СЕТКИ ПОСЛЕ ТЕЙК-ПРОФИТА ЧТО-ТО НЕ ТАК");
                            System.out.println("С ОТКРЫТИЕМ НОВОЙ СЕТКИ ПОСЛЕ ТЕЙК-ПРОФИТА ЧТО-ТО НЕ ТАК");
                        }
                    }
                    //Если включена автосушка
                    else {
                        botProvider.stopBot(botConfiguration);
                    }
                }
                //В случае ошибки при закрытии ордера
                else {
                    callAddLogMethod("С ЗАКРЫТИЕМ ОРДЕРА ПО ТЕЙК ПРОФИТУ ЧТО-ТО НЕ ТАК");
                    System.out.println("С ЗАКРЫТИЕМ ОРДЕРА ПО ТЕЙК ПРОФИТУ ЧТО-ТО НЕ ТАК");
                }
            }
            //В случае когда нет тейк профита
            else {
                //Проверка, что можно усредняться и не включена автосушка
                if(pairTradeInfo.isTradeAllowed() && !pairTradeInfo.isAutoDrying()){
                    //Если выполняются условия для усреднения
                    if (pairPriceInfo.getBidPrice() <= pairTradeInfo.getBuyPrice()-pairConfiguration.getAveragingStep()) {
                        //Если прошло достаточно времени для усреднения
                        if (System.currentTimeMillis() - pairTradeInfo.getTransactTime() >=
                                (long) pairConfiguration.getAveragingTimer() * 60 * 1000) {

                            //Выставляем новый объём для усреднения
                            double averagingLotSize = pairConfiguration.getStartingLotVolume();
                            for (int i = 1; i < pairTradeInfo.getOpenedOrders()+1; i++) {
                                averagingLotSize = averagingLotSize * pairConfiguration.getMultiplier();
                            }

                            BigDecimal bdAveragingLotSize = BigDecimal.valueOf(averagingLotSize);
                            bdAveragingLotSize = bdAveragingLotSize.setScale(
                                    binanceApiMethods.getPrecisionSizeForTicker(pair.getPairName(), true),
                                    RoundingMode.HALF_UP);
                            averagingLotSize = bdAveragingLotSize.doubleValue();
                            TradeInfo averagingTradeInfo = callOpenOrder(pair,
                                    averagingLotSize,
                                    pairTradeInfo.getOpenedOrders()+1,
                                    true
                                    );
                            //Открываем новый усредняющий ордер
                            if(averagingTradeInfo != null){

                                callAddLogMethod(String.format("Бот открыл усредняющий ордер для: %s. По цене: %f. Объём: %f",
                                        pair.getPairName(),
                                        averagingTradeInfo.getBuyPrice(),
                                        calculateLotSizeInQuoteAsset(averagingTradeInfo)));
                                System.out.printf("Бот открыл усредняющий ордер для: %s. По цене: %f. Объём: %f%n",
                                        pair.getPairName(),
                                        averagingTradeInfo.getBuyPrice(),
                                        calculateLotSizeInQuoteAsset(averagingTradeInfo));

                                BigDecimal bdNewLotSize = BigDecimal.valueOf(pairTradeInfo.getLotSize() + averagingTradeInfo.getLotSize());
                                bdNewLotSize = bdNewLotSize.setScale(
                                        binanceApiMethods.getPrecisionSizeForTicker(pair.getPairName(), true),
                                        RoundingMode.HALF_UP);
                                double newLotSize = bdNewLotSize.doubleValue();

                                double newBuyPrice = ( ( pairTradeInfo.getBuyPrice() * pairTradeInfo.getLotSize() ) +
                                        ( averagingTradeInfo.getBuyPrice() * averagingTradeInfo.getLotSize() ) ) / newLotSize;
                                averagingTradeInfo.setPairId(pairTradeInfo.getPairId());
                                averagingTradeInfo.setBuyPrice(newBuyPrice);
                                averagingTradeInfo.setLotSize(newLotSize);
                                if(averagingTradeInfo.getOpenedOrders() < 5) {
                                    averagingTradeInfo.setTradeAllowed(true);
                                }
                                else {
                                    averagingTradeInfo.setTradeAllowed(false);
                                }
                                binancePairDAO.updateTradeInfo(averagingTradeInfo);
                                callAddLogMethod(String.format("Cредняя цена покупки для %s: %f. Общий объём: %f",
                                        pair.getPairName(),
                                        averagingTradeInfo.getBuyPrice(),
                                        calculateLotSizeInQuoteAsset(averagingTradeInfo)));
                            }
                            else {
                                callAddLogMethod("С ОТКРЫТИЕМ УСРЕДНЯЮЩЕГО ОРДЕРА ЧТО-ТО НЕ ТАК");
                                System.out.println("С ОТКРЫТИЕМ УСРЕДНЯЮЩЕГО ОРДЕРА ЧТО-ТО НЕ ТАК");
                            }
                        }
                        //Не прошло достаточно времени для усреднения
                        else {
                        }
                    }
                    //Если нет условий для усреднения
                    else {
                    }
                }
                //торговля парой запрещена или включена автосушка
                else {
                }
            }
        }
        //Нет данных из бд
        else {
        }
    }

    public void autoDrying() {
        BinancePair pair = botConfiguration.getPair();
        TradeInfo pairTradeInfo = binancePairDAO.getTradeInfoForPair(binancePairDAO.getPairIdByPairName(pair.getPairName()));
        pairTradeInfo.setAutoDrying(true);
        binancePairDAO.updateAutoDryingInTradeInfo(pairTradeInfo);
    }
    public void stop(){
        BinancePair pair = botConfiguration.getPair();
        pair.setId(binancePairDAO.getPairIdByPairName(pair.getPairName()));
        PairConfiguration pairConfiguration = botConfiguration.getConfiguration();
        double lotSize;
        if (binancePairDAO.getTradeInfoForPair(pair.getId()).getBuyPrice() > 1) {
            lotSize = binancePairDAO.getTradeInfoForPair(pair.getId()).getBuyPrice() *
                    binancePairDAO.getTradeInfoForPair(pair.getId()).getLotSize();
        }
        else {
            lotSize = binancePairDAO.getTradeInfoForPair(pair.getId()).getBuyPrice() /
                    binancePairDAO.getTradeInfoForPair(pair.getId()).getLotSize();
        }
        System.out.println("Объём для продажи: " + lotSize);
        if(lotSize != 0){
            String result = binanceApiMethods.closeOrder(
                    binancePairDAO.getTokens(),
                    pair.getPairName(),
                    binancePairDAO.getTradeInfoForPair(pair.getId()).getLotSize());
            if(result!=null){
                Platform.runLater(this::updateGUIBalance);

                binancePairDAO.deleteTradesInfo(pairConfiguration.getPairId());

                System.out.println("Бот закрыл все ордера для: " + pair.getPairName());
                callAddLogMethod("Бот закрыл все ордера для: " + pair.getPairName());

            }
            else {
                Platform.runLater(() -> {
                    appMainController.testTextOutput.setText("C ЗАКРЫТИЕМ ОРДЕРОВ ВО ВРЕМЯ ОСТАНОВКИ ЧТО-ТО НЕ ТАК");
                });
                System.out.println("C ЗАКРЫТИЕМ ОРДЕРОВ ВО ВРЕМЯ ОСТАНОВКИ ЧТО-ТО НЕ ТАК");
            }
        }
        else{
            System.out.println("Тут блок кода при автосушке");
        }


    }


    //СЛУЖЕБНЫЕ МЕТОДЫ ДЛЯ КЛАССА
    //Парсинг ответа после успешной сделке для получения transactTime buyPrice, lotSize
    private TradeInfo parseOpenedOrderResult(String jsonResponse){
        TradeInfo tradeInfo = new TradeInfo();
        JSONObject jsonObject = new JSONObject(jsonResponse);
        String symbol = jsonObject.getString("symbol");
        long transactTime = jsonObject.getLong("transactTime");
        JSONArray fillsArray = jsonObject.getJSONArray("fills");

        double totalBuyPrice = 0.0;
        double lotSize = 0.0;
        for (int i = 0; i < fillsArray.length(); i++) {
            JSONObject fill = fillsArray.getJSONObject(i);
            double price = fill.getDouble("price");
            double qty = fill.getDouble("qty");
            totalBuyPrice += price * qty;
            lotSize += qty;
        }

        double buyPrice = totalBuyPrice / lotSize;

        BigDecimal bdLotSize = BigDecimal.valueOf(lotSize);
        bdLotSize = bdLotSize.setScale(
                binanceApiMethods.getPrecisionSizeForTicker(symbol, true),
                RoundingMode.HALF_DOWN);
        lotSize = bdLotSize.doubleValue();

        tradeInfo.setTransactTime(transactTime);
        tradeInfo.setBuyPrice(buyPrice);
        tradeInfo.setLotSize(lotSize);

        return tradeInfo;
    }
    //Вызов метода для открытия ордера
    private TradeInfo callOpenOrder(BinancePair pair, double quantity, int openedOrder, boolean isTradeAllowed) {
        String result;
        try {
            result = binanceApiMethods.openOrder(
                    binancePairDAO.getTokens(),
                    pair.getPairName(),
                    quantity);
        } catch (BinanceConnectorException | BinanceClientException e) {
            result = null;
            appMainController.addLogEntry(e.getMessage());
            e.printStackTrace();
        }
        TradeInfo tradeInfo;
        if (result != null) {
            updateGUIBalance();

            tradeInfo = parseOpenedOrderResult(result);
            tradeInfo.setPairId(pair.getId());
            tradeInfo.setOpenedOrders(openedOrder);
            tradeInfo.setTradeAllowed(isTradeAllowed);
        }
        else {
            tradeInfo = null;
        }

        return tradeInfo;
    }
    //Вызов метода для записи в лог
    private void callAddLogMethod(String logInfo) {
        Platform.runLater(() -> {appMainController.addLogEntry(logInfo);});
    }
    //Обновляем баланс в GUI
    private void updateGUIBalance() {
        if (appMainController.selectedStock.equals("TestNetBinance")){
            String currentBalance = String.valueOf(binanceApiMethods.getAccountBalanceForTestNet(binancePairDAO.getTokens()));
            Platform.runLater(() -> {appMainController.balanceAcc.setText(currentBalance);});
        }

    }
    //Считает объём лота в дополнительной валюте
    private double calculateLotSizeInQuoteAsset(TradeInfo tradeInfo) {
        double thisTradeLotSize;
        if(tradeInfo.getBuyPrice() > 1){
            thisTradeLotSize = tradeInfo.getBuyPrice()*tradeInfo.getLotSize();
        }
        else {
            thisTradeLotSize = tradeInfo.getBuyPrice()/tradeInfo.getLotSize();
        }
        return thisTradeLotSize;
    }
}
