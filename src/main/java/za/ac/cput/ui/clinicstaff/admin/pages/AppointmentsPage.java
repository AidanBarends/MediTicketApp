package za.ac.cput.ui.clinicstaff.admin.pages;

import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.api.BaseApiClient;
import za.ac.cput.model.domain.Appointment;
import za.ac.cput.session.SessionManager;
import za.ac.cput.ui.clinicstaff.admin.components.ApproveAppointmentDialog;
import za.ac.cput.ui.clinicstaff.admin.components.CreateTicketDialog;
import za.ac.cput.ui.clinicstaff.admin.components.SummaryCard;
import za.ac.cput.ui.theme.AppDialog;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Shared Appointments page for Admin and Nurse — identical operational
 * access per the appointment workflow doc (review, approve+assign doctor,
 * reject, mark complete). One class, reused verbatim on the Nurse
 * dashboard once it exists.
 *
 * ConfirmationStatus: PENDING, CONFIRMED, REJECTED, COMPLETED, CANCELLED,
 * RESCHEDULED. "Complete" only applies to CONFIRMED rows and needs no
 * extra params, so it's a one-click table action like Tickets' View button.
 * "Approve" needs a doctor assignment, so it opens ApproveAppointmentDialog
 * instead of acting inline.
 */
public class AppointmentsPage extends JPanel {

    private SummaryCard pendingCard, confirmedCard, completedCard, cancelledCard;
    private JPanel needsAttentionSection;
    private DefaultTableModel tableModel;
    private JTable appointmentsTable;

    private List<Appointment> allAppointments = List.of();
    private java.util.Set<Integer> appointmentIdsWithTickets = new java.util.HashSet<>();
    private String activeFilter = "ALL";
    private JPanel filterBarContainer;

    public AppointmentsPage() {
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
        needsAttentionSection = emptySection();
        content.add(needsAttentionSection);
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

        JLabel title = new JLabel("Appointments");
        title.setFont(FontManager.headlineFont(Font.BOLD, 26));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Review, approve, and manage clinic appointments.");
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

        pendingCard = new SummaryCard("Pending", "—", AppTheme.STATUS_WARNING);
        confirmedCard = new SummaryCard("Confirmed", "—", AppTheme.PRIMARY);
        completedCard = new SummaryCard("Completed", "—", AppTheme.STATUS_SUCCESS);
        cancelledCard = new SummaryCard("Cancelled", "—", AppTheme.STATUS_DANGER);

        grid.add(pendingCard);
        grid.add(confirmedCard);
        grid.add(completedCard);
        grid.add(cancelledCard);
        return grid;
    }

