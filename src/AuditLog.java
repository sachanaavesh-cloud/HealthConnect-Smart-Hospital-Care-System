import java.sql.*;
import java.util.*;

public class AuditLog {
    static class LogEntry {
        int logId;
        int userId;
        String operation;
        String tableName;
        String timestamp;

        public LogEntry(int logId, int userId, String operation, String tableName, String timestamp) {
            this.logId = logId;
            this.userId = userId;
            this.operation = operation;
            this.tableName = tableName;
            this.timestamp = timestamp;
        }
    }

    Stack<LogEntry> logStack = new Stack<>();

    public void viewLogs() throws Exception {
        System.out.println("\n📜 --- System Audit Logs (Last 50 entries) ---");
        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call ViewAuditLogs()}");
        ResultSet rs = stmt.executeQuery();

        logStack.clear();
        while (rs.next()) {
            logStack.push(new LogEntry(
                    rs.getInt("log_id"),
                    rs.getInt("user_id"),
                    rs.getString("operation"),
                    rs.getString("table_name"),
                    rs.getString("timestamp")
            ));
        }
        rs.close();
        stmt.close();

        System.out.printf("%-8s | %-8s | %-12s | %-20s | %-25s\n", "Log ID", "User ID", "Operation", "Table Name", "Timestamp");
        System.out.println("-----------------------------------------------------------------------------------------");
        boolean found = false;
        while (!logStack.isEmpty()) {
            found = true;
            LogEntry log = logStack.pop();
            System.out.printf("%-8d | %-8d | %-12s | %-20s | %-25s\n",
                    log.logId,
                    log.userId,
                    log.operation,
                    log.tableName,
                    log.timestamp
            );
        }
        if (!found) {
            System.out.println("📭 No logs found.");
        }
    }

    public void searchLogs() throws Exception {
        Scanner sc = new Scanner(System.in);

        int uId = 0;
        while (true) {
            System.out.print("👉 Enter User ID to filter logs (or 0 for all): ");
            try {
                uId = Integer.parseInt(sc.nextLine().trim());
                if (uId >= 0) {
                    break;
                }
                System.out.println("⚠️ Error: User ID must be a positive integer.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
            }
        }

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        String query = (uId == 0) ? "SELECT * FROM audit_log" : "SELECT * FROM audit_log WHERE user_id = ?";
        PreparedStatement ps = DBConnection.conn.prepareStatement(query);
        if (uId > 0) {
            ps.setInt(1, uId);
        }
        ResultSet rs = ps.executeQuery();

        Stack<LogEntry> searchStack = new Stack<>();
        while (rs.next()) {
            searchStack.push(new LogEntry(
                    rs.getInt("log_id"),
                    rs.getInt("user_id"),
                    rs.getString("operation"),
                    rs.getString("table_name"),
                    rs.getString("timestamp")
            ));
        }
        rs.close();
        ps.close();

        System.out.println("\n🔍 --- Audit Search Results ---");
        System.out.printf("%-8s | %-8s | %-12s | %-20s | %-25s\n", "Log ID", "User ID", "Operation", "Table Name", "Timestamp");
        System.out.println("-----------------------------------------------------------------------------------------");
        boolean found = false;
        while (!searchStack.isEmpty()) {
            found = true;
            LogEntry log = searchStack.pop();
            System.out.printf("%-8d | %-8d | %-12s | %-20s | %-25s\n",
                    log.logId,
                    log.userId,
                    log.operation,
                    log.tableName,
                    log.timestamp
            );
        }
        if (!found) {
            System.out.println("📭 No matching logs found.");
        }
    }

    public void deleteOldLogs() throws Exception {
        Scanner sc = new Scanner(System.in);

        String dateStr = "";
        while (true) {
            System.out.print("👉 Enter Date threshold (YYYY-MM-DD) to delete older logs: ");
            dateStr = sc.nextLine().trim();
            boolean isValid = true;
            if (dateStr.length() != 10 || dateStr.charAt(4) != '-' || dateStr.charAt(7) != '-') {
                isValid = false;
            } else {
                for (int i = 0; i < dateStr.length(); i++) {
                    if (i == 4 || i == 7) continue;
                    char ch = dateStr.charAt(i);
                    if (ch < '0' || ch > '9') {
                        isValid = false;
                        break;
                    }
                }
                if (isValid) {
                    int year = Integer.parseInt(dateStr.substring(0, 4));
                    int month = Integer.parseInt(dateStr.substring(5, 7));
                    int day = Integer.parseInt(dateStr.substring(8, 10));

                    if (month < 1 || month > 12) {
                        isValid = false;
                    } else if (day < 1) {
                        isValid = false;
                    } else {
                        int maxDays = 31;
                        if (month == 4 || month == 6 || month == 9 || month == 11) {
                            maxDays = 30;
                        } else if (month == 2) {
                            boolean isLeap = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
                            maxDays = isLeap ? 29 : 28;
                        }
                        if (day > maxDays) {
                            isValid = false;
                        }
                    }
                }
            }
            if (isValid) {
                break;
            }
            System.out.println("⚠️ Error: Date must be a valid calendar date in YYYY-MM-DD format.");
        }

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call DeleteOldLogs(?)}");
        stmt.setString(1, dateStr + " 00:00:00");
        stmt.execute();
        stmt.close();
        System.out.println("🗑️ Old logs (older than " + dateStr + ") deleted successfully.");
    }
}