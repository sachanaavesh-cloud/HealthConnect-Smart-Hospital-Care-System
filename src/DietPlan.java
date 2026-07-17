import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class DietPlan {
    String disease = "";
    String breakfast = "";
    String lunch = "";
    String dinner = "";
    String avoidFoods = "";

    public void generateDiet() throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n🥗 --- Smart Diet Planner ---");

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

        System.out.print("🔬 Enter Diagnosis/Disease (Diabetes/Dengue/Kidney Disease/Pregnancy/Other): ");
        String dis = sc.nextLine().trim();
        this.disease = dis;

        System.out.print("🤔 Would you like a Gujarati Diet Plan suggestion? (yes/no): ");
        String gujChoice = sc.nextLine().trim().toLowerCase();

        if (gujChoice.equals("yes") || gujChoice.equals("y")) {
            System.out.println("\nSelected: Gujarati Diet Customization (\"શું જમવું?\") 🍲");
            this.breakfast = "Thepla (less oil) & Milk ☕";
            this.lunch = "Dal, Rice, Roti, Shak (Green vegetables) & Salad 🍛";
            this.dinner = "Khichdi & Kadhi/Curd 🥣";
            if (dis.equalsIgnoreCase("Diabetes")) {
                this.avoidFoods = "Sweets 🍬, Sugary tea ☕, Mango 🥭, White rice (restrict quantity) 🍚";
            } else if (dis.equalsIgnoreCase("Dengue")) {
                this.avoidFoods = "Spicy 🌶️, Oily foods. Recommended: Coconut water 🥥, Kiwi 🥝, Pomegranate 🍎, Papaya leaf extract";
            } else if (dis.equalsIgnoreCase("Kidney Disease")) {
                this.avoidFoods = "High salt 🧂, High potassium foods (like bananas) 🍌, pickles, papad";
            } else {
                this.avoidFoods = "Deep-fried farsan 🍟, extra butter/ghee 🧈";
            }
        } else {
            // General diet recommendation
            if (dis.equalsIgnoreCase("Diabetes")) {
                this.breakfast = "Oats porridge / Ragi idli 🥣";
                this.lunch = "Whole wheat roti, leafy vegetable, boiled lentils, cucumber salad 🥗";
                this.dinner = "Barley soup / Grilled chicken or Paneer with sautéed veggies 🍲";
                this.avoidFoods = "Sweets 🍬, soft drinks 🥤, white bread 🍞, processed juices 🧃";
            } else if (dis.equalsIgnoreCase("Dengue")) {
                this.breakfast = "Fresh fruit salad (kiwi, papaya) & coconut water 🥝🥥";
                this.lunch = "Moong dal khichdi & warm vegetable soup 🥣";
                this.dinner = "Rice porridge & boiled carrots 🥕";
                this.avoidFoods = "Spicy curries 🌶️, oily street food 🍔, red meat 🥩";
            } else if (dis.equalsIgnoreCase("Kidney Disease")) {
                this.breakfast = "Low-sodium bread & egg whites 🍳";
                this.lunch = "White rice, boiled cabbage, cauliflower 🍚";
                this.dinner = "Renal-approved dietary plan (as prescribed) 🍽️";
                this.avoidFoods = "Bananas 🍌, tomatoes 🍅, potatoes 🥔, high-sodium foods 🧂";
            } else if (dis.equalsIgnoreCase("Pregnancy")) {
                this.breakfast = "Milk, almonds, iron-fortified cereals / sprouts 🥛🥜";
                this.lunch = "Spinach paneer, whole wheat roti, curd, chickpeas 🍛";
                this.dinner = "Vegetable pulao, grilled chicken / tofu, bean salad 🥗";
                this.avoidFoods = "Unpasteurized dairy 🥛, raw eggs 🥚, excessive caffeine ☕, papaya 🥭";
            } else {
                this.breakfast = "Poha / Upma 🥣";
                this.lunch = "Home-cooked roti, dal, subji, buttermilk 🥛";
                this.dinner = "Light khichdi, vegetable soup 🥣";
                this.avoidFoods = "Junk food 🍕, carbonated drinks 🥤";
            }
        }

        int doctorId = 1;
        if (Main.loggedInUser instanceof Doctor) {
            doctorId = ((Doctor) Main.loggedInUser).getDoctorId();
        }
        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        DBConnection.conn.setAutoCommit(false);
        try {
            // Call CreateDietPlan(IN p_pid INT, IN p_did INT, IN p_break VARCHAR(255), IN p_lunch VARCHAR(255))
            CallableStatement stmt = DBConnection.conn.prepareCall("{call CreateDietPlan(?, ?, ?, ?)}");
            stmt.setInt(1, patientId);
            stmt.setInt(2, doctorId);
            stmt.setString(3, this.breakfast);
            stmt.setString(4, this.lunch);
            stmt.execute();
            stmt.close();

            // Retrieve the diet_id
            int dietId = 0;
            PreparedStatement ps = DBConnection.conn.prepareStatement("SELECT diet_id FROM diet_plan WHERE patient_id = ? ORDER BY diet_id DESC LIMIT 1");
            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                dietId = rs.getInt("diet_id");
            }
            rs.close();
            ps.close();

            if (dietId > 0) {
                // Update dinner via UpdateDietPlan procedure
                CallableStatement stmt2 = DBConnection.conn.prepareCall("{call UpdateDietPlan(?, ?)}");
                stmt2.setInt(1, dietId);
                stmt2.setString(2, this.dinner);
                stmt2.execute();
                stmt2.close();

                // Update instructions directly in table
                PreparedStatement ps2 = DBConnection.conn.prepareStatement("UPDATE diet_plan SET instructions = ? WHERE diet_id = ?");
                ps2.setString(1, "Avoid: " + this.avoidFoods);
                ps2.setInt(2, dietId);
                ps2.executeUpdate();
                ps2.close();
            }

            DBConnection.conn.commit();
            System.out.println("🎉 Diet plan generated, saved to DB, and shared with the patient!");
            Main.logActivity(1, "INSERT", "diet_plan");
        } catch (Exception ex) {
            DBConnection.conn.rollback();
            throw ex;
        } finally {
            DBConnection.conn.setAutoCommit(true);
        }
    }

    public void viewDiet() throws Exception {
        Scanner sc = new Scanner(System.in);

        int patientId = 0;
        if (Main.loggedInUser instanceof Patient) {
            patientId = ((Patient) Main.loggedInUser).getPatientId();
        } else {
            while (true) {
                System.out.print("👉 Enter Patient ID to view diet plan: ");
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

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call ViewDietPlan(?)}");
        stmt.setInt(1, patientId);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            System.out.println("\n🥗 --- ASSIGNED DIET PLAN ---");
            System.out.println("🍳 Breakfast   : " + rs.getString("breakfast"));
            System.out.println("🍲 Lunch       : " + rs.getString("lunch"));
            System.out.println("🍛 Dinner      : " + rs.getString("dinner"));
            System.out.println("📝 Instructions: " + rs.getString("instructions"));
            System.out.println("==========================");
        } else {
            System.out.println("📭 No diet plan assigned to this patient.");
        }
        rs.close();
        stmt.close();
    }

    public void updateDiet() throws Exception {
        Scanner sc = new Scanner(System.in);

        int dietId = 0;
        while (true) {
            System.out.print("👉 Enter Diet ID to update: ");
            try {
                dietId = Integer.parseInt(sc.nextLine().trim());
                if (dietId > 0) {
                    break;
                }
                System.out.println("⚠️ Error: Diet ID must be a positive integer.");
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Error: Invalid number format. Please enter an integer.");
            }
        }

        System.out.print("🍛 Enter New Dinner: ");
        String din = sc.nextLine();

        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }
        CallableStatement stmt = DBConnection.conn.prepareCall("{call UpdateDietPlan(?, ?)}");
        stmt.setInt(1, dietId);
        stmt.setString(2, din);
        stmt.execute();
        stmt.close();
        System.out.println("✅ Diet plan dinner updated successfully.");
        Main.logActivity(1, "UPDATE", "diet_plan");
    }
}