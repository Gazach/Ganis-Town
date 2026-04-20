package system;

import java.sql.*;
import game.BuildingType;

public class Player_SaveFile {
    private static final String DB_URL = "jdbc:sqlite:player_save.db";

    public static void createDatabase() { // Membuat database dan tabel jika belum ada
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute( //buat tabel untuk menyimpan data player seperti uang, level, dll. Saat ini hanya menyimpan uang saja
                "CREATE TABLE IF NOT EXISTS player_save (" +
                "id INTEGER PRIMARY KEY," +
                "money INTEGER)"
            );

            stmt.execute( // tabel untuk menyimpan data world map dalam format string yang bisa di-parse kembali saat load
                "CREATE TABLE IF NOT EXISTS world_map (" +
                "id INTEGER PRIMARY KEY," +
                "map_data TEXT)"
            );

            stmt.execute( // tabel untuk menyimpan data bangunan dalam format string yang bisa di-parse kembali saat load
                "CREATE TABLE IF NOT EXISTS building_map (" +
                "id INTEGER PRIMARY KEY," +
                "map_data TEXT)"
            );

            stmt.execute( // save world map data ke database
                "INSERT OR IGNORE INTO world_map (id, map_data) " +
                "VALUES (1, '')"
            );

            stmt.execute( // save building data ke database
                "INSERT OR IGNORE INTO building_map (id, map_data) " +
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

    public static void saveBuildingsMap(BuildingType[][] buildingsMap) { //menyimpan data bangunan ke database dengan format string yang bisa di-parse kembali saat load
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < buildingsMap[0].length; y++) {
            for (int x = 0; x < buildingsMap.length; x++) {
                BuildingType building = buildingsMap[x][y];
                sb.append(building == null ? -1 : building.ordinal());
                if (x < buildingsMap.length - 1) sb.append(",");
            }
            if (y < buildingsMap[0].length - 1) sb.append(";");
        }
        String mapData = sb.toString();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                 "INSERT OR REPLACE INTO building_map (id, map_data) " +
                 "VALUES (1, ?)")) {

            pstmt.setString(1, mapData);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error saving building map: " + e.getMessage());
        }
    }

    public static BuildingType[][] loadBuildingsMap() { //load data bangunan dari database dan parsing kembali ke format BuildingType[][]
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT map_data FROM building_map WHERE id = 1")) {

            if (rs.next()) {
                String mapData = rs.getString("map_data");
                if (mapData == null || mapData.isEmpty()) return null;

                String[] rows = mapData.split(";");
                int width = rows[0].split(",").length;
                int height = rows.length;
                BuildingType[][] buildingsMap = new BuildingType[width][height];

                for (int y = 0; y < rows.length; y++) {
                    String[] cols = rows[y].split(",");
                    for (int x = 0; x < cols.length; x++) {
                        int buildingOrdinal = Integer.parseInt(cols[x]);
                        if (buildingOrdinal >= 0 && buildingOrdinal < BuildingType.values().length) {
                            buildingsMap[x][y] = BuildingType.values()[buildingOrdinal];
                        } else {
                            buildingsMap[x][y] = null;
                        }
                    }
                }
                return buildingsMap;
            }

        } catch (SQLException e) {
            System.out.println("Error loading building map: " + e.getMessage());
        }

        return null;
    }
}