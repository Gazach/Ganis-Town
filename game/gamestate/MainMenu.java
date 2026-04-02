package game.gamestate;

import java.awt.Color;
import java.awt.Graphics2D;

import system.KeyHandler;
import system.MouseHandler;
import system.GameStateManager;
import system.GamePanel;
import game.MenuBackgroundParallax;

import system.Sprite2D;
import system.Button;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;


public class MainMenu {
    private MouseHandler mouseH;
    Sprite2D sprite = new Sprite2D();
    private GameStateManager gsm;
    private GamePanel gp;
    private MenuBackgroundParallax menuBackground;

    private BufferedImage logo; // placeholder untuk gambar

    private Button startButton;
    private BufferedImage startButtonMain;
    private BufferedImage startButtonHover;


    public MainMenu(KeyHandler keyH, MouseHandler mouseH, GameStateManager gsm, GamePanel gp) { // init sebelum run game
        this.mouseH = mouseH;
        this.gsm = gsm; // Initialize GameStateManager
        this.gp = gp; // Initialize GamePanel
        menuBackground = new MenuBackgroundParallax(gp);
        menuBackground.getImages();

        startButton = new Button();

        getImage(); // load gambar
    }

    private BufferedImage loadImage(String resourcePath, String fallbackPath) throws IOException {
        BufferedImage image = null;
        if (getClass().getResourceAsStream(resourcePath) != null) {
            image = ImageIO.read(getClass().getResourceAsStream(resourcePath));
        }
        if (image == null) {
            File fallback = new File(fallbackPath);
            if (fallback.exists()) {
                image = ImageIO.read(fallback);
            }
        }
        return image;
    }

    public void getImage() {
        try {
            logo = loadImage("/asset/logo.png", "asset/logo.png");
            startButtonMain = loadImage("/asset/button/startbutton.png", "asset/button/startbutton.png");
            startButtonHover = loadImage("/asset/button/startbuttonhover.jpg", "asset/button/startbuttonhover.jpg");

            if (logo == null || startButtonMain == null || startButtonHover == null) {
                throw new IOException("UI images failed to load.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load main menu images. Check resource paths and file names!");
        }
    }

    public void updateMenu() {
        menuBackground.update();

        //================ Start button ================
        boolean isHovering = startButton.isHovering(350, 350, 200, 80, mouseH.mouseX, mouseH.mouseY);

        if (mouseH.consumeLeftClick() && isHovering) {
            System.out.println("Swtich ke Gameplay State!");
            gsm.setState(GameStateManager.PLAY_STATE);
        }
        //==============================================
    }

    public void drawMenu(Graphics2D g2) {
        g2.setColor(Color.gray);
        g2.fillRect(0, 0, gp.besarLayar, gp.tinggiLayar); // background main menu

        menuBackground.drawParallax(g2);

        sprite.drawSprite(g2, logo, 140, 0, 600, 300); // placeholder for menu title
        
        // draw UI untuk menu
        // draw button dengan hover effect
        startButton.drawButton(g2, startButtonMain, startButtonHover, 350, 350, 200, 80, mouseH.mouseX, mouseH.mouseY);
    

    }
}
