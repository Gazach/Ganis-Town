package game.gamestate;

import java.awt.Color;
import java.awt.Graphics2D;
import system.KeyHandler;
import system.entity.Player;

public class Gameplay {

    KeyHandler keyH;
    Player player;

    public int tileSize = 50; // <-- set sprite size
    int playerX = 100;
    int playerY = 100;
    int playerSpeed = 4;

    // Constructor: initialize KeyHandler first, then player
    public Gameplay(KeyHandler keyH){
        this.keyH = keyH;
        this.player = new Player(this, keyH); // now keyH is not null
    }

    public void updateGameplay(){
        player.update();
    }

    public void drawGameplay(Graphics2D g2){
        // Debug background for testing
        g2.setColor(Color.blue);
        g2.fillRect(playerX, playerY, tileSize, tileSize);

        g2.setColor(Color.black);
        g2.drawString("Hello World", 100, 100);

        player.draw(g2); // draw the player sprite
    }
}