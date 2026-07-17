import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Pharmacy {
    String pharmacyName = "HealthConnect Smart Government Pharmacy";
    String address = "Ground Floor, HealthConnect Hospital Building, Gandhinagar, Gujarat";

    public void displayPharmacy() throws Exception {
        System.out.println("\n💊 --- Nearby Pharmacy Info ---");
        System.out.println("🏥 Pharmacy Name : " + this.pharmacyName);
        System.out.println("📍 Address       : " + this.address);
        System.out.println("\n💊 Available Medicines Stock:");
        System.out.printf("%-5s | %-25s | %-10s | %-10s | %-8s | %-20s\n", "ID", "Name", "Stock", "Expiry", "Price", "Manufacturer");
        System.out.println("-----------------------------------------------------------------------------------------");

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        PreparedStatement ps = DBConnection.conn.prepareStatement("SELECT * FROM pharmacy ORDER BY medicine_name");
        ResultSet rs = ps.executeQuery();
        boolean found = false;
        while (rs.next()) {
            found = true;
            System.out.printf("%-5d | %-25s | %-10d | %-10s | Rs. %-5.2f | %-20s\n",
                    rs.getInt("medicine_id"),
                    rs.getString("medicine_name"),
                    rs.getInt("stock"),
                    rs.getDate("expiry").toString(),
                    rs.getDouble("price"),
                    rs.getString("manufacturer")
            );
        }
        if (!found) {
            System.out.println("📭 No medicines in stock.");
        }
        rs.close();
        ps.close();
    }
}