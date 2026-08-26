package pharmacy.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import pharmacy.manager.UserManager;
import pharmacy.model.Medicine;
import pharmacy.model.Pharmacist;
import pharmacy.model.Pharmacy;
import pharmacy.model.Prescription;
import pharmacy.model.Notification;
import pharmacy.repository.TxtDataStore;
import pharmacy.service.AuthService;

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
        JLabel titleLabel = new JLabel("Pharmacist Workspace - " + pharmacist.getFullName());
        titleLabel.setFont(titleLabel.getFont().deriveFont(16f));
        topPanel.add(titleLabel, BorderLayout.WEST);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        topPanel.add(logoutButton, BorderLayout.EAST);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(2, 3, 10, 10));

        formPanel.add(new JLabel("Select Pharmacy:"));
        pharmacyComboBox = new JComboBox<>();
        for (Pharmacy pharmacy : inventoryManager.getPharmacies()) {
            if (pharmacy.isActive()) {
                pharmacyComboBox.addItem(pharmacy);
            }
        }
        formPanel.add(pharmacyComboBox);

        formPanel.add(new JLabel());

        formPanel.add(new JLabel("Restock Quantity:"));
        restockQuantityField = new JTextField();
        formPanel.add(restockQuantityField);

        JButton addStockButton = new JButton("Restock");
        addStockButton.addActionListener(e -> addStock());
        formPanel.add(addStockButton);

        mainPanel.add(formPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        medicineListModel = new DefaultListModel<>();
        JList<String> medicineList = new JList<>(medicineListModel);
        refreshMedicines();
        centerPanel.add(new JScrollPane(medicineList));

        prescriptionListModel = new DefaultListModel<>();
        prescriptionList = new JList<>(prescriptionListModel);
        refreshPrescriptions();
        centerPanel.add(new JScrollPane(prescriptionList));

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton startButton = new JButton("Start Preparing");
        JButton completeButton = new JButton("Complete Dispensing");
        JButton collectionButton = new JButton("Confirm Collection");
        JButton paymentButton = new JButton("Process Payment");
        JButton inventoryReportButton = new JButton("Inventory Report");
        JButton salesReportButton = new JButton("Sales Report");

        buttonPanel.add(startButton);
        buttonPanel.add(completeButton);
        buttonPanel.add(collectionButton);
        buttonPanel.add(paymentButton);
        buttonPanel.add(inventoryReportButton);
        buttonPanel.add(salesReportButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        startButton.addActionListener(e -> startPreparing());
        completeButton.addActionListener(e -> completeDispensing());
        collectionButton.addActionListener(e -> confirmCollection());
        paymentButton.addActionListener(e -> processPayment());
        inventoryReportButton.addActionListener(e -> showInventoryReport());
        salesReportButton.addActionListener(e -> showSalesReport());

        add(mainPanel);
    }

    private void logout() {
        dispose();
        TxtDataStore dataStore = new TxtDataStore();
        UserManager userManager = new UserManager(dataStore);
        AuthService authService = new AuthService(userManager);
        new LoginFrame(
            authService,
            userManager,
            inventoryManager,
            prescriptionManager,
            alertManager,
            reportManager
        ).setVisible(true);
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
                rx.getStatus() == pharmacy.enumeration.PrescriptionStatus.READY_FOR_COLLECTION ||
                rx.getStatus() == pharmacy.enumeration.PrescriptionStatus.PAYMENT_PENDING) {

                String statusSymbol = "";
                switch (rx.getStatus()) {
                    case PENDING: statusSymbol = "⏳"; break;
                    case PREPARING: statusSymbol = "⚙️"; break;
                    case READY_FOR_COLLECTION: statusSymbol = "✅"; break;
                    case PAYMENT_PENDING: statusSymbol = "💰"; break;
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

    private void addStock() {
        int index = 0;
        if (medicineListModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No medicines available.");
            return;
        }

        String selected = JOptionPane.showInputDialog(
            this,
            "Enter Medicine ID to restock (e.g., MED001):"
        );

        if (selected == null || selected.trim().isEmpty()) {
            return;
        }

        Medicine medicine = inventoryManager.findMedicine(selected.trim());
        if (medicine == null) {
            JOptionPane.showMessageDialog(this, "Medicine not found.");
            return;
        }

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
                rx.getStatus() == pharmacy.enumeration.PrescriptionStatus.READY_FOR_COLLECTION ||
                rx.getStatus() == pharmacy.enumeration.PrescriptionStatus.PAYMENT_PENDING) {
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
            JOptionPane.showMessageDialog(this, 
                "Dispensing completed. The patient may collect the medication.\n" +
                "Patient must confirm collection before payment.");
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

        if (prescription.getStatus() != pharmacy.enumeration.PrescriptionStatus.READY_FOR_COLLECTION) {
            JOptionPane.showMessageDialog(this, 
                "Only prescriptions ready for collection can be confirmed.");
            return;
        }

        try {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Has the patient " + prescription.getPatient().getFullName() + 
                " confirmed collection of their medication?",
                "Patient Collection Confirmation",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                prescriptionManager.patientConfirmsCollection(
                    prescription, 
                    prescription.getPatient()
                );
                refreshPrescriptions();
                JOptionPane.showMessageDialog(this, 
                    "Collection confirmed. Payment is now pending.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void processPayment() {
        Prescription prescription = getSelectedPrescription();
        if (prescription == null) {
            JOptionPane.showMessageDialog(this, "Please select a prescription.");
            return;
        }

        if (prescription.getStatus() != pharmacy.enumeration.PrescriptionStatus.PAYMENT_PENDING) {
            JOptionPane.showMessageDialog(this, 
                "Only prescriptions with pending payment can be processed.");
            return;
        }

        try {
            double amount = prescription.getTotalPrice();
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Process payment for prescription " + prescription.getPrescriptionId() + "\n" +
                "Patient: " + prescription.getPatient().getFullName() + "\n" +
                "Total Amount: RM" + String.format("%.2f", amount),
                "Confirm Payment",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                prescriptionManager.processPayment(prescription, pharmacist);
                refreshPrescriptions();
                JOptionPane.showMessageDialog(this, 
                    "Payment processed successfully!\n" +
                    "Prescription is now marked as DISPENSED and complete.");
            }
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
                "Pharmacist Notification",
                JOptionPane.WARNING_MESSAGE
            );
            alertManager.markAsRead(notification);
        }
    }
}