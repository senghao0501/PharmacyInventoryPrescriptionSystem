// RegistrationDialog.java
package pharmacy.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import pharmacy.manager.UserManager;
import pharmacy.model.Patient;

public class RegistrationDialog extends JDialog {
    private UserManager userManager;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField fullNameField;
    private JTextField contactField;
    private JTextField emailField;
    private JTextField dobField;

    public RegistrationDialog(JFrame parent, UserManager userManager) {
        super(parent, "Patient Registration", true);
        this.userManager = userManager;

        setSize(400, 350);
        setLocationRelativeTo(parent);

        buildUI();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel(new GridLayout(7, 2, 10, 10));

        formPanel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        formPanel.add(usernameField);

        formPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);

        formPanel.add(new JLabel("Confirm Password:"));
        confirmPasswordField = new JPasswordField();
        formPanel.add(confirmPasswordField);

        formPanel.add(new JLabel("Full Name:"));
        fullNameField = new JTextField();
        formPanel.add(fullNameField);

        formPanel.add(new JLabel("Contact Number:"));
        contactField = new JTextField();
        formPanel.add(contactField);

        formPanel.add(new JLabel("Email:"));
        emailField = new JTextField();
        formPanel.add(emailField);

        formPanel.add(new JLabel("Date of Birth (YYYY-MM-DD):"));
        dobField = new JTextField();
        formPanel.add(dobField);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton registerButton = new JButton("Register");
        JButton cancelButton = new JButton("Cancel");

        registerButton.addActionListener(e -> register());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void register() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String fullName = fullNameField.getText().trim();
        String contact = contactField.getText().trim();
        String email = emailField.getText().trim();
        String dobStr = dobField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Username, password, and full name are required.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                "Passwords do not match.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (userManager.findByUsername(username) != null) {
            JOptionPane.showMessageDialog(this,
                "Username already exists.",
                "Registration Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        Date dob = null;
        if (!dobStr.isEmpty()) {
            try {
                dob = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(dobStr);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Invalid date format. Please use YYYY-MM-DD.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        String userId = "P" + System.currentTimeMillis();
        String mrn = "MRN" + System.currentTimeMillis();

        Patient patient = new Patient(
            userId, username, password, fullName,
            contact, email, true, mrn, dob, ""
        );

        try {
            userManager.addUser(patient);
            JOptionPane.showMessageDialog(this,
                "Registration successful!\nYou can now login.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Registration failed: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}