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
                // Create loading dialog but don't show it yet
                LoadingDialog loadingDialog = new LoadingDialog(frame);
                
                // Start game in background thread IMMEDIATELY
                Thread backgroundThread = new Thread(() -> {
                    try {
                        System.out.println("Starting game initialization in background thread...");
                        // Start the game (this will load all the initial data)
                        frame.startGame(
                            dialog.getPlayerName(),
                            dialog.getStartDate(),
                            dialog.getEndDate(),
                            dialog.getInitialBalance()
                        );
                        System.out.println("Game initialization completed successfully.");
                        
                        // Use SwingUtilities.invokeLater to update UI from background thread
                        SwingUtilities.invokeLater(() -> {
                            try {
                                System.out.println("Closing loading dialog and showing main frame...");
                                loadingDialog.close();
                                frame.setVisible(true);
                                System.out.println("Loading dialog closed and main frame shown.");
                            } catch (Exception e) {
                                System.err.println("Error updating UI: " + e.getMessage());
                                e.printStackTrace();
                                // Force close loading dialog and show frame even if there's an error
                                loadingDialog.close();
                                frame.setVisible(true);
                            }
                        });
                    } catch (Exception e) {
                        System.err.println("Error during game initialization: " + e.getMessage());
                        e.printStackTrace();
                        
                        // Ensure UI is updated even if there's an error
                        SwingUtilities.invokeLater(() -> {
                            loadingDialog.close();
                            frame.setVisible(true);
                        });
                    }
                });
                
                // Start the background thread immediately
                backgroundThread.start();
                
                // Show loading dialog after a very short delay to ensure background thread starts
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
} 