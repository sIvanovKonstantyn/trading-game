package com.tradinggame.utils;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;

public class TableUtils {
    /**
     * Returns a JButton-based TableCellRenderer with the given label and color.
     */
    public static TableCellRenderer createButtonRenderer(Color bg, Color fg) {
        return new JButtonRenderer(bg, fg);
    }

    /**
     * Returns a JButton-based TableCellEditor with the given color.
     */
    public static TableCellEditor createButtonEditor(Color bg, Color fg, Runnable onClick) {
        return new ButtonEditor(new JCheckBox(), bg, fg, onClick);
    }

    // --- Inner classes for renderer/editor ---
    private static class JButtonRenderer extends JButton implements TableCellRenderer {
        public JButtonRenderer(Color bg, Color fg) {
            setOpaque(true);
            setBackground(bg);
            setForeground(fg);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    private static class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private boolean isPushed;
        private Runnable onClick;
        public ButtonEditor(JCheckBox checkBox, Color bg, Color fg, Runnable onClick) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setBackground(bg);
            button.setForeground(fg);
            this.onClick = onClick;
            button.addActionListener(e -> fireEditingStopped());
        }
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            button.setText((value == null) ? "" : value.toString());
            isPushed = true;
            return button;
        }
        @Override
        public Object getCellEditorValue() {
            if (isPushed && onClick != null) {
                onClick.run();
            }
            isPushed = false;
            return button.getText();
        }
        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }
} 