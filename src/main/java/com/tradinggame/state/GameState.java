package com.tradinggame.state;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import com.tradinggame.dtos.Order;
import com.tradinggame.dtos.OrderType;
import com.tradinggame.dtos.GameStateListener;
import com.tradinggame.dtos.PriceData;
import com.tradinggame.clients.BinanceApiClient;

public class GameState {
    private String playerName;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate currentDate;
    private double initialBalance;
    private double tradingFee; // Trading fee as a percentage (e.g., 0.01 = 1%)
    private boolean gameStarted;
    private boolean gameFinished;
    
    private List<Order> openOrders;
    private List<Order> executedOrders;
    private List<PriceData> priceHistory;
    private List<GameStateListener> listeners;
    
    private BinanceApiClient apiClient;
    private int indicatorWarmupDays = 20; // For Bollinger Bands (max of RSI/Bollinger)
    private List<PriceData> allPriceHistory;
    private Map<String, SymbolState> symbolStates = new HashMap<>();
    private String currentSymbol = "BTCUSDC";
    private double usdcBalance;
    private Map<String, Double> cryptoBalances = new ConcurrentHashMap<>(); // e.g. BTC, ETH, BNB

    public GameState() {
        this.openOrders = new CopyOnWriteArrayList<>();
        this.executedOrders = new CopyOnWriteArrayList<>();
        this.priceHistory = new CopyOnWriteArrayList<>();
        this.allPriceHistory = new CopyOnWriteArrayList<>();
        this.listeners = new CopyOnWriteArrayList<>();
        this.apiClient = new BinanceApiClient();
        // Initialize with default symbol
        symbolStates.put(currentSymbol, new SymbolState(currentSymbol, 0.001)); // Default values, will be set in startGame
    }

    public void startGame(String playerName, LocalDate startDate, LocalDate endDate, double initialBalance, double tradingFee) {
        this.playerName = playerName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.currentDate = startDate;
        this.initialBalance = initialBalance;
        this.tradingFee = tradingFee;
        this.gameStarted = true;
        this.gameFinished = false;
        this.usdcBalance = initialBalance;
        this.cryptoBalances.clear();
        // Clear all symbol states and reinitialize (but no per-symbol balances)
        for (SymbolState state : symbolStates.values()) {
            state.setTradingFee(tradingFee);
            state.getOpenOrders().clear();
            state.getExecutedOrders().clear();
            state.getPriceHistory().clear();
            state.getAllPriceHistory().clear();
        }
        // Load warm-up prices for indicators for all symbols
        LocalDate warmupStart = startDate.minusDays(indicatorWarmupDays);
        for (SymbolState state : symbolStates.values()) {
            loadPricesForRange(state, warmupStart, endDate);
        }
        notifyListeners();
    }

    public void nextDay() {
        if (!gameStarted || gameFinished) {
            return;
        }

        // Execute any orders that match current prices
        executeMatchingOrders();
        
        // Move to next day
        currentDate = currentDate.plusDays(1);
        
        // Check if game is finished
        if (currentDate.isAfter(endDate)) {
            gameFinished = true;
        } else {
            // Load prices for the new day
            loadPricesForCurrentDate();
        }
        
        notifyListeners();
    }

    private void loadPricesForCurrentDate() {
        try {
            SymbolState state = getCurrentSymbolState();
            List<PriceData> newPrices = state.getApiClient().getHistoricalPrices(currentDate);
            state.getPriceHistory().addAll(newPrices);
        } catch (Exception e) {
            System.err.println("Error loading prices for " + currentDate + ": " + e.getMessage());
            addMockPricesForDate(currentDate);
        }
    }

    private void addMockPricesForDate(LocalDate date) {
        SymbolState state = getCurrentSymbolState();
        double basePrice = 45000 + Math.random() * 10000; // Random price between 45k-55k
        for (int hour = 0; hour < 24; hour += 4) {
            LocalDateTime timestamp = date.atTime(hour, 0);
            double price = basePrice + (Math.random() - 0.5) * 2000; // ±1000 variation
            double volume = 1000 + Math.random() * 5000; // Mock volume
            PriceData priceData = new PriceData(timestamp, price, price, price, price, volume);
            state.getAllPriceHistory().add(priceData);
            if (!date.isBefore(startDate)) {
                state.getPriceHistory().add(priceData);
            }
        }
    }

