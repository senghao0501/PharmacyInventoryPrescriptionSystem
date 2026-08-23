package pharmacy.gui;

import pharmacy.manager.PrescriptionManager;
import pharmacy.manager.UserManager;
import pharmacy.model.Doctor;
import pharmacy.model.User;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DoctorFrame extends JFrame {

    private final Doctor doctor;

    private final PrescriptionManager prescriptionManager =
            new PrescriptionManager();

    private final UserManager userManager =
            new UserManager();

    private final JTextArea output =
            new JTextArea();

    public DoctorFrame(Doctor doctor) {

        this.doctor = doctor;

        setTitle(
                "Doctor Dashboard - "
                        + doctor.getFullName()
        );

        setSize(800, 600);

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        setLocationRelativeTo(null);

        buildUI();
    }

    private void buildUI() {

        JPanel buttons =
                new JPanel(
                        new GridLayout(
                                1,
                                4,
                                10,
                                10
                        )
                );

        JButton searchPatient =
                new JButton("Search Patient");

        JButton createPrescription =
                new JButton("Create Prescription");

        JButton cancelPrescription =
                new JButton("Cancel Prescription");

        JButton logout =
                new JButton("Logout");

        buttons.add(searchPatient);
        buttons.add(createPrescription);
        buttons.add(cancelPrescription);
        buttons.add(logout);

        add(
                buttons,
                BorderLayout.NORTH
        );

        output.setEditable(false);

        add(
                new JScrollPane(output),
                BorderLayout.CENTER
        );

        searchPatient.addActionListener(
                e -> searchPatient()
        );

        createPrescription.addActionListener(
                e -> createPrescription()
        );

        cancelPrescription.addActionListener(
                e -> cancelPrescription()
        );

        logout.addActionListener(
                e -> logout()
        );
    }

    private void searchPatient() {

        String keyword =
                JOptionPane.showInputDialog(
                        this,
                        "Enter Patient ID or Name:"
                );

        if (keyword == null) {
            return;
        }

        List<User> patients =
                userManager.searchPatients(
                        keyword
                );

        output.setText("");

        for (User patient : patients) {

            output.append(
                    patient.getUserId()
                            + " | "
                            + patient.getFullName()
                            + " | "
                            + patient.getEmail()
                            + "\n"
            );
        }

        if (patients.isEmpty()) {

            output.setText(
                    "No patient found."
            );
        }
    }

    private void createPrescription() {

        String patientId =
                JOptionPane.showInputDialog(
                        this,
                        "Patient ID:"
                );

        if (patientId == null
                || patientId.trim().isEmpty()) {
            return;
        }

        String medicineInput =
                JOptionPane.showInputDialog(
                        this,
                        "Medicine IDs separated by comma:\n"
                                + "Example: M001,M002"
                );

        if (medicineInput == null) {
            return;
        }

        String quantityInput =
                JOptionPane.showInputDialog(
                        this,
                        "Quantities separated by comma:\n"
                                + "Example: 2,1"
                );

        if (quantityInput == null) {
            return;
        }

        String dosageInput =
                JOptionPane.showInputDialog(
                        this,
                        "Dosage instructions separated by |:\n"
                                + "Example: 2 times daily|1 time daily"
                );

        if (dosageInput == null) {
            return;
        }

        String remarks =
                JOptionPane.showInputDialog(
                        this,
                        "Remarks:"
                );

        try {

            String[] medicineIds =
                    medicineInput
                            .split(",");

            String[] quantityStrings =
                    quantityInput
                            .split(",");

            String[] dosages =
                    dosageInput
                            .split("\\|");

            if (medicineIds.length
                    != quantityStrings.length
                    || medicineIds.length
                    != dosages.length) {

                throw new IllegalArgumentException(
                        "Input count does not match."
                );
            }

            int[] quantities =
                    new int[quantityStrings.length];

            for (int i = 0;
                 i < quantityStrings.length;
                 i++) {

                quantities[i] =
                        Integer.parseInt(
                                quantityStrings[i]
                                        .trim()
                        );
            }

            String prescriptionId =
                    prescriptionManager
                            .createPrescription(
                                    doctor.getUserId(),
                                    patientId,
                                    medicineIds,
                                    quantities,
                                    dosages,
                                    remarks
                            );

            if (prescriptionId != null) {

                JOptionPane.showMessageDialog(
                        this,
                        "Prescription created successfully.\n"
                                + "Prescription ID: "
                                + prescriptionId
                );

            } else {

                JOptionPane.showMessageDialog(
                        this,
                        "Failed to create prescription."
                );
            }

        } catch (Exception ex) {

            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage()
            );
        }
    }

    private void cancelPrescription() {

        String prescriptionId =
                JOptionPane.showInputDialog(
                        this,
                        "Prescription ID:"
                );

        if (prescriptionId == null) {
            return;
        }

        String reason =
                JOptionPane.showInputDialog(
                        this,
                        "Cancellation reason:"
                );

        if (reason == null) {
            return;
        }

        boolean success =
                prescriptionManager
                        .cancelPrescription(
                                prescriptionId,
                                doctor.getUserId(),
                                reason
                        );

        JOptionPane.showMessageDialog(
                this,
                success
                        ? "Prescription cancelled."
                        : "Unable to cancel. "
                        + "It may already be dispensed."
        );
    }

    private void logout() {

        dispose();

        new LoginFrame()
                .setVisible(true);
    }
}