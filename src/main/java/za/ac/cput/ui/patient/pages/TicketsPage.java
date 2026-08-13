package za.ac.cput.ui.patient.pages;

import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.api.BaseApiClient;
import za.ac.cput.model.domain.PatientTicket;
import za.ac.cput.model.domain.TicketStatus;
import za.ac.cput.session.SessionManager;
import za.ac.cput.ui.patient.components.StatusBadge;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/**
 * Read-only ticket list for patients — they can see what's happening with
 * their consultation tickets, but only doctors/staff progress the status
 * (via PatientTicketApiClient.progressStatus), so there's no action here.
 */
public class TicketsPage extends JPanel {

    private JPanel listContainer;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a");

    public TicketsPage() {
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

        JLabel title = new JLabel("Tickets");
        title.setFont(FontManager.headlineFont(Font.BOLD, 26));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Track the status of your consultation tickets.");
        subtitle.setFont(FontManager.bodyFont(Font.PLAIN, 14));
        subtitle.setForeground(AppTheme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_XS, 0, 0, 0));

        panel.add(title);
        panel.add(subtitle);
        return panel;
    }

    // ── Data loading ─────────────────────────────────────────────

    private void loadData() {
        int patientId = SessionManager.getInstance().getUserId();
        BaseApiClient.ApiResult<List<PatientTicket>> result =
                ApiClientProvider.getInstance().patientTickets().findByPatientUserId(patientId);

        List<PatientTicket> tickets = result.isSuccess() ? result.getData() : List.of();
        renderList(tickets);
    }

    private void renderList(List<PatientTicket> tickets) {
        listContainer.removeAll();

        if (tickets.isEmpty()) {
            listContainer.add(emptyState());
            listContainer.revalidate();
            listContainer.repaint();
            return;
        }

        // Most recently created first, so the ticket you're most likely
        // checking on (a recent one) is at the top without scrolling.
        List<PatientTicket> sorted = tickets.stream()
                .sorted(Comparator.comparing(
                        (PatientTicket t) -> t.getTicketCreatedDate() != null ? t.getTicketCreatedDate() : java.time.LocalDateTime.MIN
                ).reversed())
                .toList();

        for (PatientTicket ticket : sorted) {
            listContainer.add(ticketCard(ticket));
            listContainer.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
        }

        listContainer.revalidate();
        listContainer.repaint();
    }

    private JComponent ticketCard(PatientTicket ticket) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppTheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(AppTheme.SPACE_MD, AppTheme.SPACE_MD, AppTheme.SPACE_MD, AppTheme.SPACE_MD)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Top row: description + status badge ──
        JPanel topRow = new JPanel(new BorderLayout(AppTheme.SPACE_MD, 0));
        topRow.setOpaque(false);
        topRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        String description = ticket.getTicketDescription() != null && !ticket.getTicketDescription().isBlank()
                ? ticket.getTicketDescription() : "No description";
        JLabel descLabel = new JLabel("<html><div style='width:320px;'>" + description + "</div></html>");
        descLabel.setFont(FontManager.bodyFont(Font.BOLD, 14));
        descLabel.setForeground(AppTheme.TEXT_PRIMARY);

        topRow.add(descLabel, BorderLayout.CENTER);
        topRow.add(new StatusBadge(ticket.getCurrentStatus()), BorderLayout.EAST);

        // ── Created date + linked doctor (if any) ──
        String createdText = ticket.getTicketCreatedDate() != null
                ? "Opened " + ticket.getTicketCreatedDate().format(DATE_FMT) : "Opened —";
        String doctorText = ticket.getAppointment() != null
                && ticket.getAppointment().getDoctor() != null
                && ticket.getAppointment().getDoctor().getName() != null
                ? " · Dr. " + ticket.getAppointment().getDoctor().getName().getFullName()
                : "";

        JLabel metaLabel = new JLabel(createdText + doctorText);
        metaLabel.setFont(FontManager.bodyFont(Font.PLAIN, 12));
        metaLabel.setForeground(AppTheme.TEXT_MUTED);
        metaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        metaLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        card.add(topRow);
        card.add(metaLabel);

        // ── Status history, if the backend included any ──
        if (ticket.getStatusHistory() != null && !ticket.getStatusHistory().isEmpty()) {
            card.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
            card.add(statusHistoryRow(ticket.getStatusHistory()));
        }

        return card;
    }

    private JComponent statusHistoryRow(List<TicketStatus> history) {
        // Oldest first, so it reads top-to-bottom as a timeline.
        List<TicketStatus> sorted = history.stream()
                .sorted(Comparator.comparing(
                        (TicketStatus s) -> s.getStatusDate() != null ? s.getStatusDate() : java.time.LocalDateTime.MIN
                ))
                .toList();

        StringBuilder trail = new StringBuilder();
        for (int i = 0; i < sorted.size(); i++) {
            TicketStatus s = sorted.get(i);
            trail.append(s.getStatusType() != null ? s.getStatusType() : "—");
            if (i < sorted.size() - 1) trail.append("  →  ");
        }

        JLabel label = new JLabel(trail.toString());
        label.setFont(FontManager.bodyFont(Font.PLAIN, 11));
        label.setForeground(AppTheme.TEXT_MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.DIVIDER),
                BorderFactory.createEmptyBorder(AppTheme.SPACE_SM, 0, 0, 0)
        ));
        return label;
    }

    private JComponent emptyState() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_XL, 0, 0, 0));

        JLabel label = new JLabel("You don't have any tickets yet. These are created once a doctor reviews a confirmed appointment.");
        label.setFont(FontManager.bodyFont(Font.PLAIN, 14));
        label.setForeground(AppTheme.TEXT_MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        return panel;
    }
}