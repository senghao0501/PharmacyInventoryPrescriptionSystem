package pharmacy.manager;

import pharmacy.database.DatabaseConnection;
import pharmacy.enums.MedicineCategory;
import pharmacy.model.Medicine;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryManager {

    private final AlertManager alertManager =
            new AlertManager();

    public Medicine findMedicine(String medicineId) {

        String sql =
                "SELECT * FROM medicines "
                + "WHERE medicine_id = ? "
                + "AND is_active = TRUE";

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, medicineId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapMedicine(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Medicine> getAllMedicines() {

        List<Medicine> medicines =
                new ArrayList<>();

        String sql =
                "SELECT * FROM medicines "
                + "ORDER BY medicine_id";

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {
                medicines.add(mapMedicine(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return medicines;
    }

    private Medicine mapMedicine(ResultSet rs)
            throws SQLException {

        Medicine medicine = new Medicine();

        medicine.setMedicineId(
                rs.getString("medicine_id")
        );

        medicine.setName(
                rs.getString("name")
        );

        medicine.setCategory(
                MedicineCategory.valueOf(
                        rs.getString("category")
                )
        );

        medicine.setUnitPrice(
                rs.getDouble("unit_price")
        );

        medicine.setStockQuantity(
                rs.getInt("stock_quantity")
        );

        medicine.setMinThresholdQuantity(
                rs.getInt("min_threshold_quantity")
        );

        Date expiry =
                rs.getDate("expiry_date");

        if (expiry != null) {
            medicine.setExpiryDate(
                    expiry.toString()
            );
        }

        medicine.setActive(
                rs.getBoolean("is_active")
        );

        return medicine;
    }

    public boolean addMedicine(
            Medicine medicine,
            String performedBy) {

        String sql =
                "INSERT INTO medicines "
                + "(medicine_id, name, category, unit_price, "
                + "stock_quantity, min_threshold_quantity, expiry_date) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setString(1, medicine.getMedicineId());
            ps.setString(2, medicine.getName());
            ps.setString(3, medicine.getCategory().name());
            ps.setDouble(4, medicine.getUnitPrice());
            ps.setInt(5, medicine.getStockQuantity());
            ps.setInt(
                    6,
                    medicine.getMinThresholdQuantity()
            );

            if (medicine.getExpiryDate() == null
                    || medicine.getExpiryDate().isEmpty()) {

                ps.setNull(7, Types.DATE);

            } else {

                ps.setDate(
                        7,
                        Date.valueOf(
                                medicine.getExpiryDate()
                        )
                );
            }

            boolean success =
                    ps.executeUpdate() > 0;

            if (success && medicine.getStockQuantity() > 0) {

                recordTransaction(
                        medicine.getMedicineId(),
                        "STOCK_IN",
                        medicine.getStockQuantity(),
                        performedBy,
                        "Initial stock"
                );
            }

            return success;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean increaseStock(
            String medicineId,
            int quantity,
            String performedBy) {

        if (quantity <= 0) {
            return false;
        }

        String sql =
                "UPDATE medicines "
                + "SET stock_quantity = stock_quantity + ? "
                + "WHERE medicine_id = ?";

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setInt(1, quantity);
            ps.setString(2, medicineId);

            boolean success =
                    ps.executeUpdate() > 0;

            if (success) {

                recordTransaction(
                        medicineId,
                        "STOCK_IN",
                        quantity,
                        performedBy,
                        "Stock replenishment"
                );
            }

            return success;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void recordTransaction(
            String medicineId,
            String type,
            int quantity,
            String performedBy,
            String remarks) {

        String sql =
                "INSERT INTO inventory_transactions "
                + "(medicine_id, transaction_type, quantity, "
                + "performed_by, remarks) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, medicineId);
            ps.setString(2, type);
            ps.setInt(3, quantity);
            ps.setString(4, performedBy);
            ps.setString(5, remarks);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Medicine> getLowStockMedicines() {

        List<Medicine> medicines =
                new ArrayList<>();

        String sql =
                "SELECT * FROM medicines "
                + "WHERE stock_quantity <= min_threshold_quantity "
                + "AND is_active = TRUE";

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {
                medicines.add(mapMedicine(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return medicines;
    }
} 
