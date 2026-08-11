package za.ac.cput.ui.auth;

import za.ac.cput.ui.AppFrame;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RequestSubmittedPanel extends JPanel {

    public RequestSubmittedPanel(AppFrame appFrame) {
        setLayout(new GridBagLayout());
        setBackground(AppTheme.BACKGROUND);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppTheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(AppTheme.SPACE_XL, AppTheme.SPACE_XL, AppTheme.SPACE_XL, AppTheme.SPACE_XL)
        ));
        card.setPreferredSize(new Dimension(480, 380));

        JLabel icon = new JLabel("\u2709"); // envelope glyph
        icon.setFont(FontManager.bodyFont(Font.PLAIN, 40));
        icon.setForeground(AppTheme.PRIMARY);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel eyebrow = new JLabel("REQUEST SUBMITTED");
        eyebrow.setFont(FontManager.bodyFont(Font.BOLD, 12));
        eyebrow.setForeground(AppTheme.PRIMARY);
        eyebrow.setAlignmentX(Component.CENTER_ALIGNMENT);
        eyebrow.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_MD, 0, AppTheme.SPACE_XS, 0));

        JLabel title = new JLabel("Pending Admin Review");
        title.setFont(FontManager.headlineFont(Font.BOLD, 26));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel message = new JLabel("<html><div style='text-align:center;width:360px;'>"
                + "Your access request has been submitted. An administrator will review it, and if approved, "
                + "a secure signup link will be sent to your institutional email.</div></html>");
        message.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        message.setForeground(AppTheme.TEXT_SECONDARY);
        message.setAlignmentX(Component.CENTER_ALIGNMENT);
        message.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_SM, 0, AppTheme.SPACE_LG, 0));

        JLabel haveCode = new JLabel("Already have an invite code? Enter it here");
        haveCode.setFont(FontManager.bodyFont(Font.BOLD, 13));
        haveCode.setForeground(AppTheme.PRIMARY);
        haveCode.setAlignmentX(Component.CENTER_ALIGNMENT);
        haveCode.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        haveCode.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { appFrame.showScreen(AppFrame.SCREEN_VERIFY_INVITE); }
        });

        JLabel backToLogin = new JLabel("← Back to Login");
        backToLogin.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        backToLogin.setForeground(AppTheme.TEXT_SECONDARY);
        backToLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        backToLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backToLogin.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_MD, 0, 0, 0));
        backToLogin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { appFrame.showScreen(AppFrame.SCREEN_LOGIN); }
        });

        card.add(icon);
        card.add(eyebrow);
        card.add(title);
        card.add(message);
        card.add(haveCode);
        card.add(backToLogin);

        add(card);
    }
}