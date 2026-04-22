package game.gamestate;

// Load Graphics
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.image.BufferedImage;

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
    private boolean[][] buildingOccupiedMap;
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
        buildingOccupiedMap = new boolean[gp.maxWorldCol][gp.maxWorldRow];
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

        // New terrain means no placed buildings should remain.
        buildingsMap = new BuildingType[gp.maxWorldCol][gp.maxWorldRow];
        buildingOccupiedMap = new boolean[gp.maxWorldCol][gp.maxWorldRow];
        gp.tileM.setMap(worldMap);
    }

    public void startNewGame() {
        generateWorld();
        gp.cameraWorldX = gp.worldWidth / 2;
        gp.cameraWorldY = gp.worldHeight / 2;
        resetBuildModeState();
    }

    private void resetBuildModeState() {
        showGrid = false;
        toolbar.clearSelection();
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
        
        if (mouseH.consumeLeftClick()) { // handle left click untuk interaksi, seperti klik toolbar atau tempatin bangunan di map
            if (toolbar.isInsideToolbar(mouseH.mouseY, screenHeight)) {
                toolbar.handleClick(mouseH.mouseX, mouseH.mouseY, screenHeight, this);
            } else if (showGrid && toolbar.getSelectedBuilding() != null) {
                int worldX = screenToWorldX(mouseH.mouseX);
                int worldY = screenToWorldY(mouseH.mouseY);
                placeBuilding(worldX, worldY, toolbar.getSelectedBuilding());
            }
        }
        
        toolbar.update(mouseH.mouseX, mouseH.mouseY);
    }
    // Method untuk load world map dari save file, kalau ga ada save file, generate baru
    public void loadWorldMap() { // ngeload world map dari save file, kalau ga ada save file, generate baru
        resetBuildModeState();

        int[][] loadedMap = Player_SaveFile.loadWorldMap();
        BuildingType[][] loadedBuildingsMap = Player_SaveFile.loadBuildingsMap();

        if (loadedMap != null) {
            worldMap = loadedMap;
            gp.tileM.setMap(worldMap);
        } else {
            generateWorld(); // if no save, generate new
        }

        if (loadedBuildingsMap != null
            && loadedBuildingsMap.length == gp.maxWorldCol
            && loadedBuildingsMap[0].length == gp.maxWorldRow) {
            buildingsMap = loadedBuildingsMap;
        } else {
            buildingsMap = new BuildingType[gp.maxWorldCol][gp.maxWorldRow];
        }

        rebuildBuildingOccupancyMap();
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
        
        if (canPlaceBuildingAt(gridX, gridY, building)) {
            buildingsMap[gridX][gridY] = building;
            markBuildingOccupied(gridX, gridY, building, true);
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

    //Method untuk konversi koordinat layar ke koordinat dunia, dipakai untuk menentukan tile mana yang di klik saat mau tempatin bangunan
    private int screenToWorldX(int screenX) {
        return screenX + gp.cameraWorldX - gp.besarLayar / 2;
    }

    // Method untuk konversi koordinat layar ke koordinat dunia, dipakai untuk menentukan tile mana yang di klik saat mau tempatin bangunan
    private int screenToWorldY(int screenY) {
        return screenY + gp.cameraWorldY - gp.tinggiLayar / 2;
    }

    // Method untuk menandai area yang ditempati oleh bangunan sebagai occupied di buildingOccupiedMap, agar tidak bisa ditempati bangunan lain di atasnya
    private void markBuildingOccupied(int anchorX, int anchorY, BuildingType building, boolean occupied) {
        int width = building.getWidth();
        int height = building.getHeight();

        for (int x = anchorX; x < anchorX + width; x++) {
            for (int y = anchorY; y < anchorY + height; y++) {
                if (x >= 0 && x < gp.maxWorldCol && y >= 0 && y < gp.maxWorldRow) {
                    buildingOccupiedMap[x][y] = occupied;
                }
            }
        }
    }

    private void rebuildBuildingOccupancyMap() { // setelah load game, bangunan yang sudah ada di world map perlu di mark sebagai occupied di buildingOccupiedMap agar tidak bisa ditempatin bangunan lain di atasnya
        buildingOccupiedMap = new boolean[gp.maxWorldCol][gp.maxWorldRow];

        for (int x = 0; x < gp.maxWorldCol; x++) {
            for (int y = 0; y < gp.maxWorldRow; y++) {
                BuildingType building = buildingsMap[x][y];
                if (building != null) {
                    markBuildingOccupied(x, y, building, true);
                }
            }
        }
    }

    private boolean canPlaceBuildingAt(int gridX, int gridY, BuildingType building) {
        int width = building.getWidth();
        int height = building.getHeight();

        if (gridX < 0 || gridY < 0 || gridX + width > gp.maxWorldCol || gridY + height > gp.maxWorldRow) {
            return false;
        }

        for (int x = gridX; x < gridX + width; x++) {
            for (int y = gridY; y < gridY + height; y++) {
                if (buildingOccupiedMap[x][y] || worldMap[x][y] == 2) {
                    return false;
                }
            }
        }

        return true;
    }

    private void drawBuildings(Graphics2D g2) { // gambar bangunan di atas terrain, hanya gambar yang berada di layar saja untuk performa
        int startX = (gp.cameraWorldX - gp.besarLayar / 2) / tileSize;
        int startY = (gp.cameraWorldY - gp.tinggiLayar / 2) / tileSize;
        int endX = startX + (gp.besarLayar / tileSize) + 2;
        int endY = startY + (gp.tinggiLayar / tileSize) + 2;

        startX = Math.max(0, startX);
        startY = Math.max(0, startY);
        endX = Math.min(gp.maxWorldCol - 1, endX);
        endY = Math.min(gp.maxWorldRow - 1, endY);

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                BuildingType building = buildingsMap[x][y];
                if (building == null) {
                    continue;
                }

                int worldX = x * tileSize;
                int worldY = y * tileSize;
                int screenX = worldX - gp.cameraWorldX + gp.besarLayar / 2;
                int screenY = worldY - gp.cameraWorldY + gp.tinggiLayar / 2;
                int drawWidth = tileSize * building.getWidth();
                int drawHeight = tileSize * building.getHeight();

                BufferedImage image = toolbar.getBuildingImage(building);
                if (image != null) {
                    g2.drawImage(image, screenX, screenY, drawWidth, drawHeight, null);
                } else {
                    g2.setColor(new Color(190, 150, 90));
                    g2.fillRect(screenX, screenY, drawWidth, drawHeight);
                }
            }
        }
    }

    private void drawBuildingPreview(Graphics2D g2) { // gambar preview bangunan saat player hover dengan building yang dipilih, warna preview berubah jadi merah kalau ga bisa ditempatin di situ
        BuildingType selected = toolbar.getSelectedBuilding();
        if (!showGrid || selected == null || mouseH.mouseX < 0 || mouseH.mouseY < 0) {
            return;
        }

        if (toolbar.isInsideToolbar(mouseH.mouseY, screenHeight)) {
            return;
        }

        int worldX = screenToWorldX(mouseH.mouseX);
        int worldY = screenToWorldY(mouseH.mouseY);
        int gridX = worldX / tileSize;
        int gridY = worldY / tileSize;

        int previewWidth = selected.getWidth();
        int previewHeight = selected.getHeight();

        if (gridX < 0 || gridY < 0 || gridX + previewWidth > gp.maxWorldCol || gridY + previewHeight > gp.maxWorldRow) {
            return;
        }

        int snappedWorldX = gridX * tileSize;
        int snappedWorldY = gridY * tileSize;
        int screenX = snappedWorldX - gp.cameraWorldX + gp.besarLayar / 2;
        int screenY = snappedWorldY - gp.cameraWorldY + gp.tinggiLayar / 2;
        int drawWidth = tileSize * previewWidth;
        int drawHeight = tileSize * previewHeight;

        boolean canPlace = canPlaceBuildingAt(gridX, gridY, selected);
        BufferedImage image = toolbar.getBuildingImage(selected);

        if (image != null) {
            g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, canPlace ? 0.6f : 0.35f));
            g2.drawImage(image, screenX, screenY, drawWidth, drawHeight, null);
            g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, 1f));
        } else {
            g2.setColor(canPlace ? new Color(120, 255, 120, 130) : new Color(255, 80, 80, 130));
            g2.fillRect(screenX, screenY, drawWidth, drawHeight);
        }

        g2.setColor(canPlace ? new Color(90, 255, 90, 220) : new Color(255, 80, 80, 220));
        g2.drawRect(screenX, screenY, drawWidth, drawHeight);
    }

    public void drawGameplay(Graphics2D g2){
        gp.tileM.draw(g2); // gambar tile berdasarkan worldMap

        drawBuildings(g2); // gambar bangunan di atas terrain

        drawGrid(g2); // gambar grid di atas tile

        drawBuildingPreview(g2); // gambar preview saat mau menaruh bangunan

        toolbar.draw(g2, screenWidth, screenHeight, mouseH.mouseX, mouseH.mouseY, this);
        // Player and money removed - just 2D camera view
    }

    public void saveGame() { // save game
        Player_SaveFile.saveWorldMap(worldMap);
        Player_SaveFile.saveBuildingsMap(buildingsMap);
    }
}