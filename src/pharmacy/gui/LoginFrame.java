package pharmacy.gui;

import java.awt.GridLayout;
import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import pharmacy.manager.AlertManager;
import pharmacy.manager.InventoryManager;
import pharmacy.manager.PrescriptionManager;
import pharmacy.manager.ReportManager;
import pharmacy.manager.UserManager;
import pharmacy.model.Admin;
import pharmacy.model.Doctor;
import pharmacy.model.Patient;
import pharmacy.model.Pharmacist;
import pharmacy.model.User;
import pharmacy.service.AuthService;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private AuthService authService;
    private UserManager userManager;
    private InventoryManager inventoryManager;
    private PrescriptionManager prescriptionManager;
    private AlertManager alertManager;
    private ReportManager reportManager;

    public LoginFrame(AuthService authService, UserManager userManager,
                      InventoryManager inventoryManager,
                      PrescriptionManager prescriptionManager,
                      AlertManager alertManager, ReportManager reportManager) {
        this.authService = authService;
        this.userManager = userManager;
        this.inventoryManager = inventoryManager;
        this.prescriptionManager = prescriptionManager;
        this.alertManager = alertManager;
        this.reportManager = reportManager;

        setTitle("Pharmacy Inventory and Prescription Management System");
        setSize(450, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        buildUI();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30));

        JPanel loginPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        
        loginPanel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        loginPanel.add(usernameField);

        loginPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        loginPanel.add(passwordField);

        loginPanel.add(new JLabel());
        JButton loginButton = new JButton("Log In");
        loginPanel.add(loginButton);

        mainPanel.add(loginPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton registerButton = new JButton("Register as Patient");
        registerButton.addActionListener(e -> showRegistrationDialog());
        buttonPanel.add(registerButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        loginButton.addActionListener(e -> login());

        add(mainPanel);
    }

    private void showRegistrationDialog() {
        RegistrationDialog dialog = new RegistrationDialog(this, userManager);
        dialog.setVisible(true);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        User user = authService.login(username, password);

        if (user == null) {
            JOptionPane.showMessageDialog(
                this,
                "Incorrect username or password, or the account has been disabled.",
                "Login Failed",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        dispose();

        switch (user.getRole()) {
            case PATIENT:
                new PatientFrame(
                    (Patient) user,
                    prescriptionManager,
                    alertManager
                ).setVisible(true);
                break;

            case DOCTOR:
                new DoctorFrame(
                    (Doctor) user,
                    userManager,
                    inventoryManager,
                    prescriptionManager
                ).setVisible(true);
                break;

            case PHARMACIST:
                new PharmacistFrame(
                    (Pharmacist) user,
                    inventoryManager,
                    prescriptionManager,
                    alertManager,
                    reportManager
                ).setVisible(true);
                break;

            case ADMIN:
                new AdminFrame(
                    (Admin) user,
                    userManager,
                    inventoryManager,
                    prescriptionManager,
                    reportManager
                ).setVisible(true);
                break;
        }
    }
}