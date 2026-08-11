package za.ac.cput.ui.clinicstaff.admin.pages;

import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.api.BaseApiClient;
import za.ac.cput.model.domain.Payment;
import za.ac.cput.model.domain.PatientTicket;
import za.ac.cput.ui.clinicstaff.admin.components.SummaryCard;
import za.ac.cput.ui.clinicstaff.admin.components.TicketDetailsDialog;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Shared Tickets page for Admin and Nurse — per the appointment workflow
 * doc, both roles have identical operational access here (monitor tickets,
 * generate payment requests). No separate NurseTicketsPage; this class is
 * reused as-is once the Nurse dashboard is built.
 *
 * Status model note: PatientTicket.currentStatus uses the real backend
 * StatusType enum (OPEN, IN_PROGRESS, RESOLVED, CLOSED, ESCALATED), not
 * the doc's informal "Ready for Payment" label — RESOLVED is treated as
 * "ready for payment" here since that's the state right before a Payment
 * row exists.
 */
public class TicketsPage extends JPanel {

    private SummaryCard openCard, inProgressCard, resolvedCard, closedCard;
    private JPanel needsAttentionSection;
    private DefaultTableModel tableModel;
    private JTable ticketsTable;

    private List<PatientTicket> allTickets = List.of();
    private Map<Integer, Payment> paymentsByAppointmentId = new HashMap<>();
    private String activeFilter = "ALL";

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
        needsAttentionSection = buildEmptySection();
        content.add(needsAttentionSection);
        content.add(Box.createVerticalStrut(AppTheme.SPACE_LG));
        content.add(buildFilterBar());
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

