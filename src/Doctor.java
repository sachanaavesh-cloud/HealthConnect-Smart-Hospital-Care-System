import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class Doctor extends User {
    String specialization = "";
    int experience = 0;
    boolean available = true;

    public void loadDoctorDetails() throws Exception {
        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        PreparedStatement ps = DBConnection.conn.prepareStatement("SELECT * FROM doctors WHERE user_id = ?");
        ps.setInt(1, this.userId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            this.specialization = rs.getString("specialization");
            this.experience = rs.getInt("experience");
            this.available = rs.getString("availability") != null && !rs.getString("availability").isEmpty();
            this.name = rs.getString("name");
            this.phone = rs.getString("phone");
        }
        rs.close();
        ps.close();
    }

    private int getDoctorIdByUserId(int userId) throws Exception {
        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        PreparedStatement ps = DBConnection.conn.prepareStatement("SELECT doctor_id FROM doctors WHERE user_id = ?");
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        int doctorId = 0;
        if (rs.next()) {
            doctorId = rs.getInt("doctor_id");
        }
        rs.close();
        ps.close();
        return doctorId;
    }

    public int getDoctorId() throws Exception {
        return getDoctorIdByUserId(this.userId);
    }

    public void viewAppointments() throws Exception {
        int doctorId = getDoctorIdByUserId(this.userId);
        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call GetDoctorAppointments(?)}");
        stmt.setInt(1, doctorId);
        ResultSet rs = stmt.executeQuery();
        System.out.println("\n📅 --- Today's Appointments ---");
        boolean found = false;
        while (rs.next()) {
            found = true;
            System.out.println("🔑 Appointment ID: " + rs.getInt("appointment_id"));
            System.out.println("🧑‍⚕️ Patient ID    : " + rs.getInt("patient_id"));
            System.out.println("📅 Date          : " + rs.getString("appointment_date"));
            System.out.println("⏰ Time          : " + rs.getString("appointment_time"));
            System.out.println("🚦 Status        : " + rs.getString("status"));
            System.out.println("🎟️ Token Number  : " + rs.getInt("token_number"));
            System.out.println("🚨 Priority      : " + rs.getString("priority"));
            System.out.println("💬 Remarks       : " + rs.getString("remarks"));
            System.out.println("----------------------------------------");
        }
        if (!found) {
            System.out.println("📭 No appointments scheduled.");
        }
        rs.close();
        stmt.close();
    }

    public void createReport() throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n📝 --- Create Medical Report ---");

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

        System.out.print("📝 Enter Diagnosis/Description: ");
        String description = sc.nextLine();

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call CreateMedicalReport(?, ?)}");
        stmt.setInt(1, patientId);
        stmt.setString(2, description);
        stmt.execute();
        stmt.close();
        System.out.println("🎉 Medical Report created successfully!");
        Main.logActivity(this.userId, "INSERT", "medical_history");
    }

    public void createPrescription() throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n💊 --- Create Prescription ---");

        int patientId = 0;
        while (true) {
            System.out.print("🧑‍⚕️ Enter Patient ID: ");
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

        int visitId = 0;
        while (true) {
            System.out.print("📅 Enter Appointment ID (Visit ID): ");
            try {
                visitId = Integer.parseInt(sc.nextLine().trim());
                if (visitId > 0) {
                    break;
                }
                System.out.println("⚠️ Error: Appointment ID must be a positive integer.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
            }
        }

        System.out.print("🔬 Enter Diagnosis: ");
        String diagnosis = sc.nextLine();
        System.out.print("📝 Enter Notes: ");
        String notes = sc.nextLine();

        int doctorId = getDoctorIdByUserId(this.userId);
        int prescriptionId = 0;

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        DBConnection.conn.setAutoCommit(false);
        try {
            // Call CreatePrescription(IN p_pid INT, IN p_did INT, IN p_vid INT, IN p_diag VARCHAR(200))
            CallableStatement stmt = DBConnection.conn.prepareCall("{call CreatePrescription(?, ?, ?, ?)}");
            stmt.setInt(1, patientId);
            stmt.setInt(2, doctorId);
            stmt.setInt(3, visitId);
            stmt.setString(4, diagnosis);
            stmt.execute();
            stmt.close();

            // Retrieve the generated prescription_id
            PreparedStatement ps = DBConnection.conn.prepareStatement("SELECT prescription_id FROM prescriptions WHERE patient_id = ? AND doctor_id = ? ORDER BY prescription_id DESC LIMIT 1");
            ps.setInt(1, patientId);
            ps.setInt(2, doctorId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                prescriptionId = rs.getInt("prescription_id");
            }
            rs.close();
            ps.close();

            if (prescriptionId > 0) {
                // Update notes/remarks directly in prescriptions table since procedure doesn't accept notes
                PreparedStatement ps2 = DBConnection.conn.prepareStatement("UPDATE prescriptions SET notes = ? WHERE prescription_id = ?");
                ps2.setString(1, notes);
                ps2.setInt(2, prescriptionId);
                ps2.executeUpdate();
                ps2.close();

                System.out.println("✅ Prescription created (ID: " + prescriptionId + "). Now add medicines:");
                boolean addMore = true;
                while (addMore) {
                    System.out.print("💊 Enter Medicine Name: ");
                    String medName = sc.nextLine();
                    System.out.print("⚖️ Enter Dosage (e.g. 500mg): ");
                    String dosage = sc.nextLine();
                    System.out.print("☀️ Take in Morning? (true/false): ");
                    boolean morning = Boolean.parseBoolean(sc.nextLine());
                    System.out.print("🌤️ Take in Afternoon? (true/false): ");
                    boolean afternoon = Boolean.parseBoolean(sc.nextLine());
                    System.out.print("🌙 Take in Night? (true/false): ");
                    boolean night = Boolean.parseBoolean(sc.nextLine());

                    int days = 0;
                    while (true) {
                        System.out.print("📆 Enter Duration (in days): ");
                        try {
                            days = Integer.parseInt(sc.nextLine().trim());
                            if (days > 0) {
                                break;
                            }
                            System.out.println("⚠️ Error: Duration must be a positive integer.");
                        } catch (NumberFormatException e) {
                            System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
                        }
                    }

                    PreparedStatement ps3 = DBConnection.conn.prepareStatement(
                            "INSERT INTO prescription_medicines(prescription_id, medicine_name, dosage, morning, afternoon, night, days) VALUES (?, ?, ?, ?, ?, ?, ?)");
                    ps3.setInt(1, prescriptionId);
                    ps3.setString(2, medName);
                    ps3.setString(3, dosage);
                    ps3.setBoolean(4, morning);
                    ps3.setBoolean(5, afternoon);
                    ps3.setBoolean(6, night);
                    ps3.setInt(7, days);
                    ps3.executeUpdate();
                    ps3.close();

                    System.out.print("👉 Do you want to add another medicine? (yes/no): ");
                    String choice = sc.nextLine().trim().toLowerCase();
                    if (!choice.equals("yes") && !choice.equals("y")) {
                        addMore = false;
                    }
                }
            }

            DBConnection.conn.commit();
            System.out.println("🎉 Prescription completed successfully.");
        } catch (Exception ex) {
            DBConnection.conn.rollback();
            throw ex;
        } finally {
            DBConnection.conn.setAutoCommit(true);
        }
    }

    public void createDietPlan() throws Exception {
        new DietPlan().generateDiet();
    }

    public void replyPatient() throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n💬 --- Patient Discussions ---");

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

        System.out.print("💬 Enter Message: ");
        String msg = sc.nextLine();

        System.out.print("📅 Enter Next Visit Date (YYYY-MM-DD, or leave empty for none): ");
        String nextVisitInput = sc.nextLine().trim();

        int doctorId = getDoctorIdByUserId(this.userId);
        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call SendMessage(?, ?, ?, ?)}");
        stmt.setInt(1, patientId);
        stmt.setInt(2, doctorId);
        stmt.setString(3, msg);
        if (nextVisitInput.isEmpty()) {
            stmt.setNull(4, java.sql.Types.DATE);
        } else {
            stmt.setString(4, nextVisitInput);
        }
        stmt.execute();
        stmt.close();
        System.out.println("✅ Reply sent successfully.");
    }

    public void viewDashboard() throws Exception {
        new Dashboard().showDoctorDashboard();
    }

    public void showFollowUpsAndReply() throws Exception {
        int doctorId = getDoctorId();
        Scanner sc = new Scanner(System.in);

        while (true) {
            if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
                DBConnection.initialize();
            }
            PreparedStatement ps = DBConnection.conn.prepareStatement(
                    "SELECT f.followup_id, f.patient_id, CONCAT(p.first_name, ' ', p.last_name) AS patient_name, f.remarks, f.next_visit, f.status " +
                            "FROM follow_up f " +
                            "JOIN patients p ON f.patient_id = p.patient_id " +
                            "WHERE f.doctor_id = ? " +
                            "ORDER BY f.followup_id DESC");
            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();
            System.out.println("\n💬 --- Patient Follow-ups & Messages ---");
            System.out.printf("%-5s | %-20s | %-30s | %-12s | %-8s\n", "ID", "Patient", "Last Message", "Next Visit", "Status");
            System.out.println("--------------------------------------------------------------------------------------");
            boolean found = false;
            while (rs.next()) {
                found = true;
                int fId = rs.getInt("followup_id");
                int pId = rs.getInt("patient_id");
                String pName = rs.getString("patient_name");
                String msg = rs.getString("remarks");
                if (msg != null && msg.length() > 30) {
                    msg = msg.substring(0, 27) + "...";
                }
                java.sql.Date nextVisit = rs.getDate("next_visit");
                String nextVisitStr = (nextVisit != null) ? nextVisit.toString() : "None";
                String status = rs.getString("status");
                System.out.printf("%-5d | %-20s | %-30s | %-12s | %-8s\n", fId, pName + " (ID: " + pId + ")", msg, nextVisitStr, status);
            }
            if (!found) {
                System.out.println("📭 No follow-up messages found for you.");
            }
            rs.close();
            ps.close();

            System.out.print("\n👉 Enter Followup ID to reply (or enter 0 to go back): ");
            int followUpId = 0;
            try {
                followUpId = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error: Invalid input. Please enter a valid number.");
                continue;
            }

            if (followUpId == 0) {
                break;
            }

            // Verify this follow-up exists and belongs to this doctor
            PreparedStatement psCheck = DBConnection.conn.prepareStatement(
                    "SELECT f.*, CONCAT(p.first_name, ' ', p.last_name) AS patient_name FROM follow_up f JOIN patients p ON f.patient_id = p.patient_id WHERE f.followup_id = ? AND f.doctor_id = ?");
            psCheck.setInt(1, followUpId);
            psCheck.setInt(2, doctorId);
            ResultSet rsCheck = psCheck.executeQuery();
            if (rsCheck.next()) {
                int patientId = rsCheck.getInt("patient_id");
                String patientName = rsCheck.getString("patient_name");
                String originalMsg = rsCheck.getString("remarks");
                System.out.println("\n----------------------------------------");
                System.out.println("💬 Chat Details with " + patientName + " (Patient ID: " + patientId + ")");
                System.out.println("📝 Patient Message: " + originalMsg);
                System.out.println("----------------------------------------");

                // Show full conversation history with this patient
                CallableStatement stmtConv = DBConnection.conn.prepareCall("{call ViewConversation(?, ?)}");
                stmtConv.setInt(1, patientId);
                stmtConv.setInt(2, doctorId);
                ResultSet rsConv = stmtConv.executeQuery();
                System.out.println("\n💬 Full Chat History:");
                while (rsConv.next()) {
                    System.out.println("  • " + rsConv.getString("remarks"));
                }
                rsConv.close();
                stmtConv.close();
                System.out.println("----------------------------------------");

                System.out.print("💬 Enter Reply Message: ");
                String replyMsg = sc.nextLine();

                System.out.print("📅 Enter Next Visit Date (YYYY-MM-DD, or leave empty for none): ");
                String nextVisitInput = sc.nextLine().trim();

                // Send the reply message
                CallableStatement stmtSend = DBConnection.conn.prepareCall("{call SendMessage(?, ?, ?, ?)}");
                stmtSend.setInt(1, patientId);
                stmtSend.setInt(2, doctorId);
                stmtSend.setString(3, "🧑‍⚕️ Doctor Reply: " + replyMsg);
                if (nextVisitInput.isEmpty()) {
                    stmtSend.setNull(4, java.sql.Types.DATE);
                } else {
                    stmtSend.setString(4, nextVisitInput);
                }
                stmtSend.execute();
                stmtSend.close();

                // Mark original follow-up as Done
                PreparedStatement psUpdate = DBConnection.conn.prepareStatement("UPDATE follow_up SET status = 'Done' WHERE followup_id = ?");
                psUpdate.setInt(1, followUpId);
                psUpdate.executeUpdate();
                psUpdate.close();

                System.out.println("🎉 Reply sent and follow-up marked as Completed!");
            } else {
                System.out.println("⚠️ Error: Follow-up ID not found or doesn't belong to you.");
            }
            rsCheck.close();
            psCheck.close();
        }
    }

    public void showNextAppointment() throws Exception {
        int doctorId = getDoctorId();
        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        PreparedStatement ps = DBConnection.conn.prepareStatement(
                "SELECT a.appointment_id, a.patient_id, CONCAT(p.first_name, ' ', p.last_name) AS patient_name, " +
                        "a.appointment_date, a.appointment_time, a.status, a.token_number, a.priority, a.remarks " +
                        "FROM appointments a " +
                        "JOIN patients p ON a.patient_id = p.patient_id " +
                        "WHERE a.doctor_id = ? AND a.status = 'Booked' AND a.appointment_date >= CURDATE() " +
                        "ORDER BY a.appointment_date ASC, a.token_number ASC"
        );
        ps.setInt(1, doctorId);
        ResultSet rs = ps.executeQuery();
        System.out.println("\n📅 --- Next / Upcoming Appointments (Sorted by Token Number) ---");
        boolean found = false;
        while (rs.next()) {
            found = true;
            System.out.println("🎟️ Token Number  : " + rs.getInt("token_number"));
            System.out.println("🔑 Appointment ID: " + rs.getInt("appointment_id"));
            System.out.println("👤 Patient Name  : " + rs.getString("patient_name") + " (ID: " + rs.getInt("patient_id") + ")");
            System.out.println("📅 Date          : " + rs.getString("appointment_date"));
            System.out.println("⏰ Time          : " + rs.getString("appointment_time"));
            System.out.println("🚨 Priority      : " + rs.getString("priority"));
            System.out.println("💬 Remarks       : " + rs.getString("remarks"));
            System.out.println("----------------------------------------------------------------");
        }
        if (!found) {
            System.out.println("📭 No upcoming booked appointments found.");
        }
        rs.close();
        ps.close();
    }
}