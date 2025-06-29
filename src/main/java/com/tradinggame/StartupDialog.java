package com.tradinggame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class StartupDialog extends JDialog {
    private JTextField nameField;
    private JTextField startDateField;
    private JTextField endDateField;
    private JTextField balanceField;
    private JTextField feeField;
    private JButton startButton;
    private JButton cancelButton;
    
    private boolean confirmed = false;
    private String playerName;
    private LocalDate startDate;
    private LocalDate endDate;
    private double initialBalance;
    private double tradingFee;

    public StartupDialog(JFrame parent) {
        super(parent, "Start New Trading Game", true);
        
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        
        initComponents();
        setupLayout();
        setupEventHandlers();
        
        // Set default values
        nameField.setText("Player");
        startDateField.setText(LocalDate.now().minusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE));
        endDateField.setText(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        balanceField.setText("10000");
        feeField.setText("0.0001");
    }

    private void initComponents() {
        nameField = new JTextField(20);
        startDateField = new JTextField(20);
        endDateField = new JTextField(20);
        balanceField = new JTextField(20);
        feeField = new JTextField(20);
        startButton = new JButton("Start Game");
        cancelButton = new JButton("Cancel");
        
        // Set minimum width for text fields
        Dimension minSize = new Dimension(100, nameField.getPreferredSize().height);
        nameField.setMinimumSize(minSize);
        nameField.setPreferredSize(minSize);
        startDateField.setMinimumSize(minSize);
        startDateField.setPreferredSize(minSize);
        endDateField.setMinimumSize(minSize);
        endDateField.setPreferredSize(minSize);
        balanceField.setMinimumSize(minSize);
        balanceField.setPreferredSize(minSize);
        feeField.setMinimumSize(minSize);
        feeField.setPreferredSize(minSize);
        
        // Style components
        startButton.setFont(new Font("Arial", Font.BOLD, 12));
        cancelButton.setFont(new Font("Arial", Font.BOLD, 12));
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        
        // Main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Player Name
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("Player Name:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(nameField, gbc);
        
        // Start Date
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Start Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        mainPanel.add(startDateField, gbc);
        
        // End Date
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("End Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        mainPanel.add(endDateField, gbc);
        
        // Initial Balance
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Initial Balance (USDC):"), gbc);
        gbc.gridx = 1;
        mainPanel.add(balanceField, gbc);
        
        // Trading Fee
        gbc.gridx = 0; gbc.gridy = 4;
        mainPanel.add(new JLabel("Trading Fee (%):"), gbc);
        gbc.gridx = 1;
        mainPanel.add(feeField, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(startButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateInputs()) {
                    confirmed = true;
                    dispose();
                }
            }
        });
        
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmed = false;
                dispose();
            }
        });
    }

    private boolean validateInputs() {
        // Validate player name
        playerName = nameField.getText().trim();
        if (playerName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a player name.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Validate start date
        try {
            startDate = LocalDate.parse(startDateField.getText(), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid start date (YYYY-MM-DD).", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Validate end date
        try {
            endDate = LocalDate.parse(endDateField.getText(), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid end date (YYYY-MM-DD).", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Validate date range
        if (startDate.isAfter(endDate)) {
            JOptionPane.showMessageDialog(this, "Start date must be before or equal to end date.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (startDate.isAfter(LocalDate.now())) {
            JOptionPane.showMessageDialog(this, "Start date cannot be in the future.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Validate initial balance
        try {
            initialBalance = Double.parseDouble(balanceField.getText());
            if (initialBalance <= 0) {
                JOptionPane.showMessageDialog(this, "Initial balance must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid initial balance.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Validate trading fee
        try {
            tradingFee = Double.parseDouble(feeField.getText());
            if (tradingFee < 0 || tradingFee > 100) {
                JOptionPane.showMessageDialog(this, "Trading fee must be between 0% and 100%.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid trading fee.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }

    // Getters
    public boolean isConfirmed() { return confirmed; }
    public String getPlayerName() { return playerName; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public double getInitialBalance() { return initialBalance; }
    public double getTradingFee() { return tradingFee; }
} 