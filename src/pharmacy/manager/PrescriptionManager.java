package pharmacy.manager;

import pharmacy.database.DatabaseConnection;
import pharmacy.enums.PrescriptionStatus;
import pharmacy.model.Medicine;

import java.sql.*;
import java.util.UUID;

public class PrescriptionManager {

    private final InventoryManager inventoryManager =
            new InventoryManager();

    private final AlertManager alertManager =
            new AlertManager();

    public String createPrescription(
            String doctorId,
            String patientId,
            String[] medicineIds,
            int[] quantities,
            String[] dosages,
            String remarks) {

        if (medicineIds == null
                || quantities == null
                || dosages == null) {

            return null;
        }

        if (medicineIds.length == 0
                || medicineIds.length
                != quantities.length
                || medicineIds.length
                != dosages.length) {

            return null;
        }

        double totalPrice = 0;

        for (int i = 0;
             i < medicineIds.length;
             i++) {

            Medicine medicine =
                    inventoryManager.findMedicine(
                            medicineIds[i]
                    );

            if (medicine == null) {

                throw new IllegalArgumentException(
                        "Medicine does not exist: "
                                + medicineIds[i]
                );
            }

            if (medicine.getStockQuantity() <= 0) {

                throw new IllegalArgumentException(
                        medicine.getName()
                                + " is out of stock."
                );
            }

            if (quantities[i] <= 0) {

                throw new IllegalArgumentException(
                        "Quantity must be greater than 0."
                );
            }

            if (quantities[i]
                    > medicine.getStockQuantity()) {

                throw new IllegalArgumentException(
                        "Insufficient stock for "
                                + medicine.getName()
                );
            }

            totalPrice +=
                    medicine.getUnitPrice()
                            * quantities[i];
        }

        String prescriptionId =
                "RX-" + UUID.randomUUID()
                        .toString()
                        .substring(0, 8);

        Connection conn = null;

        try {

            conn =
                    DatabaseConnection.getConnection();

            conn.setAutoCommit(false);

            String prescriptionSql =
                    "INSERT INTO prescriptions "
                    + "(prescription_id, status, remarks, "
                    + "patient_id, prescribing_doctor_id, "
                    + "total_price) "
                    + "VALUES (?, 'PENDING', ?, ?, ?, ?)";

            try (
                    PreparedStatement ps =
                            conn.prepareStatement(
                                    prescriptionSql
                            )
            ) {

                ps.setString(1, prescriptionId);
                ps.setString(2, remarks);
                ps.setString(3, patientId);
                ps.setString(4, doctorId);
                ps.setDouble(5, totalPrice);

                ps.executeUpdate();
            }

            String itemSql =
                    "INSERT INTO prescription_items "
                    + "(item_id, prescription_id, medicine_id, "
                    + "quantity, dosage_instructions, "
                    + "unit_price_at_time, subtotal) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";

            for (int i = 0;
                 i < medicineIds.length;
                 i++) {

                Medicine medicine =
                        inventoryManager.findMedicine(
                                medicineIds[i]
                        );

                String itemId =
                        "ITEM-" + UUID.randomUUID()
                                .toString()
                                .substring(0, 8);

                try (
                        PreparedStatement ps =
                                conn.prepareStatement(
                                        itemSql
                                )
                ) {

                    ps.setString(1, itemId);
                    ps.setString(2, prescriptionId);
                    ps.setString(3, medicineIds[i]);
                    ps.setInt(4, quantities[i]);
                    ps.setString(5, dosages[i]);
                    ps.setDouble(
                            6,
                            medicine.getUnitPrice()
                    );
                    ps.setDouble(
                            7,
                            medicine.getUnitPrice()
                                    * quantities[i]
                    );

                    ps.executeUpdate();
                }
            }

            conn.commit();

            return prescriptionId;

        } catch (SQLException e) {

            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }

            e.printStackTrace();

        } finally {

            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }

