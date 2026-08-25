package pharmacy.manager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import pharmacy.model.ConsoleNotification;
import pharmacy.model.Medicine;
import pharmacy.model.Notification;
import pharmacy.model.Prescription;
import pharmacy.enumeration.NotificationType;
import pharmacy.repository.TxtDataStore;

public class AlertManager {
    private List<Notification> pendingNotifications;
    private TxtDataStore dataStore;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public AlertManager(TxtDataStore dataStore) {
        this.dataStore = dataStore;
        pendingNotifications = new ArrayList<>();
        loadNotifications();
    }

    private void loadNotifications() {
        List<String> lines = dataStore.readLines("notifications.txt");
        for (int i = 1; i < lines.size(); i++) {
            String[] data = lines.get(i).split("\\|", -1);
            try {
                pendingNotifications.add(new ConsoleNotification(
                    data[0], data[1], dateFormat.parse(data[2]),
                    NotificationType.valueOf(data[3]), data[4],
                    Boolean.parseBoolean(data[5])
                ));
            } catch (Exception e) {
                // Ignore invalid notifications.
            }
        }
    }

    public void createPrescriptionReadyNotification(Prescription prescription) {
        String message = "Your prescription " + prescription.getPrescriptionId() + " is ready for collection. Please visit the pharmacy.";

        Notification notification = new ConsoleNotification(
            "N" + System.currentTimeMillis(),
            message, new Date(),
            NotificationType.PRESCRIPTION_READY,
            prescription.getPatient().getUserId(),
            false
        );

        pendingNotifications.add(notification);
        saveNotifications();
    }

    public void createLowStockNotification(Medicine medicine) {
        String message = "Low stock warning: " + medicine.getName() +
                         " has only " + medicine.getStockQuantity() + " units remaining. Please restock promptly.";

        Notification pharmacistNotification = new ConsoleNotification(
            "N" + System.currentTimeMillis(),
            message, new Date(),
            NotificationType.LOW_STOCK_WARNING,
            "ROLE:PHARMACIST",
            false
        );

        Notification adminNotification = new ConsoleNotification(
            "N" + System.currentTimeMillis(),
            message, new Date(),
            NotificationType.LOW_STOCK_WARNING,
            "ROLE:ADMIN",
            false
        );

        pendingNotifications.add(pharmacistNotification);
        pendingNotifications.add(adminNotification);
        pharmacistNotification.display();
        saveNotifications();
    }

    public List<Notification> getNotificationsForUser(String userId) {
        List<Notification> result = new ArrayList<>();
        for (Notification notification : pendingNotifications) {
            if (notification.getRecipientId().equals(userId) && !notification.isRead()) {
                result.add(notification);
            }
        }
        return result;
    }

    public List<Notification> getNotificationsForRole(String role) {
        List<Notification> result = new ArrayList<>();
        String recipient = "ROLE:" + role;
        for (Notification notification : pendingNotifications) {
            if (notification.getRecipientId().equals(recipient) && !notification.isRead()) {
                result.add(notification);
            }
        }
        return result;
    }

    public void markAsRead(Notification notification) {
        notification.markAsRead();
        saveNotifications();
    }

    private void saveNotifications() {
        List<String> lines = new ArrayList<>();
        lines.add("notificationId|message|timestamp|type|recipientId|isRead");

        for (Notification notification : pendingNotifications) {
            String safeMessage = notification.getMessage().replace("|", "/");
            lines.add(
                notification.getNotificationId() + "|" +
                safeMessage + "|" +
                dateFormat.format(notification.getTimestamp()) + "|" +
                notification.getType() + "|" +
                notification.getRecipientId() + "|" +
                notification.isRead()
            );
        }

        dataStore.overwrite("notifications.txt", lines);
    }
}
