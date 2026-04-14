package game.gamestate;

// Load Graphics
import java.awt.Graphics2D;

import game.Toolbar;
import game.BuildingType;
// load system
import system.KeyHandler;
import system.GamePanel;
import system.MouseHandler;
import system.PerlinNoise;
import system.Player_SaveFile;

public class Gameplay {

    KeyHandler keyH;
    // Player removed

    public int tileSize = 50;
    public int screenWidth;
    public int screenHeight;
    private GamePanel gp;
    private MouseHandler mouseH;

    private boolean isDragging = false;
    private int dragStartMouseX, dragStartMouseY;
    private int dragStartCameraX, dragStartCameraY;

    public int[][] worldMap;
    public BuildingType[][] buildingsMap; // Layer for buildings on top of terrain
    private boolean showGrid = false;
    Toolbar toolbar = new Toolbar();

    public Gameplay(KeyHandler keyH, MouseHandler mouseH, GamePanel gp) { //init sebelum run game
        this.keyH = keyH;
        this.mouseH = mouseH;
        this.gp = gp;
        this.tileSize = gp.tileSize; // Use the same tileSize as GamePanel for proper alignment
        this.screenWidth = gp.besarLayar;
        this.screenHeight = gp.tinggiLayar;
        worldMap = new int[gp.maxWorldCol][gp.maxWorldRow];
        buildingsMap = new BuildingType[gp.maxWorldCol][gp.maxWorldRow];
    }

    public void generateWorld() {
        PerlinNoise noise = new PerlinNoise(System.currentTimeMillis()); // seed with current time for randomness
        for (int x = 0; x < gp.maxWorldCol; x++) {
            for (int y = 0; y < gp.maxWorldRow; y++) {
                double value = noise.octaveNoise(x * 0.05, y * 0.05, 4, 0.5); // larger scale for bigger features
                // Threshold for water: only very low values become water for tiny lakes
                if (value < -0.15) { // adjust threshold to make water rarer
                    worldMap[x][y] = 2; // water
                } else {
                    worldMap[x][y] = 0; // grass
                }
            }
        }
        gp.tileM.setMap(worldMap);
    }
 
    public void updateGameplay(){ // Update untuk Logic gameplay, seperti input, movement, dll
        // update player camera based dari dragging mouse
        if (mouseH.leftPressed) {
            if (!isDragging) { // jika baru mulai drag, simpan posisi awal mouse dan kamera
                isDragging = true;
                dragStartMouseX = mouseH.mouseX;
                dragStartMouseY = mouseH.mouseY;
                dragStartCameraX = gp.cameraWorldX;
                dragStartCameraY = gp.cameraWorldY;
            } else {
                // update posisi kamera berdasarkan seberapa jauh mouse sudah digeser dari posisi awal
                int deltaX = mouseH.mouseX - dragStartMouseX;
                int deltaY = mouseH.mouseY - dragStartMouseY;
                gp.cameraWorldX = dragStartCameraX - deltaX;
                gp.cameraWorldY = dragStartCameraY - deltaY;

                // batasin kamera agar tidak keluar dari world bounds
                if (gp.cameraWorldX < gp.besarLayar / 2) gp.cameraWorldX = gp.besarLayar / 2;
                if (gp.cameraWorldY < gp.tinggiLayar / 2) gp.cameraWorldY = gp.tinggiLayar / 2;
                if (gp.cameraWorldX > gp.worldWidth - gp.besarLayar / 2) gp.cameraWorldX = gp.worldWidth - gp.besarLayar / 2;
                if (gp.cameraWorldY > gp.worldHeight - gp.tinggiLayar / 2) gp.cameraWorldY = gp.worldHeight - gp.tinggiLayar / 2;
            }
        } else {
            isDragging = false;
        }
        
        // Handle toolbar clicks
        if (mouseH.consumeLeftClick()) {
            toolbar.handleClick(mouseH.mouseX, mouseH.mouseY, screenHeight, this);
        }
        
        toolbar.update(mouseH.mouseX, mouseH.mouseY, mouseH.leftPressed);
    }
    // Method untuk load world map dari save file, kalau ga ada save file, generate baru
    public void loadWorldMap() { // ngeload world map dari save file, kalau ga ada save file, generate baru
        int[][] loadedMap = Player_SaveFile.loadWorldMap();
        if (loadedMap != null) {
            worldMap = loadedMap;
            gp.tileM.setMap(worldMap);
        } else {
            generateWorld(); // if no save, generate new
        }
    }

    public void toggleGrid() {
        showGrid = !showGrid;
    }

    public boolean isGridVisible() {
        return showGrid;
    }
 // Method untuk menempatkan bangunan di koordinat tertentu, dipanggil saat player klik di map dengan building yang dipilih
 // belum bekerja sepenuhnya
    public void placeBuilding(int worldX, int worldY, BuildingType building) {
        if (building == null) return;
        
        // Convert world coordinates to grid coordinates
        int gridX = worldX / tileSize;
        int gridY = worldY / tileSize;
        
        // Check bounds
        if (gridX >= 0 && gridX < gp.maxWorldCol && gridY >= 0 && gridY < gp.maxWorldRow) {
            buildingsMap[gridX][gridY] = building;
        }
    }

    // Method untuk mendapatkan jenis bangunan di koordinat tertentu, bisa dipakai untuk interaksi atau info tooltip
    public BuildingType getBuilding(int worldX, int worldY) {
        int gridX = worldX / tileSize;
        int gridY = worldY / tileSize;
        
        if (gridX >= 0 && gridX < gp.maxWorldCol && gridY >= 0 && gridY < gp.maxWorldRow) {
            return buildingsMap[gridX][gridY];
        }
        return null;
    }

    private void drawGrid(Graphics2D g2) { // membuat grid overlay untuk bantu player dalam menempatkan bangunan, bisa di toggle on/off
        if (!showGrid) return;

        // Calculate which tiles are visible on screen
        int startX = (gp.cameraWorldX - gp.besarLayar / 2) / tileSize;
        int startY = (gp.cameraWorldY - gp.tinggiLayar / 2) / tileSize;
        int endX = startX + (gp.besarLayar / tileSize) + 2;
        int endY = startY + (gp.tinggiLayar / tileSize) + 2;

        // Clamp to world bounds
        startX = Math.max(0, startX);
        startY = Math.max(0, startY);
        endX = Math.min(gp.maxWorldCol, endX);
        endY = Math.min(gp.maxWorldRow, endY);

        g2.setColor(new java.awt.Color(255, 255, 255, 100)); // semi-transparent white
        g2.setStroke(new java.awt.BasicStroke(1));

        // Draw vertical lines
        for (int x = startX; x <= endX; x++) {
            int screenX = x * tileSize - (gp.cameraWorldX - gp.besarLayar / 2);
            g2.drawLine(screenX, 0, screenX, gp.tinggiLayar);
        }

        // Draw horizontal lines
        for (int y = startY; y <= endY; y++) {
            int screenY = y * tileSize - (gp.cameraWorldY - gp.tinggiLayar / 2);
            g2.drawLine(0, screenY, gp.besarLayar, screenY);
        }
    }

    public void drawGameplay(Graphics2D g2){
        gp.tileM.draw(g2); // gambar tile berdasarkan worldMap

        drawGrid(g2); // gambar grid di atas tile

        toolbar.draw(g2, screenWidth, screenHeight, mouseH.mouseX, mouseH.mouseY, this);
        // Player and money removed - just 2D camera view
    }

    public void saveGame() { // save game
        Player_SaveFile.saveWorldMap(worldMap);
    }
}