import java.sql.*;
import java.util.*;

public class DietPlan {
    String disease = "";
    String breakfast = "";
    String lunch = "";
    String snacks = "";
    String dinner = "";
    String water = "";
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

        System.out.println("\n🔬 Select Diagnosis / Disease:");
        System.out.println("1. Diabetes 🩸");
        System.out.println("2. Dengue 🦟");
        System.out.println("3. Hypertension / High BP 🩺");
        System.out.println("4. Kidney Disease 🫘");
        System.out.println("5. Pregnancy 🤰");
        System.out.println("6. Other (Specify) 📝");

        String dis = "";
        while (true) {
            System.out.print("👉 Enter choice (1-6): ");
            String input = sc.nextLine().trim();
            if (input.equals("1") || input.equalsIgnoreCase("Diabetes")) {
                dis = "Diabetes";
                break;
            } else if (input.equals("2") || input.equalsIgnoreCase("Dengue")) {
                dis = "Dengue";
                break;
            } else if (input.equals("3") || input.equalsIgnoreCase("Hypertension") || input.equalsIgnoreCase("High BP") || input.equalsIgnoreCase("BP")) {
                dis = "Hypertension";
                break;
            } else if (input.equals("4") || input.equalsIgnoreCase("Kidney Disease") || input.equalsIgnoreCase("Kidney")) {
                dis = "Kidney Disease";
                break;
            } else if (input.equals("5") || input.equalsIgnoreCase("Pregnancy")) {
                dis = "Pregnancy";
                break;
            } else if (input.equals("6") || input.equalsIgnoreCase("Other")) {
                System.out.print("👉 Enter custom Diagnosis / Disease name: ");
                dis = sc.nextLine().trim();
                if (dis.isEmpty()) {
                    dis = "General Health";
                }
                break;
            } else {
                System.out.println("⚠️ Invalid choice. Please enter a number between 1 and 6.");
            }
        }
        this.disease = dis;

        boolean isGujarati = false;
        while (true) {
            System.out.println("\n🤔 Would you like a Gujarati Diet Plan suggestion?");
            System.out.println("1. Yes 🍲 (Gujarati Diet Customization)");
            System.out.println("2. No 🥗 (Standard / General Diet Plan)");
            System.out.print("👉 Enter choice (1 for Yes, 2 for No): ");
            String gujChoice = sc.nextLine().trim().toLowerCase();
            if (gujChoice.equals("1") || gujChoice.equals("yes") || gujChoice.equals("y")) {
                isGujarati = true;
                break;
            } else if (gujChoice.equals("2") || gujChoice.equals("no") || gujChoice.equals("n")) {
                isGujarati = false;
                break;
            } else {
                System.out.println("⚠️ Invalid choice. Please enter 1 (Yes) or 2 (No).");
            }
        }

