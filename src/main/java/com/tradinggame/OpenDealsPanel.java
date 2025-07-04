package com.tradinggame;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class OpenDealsPanel extends JPanel {
    private GameState gameState;
    private JTable dealsTable;
    private DefaultTableModel tableModel;
    private java.util.List<Order> openDeals = new java.util.ArrayList<>();
    private java.util.List<String> completedDeals = new java.util.ArrayList<>();
    private java.util.List<Double> completedPnLs = new java.util.ArrayList<>();

    public OpenDealsPanel(GameState gameState) {
        this.gameState = gameState;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(330, 200));
        initComponents();
        setupLayout();
        refreshOpenDeals();
        gameState.addGameStateListener(() -> {
            refreshOpenDeals();
            updateDealsList();
        });
    }

    private void initComponents() {
        String[] columnNames = {"Symbol", "Price (Open)", "Amount", "Date (Open)", "Mark Completed", "Close Price", "PnL"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only 'Mark Completed' and 'Close Price' columns are editable
                return column == 4 || column == 5;
            }
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 4) return JButton.class;
                if (column == 5) return String.class;
                return String.class;
            }
        };
        dealsTable = new JTable(tableModel);
        dealsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dealsTable.getTableHeader().setReorderingAllowed(false);
        dealsTable.setRowHeight(30);
        // Set column widths
        dealsTable.getColumnModel().getColumn(0).setPreferredWidth(65); // Symbol
        dealsTable.getColumnModel().getColumn(1).setPreferredWidth(85);
        dealsTable.getColumnModel().getColumn(2).setPreferredWidth(85);
        dealsTable.getColumnModel().getColumn(3).setPreferredWidth(75);
        dealsTable.getColumnModel().getColumn(4).setPreferredWidth(60);
        dealsTable.getColumnModel().getColumn(5).setPreferredWidth(85);
        dealsTable.getColumnModel().getColumn(6).setPreferredWidth(85);
        // Button renderer/editor for 'Mark Completed'
        dealsTable.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        dealsTable.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox()));
    }

    private void setupLayout() {
        JScrollPane scrollPane = new JScrollPane(dealsTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void refreshOpenDeals() {
        // Only add new BUY orders that are not already in openDeals and not executed
        java.util.List<Order> currentOpenBuys = new java.util.ArrayList<>();
        for (Order order : gameState.getOpenOrders()) {
            if (order.getType() == OrderType.BUY && !order.isExecuted() && !openDeals.contains(order)) {
                currentOpenBuys.add(order);
            }
        }
        // Add new ones
        for (Order order : currentOpenBuys) {
            if (!openDeals.contains(order)) {
                openDeals.add(order);
            }
        }
    }

    private void updateDealsList() {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            String symbol = getSymbolState().getSymbol();
            for (Order order : openDeals) {
                Object[] row = {
                    symbol,
                    String.format("$%.2f", order.getPrice()),
                    String.format("%.4f", order.getAmount()),
                    order.getOrderDate().toString(),
                    "Complete",
                    "", // Close price input
                    ""  // PnL
                };
                tableModel.addRow(row);
            }
        });
    }

    public java.util.List<String> getCompletedDeals() {
        return new java.util.ArrayList<>(completedDeals);
    }
    public java.util.List<Double> getCompletedPnLs() {
        return new java.util.ArrayList<>(completedPnLs);
    }

    // Button renderer for the Complete column
    private class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() { setOpaque(true); }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    // Button editor for the Complete column
    private class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }
        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                int row = dealsTable.getSelectedRow();
                if (row >= 0) {
                    // Get close price from the table
                    String closePriceStr = (String) tableModel.getValueAt(row, 5);
                    try {
                        double closePrice = Double.parseDouble(closePriceStr);
                        // Calculate PnL before removing
                        Order order = openDeals.get(row);
                        double pnl = (closePrice - order.getPrice()) * order.getAmount();
                        tableModel.setValueAt(String.format("$%.2f", pnl), row, 6);
                        // Store completed deal info
                        completedDeals.add(String.format("Buy %.4f BTC @ $%.2f, Closed @ $%.2f", order.getAmount(), order.getPrice(), closePrice));
                        completedPnLs.add(pnl);
                        // Remove from openDeals after showing PnL
                        openDeals.remove(row);
                        // Update table after a short delay to let user see PnL
                        javax.swing.Timer timer = new javax.swing.Timer(700, e -> updateDealsList());
                        timer.setRepeats(false);
                        timer.start();
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(OpenDealsPanel.this, "Enter a valid close price.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            isPushed = false;
            return label;
        }
        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }

    public void updateForSymbol() {
        repaint();
        revalidate();
    }

    private SymbolState getSymbolState() {
        return gameState.getCurrentSymbolState();
    }

    private String getCryptoSymbol() {
        String symbol = getSymbolState().getSymbol();
        if (symbol.endsWith("USDC")) {
            return symbol.replace("USDC", "");
        }
        return symbol;
    }
} 