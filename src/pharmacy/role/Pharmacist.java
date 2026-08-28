package pharmacy.role;

import pharmacy.enumeration.UserRole;

public class Pharmacist extends User {
    private String pharmacistLicenseId;
    private String shiftSchedule;

    public Pharmacist(String userId, String username, String password, String fullName,
                      String contactNumber, String email, boolean isActive,
                      String pharmacistLicenseId, String shiftSchedule) {
        super(userId, username, password, fullName, contactNumber, email, UserRole.PHARMACIST, isActive);
        this.pharmacistLicenseId = pharmacistLicenseId;
        this.shiftSchedule = shiftSchedule;
    }

    public String getPharmacistLicenseId() { return pharmacistLicenseId; }
    public String getShiftSchedule() { return shiftSchedule; }
}