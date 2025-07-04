package com.tradinggame.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import com.tradinggame.state.GameState;
import com.tradinggame.dtos.Order;
import com.tradinggame.state.SymbolState;
import com.tradinggame.dtos.GameStateListener;
import com.tradinggame.utils.TableUtils;

public class OrdersListPanel extends JPanel {
    private GameState gameState;
    private JTable ordersTable;
    private DefaultTableModel tableModel;

    public OrdersListPanel(GameState gameState) {
        this.gameState = gameState;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(330, 200));
        setBackground(Color.WHITE);
        
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
        // Create table model with Cancel button column
        String[] columnNames = {"Symbol", "Type", "Price", "Amount", "Date", "Cancel"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only Cancel button column is editable
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 5 ? JButton.class : String.class;
            }
        };
        
        ordersTable = new JTable(tableModel);
        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ordersTable.getTableHeader().setReorderingAllowed(false);
        ordersTable.setRowHeight(28);
        ordersTable.setFont(new Font("Arial", Font.PLAIN, 14));
        ordersTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        ordersTable.setBackground(Color.WHITE);
        ordersTable.setForeground(Color.BLACK);
        ordersTable.getTableHeader().setBackground(new Color(240, 240, 240));
        ordersTable.getTableHeader().setForeground(Color.BLACK);
        
        // Set column widths
        ordersTable.getColumnModel().getColumn(0).setPreferredWidth(65); // Symbol
        ordersTable.getColumnModel().getColumn(1).setPreferredWidth(55);
        ordersTable.getColumnModel().getColumn(2).setPreferredWidth(85);
        ordersTable.getColumnModel().getColumn(3).setPreferredWidth(85);
        ordersTable.getColumnModel().getColumn(4).setPreferredWidth(75);
        ordersTable.getColumnModel().getColumn(5).setPreferredWidth(30);
        
        // Set up button renderer and editor
        ordersTable.getColumnModel().getColumn(5).setCellRenderer(
            TableUtils.createButtonRenderer(new Color(200, 230, 255), Color.BLACK)
        );
        ordersTable.getColumnModel().getColumn(5).setCellEditor(
            TableUtils.createButtonEditor(new Color(200, 230, 255), Color.BLACK, () -> {
                int row = ordersTable.getSelectedRow();
                if (row >= 0 && row < getSymbolState().getOpenOrders().size()) {
                    java.util.List<Order> openOrders = getSymbolState().getOpenOrders();
                    Order orderToCancel = openOrders.get(row);
                    gameState.cancelOrder(orderToCancel);
                }
            })
        );
    }

    private void setupLayout() {
        JScrollPane scrollPane = new JScrollPane(ordersTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void updateOrdersList() {
        SwingUtilities.invokeLater(() -> {
            // Clear existing data
            tableModel.setRowCount(0);
            
            // Add open orders
            List<Order> openOrders = getSymbolState().getOpenOrders();
            String symbol = getSymbolState().getSymbol();
            for (int i = 0; i < openOrders.size(); i++) {
                Order order = openOrders.get(i);
                Object[] row = {
                    symbol,
                    order.getType().toString(),
                    String.format("$%.2f", order.getPrice()),
                    String.format("%.4f", order.getAmount()),
                    order.getOrderDate().toString(),
                    "X"
                };
                tableModel.addRow(row);
            }
        });
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