import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;

public class QueueManager {
    Queue<Appointment> queue = new PriorityQueue<>();

    public void generateToken() throws Exception {
        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{? = call GenerateQueueToken()}");
        stmt.registerOutParameter(1, Types.INTEGER);
        stmt.execute();
        int token = stmt.getInt(1);
        stmt.close();
        System.out.println("🎟️ Generated Queue Token Number: " + token);
    }

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

        System.out.print("🚨 Enter Priority (Emergency/Pregnant/Senior Citizen/Child/Disabled/Normal): ");
        String priority = sc.nextLine();

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