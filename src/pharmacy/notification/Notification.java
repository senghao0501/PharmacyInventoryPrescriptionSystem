package pharmacy.notification;

import pharmacy.enums.NotificationType;

public abstract class Notification {

    private String notificationId;
    private String message;
    private String timestamp;
    private NotificationType type;
    private String recipientId;
    private boolean read;

    public Notification() {
    }

    public Notification(String notificationId, String message, String timestamp, NotificationType type, String recipientId, boolean read) {
        this.notificationId = notificationId;
        this.message = message;
        this.timestamp = timestamp;
        this.type = type;
        this.recipientId = recipientId;
        this.read = read;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public NotificationType getType() {
        return type;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public boolean isRead() {
        return read;
    }
}