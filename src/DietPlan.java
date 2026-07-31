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

        System.out.print("🔬 Enter Diagnosis/Disease (Diabetes/Dengue/Kidney Disease/Pregnancy/Other): ");
        String dis = sc.nextLine().trim();
        this.disease = dis;

        System.out.print("🤔 Would you like a Gujarati Diet Plan suggestion? (yes/no): ");
        String gujChoice = sc.nextLine().trim().toLowerCase();

        if (gujChoice.equals("yes") || gujChoice.equals("y")) {
            System.out.println("\nSelected: Gujarati Diet Customization (\"શું જમવું?\") 🍲");
            if (dis.equalsIgnoreCase("Diabetes")) {
                this.breakfast = "Thepla (less oil) & Sugar-free Milk ☕";
                this.lunch = "Dal, Bajra Roti, Bhindi/Lauki Shak & Cucumber Salad 🍛";
                this.snacks = "Roasted Makhana / Roasted Chana & Green Tea 🍵";
                this.dinner = "Moong Dal Khichdi & Plain Kadhi 🥣";
                this.water = "3.0 Liters / day (Warm water) 💧";
                this.avoidFoods = "Sweets 🍬, Sugary tea ☕, Mango 🥭, White rice (restrict quantity) 🍚";
            } else if (dis.equalsIgnoreCase("Dengue")) {
                this.breakfast = "Soft Poha & Fresh Papaya / Kiwi 🥝";
                this.lunch = "Soft Rice, Light Toor Dal & Boiled Bottle Gourd 🍚";
                this.snacks = "Fresh Coconut Water 🥥 & Pomegranate Juice 🍎";
                this.dinner = "Light Moong Khichdi & Warm Vegetable Soup 🥣";
                this.water = "4.0 Liters / day (Coconut water & ORS fluids) 💧";
                this.avoidFoods = "Spicy 🌶️, Oily foods. Recommended: Coconut water 🥥, Kiwi 🥝, Pomegranate 🍎";
            } else if (dis.equalsIgnoreCase("Kidney Disease")) {
                this.breakfast = "Plain Poha (low salt) 🥣";
                this.lunch = "White Rice, Light Yellow Dal & Boiled Turai 🍚";
                this.snacks = "Apple slice 🍎 / Puffed Rice (Kurmura, no salt) 🍿";
                this.dinner = "Soft Rice & Steamed Gourd 🥣";
                this.water = "1.5 Liters / day (Strict Fluid Restriction) 💧";
                this.avoidFoods = "High salt 🧂, High potassium foods (bananas, papad, pickles)";
            } else if (dis.equalsIgnoreCase("Pregnancy")) {
                this.breakfast = "Multigrain Thepla, Boiled Sprouts & Milk 🥛";
                this.lunch = "Roti, Palak Shak, Dal, Rice & Fresh Curd 🍛";
                this.snacks = "Dry fruit laddu (Gond/Sukhdi), Almonds & Milk 🥛";
                this.dinner = "Khichdi, Kadhi & Sautéed Veggies 🥣";
                this.water = "3.5 Liters / day 💧";
                this.avoidFoods = "Unpasteurized dairy, raw eggs, excessive caffeine, raw papaya";
            } else {
                this.breakfast = "Thepla (less oil) & Milk ☕";
                this.lunch = "Dal, Rice, Roti, Shak (Green vegetables) & Salad 🍛";
                this.snacks = "Khakhra / Roasted Chana & Tea ☕";
                this.dinner = "Khichdi & Kadhi/Curd 🥣";
                this.water = "3.0 Liters / day 💧";
                this.avoidFoods = "Deep-fried farsan 🍟, extra butter/ghee 🧈";
            }
        } else {
            // General diet recommendation
            if (dis.equalsIgnoreCase("Diabetes")) {
                this.breakfast = "Oats porridge / Ragi idli 🥣";
                this.lunch = "Whole wheat roti, leafy vegetable, boiled lentils, cucumber salad 🥗";
                this.snacks = "Handful of Roasted Almonds/Walnuts & Green Tea 🍵";
                this.dinner = "Barley soup / Grilled Paneer with sautéed veggies 🍲";
                this.water = "3.0 Liters / day 💧";
                this.avoidFoods = "Sweets 🍬, soft drinks 🥤, white bread 🍞, processed juices 🧃";
            } else if (dis.equalsIgnoreCase("Dengue")) {
                this.breakfast = "Fresh fruit salad (kiwi, papaya) & coconut water 🥝🥥";
                this.lunch = "Moong dal khichdi & warm vegetable soup 🥣";
                this.snacks = "Fresh Coconut Water 🥥 & Herbal Tea ☕";
                this.dinner = "Rice porridge & boiled carrots 🥕";
                this.water = "4.0 Liters / day (Hydration focus) 💧";
                this.avoidFoods = "Spicy curries 🌶️, oily street food 🍔, red meat 🥩";
            } else if (dis.equalsIgnoreCase("Kidney Disease")) {
                this.breakfast = "Low-sodium bread & egg whites 🍳";
                this.lunch = "White rice, boiled cabbage, cauliflower 🍚";
                this.snacks = "Low-potassium Apple / Berries 🍎";
                this.dinner = "Renal-approved dietary plan (as prescribed) 🍽️";
                this.water = "1.5 Liters / day (Fluid restricted) 💧";
                this.avoidFoods = "Bananas 🍌, tomatoes 🍅, potatoes 🥔, high-sodium foods 🧂";
            } else if (dis.equalsIgnoreCase("Pregnancy")) {
                this.breakfast = "Milk, almonds, iron-fortified cereals / sprouts 🥛🥜";
                this.lunch = "Spinach paneer, whole wheat roti, curd, chickpeas 🍛";
                this.snacks = "Fruit smoothie / Nuts & Seeds 🥛";
                this.dinner = "Vegetable pulao, grilled chicken / tofu, bean salad 🥗";
                this.water = "3.5 Liters / day 💧";
                this.avoidFoods = "Unpasteurized dairy 🥛, raw eggs 🥚, excessive caffeine ☕";
            } else {
                this.breakfast = "Poha / Upma 🥣";
                this.lunch = "Home-cooked roti, dal, subji, buttermilk 🥛";
                this.snacks = "Sprouts salad / Fresh Fruit bowl 🍎";
                this.dinner = "Light khichdi, vegetable soup 🥣";
                this.water = "3.0 Liters / day 💧";
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
                ps2.setString(3, "🔬 Target Disease/Condition: " + this.disease + " | Avoid: " + this.avoidFoods);
                ps2.setInt(4, dietId);
                ps2.executeUpdate();
                ps2.close();
            }

            DBConnection.conn.commit();
            System.out.println("🎉 Diet plan generated for " + this.disease + ", saved to DB, and shared with the patient!");
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
        boolean found = false;
        java.util.HashSet<String> displayedDiseases = new java.util.HashSet<>();

        while (rs.next()) {
            String inst = rs.getString("instructions");
            String breakfast = rs.getString("breakfast");

            String diseaseAssigned = getCleanDiseaseForDiet(patientId, inst, breakfast, DBConnection.conn);

            // Deduplicate: If diet for this disease is already displayed, skip duplicates
            if (displayedDiseases.contains(diseaseAssigned.toLowerCase())) {
                continue;
            }
            displayedDiseases.add(diseaseAssigned.toLowerCase());
            found = true;

            String cleanInstructions = (inst != null) ? inst : "-";
            if (cleanInstructions.contains(" | Avoid:")) {
                cleanInstructions = cleanInstructions.substring(cleanInstructions.indexOf(" | Avoid:") + 3).trim();
            } else if (cleanInstructions.contains("Avoid:")) {
                cleanInstructions = cleanInstructions.substring(cleanInstructions.indexOf("Avoid:")).trim();
            }

            System.out.println("\n🥗 --- ASSIGNED DIET PLAN DETAILS ---");
            System.out.println("🔬 Assigned For Disease: " + diseaseAssigned);
            System.out.println("🍳 Breakfast            : " + rs.getString("breakfast"));
            System.out.println("🍲 Lunch                : " + rs.getString("lunch"));
            System.out.println("🍿 Evening Snacks       : " + (rs.getString("snacks") != null ? rs.getString("snacks") : "-"));
            System.out.println("🍛 Dinner               : " + rs.getString("dinner"));
            System.out.println("💧 Daily Water          : " + (rs.getString("water") != null ? rs.getString("water") : "-"));
            System.out.println("📝 Instructions         : " + cleanInstructions);
            System.out.println("==================================================================");
        }
        if (!found) {
            System.out.println("📭 No diet plan assigned to this patient.");
        }
        rs.close();
        stmt.close();
    }

    private static String getCleanDiseaseForDiet(int patientId, String inst, String breakfast, java.sql.Connection conn) {
        if (inst != null && inst.contains("Target Disease/Condition:")) {
            int start = inst.indexOf("Target Disease/Condition:") + "Target Disease/Condition:".length();
            int end = inst.indexOf(" | ", start);
            String tag = (end != -1) ? inst.substring(start, end).trim() : inst.substring(start).trim();
            if (!tag.isEmpty() && !tag.equalsIgnoreCase("General Health")) {
                return normalizeDiseaseName(tag);
            }
        } else if (inst != null && inst.contains("Target Condition:")) {
            int start = inst.indexOf("Target Condition:") + "Target Condition:".length();
            int end = inst.indexOf(" | ", start);
            String tag = (end != -1) ? inst.substring(start, end).trim() : inst.substring(start).trim();
            if (!tag.isEmpty() && !tag.equalsIgnoreCase("General Health")) {
                return normalizeDiseaseName(tag);
            }
        }

        String combined = ((inst != null ? inst : "") + " " + (breakfast != null ? breakfast : "")).toLowerCase();
        if (combined.contains("salt") || combined.contains("bp") || combined.contains("hypertension") || combined.contains("pressure") || combined.contains("low-sodium")) {
            return "High BP";
        }
        if (combined.contains("diabet") || combined.contains("sweet") || combined.contains("sugar") || combined.contains("thepla") || combined.contains("ragi")) {
            return "Diabetes";
        }
        if (combined.contains("dengue") || combined.contains("papaya") || combined.contains("kiwi") || combined.contains("coconut")) {
            return "Dengue";
        }
        if (combined.contains("kidney") || combined.contains("potassium") || combined.contains("renal")) {
            return "Kidney Disease";
        }
        if (combined.contains("pregnan") || combined.contains("sprout") || combined.contains("folic")) {
            return "Pregnancy";
        }

        try {
            PreparedStatement psHist = conn.prepareStatement(
                    "SELECT disease FROM medical_history WHERE patient_id = ? AND disease IS NOT NULL AND TRIM(disease) != '' ORDER BY history_id DESC LIMIT 1");
            psHist.setInt(1, patientId);
            ResultSet rsHist = psHist.executeQuery();
            if (rsHist.next()) {
                String d = rsHist.getString("disease").trim();
                rsHist.close();
                psHist.close();
                if (!d.isEmpty()) {
                    return normalizeDiseaseName(d);
                }
            } else {
                rsHist.close();
                psHist.close();
            }
        } catch (Exception e) {}

        return "General Health";
    }

    private static String normalizeDiseaseName(String raw) {
        String lower = raw.toLowerCase();
        if (lower.contains("bp") || lower.contains("hypertension") || lower.contains("pressure")) {
            return "High BP";
        }
        if (lower.contains("diabet") || lower.contains("sugar")) {
            return "Diabetes";
        }
        if (lower.contains("dengue")) {
            return "Dengue";
        }
        if (lower.contains("kidney")) {
            return "Kidney Disease";
        }
        if (lower.contains("pregnan")) {
            return "Pregnancy";
        }
        return raw;
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