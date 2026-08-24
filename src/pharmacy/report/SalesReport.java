package pharmacy.report;

public class SalesReport extends ReportGenerator {

    private double totalRevenue;
    private int totalPrescriptionsProcessed;
    private String periodStart;
    private String periodEnd;
    private String topSellingMedicine;

    public SalesReport(String reportId, String generatedDate, String title) {
        super(reportId, generatedDate, title);
    }

    public void setTotalRevenue(double value) {
        totalRevenue = value;
    }

    public void setTotalPrescriptionsProcessed(int value) {
        totalPrescriptionsProcessed = value;
    }

    public void setPeriodStart(String value) {
        periodStart = value;
    }

    public void setPeriodEnd(String value) {
        periodEnd = value;
    }

    public void setTopSellingMedicine(String value) {
        topSellingMedicine = value;
    }

    @Override
    public String generateReport() {
        return "===== SALES REPORT =====\n"
                + "Period: " + periodStart + " to " + periodEnd + "\n"
                + "Total Revenue: RM " + String.format("%.2f", totalRevenue) + "\n"
                + "Prescriptions Processed: " + totalPrescriptionsProcessed + "\n"
                + "Top Selling Medicine: " + topSellingMedicine + "\n";
    }
}