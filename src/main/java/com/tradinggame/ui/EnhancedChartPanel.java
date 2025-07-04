package com.tradinggame.ui;

import com.tradinggame.dtos.GameStateListener;
import com.tradinggame.dtos.PriceData;
import com.tradinggame.indicators.TechnicalIndicators;
import com.tradinggame.state.GameState;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnhancedChartPanel extends JPanel {
    private GameState gameState;
    private ChartPanel chartPanel;
    private JFreeChart chart;
    private JPanel indicatorPanel;
    private Map<String, JCheckBox> indicatorCheckboxes = new HashMap<>();
    private boolean showBollinger = true;
    private boolean showRSI = true;
    private boolean showVolume = true;
    private boolean showVWAP = false;
    private boolean showATR = false;
    private boolean showIchimoku = false;
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
        addIndicatorCheckbox("VWAP", false);
        addIndicatorCheckbox("ATR", false);
        addIndicatorCheckbox("Ichimoku Cloud", false);
        add(indicatorPanel, BorderLayout.NORTH);

        // Main chart panel
        chartPanel = createMainChartPanel();
        // RSI/ATR chart panel (may be null)
        rsiChartPanel = (showRSI || showATR) ? createIndicatorChartPanel() : null;

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
                    case "VWAP": showVWAP = checkBox.isSelected(); break;
                    case "ATR": showATR = checkBox.isSelected(); break;
                    case "Ichimoku Cloud": showIchimoku = checkBox.isSelected(); break;
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
        int datasetIndex = 1;
        if (showBollinger) { addBollingerBands(plot); datasetIndex++; }
        if (showVWAP) { addVWAP(plot, datasetIndex++); }
        if (showIchimoku) { addIchimokuCloud(plot, datasetIndex++); }
        if (showVolume) addVolume(plot);
        ChartPanel panel = new ChartPanel(mainChart);
        panel.setPreferredSize(new Dimension(800, 400));
        return panel;
    }

    private ChartPanel createIndicatorChartPanel() {
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        if (showRSI) {
            TimeSeries rsiSeries = new TimeSeries("RSI");
            List<PriceData> allPrices = getAllPriceHistory();
            LocalDate currentDate = gameState.getCurrentDate();
            if (allPrices != null && !allPrices.isEmpty()) {
                for (int i = 0; i < allPrices.size(); i++) {
                    if (allPrices.get(i).getTimestamp().toLocalDate().isAfter(currentDate)) break;
                    int startIndex = Math.max(0, i - 14);
                    List<PriceData> subList = allPrices.subList(startIndex, i + 1);
                    TechnicalIndicators.RSIResult rsi = TechnicalIndicators.calculateRSI(subList, 14);
                    if (rsi.isValid) {
                        LocalDateTime ldt = allPrices.get(i).getTimestamp();
                        Date date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
                        rsiSeries.addOrUpdate(new Millisecond(date), rsi.rsi);
                    }
                }
            }
            dataset.addSeries(rsiSeries);
        }
        if (showATR) {
            TimeSeries atrSeries = new TimeSeries("ATR %");
            List<PriceData> allPrices = getAllPriceHistory();
            LocalDate currentDate = gameState.getCurrentDate();
            if (allPrices != null && !allPrices.isEmpty()) {
                for (int i = 0; i < allPrices.size(); i++) {
                    if (allPrices.get(i).getTimestamp().toLocalDate().isAfter(currentDate)) break;
                    int startIndex = Math.max(0, i - 14);
                    List<PriceData> subList = allPrices.subList(startIndex, i + 1);
                    double atrPercent = TechnicalIndicators.calculateATRPercent(subList, 14);
                    if (subList.size() >= 15) {
                        LocalDateTime ldt = allPrices.get(i).getTimestamp();
                        Date date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
                        atrSeries.addOrUpdate(new Millisecond(date), atrPercent);
                    }
                }
            }
            dataset.addSeries(atrSeries);
        }
        JFreeChart indicatorChart = org.jfree.chart.ChartFactory.createTimeSeriesChart(
            (showRSI && showATR) ? "RSI & ATR %" : (showRSI ? "RSI" : "ATR %"),
            "Time",
            (showRSI && showATR) ? "Value" : (showRSI ? "RSI" : "ATR %"),
            dataset,
            true,
            true,
            false
        );
        org.jfree.chart.plot.XYPlot plot = (org.jfree.chart.plot.XYPlot) indicatorChart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(new Color(220, 220, 220));
        plot.setRangeGridlinePaint(new Color(220, 220, 220));
        plot.getDomainAxis().setLabelPaint(Color.BLACK);
        plot.getRangeAxis().setLabelPaint(Color.BLACK);
        plot.getDomainAxis().setTickLabelPaint(Color.BLACK);
        plot.getRangeAxis().setTickLabelPaint(Color.BLACK);
        org.jfree.chart.axis.DateAxis axis = (org.jfree.chart.axis.DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new java.text.SimpleDateFormat("MM-dd HH:mm"));
        org.jfree.chart.renderer.xy.XYLineAndShapeRenderer renderer = new org.jfree.chart.renderer.xy.XYLineAndShapeRenderer();
        int seriesIdx = 0;
        if (showRSI) {
            renderer.setSeriesPaint(seriesIdx, new java.awt.Color(0, 150, 0));
            renderer.setSeriesStroke(seriesIdx, new java.awt.BasicStroke(2.0f));
            renderer.setSeriesShapesVisible(seriesIdx, false);
            seriesIdx++;
        }
        if (showATR) {
            renderer.setSeriesPaint(seriesIdx, new java.awt.Color(128, 0, 255)); // Purple
            renderer.setSeriesStroke(seriesIdx, new java.awt.BasicStroke(2.0f));
            renderer.setSeriesShapesVisible(seriesIdx, false);
        }
        plot.setRenderer(renderer);
        ChartPanel panel = new ChartPanel(indicatorChart);
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

    private void addVWAP(XYPlot plot, int datasetIndex) {
        List<PriceData> priceHistory = getPriceHistory();
        LocalDate currentDate = gameState.getCurrentDate();
        TimeSeries vwapSeries = new TimeSeries("VWAP");
        double cumulativePV = 0;
        double cumulativeVolume = 0;
        for (PriceData pd : priceHistory) {
            if (!pd.getTimestamp().toLocalDate().isAfter(currentDate)) {
                cumulativePV += pd.getPrice() * pd.getVolume();
                cumulativeVolume += pd.getVolume();
                if (cumulativeVolume > 0) {
                    Date date = Date.from(pd.getTimestamp().atZone(ZoneId.systemDefault()).toInstant());
                    vwapSeries.addOrUpdate(new Millisecond(date), cumulativePV / cumulativeVolume);
                }
            }
        }
        TimeSeriesCollection vwapDataset = new TimeSeriesCollection();
        vwapDataset.addSeries(vwapSeries);
        plot.setDataset(datasetIndex, vwapDataset);
        XYLineAndShapeRenderer vwapRenderer = new XYLineAndShapeRenderer();
        vwapRenderer.setSeriesPaint(0, new Color(255, 140, 0)); // Orange
        vwapRenderer.setSeriesStroke(0, new BasicStroke(2.5f));
        vwapRenderer.setSeriesShapesVisible(0, false);
        plot.setRenderer(datasetIndex, vwapRenderer);
    }

    private void addIchimokuCloud(XYPlot plot, int datasetIndex) {
        List<PriceData> priceHistory = getPriceHistory();
        LocalDate currentDate = gameState.getCurrentDate();
        int n = priceHistory.size();
        TechnicalIndicators.IchimokuCloud ichimoku = TechnicalIndicators.calculateIchimokuCloud(priceHistory);
        TimeSeries tenkan = new TimeSeries("Tenkan-sen");
        TimeSeries kijun = new TimeSeries("Kijun-sen");
        TimeSeries senkouA = new TimeSeries("Senkou Span A");
        TimeSeries senkouB = new TimeSeries("Senkou Span B");
        TimeSeries chikou = new TimeSeries("Chikou Span");
        for (int i = 0; i < n; i++) {
            PriceData pd = priceHistory.get(i);
            if (!pd.getTimestamp().toLocalDate().isAfter(currentDate)) {
                Date date = Date.from(pd.getTimestamp().atZone(ZoneId.systemDefault()).toInstant());
                if (!Double.isNaN(ichimoku.tenkan[i])) tenkan.addOrUpdate(new Millisecond(date), ichimoku.tenkan[i]);
                if (!Double.isNaN(ichimoku.kijun[i])) kijun.addOrUpdate(new Millisecond(date), ichimoku.kijun[i]);
                if (!Double.isNaN(ichimoku.chikou[i])) chikou.addOrUpdate(new Millisecond(date), ichimoku.chikou[i]);
            }
            // Senkou A/B are plotted 26 periods ahead
            if (i >= 26 && i < n) {
                PriceData futurePd = priceHistory.get(i);
                Date futureDate = Date.from(futurePd.getTimestamp().atZone(ZoneId.systemDefault()).toInstant());
                if (!Double.isNaN(ichimoku.senkouA[i])) senkouA.addOrUpdate(new Millisecond(futureDate), ichimoku.senkouA[i]);
                if (!Double.isNaN(ichimoku.senkouB[i])) senkouB.addOrUpdate(new Millisecond(futureDate), ichimoku.senkouB[i]);
            }
        }
        TimeSeriesCollection ichimokuDataset = new TimeSeriesCollection();
        ichimokuDataset.addSeries(tenkan);
        ichimokuDataset.addSeries(kijun);
        ichimokuDataset.addSeries(senkouA);
        ichimokuDataset.addSeries(senkouB);
        ichimokuDataset.addSeries(chikou);
        plot.setDataset(datasetIndex, ichimokuDataset);
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.RED); // Tenkan-sen
        renderer.setSeriesStroke(0, new BasicStroke(1.5f));
        renderer.setSeriesPaint(1, new Color(255, 140, 0)); // Kijun-sen (dark orange)
        renderer.setSeriesStroke(1, new BasicStroke(1.5f));
        renderer.setSeriesPaint(2, new Color(0, 200, 0, 120)); // Senkou A (green, semi-transparent)
        renderer.setSeriesStroke(2, new BasicStroke(2.0f));
        renderer.setSeriesPaint(3, new Color(160, 82, 45, 120)); // Senkou B (brown, semi-transparent)
        renderer.setSeriesStroke(3, new BasicStroke(2.0f));
        renderer.setSeriesPaint(4, new Color(128, 0, 255)); // Chikou (purple)
        renderer.setSeriesStroke(4, new BasicStroke(1.5f));
        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesShapesVisible(1, false);
        renderer.setSeriesShapesVisible(2, false);
        renderer.setSeriesShapesVisible(3, false);
        renderer.setSeriesShapesVisible(4, false);
        plot.setRenderer(datasetIndex, renderer);
        // Fill cloud area between Senkou A and B
        org.jfree.chart.renderer.xy.XYDifferenceRenderer cloudRenderer = new org.jfree.chart.renderer.xy.XYDifferenceRenderer(
            new Color(0, 200, 0, 60), new Color(160, 82, 45, 60), false);
        cloudRenderer.setSeriesPaint(0, new Color(0, 200, 0, 60));
        cloudRenderer.setSeriesPaint(1, new Color(160, 82, 45, 60));
        plot.setDataset(datasetIndex + 1, new TimeSeriesCollection(senkouA));
        plot.setDataset(datasetIndex + 2, new TimeSeriesCollection(senkouB));
        plot.setRenderer(datasetIndex + 1, cloudRenderer);
    }

    private void updateCharts() {
        removeAll();
        add(indicatorPanel, BorderLayout.NORTH);
        chartPanel = createMainChartPanel();
        rsiChartPanel = (showRSI || showATR) ? createIndicatorChartPanel() : null;
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