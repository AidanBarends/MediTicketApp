package za.ac.cput.ui.patient.components;

import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.api.BaseApiClient;
import za.ac.cput.model.domain.Appointment;
import za.ac.cput.model.domain.Doctor;
import za.ac.cput.model.domain.Patient;
import za.ac.cput.session.SessionManager;
import za.ac.cput.ui.theme.AppDialog;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Simple appointment-booking dialog for patients. Doctor list comes from
 * DoctorApiClient.getAll() (no specialty/availability filtering yet — out
 * of scope given the time available). Time is a fixed set of half-hour
 * slots rather than free text, so the backend always receives a clean
 * LocalTime with no parsing risk.
 */
public class BookAppointmentDialog {

    private static final String[] TIME_SLOTS = {
            "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
            "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00"
    };

    public static void show(Component parent, Runnable onCreated) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent),
                "Book Appointment", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(440, 420);
        dialog.setLocationRelativeTo(parent);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(AppTheme.SURFACE);
        content.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_LG, AppTheme.SPACE_LG, AppTheme.SPACE_LG, AppTheme.SPACE_LG));

        JLabel subtitle = new JLabel("Request a new appointment. The clinic will review and confirm it.");
        subtitle.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        subtitle.setForeground(AppTheme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(0, 0, AppTheme.SPACE_MD, 0));

        // ── Doctor dropdown ──
        JLabel doctorLabel = fieldLabel("Doctor");
        JComboBox<Doctor> doctorCombo = new JComboBox<>();
        doctorCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        doctorCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        doctorCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Doctor doctor) {
                    String name = doctor.getName() != null ? doctor.getName().getFullName() : "Unnamed";
                    String specialty = doctor.getSpecialty() != null ? doctor.getSpecialty() : "General";
                    setText("Dr. " + name + " — " + specialty);
                }
                return this;
            }
        });
        loadDoctors(doctorCombo);

        // ── Date picker (JSpinner, date-only) ──
        JLabel dateLabelText = fieldLabel("Date");
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        dateSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
        dateSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        dateSpinner.setValue(tomorrow.getTime());

        // ── Time slot dropdown ──
        JLabel timeLabel = fieldLabel("Time");
        JComboBox<String> timeCombo = new JComboBox<>(TIME_SLOTS);
        timeCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        timeCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        // ── Reason ──
        JLabel reasonLabel = fieldLabel("Reason for visit");
        JTextArea reasonArea = new JTextArea(3, 20);
        reasonArea.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        JScrollPane reasonScroll = new JScrollPane(reasonArea);
        reasonScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        reasonScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel errorLabel = new JLabel(" ");
        errorLabel.setFont(FontManager.bodyFont(Font.PLAIN, 12));
        errorLabel.setForeground(AppTheme.STATUS_DANGER);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorLabel.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_SM, 0, 0, 0));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SPACE_SM, 0));
        buttonRow.setOpaque(false);
        buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonRow.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_MD, 0, 0, 0));

        JButton cancel = new JButton("Cancel");
        cancel.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        cancel.setFocusPainted(false);
        cancel.addActionListener(e -> dialog.dispose());

        JButton submit = new JButton("Request Appointment");
        submit.setFont(FontManager.bodyFont(Font.BOLD, 13));
        submit.setForeground(AppTheme.TEXT_ON_PRIMARY);
        submit.setBackground(AppTheme.PRIMARY);
        submit.setFocusPainted(false);
        submit.setBorderPainted(false);
        submit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        submit.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        submit.addActionListener(e -> {
            Doctor selectedDoctor = (Doctor) doctorCombo.getSelectedItem();
            if (selectedDoctor == null) {
                errorLabel.setText("Please select a doctor.");
                return;
            }

            Date selectedDate = (Date) dateSpinner.getValue();
            LocalDate appointmentDate = selectedDate.toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            if (appointmentDate.isBefore(LocalDate.now())) {
                errorLabel.setText("Please choose a date in the future.");
                return;
            }

            String reason = reasonArea.getText().trim();
            if (reason.isEmpty()) {
                errorLabel.setText("Please enter a reason for the visit.");
                return;
            }

            LocalTime appointmentTime = LocalTime.parse((String) timeCombo.getSelectedItem());

            // Only the patient's ID is needed for the backend to link the
            // foreign key — we don't have (and don't need) the patient's
            // full record here, just who's logged in.
            Patient patient = new Patient();
            patient.setUserId(SessionManager.getInstance().getUserId());

            Appointment appointment = new Appointment();
            appointment.setPatient(patient);
            appointment.setDoctor(selectedDoctor);
            appointment.setAppointmentDate(appointmentDate);
            appointment.setAppointmentTime(appointmentTime);
            appointment.setReason(reason);
            appointment.setConfirmationStatus("PENDING");

            BaseApiClient.ApiResult<Appointment> result =
                    ApiClientProvider.getInstance().appointments().create(appointment);

            if (result.isSuccess()) {
                dialog.dispose();
                AppDialog.show(parent, "Appointment Requested",
                        "Your appointment request has been sent. You'll see it as PENDING until the clinic confirms it.",
                        AppDialog.Type.SUCCESS);
                if (onCreated != null) onCreated.run();
            } else {
                errorLabel.setText(result.getMessage() != null ? result.getMessage() : "Unable to book appointment.");
            }
        });

        buttonRow.add(cancel);
        buttonRow.add(submit);

        content.add(subtitle);
        content.add(doctorLabel);
        content.add(doctorCombo);
        content.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
        content.add(dateLabelText);
        content.add(dateSpinner);
        content.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
        content.add(timeLabel);
        content.add(timeCombo);
        content.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
        content.add(reasonLabel);
        content.add(reasonScroll);
        content.add(errorLabel);
        content.add(buttonRow);

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }

    private static void loadDoctors(JComboBox<Doctor> combo) {
        BaseApiClient.ApiResult<List<Doctor>> result = ApiClientProvider.getInstance().doctors().getAll();
        if (result.isSuccess()) {
            for (Doctor doctor : result.getData()) {
                combo.addItem(doctor);
            }
        }
    }

    private static JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FontManager.bodyFont(Font.BOLD, 12));
        label.setForeground(AppTheme.TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_XS, 0, 4, 0));
        return label;
    }
}