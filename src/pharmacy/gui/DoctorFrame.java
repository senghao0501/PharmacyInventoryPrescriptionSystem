package pharmacy.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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

import pharmacy.inventory.Medicine;
import pharmacy.manager.AlertManager;
import pharmacy.manager.InventoryManager;
import pharmacy.manager.PrescriptionManager;
import pharmacy.manager.ReportManager;
import pharmacy.manager.UserManager;
import pharmacy.prescription.Prescription;
import pharmacy.prescription.PrescriptionItem;
import pharmacy.repository.TxtDataStore;
import pharmacy.role.Doctor;
import pharmacy.role.Patient;
import pharmacy.service.AuthService;

public class DoctorFrame extends JFrame {
    private Doctor doctor;
    private UserManager userManager;
    private InventoryManager inventoryManager;
    private PrescriptionManager prescriptionManager;

    // Create panel components
    private JComboBox<Patient> patientComboBox;
    private JComboBox<Medicine> medicineComboBox;
    private JTextField quantityField;
    private JTextField dosageField;
    private JTextField remarksField;
    private DefaultListModel<String> itemListModel;
    private List<PrescriptionItem> selectedItems;
    
    // Edit tracking
    private Prescription editingPrescription = null;
    private JButton createButton;
    private JList<String> itemList;
    
    // Cancel/Edit list components
    private DefaultListModel<String> cancelListModel;
    private JList<String> cancelList;

