package pharmacy.manager;

import pharmacy.database.DatabaseConnection;
import pharmacy.enums.NotificationType;
import pharmacy.model.Medicine;
import pharmacy.notification.ConsoleNotification;
import pharmacy.notification.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AlertManager {

    public void createNotification(
            String recipientId,
            String message,
            NotificationType type) {

        String notificationId =
                "N-" + UUID.randomUUID()
                        .toString()
                        .substring(0, 8);

        String sql =
                "INSERT INTO notifications "
                + "(notification_id, message, type, recipient_id) "
                + "VALUES (?, ?, ?, ?)";

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, notificationId);
            ps.setString(2, message);
            ps.setString(3, type.name());
            ps.setString(4, recipientId);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void notifyPatientReady(
            String patientId,
            String prescriptionId) {

        createNotification(
                patientId,
                "Your prescription "
                        + prescriptionId
                        + " is ready for collection.",
                NotificationType.PRESCRIPTION_READY
        );
    }

    public void notifyLowStock(
            Medicine medicine) {

        String sql =
                "SELECT user_id FROM users "
                + "WHERE role IN ('PHARMACIST', 'ADMIN') "
                + "AND is_active = TRUE";

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                createNotification(
                        rs.getString("user_id"),
                        "Low Stock Alert: "
                                + medicine.getName()
                                + " has only "
                                + medicine.getStockQuantity()
                                + " units remaining.",
                        NotificationType.LOW_STOCK_WARNING
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Notification> getUnreadNotifications(
            String recipientId) {

        List<Notification> notifications =
                new ArrayList<>();

        String sql =
                "SELECT * FROM notifications "
                + "WHERE recipient_id = ? "
                + "AND is_read = FALSE "
                + "ORDER BY timestamp DESC";

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, recipientId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Notification notification =
                        new ConsoleNotification(
                                rs.getString("notification_id"),
                                rs.getString("message"),
                                rs.getTimestamp("timestamp")
                                        .toString(),
                                NotificationType.valueOf(
                                        rs.getString("type")
                                ),
                                rs.getString("recipient_id"),
                                rs.getBoolean("is_read")
                        );

                notifications.add(notification);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return notifications;
    }

    public void markAsRead(String notificationId) {

        String sql =
                "UPDATE notifications "
                + "SET is_read = TRUE "
                + "WHERE notification_id = ?";

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, notificationId);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
} 
