package com.tradinggame;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import java.util.ArrayList;
import java.util.List;

public class TechnicalIndicators {
    
    public static class BollingerBands {
        public final double upperBand;
        public final double middleBand;
        public final double lowerBand;
        
        public BollingerBands(double upper, double middle, double lower) {
            this.upperBand = upper;
            this.middleBand = middle;
            this.lowerBand = lower;
        }
    }
    
    public static class RSIResult {
        public final double rsi;
        public final boolean isValid;
        
        public RSIResult(double rsi, boolean isValid) {
            this.rsi = rsi;
            this.isValid = isValid;
        }
    }
    
    public static class VolumeData {
        public final double volume;
        public final double averageVolume;
        
        public VolumeData(double volume, double averageVolume) {
            this.volume = volume;
            this.averageVolume = averageVolume;
        }
    }
    
    /**
     * Calculate RSI (Relative Strength Index)
     * @param prices List of prices
     * @param period RSI period (typically 14)
     * @return RSI value
     */
    public static RSIResult calculateRSI(List<PriceData> prices, int period) {
        if (prices.size() < period + 1) {
            return new RSIResult(50.0, false); // Default neutral RSI
        }
        
        List<Double> gains = new ArrayList<>();
        List<Double> losses = new ArrayList<>();
        
        // Calculate price changes
        for (int i = 1; i < prices.size(); i++) {
            double change = prices.get(i).getPrice() - prices.get(i - 1).getPrice();
            if (change > 0) {
                gains.add(change);
                losses.add(0.0);
            } else {
                gains.add(0.0);
                losses.add(-change);
            }
        }
        
        // Calculate average gain and loss over the period
        double avgGain = 0;
        double avgLoss = 0;
        
        for (int i = 0; i < period; i++) {
            avgGain += gains.get(i);
            avgLoss += losses.get(i);
        }
        avgGain /= period;
        avgLoss /= period;
        
        // Calculate RSI
        if (avgLoss == 0) {
            return new RSIResult(100.0, true);
        }
        
        double rs = avgGain / avgLoss;
        double rsi = 100 - (100 / (1 + rs));
        
        return new RSIResult(rsi, true);
    }
    
    /**
     * Calculate Bollinger Bands
     * @param prices List of prices
     * @param period Period for moving average (typically 20)
     * @param stdDev Standard deviation multiplier (typically 2)
     * @return Bollinger Bands
     */
    public static BollingerBands calculateBollingerBands(List<PriceData> prices, int period, double stdDev) {
        if (prices.size() < period) {
            return new BollingerBands(0, 0, 0);
        }
        
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (int i = prices.size() - period; i < prices.size(); i++) {
            stats.addValue(prices.get(i).getPrice());
        }
        
        double middleBand = stats.getMean();
        double standardDeviation = stats.getStandardDeviation();
        
        double upperBand = middleBand + (stdDev * standardDeviation);
        double lowerBand = middleBand - (stdDev * standardDeviation);
        
        return new BollingerBands(upperBand, middleBand, lowerBand);
    }
    
    /**
     * Calculate volume data (mock data for now)
     * @param prices List of prices
     * @return Volume data
     */
    public static VolumeData calculateVolume(List<PriceData> prices) {
        if (prices.isEmpty()) {
            return new VolumeData(0, 0);
        }
        
        // Generate mock volume based on price volatility
        double currentPrice = prices.get(prices.size() - 1).getPrice();
        double baseVolume = 1000 + Math.random() * 5000; // Random volume between 1000-6000
        
        // Calculate average volume over last 10 periods
        double totalVolume = 0;
        int count = Math.min(10, prices.size());
        for (int i = 0; i < count; i++) {
            totalVolume += 1000 + Math.random() * 5000;
        }
        double averageVolume = totalVolume / count;
        
        return new VolumeData(baseVolume, averageVolume);
    }
} 