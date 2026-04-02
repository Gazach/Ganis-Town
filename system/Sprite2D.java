package system;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.Color;

public class Sprite2D {

    public void drawSprite(Graphics2D g2, BufferedImage image, int posx, int posy, int sizeX, int sizeY){
        if(image != null){
            g2.drawImage(image, posx, posy, sizeX, sizeY, null);
        } else {
            // fallback rectangle if image fails
            g2.setColor(Color.RED);
            g2.fillRect(posx, posy, sizeX, sizeY);
        }
    }
}
