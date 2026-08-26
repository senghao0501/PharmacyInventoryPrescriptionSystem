package pharmacy.enumeration;

public enum PrescriptionStatus {
    PENDING,                    // Pending
    PREPARING,                  // Preparing medication
    READY_FOR_COLLECTION,       // Ready for collection
    PAYMENT_PENDING,            // Payment pending (after collection)
    DISPENSED,                  // Dispensed and paid
    CANCELLED                   // Cancelled
}