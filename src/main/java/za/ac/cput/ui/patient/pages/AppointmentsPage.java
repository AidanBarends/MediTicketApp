package za.ac.cput.ui.patient.pages;

import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.api.BaseApiClient;
import za.ac.cput.model.domain.Appointment;
import za.ac.cput.session.SessionManager;
import za.ac.cput.ui.patient.components.BookAppointmentDialog;
import za.ac.cput.ui.patient.components.StatusBadge;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class AppointmentsPage extends JPanel {

    private JPanel listContainer;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm a");

    public AppointmentsPage() {
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
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel textStack = new JPanel();
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));
        textStack.setOpaque(false);

        JLabel title = new JLabel("Appointments");
        title.setFont(FontManager.headlineFont(Font.BOLD, 26));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("View your appointments and request new ones.");
        subtitle.setFont(FontManager.bodyFont(Font.PLAIN, 14));
        subtitle.setForeground(AppTheme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_XS, 0, 0, 0));

        textStack.add(title);
        textStack.add(subtitle);

        JButton bookButton = new JButton("+ Book Appointment");
        bookButton.setFont(FontManager.bodyFont(Font.BOLD, 13));
        bookButton.setForeground(AppTheme.TEXT_ON_PRIMARY);
        bookButton.setBackground(AppTheme.PRIMARY);
        bookButton.setFocusPainted(false);
        bookButton.setBorderPainted(false);
        bookButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bookButton.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        bookButton.addActionListener(e -> BookAppointmentDialog.show(this, this::loadData));

        row.add(textStack, BorderLayout.WEST);
        row.add(bookButton, BorderLayout.EAST);
        return row;
    }

    // ── Data loading ─────────────────────────────────────────────

    private void loadData() {
        int patientId = SessionManager.getInstance().getUserId();
        BaseApiClient.ApiResult<List<Appointment>> result =
                ApiClientProvider.getInstance().appointments().findByPatient(patientId);

        List<Appointment> appointments = result.isSuccess() ? result.getData() : List.of();
        renderList(appointments);
    }

    private void renderList(List<Appointment> appointments) {
        listContainer.removeAll();

        if (appointments.isEmpty()) {
            listContainer.add(emptyState());
            listContainer.revalidate();
            listContainer.repaint();
            return;
        }

        // Soonest upcoming first, so the appointment you're about to
        // attend is always the one you see without scrolling.
        List<Appointment> sorted = appointments.stream()
                .sorted(Comparator.comparing(
                        (Appointment a) -> a.getAppointmentDate() != null ? a.getAppointmentDate() : java.time.LocalDate.MAX
                ))
                .toList();

        for (Appointment appt : sorted) {
            listContainer.add(appointmentRow(appt));
            listContainer.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
        }

        listContainer.revalidate();
        listContainer.repaint();
    }

    private JComponent appointmentRow(Appointment appt) {
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

        String doctorText = appt.getDoctor() != null && appt.getDoctor().getName() != null
                ? "Dr. " + appt.getDoctor().getName().getFullName()
                : "Doctor to be assigned";
        String specialty = appt.getDoctor() != null && appt.getDoctor().getSpecialty() != null
                ? " — " + appt.getDoctor().getSpecialty() : "";

        JLabel doctorLabel = new JLabel(doctorText + specialty);
        doctorLabel.setFont(FontManager.bodyFont(Font.BOLD, 14));
        doctorLabel.setForeground(AppTheme.TEXT_PRIMARY);
        doctorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String dateTimeText = (appt.getAppointmentDate() != null ? appt.getAppointmentDate().format(DATE_FMT) : "—")
                + (appt.getAppointmentTime() != null ? " · " + appt.getAppointmentTime().format(TIME_FMT) : "");
        JLabel dateTimeLabel = new JLabel(dateTimeText);
        dateTimeLabel.setFont(FontManager.bodyFont(Font.PLAIN, 12));
        dateTimeLabel.setForeground(AppTheme.TEXT_SECONDARY);
        dateTimeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        dateTimeLabel.setBorder(BorderFactory.createEmptyBorder(2, 0, 4, 0));

        String reasonText = appt.getReason() != null && !appt.getReason().isBlank() ? appt.getReason() : "No reason provided";
        JLabel reasonLabel = new JLabel(reasonText);
        reasonLabel.setFont(FontManager.bodyFont(Font.PLAIN, 12));
        reasonLabel.setForeground(AppTheme.TEXT_MUTED);
        reasonLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textStack.add(doctorLabel);
        textStack.add(dateTimeLabel);
        textStack.add(reasonLabel);

        StatusBadge badge = new StatusBadge(appt.getConfirmationStatus());

        card.add(textStack, BorderLayout.CENTER);
        card.add(badge, BorderLayout.EAST);
        return card;
    }

    private JComponent emptyState() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_XL, 0, 0, 0));

        JLabel label = new JLabel("You don't have any appointments yet.");
        label.setFont(FontManager.bodyFont(Font.PLAIN, 14));
        label.setForeground(AppTheme.TEXT_MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        return panel;
    }
}