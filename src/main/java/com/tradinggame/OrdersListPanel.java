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
        setPreferredSize(new Dimension(350, 200));
        
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
        String[] columnNames = {"Type", "Price", "Amount", "Date", "Cancel"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only Cancel button column is editable
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 4 ? JButton.class : String.class;
            }
        };
        
        ordersTable = new JTable(tableModel);
        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ordersTable.getTableHeader().setReorderingAllowed(false);
        ordersTable.setRowHeight(30); // Increase row height for buttons
        
        // Set column widths
        ordersTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        ordersTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        ordersTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        ordersTable.getColumnModel().getColumn(3).setPreferredWidth(70);
        ordersTable.getColumnModel().getColumn(4).setPreferredWidth(70);
        
        // Set up button renderer and editor
        ordersTable.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        ordersTable.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox()));
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
            for (int i = 0; i < openOrders.size(); i++) {
                Order order = openOrders.get(i);
                Object[] row = {
                    order.getType().toString(),
                    String.format("$%.2f", order.getPrice()),
                    String.format("%.4f", order.getAmount()),
                    order.getOrderDate().toString(),
                    "Cancel"
                };
                tableModel.addRow(row);
            }
        });
    }
    
    // Button renderer for the Cancel column
    private class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }
    
    // Button editor for the Cancel column
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
                // Cancel the order at the selected row
                int row = ordersTable.getSelectedRow();
                if (row >= 0 && row < gameState.getOpenOrders().size()) {
                    List<Order> openOrders = gameState.getOpenOrders();
                    Order orderToCancel = openOrders.get(row);
                    gameState.cancelOrder(orderToCancel);
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
} 