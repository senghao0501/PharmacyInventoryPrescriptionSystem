import javax.swing.SwingUtilities;

import pharmacy.gui.LoginFrame;
import pharmacy.manager.AlertManager;
import pharmacy.manager.InventoryManager;
import pharmacy.manager.PrescriptionManager;
import pharmacy.manager.ReportManager;
import pharmacy.manager.UserManager;
import pharmacy.repository.TxtDataStore;
import pharmacy.service.AuthService;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TxtDataStore dataStore = new TxtDataStore();

            UserManager userManager = new UserManager(dataStore);
            InventoryManager inventoryManager = new InventoryManager(dataStore);
            AlertManager alertManager = new AlertManager(dataStore);
            PrescriptionManager prescriptionManager = new PrescriptionManager(
                userManager, inventoryManager, alertManager, dataStore
            );
            ReportManager reportManager = new ReportManager();
            AuthService authService = new AuthService(userManager);

            LoginFrame loginFrame = new LoginFrame(
                authService, userManager, inventoryManager,
                prescriptionManager, alertManager, reportManager
            );
            loginFrame.setVisible(true);
        });
    }
}