import java.util.Scanner;

public class Main {
    public static User loggedInUser = null;

    public static void logActivity(int userId, String operation, String tableName) {
        try {
            if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
                DBConnection.initialize();
            }
            java.sql.PreparedStatement ps = DBConnection.conn.prepareStatement("INSERT INTO audit_log(user_id, operation, table_name) VALUES (?, ?, ?)");
            ps.setInt(1, userId > 0 ? userId : 1);
            ps.setString(2, operation);
            ps.setString(3, tableName);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            // Ignore
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("=========================================================================");
        System.out.println("   🏥 Welcome to HealthConnect  Management System   ");
        System.out.println("=========================================================================");

        // Initialize shared database connection once at system startup
        try {
            DBConnection.initialize();
        } catch (Exception e) {
            System.out.println("❌ Database connection error: " + e.getMessage());
        }

        while (true) {
            try {
                if (loggedInUser == null) {
                    System.out.println("\n🔐 --- COMPULSORY LOGIN ---");
                    System.out.println("1. 🔑 Login to your Account");
                    System.out.println("2. ❌ Exit System");
                    System.out.print("👉 Choose an option: ");
                    String opt = sc.nextLine().trim();

                    if (opt.equals("1")) {
                        User baseUser = new User();
                        baseUser.login();
                        if (baseUser.userId > 0) {
                            if (baseUser.role.equalsIgnoreCase("Admin")) {
                                Admin admin = new Admin();
                                admin.userId = baseUser.userId;
                                admin.email = baseUser.email;
                                admin.role = baseUser.role;
                                admin.password = baseUser.password;
                                loggedInUser = admin;
                            } else if (baseUser.role.equalsIgnoreCase("Doctor")) {
                                Doctor doctor = new Doctor();
                                doctor.userId = baseUser.userId;
                                doctor.email = baseUser.email;
                                doctor.role = baseUser.role;
                                doctor.password = baseUser.password;
                                doctor.loadDoctorDetails();
                                loggedInUser = doctor;
                            } else if (baseUser.role.equalsIgnoreCase("Patient")) {
                                Patient patient = new Patient();
                                patient.userId = baseUser.userId;
                                patient.email = baseUser.email;
                                patient.role = baseUser.role;
                                patient.password = baseUser.password;
                                patient.loadPatientDetails();
                                loggedInUser = patient;
                            } else {
                                loggedInUser = baseUser;
                            }
                        }
                    } else if (opt.equals("2")) {
                        System.out.println("👋 Thank you for using HealthConnect. Goodbye!");
                        break;
                    } else {
                        System.out.println("⚠️ Invalid option. Please try again.");
                    }
                } else {
                    showMenu();
                }
            } catch (Exception e) {
                System.out.println("❌ System Error: " + e.getMessage());
            }
        }
    }

    public static void showMenu() throws Exception {
        Scanner sc = new Scanner(System.in);
        if (loggedInUser instanceof Patient) {
            showPatientMenu((Patient) loggedInUser);
        } else if (loggedInUser instanceof Doctor) {
            showDoctorMenu((Doctor) loggedInUser);
        } else if (loggedInUser instanceof Admin) {
            showAdminMenu((Admin) loggedInUser);
        } else {
            System.out.println("\n⚙️ --- User Menu ---");
            System.out.println("1. 👤 Update Profile");
            System.out.println("2. 🔐 Change Password");
            System.out.println("3. 🚪 Logout");
            System.out.print("👉 Choose an option: ");
            String opt = sc.nextLine().trim();
            if (opt.equals("1")) loggedInUser.updateProfile();
            else if (opt.equals("2")) loggedInUser.changePassword();
            else if (opt.equals("3")) loggedInUser.logout();
        }
    }

    private static void showPatientMenu(Patient p) throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n====================================");
        System.out.println("      🧑‍⚕️ PATIENT PORTAL MENU       ");
        System.out.println("====================================");
        System.out.println("1.  📊 View Dashboard");
        System.out.println("2.  📅 Book Appointment");
        System.out.println("3.  📋 View Medical History");
        System.out.println("4.  💊 View Prescriptions");
        System.out.println("5.  🥗 View Assigned Diet Plan");
        System.out.println("6.  🧪 View Lab / Medical Reports");
        System.out.println("7.  💬 Message / Consult Doctor");
        System.out.println("8.  📜 View Conversation Chat History");
        System.out.println("9.  🔍 Search Specialized Doctors");
        System.out.println("10. 🏥 Find Nearby Hospital Info");
        System.out.println("11. 💊 Find Nearby Pharmacy & Stock");
        System.out.println("12. ⏰ View / Mark Active Reminders");
        System.out.println("13. ⭐ Submit Feedback for Doctor");
        System.out.println("14. 💳 Check / Pay Bills");
        System.out.println("15. 👤 Update Profile");
        System.out.println("16. 🔐 Change Password");
        System.out.println("17. 🎟️ Check Live Queue Status");
        System.out.println("18. 🚪 Logout");
        System.out.print("👉 Enter your choice (1-18): ");
        String choice = sc.nextLine().trim();

