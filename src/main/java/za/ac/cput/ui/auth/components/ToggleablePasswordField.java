package za.ac.cput.ui.auth.components;

import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;

import javax.swing.*;
import java.awt.*;

public class ToggleablePasswordField extends JPanel {

    private final JPasswordField field = new JPasswordField();
    private boolean visible = false;
    private final char defaultEchoChar;
    private final JButton toggle;

    public ToggleablePasswordField(String labelText) {
        setLayout(new BorderLayout(0, 6));
        setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(FontManager.bodyFont(Font.BOLD, 13));
        label.setForeground(AppTheme.TEXT_PRIMARY);

        field.setFont(FontManager.bodyFont(Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(0, 42));
        defaultEchoChar = field.getEchoChar();

        toggle = new JButton("Show");
        toggle.setFont(FontManager.bodyFont(Font.PLAIN, 12));
        toggle.setForeground(AppTheme.TEXT_SECONDARY);
        toggle.setBorderPainted(false);
        toggle.setContentAreaFilled(false);
        toggle.setFocusPainted(false);
        toggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggle.addActionListener(e -> {
            visible = !visible;
            field.setEchoChar(visible ? (char) 0 : defaultEchoChar);
            toggle.setText(visible ? "Hide" : "Show");
        });

        JPanel fieldRow = new JPanel(new BorderLayout());
        fieldRow.setOpaque(false);
        fieldRow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(2, 12, 2, 6)
        ));
        field.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        fieldRow.add(field, BorderLayout.CENTER);
        fieldRow.add(toggle, BorderLayout.EAST);

        add(label, BorderLayout.NORTH);
        add(fieldRow, BorderLayout.CENTER);
    }

    public char[] getPassword() { return field.getPassword(); }

    /** Clears the field's text and resets visibility back to hidden. */
    public void clear() {
        field.setText("");
        visible = false;
        field.setEchoChar(defaultEchoChar);
        toggle.setText("Show");
    }
}