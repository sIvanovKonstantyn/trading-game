package com.tradinggame;

import java.time.LocalDateTime;

public class PriceData {
    private LocalDateTime timestamp;
    private double price;
    private double volume;

    public PriceData(LocalDateTime timestamp, double price, double volume) {
        this.timestamp = timestamp;
        this.price = price;
        this.volume = volume;
    }

    // Legacy constructor for compatibility
    public PriceData(LocalDateTime timestamp, double price) {
        this(timestamp, price, 0);
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public double getPrice() {
        return price;
    }

    public double getVolume() {
        return volume;
    }

    @Override
    public String toString() {
        return String.format("Price: $%.2f, Volume: %.2f at %s", price, volume, timestamp);
    }
} 