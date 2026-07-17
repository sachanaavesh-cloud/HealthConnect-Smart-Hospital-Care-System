import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class LabTest {
    int labTestId = 0;
    Patient patient = new Patient();
    Doctor doctor = new Doctor();
    Appointment appointment = new Appointment();
    String testName = "";
    String testResult = "";
    String normalRange = "";
    String status = "";
    String testDate = "";
    String remarks = "";

    public void bookLabTest() throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n🧪 --- Book Lab Test ---");

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

        int doctorId = 0;
        if (Main.loggedInUser instanceof Doctor) {
            doctorId = ((Doctor) Main.loggedInUser).getDoctorId();
        } else {
            while (true) {
                System.out.print("👉 Enter Doctor ID: ");
                try {
                    doctorId = Integer.parseInt(sc.nextLine().trim());
                    if (doctorId > 0) {
                        break;
                    }
                    System.out.println("⚠️ Error: Doctor ID must be a positive integer.");
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
                }
            }
        }

        System.out.print("🔬 Enter Test Name (e.g. CBC, Lipid Profile, KFT, Blood Sugar): ");
        String name = sc.nextLine();

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call BookLabTest(?, ?, ?)}");
        stmt.setInt(1, patientId);
        stmt.setInt(2, doctorId);
        stmt.setString(3, name);
        stmt.execute();
        stmt.close();
        System.out.println("🎉 Lab Test scheduled successfully!");
    }

    public void updateResult() throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n🧪 --- Save Lab Test Result ---");

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

        System.out.print("📂 Enter Report PDF/File path: ");
        String path = sc.nextLine();

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call UpdateLabResult(?, ?)}");
        stmt.setInt(1, testId);
        stmt.setString(2, path);
        stmt.execute();
        stmt.close();
        System.out.println("🎉 Lab result updated successfully!");
    }

    public void viewResult() throws Exception {
        Scanner sc = new Scanner(System.in);

        int patientId = 0;
        if (Main.loggedInUser instanceof Patient) {
            patientId = ((Patient) Main.loggedInUser).getPatientId();
        } else {
            while (true) {
                System.out.print("👉 Enter Patient ID to view: ");
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
        CallableStatement stmt = DBConnection.conn.prepareCall("{call ViewLabResult(?)}");
        stmt.setInt(1, patientId);
        ResultSet rs = stmt.executeQuery();
        System.out.println("\n🧪 --- LAB TEST DETAILS ---");
        boolean found = false;
        while (rs.next()) {
            found = true;
            this.labTestId = rs.getInt("test_id");
            System.out.println("🔑 Test ID     : " + rs.getInt("test_id"));
            System.out.println("👤 Patient ID  : " + rs.getInt("patient_id"));
            System.out.println("👨‍⚕️ Doctor ID   : " + rs.getInt("doctor_id"));
            System.out.println("🔬 Test Name   : " + rs.getString("test_name"));
            System.out.println("🚦 Status      : " + rs.getString("status"));
            System.out.println("📅 Test Date   : " + rs.getString("test_date"));
            System.out.println("📎 Report File : " + rs.getString("report_file"));
            System.out.println("------------------------");
        }
        if (!found) {
            System.out.println("❌ Lab Test ID not found.");
        }
        rs.close();
        stmt.close();
    }

    public void downloadReport() {
        if (this.labTestId == 0) {
            System.out.println("⚠️ No active lab test loaded. Please view one first.");
            return;
        }
        System.out.println("⚙️ Downloading lab report PDF...");
        System.out.println("📂 Saved file to: /Users/manavvora/Downloads/LabReport_" + this.labTestId + ".pdf");
        System.out.println("📥 Download complete!");
    }

    public void updateStatus() throws Exception {
        Scanner sc = new Scanner(System.in);

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

        System.out.print("🚦 Enter New Status (Pending/Completed): ");
        String statusVal = sc.nextLine();

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call UpdateLabStatus(?, ?)}");
        stmt.setInt(1, testId);
        stmt.setString(2, statusVal);
        stmt.execute();
        stmt.close();
        System.out.println("✅ Lab test status updated successfully.");
        Main.logActivity(1, "UPDATE", "lab_tests");
    }
}