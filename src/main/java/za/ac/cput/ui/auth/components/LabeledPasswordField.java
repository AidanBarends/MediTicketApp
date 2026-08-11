package za.ac.cput.ui.auth.components;

import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LabeledPasswordField extends JPanel {

    private final JPasswordField field = new JPasswordField();
    private final JLabel forgotLink = new JLabel("Forgot password?");

    public LabeledPasswordField(String labelText) {
        setLayout(new BorderLayout(0, 6));
        setOpaque(false);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(FontManager.bodyFont(Font.BOLD, 13));
        label.setForeground(AppTheme.TEXT_PRIMARY);

        forgotLink.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        forgotLink.setForeground(AppTheme.PRIMARY);
        forgotLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        topRow.add(label, BorderLayout.WEST);
        topRow.add(forgotLink, BorderLayout.EAST);

        field.setFont(FontManager.bodyFont(Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(0, 42));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        add(topRow, BorderLayout.NORTH);
        add(field, BorderLayout.CENTER);
    }

    public char[] getPassword() { return field.getPassword(); }
    public JPasswordField getField() { return field; }

    public void onForgotPasswordClick(Runnable action) {
        forgotLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { action.run(); }
        });
    }
}