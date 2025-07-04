package com.tradinggame.ui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import com.tradinggame.state.GameState;
import com.tradinggame.dtos.GameStateListener;
import com.tradinggame.dtos.PriceData;

public class PriceChartPanel extends JPanel {
    private GameState gameState;
    private ChartPanel chartPanel;
    private JFreeChart chart;

    public PriceChartPanel(GameState gameState) {
        this.gameState = gameState;
        setLayout(new BorderLayout());
        
        // Create initial chart
        createChart();
        
        // Add listener for game state changes
        gameState.addGameStateListener(new GameStateListener() {
            @Override
            public void onGameStateChanged() {
                updateChart();
            }
        });
    }

    private void createChart() {
        XYDataset dataset = createDataset();
        
        chart = ChartFactory.createTimeSeriesChart(
            "BTC/USDC Price Chart",
            "Time",
            "Price (USDC)",
            dataset,
            true,
            true,
            false
        );
        
        // Customize the chart
        XYPlot plot = (XYPlot) chart.getPlot();
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("MM-dd HH:mm"));
        
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 400));
        
        add(chartPanel, BorderLayout.CENTER);
    }
    
    private PriceData findClosestPoint(List<PriceData> priceHistory, double targetX) {
        if (priceHistory.isEmpty()) return null;
        
        // Convert targetX (which is in milliseconds since epoch) to LocalDateTime
        Date targetDate = new Date((long) targetX);
        LocalDateTime targetDateTime = targetDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        
        PriceData closest = priceHistory.get(0);
        long minDiff = Math.abs(targetDateTime.toEpochSecond(java.time.ZoneOffset.UTC) - 
                               closest.getTimestamp().toEpochSecond(java.time.ZoneOffset.UTC));
        
        for (PriceData point : priceHistory) {
            long diff = Math.abs(targetDateTime.toEpochSecond(java.time.ZoneOffset.UTC) - 
                               point.getTimestamp().toEpochSecond(java.time.ZoneOffset.UTC));
            if (diff < minDiff) {
                minDiff = diff;
                closest = point;
            }
        }
        
        return closest;
    }

    private XYDataset createDataset() {
        TimeSeries series = new TimeSeries("BTC Price");
        
        List<PriceData> priceHistory = gameState.getPriceHistory();
        for (PriceData priceData : priceHistory) {
            LocalDateTime ldt = priceData.getTimestamp();
            Date date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
            series.add(new Millisecond(date), priceData.getPrice());
        }
        
        return new TimeSeriesCollection(series);
    }

    private void updateChart() {
        SwingUtilities.invokeLater(() -> {
            if (chartPanel != null) {
                XYDataset dataset = createDataset();
                chart.getXYPlot().setDataset(dataset);
                chartPanel.revalidate();
                chartPanel.repaint();
            }
        });
    }
} 