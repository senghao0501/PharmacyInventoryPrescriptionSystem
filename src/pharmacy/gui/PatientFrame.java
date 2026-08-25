package pharmacy.gui;

import java.awt.BorderLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import pharmacy.manager.AlertManager;
import pharmacy.manager.PrescriptionManager;
import pharmacy.model.Notification;
import pharmacy.model.Patient;
import pharmacy.model.Prescription;
import pharmacy.model.PrescriptionItem;

public class PatientFrame extends JFrame {
    private Patient patient;
    private PrescriptionManager prescriptionManager;
    private AlertManager alertManager;
    private JList<String> prescriptionList;
    private String[] displayData;

    public PatientFrame(Patient patient, PrescriptionManager prescriptionManager,
                        AlertManager alertManager) {
        this.patient = patient;
        this.prescriptionManager = prescriptionManager;
        this.alertManager = alertManager;

        setTitle("Patient Workspace - " + patient.getFullName());
        setSize(750, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        buildUI();
        showNotifications();
    }

    private void buildUI() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top: welcome message
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JLabel("Welcome back, " + patient.getFullName()), BorderLayout.WEST);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshPrescriptions());
        topPanel.add(refreshButton, BorderLayout.EAST);

        panel.add(topPanel, BorderLayout.NORTH);

        // Centre: prescription list
        refreshPrescriptions();
        prescriptionList = new JList<>(displayData);
        panel.add(new JScrollPane(prescriptionList), BorderLayout.CENTER);

        // Bottom: view details
        JButton detailButton = new JButton("View Prescription Details");
        detailButton.addActionListener(e -> showPrescriptionDetail());
        panel.add(detailButton, BorderLayout.SOUTH);

        add(panel);
    }

    private void refreshPrescriptions() {
        List<Prescription> prescriptions = prescriptionManager.getPatientPrescriptions(
            patient.getUserId()
        );

        displayData = new String[prescriptions.size()];
        for (int i = 0; i < prescriptions.size(); i++) {
            Prescription rx = prescriptions.get(i);
            String statusSymbol = "";
            switch (rx.getStatus()) {
                case PENDING: statusSymbol = "⏳ "; break;
                case PREPARING: statusSymbol = "⚙️ "; break;
                case READY_FOR_COLLECTION: statusSymbol = "✅ "; break;
                case DISPENSED: statusSymbol = "📦 "; break;
                case CANCELLED: statusSymbol = "❌ "; break;
            }
            displayData[i] = statusSymbol + rx.getPrescriptionId() +
                " | " + rx.getPrescriptionDate() +
                " | " + rx.getStatus() +
                " | Medicines: " + rx.getItems().size() +
                " | Total: RM" + rx.getTotalPrice();
        }

        if (prescriptionList != null) {
            prescriptionList.setListData(displayData);
        }
    }

    private void showPrescriptionDetail() {
        int index = prescriptionList.getSelectedIndex();
        if (index < 0) {
            JOptionPane.showMessageDialog(this, "Please select a prescription first.");
            return;
        }

        List<Prescription> prescriptions = prescriptionManager.getPatientPrescriptions(
            patient.getUserId()
        );
        Prescription rx = prescriptions.get(index);

        StringBuilder detail = new StringBuilder();
        detail.append("========== PRESCRIPTION DETAILS ==========\n");
        detail.append("Prescription ID: ").append(rx.getPrescriptionId()).append("\n");
        detail.append("Date: ").append(rx.getPrescriptionDate()).append("\n");
        detail.append("Status: ").append(rx.getStatus()).append("\n");
        detail.append("Doctor: ").append(rx.getPrescribingDoctor().getFullName()).append("\n");
        detail.append("Remarks: ").append(rx.getRemarks()).append("\n\n");
        detail.append("--- MEDICINES ---\n");

        for (PrescriptionItem item : rx.getItems()) {
            detail.append("  - ").append(item.getMedicine().getName())
                  .append(" x").append(item.getQuantity())
                  .append(" | Instructions: ").append(item.getDosageInstructions())
                  .append(" | RM").append(item.getSubtotal()).append("\n");
        }

        detail.append("\nTotal: RM").append(rx.getTotalPrice());

        if (rx.getStatus() == pharmacy.enumeration.PrescriptionStatus.CANCELLED) {
            detail.append("\nCancellation reason: ").append(rx.getCancellationReason());
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

        for (Notification notification : notifications) {
            JOptionPane.showMessageDialog(
                this,
                notification.getMessage(),
                "Notifications",
                JOptionPane.INFORMATION_MESSAGE
            );
            alertManager.markAsRead(notification);
        }
    }
}
