import java.sql.*;
import java.util.*;

// Patient class for managing patient-related operations.
public class Patient extends User {
    String healthId = "";
    String bloodGroup = "";
    String allergies = "";
    String emergencyContact = "";

    // Loads patient details from the database.
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

    // Retrieves patient ID using the logged-in user ID.
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

    // Returns the patient ID of the current user.
    public int getPatientId() throws Exception {
        return getPatientIdByUserId(this.userId);
    }

    // Books a new appointment for the patient.
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
            if (choiceOpt.equals("1")) {
                priority = "Emergency";
                break;
            } else if (choiceOpt.equals("2")) {
                priority = "Pregnant";
                break;
            } else if (choiceOpt.equals("3")) {
                priority = "Senior Citizen";
                break;
            } else if (choiceOpt.equals("4")) {
                priority = "Child";
                break;
            } else if (choiceOpt.equals("5")) {
                priority = "Disabled";
                break;
            } else if (choiceOpt.equals("6")) {
                priority = "Normal";
                break;
            } else {
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

            // If appointment date is today, auto-add to live queue
            if (dateStr.equals(java.time.LocalDate.now().toString())) {
                PreparedStatement psQCheck = DBConnection.conn.prepareStatement(
                        "SELECT queue_id FROM queue WHERE patient_id = ? AND doctor_id = ? AND status = 'Waiting'");
                psQCheck.setInt(1, patientId);
                psQCheck.setInt(2, doctorId);
                ResultSet rsQ = psQCheck.executeQuery();
                if (!rsQ.next()) {
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

                    PreparedStatement psQAdd = DBConnection.conn.prepareStatement(
                            "INSERT INTO queue (patient_id, doctor_id, priority_level, queue_number, status, arrival_time) VALUES (?, ?, ?, ?, 'Waiting', NOW())");
                    psQAdd.setInt(1, patientId);
                    psQAdd.setInt(2, doctorId);
                    psQAdd.setString(3, priority);
                    psQAdd.setInt(4, nextQNum);
                    psQAdd.executeUpdate();
                    psQAdd.close();
                }
                rsQ.close();
                psQCheck.close();
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

    // Displays the patient's medical history.
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

    // Displays all prescriptions and medicines of the patient.
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
            int prescriptionId = rs.getInt("prescription_id");
            System.out.println("🔑 Prescription ID: " + prescriptionId);
            System.out.println("🔬 Diagnosis      : " + rs.getString("diagnosis"));
            System.out.println("📝 Notes          : " + (rs.getString("notes") != null ? rs.getString("notes") : "-"));
            System.out.println("📅 Created Date   : " + rs.getString("created_date"));

            PreparedStatement psMeds = DBConnection.conn.prepareStatement(
                    "SELECT medicine_name, dosage, morning, afternoon, night, days FROM prescription_medicines WHERE prescription_id = ?");
            psMeds.setInt(1, prescriptionId);
            ResultSet rsMeds = psMeds.executeQuery();
            System.out.println("   💊 Prescribed Medicines:");
            boolean hasMeds = false;
            while (rsMeds.next()) {
                hasMeds = true;
                String medName = rsMeds.getString("medicine_name");
                String dosage = rsMeds.getString("dosage");
                boolean m = rsMeds.getBoolean("morning");
                boolean a = rsMeds.getBoolean("afternoon");
                boolean n = rsMeds.getBoolean("night");
                int days = rsMeds.getInt("days");

                StringBuilder sched = new StringBuilder();
                if (m) sched.append("☀️ Morning ");
                if (a) sched.append("🌤️ Afternoon ");
                if (n) sched.append("🌙 Night ");
                if (sched.length() == 0) sched.append("As Needed");

                System.out.printf("      • %-20s | Dosage: %-10s | Schedule: %-30s | Duration: %d days\n", medName, dosage, sched.toString().trim(), days);
            }
            if (!hasMeds) {
                System.out.println("      (No specific medicines listed)");
            }
            rsMeds.close();
            psMeds.close();
            System.out.println("-----------------------------------------------------------------------------------------");
        }
        if (!found) {
            System.out.println("📭 No prescriptions found.");
        }
        rs.close();
        stmt.close();
    }

    // Displays the assigned diet plans.
    public void viewDietPlan() throws Exception {
        int patientId = getPatientIdByUserId(this.userId);
        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call ViewDietPlans(?)}");
        stmt.setInt(1, patientId);
        ResultSet rs = stmt.executeQuery();
        System.out.println("\n🥗 --- Assigned Diet Plan Details ---");
        boolean found = false;
        java.util.HashSet<String> displayedDiseases = new java.util.HashSet<>();

