import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class DoctorFinder {

    public void searchBySpecialization() throws Exception {
        Scanner sc = new Scanner(System.in);

        String spec = "";
        while (true) {
            System.out.println("\n🩺 --- Choose Specialization ---");
            System.out.println("1. Cardiologist");
            System.out.println("2. Dentist");
            System.out.println("3. Orthopedic");
            System.out.println("4. Neurologist");
            System.out.println("5. Pediatrician");
            System.out.println("6. Gynecologist");
            System.out.print("👉 Enter choice (1-6): ");
            String opt = sc.nextLine().trim();
            if (opt.equals("1")) { spec = "Cardiologist"; break; }
            else if (opt.equals("2")) { spec = "Dentist"; break; }
            else if (opt.equals("3")) { spec = "Orthopedic"; break; }
            else if (opt.equals("4")) { spec = "Neurologist"; break; }
            else if (opt.equals("5")) { spec = "Pediatrician"; break; }
            else if (opt.equals("6")) { spec = "Gynecologist"; break; }
            else {
                System.out.println("⚠️ Error: Invalid option. Please choose between 1 and 6.");
            }
        }

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        PreparedStatement ps = DBConnection.conn.prepareStatement(
                "SELECT *, AverageDoctorRating(doctor_id) AS avg_rating FROM doctors WHERE specialization LIKE ? ORDER BY avg_rating DESC");
        ps.setString(1, "%" + spec + "%");
        ResultSet rs = ps.executeQuery();
        System.out.println("\n🔍 --- Search Results ---");
        boolean found = false;
        while (rs.next()) {
            found = true;
            System.out.println("👨‍⚕️ Doctor ID: " + rs.getInt("doctor_id"));
            System.out.println("👤 Name     : " + rs.getString("name"));
            System.out.println("🩺 Specialty: " + rs.getString("specialization"));
            System.out.println("🏢 Room No  : " + rs.getString("room_no"));
            System.out.println("⏰ Schedule : " + rs.getString("availability"));
            System.out.println("🎓 Experience: " + rs.getInt("experience") + " years");
            System.out.println("💰 Consultation Fee: Rs. " + rs.getDouble("consultation_fee"));
            System.out.println("⭐ Average Rating: " + rs.getDouble("avg_rating") + " stars");
            System.out.println("----------------------------------------");
        }
        if (!found) {
            System.out.println("📭 No doctors found with that specialization.");
        }
        rs.close();
        ps.close();
    }

    public void searchAvailableDoctors() throws Exception {
        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        PreparedStatement ps = DBConnection.conn.prepareStatement(
                "SELECT *, AverageDoctorRating(doctor_id) AS avg_rating FROM doctors WHERE availability IS NOT NULL AND availability <> '' ORDER BY avg_rating DESC");
        ResultSet rs = ps.executeQuery();
        System.out.println("\n👨‍⚕️ --- Active/Available Doctors Today ---");
        boolean found = false;
        while (rs.next()) {
            found = true;
            System.out.printf("👨‍⚕️ ID: %-3d | Name: %-22s | Specialty: %-15s | Room: %-5s | Hours: %-10s | Fee: Rs. %-5.2f | Rating: ⭐ %.2f\n",
                    rs.getInt("doctor_id"),
                    rs.getString("name"),
                    rs.getString("specialization"),
                    rs.getString("room_no"),
                    rs.getString("availability"),
                    rs.getDouble("consultation_fee"),
                    rs.getDouble("avg_rating")
            );
        }
        if (!found) {
            System.out.println("📭 No available doctors found.");
        }
        rs.close();
        ps.close();
    }

    public void bookDoctor() throws Exception {
        System.out.println("\n📅 Redirecting you to booking menu...");
        Patient p = (Patient) Main.loggedInUser;
        p.bookAppointment();
    }
}