        if (isGujarati) {
            System.out.println("\nSelected: Gujarati Diet Customization (\"શું જમવું?\") 🍲");
            this.breakfast = "Thepla (less oil) & Milk ☕";
            this.lunch = "Dal, Rice, Roti, Shak (Green vegetables) & Salad 🍛";
            this.dinner = "Khichdi & Kadhi/Curd 🥣";

            String disLower = dis.toLowerCase();
            if (disLower.contains("diabet")) {
                this.snacks = "Roasted Chana / Masala Khakhra (no sugar) & Green Tea 🍵";
                this.water = "3.0L daily (Hydrate well) 💧";
                this.avoidFoods = "Sweets 🍬, Sugary tea ☕, Mango 🥭, White rice (restrict quantity) 🍚";
            } else if (disLower.contains("dengue")) {
                this.snacks = "Kiwi slices / Papaya & Coconut Water 🥥🥝";
                this.water = "3.5L daily (High fluids crucial) 💧";
                this.avoidFoods = "Spicy 🌶️, Oily foods. Recommended: Coconut water 🥥, Kiwi 🥝, Pomegranate 🍎, Papaya leaf extract";
            } else if (disLower.contains("bp") || disLower.contains("hypertension")) {
                this.snacks = "Unsalted Roasted Chana / Makhana & Chaas (low salt) 🥛";
                this.water = "2.5L - 3.0L daily 💧";
                this.avoidFoods = "High salt 🧂, pickles, papad, salty farsan 🍟";
            } else if (disLower.contains("kidney")) {
                this.snacks = "Apple slices / Low-sodium puffed rice (Mamra) 🍏";
                this.water = "1.5L daily (Fluid intake restricted) 💧";
                this.avoidFoods = "High salt 🧂, High potassium foods (like bananas) 🍌, pickles, papad";
            } else if (disLower.contains("pregnan")) {
                this.snacks = "Almonds, Walnut / Sukha Meva & Milk 🥛🥜";
                this.water = "3.0L daily 💧";
                this.avoidFoods = "Raw eggs 🥚, unpasteurized dairy 🥛, excess caffeine ☕, papaya 🥭";
            } else {
                this.snacks = "Khakhra / Roasted Chana & Herbal Tea ☕";
                this.water = "2.5L - 3.0L daily 💧";
                this.avoidFoods = "Deep-fried farsan 🍟, extra butter/ghee 🧈";
            }
        } else {
            // General diet recommendation
            String disLower = dis.toLowerCase();
            if (disLower.contains("diabet")) {
                this.breakfast = "Oats porridge / Ragi idli 🥣";
                this.lunch = "Whole wheat roti, leafy vegetable, boiled lentils, cucumber salad 🥗";
                this.dinner = "Barley soup / Grilled chicken or Paneer with sautéed veggies 🍲";
                this.snacks = "Roasted Makhana / Sugar-free Green Tea ☕🌰";
                this.water = "3.0L daily 💧";
                this.avoidFoods = "Sweets 🍬, soft drinks 🥤, white bread 🍞, processed juices 🧃";
            } else if (disLower.contains("dengue")) {
                this.breakfast = "Fresh fruit salad (kiwi, papaya) & coconut water 🥝🥥";
                this.lunch = "Moong dal khichdi & warm vegetable soup 🥣";
                this.dinner = "Rice porridge & boiled carrots 🥕";
                this.snacks = "Fresh Kiwi / Pomegranate / Coconut Water 🥥🥝";
                this.water = "3.5L daily (High fluid intake) 💧";
                this.avoidFoods = "Spicy curries 🌶️, oily street food 🍔, red meat 🥩";
            } else if (disLower.contains("bp") || disLower.contains("hypertension")) {
                this.breakfast = "Whole grain oats, fresh fruits, low-fat milk 🥣";
                this.lunch = "Brown rice, whole wheat roti, steamed vegetables, low-sodium dal 🥗";
                this.dinner = "Vegetable soup, baked paneer / tofu 🍲";
                this.snacks = "Unsalted nuts / Fruit salad / Hibiscus tea 🍎🍵";
                this.water = "2.5L - 3.0L daily 💧";
                this.avoidFoods = "High salt 🧂, pickles, papad, high-sodium canned/junk food 🍟";
            } else if (disLower.contains("kidney")) {
                this.breakfast = "Low-sodium bread & egg whites 🍳";
                this.lunch = "White rice, boiled cabbage, cauliflower 🍚";
                this.dinner = "Renal-approved dietary plan (as prescribed) 🍽️";
                this.snacks = "Apple slices / Berries / Low-sodium crackers 🍏";
                this.water = "1.5L daily (Restricted fluid as prescribed) 💧";
                this.avoidFoods = "Bananas 🍌, tomatoes 🍅, potatoes 🥔, high-sodium foods 🧂";
            } else if (disLower.contains("pregnan")) {
                this.breakfast = "Milk, almonds, iron-fortified cereals / sprouts 🥛🥜";
                this.lunch = "Spinach paneer, whole wheat roti, curd, chickpeas 🍛";
                this.dinner = "Vegetable pulao, grilled chicken / tofu, bean salad 🥗";
                this.snacks = "Walnuts, almonds, fruit smoothie 🥛🍎";
                this.water = "3.0L daily 💧";
                this.avoidFoods = "Unpasteurized dairy 🥛, raw eggs 🥚, excessive caffeine ☕, papaya 🥭";
            } else {
                this.breakfast = "Poha / Upma 🥣";
                this.lunch = "Home-cooked roti, dal, subji, buttermilk 🥛";
                this.dinner = "Light khichdi, vegetable soup 🥣";
                this.snacks = "Mixed nuts / Fruit slice / Green Tea ☕🍎";
                this.water = "2.5L - 3.0L daily 💧";
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

                // Update snacks, water, and instructions directly in table
                PreparedStatement ps2 = DBConnection.conn.prepareStatement("UPDATE diet_plan SET snacks = ?, water = ?, instructions = ? WHERE diet_id = ?");
                ps2.setString(1, this.snacks);
                ps2.setString(2, this.water);
                ps2.setString(3, "Avoid: " + this.avoidFoods);
                ps2.setInt(4, dietId);
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

    public void displayDietPlanForPatient(int patientId) throws Exception {
        if (DBConnection.conn == null || DBConnection.conn.isClosed()) {
            DBConnection.initialize();
        }

        // Use LinkedHashSet for deduplication of diseases (e.g. if BP appears 2 times, show 1 time only)
        Set<String> uniqueDiseases = new LinkedHashSet<>();

        PreparedStatement psMed = DBConnection.conn.prepareStatement("SELECT disease FROM medical_history WHERE patient_id = ?");
        psMed.setInt(1, patientId);
        ResultSet rsMed = psMed.executeQuery();
        while (rsMed.next()) {
            String dis = rsMed.getString("disease");
            if (dis != null && !dis.trim().isEmpty()) {
                uniqueDiseases.add(dis.trim());
            }
        }
        rsMed.close();
        psMed.close();

        System.out.println("\n🥗 --- CUSTOMIZED DIET PLAN BY DISEASE ---");

        if (uniqueDiseases.isEmpty()) {
            // Check assigned diet plans if no specific medical history
            CallableStatement stmt = DBConnection.conn.prepareCall("{call ViewDietPlan(?)}");
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("🍳 Breakfast   : " + rs.getString("breakfast"));
                System.out.println("🍲 Lunch       : " + rs.getString("lunch"));
                String snk = rs.getString("snacks");
                System.out.println("🍿 Snacks      : " + (snk != null && !snk.isEmpty() ? snk : "Mixed nuts & Green Tea 🍵"));
                System.out.println("🍛 Dinner      : " + rs.getString("dinner"));
                String wtr = rs.getString("water");
                System.out.println("💧 Water Intake: " + (wtr != null && !wtr.isEmpty() ? wtr : "2.5L - 3.0L daily 💧"));
                System.out.println("📝 Instructions: " + rs.getString("instructions"));
                System.out.println("--------------------------------------------------");
            } else {
                System.out.println("📭 No specific disease history or diet plan assigned to this patient.");
            }
            rs.close();
            stmt.close();
            return;
        }

        // Iterate through deduplicated diseases (each disease shown 1 time only)
        for (String dis : uniqueDiseases) {
            String disLower = dis.toLowerCase();
            String bFast = "";
            String lun = "";
            String snack = "";
            String din = "";
            String wat = "";
            String avoid = "";

            if (disLower.contains("bp") || disLower.contains("hypertension") || disLower.contains("blood pressure")) {
                bFast = "Whole grain oats, fresh fruits, low-fat milk 🥣";
                lun = "Brown rice, steamed vegetables, low-sodium lentils (dal) 🥗";
                snack = "Unsalted nuts / Fruit salad / Hibiscus tea 🍎🍵";
                din = "Vegetable soup, baked paneer / tofu 🍲";
                wat = "2.5L - 3.0L daily 💧";
                avoid = "High salt 🧂, pickles, papad, high-sodium canned/junk food 🍟";
            } else if (disLower.contains("diabet")) {
                bFast = "Oats porridge / Ragi idli 🥣";
                lun = "Whole wheat roti, leafy vegetables, boiled lentils, cucumber salad 🥗";
                snack = "Roasted Makhana / Sugar-free Green Tea ☕🌰";
                din = "Barley soup / Sautéed veggies 🍲";
                wat = "3.0L daily 💧";
                avoid = "Sweets 🍬, sugary tea ☕, soft drinks 🥤, white bread 🍞, mango 🥭";
            } else if (disLower.contains("dengue")) {
                bFast = "Fresh fruit salad (kiwi, papaya) & coconut water 🥝🥥";
                lun = "Moong dal khichdi & warm vegetable soup 🥣";
                snack = "Fresh Kiwi / Pomegranate / Coconut Water 🥥🥝";
                din = "Rice porridge & boiled carrots 🥕";
                wat = "3.5L daily (High fluid intake) 💧";
                avoid = "Spicy curries 🌶️, oily street food 🍔";
            } else if (disLower.contains("kidney")) {
                bFast = "Low-sodium bread & egg whites 🍳";
                lun = "White rice, boiled cabbage, cauliflower 🍚";
                snack = "Apple slices / Berries / Low-sodium crackers 🍏";
                din = "Renal-approved dietary plan 🍽️";
                wat = "1.5L daily (Restricted fluid as prescribed) 💧";
                avoid = "Bananas 🍌, tomatoes 🍅, potatoes 🥔, high-sodium/potassium foods 🧂";
            } else if (disLower.contains("pregnan")) {
                bFast = "Milk, almonds, iron-fortified cereals / sprouts 🥛";
                lun = "Spinach paneer, whole wheat roti, curd, chickpeas 🍛";
                snack = "Walnuts, almonds, fruit smoothie 🥛🍎";
                din = "Vegetable pulao, bean salad 🥗";
                wat = "3.0L daily 💧";
                avoid = "Raw eggs 🥚, unpasteurized dairy 🥛, excess caffeine ☕, papaya 🥭";
            } else {
                bFast = "Poha / Upma 🥣";
                lun = "Home-cooked roti, dal, subji, buttermilk 🥛";
                snack = "Mixed nuts / Fruit slice / Green Tea ☕🍎";
                din = "Light khichdi, vegetable soup 🥣";
                wat = "2.5L - 3.0L daily 💧";
                avoid = "Junk food 🍕, carbonated drinks 🥤";
            }

            System.out.println("🦠 Disease      : " + dis);
            System.out.println("🍳 Breakfast    : " + bFast);
            System.out.println("🍲 Lunch        : " + lun);
            System.out.println("🍿 Snacks       : " + snack);
            System.out.println("🍛 Dinner       : " + din);
            System.out.println("💧 Water Intake : " + wat);
            System.out.println("🚫 Avoid / Notes: " + avoid);
            System.out.println("--------------------------------------------------");
        }
    }

}