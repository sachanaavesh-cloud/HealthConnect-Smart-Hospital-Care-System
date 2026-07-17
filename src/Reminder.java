import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class Reminder {
    String reminderDate = "";
    String type = "";
    String message = "";

    public void createReminder() throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n⏰ --- Create Reminder ---");

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

        System.out.print("👉 Enter Type (Appointment/Medicine/Vaccine): ");
        String remType = sc.nextLine();

        String dateStr = "";
        while (true) {
            System.out.print("📅 Enter Date (YYYY-MM-DD): ");
            dateStr = sc.nextLine().trim();
            boolean isValid = true;
            if (dateStr.length() != 10 || dateStr.charAt(4) != '-' || dateStr.charAt(7) != '-') {
                isValid = false;
            } else {
                for (int i = 0; i < dateStr.length(); i++) {
                    if (i == 4 || i == 7) continue;
                    char ch = dateStr.charAt(i);
                    if (ch < '0' || ch > '9') {
                        isValid = false;
                        break;
                    }
                }
                if (isValid) {
                    int year = Integer.parseInt(dateStr.substring(0, 4));
                    int month = Integer.parseInt(dateStr.substring(5, 7));
                    int day = Integer.parseInt(dateStr.substring(8, 10));

                    if (month < 1 || month > 12) {
                        isValid = false;
                    } else if (day < 1) {
                        isValid = false;
                    } else {
                        int maxDays = 31;
                        if (month == 4 || month == 6 || month == 9 || month == 11) {
                            maxDays = 30;
                        } else if (month == 2) {
                            boolean isLeap = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
                            maxDays = isLeap ? 29 : 28;
                        }
                        if (day > maxDays) {
                            isValid = false;
                        }
                    }
                }
            }
            if (isValid) {
                break;
            }
            System.out.println("⚠️ Error: Date must be a valid calendar date in YYYY-MM-DD format.");
        }

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call CreateReminder(?, ?, ?)}");
        stmt.setInt(1, patientId);
        stmt.setString(2, remType);
        stmt.setString(3, dateStr + " 09:00:00");
        stmt.execute();
        stmt.close();
        System.out.println("🎉 Reminder created successfully!");
    }

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
        CallableStatement stmt = DBConnection.conn.prepareCall("{call CompleteReminder(?)}");
        stmt.setInt(1, reminderId);
        stmt.execute();
        stmt.close();
        System.out.println("✅ Reminder marked as completed (Status: Sent) successfully!");
    }
}