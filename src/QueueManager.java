import java.sql.*;
import java.util.*;

public class QueueManager {
    public void addPatient() throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n🚦 --- Add Patient to Queue ---");

        int patientId = 0;
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

        int doctorId = 0;
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

        String priority = "";
        do {
            System.out.println("🚨 Choose Priority:");
            System.out.println("1. Emergency");
            System.out.println("2. Pregnant");
            System.out.println("3. Senior Citizen");
            System.out.println("4. Child");
            System.out.println("5. Disabled");
            System.out.println("6. Normal");
            System.out.print("👉 Enter choice (1-6): ");
            String choiceOpt = sc.nextLine().trim();
            switch (choiceOpt) {
                case "1": priority = "Emergency"; break;
                case "2": priority = "Pregnant"; break;
                case "3": priority = "Senior Citizen"; break;
                case "4": priority = "Child"; break;
                case "5": priority = "Disabled"; break;
                case "6": priority = "Normal"; break;
                default:
                    System.out.println("⚠️ Error: Invalid option. Please enter a choice between 1 and 6.");
                    break;
            }
        } while (priority.isEmpty());

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call AddPatientToQueue(?, ?, ?)}");
        stmt.setInt(1, patientId);
        stmt.setInt(2, doctorId);
        stmt.setString(3, priority);
        stmt.execute();
        stmt.close();
        System.out.println("🎉 Patient added to queue successfully!");
        Main.logActivity(1, "INSERT", "queue");
    }

    public void removePatient() throws Exception {
        Scanner sc = new Scanner(System.in);

        int queueId = 0;
        while (true) {
            System.out.print("👉 Enter Queue ID to remove (Served): ");
            try {
                queueId = Integer.parseInt(sc.nextLine().trim());
                if (queueId > 0) {
                    break;
                }
                System.out.println("⚠️ Error: Queue ID must be a positive integer.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
            }
        }

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call RemovePatientFromQueue(?)}");
        stmt.setInt(1, queueId);
        stmt.execute();
        stmt.close();
        System.out.println("✅ Patient removed from queue (Status: Served).");
        Main.logActivity(1, "DELETE", "queue");
    }

    public void displayQueue() throws Exception {
        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call ViewQueue()}");
        ResultSet rs = stmt.executeQuery();
        System.out.println("\n🚦 --- Active Live Queue ---");
        System.out.printf("%-10s | %-12s | %-12s | %-12s | %-20s\n", "Queue ID", "Patient ID", "Doctor ID", "Priority", "Arrival Time");
        System.out.println("-----------------------------------------------------------------------------");
        boolean found = false;
        while (rs.next()) {
            found = true;
            System.out.printf("%-10d | %-12d | %-12d | %-12s | %-20s\n",
                    rs.getInt("queue_id"),
                    rs.getInt("patient_id"),
                    rs.getInt("doctor_id"),
                    rs.getString("priority_level"),
                    rs.getTimestamp("arrival_time").toString()
            );
        }
        if (!found) {
            System.out.println("📭 No patients waiting in queue.");
        }
        rs.close();
        stmt.close();
    }

    public void displayQueue(int doctorId) throws Exception {
        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        PreparedStatement ps = DBConnection.conn.prepareStatement(
                "SELECT q.queue_id, q.patient_id, CONCAT(IFNULL(p.first_name, ''), ' ', IFNULL(p.last_name, '')) AS patient_name, " +
                        "q.priority_level, q.arrival_time, q.status " +
                        "FROM queue q " +
                        "LEFT JOIN patients p ON q.patient_id = p.patient_id " +
                        "WHERE q.doctor_id = ? AND q.status = 'Waiting' " +
                        "ORDER BY FIELD(q.priority_level, 'Emergency','Pregnant','Senior Citizen','Child','Disabled','Normal'), q.arrival_time"
        );
        ps.setInt(1, doctorId);
        ResultSet rs = ps.executeQuery();
        System.out.println("\n🚦 --- Live Waiting Queue for Doctor ID: " + doctorId + " ---");
        System.out.printf("%-10s | %-12s | %-22s | %-15s | %-20s\n", "Queue ID", "Patient ID", "Patient Name", "Priority", "Arrival Time");
        System.out.println("--------------------------------------------------------------------------------------------------");
        boolean found = false;
        while (rs.next()) {
            found = true;
            String name = rs.getString("patient_name").trim();
            if (name.isEmpty()) name = "N/A";
            System.out.printf("%-10d | %-12d | %-22s | %-15s | %-20s\n",
                    rs.getInt("queue_id"),
                    rs.getInt("patient_id"),
                    name,
                    rs.getString("priority_level"),
                    rs.getTimestamp("arrival_time").toString()
            );
        }
        if (!found) {
            System.out.println("📭 No patients currently waiting in queue for Doctor ID: " + doctorId);
        }
        rs.close();
        ps.close();
    }

    public void calculateWaitingTime() throws Exception {
        Scanner sc = new Scanner(System.in);

        int queueId = 0;
        while (true) {
            System.out.print("👉 Enter Queue ID: ");
            try {
                queueId = Integer.parseInt(sc.nextLine().trim());
                if (queueId > 0) {
                    break;
                }
                System.out.println("⚠️ Error: Queue ID must be a positive integer.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
            }
        }

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{? = call CalculateWaitingTime(?)}");
        stmt.registerOutParameter(1, Types.INTEGER);
        stmt.setInt(2, queueId);
        stmt.execute();
        int time = stmt.getInt(1);
        stmt.close();
        System.out.println("⏰ Estimated Waiting Time for Queue ID " + queueId + " is: " + time + " minutes.");
    }

    public void prioritizeEmergency() throws Exception {
        Scanner sc = new Scanner(System.in);

        int queueId = 0;
        while (true) {
            System.out.print("👉 Enter Queue ID to prioritize as Emergency: ");
            try {
                queueId = Integer.parseInt(sc.nextLine().trim());
                if (queueId > 0) {
                    break;
                }
                System.out.println("⚠️ Error: Queue ID must be a positive integer.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
            }
        }

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call PrioritizeEmergency(?)}");
        stmt.setInt(1, queueId);
        stmt.execute();
        stmt.close();
        System.out.println("🚨 Queue record prioritized to EMERGENCY successfully!");
        Main.logActivity(1, "UPDATE", "queue");
    }
}