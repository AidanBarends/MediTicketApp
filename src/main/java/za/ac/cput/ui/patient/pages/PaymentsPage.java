package za.ac.cput.ui.patient.pages;

import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.api.BaseApiClient;
import za.ac.cput.model.domain.Payment;
import za.ac.cput.session.SessionManager;
import za.ac.cput.ui.patient.components.StatusBadge;
import za.ac.cput.ui.theme.AppDialog;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/**
 * PaymentApiClient has no findByPatient(id) endpoint yet, so this filters
 * client-side from getAll(). Fine for a small dataset; a real
 * GET /payments/patient/{id} endpoint would be a better long-term fix if
 * the payment list ever grows large.
 */
public class PaymentsPage extends JPanel {

    private JPanel listContainer;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private static final String[] PAY_METHODS = {"CARD", "EFT", "CASH", "MEDICAL_AID"};

    public PaymentsPage() {
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

        JLabel title = new JLabel("Payments");
        title.setFont(FontManager.headlineFont(Font.BOLD, 26));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("View and settle payments for your appointments.");
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
        BaseApiClient.ApiResult<List<Payment>> result = ApiClientProvider.getInstance().payments().getAll();

        List<Payment> myPayments = result.isSuccess()
                ? result.getData().stream()
                .filter(p -> p.getAppointment() != null
                        && p.getAppointment().getPatient() != null
                        && p.getAppointment().getPatient().getUserId() == patientId)
                .toList()
                : List.of();

        renderList(myPayments);
    }

    private void renderList(List<Payment> payments) {
        listContainer.removeAll();

        if (payments.isEmpty()) {
            listContainer.add(emptyState());
            listContainer.revalidate();
            listContainer.repaint();
            return;
        }

        // Outstanding (PENDING) payments float to the top — those are
        // the ones that actually need the patient's attention.
        List<Payment> sorted = payments.stream()
                .sorted(Comparator.comparing((Payment p) -> "PENDING".equals(p.getPaymentStatus()) ? 0 : 1))
                .toList();

        for (Payment payment : sorted) {
            listContainer.add(paymentCard(payment));
            listContainer.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
        }

        listContainer.revalidate();
        listContainer.repaint();
    }

    private JComponent paymentCard(Payment payment) {
        JPanel card = new JPanel(new BorderLayout(AppTheme.SPACE_MD, 0));
        card.setBackground(AppTheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(AppTheme.SPACE_MD, AppTheme.SPACE_MD, AppTheme.SPACE_MD, AppTheme.SPACE_MD)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JPanel textStack = new JPanel();
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));
        textStack.setOpaque(false);

        String amountText = payment.getPaymentAmount() != null
                ? "R " + payment.getPaymentAmount().setScale(2, java.math.RoundingMode.HALF_UP)
                : "R —";
        JLabel amountLabel = new JLabel(amountText);
        amountLabel.setFont(FontManager.headlineFont(Font.BOLD, 18));
        amountLabel.setForeground(AppTheme.TEXT_PRIMARY);
        amountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String doctorText = payment.getAppointment() != null
                && payment.getAppointment().getDoctor() != null
                && payment.getAppointment().getDoctor().getName() != null
                ? "Dr. " + payment.getAppointment().getDoctor().getName().getFullName()
                : "General consultation";
        String dateText = payment.getPaymentDate() != null
                ? " · " + payment.getPaymentDate().toLocalDate().format(DATE_FMT) : "";

        JLabel metaLabel = new JLabel(doctorText + dateText);
        metaLabel.setFont(FontManager.bodyFont(Font.PLAIN, 12));
        metaLabel.setForeground(AppTheme.TEXT_SECONDARY);
        metaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        metaLabel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        String methodText = payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "Method not set";
        JLabel methodLabel = new JLabel(methodText);
        methodLabel.setFont(FontManager.bodyFont(Font.PLAIN, 11));
        methodLabel.setForeground(AppTheme.TEXT_MUTED);
        methodLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textStack.add(amountLabel);
        textStack.add(metaLabel);
        textStack.add(methodLabel);

        JPanel rightSide = new JPanel();
        rightSide.setLayout(new BoxLayout(rightSide, BoxLayout.Y_AXIS));
        rightSide.setOpaque(false);

        StatusBadge badge = new StatusBadge(payment.getPaymentStatus());
        badge.setAlignmentX(Component.RIGHT_ALIGNMENT);
        rightSide.add(badge);

        if ("PENDING".equals(payment.getPaymentStatus())) {
            JButton payButton = new JButton("Pay Now");
            payButton.setFont(FontManager.bodyFont(Font.BOLD, 12));
            payButton.setForeground(AppTheme.TEXT_ON_PRIMARY);
            payButton.setBackground(AppTheme.PRIMARY);
            payButton.setFocusPainted(false);
            payButton.setBorderPainted(false);
            payButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            payButton.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
            payButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
            payButton.addActionListener(e -> payNow(payment));
            rightSide.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
            rightSide.add(payButton);
        }

        card.add(textStack, BorderLayout.CENTER);
        card.add(rightSide, BorderLayout.EAST);
        return card;
    }

    private void payNow(Payment payment) {
        String method = (String) JOptionPane.showInputDialog(this, "Choose a payment method:",
                "Pay Now", JOptionPane.PLAIN_MESSAGE, null, PAY_METHODS, PAY_METHODS[0]);

        if (method == null) return; // user cancelled

        payment.setPaymentMethod(method);
        payment.setPaymentStatus("PAID");
        payment.setPaymentDate(java.time.LocalDateTime.now());

        BaseApiClient.ApiResult<Payment> result = ApiClientProvider.getInstance().payments().update(payment);

        if (result.isSuccess()) {
            AppDialog.show(this, "Payment Successful", "Your payment has been recorded.", AppDialog.Type.SUCCESS);
            loadData();
        } else {
            AppDialog.show(this, "Payment Failed",
                    result.getMessage() != null ? result.getMessage() : "Something went wrong.", AppDialog.Type.ERROR);
        }
    }

    private JComponent emptyState() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_XL, 0, 0, 0));

        JLabel label = new JLabel("You have no payment records yet.");
        label.setFont(FontManager.bodyFont(Font.PLAIN, 14));
        label.setForeground(AppTheme.TEXT_MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        return panel;
    }
}