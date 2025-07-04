package com.tradinggame.utils;

import javax.swing.*;
import java.awt.*;

public class DialogUtils {
    /**
     * Shows a loading dialog with the given parent and message.
     */
    public static JDialog showLoadingDialog(JFrame parent, String message) {
        JDialog dialog = new JDialog(parent, "Loading", false);
        JLabel label = new JLabel(message, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        dialog.add(label);
        dialog.setSize(350, 120);
        dialog.setLocationRelativeTo(parent);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setResizable(false);
        dialog.setVisible(true);
        return dialog;
    }

    /**
     * Closes the given dialog safely.
     */
    public static void closeDialog(JDialog dialog) {
        if (dialog != null) {
            dialog.dispose();
        }
    }

    /**
     * Creates and starts a one-shot timer with the given delay and action.
     */
    public static Timer startOneShotTimer(int delayMs, Runnable action) {
        Timer timer = new Timer(delayMs, e -> action.run());
        timer.setRepeats(false);
        timer.start();
        return timer;
    }
} 