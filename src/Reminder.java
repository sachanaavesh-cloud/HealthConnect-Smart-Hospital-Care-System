import java.sql.*;
import java.util.*;

// Manages patient reminders for appointments, medicines, and vaccines.
public class Reminder {

    // Displays all pending reminders for a patient.
    public void sendReminder() throws Exception {
        Scanner sc = new Scanner(System.in);

        int patientId = 0;
        if (Main.loggedInUser instanceof Patient) {
            patientId = ((Patient) Main.loggedInUser).getPatientId();
        } else {
            while (true) {
                System.out.print("👉 Enter Patient ID to check active reminders: ");
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
        PreparedStatement ps = DBConnection.conn.prepareStatement("SELECT * FROM reminders WHERE patient_id = ? AND status = 'Pending'");
        ps.setInt(1, patientId);
        ResultSet rs = ps.executeQuery();
        System.out.println("\n🔔 --- PENDING REMINDERS ---");
        boolean found = false;
        while (rs.next()) {
            found = true;
            System.out.println("🔑 Reminder ID: " + rs.getInt("reminder_id"));
            System.out.println("🏷️ Type       : " + rs.getString("type"));
            System.out.println("⏰ Time       : " + rs.getString("date"));
            System.out.println("🚦 Status     : " + rs.getString("status"));
            System.out.println("📢 Notification: 🔔 Remember to attend/take your " + rs.getString("type") + "!");
            System.out.println("--------------------------------------------------");
        }
        if (!found) {
            System.out.println("📭 No pending reminders found.");
        }
        rs.close();
        ps.close();
    }

    // Marks a reminder as completed.
    public void markCompleted() throws Exception {
        Scanner sc = new Scanner(System.in);

        int reminderId = 0;
        while (true) {
            System.out.print("👉 Enter Reminder ID to mark as completed: ");
            try {
                reminderId = Integer.parseInt(sc.nextLine().trim());
                if (reminderId > 0) {
                    break;
                }
                System.out.println("⚠️ Error: Reminder ID must be a positive integer.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
            }
        }

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        String checkSql;
        if (Main.loggedInUser instanceof Patient) {
            checkSql = "SELECT status FROM reminders WHERE reminder_id = ? AND patient_id = ?";
        } else {
            checkSql = "SELECT status FROM reminders WHERE reminder_id = ?";
        }

        PreparedStatement checkPs = DBConnection.conn.prepareStatement(checkSql);
        checkPs.setInt(1, reminderId);
        if (Main.loggedInUser instanceof Patient) {
            checkPs.setInt(2, ((Patient) Main.loggedInUser).getPatientId());
        }

        ResultSet rs = checkPs.executeQuery();
        if (!rs.next()) {
            System.out.println("⚠️ Error: Reminder ID " + reminderId + " does not exist.");
            rs.close();
            checkPs.close();
            return;
        }

        String currentStatus = rs.getString("status");
        if ("Sent".equalsIgnoreCase(currentStatus) || "Completed".equalsIgnoreCase(currentStatus)) {
            System.out.println("⚠️ Error: Reminder ID " + reminderId + " is already marked as completed.");
            rs.close();
            checkPs.close();
            return;
        }

        rs.close();
        checkPs.close();
        CallableStatement stmt = DBConnection.conn.prepareCall("{call CompleteReminder(?)}");
        stmt.setInt(1, reminderId);
        stmt.execute();
        stmt.close();
        System.out.println("✅ Reminder marked as completed (Status: Sent) successfully!");
    }
}