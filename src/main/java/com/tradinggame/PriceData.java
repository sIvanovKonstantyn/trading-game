package com.tradinggame;

import java.time.LocalDateTime;

public class PriceData {
    private LocalDateTime timestamp;
    private double price;

    public PriceData(LocalDateTime timestamp, double price) {
        this.timestamp = timestamp;
        this.price = price;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return String.format("Price: $%.2f at %s", price, timestamp);
    }
} 