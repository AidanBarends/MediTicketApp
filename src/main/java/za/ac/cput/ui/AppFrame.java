package za.ac.cput.ui;

import za.ac.cput.ui.auth.*;
import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.ImageManager;

import javax.swing.*;
import java.awt.*;

/**
 * The single persistent window for the whole application. Rather than
 * opening/closing separate JFrames per screen (login, dashboards, etc.),
 * this frame owns one CardLayout-based content container and screens are
 * swapped in and out of it. This avoids window-flicker on login/logout/
 * role-switch and keeps taskbar/icon behavior consistent throughout.
 *
 * Screens register themselves with a String key and are shown via
 * showScreen(key). Each dashboard (Patient/Doctor/ClinicStaff) and every
 * auth screen (login/signup/etc.) is added as a card here.
 */
public class AppFrame extends JFrame {

    public static final String SCREEN_LOGIN = "LOGIN";
    public static final String SCREEN_PATIENT_SIGNUP = "PATIENT_SIGNUP";
    public static final String SCREEN_ACCESS_REQUEST = "ACCESS_REQUEST";
    public static final String SCREEN_REQUEST_SUBMITTED = "REQUEST_SUBMITTED";
    public static final String SCREEN_VERIFY_INVITE = "VERIFY_INVITE";
    public static final String SCREEN_DOCTOR_SIGNUP = "DOCTOR_SIGNUP";
    public static final String SCREEN_CLINICSTAFF_SIGNUP = "CLINICSTAFF_SIGNUP";
    public static final String SCREEN_EMPLOYEE_SIGNUP_SUCCESS = "EMPLOYEE_SIGNUP_SUCCESS";
    public static final String SCREEN_ADMIN_DASHBOARD = "ADMIN_DASHBOARD";

    // CLINIC_STAFF_DASHBOARD, etc.) get added here as those screens are built.

    private final CardLayout cardLayout;
    private final JPanel contentContainer;

    private DoctorSignupPanel doctorSignupPanel;
    private ClinicStaffSignupPanel clinicStaffSignupPanel;

    public AppFrame() {
        super("MediTicket");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null); // center on screen

        ImageManager.getIcon(ImageManager.LOGO_ICON, 64, 64);
        setIconImage(ImageManager.getImage(ImageManager.LOGO_ICON));

        cardLayout = new CardLayout();
        contentContainer = new JPanel(cardLayout);
        contentContainer.setBackground(AppTheme.BACKGROUND);

        setContentPane(contentContainer);

        registerScreens();
        showScreen(SCREEN_LOGIN);
    }

    /**
     * Adds every top-level screen as a card. Placeholder screens are used
     * for anything not yet built, so the app is runnable end-to-end at
     * every step rather than only compiling once everything exists.
     */
    private void registerScreens() {
        contentContainer.add(new LoginPanel(this), SCREEN_LOGIN);
        contentContainer.add(new PatientSignupPanel(this), SCREEN_PATIENT_SIGNUP);
        contentContainer.add(new EmployeeAccessRequestPanel(this), SCREEN_ACCESS_REQUEST);
        contentContainer.add(new RequestSubmittedPanel(this), SCREEN_REQUEST_SUBMITTED);
        contentContainer.add(new VerifyInviteCodePanel(this), SCREEN_VERIFY_INVITE);

        doctorSignupPanel = new DoctorSignupPanel(this);
        clinicStaffSignupPanel = new ClinicStaffSignupPanel(this);
        contentContainer.add(doctorSignupPanel, SCREEN_DOCTOR_SIGNUP);
        contentContainer.add(clinicStaffSignupPanel, SCREEN_CLINICSTAFF_SIGNUP);
        contentContainer.add(new EmployeeSignupSuccessPanel(this), SCREEN_EMPLOYEE_SIGNUP_SUCCESS);
    }

    /**
     * Swaps the visible screen. Called by screens themselves after a
     * successful action (e.g. LoginPanel calls this after SessionManager
     * is populated, to move to the correct dashboard).
     */
    public void showScreen(String screenKey) {
        cardLayout.show(contentContainer, screenKey);
    }

    public DoctorSignupPanel getDoctorSignupPanel() { return doctorSignupPanel; }
    public ClinicStaffSignupPanel getClinicStaffSignupPanel() { return clinicStaffSignupPanel; }

    /**
     * Registers a screen built after construction (e.g. a dashboard that
     * needs SessionManager data to build its content, so it can't be
     * constructed eagerly in registerScreens()).
     */
    public void addScreen(String screenKey, JComponent screen) {
        contentContainer.add(screen, screenKey);
    }

    private JComponent buildPlaceholder(String message) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AppTheme.BACKGROUND);
        JLabel label = new JLabel(message);
        label.setFont(za.ac.cput.ui.theme.FontManager.bodyFont(Font.PLAIN, 16));
        label.setForeground(AppTheme.TEXT_SECONDARY);
        panel.add(label);
        return panel;
    }
}