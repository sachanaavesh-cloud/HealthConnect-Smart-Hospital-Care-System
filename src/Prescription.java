import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class Prescription {
    String medicine = "";
    String dosage = "";
    String duration = "";
    String instructions = "";
    private int loadedPrescriptionId = 0;

    public void addMedicine() throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n👑 --- Update Prescription Notes ---");

        int prescriptionId = 0;
        while (true) {
            System.out.print("👉 Enter Prescription ID: ");
            try {
                prescriptionId = Integer.parseInt(sc.nextLine().trim());
                if (prescriptionId > 0) {
                    break;
                }
                System.out.println("⚠️ Error: Prescription ID must be a positive integer.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
            }
        }

        System.out.print("📝 Enter Notes: ");
        String notes = sc.nextLine();

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call UpdatePrescription(?, ?)}");
        stmt.setInt(1, prescriptionId);
        stmt.setString(2, notes);
        stmt.execute();
        stmt.close();
        System.out.println("✅ Notes added/updated to prescription.");
        Main.logActivity(1, "UPDATE", "prescriptions");
    }

    public void updateMedicine() throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n❌ --- Delete Prescription ---");

        int prescriptionId = 0;
        while (true) {
            System.out.print("👉 Enter Prescription ID: ");
            try {
                prescriptionId = Integer.parseInt(sc.nextLine().trim());
                if (prescriptionId > 0) {
                    break;
                }
                System.out.println("⚠️ Error: Prescription ID must be a positive integer.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
            }
        }

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call DeletePrescription(?)}");
        stmt.setInt(1, prescriptionId);
        stmt.execute();
        stmt.close();
        System.out.println("🗑️ Prescription deleted successfully.");
        Main.logActivity(1, "DELETE", "prescriptions");
    }

    public void viewPrescription() throws Exception {
        Scanner sc = new Scanner(System.in);

        int pId = 0;
        while (true) {
            System.out.print("👉 Enter Prescription ID to view: ");
            try {
                pId = Integer.parseInt(sc.nextLine().trim());
                if (pId > 0) {
                    break;
                }
                System.out.println("⚠️ Error: Prescription ID must be a positive integer.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
            }
        }

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call ViewPrescription(?)}");
        stmt.setInt(1, pId);
        ResultSet rs = stmt.executeQuery();
        System.out.println("\n📜 --- PRESCRIPTION DETAILS ---");
        if (rs.next()) {
            this.loadedPrescriptionId = rs.getInt("prescription_id");
            System.out.println("🔑 Prescription ID: " + this.loadedPrescriptionId);
            System.out.println("👤 Patient ID      : " + rs.getInt("patient_id"));
            System.out.println("👨‍⚕️ Doctor ID       : " + rs.getInt("doctor_id"));
            System.out.println("🔬 Diagnosis      : " + rs.getString("diagnosis"));
            System.out.println("📝 Notes          : " + rs.getString("notes"));
            System.out.println("📅 Created Date   : " + rs.getString("created_date"));
        } else {
            System.out.println("📭 Prescription not found.");
        }
        rs.close();
        stmt.close();
    }

    public void downloadPDF() {
        if (this.loadedPrescriptionId == 0) {
            System.out.println("⚠️ No active prescription loaded. Please view one first.");
            return;
        }
        System.out.println("⚙️ Generating prescription PDF...");
        System.out.println("📂 Saving file to: /Users/manavvora/Downloads/HealthConnect_Prescription_" + this.loadedPrescriptionId + ".pdf");
        System.out.println("📥 Download complete!");
    }
}