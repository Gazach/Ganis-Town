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
                "CREATE TABLE IF NOT EXISTS world_map (" +
                "id INTEGER PRIMARY KEY," +
                "map_data TEXT)"
            );

            stmt.execute(
                "INSERT OR IGNORE INTO world_map (id, map_data) " +
                "VALUES (1, '')"
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

    public static void saveWorldMap(int[][] worldMap) {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < worldMap[0].length; y++) {
            for (int x = 0; x < worldMap.length; x++) {
                sb.append(worldMap[x][y]);
                if (x < worldMap.length - 1) sb.append(",");
            }
            if (y < worldMap[0].length - 1) sb.append(";");
        }
        String mapData = sb.toString();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                 "INSERT OR REPLACE INTO world_map (id, map_data) " +
                 "VALUES (1, ?)")) {

            pstmt.setString(1, mapData);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error saving world map: " + e.getMessage());
        }
    }

    public static int[][] loadWorldMap() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT map_data FROM world_map WHERE id = 1")) {

            if (rs.next()) {
                String mapData = rs.getString("map_data");
                if (mapData == null || mapData.isEmpty()) return null;
                String[] rows = mapData.split(";");
                int[][] worldMap = new int[100][100]; // assuming 100x100
                for (int y = 0; y < rows.length; y++) {
                    String[] cols = rows[y].split(",");
                    for (int x = 0; x < cols.length; x++) {
                        worldMap[x][y] = Integer.parseInt(cols[x]);
                    }
                }
                return worldMap;
            }

        } catch (SQLException e) {
            System.out.println("Error loading world map: " + e.getMessage());
        }

        return null;
    }
}