        JLabel subtitle = new JLabel("Manage patient consultation tickets and payment progress.");
        subtitle.setFont(FontManager.bodyFont(Font.PLAIN, 14));
        subtitle.setForeground(AppTheme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_XS, 0, 0, 0));

        panel.add(title);
        panel.add(subtitle);
        return panel;
    }

    private JComponent buildSummaryCards() {
        JPanel grid = new JPanel(new GridLayout(1, 4, AppTheme.SPACE_MD, 0));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        openCard = new SummaryCard("Open", "—", AppTheme.PRIMARY);
        inProgressCard = new SummaryCard("In Consultation", "—", AppTheme.STATUS_INFO);
        resolvedCard = new SummaryCard("Ready for Payment", "—", AppTheme.STATUS_WARNING);
        closedCard = new SummaryCard("Closed", "—", AppTheme.STATUS_SUCCESS);

        grid.add(openCard);
        grid.add(inProgressCard);
        grid.add(resolvedCard);
        grid.add(closedCard);
        return grid;
    }

    private JPanel buildEmptySection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        return section;
    }

    private JComponent buildFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, AppTheme.SPACE_SM, 0));
        bar.setOpaque(false);
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[][] filters = {
                {"ALL", "All"}, {"OPEN", "Open"}, {"IN_PROGRESS", "In Consultation"},
                {"RESOLVED", "Ready for Payment"}, {"CLOSED", "Closed"}, {"ESCALATED", "Escalated"}
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
                // Refresh filter bar styling by rebuilding — simplest correct approach
                // given buttons don't currently expose a re-stylable reference map.
                Container parentPanel = bar.getParent();
                if (parentPanel != null) {
                    int idx = java.util.Arrays.asList(parentPanel.getComponents()).indexOf(bar);
                    parentPanel.remove(bar);
                    JComponent rebuilt = (JComponent) buildFilterBar();
                    parentPanel.add(rebuilt, idx);
                    parentPanel.revalidate();
                    parentPanel.repaint();
                }
            });
            bar.add(btn);
        }
        return bar;
    }

    private JComponent buildTable() {
        String[] columns = {"Ticket", "Patient", "Doctor", "Appointment", "Status", "Amount", "Action"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return col == 6; }
        };
        ticketsTable = new JTable(tableModel);
        ticketsTable.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        ticketsTable.setRowHeight(40);
        ticketsTable.getTableHeader().setFont(FontManager.bodyFont(Font.BOLD, 12));
        ticketsTable.setShowGrid(false);
        ticketsTable.setIntercellSpacing(new Dimension(0, 0));
        ticketsTable.getColumnModel().getColumn(6).setCellRenderer(new ActionCellRenderer());
        ticketsTable.getColumnModel().getColumn(6).setCellEditor(new ActionCellEditor());

        JScrollPane scroll = new JScrollPane(ticketsTable);
        scroll.setPreferredSize(new Dimension(0, 360));
        scroll.setBorder(BorderFactory.createLineBorder(AppTheme.DIVIDER));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        return scroll;
    }

    // ── Data loading ──────────────────────────────────────────────
    // Single getAll() call each for tickets and payments; payments are
    // indexed by appointment ID client-side since no findByAppointmentId
    // endpoint exists on the backend yet.

    private void loadData() {
        BaseApiClient.ApiResult<List<PatientTicket>> ticketResult =
                ApiClientProvider.getInstance().patientTickets().getAll();
        allTickets = ticketResult.isSuccess() ? ticketResult.getData() : List.of();

        BaseApiClient.ApiResult<List<Payment>> paymentResult =
                ApiClientProvider.getInstance().payments().getAll();
        List<Payment> payments = paymentResult.isSuccess() ? paymentResult.getData() : List.of();

        paymentsByAppointmentId = payments.stream()
                .filter(p -> p.getAppointment() != null)
                .collect(Collectors.toMap(p -> p.getAppointment().getAppointmentId(), p -> p, (a, b) -> a));

        updateSummaryCards();
        updateNeedsAttention();
        renderTable();
    }

    private void updateSummaryCards() {
        long open = countByStatus("OPEN");
        long inProgress = countByStatus("IN_PROGRESS");
        long resolved = countByStatus("RESOLVED");
        long closed = countByStatus("CLOSED");

        openCard.setValue(String.valueOf(open));
        inProgressCard.setValue(String.valueOf(inProgress));
        resolvedCard.setValue(String.valueOf(resolved));
        closedCard.setValue(String.valueOf(closed));
    }

    private long countByStatus(String status) {
        return allTickets.stream().filter(t -> status.equals(t.getCurrentStatus())).count();
    }

    private void updateNeedsAttention() {
        needsAttentionSection.removeAll();

        long readyForPayment = countByStatus("RESOLVED");
        long escalated = countByStatus("ESCALATED");

        if (readyForPayment == 0 && escalated == 0) {
            needsAttentionSection.revalidate();
            needsAttentionSection.repaint();
            return;
        }

        JLabel title = new JLabel("Needs Attention");
        title.setFont(FontManager.bodyFont(Font.BOLD, 15));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, AppTheme.SPACE_SM, 0));
        needsAttentionSection.add(title);

        if (readyForPayment > 0) {
            needsAttentionSection.add(attentionBanner(
                    "\uD83D\uDCB3 " + readyForPayment + " ticket" + (readyForPayment == 1 ? "" : "s") + " ready for payment",
                    "Patients are waiting for payment requests.", "RESOLVED"));
            needsAttentionSection.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
        }
        if (escalated > 0) {
            needsAttentionSection.add(attentionBanner(
                    "\uD83D\uDEA9 " + escalated + " ticket" + (escalated == 1 ? "" : "s") + " escalated",
                    "These tickets need immediate review.", "ESCALATED"));
        }

        needsAttentionSection.revalidate();
        needsAttentionSection.repaint();
    }

    private JComponent attentionBanner(String title, String subtitle, String filterOnClick) {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(AppTheme.SURFACE_ALT);
        banner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(AppTheme.SPACE_SM, AppTheme.SPACE_MD, AppTheme.SPACE_SM, AppTheme.SPACE_MD)
        ));
        banner.setAlignmentX(Component.LEFT_ALIGNMENT);
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel textStack = new JPanel();
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));
        textStack.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FontManager.bodyFont(Font.BOLD, 13));
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(FontManager.bodyFont(Font.PLAIN, 12));
        subtitleLabel.setForeground(AppTheme.TEXT_SECONDARY);

        textStack.add(titleLabel);
        textStack.add(subtitleLabel);

        JButton viewButton = new JButton("View Tickets");
        viewButton.setFont(FontManager.bodyFont(Font.BOLD, 12));
        viewButton.setFocusPainted(false);
        viewButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        viewButton.addActionListener(e -> {
            activeFilter = filterOnClick;
            renderTable();
        });

        banner.add(textStack, BorderLayout.WEST);
        banner.add(viewButton, BorderLayout.EAST);
        return banner;
    }

    private void renderTable() {
        tableModel.setRowCount(0);

        List<PatientTicket> filtered = "ALL".equals(activeFilter)
                ? allTickets
                : allTickets.stream().filter(t -> activeFilter.equals(t.getCurrentStatus())).collect(Collectors.toList());

        for (PatientTicket ticket : filtered) {
            Payment payment = ticket.getAppointment() != null
                    ? paymentsByAppointmentId.get(ticket.getAppointment().getAppointmentId())
                    : null;

            tableModel.addRow(new Object[]{
                    "TK-" + String.format("%03d", ticket.getTicketId()),
                    patientName(ticket),
                    doctorName(ticket),
                    appointmentDate(ticket),
                    ticket.getCurrentStatus() != null ? ticket.getCurrentStatus().replace("_", " ") : "—",
                    payment != null ? "R" + payment.getPaymentAmount() : "—",
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

    private String doctorName(PatientTicket ticket) {
        if (ticket.getAppointment() == null || ticket.getAppointment().getDoctor() == null
                || ticket.getAppointment().getDoctor().getName() == null) return "—";
        String last = ticket.getAppointment().getDoctor().getName().getLastName();
        return "Dr. " + (last != null ? last : "—");
    }

    private String appointmentDate(PatientTicket ticket) {
        if (ticket.getAppointment() == null || ticket.getAppointment().getAppointmentDate() == null) return "—";
        return ticket.getAppointment().getAppointmentDate().toString();
    }

    // ── Table action column ──────────────────────────────────────

    private PatientTicket findTicketById(int ticketId) {
        return allTickets.stream().filter(t -> t.getTicketId() == ticketId).findFirst().orElse(null);
    }

    private class ActionCellRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        ActionCellRenderer() { setLayout(new FlowLayout(FlowLayout.LEFT, 4, 4)); }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int col) {
            removeAll();
            setBackground(AppTheme.SURFACE);
            add(smallButton("View"));
            return this;
        }
    }

    private class ActionCellEditor extends AbstractCellEditor implements javax.swing.table.TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        private int currentTicketId;

        ActionCellEditor() {
            JButton viewBtn = smallButton("View");
            viewBtn.addActionListener(e -> {
                fireEditingStopped();
                PatientTicket ticket = findTicketById(currentTicketId);
                if (ticket != null) {
                    Payment payment = ticket.getAppointment() != null
                            ? paymentsByAppointmentId.get(ticket.getAppointment().getAppointmentId())
                            : null;
                    TicketDetailsDialog.show(TicketsPage.this, ticket, payment, TicketsPage.this::loadData);
                }
            });
            panel.add(viewBtn);
            panel.setBackground(AppTheme.SURFACE);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
            currentTicketId = (int) tableModel.getValueAt(row, 6);
            return panel;
        }

        @Override
        public Object getCellEditorValue() { return currentTicketId; }
    }

    private JButton smallButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FontManager.bodyFont(Font.BOLD, 11));
        button.setForeground(AppTheme.PRIMARY);
        button.setBackground(AppTheme.SURFACE);
        button.setBorder(BorderFactory.createLineBorder(AppTheme.PRIMARY, 1, true));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(2, 8, 2, 8));
        return button;
    }
}