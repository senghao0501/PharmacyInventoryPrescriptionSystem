package pharmacy.report;

public class InventoryReport extends ReportGenerator {

    private int lowStockItemsCount;
    private double totalStockValue;
    private int totalItems;
    private int expiredItemsCount;
    private String reportDataForEachMedicine;

    public InventoryReport(
            String reportId,
            String generatedDate,
            String title) {

        super(reportId, generatedDate, title);
    }

    public void setLowStockItemsCount(int value) {
        lowStockItemsCount = value;
    }

    public void setTotalStockValue(double value) {
        totalStockValue = value;
    }

    public void setTotalItems(int value) {
        totalItems = value;
    }

    public void setExpiredItemsCount(int value) {
        expiredItemsCount = value;
    }

    public void setReportDataForEachMedicine(String value) {
        reportDataForEachMedicine = value;
    }

    @Override
    public String generateReport() {

        return "===== INVENTORY REPORT =====\n"
                + "Total Medicine Types: " + totalItems + "\n"
                + "Total Stock Value: RM "
                + String.format("%.2f", totalStockValue) + "\n"
                + "Low Stock Items: " + lowStockItemsCount + "\n"
                + "Expired Items: " + expiredItemsCount + "\n\n"
                + reportDataForEachMedicine;
    }
}