 package pharmacy.manager;

import pharmacy.database.DatabaseConnection;
import pharmacy.report.InventoryReport;
import pharmacy.report.SalesReport;

import java.sql.*;
import java.time.LocalDate;
import java.util.UUID;

public class ReportManager {

    public InventoryReport generateInventoryReport() {

        InventoryReport report =
                new InventoryReport(
                        "IR-" + UUID.randomUUID()
                                .toString()
                                .substring(0, 8),
                        LocalDate.now().toString(),
                        "Medicine Inventory Report"
                );

        String sql =
                "SELECT "
                + "COUNT(*) AS total_items, "
                + "COALESCE(SUM(stock_quantity * unit_price), 0) "
                + "AS total_value, "
                + "SUM(CASE WHEN stock_quantity "
                + "<= min_threshold_quantity "
                + "THEN 1 ELSE 0 END) AS low_stock, "
                + "SUM(CASE WHEN expiry_date < CURRENT_DATE "
                + "THEN 1 ELSE 0 END) AS expired "
                + "FROM medicines "
                + "WHERE is_active = TRUE";

        StringBuilder medicineData =
                new StringBuilder();

        try (
                Connection conn =
                        DatabaseConnection.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(sql);
                ResultSet rs =
                        ps.executeQuery()
        ) {

            if (rs.next()) {

                report.setTotalItems(
                        rs.getInt("total_items")
                );

                report.setTotalStockValue(
                        rs.getDouble("total_value")
                );

                report.setLowStockItemsCount(
                        rs.getInt("low_stock")
                );

                report.setExpiredItemsCount(
                        rs.getInt("expired")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        String detailSql =
                "SELECT medicine_id, name, "
                + "stock_quantity, unit_price "
                + "FROM medicines "
                + "WHERE is_active = TRUE "
                + "ORDER BY medicine_id";

        try (
                Connection conn =
                        DatabaseConnection.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(detailSql);
                ResultSet rs =
                        ps.executeQuery()
        ) {

            while (rs.next()) {

                medicineData.append(
                        rs.getString("medicine_id")
                );

                medicineData.append(" | ");

                medicineData.append(
                        rs.getString("name")
                );

                medicineData.append(
                        " | Stock: "
                );

                medicineData.append(
                        rs.getInt("stock_quantity")
                );

                medicineData.append(
                        " | Unit Price: RM "
                );

                medicineData.append(
                        String.format(
                                "%.2f",
                                rs.getDouble("unit_price")
                        )
                );

                medicineData.append("\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        report.setReportDataForEachMedicine(
                medicineData.toString()
        );

        return report;
    }

    public SalesReport generateSalesReport(
            String startDate,
            String endDate) {

        SalesReport report =
                new SalesReport(
                        "SR-" + UUID.randomUUID()
                                .toString()
                                .substring(0, 8),
                        LocalDate.now().toString(),
                        "Sales / Revenue Report"
                );

        report.setPeriodStart(startDate);
        report.setPeriodEnd(endDate);

        String sql =
                "SELECT "
                + "COALESCE(SUM(amount), 0) AS revenue, "
                + "COUNT(*) AS total "
                + "FROM sales_transactions "
                + "WHERE DATE(transaction_date) "
                + "BETWEEN ? AND ?";

        try (
                Connection conn =
                        DatabaseConnection.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(sql)
        ) {

            ps.setDate(
                    1,
                    Date.valueOf(startDate)
            );

            ps.setDate(
                    2,
                    Date.valueOf(endDate)
            );

            ResultSet rs =
                    ps.executeQuery();

            if (rs.next()) {

                report.setTotalRevenue(
                        rs.getDouble("revenue")
                );

                report.setTotalPrescriptionsProcessed(
                        rs.getInt("total")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        String topMedicineSql =
                "SELECT m.name, SUM(pi.quantity) AS total_qty "
                + "FROM sales_transactions st "
                + "JOIN prescription_items pi "
                + "ON st.prescription_id = pi.prescription_id "
                + "JOIN medicines m "
                + "ON pi.medicine_id = m.medicine_id "
                + "WHERE DATE(st.transaction_date) "
                + "BETWEEN ? AND ? "
                + "GROUP BY m.medicine_id, m.name "
                + "ORDER BY total_qty DESC "
                + "LIMIT 1";

        try (
                Connection conn =
                        DatabaseConnection.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(
                                topMedicineSql
                        )
        ) {

            ps.setDate(
                    1,
                    Date.valueOf(startDate)
            );

            ps.setDate(
                    2,
                    Date.valueOf(endDate)
            );

            ResultSet rs =
                    ps.executeQuery();

            if (rs.next()) {
                report.setTopSellingMedicine(
                        rs.getString("name")
                );
            } else {
                report.setTopSellingMedicine(
                        "No sales"
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return report;
    }
}
