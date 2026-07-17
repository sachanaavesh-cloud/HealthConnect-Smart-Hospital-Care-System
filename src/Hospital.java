import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Hospital {
    String hospitalName = "HealthConnect Smart AI Government Hospital";
    String address = "Opp. Swaminarayan Temple, Sector 20, Gandhinagar, Gujarat";

    public void displayHospital() throws Exception {
        System.out.println("\n🏥 --- Nearby Hospital Info ---");
        System.out.println("🏨 Hospital Name : " + this.hospitalName);
        System.out.println("📍 Address       : " + this.address);
        System.out.println("📞 Emergency Call: +91-79-23214567 / 108 🚑");
        System.out.println("\nActive Specialized Departments:");
        System.out.printf("%-20s | %-15s | %-30s\n", "Department Name", "Floor", "Description");
        System.out.println("----------------------------------------------------------------------------------");

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        PreparedStatement ps = DBConnection.conn.prepareStatement("SELECT * FROM departments");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            System.out.printf("%-20s | %-15s | %-30s\n",
                    rs.getString("department_name"),
                    "🏢 " + rs.getString("floor"),
                    rs.getString("description")
            );
        }
        rs.close();
        ps.close();
    }
}