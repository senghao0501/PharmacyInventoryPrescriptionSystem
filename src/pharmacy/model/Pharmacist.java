package pharmacy.model;

import pharmacy.enums.UserRole;

public class Pharmacist extends User {

    private String pharmacistLicenseId;
    private String shiftSchedule;

    public Pharmacist() {
        setRole(UserRole.PHARMACIST);
    }

    public String getPharmacistLicenseId() {
        return pharmacistLicenseId;
    }

    public void setPharmacistLicenseId(String pharmacistLicenseId) {
        this.pharmacistLicenseId = pharmacistLicenseId;
    }

    public String getShiftSchedule() {
        return shiftSchedule;
    }

    public void setShiftSchedule(String shiftSchedule) {
        this.shiftSchedule = shiftSchedule;
    }
} 
