package game.gamestate;

import java.awt.Color;
import java.awt.Graphics2D;
import system.KeyHandler;

public class Gameplay {

    KeyHandler keyH;

    int playerX = 100;
    int playerY = 100;
    int playerSpeed = 4;

    // Constructor untuk Gameplay, menerima KeyHandler sebagai parameter
    // biar bisa ngambil input dari keyboard untuk update posisi player

    public Gameplay(KeyHandler keyH){
        this.keyH = keyH;
    }

    public void updateGameplay(){

        if(keyH.upPressed){
            playerY -= playerSpeed;
        }
        if(keyH.downPressed){
            playerY += playerSpeed;
        }
        if(keyH.leftPressed){
            playerX -= playerSpeed;
        }
        if(keyH.rightPressed){
            playerX += playerSpeed;
        }
    }

    public void drawGameplay(Graphics2D g2){
        g2.setColor(Color.blue);
        g2.fillRect(playerX, playerY, 50, 50);

        g2.setColor(Color.black);
        g2.drawString("Hello World", 100, 100);
    }
}