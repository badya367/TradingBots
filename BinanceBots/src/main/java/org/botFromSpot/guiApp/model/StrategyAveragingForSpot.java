package org.botFromSpot.guiApp.model;

import javafx.application.Platform;
import org.botFromSpot.guiApp.AppMainController;
import org.botFromSpot.guiApp.services.BinanceApiMethods;
import org.botFromSpot.guiApp.services.BinancePairDAO;
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
    public void setBinanceApiMethods(BinanceApiMethods binanceApiMethods) {this.binanceApiMethods = binanceApiMethods;}
    public void setBinancePairDAO(BinancePairDAO binancePairDAO) {
        this.binancePairDAO = binancePairDAO;
    }
    public void setAppMainController(AppMainController appMainController) {this.appMainController = appMainController;}

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
        String result = binanceApiMethods.openOrder(
                binancePairDAO.getTokens(),
                pair.getPairName(),
                pairConfiguration.getStartingLotVolume());

        //System.out.println("Ответ от создания ордера:\n" + result);
        if (result != null){
            appMainController.balanceAcc.setText(
                    String.valueOf(binanceApiMethods.getAccountBalanceForTestNet(binancePairDAO.getTokens()))
            );

            TradeInfo tradeInfo = parseOpenedOrderResult(result);
            tradeInfo.setPairId(pairConfiguration.getPairId());
            tradeInfo.setOpenedOrders(1);
            tradeInfo.setTradeAllowed(true);

            binancePairDAO.addTradeInfo(tradeInfo);
            System.out.printf("Бот открыл ордер для: %s. По цене: %f", pair.getPairName(), tradeInfo.getBuyPrice());
            if(tradeInfo.getBuyPrice() > 1){
                 System.out.printf(" Объёмом: %f%n", tradeInfo.getBuyPrice()*tradeInfo.getLotSize());
            }
            else {
                System.out.printf("Объёмом: %f%n", tradeInfo.getBuyPrice()/tradeInfo.getLotSize());
            }

        }
        else {
            appMainController.testTextOutput.setText("ОШИБКА!!! ПЕРВЫЙ ОРДЕР ПО НОВОЙ ПАРЕ НЕ ОТКРЫЛСЯ");
            System.out.println("ОШИБКА!!! ПЕРВЫЙ ОРДЕР ПО НОВОЙ ПАРЕ НЕ ОТКРЫЛСЯ");
        }


    }
    public void update(PairPriceInfo pairPriceInfo){
        BinancePair pair = botConfiguration.getPair();
        PairConfiguration pairConfiguration = botConfiguration.getConfiguration();

        TradeInfo pairTradeInfo = binancePairDAO.getTradeInfoForPair(pair.getId());

        if(pairTradeInfo!=null) {
            System.out.println("В методе update: \n" + pairTradeInfo);
            if (pairPriceInfo.getBidPrice() >= pairTradeInfo.getBuyPrice() + pairConfiguration.getTakeProfit()) {
                //Это тейк-профит

                String result = binanceApiMethods.closeOrder(
                        binancePairDAO.getTokens(),
                        pairPriceInfo.getSymbol(),
                        pairTradeInfo.getLotSize());
                if (result != null) {
                    System.out.println("Тейк профит по сделке с " + pairPriceInfo.getSymbol());
                    //Открываем новую сетку
                    String openOrderResult = binanceApiMethods.openOrder(
                            binancePairDAO.getTokens(),
                            pair.getPairName(),
                            pairConfiguration.getStartingLotVolume());

                    Platform.runLater(() -> {
                        appMainController.balanceAcc.setText(
                                String.valueOf(binanceApiMethods.getAccountBalanceForTestNet(binancePairDAO.getTokens()))
                        );
                    });
                    //System.out.println("Ответ от создания ордера:\n" + result);
                    if (openOrderResult != null) {

                        TradeInfo newTradeInfo = parseOpenedOrderResult(openOrderResult);
                        newTradeInfo.setPairId(pairConfiguration.getPairId());
                        newTradeInfo.setOpenedOrders(1);
                        newTradeInfo.setTradeAllowed(true);
                        binancePairDAO.updateTradeInfo(newTradeInfo);
                        System.out.printf("Бот открыл новую сетку для: %s. По цене: %f", pair.getPairName(), newTradeInfo.getBuyPrice());
                        if(newTradeInfo.getBuyPrice() > 1){
                            System.out.printf(" Объёмом: %f%n", newTradeInfo.getBuyPrice()*newTradeInfo.getLotSize());
                        }
                        else {
                            System.out.printf("Объёмом: %f%n", newTradeInfo.getBuyPrice()/newTradeInfo.getLotSize());
                        }
                        Platform.runLater(() -> {
                            appMainController.balanceAcc.setText(
                                    String.valueOf(binanceApiMethods.getAccountBalanceForTestNet(binancePairDAO.getTokens()))
                            );
                        });
                    }
                    else {
                        Platform.runLater(() -> {
                            appMainController.testTextOutput.setText("С ОТКРЫТИЕМ НОВОЙ СЕТКИ ПОСЛЕ ТЕЙК-ПРОФИТА ЧТО-ТО НЕ ТАК");
                        });

                        System.out.println("С ОТКРЫТИЕМ НОВОЙ СЕТКИ ПОСЛЕ ТЕЙК-ПРОФИТА ЧТО-ТО НЕ ТАК");
                    }
                }
                else {
                    Platform.runLater(() -> {
                        appMainController.testTextOutput.setText("С ЗАКРЫТИЕМ ОРДЕРА ПО ТЕЙК ПРОФИТУ ЧТО-ТО НЕ ТАК");
                    });
                    System.out.println("С ЗАКРЫТИЕМ ОРДЕРА ПО ТЕЙК ПРОФИТУ ЧТО-ТО НЕ ТАК");
                }
            }
            else {
                if(pairTradeInfo.isTradeAllowed()){
                    if (pairPriceInfo.getBidPrice() <= pairTradeInfo.getBuyPrice()-pairConfiguration.getAveragingStep()) {
                        //Тут разрешено усредняться
                        if (System.currentTimeMillis() - pairTradeInfo.getTransactTime() >=
                                pairConfiguration.getAveragingTimer() * 60 * 1000) {
                            // Прошло достаточно времени для усреднения

                            //Выставляем новый объём для усреднения
                            double averagingLotSize = pairConfiguration.getStartingLotVolume();
                            for (int i = 1; i < pairTradeInfo.getOpenedOrders()+1; i++) {
                                System.out.println("В цикле перед рассчётом lotSize: " + averagingLotSize);
                                averagingLotSize = averagingLotSize * pairConfiguration.getMultiplier();
                                System.out.println("В цикле после рассчёта lotSize: " + averagingLotSize);
                            }

                            BigDecimal bdAveragingLotSize = BigDecimal.valueOf(averagingLotSize);
                            bdAveragingLotSize = bdAveragingLotSize.setScale(
                                    binanceApiMethods.getPrecisionSizeForTicker(pair.getPairName(), true),
                                    RoundingMode.HALF_UP);
                            averagingLotSize = bdAveragingLotSize.doubleValue();

                            //Открываем новый усредняющий ордер
                            String result = binanceApiMethods.openOrder(
                                    binancePairDAO.getTokens(),
                                    pairPriceInfo.getSymbol(),
                                    averagingLotSize);
                            if (result != null) {
                                //Трейд успешно выполнился

                                TradeInfo newTradeInfo = parseOpenedOrderResult(result);

                                System.out.printf("Бот открыл усредняющий ордер для: %s. По цене: %f", pair.getPairName(), newTradeInfo.getBuyPrice());
                                if(newTradeInfo.getBuyPrice() > 1){
                                    System.out.printf(" Объёмом: %f%n", newTradeInfo.getBuyPrice()*newTradeInfo.getLotSize());
                                }
                                else {
                                    System.out.printf("Объёмом: %f%n", newTradeInfo.getBuyPrice()/newTradeInfo.getLotSize());
                                }

                                BigDecimal bdNewLotSize = BigDecimal.valueOf(pairTradeInfo.getLotSize() + newTradeInfo.getLotSize());
                                bdNewLotSize = bdNewLotSize.setScale(
                                        binanceApiMethods.getPrecisionSizeForTicker(pair.getPairName(), true),
                                        RoundingMode.HALF_UP);
                                double newLotSize = bdNewLotSize.doubleValue();

                                double newBuyPrice = ( ( pairTradeInfo.getBuyPrice() * pairTradeInfo.getLotSize() ) +
                                        ( newTradeInfo.getBuyPrice() * newTradeInfo.getLotSize() ) ) / newLotSize;
                                newTradeInfo.setPairId(pairTradeInfo.getPairId());
                                newTradeInfo.setBuyPrice(newBuyPrice);
                                newTradeInfo.setLotSize(newLotSize);
                                newTradeInfo.setOpenedOrders(pairTradeInfo.getOpenedOrders()+1);
                                if(newTradeInfo.getOpenedOrders() < 5) {
                                    newTradeInfo.setTradeAllowed(true);
                                }
                                else {
                                    newTradeInfo.setTradeAllowed(false);
                                }

                                binancePairDAO.updateTradeInfo(newTradeInfo);
                                Platform.runLater(() -> {
                                    appMainController.balanceAcc.setText(
                                            String.valueOf(binanceApiMethods.getAccountBalanceForTestNet(binancePairDAO.getTokens()))
                                    );
                                });

                            }
                            else {
                                Platform.runLater(() -> {
                                    appMainController.testTextOutput.setText("С ОТКРЫТИЕМ УСРЕДНЯЮЩЕГО ОРДЕРА ЧТО-ТО НЕ ТАК");
                                });
                                System.out.println("С ОТКРЫТИЕМ УСРЕДНЯЮЩЕГО ОРДЕРА ЧТО-ТО НЕ ТАК");
                            }


                        } else {
                            //Не прошло достаточно времени для усреднения
                        }
                    }
                }
                else {
                    //торговля парой запрещена
                }
            }
        }
        else {
            //Нет данных из бд
        }


        //System.out.println("Бот обновлен для конфигурации:  " + pair.getPairName() + " " + pairConfiguration.getSumToTrade());
    }
    public void stop(){
        BinancePair pair = botConfiguration.getPair();
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
        String result = binanceApiMethods.closeOrder(
                binancePairDAO.getTokens(),
                pair.getPairName(),
                binancePairDAO.getTradeInfoForPair(pair.getId()).getLotSize());
        if(result!=null){
            appMainController.balanceAcc.setText(
                    String.valueOf(binanceApiMethods.getAccountBalanceForTestNet(binancePairDAO.getTokens()))
            );
            binancePairDAO.deleteTradesInfo(pairConfiguration.getPairId());

            System.out.println("Бот закрыл все ордера для: " + pair.getPairName());
        }
        else {
            appMainController.testTextOutput.setText("C ЗАКРЫТИЕМ ОРДЕРОВ ВО ВРЕМЯ ОСТАНОВКИ ЧТО-ТО НЕ ТАК");
            System.out.println("C ЗАКРЫТИЕМ ОРДЕРОВ ВО ВРЕМЯ ОСТАНОВКИ ЧТО-ТО НЕ ТАК");
        }

    }


    //СЛУЖЕБНЫЕ МЕТОДЫ ДЛЯ КЛАССА
    //Парсинг ответа после успешной сделке для получения transactTime buyPrice, lotSize
    private TradeInfo parseOpenedOrderResult(String jsonResponse){
        TradeInfo tradeInfo = new TradeInfo();
        JSONObject jsonObject = new JSONObject(jsonResponse);
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
        tradeInfo.setTransactTime(transactTime);
        tradeInfo.setBuyPrice(buyPrice);
        tradeInfo.setLotSize(lotSize);

        return tradeInfo;
    }

}
