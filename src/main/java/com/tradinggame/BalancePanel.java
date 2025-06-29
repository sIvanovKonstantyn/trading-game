package com.tradinggame;

import javax.swing.*;
import java.awt.*;

public class BalancePanel extends JPanel {
    private GameState gameState;
    private JLabel usdcBalanceLabel;
    private JLabel btcBalanceLabel;
    private JLabel totalValueLabel;

    public BalancePanel(GameState gameState) {
        this.gameState = gameState;
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        initComponents();
        setupLayout();
        
        // Add listener for game state changes
        gameState.addGameStateListener(new GameStateListener() {
            @Override
            public void onGameStateChanged() {
                updateBalance();
            }
        });
    }

    private void initComponents() {
        usdcBalanceLabel = new JLabel("USDC: $0.00");
        btcBalanceLabel = new JLabel("BTC: 0.0000");
        totalValueLabel = new JLabel("Total Value: $0.00");
        
        // Style labels
        Font boldFont = new Font("Arial", Font.BOLD, 14);
        usdcBalanceLabel.setFont(boldFont);
        btcBalanceLabel.setFont(boldFont);
        totalValueLabel.setFont(boldFont);
        
        totalValueLabel.setForeground(new Color(0, 100, 0)); // Dark green
    }

    private void setupLayout() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        add(usdcBalanceLabel, gbc);
        
        gbc.gridy = 1;
        add(btcBalanceLabel, gbc);
        
        gbc.gridy = 2;
        add(totalValueLabel, gbc);
    }

    private void updateBalance() {
        SwingUtilities.invokeLater(() -> {
            double usdcBalance = gameState.getCurrentBalance();
            double btcBalance = gameState.getBtcBalance();
            double currentBtcPrice = gameState.getCurrentBtcPrice();
            double totalValue = usdcBalance + (btcBalance * currentBtcPrice);
            
            usdcBalanceLabel.setText(String.format("USDC: $%.2f", usdcBalance));
            btcBalanceLabel.setText(String.format("BTC: %.4f", btcBalance));
            totalValueLabel.setText(String.format("Total Value: $%.2f", totalValue));
        });
    }
} 