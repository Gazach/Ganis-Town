package system.entity;

import java.awt.Graphics2D;
import java.io.IOException;

import javax.imageio.ImageIO;

import system.KeyHandler;

import java.awt.Color;

import system.Entity;
import system.GamePanel;
import java.awt.image.BufferedImage;

public class Player extends Entity {
    GamePanel gp;
    KeyHandler keyH;
    
    
    public Player(GamePanel gp, KeyHandler keyH) {
        this.gp = gp;
        this.keyH = keyH;

        setDefaultValues();
        getPlayerImage();

    }
    public void setDefaultValues(){
        x = 100;
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


        }catch(IOException e){
            e.printStackTrace();
        }
    }
        
    
    public void update(){// buat bikin logika update game di sini, misalnya untuk menggerakkan karakter
        if(keyH.upPressed == true || keyH.downPressed == true || 
            keyH.leftPressed == true || keyH.rightPressed == true){//biar karakter gak bergerak kalau tidak disentuh
                
		if(keyH.upPressed == true){
            direction = "up";
			y -= speed; //di java kalo Y nurun(ke negatif) Y valuenya nambah
									//jadinya karakter ke atas
		}
		else if(keyH.downPressed == true) {
            direction = "down";
			y += speed;
		}
		else if(keyH.leftPressed == true){//kalo ke kanan jadinya nambah kalo X
            direction = "left";
			x -= speed;
		}
		else if(keyH.rightPressed == true) {
            direction = "right";
			x += speed;
		}
        spriteCounter++; //Perubahan sprite setiap 12 frames
        if (spriteCounter > 12){
            if(spriteNum == 1){
                spriteNum = 2;
            }
            else if (spriteNum == 2){
                spriteNum = 1;
            }
            spriteCounter = 0;
        }    
    }

    }
    public void draw(Graphics2D g2){
        BufferedImage image = null;

        switch(direction){
        case "up":
            if(spriteNum == 1){
                image = up1;
            }
            if (spriteNum ==2){
                image = up2;
            }
            break;
        case "down":
            if(spriteNum == 1){
                image = down1;
            }
            if(spriteNum == 2){
                image = down2;
            }
            break;
        case "left":
            if(spriteNum == 1){
                image = left1;
            }
            if (spriteNum == 2){
                image = left2;
            }
            break;
        case "right":
            if(spriteNum == 1){
                image = right1;
            }
            if(spriteNum == 2){
                image = right2;
            }
            break;


        }
        g2.drawImage(image, x, y, gp.tileSize,gp.tileSize, null);
    }
}
