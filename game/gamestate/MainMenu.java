package game.gamestate;

import java.awt.Color;
import java.awt.Graphics2D;
import system.KeyHandler;
import system.GameStateManager;

public class MainMenu {
    private KeyHandler keyH;
    private GameStateManager gsm;

    public MainMenu(KeyHandler keyH, GameStateManager gsm) {
        this.keyH = keyH;
        this.gsm = gsm; // Initialize GameStateManager
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
        g2.fillRect(0, 0, 800, 600); // background main menu

        g2.setColor(Color.white);
        g2.drawString("Main Menu - Press [Enter] to Start", 300, 300);
    }
}
