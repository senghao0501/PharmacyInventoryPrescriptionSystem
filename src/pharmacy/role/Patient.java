package pharmacy.role;

import java.util.Date;
import pharmacy.enumeration.UserRole;

public class Patient extends User {
    private String medicalRecordNumber;
    private Date dateOfBirth;
    private String allergyHistory;

    public Patient(String userId, String username, String password, String fullName,
                   String contactNumber, String email, boolean isActive,
                   String medicalRecordNumber, Date dateOfBirth, String allergyHistory) {
        super(userId, username, password, fullName, contactNumber, email, UserRole.PATIENT, isActive);
        this.medicalRecordNumber = medicalRecordNumber;
        this.dateOfBirth = dateOfBirth;
        this.allergyHistory = allergyHistory;
    }

    public String getMedicalRecordNumber() { return medicalRecordNumber; }
    public Date getDateOfBirth() { return dateOfBirth; }
    public String getAllergyHistory() { return allergyHistory; }
}