    public DoctorFrame(Doctor doctor, UserManager userManager,
                       InventoryManager inventoryManager,
                       PrescriptionManager prescriptionManager) {
        this.doctor = doctor;
        this.userManager = userManager;
        this.inventoryManager = inventoryManager;
        this.prescriptionManager = prescriptionManager;

        selectedItems = new ArrayList<>();

        setTitle("Doctor Workspace - " + doctor.getFullName());
        setSize(1000, 700);
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
        JLabel titleLabel = new JLabel("Doctor Workspace - " + doctor.getFullName());
        titleLabel.setFont(titleLabel.getFont().deriveFont(16f));
        topPanel.add(titleLabel, BorderLayout.WEST);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        topPanel.add(logoutButton, BorderLayout.EAST);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        centerPanel.add(buildCreatePanel());
        centerPanel.add(buildManagePanel());

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    private JPanel buildCreatePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Create / Edit Prescription"));

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

        panel.add(formPanel, BorderLayout.NORTH);

        itemListModel = new DefaultListModel<>();
        itemList = new JList<>(itemListModel);
        panel.add(new JScrollPane(itemList), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton addButton = new JButton("Add Medicine");
        JButton updateQuantityButton = new JButton("Update Selected Item");
        createButton = new JButton("Create Prescription");
        JButton clearButton = new JButton("Clear Form");
        JButton cancelEditButton = new JButton("Cancel Edit");

        buttonPanel.add(addButton);
        buttonPanel.add(updateQuantityButton);
        buttonPanel.add(createButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(cancelEditButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addMedicine());
        updateQuantityButton.addActionListener(e -> updateSelectedItem());
        createButton.addActionListener(e -> savePrescription());
        clearButton.addActionListener(e -> clearForm());
        cancelEditButton.addActionListener(e -> cancelEdit());

        return panel;
    }

    private void updateSelectedItem() {
        int index = itemList.getSelectedIndex();
        if (index < 0 || index >= selectedItems.size()) {
            JOptionPane.showMessageDialog(this, "Please select an item to update.");
            return;
        }

        PrescriptionItem item = selectedItems.get(index);
        
        try {
            int newQuantity = Integer.parseInt(quantityField.getText().trim());
            if (newQuantity <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero.");
            }

            // Check stock availability
            if (!inventoryManager.hasEnoughStock(item.getMedicine().getMedicineId(), newQuantity)) {
                JOptionPane.showMessageDialog(this, "Insufficient stock: " + item.getMedicine().getName());
                return;
            }

            // Check if this is an existing item being edited
            String dosage = dosageField.getText().trim();
            if (dosage.isEmpty()) {
                dosage = item.getDosageInstructions();
            }

            // Remove old item and add updated one
            selectedItems.remove(index);
            
            // Create new item with updated quantity and dosage
            PrescriptionItem newItem = new PrescriptionItem(
                item.getItemId(),
                newQuantity,
                dosage,
                item.getMedicine()
            );
            selectedItems.add(index, newItem);
            
            // Update display
            refreshItemList();
            
            quantityField.setText("");
            dosageField.setText("");
            
            JOptionPane.showMessageDialog(this, "Item updated successfully!");
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid quantity.");
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    private void refreshItemList() {
        itemListModel.clear();
        for (int i = 0; i < selectedItems.size(); i++) {
            PrescriptionItem item = selectedItems.get(i);
            String suffix = (editingPrescription != null) ? " (Existing)" : "";
            itemListModel.addElement(
                (i + 1) + ". " + item.getMedicine().getName() + 
                " | Qty: " + item.getQuantity() + 
                " | " + item.getDosageInstructions() + suffix
            );
        }
    }

    private JPanel buildManagePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Manage Prescriptions"));

        cancelListModel = new DefaultListModel<>();
        cancelList = new JList<>(cancelListModel);
        refreshManageList();
        panel.add(new JScrollPane(cancelList), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        JButton editButton = new JButton("Edit Selected Prescription");
        JButton cancelButton = new JButton("Cancel Selected Prescription");
        JButton refreshButton = new JButton("Refresh List");

        editButton.addActionListener(e -> editSelectedPrescription());
        cancelButton.addActionListener(e -> cancelSelectedPrescription());
        refreshButton.addActionListener(e -> refreshManageList());

        buttonPanel.add(editButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(refreshButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshManageList() {
        cancelListModel.clear();
        List<Prescription> prescriptions = prescriptionManager.getPrescriptionList();

        for (Prescription rx : prescriptions) {
            if (rx.canEdit() && rx.getPrescribingDoctor().getUserId().equals(doctor.getUserId())) {
                String statusIcon = rx.getStatus() == pharmacy.enumeration.PrescriptionStatus.PENDING ? "⏳" : "⚙️";
                String displayText = String.format(
                    "%s %s | Patient: %s | Status: %s | Date: %s | Total: RM%.2f | Items: %d",
                    statusIcon,
                    rx.getPrescriptionId(),
                    rx.getPatient().getFullName(),
                    rx.getStatus(),
                    rx.getPrescriptionDate(),
                    rx.getTotalPrice(),
                    rx.getItems().size()
                );
                cancelListModel.addElement(displayText);
            }
        }

        if (cancelListModel.isEmpty()) {
            cancelListModel.addElement("No editable prescriptions found.");
        }
    }

    private void editSelectedPrescription() {
        int index = cancelList.getSelectedIndex();
        if (index < 0 || index >= cancelListModel.size()) {
            JOptionPane.showMessageDialog(this, "Please select a prescription to edit.");
            return;
        }

        String selected = cancelListModel.get(index);
        if (selected.equals("No editable prescriptions found.")) {
            return;
        }

        String[] parts = selected.split(" \\| ");
        String rxId = parts[0].trim();
        if (rxId.startsWith("⏳") || rxId.startsWith("⚙️")) {
            rxId = rxId.substring(1).trim();
        }

        Prescription targetRx = null;
        for (Prescription rx : prescriptionManager.getPrescriptionList()) {
            if (rx.getPrescriptionId().equals(rxId)) {
                targetRx = rx;
                break;
            }
        }

        if (targetRx == null) {
            JOptionPane.showMessageDialog(this, "Prescription not found.", "Error", JOptionPane.ERROR_MESSAGE);
            refreshManageList();
            return;
        }

        if (!targetRx.canEdit()) {
            JOptionPane.showMessageDialog(
                this,
                "This prescription is " + targetRx.getStatus() + " and cannot be edited.",
                "Edit Unavailable",
                JOptionPane.WARNING_MESSAGE
            );
            refreshManageList();
            return;
        }

        loadPrescriptionForEdit(targetRx);
    }

    private void loadPrescriptionForEdit(Prescription prescription) {
        clearForm();
        
        editingPrescription = prescription;
        createButton.setText("Update Prescription");
        
        patientComboBox.setSelectedItem(prescription.getPatient());
        patientComboBox.setEnabled(false);
        remarksField.setText(prescription.getRemarks());
        
        // Load items with their original IDs
        for (PrescriptionItem item : prescription.getItems()) {
            selectedItems.add(item);
        }
        refreshItemList();
        
        for (int i = 0; i < cancelListModel.size(); i++) {
            String item = cancelListModel.get(i);
            if (item.contains(prescription.getPrescriptionId())) {
                cancelList.setSelectedIndex(i);
                break;
            }
        }
        
        JOptionPane.showMessageDialog(
            this,
            "Editing: " + prescription.getPrescriptionId() + "\n" +
            "Patient: " + prescription.getPatient().getFullName() + "\n" +
            "Select an item and click 'Update Selected Item' to modify quantity/dosage.",
            "Edit Mode",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void cancelEdit() {
        if (editingPrescription != null) {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Cancel editing " + editingPrescription.getPrescriptionId() + "?",
                "Cancel Edit",
                JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                clearForm();
                patientComboBox.setEnabled(true);
                editingPrescription = null;
                createButton.setText("Create Prescription");
                refreshManageList();
            }
        } else {
            clearForm();
        }
    }

    private void savePrescription() {
        if (editingPrescription != null) {
            updatePrescription();
        } else {
            createPrescription();
        }
    }

    private void updatePrescription() {
        if (editingPrescription == null) {
            return;
        }

        if (selectedItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please add at least one medicine.");
            return;
        }

        try {
            Patient patient = (Patient) patientComboBox.getSelectedItem();
            if (patient == null) {
                JOptionPane.showMessageDialog(this, "Please select a patient.");
                return;
            }

            String remarks = remarksField.getText().trim();

            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Update prescription " + editingPrescription.getPrescriptionId() + "?\n\n" +
                "Patient: " + patient.getFullName() + "\n" +
                "Items: " + selectedItems.size() + "\n" +
                "Total: RM" + String.format("%.2f", calculateTotal()),
                "Confirm Update",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                // Create a new list of items with new IDs to ensure proper saving
                List<PrescriptionItem> updatedItems = new ArrayList<>();
                for (PrescriptionItem item : selectedItems) {
                    // Create new items with new IDs to avoid reference issues
                    PrescriptionItem newItem = new PrescriptionItem(
                        "ITEM" + System.nanoTime(),
                        item.getQuantity(),
                        item.getDosageInstructions(),
                        item.getMedicine()
                    );
                    updatedItems.add(newItem);
                }
                
                prescriptionManager.updatePrescription(editingPrescription, patient, updatedItems, remarks);
                
                JOptionPane.showMessageDialog(
                    this,
                    "Prescription updated successfully!\nID: " + editingPrescription.getPrescriptionId()
                );
                
                clearForm();
                patientComboBox.setEnabled(true);
                editingPrescription = null;
                createButton.setText("Create Prescription");
                refreshManageList();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Update failed: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private double calculateTotal() {
        double total = 0;
        for (PrescriptionItem item : selectedItems) {
            total += item.getSubtotal();
        }
        return total;
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

            clearForm();
            refreshManageList();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                e.getMessage(),
                "Creation Failed",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void clearForm() {
        selectedItems.clear();
        itemListModel.clear();
        quantityField.setText("");
        dosageField.setText("");
        remarksField.setText("");
        patientComboBox.setEnabled(true);
        if (editingPrescription == null) {
            createButton.setText("Create Prescription");
        }
    }

    private void cancelSelectedPrescription() {
        int index = cancelList.getSelectedIndex();
        if (index < 0 || index >= cancelListModel.size()) {
            JOptionPane.showMessageDialog(this, "Please select a prescription to cancel.");
            return;
        }

        String selected = cancelListModel.get(index);
        if (selected.equals("No editable prescriptions found.")) {
            return;
        }

        String[] parts = selected.split(" \\| ");
        String rxId = parts[0].trim();
        if (rxId.startsWith("⏳") || rxId.startsWith("⚙️")) {
            rxId = rxId.substring(1).trim();
        }

        Prescription targetRx = null;
        for (Prescription rx : prescriptionManager.getPrescriptionList()) {
            if (rx.getPrescriptionId().equals(rxId)) {
                targetRx = rx;
                break;
            }
        }

        if (targetRx == null) {
            JOptionPane.showMessageDialog(this, "Prescription not found.", "Error", JOptionPane.ERROR_MESSAGE);
            refreshManageList();
            return;
        }

        if (!targetRx.canCancel()) {
            JOptionPane.showMessageDialog(
                this,
                "This prescription cannot be cancelled. Status: " + targetRx.getStatus(),
                "Cancellation Unavailable",
                JOptionPane.WARNING_MESSAGE
            );
            refreshManageList();
            return;
        }

        StringBuilder details = new StringBuilder();
        details.append("Cancel this prescription?\n\n");
        details.append("ID: ").append(targetRx.getPrescriptionId()).append("\n");
        details.append("Patient: ").append(targetRx.getPatient().getFullName()).append("\n");
        details.append("Status: ").append(targetRx.getStatus()).append("\n");
        details.append("Total: RM").append(String.format("%.2f", targetRx.getTotalPrice())).append("\n\n");
        details.append("Medicines:\n");
        for (PrescriptionItem item : targetRx.getItems()) {
            details.append("  - ").append(item.getMedicine().getName())
                   .append(" x").append(item.getQuantity())
                   .append(" | ").append(item.getDosageInstructions()).append("\n");
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            details.toString(),
            "Confirm Cancellation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            String reason = JOptionPane.showInputDialog(
                this,
                "Cancellation reason:",
                "Reason",
                JOptionPane.QUESTION_MESSAGE
            );

            if (reason == null || reason.trim().isEmpty()) {
                reason = "Cancelled by doctor";
            }

            try {
                prescriptionManager.cancelPrescription(targetRx, reason);
                refreshManageList();
                if (editingPrescription != null && editingPrescription.getPrescriptionId().equals(targetRx.getPrescriptionId())) {
                    clearForm();
                    patientComboBox.setEnabled(true);
                    editingPrescription = null;
                    createButton.setText("Create Prescription");
                }
                JOptionPane.showMessageDialog(
                    this,
                    "Cancelled successfully.",
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

    private void addMedicine() {
        Medicine medicine = (Medicine) medicineComboBox.getSelectedItem();
        if (medicine == null) {
            JOptionPane.showMessageDialog(this, "Please select a medicine.");
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityField.getText().trim());
            String dosage = dosageField.getText().trim();

            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be > 0.");
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
            refreshItemList();

            quantityField.setText("");
            dosageField.setText("");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid quantity.");
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }
}