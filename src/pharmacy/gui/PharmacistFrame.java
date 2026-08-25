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

import pharmacy.manager.AlertManager;
import pharmacy.manager.InventoryManager;
import pharmacy.manager.PrescriptionManager;
import pharmacy.manager.ReportManager;
import pharmacy.model.Medicine;
import pharmacy.model.Pharmacist;
import pharmacy.model.Pharmacy;
import pharmacy.model.Prescription;
import pharmacy.model.Notification;

public class PharmacistFrame extends JFrame {
    private Pharmacist pharmacist;
    private InventoryManager inventoryManager;
    private PrescriptionManager prescriptionManager;
    private AlertManager alertManager;
    private ReportManager reportManager;

    private JComboBox<Pharmacy> pharmacyComboBox;
    private DefaultListModel<String> medicineListModel;
    private DefaultListModel<String> prescriptionListModel;
    private JList<String> prescriptionList;
    private JTextField restockQuantityField;

    public PharmacistFrame(Pharmacist pharmacist, InventoryManager inventoryManager,
                           PrescriptionManager prescriptionManager,
                           AlertManager alertManager, ReportManager reportManager) {
        this.pharmacist = pharmacist;
        this.inventoryManager = inventoryManager;
        this.prescriptionManager = prescriptionManager;
        this.alertManager = alertManager;
        this.reportManager = reportManager;

        setTitle("Pharmacist Workspace - " + pharmacist.getFullName());
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        buildUI();
        showNotifications();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top: pharmacy selection and restocking
        JPanel topPanel = new JPanel(new GridLayout(2, 2, 10, 10));

        topPanel.add(new JLabel("Select Pharmacy:"));
        pharmacyComboBox = new JComboBox<>();
        for (Pharmacy pharmacy : inventoryManager.getPharmacies()) {
            if (pharmacy.isActive()) {
                pharmacyComboBox.addItem(pharmacy);
            }
        }
        topPanel.add(pharmacyComboBox);

        topPanel.add(new JLabel("Restock Quantity:"));
        restockQuantityField = new JTextField();
        topPanel.add(restockQuantityField);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Centre: inventory and prescriptions
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        // Left: medicine inventory
        medicineListModel = new DefaultListModel<>();
        JList<String> medicineList = new JList<>(medicineListModel);
        refreshMedicines();
        centerPanel.add(new JScrollPane(medicineList));

        // Right: prescription list
        prescriptionListModel = new DefaultListModel<>();
        prescriptionList = new JList<>(prescriptionListModel);
        refreshPrescriptions();
        centerPanel.add(new JScrollPane(prescriptionList));

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Bottom: actions
        JPanel buttonPanel = new JPanel();

        JButton addStockButton = new JButton("Restock");
        JButton startButton = new JButton("Start Preparing");
        JButton completeButton = new JButton("Complete Dispensing");
        JButton collectionButton = new JButton("Confirm Collection");
        JButton inventoryReportButton = new JButton("Inventory Report");
        JButton salesReportButton = new JButton("Sales Report");

        buttonPanel.add(addStockButton);
        buttonPanel.add(startButton);
        buttonPanel.add(completeButton);
        buttonPanel.add(collectionButton);
        buttonPanel.add(inventoryReportButton);
        buttonPanel.add(salesReportButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        addStockButton.addActionListener(e -> addStock(medicineList));
        startButton.addActionListener(e -> startPreparing());
        completeButton.addActionListener(e -> completeDispensing());
        collectionButton.addActionListener(e -> confirmCollection());
        inventoryReportButton.addActionListener(e -> showInventoryReport());
        salesReportButton.addActionListener(e -> showSalesReport());

        add(mainPanel);
    }

    private void refreshMedicines() {
        medicineListModel.clear();
        for (Medicine medicine : inventoryManager.getMedicineInventory()) {
            String status = medicine.isLowStock() ? " ⚠️ Low Stock" : "";
            medicineListModel.addElement(
                medicine.getMedicineId() + " | " + medicine.getName() +
                " | Stock: " + medicine.getStockQuantity() +
                " | RM" + medicine.getUnitPrice() + status
            );
        }
    }

    private void refreshPrescriptions() {
        prescriptionListModel.clear();
        List<Prescription> prescriptions = prescriptionManager.getPrescriptionList();

        for (Prescription rx : prescriptions) {
            if (rx.getStatus() == pharmacy.enumeration.PrescriptionStatus.PENDING ||
                rx.getStatus() == pharmacy.enumeration.PrescriptionStatus.PREPARING ||
                rx.getStatus() == pharmacy.enumeration.PrescriptionStatus.READY_FOR_COLLECTION) {

                String statusSymbol = "";
                switch (rx.getStatus()) {
                    case PENDING: statusSymbol = "⏳"; break;
                    case PREPARING: statusSymbol = "⚙️"; break;
                    case READY_FOR_COLLECTION: statusSymbol = "✅"; break;
                    default: break;
                }

                prescriptionListModel.addElement(
                    statusSymbol + " " + rx.getPrescriptionId() +
                    " | Patient: " + rx.getPatient().getFullName() +
                    " | Status: " + rx.getStatus() +
                    " | Medicines: " + rx.getItems().size()
                );
            }
        }
    }

    private void addStock(JList<String> medicineList) {
        int index = medicineList.getSelectedIndex();
        if (index < 0) {
            JOptionPane.showMessageDialog(this, "Please select a medicine first.");
            return;
        }

        Medicine medicine = inventoryManager.getMedicineInventory().get(index);

        try {
            int quantity = Integer.parseInt(restockQuantityField.getText().trim());
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero.");
            }

            inventoryManager.addStock(medicine.getMedicineId(), quantity, pharmacist.getUserId());
            refreshMedicines();
            restockQuantityField.setText("");

            JOptionPane.showMessageDialog(this, "Restocking successful!\n" +
                medicine.getName() + " increased by " + quantity + " units.");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid quantity.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private Prescription getSelectedPrescription() {
        int index = prescriptionList.getSelectedIndex();
        if (index < 0) {
            return null;
        }

        List<Prescription> prescriptions = prescriptionManager.getPrescriptionList();
        int count = 0;
        for (Prescription rx : prescriptions) {
            if (rx.getStatus() == pharmacy.enumeration.PrescriptionStatus.PENDING ||
                rx.getStatus() == pharmacy.enumeration.PrescriptionStatus.PREPARING ||
                rx.getStatus() == pharmacy.enumeration.PrescriptionStatus.READY_FOR_COLLECTION) {
                if (count == index) {
                    return rx;
                }
                count++;
            }
        }
        return null;
    }

    private void startPreparing() {
        Prescription prescription = getSelectedPrescription();
        if (prescription == null) {
            JOptionPane.showMessageDialog(this, "Please select a prescription.");
            return;
        }

        try {
            prescriptionManager.startPreparing(prescription, pharmacist);
            refreshPrescriptions();
            JOptionPane.showMessageDialog(this, "Medication preparation started.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void completeDispensing() {
        Prescription prescription = getSelectedPrescription();
        if (prescription == null) {
            JOptionPane.showMessageDialog(this, "Please select a prescription.");
            return;
        }

        try {
            prescriptionManager.completeDispensing(prescription, pharmacist);
            refreshPrescriptions();
            refreshMedicines();
            JOptionPane.showMessageDialog(this, "Dispensing completed. The patient may collect the medication.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void confirmCollection() {
        Prescription prescription = getSelectedPrescription();
        if (prescription == null) {
            JOptionPane.showMessageDialog(this, "Please select a prescription.");
            return;
        }

        try {
            prescriptionManager.confirmCollection(prescription, pharmacist);
            refreshPrescriptions();
            JOptionPane.showMessageDialog(this, "Collection confirmed. The prescription is complete.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
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

    private void showNotifications() {
        List<Notification> notifications = alertManager.getNotificationsForRole("PHARMACIST");
        for (Notification notification : notifications) {
            JOptionPane.showMessageDialog(
                this,
                notification.getMessage(),
                "Pharmacist Notifications",
                JOptionPane.WARNING_MESSAGE
            );
            alertManager.markAsRead(notification);
        }
    }
}
