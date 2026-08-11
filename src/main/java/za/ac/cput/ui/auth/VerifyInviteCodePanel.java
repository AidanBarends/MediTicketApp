package za.ac.cput.ui.auth;

import za.ac.cput.api.ApiClientProvider;
import za.ac.cput.api.BaseApiClient;
import za.ac.cput.model.auth.EmployeeInviteResponse;
import za.ac.cput.ui.AppFrame;
import za.ac.cput.ui.auth.components.LabeledTextField;
import za.ac.cput.ui.auth.components.PrimaryButton;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class VerifyInviteCodePanel extends JPanel {

    private final AppFrame appFrame;
    private LabeledTextField tokenField;
    private JLabel errorLabel;

    public VerifyInviteCodePanel(AppFrame appFrame) {
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
        card.setPreferredSize(new Dimension(480, 340));

        JLabel title = new JLabel("Enter Your Invite Code");
        title.setFont(FontManager.headlineFont(Font.BOLD, 24));
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("<html>Paste the secure signup code from your invitation email below.</html>");
        subtitle.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        subtitle.setForeground(AppTheme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_XS, 0, AppTheme.SPACE_LG, 0));

        tokenField = new LabeledTextField("Invite Code");
        tokenField.getField().putClientProperty("JTextField.placeholderText", "Paste your invite token here");
        tokenField.setAlignmentX(Component.LEFT_ALIGNMENT);
        tokenField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        errorLabel = new JLabel(" ");
        errorLabel.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        errorLabel.setForeground(AppTheme.STATUS_DANGER);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        PrimaryButton verifyButton = new PrimaryButton("Verify Code");
        verifyButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        verifyButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        verifyButton.addActionListener(e -> onVerify());

        JLabel backToLogin = new JLabel("← Back to Login");
        backToLogin.setFont(FontManager.bodyFont(Font.PLAIN, 13));
        backToLogin.setForeground(AppTheme.TEXT_SECONDARY);
        backToLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        backToLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backToLogin.setBorder(BorderFactory.createEmptyBorder(AppTheme.SPACE_MD, 0, 0, 0));
        backToLogin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { appFrame.showScreen(AppFrame.SCREEN_LOGIN); }
        });

        card.add(title);
        card.add(subtitle);
        card.add(tokenField);
        card.add(Box.createVerticalStrut(AppTheme.SPACE_SM));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(AppTheme.SPACE_MD));
        card.add(verifyButton);
        card.add(backToLogin);

        return card;
    }

    private void onVerify() {
        String token = tokenField.getText().trim();
        if (token.isEmpty()) {
            errorLabel.setText("Please enter your invite code.");
            return;
        }
        errorLabel.setText(" ");

        BaseApiClient.ApiResult<EmployeeInviteResponse> result =
                ApiClientProvider.getInstance().auth().verifyEmployeeInvite(token);

        if (!result.isSuccess()) {
            errorLabel.setText("Invalid or expired invite code.");
            return;
        }

        EmployeeInviteResponse invite = result.getData();
        tokenField.getField().setText("");

        if ("DOCTOR".equals(invite.getUserType())) {
            appFrame.getDoctorSignupPanel().prefill(token, invite.getEmail());
            appFrame.showScreen(AppFrame.SCREEN_DOCTOR_SIGNUP);
        } else {
            appFrame.getClinicStaffSignupPanel().prefill(token, invite.getEmail(), invite.getStaffRole());
            appFrame.showScreen(AppFrame.SCREEN_CLINICSTAFF_SIGNUP);
        }
    }
}