import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class Patient extends User {
    String healthId = "";
    String bloodGroup = "";
    String allergies = "";
    String emergencyContact = "";

    public void loadPatientDetails() throws Exception {
        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        PreparedStatement ps = DBConnection.conn.prepareStatement("SELECT * FROM patients WHERE user_id = ?");
        ps.setInt(1, this.userId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            this.healthId = rs.getString("health_id");
            this.bloodGroup = rs.getString("blood_group");
            this.emergencyContact = rs.getString("emergency_contact");
            this.name = rs.getString("first_name") + " " + rs.getString("last_name");
            this.phone = rs.getString("phone");

            // Fetch allergy from medical_history
            PreparedStatement ps2 = DBConnection.conn.prepareStatement("SELECT allergy FROM medical_history WHERE patient_id = ? LIMIT 1");
            ps2.setInt(1, rs.getInt("patient_id"));
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next()) {
                this.allergies = rs2.getString("allergy");
            } else {
                this.allergies = "None";
            }
            rs2.close();
            ps2.close();
        }
        rs.close();
        ps.close();
    }

    private int getPatientIdByUserId(int userId) throws Exception {
        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        PreparedStatement ps = DBConnection.conn.prepareStatement("SELECT patient_id FROM patients WHERE user_id = ?");
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        int patientId = 0;
        if (rs.next()) {
            patientId = rs.getInt("patient_id");
        }
        rs.close();
        ps.close();
        return patientId;
    }

    public int getPatientId() throws Exception {
        return getPatientIdByUserId(this.userId);
    }

    public void bookAppointment() throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n📅 --- Book Appointment ---");
        int patientId = getPatientIdByUserId(this.userId);
        if (patientId == 0) {
            System.out.println("⚠️ Patient profile not found.");
            return;
        }

        int doctorId = 0;
        while (true) {
            System.out.print("👨‍⚕️ Enter Doctor ID: ");
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

        String timeStr = "";
        while (true) {
            System.out.print("⏰ Enter Time (HH:MM:SS): ");
            timeStr = sc.nextLine().trim();
            boolean isValid = true;
            if (timeStr.length() != 8 || timeStr.charAt(2) != ':' || timeStr.charAt(5) != ':') {
                isValid = false;
            } else {
                for (int i = 0; i < timeStr.length(); i++) {
                    if (i == 2 || i == 5) continue;
                    char ch = timeStr.charAt(i);
                    if (ch < '0' || ch > '9') {
                        isValid = false;
                        break;
                    }
                }
                if (isValid) {
                    int hour = Integer.parseInt(timeStr.substring(0, 2));
                    int minute = Integer.parseInt(timeStr.substring(3, 5));
                    int second = Integer.parseInt(timeStr.substring(6, 8));

                    if (hour < 0 || hour > 23 || minute < 0 || minute > 59 || second < 0 || second > 59) {
                        isValid = false;
                    }
                }
            }
            if (isValid) {
                break;
            }
            System.out.println("⚠️ Error: Time must be a valid time in HH:MM:SS format.");
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
        System.out.print("💬 Enter Remarks: ");
        String remarks = sc.nextLine();

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        DBConnection.conn.setAutoCommit(false);
        try {
            // Call BookAppointment(IN p_pid INT, IN p_did INT, IN p_date DATE, IN p_time TIME)
            CallableStatement stmt = DBConnection.conn.prepareCall("{call BookAppointment(?, ?, ?, ?)}");
            stmt.setInt(1, patientId);
            stmt.setInt(2, doctorId);
            stmt.setString(3, dateStr);
            stmt.setString(4, timeStr);
            stmt.execute();
            stmt.close();

            // Retrieve generated appointment_id
            int appId = 0;
            PreparedStatement ps = DBConnection.conn.prepareStatement("SELECT appointment_id FROM appointments WHERE patient_id = ? AND doctor_id = ? ORDER BY appointment_id DESC LIMIT 1");
            ps.setInt(1, patientId);
            ps.setInt(2, doctorId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                appId = rs.getInt("appointment_id");
            }
            rs.close();
            ps.close();

            // Update priority and remarks in appointments table directly
            if (appId > 0) {
                PreparedStatement ps2 = DBConnection.conn.prepareStatement("UPDATE appointments SET priority = ?, remarks = ? WHERE appointment_id = ?");
                ps2.setString(1, priority);
                ps2.setString(2, remarks);
                ps2.setInt(3, appId);
                ps2.executeUpdate();
                ps2.close();
            }

            DBConnection.conn.commit();
            System.out.println("🎉 Appointment booked successfully!");
        } catch (Exception ex) {
            DBConnection.conn.rollback();
            throw ex;
        } finally {
            DBConnection.conn.setAutoCommit(true);
        }
    }

    public void viewMedicalHistory() throws Exception {
        int patientId = getPatientIdByUserId(this.userId);
        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call ViewMedicalHistory(?)}");
        stmt.setInt(1, patientId);
        ResultSet rs = stmt.executeQuery();
        System.out.println("\n📋 --- Medical History ---");
        boolean found = false;
        while (rs.next()) {
            found = true;
            System.out.println("🦠 Disease: " + rs.getString("disease"));
            System.out.println("🤧 Allergy: " + rs.getString("allergy"));
            System.out.println("🔪 Surgery: " + rs.getString("surgery"));
            System.out.println("👪 Family History: " + rs.getString("family_history"));
            System.out.println("📝 Description: " + rs.getString("description"));
            System.out.println("----------------------------------------");
        }
        if (!found) {
            System.out.println("📭 No medical history found.");
        }
        rs.close();
        stmt.close();
    }

    public void viewPrescription() throws Exception {
        int patientId = getPatientIdByUserId(this.userId);
        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call ViewPrescriptions(?)}");
        stmt.setInt(1, patientId);
        ResultSet rs = stmt.executeQuery();
        System.out.println("\n💊 --- Prescriptions ---");
        boolean found = false;
        while (rs.next()) {
            found = true;
            System.out.println("🔑 Prescription ID: " + rs.getInt("prescription_id"));
            System.out.println("🔬 Diagnosis      : " + rs.getString("diagnosis"));
            System.out.println("📝 Notes          : " + rs.getString("notes"));
            System.out.println("📅 Created Date   : " + rs.getString("created_date"));
            System.out.println("----------------------------------------");
        }
        if (!found) {
            System.out.println("📭 No prescriptions found.");
        }
        rs.close();
        stmt.close();
    }

    public void viewDietPlan() throws Exception {
        int patientId = getPatientIdByUserId(this.userId);
        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call ViewDietPlans(?)}");
        stmt.setInt(1, patientId);
        ResultSet rs = stmt.executeQuery();
        System.out.println("\n🥗 --- Diet Plan ---");
        boolean found = false;
        while (rs.next()) {
            found = true;
            System.out.println("🍳 Breakfast   : " + rs.getString("breakfast"));
            System.out.println("🍲 Lunch       : " + rs.getString("lunch"));
            System.out.println("🍛 Dinner      : " + rs.getString("dinner"));
            System.out.println("📝 Instructions: " + rs.getString("instructions"));
            System.out.println("----------------------------------------");
        }
        if (!found) {
            System.out.println("📭 No diet plan assigned.");
        }
        rs.close();
        stmt.close();
    }

    public void viewReports() throws Exception {
        int patientId = getPatientIdByUserId(this.userId);
        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call ViewLabResult(?)}");
        stmt.setInt(1, patientId);
        ResultSet rs = stmt.executeQuery();
        System.out.println("\n🧪 --- Lab / Medical Reports ---");
        boolean found = false;
        while (rs.next()) {
            found = true;
            System.out.println("🔑 Test ID    : " + rs.getInt("test_id"));
            System.out.println("🔬 Test Name  : " + rs.getString("test_name"));
            System.out.println("🚦 Status     : " + rs.getString("status"));
            System.out.println("📅 Test Date   : " + rs.getString("test_date"));
            System.out.println("📎 Report File : " + rs.getString("report_file"));
            System.out.println("----------------------------------------");
        }
        if (!found) {
            System.out.println("📭 No reports found.");
        }
        rs.close();
        stmt.close();
    }

    public void viewDashboard() throws Exception {
        new Dashboard().showPatientDashboard();
    }

    public void displayQueueStatus() throws Exception {
        int patientId = getPatientIdByUserId(this.userId);
        if (patientId == 0) {
            System.out.println("⚠️ Patient profile not found.");
            return;
        }

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }

        // 1. Find next booked appointment
        PreparedStatement ps = DBConnection.conn.prepareStatement(
                "SELECT a.appointment_id, a.doctor_id, d.name AS doctor_name, d.availability AS doctor_avail, " +
                        "a.appointment_date, a.appointment_time, a.token_number " +
                        "FROM appointments a " +
                        "JOIN doctors d ON a.doctor_id = d.doctor_id " +
                        "WHERE a.patient_id = ? AND a.status = 'Booked' AND a.appointment_date >= CURDATE() " +
                        "ORDER BY a.appointment_date ASC, a.appointment_time ASC LIMIT 1"
        );
        ps.setInt(1, patientId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            int appId = rs.getInt("appointment_id");
            int doctorId = rs.getInt("doctor_id");
            String doctorName = rs.getString("doctor_name");
            String doctorAvail = rs.getString("doctor_avail");
            java.sql.Date appDate = rs.getDate("appointment_date");
            java.sql.Time appTime = rs.getTime("appointment_time");
            int myToken = rs.getInt("token_number");

            System.out.println("\n🎟️ --- Live Queue & Next Appointment Info ---");
            System.out.println("👨‍⚕️ Doctor Name      : " + doctorName);
            System.out.println("⏰ Doctor Hours      : " + (doctorAvail != null && !doctorAvail.isEmpty() ? doctorAvail : "Not Specified"));
            System.out.println("📅 Appointment Date  : " + appDate);
            System.out.println("⏰ Appointment Time  : " + appTime);
            System.out.println("🎟️ Your Token Number : " + myToken);

            // 2. Which token number is being treated currently
            int treatedToken = -1;
            PreparedStatement psTreated = DBConnection.conn.prepareStatement(
                    "SELECT queue_number FROM queue WHERE doctor_id = ? AND status = 'In-progress' LIMIT 1"
            );
            psTreated.setInt(1, doctorId);
            ResultSet rsTreated = psTreated.executeQuery();
            if (rsTreated.next()) {
                treatedToken = rsTreated.getInt("queue_number");
            }
            rsTreated.close();
            psTreated.close();

            if (treatedToken == -1) {
                PreparedStatement psTreated2 = DBConnection.conn.prepareStatement(
                        "SELECT t.token_number FROM tokens t JOIN appointments a ON t.appointment_id = a.appointment_id " +
                                "WHERE a.doctor_id = ? AND t.status = 'Serving' LIMIT 1"
                );
                psTreated2.setInt(1, doctorId);
                ResultSet rsTreated2 = psTreated2.executeQuery();
                if (rsTreated2.next()) {
                    treatedToken = rsTreated2.getInt("token_number");
                }
                rsTreated2.close();
                psTreated2.close();
            }

            if (treatedToken != -1) {
                System.out.println("🩺 Token Being Treated: " + treatedToken);
            } else {
                System.out.println("🩺 Token Being Treated: None (No patient is currently in treatment)");
            }

            // 3. How much left before me in queue
            // Check if patient is in active queue
            PreparedStatement psQueue = DBConnection.conn.prepareStatement(
                    "SELECT queue_id, priority_level, arrival_time FROM queue WHERE patient_id = ? AND doctor_id = ? AND status = 'Waiting' LIMIT 1"
            );
            psQueue.setInt(1, patientId);
            psQueue.setInt(2, doctorId);
            ResultSet rsQueue = psQueue.executeQuery();
            boolean inQueue = false;
            int queueId = -1;
            String priorityLevel = "";
            java.sql.Timestamp arrivalTime = null;
            if (rsQueue.next()) {
                inQueue = true;
                queueId = rsQueue.getInt("queue_id");
                priorityLevel = rsQueue.getString("priority_level");
                arrivalTime = rsQueue.getTimestamp("arrival_time");
            }
            rsQueue.close();
            psQueue.close();

            int peopleAhead = 0;
            if (inQueue) {
                PreparedStatement psAhead = DBConnection.conn.prepareStatement(
                        "SELECT COUNT(*) FROM queue " +
                                "WHERE doctor_id = ? AND status = 'Waiting' " +
                                "AND (" +
                                "  FIELD(priority_level, 'Emergency','Pregnant','Senior Citizen','Child','Disabled','Normal') < FIELD(?, 'Emergency','Pregnant','Senior Citizen','Child','Disabled','Normal') " +
                                "  OR (" +
                                "    FIELD(priority_level, 'Emergency','Pregnant','Senior Citizen','Child','Disabled','Normal') = FIELD(?, 'Emergency','Pregnant','Senior Citizen','Child','Disabled','Normal') " +
                                "    AND arrival_time < ?" +
                                "  )" +
                                ")"
                );
                psAhead.setInt(1, doctorId);
                psAhead.setString(2, priorityLevel);
                psAhead.setString(3, priorityLevel);
                psAhead.setTimestamp(4, arrivalTime);
                ResultSet rsAhead = psAhead.executeQuery();
                if (rsAhead.next()) {
                    peopleAhead = rsAhead.getInt(1);
                }
                rsAhead.close();
                psAhead.close();
                System.out.println("🚶 People Ahead of You: " + peopleAhead + " (Active in live queue)");
            } else {
                PreparedStatement psAheadApp = DBConnection.conn.prepareStatement(
                        "SELECT COUNT(*) FROM appointments " +
                                "WHERE doctor_id = ? AND appointment_date = ? AND status = 'Booked' AND token_number < ?"
                );
                psAheadApp.setInt(1, doctorId);
                psAheadApp.setDate(2, appDate);
                psAheadApp.setInt(3, myToken);
                ResultSet rsAheadApp = psAheadApp.executeQuery();
                if (rsAheadApp.next()) {
                    peopleAhead = rsAheadApp.getInt(1);
                }
                rsAheadApp.close();
                psAheadApp.close();
                System.out.println("🚶 Booked Appointments Ahead of You: " + peopleAhead + " (Not yet checked-in to active queue)");
            }

            // 4. Time left for that particular doctor
            int waitingTime = 0;
            if (inQueue && queueId != -1) {
                PreparedStatement psTime = DBConnection.conn.prepareStatement("SELECT CalculateWaitingTime(?)");
                psTime.setInt(1, queueId);
                ResultSet rsTime = psTime.executeQuery();
                if (rsTime.next()) {
                    waitingTime = rsTime.getInt(1);
                }
                rsTime.close();
                psTime.close();
            } else {
                waitingTime = (peopleAhead + 1) * 15; // fallback estimation: 15 mins per ahead patient + self
            }
            System.out.println("⏰ Estimated Waiting Time: " + waitingTime + " minutes");
            System.out.println("----------------------------------------------");
        } else {
            System.out.println("📭 No upcoming booked appointments found.");
        }
        rs.close();
        ps.close();
    }
}