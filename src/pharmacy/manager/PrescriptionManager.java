package pharmacy.manager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import pharmacy.model.Doctor;
import pharmacy.model.Patient;
import pharmacy.model.Pharmacist;
import pharmacy.model.Medicine;
import pharmacy.model.Prescription;
import pharmacy.model.PrescriptionItem;
import pharmacy.enumeration.PrescriptionStatus;
import pharmacy.repository.TxtDataStore;

public class PrescriptionManager {
    private List<Prescription> prescriptionList;
    private UserManager userManager;
    private InventoryManager inventoryManager;
    private AlertManager alertManager;
    private TxtDataStore dataStore;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public PrescriptionManager(UserManager userManager, InventoryManager inventoryManager,
                               AlertManager alertManager, TxtDataStore dataStore) {
        this.userManager = userManager;
        this.inventoryManager = inventoryManager;
        this.alertManager = alertManager;
        this.dataStore = dataStore;
        prescriptionList = new ArrayList<>();
        loadPrescriptions();
    }

    private void loadPrescriptions() {
        List<String> prescriptionLines = dataStore.readLines("prescriptions.txt");

        for (int i = 1; i < prescriptionLines.size(); i++) {
            String[] data = prescriptionLines.get(i).split("\\|", -1);
            try {
                Patient patient = (Patient) userManager.findById(data[3]);
                Doctor doctor = (Doctor) userManager.findById(data[4]);

                Prescription prescription = new Prescription(data[0], patient, doctor, data[2]);
                prescription.setPrescriptionDate(dateFormat.parse(data[1]));
                prescription.setStatus(PrescriptionStatus.valueOf(data[5]));

                if (!data[6].isEmpty()) {
                    Pharmacist pharmacist = (Pharmacist) userManager.findById(data[6]);
                    prescription.setDispensingPharmacist(pharmacist);
                }

                prescription.setUpdatedAt(dateFormat.parse(data[8]));

                loadPrescriptionItems(prescription);
                prescriptionList.add(prescription);

            } catch (Exception e) {
                System.out.println("Unable to load prescription: " + prescriptionLines.get(i));
            }
        }
    }

    private void loadPrescriptionItems(Prescription prescription) {
        List<String> lines = dataStore.readLines("prescription_items.txt");

        for (int i = 1; i < lines.size(); i++) {
            String[] data = lines.get(i).split("\\|", -1);
            if (!data[0].equals(prescription.getPrescriptionId())) {
                continue;
            }

            Medicine medicine = inventoryManager.findMedicine(data[2]);
            if (medicine == null) {
                continue;
            }

            PrescriptionItem item = new PrescriptionItem(
                data[1], Integer.parseInt(data[3]), data[4], medicine
            );
            prescription.addItem(item);
        }
    }

    public Prescription createPrescription(Patient patient, Doctor doctor,
                                           List<PrescriptionItem> items, String remarks) {
        if (patient == null) {
            throw new IllegalArgumentException("Please select a patient.");
        }

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("A prescription must contain at least one medicine.");
        }

        for (PrescriptionItem item : items) {
            Medicine medicine = item.getMedicine();
            if (medicine == null) {
                throw new IllegalArgumentException("Medicine does not exist.");
            }
            if (!medicine.isActive()) {
                throw new IllegalArgumentException(medicine.getName() + " is inactive.");
            }
            if (!inventoryManager.hasEnoughStock(medicine.getMedicineId(), item.getQuantity())) {
                if (medicine.getStockQuantity() <= medicine.getMinThresholdQuantity()) {
                    alertManager.createLowStockNotification(medicine);
                }
                throw new IllegalArgumentException("Insufficient stock: " + medicine.getName());
            }
        }

        Prescription prescription = new Prescription(
            "RX" + System.currentTimeMillis(),
            patient, doctor, remarks
        );

        for (PrescriptionItem item : items) {
            prescription.addItem(item);
        }

        prescriptionList.add(prescription);
        savePrescriptions();

