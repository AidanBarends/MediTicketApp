package za.ac.cput.ui.patient.pages;

import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.api.BaseApiClient;
import za.ac.cput.model.domain.Appointment;
import za.ac.cput.model.domain.Payment;
import za.ac.cput.session.SessionManager;
import za.ac.cput.ui.patient.components.StatusBadge;
import za.ac.cput.ui.patient.components.SummaryCard;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class DashboardPage extends JPanel {

    private SummaryCard pendingAppointmentsCard;
    private SummaryCard activeTicketsCard;
    private SummaryCard outstandingPaymentsCard;
    private SummaryCard notificationsCard;

    private JPanel timelineSection;
    private JPanel nextAppointmentSection;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm a");

    public DashboardPage() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BACKGROUND);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(AppTheme.BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_LG, AppTheme.SPACE_LG, AppTheme.SPACE_LG, AppTheme.SPACE_LG));

        content.add(buildGreeting());
        content.add(Box.createVerticalStrut(AppTheme.SPACE_LG));
        content.add(buildSummaryCards());
        content.add(Box.createVerticalStrut(AppTheme.SPACE_LG));
        content.add(buildTwoColumnSection());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        loadData();
    }

    // ── Greeting + quick actions ────────────────────────────────

    private JComponent buildGreeting() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        SessionManager session = SessionManager.getInstance();
        String firstName = extractFirstName(session.getFullName());

        // The 👋 emoji has no glyph in our embedded Playfair/Inter fonts, so it's
        // wrapped in its own HTML span with a system font-family — the rest of
        // the greeting keeps using the real headline font via setFont() below.
        JLabel greeting = new JLabel(
                "<html>" + greetingForTime() + ", " + firstName
                        + " <span style='font-family:" + Font.SANS_SERIF + ";'>\uD83D\uDC4B</span></html>"
        );
        greeting.setFont(FontManager.headlineFont(Font.BOLD, 26));
        greeting.setForeground(AppTheme.TEXT_PRIMARY);
        greeting.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Welcome back to MediTicket. Everything you need to manage your healthcare is right here.");
        subtitle.setFont(FontManager.bodyFont(Font.PLAIN, 14));
        subtitle.setForeground(AppTheme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_XS, 0, AppTheme.SPACE_MD, 0));

        panel.add(greeting);
        panel.add(subtitle);
        panel.add(buildQuickActions());
        return panel;
    }

    private JComponent buildQuickActions() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, AppTheme.SPACE_SM, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        row.add(quickActionButton("\uD83D\uDCC5", "Appointment", AppTheme.PRIMARY, AppTheme.TEXT_ON_PRIMARY));
        row.add(quickActionButton("\uD83C\uDFAB", "Tickets", AppTheme.SURFACE, AppTheme.TEXT_PRIMARY));
        row.add(quickActionButton("\uD83D\uDCB3", "Payments", AppTheme.ACCENT_DARK, AppTheme.TEXT_ON_PRIMARY));
        return row;
    }

    private JButton quickActionButton(String emoji, String label, Color background, Color foreground) {
        // Same font-fallback issue as the sidebar and greeting: the emoji is
        // wrapped in its own HTML span using a system font-family, while the
        // label text still renders in our real UI font via setFont() below.
        String html = "<html><span style='font-family:" + Font.SANS_SERIF + ";'>" + emoji + "</span> " + label + "</html>";

        JButton button = new JButton(html) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppTheme.RADIUS_MD, AppTheme.RADIUS_MD);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(FontManager.bodyFont(Font.BOLD, 13));
        button.setForeground(foreground);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setBorderPainted(background == AppTheme.SURFACE); // outline only the white one
        button.setBorder(BorderFactory.createCompoundBorder(
                background == AppTheme.SURFACE ? BorderFactory.createLineBorder(AppTheme.BORDER, 1, true) : null,
                BorderFactory.createEmptyBorder(10, 18, 10, 18)
        ));
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private String greetingForTime() {
        int hour = java.time.LocalTime.now().getHour();
        if (hour < 12) return "Good Morning";
        if (hour < 17) return "Good Afternoon";
        return "Good Evening";
    }

    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.isBlank()) return "there";
        return fullName.split(" ")[0];
    }

    // ── Summary cards ────────────────────────────────────────────

    private JComponent buildSummaryCards() {
        JPanel grid = new JPanel(new GridLayout(1, 4, AppTheme.SPACE_MD, 0));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        pendingAppointmentsCard = new SummaryCard("Pending Appointments", "—", AppTheme.PRIMARY);
        activeTicketsCard = new SummaryCard("Active Tickets", "—", AppTheme.STATUS_INFO);
        outstandingPaymentsCard = new SummaryCard("Outstanding Payments", "—", AppTheme.ACCENT_DARK);
        notificationsCard = new SummaryCard("Notifications", "—", AppTheme.STATUS_DANGER);

        grid.add(pendingAppointmentsCard);
        grid.add(activeTicketsCard);
        grid.add(outstandingPaymentsCard);
        grid.add(notificationsCard);
        return grid;
    }

    // ── Timeline + Next Appointment ─────────────────────────────

    private JComponent buildTwoColumnSection() {
        JPanel columns = new JPanel(new GridLayout(1, 2, AppTheme.SPACE_LG, 0));
        columns.setOpaque(false);
        columns.setAlignmentX(Component.LEFT_ALIGNMENT);

        timelineSection = buildCardSection("Appointment Timeline");
        nextAppointmentSection = buildNextAppointmentCard();

        columns.add(timelineSection);
        columns.add(nextAppointmentSection);
        return columns;
    }

    private JPanel buildCardSection(String title) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppTheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(AppTheme.SPACE_MD, AppTheme.SPACE_MD, AppTheme.SPACE_MD, AppTheme.SPACE_MD)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FontManager.bodyFont(Font.BOLD, 15));
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, AppTheme.SPACE_SM, 0));
        card.add(titleLabel);

        JLabel loading = new JLabel("Loading...");
        loading.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        loading.setForeground(AppTheme.TEXT_MUTED);
        loading.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(loading);

        return card;
    }

    private JPanel buildNextAppointmentCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppTheme.PRIMARY);
        card.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_MD, AppTheme.SPACE_MD, AppTheme.SPACE_MD, AppTheme.SPACE_MD));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel loading = new JLabel("Loading...");
        loading.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        loading.setForeground(Color.WHITE);
        loading.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(loading);

        return card;
    }

    private void replaceSectionBody(JPanel section, List<JComponent> rows) {
        while (section.getComponentCount() > 1) {
            section.remove(1);
        }
        for (JComponent row : rows) {
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            section.add(row);
        }
        section.revalidate();
        section.repaint();
    }

    // ── Data loading ─────────────────────────────────────────────

    private void loadData() {
        int patientId = SessionManager.getInstance().getUserId();

        List<Appointment> appointments = List.of();
        BaseApiClient.ApiResult<List<Appointment>> apptResult =
                ApiClientProvider.getInstance().appointments().findByPatient(patientId);
        if (apptResult.isSuccess()) appointments = apptResult.getData();

        int activeTicketCount = 0;
        var ticketResult = ApiClientProvider.getInstance().patientTickets().findByPatientUserId(patientId);
        if (ticketResult.isSuccess()) {
            activeTicketCount = (int) ticketResult.getData().stream()
                    .filter(t -> t.getCurrentStatus() != null
                            && !t.getCurrentStatus().equals("RESOLVED")
                            && !t.getCurrentStatus().equals("CLOSED"))
                    .count();
        }

        int notificationCount = 0;
        var notifResult = ApiClientProvider.getInstance().notifications().findByPatient(patientId);
        if (notifResult.isSuccess()) notificationCount = notifResult.getData().size();

        // No findByPatient endpoint on PaymentApiClient yet — filtering
        // client-side from getAll() as a stopgap. Worth adding a proper
        // backend endpoint (e.g. GET /payments/patient/{id}) later.
        int outstandingPaymentCount = 0;
        var paymentResult = ApiClientProvider.getInstance().payments().getAll();
        if (paymentResult.isSuccess()) {
            outstandingPaymentCount = (int) paymentResult.getData().stream()
                    .filter(p -> p.getAppointment() != null
                            && p.getAppointment().getPatient() != null
                            && p.getAppointment().getPatient().getUserId() == patientId
                            && "PENDING".equals(p.getPaymentStatus()))
                    .count();
        }

        long pendingCount = appointments.stream()
                .filter(a -> "PENDING".equals(a.getConfirmationStatus()))
                .count();

        pendingAppointmentsCard.setValue(String.valueOf(pendingCount));
        activeTicketsCard.setValue(String.valueOf(activeTicketCount));
        outstandingPaymentsCard.setValue(String.valueOf(outstandingPaymentCount));
        notificationsCard.setValue(String.valueOf(notificationCount));

        Optional<Appointment> nextAppointment = appointments.stream()
                .filter(a -> a.getAppointmentDate() != null && !a.getAppointmentDate().isBefore(LocalDate.now()))
                .filter(a -> !"CANCELLED".equals(a.getConfirmationStatus()) && !"REJECTED".equals(a.getConfirmationStatus()))
                .min(Comparator.comparing(Appointment::getAppointmentDate));

        renderTimeline(nextAppointment.orElse(null));
        renderNextAppointment(nextAppointment.orElse(null));
    }

    private void renderTimeline(Appointment appointment) {
        if (appointment == null) {
            replaceSectionBody(timelineSection, List.of(
                    infoLabel("No upcoming appointments. Book one from the Appointments tab.")
            ));
            return;
        }

        String status = appointment.getConfirmationStatus();
        boolean requested = true; // always true if it exists
        boolean underReview = "PENDING".equals(status);
        boolean confirmed = "CONFIRMED".equals(status) || "COMPLETED".equals(status);
        boolean completed = "COMPLETED".equals(status);

        replaceSectionBody(timelineSection, List.of(
                timelineStep("Appointment Requested", "Request received and awaiting triage.", requested),
                timelineStep("Clinical Review", underReview ? "Medical team reviewing your request." : "Complete.", requested),
                timelineStep("Doctor Assigned", confirmed ? "You've been matched with a specialist." : "Matching you with the right specialist.", confirmed),
                timelineStep("Final Confirmation", completed ? "Appointment completed." : "Ready for your arrival.", completed)
        ));
    }

    private JComponent timelineStep(String title, String subtitle, boolean done) {
        JPanel row = new JPanel(new BorderLayout(AppTheme.SPACE_SM, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel dot = new JLabel(done ? "\u2713" : "\u25CB"); // ✓ or ○
        dot.setFont(FontManager.bodyFont(Font.BOLD, 14));
        dot.setForeground(done ? AppTheme.STATUS_SUCCESS : AppTheme.TEXT_MUTED);
        dot.setPreferredSize(new Dimension(20, 20));
        dot.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel textStack = new JPanel();
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));
        textStack.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FontManager.bodyFont(Font.BOLD, 13));
        titleLabel.setForeground(done ? AppTheme.TEXT_PRIMARY : AppTheme.TEXT_MUTED);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(FontManager.bodyFont(Font.PLAIN, 12));
        subtitleLabel.setForeground(AppTheme.TEXT_MUTED);

        textStack.add(titleLabel);
        textStack.add(subtitleLabel);

        row.add(dot, BorderLayout.WEST);
        row.add(textStack, BorderLayout.CENTER);
        return row;
    }

    private void renderNextAppointment(Appointment appointment) {
        nextAppointmentSection.removeAll();

        if (appointment == null) {
            JLabel empty = new JLabel("No upcoming appointments.");
            empty.setFont(FontManager.bodyFont(Font.PLAIN, 13));
            empty.setForeground(Color.WHITE);
            nextAppointmentSection.add(empty);
            nextAppointmentSection.revalidate();
            nextAppointmentSection.repaint();
            return;
        }

        JLabel tag = new JLabel("NEXT APPOINTMENT");
        tag.setFont(FontManager.bodyFont(Font.BOLD, 10));
        tag.setForeground(AppTheme.PRIMARY_LIGHT);
        tag.setAlignmentX(Component.LEFT_ALIGNMENT);

        String doctorText = appointment.getDoctor() != null && appointment.getDoctor().getName() != null
                ? "Dr. " + appointment.getDoctor().getName().getFullName()
                : "Doctor to be assigned";
        String specialty = appointment.getDoctor() != null && appointment.getDoctor().getSpecialty() != null
                ? appointment.getDoctor().getSpecialty()
                : appointment.getReason() != null ? appointment.getReason() : "General Consultation";

        JLabel headline = new JLabel("<html>" + specialty + "</html>");
        headline.setFont(FontManager.headlineFont(Font.BOLD, 22));
        headline.setForeground(Color.WHITE);
        headline.setAlignmentX(Component.LEFT_ALIGNMENT);
        headline.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_XS, 0, AppTheme.SPACE_SM, 0));

        JLabel doctorLabel = new JLabel(doctorText);
        doctorLabel.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        doctorLabel.setForeground(AppTheme.PRIMARY_LIGHT);
        doctorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        doctorLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, AppTheme.SPACE_MD, 0));

        JPanel dateTimeRow = new JPanel(new GridLayout(1, 2, AppTheme.SPACE_MD, 0));
        dateTimeRow.setOpaque(false);
        dateTimeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        dateTimeRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        dateTimeRow.add(dateTimeBlock("DATE", appointment.getAppointmentDate() != null
                ? appointment.getAppointmentDate().format(DATE_FMT) : "—"));
        dateTimeRow.add(dateTimeBlock("TIME", appointment.getAppointmentTime() != null
                ? appointment.getAppointmentTime().format(TIME_FMT) : "—"));

        StatusBadge statusBadge = new StatusBadge(appointment.getConfirmationStatus());
        statusBadge.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusBadge.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_MD, 0, 0, 0));

        nextAppointmentSection.add(tag);
        nextAppointmentSection.add(headline);
        nextAppointmentSection.add(doctorLabel);
        nextAppointmentSection.add(dateTimeRow);
        nextAppointmentSection.add(statusBadge);

        nextAppointmentSection.revalidate();
        nextAppointmentSection.repaint();
    }

    private JComponent dateTimeBlock(String label, String value) {
        JPanel block = new JPanel();
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setOpaque(false);

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(FontManager.bodyFont(Font.BOLD, 10));
        labelComp.setForeground(AppTheme.PRIMARY_LIGHT);

        JLabel valueComp = new JLabel(value);
        valueComp.setFont(FontManager.bodyFont(Font.BOLD, 14));
        valueComp.setForeground(Color.WHITE);

        block.add(labelComp);
        block.add(valueComp);
        return block;
    }

    private JComponent infoLabel(String text) {
        JLabel label = new JLabel("<html>" + text + "</html>");
        label.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        label.setForeground(AppTheme.TEXT_MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
}