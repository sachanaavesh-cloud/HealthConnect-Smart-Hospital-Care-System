import java.sql.*;
import java.util.*;

// Manages patient feedback submission, updates, viewing, deletion, and rating calculation.
public class Feedback {

    // Submits feedback and rating for a doctor.
    public void submitFeedback() throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n⭐ --- Submit Feedback ---");

        int patientId = 0;
        if (Main.loggedInUser instanceof Patient) {
            patientId = ((Patient) Main.loggedInUser).getPatientId();
        } else {
            while (true) {
                System.out.print("👉 Enter Patient ID: ");
                try {
                    patientId = Integer.parseInt(sc.nextLine().trim());
                    if (patientId > 0) {
                        break;
                    }
                    System.out.println("⚠️ Error: Patient ID must be a positive integer.");
                } catch (NumberFormatException e) {
                    System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
                }
            }
        }

        int doctorId = 0;
        while (true) {
            System.out.print("👉 Enter Doctor ID: ");
            try {
                doctorId = Integer.parseInt(sc.nextLine().trim());
                if (doctorId > 0) {
                    break;
                }
                System.out.println("⚠️ Error: Doctor ID must be a positive integer.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
            }
        }

        int stars = 0;
        while (true) {
            System.out.print("⭐ Enter Rating (1-5): ");
            try {
                stars = Integer.parseInt(sc.nextLine().trim());
                if (stars >= 1 && stars <= 5) {
                    break;
                }
                System.out.println("⚠️ Error: Rating must be between 1 and 5.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
            }
        }

        System.out.print("💬 Enter Comments: ");
        String commentsText = sc.nextLine();

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call SubmitFeedback(?, ?, ?, ?)}");
        stmt.setInt(1, patientId);
        stmt.setInt(2, doctorId);
        stmt.setInt(3, stars);
        stmt.setString(4, commentsText);
        stmt.execute();
        stmt.close();
        System.out.println("🎉 Feedback submitted successfully!");
        Main.logActivity(1, "INSERT", "feedback");
    }
}