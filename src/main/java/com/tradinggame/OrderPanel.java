package com.tradinggame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;

public class OrderPanel extends JPanel {
    private GameState gameState;
    private JComboBox<OrderType> orderTypeCombo;
    private JTextField priceField;
    private JTextField amountField;
    private JComboBox<String> orderDateCombo;
    private JButton placeOrderButton;

    public OrderPanel(GameState gameState) {
        this.gameState = gameState;
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        initComponents();
        setupLayout();
        setupEventHandlers();
        
        // Add listener for game state changes
        gameState.addGameStateListener(new GameStateListener() {
            @Override
            public void onGameStateChanged() {
                updateOrderDateCombo();
            }
        });
    }

    private void initComponents() {
        orderTypeCombo = new JComboBox<>(OrderType.values());
        priceField = new JTextField(10);
        amountField = new JTextField(10);
        orderDateCombo = new JComboBox<>();
        placeOrderButton = new JButton("Place Order");
        
        // Set default values
        priceField.setText("50000");
        amountField.setText("0.001");
        
        // Set minimum width for text fields and combo box
        Dimension minSize = new Dimension(100, priceField.getPreferredSize().height);
        priceField.setMinimumSize(minSize);
        priceField.setPreferredSize(minSize);
        amountField.setMinimumSize(minSize);
        amountField.setPreferredSize(minSize);
        orderDateCombo.setMinimumSize(minSize);
        orderDateCombo.setPreferredSize(minSize);
        
        // Style components
        placeOrderButton.setFont(new Font("Arial", Font.BOLD, 12));
    }

    private void setupLayout() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Order Type
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Order Type:"), gbc);
        gbc.gridx = 1;
        add(orderTypeCombo, gbc);
        
        // Price
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Price (USDC):"), gbc);
        gbc.gridx = 1;
        add(priceField, gbc);
        
        // Amount
        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Amount (BTC):"), gbc);
        gbc.gridx = 1;
        add(amountField, gbc);
        
        // Order Date
        gbc.gridx = 0; gbc.gridy = 3;
        add(new JLabel("Order Date:"), gbc);
        gbc.gridx = 1;
        add(orderDateCombo, gbc);
        
        // Place Order Button
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(placeOrderButton, gbc);
        
        updateOrderDateCombo();
    }

    private void setupEventHandlers() {
        placeOrderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placeOrder();
            }
        });
    }

    private void placeOrder() {
        try {
            OrderType type = (OrderType) orderTypeCombo.getSelectedItem();
            double price = Double.parseDouble(priceField.getText());
            double amount = Double.parseDouble(amountField.getText());
            
            String selectedDateStr = (String) orderDateCombo.getSelectedItem();
            if (selectedDateStr == null) {
                JOptionPane.showMessageDialog(this, "Please select an order date.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            LocalDate orderDate = LocalDate.parse(selectedDateStr);
            
            // Validate inputs
            if (price <= 0 || amount <= 0) {
                JOptionPane.showMessageDialog(this, "Price and amount must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check if order date is in the past
            if (orderDate.isBefore(gameState.getCurrentDate())) {
                JOptionPane.showMessageDialog(this, "Cannot place orders in the past.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check balance for buy orders
            if (type == OrderType.BUY) {
                double cost = price * amount;
                if (cost > gameState.getCurrentBalance()) {
                    JOptionPane.showMessageDialog(this, 
                        "Insufficient USDC balance. Cost: $" + String.format("%.2f", cost) + 
                        ", Balance: $" + String.format("%.2f", gameState.getCurrentBalance()), 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                // Check BTC balance for sell orders
                if (amount > gameState.getBtcBalance()) {
                    JOptionPane.showMessageDialog(this, 
                        "Insufficient BTC balance. Required: " + String.format("%.4f", amount) + 
                        " BTC, Available: " + String.format("%.4f", gameState.getBtcBalance()) + " BTC", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            gameState.placeOrder(type, price, amount, orderDate);
            
            JOptionPane.showMessageDialog(this, 
                "Order placed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for price and amount.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error placing order: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateOrderDateCombo() {
        orderDateCombo.removeAllItems();
        
        if (gameState.isGameStarted() && !gameState.isGameFinished()) {
            LocalDate currentDate = gameState.getCurrentDate();
            LocalDate endDate = gameState.getEndDate();
            
            // Add current date and future dates up to end date
            LocalDate date = currentDate;
            while (!date.isAfter(endDate)) {
                orderDateCombo.addItem(date.toString());
                date = date.plusDays(1);
            }
        }
    }
} 