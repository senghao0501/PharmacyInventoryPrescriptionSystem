package pharmacy.role;

import pharmacy.enumeration.UserRole;

public class Admin extends User {
    private String adminAccessLevel;

    public Admin(String userId, String username, String password, String fullName,
                 String contactNumber, String email, boolean isActive,
                 String adminAccessLevel) {
        super(userId, username, password, fullName, contactNumber, email, UserRole.ADMIN, isActive);
        this.adminAccessLevel = adminAccessLevel;
    }

    public String getAdminAccessLevel() { return adminAccessLevel; }
}