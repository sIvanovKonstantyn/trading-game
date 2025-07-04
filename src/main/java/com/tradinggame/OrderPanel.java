package com.tradinggame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class OrderPanel extends JPanel {
    private GameState gameState;
    private JComboBox<OrderType> orderTypeCombo;
    private JTextField priceField;
    private JTextField amountField;
    private JTextField usdcAmountField;
    private JComboBox<String> orderDateCombo;
    private JButton placeOrderButton;
    private JRadioButton btcAmountRadio;
    private JRadioButton usdcAmountRadio;
    private ButtonGroup amountTypeGroup;
    private JLabel cryptoAmountLabel;

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
        usdcAmountField = new JTextField(10);
        orderDateCombo = new JComboBox<>();
        placeOrderButton = new JButton("Place Order");
        
        // Radio buttons for amount type
        btcAmountRadio = new JRadioButton("Crypto Amount");
        usdcAmountRadio = new JRadioButton("USDC Amount");
        amountTypeGroup = new ButtonGroup();
        amountTypeGroup.add(btcAmountRadio);
        amountTypeGroup.add(usdcAmountRadio);
        btcAmountRadio.setSelected(true); // Default to crypto
        
        // Set default values
        priceField.setText("50000");
        amountField.setText("0.001");
        usdcAmountField.setText("50");
        
        // Set minimum width for text fields and combo box
        Dimension minSize = new Dimension(100, priceField.getPreferredSize().height);
        priceField.setMinimumSize(minSize);
        priceField.setPreferredSize(minSize);
        amountField.setMinimumSize(minSize);
        amountField.setPreferredSize(minSize);
        usdcAmountField.setMinimumSize(minSize);
        usdcAmountField.setPreferredSize(minSize);
        orderDateCombo.setMinimumSize(minSize);
        orderDateCombo.setPreferredSize(minSize);
        
        // Style components
        placeOrderButton.setFont(new Font("Arial", Font.BOLD, 12));
        
        // Dynamic crypto label
        cryptoAmountLabel = new JLabel();
        updateCryptoAmountLabel();
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
        
        // Amount Type Selection
        gbc.gridx = 0; gbc.gridy = 2;
        add(new JLabel("Amount Type:"), gbc);
        gbc.gridx = 1;
        JPanel amountTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        amountTypePanel.add(btcAmountRadio);
        amountTypePanel.add(usdcAmountRadio);
        add(amountTypePanel, gbc);
        
        // Crypto Amount (dynamic label)
        gbc.gridx = 0; gbc.gridy = 3;
        add(cryptoAmountLabel, gbc);
        gbc.gridx = 1;
        add(amountField, gbc);
        
        // USDC Amount
        gbc.gridx = 0; gbc.gridy = 4;
        add(new JLabel("Amount (USDC):"), gbc);
        gbc.gridx = 1;
        add(usdcAmountField, gbc);
        
        // Order Date
        gbc.gridx = 0; gbc.gridy = 5;
        add(new JLabel("Order Date:"), gbc);
        gbc.gridx = 1;
        add(orderDateCombo, gbc);
        
        // Place Order Button
        gbc.gridx = 0; gbc.gridy = 6;
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
        
        // Add listeners for automatic calculation
        // Document listeners for live recalculation
        priceField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { recalcAmounts(); }
            public void removeUpdate(DocumentEvent e) { recalcAmounts(); }
            public void changedUpdate(DocumentEvent e) { recalcAmounts(); }
        });
        amountField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { if (btcAmountRadio.isSelected()) calculateUsdcAmount(); }
            public void removeUpdate(DocumentEvent e) { if (btcAmountRadio.isSelected()) calculateUsdcAmount(); }
            public void changedUpdate(DocumentEvent e) { if (btcAmountRadio.isSelected()) calculateUsdcAmount(); }
        });
        usdcAmountField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { if (usdcAmountRadio.isSelected()) calculateBtcAmount(); }
            public void removeUpdate(DocumentEvent e) { if (usdcAmountRadio.isSelected()) calculateBtcAmount(); }
            public void changedUpdate(DocumentEvent e) { if (usdcAmountRadio.isSelected()) calculateBtcAmount(); }
        });
        
        btcAmountRadio.addActionListener(e -> {
            amountField.setEnabled(true);
            usdcAmountField.setEnabled(false);
        });
        
        usdcAmountRadio.addActionListener(e -> {
            amountField.setEnabled(false);
            usdcAmountField.setEnabled(true);
        });
        
        // Set initial state
        amountField.setEnabled(true);
        usdcAmountField.setEnabled(false);
    }
    
    private void recalcAmounts() {
        if (usdcAmountRadio.isSelected()) {
            calculateBtcAmount();
        } else {
            calculateUsdcAmount();
        }
    }
    
    private void calculateAmount() {
        try {
            double price = Double.parseDouble(priceField.getText());
            double usdcAmount = Double.parseDouble(usdcAmountField.getText());
            if (price > 0 && usdcAmount > 0) {
                double btcAmount = usdcAmount / price;
                amountField.setText(String.format("%.6f", btcAmount));
            }
        } catch (NumberFormatException ex) {
            // Ignore invalid input
        }
    }
    
    private void calculateUsdcAmount() {
        try {
            double price = Double.parseDouble(priceField.getText());
            double btcAmount = Double.parseDouble(amountField.getText());
            if (price > 0 && btcAmount > 0) {
                double usdcAmount = btcAmount * price;
                usdcAmountField.setText(String.format("%.2f", usdcAmount));
            }
        } catch (NumberFormatException ex) {
            // Ignore invalid input
        }
    }
    
    private void calculateBtcAmount() {
        try {
            double price = Double.parseDouble(priceField.getText());
            double usdcAmount = Double.parseDouble(usdcAmountField.getText());
            if (price > 0 && usdcAmount > 0) {
                double btcAmount = usdcAmount / price;
                amountField.setText(String.format("%.6f", btcAmount));
            }
        } catch (NumberFormatException ex) {
            // Ignore invalid input
        }
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
                if (cost > gameState.getUsdcBalance()) {
                    JOptionPane.showMessageDialog(this, 
                        "Insufficient USDC balance. Cost: $" + String.format("%.2f", cost) + 
                        ", Balance: $" + String.format("%.2f", gameState.getUsdcBalance()), 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                // Check crypto balance for sell orders
                String crypto = getCryptoSymbol();
                if (amount > gameState.getCryptoBalance(crypto)) {
                    JOptionPane.showMessageDialog(this, 
                        "Insufficient " + crypto + " balance. Required: " + String.format("%.4f", amount) + 
                        " " + crypto + ", Available: " + String.format("%.4f", gameState.getCryptoBalance(crypto)) + " " + crypto, 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            // Place order for the current symbol
            SymbolState state = getSymbolState();
            // Use state.getOpenOrders() etc. as needed
            
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

    private SymbolState getSymbolState() {
        return gameState.getCurrentSymbolState();
    }

    public void updateForSymbol() {
        updateCryptoAmountLabel();
        repaint();
        revalidate();
    }

    private String getCryptoSymbol() {
        String symbol = getSymbolState().getSymbol();
        if (symbol.endsWith("USDC")) {
            return symbol.replace("USDC", "");
        }
        return symbol;
    }

    private void updateCryptoAmountLabel() {
        String crypto = getCryptoSymbol();
        cryptoAmountLabel.setText("Amount (" + crypto + "):");
    }
} 