package com.tradinggame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.io.*;
import java.util.*;

public class TradingGameFrame extends JFrame {
    private GameState gameState;
    private EnhancedChartPanel chartPanel;
    private OrderPanel orderPanel;
    private BalancePanel balancePanel;
    private OrdersListPanel ordersListPanel;
    private OpenDealsPanel openDealsPanel;
    private JButton nextDayButton;
    private JLabel currentDateLabel;
    private JLabel gameStatusLabel;
    private boolean gameResultsShown = false;
    private JComboBox<String> symbolComboBox;

    public TradingGameFrame() {
        setTitle("Crypto Trading Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);

        initComponents();
        setupLayout();
        setupEventHandlers();
    }

    private void initComponents() {
        gameState = new GameState();
        
        // Initialize symbol selection
        symbolComboBox = new JComboBox<>(new String[]{"BTCUSDC", "ETHUSDC", "BNBUSDC"});
        symbolComboBox.setSelectedItem(gameState.getCurrentSymbol());
        symbolComboBox.addActionListener(e -> {
            String selected = (String) symbolComboBox.getSelectedItem();
            gameState.setCurrentSymbol(selected);
            updatePanelsForSymbol();
        });
        
        // Initialize panels
        chartPanel = new EnhancedChartPanel(gameState);
        orderPanel = new OrderPanel(gameState);
        balancePanel = new BalancePanel(gameState);
        ordersListPanel = new OrdersListPanel(gameState);
        openDealsPanel = new OpenDealsPanel(gameState);
        
        // Initialize buttons and labels
        nextDayButton = new JButton("Next Day");
        currentDateLabel = new JLabel("Current Date: Not Started");
        gameStatusLabel = new JLabel("Game Status: Not Started");
        
        // Style components
        nextDayButton.setFont(new Font("Arial", Font.BOLD, 14));
        currentDateLabel.setFont(new Font("Arial", Font.BOLD, 12));
        gameStatusLabel.setFont(new Font("Arial", Font.BOLD, 12));
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Top panel for game info
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.add(new JLabel("Symbol:"));
        topPanel.add(symbolComboBox);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(currentDateLabel);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(gameStatusLabel);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(nextDayButton);
        add(topPanel, BorderLayout.NORTH);

        // Center panel for charts
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Technical Analysis Charts"));
        centerPanel.add(chartPanel, BorderLayout.CENTER);

        // Right panel for trading controls
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(350, 0));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Balance panel at top
        JPanel balanceWrapper = new JPanel(new BorderLayout());
        balanceWrapper.setBorder(BorderFactory.createTitledBorder("Account Balance"));
        balanceWrapper.add(balancePanel, BorderLayout.CENTER);
        rightPanel.add(balanceWrapper, BorderLayout.NORTH);

        // Order panel in middle
        JPanel orderWrapper = new JPanel(new BorderLayout());
        orderWrapper.setBorder(BorderFactory.createTitledBorder("Place Order"));
        orderWrapper.add(orderPanel, BorderLayout.CENTER);
        rightPanel.add(orderWrapper, BorderLayout.CENTER);

        // Orders and Deals tabbed pane
        JTabbedPane ordersTabbedPane = new JTabbedPane();
        ordersTabbedPane.addTab("Open Orders", ordersListPanel);
        ordersTabbedPane.addTab("Open Deals", openDealsPanel);
        JPanel ordersWrapper = new JPanel(new BorderLayout());
        ordersWrapper.setBorder(BorderFactory.createTitledBorder("Orders & Deals"));
        ordersWrapper.add(ordersTabbedPane, BorderLayout.CENTER);
        rightPanel.add(ordersWrapper, BorderLayout.SOUTH);

        // Add a JSplitPane between centerPanel and rightPanel
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, centerPanel, rightPanel);
        splitPane.setResizeWeight(0.7); // 70% charts, 30% right panel by default
        splitPane.setDividerLocation(0.7);
        splitPane.setOneTouchExpandable(true);
        add(splitPane, BorderLayout.CENTER);
    }

    private void setupEventHandlers() {
        nextDayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gameState.nextDay();
                updateUI();
            }
        });

        // Add listeners for game state changes
        gameState.addGameStateListener(new GameStateListener() {
            @Override
            public void onGameStateChanged() {
                updateUI();
            }
        });
    }

    private void updateUI() {
        SwingUtilities.invokeLater(() -> {
            if (gameState.isGameStarted()) {
                currentDateLabel.setText("Current Date: " + 
                    gameState.getCurrentDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                
                if (gameState.isGameFinished()) {
                    gameStatusLabel.setText("Game Status: FINISHED");
                    nextDayButton.setEnabled(false);
                    if (!gameResultsShown) {
                        gameResultsShown = true;
                        showGameResults();
                    }
                } else {
                    gameStatusLabel.setText("Game Status: Active");
                    nextDayButton.setEnabled(true);
                }
            } else {
                currentDateLabel.setText("Current Date: Not Started");
                gameStatusLabel.setText("Game Status: Not Started");
                nextDayButton.setEnabled(false);
            }
        });
    }

    private void showGameResults() {
        double initialBalance = gameState.getInitialBalance();
        double finalBalance = gameState.getUsdcBalance();
        for (Map.Entry<String, Double> entry : gameState.getAllCryptoBalances().entrySet()) {
            String crypto = entry.getKey();
            double amount = entry.getValue();
            String symbol = crypto + "USDC";
            SymbolState state = gameState.getSymbolStates().get(symbol);
            double price = (state != null) ? state.getCurrentBtcPrice() : 0.0;
            finalBalance += amount * price;
        }
        double pnl = finalBalance - initialBalance;
        double pnlPercentage = (pnl / initialBalance) * 100;
        String playerName = gameState.getPlayerName();

        // Save to leaderboard.txt
        saveToLeaderboard(playerName, initialBalance, finalBalance, pnl);

        // Create custom results dialog
        JDialog resultsDialog = new JDialog(this, "Game Results", true);
        resultsDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        resultsDialog.setResizable(false);
        resultsDialog.setSize(600, 500);
        resultsDialog.setLocationRelativeTo(this);

        // Create main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Results text area
        JTextArea resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        resultsArea.setLineWrap(true);
        resultsArea.setWrapStyleWord(true);
        resultsArea.setFont(new Font("Arial", Font.BOLD, 14));
        resultsArea.setBackground(new Color(248, 248, 248));
        resultsArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String resultsText = String.format(
            "🎉 Game Finished! 🎉\n\n" +
            "Player: %s\n" +
            "Initial Balance: $%.2f USDC\n" +
            "Final Balance: $%.2f\n" +
            "PnL: $%.2f USDC (%.2f%%)\n\n" +
            "Thank you for playing the Crypto Trading Simulator!",
            playerName, initialBalance, finalBalance, pnl, pnlPercentage
        );
        resultsArea.setText(resultsText);

        // --- Open Deals Summary ---
        JTextArea dealsSummaryArea = new JTextArea();
        dealsSummaryArea.setEditable(false);
        dealsSummaryArea.setLineWrap(true);
        dealsSummaryArea.setWrapStyleWord(true);
        dealsSummaryArea.setFont(new Font("Arial", Font.PLAIN, 13));
        dealsSummaryArea.setBackground(new Color(248, 248, 248));
        dealsSummaryArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        StringBuilder dealsSummary = new StringBuilder();
        dealsSummary.append("\nCompleted Deals (PnL):\n");
        if (openDealsPanel != null && !openDealsPanel.getCompletedDeals().isEmpty()) {
            java.util.List<String> deals = openDealsPanel.getCompletedDeals();
            java.util.List<Double> pnls = openDealsPanel.getCompletedPnLs();
            for (int i = 0; i < deals.size(); i++) {
                dealsSummary.append(String.format("%s | PnL: $%.2f\n", deals.get(i), pnls.get(i)));
            }
        } else {
            dealsSummary.append("No deals completed.\n");
        }
        dealsSummaryArea.setText(dealsSummary.toString());
        // --- End Open Deals Summary ---

        // Leaderboard panel
        LeaderboardPanel leaderboardPanel = new LeaderboardPanel();

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton newGameButton = new JButton("New Game");
        newGameButton.setFont(new Font("Arial", Font.BOLD, 14));
        newGameButton.setPreferredSize(new Dimension(120, 35));
        JButton exitButton = new JButton("Exit");
        exitButton.setFont(new Font("Arial", Font.BOLD, 14));
        exitButton.setPreferredSize(new Dimension(120, 35));
        newGameButton.addActionListener(e -> {
            resultsDialog.dispose();
            restartGame();
        });
        exitButton.addActionListener(e -> {
            resultsDialog.dispose();
            System.exit(0);
        });
        buttonPanel.add(newGameButton);
        buttonPanel.add(exitButton);

        // Add components to main panel
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(resultsArea, BorderLayout.NORTH);
        northPanel.add(dealsSummaryArea, BorderLayout.CENTER);
        mainPanel.add(northPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(leaderboardPanel), BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        resultsDialog.setContentPane(mainPanel);
        resultsDialog.setVisible(true);
    }

    private void saveToLeaderboard(String playerName, double initial, double fin, double pnl) {
        try (FileWriter fw = new FileWriter("leaderboard.txt", true); BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(String.format("%s,%.2f,%.2f,%.2f\n", playerName, initial, fin, pnl));
        } catch (IOException e) {
            System.err.println("Failed to write to leaderboard.txt: " + e.getMessage());
        }
    }

    private void restartGame() {
        // Dispose current frame
        this.dispose();
        // Reset the guard for new game
        gameResultsShown = false;
        
        // Create new frame and startup dialog
        SwingUtilities.invokeLater(() -> {
            TradingGameFrame newFrame = new TradingGameFrame();
            StartupDialog dialog = new StartupDialog(newFrame);
            dialog.setVisible(true);
            
            if (dialog.isConfirmed()) {
                // Create loading dialog
                LoadingDialog loadingDialog = new LoadingDialog(newFrame);
                
                // Start game in background thread
                Thread backgroundThread = new Thread(() -> {
                    try {
                        System.out.println("Starting new game initialization in background thread...");
                        newFrame.startGame(
                            dialog.getPlayerName(),
                            dialog.getStartDate(),
                            dialog.getEndDate(),
                            dialog.getInitialBalance(),
                            dialog.getTradingFee()
                        );
                        System.out.println("New game initialization completed successfully.");
                        
                        SwingUtilities.invokeLater(() -> {
                            try {
                                System.out.println("Closing loading dialog and showing new main frame...");
                                loadingDialog.close();
                                newFrame.setVisible(true);
                                System.out.println("Loading dialog closed and new main frame shown.");
                            } catch (Exception e) {
                                System.err.println("Error updating UI: " + e.getMessage());
                                e.printStackTrace();
                                loadingDialog.close();
                                newFrame.setVisible(true);
                            }
                        });
                    } catch (Exception e) {
                        System.err.println("Error during new game initialization: " + e.getMessage());
                        e.printStackTrace();
                        
                        SwingUtilities.invokeLater(() -> {
                            loadingDialog.close();
                            newFrame.setVisible(true);
                        });
                    }
                });
                
                backgroundThread.start();
                
                javax.swing.Timer showDialogTimer = new javax.swing.Timer(50, e -> {
                    loadingDialog.setVisible(true);
                });
                showDialogTimer.setRepeats(false);
                showDialogTimer.start();
                
            } else {
                // User cancelled, exit the application
                System.exit(0);
            }
        });
    }

    public void startGame(String playerName, LocalDate startDate, LocalDate endDate, double initialBalance, double tradingFee) {
        // Start the game (this will load all the initial data)
        gameState.startGame(playerName, startDate, endDate, initialBalance, tradingFee);
        updateUI();
    }

    private void updatePanelsForSymbol() {
        chartPanel.updateForSymbol();
        orderPanel.updateForSymbol();
        balancePanel.updateForSymbol();
        ordersListPanel.updateForSymbol();
        openDealsPanel.updateForSymbol();
    }
} 