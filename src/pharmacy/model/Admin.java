package pharmacy.model;

import pharmacy.enums.UserRole;

public class Admin extends User {

    private String adminAccessLevel;

    public Admin() {
        setRole(UserRole.ADMIN);
    }

    public String getAdminAccessLevel() {
        return adminAccessLevel;
    }

    public void setAdminAccessLevel(String adminAccessLevel) {
        this.adminAccessLevel = adminAccessLevel;
    }
} 
