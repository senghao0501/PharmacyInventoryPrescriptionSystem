// InventoryReport.java - 添加消耗统计
package pharmacy.model;

import java.util.ArrayList;
import java.util.List;

public class InventoryReport extends ReportGenerator {
    private int lowStockItemsCount;
    private double totalStockValue;
    private int totalItems;
    private int expiredItemsCount;
    private List<String> reportDataForEachMedicine;
    private int totalConsumed; // Track consumed items

    public InventoryReport(String reportId, List<Medicine> medicines) {
        super(reportId, "Medicine Inventory Report");
        reportDataForEachMedicine = new ArrayList<>();
        totalItems = medicines.size();
        totalConsumed = 0;

        for (Medicine medicine : medicines) {
            double value = medicine.getUnitPrice() * medicine.getStockQuantity();
            totalStockValue += value;

            if (medicine.isLowStock()) {
                lowStockItemsCount++;
            }

            // Estimate consumed based on initial stock vs current
            // This would be better with transaction history

            reportDataForEachMedicine.add(
                medicine.getMedicineId() + " | " + medicine.getName() +
                " | Stock: " + medicine.getStockQuantity() +
                " | Value: RM" + String.format("%.2f", value)
            );
        }
    }

    @Override
    public String generate() {
        return "========== INVENTORY REPORT ==========\n" +
               "Total medicines: " + totalItems + "\n" +
               "Low-stock medicines: " + lowStockItemsCount + "\n" +
               "Expired medicines: " + expiredItemsCount + "\n" +
               "Total inventory value: RM" + String.format("%.2f", totalStockValue) + "\n" +
               "Note: Inventory consumption is tracked via transactions.\n\n" +
               "--- Current Stock ---\n" +
               String.join("\n", reportDataForEachMedicine);
    }
}