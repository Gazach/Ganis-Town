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

    private Button newGameButton, loadGameButton, exitGameButton;
    private BufferedImage newGameMain, newGameHover, newGameClicked;
    private BufferedImage loadGameMain, loadGameHover, loadGameClicked;
    private BufferedImage exitGameMain, exitGameHover, exitGameClicked;


    public MainMenu(KeyHandler keyH, MouseHandler mouseH, GameStateManager gsm, GamePanel gp) { // init sebelum run game
        this.mouseH = mouseH;
        this.gsm = gsm; // Initialize GameStateManager
        this.gp = gp; // Initialize GamePanel
        menuBackground = new MenuBackgroundParallax(gp);
        menuBackground.getImages();

        newGameButton = new Button();
        loadGameButton = new Button();
        exitGameButton = new Button();

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

            newGameMain = loadImage("/asset/mainmenu/button/New_game_button.png", "asset/mainmenu/button/New_game_button.png");
            newGameHover = loadImage("/asset/mainmenu/button/New_game_button_Hover.png", "asset/mainmenu/button/New_game_button_Hover.png");
            newGameClicked = loadImage("/asset/mainmenu/button/New_game_button_Hover_Clicked.png", "asset/mainmenu/button/New_game_button_Hover_Clicked.png");

            loadGameMain = loadImage("/asset/mainmenu/button/Load_game_button.png", "asset/mainmenu/button/Load_game_button.png");
            loadGameHover = loadImage("/asset/mainmenu/button/Load_game_button_Hover.png", "asset/mainmenu/button/Load_game_button_Hover.png");
            loadGameClicked = loadImage("/asset/mainmenu/button/Load_game_button_Hover_Clicked.png", "asset/mainmenu/button/Load_game_button_Hover_Clicked.png");

            exitGameMain = loadImage("/asset/mainmenu/button/Exit_game_button.png", "asset/mainmenu/button/Exit_game_button.png");
            exitGameHover = loadImage("/asset/mainmenu/button/Exit_game_button_Hover.png", "asset/mainmenu/button/Exit_game_button_Hover.png");
            exitGameClicked = loadImage("/asset/mainmenu/button/Exit_game_button_Hover_Clicked.png", "asset/mainmenu/button/Exit_game_button_Hover_Clicked.png");

            if (logo == null ||
                newGameMain == null || newGameHover == null || newGameClicked == null ||
                loadGameMain == null || loadGameHover == null || loadGameClicked == null ||
                exitGameMain == null || exitGameHover == null || exitGameClicked == null) {
                throw new IOException("UI images failed to load.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load main menu images. Check resource paths and file names!");
        }
    }

    public void updateMenu() {
        menuBackground.update();

        // Button dimensions and positions
        int buttonWidth = 200;
        int buttonHeight = 120;
        int margin = 25;
        int totalWidth = buttonWidth * 3 + margin * 2;
        int startX = (gp.besarLayar - totalWidth) / 2;
        int y = 350;

        boolean clicked = mouseH.consumeLeftClick(); // Cek klik sekali dan reset state klik setelahnya
        //================ New Game button ================
        boolean isHoveringNew = newGameButton.isHovering(startX, y, buttonWidth, buttonHeight, mouseH.mouseX, mouseH.mouseY);
        if (clicked && isHoveringNew) {
            System.out.println("Starting New Game!");
            gsm.setState(GameStateManager.PLAY_STATE);
        }
        //==============================================

        //================ Load Game button ================
        int loadX = startX + buttonWidth + margin;
        boolean isHoveringLoad = loadGameButton.isHovering(loadX, y, buttonWidth, buttonHeight, mouseH.mouseX, mouseH.mouseY);
        if (clicked && isHoveringLoad) {
            System.out.println("Loading Game!");
        }
        //==============================================

        //================ Exit Game button ================
        int exitX = loadX + buttonWidth + margin;
        boolean isHoveringExit = exitGameButton.isHovering(exitX, y, buttonWidth, buttonHeight, mouseH.mouseX, mouseH.mouseY);
        if (clicked && isHoveringExit) {
            System.out.println("Exiting Game!");
            System.exit(0);
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

        // Button dimensions and positions
        int buttonWidth = 200;
        int buttonHeight = 120;
        int margin = 25;
        int totalWidth = buttonWidth * 3 + margin * 2;
        int startX = (gp.besarLayar - totalWidth) / 2;
        int y = 350;

        // Draw buttons agar bisa diliat hover effectnya, klik effectnya, dan juga untuk cek apakah mouse sedang hover atau tidak
        boolean isNewGameClicked = mouseH.leftPressed && newGameButton.isHovering(startX, y, buttonWidth, buttonHeight, mouseH.mouseX, mouseH.mouseY);
        newGameButton.drawButton(g2, newGameMain, newGameHover, newGameClicked, startX, y, buttonWidth, buttonHeight, mouseH.mouseX, mouseH.mouseY, isNewGameClicked);

        int loadX = startX + buttonWidth + margin;
        boolean isLoadGameClicked = mouseH.leftPressed && loadGameButton.isHovering(loadX, y, buttonWidth, buttonHeight, mouseH.mouseX, mouseH.mouseY);
        loadGameButton.drawButton(g2, loadGameMain, loadGameHover, loadGameClicked, loadX, y, buttonWidth, buttonHeight, mouseH.mouseX, mouseH.mouseY, isLoadGameClicked);

        int exitX = loadX + buttonWidth + margin;
        boolean isExitGameClicked = mouseH.leftPressed && exitGameButton.isHovering(exitX, y, buttonWidth, buttonHeight, mouseH.mouseX, mouseH.mouseY);
        exitGameButton.drawButton(g2, exitGameMain, exitGameHover, exitGameClicked, exitX, y, buttonWidth, buttonHeight, mouseH.mouseX, mouseH.mouseY, isExitGameClicked);
    }
}
