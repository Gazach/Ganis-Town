package game.gamestate;

// Load Graphics
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.BasicStroke;
import java.awt.Stroke;
import java.awt.RadialGradientPaint;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import game.Toolbar;
import game.BuildingType;
import game.BuildingInstance;
import game.dayCycle;
import game.worldTemperature;
// load system
import system.KeyHandler;
import system.GamePanel;
import system.MouseHandler;
import system.PerlinNoise;
import system.Player_SaveFile;
import system.panel;

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
    public BuildingInstance[][] buildingDataMap;
    private boolean[][] buildingOccupiedMap;
    private int maxBuildingWidthTiles = 1;
    private int maxBuildingHeightTiles = 1;
    private boolean showGrid = false;
    private BuildingInstance selectedBuildingInfo;
    private boolean isEditingBuildingTitle = false;
    private String buildingTitleDraft = "";
    private panel buildingInfoPanelSkin;
    private panel hudPanelSkin;
    private Font buildingInfoTitleFont;
    private Font buildingInfoBodyFont;
    Toolbar toolbar = new Toolbar();
    private int playerMoney = 2500;
    private long lastIncomeTime = System.currentTimeMillis();
    private BufferedImage coinImage;
    private BufferedImage personImage;
    private dayCycle dayTime = new dayCycle();
    private worldTemperature temperature = new worldTemperature();

    // cached values to avoid per-frame recalculation
    private double cachedTotalIncome = 0.0;
    private int cachedTotalPopulation = 0;
    private double incomeAccumulator = 0.0;
    private static final Random rng = new Random();
    private int nextPlacementOrder = 0;
    private BufferedImage buildingGlowImage;
    private Font hudFont;
    private Font detailTitleFontCached;
    private Font detailBodyFontCached;

    // konstanta untuk layout panel info detail bangunan, untuk memudahkan penyesuaian tampilan
    private static final int BUILDING_INFO_PANEL_TOP_BOTTOM_MARGIN = 40;
    private static final int BUILDING_INFO_PANEL_RIGHT_MARGIN = 0;
    private static final int BUILDING_INFO_PANEL_WIDTH = 250;
    private static final int BUILDING_TITLE_MAX_LENGTH = 100;
    private static final int BUILDING_TITLE_INPUT_X_PADDING = 10;
    private static final int BUILDING_TITLE_INPUT_Y = 8;
    private static final int BUILDING_TITLE_INPUT_HEIGHT = 30;
    private static final int BUILDING_TITLE_INPUT_OFFSET_Y = 10;

    // Method untuk load font khusus untuk UI detail bangunan, kalau gagal load font, pakai font default
    public Gameplay(KeyHandler keyH, MouseHandler mouseH, GamePanel gp) { //init sebelum run game
        this.keyH = keyH;
        this.mouseH = mouseH;
        this.gp = gp;
        this.tileSize = gp.tileSize; // Use the same tileSize as GamePanel for proper alignment
        this.screenWidth = gp.besarLayar;
        this.screenHeight = gp.tinggiLayar;
        worldMap = new int[gp.maxWorldCol][gp.maxWorldRow];
        buildingsMap = new BuildingType[gp.maxWorldCol][gp.maxWorldRow];
        buildingDataMap = new BuildingInstance[gp.maxWorldCol][gp.maxWorldRow];
        buildingOccupiedMap = new boolean[gp.maxWorldCol][gp.maxWorldRow];
        cacheMaxBuildingDimensions();
        buildingInfoPanelSkin = new panel();
        hudPanelSkin = new panel("/asset/Panel/butmore");
        loadBuildingInfoFonts();
        loadCoinImage();
        loadPersonImage();
        buildGlowImage();
    }

    private void cacheMaxBuildingDimensions() {
        int maxWidth = 1;
        int maxHeight = 1;
        for (BuildingType type : BuildingType.values()) {
            maxWidth = Math.max(maxWidth, type.getWidth());
            maxHeight = Math.max(maxHeight, type.getHeight());
        }
        maxBuildingWidthTiles = maxWidth;
        maxBuildingHeightTiles = maxHeight;
    }

    private void loadCoinImage() {
        try {
            InputStream stream = getClass().getResourceAsStream("/asset/gameplayUI/coin.png");
            if (stream != null) {
                coinImage = ImageIO.read(stream);
                return;
            }
            File file = new File("asset/gameplayUI/coin.png");
            if (file.exists()) {
                coinImage = ImageIO.read(file);
            }
        } catch (IOException e) {
            System.out.println("Failed to load coin image: " + e.getMessage());
        }
    }

    private void loadPersonImage() {
        try {
            InputStream stream = getClass().getResourceAsStream("/asset/gameplayUI/person_icon.png");
            if (stream != null) {
                personImage = ImageIO.read(stream);
                return;
            }
            File file = new File("asset/gameplayUI/person_icon.png");
            if (file.exists()) {
                personImage = ImageIO.read(file);
            }
        } catch (IOException e) {
            System.out.println("Failed to load person image: " + e.getMessage());
        }
    }

    // Pre-render a single 128x128 glow sprite used for all buildings at runtime
    private void buildGlowImage() {
        int size = 128;
        buildingGlowImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D bg = buildingGlowImage.createGraphics();
        bg.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        float half = size / 2f;
        RadialGradientPaint p = new RadialGradientPaint(
            new java.awt.geom.Point2D.Float(half, half), half,
            new float[]{0f, 1f},
            new Color[]{new Color(255, 200, 80, 255), new Color(255, 160, 30, 0)}
        );
        bg.setPaint(p);
        bg.fillOval(0, 0, size, size);
        bg.dispose();
    }

    // Method untuk generate world map baru dengan Perlin noise, dipanggil saat start new game atau load game tanpa save file
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
        buildingDataMap = new BuildingInstance[gp.maxWorldCol][gp.maxWorldRow];
        buildingOccupiedMap = new boolean[gp.maxWorldCol][gp.maxWorldRow];
        gp.tileM.setMap(worldMap);
    }

    // Method untuk memulai game baru, dengan generate world baru dan reset state terkait build mode
    public void startNewGame() {
        generateWorld();
        gp.cameraWorldX = gp.worldWidth / 2;
        gp.cameraWorldY = gp.worldHeight / 2;
        playerMoney = 22500;
        lastIncomeTime = System.currentTimeMillis();
        dayTime.setHour(6.0f);
        temperature = new worldTemperature();
        cachedTotalIncome = 0.0;
        cachedTotalPopulation = 0;
        incomeAccumulator = 0.0;
        resetBuildModeState();
    }

    // Method untuk reset state terkait build mode, dipanggil saat start new game atau load game, untuk memastikan state build mode bersih dan konsisten
    private void resetBuildModeState() {
        finishTitleEditingAndSave();
        showGrid = false;
        toolbar.clearSelection();
        selectedBuildingInfo = null;
    }
 
    public void updateGameplay(){ // Update untuk Logic gameplay, seperti input, movement, dll
        handleBuildingTitleEditingInput();

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
                finishTitleEditingAndSave();
                toolbar.handleClick(mouseH.mouseX, mouseH.mouseY, screenHeight, this);
            } else if (isInsideBuildingDetailPanel(mouseH.mouseX, mouseH.mouseY)) {
                if (selectedBuildingInfo != null && isInsideBuildingTitleEditArea(mouseH.mouseX, mouseH.mouseY)) {
                    startTitleEditing();
                } else {
                    finishTitleEditingAndSave();
                }
            } else if (showGrid && toolbar.getSelectedBuilding() != null) {
                finishTitleEditingAndSave();
                int worldX = screenToWorldX(mouseH.mouseX);
                int worldY = screenToWorldY(mouseH.mouseY);
                placeBuilding(worldX, worldY, toolbar.getSelectedBuilding());
            } else {
                int worldX = screenToWorldX(mouseH.mouseX);
                int worldY = screenToWorldY(mouseH.mouseY);
                BuildingInstance nextSelection = findBuildingAtWorld(worldX, worldY);
                if (nextSelection != selectedBuildingInfo) {
                    finishTitleEditingAndSave();
                }
                selectedBuildingInfo = nextSelection;
            }
        }
        
        toolbar.update(mouseH.mouseX, mouseH.mouseY);
        dayTime.update();
        temperature.update(dayTime.getHour());
        tickBuildingIncome();
    }

    // Setiap detik, kumpulkan income dari semua bangunan yang sudah ditempatkan
    private void tickBuildingIncome() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastIncomeTime;
        if (elapsed < 1000) return;

        int seconds = (int) (elapsed / 1000);
        lastIncomeTime += seconds * 1000L;

        if (cachedTotalIncome > 0) {
            incomeAccumulator += cachedTotalIncome * seconds;
            int wholeEarned = (int) incomeAccumulator;
            if (wholeEarned > 0) {
                playerMoney += wholeEarned;
                incomeAccumulator -= wholeEarned;
            }
        }
    }
    // Method untuk load world map dari save file, kalau ga ada save file, generate baru
    public void loadWorldMap() { // ngeload world map dari save file, kalau ga ada save file, generate baru
        resetBuildModeState();

        int[][] loadedMap = Player_SaveFile.loadWorldMap();
        BuildingInstance[][] loadedBuildingsMap = Player_SaveFile.loadBuildingsMap();

        if (loadedMap != null) {
            worldMap = loadedMap;
            gp.tileM.setMap(worldMap);
        } else {
            generateWorld(); // if no save, generate new
        }

        playerMoney = Player_SaveFile.loadPlayerData();
        float savedHour = Player_SaveFile.loadDayTime();
        dayTime.setHour(savedHour);
        temperature.load(
            Player_SaveFile.loadTemperatureHigh(),
            Player_SaveFile.loadTemperatureLow(),
            savedHour
        );
        lastIncomeTime = System.currentTimeMillis();

        if (loadedBuildingsMap != null
            && loadedBuildingsMap.length == gp.maxWorldCol
            && loadedBuildingsMap[0].length == gp.maxWorldRow) {
            buildingDataMap = loadedBuildingsMap;
            normalizeBuildingNames();
            rebuildBuildingTypeMap();
        } else {
            buildingsMap = new BuildingType[gp.maxWorldCol][gp.maxWorldRow];
            buildingDataMap = new BuildingInstance[gp.maxWorldCol][gp.maxWorldRow];
        }

        rebuildBuildingOccupancyMap();
        // Compute nextPlacementOrder from loaded buildings (fallback assigns scan-order index)
        int maxOrder = -1;
        int fallbackIdx = 0;
        for (int x = 0; x < gp.maxWorldCol; x++) {
            for (int y = 0; y < gp.maxWorldRow; y++) {
                BuildingInstance inst = buildingDataMap[x][y];
                if (inst != null) {
                    if (inst.getPlacementOrder() < 0) {
                        inst.setPlacementOrder(fallbackIdx);
                    }
                    fallbackIdx++;
                    maxOrder = Math.max(maxOrder, inst.getPlacementOrder());
                }
            }
        }
        nextPlacementOrder = maxOrder + 1;
        recalcIncomeCache();
    }

    // Method untuk save world map ke save file, dipanggil saat player save game atau auto-save
    public void toggleGrid() {
        showGrid = !showGrid;
    }
    // Method untuk cek apakah grid sedang ditampilkan, dipakai untuk menentukan apakah akan gambar grid overlay dan preview bangunan saat hover
    public boolean isGridVisible() {
        return showGrid;
    }
 // Method untuk menempatkan bangunan di koordinat tertentu, dipanggil saat player klik di map dengan building yang dipilih
 // belum bekerja sepenuhnya
    public void placeBuilding(int worldX, int worldY, BuildingType building) {
        if (building == null) return;

        if (playerMoney < building.getPrice()) return; // not enough money

        // Convert world coordinates to grid coordinates
        int gridX = worldX / tileSize;
        int gridY = worldY / tileSize;

        if (canPlaceBuildingAt(gridX, gridY, building)) {
            BuildingInstance instance = createBuildingInstance(building);
            if (building.getCategory() == BuildingType.BuildingCategory.HOUSING) {
                int min = building.getMinPeople();
                int max = building.getMaxPeople();
                int pop = (min == max) ? min : min + rng.nextInt(max - min + 1);
                instance.setPopulation(pop);
            }
            instance.setPlacementOrder(nextPlacementOrder++);
            buildingsMap[gridX][gridY] = building;
            buildingDataMap[gridX][gridY] = instance;
            markBuildingOccupied(gridX, gridY, building, true);
            playerMoney -= building.getPrice();
            recalcIncomeCache();
            saveGame();
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

    public String getBuildingName(int worldX, int worldY) {
        int gridX = worldX / tileSize;
        int gridY = worldY / tileSize;

        if (gridX >= 0 && gridX < gp.maxWorldCol && gridY >= 0 && gridY < gp.maxWorldRow) {
            BuildingInstance instance = buildingDataMap[gridX][gridY];
            return instance == null ? null : instance.getName();
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
    // Method untuk mencari apakah ada bangunan di koordinat dunia tertentu, dipakai untuk interaksi klik bangunan dan info detail
    private void rebuildBuildingOccupancyMap() { 

        for (int x = 0; x < gp.maxWorldCol; x++) {
            for (int y = 0; y < gp.maxWorldRow; y++) {
                BuildingType building = buildingsMap[x][y];
                if (building != null) {
                    markBuildingOccupied(x, y, building, true);
                }
            }
        }
    }

    // Method untuk mencari apakah ada bangunan di koordinat dunia tertentu, dipakai untuk interaksi klik bangunan dan info detail
    private void rebuildBuildingTypeMap() {
        buildingsMap = new BuildingType[gp.maxWorldCol][gp.maxWorldRow];

        for (int x = 0; x < gp.maxWorldCol; x++) {
            for (int y = 0; y < gp.maxWorldRow; y++) {
                BuildingInstance instance = buildingDataMap[x][y];
                buildingsMap[x][y] = (instance == null) ? null : instance.getType();
            }
        }
    }

    // Method untuk mencari apakah ada bangunan di koordinat dunia tertentu, dipakai untuk interaksi klik bangunan dan info detail
    private BuildingInstance createBuildingInstance(BuildingType type) {
        String baseName = getBaseBuildingName(type);
        String candidate = baseName;
        int index = 0;

        while (isBuildingNameUsed(candidate)) {
            index++;
            candidate = baseName + " (" + index + ")";
        }

        return new BuildingInstance(type, candidate);
    }

    // Method untuk mencari apakah ada bangunan di koordinat dunia tertentu, dipakai untuk interaksi klik bangunan dan info detail
    private String getBaseBuildingName(BuildingType type) {
        return type.name().toLowerCase().replace('_', ' ');
    }

    // Method untuk mencari apakah ada bangunan di koordinat dunia tertentu, dipakai untuk interaksi klik bangunan dan info detail
    private boolean isBuildingNameUsed(String name) {
        for (int x = 0; x < gp.maxWorldCol; x++) {
            for (int y = 0; y < gp.maxWorldRow; y++) {
                BuildingInstance instance = buildingDataMap[x][y];
                if (instance != null && instance.getName().equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Method untuk mencari apakah ada bangunan di koordinat dunia tertentu, dipakai untuk interaksi klik bangunan dan info detail
    private void normalizeBuildingNames() {
        Set<String> usedNames = new HashSet<>();

        for (int x = 0; x < gp.maxWorldCol; x++) {
            for (int y = 0; y < gp.maxWorldRow; y++) {
                BuildingInstance instance = buildingDataMap[x][y];
                if (instance == null) {
                    continue;
                }

                String baseName = getBaseBuildingName(instance.getType());
                String candidate = instance.getName();

                if (candidate == null || candidate.isEmpty() || usedNames.contains(candidate)) {
                    candidate = baseName;
                    int index = 0;
                    while (usedNames.contains(candidate)) {
                        index++;
                        candidate = baseName + " (" + index + ")";
                    }
                }

                buildingDataMap[x][y] = new BuildingInstance(instance.getType(), candidate, instance.getPopulation());
                usedNames.add(candidate);
            }
        }
    }
    // Method untuk cek apakah sebuah bangunan bisa ditempatkan di koordinat grid tertentu, dengan ngecek apakah area yang dibutuhkan bangunan kosong dan tidak ada water tile
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

    // gambar bangunan di atas terrain, hanya gambar yang berada di layar saja untuk performance
    private void drawBuildings(Graphics2D g2) { 
        int startX = (gp.cameraWorldX - gp.besarLayar / 2) / tileSize;
        int startY = (gp.cameraWorldY - gp.tinggiLayar / 2) / tileSize;
        int endX = startX + (gp.besarLayar / tileSize) + 2;
        int endY = startY + (gp.tinggiLayar / tileSize) + 2;

        // Expand start bounds so multi-tile buildings remain visible until fully out of view.
        startX -= (maxBuildingWidthTiles - 1);
        startY -= (maxBuildingHeightTiles - 1);

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

    /** Draws warm radial glows for all on-screen buildings after the night overlay. */
    private void drawBuildingGlows(Graphics2D g2) {
        float darkness = dayTime.getDarkness();
        if (darkness <= 0f) return;

        int startX = (gp.cameraWorldX - gp.besarLayar / 2) / tileSize;
        int startY = (gp.cameraWorldY - gp.tinggiLayar / 2) / tileSize;
        int endX = startX + (gp.besarLayar / tileSize) + 2;
        int endY = startY + (gp.tinggiLayar / tileSize) + 2;

        // Keep glow rendering in sync with building visibility culling.
        startX -= (maxBuildingWidthTiles - 1);
        startY -= (maxBuildingHeightTiles - 1);

        startX = Math.max(0, startX);
        startY = Math.max(0, startY);
        endX = Math.min(gp.maxWorldCol - 1, endX);
        endY = Math.min(gp.maxWorldRow - 1, endY);

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                BuildingType building = buildingsMap[x][y];
                if (building == null) continue;

                int screenX = x * tileSize - gp.cameraWorldX + gp.besarLayar / 2;
                int screenY = y * tileSize - gp.cameraWorldY + gp.tinggiLayar / 2;
                int drawWidth = tileSize * building.getWidth();
                int drawHeight = tileSize * building.getHeight();

                drawBuildingGlow(g2, screenX, screenY, drawWidth, drawHeight, darkness);

                // Redraw the building sprite on top of its own glow so it stays crisp
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

    /** Draws a warm radial glow around a building using a pre-rendered image (cheap drawImage instead of RadialGradientPaint). */
    private void drawBuildingGlow(Graphics2D g2, int screenX, int screenY, int drawWidth, int drawHeight, float darkness) {
        if (buildingGlowImage == null) return;
        float radius = Math.max(drawWidth, drawHeight) * 0.96f;
        int glowDiam = (int)(radius * 2);
        int glowX = screenX + drawWidth  / 2 - (int) radius;
        int glowY = screenY + drawHeight / 2 - (int) radius;
        float alpha = Math.min(darkness * (120f / 255f), 1f);
        java.awt.Composite old = g2.getComposite();
        g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alpha));
        g2.drawImage(buildingGlowImage, glowX, glowY, glowDiam, glowDiam, null);
        g2.setComposite(old);
    }

    // gambar preview bangunan saat player hover dengan building yang dipilih, warna preview berubah jadi merah kalau ga bisa ditempatin di situ
    private void drawBuildingPreview(Graphics2D g2) { 
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

    // Method untuk menggambar UI detail bangunan yang sedang dipilih, muncul di sebelah kanan layar saat ada bangunan yang dipilih, menampilkan nama, tipe, ukuran, dan opsi untuk edit nama
    private void drawBuildingDetailUI(Graphics2D g2) {
        if (selectedBuildingInfo == null) {
            return;
        }

        int panelWidth = BUILDING_INFO_PANEL_WIDTH;
        int panelHeight = screenHeight - (BUILDING_INFO_PANEL_TOP_BOTTOM_MARGIN * 2);
        int panelX = getBuildingDetailPanelX();
        int panelY = BUILDING_INFO_PANEL_TOP_BOTTOM_MARGIN;

        buildingInfoPanelSkin.draw(g2, panelX, panelY, panelWidth, panelHeight);

        int titleWidth = BUILDING_INFO_PANEL_WIDTH - (BUILDING_TITLE_INPUT_X_PADDING * 2) - 10;
        int titleX = panelX + (BUILDING_INFO_PANEL_WIDTH - titleWidth) / 2;
        int titleY = panelY + BUILDING_TITLE_INPUT_Y;

        // Background dan border untuk area judul, juga menampilkan nama bangunan dan kursor saat edit
        g2.setColor(new Color(24, 24, 24, 77));
        g2.fillRect(titleX, titleY + BUILDING_TITLE_INPUT_OFFSET_Y, titleWidth, BUILDING_TITLE_INPUT_HEIGHT);
        Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(3f));
        g2.setColor(new Color(219, 227, 222));
        g2.drawRect(titleX, titleY + BUILDING_TITLE_INPUT_OFFSET_Y, titleWidth, BUILDING_TITLE_INPUT_HEIGHT);
        g2.setStroke(oldStroke);

        g2.setColor(Color.WHITE);
        g2.setFont(detailTitleFontCached != null ? detailTitleFontCached : new Font("Dialog", Font.BOLD, 21));
        int textX = titleX + 6;
        int titleTextY = titleY + BUILDING_TITLE_INPUT_OFFSET_Y + 21;
        String titleText = isEditingBuildingTitle ? buildingTitleDraft : selectedBuildingInfo.getName();

        // Clip text rendering to input box bounds so overflow stays hidden
        java.awt.Shape oldClip = g2.getClip();
        g2.setClip(titleX + 2, titleY + BUILDING_TITLE_INPUT_OFFSET_Y + 2, titleWidth - 4, BUILDING_TITLE_INPUT_HEIGHT - 4);

        int availableTextWidth = titleWidth - 12;
        int textWidth = g2.getFontMetrics().stringWidth(titleText);
        int drawTextX = textX;
        if (textWidth > availableTextWidth) {
            drawTextX = textX + availableTextWidth - textWidth;
        }

        drawStrokedText(g2, titleText, drawTextX, titleTextY, Color.WHITE, new Color(8, 8, 8, 220), 1.5f);

        if (isEditingBuildingTitle) {
            int cursorX = drawTextX + g2.getFontMetrics().stringWidth(titleText);
            drawStrokedText(g2, "_", cursorX, titleTextY, Color.WHITE, new Color(8, 8, 8, 220), 1.5f);
        }

        g2.setClip(oldClip);

        g2.setFont(detailBodyFontCached != null ? detailBodyFontCached : new Font("Dialog", Font.BOLD, 17));
        int bodyY = titleY + BUILDING_TITLE_INPUT_OFFSET_Y + BUILDING_TITLE_INPUT_HEIGHT + 28;
        drawStrokedText(
            g2,
            "Type: " + selectedBuildingInfo.getType().name().toLowerCase().replace('_', ' '),
            textX,
            bodyY,
            Color.WHITE,
            new Color(8, 8, 8, 220),
            1.5f
        );
        drawStrokedText(
            g2,
            "Size: " + selectedBuildingInfo.getType().getWidth() + "x" + selectedBuildingInfo.getType().getHeight(),
            textX,
            bodyY + 18,
            Color.WHITE,
            new Color(8, 8, 8, 220),
            1.5f
        );

        BuildingType.BuildingCategory cat = selectedBuildingInfo.getType().getCategory();
        if (cat == BuildingType.BuildingCategory.HOUSING) {
            drawStrokedText(
                g2,
                "Population: " + selectedBuildingInfo.getPopulation(),
                textX,
                bodyY + 36,
                new Color(80, 180, 255),
                new Color(8, 8, 8, 220),
                1.5f
            );
        } else {
            BuildingType btype = selectedBuildingInfo.getType();
            int maxWorkers = btype.getMaxWorkers();
            int maxIncome  = btype.getIncomePerSecond();

            // Collect and sort production buildings by placement order (same as recalcIncomeCache)
            List<BuildingInstance> prodList = new ArrayList<>();
            for (int x = 0; x < gp.maxWorldCol; x++) {
                for (int y = 0; y < gp.maxWorldRow; y++) {
                    BuildingInstance inst = buildingDataMap[x][y];
                    if (inst != null && inst.getType().getCategory() == BuildingType.BuildingCategory.PRODUCTION) {
                        prodList.add(inst);
                    }
                }
            }
            prodList.sort((a, b) -> Integer.compare(a.getPlacementOrder(), b.getPlacementOrder()));

            int remainingPop = cachedTotalPopulation;
            int effectiveWorkers = 0;
            for (BuildingInstance inst : prodList) {
                int workers = Math.min(remainingPop, inst.getType().getMaxWorkers());
                remainingPop -= workers;
                if (inst == selectedBuildingInfo) {
                    effectiveWorkers = workers;
                    break;
                }
            }

            double localRatio = maxWorkers > 0 ? (double) effectiveWorkers / maxWorkers : 0.0;
            double effectiveIncome = Math.floor(localRatio * maxIncome * 10.0) / 10.0;

            drawStrokedText(
                g2,
                "Workers: " + effectiveWorkers + " / " + maxWorkers,
                textX,
                bodyY + 36,
                new Color(255, 200, 100),
                new Color(8, 8, 8, 220),
                1.5f
            );
            drawStrokedText(
                g2,
                "Income: +" + formatIncome(effectiveIncome) + "/s",
                textX,
                bodyY + 54,
                new Color(100, 230, 120),
                new Color(8, 8, 8, 220),
                1.5f
            );
            drawStrokedText(
                g2,
                "Max income: +" + maxIncome + "/s",
                textX,
                bodyY + 72,
                new Color(150, 200, 150),
                new Color(8, 8, 8, 220),
                1.5f
            );
        }
    }

    // Method untuk handle input saat sedang edit nama bangunan, menangkap karakter yang diketik, backspace, dan enter untuk save
    private void handleBuildingTitleEditingInput() {
        if (!isEditingBuildingTitle || selectedBuildingInfo == null) {
            return;
        }

        String typed = keyH.consumeTypedChars();
        if (!typed.isEmpty()) {
            for (int i = 0; i < typed.length(); i++) {
                char c = typed.charAt(i);
                if (Character.isISOControl(c)) {
                    continue;
                }
                if (buildingTitleDraft.length() < BUILDING_TITLE_MAX_LENGTH) {
                    buildingTitleDraft += c;
                }
            }
        }

        if (keyH.consumeBackspaceClick() && !buildingTitleDraft.isEmpty()) {
            buildingTitleDraft = buildingTitleDraft.substring(0, buildingTitleDraft.length() - 1);
        }

        if (keyH.consumeEnterClick()) {
            finishTitleEditingAndSave();
        }
    }

    // Method untuk mulai edit nama bangunan, dipanggil saat player klik area nama di UI detail bangunan, menyimpan nama yang sedang diedit di buildingTitleDraft dan menangkap input keyboard untuk update draft
    private void startTitleEditing() {
        if (selectedBuildingInfo == null) {
            return;
        }

        isEditingBuildingTitle = true;
        buildingTitleDraft = selectedBuildingInfo.getName();
        keyH.consumeTypedChars();
    }

    // Method untuk selesai edit nama bangunan, dipanggil saat player tekan enter atau klik di luar area edit, menyimpan nama baru ke BuildingInstance dan save game jika ada perubahan
    private void finishTitleEditingAndSave() {
        if (!isEditingBuildingTitle || selectedBuildingInfo == null) {
            isEditingBuildingTitle = false;
            buildingTitleDraft = "";
            return;
        }

        String newName = buildingTitleDraft.trim();
        if (!newName.isEmpty() && !newName.equals(selectedBuildingInfo.getName())) {
            selectedBuildingInfo.setName(newName);
            saveGame();
        }

        isEditingBuildingTitle = false;
        buildingTitleDraft = "";
    }

    // Method untuk menghitung posisi X panel detail bangunan di sebelah kanan layar, dipakai untuk cek klik dan gambar panel
    private int getBuildingDetailPanelX() {
        return screenWidth - BUILDING_INFO_PANEL_WIDTH - BUILDING_INFO_PANEL_RIGHT_MARGIN;
    }

    // Method untuk mencari apakah koordinat mouse berada di dalam area panel detail bangunan, dipakai untuk interaksi klik panel dan edit nama
    private boolean isInsideBuildingDetailPanel(int mouseX, int mouseY) {
        int panelX = getBuildingDetailPanelX();
        int panelY = BUILDING_INFO_PANEL_TOP_BOTTOM_MARGIN;
        int panelHeight = screenHeight - (BUILDING_INFO_PANEL_TOP_BOTTOM_MARGIN * 2);

        return mouseX >= panelX
            && mouseX <= panelX + BUILDING_INFO_PANEL_WIDTH
            && mouseY >= panelY
            && mouseY <= panelY + panelHeight;
    }

    // Method untuk mencari apakah koordinat mouse berada di dalam area judul di panel detail bangunan, dipakai untuk mulai edit nama saat klik area judul
    private boolean isInsideBuildingTitleEditArea(int mouseX, int mouseY) {
        int panelX = getBuildingDetailPanelX();
        int panelY = BUILDING_INFO_PANEL_TOP_BOTTOM_MARGIN;

        int titleX = panelX + BUILDING_TITLE_INPUT_X_PADDING;
        int titleY = panelY + BUILDING_TITLE_INPUT_Y + BUILDING_TITLE_INPUT_OFFSET_Y;
        int titleWidth = BUILDING_INFO_PANEL_WIDTH - (BUILDING_TITLE_INPUT_X_PADDING * 2);
        int titleHeight = BUILDING_TITLE_INPUT_HEIGHT;

        return mouseX >= titleX
            && mouseX <= titleX + titleWidth
            && mouseY >= titleY
            && mouseY <= titleY + titleHeight;
    }

    // gambar teks dengan outline stroke untuk meningkatkan keterbacaan di atas background yang sibuk, dipakai untuk nama dan info detail bangunan di panel detail
    // format uang dengan satuan yang lebih besar untuk jumlah besar, dipakai untuk menampilkan uang player di UI
    private String formatMoney(long amount) {
        if (amount >= 1_000_000_000_000_000L) {
            return String.format("%.1fqua", amount / 1_000_000_000_000_000.0);
        } else if (amount >= 1_000_000_000_000L) {
            return String.format("%.1ftri", amount / 1_000_000_000_000.0);
        } else if (amount >= 1_000_000_000L) {
            return String.format("%.1fbil", amount / 1_000_000_000.0);
        } else if (amount >= 1_000_000L) {
            return String.format("%.1fmil", amount / 1_000_000.0);
        } else if (amount >= 1_000L) {
            return String.format("%.1fk", amount / 1_000.0);
        } else {
            return String.valueOf(amount);
        }
    }

    private String formatIncome(double amount) {
        if (amount >= 1_000_000_000.0) return String.format("%.1fbil", amount / 1_000_000_000.0);
        if (amount >= 1_000_000.0)     return String.format("%.1fmil", amount / 1_000_000.0);
        if (amount >= 1_000.0)         return String.format("%.1fk",   amount / 1_000.0);
        // Show 1 decimal, strip trailing zero (e.g. 7.0 → "7", 7.5 → "7.5")
        String s = String.format("%.1f", amount);
        return s.endsWith(".0") ? s.substring(0, s.length() - 2) : s;
    }
    private void drawStrokedText(Graphics2D g2, String text, int x, int y, Color fillColor, Color strokeColor, float strokeWidth) {
        if (text == null || text.isEmpty()) {
            return;
        }

        TextLayout layout = new TextLayout(text, g2.getFont(), g2.getFontRenderContext());
        java.awt.Shape outline = layout.getOutline(AffineTransform.getTranslateInstance(x, y));

        Color oldColor = g2.getColor();
        Stroke oldStroke = g2.getStroke();

        g2.setColor(strokeColor);
        g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(outline);

        g2.setColor(fillColor);
        g2.fill(outline);

        g2.setColor(oldColor);
        g2.setStroke(oldStroke);
    }

    // Method untuk mencari apakah ada bangunan di koordinat dunia tertentu, dipakai untuk interaksi klik bangunan dan info detail
    private BuildingInstance findBuildingAtWorld(int worldX, int worldY) {
        int gridX = worldX / tileSize;
        int gridY = worldY / tileSize;

        if (gridX < 0 || gridX >= gp.maxWorldCol || gridY < 0 || gridY >= gp.maxWorldRow) {
            return null;
        }

        if (buildingDataMap[gridX][gridY] != null) {
            return buildingDataMap[gridX][gridY];
        }

        int maxBuildingWidth = 1;
        int maxBuildingHeight = 1;
        for (BuildingType type : BuildingType.values()) {
            maxBuildingWidth = Math.max(maxBuildingWidth, type.getWidth());
            maxBuildingHeight = Math.max(maxBuildingHeight, type.getHeight());
        }

        int startAnchorX = Math.max(0, gridX - maxBuildingWidth + 1);
        int startAnchorY = Math.max(0, gridY - maxBuildingHeight + 1);

        for (int anchorX = startAnchorX; anchorX <= gridX; anchorX++) {
            for (int anchorY = startAnchorY; anchorY <= gridY; anchorY++) {
                BuildingInstance instance = buildingDataMap[anchorX][anchorY];
                if (instance == null) {
                    continue;
                }

                BuildingType type = instance.getType();
                int endX = anchorX + type.getWidth() - 1;
                int endY = anchorY + type.getHeight() - 1;
                if (gridX >= anchorX && gridX <= endX && gridY >= anchorY && gridY <= endY) {
                    return instance;
                }
            }
        }

        return null;
    }

    // Method untuk load font khusus untuk UI detail bangunan, kalau gagal load font, pakai font default
    private void loadBuildingInfoFonts() {
        Font fallbackTitle = new Font("Dialog", Font.BOLD, 19);
        Font fallbackBody = new Font("Dialog", Font.BOLD, 15);
        String fontPath = "/asset/font/terminal-grotesque.grotesque-regular.ttf";

        try {
            Font base = loadFont(fontPath);
            if (base != null) {
                buildingInfoTitleFont = base.deriveFont(Font.PLAIN, 19f);
                buildingInfoBodyFont = base.deriveFont(Font.BOLD, 15f);
            } else {
                buildingInfoTitleFont = fallbackTitle;
                buildingInfoBodyFont = fallbackBody;
            }
        } catch (IOException | FontFormatException e) {
            System.out.println("Failed to load custom UI font: " + e.getMessage());
            buildingInfoTitleFont = fallbackTitle;
            buildingInfoBodyFont = fallbackBody;
        }
        // Pre-derive all fonts used every frame so deriveFont() is never called at render time
        hudFont = buildingInfoBodyFont.deriveFont(Font.BOLD, 17f);
        detailTitleFontCached = buildingInfoTitleFont.deriveFont(buildingInfoTitleFont.getSize2D() + 2f);
        detailBodyFontCached  = buildingInfoBodyFont.deriveFont(buildingInfoBodyFont.getSize2D() + 2f);
    }

    // Method untuk load font dari resource path, mencoba dari classpath dulu, kalau gagal coba dari file system, dipakai untuk load font khusus UI detail bangunan
    private Font loadFont(String resourcePath) throws IOException, FontFormatException {
        try (InputStream stream = getClass().getResourceAsStream(resourcePath)) {
            if (stream != null) {
                return Font.createFont(Font.TRUETYPE_FONT, stream);
            }
        }

        String filePath = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }

        return Font.createFont(Font.TRUETYPE_FONT, file);
    }

    private void drawDayNightOverlay(Graphics2D g2) {
        java.awt.Color overlay = dayTime.getOverlayColor();
        if (overlay.getAlpha() > 0) {
            g2.setColor(overlay);
            g2.fillRect(0, 0, screenWidth, screenHeight);
        }
    }

    public void drawGameplay(Graphics2D g2){
        gp.tileM.draw(g2); // gambar tile berdasarkan worldMap

        drawBuildings(g2); // gambar bangunan di atas terrain

        drawDayNightOverlay(g2); // gambar overlay siang/malam

        drawBuildingGlows(g2); // gambar glow bangunan di atas overlay malam

        drawGrid(g2); // gambar grid di atas tile

        drawBuildingPreview(g2); // gambar preview saat mau menaruh bangunan

        // gambar toolbar di atas semua layer lain, jadi selalu terlihat
        toolbar.draw(g2, screenWidth, screenHeight, mouseH.mouseX, mouseH.mouseY, this);

        // gambar UI detail bangunan saat player klik bangunan di map
        drawBuildingDetailUI(g2);

        drawPlayerHUD(g2);
    }
    private void recalcIncomeCache() {
        cachedTotalPopulation = getTotalPopulation();

        // Collect production buildings and sort by placement order so workers
        // prioritize the first-placed building.
        List<BuildingInstance> prodList = new ArrayList<>();
        for (int x = 0; x < gp.maxWorldCol; x++) {
            for (int y = 0; y < gp.maxWorldRow; y++) {
                BuildingInstance inst = buildingDataMap[x][y];
                if (inst != null && inst.getType().getCategory() == BuildingType.BuildingCategory.PRODUCTION) {
                    prodList.add(inst);
                }
            }
        }
        prodList.sort((a, b) -> Integer.compare(a.getPlacementOrder(), b.getPlacementOrder()));

        int remainingPop = cachedTotalPopulation;
        double effectiveIncome = 0.0;
        for (BuildingInstance inst : prodList) {
            int maxW = inst.getType().getMaxWorkers();
            int workers = Math.min(remainingPop, maxW);
            remainingPop -= workers;
            double localRatio = maxW > 0 ? (double) workers / maxW : 0.0;
            effectiveIncome += Math.floor(localRatio * inst.getType().getIncomePerSecond() * 10.0) / 10.0;
        }
        cachedTotalIncome = effectiveIncome;
    }

    // Method untuk menghitung total populasi dari semua bangunan HOUSING yang sudah ditempatkan
    private int getTotalPopulation() {
        int total = 0;
        for (int x = 0; x < gp.maxWorldCol; x++) {
            for (int y = 0; y < gp.maxWorldRow; y++) {
                BuildingInstance inst = buildingDataMap[x][y];
                if (inst != null) {
                    total += inst.getPopulation();
                }
            }
        }
        return total;
    }

    // Gambar uang player di pojok kiri atas layar
    private void drawPlayerHUD(Graphics2D g2) {
        g2.setFont(hudFont != null ? hudFont : new Font("Dialog", Font.BOLD, 17));

        int coinSize = 20;
        int gap = 6; // gap between coin icon and text

        int innerPadX = 10;
        int boxH = 28;
        int innerPadY = 0;
        int contentH = boxH;
        int boxX = 8;
        int boxY = 8;

        String moneyText = formatMoney(playerMoney);
        String incomeText = cachedTotalIncome > 0 ? "  +" + formatIncome(cachedTotalIncome) + "/s" : "";
        String popDivider = "  |  ";
        String populationText = String.valueOf(cachedTotalPopulation);
        String timeDivider = "  |  ";
        String timeText = dayTime.getTimeLabel() + "  " + dayTime.getPeriodName()
            + "   " + temperature.getTempLabel(dayTime.getHour());
        int textWidth       = g2.getFontMetrics().stringWidth(moneyText);
        int incomeWidth     = g2.getFontMetrics().stringWidth(incomeText);
        int popDividerWidth = g2.getFontMetrics().stringWidth(popDivider);
        int populationWidth = g2.getFontMetrics().stringWidth(populationText);
        int timeDividerWidth = g2.getFontMetrics().stringWidth(timeDivider);
        int timeTextWidth   = g2.getFontMetrics().stringWidth(timeText);
        int iconSlot = (coinImage != null) ? coinSize + gap : 0;
        int personIconSlot = (personImage != null) ? coinSize + gap : 0;

        // Warna teks untuk waktu dan temperatur berubah berdasarkan darkness, semakin gelap semakin mendekati warna oranye, semakin terang mendekati warna putih
        float darkness = dayTime.getDarkness();

        int boxW = iconSlot + textWidth + incomeWidth + popDividerWidth + personIconSlot + populationWidth + timeDividerWidth + timeTextWidth + innerPadX * 2;

        hudPanelSkin.draw(g2, boxX, boxY, boxW, boxH);

        int contentX = boxX + innerPadX;
        int coinY = boxY + innerPadY + (contentH - coinSize) / 2;
        if (coinImage != null) {
            g2.drawImage(coinImage, contentX, coinY, coinSize, coinSize, null);
            contentX += coinSize + gap;
        }

        int textBaseY = boxY + boxH - 8;
        drawStrokedText(g2, moneyText, contentX, textBaseY,
            new Color(255, 220, 60), new Color(0, 0, 0, 220), 1.5f);

        if (!incomeText.isEmpty()) {
            drawStrokedText(
                g2,
                incomeText,
                contentX + textWidth,
                textBaseY,
                new Color(100, 230, 120),
                new Color(0, 0, 0, 220),
                1.5f
            );
        }

        int popDividerX = contentX + textWidth + incomeWidth;
        drawStrokedText(g2, popDivider, popDividerX, textBaseY,
            new Color(180, 180, 180), new Color(0, 0, 0, 220), 1.5f);
        int personIconX = popDividerX + popDividerWidth;
        if (personImage != null) {
            int personIconY = boxY + innerPadY + (contentH - coinSize) / 2;
            g2.drawImage(personImage, personIconX, personIconY, coinSize, coinSize, null);
            personIconX += coinSize + gap;
        }
        drawStrokedText(g2, populationText, personIconX, textBaseY,
            new Color(80, 180, 255), new Color(0, 0, 0, 220), 1.5f);

        // waktu dan temperatur di pojok kanan atas, warnanya berubah berdasarkan darkness, semakin gelap semakin mendekati warna oranye, semakin terang mendekati warna putih
        int tr = (int)(255 * (1f - darkness * 0.4f));
        int tg = (int)(220 + darkness * 20f);
        int tb = (int)(150 + darkness * 105f);
        int dividerX = popDividerX + popDividerWidth + personIconSlot + populationWidth;
        drawStrokedText(g2, timeDivider, dividerX, textBaseY,
            new Color(180, 180, 180), new Color(0, 0, 0, 220), 1.5f);
        drawStrokedText(g2, timeText, dividerX + timeDividerWidth, textBaseY,
            new Color(Math.min(tr,255), Math.min(tg,255), Math.min(tb,255)),
            new Color(0, 0, 0, 220), 1.5f);

        // Kalau sedang dalam build mode dan bangunan dipilih, tampilkan harga bangunan
        BuildingType selected = toolbar.getSelectedBuilding();
        if (showGrid && selected != null) {
            String priceText = formatMoney(selected.getPrice());
            boolean canAfford = playerMoney >= selected.getPrice();
            int pw = g2.getFontMetrics().stringWidth(priceText);
            int pIconSlot = (coinImage != null) ? coinSize + gap : 0;
            int pBoxX = 8;
            int pBoxY = boxY + boxH + 4;
            int pBoxW = pIconSlot + pw + innerPadX * 2;

            hudPanelSkin.draw(g2, pBoxX, pBoxY, pBoxW, boxH);

            int pContentX = pBoxX + innerPadX;
            int pCoinY = pBoxY + (boxH - coinSize) / 2;
            if (coinImage != null) {
                g2.drawImage(coinImage, pContentX, pCoinY, coinSize, coinSize, null);
                pContentX += coinSize + gap;
            }

            drawStrokedText(
                g2,
                priceText,
                pContentX,
                pBoxY + boxH - 8,
                canAfford ? new Color(120, 255, 120) : new Color(255, 80, 80),
                new Color(0, 0, 0, 220),
                1.5f
            );
        }
    }

    public void saveGame() { // save game
        Player_SaveFile.saveWorldMap(worldMap);
        Player_SaveFile.saveBuildingsMap(buildingDataMap);
        Player_SaveFile.savePlayerData(playerMoney);
        Player_SaveFile.saveDayTime(dayTime.getHour());
        Player_SaveFile.saveTemperature(temperature.getDailyHigh(), temperature.getDailyLow());
    }
}