package pharmacy.model;

import java.util.Date;
import pharmacy.enumeration.NotificationType;

public abstract class Notification {
    private String notificationId;
    private String message;
    private Date timestamp;
    private NotificationType type;
    private String recipientId;
    private boolean isRead;

    public Notification(String notificationId, String message, Date timestamp,
                        NotificationType type, String recipientId, boolean isRead) {
        this.notificationId = notificationId;
        this.message = message;
        this.timestamp = timestamp;
        this.type = type;
        this.recipientId = recipientId;
        this.isRead = isRead;
    }

    public abstract void display();

    public String getNotificationId() { return notificationId; }
    public String getMessage() { return message; }
    public Date getTimestamp() { return timestamp; }
    public NotificationType getType() { return type; }
    public String getRecipientId() { return recipientId; }
    public boolean isRead() { return isRead; }
    public void markAsRead() { isRead = true; }
}