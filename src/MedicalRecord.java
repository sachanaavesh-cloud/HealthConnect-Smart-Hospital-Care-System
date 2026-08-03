import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.Scanner;

public class MedicalRecord {
    public int historyId;
    public int patientId;
    public String disease = "";
    public String description = "";

    public MedicalRecord() {}

    public MedicalRecord(int historyId, int patientId, String disease, String description) {
        this.historyId = historyId;
        this.patientId = patientId;
        this.disease = disease;
        this.description = description;
    }

    public void addRecord() throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n📋 --- Add Medical Record ---");

        int pId = 0;
        if (Main.loggedInUser instanceof Patient) {
            pId = ((Patient) Main.loggedInUser).getPatientId();
        } else {
            while (true) {
                System.out.print("👉 Enter Patient ID: ");
                try {
                    pId = Integer.parseInt(sc.nextLine().trim());
                    if (pId > 0) {
                        break;
                    }
                    System.out.println("⚠️ Error: Patient ID must be a positive integer.");
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
                }
            }
        }

        System.out.print("🦠 Enter Disease/Diagnosis: ");
        String dis = sc.nextLine();
        System.out.print("📝 Enter Description/Notes: ");
        String desc = sc.nextLine();

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call AddMedicalRecord(?, ?, ?)}");
        stmt.setInt(1, pId);
        stmt.setString(2, dis);
        stmt.setString(3, desc);
        stmt.execute();
        stmt.close();
        System.out.println("✅ Medical record added successfully.");
        Main.logActivity(1, "INSERT", "medical_history");
    }

    public void updateRecord() throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n⚙️ --- Update Medical Record ---");

        int hId = 0;
        while (true) {
            System.out.print("👉 Enter History ID: ");
            try {
                hId = Integer.parseInt(sc.nextLine().trim());
                if (hId > 0) {
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
        stmt.setInt(1, hId);
        stmt.setString(2, desc);
        stmt.execute();
        stmt.close();
        System.out.println("✅ Medical record updated successfully.");
    }

    public LinkedList<MedicalRecord> fetchMedicalHistoryList(int targetPatientId) throws Exception {
        LinkedList<MedicalRecord> historyLinkedList = new LinkedList<>();
        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call ViewMedicalRecord(?)}");
        stmt.setInt(1, targetPatientId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            historyLinkedList.add(new MedicalRecord(
                    rs.getInt("history_id"),
                    targetPatientId,
                    rs.getString("disease"),
                    rs.getString("description")
            ));
        }
        rs.close();
        stmt.close();
        return historyLinkedList;
    }

    public void viewRecord() throws Exception {
        Scanner sc = new Scanner(System.in);

        int targetPatientId = 0;
        if (Main.loggedInUser instanceof Patient) {
            targetPatientId = ((Patient) Main.loggedInUser).getPatientId();
        } else {
            while (true) {
                System.out.print("👉 Enter Patient ID to view records: ");
                try {
                    targetPatientId = Integer.parseInt(sc.nextLine().trim());
                    if (targetPatientId > 0) {
                        break;
                    }
                    System.out.println("⚠️ Error: Patient ID must be a positive integer.");
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
                }
            }
        }

        // Populate in-memory LinkedList data structure from Database
        LinkedList<MedicalRecord> historyList = fetchMedicalHistoryList(targetPatientId);

        System.out.println("\n📋 --- Medical History Records (Traversing LinkedList) for Patient ID " + targetPatientId + " ---");
        if (historyList.isEmpty()) {
            System.out.println("📭 No medical history records found.");
        } else {
            // Traversal across the LinkedList nodes
            for (MedicalRecord rec : historyList) {
                System.out.println("🔑 History ID       : " + rec.historyId);
                System.out.println("🦠 Disease/Diagnosis: " + rec.disease);
                System.out.println("📝 Description      : " + rec.description);
                System.out.println("--------------------------------------------------");
            }
        }
    }
}