    private void executeMatchingOrders() {
        for (String symbol : symbolStates.keySet()) {
            SymbolState state = symbolStates.get(symbol);
            List<Order> ordersToExecute = new ArrayList<>();
            for (Order order : new ArrayList<>(state.getOpenOrders())) {
                for (PriceData price : getPricesForDate(state, currentDate)) {
                    System.out.printf("[DEBUG] Checking order %s %s @ %.2f for %s on %s (market price: %.2f)\n",
                        order.getType(), symbol, order.getPrice(), order.getOrderDate(), currentDate, price.getPrice());
                    if (order.getOrderDate().equals(currentDate) && isOrderExecutable(order, price)) {
                        ordersToExecute.add(order);
                        break;
                    }
                }
            }
            for (Order order : ordersToExecute) {
                executeOrder(order, state);
            }
        }
    }

    private boolean isOrderExecutable(Order order, PriceData price) {
        if (order.getType() == OrderType.BUY) {
            return price.getPrice() <= order.getPrice();
        } else {
            return price.getPrice() >= order.getPrice();
        }
    }

    private void executeOrder(Order order, SymbolState state) {
        double executionPrice = order.getPrice();
        double orderAmount = order.getAmount();
        String symbol = state.getSymbol();
        String crypto = symbol.replace("USDC", "");
        if (order.getType() == OrderType.BUY) {
            double cost = orderAmount * executionPrice;
            double feeAmount = cost * state.getTradingFee();
            double totalCost = cost + feeAmount;
            if (usdcBalance >= totalCost) {
                usdcBalance -= totalCost;
                cryptoBalances.put(crypto, cryptoBalances.getOrDefault(crypto, 0.0) + orderAmount);
                order.setExecuted(true);
                order.setExecutionDate(currentDate);
                order.setExecutionPrice(executionPrice);
                System.out.printf("[DEBUG] BUY EXECUTED: %s %.4f @ %.2f | Fee: %.2f | USDC left: %.2f | %s balance: %.4f\n",
                    crypto, orderAmount, executionPrice, feeAmount, usdcBalance, crypto, cryptoBalances.get(crypto));
            } else {
                System.out.printf("[DEBUG] BUY FAILED (insufficient USDC): %s %.4f @ %.2f | Needed: %.2f, Available: %.2f\n",
                    crypto, orderAmount, executionPrice, totalCost, usdcBalance);
            }
        } else {
            double cryptoBal = cryptoBalances.getOrDefault(crypto, 0.0);
            if (cryptoBal >= orderAmount) {
                double revenue = orderAmount * executionPrice;
                double feeAmount = revenue * state.getTradingFee();
                double netRevenue = revenue - feeAmount;
                usdcBalance += netRevenue;
                cryptoBalances.put(crypto, cryptoBal - orderAmount);
                order.setExecuted(true);
                order.setExecutionDate(currentDate);
                order.setExecutionPrice(executionPrice);
                System.out.printf("[DEBUG] SELL EXECUTED: %s %.4f @ %.2f | Fee: %.2f | USDC now: %.2f | %s balance: %.4f\n",
                    crypto, orderAmount, executionPrice, feeAmount, usdcBalance, crypto, cryptoBalances.get(crypto));
            } else {
                System.out.printf("[DEBUG] SELL FAILED (insufficient %s): %.4f @ %.2f | Needed: %.4f, Available: %.4f\n",
                    crypto, orderAmount, executionPrice, orderAmount, cryptoBal);
            }
        }
        if (order.isExecuted()) {
            state.getOpenOrders().remove(order);
            state.getExecutedOrders().add(order);
        }
    }

    public void placeOrder(OrderType type, double price, double amount, LocalDate orderDate) {
        placeOrder(type, price, amount, orderDate, currentSymbol);
    }

    public void placeOrder(OrderType type, double price, double amount, LocalDate orderDate, String symbol) {
        if (!gameStarted || gameFinished) {
            return;
        }
        if (orderDate.isBefore(currentDate)) {
            return;
        }
        SymbolState state = symbolStates.get(symbol);
        if (state == null) {
            state = new SymbolState(symbol, tradingFee);
            symbolStates.put(symbol, state);
        }
        Order order = new Order(type, price, amount, orderDate);
        state.getOpenOrders().add(order);
        notifyListeners();
    }

    public void cancelOrder(Order order) {
        if (!gameStarted || gameFinished) {
            return;
        }
        SymbolState state = symbolStates.get(currentSymbol);
        if (state != null && state.getOpenOrders().contains(order)) {
            state.getOpenOrders().remove(order);
            notifyListeners();
        }
    }

