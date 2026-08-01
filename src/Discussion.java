import java.sql.*;
import java.util.*;

// Manages communication between patients and doctors.
public class Discussion {
    Appointment appointment = new Appointment();
    String message = "";
    String time = "";

    // Sends a message from a patient to a doctor.
    public void sendMessage() throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n💬 --- Send Message ---");

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

        System.out.print("💬 Enter Message Content: ");
        String msg = sc.nextLine();

        System.out.print("📅 Enter Next Visit Date (YYYY-MM-DD, or leave empty for none): ");
        String nextVisitInput = sc.nextLine().trim();

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call SendMessage(?, ?, ?, ?)}");
        stmt.setInt(1, patientId);
        stmt.setInt(2, doctorId);
        stmt.setString(3, msg);
        if (nextVisitInput.isEmpty()) {
            stmt.setNull(4, java.sql.Types.DATE);
        } else {
            stmt.setString(4, nextVisitInput);
        }
        stmt.execute();
        stmt.close();

        System.out.println("🎉 Message sent successfully!");
    }

    // Displays the conversation between a patient and a doctor.
    public void viewConversation() throws Exception {
        Scanner sc = new Scanner(System.in);

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

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call ViewConversation(?, ?)}");
        stmt.setInt(1, patientId);
        stmt.setInt(2, doctorId);
        ResultSet rs = stmt.executeQuery();
        System.out.println("\n💬 --- Chat History ---");
        boolean found = false;
        while (rs.next()) {
            found = true;
            System.out.println("💬 Message: " + rs.getString("remarks"));
        }
        if (!found) {
            System.out.println("📭 No conversation history found.");
        }
        rs.close();
        stmt.close();
    }

    // Uploads and shares a report file in the discussion.
    public void uploadReport() throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n📎 --- Upload Report File to Discussion ---");

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

        System.out.print("📁 Enter Report File Path (e.g. report.pdf): ");
        String filePath = sc.nextLine();

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call SendMessage(?, ?, ?, ?)}");
        stmt.setInt(1, patientId);
        stmt.setInt(2, doctorId);
        stmt.setString(3, "📎 Attached Report File: " + filePath);
        stmt.setNull(4, java.sql.Types.DATE);
        stmt.execute();
        stmt.close();
        System.out.println("✅ File uploaded and shared in conversation successfully!");
    }

    public void sharePDF() throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n📄 --- Share PDF Document ---");

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

        System.out.print("📄 Enter PDF File Path (e.g. invoice.pdf): ");
        String pdfPath = sc.nextLine();

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call SendMessage(?, ?, ?, ?)}");
        stmt.setInt(1, patientId);
        stmt.setInt(2, doctorId);
        stmt.setString(3, "📄 Shared PDF Document: " + pdfPath);
        stmt.setNull(4, java.sql.Types.DATE);
        stmt.execute();
        stmt.close();
        System.out.println("✅ PDF document shared in conversation successfully!");
    }
}