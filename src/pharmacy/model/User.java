 package pharmacy.model;

import pharmacy.enums.UserRole;

public abstract class User {

    private String userId;
    private String username;
    private String password;
    private String fullName;
    private String contactNumber;
    private String email;
    private UserRole role;
    private boolean active;

    public User() {
    }

    public User(
            String userId,
            String username,
            String password,
            String fullName,
            String contactNumber,
            String email,
            UserRole role,
            boolean active) {

        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.contactNumber = contactNumber;
        this.email = email;
        this.role = role;
        this.active = active;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}