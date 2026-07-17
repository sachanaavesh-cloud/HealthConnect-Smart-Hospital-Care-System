import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.Scanner;

public class Billing {
    int billId = 0;
    Patient patient = new Patient();
    Appointment appointment = new Appointment();
    double consultationFee = 0.0;
    double labCharges = 0.0;
    double medicineCharges = 0.0;
    double otherCharges = 0.0;
    double totalAmount = 0.0;
    String paymentStatus = "";
    String paymentMethod = "";
    String billDate = "";

    public void generateBill() throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n💳 --- Generate Bill ---");

        int patientId = 0;
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

        int appId = 0;
        while (true) {
            System.out.print("👉 Enter Appointment ID: ");
            try {
                appId = Integer.parseInt(sc.nextLine().trim());
                if (appId > 0) {
                    break;
                }
                System.out.println("⚠️ Error: Appointment ID must be a positive integer.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
            }
        }

        double amount = 0.0;
        while (true) {
            System.out.print("💰 Enter Total Bill Amount: ");
            try {
                amount = Double.parseDouble(sc.nextLine().trim());
                if (amount >= 0) {
                    break;
                }
                System.out.println("⚠️ Error: Amount must be a positive number.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error: Invalid decimal format. Please enter a decimal.");
            }
        }

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call GenerateBill(?, ?, ?)}");
        stmt.setInt(1, patientId);
        stmt.setInt(2, appId);
        stmt.setDouble(3, amount);
        stmt.execute();
        stmt.close();
        System.out.println("🎉 Bill generated successfully!");
        Main.logActivity(1, "INSERT", "payments");
    }

    public void calculateTotal() throws Exception {
        Scanner sc = new Scanner(System.in);

        int appId = 0;
        while (true) {
            System.out.print("👉 Enter Appointment ID to calculate total payments: ");
            try {
                appId = Integer.parseInt(sc.nextLine().trim());
                if (appId > 0) {
                    break;
                }
                System.out.println("⚠️ Error: Appointment ID must be a positive integer.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
            }
        }

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{? = call CalculateBillTotal(?)}");
        stmt.registerOutParameter(1, Types.DECIMAL);
        stmt.setInt(2, appId);
        stmt.execute();
        double total = stmt.getDouble(1);
        stmt.close();
        System.out.println("💰 Total calculated bill amount: Rs. " + total);
    }

    public void makePayment() throws Exception {
        Scanner sc = new Scanner(System.in);

        int loggedInPatientId = 0;
        if (Main.loggedInUser instanceof Patient) {
            loggedInPatientId = ((Patient) Main.loggedInUser).getPatientId();
        }

        // Show patient's own bills first
        if (loggedInPatientId > 0) {
            if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
                DBConnection.initialize();
            }
            PreparedStatement ps = DBConnection.conn.prepareStatement("SELECT * FROM payments WHERE patient_id = ?");
            ps.setInt(1, loggedInPatientId);
            ResultSet rs = ps.executeQuery();
            System.out.println("\n💳 --- Your Bills ---");
            System.out.printf("%-10s | %-15s | %-10s | %-12s | %-10s\n", "Bill ID", "Appointment ID", "Amount", "Mode", "Status");
            System.out.println("------------------------------------------------------------------");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-10d | %-15d | Rs. %-8.2f | %-12s | %-10s\n",
                        rs.getInt("payment_id"),
                        rs.getInt("appointment_id"),
                        rs.getDouble("amount"),
                        rs.getString("payment_mode"),
                        rs.getString("status")
                );
            }
            if (!found) {
                System.out.println("📭 No bills found for you.");
            }
            rs.close();
            ps.close();
        }

        int bId = 0;
        while (true) {
            System.out.print("👉 Enter Payment ID (Bill ID) to pay: ");
            try {
                bId = Integer.parseInt(sc.nextLine().trim());
                if (bId <= 0) {
                    System.out.println("⚠️ Error: Payment ID must be a positive integer.");
                    continue;
                }

                // Verify that this bill exists and belongs to the logged-in patient
                if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
                    DBConnection.initialize();
                }
                PreparedStatement ps = DBConnection.conn.prepareStatement("SELECT * FROM payments WHERE payment_id = ?");
                ps.setInt(1, bId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int billPatientId = rs.getInt("patient_id");
                    String status = rs.getString("status");

                    if (loggedInPatientId > 0 && billPatientId != loggedInPatientId) {
                        System.out.println("⚠️ Error: You can only pay your own bills. This bill belongs to Patient ID " + billPatientId + ".");
                        rs.close();
                        ps.close();
                        continue;
                    }
                    if (status.equalsIgnoreCase("Paid")) {
                        System.out.println("⚠️ Error: This bill is already paid!");
                        rs.close();
                        ps.close();
                        continue;
                    }

                    rs.close();
                    ps.close();
                    break; // Valid bill ID found
                } else {
                    System.out.println("⚠️ Error: Bill ID " + bId + " not found. Please try again.");
                    rs.close();
                    ps.close();
                }
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
            }
        }

        String method = "";
        while (true) {
            System.out.print("💳 Enter Payment Method (Cash/Card/UPI): ");
            method = sc.nextLine().trim();
            if (method.equalsIgnoreCase("Cash")) {
                method = "Cash";
                break;
            } else if (method.equalsIgnoreCase("Card")) {
                method = "Card";
                // Prompt and validate Card details
                String cardNo = "";
                while (true) {
                    System.out.print("💳 Enter 16-digit Card Number: ");
                    cardNo = sc.nextLine().trim();
                    if (cardNo.length() == 16 && cardNo.matches("\\d+")) {
                        break;
                    }
                    System.out.println("⚠️ Error: Card number must be exactly 16 digits.");
                }

                String expiry = "";
                while (true) {
                    System.out.print("📅 Enter Expiry Date (MM/YY): ");
                    expiry = sc.nextLine().trim();
                    if (expiry.length() == 5 && expiry.charAt(2) == '/' &&
                            Character.isDigit(expiry.charAt(0)) && Character.isDigit(expiry.charAt(1)) &&
                            Character.isDigit(expiry.charAt(3)) && Character.isDigit(expiry.charAt(4))) {
                        break;
                    }
                    System.out.println("⚠️ Error: Expiry date must be in MM/YY format.");
                }

                String cvv = "";
                while (true) {
                    System.out.print("🔒 Enter 3-digit CVV: ");
                    cvv = sc.nextLine().trim();
                    if (cvv.length() == 3 && cvv.matches("\\d+")) {
                        break;
                    }
                    System.out.println("⚠️ Error: CVV must be exactly 3 digits.");
                }

                System.out.println("⚙️ Processing Card payment...");
                break;
            } else if (method.equalsIgnoreCase("UPI")) {
                method = "UPI";
                // Prompt and validate UPI ID
                String upiId = "";
                while (true) {
                    System.out.print("👉 Enter UPI ID (e.g. name@okhdfc): ");
                    upiId = sc.nextLine().trim();
                    if (upiId.contains("@") && upiId.length() > 3) {
                        break;
                    }
                    System.out.println("⚠️ Error: Invalid UPI ID format. Must contain '@'.");
                }
                System.out.println("⚙️ Processing UPI payment...");
                break;
            } else {
                System.out.println("⚠️ Error: Invalid payment method. Choose Cash, Card, or UPI.");
            }
        }

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call MakePayment(?, ?)}");
        stmt.setInt(1, bId);
        stmt.setString(2, method);
        stmt.execute();
        stmt.close();
        System.out.println("🎉 Payment successful! Payment ID " + bId + " is now Paid.");
    }

    public void downloadBill() {
        if (this.billId == 0) {
            System.out.println("⚠️ No active bill loaded. Please view a bill first.");
            return;
        }
        System.out.println("⚙️ Downloading receipt...");
        System.out.println("📂 Receipt saved to: /Users/manavvora/Downloads/HealthConnect_Bill_" + this.billId + ".txt");
        System.out.println("📥 Download complete!");
    }

    public void viewBill() throws Exception {
        Scanner sc = new Scanner(System.in);

        int loggedInPatientId = 0;
        if (Main.loggedInUser instanceof Patient) {
            loggedInPatientId = ((Patient) Main.loggedInUser).getPatientId();
        }

        // Show patient's own bills first
        if (loggedInPatientId > 0) {
            if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
                DBConnection.initialize();
            }
            PreparedStatement ps = DBConnection.conn.prepareStatement("SELECT * FROM payments WHERE patient_id = ?");
            ps.setInt(1, loggedInPatientId);
            ResultSet rs = ps.executeQuery();
            System.out.println("\n💳 --- Your Bills ---");
            System.out.printf("%-10s | %-15s | %-10s | %-12s | %-10s\n", "Bill ID", "Appointment ID", "Amount", "Mode", "Status");
            System.out.println("------------------------------------------------------------------");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-10d | %-15d | Rs. %-8.2f | %-12s | %-10s\n",
                        rs.getInt("payment_id"),
                        rs.getInt("appointment_id"),
                        rs.getDouble("amount"),
                        rs.getString("payment_mode"),
                        rs.getString("status")
                );
            }
            if (!found) {
                System.out.println("📭 No bills found for you.");
            }
            rs.close();
            ps.close();
        }

        int bId = 0;
        while (true) {
            System.out.print("👉 Enter Payment ID (Bill ID) to view: ");
            try {
                bId = Integer.parseInt(sc.nextLine().trim());
                if (bId <= 0) {
                    System.out.println("⚠️ Error: Payment ID must be a positive integer.");
                    continue;
                }

                if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
                    DBConnection.initialize();
                }
                PreparedStatement ps = DBConnection.conn.prepareStatement("SELECT * FROM payments WHERE payment_id = ?");
                ps.setInt(1, bId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int billPatientId = rs.getInt("patient_id");
                    if (loggedInPatientId > 0 && billPatientId != loggedInPatientId) {
                        System.out.println("⚠️ Error: You can only view your own bills. This bill belongs to Patient ID " + billPatientId + ".");
                        rs.close();
                        ps.close();
                        continue;
                    }

                    this.billId = rs.getInt("payment_id");
                    this.totalAmount = rs.getDouble("amount");
                    this.paymentStatus = rs.getString("status");
                    this.paymentMethod = rs.getString("payment_mode");
                    this.billDate = rs.getString("date");

                    System.out.println("\n💳 --- BILL DETAILS ---");
                    System.out.println("🔑 Bill ID          : " + this.billId);
                    System.out.println("👤 Patient ID       : " + rs.getInt("patient_id"));
                    System.out.println("📅 Appointment ID   : " + rs.getInt("appointment_id"));
                    System.out.println("------------------------------------");
                    System.out.println("💰 Total Amount     : Rs. " + this.totalAmount);
                    System.out.println("🚦 Payment Status   : " + this.paymentStatus);
                    System.out.println("💳 Payment Method   : " + this.paymentMethod);
                    System.out.println("📅 Bill Date        : " + this.billDate);
                    System.out.println("------------------------------------");
                    rs.close();
                    ps.close();
                    break; // Exit loop on successful view
                } else {
                    System.out.println("❌ Error: Bill ID " + bId + " not found. Please try again.");
                    rs.close();
                    ps.close();
                }
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
            }
        }
    }
}