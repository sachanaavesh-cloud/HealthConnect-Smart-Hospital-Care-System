import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Dashboard {

    public void showPatientDashboard() throws Exception {
        if (Main.loggedInUser == null || !(Main.loggedInUser instanceof Patient)) {
            System.out.println("⚠️ No patient is currently logged in.");
            return;
        }
        Patient p = (Patient) Main.loggedInUser;
        System.out.println("\n========================================================");
        System.out.println("            📊 PATIENT DIGITAL DASHBOARD 📊             ");
        System.out.println("========================================================");
        System.out.println("CN Health ID   : " + p.healthId);
        System.out.println("👤 Patient Name: " + p.name);
        System.out.println("📧 Email       : " + p.email);
        System.out.println("📞 Phone       : " + p.phone);
        System.out.println("🩸 Blood Group : " + p.bloodGroup);
        System.out.println("🤧 Allergies   : " + p.allergies);
        System.out.println("🚨 Emergency   : " + p.emergencyContact);
        System.out.println("--------------------------------------------------------");

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        // Fetch appointment counts
        int patientId = 0;
        PreparedStatement ps = DBConnection.conn.prepareStatement("SELECT patient_id FROM patients WHERE user_id = ?");
        ps.setInt(1, p.userId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            patientId = rs.getInt("patient_id");
        }
        rs.close();
        ps.close();

        if (patientId > 0) {
            // Total appointments
            PreparedStatement ps1 = DBConnection.conn.prepareStatement("SELECT COUNT(*) FROM appointments WHERE patient_id = ?");
            ps1.setInt(1, patientId);
            ResultSet rs1 = ps1.executeQuery();
            if (rs1.next()) {
                System.out.println("📅 Total Appointments booked   : " + rs1.getInt(1));
            }
            rs1.close();
            ps1.close();

            // Pending reminders
            PreparedStatement ps2 = DBConnection.conn.prepareStatement("SELECT COUNT(*) FROM reminders WHERE patient_id = ? AND status = 'Pending'");
            ps2.setInt(1, patientId);
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next()) {
                System.out.println("🔔 Pending Reminders           : " + rs2.getInt(1));
            }
            rs2.close();
            ps2.close();

            // Billing info (using payments table)
            PreparedStatement ps3 = DBConnection.conn.prepareStatement("SELECT COUNT(*) FROM payments WHERE patient_id = ? AND status = 'Pending'");
            ps3.setInt(1, patientId);
            ResultSet rs3 = ps3.executeQuery();
            if (rs3.next()) {
                System.out.println("💸 Unpaid Bills                : " + rs3.getInt(1));
            }
            rs3.close();
            ps3.close();

            // Display live queue status for next appointment
            p.displayQueueStatus();
        }
        System.out.println("========================================================");
    }

    public void showDoctorDashboard() throws Exception {
        if (Main.loggedInUser == null || !(Main.loggedInUser instanceof Doctor)) {
            System.out.println("⚠️ No doctor is currently logged in.");
            return;
        }
        Doctor d = (Doctor) Main.loggedInUser;
        System.out.println("\n========================================================");
        System.out.println("             📊 DOCTOR DIGITAL DASHBOARD 📊             ");
        System.out.println("========================================================");
        System.out.println("👤 Doctor Name   : " + d.name);
        System.out.println("🩺 Specialization: " + d.specialization);
        System.out.println("🎓 Experience    : " + d.experience + " years");
        System.out.println("📧 Email         : " + d.email);
        System.out.println("📞 Phone         : " + d.phone);
        System.out.println("--------------------------------------------------------");

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        int doctorId = 0;
        PreparedStatement ps = DBConnection.conn.prepareStatement("SELECT doctor_id FROM doctors WHERE user_id = ?");
        ps.setInt(1, d.userId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            doctorId = rs.getInt("doctor_id");
        }
        rs.close();
        ps.close();

        if (doctorId > 0) {
            // Today's appointments
            PreparedStatement ps1 = DBConnection.conn.prepareStatement("SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND appointment_date = CURDATE()");
            ps1.setInt(1, doctorId);
            ResultSet rs1 = ps1.executeQuery();
            if (rs1.next()) {
                System.out.println("📅 Appointments Scheduled Today: " + rs1.getInt(1));
            }
            rs1.close();
            ps1.close();

            // Patients in queue
            PreparedStatement ps2 = DBConnection.conn.prepareStatement("SELECT COUNT(*) FROM queue WHERE doctor_id = ? AND status = 'Waiting'");
            ps2.setInt(1, doctorId);
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next()) {
                System.out.println("🚦 Patients Currently in Queue : " + rs2.getInt(1));
            }
            rs2.close();
            ps2.close();

            // Average Rating
            PreparedStatement ps3 = DBConnection.conn.prepareStatement("SELECT AverageDoctorRating(?)");
            ps3.setInt(1, doctorId);
            ResultSet rs3 = ps3.executeQuery();
            if (rs3.next()) {
                System.out.println("⭐ Average Feedback Rating     : " + rs3.getDouble(1) + " / 5.0");
            }
            rs3.close();
            ps3.close();
        }
        System.out.println("========================================================");
    }
}