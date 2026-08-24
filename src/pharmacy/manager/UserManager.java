package pharmacy.manager;

import pharmacy.database.DatabaseConnection;
import pharmacy.enums.UserRole;
import pharmacy.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserManager {

    public User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND is_active = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return createUserFromResultSet(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // 新增方法：根据用户ID获取用户
    public User getUserById(String userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return createUserFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private User createUserFromResultSet(ResultSet rs) throws SQLException {
        UserRole role = UserRole.valueOf(rs.getString("role"));
        User user;

        switch (role) {
            case PATIENT:
                Patient patient = new Patient();
                patient.setMedicalRecordNumber(rs.getString("medical_record_number"));

                Date dob = rs.getDate("date_of_birth");
                if (dob != null) {
                    patient.setDateOfBirth(dob.toString());
                }

                patient.setAllergyHistory(rs.getString("allergy_history"));
                user = patient;
                break;

            case DOCTOR:
                Doctor doctor = new Doctor();
                doctor.setLicenseNumber(rs.getString("license_number"));
                doctor.setSpecialization(rs.getString("specialization"));
                doctor.setDepartment(rs.getString("department"));
                user = doctor;
                break;

            case PHARMACIST:
                Pharmacist pharmacist = new Pharmacist();
                pharmacist.setPharmacistLicenseId(rs.getString("pharmacist_license_id"));
                pharmacist.setShiftSchedule(rs.getString("shift_schedule"));
                user = pharmacist;
                break;

            case ADMIN:
                Admin admin = new Admin();
                admin.setAdminAccessLevel(rs.getString("admin_access_level"));
                user = admin;
                break;

            default:
                throw new SQLException("Unknown role.");
        }

        user.setUserId(rs.getString("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setFullName(rs.getString("full_name"));
        user.setContactNumber(rs.getString("contact_number"));
        user.setEmail(rs.getString("email"));
        user.setRole(role);
        user.setActive(rs.getBoolean("is_active"));

        return user;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY role, user_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(createUserFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }

    public boolean addUser(String userId, String username, String password, String fullName, String email, UserRole role) {
        String sql = "INSERT INTO users (user_id, username, password, full_name, email, role) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.setString(2, username);
            ps.setString(3, password);
            ps.setString(4, fullName);
            ps.setString(5, email);
            ps.setString(6, role.name());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean setUserActive(String userId, boolean active) {
        String sql = "UPDATE users SET is_active = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, active);
            ps.setString(2, userId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean resetPassword(String userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newPassword);
            ps.setString(2, userId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<User> searchPatients(String keyword) {
        List<User> patients = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'PATIENT' AND is_active = TRUE AND (user_id LIKE ? OR full_name LIKE ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                patients.add(createUserFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return patients;
    }
}