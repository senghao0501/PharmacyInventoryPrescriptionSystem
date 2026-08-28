package pharmacy.manager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import pharmacy.enumeration.UserRole;
import pharmacy.repository.TxtDataStore;
import pharmacy.role.Admin;
import pharmacy.role.Doctor;
import pharmacy.role.Patient;
import pharmacy.role.Pharmacist;
import pharmacy.role.User;

public class UserManager {
    private List<User> userList;
    private TxtDataStore dataStore;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public UserManager(TxtDataStore dataStore) {
        this.dataStore = dataStore;
        this.userList = new ArrayList<>();
        loadUsers();
    }

    private void loadUsers() {
        List<String> lines = dataStore.readLines("users.txt");
        for (int i = 1; i < lines.size(); i++) {
            String[] data = lines.get(i).split("\\|", -1);
            if (data.length < 8) {
                continue;
            }

            try {
                String role = data[6];
                User user = null;

                if (role.equals("PATIENT")) {
                    user = new Patient(
                        data[0], data[1], data[2], data[3],
                        data[4], data[5], Boolean.parseBoolean(data[7]),
                        data.length > 8 ? data[8] : "",
                        data.length > 9 && !data[9].isEmpty() ? dateFormat.parse(data[9]) : null,
                        data.length > 10 ? data[10] : ""
                    );
                } else if (role.equals("DOCTOR")) {
                    user = new Doctor(
                        data[0], data[1], data[2], data[3],
                        data[4], data[5], Boolean.parseBoolean(data[7]),
                        data.length > 8 ? data[8] : "",
                        data.length > 9 ? data[9] : "",
                        data.length > 10 ? data[10] : ""
                    );
                } else if (role.equals("PHARMACIST")) {
                    user = new Pharmacist(
                        data[0], data[1], data[2], data[3],
                        data[4], data[5], Boolean.parseBoolean(data[7]),
                        data.length > 8 ? data[8] : "",
                        data.length > 9 ? data[9] : ""
                    );
                } else if (role.equals("ADMIN")) {
                    user = new Admin(
                        data[0], data[1], data[2], data[3],
                        data[4], data[5], Boolean.parseBoolean(data[7]),
                        data.length > 8 ? data[8] : ""
                    );
                }

                if (user != null) {
                    userList.add(user);
                }
            } catch (Exception e) {
                System.out.println("Unable to load user: " + lines.get(i));
            }
        }
    }

    public List<User> getUserList() {
        return userList;
    }

    public List<Patient> getPatients() {
        List<Patient> result = new ArrayList<>();
        for (User user : userList) {
            if (user instanceof Patient) {
                result.add((Patient) user);
            }
        }
        return result;
    }

    public List<Doctor> getDoctors() {
        List<Doctor> result = new ArrayList<>();
        for (User user : userList) {
            if (user instanceof Doctor) {
                result.add((Doctor) user);
            }
        }
        return result;
    }

    public List<Pharmacist> getPharmacists() {
        List<Pharmacist> result = new ArrayList<>();
        for (User user : userList) {
            if (user instanceof Pharmacist) {
                result.add((Pharmacist) user);
            }
        }
        return result;
    }

    public User findByUsername(String username) {
        for (User user : userList) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }

    public User findById(String userId) {
        for (User user : userList) {
            if (user.getUserId().equals(userId)) {
                return user;
            }
        }
        return null;
    }

    public void disableUser(String userId) {
        User user = findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User does not exist.");
        }
        user.setActive(false);
        saveUsers();
    }

    public void enableUser(String userId) {
        User user = findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User does not exist.");
        }
        user.setActive(true);
        saveUsers();
    }

    public void resetPassword(String userId, String newPassword) {
        User user = findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User does not exist.");
        }
        user.setPassword(newPassword);
        saveUsers();
    }

    public void addUser(User user) {
        if (findByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("Username already exists.");
        }
        userList.add(user);
        saveUsers();
    }

    private void saveUsers() {
        List<String> lines = new ArrayList<>();
        lines.add("userId|username|password|fullName|contactNumber|email|role|active|extra1|extra2|extra3");

        for (User user : userList) {
            String extra1 = "", extra2 = "", extra3 = "";

            if (user instanceof Patient) {
                Patient patient = (Patient) user;
                extra1 = patient.getMedicalRecordNumber();
                extra2 = patient.getDateOfBirth() == null ? "" : dateFormat.format(patient.getDateOfBirth());
                extra3 = patient.getAllergyHistory();
            } else if (user instanceof Doctor) {
                Doctor doctor = (Doctor) user;
                extra1 = doctor.getLicenseNumber();
                extra2 = doctor.getSpecialization();
                extra3 = doctor.getDepartment();
            } else if (user instanceof Pharmacist) {
                Pharmacist pharmacist = (Pharmacist) user;
                extra1 = pharmacist.getPharmacistLicenseId();
                extra2 = pharmacist.getShiftSchedule();
            } else if (user instanceof Admin) {
                Admin admin = (Admin) user;
                extra1 = admin.getAdminAccessLevel();
            }

            lines.add(
                user.getUserId() + "|" +
                user.getUsername() + "|" +
                user.getPassword() + "|" +
                user.getFullName() + "|" +
                user.getContactNumber() + "|" +
                user.getEmail() + "|" +
                user.getRole() + "|" +
                user.isActive() + "|" +
                extra1 + "|" +
                extra2 + "|" +
                extra3
            );
        }

        dataStore.overwrite("users.txt", lines);
    }
}
