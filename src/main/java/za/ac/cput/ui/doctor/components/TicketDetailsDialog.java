package za.ac.cput.ui.doctor.components;

import za.ac.cput.model.domain.PatientTicket;
import za.ac.cput.model.domain.TicketStatus;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/**
 * Read-only detail view for RESOLVED tickets — once the doctor has
 * completed a consultation, there's nothing further to do here; payment/
 * closing is clinic-staff territory from this point on.
 */
public class TicketDetailsDialog {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm");

    public static void show(Component parent, PatientTicket ticket) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent),
                "Ticket #TK-" + String.format("%03d", ticket.getTicketId()),
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(460, 480);
        dialog.setLocationRelativeTo(parent);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(AppTheme.SURFACE);
        content.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_LG, AppTheme.SPACE_LG, AppTheme.SPACE_LG, AppTheme.SPACE_LG));

        content.add(field("Patient", patientName(ticket)));
        content.add(field("Appointment", appointmentDate(ticket)));

        JLabel statusValue = new JLabel(ticket.getCurrentStatus() != null ? ticket.getCurrentStatus().replace("_", " ") : "—");
        statusValue.setFont(FontManager.bodyFont(Font.BOLD, 14));
        statusValue.setForeground(AppTheme.statusColor(ticket.getCurrentStatus()));
        content.add(labeledRow("Status", statusValue));

        content.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
        JLabel historyTitle = new JLabel("Consultation History");
        historyTitle.setFont(FontManager.bodyFont(Font.BOLD, 14));
        historyTitle.setForeground(AppTheme.TEXT_PRIMARY);
        historyTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        historyTitle.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_SM, 0, AppTheme.SPACE_XS, 0));
        content.add(historyTitle);

        content.add(buildHistoryList(ticket));

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        dialog.setContentPane(scroll);
        dialog.setVisible(true);
    }

    private static String patientName(PatientTicket ticket) {
        if (ticket.getPatient() == null || ticket.getPatient().getName() == null) return "—";
        String first = ticket.getPatient().getName().getFirstName();
        String last = ticket.getPatient().getName().getLastName();
        return (first != null ? first : "") + " " + (last != null ? last : "");
    }

    private static String appointmentDate(PatientTicket ticket) {
        if (ticket.getAppointment() == null || ticket.getAppointment().getAppointmentDate() == null) return "—";
        return ticket.getAppointment().getAppointmentDate() + " " +
                (ticket.getAppointment().getAppointmentTime() != null ? ticket.getAppointment().getAppointmentTime() : "");
    }

    private static JComponent buildHistoryList(PatientTicket ticket) {
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);
        list.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (ticket.getStatusHistory() == null || ticket.getStatusHistory().isEmpty()) {
            JLabel none = new JLabel("No history recorded.");
            none.setFont(FontManager.bodyFont(Font.PLAIN, 12));
            none.setForeground(AppTheme.TEXT_MUTED);
            list.add(none);
            return list;
        }

        for (TicketStatus status : ticket.getStatusHistory()) {
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

            JLabel statusLabel = new JLabel(status.getStatusType() != null ? status.getStatusType().replace("_", " ") : "—");
            statusLabel.setFont(FontManager.bodyFont(Font.BOLD, 12));
            statusLabel.setForeground(AppTheme.statusColor(status.getStatusType()));

            JLabel dateLabel = new JLabel(status.getStatusDate() != null ? status.getStatusDate().format(DATE_FMT) : "");
            dateLabel.setFont(FontManager.bodyFont(Font.PLAIN, 11));
            dateLabel.setForeground(AppTheme.TEXT_MUTED);

            row.add(statusLabel, BorderLayout.WEST);
            row.add(dateLabel, BorderLayout.EAST);
            list.add(row);

            if (status.getNotes() != null && !status.getNotes().isBlank()) {
                JLabel notes = new JLabel("<html><i>" + status.getNotes() + "</i></html>");
                notes.setFont(FontManager.bodyFont(Font.PLAIN, 11));
                notes.setForeground(AppTheme.TEXT_SECONDARY);
                notes.setAlignmentX(Component.LEFT_ALIGNMENT);
                notes.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
                list.add(notes);
            }
        }
        return list;
    }

    private static JComponent field(String label, String value) {
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(FontManager.bodyFont(Font.PLAIN, 14));
        valueLabel.setForeground(AppTheme.TEXT_PRIMARY);
        return labeledRow(label, valueLabel);
    }

    private static JComponent labeledRow(String label, JComponent valueComponent) {
        JPanel block = new JPanel();
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setOpaque(false);
        block.setAlignmentX(Component.LEFT_ALIGNMENT);
        block.setBorder(BorderFactory.createEmptyBorder(0, 0, AppTheme.SPACE_SM, 0));

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(FontManager.bodyFont(Font.BOLD, 11));
        labelComp.setForeground(AppTheme.TEXT_MUTED);
        labelComp.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueComponent.setAlignmentX(Component.LEFT_ALIGNMENT);

        block.add(labelComp);
        block.add(valueComponent);
        return block;
    }
}