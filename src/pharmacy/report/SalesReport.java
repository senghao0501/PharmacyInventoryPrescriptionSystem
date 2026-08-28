package pharmacy.report;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pharmacy.enumeration.PrescriptionStatus;
import pharmacy.prescription.Prescription;
import pharmacy.prescription.PrescriptionItem;

public class SalesReport extends ReportGenerator {
    private double totalRevenue;
    private int totalPrescriptionsProcessed;
    private String topSellingMedicine;
    private int pendingPaymentCount;

    public SalesReport(String reportId, List<Prescription> prescriptions) {
        super(reportId, "Pharmacy Sales Report");
        Map<String, Integer> medicineSales = new HashMap<>();
        pendingPaymentCount = 0;

        for (Prescription prescription : prescriptions) {
            if (prescription.getStatus() == PrescriptionStatus.DISPENSED) {
                totalRevenue += prescription.getTotalPrice();
                totalPrescriptionsProcessed++;

                for (PrescriptionItem item : prescription.getItems()) {
                    String medicineName = item.getMedicine().getName();
                    int current = medicineSales.getOrDefault(medicineName, 0);
                    medicineSales.put(medicineName, current + item.getQuantity());
                }
            } else if (prescription.getStatus() == PrescriptionStatus.PAYMENT_PENDING) {
                pendingPaymentCount++;
            }
        }

        int highest = 0;
        for (Map.Entry<String, Integer> entry : medicineSales.entrySet()) {
            if (entry.getValue() > highest) {
                highest = entry.getValue();
                topSellingMedicine = entry.getKey();
            }
        }
    }

    @Override
    public String generate() {
        return "========== SALES REPORT ==========\n" +
               "Dispensed prescriptions (Paid): " + totalPrescriptionsProcessed + "\n" +
               "Total revenue: RM" + String.format("%.2f", totalRevenue) + "\n" +
               "Top-selling medicine: " + (topSellingMedicine == null ? "No data" : topSellingMedicine) + "\n" +
               "Pending payment prescriptions: " + pendingPaymentCount;
    }
}