import java.sql.*;
import java.util.*;

// Generates and verifies Health IDs for patients.
public class HealthIDGenerator {

    // Generates a unique Health ID using the GenerateHealthID stored function.
    public String generateHealthID() throws Exception {
        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{? = call GenerateHealthID()}");
        stmt.registerOutParameter(1, Types.VARCHAR);
        stmt.execute();
        String id = stmt.getString(1);
        stmt.close();
        System.out.println("💳 Generated Health ID: " + id);
        return id;
    }

    // Verifies whether the given Health ID exists in the database.
    public boolean verifyHealthID(String healthId) throws Exception {
        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        PreparedStatement ps = DBConnection.conn.prepareStatement("SELECT COUNT(*) FROM patients WHERE health_id = ?");
        ps.setString(1, healthId);
        ResultSet rs = ps.executeQuery();
        boolean isValid = false;
        if (rs.next()) {
            isValid = rs.getInt(1) > 0;
            System.out.println("🔍 Verification Result for Health ID (" + healthId + "): " + (isValid ? "✅ VALID" : "❌ INVALID"));
        }
        rs.close();
        ps.close();
        return isValid;
    }
}