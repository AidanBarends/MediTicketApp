package za.ac.cput;

import za.ac.cput.ui.AppFrame;
import za.ac.cput.ui.theme.AppTheme;

import javax.swing.*;

/**
 * Application entry point. Responsible only for:
 *   1. Initializing the theme (fonts, FlatLaf, UI defaults) before any
 *      Swing component is constructed — must happen first, or components
 *      built before this point won't pick up custom fonts/colors.
 *   2. Launching the single AppFrame on the Event Dispatch Thread.
 *
 * No business logic lives here — routing, auth, and screen composition
 * are AppFrame's responsibility.
 */
public class Main {

    public static void main(String[] args) {
        AppTheme.initialize();

        SwingUtilities.invokeLater(() -> {
            AppFrame frame = new AppFrame();
            frame.setVisible(true);
        });
    }
}