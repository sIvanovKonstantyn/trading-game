package com.tradinggame;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;

public class LeaderboardPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;

    public LeaderboardPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        String[] columns = {"Player", "Initial Balance", "Final Balance", "PnL"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setBackground(Color.WHITE);
        table.setForeground(Color.BLACK);
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        table.getTableHeader().setForeground(Color.BLACK);
        loadLeaderboard();
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadLeaderboard() {
        java.util.List<LeaderboardEntry> entries = new java.util.ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("leaderboard.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    String player = parts[0];
                    double initial = Double.parseDouble(parts[1]);
                    double fin = Double.parseDouble(parts[2]);
                    double pnl = Double.parseDouble(parts[3]);
                    entries.add(new LeaderboardEntry(player, initial, fin, pnl));
                }
            }
        } catch (IOException e) {
            // File may not exist yet, that's fine
        }
        // Sort by PnL descending
        entries.sort((a, b) -> Double.compare(b.pnl, a.pnl));
        // Add to table
        tableModel.setRowCount(0);
        for (LeaderboardEntry entry : entries) {
            tableModel.addRow(new Object[] {
                entry.player,
                String.format("$%.2f", entry.initial),
                String.format("$%.2f", entry.finalBal),
                String.format("$%.2f", entry.pnl)
            });
        }
    }

    private static class LeaderboardEntry {
        String player;
        double initial;
        double finalBal;
        double pnl;
        LeaderboardEntry(String player, double initial, double finalBal, double pnl) {
            this.player = player;
            this.initial = initial;
            this.finalBal = finalBal;
            this.pnl = pnl;
        }
    }
} 