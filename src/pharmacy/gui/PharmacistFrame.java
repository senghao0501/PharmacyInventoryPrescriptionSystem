package pharmacy.gui;

import pharmacy.manager.InventoryManager;
import pharmacy.manager.PrescriptionManager;
import pharmacy.manager.ReportManager;
import pharmacy.model.Medicine;
import pharmacy.model.Pharmacist;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class PharmacistFrame extends JFrame {

    private final Pharmacist pharmacist;
    private final PrescriptionManager prescriptionManager = new PrescriptionManager();
    private final InventoryManager inventoryManager = new InventoryManager();
    private final ReportManager reportManager = new ReportManager();
    private final JTextArea output = new JTextArea();

    public PharmacistFrame(Pharmacist pharmacist) {
        this.pharmacist = pharmacist;
        setTitle("Pharmacist Dashboard - " + pharmacist.getFullName());
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        JPanel panel = new JPanel(new GridLayout(2, 5, 5, 5));

        JButton pending = new JButton("Pending Prescriptions");
        JButton preparing = new JButton("Start Preparing");
        JButton dispense = new JButton("Dispense");
        JButton collection = new JButton("Confirm Collection");
        JButton cancel = new JButton("Cancel Prescription");
        JButton inventory = new JButton("Inventory");
        JButton addStock = new JButton("Add Stock");
        JButton report = new JButton("Reports");
        JButton logout = new JButton("Logout");

        panel.add(pending);
        panel.add(preparing);
        panel.add(dispense);
        panel.add(collection);
        panel.add(cancel);
        panel.add(inventory);
        panel.add(addStock);
        panel.add(report);
        panel.add(logout);

        add(panel, BorderLayout.NORTH);

        output.setEditable(false);
        add(new JScrollPane(output), BorderLayout.CENTER);

        pending.addActionListener(e -> showPending());
        preparing.addActionListener(e -> startPreparing());
        dispense.addActionListener(e -> dispense());
        collection.addActionListener(e -> completeCollection());
        cancel.addActionListener(e -> cancelPrescription());
        inventory.addActionListener(e -> showInventory());
        addStock.addActionListener(e -> addStock());
        report.addActionListener(e -> showReports());
        logout.addActionListener(e -> logout());
    }

    private void showPending() {
        String sql = "SELECT prescription_id, patient_id, prescribing_doctor_id, total_price FROM prescriptions WHERE status = 'PENDING' ORDER BY prescription_date";

        try (var conn = pharmacy.database.DatabaseConnection.getConnection();
             var ps = conn.prepareStatement(sql);
             var rs = ps.executeQuery()) {

            output.setText("===== PENDING PRESCRIPTIONS =====\n\n");

            while (rs.next()) {
                output.append(rs.getString("prescription_id") + " | Patient: " + rs.getString("patient_id") + " | Doctor: " + rs.getString("prescribing_doctor_id") + " | RM " + rs.getDouble("total_price") + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startPreparing() {
        String prescriptionId = JOptionPane.showInputDialog(this, "Prescription ID:");
        if (prescriptionId == null) {
            return;
        }

        boolean success = prescriptionManager.startPreparing(prescriptionId, pharmacist.getUserId());
        JOptionPane.showMessageDialog(this, success ? "Prescription is now Preparing." : "Unable to start preparing.");
    }

    private void dispense() {
        String prescriptionId = JOptionPane.showInputDialog(this, "Prescription ID:");
        if (prescriptionId == null) {
            return;
        }

        try {
            boolean success = prescriptionManager.dispensePrescription(prescriptionId, pharmacist.getUserId());
            JOptionPane.showMessageDialog(this, success ? "Dispensing completed.\nPatient has been notified." : "Dispensing failed.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void completeCollection() {
        String prescriptionId = JOptionPane.showInputDialog(this, "Prescription ID:");
        if (prescriptionId == null) {
            return;
        }

        boolean success = prescriptionManager.completeCollection(prescriptionId, pharmacist.getUserId());
        JOptionPane.showMessageDialog(this, success ? "Collection confirmed.\nSales transaction recorded." : "Unable to confirm collection.");
    }

    // 新增：药剂师取消处方
    private void cancelPrescription() {
        String prescriptionId = JOptionPane.showInputDialog(this, "Prescription ID:");
        if (prescriptionId == null) {
            return;
        }
        
        String reason = JOptionPane.showInputDialog(this, "Cancellation reason:");
        if (reason == null) {
            return;
        }
        
        boolean success = prescriptionManager.cancelPrescription(prescriptionId, pharmacist.getUserId(), reason);
        JOptionPane.showMessageDialog(this, success ? "Prescription cancelled." : "Unable to cancel. It may already be dispensed.");
    }

    private void showInventory() {
        List<Medicine> medicines = inventoryManager.getAllMedicines();
        output.setText("===== INVENTORY =====\n\n");

        for (Medicine medicine : medicines) {
            output.append(medicine.toString() + "\n");
        }
    }

    private void addStock() {
        String medicineId = JOptionPane.showInputDialog(this, "Medicine ID:");
        if (medicineId == null) {
            return;
        }

        String quantityText = JOptionPane.showInputDialog(this, "Quantity to add:");
        if (quantityText == null) {
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityText);
            boolean success = inventoryManager.increaseStock(medicineId, quantity, pharmacist.getUserId());
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
            String start = JOptionPane.showInputDialog(this, "Start date (YYYY-MM-DD):", LocalDate.now().minusDays(30).toString());
            String end = JOptionPane.showInputDialog(this, "End date (YYYY-MM-DD):", LocalDate.now().toString());

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