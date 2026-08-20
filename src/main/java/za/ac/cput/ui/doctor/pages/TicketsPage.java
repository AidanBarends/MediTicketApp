package za.ac.cput.ui.doctor.pages;

import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.api.BaseApiClient;
import za.ac.cput.model.domain.PatientTicket;
import za.ac.cput.session.SessionManager;
import za.ac.cput.ui.doctor.components.CompleteConsultationDialog;
import za.ac.cput.ui.doctor.components.TicketDetailsDialog;
import za.ac.cput.ui.clinicstaff.components.SummaryCard;
import za.ac.cput.ui.theme.AppDialog;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Doctor's core workflow page. No findByDoctor endpoint exists on
 * PatientTicketApiClient, so tickets are loaded via getAll() and filtered
 * client-side to those whose appointment.doctor.userId matches the
 * logged-in doctor — same "load once, filter locally" pattern used
 * elsewhere (Reports page, Tickets/Payments cross-referencing).
 *
 * OPEN → "Start Consultation" (single click, IN_PROGRESS).
 * IN_PROGRESS → "Complete Consultation" (requires notes, RESOLVED —
 * triggers the backend's auto-complete-appointment side effect).
 * RESOLVED → "View" only, read-only from here on.
 * CLOSED tickets are excluded entirely — not part of the doctor's
 * active working queue.
 */
public class TicketsPage extends JPanel {

    private SummaryCard openCard, inProgressCard, resolvedCard;
    private DefaultTableModel tableModel;
    private JTable ticketsTable;

    private List<PatientTicket> myTickets = List.of();
    private String activeFilter = "ALL";
    private JPanel filterBarContainer;

    public TicketsPage() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BACKGROUND);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_LG, AppTheme.SPACE_LG, AppTheme.SPACE_LG, AppTheme.SPACE_LG));

        content.add(buildHeader());
        content.add(Box.createVerticalStrut(AppTheme.SPACE_LG));
        content.add(buildSummaryCards());
        content.add(Box.createVerticalStrut(AppTheme.SPACE_LG));

        filterBarContainer = new JPanel(new BorderLayout());
        filterBarContainer.setOpaque(false);
        filterBarContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        filterBarContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        filterBarContainer.add(buildFilterBar(), BorderLayout.WEST);
        content.add(filterBarContainer);

        content.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
        content.add(buildTable());

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

        JLabel subtitle = new JLabel("Your assigned consultations.");
        subtitle.setFont(FontManager.bodyFont(Font.PLAIN, 14));
        subtitle.setForeground(AppTheme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_XS, 0, 0, 0));

        panel.add(title);
        panel.add(subtitle);
        return panel;
    }

    private JComponent buildSummaryCards() {
        JPanel grid = new JPanel(new GridLayout(1, 3, AppTheme.SPACE_MD, 0));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        openCard = new SummaryCard("Open", "—", AppTheme.PRIMARY);
        inProgressCard = new SummaryCard("In Progress", "—", AppTheme.STATUS_INFO);
        resolvedCard = new SummaryCard("Resolved", "—", AppTheme.STATUS_SUCCESS);

        grid.add(openCard);
        grid.add(inProgressCard);
        grid.add(resolvedCard);
        return grid;
    }

    private JComponent buildFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, AppTheme.SPACE_SM, 0));
        bar.setOpaque(false);

        String[][] filters = {
                {"ALL", "All"}, {"OPEN", "Open"}, {"IN_PROGRESS", "In Progress"}, {"RESOLVED", "Resolved"}
        };

        for (String[] f : filters) {
            JButton btn = new JButton(f[1]);
            btn.setFont(FontManager.bodyFont(Font.BOLD, 12));
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setBackground(f[0].equals(activeFilter) ? AppTheme.PRIMARY : AppTheme.SURFACE);
            btn.setForeground(f[0].equals(activeFilter) ? AppTheme.TEXT_ON_PRIMARY : AppTheme.TEXT_PRIMARY);
            btn.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER, 1, true));
            btn.addActionListener(e -> {
                activeFilter = f[0];
                renderTable();
                filterBarContainer.removeAll();
                filterBarContainer.add(buildFilterBar(), BorderLayout.WEST);
                filterBarContainer.revalidate();
                filterBarContainer.repaint();
            });
            bar.add(btn);
        }
        return bar;
    }

    private JComponent buildTable() {
        String[] columns = {"Ticket", "Patient", "Appointment Date", "Status", "Action"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return col == 4; }
        };
        ticketsTable = new JTable(tableModel);
        ticketsTable.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        ticketsTable.setRowHeight(40);
        ticketsTable.getTableHeader().setFont(FontManager.bodyFont(Font.BOLD, 12));
        ticketsTable.setShowGrid(false);
        ticketsTable.setIntercellSpacing(new Dimension(0, 0));
        ticketsTable.getColumnModel().getColumn(4).setCellRenderer(new ActionCellRenderer());
        ticketsTable.getColumnModel().getColumn(4).setCellEditor(new ActionCellEditor());

        JScrollPane scroll = new JScrollPane(ticketsTable);
        scroll.setPreferredSize(new Dimension(0, 380));
        scroll.setBorder(BorderFactory.createLineBorder(AppTheme.DIVIDER));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        return scroll;
    }

    // ── Data loading ──────────────────────────────────────────────

    private void loadData() {
        int doctorId = SessionManager.getInstance().getUserId();

        BaseApiClient.ApiResult<List<PatientTicket>> result = ApiClientProvider.getInstance().patientTickets().getAll();
        List<PatientTicket> all = result.isSuccess() ? result.getData() : List.of();

        myTickets = all.stream()
                .filter(t -> t.getAppointment() != null
                        && t.getAppointment().getDoctor() != null
                        && t.getAppointment().getDoctor().getUserId() == doctorId)
                .filter(t -> !"CLOSED".equals(t.getCurrentStatus())) // not part of the active working queue
                .collect(Collectors.toList());

        updateSummaryCards();
        renderTable();
    }

    private void updateSummaryCards() {
        openCard.setValue(String.valueOf(countByStatus("OPEN")));
        inProgressCard.setValue(String.valueOf(countByStatus("IN_PROGRESS")));
        resolvedCard.setValue(String.valueOf(countByStatus("RESOLVED")));
    }

    private long countByStatus(String status) {
        return myTickets.stream().filter(t -> status.equals(t.getCurrentStatus())).count();
    }

    private void renderTable() {
        tableModel.setRowCount(0);

        List<PatientTicket> filtered = "ALL".equals(activeFilter)
                ? myTickets
                : myTickets.stream().filter(t -> activeFilter.equals(t.getCurrentStatus())).collect(Collectors.toList());

        for (PatientTicket ticket : filtered) {
            tableModel.addRow(new Object[]{
                    "TK-" + String.format("%03d", ticket.getTicketId()),
                    patientName(ticket),
                    appointmentDate(ticket),
                    ticket.getCurrentStatus() != null ? ticket.getCurrentStatus().replace("_", " ") : "—",
                    ticket.getTicketId()
            });
        }
    }

    private String patientName(PatientTicket ticket) {
        if (ticket.getPatient() == null || ticket.getPatient().getName() == null) return "—";
        String first = ticket.getPatient().getName().getFirstName();
        String last = ticket.getPatient().getName().getLastName();
        return (first != null ? first : "") + " " + (last != null ? last.charAt(0) + "." : "");
    }

    private String appointmentDate(PatientTicket ticket) {
        if (ticket.getAppointment() == null || ticket.getAppointment().getAppointmentDate() == null) return "—";
        return ticket.getAppointment().getAppointmentDate().toString();
    }

    private PatientTicket findById(int ticketId) {
        return myTickets.stream().filter(t -> t.getTicketId() == ticketId).findFirst().orElse(null);
    }

    // ── Table action column — buttons vary by status ────────────────

    private class ActionCellRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        ActionCellRenderer() { setLayout(new FlowLayout(FlowLayout.LEFT, 4, 4)); }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int col) {
            removeAll();
            setBackground(AppTheme.SURFACE);
            if (row < 0 || row >= tableModel.getRowCount()) return this;

            Object idValue = tableModel.getValueAt(row, 4);
            if (idValue == null) return this;

            PatientTicket ticket = findById((int) idValue);
            addButtonsFor(this, ticket);
            return this;
        }
    }

    private class ActionCellEditor extends AbstractCellEditor implements javax.swing.table.TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
            panel.removeAll();
            panel.setBackground(AppTheme.SURFACE);

            if (row < 0 || row >= tableModel.getRowCount()) return panel;
            Object idValue = tableModel.getValueAt(row, 4);
            if (idValue == null) return panel;

            PatientTicket ticket = findById((int) idValue);
            addButtonsFor(panel, ticket, this::fireEditingStopped);
            return panel;
        }

        @Override
        public Object getCellEditorValue() { return null; }
    }

    private void addButtonsFor(JPanel container, PatientTicket ticket) { addButtonsFor(container, ticket, null); }

    private void addButtonsFor(JPanel container, PatientTicket ticket, Runnable stopEditing) {
        if (ticket == null) return;
        String status = ticket.getCurrentStatus();

        if ("OPEN".equals(status)) {
            JButton start = smallButton("Start Consultation", AppTheme.PRIMARY);
            start.addActionListener(e -> {
                if (stopEditing != null) stopEditing.run();
                SwingUtilities.invokeLater(() -> startConsultation(ticket));
            });
            container.add(start);
        } else if ("IN_PROGRESS".equals(status)) {
            JButton complete = smallButton("Complete Consultation", AppTheme.STATUS_SUCCESS);
            complete.addActionListener(e -> {
                if (stopEditing != null) stopEditing.run();
                SwingUtilities.invokeLater(() -> CompleteConsultationDialog.show(this, ticket, this::loadData));
            });
            container.add(complete);
        } else {
            JButton view = smallButton("View", AppTheme.TEXT_SECONDARY);
            view.addActionListener(e -> {
                if (stopEditing != null) stopEditing.run();
                SwingUtilities.invokeLater(() -> TicketDetailsDialog.show(this, ticket));
            });
            container.add(view);
        }
    }

    private void startConsultation(PatientTicket ticket) {
        BaseApiClient.ApiResult<PatientTicket> result = ApiClientProvider.getInstance()
                .patientTickets().progressStatus(ticket.getTicketId(), "IN_PROGRESS", null);

        if (result.isSuccess()) {
            AppDialog.show(this, "Consultation Started",
                    "The ticket is now in progress.", AppDialog.Type.SUCCESS);
            loadData();
        } else {
            AppDialog.show(this, "Unable to Start",
                    result.getMessage() != null ? result.getMessage() : "Something went wrong.", AppDialog.Type.ERROR);
        }
    }

    private JButton smallButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(FontManager.bodyFont(Font.BOLD, 11));
        button.setForeground(color);
        button.setBackground(AppTheme.SURFACE);
        button.setBorder(BorderFactory.createLineBorder(color, 1, true));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(2, 8, 2, 8));
        return button;
    }
}