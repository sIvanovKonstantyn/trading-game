package com.tradinggame.indicators;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import java.util.ArrayList;
import java.util.List;
import com.tradinggame.dtos.PriceData;

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
    
    public static class IchimokuCloud {
        public final double[] tenkan;
        public final double[] kijun;
        public final double[] senkouA;
        public final double[] senkouB;
        public final double[] chikou;
        public IchimokuCloud(double[] tenkan, double[] kijun, double[] senkouA, double[] senkouB, double[] chikou) {
            this.tenkan = tenkan;
            this.kijun = kijun;
            this.senkouA = senkouA;
            this.senkouB = senkouB;
            this.chikou = chikou;
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
     * Calculate ATR (Average True Range)
     * @param prices List of PriceData
     * @param period ATR period (typically 14)
     * @return ATR value, or 0 if not enough data
     */
    public static double calculateATR(List<PriceData> prices, int period) {
        if (prices.size() < period + 1) return 0;
        double sumTR = 0;
        for (int i = prices.size() - period; i < prices.size(); i++) {
            PriceData curr = prices.get(i);
            PriceData prev = prices.get(i - 1);
            double high = curr.getHigh();
            double low = curr.getLow();
            double prevClose = prev.getPrice();
            double tr = Math.max(high - low, Math.max(Math.abs(high - prevClose), Math.abs(low - prevClose)));
            sumTR += tr;
        }
        return sumTR / period;
    }
    
    /**
     * Calculate ATR% (ATR as a percentage of close price)
     * @param prices List of PriceData
     * @param period ATR period (typically 14)
     * @return ATR% value, or 0 if not enough data or close is zero
     */
    public static double calculateATRPercent(List<PriceData> prices, int period) {
        if (prices.size() < period + 1) return 0;
        double atr = calculateATR(prices, period);
        double close = prices.get(prices.size() - 1).getPrice();
        if (close == 0) return 0;
        return (atr / close) * 100.0;
    }
    
    /**
     * Calculate Ichimoku Cloud components
     * @param prices List of PriceData
     * @return IchimokuCloud object with arrays for each line
     */
    public static IchimokuCloud calculateIchimokuCloud(List<PriceData> prices) {
        int n = prices.size();
        double[] tenkan = new double[n];
        double[] kijun = new double[n];
        double[] senkouA = new double[n];
        double[] senkouB = new double[n];
        double[] chikou = new double[n];
        for (int i = 0; i < n; i++) {
            // Tenkan-sen (Conversion Line): (9-period high + 9-period low) / 2
            if (i >= 8) {
                double maxHigh = Double.NEGATIVE_INFINITY;
                double minLow = Double.POSITIVE_INFINITY;
                for (int j = i - 8; j <= i; j++) {
                    maxHigh = Math.max(maxHigh, prices.get(j).getHigh());
                    minLow = Math.min(minLow, prices.get(j).getLow());
                }
                tenkan[i] = (maxHigh + minLow) / 2.0;
            } else {
                tenkan[i] = Double.NaN;
            }
            // Kijun-sen (Base Line): (26-period high + 26-period low) / 2
            if (i >= 25) {
                double maxHigh = Double.NEGATIVE_INFINITY;
                double minLow = Double.POSITIVE_INFINITY;
                for (int j = i - 25; j <= i; j++) {
                    maxHigh = Math.max(maxHigh, prices.get(j).getHigh());
                    minLow = Math.min(minLow, prices.get(j).getLow());
                }
                kijun[i] = (maxHigh + minLow) / 2.0;
            } else {
                kijun[i] = Double.NaN;
            }
            // Chikou Span (Lagging): close shifted -26
            if (i + 26 < n) {
                chikou[i] = prices.get(i + 26).getPrice();
            } else {
                chikou[i] = Double.NaN;
            }
        }
        // Senkou Span A: (Tenkan + Kijun) / 2, plotted 26 periods ahead
        for (int i = 0; i < n; i++) {
            if (i >= 25 && i + 26 < n && !Double.isNaN(tenkan[i]) && !Double.isNaN(kijun[i])) {
                senkouA[i + 26] = (tenkan[i] + kijun[i]) / 2.0;
            }
        }
        // Senkou Span B: (52-period high + 52-period low) / 2, plotted 26 periods ahead
        for (int i = 51; i < n; i++) {
            double maxHigh = Double.NEGATIVE_INFINITY;
            double minLow = Double.POSITIVE_INFINITY;
            for (int j = i - 51; j <= i; j++) {
                maxHigh = Math.max(maxHigh, prices.get(j).getHigh());
                minLow = Math.min(minLow, prices.get(j).getLow());
            }
            if (i + 26 < n) {
                senkouB[i + 26] = (maxHigh + minLow) / 2.0;
            }
        }
        // Fill NaN for uninitialized SenkouA/B
        for (int i = 0; i < n; i++) {
            if (Double.isNaN(senkouA[i])) senkouA[i] = Double.NaN;
            if (Double.isNaN(senkouB[i])) senkouB[i] = Double.NaN;
        }
        return new IchimokuCloud(tenkan, kijun, senkouA, senkouB, chikou);
    }
} 