import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String url = "jdbc:mysql://localhost:3307/HealthConnect";
    private static final String username = "root";
    private static final String password = "";
    public static Connection conn = null;

    public static void initialize() throws Exception {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(url, username, password);
        }
    }
}