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
                case "1":
                    priority = "Emergency";
                    break;
                case "2":
                    priority = "Pregnant";
                    break;
                case "3":
                    priority = "Senior Citizen";
                    break;
                case "4":
                    priority = "Child";
                    break;
                case "5":
                    priority = "Disabled";
                    break;
                case "6":
                    priority = "Normal";
                    break;
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
        PreparedStatement checkPs = DBConnection.conn.prepareStatement("SELECT queue_id, status FROM queue WHERE queue_id = ?");
        checkPs.setInt(1, queueId);
        ResultSet checkRs = checkPs.executeQuery();
        if (!checkRs.next()) {
            System.out.println("⚠️ Error: Queue ID " + queueId + " does not exist. Please insert a valid Queue ID.");
            checkRs.close();
            checkPs.close();
            return;
        }
        String qStatus = checkRs.getString("status");
        checkRs.close();
        checkPs.close();

        if (qStatus != null && (qStatus.equalsIgnoreCase("Served") || qStatus.equalsIgnoreCase("Completed"))) {
            System.out.println("ℹ️ Notice: Patient for Queue ID " + queueId + " has already been served. Estimated waiting time: 0 minutes.");
            return;
        }
        PreparedStatement psPos = DBConnection.conn.prepareStatement(
                "SELECT COUNT(*) FROM queue q1 " +
                        "JOIN queue q2 ON q2.queue_id = ? " +
                        "WHERE q1.doctor_id = q2.doctor_id AND q1.status IN ('Waiting', 'In-progress') " +
                        "AND (FIELD(q1.priority_level, 'Emergency','Pregnant','Senior Citizen','Child','Disabled','Normal') < FIELD(q2.priority_level, 'Emergency','Pregnant','Senior Citizen','Child','Disabled','Normal') " +
                        "OR (FIELD(q1.priority_level, 'Emergency','Pregnant','Senior Citizen','Child','Disabled','Normal') = FIELD(q2.priority_level, 'Emergency','Pregnant','Senior Citizen','Child','Disabled','Normal') AND q1.queue_id <= q2.queue_id))"
        );
        psPos.setInt(1, queueId);
        ResultSet rsPos = psPos.executeQuery();
        int time = 0;
        if (rsPos.next()) {
            int pos = rsPos.getInt(1);
            time = (pos > 0 ? pos : 1) * 15;
        } else {
            time = 15;
        }
        rsPos.close();
        psPos.close();
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