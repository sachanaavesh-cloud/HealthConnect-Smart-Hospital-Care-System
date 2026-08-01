import java.sql.*;
import java.util.*;

// Represents an appointment between a patient and a doctor.
public class Appointment {
    int appointmentId = 0;
    Patient patient = new Patient();
    Doctor doctor = new Doctor();
    String appointmentDate = "";
    String appointmentTime = "";
    String status = "";

    // Books a new appointment using the BookAppointment stored procedure.
    public void bookAppointment() throws Exception {
        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call BookAppointment(?, ?, ?, ?)}");
        stmt.setInt(1, patient.userId);
        stmt.setInt(2, doctor.userId);
        stmt.setString(3, appointmentDate);
        stmt.setString(4, appointmentTime);
        stmt.execute();
        stmt.close();
        System.out.println("🎉 Appointment booked successfully.");
    }

    // Cancels an existing appointment using the CancelAppointment stored procedure.
    public void cancelAppointment() throws Exception {
        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call CancelAppointment(?)}");
        stmt.setInt(1, this.appointmentId);
        stmt.execute();
        stmt.close();
        this.status = "Cancelled";
        System.out.println("🗑️ Appointment ID " + this.appointmentId + " cancelled.");
    }

    // Updates the appointment status using the UpdateAppointmentStatus stored procedure.
    public void updateStatus() throws Exception {
        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call UpdateAppointmentStatus(?, ?)}");
        stmt.setInt(1, this.appointmentId);
        stmt.setString(2, this.status);
        stmt.execute();
        stmt.close();
        System.out.println("✅ Appointment status updated successfully.");
    }

    // Retrieves and displays complete appointment details from the database.
    public void getDetails() throws Exception {
        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call GetAppointmentDetails(?)}");
        stmt.setInt(1, this.appointmentId);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            System.out.println("\n📅 --- Appointment Details ---");
            System.out.println("🔑 ID          : " + rs.getInt("appointment_id"));
            System.out.println("👤 Patient Name: " + rs.getString("first_name") + " " + rs.getString("last_name"));
            System.out.println("👨‍⚕️ Doctor Name : " + rs.getString("doctor_name"));
            System.out.println("📅 Date        : " + rs.getString("appointment_date"));
            System.out.println("⏰ Time        : " + rs.getString("appointment_time"));
            System.out.println("🚦 Status      : " + rs.getString("status"));
            System.out.println("🚨 Priority    : " + rs.getString("priority"));
            System.out.println("💬 Remarks     : " + rs.getString("remarks"));
            this.status = rs.getString("status");
        } else {
            System.out.println("📭 Appointment not found.");
        }
        rs.close();
        stmt.close();
    }
}