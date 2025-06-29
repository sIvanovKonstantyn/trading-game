package com.tradinggame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class NewsPanel extends JPanel {
    private GameState gameState;
    private JTextArea newsTextArea;
    private JButton refreshButton;
    private OpenAiNewsService openAiNewsService;

    public NewsPanel(GameState gameState) {
        this.gameState = gameState;
        this.openAiNewsService = new OpenAiNewsService();
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
            
            // Get current game date
            if (gameState.isGameStarted() && gameState.getCurrentDate() != null) {
                newsContent.append("=== ").append(gameState.getCurrentDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append(" ===\n\n");
                
                // Generate news using OpenAI
                String aiNews = openAiNewsService.generateCryptoNews(gameState.getCurrentDate());
                newsContent.append(formatNews(aiNews));
            } else {
                newsContent.append("=== Game Not Started ===\n\n");
                newsContent.append("Start the game to see crypto news updates.");
            }
            
            newsTextArea.setText(newsContent.toString());
            newsTextArea.setCaretPosition(0); // Scroll to top
        });
    }

    /**
     * Parses and formats the OpenAI response for better readability in the text area.
     */
    private String formatNews(String aiNews) {
        if (aiNews == null || aiNews.isEmpty()) return "No news available.";
        StringBuilder formatted = new StringBuilder();
        String[] lines = aiNews.split("\n");
        boolean inTable = false;
        int colDate = -1, colNews = -1;
        // Try to detect a markdown table
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.startsWith("|") && line.endsWith("|")) {
                // Table header or row
                String[] cells = line.split("\\|");
                // Remove empty cells from split
                java.util.List<String> cellList = new java.util.ArrayList<>();
                for (String c : cells) {
                    String cc = c.trim();
                    if (!cc.isEmpty()) cellList.add(cc);
                }
                if (!inTable) {
                    // Header row
                    inTable = true;
                    for (int j = 0; j < cellList.size(); j++) {
                        String cell = cellList.get(j).toLowerCase();
                        if (cell.contains("date")) colDate = j;
                        if (cell.contains("news") || cell.contains("event") || cell.contains("headline")) colNews = j;
                    }
                } else if (cellList.size() > 1 && (colDate != -1 && colNews != -1)) {
                    // Data row
                    String date = cellList.get(colDate);
                    String news = cellList.get(colNews);
                    formatted.append("• ").append(date).append(": ").append(news).append("\n");
                }
            } else if (inTable && line.matches("^\\s*\\|?\\s*-+.*-+\\s*\\|?\\s*$")) {
                // Table separator row, skip
                continue;
            } else {
                // Not a table, just append as plain text
                if (!inTable && !line.isEmpty()) {
                    formatted.append("• ").append(line).append("\n");
                }
            }
        }
        if (formatted.length() == 0) {
            // Fallback: show original
            return aiNews;
        }
        return formatted.toString();
    }
} 