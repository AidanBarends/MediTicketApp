package za.ac.cput.ui.clinicstaff.admin.components;

import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.api.BaseApiClient;
import za.ac.cput.model.domain.Payment;
import za.ac.cput.model.domain.PatientTicket;
import za.ac.cput.model.domain.TicketStatus;
import za.ac.cput.ui.theme.AppDialog;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * Modal detail view for a single ticket. Shows patient/doctor/appointment
 * context, full status history, and — only when the ticket is RESOLVED and
 * no Payment row exists yet for its appointment — a "Generate Payment
 * Request" action. This mirrors the nurse/admin operational split from the
 * appointment workflow doc: doctors own the clinical side (consultation
 * notes aren't modeled on PatientTicket yet, so there's nothing to show
 * there beyond status/notes), nurse/admin own getting the ticket to CLOSED.
 */
public class TicketDetailsDialog {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm");

    public static void show(Component parent, PatientTicket ticket, Payment existingPayment, Runnable onChanged) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent),
                "Ticket #TK-" + String.format("%03d", ticket.getTicketId()),
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(480, 560);
        dialog.setLocationRelativeTo(parent);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(AppTheme.SURFACE);
        content.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_LG, AppTheme.SPACE_LG, AppTheme.SPACE_LG, AppTheme.SPACE_LG));

        content.add(fieldBlock("Patient", patientName(ticket)));
        content.add(fieldBlock("Doctor", doctorName(ticket)));
        content.add(fieldBlock("Appointment",
                ticket.getAppointment() != null && ticket.getAppointment().getAppointmentDate() != null
                        ? ticket.getAppointment().getAppointmentDate() + " " +
                        (ticket.getAppointment().getAppointmentTime() != null ? ticket.getAppointment().getAppointmentTime() : "")
                        : "—"));

        JLabel statusValue = new JLabel(statusBadgeText(ticket.getCurrentStatus()));
        statusValue.setFont(FontManager.bodyFont(Font.BOLD, 13));
        statusValue.setForeground(AppTheme.statusColor(ticket.getCurrentStatus()));
        content.add(labeledRow("Ticket Status", statusValue));

        content.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
        content.add(sectionTitle("Status History"));
        content.add(buildHistoryList(ticket));

        content.add(Box.createVerticalStrut(AppTheme.SPACE_MD));
        content.add(sectionTitle("Payment"));

        if (existingPayment != null) {
            content.add(fieldBlock("Amount", "R" + formatAmount(existingPayment.getPaymentAmount())));
            JLabel payStatus = new JLabel(existingPayment.getPaymentStatus());
            payStatus.setFont(FontManager.bodyFont(Font.BOLD, 13));
            payStatus.setForeground(AppTheme.statusColor(existingPayment.getPaymentStatus()));
            content.add(labeledRow("Status", payStatus));

            if ("PENDING".equals(existingPayment.getPaymentStatus())) {
                JButton markPaidButton = new JButton("Mark as Paid");
                markPaidButton.setFont(FontManager.bodyFont(Font.BOLD, 13));
                markPaidButton.setForeground(AppTheme.TEXT_ON_PRIMARY);
                markPaidButton.setBackground(AppTheme.STATUS_SUCCESS);
                markPaidButton.setFocusPainted(false);
                markPaidButton.setBorderPainted(false);
                markPaidButton.setAlignmentX(Component.LEFT_ALIGNMENT);
                markPaidButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                markPaidButton.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
                markPaidButton.addActionListener(e -> markAsPaid(dialog, parent, existingPayment, onChanged));
                content.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
                content.add(markPaidButton);
            }
        } else if ("RESOLVED".equals(ticket.getCurrentStatus())) {
            JLabel noPayment = new JLabel("No payment request has been generated yet.");
            noPayment.setFont(FontManager.bodyFont(Font.PLAIN, 13));
            noPayment.setForeground(AppTheme.TEXT_SECONDARY);
            noPayment.setAlignmentX(Component.LEFT_ALIGNMENT);
            noPayment.setBorder(BorderFactory.createEmptyBorder(0, 0, AppTheme.SPACE_SM, 0));
            content.add(noPayment);

            JButton generateButton = new JButton("Generate Payment Request");
            generateButton.setFont(FontManager.bodyFont(Font.BOLD, 13));
            generateButton.setForeground(AppTheme.TEXT_ON_PRIMARY);
            generateButton.setBackground(AppTheme.PRIMARY);
            generateButton.setFocusPainted(false);
            generateButton.setBorderPainted(false);
            generateButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            generateButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            generateButton.addActionListener(e -> {
                dialog.dispose();
                GeneratePaymentDialog.show(parent, ticket, onChanged);
            });
            content.add(generateButton);
        } else {
            JLabel notReady = new JLabel("Payment becomes available once the ticket is resolved.");
            notReady.setFont(FontManager.bodyFont(Font.PLAIN, 13));
            notReady.setForeground(AppTheme.TEXT_MUTED);
            notReady.setAlignmentX(Component.LEFT_ALIGNMENT);
            content.add(notReady);
        }

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        dialog.setContentPane(scroll);
        dialog.setVisible(true);
    }

    // Note: no consultation-fee input here — Payment.paymentAmount must
    // come from somewhere. PatientTicket has no fee field, so for now the
    // admin/nurse types it in manually when generating the request. If a
    // "consultationFee" field ever gets added to PatientTicket/TicketStatus
    // by the doctor at RESOLVED time, this should read that instead of
    // prompting — flagging as a known gap, not a design choice.
    private static void generatePayment(JDialog dialog, Component parent, PatientTicket ticket, Runnable onChanged) {
        String amountStr = JOptionPane.showInputDialog(dialog,
                "Enter the consultation fee for this ticket:", "Generate Payment Request",
                JOptionPane.PLAIN_MESSAGE);
        if (amountStr == null || amountStr.isBlank()) return;

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountStr.trim());
        } catch (NumberFormatException ex) {
            AppDialog.show(parent, "Invalid Amount", "Please enter a valid number.", AppDialog.Type.ERROR);
            return;
        }

        Payment payment = new Payment();
        payment.setAppointment(ticket.getAppointment());
        payment.setPaymentAmount(amount);
        payment.setPaymentStatus("PENDING");

        BaseApiClient.ApiResult<Payment> result = ApiClientProvider.getInstance().payments().create(payment);

        if (result.isSuccess()) {
            dialog.dispose();
            AppDialog.show(parent, "Payment Request Generated",
                    "A payment request for R" + formatAmount(amount) + " has been created.", AppDialog.Type.SUCCESS);
            if (onChanged != null) onChanged.run();
        } else {
            AppDialog.show(parent, "Unable to Generate Payment",
                    result.getMessage() != null ? result.getMessage() : "Something went wrong.", AppDialog.Type.ERROR);
        }
    }

    // Sends the payment back with paymentStatus flipped to PAID. Backend's
    // PaymentService.update() detects this and auto-closes the linked ticket,
    // so no separate "close ticket" call is needed here.
    private static void markAsPaid(JDialog dialog, Component parent, Payment payment, Runnable onChanged) {
        int confirm = JOptionPane.showConfirmDialog(dialog,
                "Confirm that payment of R" + formatAmount(payment.getPaymentAmount()) + " has been received?",
                "Mark as Paid", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        payment.setPaymentStatus("PAID");
        BaseApiClient.ApiResult<Payment> result = ApiClientProvider.getInstance().payments().update(payment);

        if (result.isSuccess()) {
            dialog.dispose();
            AppDialog.show(parent, "Payment Confirmed",
                    "The payment has been marked as paid, and the ticket has been closed.", AppDialog.Type.SUCCESS);
            if (onChanged != null) onChanged.run();
        } else {
            AppDialog.show(parent, "Unable to Update Payment",
                    result.getMessage() != null ? result.getMessage() : "Something went wrong.", AppDialog.Type.ERROR);
        }
    }

    private static String formatAmount(BigDecimal amount) {
        return amount != null ? amount.setScale(2, java.math.RoundingMode.HALF_UP).toString() : "0.00";
    }

    private static String patientName(PatientTicket ticket) {
        if (ticket.getPatient() == null || ticket.getPatient().getName() == null) return "—";
        return fullName(ticket.getPatient().getName().getFirstName(), ticket.getPatient().getName().getLastName());
    }

    private static String doctorName(PatientTicket ticket) {
        if (ticket.getAppointment() == null || ticket.getAppointment().getDoctor() == null
                || ticket.getAppointment().getDoctor().getName() == null) return "—";
        return "Dr. " + fullName(ticket.getAppointment().getDoctor().getName().getFirstName(),
                ticket.getAppointment().getDoctor().getName().getLastName());
    }

    private static String fullName(String first, String last) {
        StringBuilder sb = new StringBuilder();
        if (first != null) sb.append(first);
        if (last != null) sb.append(" ").append(last);
        return sb.length() > 0 ? sb.toString().trim() : "—";
    }

    private static String statusBadgeText(String status) {
        return status != null ? status.replace("_", " ") : "—";
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

            JLabel statusLabel = new JLabel(statusBadgeText(status.getStatusType()));
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

    private static JComponent fieldBlock(String label, String value) {
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

    private static JComponent sectionTitle(String title) {
        JLabel label = new JLabel(title);
        label.setFont(FontManager.bodyFont(Font.BOLD, 14));
        label.setForeground(AppTheme.TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_SM, 0, AppTheme.SPACE_XS, 0));
        return label;
    }
}