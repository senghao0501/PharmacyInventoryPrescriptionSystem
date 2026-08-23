package pharmacy.gui;

import pharmacy.database.DatabaseConnection;
import pharmacy.manager.AlertManager;
import pharmacy.model.Patient;
import pharmacy.notification.Notification;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class PatientFrame extends JFrame {

    private final Patient patient;

    private final AlertManager alertManager =
            new AlertManager();

    private final JTextArea output =
            new JTextArea();

    public PatientFrame(Patient patient) {

        this.patient = patient;

        setTitle(
                "Patient Dashboard - "
                        + patient.getFullName()
        );

        setSize(800, 600);

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        setLocationRelativeTo(null);

        buildUI();

        showNotificationsOnLogin();

        loadPrescriptions();
    }

    private void buildUI() {

        JPanel buttons =
                new JPanel(
                        new GridLayout(
                                1,
                                3,
                                10,
                                10
                        )
                );

        JButton refresh =
                new JButton("Refresh");

        JButton notifications =
                new JButton("Notifications");

        JButton logout =
                new JButton("Logout");

        buttons.add(refresh);
        buttons.add(notifications);
        buttons.add(logout);

        add(
                buttons,
                BorderLayout.NORTH
        );

        output.setEditable(false);

        add(
                new JScrollPane(output),
                BorderLayout.CENTER
        );

        refresh.addActionListener(
                e -> loadPrescriptions()
        );

        notifications.addActionListener(
                e -> showNotificationsOnLogin()
        );

        logout.addActionListener(
                e -> logout()
        );
    }

    private void showNotificationsOnLogin() {

        var notifications =
                alertManager
                        .getUnreadNotifications(
                                patient.getUserId()
                        );

        if (notifications.isEmpty()) {
            return;
        }

        StringBuilder message =
                new StringBuilder();

        message.append(
                "===== NEW NOTIFICATIONS =====\n\n"
        );

        for (Notification notification
                : notifications) {

            message.append(
                    notification.getMessage()
            );

            message.append("\n");

            alertManager.markAsRead(
                    notification.getNotificationId()
            );
        }

        JOptionPane.showMessageDialog(
                this,
                message.toString(),
                "Notifications",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void loadPrescriptions() {

        String sql =
                "SELECT prescription_id, "
                + "prescription_date, status, "
                + "total_price, remarks "
                + "FROM prescriptions "
                + "WHERE patient_id = ? "
                + "ORDER BY prescription_date DESC";

        output.setText(
                "===== MY PRESCRIPTIONS =====\n\n"
        );

        try (
                Connection conn =
                        DatabaseConnection.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    patient.getUserId()
            );

            ResultSet rs =
                    ps.executeQuery();

            while (rs.next()) {

                output.append(
                        "Prescription ID: "
                                + rs.getString(
                                        "prescription_id"
                                )
                                + "\n"
                );

                output.append(
                        "Date: "
                                + rs.getTimestamp(
                                        "prescription_date"
                                )
                                + "\n"
                );

                output.append(
                        "Status: "
                                + rs.getString(
                                        "status"
                                )
                                + "\n"
                );

                output.append(
                        "Total: RM "
                                + rs.getDouble(
                                        "total_price"
                                )
                                + "\n"
                );

                output.append(
                        "Remarks: "
                                + rs.getString(
                                        "remarks"
                                )
                                + "\n"
                );

                output.append(
                        "-----------------------------\n"
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void logout() {

        dispose();

        new LoginFrame()
                .setVisible(true);
    }
}