package za.ac.cput.ui.auth.components;

import za.ac.cput.ui.theme.AppTheme;
import za.ac.cput.ui.theme.FontManager;

import javax.swing.*;
import java.awt.*;

public class LabeledComboBox extends JPanel {

    private final JComboBox<String> combo;

    public LabeledComboBox(String labelText, String[] options) {
        setLayout(new BorderLayout(0, 6));
        setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(FontManager.bodyFont(Font.BOLD, 13));
        label.setForeground(AppTheme.TEXT_PRIMARY);

        combo = new JComboBox<>(options);
        combo.setFont(FontManager.bodyFont(Font.PLAIN, 14));
        combo.setPreferredSize(new Dimension(0, 42));
        combo.setBackground(Color.WHITE);

        add(label, BorderLayout.NORTH);
        add(combo, BorderLayout.CENTER);
    }

    public String getSelected() {
        return (String) combo.getSelectedItem();
    }

    public JComboBox<String> getCombo() { return combo; }
}