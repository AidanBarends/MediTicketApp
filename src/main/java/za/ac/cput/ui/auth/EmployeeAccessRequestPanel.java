package za.ac.cput.ui.auth;

import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.api.BaseApiClient;
import za.ac.cput.model.auth.EmployeeAccessRequestSubmission;
import za.ac.cput.ui.AppFrame;
import za.ac.cput.ui.auth.components.LabeledComboBox;
import za.ac.cput.ui.auth.components.LabeledTextField;
import za.ac.cput.ui.auth.components.PrimaryButton;
import za.ac.cput.ui.theme.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class EmployeeAccessRequestPanel extends JPanel {

    private final AppFrame appFrame;

    private LabeledTextField emailField;
    private LabeledComboBox roleField;
    private JLabel errorLabel;

    private static final String ROLE_DOCTOR = "Doctor";
    private static final String ROLE_NURSE = "Clinic Staff (Nurse)";

    public EmployeeAccessRequestPanel(AppFrame appFrame) {
        this.appFrame = appFrame;
        setLayout(new GridBagLayout());
        setBackground(AppTheme.BACKGROUND);
        add(buildCard());
    }

    private JComponent buildCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppTheme.SURFACE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AppTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(AppTheme.SPACE_XL, AppTheme.SPACE_XL, AppTheme.SPACE_XL, AppTheme.SPACE_XL)
        ));
        card.setPreferredSize(new Dimension(480, 460));

        JLabel badge = new JLabel("SECURE PORTAL ACCESS");
        badge.setFont(FontManager.bodyFont(Font.BOLD, 11));
        badge.setForeground(AppTheme.PRIMARY);
        badge.setOpaque(true);
        badge.setBackground(AppTheme.PRIMARY_LIGHT);
        badge.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        badge.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Employee Access Request");
        title.setFont(FontManager.headlineFont(Font.BOLD, 26));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_MD, 0, AppTheme.SPACE_SM, 0));

        JLabel subtitle = new JLabel("<html><div style='text-align:center;width:300px;'>"
                + "Enter your institutional details to request an invitation to the MediTicket portal. "
                + "Access is granted after Admin review.</div></html>");
        subtitle.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        subtitle.setForeground(AppTheme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(0, 0, AppTheme.SPACE_LG, 0));

        emailField = new LabeledTextField("Institutional Email");
        emailField.getField().putClientProperty("JTextField.placeholderText", "e.g., dr.vance@clinic-group.com");
        emailField.setAlignmentX(Component.CENTER_ALIGNMENT);
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        roleField = new LabeledComboBox("Requested Role", new String[]{ROLE_DOCTOR, ROLE_NURSE});
        roleField.setAlignmentX(Component.CENTER_ALIGNMENT);
        roleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        errorLabel = new JLabel(" ");
        errorLabel.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        errorLabel.setForeground(AppTheme.STATUS_DANGER);
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        PrimaryButton sendButton = new PrimaryButton("Send Invitation");
        sendButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        sendButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        sendButton.addActionListener(e -> onSendRequest());

        JLabel backToLogin = new JLabel("← Back to Login");
        backToLogin.setFont(FontManager.bodyFont(Font.BOLD, 13));
        backToLogin.setForeground(AppTheme.TEXT_SECONDARY);
        backToLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        backToLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backToLogin.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_MD, 0, 0, 0));
        backToLogin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { appFrame.showScreen(AppFrame.SCREEN_LOGIN); }
        });

        JLabel haveTokenLink = new JLabel("Already have a token? Enter it here");
        haveTokenLink.setFont(FontManager.bodyFont(Font.BOLD, 13));
        haveTokenLink.setForeground(AppTheme.PRIMARY);
        haveTokenLink.setAlignmentX(Component.CENTER_ALIGNMENT);
        haveTokenLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        haveTokenLink.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_SM, 0, 0, 0));
        haveTokenLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { appFrame.showScreen(AppFrame.SCREEN_VERIFY_INVITE); }
        });

        card.add(badge);
        card.add(title);
        card.add(subtitle);
        card.add(emailField);
        card.add(Box.createVerticalStrut(AppTheme.SPACE_MD));
        card.add(roleField);
        card.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(AppTheme.SPACE_MD));
        card.add(sendButton);
        card.add(backToLogin);
        card.add(haveTokenLink);

        return card;
    }

    private void onSendRequest() {
        String email = emailField.getText().trim();
        if (email.isEmpty() || !email.contains("@")) {
            errorLabel.setText("Please enter a valid email address.");
            return;
        }
        errorLabel.setText(" ");

        String selectedRole = roleField.getSelected();
        String userType = selectedRole.equals(ROLE_DOCTOR) ? "DOCTOR" : "CLINIC_STAFF";
        String staffRole = selectedRole.equals(ROLE_DOCTOR) ? null : "NURSE";

        EmployeeAccessRequestSubmission submission = new EmployeeAccessRequestSubmission();
        submission.setEmail(email);
        submission.setUserType(userType);
        submission.setStaffRole(staffRole);

        BaseApiClient.ApiResult<String> result = ApiClientProvider.getInstance().auth().requestAccess(submission);

        if (result.isSuccess()) {
            emailField.getField().setText("");
            appFrame.showScreen(AppFrame.SCREEN_REQUEST_SUBMITTED);
        } else {
            errorLabel.setText(result.getMessage() != null ? result.getMessage() : "Unable to submit request.");
        }
    }
}