package za.ac.cput.ui.patient.pages;

import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.api.BaseApiClient;
import za.ac.cput.model.domain.Notification;
import za.ac.cput.session.SessionManager;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;


public class NotificationsPage extends JPanel {

    private JPanel listContainer;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a");

    public NotificationsPage() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BACKGROUND);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_LG, AppTheme.SPACE_LG, AppTheme.SPACE_LG, AppTheme.SPACE_LG));

        content.add(buildHeader());
        content.add(Box.createVerticalStrut(AppTheme.SPACE_LG));

        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setOpaque(false);
        listContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(listContainer);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        loadData();
    }

    private JComponent buildHeader() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Notifications");
        title.setFont(FontManager.headlineFont(Font.BOLD, 26));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Updates about your appointments, tickets, and payments.");
        subtitle.setFont(FontManager.bodyFont(Font.PLAIN, 14));
        subtitle.setForeground(AppTheme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_XS, 0, 0, 0));

        panel.add(title);
        panel.add(subtitle);
        return panel;
    }



    private void loadData() {
        int patientId = SessionManager.getInstance().getUserId();
        BaseApiClient.ApiResult<List<Notification>> result =
                ApiClientProvider.getInstance().notifications().findByPatient(patientId);

        List<Notification> notifications = result.isSuccess() ? result.getData() : List.of();
        renderList(notifications);
    }

    private void renderList(List<Notification> notifications) {
        listContainer.removeAll();

        if (notifications.isEmpty()) {
            listContainer.add(emptyState());
            listContainer.revalidate();
            listContainer.repaint();
            return;
        }

        List<Notification> sorted = notifications.stream()
                .sorted(Comparator.comparing(
                        (Notification n) -> n.getNotificationDate() != null ? n.getNotificationDate() : java.time.LocalDateTime.MIN
                ).reversed())
                .toList();

        for (Notification notification : sorted) {
            listContainer.add(notificationCard(notification));
            listContainer.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
        }

        listContainer.revalidate();
        listContainer.repaint();
    }

    private JComponent notificationCard(Notification notification) {
        JPanel card = new JPanel(new BorderLayout(AppTheme.SPACE_MD, 0));
        card.setBackground(AppTheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(AppTheme.SPACE_MD, AppTheme.SPACE_MD, AppTheme.SPACE_MD, AppTheme.SPACE_MD)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel icon = new JLabel(iconFor(notification));
        icon.setFont(FontManager.bodyFont(Font.PLAIN, 20));
        icon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, AppTheme.SPACE_SM));

        JPanel textStack = new JPanel();
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));
        textStack.setOpaque(false);

        String message = notification.getNotificationMessage() != null && !notification.getNotificationMessage().isBlank()
                ? notification.getNotificationMessage() : "No message content";
        JLabel messageLabel = new JLabel("<html><div style='width:400px;'>" + message + "</div></html>");
        messageLabel.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        messageLabel.setForeground(AppTheme.TEXT_PRIMARY);
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String dateText = notification.getNotificationDate() != null
                ? notification.getNotificationDate().format(DATE_FMT) : "—";
        String typeText = notification.getNotificationType() != null ? " · " + notification.getNotificationType() : "";

        JLabel metaLabel = new JLabel(dateText + typeText);
        metaLabel.setFont(FontManager.bodyFont(Font.PLAIN, 11));
        metaLabel.setForeground(AppTheme.TEXT_MUTED);
        metaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        metaLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        textStack.add(messageLabel);
        textStack.add(metaLabel);

        card.add(icon, BorderLayout.WEST);
        card.add(textStack, BorderLayout.CENTER);
        return card;
    }


    private String iconFor(Notification notification) {
        if (notification.getAppointment() != null) return "\uD83D\uDCC5"; // 📅
        if (notification.getTicket() != null) return "\uD83C\uDFAB";      // 🎫
        return "\uD83D\uDD14";                                            // 🔔
    }

    private JComponent emptyState() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_XL, 0, 0, 0));

        JLabel label = new JLabel("No notifications yet.");
        label.setFont(FontManager.bodyFont(Font.PLAIN, 14));
        label.setForeground(AppTheme.TEXT_MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        return panel;
    }
}