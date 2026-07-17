import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class User {
    int userId = 0;
    String name = "";
    String email = "";
    String password = "";
    String phone = "";
    String role = "";

    public void login() throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.print("📧 Enter Email/Username: ");
        String username = sc.nextLine().trim();
        System.out.print("🔑 Enter Password: ");
        String pwd = sc.nextLine();

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call LoginUser(?, ?)}");
        stmt.setString(1, username);
        stmt.setString(2, pwd);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            this.userId = rs.getInt("user_id");
            this.email = rs.getString("username");
            this.role = rs.getString("role");
            this.password = pwd;
            System.out.println("🎉 Login successful! Welcome, " + this.email + " (" + this.role + ")");

            // Log login activity in login_history table
            PreparedStatement ps = DBConnection.conn.prepareStatement("INSERT INTO login_history(user_id, login_time, ip_address) VALUES (?, NOW(), '127.0.0.1')");
            ps.setInt(1, this.userId);
            ps.executeUpdate();
            ps.close();

            Main.logActivity(this.userId, "LOGIN", "users");
        } else {
            System.out.println("❌ Invalid email/username or password.");
        }
        rs.close();
        stmt.close();
    }

    public void logout() throws Exception {
        if (this.userId == 0) {
            System.out.println("⚠️ No user is currently logged in.");
            return;
        }
        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        PreparedStatement ps = DBConnection.conn.prepareStatement("UPDATE login_history SET logout_time = NOW() WHERE user_id = ? AND logout_time IS NULL");
        ps.setInt(1, this.userId);
        ps.executeUpdate();
        ps.close();
        System.out.println("🚪 Logged out successfully.");
        Main.logActivity(this.userId, "LOGOUT", "users");
        this.userId = 0;
        this.name = "";
        this.email = "";
        this.password = "";
        this.phone = "";
        this.role = "";
    }

    public void updateProfile() throws Exception {
        if (this.userId == 0) {
            System.out.println("⚠️ Please login first.");
            return;
        }
        Scanner sc = new Scanner(System.in);

        String newName = "";
        while (true) {
            System.out.print("👤 Enter New Name: ");
            newName = sc.nextLine().trim();
            boolean isValid = true;
            if (newName.isEmpty()) {
                isValid = false;
            } else {
                for (int i = 0; i < newName.length(); i++) {
                    char ch = newName.charAt(i);
                    if (!((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == ' ')) {
                        isValid = false;
                        break;
                    }
                }
            }
            if (isValid) {
                break;
            }
            System.out.println("⚠️ Error: Name must contain letters and spaces only.");
        }

        String newPhone = "";
        while (true) {
            System.out.print("📞 Enter New Phone: ");
            newPhone = sc.nextLine().trim();
            boolean isValid = true;
            if (newPhone.length() != 10) {
                isValid = false;
            } else {
                for (int i = 0; i < newPhone.length(); i++) {
                    char ch = newPhone.charAt(i);
                    if (ch < '0' || ch > '9') {
                        isValid = false;
                        break;
                    }
                }
            }
            if (isValid) {
                break;
            }
            System.out.println("⚠️ Error: Phone number must be exactly 10 digits.");
        }

        String newEmail = "";
        while (true) {
            System.out.print("📧 Enter New Email/Username: ");
            newEmail = sc.nextLine().trim();
            int atIndex = newEmail.indexOf('@');
            int dotIndex = newEmail.lastIndexOf('.');
            boolean isValid = (atIndex > 0 && dotIndex > atIndex + 1 && dotIndex < newEmail.length() - 1);
            if (isValid) {
                break;
            }
            System.out.println("⚠️ Error: Please enter a valid email address.");
        }

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        DBConnection.conn.setAutoCommit(false);
        try {
            // Update username in users table
            PreparedStatement ps = DBConnection.conn.prepareStatement("UPDATE users SET username = ? WHERE user_id = ?");
            ps.setString(1, newEmail);
            ps.setInt(2, this.userId);
            ps.executeUpdate();
            ps.close();

            // Update respective table based on role
            if (this.role.equalsIgnoreCase("Patient")) {
                PreparedStatement ps1 = DBConnection.conn.prepareStatement("UPDATE patients SET first_name = ?, phone = ? WHERE user_id = ?");
                ps1.setString(1, newName);
                ps1.setString(2, newPhone);
                ps1.setInt(3, this.userId);
                ps1.executeUpdate();
                ps1.close();
            } else if (this.role.equalsIgnoreCase("Doctor")) {
                PreparedStatement ps2 = DBConnection.conn.prepareStatement("UPDATE doctors SET name = ?, phone = ? WHERE user_id = ?");
                ps2.setString(1, newName);
                ps2.setString(2, newPhone);
                ps2.setInt(3, this.userId);
                ps2.executeUpdate();
                ps2.close();
            }

            // Call UpdateUserProfile procedure
            CallableStatement stmt = DBConnection.conn.prepareCall("{call UpdateUserProfile(?, ?)}");
            stmt.setInt(1, this.userId);
            stmt.setString(2, "Active");
            stmt.execute();
            stmt.close();

            DBConnection.conn.commit();
            this.name = newName;
            this.phone = newPhone;
            this.email = newEmail;
            System.out.println("✅ Profile updated successfully.");
        } catch (Exception ex) {
            DBConnection.conn.rollback();
            throw ex;
        } finally {
            DBConnection.conn.setAutoCommit(true);
        }
    }

    public void changePassword() throws Exception {
        if (this.userId == 0) {
            System.out.println("⚠️ Please login first.");
            return;
        }
        Scanner sc = new Scanner(System.in);
        System.out.print("🔑 Enter Old Password: ");
        String oldPwd = sc.nextLine();
        System.out.print("🔑 Enter New Password: ");
        String newPwd = sc.nextLine();

        if (newPwd.trim().isEmpty()) {
            System.out.println("⚠️ New password cannot be empty.");
            return;
        }

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        // Check old password first
        boolean oldPwdCorrect = false;
        PreparedStatement ps = DBConnection.conn.prepareStatement("SELECT password FROM users WHERE user_id = ?");
        ps.setInt(1, this.userId);
        ResultSet rs = ps.executeQuery();
        if (rs.next() && rs.getString("password").equals(oldPwd)) {
            oldPwdCorrect = true;
        }
        rs.close();
        ps.close();

        if (!oldPwdCorrect) {
            System.out.println("❌ Old password incorrect. Password was not changed.");
            return;
        }

        // Call ChangePassword procedure
        CallableStatement stmt = DBConnection.conn.prepareCall("{call ChangePassword(?, ?)}");
        stmt.setInt(1, this.userId);
        stmt.setString(2, newPwd);
        stmt.execute();
        stmt.close();

        this.password = newPwd;
        System.out.println("✅ Password changed successfully.");
    }
}