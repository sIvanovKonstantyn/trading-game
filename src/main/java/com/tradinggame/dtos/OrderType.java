package com.tradinggame.dtos;

public enum OrderType {
    BUY("Buy"),
    SELL("Sell");

    private final String displayName;

    OrderType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
} 