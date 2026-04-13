package game.gamestate;

// Load Graphics
import java.awt.Graphics2D;

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
    Toolbar toolbar = new Toolbar();

    public Gameplay(KeyHandler keyH, MouseHandler mouseH, GamePanel gp) { //init sebelum run game
        this.keyH = keyH;
        this.mouseH = mouseH;
        this.gp = gp;
        this.tileSize = gp.tileSize;
        this.screenWidth = gp.besarLayar;
        this.screenHeight = gp.tinggiLayar;
        worldMap = new int[gp.maxWorldCol][gp.maxWorldRow];
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
        toolbar.update(mouseH.mouseX, mouseH.mouseY, mouseH.leftPressed);

    }

    public void loadWorldMap() { // ngeload world map dari save file, kalau ga ada save file, generate baru
        int[][] loadedMap = Player_SaveFile.loadWorldMap();
        if (loadedMap != null) {
            worldMap = loadedMap;
            gp.tileM.setMap(worldMap);
        } else {
            generateWorld(); // if no save, generate new
        }
    }

    public void drawGameplay(Graphics2D g2){
        gp.tileM.draw(g2); // draw tile

        toolbar.draw(g2, screenWidth, screenHeight, mouseH.mouseX, mouseH.mouseY);
        // Player and money removed - just 2D camera view
    }

    public void saveGame() { // save game
        Player_SaveFile.saveWorldMap(worldMap);
    }
}