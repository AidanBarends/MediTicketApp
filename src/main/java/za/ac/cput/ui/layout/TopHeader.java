package za.ac.cput.ui.layout;

import za.ac.cput.session.SessionManager;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;
import za.ac.cput.ui.theme.ImageManager;

import javax.swing.*;
import java.awt.*;

public class TopHeader extends JPanel {

    private JLabel notificationBadge;

    public TopHeader() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER),
                BorderFactory.createEmptyBorder(AppTheme.SPACE_MD, AppTheme.SPACE_LG, AppTheme.SPACE_MD, AppTheme.SPACE_LG)
        ));
        setPreferredSize(new Dimension(0, 72));

        add(buildProfileSection(), BorderLayout.WEST);
        add(buildRightSection(), BorderLayout.EAST);
    }

    private JComponent buildProfileSection() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, AppTheme.SPACE_SM, 0));
        panel.setOpaque(false);
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        SessionManager session = SessionManager.getInstance();

        JLabel avatar = new JLabel(ImageManager.getCircularAvatar(null, 40));

        JPanel textStack = new JPanel();
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));
        textStack.setOpaque(false);

        String displayName = (session.getFullName() != null && !session.getFullName().isBlank())
                ? session.getFullName()
                : session.getEmail();

        JLabel nameLabel = new JLabel(displayName != null ? displayName : "—");
        nameLabel.setFont(FontManager.bodyFont(Font.BOLD, 14));
        nameLabel.setForeground(AppTheme.TEXT_PRIMARY);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String roleText = resolveRoleLabel(session);
        JLabel roleLabel = new JLabel(roleText);
        roleLabel.setFont(FontManager.bodyFont(Font.BOLD, 10));
        roleLabel.setForeground(AppTheme.TEXT_MUTED);
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textStack.add(nameLabel);
        textStack.add(roleLabel);

        panel.add(avatar);
        panel.add(textStack);
        return panel;
    }

    private String resolveRoleLabel(SessionManager session) {
        if ("CLINIC_STAFF".equals(session.getUserType())) {
            return "ADMIN".equals(session.getStaffRole()) ? "ADMINISTRATOR" : "NURSE";
        }
        if ("DOCTOR".equals(session.getUserType())) return "DOCTOR";
        if ("PATIENT".equals(session.getUserType())) return "PATIENT";
        return "";
    }

    private JComponent buildRightSection() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SPACE_MD, 0));
        panel.setOpaque(false);

        panel.add(buildNotificationBell());
        panel.add(buildSearchField());
        return panel;
    }

    private JComponent buildNotificationBell() {
        JPanel wrapper = new JPanel(null); // absolute positioning for the badge overlay
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(36, 36));

        JLabel bell = new JLabel("\uD83D\uDD14"); // 🔔
        bell.setFont(FontManager.bodyFont(Font.PLAIN, 20));
        bell.setBounds(0, 0, 36, 36);
        bell.setHorizontalAlignment(SwingConstants.CENTER);
        bell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        notificationBadge = new JLabel();
        notificationBadge.setOpaque(true);
        notificationBadge.setBackground(AppTheme.STATUS_DANGER);
        notificationBadge.setBounds(24, 2, 10, 10);
        notificationBadge.setVisible(false); // toggled via setUnreadCount()

        wrapper.add(notificationBadge);
        wrapper.add(bell);
        return wrapper;
    }

    /** Call once NotificationApiClient results are available for this user. */
    public void setUnreadCount(int count) {
        notificationBadge.setVisible(count > 0);
    }

    private JComponent buildSearchField() {
        JTextField search = new JTextField();
        search.putClientProperty("JTextField.placeholderText", "Search medical records...");
        search.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        search.setPreferredSize(new Dimension(260, 36));
        search.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));
        return search;
    }
}