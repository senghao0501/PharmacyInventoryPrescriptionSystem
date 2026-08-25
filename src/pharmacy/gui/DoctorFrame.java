package pharmacy.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
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
import pharmacy.manager.UserManager;
import pharmacy.model.Doctor;
import pharmacy.model.Medicine;
import pharmacy.model.Patient;
import pharmacy.model.Prescription;
import pharmacy.model.PrescriptionItem;

public class DoctorFrame extends JFrame {
    private Doctor doctor;
    private UserManager userManager;
    private InventoryManager inventoryManager;
    private PrescriptionManager prescriptionManager;

    private JComboBox<Patient> patientComboBox;
    private JComboBox<Medicine> medicineComboBox;
    private JTextField quantityField;
    private JTextField dosageField;
    private JTextField remarksField;
    private DefaultListModel<String> itemListModel;
    private List<PrescriptionItem> selectedItems;

    public DoctorFrame(Doctor doctor, UserManager userManager,
                       InventoryManager inventoryManager,
                       PrescriptionManager prescriptionManager) {
        this.doctor = doctor;
        this.userManager = userManager;
        this.inventoryManager = inventoryManager;
        this.prescriptionManager = prescriptionManager;

        selectedItems = new ArrayList<>();

        setTitle("Doctor Workspace - " + doctor.getFullName());
        setSize(850, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        buildUI();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top: prescription form
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));

        formPanel.add(new JLabel("Select Patient:"));
        patientComboBox = new JComboBox<>();
        for (Patient patient : userManager.getPatients()) {
            patientComboBox.addItem(patient);
        }
        formPanel.add(patientComboBox);

        formPanel.add(new JLabel("Select Medicine:"));
        medicineComboBox = new JComboBox<>();
        for (Medicine medicine : inventoryManager.getMedicineInventory()) {
            if (medicine.isActive()) {
                medicineComboBox.addItem(medicine);
            }
        }
        formPanel.add(medicineComboBox);

        formPanel.add(new JLabel("Quantity:"));
        quantityField = new JTextField();
        formPanel.add(quantityField);

        formPanel.add(new JLabel("Dosage Instructions:"));
        dosageField = new JTextField();
        formPanel.add(dosageField);

        formPanel.add(new JLabel("Remarks:"));
        remarksField = new JTextField();
        formPanel.add(remarksField);

        mainPanel.add(formPanel, BorderLayout.NORTH);

        // Centre: selected medicines
        itemListModel = new DefaultListModel<>();
        JList<String> itemList = new JList<>(itemListModel);
        mainPanel.add(new JScrollPane(itemList), BorderLayout.CENTER);

        // Bottom: actions
        JPanel buttonPanel = new JPanel();

        JButton addButton = new JButton("Add Medicine");
        JButton createButton = new JButton("Create Prescription");
        JButton clearButton = new JButton("Clear List");
        JButton cancelButton = new JButton("Cancel Prescription");

        buttonPanel.add(addButton);
        buttonPanel.add(createButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addMedicine());
        createButton.addActionListener(e -> createPrescription());
        clearButton.addActionListener(e -> clearItems());
        cancelButton.addActionListener(e -> cancelPrescription());

        add(mainPanel);
    }

    private void addMedicine() {
        Medicine medicine = (Medicine) medicineComboBox.getSelectedItem();
        if (medicine == null) {
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            String dosage = dosageField.getText().trim();

            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero.");
            }
            if (dosage.isEmpty()) {
                throw new IllegalArgumentException("Please enter dosage instructions.");
            }

            if (!inventoryManager.hasEnoughStock(medicine.getMedicineId(), quantity)) {
                JOptionPane.showMessageDialog(this, "Insufficient stock: " + medicine.getName());
                return;
            }

            PrescriptionItem item = new PrescriptionItem(
                "ITEM" + System.nanoTime(),
                quantity,
                dosage,
                medicine
            );

            selectedItems.add(item);
            itemListModel.addElement(
                medicine.getName() + " | Quantity: " + quantity + " | " + dosage
            );

            quantityField.setText("");
            dosageField.setText("");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid quantity.");
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void createPrescription() {
        Patient patient = (Patient) patientComboBox.getSelectedItem();
        if (patient == null) {
            JOptionPane.showMessageDialog(this, "Please select a patient.");
            return;
        }

        if (selectedItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please add at least one medicine.");
            return;
        }

        try {
            String remarks = remarksField.getText().trim();

            Prescription prescription = prescriptionManager.createPrescription(
                patient, doctor, selectedItems, remarks
            );

            JOptionPane.showMessageDialog(
                this,
                "Prescription created successfully!\n\nPrescription ID: " + prescription.getPrescriptionId()
            );

            clearItems();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                e.getMessage(),
                "Creation Failed",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void clearItems() {
        selectedItems.clear();
        itemListModel.clear();
        quantityField.setText("");
        dosageField.setText("");
        remarksField.setText("");
    }

    private void cancelPrescription() {
        String rxId = JOptionPane.showInputDialog(
            this,
            "Enter the prescription ID to cancel:",
            "Cancel Prescription",
            JOptionPane.QUESTION_MESSAGE
        );

        if (rxId == null || rxId.trim().isEmpty()) {
            return;
        }

        Prescription targetRx = null;
        for (Prescription rx : prescriptionManager.getPrescriptionList()) {
            if (rx.getPrescriptionId().equals(rxId.trim())) {
                targetRx = rx;
                break;
            }
        }

        if (targetRx == null) {
            JOptionPane.showMessageDialog(this, "Prescription not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Ensure that this prescription was issued by the current doctor.
        if (!targetRx.getPrescribingDoctor().getUserId().equals(doctor.getUserId())) {
            JOptionPane.showMessageDialog(
                this,
                "You can only cancel prescriptions that you issued.",
                "Insufficient Permission",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (!targetRx.canCancel()) {
            JOptionPane.showMessageDialog(
                this,
                "This prescription is " + targetRx.getStatus() + " and cannot be cancelled.\n" +
                "Only pending or preparing prescriptions can be cancelled.",
                "Cancellation Unavailable",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to cancel prescription " + rxId + "?\n" +
            "Patient: " + targetRx.getPatient().getFullName() + "\n" +
            "Status: " + targetRx.getStatus(),
            "Confirm Cancellation",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            String reason = JOptionPane.showInputDialog(
                this,
                "Enter the cancellation reason:",
                "Cancellation Reason",
                JOptionPane.QUESTION_MESSAGE
            );

            if (reason == null || reason.trim().isEmpty()) {
                reason = "Cancelled by doctor";
            }

            try {
                prescriptionManager.cancelPrescription(targetRx, reason);
                JOptionPane.showMessageDialog(
                    this,
                    "Prescription cancelled.\nReason: " + reason,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                    this,
                    "Cancellation failed: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
}
