package pharmacy.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import pharmacy.manager.InventoryManager;
import pharmacy.manager.PrescriptionManager;
import pharmacy.manager.ReportManager;
import pharmacy.manager.UserManager;
import pharmacy.model.Admin;
import pharmacy.model.Medicine;
import pharmacy.model.Prescription;
import pharmacy.model.User;

public class AdminFrame extends JFrame {
    private Admin admin;
    private UserManager userManager;
    private InventoryManager inventoryManager;
    private PrescriptionManager prescriptionManager;
    private ReportManager reportManager;

    // User-management components
    private DefaultListModel<String> userListModel;
    private JList<String> userList;

    // Medicine-management components
    private DefaultListModel<String> medicineListModel;
    private JList<String> medicineList;
    private JTextField medicineNameField;
    private JTextField medicinePriceField;
    private JTextField medicineStockField;

    // Prescription-viewing components
    private DefaultListModel<String> prescriptionListModel;
    private JList<String> prescriptionList;

    public AdminFrame(Admin admin, UserManager userManager,
                      InventoryManager inventoryManager,
                      PrescriptionManager prescriptionManager,
                      ReportManager reportManager) {
        this.admin = admin;
        this.userManager = userManager;
        this.inventoryManager = inventoryManager;
        this.prescriptionManager = prescriptionManager;
        this.reportManager = reportManager;

        setTitle("Administrator Dashboard - " + admin.getFullName());
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        buildUI();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        JLabel titleLabel = new JLabel("Welcome back, Administrator " + admin.getFullName() + "!");
        titleLabel.setFont(titleLabel.getFont().deriveFont(18f));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Main area: three columns
        JPanel centerPanel = new JPanel(new GridLayout(1, 3, 10, 10));

        // Left: user management
        centerPanel.add(buildUserPanel());

        // Centre: medicine management
        centerPanel.add(buildMedicinePanel());

        // Right: prescriptions and reports
        centerPanel.add(buildPrescriptionPanel());

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    // ==================== USER MANAGEMENT ====================
    private JPanel buildUserPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("User Management"));

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        refreshUserList();

