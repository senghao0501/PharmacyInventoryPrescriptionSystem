package pharmacy.service;

import pharmacy.manager.UserManager;
import pharmacy.model.User;

public class AuthService {
    private UserManager userManager;

    public AuthService(UserManager userManager) {
        this.userManager = userManager;
    }

    public User login(String username, String password) {
        User user = userManager.findByUsername(username);
        if (user == null) {
            return null;
        }
        if (!user.isActive()) {
            return null;
        }
        if (!user.getPassword().equals(password)) {
            return null;
        }
        return user;
    }
}