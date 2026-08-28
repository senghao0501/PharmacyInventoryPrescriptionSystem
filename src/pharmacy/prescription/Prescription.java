package pharmacy.prescription;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import pharmacy.enumeration.PrescriptionStatus;
import pharmacy.role.Doctor;
import pharmacy.role.Patient;
import pharmacy.role.Pharmacist;

public class Prescription {
    private String prescriptionId;
    private Date prescriptionDate;
    private PrescriptionStatus status;
    private String remarks;
    private Patient patient;
    private Doctor prescribingDoctor;
    private Pharmacist dispensingPharmacist;
    private double totalPrice;
    private Date updatedAt;
    private String cancellationReason;
    private List<PrescriptionItem> items;

    public Prescription(String prescriptionId, Patient patient, Doctor prescribingDoctor, String remarks) {
        this.prescriptionId = prescriptionId;
        this.prescriptionDate = new Date();
        this.status = PrescriptionStatus.PENDING;
        this.remarks = remarks;
        this.patient = patient;
        this.prescribingDoctor = prescribingDoctor;
        this.totalPrice = 0;
        this.updatedAt = new Date();
        this.items = new ArrayList<>();
    }

    public void addItem(PrescriptionItem item) {
        items.add(item);
        calculateTotal();
    }

    public void calculateTotal() {
        totalPrice = 0;
        for (PrescriptionItem item : items) {
            totalPrice += item.getSubtotal();
        }
    }

    public boolean canEdit() {
        return status == PrescriptionStatus.PENDING || status == PrescriptionStatus.PREPARING;
    }

    public boolean canCancel() {
        return status == PrescriptionStatus.PENDING || status == PrescriptionStatus.PREPARING;
    }

    public void setStatus(PrescriptionStatus status) {
        this.status = status;
        this.updatedAt = new Date();
    }

    public void setDispensingPharmacist(Pharmacist pharmacist) {
        this.dispensingPharmacist = pharmacist;
        this.updatedAt = new Date();
    }

    public void setCancellationReason(String reason) {
        if (!canCancel()) {
            throw new IllegalStateException("A dispensed prescription cannot be cancelled.");
        }
        this.cancellationReason = reason;
        this.status = PrescriptionStatus.CANCELLED;
        this.updatedAt = new Date();
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
        this.updatedAt = new Date();
    }

    // Used to restore dates when loading from a file
    public void setPrescriptionDate(Date date) { this.prescriptionDate = date; }
    public void setUpdatedAt(Date date) { this.updatedAt = date; }

    // Getters
    public String getPrescriptionId() { return prescriptionId; }
    public Date getPrescriptionDate() { return prescriptionDate; }
    public PrescriptionStatus getStatus() { return status; }
    public String getRemarks() { return remarks; }
    public Patient getPatient() { return patient; }
    public Doctor getPrescribingDoctor() { return prescribingDoctor; }
    public Pharmacist getDispensingPharmacist() { return dispensingPharmacist; }
    public double getTotalPrice() { return totalPrice; }
    public Date getUpdatedAt() { return updatedAt; }
    public String getCancellationReason() { return cancellationReason; }
    public List<PrescriptionItem> getItems() { return items; }

    @Override
    public String toString() {
        return prescriptionId + " | Patient: " + patient.getFullName() + " | Status: " + status;
    }
}