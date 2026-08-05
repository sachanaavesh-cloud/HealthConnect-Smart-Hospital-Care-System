import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;

public class User {
    int userId = 0;
    String name = "";
    String email = "";
    String password = "";
    String phone = "";
    String role = "";

    public void login() throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.print("📧 Enter Username: ");
        String username = sc.nextLine().trim();
        System.out.print("🔑 Enter Password: ");
        String pwd = sc.nextLine();

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call LoginUser(?, ?)}");
        stmt.setString(1, username);
        stmt.setString(2, pwd);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            String userStatus = rs.getString("status");
            if ("Inactive".equalsIgnoreCase(userStatus)) {
                System.out.println("❌ Your account is inactive. Access denied.");
                rs.close();
                stmt.close();
                return;
            }
            this.userId = rs.getInt("user_id");
            this.email = rs.getString("username");
            this.role = rs.getString("role");
            this.password = pwd;
            System.out.println("🎉 Login successful! Welcome, " + this.email + " (" + this.role + ")");

            // Log login activity in login_history table
            PreparedStatement ps = DBConnection.conn.prepareStatement("INSERT INTO login_history(user_id, login_time, ip_address) VALUES (?, NOW(), '127.0.0.1')");
            ps.setInt(1, this.userId);
            ps.executeUpdate();
            ps.close();

            Main.logActivity(this.userId, "LOGIN", "users");
        } else {
            System.out.println("❌ Invalid username or password.");
        }
        rs.close();
        stmt.close();
    }

    public void logout() throws Exception {
        if (this.userId == 0) {
            System.out.println("⚠️ No user is currently logged in.");
            return;
        }
        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        PreparedStatement ps = DBConnection.conn.prepareStatement("UPDATE login_history SET logout_time = NOW() WHERE user_id = ? AND logout_time IS NULL");
        ps.setInt(1, this.userId);
        ps.executeUpdate();
        ps.close();
        System.out.println("🚪 Logged out successfully.");
        Main.logActivity(this.userId, "LOGOUT", "users");
        this.userId = 0;
        this.name = "";
        this.email = "";
        this.password = "";
        this.phone = "";
        this.role = "";
    }

    public void updateProfile() throws Exception {
        if (this.userId == 0) {
            System.out.println("⚠️ Please login first.");
            return;
        }
        Scanner sc = new Scanner(System.in);

        String newName = "";
        while (true) {
            System.out.print("👤 Enter New Name: ");
            newName = sc.nextLine().trim();
            boolean isValid = true;
            if (newName.isEmpty()) {
                isValid = false;
            } else {
                for (int i = 0; i < newName.length(); i++) {
                    char ch = newName.charAt(i);
                    if (!((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == ' ')) {
                        isValid = false;
                        break;
                    }
                }
            }
            if (isValid) {
                break;
            }
            System.out.println("⚠️ Error: Name must contain letters and spaces only.");
        }

        String newPhone = "";
        while (true) {
            System.out.print("📞 Enter New Phone: ");
            newPhone = sc.nextLine().trim();
            boolean isValid = true;
            if (newPhone.length() != 10) {
                isValid = false;
            } else {
                for (int i = 0; i < newPhone.length(); i++) {
                    char ch = newPhone.charAt(i);
                    if (ch < '0' || ch > '9') {
                        isValid = false;
                        break;
                    }
                }
            }
            if (isValid) {
                break;
            }
            System.out.println("⚠️ Error: Phone number must be exactly 10 digits.");
        }

        String newEmail = "";
        while (true) {
            System.out.print("📧 Enter New Email: ");
            newEmail = sc.nextLine().trim();
            int atIndex = newEmail.indexOf('@');
            int dotIndex = newEmail.lastIndexOf('.');
            boolean isValid = (!newEmail.isEmpty() && atIndex > 0 && dotIndex > atIndex + 1 && dotIndex < newEmail.length() - 1);
            if (isValid) {
                break;
            }
            System.out.println("⚠️ Error: Please enter a valid email address.");
        }

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        DBConnection.conn.setAutoCommit(false);
        try {

            // Update respective table based on role
            if (this.role.equalsIgnoreCase("Patient")) {
                PreparedStatement ps1 = DBConnection.conn.prepareStatement("UPDATE patients SET first_name = ?, phone = ?, email = ? WHERE user_id = ?");
                ps1.setString(1, newName);
                ps1.setString(2, newPhone);
                ps1.setString(3, newEmail);
                ps1.setInt(4, this.userId);
                ps1.executeUpdate();
                ps1.close();
            } else if (this.role.equalsIgnoreCase("Doctor")) {
                PreparedStatement ps2 = DBConnection.conn.prepareStatement("UPDATE doctors SET name = ?, phone = ?, email = ? WHERE user_id = ?");
                ps2.setString(1, newName);
                ps2.setString(2, newPhone);
                ps2.setString(3, newEmail);
                ps2.setInt(4, this.userId);
                ps2.executeUpdate();
                ps2.executeUpdate();
                ps2.close();
            }

            // Call UpdateUserProfile procedure
            CallableStatement stmt = DBConnection.conn.prepareCall("{call UpdateUserProfile(?, ?)}");
            stmt.setInt(1, this.userId);
            stmt.setString(2, "Active");
            stmt.execute();
            stmt.close();

            DBConnection.conn.commit();
            this.name = newName;
            this.phone = newPhone;
            this.email = newEmail;
            System.out.println("✅ Profile updated successfully.");
        } catch (Exception ex) {
            DBConnection.conn.rollback();
            throw ex;
        } finally {
            DBConnection.conn.setAutoCommit(true);
        }
    }

    public void changePassword() throws Exception {
        if (this.userId == 0) {
            System.out.println("⚠️ Please login first.");
            return;
        }
        Scanner sc = new Scanner(System.in);
        System.out.print("🔑 Enter Old Password: ");
        String oldPwd = sc.nextLine();
        System.out.print("🔑 Enter New Password: ");
        String newPwd = sc.nextLine();

        if (newPwd.trim().isEmpty()) {
            System.out.println("⚠️ New password cannot be empty.");
            return;
        }

        if (oldPwd.equals(newPwd)) {
            System.out.println("⚠️ Error: New password cannot be the same as the old password.");
            return;
        }

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        // Check old password first
        boolean oldPwdCorrect = false;
        PreparedStatement ps = DBConnection.conn.prepareStatement("SELECT password FROM users WHERE user_id = ?");
        ps.setInt(1, this.userId);
        ResultSet rs = ps.executeQuery();
        if (rs.next() && rs.getString("password").equals(oldPwd)) {
            oldPwdCorrect = true;
        }
        rs.close();
        ps.close();

        if (!oldPwdCorrect) {
            System.out.println("❌ Old password incorrect. Password was not changed.");
            return;
        }

        // Call ChangePassword procedure
        CallableStatement stmt = DBConnection.conn.prepareCall("{call ChangePassword(?, ?)}");
        stmt.setInt(1, this.userId);
        stmt.setString(2, newPwd);
        stmt.execute();
        stmt.close();

        this.password = newPwd;
        System.out.println("✅ Password changed successfully.");
    }

    public void downloadPatientHistory() throws Exception {
        if (this.userId == 0) {
            System.out.println("⚠️ Please login first.");
            return;
        }

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }

        Scanner sc = new Scanner(System.in);
        int targetPatientId = 0;

        if (this instanceof Patient) {
            PreparedStatement psP = DBConnection.conn.prepareStatement("SELECT patient_id FROM patients WHERE user_id = ?");
            psP.setInt(1, this.userId);
            ResultSet rsP = psP.executeQuery();
            if (rsP.next()) {
                targetPatientId = rsP.getInt("patient_id");
            }
            rsP.close();
            psP.close();

            if (targetPatientId == 0) {
                System.out.println("⚠️ Patient record not found for your account.");
                return;
            }
        } else {
            System.out.println("\n📥 --- Download Patient History File (IO) ---");
            while (true) {
                System.out.print("👉 Enter Patient ID to download history: ");
                try {
                    targetPatientId = Integer.parseInt(sc.nextLine().trim());
                    if (targetPatientId > 0) {
                        break;
                    }
                    System.out.println("⚠️ Error: Patient ID must be a positive integer.");
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
                }
            }
        }

        // Verify patient existence
        PreparedStatement psCheck = DBConnection.conn.prepareStatement(
                "SELECT p.*, u.username FROM patients p LEFT JOIN users u ON p.user_id = u.user_id WHERE p.patient_id = ?");
        psCheck.setInt(1, targetPatientId);
        ResultSet rsCheck = psCheck.executeQuery();

        if (!rsCheck.next()) {
            System.out.println("❌ Error: Patient with ID " + targetPatientId + " does not exist.");
            rsCheck.close();
            psCheck.close();
            return;
        }

        String firstName = rsCheck.getString("first_name");
        String lastName = rsCheck.getString("last_name");
        String patientName = ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
        String healthId = rsCheck.getString("health_id") != null ? rsCheck.getString("health_id") : "N/A";
        String gender = rsCheck.getString("gender") != null ? rsCheck.getString("gender") : "N/A";
        String dob = rsCheck.getString("dob") != null ? rsCheck.getString("dob") : "N/A";
        String bloodGroup = rsCheck.getString("blood_group") != null ? rsCheck.getString("blood_group") : "N/A";
        String phone = rsCheck.getString("phone") != null ? rsCheck.getString("phone") : "N/A";
        String emergencyContact = rsCheck.getString("emergency_contact") != null ? rsCheck.getString("emergency_contact") : "N/A";
        String height = rsCheck.getString("height") != null ? rsCheck.getString("height") : "N/A";
        String weight = rsCheck.getString("weight") != null ? rsCheck.getString("weight") : "N/A";
        String address = rsCheck.getString("address") != null ? rsCheck.getString("address") : "N/A";

        rsCheck.close();
        psCheck.close();

        // Create downloads directory if not exists
        File downloadDir = new File("./downloads");
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }

        File outFile = new File(downloadDir, "patient_history_" + targetPatientId + ".txt");

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outFile)))) {
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            writer.println("=========================================================================");
            writer.println("         🏥 HEALTHCONNECT SMART HOSPITAL CARE SYSTEM                    ");
            writer.println("                   PATIENT HISTORY FILE REPORT                           ");
            writer.println("=========================================================================");
            writer.println("Generated Date/Time : " + timeStamp);
            writer.println("Downloaded By User  : " + this.email + " (Role: " + this.role + ")");
            writer.println("=========================================================================\n");

            // 1. PATIENT DEMOGRAPHICS & PROFILE
            writer.println("📋 1. PATIENT PROFILE INFORMATION");
            writer.println("-------------------------------------------------------------------------");
            writer.println("Patient ID        : " + targetPatientId);
            writer.println("Health ID         : " + healthId);
            writer.println("Full Name         : " + patientName);
            writer.println("Gender            : " + gender);
            writer.println("Date of Birth     : " + dob);
            writer.println("Blood Group       : " + bloodGroup);
            writer.println("Contact Phone     : " + phone);
            writer.println("Emergency Contact : " + emergencyContact);
            writer.println("Height / Weight   : " + height + " cm / " + weight + " kg");
            writer.println("Address           : " + address);
            writer.println("-------------------------------------------------------------------------\n");

            // 2. MEDICAL HISTORY & RECORDS
            writer.println("📋 2. MEDICAL HISTORY & DIAGNOSES");
            writer.println("-------------------------------------------------------------------------");
            try {
                PreparedStatement psMed = DBConnection.conn.prepareStatement("SELECT * FROM medical_history WHERE patient_id = ?");
                psMed.setInt(1, targetPatientId);
                ResultSet rsMed = psMed.executeQuery();
                boolean hasMed = false;
                while (rsMed.next()) {
                    hasMed = true;
                    writer.println("  • History ID  : " + rsMed.getInt("history_id"));
                    writer.println("    Disease     : " + rsMed.getString("disease"));
                    writer.println("    Description : " + rsMed.getString("description"));
                    writer.println("    Allergies   : " + rsMed.getString("allergy"));
                    writer.println("    - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
                }
                if (!hasMed) {
                    writer.println("  (No medical history records registered)");
                }
                rsMed.close();
                psMed.close();
            } catch (Exception e) {
                writer.println("  (Error fetching medical history: " + e.getMessage() + ")");
            }
            writer.println("-------------------------------------------------------------------------\n");

            // 3. APPOINTMENT HISTORY
            writer.println("📅 3. APPOINTMENTS HISTORY");
            writer.println("-------------------------------------------------------------------------");
            try {
                PreparedStatement psApp = DBConnection.conn.prepareStatement(
                        "SELECT a.*, d.name AS doctor_name " +
                                "FROM appointments a LEFT JOIN doctors d ON a.doctor_id = d.doctor_id " +
                                "WHERE a.patient_id = ? ORDER BY a.appointment_id DESC");
                psApp.setInt(1, targetPatientId);
                ResultSet rsApp = psApp.executeQuery();
                boolean hasApp = false;
                while (rsApp.next()) {
                    hasApp = true;
                    String docName = rsApp.getString("doctor_name");
                    writer.println("  • Appointment ID : " + rsApp.getInt("appointment_id"));
                    writer.println("    Doctor Name    : " + (docName != null && !docName.trim().isEmpty() ? "Dr. " + docName.trim() : "ID: " + rsApp.getInt("doctor_id")));
                    writer.println("    Date & Time    : " + rsApp.getString("appointment_date") + " @ " + rsApp.getString("appointment_time"));
                    writer.println("    Status         : " + rsApp.getString("status"));
                    writer.println("    Remarks        : " + rsApp.getString("remarks"));
                    writer.println("    - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
                }
                if (!hasApp) {
                    writer.println("  (No appointment records found)");
                }
                rsApp.close();
                psApp.close();
            } catch (Exception e) {
                writer.println("  (Error fetching appointments: " + e.getMessage() + ")");
            }
            writer.println("-------------------------------------------------------------------------\n");

            // 4. PRESCRIPTION HISTORY
            writer.println("💊 4. PRESCRIPTION & MEDICATION HISTORY");
            writer.println("-------------------------------------------------------------------------");
            try {
                PreparedStatement psPr = DBConnection.conn.prepareStatement(
                        "SELECT pr.*, d.name AS doctor_name " +
                                "FROM prescriptions pr LEFT JOIN doctors d ON pr.doctor_id = d.doctor_id " +
                                "WHERE pr.patient_id = ? ORDER BY pr.prescription_id DESC");
                psPr.setInt(1, targetPatientId);
                ResultSet rsPr = psPr.executeQuery();
                boolean hasPr = false;
                while (rsPr.next()) {
                    hasPr = true;
                    int pId = rsPr.getInt("prescription_id");
                    String docName = rsPr.getString("doctor_name");
                    writer.println("  • Prescription ID : " + pId);
                    writer.println("    Doctor Name     : " + (docName != null && !docName.trim().isEmpty() ? "Dr. " + docName.trim() : "ID: " + rsPr.getInt("doctor_id")));
                    writer.println("    Diagnosis       : " + rsPr.getString("diagnosis"));
                    writer.println("    Notes           : " + rsPr.getString("notes"));
                    writer.println("    Created Date    : " + rsPr.getString("created_date"));

                    try {
                        PreparedStatement psMeds = DBConnection.conn.prepareStatement(
                                "SELECT * FROM prescription_medicines WHERE prescription_id = ?");
                        psMeds.setInt(1, pId);
                        ResultSet rsMeds = psMeds.executeQuery();
                        boolean hasMeds = false;
                        while (rsMeds.next()) {
                            if (!hasMeds) {
                                writer.println("    Medicines       :");
                                hasMeds = true;
                            }
                            writer.println("      - " + rsMeds.getString("medicine_name") + " (" + rsMeds.getString("dosage") + ") for " + rsMeds.getInt("days") + " days");
                        }
                        rsMeds.close();
                        psMeds.close();
                    } catch (Exception exMeds) {
                        // ignore optional medicines
                    }
                    writer.println("    - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
                }
                if (!hasPr) {
                    writer.println("  (No prescription records found)");
                }
                rsPr.close();
                psPr.close();
            } catch (Exception e) {
                writer.println("  (Error fetching prescriptions: " + e.getMessage() + ")");
            }
            writer.println("-------------------------------------------------------------------------\n");

            // 5. DIET PLANS
            writer.println("🥗 5. ASSIGNED DIET PLANS");
            writer.println("-------------------------------------------------------------------------");
            try {
                PreparedStatement psDiet = DBConnection.conn.prepareStatement(
                        "SELECT dp.*, d.name AS doctor_name " +
                                "FROM diet_plan dp LEFT JOIN doctors d ON dp.doctor_id = d.doctor_id " +
                                "WHERE dp.patient_id = ? ORDER BY dp.diet_id DESC");
                psDiet.setInt(1, targetPatientId);
                ResultSet rsDiet = psDiet.executeQuery();
                boolean hasDiet = false;
                while (rsDiet.next()) {
                    hasDiet = true;
                    String docName = rsDiet.getString("doctor_name");
                    writer.println("  • Diet Plan ID : " + rsDiet.getInt("diet_id"));
                    if (docName != null && !docName.trim().isEmpty()) {
                        writer.println("    Doctor Name  : Dr. " + docName.trim());
                    }
                    writer.println("    Breakfast    : " + rsDiet.getString("breakfast"));
                    writer.println("    Lunch        : " + rsDiet.getString("lunch"));
                    writer.println("    Snacks       : " + rsDiet.getString("snacks"));
                    writer.println("    Dinner       : " + rsDiet.getString("dinner"));
                    writer.println("    Water Intake : " + rsDiet.getString("water"));
                    writer.println("    Instructions : " + rsDiet.getString("instructions"));
                    writer.println("    - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
                }
                if (!hasDiet) {
                    writer.println("  (No diet plan records found)");
                }
                rsDiet.close();
                psDiet.close();
            } catch (Exception e) {
                writer.println("  (Error fetching diet plans: " + e.getMessage() + ")");
            }
            writer.println("-------------------------------------------------------------------------\n");

            // 6. MESSAGES / CONSULTATION CHAT HISTORY
            writer.println("💬 6. CONSULTATION & MESSAGE HISTORY");
            writer.println("-------------------------------------------------------------------------");
            try {
                PreparedStatement psMsg = DBConnection.conn.prepareStatement(
                        "SELECT f.*, d.name AS doctor_name " +
                                "FROM follow_up f LEFT JOIN doctors d ON f.doctor_id = d.doctor_id " +
                                "WHERE f.patient_id = ? ORDER BY f.followup_id DESC");
                psMsg.setInt(1, targetPatientId);
                ResultSet rsMsg = psMsg.executeQuery();
                boolean hasMsg = false;
                while (rsMsg.next()) {
                    hasMsg = true;
                    String docName = rsMsg.getString("doctor_name");
                    writer.println("  • Follow-up ID : " + rsMsg.getInt("followup_id"));
                    writer.println("    Doctor Name  : " + (docName != null && !docName.trim().isEmpty() ? "Dr. " + docName.trim() : "ID: " + rsMsg.getInt("doctor_id")));
                    writer.println("    Message      : " + rsMsg.getString("remarks"));
                    writer.println("    Next Visit   : " + rsMsg.getString("next_visit"));
                    writer.println("    Status       : " + rsMsg.getString("status"));
                    writer.println("    - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
                }
                if (!hasMsg) {
                    writer.println("  (No message/consultation records found)");
                }
                rsMsg.close();
                psMsg.close();
            } catch (Exception e) {
                writer.println("  (Error fetching consultation messages: " + e.getMessage() + ")");
            }
            writer.println("-------------------------------------------------------------------------\n");

            // 7. LAB / MEDICAL REPORTS
            writer.println("🧪 7. LAB & DIAGNOSTIC REPORTS");
            writer.println("-------------------------------------------------------------------------");
            try {
                CallableStatement cstmtReport = DBConnection.conn.prepareCall("{call ViewMedicalReport(?)}");
                cstmtReport.setInt(1, targetPatientId);
                ResultSet rsRep = cstmtReport.executeQuery();
                boolean hasRep = false;
                while (rsRep.next()) {
                    hasRep = true;
                    writer.println("  • Test ID     : " + rsRep.getInt("test_id"));
                    writer.println("    Test Name   : " + rsRep.getString("test_name"));
                    writer.println("    Status      : " + rsRep.getString("status"));
                    writer.println("    Report File : " + rsRep.getString("report_file"));
                    writer.println("    - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
                }
                if (!hasRep) {
                    writer.println("  (No lab report records found)");
                }
                rsRep.close();
                cstmtReport.close();
            } catch (Exception e) {
                writer.println("  (Error fetching lab reports: " + e.getMessage() + ")");
            }
            writer.println("-------------------------------------------------------------------------\n");

            // 8. BILLING & PAYMENTS
            writer.println("💳 8. BILLING & PAYMENTS HISTORY");
            writer.println("-------------------------------------------------------------------------");
            try {
                PreparedStatement psBill = DBConnection.conn.prepareStatement("SELECT * FROM payments WHERE patient_id = ? ORDER BY payment_id DESC");
                psBill.setInt(1, targetPatientId);
                ResultSet rsBill = psBill.executeQuery();
                boolean hasBill = false;
                while (rsBill.next()) {
                    hasBill = true;
                    writer.println("  • Payment ID     : " + rsBill.getInt("payment_id"));
                    writer.println("    Appointment ID : " + rsBill.getInt("appointment_id"));
                    writer.println("    Amount         : Rs. " + String.format("%.2f", rsBill.getDouble("amount")));
                    writer.println("    Payment Mode   : " + rsBill.getString("payment_mode"));
                    writer.println("    Status         : " + rsBill.getString("status"));
                    writer.println("    - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
                }
                if (!hasBill) {
                    writer.println("  (No payment records found)");
                }
                rsBill.close();
                psBill.close();
            } catch (Exception e) {
                writer.println("  (Error fetching payment history: " + e.getMessage() + ")");
            }
            writer.println("-------------------------------------------------------------------------");
            writer.println("               *** END OF PATIENT HISTORY FILE ***                       ");
            writer.println("=========================================================================");
        }

        System.out.println("\n🎉 Patient history file successfully downloaded using Java File I/O!");
        System.out.println("📂 Saved to local storage at:");
        System.out.println("   " + outFile.getAbsolutePath());

        Main.logActivity(this.userId, "DOWNLOAD_HISTORY", "patients");
    }
}