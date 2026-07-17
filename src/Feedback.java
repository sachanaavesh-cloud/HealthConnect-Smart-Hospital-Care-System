import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.Scanner;

public class Feedback {
    int feedbackId = 0;
    Patient patient = new Patient();
    Doctor doctor = new Doctor();
    Appointment appointment = new Appointment();
    int rating = 0;
    String comments = "";
    String feedbackDate = "";

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

    public void updateFeedback() throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n⭐ --- Update Feedback ---");

        int id = 0;
        while (true) {
            System.out.print("👉 Enter Feedback ID to update: ");
            try {
                id = Integer.parseInt(sc.nextLine().trim());
                if (id > 0) {
                    break;
                }
                System.out.println("⚠️ Error: Feedback ID must be a positive integer.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
            }
        }

        int stars = 0;
        while (true) {
            System.out.print("⭐ Enter New Rating (1-5): ");
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

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call UpdateFeedback(?, ?)}");
        stmt.setInt(1, id);
        stmt.setInt(2, stars);
        stmt.execute();
        stmt.close();
        System.out.println("✅ Feedback updated successfully.");
        Main.logActivity(1, "UPDATE", "feedback");
    }

    public void viewFeedback() throws Exception {
        Scanner sc = new Scanner(System.in);

        int docId = 0;
        while (true) {
            System.out.print("👉 Enter Doctor ID to view feedback: ");
            try {
                docId = Integer.parseInt(sc.nextLine().trim());
                if (docId > 0) {
                    break;
                }
                System.out.println("⚠️ Error: Doctor ID must be a positive integer.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
            }
        }

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call ViewFeedback(?)}");
        stmt.setInt(1, docId);
        ResultSet rs = stmt.executeQuery();
        System.out.println("\n⭐ --- Doctor Feedback ---");
        boolean found = false;
        while (rs.next()) {
            found = true;
            System.out.println("🔑 Feedback ID: " + rs.getInt("feedback_id"));
            System.out.println("👤 Patient    : " + rs.getString("patient_id"));
            System.out.println("⭐ Rating     : " + rs.getInt("rating") + " stars");
            System.out.println("💬 Comments   : " + rs.getString("comments"));
            System.out.println("📅 Date       : " + rs.getString("date"));
            System.out.println("----------------------------------------");
        }
        if (!found) {
            System.out.println("📭 No feedback found for this doctor.");
        }
        rs.close();
        stmt.close();
    }

    public void deleteFeedback() throws Exception {
        Scanner sc = new Scanner(System.in);

        int id = 0;
        while (true) {
            System.out.print("👉 Enter Feedback ID to delete: ");
            try {
                id = Integer.parseInt(sc.nextLine().trim());
                if (id > 0) {
                    break;
                }
                System.out.println("⚠️ Error: Feedback ID must be a positive integer.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
            }
        }

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call DeleteFeedback(?)}");
        stmt.setInt(1, id);
        stmt.execute();
        stmt.close();
        System.out.println("🗑️ Feedback deleted successfully.");
        Main.logActivity(1, "DELETE", "feedback");
    }

    public void calculateAverageRating() throws Exception {
        Scanner sc = new Scanner(System.in);

        int docId = 0;
        while (true) {
            System.out.print("👉 Enter Doctor ID: ");
            try {
                docId = Integer.parseInt(sc.nextLine().trim());
                if (docId > 0) {
                    break;
                }
                System.out.println("⚠️ Error: Doctor ID must be a positive integer.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
            }
        }

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{? = call AverageDoctorRating(?)}");
        stmt.registerOutParameter(1, Types.DECIMAL);
        stmt.setInt(2, docId);
        stmt.execute();
        double avg = stmt.getDouble(1);
        stmt.close();
        System.out.println("⭐ Average Doctor Rating: " + avg + " / 5.0");
    }
}