    private JPanel emptySection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        return section;
    }

    private JComponent buildFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, AppTheme.SPACE_SM, 0));
        bar.setOpaque(false);

        String[][] filters = {
                {"ALL", "All"}, {"PENDING", "Pending"}, {"CONFIRMED", "Confirmed"},
                {"COMPLETED", "Completed"}, {"CANCELLED", "Cancelled"}, {"RESCHEDULED", "Rescheduled"}
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
        String[] columns = {"Patient", "Doctor", "Date", "Time", "Status", "Reason", "Action"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return col == 6; }
        };
        appointmentsTable = new JTable(tableModel);
        appointmentsTable.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        appointmentsTable.setRowHeight(40);
        appointmentsTable.getTableHeader().setFont(FontManager.bodyFont(Font.BOLD, 12));
        appointmentsTable.setShowGrid(false);
        appointmentsTable.setIntercellSpacing(new Dimension(0, 0));
        appointmentsTable.getColumnModel().getColumn(6).setCellRenderer(new ActionCellRenderer());
        appointmentsTable.getColumnModel().getColumn(6).setCellEditor(new ActionCellEditor());

        JScrollPane scroll = new JScrollPane(appointmentsTable);
        scroll.setPreferredSize(new Dimension(0, 360));
        scroll.setBorder(BorderFactory.createLineBorder(AppTheme.DIVIDER));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        return scroll;
    }

    // ── Data loading ──────────────────────────────────────────────

    private void loadData() {
        BaseApiClient.ApiResult<List<Appointment>> result = ApiClientProvider.getInstance().appointments().getAll();
        allAppointments = result.isSuccess() ? result.getData() : List.of();

        BaseApiClient.ApiResult<List<za.ac.cput.model.domain.PatientTicket>> ticketResult =
                ApiClientProvider.getInstance().patientTickets().getAll();
        List<za.ac.cput.model.domain.PatientTicket> tickets = ticketResult.isSuccess() ? ticketResult.getData() : List.of();

        appointmentIdsWithTickets = tickets.stream()
                .filter(t -> t.getAppointment() != null)
                .map(t -> t.getAppointment().getAppointmentId())
                .collect(java.util.stream.Collectors.toSet());

        updateSummaryCards();
        updateNeedsAttention();
        renderTable();
    }

    private void updateSummaryCards() {
        pendingCard.setValue(String.valueOf(countByStatus("PENDING")));
        confirmedCard.setValue(String.valueOf(countByStatus("CONFIRMED")));
        completedCard.setValue(String.valueOf(countByStatus("COMPLETED")));
        cancelledCard.setValue(String.valueOf(countByStatus("CANCELLED")));
    }

    private long countByStatus(String status) {
        return allAppointments.stream().filter(a -> status.equals(a.getConfirmationStatus())).count();
    }

    private void updateNeedsAttention() {
        needsAttentionSection.removeAll();

        long pending = countByStatus("PENDING");
        if (pending == 0) {
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

        needsAttentionSection.add(attentionBanner(
                "\uD83D\uDCC5 " + pending + " appointment" + (pending == 1 ? "" : "s") + " awaiting approval",
                "New booking requests need review.", "PENDING"));

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

        JButton viewButton = new JButton("View Appointments");
        viewButton.setFont(FontManager.bodyFont(Font.BOLD, 12));
        viewButton.setFocusPainted(false);
        viewButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        viewButton.addActionListener(e -> {
            activeFilter = filterOnClick;
            renderTable();
            filterBarContainer.removeAll();
            filterBarContainer.add(buildFilterBar(), BorderLayout.WEST);
            filterBarContainer.revalidate();
            filterBarContainer.repaint();
        });

        banner.add(textStack, BorderLayout.WEST);
        banner.add(viewButton, BorderLayout.EAST);
        return banner;
    }

    private void renderTable() {
        tableModel.setRowCount(0);

        List<Appointment> filtered = "ALL".equals(activeFilter)
                ? allAppointments
                : allAppointments.stream().filter(a -> activeFilter.equals(a.getConfirmationStatus())).collect(Collectors.toList());

        for (Appointment appt : filtered) {
            tableModel.addRow(new Object[]{
                    patientName(appt),
                    doctorName(appt),
                    appt.getAppointmentDate() != null ? appt.getAppointmentDate().toString() : "—",
                    appt.getAppointmentTime() != null ? appt.getAppointmentTime().toString() : "—",
                    appt.getConfirmationStatus() != null ? appt.getConfirmationStatus() : "—",
                    appt.getReason() != null && !appt.getReason().isBlank() ? appt.getReason() : "—",
                    appt.getAppointmentId()
            });
        }
    }

    private String patientName(Appointment appt) {
        if (appt.getPatient() == null || appt.getPatient().getName() == null) return "—";
        String first = appt.getPatient().getName().getFirstName();
        String last = appt.getPatient().getName().getLastName();
        return (first != null ? first : "") + " " + (last != null ? last.charAt(0) + "." : "");
    }

    private String doctorName(Appointment appt) {
        if (appt.getDoctor() == null || appt.getDoctor().getName() == null) return "Unassigned";
        String last = appt.getDoctor().getName().getLastName();
        return "Dr. " + (last != null ? last : "—");
    }

    private Appointment findById(int appointmentId) {
        return allAppointments.stream().filter(a -> a.getAppointmentId() == appointmentId).findFirst().orElse(null);
    }

    // ── Table action column — buttons vary by status ────────────────
    private class ActionCellRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        ActionCellRenderer() { setLayout(new FlowLayout(FlowLayout.LEFT, 4, 4)); }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int col) {
            removeAll();
            setBackground(AppTheme.SURFACE);
            // Guard: row may momentarily be out of range while the model is
            // being swapped out from under an in-progress repaint (e.g. right
            // after an action triggers loadData()). Render nothing rather than
            // crash the EDT.
            if (row < 0 || row >= tableModel.getRowCount()) return this;

            Object idValue = tableModel.getValueAt(row, 6);
            if (idValue == null) return this;

            Appointment appt = findById((int) idValue);
            addButtonsFor(this, appt);
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
            Object idValue = tableModel.getValueAt(row, 6);
            if (idValue == null) return panel;

            Appointment appt = findById((int) idValue);
            addButtonsFor(panel, appt, this::fireEditingStopped);
            return panel;
        }

        @Override
        public Object getCellEditorValue() { return null; }
    }


    private void addButtonsFor(JPanel container, Appointment appt) { addButtonsFor(container, appt, null); }

    private void addButtonsFor(JPanel container, Appointment appt, Runnable stopEditing) {
        if (appt == null) return;
        String status = appt.getConfirmationStatus();

        if ("PENDING".equals(status)) {
            JButton approve = smallButton("Approve", AppTheme.STATUS_SUCCESS);
            approve.addActionListener(e -> {
                if (stopEditing != null) stopEditing.run();
                SwingUtilities.invokeLater(() -> ApproveAppointmentDialog.show(this, appt, this::loadData));
            });
            JButton reject = smallButton("Reject", AppTheme.STATUS_DANGER);
            reject.addActionListener(e -> {
                if (stopEditing != null) stopEditing.run();
                SwingUtilities.invokeLater(() -> rejectAppointment(appt));
            });
            container.add(approve);
            container.add(reject);
        } else if ("CONFIRMED".equals(status)) {
            if (!appointmentIdsWithTickets.contains(appt.getAppointmentId())) {
                JButton createTicket = smallButton("Create Ticket", AppTheme.PRIMARY);
                createTicket.addActionListener(e -> {
                    if (stopEditing != null) stopEditing.run();
                    SwingUtilities.invokeLater(() -> CreateTicketDialog.show(this, appt, this::loadData));
                });
                container.add(createTicket);
            }
            JButton complete = smallButton("Complete", AppTheme.PRIMARY);
            complete.addActionListener(e -> {
                if (stopEditing != null) stopEditing.run();
                SwingUtilities.invokeLater(() -> completeAppointment(appt));
            });
            container.add(complete);
        } else {
            JLabel none = new JLabel("—");
            none.setFont(FontManager.bodyFont(Font.PLAIN, 12));
            none.setForeground(AppTheme.TEXT_MUTED);
            container.add(none);
        }
    }

    private void rejectAppointment(Appointment appt) {
        String reason = JOptionPane.showInputDialog(this, "Reason for rejection (optional):",
                "Reject Appointment", JOptionPane.PLAIN_MESSAGE);

        int staffId = SessionManager.getInstance().getUserId();
        BaseApiClient.ApiResult<Appointment> result = ApiClientProvider.getInstance()
                .appointments().reject(appt.getAppointmentId(), staffId, reason);

        if (result.isSuccess()) {
            AppDialog.show(this, "Appointment Rejected", "The appointment has been rejected.", AppDialog.Type.INFO);
            loadData();
        } else {
            AppDialog.show(this, "Unable to Reject",
                    result.getMessage() != null ? result.getMessage() : "Something went wrong.", AppDialog.Type.ERROR);
        }
    }

    private void completeAppointment(Appointment appt) {
        BaseApiClient.ApiResult<Appointment> result = ApiClientProvider.getInstance()
                .appointments().complete(appt.getAppointmentId());

        if (result.isSuccess()) {
            AppDialog.show(this, "Appointment Completed", "The appointment has been marked complete.", AppDialog.Type.SUCCESS);
            loadData();
        } else {
            AppDialog.show(this, "Unable to Complete",
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