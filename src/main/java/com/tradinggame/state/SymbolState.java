package com.tradinggame.state;

import java.util.ArrayList;
import java.util.List;
import com.tradinggame.dtos.Order;
import com.tradinggame.dtos.PriceData;
import com.tradinggame.clients.BinanceApiClient;

public class SymbolState {
    private final String symbol;
    private double tradingFee;
    private List<Order> openOrders;
    private List<Order> executedOrders;
    private List<PriceData> priceHistory;
    private List<PriceData> allPriceHistory;
    private BinanceApiClient apiClient;

    public SymbolState(String symbol, double tradingFee) {
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