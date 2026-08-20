package za.ac.cput.ui.clinicstaff.components;

import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.api.BaseApiClient;
import za.ac.cput.model.auth.EmployeeInviteRequest;
import za.ac.cput.ui.auth.components.LabeledTextField;
import za.ac.cput.ui.auth.components.PrimaryButton;
import za.ac.cput.ui.theme.AppDialog;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;

import javax.swing.*;
import java.awt.*;

public class InviteEmployeeDialog {

    public static void show(Component parent, String userType, String staffRole, Runnable onSent) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent),
                "DOCTOR".equals(userType) ? "Invite Doctor" : "Invite Clinic Nurse",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(440, 280);
        dialog.setLocationRelativeTo(parent);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(AppTheme.SURFACE);
        content.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_LG, AppTheme.SPACE_LG, AppTheme.SPACE_LG, AppTheme.SPACE_LG));

        JLabel subtitle = new JLabel("<html>Enter the " + ("DOCTOR".equals(userType) ? "doctor's" : "nurse's")
                + " email address. An invitation will be sent to this email.</html>");
        subtitle.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        subtitle.setForeground(AppTheme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(0, 0, AppTheme.SPACE_MD, 0));

        LabeledTextField emailField = new LabeledTextField("Email");
        emailField.getField().putClientProperty("JTextField.placeholderText",
                "DOCTOR".equals(userType) ? "doctor@example.com" : "nurse@example.com");
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        JLabel errorLabel = new JLabel(" ");
        errorLabel.setFont(FontManager.bodyFont(Font.PLAIN, 12));
        errorLabel.setForeground(AppTheme.STATUS_DANGER);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, AppTheme.SPACE_SM, 0));
        buttonRow.setOpaque(false);
        buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JButton cancel = new JButton("Cancel");
        cancel.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        cancel.setFocusPainted(false);
        cancel.addActionListener(e -> dialog.dispose());

        PrimaryButton send = new PrimaryButton("Send Invitation");
        send.setPreferredSize(new Dimension(160, 42));
        send.addActionListener(e -> {
            String email = emailField.getText().trim();
            if (email.isEmpty() || !email.contains("@")) {
                errorLabel.setText("Please enter a valid email address.");
                return;
            }

            EmployeeInviteRequest request = new EmployeeInviteRequest();
            request.setEmail(email);
            request.setUserType(userType);
            request.setStaffRole(staffRole);

            BaseApiClient.ApiResult<String> result = ApiClientProvider.getInstance().auth().inviteEmployee(request);

            if (result.isSuccess()) {
                dialog.dispose();
                AppDialog.show(parent, "Invitation Sent",
                        "An invitation has been sent to " + email + ".",
                        AppDialog.Type.SUCCESS);
                if (onSent != null) onSent.run();
            } else {
                errorLabel.setText(result.getMessage() != null ? result.getMessage() : "Unable to send invitation.");
            }
        });

        buttonRow.add(cancel);
        buttonRow.add(send);

        content.add(subtitle);
        content.add(emailField);
        content.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
        content.add(errorLabel);
        content.add(Box.createVerticalStrut(AppTheme.SPACE_MD));
        content.add(buttonRow);

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }
}