        panel.add(new JScrollPane(userList), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 5, 5));

        JButton disableButton = new JButton("Disable User");
        JButton enableButton = new JButton("Enable User");
        JButton resetPassButton = new JButton("Reset Password");
        JButton addUserButton = new JButton("Add User");

        disableButton.addActionListener(e -> disableUser());
        enableButton.addActionListener(e -> enableUser());
        resetPassButton.addActionListener(e -> resetPassword());
        addUserButton.addActionListener(e -> addUser());

        buttonPanel.add(disableButton);
        buttonPanel.add(enableButton);
        buttonPanel.add(resetPassButton);
        buttonPanel.add(addUserButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshUserList() {
        userListModel.clear();
        List<User> users = userManager.getUserList();
        for (User user : users) {
            String status = user.isActive() ? "✓" : "✗";
            userListModel.addElement(
                status + " " + user.getUserId() + " | " +
                user.getFullName() + " (" + user.getRole() + ")"
            );
        }
    }

    private String getSelectedUserId() {
        int index = userList.getSelectedIndex();
        if (index < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user.");
            return null;
        }
        String item = userListModel.get(index);
        String[] parts = item.split(" \\| ");
        return parts[0].substring(2); // Remove the status symbol.
    }

    private void disableUser() {
        String userId = getSelectedUserId();
        if (userId == null) return;
        try {
            userManager.disableUser(userId);
            refreshUserList();
            JOptionPane.showMessageDialog(this, "User disabled.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void enableUser() {
        String userId = getSelectedUserId();
        if (userId == null) return;
        try {
            userManager.enableUser(userId);
            refreshUserList();
            JOptionPane.showMessageDialog(this, "User enabled.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void resetPassword() {
        String userId = getSelectedUserId();
        if (userId == null) return;

        String newPass = JOptionPane.showInputDialog(this, "Enter the new password:");
        if (newPass == null || newPass.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Password cannot be empty.");
            return;
        }

        try {
            userManager.resetPassword(userId, newPass);
            JOptionPane.showMessageDialog(this, "Password reset successfully.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void addUser() {
        String username = JOptionPane.showInputDialog(this, "Enter username:");
        if (username == null || username.trim().isEmpty()) return;

        String password = JOptionPane.showInputDialog(this, "Enter password:");
        if (password == null || password.trim().isEmpty()) return;

        String fullName = JOptionPane.showInputDialog(this, "Enter full name:");
        if (fullName == null || fullName.trim().isEmpty()) return;

        String[] roles = {"PATIENT", "DOCTOR", "PHARMACIST", "ADMIN"};
        String roleStr = (String) JOptionPane.showInputDialog(
            this, "Select role:", "Add User",
            JOptionPane.QUESTION_MESSAGE, null, roles, roles[0]
        );
        if (roleStr == null) return;

        pharmacy.enumeration.UserRole role = pharmacy.enumeration.UserRole.valueOf(roleStr);
        String userId = "U" + System.currentTimeMillis();

        pharmacy.model.User newUser = null;
        switch (role) {
            case PATIENT:
                newUser = new pharmacy.model.Patient(
                    userId, username, password, fullName,
                    "", "", true,
                    "MRN" + System.currentTimeMillis(),
                    null, ""
                );
                break;
            case DOCTOR:
                newUser = new pharmacy.model.Doctor(
                    userId, username, password, fullName,
                    "", "", true,
                    "LIC" + System.currentTimeMillis(),
                    "General Practice", "Outpatient Department"
                );
                break;
            case PHARMACIST:
                newUser = new pharmacy.model.Pharmacist(
                    userId, username, password, fullName,
                    "", "", true,
                    "PHARM" + System.currentTimeMillis(),
                    "Morning Shift"
                );
                break;
            case ADMIN:
                newUser = new pharmacy.model.Admin(
                    userId, username, password, fullName,
                    "", "", true, "Full Access"
                );
                break;
        }

        try {
            userManager.addUser(newUser);
            refreshUserList();
            JOptionPane.showMessageDialog(this, "User added successfully!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // ==================== MEDICINE MANAGEMENT ====================
    private JPanel buildMedicinePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Medicine Management"));

        medicineListModel = new DefaultListModel<>();
        medicineList = new JList<>(medicineListModel);
        refreshMedicineList();

        panel.add(new JScrollPane(medicineList), BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        formPanel.add(new JLabel("Medicine Name:"));
        medicineNameField = new JTextField();
        formPanel.add(medicineNameField);

        formPanel.add(new JLabel("Price (RM):"));
        medicinePriceField = new JTextField();
        formPanel.add(medicinePriceField);

        formPanel.add(new JLabel("Initial Stock:"));
        medicineStockField = new JTextField();
        formPanel.add(medicineStockField);

        JButton addMedicineButton = new JButton("Add Medicine");
        formPanel.add(addMedicineButton);
        addMedicineButton.addActionListener(e -> addMedicine());

        JButton refreshMedButton = new JButton("Refresh List");
        formPanel.add(refreshMedButton);
        refreshMedButton.addActionListener(e -> refreshMedicineList());

        panel.add(formPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshMedicineList() {
        medicineListModel.clear();
        List<Medicine> medicines = inventoryManager.getMedicineInventory();
        for (Medicine med : medicines) {
            String status = med.isActive() ? "" : " [Inactive]";
            String lowStock = med.isLowStock() ? " ⚠️ Low Stock" : "";
            medicineListModel.addElement(
                med.getMedicineId() + " | " + med.getName() +
                " | Stock: " + med.getStockQuantity() +
                " | RM" + med.getUnitPrice() + lowStock + status
            );
        }
    }

    private void addMedicine() {
        String name = medicineNameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a medicine name.");
            return;
        }

        try {
            double price = Double.parseDouble(medicinePriceField.getText().trim());
            int stock = Integer.parseInt(medicineStockField.getText().trim());

            if (price < 0 || stock < 0) {
                JOptionPane.showMessageDialog(this, "Price and quantity cannot be negative.");
                return;
            }

            String medId = "MED" + System.currentTimeMillis();
            Medicine newMed = new Medicine(
                medId, name,
                pharmacy.enumeration.MedicineCategory.OVER_THE_COUNTER,
                price, stock, 5, new java.util.Date(), true
            );

            inventoryManager.getMedicineInventory().add(newMed);
            inventoryManager.addStock(medId, 0, admin.getUserId()); // Trigger persistence.

            refreshMedicineList();
            medicineNameField.setText("");
            medicinePriceField.setText("");
            medicineStockField.setText("");

            JOptionPane.showMessageDialog(this, "Medicine added successfully! ID: " + medId);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // ==================== PRESCRIPTIONS AND REPORTS ====================
    private JPanel buildPrescriptionPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Prescriptions and Reports"));

        prescriptionListModel = new DefaultListModel<>();
        prescriptionList = new JList<>(prescriptionListModel);
        refreshPrescriptionList();

        panel.add(new JScrollPane(prescriptionList), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 5, 5));

        JButton refreshRxButton = new JButton("Refresh Prescriptions");
        JButton inventoryReportButton = new JButton("Generate Inventory Report");
        JButton salesReportButton = new JButton("Generate Sales Report");

        refreshRxButton.addActionListener(e -> refreshPrescriptionList());
        inventoryReportButton.addActionListener(e -> showInventoryReport());
        salesReportButton.addActionListener(e -> showSalesReport());

        buttonPanel.add(refreshRxButton);
        buttonPanel.add(inventoryReportButton);
        buttonPanel.add(salesReportButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshPrescriptionList() {
        prescriptionListModel.clear();
        List<Prescription> prescriptions = prescriptionManager.getPrescriptionList();
        for (Prescription rx : prescriptions) {
            String status = rx.getStatus().toString();
            prescriptionListModel.addElement(
                rx.getPrescriptionId() + " | " +
                rx.getPatient().getFullName() + " | " +
                status + " | RM" + rx.getTotalPrice()
            );
        }
    }

    private void showInventoryReport() {
        String report = reportManager.generateInventoryReport(inventoryManager).generate();
        JOptionPane.showMessageDialog(this, report, "Inventory Report", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showSalesReport() {
        String report = reportManager.generateSalesReport(prescriptionManager).generate();
        JOptionPane.showMessageDialog(this, report, "Sales Report", JOptionPane.INFORMATION_MESSAGE);
    }
}