    public void addGameStateListener(GameStateListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        for (GameStateListener listener : listeners) {
            listener.onGameStateChanged();
        }
    }

    // Getters
    public String getPlayerName() { return playerName; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public LocalDate getCurrentDate() { return currentDate; }
    public double getInitialBalance() { return initialBalance; }
    public double getTradingFee() { return tradingFee; }
    public boolean isGameStarted() { return gameStarted; }
    public boolean isGameFinished() { return gameFinished; }
    public List<Order> getOpenOrders() {
        SymbolState state = symbolStates.get(currentSymbol);
        if (state != null) return new ArrayList<>(state.getOpenOrders());
        return new ArrayList<>();
    }
    public List<Order> getExecutedOrders() {
        SymbolState state = symbolStates.get(currentSymbol);
        if (state != null) return new ArrayList<>(state.getExecutedOrders());
        return new ArrayList<>();
    }
    public List<PriceData> getPriceHistory() { return new ArrayList<>(priceHistory); }

    public List<PriceData> getPricesForDate(SymbolState state, LocalDate date) {
        List<PriceData> result = new ArrayList<>();
        for (PriceData price : state.getPriceHistory()) {
            if (price.getTimestamp().toLocalDate().equals(date)) {
                result.add(price);
            }
        }
        return result;
    }

    private void loadPricesForRange(SymbolState state, LocalDate from, LocalDate to) {
        System.out.println("Loading prices for symbol " + state.getSymbol() + " from " + from + " to " + to);
        LocalDate date = from;
        int dayCount = 0;
        while (!date.isAfter(to)) {
            try {
                System.out.println("Loading prices for date: " + date + " (day " + (++dayCount) + ")");
                List<PriceData> newPrices = state.getApiClient().getHistoricalPrices(date);
                System.out.println("Loaded " + newPrices.size() + " price points for " + date);
                state.getAllPriceHistory().addAll(newPrices);
                if (!date.isBefore(startDate)) {
                    state.getPriceHistory().addAll(newPrices);
                }
            } catch (Exception e) {
                System.err.println("Error loading prices for " + date + ": " + e.getMessage());
                // Add mock data for this symbol
                double basePrice = 45000 + Math.random() * 10000;
                for (int hour = 0; hour < 24; hour += 4) {
                    LocalDateTime timestamp = date.atTime(hour, 0);
                    double price = basePrice + (Math.random() - 0.5) * 2000;
                    double volume = 1000 + Math.random() * 5000;
                    PriceData priceData = new PriceData(timestamp, price, price, price, price, volume);
                    state.getAllPriceHistory().add(priceData);
                    if (!date.isBefore(startDate)) {
                        state.getPriceHistory().add(priceData);
                    }
                }
            }
            date = date.plusDays(1);
        }
        System.out.println("Finished loading prices for symbol " + state.getSymbol() + ". Total days processed: " + dayCount);
    }

    // For indicator calculations, use allPriceHistory
    public List<PriceData> getAllPriceHistory() {
        if (allPriceHistory == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(allPriceHistory);
    }

    public void setCurrentSymbol(String symbol) {
        if (!symbolStates.containsKey(symbol)) {
            // Use default initial balance and trading fee for new symbol
            symbolStates.put(symbol, new SymbolState(symbol, 0.001));
        }
        this.currentSymbol = symbol;
        SymbolState state = symbolStates.get(symbol);
        // If price history is empty or missing for the current range, load it
        if (state.getPriceHistory().isEmpty() || state.getAllPriceHistory().isEmpty()) {
            LocalDate warmupStart = startDate != null ? startDate.minusDays(indicatorWarmupDays) : LocalDate.now().minusDays(indicatorWarmupDays);
            LocalDate end = endDate != null ? endDate : LocalDate.now();
            loadPricesForRange(state, warmupStart, end);
        }
        notifyListeners();
    }
    public String getCurrentSymbol() { return currentSymbol; }
    public SymbolState getCurrentSymbolState() { return symbolStates.get(currentSymbol); }
    public Set<String> getAvailableSymbols() { return symbolStates.keySet(); }
    public Map<String, SymbolState> getSymbolStates() {
        return symbolStates;
    }
    // Getters for balances
    public double getUsdcBalance() { return usdcBalance; }
    public double getCryptoBalance(String crypto) { return cryptoBalances.getOrDefault(crypto, 0.0); }
    public Map<String, Double> getAllCryptoBalances() { return cryptoBalances; }
} 