import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

public class HealthIDGenerator {

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