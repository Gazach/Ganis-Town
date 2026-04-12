package system;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Button {
    Sprite2D sprite = new Sprite2D();

    public boolean isHovering(int x, int y, int width, int height, int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void drawButton(Graphics2D g2, BufferedImage image_main, BufferedImage image_hover, int x, int y, int width, int height, int mouseX, int mouseY) {
        boolean isHover = isHovering(x, y, width, height, mouseX, mouseY);
        if (isHover) {
            sprite.drawSprite(g2, image_hover, x, y, width, height);
        } else {
            sprite.drawSprite(g2, image_main, x, y, width, height);
        }
    }

    public void drawButton(Graphics2D g2, BufferedImage image_main, BufferedImage image_hover, BufferedImage image_clicked, int x, int y, int width, int height, int mouseX, int mouseY, boolean isClicked) {
        boolean isHover = isHovering(x, y, width, height, mouseX, mouseY);
        if (isClicked) {
            sprite.drawSprite(g2, image_clicked, x, y, width, height);
        } else if (isHover) {
            sprite.drawSprite(g2, image_hover, x, y, width, height);
        } else {
            sprite.drawSprite(g2, image_main, x, y, width, height);
        }
    }

    public void drawButton(Graphics2D g2, BufferedImage image_main, BufferedImage image_hover, int x, int y, int width, int height, boolean isHover) {
        if (isHover) {
            sprite.drawSprite(g2, image_hover, x, y, width, height);
        } else {
            sprite.drawSprite(g2, image_main, x, y, width, height);
        }
    }
}
