package pharmacy.model;

import java.util.Date;
import pharmacy.enumeration.NotificationType;

public class ConsoleNotification extends Notification {
    private String consoleFormatHeader;

    public ConsoleNotification(String notificationId, String message, Date timestamp,
                               NotificationType type, String recipientId, boolean isRead) {
        super(notificationId, message, timestamp, type, recipientId, isRead);
        this.consoleFormatHeader = "[Notification]";
    }

    @Override
    public void display() {
        System.out.println(consoleFormatHeader + " " + getMessage());
    }
}
