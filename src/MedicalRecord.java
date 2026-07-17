import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class MedicalRecord {
    Patient patient = new Patient();
    String diagnosis = "";
    String allergies = "";
    String history = "";

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
        System.out.print("📝 Enter Description/Notes: ");
        String desc = sc.nextLine();

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call AddMedicalRecord(?, ?, ?)}");
        stmt.setInt(1, patientId);
        stmt.setString(2, disease);
        stmt.setString(3, desc);
        stmt.execute();
        stmt.close();
        System.out.println("✅ Medical record added successfully.");
        Main.logActivity(1, "INSERT", "medical_history");
    }

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

        System.out.print("📝 Enter Description/Notes: ");
        String desc = sc.nextLine();

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call UpdateMedicalRecord(?, ?)}");
        stmt.setInt(1, historyId);
        stmt.setString(2, desc);
        stmt.execute();
        stmt.close();
        System.out.println("✅ Medical record updated successfully.");
    }

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
        CallableStatement stmt = DBConnection.conn.prepareCall("{call ViewMedicalRecord(?)}");
        stmt.setInt(1, patientId);
        ResultSet rs = stmt.executeQuery();
        System.out.println("\n📋 --- Medical History Records for Patient ID " + patientId + " ---");
        boolean found = false;
        while (rs.next()) {
            found = true;
            System.out.println("🔑 History ID       : " + rs.getInt("history_id"));
            System.out.println("🦠 Disease/Diagnosis: " + rs.getString("disease"));
            System.out.println("📝 Description      : " + rs.getString("description"));
            System.out.println("--------------------------------------------------");
        }
        if (!found) {
            System.out.println("📭 No medical history records found.");
        }
        rs.close();
        stmt.close();
    }
}