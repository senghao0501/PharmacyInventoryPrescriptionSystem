package pharmacy.notification;

import pharmacy.enums.NotificationType;

public class ConsoleNotification extends Notification {

    private String consoleFormatHeader;

    public ConsoleNotification(String notificationId, String message, String timestamp, NotificationType type, String recipientId, boolean read) {
        super(notificationId, message, timestamp, type, recipientId, read);
        this.consoleFormatHeader = "[PHARMACY NOTIFICATION]";
    }

    public void display() {
        System.out.println(consoleFormatHeader + " " + getMessage());
    }
}