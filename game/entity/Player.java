package game.entity;

import java.awt.Graphics2D;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import game.gamestate.Gameplay;
import system.Entity;
import system.KeyHandler;
import system.Sprite2D;

public class Player extends Entity {

    Gameplay gp;
    Sprite2D sprite = new Sprite2D();
    KeyHandler keyH;

    public final int screenX;
    public final int screenY;

    public Player(Gameplay gp, KeyHandler keyH) {
        this.gp = gp;
        this.keyH = keyH;
        screenX = gp.screenWidth/2 - (gp.tileSize/2);
        screenY = gp.screenHeight/2 - (gp.tileSize/2);

        setDefaultValues();
        getPlayerImage();
    }

    public void setDefaultValues(){
        worldX = gp.tileSize * 5;
        worldY = gp.tileSize * 5;
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
        // Animation for sprite (can be based on movement, but for drag, perhaps always still or detect change)
        // For now, keep sprite static or add logic later
        // Since movement is handled by drag in Gameplay, no keyboard input here
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
        sprite.drawSprite(g2, image, screenX, screenY, gp.tileSize, gp.tileSize); // sprite player

    }
}