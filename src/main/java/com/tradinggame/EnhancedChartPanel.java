package com.tradinggame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class EnhancedChartPanel extends JPanel {
    private GameState gameState;
    private ChartPanel chartPanel;
    private JFreeChart chart;
    private JTabbedPane tabbedPane;

    public EnhancedChartPanel(GameState gameState) {
        this.gameState = gameState;
        setLayout(new BorderLayout());
        
        initComponents();
        
        // Add listener for game state changes
        gameState.addGameStateListener(new GameStateListener() {
            @Override
            public void onGameStateChanged() {
                updateCharts();
            }
        });
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();
        
        // Create different chart tabs
        tabbedPane.addTab("Price & Bollinger Bands", createPriceChart());
        tabbedPane.addTab("RSI", createRSIChart());
        tabbedPane.addTab("Volume", createVolumeChart());
        
        add(tabbedPane, BorderLayout.CENTER);
    }

    private ChartPanel createPriceChart() {
        XYDataset dataset = createPriceDataset();
        
        JFreeChart priceChart = ChartFactory.createTimeSeriesChart(
            "BTC/USDC Price with Bollinger Bands",
            "Time",
            "Price (USDC)",
            dataset,
            true,
            true,
            false
        );
        
        // Customize the chart
        XYPlot plot = (XYPlot) priceChart.getPlot();
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("MM-dd HH:mm"));
        
        // Add Bollinger Bands
        addBollingerBands(plot);
        
        ChartPanel panel = new ChartPanel(priceChart);
        panel.setPreferredSize(new Dimension(800, 300));
        return panel;
    }

    private ChartPanel createRSIChart() {
        XYDataset dataset = createRSIDataset();
        
        JFreeChart rsiChart = ChartFactory.createTimeSeriesChart(
            "RSI (Relative Strength Index)",
            "Time",
            "RSI",
            dataset,
            true,
            true,
            false
        );
        
        // Customize the chart
        XYPlot plot = (XYPlot) rsiChart.getPlot();
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("MM-dd HH:mm"));
        
        // Add overbought/oversold lines
        addRSILines(plot);
        
        ChartPanel panel = new ChartPanel(rsiChart);
        panel.setPreferredSize(new Dimension(800, 200));
        return panel;
    }

    private ChartPanel createVolumeChart() {
        XYDataset dataset = createVolumeDataset();
        
        JFreeChart volumeChart = ChartFactory.createTimeSeriesChart(
            "Trading Volume",
            "Time",
            "Volume",
            dataset,
            true,
            true,
            false
        );
        
        // Customize the chart
        XYPlot plot = (XYPlot) volumeChart.getPlot();
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("MM-dd HH:mm"));
        
        ChartPanel panel = new ChartPanel(volumeChart);
        panel.setPreferredSize(new Dimension(800, 200));
        return panel;
    }

    private XYDataset createPriceDataset() {
        TimeSeries series = new TimeSeries("BTC Price");
        List<PriceData> priceHistory = gameState.getPriceHistory();
        LocalDate currentDate = gameState.getCurrentDate();
        
        if (priceHistory != null) {
            for (PriceData priceData : priceHistory) {
                try {
                    // Only show data up to the current game date
                    if (!priceData.getTimestamp().toLocalDate().isAfter(currentDate)) {
                        LocalDateTime ldt = priceData.getTimestamp();
                        Date date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
                        series.addOrUpdate(new Millisecond(date), priceData.getPrice());
                    }
                } catch (Exception e) {
                    // Skip invalid data points
                    System.err.println("Error adding price data point: " + e.getMessage());
                }
            }
        }
        return new TimeSeriesCollection(series);
    }

    private XYDataset createRSIDataset() {
        TimeSeries series = new TimeSeries("RSI");
        List<PriceData> allPrices = gameState.getAllPriceHistory();
        LocalDate currentDate = gameState.getCurrentDate();
        
        if (allPrices == null || allPrices.isEmpty()) {
            return new TimeSeriesCollection(series);
        }
        
        // Calculate RSI dynamically for each data point up to current date
        for (int i = 0; i < allPrices.size(); i++) {
            try {
                // Only process data up to the current game date
                if (allPrices.get(i).getTimestamp().toLocalDate().isAfter(currentDate)) {
                    break;
                }
                
                // Use a rolling window of the last 14+1 data points for RSI calculation
                int startIndex = Math.max(0, i - 14);
                List<PriceData> subList = allPrices.subList(startIndex, i + 1);
                TechnicalIndicators.RSIResult rsi = TechnicalIndicators.calculateRSI(subList, 14);
                if (rsi.isValid) {
                    LocalDateTime ldt = allPrices.get(i).getTimestamp();
                    Date date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
                    series.addOrUpdate(new Millisecond(date), rsi.rsi);
                }
            } catch (Exception e) {
                // Skip invalid data points
                System.err.println("Error adding RSI data point: " + e.getMessage());
            }
        }
        return new TimeSeriesCollection(series);
    }

    private XYDataset createVolumeDataset() {
        TimeSeries series = new TimeSeries("Volume");
        List<PriceData> priceHistory = gameState.getPriceHistory();
        LocalDate currentDate = gameState.getCurrentDate();
        
        if (priceHistory != null) {
            for (PriceData priceData : priceHistory) {
                try {
                    // Only show data up to the current game date
                    if (!priceData.getTimestamp().toLocalDate().isAfter(currentDate)) {
                        LocalDateTime ldt = priceData.getTimestamp();
                        Date date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
                        
                        // Generate fresh mock volume data based on price volatility
                        double price = priceData.getPrice();
                        double baseVolume = 1000 + Math.random() * 5000;
                        // Add some correlation with price movement (higher volume for more volatile prices)
                        double volatilityFactor = 1.0 + Math.abs(price - 50000) / 10000; // 50000 as reference price
                        double volume = baseVolume * volatilityFactor;
                        
                        series.addOrUpdate(new Millisecond(date), volume);
                    }
                } catch (Exception e) {
                    // Skip invalid data points
                    System.err.println("Error adding volume data point: " + e.getMessage());
                }
            }
        }
        
        return new TimeSeriesCollection(series);
    }

    private void addBollingerBands(XYPlot plot) {
        List<PriceData> allPrices = gameState.getAllPriceHistory();
        LocalDate currentDate = gameState.getCurrentDate();
        
        if (allPrices == null || allPrices.isEmpty() || allPrices.size() < 20) {
            return;
        }
        
        try {
            TimeSeries upperSeries = new TimeSeries("Upper Band");
            TimeSeries middleSeries = new TimeSeries("Middle Band");
            TimeSeries lowerSeries = new TimeSeries("Lower Band");
            
            // Calculate Bollinger Bands dynamically for each data point up to current date
            for (int i = 0; i < allPrices.size(); i++) {
                try {
                    // Only process data up to the current game date
                    if (allPrices.get(i).getTimestamp().toLocalDate().isAfter(currentDate)) {
                        break;
                    }
                    
                    // Use a rolling window of the last 20 data points for Bollinger Bands calculation
                    int startIndex = Math.max(0, i - 19); // 20 data points (i-19 to i inclusive)
                    List<PriceData> rollingWindow = allPrices.subList(startIndex, i + 1);
                    
                    if (rollingWindow.size() >= 20) {
                        TechnicalIndicators.BollingerBands bb = TechnicalIndicators.calculateBollingerBands(rollingWindow, 20, 2);
                        
                        LocalDateTime ldt = allPrices.get(i).getTimestamp();
                        Date date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
                        upperSeries.addOrUpdate(new Millisecond(date), bb.upperBand);
                        middleSeries.addOrUpdate(new Millisecond(date), bb.middleBand);
                        lowerSeries.addOrUpdate(new Millisecond(date), bb.lowerBand);
                    }
                } catch (Exception e) {
                    System.err.println("Error calculating Bollinger Bands for data point " + i + ": " + e.getMessage());
                }
            }
            
            TimeSeriesCollection bbDataset = new TimeSeriesCollection();
            bbDataset.addSeries(upperSeries);
            bbDataset.addSeries(middleSeries);
            bbDataset.addSeries(lowerSeries);
            plot.setDataset(1, bbDataset);
            XYLineAndShapeRenderer bbRenderer = new XYLineAndShapeRenderer();
            bbRenderer.setSeriesPaint(0, Color.RED);
            bbRenderer.setSeriesPaint(1, Color.BLUE);
            bbRenderer.setSeriesPaint(2, Color.RED);
            bbRenderer.setSeriesStroke(0, new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
            bbRenderer.setSeriesStroke(1, new BasicStroke(1.0f));
            bbRenderer.setSeriesStroke(2, new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
            plot.setRenderer(1, bbRenderer);
        } catch (Exception e) {
            System.err.println("Error adding Bollinger Bands: " + e.getMessage());
        }
    }

    private void addRSILines(XYPlot plot) {
        // Add overbought (70) and oversold (30) lines
        TimeSeries overboughtSeries = new TimeSeries("Overbought (70)");
        TimeSeries oversoldSeries = new TimeSeries("Oversold (30)");
        
        List<PriceData> priceHistory = gameState.getPriceHistory();
        if (priceHistory != null) {
            for (PriceData priceData : priceHistory) {
                try {
                    LocalDateTime ldt = priceData.getTimestamp();
                    Date date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
                    
                    overboughtSeries.addOrUpdate(new Millisecond(date), 70);
                    oversoldSeries.addOrUpdate(new Millisecond(date), 30);
                } catch (Exception e) {
                    System.err.println("Error adding RSI lines: " + e.getMessage());
                }
            }
        }
        
        TimeSeriesCollection linesDataset = new TimeSeriesCollection();
        linesDataset.addSeries(overboughtSeries);
        linesDataset.addSeries(oversoldSeries);
        
        plot.setDataset(1, linesDataset);
        
        // Customize renderer for RSI lines
        XYLineAndShapeRenderer linesRenderer = new XYLineAndShapeRenderer();
        linesRenderer.setSeriesPaint(0, Color.RED);
        linesRenderer.setSeriesPaint(1, Color.GREEN);
        linesRenderer.setSeriesStroke(0, new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
        linesRenderer.setSeriesStroke(1, new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
        
        plot.setRenderer(1, linesRenderer);
    }

    private void updateCharts() {
        SwingUtilities.invokeLater(() -> {
            if (tabbedPane != null) {
                tabbedPane.removeAll();
                tabbedPane.addTab("Price & Bollinger Bands", createPriceChart());
                tabbedPane.addTab("RSI", createRSIChart());
                tabbedPane.addTab("Volume", createVolumeChart());
            }
        });
    }
} 