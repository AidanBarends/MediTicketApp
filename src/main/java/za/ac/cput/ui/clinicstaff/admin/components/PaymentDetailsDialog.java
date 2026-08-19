package za.ac.cput.ui.clinicstaff.admin.components;

import za.ac.cput.model.domain.Appointment;
import za.ac.cput.model.domain.Payment;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;

import javax.swing.*;
import java.awt.*;
import java.math.RoundingMode;

/**
 * Read-only for admin/nurse — payment confirmation is now entirely the
 * patient's action (their own Payments page). Staff can monitor status
 * here but can no longer mark anything paid on the patient's behalf.
 */
public class PaymentDetailsDialog {

    public static void show(Component parent, Payment payment) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent),
                "Payment Details", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(420, 380);
        dialog.setLocationRelativeTo(parent);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(AppTheme.SURFACE);
        content.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_LG, AppTheme.SPACE_LG, AppTheme.SPACE_LG, AppTheme.SPACE_LG));

        content.add(field("Patient", patientName(payment)));
        content.add(field("Doctor", doctorName(payment)));
        content.add(field("Appointment", appointmentDate(payment)));
        content.add(field("Amount", payment.getPaymentAmount() != null
                ? "R" + payment.getPaymentAmount().setScale(2, RoundingMode.HALF_UP) : "—"));
        content.add(field("Method", payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "—"));

        JLabel statusValue = new JLabel(payment.getPaymentStatus() != null ? payment.getPaymentStatus() : "—");
        statusValue.setFont(FontManager.bodyFont(Font.BOLD, 14));
        statusValue.setForeground(AppTheme.statusColor(payment.getPaymentStatus()));
        content.add(labeledRow("Status", statusValue));

        content.add(Box.createVerticalStrut(AppTheme.SPACE_MD));

        if ("PENDING".equals(payment.getPaymentStatus())) {
            JLabel note = new JLabel("<html><i>Waiting for the patient to complete payment on their dashboard.</i></html>");
            note.setFont(FontManager.bodyFont(Font.PLAIN, 12));
            note.setForeground(AppTheme.TEXT_MUTED);
            note.setAlignmentX(Component.LEFT_ALIGNMENT);
            content.add(note);
        }

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }

    private static String patientName(Payment payment) {
        Appointment appt = payment.getAppointment();
        if (appt == null || appt.getPatient() == null || appt.getPatient().getName() == null) return "—";
        String first = appt.getPatient().getName().getFirstName();
        String last = appt.getPatient().getName().getLastName();
        return (first != null ? first : "") + " " + (last != null ? last : "");
    }

    private static String doctorName(Payment payment) {
        Appointment appt = payment.getAppointment();
        if (appt == null || appt.getDoctor() == null || appt.getDoctor().getName() == null) return "—";
        String last = appt.getDoctor().getName().getLastName();
        return "Dr. " + (last != null ? last : "—");
    }

    private static String appointmentDate(Payment payment) {
        Appointment appt = payment.getAppointment();
        if (appt == null || appt.getAppointmentDate() == null) return "—";
        return appt.getAppointmentDate() + " " + (appt.getAppointmentTime() != null ? appt.getAppointmentTime() : "");
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