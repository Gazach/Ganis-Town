package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import system.Button;
import game.gamestate.Gameplay;

public class Toolbar {
    Button button = new Button();
    
    BufferedImage[] btnNormal;
    BufferedImage[] btnHover;
    
    private BuildingType selectedBuilding = null;
    
    int toolbarHeight = 80;
    int btnSize = 64;
    int btnPadding = 10;

    public Toolbar() {
        loadImages(); 
    }

    private void loadImages() {
        int total = BuildingType.values().length;
        btnNormal = new BufferedImage[total];
        btnHover  = new BufferedImage[total];

        try {
            BufferedImage normal = ImageIO.read(getClass().getResourceAsStream("/asset/Toolbar/house_normal.png"));
            BufferedImage hover = ImageIO.read(getClass().getResourceAsStream("/asset/Toolbar/house_hover.png"));

            for (int i = 0; i < total; i++) {
                btnNormal[i] = normal;
                btnHover[i] = hover;
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    
    public void update(int mouseX, int mouseY) {
        // Reserved for hover/animation updates if needed.
    }

    public void draw(Graphics2D g2, int screenWidth, int screenHeight, int mouseX, int mouseY, Gameplay gameplay) {
        g2.setColor(new Color(50, 50, 50, 254)); // semi-transparent dark background
        g2.fillRect(0, screenHeight - toolbarHeight, screenWidth, toolbarHeight);
        
        for (int i = 0; i < BuildingType.values().length; i++) {
            int x = btnPadding + i * (btnSize + btnPadding);
            int y = screenHeight - toolbarHeight + 8;
            
            button.drawButton(g2, btnNormal[i], btnHover[i], x, y, btnSize, btnSize, mouseX, mouseY);
        }
    }

    public void handleClick(int mouseX, int mouseY, int screenHeight, Gameplay gameplay) {
        BuildingType clicked = getClickedBuilding(mouseX, mouseY, screenHeight);
        if (clicked != null) {
            boolean isSameSelection = (selectedBuilding == clicked);
            if (isSameSelection) {
                selectedBuilding = null;
                if (gameplay.isGridVisible()) {
                    gameplay.toggleGrid();
                }
            } else {
                selectedBuilding = clicked;
                if (!gameplay.isGridVisible()) {
                    gameplay.toggleGrid();
                }
            }
        }
    }

    public BuildingType getSelectedBuilding() {
        return selectedBuilding;
    }

    public BufferedImage getBuildingImage(BuildingType building) {
        if (building == null) {
            return null;
        }
        return btnNormal[building.ordinal()];
    }

    public boolean isInsideToolbar(int mouseY, int screenHeight) {
        return mouseY >= screenHeight - toolbarHeight;
    }
    
    public BuildingType getClickedBuilding(int mouseX, int mouseY, int screenHeight) {
        for (int i = 0; i < BuildingType.values().length; i++) {
            int x = btnPadding + i * (btnSize + btnPadding);
            int y = screenHeight - toolbarHeight + 8;
            
            if (button.isHovering(x, y, btnSize, btnSize, mouseX, mouseY)) {
                return BuildingType.values()[i];
            }
        }
        return null;
    }
}