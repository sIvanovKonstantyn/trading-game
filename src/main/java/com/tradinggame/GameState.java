package com.tradinggame;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameState {
    private String playerName;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate currentDate;
    private double initialBalance;
    private double currentBalance;
    private double btcBalance;
    private boolean gameStarted;
    private boolean gameFinished;
    
    private List<Order> openOrders;
    private List<Order> executedOrders;
    private List<PriceData> priceHistory;
    private List<GameStateListener> listeners;
    
    private BinanceApiClient apiClient;

    public GameState() {
        this.openOrders = new CopyOnWriteArrayList<>();
        this.executedOrders = new CopyOnWriteArrayList<>();
        this.priceHistory = new CopyOnWriteArrayList<>();
        this.listeners = new CopyOnWriteArrayList<>();
        this.apiClient = new BinanceApiClient();
        this.btcBalance = 0.0;
    }

    public void startGame(String playerName, LocalDate startDate, LocalDate endDate, double initialBalance) {
        this.playerName = playerName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.currentDate = startDate;
        this.initialBalance = initialBalance;
        this.currentBalance = initialBalance;
        this.gameStarted = true;
        this.gameFinished = false;
        
        // Load first day's prices
        loadPricesForCurrentDate();
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
            List<PriceData> newPrices = apiClient.getHistoricalPrices(currentDate);
            priceHistory.addAll(newPrices);
        } catch (Exception e) {
            System.err.println("Error loading prices for " + currentDate + ": " + e.getMessage());
            // Add some mock data if API fails
            addMockPricesForDate(currentDate);
        }
    }

    private void addMockPricesForDate(LocalDate date) {
        // Add 4-hour intervals for the day (6 data points)
        double basePrice = 45000 + Math.random() * 10000; // Random price between 45k-55k
        for (int hour = 0; hour < 24; hour += 4) {
            LocalDateTime timestamp = date.atTime(hour, 0);
            double price = basePrice + (Math.random() - 0.5) * 2000; // ±1000 variation
            priceHistory.add(new PriceData(timestamp, price));
        }
    }

    private void executeMatchingOrders() {
        List<Order> ordersToExecute = new ArrayList<>();
        
        for (Order order : openOrders) {
            for (PriceData price : getPricesForDate(currentDate)) {
                if (order.getOrderDate().equals(currentDate) && 
                    isOrderExecutable(order, price)) {
                    ordersToExecute.add(order);
                    break;
                }
            }
        }
        
        for (Order order : ordersToExecute) {
            executeOrder(order);
        }
    }

    private boolean isOrderExecutable(Order order, PriceData price) {
        if (order.getType() == OrderType.BUY) {
            return price.getPrice() <= order.getPrice();
        } else {
            return price.getPrice() >= order.getPrice();
        }
    }

    private void executeOrder(Order order) {
        double executionPrice = order.getPrice();
        double orderAmount = order.getAmount();
        
        if (order.getType() == OrderType.BUY) {
            // Buy BTC with USDC
            double cost = orderAmount * executionPrice;
            if (currentBalance >= cost) {
                currentBalance -= cost;
                btcBalance += orderAmount;
                order.setExecuted(true);
                order.setExecutionDate(currentDate);
                order.setExecutionPrice(executionPrice);
            }
        } else {
            // Sell BTC for USDC
            if (btcBalance >= orderAmount) {
                double revenue = orderAmount * executionPrice;
                currentBalance += revenue;
                btcBalance -= orderAmount;
                order.setExecuted(true);
                order.setExecutionDate(currentDate);
                order.setExecutionPrice(executionPrice);
            }
        }
        
        if (order.isExecuted()) {
            openOrders.remove(order);
            executedOrders.add(order);
        }
    }

    public void placeOrder(OrderType type, double price, double amount, LocalDate orderDate) {
        if (!gameStarted || gameFinished) {
            return;
        }
        
        // Cannot place orders in the past
        if (orderDate.isBefore(currentDate)) {
            return;
        }
        
        Order order = new Order(type, price, amount, orderDate);
        openOrders.add(order);
        notifyListeners();
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
    public double getCurrentBalance() { return currentBalance; }
    public double getBtcBalance() { return btcBalance; }
    public boolean isGameStarted() { return gameStarted; }
    public boolean isGameFinished() { return gameFinished; }
    public List<Order> getOpenOrders() { return new ArrayList<>(openOrders); }
    public List<Order> getExecutedOrders() { return new ArrayList<>(executedOrders); }
    public List<PriceData> getPriceHistory() { return new ArrayList<>(priceHistory); }

    public List<PriceData> getPricesForDate(LocalDate date) {
        List<PriceData> result = new ArrayList<>();
        for (PriceData price : priceHistory) {
            if (price.getTimestamp().toLocalDate().equals(date)) {
                result.add(price);
            }
        }
        return result;
    }

    public double getCurrentBtcPrice() {
        if (priceHistory.isEmpty()) {
            return 50000.0; // Default price
        }
        return priceHistory.get(priceHistory.size() - 1).getPrice();
    }
} 