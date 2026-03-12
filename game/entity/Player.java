package game.entity;

import java.awt.Graphics2D;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import game.gamestate.Gameplay;
import system.Entity;
import system.KeyHandler;
import system.sprite2D;



public class Player extends Entity {

    Gameplay gp;
    sprite2D sprite = new sprite2D();
    KeyHandler keyH;

    public Player(Gameplay gp, KeyHandler keyH) {
        this.gp = gp;
        this.keyH = keyH;

        setDefaultValues();
        getPlayerImage();
    }

    public void setDefaultValues(){
        x = 50;
        y = 100;
        speed = 4;
        direction = "down";
    }

    public void getPlayerImage(){ //untuk ngambil gambar yang ada di folder player
        try{
            up1 = ImageIO.read(getClass().getResourceAsStream("/asset/Player/Up.png"));
            up2 = ImageIO.read(getClass().getResourceAsStream("/asset/Player/Up1.png"));
            down1 = ImageIO.read(getClass().getResourceAsStream("/asset/Player/Down.png"));
            down2 = ImageIO.read(getClass().getResourceAsStream("/asset/Player/Down1.png"));
            left1 = ImageIO.read(getClass().getResourceAsStream("/asset/Player/Left.png"));
            left2 = ImageIO.read(getClass().getResourceAsStream("/asset/Player/Left1.png"));
            right1 = ImageIO.read(getClass().getResourceAsStream("/asset/Player/Right.png"));
            right2 = ImageIO.read(getClass().getResourceAsStream("/asset/Player/Right1.png"));
        }catch(IOException | NullPointerException e){
            e.printStackTrace();
            System.out.println("Failed to load player images. Check resource paths!");
        }
    }

    public void update() {
        boolean moving = false;
        // untuk menghitung perubahan posisi berdasarkan input
        int dx = 0;
        int dy = 0;

        if(keyH.upPressed) { // ke atas
            dy -= speed;
            moving = true;
        }
        if(keyH.downPressed) { // ke bawah
            dy += speed;
            moving = true;
        }
        if(keyH.leftPressed) { // ke kiri
            dx -= speed;
            moving = true;
        }
        if(keyH.rightPressed) { // ke kanan
            dx += speed;
            moving = true;
        }

        // Normalisasi kecepatan diagonal agar gak lebih cepat saat bergerak diagonal
        if(dx != 0 && dy != 0){
            dx = (int)(dx / Math.sqrt(1.5));
            dy = (int)(dy / Math.sqrt(1.5));
        }

        // Update posisi pemain
        x += dx;
        y += dy;

        // Update arah berdasarkan input
        if(dx != 0) direction = (dx > 0) ? "right" : "left";
        else if(dy != 0) direction = (dy > 0) ? "down" : "up";

        // animasi sprite hanya jika pemain bergerak
        if(moving){
            spriteCounter++;
            if(spriteCounter > 6){
                spriteNum = (spriteNum == 1) ? 2 : 1;
                spriteCounter = 0;
            }
        }
    }

    public void draw(Graphics2D g2){
        BufferedImage image = null;

        switch(direction){
            case "up":    image = (spriteNum==1)? up1 : up2; break;
            case "down":  image = (spriteNum==1)? down1 : down2; break;
            case "left":  image = (spriteNum==1)? left1 : left2; break;
            case "right": image = (spriteNum==1)? right1 : right2; break;
        }

        //g2.drawImage(image, x, y, gp.tileSize, gp.tileSize, null);
        sprite.drawSprite(g2, image, x, y, gp.tileSize, gp.tileSize); // sprite player

    }
}