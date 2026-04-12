package game.gamestate;

// Load Graphics
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;

// load system
import system.KeyHandler;
import game.entity.Player;
import system.GamePanel;
import system.Player_SaveFile;
import system.Button;
import system.MouseHandler;

//load image
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;

public class Gameplay {

    KeyHandler keyH;
    Player player;

    public int tileSize = 50;
    public int screenWidth;
    public int screenHeight;
    private GamePanel gp;
    private int money; // loaded from save
    private MouseHandler mouseH;
    private Button button;
    private BufferedImage buttonImage;
    private BufferedImage buttonImageHover;
    private int buttonX, buttonY, buttonWidth, buttonHeight;

    public Gameplay(KeyHandler keyH, MouseHandler mouseH, GamePanel gp) { //init sebelum run game
        this.keyH = keyH;
        this.mouseH = mouseH;
        this.gp = gp;
        this.tileSize = gp.tileSize;
        this.screenWidth = gp.besarLayar;
        this.screenHeight = gp.tinggiLayar;

        // Load Semua data milik player dari database.
        money = Player_SaveFile.loadPlayerData();


        this.player = new Player(this, keyH);
        gp.player = this.player;

        // Initialize button
        button = new Button();
        buttonX = screenWidth - 100;
        buttonY = screenHeight - 50;
        buttonWidth = 80;
        buttonHeight = 40;

        // Load button image
        try {
            buttonImage = ImageIO.read(new File("asset/button/startbutton.png"));
            buttonImageHover = ImageIO.read(new File("asset/button/startbuttonhover.png"));
        } catch (IOException e) {
            e.printStackTrace();
            // Placeholder if image not found
            buttonImage = null;
            buttonImageHover = null;
        }
    }

    public void saveGame() { // save game ke database
        Player_SaveFile.savePlayerData(money);
    }

    public void updateGameplay(){ // Update untuk Logic gameplay, seperti input, movement, dll
        player.update(); // update player dulu biar bisa akses posisinya untuk logic lain

        // Check for button click
        if (mouseH.consumeLeftClick() && button.isHovering(buttonX, buttonY, buttonWidth, buttonHeight, mouseH.mouseX, mouseH.mouseY)) {
            money += 100;
        }
    }

    public void drawGameplay(Graphics2D g2){
        gp.tileM.draw(g2); // draw tile dulu biar background muncul sebelum player
        player.draw(g2); // baru draw player setelah tile

        // Draw monney/uang di pojok kiri atas
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.setColor(Color.BLACK); // shadow
        g2.drawString("Money: " + money, 16, 41);
        g2.setColor(Color.YELLOW); // main text
        g2.drawString("Money: " + money, 15, 40);

        // Draw button
        if (buttonImage != null && buttonImageHover != null) {
            button.drawButton(g2, buttonImage, buttonImageHover, buttonX, buttonY, buttonWidth, buttonHeight, mouseH.mouseX, mouseH.mouseY);
        } else {
            // Fallback: draw kotak sederhana jika gambar tidak tersedia
            g2.setColor(Color.BLUE);
            g2.fillRect(buttonX, buttonY, buttonWidth, buttonHeight);
            g2.setColor(Color.WHITE);
            g2.drawString("Add Money", buttonX + 30, buttonY + 25);
        }
    }
}