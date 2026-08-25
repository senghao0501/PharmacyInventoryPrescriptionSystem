package pharmacy.model;

import java.util.ArrayList;
import java.util.List;

public class InventoryReport extends ReportGenerator {
    private int lowStockItemsCount;
    private double totalStockValue;
    private int totalItems;
    private int expiredItemsCount;
    private List<String> reportDataForEachMedicine;

    public InventoryReport(String reportId, List<Medicine> medicines) {
        super(reportId, "Medicine Inventory Report");
        reportDataForEachMedicine = new ArrayList<>();
        totalItems = medicines.size();

        for (Medicine medicine : medicines) {
            double value = medicine.getUnitPrice() * medicine.getStockQuantity();
            totalStockValue += value;

            if (medicine.isLowStock()) {
                lowStockItemsCount++;
            }

            reportDataForEachMedicine.add(
                medicine.getMedicineId() + " | " + medicine.getName() +
                " | Stock: " + medicine.getStockQuantity() +
                " | Value: RM" + String.format("%.2f", value)
            );
        }
    }

    @Override
    public String generate() {
        StringBuilder result = new StringBuilder();
        result.append("========== INVENTORY REPORT ==========\n");
        result.append("Total medicines: " + totalItems + "\n");
        result.append("Low-stock medicines: " + lowStockItemsCount + "\n");
        result.append("Expired medicines: " + expiredItemsCount + "\n");
        result.append("Total inventory value: RM" + String.format("%.2f", totalStockValue) + "\n\n");

        for (String line : reportDataForEachMedicine) {
            result.append(line).append("\n");
        }
        return result.toString();
    }
}
