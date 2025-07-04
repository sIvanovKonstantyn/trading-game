package com.tradinggame.dtos;

import java.time.LocalDate;

public class Order {
    private OrderType type;
    private double price;
    private double amount;
    private LocalDate orderDate;
    private boolean executed;
    private LocalDate executionDate;
    private double executionPrice;

    public Order(OrderType type, double price, double amount, LocalDate orderDate) {
        this.type = type;
        this.price = price;
        this.amount = amount;
        this.orderDate = orderDate;
        this.executed = false;
    }

    // Getters and setters
    public OrderType getType() { return type; }
    public double getPrice() { return price; }
    public double getAmount() { return amount; }
    public LocalDate getOrderDate() { return orderDate; }
    public boolean isExecuted() { return executed; }
    public LocalDate getExecutionDate() { return executionDate; }
    public double getExecutionPrice() { return executionPrice; }

    public void setExecuted(boolean executed) { this.executed = executed; }
    public void setExecutionDate(LocalDate executionDate) { this.executionDate = executionDate; }
    public void setExecutionPrice(double executionPrice) { this.executionPrice = executionPrice; }

    @Override
    public String toString() {
        if (executed) {
            return String.format("%s %.4f BTC @ $%.2f (Executed @ $%.2f on %s)", 
                type, amount, price, executionPrice, executionDate);
        } else {
            return String.format("%s %.4f BTC @ $%.2f (Placed on %s)", 
                type, amount, price, orderDate);
        }
    }
} 