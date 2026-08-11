package za.ac.cput.ui.theme;

import javax.swing.*;
import java.awt.*;

/**
 * Branded replacement for raw JOptionPane dialogs. Used anywhere the app
 * needs to show a success/error/info confirmation to the user, styled
 * consistently with AppTheme rather than the platform's default look.
 */
public class AppDialog {

    public enum Type { SUCCESS, ERROR, INFO }

    public static void show(Component parent, String title, String message, Type type) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setSize(420, 220);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(AppTheme.SURFACE);
        content.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColorFor(type), 1, true),
                BorderFactory.createEmptyBorder(AppTheme.SPACE_LG, AppTheme.SPACE_LG, AppTheme.SPACE_LG, AppTheme.SPACE_LG)
        ));

        JLabel iconLabel = new JLabel(iconTextFor(type));
        iconLabel.setFont(FontManager.bodyFont(Font.BOLD, 28));
        iconLabel.setForeground(colorFor(type));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FontManager.headlineFont(Font.BOLD, 18));
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_SM, 0, AppTheme.SPACE_SM, 0));

        JLabel messageLabel = new JLabel("<html><div style='text-align:center;width:320px;'>"
                + message.replace("\n", "<br>") + "</div></html>");
        messageLabel.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        messageLabel.setForeground(AppTheme.TEXT_SECONDARY);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton okButton = new JButton("OK");
        okButton.setFont(FontManager.bodyFont(Font.BOLD, 14));
        okButton.setForeground(AppTheme.TEXT_ON_PRIMARY);
        okButton.setBackground(colorFor(type));
        okButton.setFocusPainted(false);
        okButton.setBorderPainted(false);
        okButton.setPreferredSize(new Dimension(100, 38));
        okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        okButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        okButton.addActionListener(e -> dialog.dispose());

        content.add(iconLabel);
        content.add(titleLabel);
        content.add(messageLabel);
        content.add(Box.createVerticalStrut(AppTheme.SPACE_LG));
        content.add(okButton);

        dialog.add(content, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private static Color colorFor(Type type) {
        return switch (type) {
            case SUCCESS -> AppTheme.STATUS_SUCCESS;
            case ERROR -> AppTheme.STATUS_DANGER;
            case INFO -> AppTheme.PRIMARY;
        };
    }

    private static Color borderColorFor(Type type) {
        return switch (type) {
            case SUCCESS -> AppTheme.STATUS_SUCCESS_BG;
            case ERROR -> AppTheme.STATUS_DANGER_BG;
            case INFO -> AppTheme.PRIMARY_LIGHT;
        };
    }

    private static String iconTextFor(Type type) {
        return switch (type) {
            case SUCCESS -> "\u2713"; // check mark
            case ERROR -> "\u2715";   // x mark
            case INFO -> "\u2139";    // info mark
        };
    }
}