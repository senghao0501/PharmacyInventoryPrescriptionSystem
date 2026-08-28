package pharmacy.role;

import pharmacy.enumeration.UserRole;

public abstract class User {
    private String userId;
    private String username;
    private String password;
    private String fullName;
    private String contactNumber;
    private String email;
    private UserRole role;
    private boolean isActive;

    public User(String userId, String username, String password, String fullName,
                String contactNumber, String email, UserRole role, boolean isActive) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.contactNumber = contactNumber;
        this.email = email;
        this.role = role;
        this.isActive = isActive;
    }

    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getContactNumber() { return contactNumber; }
    public String getEmail() { return email; }
    public UserRole getRole() { return role; }
    public boolean isActive() { return isActive; }

    public void setPassword(String password) { this.password = password; }
    public void setActive(boolean active) { isActive = active; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return userId + " - " + fullName;
    }
}