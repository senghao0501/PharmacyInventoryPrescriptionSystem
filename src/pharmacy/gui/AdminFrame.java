package pharmacy.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import pharmacy.inventory.Medicine;
import pharmacy.manager.AlertManager;
import pharmacy.manager.InventoryManager;
import pharmacy.manager.PrescriptionManager;
import pharmacy.manager.ReportManager;
import pharmacy.manager.UserManager;
import pharmacy.prescription.Prescription;
import pharmacy.repository.TxtDataStore;
import pharmacy.role.Admin;
import pharmacy.role.User;
import pharmacy.service.AuthService;

public class AdminFrame extends JFrame {
    private Admin admin;
    private UserManager userManager;
    private InventoryManager inventoryManager;
    private PrescriptionManager prescriptionManager;
    private ReportManager reportManager;

    private DefaultListModel<String> userListModel;
    private JList<String> userList;

    private DefaultListModel<String> medicineListModel;
    private JList<String> medicineList;
    private JTextField medicineNameField;
    private JTextField medicinePriceField;
    private JTextField medicineStockField;
    private JTextField medicineThresholdField;
    private Medicine selectedMedicine;

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
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                logout();
            }
        });
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Welcome back, Administrator " + admin.getFullName() + "!");
        titleLabel.setFont(titleLabel.getFont().deriveFont(18f));
        topPanel.add(titleLabel, BorderLayout.WEST);

        JPanel topRightPanel = new JPanel();
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        topRightPanel.add(logoutButton);
        topPanel.add(topRightPanel, BorderLayout.EAST);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 3, 10, 10));

        centerPanel.add(buildUserPanel());
        centerPanel.add(buildMedicinePanel());
        centerPanel.add(buildPrescriptionPanel());

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    private void logout() {
        dispose();
        TxtDataStore dataStore = new TxtDataStore();
        UserManager newUserManager = new UserManager(dataStore);
        InventoryManager newInventoryManager = new InventoryManager(dataStore);
        AlertManager newAlertManager = new AlertManager(dataStore);
        ReportManager newReportManager = new ReportManager();
        AuthService authService = new AuthService(newUserManager);
        
        new LoginFrame(
            authService,
            newUserManager,
            newInventoryManager,
            prescriptionManager,
            newAlertManager,
            newReportManager
        ).setVisible(true);
    }

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
        return parts[0].substring(2);
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

        pharmacy.role.User newUser = null;
        switch (role) {
            case PATIENT:
                newUser = new pharmacy.role.Patient(
                    userId, username, password, fullName,
                    "", "", true,
                    "MRN" + System.currentTimeMillis(),
                    null, ""
                );
                break;
            case DOCTOR:
                newUser = new pharmacy.role.Doctor(
                    userId, username, password, fullName,
                    "", "", true,
                    "LIC" + System.currentTimeMillis(),
                    "General Practice", "Outpatient Department"
                );
                break;
            case PHARMACIST:
                newUser = new pharmacy.role.Pharmacist(
                    userId, username, password, fullName,
                    "", "", true,
                    "PHARM" + System.currentTimeMillis(),
                    "Morning Shift"
                );
                break;
            case ADMIN:
                newUser = new pharmacy.role.Admin(
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

        medicineList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    loadSelectedMedicine();
                }
            }
        });

        panel.add(new JScrollPane(medicineList), BorderLayout.CENTER);

        // Form Panel
        JPanel formPanel = new JPanel(new BorderLayout(5, 5));
        
        JPanel fieldsPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        fieldsPanel.add(new JLabel("Medicine Name:"));
        medicineNameField = new JTextField();
        fieldsPanel.add(medicineNameField);

        fieldsPanel.add(new JLabel("Price (RM) - Leave blank:"));
        medicinePriceField = new JTextField();
        fieldsPanel.add(medicinePriceField);

        fieldsPanel.add(new JLabel("Stock Quantity - Leave blank:"));
        medicineStockField = new JTextField();
        fieldsPanel.add(medicineStockField);

        fieldsPanel.add(new JLabel("Min Threshold - Leave blank:"));
        medicineThresholdField = new JTextField();
        fieldsPanel.add(medicineThresholdField);
        
        formPanel.add(fieldsPanel, BorderLayout.CENTER);

        // Button Panel with FlowLayout
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton updateMedicineButton = new JButton("Update Medicine");
        JButton addMedicineButton = new JButton("Add Medicine");
        JButton refreshMedButton = new JButton("Refresh List");

        updateMedicineButton.addActionListener(e -> updateMedicine());
        addMedicineButton.addActionListener(e -> addMedicine());
        refreshMedButton.addActionListener(e -> refreshMedicineList());

        buttonPanel.add(updateMedicineButton);
        buttonPanel.add(addMedicineButton);
        buttonPanel.add(refreshMedButton);
        formPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(formPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void loadSelectedMedicine() {
        int index = medicineList.getSelectedIndex();
        if (index < 0) {
            return;
        }

        String item = medicineListModel.get(index);
        String[] parts = item.split(" \\| ");
        String medId = parts[0].trim();

        selectedMedicine = inventoryManager.findMedicine(medId);
        if (selectedMedicine == null) {
            return;
        }

        medicineNameField.setText(selectedMedicine.getName());
        medicinePriceField.setText("");
        medicineStockField.setText("");
        medicineThresholdField.setText("");
    }

    private void updateMedicine() {
        if (selectedMedicine == null) {
            JOptionPane.showMessageDialog(this, "Please select a medicine first.");
            return;
        }

        try {
            boolean updated = false;

            String priceStr = medicinePriceField.getText().trim();
            if (!priceStr.isEmpty()) {
                double price = Double.parseDouble(priceStr);
                if (price < 0) {
                    JOptionPane.showMessageDialog(this, "Price cannot be negative.");
                    return;
                }
                updated = true;
            }

            String stockStr = medicineStockField.getText().trim();
            if (!stockStr.isEmpty()) {
                int stock = Integer.parseInt(stockStr);
                if (stock < 0) {
                    JOptionPane.showMessageDialog(this, "Stock cannot be negative.");
                    return;
                }
                updated = true;
            }

            String thresholdStr = medicineThresholdField.getText().trim();
            if (!thresholdStr.isEmpty()) {
                int threshold = Integer.parseInt(thresholdStr);
                if (threshold < 0) {
                    JOptionPane.showMessageDialog(this, "Threshold cannot be negative.");
                    return;
                }
                updated = true;
            }

            if (!updated) {
                JOptionPane.showMessageDialog(this, "No changes made. Fill in fields to update.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Update: " + selectedMedicine.getName() + " (" + selectedMedicine.getMedicineId() + ")?",
                "Confirm Update",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                inventoryManager.updateMedicine(
                    selectedMedicine.getMedicineId(),
                    medicineNameField.getText().trim(),
                    medicinePriceField.getText().trim(),
                    medicineStockField.getText().trim(),
                    medicineThresholdField.getText().trim()
                );
                
                refreshMedicineList();
                selectedMedicine = null;
                medicineNameField.setText("");
                medicinePriceField.setText("");
                medicineStockField.setText("");
                medicineThresholdField.setText("");
                
                JOptionPane.showMessageDialog(this, "Medicine updated!");
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Update failed: " + e.getMessage());
        }
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
                " | RM" + med.getUnitPrice() +
                " | Threshold: " + med.getMinThresholdQuantity() +
                lowStock + status
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
            int threshold = Integer.parseInt(medicineThresholdField.getText().trim());

            if (price < 0 || stock < 0 || threshold < 0) {
                JOptionPane.showMessageDialog(this, "Values cannot be negative.");
                return;
            }

            String medId = "MED" + System.currentTimeMillis();
            Medicine newMed = new Medicine(
                medId, name,
                pharmacy.enumeration.MedicineCategory.OVER_THE_COUNTER,
                price, stock, threshold, new java.util.Date(), true
            );

            inventoryManager.getMedicineInventory().add(newMed);
            inventoryManager.addStock(medId, 0, admin.getUserId());

            refreshMedicineList();
            medicineNameField.setText("");
            medicinePriceField.setText("");
            medicineStockField.setText("");
            medicineThresholdField.setText("");

            JOptionPane.showMessageDialog(this, "Medicine added! ID: " + medId);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private JPanel buildPrescriptionPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Prescriptions and Reports"));

        prescriptionListModel = new DefaultListModel<>();
        prescriptionList = new JList<>(prescriptionListModel);
        refreshPrescriptionList();

        panel.add(new JScrollPane(prescriptionList), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 5, 5));

        JButton refreshRxButton = new JButton("Refresh Prescriptions");
        JButton inventoryReportButton = new JButton("Inventory Report");
        JButton salesReportButton = new JButton("Sales Report");

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
            String statusSymbol = "";
            switch (rx.getStatus()) {
                case PENDING: statusSymbol = "⏳"; break;
                case PREPARING: statusSymbol = "⚙️"; break;
                case READY_FOR_COLLECTION: statusSymbol = "✅"; break;
                case PAYMENT_PENDING: statusSymbol = "💰"; break;
                case DISPENSED: statusSymbol = "📦"; break;
                case CANCELLED: statusSymbol = "❌"; break;
            }
            prescriptionListModel.addElement(
                statusSymbol + " " + rx.getPrescriptionId() + " | " +
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