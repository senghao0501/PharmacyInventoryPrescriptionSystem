package pharmacy.model;

public class InventoryTransaction {

    private int transactionId;
    private String medicineId;
    private String transactionType;
    private int quantity;
    private String transactionDate;
    private String performedBy;
    private String remarks;

    public int getTransactionId() {
        return transactionId;
    }

    public String getMedicineId() {
        return medicineId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public String getRemarks() {
        return remarks;
    }
} 
