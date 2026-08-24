package za.ac.cput.ui.clinicstaff.admin.pages;

import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.api.BaseApiClient;
import za.ac.cput.model.domain.Patient;
import za.ac.cput.ui.clinicstaff.components.PatientDetailsDialog;
import za.ac.cput.ui.clinicstaff.components.SummaryCard;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Read-only patient directory for admins — the counterpart to StaffPage,
 * but for the patient user base rather than clinic staff. Patient lists
 * can get much longer than the staff roster, so this adds a live search
 * box (name/email) on top of the same status-filter-bar pattern used
 * elsewhere. No create/edit/delete here; that's out of scope for admin
 * and there's no safe update-patient path wired from this side yet,
 * same reasoning as StaffPage.
 */
public class PatientsPage extends JPanel {

    private SummaryCard totalCard, activeCard, newThisMonthCard;
    private DefaultTableModel tableModel;
    private JTable patientsTable;
    private JPanel filterBarContainer;
    private JTextField searchField;

    private List<Patient> allPatients = List.of();
    private String activeFilter = "ALL";
    private String searchText = "";

    public PatientsPage() {
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

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.setAlignmentX(Component.LEFT_ALIGNMENT);
        toolbar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        filterBarContainer = new JPanel(new BorderLayout());
        filterBarContainer.setOpaque(false);
        filterBarContainer.add(buildFilterBar(), BorderLayout.WEST);
        toolbar.add(filterBarContainer, BorderLayout.WEST);
        toolbar.add(buildSearchField(), BorderLayout.EAST);
        content.add(toolbar);

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

        JLabel title = new JLabel("Patients");
        title.setFont(FontManager.headlineFont(Font.BOLD, 26));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("View everyone registered as a patient on MediTicket.");
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

        totalCard = new SummaryCard("Total Patients", "—", AppTheme.PRIMARY);
        activeCard = new SummaryCard("Active", "—", AppTheme.STATUS_SUCCESS);
        newThisMonthCard = new SummaryCard("New This Month", "—", AppTheme.STATUS_INFO);

        grid.add(totalCard);
        grid.add(activeCard);
        grid.add(newThisMonthCard);
        return grid;
    }

    private JComponent buildFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, AppTheme.SPACE_SM, 0));
        bar.setOpaque(false);

        String[][] filters = {
                {"ALL", "All"}, {"ACTIVE", "Active"}, {"INACTIVE", "Inactive"}, {"SUSPENDED", "Suspended"}
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

    private JComponent buildSearchField() {
        searchField = new JTextField(20);
        searchField.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        searchField.putClientProperty("JTextField.placeholderText", "Search by name or email...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { onSearchChanged(); }
            @Override
            public void removeUpdate(DocumentEvent e) { onSearchChanged(); }
            @Override
            public void changedUpdate(DocumentEvent e) { onSearchChanged(); }
        });
        return searchField;
    }

    private void onSearchChanged() {
        searchText = searchField.getText().trim().toLowerCase();
        renderTable();
    }

    private JComponent buildTable() {
        String[] columns = {"Name", "Email", "Phone", "Date Registered", "Status", "Action"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return col == 5; }
        };
        patientsTable = new JTable(tableModel);
        patientsTable.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        patientsTable.setRowHeight(40);
        patientsTable.getTableHeader().setFont(FontManager.bodyFont(Font.BOLD, 12));
        patientsTable.setShowGrid(false);
        patientsTable.setIntercellSpacing(new Dimension(0, 0));
        patientsTable.getColumnModel().getColumn(5).setCellRenderer(new ActionCellRenderer());
        patientsTable.getColumnModel().getColumn(5).setCellEditor(new ActionCellEditor());

        JScrollPane scroll = new JScrollPane(patientsTable);
        scroll.setPreferredSize(new Dimension(0, 400));
        scroll.setBorder(BorderFactory.createLineBorder(AppTheme.DIVIDER));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        return scroll;
    }

    // ── Data loading ──────────────────────────────────────────────

    private void loadData() {
        BaseApiClient.ApiResult<List<Patient>> result = ApiClientProvider.getInstance().patients().getAll();
        allPatients = result.isSuccess() ? result.getData() : List.of();
        updateSummaryCards();
        renderTable();
    }

    private void updateSummaryCards() {
        long activeCount = allPatients.stream().filter(p -> "ACTIVE".equals(p.getAccountStatus())).count();

        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        long newThisMonth = allPatients.stream()
                .filter(p -> p.getDateRegistered() != null && !p.getDateRegistered().isBefore(startOfMonth))
                .count();

        totalCard.setValue(String.valueOf(allPatients.size()));
        activeCard.setValue(String.valueOf(activeCount));
        newThisMonthCard.setValue(String.valueOf(newThisMonth));
    }

    private List<Patient> currentFilteredRows() {
        return allPatients.stream()
                .filter(p -> "ALL".equals(activeFilter) || activeFilter.equals(p.getAccountStatus()))
                .filter(this::matchesSearch)
                .collect(Collectors.toList());
    }

    private boolean matchesSearch(Patient p) {
        if (searchText.isEmpty()) return true;
        String name = fullName(p).toLowerCase();
        String email = p.getEmail() != null ? p.getEmail().toLowerCase() : "";
        return name.contains(searchText) || email.contains(searchText);
    }

    private void renderTable() {
        tableModel.setRowCount(0);
        List<Patient> filtered = currentFilteredRows();

        for (int i = 0; i < filtered.size(); i++) {
            Patient p = filtered.get(i);
            tableModel.addRow(new Object[]{
                    fullName(p).isBlank() ? "—" : fullName(p),
                    p.getEmail() != null ? p.getEmail() : "—",
                    p.getCellPhone() != null ? p.getCellPhone() : "—",
                    p.getDateRegistered() != null ? p.getDateRegistered().toString() : "—",
                    p.getAccountStatus() != null ? p.getAccountStatus() : "—",
                    i // row index into the filtered list, used by the action column
            });
        }
    }

    private String fullName(Patient p) {
        if (p.getName() == null) return "";
        String first = p.getName().getFirstName();
        String last = p.getName().getLastName();
        return (first != null ? first : "") + " " + (last != null ? last : "");
    }

    // ── Table action column ──────────────────────────────────────

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
        private int currentIndex;

        ActionCellEditor() {
            JButton viewBtn = smallButton("View");
            viewBtn.addActionListener(e -> {
                fireEditingStopped();
                List<Patient> filtered = currentFilteredRows();
                if (currentIndex < 0 || currentIndex >= filtered.size()) return;
                Patient patient = filtered.get(currentIndex);
                SwingUtilities.invokeLater(() -> PatientDetailsDialog.show(PatientsPage.this, patient));
            });
            panel.add(viewBtn);
            panel.setBackground(AppTheme.SURFACE);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
            if (row < 0 || row >= tableModel.getRowCount()) return panel;
            Object idxValue = tableModel.getValueAt(row, 5);
            currentIndex = idxValue != null ? (int) idxValue : -1;
            return panel;
        }

        @Override
        public Object getCellEditorValue() { return currentIndex; }
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