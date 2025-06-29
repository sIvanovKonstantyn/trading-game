package com.tradinggame;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class OrdersListPanel extends JPanel {
    private GameState gameState;
    private JTable ordersTable;
    private DefaultTableModel tableModel;

    public OrdersListPanel(GameState gameState) {
        this.gameState = gameState;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(280, 200));
        
        initComponents();
        setupLayout();
        
        // Add listener for game state changes
        gameState.addGameStateListener(new GameStateListener() {
            @Override
            public void onGameStateChanged() {
                updateOrdersList();
            }
        });
    }

    private void initComponents() {
        // Create table model
        String[] columnNames = {"Type", "Price", "Amount", "Date"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        ordersTable = new JTable(tableModel);
        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ordersTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        ordersTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        ordersTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        ordersTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        ordersTable.getColumnModel().getColumn(3).setPreferredWidth(70);
    }

    private void setupLayout() {
        JScrollPane scrollPane = new JScrollPane(ordersTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        add(scrollPane, BorderLayout.CENTER);
    }

    private void updateOrdersList() {
        SwingUtilities.invokeLater(() -> {
            // Clear existing data
            tableModel.setRowCount(0);
            
            // Add open orders
            List<Order> openOrders = gameState.getOpenOrders();
            for (Order order : openOrders) {
                Object[] row = {
                    order.getType().toString(),
                    String.format("$%.2f", order.getPrice()),
                    String.format("%.4f", order.getAmount()),
                    order.getOrderDate().toString()
                };
                tableModel.addRow(row);
            }
        });
    }
} 