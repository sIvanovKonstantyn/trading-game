package com.tradinggame;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class LoadingDialog extends JDialog {
    private JLabel loadingLabel;
    private JLabel messageLabel;
    private Timer messageTimer;
    private int messageIndex = 0;
    private Random random = new Random();
    
    private final String[] funnyMessages = {
        "Mining digital gold... ⛏️",
        "Teaching AI to trade... 🤖",
        "Convincing Bitcoin to go to the moon... 🚀",
        "Calculating the meaning of life, the universe, and crypto... 🌌",
        "Summoning the crypto gods... 🙏",
        "Brewing coffee for the trading algorithms... ☕",
        "Convincing your wallet it's not empty... 💸",
        "Teaching old money new tricks... 🎩",
        "Downloading more RAM... 💾",
        "Convincing the market to behave... 📈",
        "Finding the perfect trading strategy... 🔍",
        "Convincing Bitcoin that 2+2=4... 🧮",
        "Teaching bears to be bulls... 🐻➡️🐮",
        "Convincing your bank that crypto is real... 🏦",
        "Loading diamond hands... 💎",
        "Convincing the moon to come closer... 🌙",
        "Teaching paper hands to be strong... 📄➡️💪",
        "Convincing the market that HODL is a strategy... 📊",
        "Loading infinite wisdom... 🧠",
        "Convincing your cat to invest in crypto... 🐱",
        "Teaching the internet to be faster... 🌐",
        "Convincing your computer that it's not too old... 💻",
        "Loading the secret sauce... 🥫",
        "Convincing time to go faster... ⏰",
        "Teaching money to multiply... ✖️",
        "Convincing the charts to be more predictable... 📊",
        "Loading the matrix... 🕶️",
        "Convincing your future self to thank you... 👤",
        "Teaching patience to impatient traders... 😤➡️😌"
    };

    public LoadingDialog(JFrame parent) {
        super(parent, "Loading Crypto Trading Simulator", false);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        
        initComponents();
        setupLayout();
        startMessageRotation();
        
        // Add timeout mechanism - close dialog after 60 seconds if not closed manually
        Timer timeoutTimer = new Timer(60000, e -> {
            System.err.println("Loading dialog timeout reached - forcing close");
            close();
        });
        timeoutTimer.setRepeats(false);
        timeoutTimer.start();
        
        // Center the dialog on screen
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        loadingLabel = new JLabel("Loading...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Arial", Font.BOLD, 24));
        loadingLabel.setForeground(new Color(52, 152, 219));
        
        messageLabel = new JLabel("", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        messageLabel.setForeground(new Color(44, 62, 80));
        
        // Create a progress bar for visual feedback
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(300, 20));
    }

    private void setupLayout() {
        setLayout(new BorderLayout(20, 20));
        
        // Main panel with padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(new Color(236, 240, 241));
        
        // Add components with spacing
        mainPanel.add(Box.createVerticalGlue());
        
        // App title
        JLabel titleLabel = new JLabel("Crypto Trading Simulator", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(41, 128, 185));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Loading label
        loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(loadingLabel);
        
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Progress bar
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(300, 20));
        progressBar.setMaximumSize(new Dimension(300, 20));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(progressBar);
        
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Message label
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(messageLabel);
        
        mainPanel.add(Box.createVerticalGlue());
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Set dialog size
        setSize(450, 250);
    }

    private void startMessageRotation() {
        // Show initial message
        updateMessage();
        
        // Create timer to rotate messages every 2 seconds
        messageTimer = new Timer(2000, e -> updateMessage());
        messageTimer.start();
    }

    private void updateMessage() {
        // Get a random message
        String message = funnyMessages[random.nextInt(funnyMessages.length)];
        messageLabel.setText(message);
    }

    public void close() {
        if (messageTimer != null) {
            messageTimer.stop();
        }
        dispose();
    }
} 