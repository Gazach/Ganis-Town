package system;

import java.sql.*;

public class Player_SaveFile {
    private static final String DB_URL = "jdbc:sqlite:player_save.db";

    public static void createDatabase() { // Membuat database dan tabel jika belum ada
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute(
                "CREATE TABLE IF NOT EXISTS player_save (" +
                "id INTEGER PRIMARY KEY," +
                "money INTEGER)"
            );

            stmt.execute(
                "INSERT OR IGNORE INTO player_save (id, money) " +
                "VALUES (1, 100)"
            );

        } catch (SQLException e) {
            System.out.println("Error creating database: " + e.getMessage());
        }
    }

    public static int loadPlayerData() { // load data player dari database
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT money FROM player_save WHERE id = 1")) {

            if (rs.next()) {
                return rs.getInt("money");
            }

        } catch (SQLException e) {
            System.out.println("Error loading player data: " + e.getMessage());
        }

        return 100; // fallback default
    }

    public static void savePlayerData(int money) { // menyimpan data player ke database
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                 "INSERT OR REPLACE INTO player_save (id, money) " +
                 "VALUES (1, ?)")) {

            pstmt.setInt(1, money);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error saving player data: " + e.getMessage());
        }
    }
}