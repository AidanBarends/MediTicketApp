package za.ac.cput.ui.auth;

import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.api.BaseApiClient;
import za.ac.cput.model.auth.ClinicStaffSignupRequest;
import za.ac.cput.ui.AppFrame;
import za.ac.cput.ui.auth.components.LabeledComboBox;
import za.ac.cput.ui.auth.components.LabeledTextField;
import za.ac.cput.ui.auth.components.PrimaryButton;
import za.ac.cput.ui.auth.components.ToggleablePasswordField;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ClinicStaffSignupPanel extends JPanel {

    private final AppFrame appFrame;
    private String inviteToken;
    private JLabel emailValueLabel;
    private JLabel roleValueLabel;

    private LabeledTextField firstName, middleName, lastName, cellPhone, dob;
    private LabeledComboBox department;
    private ToggleablePasswordField password, confirmPassword;
    private JLabel errorLabel;

    private static final DateTimeFormatter DOB_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String[] DEPARTMENTS = {
            "General Practice", "Cardiology", "Pediatrics", "Emergency", "Radiology", "Administration"
    };

    public ClinicStaffSignupPanel(AppFrame appFrame) {
        this.appFrame = appFrame;
        setLayout(new GridBagLayout());
        setBackground(AppTheme.BACKGROUND);
        add(buildCard());
    }

    public void prefill(String token, String email, String staffRole) {
        this.inviteToken = token;
        emailValueLabel.setText(email);
        roleValueLabel.setText(staffRole != null ? staffRole : "NURSE");
    }

    private JComponent buildCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppTheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(AppTheme.SPACE_XL, AppTheme.SPACE_XL, AppTheme.SPACE_XL, AppTheme.SPACE_XL)
        ));
        card.setPreferredSize(new Dimension(780, 620));

        JLabel eyebrow = new JLabel("PROFESSIONAL ENROLLMENT");
        eyebrow.setFont(FontManager.bodyFont(Font.BOLD, 12));
        eyebrow.setForeground(AppTheme.PRIMARY);
        eyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Complete Your Staff Profile");
        title.setFont(FontManager.headlineFont(Font.BOLD, 28));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel infoBanner = new JPanel();
        infoBanner.setLayout(new BoxLayout(infoBanner, BoxLayout.Y_AXIS));
        infoBanner.setBackground(AppTheme.PRIMARY_LIGHT);
        infoBanner.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_SM, AppTheme.SPACE_MD, AppTheme.SPACE_SM, AppTheme.SPACE_MD));
        infoBanner.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        emailValueLabel = new JLabel("—");
        roleValueLabel = new JLabel("—");
        JLabel infoText = new JLabel("<html>Signing up as <b>" + "</b> — role pre-verified by invitation code.</html>");

        JPanel infoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        infoRow.setOpaque(false);
        JLabel prefix = new JLabel("Signing up as");
        prefix.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        prefix.setForeground(AppTheme.TEXT_SECONDARY);
        roleValueLabel.setFont(FontManager.bodyFont(Font.BOLD, 13));
        roleValueLabel.setForeground(AppTheme.PRIMARY);
        JLabel forEmail = new JLabel("for");
        forEmail.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        forEmail.setForeground(AppTheme.TEXT_SECONDARY);
        emailValueLabel.setFont(FontManager.bodyFont(Font.BOLD, 13));
        emailValueLabel.setForeground(AppTheme.PRIMARY);
        infoRow.add(prefix);
        infoRow.add(roleValueLabel);
        infoRow.add(forEmail);
        infoRow.add(emailValueLabel);
        infoBanner.add(infoRow);
        infoBanner.setBorder(BorderFactory.createCompoundBorder(
                infoBanner.getBorder(),
                BorderFactory.createEmptyBorder(AppTheme.SPACE_SM, 0, AppTheme.SPACE_LG, 0)
        ));

        firstName = fieldOf("First Name", "e.g. Julian");
        middleName = fieldOf("Middle Name (optional)", "e.g. Alistair");
        lastName = fieldOf("Last Name", "e.g. Vance");
        cellPhone = fieldOf("Cell Phone", "+27 (0) 00 000 0000");
        dob = fieldOf("Date of Birth (yyyy-mm-dd)", "1990-06-10");
        department = new LabeledComboBox("Department", DEPARTMENTS);
        department.setAlignmentX(Component.LEFT_ALIGNMENT);

        password = new ToggleablePasswordField("Password");
        confirmPassword = new ToggleablePasswordField("Confirm Password");

        errorLabel = new JLabel(" ");
        errorLabel.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        errorLabel.setForeground(AppTheme.STATUS_DANGER);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        PrimaryButton finishButton = new PrimaryButton("Finish Setup");
        finishButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        finishButton.setMaximumSize(new Dimension(240, 46));
        finishButton.addActionListener(e -> onComplete());

        card.add(eyebrow);
        card.add(title);
        card.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
        card.add(infoBanner);
        card.add(row(firstName, middleName, lastName));
        card.add(Box.createVerticalStrut(AppTheme.SPACE_MD));
        card.add(row(cellPhone, dob, department));
        card.add(Box.createVerticalStrut(AppTheme.SPACE_MD));
        card.add(row(password, confirmPassword));
        card.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(AppTheme.SPACE_MD));
        card.add(finishButton);

        JScrollPane scroll = new JScrollPane(card);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(800, 660));
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private LabeledTextField fieldOf(String label, String placeholder) {
        LabeledTextField f = new LabeledTextField(label);
        f.getField().putClientProperty("JTextField.placeholderText", placeholder);
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        return f;
    }

    private JPanel row(JComponent... fields) {
        JPanel row = new JPanel(new GridLayout(1, fields.length, AppTheme.SPACE_MD, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        for (JComponent f : fields) row.add(f);
        return row;
    }

    private void onComplete() {
        String pwd = new String(password.getPassword());
        String confirmPwd = new String(confirmPassword.getPassword());

        if (firstName.getText().isBlank() || lastName.getText().isBlank() || pwd.isBlank()) {
            errorLabel.setText("Please fill in all required fields.");
            return;
        }
        if (!pwd.equals(confirmPwd)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }

        LocalDate parsedDob;
        try {
            parsedDob = LocalDate.parse(dob.getText().trim(), DOB_FORMAT);
        } catch (DateTimeParseException ex) {
            errorLabel.setText("Date of birth must be in yyyy-mm-dd format.");
            return;
        }

        errorLabel.setText(" ");

        ClinicStaffSignupRequest request = new ClinicStaffSignupRequest();
        request.setToken(inviteToken);
        request.setFirstName(firstName.getText().trim());
        request.setMiddleName(middleName.getText().trim());
        request.setLastName(lastName.getText().trim());
        request.setCellPhone(cellPhone.getText().trim());
        request.setPassword(pwd);
        request.setDob(parsedDob);
        request.setDepartment(department.getSelected());

        BaseApiClient.ApiResult<String> result = ApiClientProvider.getInstance().auth().signupClinicStaff(request);

        if (result.isSuccess()) {
            clearForm();
            appFrame.showScreen(AppFrame.SCREEN_EMPLOYEE_SIGNUP_SUCCESS);
        } else {
            errorLabel.setText(result.getMessage() != null ? result.getMessage() : "Signup failed.");
        }
    }

    private void clearForm() {
        firstName.getField().setText("");
        middleName.getField().setText("");
        lastName.getField().setText("");
        cellPhone.getField().setText("");
        dob.getField().setText("");
        password.clear();
        confirmPassword.clear();
        errorLabel.setText(" ");
    }
}