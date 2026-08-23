package pharmacy.model;

import pharmacy.enums.UserRole;

public class Doctor extends User {

    private String licenseNumber;
    private String specialization;
    private String department;

    public Doctor() {
        setRole(UserRole.DOCTOR);
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
} 
