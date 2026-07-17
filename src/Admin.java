import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;

public class Admin extends User {
    String designation = "";
    String accessLevel = "";

    public void addDoctor() throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n👨‍⚕️ --- Add New Doctor ---");

        String name = "";
        while (true) {
            System.out.print("👉 Enter Name: ");
            name = sc.nextLine().trim();
            boolean isValid = true;
            if (name.isEmpty()) {
                isValid = false;
            } else {
                for (int i = 0; i < name.length(); i++) {
                    char ch = name.charAt(i);
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

        String specialization = "";
        while (true) {
            System.out.print("👉 Enter Specialization: ");
            specialization = sc.nextLine().trim();
            boolean isValid = true;
            if (specialization.isEmpty()) {
                isValid = false;
            } else {
                for (int i = 0; i < specialization.length(); i++) {
                    char ch = specialization.charAt(i);
                    if (!((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == ' ')) {
                        isValid = false;
                        break;
                    }
                }
            }
            if (isValid) {
                break;
            }
            System.out.println("⚠️ Error: Specialization must contain letters and spaces only.");
        }

        int deptId = 0;
        while (true) {
            System.out.println("\n🏥 --- Choose Department ---");
            if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
                DBConnection.initialize();
            }
            PreparedStatement psDept = DBConnection.conn.prepareStatement("SELECT department_id, department_name FROM departments ORDER BY department_id");
            ResultSet rsDept = psDept.executeQuery();
            while (rsDept.next()) {
                System.out.println(rsDept.getInt("department_id") + ". " + rsDept.getString("department_name"));
            }
            rsDept.close();
            psDept.close();

            System.out.print("👉 Select Department ID (1-15): ");
            try {
                deptId = Integer.parseInt(sc.nextLine().trim());
                if (deptId > 0 && deptId <= 15) {
                    break;
                }
                System.out.println("⚠️ Error: Department ID must be between 1 and 15.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
            }
        }

        System.out.print("👉 Enter Qualification: ");
        String qualification = sc.nextLine();

        int exp = 0;
        while (true) {
            System.out.print("👉 Enter Experience (in years): ");
            try {
                exp = Integer.parseInt(sc.nextLine().trim());
                if (exp >= 0) {
                    break;
                }
                System.out.println("⚠️ Error: Experience must be a positive integer.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
            }
        }

        String phone = "";
        while (true) {
            System.out.print("👉 Enter Phone: ");
            phone = sc.nextLine().trim();
            boolean isValid = true;
            if (phone.length() != 10) {
                isValid = false;
            } else {
                for (int i = 0; i < phone.length(); i++) {
                    char ch = phone.charAt(i);
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

        String email = "";
        while (true) {
            System.out.print("👉 Enter Email: ");
            email = sc.nextLine().trim();
            int atIndex = email.indexOf('@');
            int dotIndex = email.lastIndexOf('.');
            boolean isValid = (atIndex > 0 && dotIndex > atIndex + 1 && dotIndex < email.length() - 1);
            if (isValid) {
                break;
            }
            System.out.println("⚠️ Error: Please enter a valid email address.");
        }

        System.out.print("👉 Enter Room Number: ");
        String room = sc.nextLine();
        System.out.print("👉 Enter Availability (e.g. Mon-Sat): ");
        String avail = sc.nextLine();

        double fee = 0.0;
        while (true) {
            System.out.print("👉 Enter Consultation Fee: ");
            try {
                fee = Double.parseDouble(sc.nextLine().trim());
                if (fee >= 0) {
                    break;
                }
                System.out.println("⚠️ Error: Consultation fee must be a positive number.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error: Invalid decimal format. Please enter a decimal.");
            }
        }

        System.out.print("👉 Enter Password for Account: ");
        String pwd = sc.nextLine();

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        DBConnection.conn.setAutoCommit(false);
        try {
            // Call AddDoctor(IN p_name VARCHAR(100), IN p_spec VARCHAR(100), IN p_dept INT)
            CallableStatement stmt = DBConnection.conn.prepareCall("{call AddDoctor(?, ?, ?)}");
            stmt.setString(1, name);
            stmt.setString(2, specialization);
            stmt.setInt(3, deptId);
            stmt.execute();
            stmt.close();

            // Find generated doctor_id
            int doctorId = 0;
            PreparedStatement ps = DBConnection.conn.prepareStatement("SELECT doctor_id FROM doctors WHERE name = ? ORDER BY doctor_id DESC LIMIT 1");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                doctorId = rs.getInt("doctor_id");
            }
            rs.close();
            ps.close();

            if (doctorId > 0) {
                // Update doctor specific details directly in DB
                PreparedStatement ps1 = DBConnection.conn.prepareStatement(
                        "UPDATE doctors SET qualification = ?, experience = ?, phone = ?, email = ?, room_no = ?, availability = ?, consultation_fee = ? WHERE doctor_id = ?");
                ps1.setString(1, qualification);
                ps1.setInt(2, exp);
                ps1.setString(3, phone);
                ps1.setString(4, email);
                ps1.setString(5, room);
                ps1.setString(6, avail);
                ps1.setDouble(7, fee);
                ps1.setInt(8, doctorId);
                ps1.executeUpdate();
                ps1.close();

                // Create user login
                int userId = 0;
                PreparedStatement ps2 = DBConnection.conn.prepareStatement("INSERT INTO users (username, password, role, status) VALUES (?, ?, 'Doctor', 'Active')", Statement.RETURN_GENERATED_KEYS);
                ps2.setString(1, email);
                ps2.setString(2, pwd);
                ps2.executeUpdate();
                ResultSet rs2 = ps2.getGeneratedKeys();
                if (rs2.next()) {
                    userId = rs2.getInt(1);
                }
                rs2.close();
                ps2.close();

                // Link user_id in doctors
                if (userId > 0) {
                    PreparedStatement ps3 = DBConnection.conn.prepareStatement("UPDATE doctors SET user_id = ? WHERE doctor_id = ?");
                    ps3.setInt(1, userId);
                    ps3.setInt(2, doctorId);
                    ps3.executeUpdate();
                    ps3.close();
                }
            }

            DBConnection.conn.commit();
            System.out.println("🎉 Doctor added successfully and login account created!");
            Main.logActivity(this.userId, "INSERT", "doctors");
        } catch (Exception ex) {
            DBConnection.conn.rollback();
            throw ex;
        } finally {
            DBConnection.conn.setAutoCommit(true);
        }
    }

    public void removeDoctor() throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n❌ --- Remove Doctor ---");

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

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        int userId = 0;
        PreparedStatement ps = DBConnection.conn.prepareStatement("SELECT user_id FROM doctors WHERE doctor_id = ?");
        ps.setInt(1, doctorId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            userId = rs.getInt("user_id");
        }
        rs.close();
        ps.close();

        DBConnection.conn.setAutoCommit(false);
        try {
            // Delete from doctors
            PreparedStatement ps1 = DBConnection.conn.prepareStatement("DELETE FROM doctors WHERE doctor_id = ?");
            ps1.setInt(1, doctorId);
            ps1.executeUpdate();
            ps1.close();

            // Delete from users
            if (userId > 0) {
                PreparedStatement ps2 = DBConnection.conn.prepareStatement("DELETE FROM users WHERE user_id = ?");
                ps2.setInt(1, userId);
                ps2.executeUpdate();
                ps2.close();
            }
            DBConnection.conn.commit();
            System.out.println("🗑️ Doctor and their user account removed successfully.");
            Main.logActivity(this.userId, "DELETE", "doctors");
        } catch (Exception ex) {
            DBConnection.conn.rollback();
            throw ex;
        } finally {
            DBConnection.conn.setAutoCommit(true);
        }
    }

    public void addPatient() throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n🧑‍⚕️ --- Add/Register New Patient ---");

        String fName = "";
        while (true) {
            System.out.print("👉 Enter First Name: ");
            fName = sc.nextLine().trim();
            boolean isValid = true;
            if (fName.isEmpty()) {
                isValid = false;
            } else {
                for (int i = 0; i < fName.length(); i++) {
                    char ch = fName.charAt(i);
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

        String lName = "";
        while (true) {
            System.out.print("👉 Enter Last Name: ");
            lName = sc.nextLine().trim();
            boolean isValid = true;
            if (lName.isEmpty()) {
                isValid = false;
            } else {
                for (int i = 0; i < lName.length(); i++) {
                    char ch = lName.charAt(i);
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

        String gender = "";
        while (true) {
            System.out.print("👉 Enter Gender (M/F/O): ");
            gender = sc.nextLine().trim().toUpperCase();
            if (gender.equals("M") || gender.equals("F") || gender.equals("O")) {
                break;
            }
            System.out.println("⚠️ Error: Gender must be M, F, or O.");
        }

        String dob = "";
        while (true) {
            System.out.print("👉 Enter DOB (YYYY-MM-DD): ");
            dob = sc.nextLine().trim();
            boolean isValid = true;
            if (dob.length() != 10 || dob.charAt(4) != '-' || dob.charAt(7) != '-') {
                isValid = false;
            } else {
                for (int i = 0; i < dob.length(); i++) {
                    if (i == 4 || i == 7) continue;
                    char ch = dob.charAt(i);
                    if (ch < '0' || ch > '9') {
                        isValid = false;
                        break;
                    }
                }
                if (isValid) {
                    int year = Integer.parseInt(dob.substring(0, 4));
                    int month = Integer.parseInt(dob.substring(5, 7));
                    int day = Integer.parseInt(dob.substring(8, 10));

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

        System.out.print("👉 Enter Blood Group: ");
        String blood = sc.nextLine();

        String phone = "";
        while (true) {
            System.out.print("👉 Enter Phone: ");
            phone = sc.nextLine().trim();
            boolean isValid = true;
            if (phone.length() != 10) {
                isValid = false;
            } else {
                for (int i = 0; i < phone.length(); i++) {
                    char ch = phone.charAt(i);
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

        String email = "";
        while (true) {
            System.out.print("👉 Enter Email: ");
            email = sc.nextLine().trim();
            int atIndex = email.indexOf('@');
            int dotIndex = email.lastIndexOf('.');
            boolean isValid = (atIndex > 0 && dotIndex > atIndex + 1 && dotIndex < email.length() - 1);
            if (isValid) {
                break;
            }
            System.out.println("⚠️ Error: Please enter a valid email address.");
        }

        System.out.print("👉 Enter Address: ");
        String address = sc.nextLine();
        System.out.print("👉 Enter City: ");
        String city = sc.nextLine();
        System.out.print("👉 Enter State: ");
        String state = sc.nextLine();
        System.out.print("👉 Enter Pincode: ");
        String pin = sc.nextLine();

        String aadhaar = "";
        while (true) {
            System.out.print("👉 Enter Aadhaar: ");
            aadhaar = sc.nextLine().trim();
            boolean isValid = true;
            if (aadhaar.length() != 12) {
                isValid = false;
            } else {
                for (int i = 0; i < aadhaar.length(); i++) {
                    char ch = aadhaar.charAt(i);
                    if (ch < '0' || ch > '9') {
                        isValid = false;
                        break;
                    }
                }
            }
            if (isValid) {
                break;
            }
            System.out.println("⚠️ Error: Aadhaar must be exactly 12 digits.");
        }

        System.out.print("👉 Enter ABHA ID: ");
        String abha = sc.nextLine();

        double height = 0.0;
        while (true) {
            System.out.print("👉 Enter Height (cm): ");
            try {
                height = Double.parseDouble(sc.nextLine().trim());
                if (height > 0) {
                    break;
                }
                System.out.println("⚠️ Error: Height must be a positive number.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error: Invalid decimal format. Please enter a decimal.");
            }
        }

        double weight = 0.0;
        while (true) {
            System.out.print("👉 Enter Weight (kg): ");
            try {
                weight = Double.parseDouble(sc.nextLine().trim());
                if (weight > 0) {
                    break;
                }
                System.out.println("⚠️ Error: Weight must be a positive number.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error: Invalid decimal format. Please enter a decimal.");
            }
        }

        System.out.print("👉 Enter Emergency Contact Name/Phone: ");
        String emergency = sc.nextLine();
        System.out.print("👉 Enter Password for Patient Account: ");
        String pwd = sc.nextLine();

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        DBConnection.conn.setAutoCommit(false);
        try {
            // Call RegisterPatient(IN p_first VARCHAR(50), IN p_last VARCHAR(50), IN p_phone VARCHAR(15))
            CallableStatement stmt = DBConnection.conn.prepareCall("{call RegisterPatient(?, ?, ?)}");
            stmt.setString(1, fName);
            stmt.setString(2, lName);
            stmt.setString(3, phone);
            stmt.execute();
            stmt.close();

            // Retrieve generated patient_id
            int patientId = 0;
            PreparedStatement ps = DBConnection.conn.prepareStatement("SELECT patient_id FROM patients WHERE phone = ? ORDER BY patient_id DESC LIMIT 1");
            ps.setString(1, phone);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                patientId = rs.getInt("patient_id");
            }
            rs.close();
            ps.close();

            if (patientId > 0) {
                // Update other patient attributes directly
                PreparedStatement ps1 = DBConnection.conn.prepareStatement(
                        "UPDATE patients SET gender = ?, dob = ?, blood_group = ?, address = ?, city = ?, state = ?, pincode = ?, aadhaar = ?, abha_id = ?, height = ?, weight = ?, emergency_contact = ? WHERE patient_id = ?");
                ps1.setString(1, gender);
                ps1.setString(2, dob);
                ps1.setString(3, blood);
                ps1.setString(4, address);
                ps1.setString(5, city);
                ps1.setString(6, state);
                ps1.setString(7, pin);
                ps1.setString(8, aadhaar);
                ps1.setString(9, abha);
                ps1.setDouble(10, height);
                ps1.setDouble(11, weight);
                ps1.setString(12, emergency);
                ps1.setInt(13, patientId);
                ps1.executeUpdate();
                ps1.close();

                // Create user login
                int userId = 0;
                PreparedStatement ps2 = DBConnection.conn.prepareStatement("INSERT INTO users (username, password, role, status) VALUES (?, ?, 'Patient', 'Active')", Statement.RETURN_GENERATED_KEYS);
                ps2.setString(1, email);
                ps2.setString(2, pwd);
                ps2.executeUpdate();
                ResultSet rs2 = ps2.getGeneratedKeys();
                if (rs2.next()) {
                    userId = rs2.getInt(1);
                }
                rs2.close();
                ps2.close();

                // Link user_id to patients
                if (userId > 0) {
                    PreparedStatement ps3 = DBConnection.conn.prepareStatement("UPDATE patients SET user_id = ? WHERE patient_id = ?");
                    ps3.setInt(1, userId);
                    ps3.setInt(2, patientId);
                    ps3.executeUpdate();
                    ps3.close();
                }
            }

            DBConnection.conn.commit();
            System.out.println("🎉 Patient registered successfully!");
            Main.logActivity(this.userId, "INSERT", "patients");
        } catch (Exception ex) {
            DBConnection.conn.rollback();
            throw ex;
        } finally {
            DBConnection.conn.setAutoCommit(true);
        }
    }

    public void removePatient() throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n❌ --- Remove Patient ---");

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

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        int userId = 0;
        PreparedStatement ps = DBConnection.conn.prepareStatement("SELECT user_id FROM patients WHERE patient_id = ?");
        ps.setInt(1, patientId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            userId = rs.getInt("user_id");
        }
        rs.close();
        ps.close();

        DBConnection.conn.setAutoCommit(false);
        try {
            PreparedStatement ps1 = DBConnection.conn.prepareStatement("DELETE FROM patients WHERE patient_id = ?");
            ps1.setInt(1, patientId);
            ps1.executeUpdate();
            ps1.close();

            if (userId > 0) {
                PreparedStatement ps2 = DBConnection.conn.prepareStatement("DELETE FROM users WHERE user_id = ?");
                ps2.setInt(1, userId);
                ps2.executeUpdate();
                ps2.close();
            }
            DBConnection.conn.commit();
            System.out.println("🗑️ Patient and their user account removed successfully.");
            Main.logActivity(this.userId, "DELETE", "patients");
        } catch (Exception ex) {
            DBConnection.conn.rollback();
            throw ex;
        } finally {
            DBConnection.conn.setAutoCommit(true);
        }
    }

    public void manageUsers() throws Exception {
        System.out.println("\n👤 --- System Users List ---");
        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        Statement stmt = DBConnection.conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT user_id, username, role, status, created_at FROM users");
        System.out.printf("%-10s | %-30s | %-15s | %-10s | %-20s\n", "User ID", "Username/Email", "Role", "Status", "Created At");
        System.out.println("------------------------------------------------------------------------------------------------");
        while (rs.next()) {
            System.out.printf("%-10d | %-30s | %-15s | %-10s | %-20s\n",
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("role"),
                    rs.getString("status"),
                    rs.getTimestamp("created_at").toString()
            );
        }
        rs.close();
        stmt.close();
    }

    public void manageAppointments() throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n📅 --- Manage Appointments ---");
        System.out.println("1. 📋 List All Appointments");
        System.out.println("2. 🚦 Update Appointment Status");
        System.out.println("3. ⏰ Reschedule Appointment");

        int choice = 0;
        while (true) {
            System.out.print("👉 Choose an option: ");
            try {
                choice = Integer.parseInt(sc.nextLine().trim());
                if (choice >= 1 && choice <= 3) {
                    break;
                }
                System.out.println("⚠️ Error: Choice must be between 1 and 3.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
            }
        }

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        if (choice == 1) {
            Statement stmt = DBConnection.conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM appointments ORDER BY appointment_date DESC LIMIT 50");
            System.out.printf("%-5s | %-10s | %-10s | %-12s | %-10s | %-10s | %-10s\n", "ID", "Patient ID", "Doctor ID", "Date", "Time", "Status", "Priority");
            System.out.println("---------------------------------------------------------------------------------------");
            while (rs.next()) {
                System.out.printf("%-5d | %-10d | %-10d | %-12s | %-10s | %-10s | %-10s\n",
                        rs.getInt("appointment_id"),
                        rs.getInt("patient_id"),
                        rs.getInt("doctor_id"),
                        rs.getString("appointment_date"),
                        rs.getString("appointment_time"),
                        rs.getString("status"),
                        rs.getString("priority")
                );
            }
            rs.close();
            stmt.close();
        } else if (choice == 2) {
            int appId = 0;
            while (true) {
                System.out.print("👉 Enter Appointment ID: ");
                try {
                    appId = Integer.parseInt(sc.nextLine().trim());
                    if (appId > 0) {
                        break;
                    }
                    System.out.println("⚠️ Error: Appointment ID must be a positive integer.");
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
                }
            }
            String status = "";
            while (true) {
                System.out.println("\n🚦 --- Choose New Status ---");
                System.out.println("1. Booked");
                System.out.println("2. Completed");
                System.out.println("3. Cancelled");
                System.out.print("👉 Enter choice (1-3): ");
                String statusOpt = sc.nextLine().trim();
                if (statusOpt.equals("1")) {
                    status = "Booked";
                    break;
                } else if (statusOpt.equals("2")) {
                    status = "Completed";
                    break;
                } else if (statusOpt.equals("3")) {
                    status = "Cancelled";
                    break;
                } else {
                    System.out.println("⚠️ Error: Invalid choice. Please choose 1, 2, or 3.");
                }
            }
            CallableStatement cstmt = DBConnection.conn.prepareCall("{call UpdateAppointmentStatus(?, ?)}");
            cstmt.setInt(1, appId);
            cstmt.setString(2, status);
            cstmt.execute();
            cstmt.close();
            System.out.println("✅ Appointment status updated successfully.");
        } else if (choice == 3) {
            int appId = 0;
            while (true) {
                System.out.print("👉 Enter Appointment ID: ");
                try {
                    appId = Integer.parseInt(sc.nextLine().trim());
                    if (appId > 0) {
                        break;
                    }
                    System.out.println("⚠️ Error: Appointment ID must be a positive integer.");
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
                }
            }

            String newDate = "";
            while (true) {
                System.out.print("👉 Enter New Date (YYYY-MM-DD): ");
                newDate = sc.nextLine().trim();
                boolean isValid = true;
                if (newDate.length() != 10 || newDate.charAt(4) != '-' || newDate.charAt(7) != '-') {
                    isValid = false;
                } else {
                    for (int i = 0; i < newDate.length(); i++) {
                        if (i == 4 || i == 7) continue;
                        char ch = newDate.charAt(i);
                        if (ch < '0' || ch > '9') {
                            isValid = false;
                            break;
                        }
                    }
                    if (isValid) {
                        int year = Integer.parseInt(newDate.substring(0, 4));
                        int month = Integer.parseInt(newDate.substring(5, 7));
                        int day = Integer.parseInt(newDate.substring(8, 10));

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

            String newTime = "";
            while (true) {
                System.out.print("👉 Enter New Time (HH:MM:SS): ");
                newTime = sc.nextLine().trim();
                boolean isValid = true;
                if (newTime.length() != 8 || newTime.charAt(2) != ':' || newTime.charAt(5) != ':') {
                    isValid = false;
                } else {
                    for (int i = 0; i < newTime.length(); i++) {
                        if (i == 2 || i == 5) continue;
                        char ch = newTime.charAt(i);
                        if (ch < '0' || ch > '9') {
                            isValid = false;
                            break;
                        }
                    }
                    if (isValid) {
                        int hour = Integer.parseInt(newTime.substring(0, 2));
                        int minute = Integer.parseInt(newTime.substring(3, 5));
                        int second = Integer.parseInt(newTime.substring(6, 8));

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

            CallableStatement cstmt = DBConnection.conn.prepareCall("{call RescheduleAppointment(?, ?, ?)}");
            cstmt.setInt(1, appId);
            cstmt.setString(2, newDate);
            cstmt.setString(3, newTime);
            cstmt.execute();
            cstmt.close();
            System.out.println("✅ Appointment rescheduled successfully.");
        }
    }

    public void viewSystemReports() throws Exception {
        System.out.println("\n📊 --- HealthConnect System Analytics & Reports ---");
        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        int patientCount = 0;
        int doctorCount = 0;
        int appCount = 0;
        double totalRevenue = 0.0;

        Statement st = DBConnection.conn.createStatement();
        ResultSet rs1 = st.executeQuery("SELECT COUNT(*) FROM patients");
        if (rs1.next()) patientCount = rs1.getInt(1);
        rs1.close();

        ResultSet rs2 = st.executeQuery("SELECT COUNT(*) FROM doctors");
        if (rs2.next()) doctorCount = rs2.getInt(1);
        rs2.close();

        ResultSet rs3 = st.executeQuery("SELECT COUNT(*) FROM appointments");
        if (rs3.next()) appCount = rs3.getInt(1);
        rs3.close();

        ResultSet rs4 = st.executeQuery("SELECT SUM(amount) FROM payments WHERE status='Paid'");
        if (rs4.next()) totalRevenue = rs4.getDouble(1);
        rs4.close();
        st.close();

        System.out.println("🧑‍⚕️ Total Registered Patients : " + patientCount);
        System.out.println("👨‍⚕️ Total Active Doctors      : " + doctorCount);
        System.out.println("📅 Total Appointments        : " + appCount);
        System.out.println("💰 Total Billing Revenue     : Rs. " + totalRevenue);
        System.out.println("-------------------------------------------------");
    }

    public void manageBilling() throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n¾ --- Manage Billing & Payments ---");
        System.out.println("1. ➕ Generate Patient Bill");
        System.out.println("2. 💸 Make Payment (Record)");
        System.out.println("3. 📋 View Patient Bill");

        int choice = 0;
        while (true) {
            System.out.print("👉 Choose an option: ");
            try {
                choice = Integer.parseInt(sc.nextLine().trim());
                if (choice >= 1 && choice <= 3) {
                    break;
                }
                System.out.println("⚠️ Error: Choice must be between 1 and 3.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
            }
        }

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        if (choice == 1) {
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

            int appId = 0;
            while (true) {
                System.out.print("👉 Enter Appointment ID: ");
                try {
                    appId = Integer.parseInt(sc.nextLine().trim());
                    if (appId > 0) {
                        break;
                    }
                    System.out.println("⚠️ Error: Appointment ID must be a positive integer.");
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
                }
            }

            double amount = 0.0;
            while (true) {
                System.out.print("💰 Enter Total Bill Amount: ");
                try {
                    amount = Double.parseDouble(sc.nextLine().trim());
                    if (amount >= 0) {
                        break;
                    }
                    System.out.println("⚠️ Error: Amount must be a positive number.");
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ Error: Invalid decimal format. Please enter a decimal.");
                }
            }

            CallableStatement stmt = DBConnection.conn.prepareCall("{call GenerateBill(?, ?, ?)}");
            stmt.setInt(1, patientId);
            stmt.setInt(2, appId);
            stmt.setDouble(3, amount);
            stmt.execute();
            stmt.close();
            System.out.println("✅ Bill generated successfully!");
            Main.logActivity(this.userId, "INSERT", "payments");
        } else if (choice == 2) {
            int billId = 0;
            while (true) {
                System.out.print("👉 Enter Payment ID (Bill ID): ");
                try {
                    billId = Integer.parseInt(sc.nextLine().trim());
                    if (billId > 0) {
                        break;
                    }
                    System.out.println("⚠️ Error: Payment ID must be a positive integer.");
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
                }
            }

            System.out.print("👉 Enter Payment Method (Cash/Card/UPI): ");
            String method = sc.nextLine();

            CallableStatement stmt = DBConnection.conn.prepareCall("{call MakePayment(?, ?)}");
            stmt.setInt(1, billId);
            stmt.setString(2, method);
            stmt.execute();
            stmt.close();
            System.out.println("✅ Payment recorded successfully!");
        } else if (choice == 3) {
            int billId = 0;
            while (true) {
                System.out.print("👉 Enter Payment ID (Bill ID): ");
                try {
                    billId = Integer.parseInt(sc.nextLine().trim());
                    if (billId > 0) {
                        break;
                    }
                    System.out.println("⚠️ Error: Payment ID must be a positive integer.");
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
                }
            }

            PreparedStatement ps = DBConnection.conn.prepareStatement("SELECT * FROM payments WHERE payment_id = ?");
            ps.setInt(1, billId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("\n💳 --- BILL DETAILS ---");
                System.out.println("Bill ID          : " + rs.getInt("payment_id"));
                System.out.println("Patient ID       : " + rs.getInt("patient_id"));
                System.out.println("Appointment ID   : " + rs.getInt("appointment_id"));
                System.out.println("Total Amount     : Rs. " + rs.getDouble("amount"));
                System.out.println("Payment Status   : " + rs.getString("status"));
                System.out.println("Payment Method   : " + rs.getString("payment_mode"));
                System.out.println("Date             : " + rs.getString("date"));
                System.out.println("------------------------------------");
            } else {
                System.out.println("❌ Bill ID not found.");
            }
            rs.close();
            ps.close();
        }
    }

    public void viewFeedback() throws Exception {
        Scanner sc = new Scanner(System.in);

        int docId = 0;
        while (true) {
            System.out.print("\n👉 Enter Doctor ID to view feedback: ");
            try {
                docId = Integer.parseInt(sc.nextLine().trim());
                if (docId > 0) {
                    break;
                }
                System.out.println("⚠️ Error: Doctor ID must be a positive integer.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
            }
        }

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call ViewFeedback(?)}");
        stmt.setInt(1, docId);
        ResultSet rs = stmt.executeQuery();
        System.out.println("\n⭐ --- Patient Feedback for Doctor ID: " + docId + " ---");
        boolean found = false;
        while (rs.next()) {
            found = true;
            System.out.println("🔑 Feedback ID: " + rs.getInt("feedback_id"));
            System.out.println("👤 Patient    : " + rs.getString("patient_id"));
            System.out.println("⭐ Rating     : " + rs.getInt("rating") + " stars");
            System.out.println("💬 Comments   : " + rs.getString("comments"));
            System.out.println("📅 Date       : " + rs.getString("date"));
            System.out.println("----------------------------------------");
        }
        if (!found) {
            System.out.println("📭 No feedback found for this doctor.");
        }
        rs.close();
        stmt.close();
    }
}