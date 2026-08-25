package pharmacy.gui;

import java.awt.GridLayout;
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
        setSize(450, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        buildUI();
    }

    private void buildUI() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        panel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        panel.add(new JLabel());
        JButton loginButton = new JButton("Log In");
        panel.add(loginButton);

        loginButton.addActionListener(e -> login());

        add(panel);
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
