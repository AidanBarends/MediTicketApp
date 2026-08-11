package za.ac.cput.ui.auth;

import za.ac.cput.ui.AppFrame;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;
import za.ac.cput.ui.theme.ImageManager;

import javax.swing.*;
import java.awt.*;

public class EmployeeSignupSuccessPanel extends JPanel {

    public EmployeeSignupSuccessPanel(AppFrame appFrame) {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BACKGROUND);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_LG, AppTheme.SPACE_XL, AppTheme.SPACE_LG, AppTheme.SPACE_XL));
        header.add(new JLabel(ImageManager.getIcon(ImageManager.LOGO_PRIMARY, -1, 40)), BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppTheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(AppTheme.SPACE_XL, AppTheme.SPACE_XL, AppTheme.SPACE_XL, AppTheme.SPACE_XL)
        ));
        card.setPreferredSize(new Dimension(440, 420));

        JLabel check = new JLabel("\u2713");
        check.setFont(FontManager.bodyFont(Font.BOLD, 36));
        check.setForeground(AppTheme.STATUS_SUCCESS);
        check.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("<html><div style='text-align:center;'>Welcome to<br>MediTicket</div></html>");
        title.setFont(FontManager.headlineFont(Font.BOLD, 30));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_MD, 0, AppTheme.SPACE_SM, 0));

        JLabel message = new JLabel("<html><div style='text-align:center;width:320px;'>"
                + "Your professional profile has been successfully created. You can now access the "
                + "MediTicket portal using your credentials.</div></html>");
        message.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        message.setForeground(AppTheme.TEXT_SECONDARY);
        message.setAlignmentX(Component.CENTER_ALIGNMENT);
        message.setBorder(BorderFactory.createEmptyBorder(0, 0, AppTheme.SPACE_LG, 0));

        JButton backButton = new JButton("Back to Login →");
        backButton.setFont(FontManager.bodyFont(Font.BOLD, 14));
        backButton.setForeground(AppTheme.TEXT_ON_PRIMARY);
        backButton.setBackground(AppTheme.PRIMARY);
        backButton.setFocusPainted(false);
        backButton.setBorderPainted(false);
        backButton.setPreferredSize(new Dimension(200, 44));
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> appFrame.showScreen(AppFrame.SCREEN_LOGIN));

        card.add(check);
        card.add(title);
        card.add(message);
        card.add(backButton);

        center.add(card);
        add(center, BorderLayout.CENTER);
    }
}