        switch (choice) {
            case "1":
                p.viewDashboard();
                break;
            case "2":
                p.bookAppointment();
                break;
            case "3":
                p.viewMedicalHistory();
                break;
            case "4":
                p.viewPrescription();
                break;
            case "5":
                p.viewDietPlan();
                break;
            case "6":
                p.viewReports();
                break;
            case "7":
                new Discussion().sendMessage();
                break;
            case "8":
                new Discussion().viewConversation();
                break;
            case "9":
                new DoctorFinder().searchBySpecialization();
                break;
            case "10":
                new Hospital().displayHospital();
                break;
            case "11":
                new Pharmacy().displayPharmacy();
                break;
            case "12":
                Reminder rem = new Reminder();
                System.out.println("1. ⏰ View Active Reminders");
                System.out.println("2. ✅ Mark Reminder as Completed");
                System.out.print("👉 Choose: ");
                String remOpt = sc.nextLine().trim();
                if (remOpt.equals("1")) rem.sendReminder();
                else if (remOpt.equals("2")) rem.markCompleted();
                break;
            case "13":
                new Feedback().submitFeedback();
                break;
            case "14":
                Billing b = new Billing();
                while (true) {
                    System.out.println("1. 💳 View Bill details");
                    System.out.println("2. 💰 Make Payment");
                    System.out.print("👉 Choose (1 or 2): ");
                    String billOpt = sc.nextLine().trim();
                    if (billOpt.equals("1")) {
                        b.viewBill();
                        break;
                    } else if (billOpt.equals("2")) {
                        b.makePayment();
                        break;
                    } else {
                        System.out.println("⚠️ Error: Invalid choice. Please choose 1 or 2.");
                    }
                }
                break;
            case "15":
                p.updateProfile();
                break;
            case "16":
                p.changePassword();
                break;
            case "17":
                p.displayQueueStatus();
                break;
            case "18":
                p.logout();
                loggedInUser = null;
                break;
            default:
                System.out.println("⚠️ Invalid option. Please choose 1-18.");
        }
    }

    private static void showDoctorMenu(Doctor d) throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n====================================");
        System.out.println("      👨‍⚕️ DOCTOR PORTAL MENU        ");
        System.out.println("====================================");
        System.out.println("1.  📊 View Dashboard");
        System.out.println("2.  📅 View Scheduled Appointments");
        System.out.println("3.  📝 Create Medical Report");
        System.out.println("4.  💊 Create Prescription & Add Medicines");
        System.out.println("5.  🥗 Create Diet Plan");
        System.out.println("6.  💬 View All Follow-ups & Reply");
        System.out.println("7.  🚦 View Live Waiting Queue");
        System.out.println("8.  ⏰ Update Available Hours");
        System.out.println("9.  👤 Update Profile");
        System.out.println("10. 🔐 Change Password");
        System.out.println("11. 📅 Show Next Appointment");
        System.out.println("12. 🚪 Logout");
        System.out.print("👉 Enter your choice (1-12): ");
        String choice = sc.nextLine().trim();

        switch (choice) {
            case "1":
                d.viewDashboard();
                break;
            case "2":
                d.viewAppointments();
                break;
            case "3":
                d.createReport();
                break;
            case "4":
                d.createPrescription();
                break;
            case "5":
                d.createDietPlan();
                break;
            case "6":
                d.showFollowUpsAndReply();
                break;
            case "7":
                new QueueManager().displayQueue();
                break;
            case "8":
                System.out.print("👉 Enter availability string (e.g. Mon-Fri 9AM-2PM): ");
                String avail = sc.nextLine();
                if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
                    DBConnection.initialize();
                }
                java.sql.PreparedStatement ps = DBConnection.conn.prepareStatement("UPDATE doctors SET availability = ? WHERE user_id = ?");
                ps.setString(1, avail);
                ps.setInt(2, d.userId);
                ps.executeUpdate();
                ps.close();
                System.out.println("✅ Availability updated successfully.");
                break;
            case "9":
                d.updateProfile();
                break;
            case "10":
                d.changePassword();
                break;
            case "11":
                d.showNextAppointment();
                break;
            case "12":
                d.logout();
                loggedInUser = null;
                break;
            default:
                System.out.println("⚠️ Invalid option. Please choose 1-12.");
        }
    }

    private static void showAdminMenu(Admin a) throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n====================================");
        System.out.println("       🛡️ ADMIN PORTAL MENU         ");
        System.out.println("====================================");
        System.out.println("1.  📊 System Overview & Analytics");
        System.out.println("2.  👨‍⚕️ Add New Doctor");
        System.out.println("3.  ❌ Remove Doctor");
        System.out.println("4.  🧑‍⚕️ Register New Patient");
        System.out.println("5.  ❌ Remove Patient");
        System.out.println("6.  👤 List All System Users");
        System.out.println("7.  📅 Manage Appointments");
        System.out.println("8.  🚦 Manage Waiting Queue");
        System.out.println("9.  📋 Manage Medical History Records");
        System.out.println("10. 💳 Manage Billing & Payments");
        System.out.println("11. ⭐ View Doctor Feedback Ratings");
        System.out.println("12. 🧪 Manage Lab Tests");
        System.out.println("13. 📜 System Audit Logs");
        System.out.println("14. 👤 Update Profile");
        System.out.println("15. 🔐 Change Password");
        System.out.println("16. 🚪 Logout");
        System.out.print("👉 Enter your choice (1-16): ");
        String choice = sc.nextLine().trim();

        switch (choice) {
            case "1":
                a.viewSystemReports();
                break;
            case "2":
                a.addDoctor();
                break;
            case "3":
                a.removeDoctor();
                break;
            case "4":
                a.addPatient();
                break;
            case "5":
                a.removePatient();
                break;
            case "6":
                a.manageUsers();
                break;
            case "7":
                a.manageAppointments();
                break;
            case "8":
                QueueManager qm = new QueueManager();
                System.out.println("1. 🚦 Display Queue");
                System.out.println("2. ➕ Add Patient to Queue");
                System.out.println("3. ➖ Remove Patient from Queue");
                System.out.println("4. 🚨 Prioritize Emergency Patient");
                System.out.println("5. ⏰ Calculate Waiting Time");
                System.out.print("👉 Choose: ");
                String qOpt = sc.nextLine().trim();
                if (qOpt.equals("1")) qm.displayQueue();
                else if (qOpt.equals("2")) qm.addPatient();
                else if (qOpt.equals("3")) qm.removePatient();
                else if (qOpt.equals("4")) qm.prioritizeEmergency();
                else if (qOpt.equals("5")) qm.calculateWaitingTime();
                break;
            case "9":
                MedicalRecord mr = new MedicalRecord();
                System.out.println("1. ➕ Add Medical Record");
                System.out.println("2. ⚙️ Update Medical Record");
                System.out.println("3. 📋 View Medical Record");
                System.out.print("👉 Choose: ");
                String mrOpt = sc.nextLine().trim();
                if (mrOpt.equals("1")) mr.addRecord();
                else if (mrOpt.equals("2")) mr.updateRecord();
                else if (mrOpt.equals("3")) mr.viewRecord();
                break;
            case "10":
                a.manageBilling();
                break;
            case "11":
                a.viewFeedback();
                break;
            case "12":
                LabTest lt = new LabTest();
                System.out.println("1. 🧪 Book Lab Test");
                System.out.println("2. 🚦 Update Lab Test Status");
                System.out.println("3. 💾 Save Lab Test Result");
                System.out.println("4. 📋 View Lab Result");
                System.out.print("👉 Choose: ");
                String ltOpt = sc.nextLine().trim();
                if (ltOpt.equals("1")) lt.bookLabTest();
                else if (ltOpt.equals("2")) lt.updateStatus();
                else if (ltOpt.equals("3")) lt.updateResult();
                else if (ltOpt.equals("4")) lt.viewResult();
                break;
            case "13":
                AuditLog al = new AuditLog();
                System.out.println("1. 📋 View Last 50 Logs");
                System.out.println("2. 🔍 Search Logs by User");
                System.out.println("3. 🗑️ Purge/Delete Old Logs");
                System.out.print("👉 Choose: ");
                String alOpt = sc.nextLine().trim();
                if (alOpt.equals("1")) al.viewLogs();
                else if (alOpt.equals("2")) al.searchLogs();
                else if (alOpt.equals("3")) al.deleteOldLogs();
                break;
            case "14":
                a.updateProfile();
                break;
            case "15":
                a.changePassword();
                break;
            case "16":
                a.logout();
                loggedInUser = null;
                break;
            default:
                System.out.println("⚠️ Invalid option. Please choose 1-16.");
        }
    }
}