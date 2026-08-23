package pharmacy.model;

public class PrescriptionItem {

    private String itemId;
    private int quantity;
    private String dosageInstructions;
    private Medicine medicine;
    private double unitPriceAtTime;
    private double subtotal;

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getDosageInstructions() {
        return dosageInstructions;
    }

    public void setDosageInstructions(String dosageInstructions) {
        this.dosageInstructions = dosageInstructions;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
    }

    public double getUnitPriceAtTime() {
        return unitPriceAtTime;
    }

    public void setUnitPriceAtTime(double unitPriceAtTime) {
        this.unitPriceAtTime = unitPriceAtTime;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }
} 
