package pharmacy.manager;

import java.util.ArrayList;
import java.util.List;
import pharmacy.model.InventoryReport;
import pharmacy.model.ReportGenerator;
import pharmacy.model.SalesReport;

public class ReportManager {
    private List<ReportGenerator> generatedReports;

    public ReportManager() {
        generatedReports = new ArrayList<>();
    }

    public InventoryReport generateInventoryReport(InventoryManager inventoryManager) {
        InventoryReport report = new InventoryReport(
            "IR" + System.currentTimeMillis(),
            inventoryManager.getMedicineInventory()
        );
        generatedReports.add(report);
        return report;
    }

    public SalesReport generateSalesReport(PrescriptionManager prescriptionManager) {
        SalesReport report = new SalesReport(
            "SR" + System.currentTimeMillis(),
            prescriptionManager.getPrescriptionList()
        );
        generatedReports.add(report);
        return report;
    }

    public List<ReportGenerator> getGeneratedReports() {
        return generatedReports;
    }
}