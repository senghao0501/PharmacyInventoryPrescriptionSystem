package pharmacy.model;

public class PrescriptionItem {
    private String itemId;
    private int quantity;
    private String dosageInstructions;
    private Medicine medicine;
    private double unitPriceAtTime;
    private double subtotal;

    public PrescriptionItem(String itemId, int quantity, String dosageInstructions, Medicine medicine) {
        this.itemId = itemId;
        this.quantity = quantity;
        this.dosageInstructions = dosageInstructions;
        this.medicine = medicine;
        this.unitPriceAtTime = medicine.getUnitPrice();
        this.subtotal = quantity * unitPriceAtTime;
    }

    public String getItemId() { return itemId; }
    public int getQuantity() { return quantity; }
    public String getDosageInstructions() { return dosageInstructions; }
    public Medicine getMedicine() { return medicine; }
    public double getUnitPriceAtTime() { return unitPriceAtTime; }
    public double getSubtotal() { return subtotal; }
}