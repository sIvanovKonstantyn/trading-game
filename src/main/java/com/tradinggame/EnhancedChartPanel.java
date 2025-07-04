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
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.xy.DefaultHighLowDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.text.NumberFormat;

public class EnhancedChartPanel extends JPanel {
    private GameState gameState;
    private ChartPanel chartPanel;
    private JFreeChart chart;
    private JPanel indicatorPanel;
    private Map<String, JCheckBox> indicatorCheckboxes = new HashMap<>();
    private boolean showBollinger = true;
    private boolean showRSI = true;
    private boolean showVolume = true;
    private ChartPanel rsiChartPanel;

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
        setLayout(new BorderLayout());
        // Indicator selection panel
        indicatorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        indicatorPanel.setBorder(BorderFactory.createTitledBorder("Indicators"));
        addIndicatorCheckbox("Bollinger Bands", true);
        addIndicatorCheckbox("RSI", true);
        addIndicatorCheckbox("Volume", true);
        add(indicatorPanel, BorderLayout.NORTH);

        // Main chart panel
        chartPanel = createMainChartPanel();
        // RSI chart panel (may be null)
        rsiChartPanel = showRSI ? createRSIChartPanel() : null;

        JPanel chartsPanel = new JPanel();
        chartsPanel.setLayout(new BoxLayout(chartsPanel, BoxLayout.Y_AXIS));
        chartsPanel.add(chartPanel);
        if (rsiChartPanel != null) chartsPanel.add(rsiChartPanel);
        add(chartsPanel, BorderLayout.CENTER);
    }

    private void addIndicatorCheckbox(String name, boolean selected) {
        JCheckBox checkBox = new JCheckBox(name, selected);
        indicatorCheckboxes.put(name, checkBox);
        indicatorPanel.add(checkBox);
        checkBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                switch (name) {
                    case "Bollinger Bands": showBollinger = checkBox.isSelected(); break;
                    case "RSI": showRSI = checkBox.isSelected(); break;
                    case "Volume": showVolume = checkBox.isSelected(); break;
                }
                updateCharts();
            }
        });
    }

    private ChartPanel createMainChartPanel() {
        XYDataset priceDataset = createPriceDataset();
        JFreeChart mainChart = ChartFactory.createTimeSeriesChart(
            gameState.getCurrentSymbol() + " Price Chart",
            "Time",
            "Price (USDC)",
            priceDataset,
            true,
            true,
            false
        );
        XYPlot plot = (XYPlot) mainChart.getPlot();
        plot.setBackgroundPaint(Color.WHITE); // Light theme background
        plot.setDomainGridlinePaint(new Color(220, 220, 220));
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        plot.getDomainAxis().setLabelPaint(Color.BLACK);
        plot.getRangeAxis().setLabelPaint(Color.BLACK);
        plot.getDomainAxis().setTickLabelPaint(Color.BLACK);
        plot.getRangeAxis().setTickLabelPaint(Color.BLACK);
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("MM-dd HH:mm"));
        // Price line
        XYLineAndShapeRenderer priceRenderer = new XYLineAndShapeRenderer();
        priceRenderer.setSeriesPaint(0, new Color(0, 100, 200));
        priceRenderer.setSeriesStroke(0, new BasicStroke(2.5f));
        priceRenderer.setSeriesShapesVisible(0, false);
        plot.setRenderer(0, priceRenderer);
        // Add indicators
        if (showBollinger) addBollingerBands(plot);
        if (showVolume) addVolume(plot);
        ChartPanel panel = new ChartPanel(mainChart);
        panel.setPreferredSize(new Dimension(800, 400));
        return panel;
    }

    private ChartPanel createRSIChartPanel() {
        XYDataset rsiDataset = createRSIDataset();
        JFreeChart rsiChart = org.jfree.chart.ChartFactory.createTimeSeriesChart(
            "RSI (Relative Strength Index)",
            "Time",
            "RSI",
            rsiDataset,
            true,
            true,
            false
        );
        org.jfree.chart.plot.XYPlot plot = (org.jfree.chart.plot.XYPlot) rsiChart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(new Color(220, 220, 220));
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        plot.getDomainAxis().setLabelPaint(Color.BLACK);
        plot.getRangeAxis().setLabelPaint(Color.BLACK);
        plot.getDomainAxis().setTickLabelPaint(Color.BLACK);
        plot.getRangeAxis().setTickLabelPaint(Color.BLACK);
        org.jfree.chart.axis.DateAxis axis = (org.jfree.chart.axis.DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new java.text.SimpleDateFormat("MM-dd HH:mm"));
        org.jfree.chart.renderer.xy.XYLineAndShapeRenderer rsiRenderer = new org.jfree.chart.renderer.xy.XYLineAndShapeRenderer();
        rsiRenderer.setSeriesPaint(0, new java.awt.Color(0, 150, 0));
        rsiRenderer.setSeriesStroke(0, new java.awt.BasicStroke(2.0f));
        rsiRenderer.setSeriesShapesVisible(0, false);
        plot.setRenderer(0, rsiRenderer);
        addRSILines(plot);
        ChartPanel panel = new ChartPanel(rsiChart);
        panel.setPreferredSize(new java.awt.Dimension(800, 150));
        return panel;
    }

    private List<PriceData> getPriceHistory() {
        return gameState.getCurrentSymbolState().getPriceHistory();
    }
    private List<PriceData> getAllPriceHistory() {
        return gameState.getCurrentSymbolState().getAllPriceHistory();
    }

    private XYDataset createPriceDataset() {
        TimeSeries series = new TimeSeries(gameState.getCurrentSymbol() + " Price");
        List<PriceData> priceHistory = getPriceHistory();
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
        List<PriceData> allPrices = getAllPriceHistory();
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
        List<PriceData> priceHistory = getPriceHistory();
        LocalDate currentDate = gameState.getCurrentDate();
        
        if (priceHistory != null) {
            for (PriceData priceData : priceHistory) {
                try {
                    // Only show data up to the current game date
                    if (!priceData.getTimestamp().toLocalDate().isAfter(currentDate)) {
                        LocalDateTime ldt = priceData.getTimestamp();
                        Date date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
                        series.addOrUpdate(new Millisecond(date), priceData.getVolume());
                    }
                } catch (Exception e) {
                    // Skip invalid data points
                    System.err.println("Error adding volume data point: " + e.getMessage());
                }
            }
        }
        
        return new TimeSeriesCollection(series);
    }

    private DefaultHighLowDataset createOHLCDataset() {
        List<PriceData> priceHistory = getPriceHistory();
        LocalDate currentDate = gameState.getCurrentDate();
        int n = priceHistory == null ? 0 : priceHistory.size();
        int validCount = 0;
        for (PriceData pd : priceHistory) {
            if (!pd.getTimestamp().toLocalDate().isAfter(currentDate)) validCount++;
        }
        Date[] dates = new Date[validCount];
        double[] opens = new double[validCount];
        double[] highs = new double[validCount];
        double[] lows = new double[validCount];
        double[] closes = new double[validCount];
        double[] volumes = new double[validCount];
        int idx = 0;
        for (PriceData pd : priceHistory) {
            if (!pd.getTimestamp().toLocalDate().isAfter(currentDate)) {
                Date date = Date.from(pd.getTimestamp().atZone(ZoneId.systemDefault()).toInstant());
                dates[idx] = date;
                opens[idx] = pd.getOpen();
                highs[idx] = pd.getHigh();
                lows[idx] = pd.getLow();
                closes[idx] = pd.getPrice();
                volumes[idx] = pd.getVolume();
                idx++;
            }
        }
        return new DefaultHighLowDataset(gameState.getCurrentSymbol(), dates, highs, lows, opens, closes, volumes);
    }

    private void addBollingerBands(XYPlot plot) {
        List<PriceData> allPrices = getAllPriceHistory();
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
            
            // Use lighter, more transparent colors for Bollinger Bands
            bbRenderer.setSeriesPaint(0, new Color(255, 100, 100, 120)); // Light red with transparency
            bbRenderer.setSeriesPaint(1, new Color(100, 100, 255, 100)); // Light blue with transparency  
            bbRenderer.setSeriesPaint(2, new Color(255, 100, 100, 120)); // Light red with transparency
            
            // Use thinner strokes for Bollinger Bands
            bbRenderer.setSeriesStroke(0, new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
            bbRenderer.setSeriesStroke(1, new BasicStroke(0.8f)); // Thinner middle line
            bbRenderer.setSeriesStroke(2, new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
            
            // Make shapes invisible for cleaner look
            bbRenderer.setSeriesShapesVisible(0, false);
            bbRenderer.setSeriesShapesVisible(1, false);
            bbRenderer.setSeriesShapesVisible(2, false);
            
            plot.setRenderer(1, bbRenderer);
        } catch (Exception e) {
            System.err.println("Error adding Bollinger Bands: " + e.getMessage());
        }
    }

    private void addRSILines(XYPlot plot) {
        // Add overbought (70) and oversold (30) lines
        TimeSeries overboughtSeries = new TimeSeries("Overbought (70)");
        TimeSeries oversoldSeries = new TimeSeries("Oversold (30)");
        
        List<PriceData> priceHistory = getPriceHistory();
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
        linesRenderer.setSeriesPaint(0, new Color(255, 100, 100, 120)); // Light red with transparency
        linesRenderer.setSeriesPaint(1, new Color(100, 255, 100, 120)); // Light green with transparency
        linesRenderer.setSeriesStroke(0, new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
        linesRenderer.setSeriesStroke(1, new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5}, 0));
        
        // Make shapes invisible for cleaner look
        linesRenderer.setSeriesShapesVisible(0, false);
        linesRenderer.setSeriesShapesVisible(1, false);
        
        plot.setRenderer(1, linesRenderer);
    }

    private void addVolume(XYPlot plot) {
        XYDataset volumeDataset = createVolumeDataset();
        int volumeIndex = plot.getDatasetCount();
        plot.setDataset(volumeIndex, volumeDataset);
        org.jfree.chart.axis.NumberAxis volumeAxis = new org.jfree.chart.axis.NumberAxis("Volume");
        volumeAxis.setAutoRangeIncludesZero(true);
        plot.setRangeAxis(volumeIndex, volumeAxis);
        plot.mapDatasetToRangeAxis(volumeIndex, volumeIndex);
        org.jfree.chart.renderer.xy.XYLineAndShapeRenderer volumeRenderer = new org.jfree.chart.renderer.xy.XYLineAndShapeRenderer();
        volumeRenderer.setSeriesPaint(0, new java.awt.Color(200, 100, 0));
        volumeRenderer.setSeriesStroke(0, new java.awt.BasicStroke(2.0f));
        volumeRenderer.setSeriesShapesVisible(0, false);
        plot.setRenderer(volumeIndex, volumeRenderer);
    }

    private void updateCharts() {
        removeAll();
        add(indicatorPanel, BorderLayout.NORTH);
        chartPanel = createMainChartPanel();
        rsiChartPanel = showRSI ? createRSIChartPanel() : null;
        JPanel chartsPanel = new JPanel();
        chartsPanel.setLayout(new BoxLayout(chartsPanel, BoxLayout.Y_AXIS));
        chartsPanel.add(chartPanel);
        if (rsiChartPanel != null) chartsPanel.add(rsiChartPanel);
        add(chartsPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    public void updateForSymbol() {
        updateCharts();
    }
} 