        return null;
    }

    public boolean startPreparing(
            String prescriptionId,
            String pharmacistId) {

        String sql =
                "UPDATE prescriptions "
                + "SET status = 'PREPARING', "
                + "dispensing_pharmacist_id = ?, "
                + "updated_at = CURRENT_TIMESTAMP "
                + "WHERE prescription_id = ? "
                + "AND status = 'PENDING'";

        try (
                Connection conn =
                        DatabaseConnection.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setString(1, pharmacistId);
            ps.setString(2, prescriptionId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean cancelPrescription(
            String prescriptionId,
            String userId,
            String reason) {

        String sql =
                "UPDATE prescriptions "
                + "SET status = 'CANCELLED', "
                + "cancellation_reason = ?, "
                + "updated_at = CURRENT_TIMESTAMP "
                + "WHERE prescription_id = ? "
                + "AND status IN ('PENDING', 'PREPARING') "
                + "AND (prescribing_doctor_id = ? "
                + "OR dispensing_pharmacist_id = ?)";

        try (
                Connection conn =
                        DatabaseConnection.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setString(1, reason);
            ps.setString(2, prescriptionId);
            ps.setString(3, userId);
            ps.setString(4, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean editPrescription(
            String prescriptionId,
            String userId,
            String[] medicineIds,
            int[] quantities,
            String[] dosages,
            String remarks) {

        String status = getPrescriptionStatus(
                prescriptionId
        );

        if (status == null) {
            return false;
        }

        if (!status.equals("PENDING")
                && !status.equals("PREPARING")) {

            return false;
        }

        if (!isAuthorizedEditor(
                prescriptionId,
                userId
        )) {
            return false;
        }

        double totalPrice = 0;

        for (int i = 0;
             i < medicineIds.length;
             i++) {

            Medicine medicine =
                    inventoryManager.findMedicine(
                            medicineIds[i]
                    );

            if (medicine == null
                    || medicine.getStockQuantity() <= 0
                    || quantities[i] <= 0
                    || quantities[i]
                    > medicine.getStockQuantity()) {

                return false;
            }

            totalPrice +=
                    medicine.getUnitPrice()
                            * quantities[i];
        }

        Connection conn = null;

        try {

            conn =
                    DatabaseConnection.getConnection();

            conn.setAutoCommit(false);

            String deleteSql =
                    "DELETE FROM prescription_items "
                    + "WHERE prescription_id = ?";

            try (
                    PreparedStatement ps =
                            conn.prepareStatement(
                                    deleteSql
                            )
            ) {

                ps.setString(1, prescriptionId);
                ps.executeUpdate();
            }

            String itemSql =
                    "INSERT INTO prescription_items "
                    + "(item_id, prescription_id, medicine_id, "
                    + "quantity, dosage_instructions, "
                    + "unit_price_at_time, subtotal) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";

            for (int i = 0;
                 i < medicineIds.length;
                 i++) {

                Medicine medicine =
                        inventoryManager.findMedicine(
                                medicineIds[i]
                        );

                try (
                        PreparedStatement ps =
                                conn.prepareStatement(
                                        itemSql
                                )
                ) {

                    ps.setString(
                            1,
                            "ITEM-" + UUID.randomUUID()
                                    .toString()
                                    .substring(0, 8)
                    );

                    ps.setString(2, prescriptionId);
                    ps.setString(3, medicineIds[i]);
                    ps.setInt(4, quantities[i]);
                    ps.setString(5, dosages[i]);
                    ps.setDouble(
                            6,
                            medicine.getUnitPrice()
                    );

                    ps.setDouble(
                            7,
                            medicine.getUnitPrice()
                                    * quantities[i]
                    );

                    ps.executeUpdate();
                }
            }

            String updatePrescription =
                    "UPDATE prescriptions "
                    + "SET remarks = ?, "
                    + "total_price = ?, "
                    + "updated_at = CURRENT_TIMESTAMP "
                    + "WHERE prescription_id = ?";

            try (
                    PreparedStatement ps =
                            conn.prepareStatement(
                                    updatePrescription
                            )
            ) {

                ps.setString(1, remarks);
                ps.setDouble(2, totalPrice);
                ps.setString(3, prescriptionId);

                ps.executeUpdate();
            }

            conn.commit();

            return true;

        } catch (SQLException e) {

            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }

            e.printStackTrace();

        } finally {

            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }

        return false;
    }

    private boolean isAuthorizedEditor(
            String prescriptionId,
            String userId) {

        String sql =
                "SELECT prescription_id "
                + "FROM prescriptions "
                + "WHERE prescription_id = ? "
                + "AND (prescribing_doctor_id = ? "
                + "OR dispensing_pharmacist_id = ?)";

        try (
                Connection conn =
                        DatabaseConnection.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setString(1, prescriptionId);
            ps.setString(2, userId);
            ps.setString(3, userId);

            ResultSet rs =
                    ps.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private String getPrescriptionStatus(
            String prescriptionId) {

        String sql =
                "SELECT status FROM prescriptions "
                + "WHERE prescription_id = ?";

        try (
                Connection conn =
                        DatabaseConnection.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setString(1, prescriptionId);

            ResultSet rs =
                    ps.executeQuery();

            if (rs.next()) {
                return rs.getString("status");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean dispensePrescription(
            String prescriptionId,
            String pharmacistId) {

        Connection conn = null;

        try {

            conn =
                    DatabaseConnection.getConnection();

            conn.setAutoCommit(false);

            String checkSql =
                    "SELECT patient_id, total_price "
                    + "FROM prescriptions "
                    + "WHERE prescription_id = ? "
                    + "AND status = 'PREPARING'";

            String patientId;
            double totalPrice;

            try (
                    PreparedStatement ps =
                            conn.prepareStatement(
                                    checkSql
                            )
            ) {

                ps.setString(1, prescriptionId);

                ResultSet rs =
                        ps.executeQuery();

                if (!rs.next()) {
                    conn.rollback();
                    return false;
                }

                patientId =
                        rs.getString("patient_id");

                totalPrice =
                        rs.getDouble("total_price");
            }

            String itemSql =
                    "SELECT medicine_id, quantity "
                    + "FROM prescription_items "
                    + "WHERE prescription_id = ?";

            try (
                    PreparedStatement ps =
                            conn.prepareStatement(
                                    itemSql
                            )
            ) {

                ps.setString(1, prescriptionId);

                ResultSet rs =
                        ps.executeQuery();

                while (rs.next()) {

                    String medicineId =
                            rs.getString("medicine_id");

                    int quantity =
                            rs.getInt("quantity");

                    String stockSql =
                            "SELECT stock_quantity "
                            + "FROM medicines "
                            + "WHERE medicine_id = ? "
                            + "FOR UPDATE";

                    int stock;

                    try (
                            PreparedStatement stockPs =
                                    conn.prepareStatement(
                                            stockSql
                                    )
                    ) {

                        stockPs.setString(
                                1,
                                medicineId
                        );

                        ResultSet stockRs =
                                stockPs.executeQuery();

                        if (!stockRs.next()) {
                            conn.rollback();
                            return false;
                        }

                        stock =
                                stockRs.getInt(
                                        "stock_quantity"
                                );
                    }

                    if (stock < quantity) {

                        conn.rollback();

                        throw new IllegalStateException(
                                "Insufficient stock during dispensing."
                        );
                    }

                    String updateStock =
                            "UPDATE medicines "
                            + "SET stock_quantity = "
                            + "stock_quantity - ? "
                            + "WHERE medicine_id = ?";

                    try (
                            PreparedStatement stockPs =
                                    conn.prepareStatement(
                                            updateStock
                                    )
                    ) {

                        stockPs.setInt(1, quantity);
                        stockPs.setString(
                                2,
                                medicineId
                        );

                        stockPs.executeUpdate();
                    }

                    String inventoryLog =
                            "INSERT INTO inventory_transactions "
                            + "(medicine_id, transaction_type, "
                            + "quantity, performed_by, remarks) "
                            + "VALUES (?, 'DISPENSE', ?, ?, ?)";

                    try (
                            PreparedStatement logPs =
                                    conn.prepareStatement(
                                            inventoryLog
                                    )
                    ) {

                        logPs.setString(
                                1,
                                medicineId
                        );

                        logPs.setInt(
                                2,
                                quantity
                        );

                        logPs.setString(
                                3,
                                pharmacistId
                        );

                        logPs.setString(
                                4,
                                "Prescription "
                                        + prescriptionId
                        );

                        logPs.executeUpdate();
                    }
                }
            }

            String updatePrescription =
                    "UPDATE prescriptions "
                    + "SET status = 'READY_FOR_COLLECTION', "
                    + "dispensing_pharmacist_id = ?, "
                    + "updated_at = CURRENT_TIMESTAMP "
                    + "WHERE prescription_id = ?";

            try (
                    PreparedStatement ps =
                            conn.prepareStatement(
                                    updatePrescription
                            )
            ) {

                ps.setString(1, pharmacistId);
                ps.setString(2, prescriptionId);

                ps.executeUpdate();
            }

            conn.commit();

            alertManager.notifyPatientReady(
                    patientId,
                    prescriptionId
            );

            checkLowStockAfterDispensing(
                    prescriptionId
            );

            return true;

        } catch (SQLException e) {

            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {
                }
            }

            e.printStackTrace();

        } finally {

            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }

        return false;
    }

    private void checkLowStockAfterDispensing(
            String prescriptionId) {

        String sql =
                "SELECT m.* "
                + "FROM medicines m "
                + "JOIN prescription_items pi "
                + "ON m.medicine_id = pi.medicine_id "
                + "WHERE pi.prescription_id = ? "
                + "AND m.stock_quantity <= "
                + "m.min_threshold_quantity";

        try (
                Connection conn =
                        DatabaseConnection.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setString(1, prescriptionId);

            ResultSet rs =
                    ps.executeQuery();

            while (rs.next()) {

                Medicine medicine =
                        new Medicine();

                medicine.setMedicineId(
                        rs.getString("medicine_id")
                );

                medicine.setName(
                        rs.getString("name")
                );

                medicine.setStockQuantity(
                        rs.getInt("stock_quantity")
                );

                alertManager.notifyLowStock(
                        medicine
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean completeCollection(
            String prescriptionId,
            String pharmacistId) {

        String sql =
                "UPDATE prescriptions "
                + "SET status = 'DISPENSED', "
                + "updated_at = CURRENT_TIMESTAMP "
                + "WHERE prescription_id = ? "
                + "AND status = 'READY_FOR_COLLECTION' "
                + "AND dispensing_pharmacist_id = ?";

        try (
                Connection conn =
                        DatabaseConnection.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setString(1, prescriptionId);
            ps.setString(2, pharmacistId);

            if (ps.executeUpdate() == 0) {
                return false;
            }

            String transactionId =
                    "SALE-" + UUID.randomUUID()
                            .toString()
                            .substring(0, 8);

            String salesSql =
                    "INSERT INTO sales_transactions "
                    + "(transaction_id, prescription_id, "
                    + "patient_id, pharmacist_id, amount) "
                    + "SELECT ?, prescription_id, "
                    + "patient_id, ?, total_price "
                    + "FROM prescriptions "
                    + "WHERE prescription_id = ?";

            try (
                    PreparedStatement salesPs =
                            conn.prepareStatement(
                                    salesSql
                            )
            ) {

                salesPs.setString(1, transactionId);
                salesPs.setString(2, pharmacistId);
                salesPs.setString(3, prescriptionId);

                salesPs.executeUpdate();
            }

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
} 
