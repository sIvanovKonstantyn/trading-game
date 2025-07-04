package com.tradinggame;

import java.time.LocalDateTime;

public class PriceData {
    private LocalDateTime timestamp;
    private double price;
    private double volume;
    private double open;
    private double high;
    private double low;

    public PriceData(LocalDateTime timestamp, double open, double high, double low, double close, double volume) {
        this.timestamp = timestamp;
        this.open = open;
        this.high = high;
        this.low = low;
        this.price = close;
        this.volume = volume;
    }

    public PriceData(LocalDateTime timestamp, double price, double volume) {
        this(timestamp, price, price, price, price, volume);
    }

    public PriceData(LocalDateTime timestamp, double price) {
        this(timestamp, price, price, price, price, 0);
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

    public double getOpen() {
        return open;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    @Override
    public String toString() {
        return String.format("O: %.2f H: %.2f L: %.2f C: %.2f, V: %.2f at %s", open, high, low, price, volume, timestamp);
    }
} 