package com.tradinggame;

import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;
import java.util.Map;

public class BalancePanel extends JPanel {
    private GameState gameState;
    private JTable balancesTable;
    private DefaultTableModel tableModel;
    private JLabel totalValueLabel;
    private JLabel tradingFeeLabel;
    private JPanel infoPanel;

    public BalancePanel(GameState gameState) {
        this.gameState = gameState;
        setLayout(new BorderLayout(10, 10));
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
        // Table for balances
        String[] columnNames = {"Symbol", "Crypto Balance", "USDC Balance", "Value (USDC)"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        balancesTable = new JTable(tableModel);
        balancesTable.setRowHeight(28);
        balancesTable.setFont(new Font("Arial", Font.PLAIN, 14));
        balancesTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        balancesTable.setFillsViewportHeight(true);
        balancesTable.setPreferredScrollableViewportSize(new Dimension(340, 120));
        // Info panel for total and fee
        infoPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        totalValueLabel = new JLabel("Total Value: $0.00");
        totalValueLabel.setFont(new Font("Arial", Font.BOLD, 15));
        totalValueLabel.setForeground(new Color(0, 100, 0));
        tradingFeeLabel = new JLabel("Trading Fee: 0.00%");
        tradingFeeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        infoPanel.add(totalValueLabel);
        infoPanel.add(tradingFeeLabel);
    }

    private void setupLayout() {
        removeAll();
        add(new JScrollPane(balancesTable), BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);
    }

    private void updateBalance() {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            double totalValue = 0.0;
            // USDC row
            double usdc = gameState.getUsdcBalance();
            if (usdc != 0.0) {
                totalValue += usdc;
                tableModel.addRow(new Object[]{"USDC", "", String.format("%.2f", usdc), String.format("$%.2f", usdc)});
            }
            // Crypto rows
            for (Map.Entry<String, Double> entry : gameState.getAllCryptoBalances().entrySet()) {
                String crypto = entry.getKey();
                double amount = entry.getValue();
                if (amount != 0.0) {
                    String symbol = crypto + "USDC";
                    SymbolState state = gameState.getSymbolStates().get(symbol);
                    double price = (state != null) ? state.getCurrentBtcPrice() : 0.0;
                    double value = amount * price;
                    totalValue += value;
                    tableModel.addRow(new Object[]{crypto, String.format("%.4f", amount), "", String.format("$%.2f", value)});
                }
            }
            totalValueLabel.setText(String.format("Total Value: $%.2f", totalValue));
            tradingFeeLabel.setText(String.format("Trading Fee: %.2f%%", gameState.getCurrentSymbolState().getTradingFee() * 100));
        });
    }

    private SymbolState getSymbolState() {
        return gameState.getCurrentSymbolState();
    }

    public void updateForSymbol() {
        repaint();
        revalidate();
    }
} 