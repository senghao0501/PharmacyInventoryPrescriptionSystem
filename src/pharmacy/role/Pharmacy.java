package pharmacy.role;

public class Pharmacy {
    private String pharmacyId;
    private String pharmacyName;
    private String location;
    private boolean isActive;

    public Pharmacy(String pharmacyId, String pharmacyName, String location, boolean isActive) {
        this.pharmacyId = pharmacyId;
        this.pharmacyName = pharmacyName;
        this.location = location;
        this.isActive = isActive;
    }

    public String getPharmacyId() { return pharmacyId; }
    public String getPharmacyName() { return pharmacyName; }
    public String getLocation() { return location; }
    public boolean isActive() { return isActive; }

    @Override
    public String toString() {
        return pharmacyId + " - " + pharmacyName + " (" + location + ")";
    }
}