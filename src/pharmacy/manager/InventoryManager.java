package pharmacy.manager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import pharmacy.model.InventoryTransaction;
import pharmacy.model.Medicine;
import pharmacy.model.Pharmacy;
import pharmacy.enumeration.MedicineCategory;
import pharmacy.repository.TxtDataStore;

public class InventoryManager {
    private List<Medicine> medicineInventory;
    private List<Pharmacy> pharmacies;
    private List<InventoryTransaction> transactionHistory;
    private TxtDataStore dataStore;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public InventoryManager(TxtDataStore dataStore) {
        this.dataStore = dataStore;
        medicineInventory = new ArrayList<>();
        pharmacies = new ArrayList<>();
        transactionHistory = new ArrayList<>();
        loadMedicines();
        loadPharmacies();
        loadTransactions();
    }

    private void loadMedicines() {
        List<String> lines = dataStore.readLines("medicines.txt");
        for (int i = 1; i < lines.size(); i++) {
            String[] data = lines.get(i).split("\\|", -1);
            try {
                Date expiry = new SimpleDateFormat("yyyy-MM-dd").parse(data[6]);
                Medicine medicine = new Medicine(
                    data[0], data[1], MedicineCategory.valueOf(data[2]),
                    Double.parseDouble(data[3]), Integer.parseInt(data[4]),
                    Integer.parseInt(data[5]), expiry, Boolean.parseBoolean(data[7])
                );
                medicineInventory.add(medicine);
            } catch (Exception e) {
                System.out.println("Invalid medicine data: " + lines.get(i));
            }
        }
    }

    private void loadPharmacies() {
        List<String> lines = dataStore.readLines("pharmacies.txt");
        for (int i = 1; i < lines.size(); i++) {
            String[] data = lines.get(i).split("\\|", -1);
            if (data.length < 4) continue;
            pharmacies.add(new Pharmacy(data[0], data[1], data[2], Boolean.parseBoolean(data[3])));
        }
    }

    private void loadTransactions() {
        List<String> lines = dataStore.readLines("inventory_transactions.txt");
        for (int i = 1; i < lines.size(); i++) {
            String[] data = lines.get(i).split("\\|", -1);
            try {
                transactionHistory.add(new InventoryTransaction(
                    data[0], data[1], Integer.parseInt(data[2]),
                    data[3], dateFormat.parse(data[4]), data[5]
                ));
            } catch (Exception e) {
                // Ignore invalid records.
            }
        }
    }

    public List<Medicine> getMedicineInventory() {
        return medicineInventory;
    }

    public List<Pharmacy> getPharmacies() {
        return pharmacies;
    }

    public List<InventoryTransaction> getTransactionHistory() {
        return transactionHistory;
    }

    public Medicine findMedicine(String medicineId) {
        for (Medicine medicine : medicineInventory) {
            if (medicine.getMedicineId().equals(medicineId)) {
                return medicine;
            }
        }
        return null;
    }

    public boolean hasEnoughStock(String medicineId, int quantity) {
        Medicine medicine = findMedicine(medicineId);
        return medicine != null && medicine.isActive() && medicine.getStockQuantity() >= quantity;
    }

    public boolean deductStock(String medicineId, int quantity, String performedBy) {
        Medicine medicine = findMedicine(medicineId);
        if (medicine == null) {
            throw new IllegalArgumentException("Medicine not found: " + medicineId);
        }
        if (!medicine.removeStock(quantity)) {
            throw new IllegalStateException("Insufficient stock for: " + medicine.getName() + 
                                             ". Available: " + medicine.getStockQuantity() + 
                                             ", Requested: " + quantity);
        }

        InventoryTransaction transaction = new InventoryTransaction(
            "IT" + System.currentTimeMillis(),
            medicineId, quantity, "Dispensing",
            new Date(), performedBy
        );
        transactionHistory.add(transaction);

        saveMedicines();
        saveTransactions();
        return true;
    }

    public void addStock(String medicineId, int quantity, String performedBy) {
        Medicine medicine = findMedicine(medicineId);
        if (medicine == null) {
            throw new IllegalArgumentException("Medicine does not exist.");
        }
        medicine.addStock(quantity);

        transactionHistory.add(new InventoryTransaction(
            "IT" + System.currentTimeMillis(),
            medicineId, quantity, "Restocking",
            new Date(), performedBy
        ));

        saveMedicines();
        saveTransactions();
    }

    public void updateMedicine(String medicineId, String name, String priceStr, 
                               String stockStr, String thresholdStr) {
        Medicine medicine = findMedicine(medicineId);
        if (medicine == null) {
            throw new IllegalArgumentException("Medicine not found.");
        }
        
        if (name != null && !name.trim().isEmpty()) {
            medicine.setName(name.trim());
        }
        
        if (priceStr != null && !priceStr.trim().isEmpty()) {
            double price = Double.parseDouble(priceStr);
            medicine.setUnitPrice(price);
        }
        
        if (stockStr != null && !stockStr.trim().isEmpty()) {
            int stock = Integer.parseInt(stockStr);
            medicine.setStockQuantity(stock);
        }
        
        if (thresholdStr != null && !thresholdStr.trim().isEmpty()) {
            int threshold = Integer.parseInt(thresholdStr);
            medicine.setMinThresholdQuantity(threshold);
        }
        
        saveMedicines();
    }

    public List<Medicine> getLowStockMedicines() {
        List<Medicine> result = new ArrayList<>();
        for (Medicine medicine : medicineInventory) {
            if (medicine.isLowStock()) {
                result.add(medicine);
            }
        }
        return result;
    }

    private void saveMedicines() {
        List<String> lines = new ArrayList<>();
        lines.add("medicineId|name|category|unitPrice|stockQuantity|minThresholdQuantity|expiryDate|active");

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        for (Medicine medicine : medicineInventory) {
            lines.add(
                medicine.getMedicineId() + "|" +
                medicine.getName() + "|" +
                medicine.getCategory() + "|" +
                medicine.getUnitPrice() + "|" +
                medicine.getStockQuantity() + "|" +
                medicine.getMinThresholdQuantity() + "|" +
                format.format(medicine.getExpiryDate()) + "|" +
                medicine.isActive()
            );
        }

        dataStore.overwrite("medicines.txt", lines);
    }

    private void saveTransactions() {
        List<String> lines = new ArrayList<>();
        lines.add("transactionId|medicineId|quantity|transactionType|transactionDate|performedBy");

        for (InventoryTransaction transaction : transactionHistory) {
            lines.add(
                transaction.getTransactionId() + "|" +
                transaction.getMedicineId() + "|" +
                transaction.getQuantity() + "|" +
                transaction.getTransactionType() + "|" +
                dateFormat.format(transaction.getTransactionDate()) + "|" +
                transaction.getPerformedBy()
            );
        }

        dataStore.overwrite("inventory_transactions.txt", lines);
    }
}