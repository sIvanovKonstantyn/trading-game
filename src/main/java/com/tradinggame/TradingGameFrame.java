package com.tradinggame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TradingGameFrame extends JFrame {
    private GameState gameState;
    private EnhancedChartPanel chartPanel;
    private OrderPanel orderPanel;
    private BalancePanel balancePanel;
    private OrdersListPanel ordersListPanel;
    private NewsPanel newsPanel;
    private JButton nextDayButton;
    private JLabel currentDateLabel;
    private JLabel gameStatusLabel;

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
        
        // Initialize panels
        chartPanel = new EnhancedChartPanel(gameState);
        orderPanel = new OrderPanel(gameState);
        balancePanel = new BalancePanel(gameState);
        ordersListPanel = new OrdersListPanel(gameState);
        newsPanel = new NewsPanel(gameState);
        
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
        add(centerPanel, BorderLayout.CENTER);

        // Right panel for trading controls and news
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

        // Orders list
        JPanel ordersWrapper = new JPanel(new BorderLayout());
        ordersWrapper.setBorder(BorderFactory.createTitledBorder("Open Orders"));
        ordersWrapper.add(ordersListPanel, BorderLayout.CENTER);
        rightPanel.add(ordersWrapper, BorderLayout.SOUTH);

        add(rightPanel, BorderLayout.EAST);

        // News panel at bottom
        JPanel newsWrapper = new JPanel(new BorderLayout());
        newsWrapper.setBorder(BorderFactory.createTitledBorder("Market News"));
        newsWrapper.add(newsPanel, BorderLayout.CENTER);
        add(newsWrapper, BorderLayout.SOUTH);
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
                    showGameResults();
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
        double finalBalance = gameState.getCurrentBalance();
        double pnl = finalBalance - initialBalance;
        double pnlPercentage = (pnl / initialBalance) * 100;

        // Create custom results dialog
        JDialog resultsDialog = new JDialog(this, "Game Results", true);
        resultsDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        resultsDialog.setResizable(false);
        resultsDialog.setSize(400, 300);
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
            "Final Balance: $%.2f USDC\n" +
            "PnL: $%.2f USDC (%.2f%%)\n\n" +
            "Thank you for playing the Crypto Trading Simulator!",
            gameState.getPlayerName(), initialBalance, finalBalance, pnl, pnlPercentage
        );
        resultsArea.setText(resultsText);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        JButton newGameButton = new JButton("New Game");
        newGameButton.setFont(new Font("Arial", Font.BOLD, 14));
        newGameButton.setPreferredSize(new Dimension(120, 35));
        
        JButton exitButton = new JButton("Exit");
        exitButton.setFont(new Font("Arial", Font.BOLD, 14));
        exitButton.setPreferredSize(new Dimension(120, 35));

        // Add action listeners
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
        mainPanel.add(new JScrollPane(resultsArea), BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        resultsDialog.add(mainPanel);
        resultsDialog.setVisible(true);
    }

    private void restartGame() {
        // Dispose current frame
        this.dispose();
        
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
                
                Timer showDialogTimer = new Timer(50, e -> {
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
} 