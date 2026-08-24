package pharmacy.gui;

import pharmacy.manager.UserManager;
import pharmacy.model.*;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final UserManager userManager = new UserManager();

    public LoginFrame() {
        setTitle("Pharmacy Inventory & Prescription System");
        setSize(450, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel());

        JButton loginButton = new JButton("Login");
        panel.add(loginButton);
        loginButton.addActionListener(e -> login());

        panel.add(new JLabel());
        JButton exitButton = new JButton("Exit");
        panel.add(exitButton);
        exitButton.addActionListener(e -> System.exit(0));

        add(panel);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.");
            return;
        }

        User user = userManager.authenticate(username, password);

        if (user == null) {
            JOptionPane.showMessageDialog(this, "Invalid account or password.");
            return;
        }

        dispose();

        if (user instanceof Doctor) {
            new DoctorFrame((Doctor) user).setVisible(true);
        } else if (user instanceof Pharmacist) {
            new PharmacistFrame((Pharmacist) user).setVisible(true);
        } else if (user instanceof Patient) {
            new PatientFrame((Patient) user).setVisible(true);
        } else if (user instanceof Admin) {
            new AdminFrame((Admin) user).setVisible(true);
        }
    }
}