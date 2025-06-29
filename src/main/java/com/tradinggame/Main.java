package com.tradinggame;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        // Run the application on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            // Show startup dialog
            TradingGameFrame frame = new TradingGameFrame();
            StartupDialog dialog = new StartupDialog(frame);
            dialog.setVisible(true);
            
            if (dialog.isConfirmed()) {
                // Start the game with the provided parameters
                frame.startGame(
                    dialog.getPlayerName(),
                    dialog.getStartDate(),
                    dialog.getEndDate(),
                    dialog.getInitialBalance()
                );
                frame.setVisible(true);
            } else {
                // User cancelled, exit the application
                System.exit(0);
            }
        });
    }
} 