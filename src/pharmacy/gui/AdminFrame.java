package pharmacy.gui;

import pharmacy.enums.UserRole;
import pharmacy.manager.InventoryManager;
import pharmacy.manager.ReportManager;
import pharmacy.manager.UserManager;
import pharmacy.model.Admin;
import pharmacy.model.User;

import javax.swing.*;
import java.awt.*;

public class AdminFrame extends JFrame {

    private final Admin admin;
    private final UserManager userManager = new UserManager();
    private final InventoryManager inventoryManager = new InventoryManager();
    private final ReportManager reportManager = new ReportManager();
    private final JTextArea output = new JTextArea();

    public AdminFrame(Admin admin) {
        this.admin = admin;
        setTitle("Admin Dashboard - " + admin.getFullName());
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 5, 5));

        JButton users = new JButton("Manage Users");
        JButton addUser = new JButton("Add User");
        JButton disableUser = new JButton("Enable / Disable User");
        JButton resetPassword = new JButton("Reset Password");
        JButton inventory = new JButton("Inventory");
        JButton reports = new JButton("Reports");
        JButton addStock = new JButton("Add Stock");
        JButton logout = new JButton("Logout");

        panel.add(users);
        panel.add(addUser);
        panel.add(disableUser);
        panel.add(resetPassword);
        panel.add(inventory);
        panel.add(reports);
        panel.add(addStock);
        panel.add(logout);

        add(panel, BorderLayout.NORTH);

        output.setEditable(false);
        add(new JScrollPane(output), BorderLayout.CENTER);

        users.addActionListener(e -> showUsers());
        addUser.addActionListener(e -> addUser());
        disableUser.addActionListener(e -> toggleUser());
        resetPassword.addActionListener(e -> resetPassword());
        inventory.addActionListener(e -> showInventory());
        reports.addActionListener(e -> showReports());
        addStock.addActionListener(e -> addStock());
        logout.addActionListener(e -> logout());
    }

    private void showUsers() {
        output.setText("===== USERS =====\n\n");
        for (User user : userManager.getAllUsers()) {
            output.append(user.getUserId() + " | " + user.getUsername() + " | " + user.getFullName() + " | " + user.getRole() + " | Active: " + user.isActive() + "\n");
        }
    }

    private void addUser() {
        String userId = JOptionPane.showInputDialog(this, "User ID:");
        String username = JOptionPane.showInputDialog(this, "Username:");
        String password = JOptionPane.showInputDialog(this, "Password:");
        String fullName = JOptionPane.showInputDialog(this, "Full name:");
        String email = JOptionPane.showInputDialog(this, "Email:");

        UserRole role = (UserRole) JOptionPane.showInputDialog(this, "Role:", "Select Role", JOptionPane.QUESTION_MESSAGE, null, UserRole.values(), UserRole.PATIENT);

        if (userId == null || username == null || password == null || fullName == null || role == null) {
            return;
        }

        boolean success = userManager.addUser(userId, username, password, fullName, email, role);
        JOptionPane.showMessageDialog(this, success ? "User created." : "Unable to create user.");
    }

    private void toggleUser() {
        String userId = JOptionPane.showInputDialog(this, "User ID:");
        if (userId == null) {
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this, "Enable this user?", "Account Status", JOptionPane.YES_NO_OPTION);
        boolean active = choice == JOptionPane.YES_OPTION;
        boolean success = userManager.setUserActive(userId, active);
        JOptionPane.showMessageDialog(this, success ? "Account status updated." : "User not found.");
    }

    private void resetPassword() {
        String userId = JOptionPane.showInputDialog(this, "User ID:");
        String password = JOptionPane.showInputDialog(this, "New password:");
        if (userId == null || password == null) {
            return;
        }

        boolean success = userManager.resetPassword(userId, password);
        JOptionPane.showMessageDialog(this, success ? "Password reset." : "Unable to reset password.");
    }

    private void showInventory() {
        output.setText("===== INVENTORY =====\n\n");
        inventoryManager.getAllMedicines().forEach(medicine -> output.append(medicine.toString() + "\n"));
    }

    private void addStock() {
        String medicineId = JOptionPane.showInputDialog(this, "Medicine ID:");
        String quantityText = JOptionPane.showInputDialog(this, "Quantity:");
        if (medicineId == null || quantityText == null) {
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityText);
            boolean success = inventoryManager.increaseStock(medicineId, quantity, admin.getUserId());
            JOptionPane.showMessageDialog(this, success ? "Stock updated." : "Unable to update stock.");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity.");
        }
    }

    private void showReports() {
        String[] options = {"Inventory Report", "Sales Report"};
        int choice = JOptionPane.showOptionDialog(this, "Select report:", "Reports", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice == 0) {
            output.setText(reportManager.generateInventoryReport().generateReport());
        } else if (choice == 1) {
            String start = JOptionPane.showInputDialog(this, "Start date (YYYY-MM-DD):");
            String end = JOptionPane.showInputDialog(this, "End date (YYYY-MM-DD):");
            if (start != null && end != null) {
                output.setText(reportManager.generateSalesReport(start, end).generateReport());
            }
        }
    }

    private void logout() {
        dispose();
        new LoginFrame().setVisible(true);
    }
}