        return prescription;
    }

    public List<Prescription> getPrescriptionList() {
        return prescriptionList;
    }

    public List<Prescription> getPendingPrescriptions() {
        List<Prescription> result = new ArrayList<>();
        for (Prescription prescription : prescriptionList) {
            if (prescription.getStatus() == PrescriptionStatus.PENDING) {
                result.add(prescription);
            }
        }
        return result;
    }

    public List<Prescription> getPatientPrescriptions(String patientId) {
        List<Prescription> result = new ArrayList<>();
        for (Prescription prescription : prescriptionList) {
            if (prescription.getPatient().getUserId().equals(patientId)) {
                result.add(prescription);
            }
        }
        return result;
    }

    public void startPreparing(Prescription prescription, Pharmacist pharmacist) {
        if (prescription.getStatus() != PrescriptionStatus.PENDING) {
            throw new IllegalStateException("Only pending prescriptions can be prepared.");
        }

        prescription.setDispensingPharmacist(pharmacist);
        prescription.setStatus(PrescriptionStatus.PREPARING);
        savePrescriptions();
    }

    public void completeDispensing(Prescription prescription, Pharmacist pharmacist) {
        if (prescription.getStatus() != PrescriptionStatus.PREPARING) {
            throw new IllegalStateException("The prescription is not currently being prepared.");
        }

        for (PrescriptionItem item : prescription.getItems()) {
            if (!inventoryManager.hasEnoughStock(
                    item.getMedicine().getMedicineId(), item.getQuantity())) {
                throw new IllegalStateException("Insufficient stock: " + item.getMedicine().getName());
            }
        }

        for (PrescriptionItem item : prescription.getItems()) {
            inventoryManager.deductStock(
                item.getMedicine().getMedicineId(),
                item.getQuantity(),
                pharmacist.getUserId()
            );
        }

        prescription.setDispensingPharmacist(pharmacist);
        prescription.setStatus(PrescriptionStatus.READY_FOR_COLLECTION);

        alertManager.createPrescriptionReadyNotification(prescription);

        savePrescriptions();
    }

    public void patientConfirmsCollection(Prescription prescription, Patient patient) {
        if (prescription.getStatus() != PrescriptionStatus.READY_FOR_COLLECTION) {
            throw new IllegalStateException("The prescription is not ready for collection.");
        }

        if (!prescription.getPatient().getUserId().equals(patient.getUserId())) {
            throw new IllegalStateException("This prescription does not belong to you.");
        }

        prescription.setStatus(PrescriptionStatus.PAYMENT_PENDING);
        savePrescriptions();
    }

    // Pharmacist processes payment (for pharmacist frame)
    public void processPayment(Prescription prescription, Pharmacist pharmacist) {
        if (prescription.getStatus() != PrescriptionStatus.PAYMENT_PENDING) {
            throw new IllegalStateException("Payment can only be processed after collection.");
        }

        prescription.setDispensingPharmacist(pharmacist);
        prescription.setStatus(PrescriptionStatus.DISPENSED);
        savePrescriptions();
    }

    // Patient makes payment (for patient frame)
    public void processPatientPayment(Prescription prescription, Patient patient) {
        if (prescription.getStatus() != PrescriptionStatus.PAYMENT_PENDING) {
            throw new IllegalStateException("Payment can only be processed after collection.");
        }

        if (!prescription.getPatient().getUserId().equals(patient.getUserId())) {
            throw new IllegalStateException("This prescription does not belong to you.");
        }

        // Find a pharmacist to associate with this transaction
        Pharmacist pharmacist = null;
        if (prescription.getDispensingPharmacist() != null) {
            pharmacist = prescription.getDispensingPharmacist();
        } else {
            // Get first available pharmacist
            List<Pharmacist> pharmacists = userManager.getPharmacists();
            if (!pharmacists.isEmpty()) {
                pharmacist = pharmacists.get(0);
            }
        }

        prescription.setDispensingPharmacist(pharmacist);
        prescription.setStatus(PrescriptionStatus.DISPENSED);
        savePrescriptions();
    }

    public void cancelPrescription(Prescription prescription, String reason) {
        if (!prescription.canCancel()) {
            throw new IllegalStateException("A dispensed prescription cannot be cancelled.");
        }
        prescription.setCancellationReason(reason);
        savePrescriptions();
    }

    private void savePrescriptions() {
        List<String> prescriptionLines = new ArrayList<>();
        prescriptionLines.add("prescriptionId|prescriptionDate|remarks|patientId|doctorId|status|pharmacistId|totalPrice|updatedAt|cancellationReason");

        List<String> itemLines = new ArrayList<>();
        itemLines.add("prescriptionId|itemId|medicineId|quantity|dosageInstructions|unitPriceAtTime|subtotal");

        for (Prescription prescription : prescriptionList) {
            String pharmacistId = "";
            if (prescription.getDispensingPharmacist() != null) {
                pharmacistId = prescription.getDispensingPharmacist().getUserId();
            }

            String cancellationReason = prescription.getCancellationReason();
            if (cancellationReason == null) {
                cancellationReason = "";
            }

            prescriptionLines.add(
                prescription.getPrescriptionId() + "|" +
                dateFormat.format(prescription.getPrescriptionDate()) + "|" +
                prescription.getRemarks().replace("|", "/") + "|" +
                prescription.getPatient().getUserId() + "|" +
                prescription.getPrescribingDoctor().getUserId() + "|" +
                prescription.getStatus() + "|" +
                pharmacistId + "|" +
                prescription.getTotalPrice() + "|" +
                dateFormat.format(prescription.getUpdatedAt()) + "|" +
                cancellationReason.replace("|", "/")
            );

            for (PrescriptionItem item : prescription.getItems()) {
                itemLines.add(
                    prescription.getPrescriptionId() + "|" +
                    item.getItemId() + "|" +
                    item.getMedicine().getMedicineId() + "|" +
                    item.getQuantity() + "|" +
                    item.getDosageInstructions().replace("|", "/") + "|" +
                    item.getUnitPriceAtTime() + "|" +
                    item.getSubtotal()
                );
            }
        }

        dataStore.overwrite("prescriptions.txt", prescriptionLines);
        dataStore.overwrite("prescription_items.txt", itemLines);
    }
}