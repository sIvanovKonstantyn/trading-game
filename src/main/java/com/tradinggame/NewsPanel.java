package com.tradinggame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class NewsPanel extends JPanel {
    private GameState gameState;
    private JTextArea newsTextArea;
    private JButton refreshButton;
    private NewsService newsService;

    public NewsPanel(GameState gameState) {
        this.gameState = gameState;
        this.newsService = new NewsService();
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(300, 200));
        
        initComponents();
        setupLayout();
        setupEventHandlers();
        
        // Load initial news
        refreshNews();
        
        // Add listener for game state changes
        gameState.addGameStateListener(new GameStateListener() {
            @Override
            public void onGameStateChanged() {
                // Update news when game state changes
                refreshNews();
            }
        });
    }

    private void initComponents() {
        newsTextArea = new JTextArea();
        newsTextArea.setEditable(false);
        newsTextArea.setLineWrap(true);
        newsTextArea.setWrapStyleWord(true);
        newsTextArea.setFont(new Font("Arial", Font.PLAIN, 12));
        newsTextArea.setBackground(new Color(248, 248, 248));
        
        refreshButton = new JButton("Refresh News");
        refreshButton.setFont(new Font("Arial", Font.BOLD, 12));
    }

    private void setupLayout() {
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        headerPanel.add(new JLabel("Crypto News & Market Updates", SwingConstants.CENTER), BorderLayout.CENTER);
        headerPanel.add(refreshButton, BorderLayout.EAST);
        
        // News text area with scroll pane
        JScrollPane scrollPane = new JScrollPane(newsTextArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void setupEventHandlers() {
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshNews();
            }
        });
    }

    private void refreshNews() {
        SwingUtilities.invokeLater(() -> {
            StringBuilder newsContent = new StringBuilder();
            
            // Add current date/time
            newsContent.append("=== ").append(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))).append(" ===\n\n");
            
            // Add main news
            newsContent.append(newsService.getRandomNews()).append("\n\n");
            
            // Add technical analysis
            newsContent.append(newsService.getTechnicalAnalysis()).append("\n\n");
            
            // Add market update
            newsContent.append(newsService.getMarketUpdate());
            
            newsTextArea.setText(newsContent.toString());
            newsTextArea.setCaretPosition(0); // Scroll to top
        });
    }
} 