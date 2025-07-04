package com.tradinggame.utils;

public class OrderUtils {
    /**
     * Calculates BTC amount from USDC amount and price.
     */
    public static double usdcToBtc(double usdcAmount, double price) {
        if (price <= 0) return 0;
        return usdcAmount / price;
    }

    /**
     * Calculates USDC amount from BTC amount and price.
     */
    public static double btcToUsdc(double btcAmount, double price) {
        return btcAmount * price;
    }

    /**
     * Parses a double from string, returns defaultValue if invalid.
     */
    public static double parseDoubleOrDefault(String s, double defaultValue) {
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return defaultValue;
        }
    }
} 