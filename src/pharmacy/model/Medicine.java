package pharmacy.model;

import java.util.Date;
import pharmacy.enumeration.MedicineCategory;

public class Medicine {
    private String medicineId;
    private String name;
    private MedicineCategory category;
    private double unitPrice;
    private int stockQuantity;
    private int minThresholdQuantity;
    private Date expiryDate;
    private boolean isActive;

    public Medicine(String medicineId, String name, MedicineCategory category,
                    double unitPrice, int stockQuantity, int minThresholdQuantity,
                    Date expiryDate, boolean isActive) {
        this.medicineId = medicineId;
        this.name = name;
        this.category = category;
        this.unitPrice = unitPrice;
        this.stockQuantity = stockQuantity;
        this.minThresholdQuantity = minThresholdQuantity;
        this.expiryDate = expiryDate;
        this.isActive = isActive;
    }

    public String getMedicineId() { return medicineId; }
    public String getName() { return name; }
    public MedicineCategory getCategory() { return category; }
    public double getUnitPrice() { return unitPrice; }
    public int getStockQuantity() { return stockQuantity; }
    public int getMinThresholdQuantity() { return minThresholdQuantity; }
    public Date getExpiryDate() { return expiryDate; }
    public boolean isActive() { return isActive; }

    public void addStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        stockQuantity += quantity;
    }

    public boolean removeStock(int quantity) {
        if (quantity <= 0 || quantity > stockQuantity) {
            return false;
        }
        stockQuantity -= quantity;
        return true;
    }

    public boolean isLowStock() {
        return stockQuantity <= minThresholdQuantity;
    }

    @Override
    public String toString() {
        return medicineId + " - " + name + " | Stock: " + stockQuantity;
    }
}
