package za.ac.cput.ui.clinicstaff.admin.pages;

import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.api.BaseApiClient;
import za.ac.cput.model.auth.EmployeeAccessRequest;
import za.ac.cput.session.SessionManager;
import za.ac.cput.ui.clinicstaff.admin.components.InviteEmployeeDialog;
import za.ac.cput.ui.theme.AppDialog;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class EmployeeOnboardingPage extends JPanel {

    private DefaultTableModel tableModel;
    private JTable requestsTable;
    private List<EmployeeAccessRequest> currentRequests = List.of();

    public EmployeeOnboardingPage() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BACKGROUND);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_LG, AppTheme.SPACE_LG, AppTheme.SPACE_LG, AppTheme.SPACE_LG));

        content.add(buildHeader());
        content.add(Box.createVerticalStrut(AppTheme.SPACE_LG));
        content.add(buildInviteCards());
        content.add(Box.createVerticalStrut(AppTheme.SPACE_LG));
        content.add(buildPendingRequestsSection());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        loadPendingRequests();
    }

    private JComponent buildHeader() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Employee Onboarding");
        title.setFont(FontManager.headlineFont(Font.BOLD, 26));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Invite new employees to join the MediTicket clinic.");
        subtitle.setFont(FontManager.bodyFont(Font.PLAIN, 14));
        subtitle.setForeground(AppTheme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_XS, 0, 0, 0));

        panel.add(title);
        panel.add(subtitle);
        return panel;
    }

    private JComponent buildInviteCards() {
        JPanel grid = new JPanel(new GridLayout(1, 2, AppTheme.SPACE_LG, 0));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        grid.add(buildInviteCard(
                "Invite Doctor",
                "Send an invitation to a new doctor to join the clinic.",
                () -> InviteEmployeeDialog.show(this, "DOCTOR", null, this::loadPendingRequests)
        ));
        grid.add(buildInviteCard(
                "Invite Nurse",
                "Send an invitation to a new clinic nurse to join the clinic.",
                () -> InviteEmployeeDialog.show(this, "CLINIC_STAFF", "NURSE", this::loadPendingRequests)
        ));

        return grid;
    }

    private JPanel buildInviteCard(String title, String description, Runnable onInvite) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppTheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(AppTheme.SPACE_MD, AppTheme.SPACE_MD, AppTheme.SPACE_MD, AppTheme.SPACE_MD)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FontManager.bodyFont(Font.BOLD, 16));
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descLabel = new JLabel("<html>" + description + "</html>");
        descLabel.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        descLabel.setForeground(AppTheme.TEXT_SECONDARY);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        descLabel.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_XS, 0, AppTheme.SPACE_MD, 0));

        JButton inviteButton = new JButton(title);
        inviteButton.setFont(FontManager.bodyFont(Font.BOLD, 13));
        inviteButton.setForeground(AppTheme.TEXT_ON_PRIMARY);
        inviteButton.setBackground(AppTheme.PRIMARY);
        inviteButton.setFocusPainted(false);
        inviteButton.setBorderPainted(false);
        inviteButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        inviteButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        inviteButton.addActionListener(e -> onInvite.run());

        card.add(titleLabel);
        card.add(descLabel);
        card.add(inviteButton);
        return card;
    }

    private JComponent buildPendingRequestsSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(AppTheme.SURFACE);
        section.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(AppTheme.SPACE_MD, AppTheme.SPACE_MD, AppTheme.SPACE_MD, AppTheme.SPACE_MD)
        ));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Pending Access Requests");
        title.setFont(FontManager.bodyFont(Font.BOLD, 16));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel note = new JLabel("Employees who self-requested access. Approving sends them an invitation email.");
        note.setFont(FontManager.bodyFont(Font.PLAIN, 12));
        note.setForeground(AppTheme.TEXT_MUTED);
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        note.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_XS, 0, AppTheme.SPACE_SM, 0));

        String[] columns = {"Email", "Type", "Role", "Requested", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return col == 4; }
        };
        requestsTable = new JTable(tableModel);
        requestsTable.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        requestsTable.setRowHeight(40);
        requestsTable.getTableHeader().setFont(FontManager.bodyFont(Font.BOLD, 12));
        requestsTable.setShowGrid(false);
        requestsTable.setIntercellSpacing(new Dimension(0, 0));
        requestsTable.getColumnModel().getColumn(4).setCellRenderer(new ActionCellRenderer());
        requestsTable.getColumnModel().getColumn(4).setCellEditor(new ActionCellEditor());

        JScrollPane tableScroll = new JScrollPane(requestsTable);
        tableScroll.setPreferredSize(new Dimension(0, 260));
        tableScroll.setBorder(BorderFactory.createLineBorder(AppTheme.DIVIDER));
        tableScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        section.add(title);
        section.add(note);
        section.add(tableScroll);
        return section;
    }

    private void loadPendingRequests() {
        BaseApiClient.ApiResult<List<EmployeeAccessRequest>> result =
                ApiClientProvider.getInstance().auth().getAccessRequests("PENDING");



        tableModel.setRowCount(0);
        currentRequests = result.isSuccess() ? result.getData() : List.of();

        for (EmployeeAccessRequest r : currentRequests) {
            String role = r.getRequestedStaffRole() != null ? r.getRequestedStaffRole() : "—";
            tableModel.addRow(new Object[]{
                    r.getEmail(), r.getRequestedUserType(), role,
                    r.getRequestDate() != null ? r.getRequestDate().toLocalDate().toString() : "—",
                    r.getRequestId() // used by the action editor to look up the row
            });
        }
    }

    private void approve(int requestId) {
        int adminUserId = SessionManager.getInstance().getUserId();
        BaseApiClient.ApiResult<String> result = ApiClientProvider.getInstance().auth().approveAccessRequest(requestId);
        if (result.isSuccess()) {
            AppDialog.show(this, "Request Approved",
                    "An invitation email has been sent to the employee.", AppDialog.Type.SUCCESS);
            loadPendingRequests();
        } else {
            AppDialog.show(this, "Unable to Approve",
                    result.getMessage() != null ? result.getMessage() : "Something went wrong.", AppDialog.Type.ERROR);
        }
    }

    private void reject(int requestId) {
        String notes = JOptionPane.showInputDialog(this, "Reason for rejection (optional):", "Reject Request", JOptionPane.PLAIN_MESSAGE);
        BaseApiClient.ApiResult<String> result = ApiClientProvider.getInstance().auth().rejectAccessRequest(requestId, notes);
        if (result.isSuccess()) {
            AppDialog.show(this, "Request Rejected", "The request has been rejected.", AppDialog.Type.INFO);
            loadPendingRequests();
        } else {
            AppDialog.show(this, "Unable to Reject",
                    result.getMessage() != null ? result.getMessage() : "Something went wrong.", AppDialog.Type.ERROR);
        }
    }

    // ── Table action column: Approve / Reject buttons per row ─────

    private class ActionCellRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        ActionCellRenderer() { setLayout(new FlowLayout(FlowLayout.LEFT, 6, 4)); }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int col) {
            removeAll();
            setBackground(AppTheme.SURFACE);
            add(smallButton("Approve", AppTheme.STATUS_SUCCESS));
            add(smallButton("Reject", AppTheme.STATUS_DANGER));
            return this;
        }
    }

    private class ActionCellEditor extends javax.swing.AbstractCellEditor implements javax.swing.table.TableCellEditor {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        private int currentRequestId;

        ActionCellEditor() {
            JButton approveBtn = smallButton("Approve", AppTheme.STATUS_SUCCESS);
            JButton rejectBtn = smallButton("Reject", AppTheme.STATUS_DANGER);
            approveBtn.addActionListener(e -> { fireEditingStopped(); approve(currentRequestId); });
            rejectBtn.addActionListener(e -> { fireEditingStopped(); reject(currentRequestId); });
            panel.add(approveBtn);
            panel.add(rejectBtn);
            panel.setBackground(AppTheme.SURFACE);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
            currentRequestId = (int) tableModel.getValueAt(row, 4);
            return panel;
        }

        @Override
        public Object getCellEditorValue() { return currentRequestId; }
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