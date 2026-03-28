package game.gamestate;

import java.awt.Color;
import java.awt.Graphics2D;

import system.KeyHandler;
import system.GameStateManager;
import system.GamePanel;
import game.MenuBackgroundParallax;

import system.Sprite2D;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class MainMenu {
    private KeyHandler keyH;
    Sprite2D sprite = new Sprite2D();
    private GameStateManager gsm;
    private GamePanel gp;
    private MenuBackgroundParallax menuBackground;

    private BufferedImage logo; // placeholder untuk gambar

    public MainMenu(KeyHandler keyH, GameStateManager gsm, GamePanel gp) { // init sebelum run game
        this.keyH = keyH;
        this.gsm = gsm; // Initialize GameStateManager
        this.gp = gp; // Initialize GamePanel
        menuBackground = new MenuBackgroundParallax(gp);
        menuBackground.getImages();


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
        menuBackground.update();

        if (keyH.enterPressed) {
            // Logic to switch to gameplay state
            System.out.println("Enter pressed! Switching to Gameplay...");
            gsm.setState(GameStateManager.PLAY_STATE);
            
        }
    }

    public void drawMenu(Graphics2D g2) {
        g2.setColor(Color.gray);
        g2.fillRect(0, 0, gp.besarLayar, gp.tinggiLayar); // background main menu

        menuBackground.drawParallax(g2);



        sprite.drawSprite(g2, logo, 140, 0, 600, 300); // placeholder for menu title

        g2.setColor(Color.white);
        g2.drawString("Main Menu - Press [Enter] to Start", 360, 300);
    }
}
