package pharmacy.gui;

import java.awt.BorderLayout;
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
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import pharmacy.manager.AlertManager;
import pharmacy.manager.InventoryManager;
import pharmacy.manager.PrescriptionManager;
import pharmacy.manager.ReportManager;
import pharmacy.manager.UserManager;
import pharmacy.model.Notification;
import pharmacy.model.Patient;
import pharmacy.model.Prescription;
import pharmacy.model.PrescriptionItem;
import pharmacy.repository.TxtDataStore;
import pharmacy.service.AuthService;

public class PatientFrame extends JFrame {
    private Patient patient;
    private PrescriptionManager prescriptionManager;
    private AlertManager alertManager;
    
    private DefaultListModel<String> prescriptionListModel;
    private JList<String> prescriptionList;
    private JLabel statusLabel;
    private JButton collectionButton;
    private JButton paymentButton;
    private JButton detailButton;
    private List<Prescription> currentPrescriptions;

    public PatientFrame(Patient patient, PrescriptionManager prescriptionManager,
                        AlertManager alertManager) {
        this.patient = patient;
        this.prescriptionManager = prescriptionManager;
        this.alertManager = alertManager;

        setTitle("Patient Workspace - " + patient.getFullName());
        setSize(850, 600);
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

        // Top Panel - Welcome and Logout
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JLabel("Welcome back, " + patient.getFullName()), BorderLayout.WEST);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        topPanel.add(logoutButton, BorderLayout.EAST);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Center Panel - Split into two sections
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.7);

        // Top section - Prescription List
        JPanel listPanel = new JPanel(new BorderLayout(5, 5));
        listPanel.setBorder(BorderFactory.createTitledBorder("My Prescriptions"));

        prescriptionListModel = new DefaultListModel<>();
        prescriptionList = new JList<>(prescriptionListModel);
        listPanel.add(new JScrollPane(prescriptionList), BorderLayout.CENTER);

        // Bottom section - Action Buttons
        JPanel actionPanel = new JPanel(new BorderLayout(5, 5));
        actionPanel.setBorder(BorderFactory.createTitledBorder("Actions"));

        // Status label
        statusLabel = new JLabel("Select a prescription to perform actions");
        actionPanel.add(statusLabel, BorderLayout.NORTH);

        // Buttons
        collectionButton = new JButton("Confirm Collection");
        paymentButton = new JButton("Make Payment");
        detailButton = new JButton("View Details");

        collectionButton.addActionListener(e -> confirmCollection());
        paymentButton.addActionListener(e -> makePayment());
        detailButton.addActionListener(e -> showPrescriptionDetail());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(collectionButton);
        buttonPanel.add(paymentButton);
        buttonPanel.add(detailButton);

        actionPanel.add(buttonPanel, BorderLayout.CENTER);

        // Add to split pane
        splitPane.setTopComponent(listPanel);
        splitPane.setBottomComponent(actionPanel);

        mainPanel.add(splitPane, BorderLayout.CENTER);
        add(mainPanel);

        // Add selection listener to update buttons when user selects a prescription
        prescriptionList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    updateButtonStates();
                }
            }
        });

        // Refresh prescriptions
        refreshPrescriptions();
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

    private void refreshPrescriptions() {
        if (prescriptionListModel == null) {
            return;
        }
        
        prescriptionListModel.clear();
        currentPrescriptions = prescriptionManager.getPatientPrescriptions(
            patient.getUserId()
        );

        // Sort by date (newest first)
        currentPrescriptions.sort((p1, p2) -> p2.getPrescriptionDate().compareTo(p1.getPrescriptionDate()));

        for (Prescription rx : currentPrescriptions) {
            String statusSymbol = "";
            String actionHint = "";
            
            switch (rx.getStatus()) {
                case PENDING: 
                    statusSymbol = "⏳ "; 
                    actionHint = " (Waiting for pharmacist)";
                    break;
                case PREPARING: 
                    statusSymbol = "⚙️ "; 
                    actionHint = " (Being prepared)";
                    break;
                case READY_FOR_COLLECTION: 
                    statusSymbol = "✅ "; 
                    actionHint = " ★ READY FOR COLLECTION";
                    break;
                case PAYMENT_PENDING: 
                    statusSymbol = "💰 "; 
                    actionHint = " ★ PAYMENT PENDING";
                    break;
                case DISPENSED: 
                    statusSymbol = "📦 "; 
                    actionHint = " (Completed)";
                    break;
                case CANCELLED: 
                    statusSymbol = "❌ "; 
                    actionHint = " (Cancelled)";
                    break;
            }

            String displayText = String.format(
                "%s%s | %s | %s%s | Medicines: %d | Total: RM%.2f",
                statusSymbol,
                rx.getPrescriptionId(),
                rx.getPrescriptionDate(),
                rx.getStatus(),
                actionHint,
                rx.getItems().size(),
                rx.getTotalPrice()
            );

            prescriptionListModel.addElement(displayText);
        }

        // If list is empty, reset button states
        if (prescriptionListModel.isEmpty()) {
            collectionButton.setEnabled(false);
            paymentButton.setEnabled(false);
            detailButton.setEnabled(false);
            statusLabel.setText("No prescriptions found.");
        } else {
            // Select first item
            prescriptionList.setSelectedIndex(0);
            updateButtonStates();
        }
    }

    private Prescription getSelectedPrescription() {
        int index = prescriptionList.getSelectedIndex();
        if (index < 0 || currentPrescriptions == null || index >= currentPrescriptions.size()) {
            return null;
        }
        return currentPrescriptions.get(index);
    }

    private void updateButtonStates() {
        // Check all components are initialized
        if (collectionButton == null || paymentButton == null || detailButton == null || statusLabel == null) {
            return;
        }
        
        Prescription rx = getSelectedPrescription();
        
        if (rx == null) {
            collectionButton.setEnabled(false);
            paymentButton.setEnabled(false);
            detailButton.setEnabled(false);
            statusLabel.setText("Select a prescription to perform actions");
            return;
        }

        detailButton.setEnabled(true);
        
        // Get current status
        pharmacy.enumeration.PrescriptionStatus status = rx.getStatus();
        boolean isReadyForCollection = status == pharmacy.enumeration.PrescriptionStatus.READY_FOR_COLLECTION;
        boolean isPaymentPending = status == pharmacy.enumeration.PrescriptionStatus.PAYMENT_PENDING;
        
        collectionButton.setEnabled(isReadyForCollection);
        paymentButton.setEnabled(isPaymentPending);

        // Update status label
        if (isReadyForCollection) {
            statusLabel.setText("✅ This prescription is ready for collection. Please confirm you have collected the medication.");
            collectionButton.setEnabled(true);
            paymentButton.setEnabled(false);
        } else if (isPaymentPending) {
            statusLabel.setText("💰 You have collected the medication. Please make payment to complete the process.");
            collectionButton.setEnabled(false);
            paymentButton.setEnabled(true);
        } else if (status == pharmacy.enumeration.PrescriptionStatus.DISPENSED) {
            statusLabel.setText("📦 This prescription has been completed and dispensed.");
            collectionButton.setEnabled(false);
            paymentButton.setEnabled(false);
        } else if (status == pharmacy.enumeration.PrescriptionStatus.CANCELLED) {
            statusLabel.setText("❌ This prescription has been cancelled.");
            collectionButton.setEnabled(false);
            paymentButton.setEnabled(false);
        } else {
            statusLabel.setText("⏳ This prescription is being processed. Please wait.");
            collectionButton.setEnabled(false);
            paymentButton.setEnabled(false);
        }
    }

    private void confirmCollection() {
        Prescription rx = getSelectedPrescription();
        if (rx == null) {
            JOptionPane.showMessageDialog(this, "Please select a prescription.");
            return;
        }

        if (rx.getStatus() != pharmacy.enumeration.PrescriptionStatus.READY_FOR_COLLECTION) {
            JOptionPane.showMessageDialog(this, 
                "This prescription is not ready for collection yet.");
            return;
        }

        // Verify patient identity
        if (!rx.getPatient().getUserId().equals(patient.getUserId())) {
            JOptionPane.showMessageDialog(this, 
                "This prescription does not belong to you.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Show prescription details for confirmation
        StringBuilder details = new StringBuilder();
        details.append("Please confirm that you are collecting:\n\n");
        details.append("Prescription ID: ").append(rx.getPrescriptionId()).append("\n");
        details.append("Doctor: ").append(rx.getPrescribingDoctor().getFullName()).append("\n");
        details.append("Date: ").append(rx.getPrescriptionDate()).append("\n\n");
        details.append("Medicines:\n");
        for (PrescriptionItem item : rx.getItems()) {
            details.append("  - ").append(item.getMedicine().getName())
                   .append(" x").append(item.getQuantity())
                   .append(" | ").append(item.getDosageInstructions()).append("\n");
        }
        details.append("\nTotal Amount: RM").append(String.format("%.2f", rx.getTotalPrice()));

        int confirm = JOptionPane.showConfirmDialog(
            this,
            details.toString(),
            "Confirm Collection",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                prescriptionManager.patientConfirmsCollection(rx, patient);
                refreshPrescriptions();
                JOptionPane.showMessageDialog(this, 
                    "Collection confirmed successfully!\n\n" +
                    "Please proceed to payment.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Failed to confirm collection: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void makePayment() {
        Prescription rx = getSelectedPrescription();
        if (rx == null) {
            JOptionPane.showMessageDialog(this, "Please select a prescription.");
            return;
        }

        if (rx.getStatus() != pharmacy.enumeration.PrescriptionStatus.PAYMENT_PENDING) {
            JOptionPane.showMessageDialog(this, 
                "This prescription is not pending payment.");
            return;
        }

        // Verify patient identity
        if (!rx.getPatient().getUserId().equals(patient.getUserId())) {
            JOptionPane.showMessageDialog(this, 
                "This prescription does not belong to you.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Show payment details
        StringBuilder details = new StringBuilder();
        details.append("Payment Details:\n\n");
        details.append("Prescription ID: ").append(rx.getPrescriptionId()).append("\n");
        details.append("Patient: ").append(rx.getPatient().getFullName()).append("\n\n");
        details.append("Medicines:\n");
        for (PrescriptionItem item : rx.getItems()) {
            details.append("  - ").append(item.getMedicine().getName())
                   .append(" x").append(item.getQuantity())
                   .append(" @ RM").append(String.format("%.2f", item.getUnitPriceAtTime()))
                   .append(" = RM").append(String.format("%.2f", item.getSubtotal())).append("\n");
        }
        details.append("\nTotal Amount: RM").append(String.format("%.2f", rx.getTotalPrice()));

        int confirm = JOptionPane.showConfirmDialog(
            this,
            details.toString(),
            "Confirm Payment",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            // Simulate payment processing
            int paymentConfirm = JOptionPane.showConfirmDialog(
                this,
                "Click YES to simulate payment processing.\n\n" +
                "Amount: RM" + String.format("%.2f", rx.getTotalPrice()) + "\n" +
                "Payment Method: Cash/Card\n\n" +
                "Note: After payment, pharmacist will confirm to complete.",
                "Process Payment",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            if (paymentConfirm == JOptionPane.YES_OPTION) {
                try {
                    prescriptionManager.processPatientPayment(rx, patient);
                    refreshPrescriptions();
                    JOptionPane.showMessageDialog(this, 
                        "Payment processed successfully!\n\n" +
                        "Thank you for your payment. Your prescription is now complete.",
                        "Payment Successful",
                        JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, 
                        "Payment failed: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void showPrescriptionDetail() {
        Prescription rx = getSelectedPrescription();
        if (rx == null) {
            JOptionPane.showMessageDialog(this, "Please select a prescription first.");
            return;
        }

        StringBuilder detail = new StringBuilder();
        detail.append("========== PRESCRIPTION DETAILS ==========\n\n");
        detail.append("Prescription ID: ").append(rx.getPrescriptionId()).append("\n");
        detail.append("Date: ").append(rx.getPrescriptionDate()).append("\n");
        detail.append("Status: ").append(rx.getStatus()).append("\n");
        detail.append("Doctor: ").append(rx.getPrescribingDoctor().getFullName()).append("\n");
        detail.append("Remarks: ").append(rx.getRemarks() != null && !rx.getRemarks().isEmpty() ? rx.getRemarks() : "None").append("\n\n");
        detail.append("--- MEDICINES ---\n");

        for (PrescriptionItem item : rx.getItems()) {
            detail.append("  - ").append(item.getMedicine().getName())
                  .append(" x").append(item.getQuantity())
                  .append(" | Instructions: ").append(item.getDosageInstructions())
                  .append(" | RM").append(String.format("%.2f", item.getSubtotal())).append("\n");
        }

        detail.append("\nTotal: RM").append(String.format("%.2f", rx.getTotalPrice()));

        if (rx.getStatus() == pharmacy.enumeration.PrescriptionStatus.CANCELLED) {
            detail.append("\n\nCancellation reason: ").append(rx.getCancellationReason());
        }

        JOptionPane.showMessageDialog(
            this,
            detail.toString(),
            "Prescription Details",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showNotifications() {
        List<Notification> notifications = alertManager.getNotificationsForUser(
            patient.getUserId()
        );

        if (!notifications.isEmpty()) {
            StringBuilder allNotifications = new StringBuilder();
            allNotifications.append("You have new notifications:\n\n");
            for (Notification notification : notifications) {
                allNotifications.append("• ").append(notification.getMessage()).append("\n");
                alertManager.markAsRead(notification);
            }
            
            JOptionPane.showMessageDialog(
                this,
                allNotifications.toString(),
                "Notifications",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
}