package za.ac.cput.ui.clinicstaff.components;

import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.api.BaseApiClient;
import za.ac.cput.model.auth.ChangePasswordRequest;
import za.ac.cput.session.SessionManager;
import za.ac.cput.ui.auth.components.PrimaryButton;
import za.ac.cput.ui.auth.components.ToggleablePasswordField;
import za.ac.cput.ui.theme.AppDialog;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;

import javax.swing.*;
import java.awt.*;

public class ChangePasswordDialog {

    public static void show(Component parent) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent),
                "Change Password", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(400, 340);
        dialog.setLocationRelativeTo(parent);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(AppTheme.SURFACE);
        content.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_LG, AppTheme.SPACE_LG, AppTheme.SPACE_LG, AppTheme.SPACE_LG));

        ToggleablePasswordField oldPassword = new ToggleablePasswordField("Current Password");
        oldPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        oldPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        ToggleablePasswordField newPassword = new ToggleablePasswordField("New Password");
        newPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        newPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        ToggleablePasswordField confirmPassword = new ToggleablePasswordField("Confirm New Password");
        confirmPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        confirmPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        JLabel errorLabel = new JLabel(" ");
        errorLabel.setFont(FontManager.bodyFont(Font.PLAIN, 12));
        errorLabel.setForeground(AppTheme.STATUS_DANGER);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorLabel.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_SM, 0, AppTheme.SPACE_SM, 0));

        PrimaryButton submit = new PrimaryButton("Update Password");
        submit.setAlignmentX(Component.LEFT_ALIGNMENT);
        submit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        submit.addActionListener(e -> {
            String oldPass = new String(oldPassword.getPassword());
            String newPass = new String(newPassword.getPassword());
            String confirmPass = new String(confirmPassword.getPassword());

            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                errorLabel.setText("Please fill in all fields.");
                return;
            }
            if (!newPass.equals(confirmPass)) {
                errorLabel.setText("New passwords do not match.");
                return;
            }
            if (newPass.length() < 8) {
                errorLabel.setText("New password must be at least 8 characters.");
                return;
            }

            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setEmail(SessionManager.getInstance().getEmail());
            request.setOldPassword(oldPass);
            request.setNewPassword(newPass);

            BaseApiClient.ApiResult<String> result = ApiClientProvider.getInstance().auth().changePassword(request);

            if (result.isSuccess()) {
                dialog.dispose();
                AppDialog.show(parent, "Password Updated", "Your password has been changed successfully.", AppDialog.Type.SUCCESS);
            } else {
                errorLabel.setText(result.getMessage() != null ? result.getMessage() : "Unable to change password.");
            }
        });

        content.add(oldPassword);
        content.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
        content.add(newPassword);
        content.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
        content.add(confirmPassword);
        content.add(errorLabel);
        content.add(submit);

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }
}