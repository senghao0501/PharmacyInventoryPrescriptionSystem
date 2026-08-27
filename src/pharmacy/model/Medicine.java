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

    // Setter methods
    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
    }

    public void setUnitPrice(double unitPrice) {
        if (unitPrice >= 0) {
            this.unitPrice = unitPrice;
        }
    }

    public void setStockQuantity(int stockQuantity) {
        if (stockQuantity >= 0) {
            this.stockQuantity = stockQuantity;
        }
    }

    public void setMinThresholdQuantity(int minThresholdQuantity) {
        if (minThresholdQuantity >= 0) {
            this.minThresholdQuantity = minThresholdQuantity;
        }
    }

    public void setActive(boolean active) {
        isActive = active;
    }

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