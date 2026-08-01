import java.sql.*;
import java.util.*;

// Manages patient medical records including adding, updating, and viewing records.
public class MedicalRecord {
    Patient patient = new Patient();
    String diagnosis = "";
    String allergies = "";
    String history = "";

    // Adds a new medical record for a patient.
    public void addRecord() throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n📋 --- Add Medical Record ---");

        int patientId = 0;
        if (Main.loggedInUser instanceof Patient) {
            patientId = ((Patient) Main.loggedInUser).getPatientId();
        } else {
            while (true) {
                System.out.print("👉 Enter Patient ID: ");
                try {
                    patientId = Integer.parseInt(sc.nextLine().trim());
                    if (patientId > 0) {
                        break;
                    }
                    System.out.println("⚠️ Error: Patient ID must be a positive integer.");
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
                }
            }
        }

        System.out.print("🦠 Enter Disease/Diagnosis: ");
        String disease = sc.nextLine();
        System.out.print("🤧 Enter Allergy (or None): ");
        String allergy = sc.nextLine();
        System.out.print("🔪 Enter Surgery (or None): ");
        String surgery = sc.nextLine();
        System.out.print("👪 Enter Family History (or None): ");
        String familyHistory = sc.nextLine();
        System.out.print("📝 Enter Description/Notes: ");
        String desc = sc.nextLine();

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        java.sql.PreparedStatement ps = DBConnection.conn.prepareStatement(
                "INSERT INTO medical_history (patient_id, disease, allergy, surgery, family_history, description) VALUES (?, ?, ?, ?, ?, ?)");
        ps.setInt(1, patientId);
        ps.setString(2, disease);
        ps.setString(3, allergy);
        ps.setString(4, surgery);
        ps.setString(5, familyHistory);
        ps.setString(6, desc);
        ps.executeUpdate();
        ps.close();

        System.out.println("✅ Medical record with Allergy, Surgery, and Family History added successfully.");
        Main.logActivity(1, "INSERT", "medical_history");
    }

    // Updates an existing medical record.
    public void updateRecord() throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n⚙️ --- Update Medical Record ---");

        int historyId = 0;
        while (true) {
            System.out.print("👉 Enter History ID: ");
            try {
                historyId = Integer.parseInt(sc.nextLine().trim());
                if (historyId > 0) {
                    break;
                }
                System.out.println("⚠️ Error: History ID must be a positive integer.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
            }
        }

        System.out.print("🦠 Enter Disease/Diagnosis: ");
        String disease = sc.nextLine();
        System.out.print("🤧 Enter Allergy (or None): ");
        String allergy = sc.nextLine();
        System.out.print("🔪 Enter Surgery (or None): ");
        String surgery = sc.nextLine();
        System.out.print("👪 Enter Family History (or None): ");
        String familyHistory = sc.nextLine();
        System.out.print("📝 Enter Description/Notes: ");
        String desc = sc.nextLine();

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        java.sql.PreparedStatement ps = DBConnection.conn.prepareStatement(
                "UPDATE medical_history SET disease = ?, allergy = ?, surgery = ?, family_history = ?, description = ? WHERE history_id = ?");
        ps.setString(1, disease);
        ps.setString(2, allergy);
        ps.setString(3, surgery);
        ps.setString(4, familyHistory);
        ps.setString(5, desc);
        ps.setInt(6, historyId);
        int rows = ps.executeUpdate();
        ps.close();

        if (rows > 0) {
            System.out.println("✅ Medical record updated successfully.");
        } else {
            System.out.println("❌ Medical History ID not found.");
        }
    }

    // Displays the medical records of a patient.
    public void viewRecord() throws Exception {
        Scanner sc = new Scanner(System.in);

        int patientId = 0;
        if (Main.loggedInUser instanceof Patient) {
            patientId = ((Patient) Main.loggedInUser).getPatientId();
        } else {
            while (true) {
                System.out.print("👉 Enter Patient ID to view records: ");
                try {
                    patientId = Integer.parseInt(sc.nextLine().trim());
                    if (patientId > 0) {
                        break;
                    }
                    System.out.println("⚠️ Error: Patient ID must be a positive integer.");
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
                }
            }
        }

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        java.sql.PreparedStatement ps = DBConnection.conn.prepareStatement("SELECT * FROM medical_history WHERE patient_id = ?");
        ps.setInt(1, patientId);
        ResultSet rs = ps.executeQuery();
        System.out.println("\n📋 --- Medical History Records for Patient ID " + patientId + " ---");
        boolean found = false;
        while (rs.next()) {
            found = true;
            System.out.println("🔑 History ID       : " + rs.getInt("history_id"));
            System.out.println("🦠 Disease/Diagnosis: " + rs.getString("disease"));
            System.out.println("🤧 Allergy          : " + (rs.getString("allergy") != null ? rs.getString("allergy") : "-"));
            System.out.println("🔪 Surgery          : " + (rs.getString("surgery") != null ? rs.getString("surgery") : "-"));
            System.out.println("👪 Family History   : " + (rs.getString("family_history") != null ? rs.getString("family_history") : "-"));
            System.out.println("📝 Description      : " + (rs.getString("description") != null ? rs.getString("description") : "-"));
            System.out.println("--------------------------------------------------");
        }
        if (!found) {
            System.out.println("📭 No medical history records found.");
        }
        rs.close();
        ps.close();
    }
}