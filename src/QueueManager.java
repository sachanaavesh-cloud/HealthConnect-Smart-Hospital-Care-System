import java.sql.*;
import java.util.*;

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

        String priority = "";
        while (true) {
            System.out.println("🚨 Choose Priority:");
            System.out.println("1. Emergency");
            System.out.println("2. Pregnant");
            System.out.println("3. Senior Citizen");
            System.out.println("4. Child");
            System.out.println("5. Disabled");
            System.out.println("6. Normal");
            System.out.print("👉 Enter choice (1-6): ");
            String choiceOpt = sc.nextLine().trim();
            if (choiceOpt.equals("1")) { priority = "Emergency"; break; }
            else if (choiceOpt.equals("2")) { priority = "Pregnant"; break; }
            else if (choiceOpt.equals("3")) { priority = "Senior Citizen"; break; }
            else if (choiceOpt.equals("4")) { priority = "Child"; break; }
            else if (choiceOpt.equals("5")) { priority = "Disabled"; break; }
            else if (choiceOpt.equals("6")) { priority = "Normal"; break; }
            else {
                System.out.println("⚠️ Error: Invalid option. Please choose between 1 and 6.");
            }
        }

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }

        // Auto-assign sequential queue_number for the doctor for today
        int nextQNum = 1;
        PreparedStatement psNum = DBConnection.conn.prepareStatement(
                "SELECT COALESCE(MAX(queue_number), 0) + 1 FROM queue WHERE doctor_id = ? AND DATE(arrival_time) = CURDATE()");
        psNum.setInt(1, doctorId);
        ResultSet rsNum = psNum.executeQuery();
        if (rsNum.next()) {
            nextQNum = rsNum.getInt(1);
        }
        rsNum.close();
        psNum.close();

        // 1. Ensure an appointment record exists for today
        PreparedStatement psAppCheck = DBConnection.conn.prepareStatement(
                "SELECT appointment_id FROM appointments WHERE patient_id = ? AND doctor_id = ? AND appointment_date = CURDATE() AND status = 'Booked'");
        psAppCheck.setInt(1, patientId);
        psAppCheck.setInt(2, doctorId);
        ResultSet rsApp = psAppCheck.executeQuery();
        if (!rsApp.next()) {
            PreparedStatement psAppIns = DBConnection.conn.prepareStatement(
                    "INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, status, token_number, priority, booking_type, remarks) VALUES (?, ?, CURDATE(), CURTIME(), 'Booked', ?, ?, 'Walk-in', 'Walk-in Queue Entry')");
            psAppIns.setInt(1, patientId);
            psAppIns.setInt(2, doctorId);
            psAppIns.setInt(3, nextQNum);
            psAppIns.setString(4, priority);
            psAppIns.executeUpdate();
            psAppIns.close();
        }
        rsApp.close();
        psAppCheck.close();

        // 2. Insert into live queue
        PreparedStatement psAdd = DBConnection.conn.prepareStatement(
                "INSERT INTO queue (patient_id, doctor_id, priority_level, queue_number, status, arrival_time) VALUES (?, ?, ?, ?, 'Waiting', NOW())");
        psAdd.setInt(1, patientId);
        psAdd.setInt(2, doctorId);
        psAdd.setString(3, priority);
        psAdd.setInt(4, nextQNum);
        psAdd.executeUpdate();
        psAdd.close();

        System.out.println("🎉 Patient added to queue & appointment recorded successfully! (Queue #" + nextQNum + ")");
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

        PreparedStatement psCheck = DBConnection.conn.prepareStatement("SELECT queue_id FROM queue WHERE queue_id = ? AND status = 'Waiting'");
        psCheck.setInt(1, queueId);
        ResultSet rsCheck = psCheck.executeQuery();
        if (!rsCheck.next()) {
            rsCheck.close();
            psCheck.close();
            System.out.println("❌ Queue ID " + queueId + " not found in active waiting queue.");
            return;
        }
        rsCheck.close();
        psCheck.close();

        CallableStatement stmt = DBConnection.conn.prepareCall("{call RemovePatientFromQueue(?)}");
        stmt.setInt(1, queueId);
        stmt.execute();
        stmt.close();
        System.out.println("✅ Patient removed from queue (Status: Served).");
        Main.logActivity(1, "DELETE", "queue");
    }

    public void displayQueue() throws Exception {
        Scanner sc = new Scanner(System.in);
        int doctorId = 0;

        if (Main.loggedInUser instanceof Doctor) {
            doctorId = ((Doctor) Main.loggedInUser).getDoctorId();
        } else {
            System.out.print("👨‍⚕️ Enter Doctor ID to filter queue (or press Enter / 0 for All Doctors): ");
            String input = sc.nextLine().trim();
            try {
                if (!input.isEmpty()) {
                    doctorId = Integer.parseInt(input);
                }
            } catch (NumberFormatException e) {
                doctorId = 0;
            }
        }

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }

        PreparedStatement ps;
        if (doctorId > 0) {
            ps = DBConnection.conn.prepareStatement(
                    "SELECT queue_id, patient_id, doctor_id, priority_level, queue_number, arrival_time FROM queue WHERE doctor_id = ? AND status = 'Waiting' ORDER BY queue_number ASC, arrival_time ASC");
            ps.setInt(1, doctorId);
        } else {
            ps = DBConnection.conn.prepareStatement(
                    "SELECT queue_id, patient_id, doctor_id, priority_level, queue_number, arrival_time FROM queue WHERE status = 'Waiting' ORDER BY doctor_id ASC, queue_number ASC, arrival_time ASC");
        }

        ResultSet rs = ps.executeQuery();
        if (doctorId > 0) {
            System.out.println("\n🚦 --- Active Live Queue for Doctor ID " + doctorId + " ---");
        } else {
            System.out.println("\n🚦 --- Active Live Queue (All Doctors) ---");
        }
        System.out.printf("%-10s | %-10s | %-12s | %-12s | %-14s | %-20s\n", "Queue ID", "Queue #", "Patient ID", "Doctor ID", "Priority", "Arrival Time");
        System.out.println("-----------------------------------------------------------------------------------------");
        boolean found = false;
        while (rs.next()) {
            found = true;
            System.out.printf("%-10d | %-10d | %-12d | %-12d | %-14s | %-20s\n",
                    rs.getInt("queue_id"),
                    rs.getInt("queue_number"),
                    rs.getInt("patient_id"),
                    rs.getInt("doctor_id"),
                    rs.getString("priority_level"),
                    rs.getTimestamp("arrival_time") != null ? rs.getTimestamp("arrival_time").toString() : "-"
            );
        }
        if (!found) {
            if (doctorId > 0) {
                System.out.println("📭 No patients waiting in queue for Doctor ID " + doctorId + ".");
            } else {
                System.out.println("📭 No patients waiting in queue.");
            }
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

        PreparedStatement psCheck = DBConnection.conn.prepareStatement("SELECT queue_id FROM queue WHERE queue_id = ? AND status = 'Waiting'");
        psCheck.setInt(1, queueId);
        ResultSet rsCheck = psCheck.executeQuery();
        if (!rsCheck.next()) {
            rsCheck.close();
            psCheck.close();
            System.out.println("❌ Queue ID " + queueId + " not found in active waiting queue.");
            return;
        }
        rsCheck.close();
        psCheck.close();

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

        PreparedStatement psCheck = DBConnection.conn.prepareStatement(
                "SELECT doctor_id, queue_number, priority_level FROM queue WHERE queue_id = ? AND status = 'Waiting'");
        psCheck.setInt(1, queueId);
        ResultSet rsCheck = psCheck.executeQuery();
        int doctorId = 0;
        int currentQNum = 0;
        if (rsCheck.next()) {
            doctorId = rsCheck.getInt("doctor_id");
            currentQNum = rsCheck.getInt("queue_number");
        } else {
            rsCheck.close();
            psCheck.close();
            System.out.println("❌ Queue ID " + queueId + " not found in active waiting queue.");
            return;
        }
        rsCheck.close();
        psCheck.close();

        // Calculate top position for emergency patient under this doctor
        PreparedStatement psEmergCount = DBConnection.conn.prepareStatement(
                "SELECT COUNT(*) FROM queue WHERE doctor_id = ? AND status = 'Waiting' AND priority_level = 'Emergency' AND queue_id != ?");
        psEmergCount.setInt(1, doctorId);
        psEmergCount.setInt(2, queueId);
        ResultSet rsEmerg = psEmergCount.executeQuery();
        int newQNum = 1;
        if (rsEmerg.next()) {
            newQNum = rsEmerg.getInt(1) + 1;
        }
        rsEmerg.close();
        psEmergCount.close();

        // Shift queue numbers of other patients between newQNum and currentQNum
        if (currentQNum > newQNum) {
            PreparedStatement psShift = DBConnection.conn.prepareStatement(
                    "UPDATE queue SET queue_number = queue_number + 1 WHERE doctor_id = ? AND status = 'Waiting' AND queue_number >= ? AND queue_number < ? AND queue_id != ?");
            psShift.setInt(1, doctorId);
            psShift.setInt(2, newQNum);
            psShift.setInt(3, currentQNum);
            psShift.setInt(4, queueId);
            psShift.executeUpdate();
            psShift.close();
        }

        // Update target queue entry
        PreparedStatement psUpd = DBConnection.conn.prepareStatement(
                "UPDATE queue SET priority_level = 'Emergency', queue_number = ? WHERE queue_id = ?");
        psUpd.setInt(1, newQNum);
        psUpd.setInt(2, queueId);
        psUpd.executeUpdate();
        psUpd.close();

        System.out.println("🚨 Queue ID " + queueId + " prioritized to EMERGENCY successfully! (Promoted to Queue #" + newQNum + ")");
        Main.logActivity(1, "UPDATE", "queue");
    }
}