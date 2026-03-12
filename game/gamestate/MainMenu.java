package game.gamestate;

import java.awt.Color;
import java.awt.Graphics2D;
import system.KeyHandler;
import system.GameStateManager;
import system.GamePanel;

import system.sprite2D;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class MainMenu {
    private KeyHandler keyH;
    sprite2D sprite = new sprite2D();
    private GameStateManager gsm;
    private GamePanel gp;

    private BufferedImage logo; // placeholder untuk gambar

    public MainMenu(KeyHandler keyH, GameStateManager gsm, GamePanel gp) {
        this.keyH = keyH;
        this.gsm = gsm; // Initialize GameStateManager
        this.gp = gp; // Initialize GamePanel

        getImage(); // load gambar
    }

    public void getImage() {
        try {
            logo = ImageIO.read(getClass().getResourceAsStream("/asset/Logo.png"));

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load main menu images. Check resource paths!");
        }
    }

    public void updateMenu() {
        if (keyH.enterPressed) {
            // Logic to switch to gameplay state
            System.out.println("Enter pressed! Switching to Gameplay...");
            gsm.setState(GameStateManager.PLAY_STATE);
        }
    }

    public void drawMenu(Graphics2D g2) {
        g2.setColor(Color.gray);
        g2.fillRect(0, 0, gp.besarLayar, gp.tinggiLayar); // background main menu

        sprite.drawSprite(g2, logo, 80, 0, 600, 300); // placeholder for menu title

        g2.setColor(Color.white);
        g2.drawString("Main Menu - Press [Enter] to Start", 300, 300);
    }
}
