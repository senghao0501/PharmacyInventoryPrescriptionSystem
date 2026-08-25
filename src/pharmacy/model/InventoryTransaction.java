package pharmacy.model;

import java.util.Date;

public class InventoryTransaction {
    private String transactionId;
    private String medicineId;
    private int quantity;
    private String transactionType;
    private Date transactionDate;
    private String performedBy;

    public InventoryTransaction(String transactionId, String medicineId, int quantity,
                                String transactionType, Date transactionDate, String performedBy) {
        this.transactionId = transactionId;
        this.medicineId = medicineId;
        this.quantity = quantity;
        this.transactionType = transactionType;
        this.transactionDate = transactionDate;
        this.performedBy = performedBy;
    }

    public String getTransactionId() { return transactionId; }
    public String getMedicineId() { return medicineId; }
    public int getQuantity() { return quantity; }
    public String getTransactionType() { return transactionType; }
    public Date getTransactionDate() { return transactionDate; }
    public String getPerformedBy() { return performedBy; }
}