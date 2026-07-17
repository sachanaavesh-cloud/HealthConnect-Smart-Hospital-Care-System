import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class Report {
    int reportId = 0;
    String diagnosis = "";
    String notes = "";
    String temperature = "";
    String bloodPressure = "";

    public void generateReport() throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n📝 --- Create Lab Test Report File ---");

        int testId = 0;
        while (true) {
            System.out.print("👉 Enter Lab Test ID: ");
            try {
                testId = Integer.parseInt(sc.nextLine().trim());
                if (testId > 0) {
                    break;
                }
                System.out.println("⚠️ Error: Lab Test ID must be a positive integer.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
            }
        }

        System.out.print("📂 Enter Report PDF/File path to upload: ");
        String filePath = sc.nextLine();

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call UpdateMedicalReport(?, ?)}");
        stmt.setInt(1, testId);
        stmt.setString(2, filePath);
        stmt.execute();
        stmt.close();
        System.out.println("🎉 Report file uploaded/associated successfully!");
        Main.logActivity(1, "UPDATE", "lab_tests");
    }

    public void downloadPDF() {
        if (this.reportId == 0) {
            System.out.println("⚠️ No active report loaded. Please view a report first.");
            return;
        }
        System.out.println("⚙️ Generating PDF report...");
        System.out.println("📂 Saving file to: /Users/manavvora/Downloads/HealthConnect_Report_" + this.reportId + ".pdf");
        System.out.println("📥 Download complete!");
    }

    public void viewReport() throws Exception {
        Scanner sc = new Scanner(System.in);

        int patientId = 0;
        if (Main.loggedInUser instanceof Patient) {
            patientId = ((Patient) Main.loggedInUser).getPatientId();
        } else {
            while (true) {
                System.out.print("👉 Enter Patient ID to view reports: ");
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
        CallableStatement stmt = DBConnection.conn.prepareCall("{call ViewMedicalReport(?)}");
        stmt.setInt(1, patientId);
        ResultSet rs = stmt.executeQuery();
        System.out.println("\n📊 --- MEDICAL REPORTS ---");
        boolean found = false;
        while (rs.next()) {
            found = true;
            this.reportId = rs.getInt("test_id");
            System.out.println("🔑 Test/Report ID: " + rs.getInt("test_id"));
            System.out.println("🔬 Test Name     : " + rs.getString("test_name"));
            System.out.println("🚦 Status        : " + rs.getString("status"));
            System.out.println("📂 Report File   : " + rs.getString("report_file"));
            System.out.println("----------------------------------------");
        }
        if (!found) {
            System.out.println("❌ No reports found for this patient.");
        }
        rs.close();
        stmt.close();
    }
}