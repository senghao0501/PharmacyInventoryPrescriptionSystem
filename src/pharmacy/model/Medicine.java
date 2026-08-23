package pharmacy.model;

import pharmacy.enums.MedicineCategory;

public class Medicine {

    private String medicineId;
    private String name;
    private MedicineCategory category;
    private double unitPrice;
    private int stockQuantity;
    private int minThresholdQuantity;
    private String expiryDate;
    private boolean active;

    public String getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(String medicineId) {
        this.medicineId = medicineId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MedicineCategory getCategory() {
        return category;
    }

    public void setCategory(MedicineCategory category) {
        this.category = category;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public int getMinThresholdQuantity() {
        return minThresholdQuantity;
    }

    public void setMinThresholdQuantity(int minThresholdQuantity) {
        this.minThresholdQuantity = minThresholdQuantity;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return medicineId + " - " + name
                + " | Stock: " + stockQuantity
                + " | RM " + unitPrice;
    }
} 
