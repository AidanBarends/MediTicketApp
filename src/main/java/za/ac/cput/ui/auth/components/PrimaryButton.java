package za.ac.cput.ui.auth.components;

import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;

import javax.swing.*;
import java.awt.*;

public class PrimaryButton extends JButton {

    public PrimaryButton(String text) {
        super(text);
        setFont(FontManager.bodyFont(Font.BOLD, 15));
        setForeground(AppTheme.TEXT_ON_PRIMARY);
        setBackground(AppTheme.PRIMARY);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(0, 46));

        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                setBackground(AppTheme.PRIMARY_DARK);
                repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                setBackground(AppTheme.PRIMARY);
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppTheme.RADIUS_MD, AppTheme.RADIUS_MD);
        g2.dispose();
        super.paintComponent(g);
    }
}