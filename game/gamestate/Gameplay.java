package game.gamestate;

// Load Graphics
import java.awt.Graphics2D;

// load system
import system.KeyHandler;
import system.GamePanel;
import system.MouseHandler;

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

    public Gameplay(KeyHandler keyH, MouseHandler mouseH, GamePanel gp) { //init sebelum run game
        this.keyH = keyH;
        this.mouseH = mouseH;
        this.gp = gp;
        this.tileSize = gp.tileSize;
        this.screenWidth = gp.besarLayar;
        this.screenHeight = gp.tinggiLayar;
    }

    public void saveGame() { // save game - nothing to save now
        // Removed money save
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
                // if (gp.cameraWorldX < 0) gp.cameraWorldX = 0;
                // if (gp.cameraWorldY < 0) gp.cameraWorldY = 0;
                // if (gp.cameraWorldX > gp.worldWidth - gp.besarLayar) gp.cameraWorldX = gp.worldWidth - gp.besarLayar;
                // if (gp.cameraWorldY > gp.worldHeight - gp.tinggiLayar) gp.cameraWorldY = gp.worldHeight - gp.tinggiLayar;
            }
        } else {
            isDragging = false;
        }

    }

    public void drawGameplay(Graphics2D g2){
        gp.tileM.draw(g2); // draw tile

        // Player and money removed - just 2D camera view
    }
}