        while (rs.next()) {
            String inst = rs.getString("instructions");
            String breakfast = rs.getString("breakfast");

            String diseaseAssigned = getCleanDiseaseForDiet(patientId, inst, breakfast, DBConnection.conn);

            // Deduplicate: If diet for this disease is already displayed, skip duplicates
            if (displayedDiseases.contains(diseaseAssigned.toLowerCase())) {
                continue;
            }
            displayedDiseases.add(diseaseAssigned.toLowerCase());
            found = true;

            String cleanInstructions = (inst != null) ? inst : "-";
            if (cleanInstructions.contains(" | Avoid:")) {
                cleanInstructions = cleanInstructions.substring(cleanInstructions.indexOf(" | Avoid:") + 3).trim();
            } else if (cleanInstructions.contains("Avoid:")) {
                cleanInstructions = cleanInstructions.substring(cleanInstructions.indexOf("Avoid:")).trim();
            }

            System.out.println("🔬 Assigned For Disease: " + diseaseAssigned);
            System.out.println("🍳 Breakfast            : " + rs.getString("breakfast"));
            System.out.println("🍲 Lunch                : " + rs.getString("lunch"));
            System.out.println("🍿 Evening Snacks       : " + (rs.getString("snacks") != null ? rs.getString("snacks") : "-"));
            System.out.println("🍛 Dinner               : " + rs.getString("dinner"));
            System.out.println("💧 Daily Water          : " + (rs.getString("water") != null ? rs.getString("water") : "-"));
            System.out.println("📝 Instructions         : " + cleanInstructions);
            System.out.println("------------------------------------------------------------------");
        }
        if (!found) {
            System.out.println("📭 No diet plan assigned.");
        }
        rs.close();
        stmt.close();
    }

    // Identifies the disease associated with a diet plan.
    private static String getCleanDiseaseForDiet(int patientId, String inst, String breakfast, java.sql.Connection conn) {
        if (inst != null && inst.contains("Target Disease/Condition:")) {
            int start = inst.indexOf("Target Disease/Condition:") + "Target Disease/Condition:".length();
            int end = inst.indexOf(" | ", start);
            String tag = (end != -1) ? inst.substring(start, end).trim() : inst.substring(start).trim();
            if (!tag.isEmpty() && !tag.equalsIgnoreCase("General Health")) {
                return normalizeDiseaseName(tag);
            }
        } else if (inst != null && inst.contains("Target Condition:")) {
            int start = inst.indexOf("Target Condition:") + "Target Condition:".length();
            int end = inst.indexOf(" | ", start);
            String tag = (end != -1) ? inst.substring(start, end).trim() : inst.substring(start).trim();
            if (!tag.isEmpty() && !tag.equalsIgnoreCase("General Health")) {
                return normalizeDiseaseName(tag);
            }
        }

        String combined = ((inst != null ? inst : "") + " " + (breakfast != null ? breakfast : "")).toLowerCase();
        if (combined.contains("salt") || combined.contains("bp") || combined.contains("hypertension") || combined.contains("pressure") || combined.contains("low-sodium")) {
            return "High BP";
        }
        if (combined.contains("diabet") || combined.contains("sweet") || combined.contains("sugar") || combined.contains("thepla") || combined.contains("ragi")) {
            return "Diabetes";
        }
        if (combined.contains("dengue") || combined.contains("papaya") || combined.contains("kiwi") || combined.contains("coconut")) {
            return "Dengue";
        }
        if (combined.contains("kidney") || combined.contains("potassium") || combined.contains("renal")) {
            return "Kidney Disease";
        }
        if (combined.contains("pregnan") || combined.contains("sprout") || combined.contains("folic")) {
            return "Pregnancy";
        }

        try {
            PreparedStatement psHist = conn.prepareStatement(
                    "SELECT disease FROM medical_history WHERE patient_id = ? AND disease IS NOT NULL AND TRIM(disease) != '' ORDER BY history_id DESC LIMIT 1");
            psHist.setInt(1, patientId);
            ResultSet rsHist = psHist.executeQuery();
            if (rsHist.next()) {
                String d = rsHist.getString("disease").trim();
                rsHist.close();
                psHist.close();
                if (!d.isEmpty()) {
                    return normalizeDiseaseName(d);
                }
            } else {
                rsHist.close();
                psHist.close();
            }
        } catch (Exception e) {
        }

        return "General Health";
    }

    // Formats disease names into a standard form.
    private static String normalizeDiseaseName(String raw) {
        String lower = raw.toLowerCase();
        if (lower.contains("bp") || lower.contains("hypertension") || lower.contains("pressure")) {
            return "High BP";
        }
        if (lower.contains("diabet") || lower.contains("sugar")) {
            return "Diabetes";
        }
        if (lower.contains("dengue")) {
            return "Dengue";
        }
        if (lower.contains("kidney")) {
            return "Kidney Disease";
        }
        if (lower.contains("pregnan")) {
            return "Pregnancy";
        }
        return raw;
    }

    // Displays the patient's lab and medical reports.
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

    // Opens the patient dashboard.
    public void viewDashboard() throws Exception {
        new Dashboard().showPatientDashboard();
    }
}