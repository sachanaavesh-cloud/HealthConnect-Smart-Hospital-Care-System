import java.sql.*;
import java.util.*;

// Establishes and maintains a single database connection for the HealthConnect system.
public class DBConnection {
    private static final String url = "jdbc:mysql://localhost:3307/HealthConnect";
    private static final String username = "root";
    private static final String password = "";
    public static Connection conn = null;

    // Initializes the database connection if it is not already established.
    public static void initialize() throws Exception {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(url, username, password);
        }
    }
}