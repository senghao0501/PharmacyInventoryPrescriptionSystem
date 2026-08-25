package pharmacy.model;

import pharmacy.enumeration.UserRole;

public class Doctor extends User {
    private String licenseNumber;
    private String specialization;
    private String department;

    public Doctor(String userId, String username, String password, String fullName,
                  String contactNumber, String email, boolean isActive,
                  String licenseNumber, String specialization, String department) {
        super(userId, username, password, fullName, contactNumber, email, UserRole.DOCTOR, isActive);
        this.licenseNumber = licenseNumber;
        this.specialization = specialization;
        this.department = department;
    }

    public String getLicenseNumber() { return licenseNumber; }
    public String getSpecialization() { return specialization; }
    public String getDepartment() { return department; }
}