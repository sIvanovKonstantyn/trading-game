package com.tradinggame;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SymbolState {
    private final String symbol;
    private double tradingFee;
    private List<Order> openOrders;
    private List<Order> executedOrders;
    private List<PriceData> priceHistory;
    private List<PriceData> allPriceHistory;
    private BinanceApiClient apiClient;

    public SymbolState(String symbol, double initialBalance, double tradingFee) {
        this.symbol = symbol;
        this.tradingFee = tradingFee;
        this.openOrders = new ArrayList<>();
        this.executedOrders = new ArrayList<>();
        this.priceHistory = new ArrayList<>();
        this.allPriceHistory = new ArrayList<>();
        this.apiClient = new BinanceApiClient(symbol);
    }

    public String getSymbol() { return symbol; }
    public double getTradingFee() { return tradingFee; }
    public List<Order> getOpenOrders() { return openOrders; }
    public List<Order> getExecutedOrders() { return executedOrders; }
    public List<PriceData> getPriceHistory() { return priceHistory; }
    public List<PriceData> getAllPriceHistory() { return allPriceHistory; }
    public BinanceApiClient getApiClient() { return apiClient; }
    public void setTradingFee(double tradingFee) { this.tradingFee = tradingFee; }

    public double getCurrentBtcPrice() {
        if (priceHistory.isEmpty()) {
            return 50000.0; // Default price
        }
        return priceHistory.get(priceHistory.size() - 1).getPrice();
    }
} 