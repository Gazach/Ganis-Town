package game;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import system.Button;
import system.panel;
import game.gamestate.Gameplay;

public class Toolbar {
    Button button = new Button();
    
    BufferedImage[] btnNormal;
    BufferedImage[] btnHover;
    
    private BuildingType selectedBuilding = null;
    private panel toolbarPanel;
    private tooltips_toolbar tooltip;
    
    int toolbarHeight = 80;
    int btnSize = 64;
    int btnPadding = 10;

    public Toolbar() {
        toolbarPanel = new panel();
        tooltip = new tooltips_toolbar();
        loadImages(); 
    }

    private void loadImages() {
        int total = BuildingType.values().length;
        btnNormal = new BufferedImage[total];
        btnHover  = new BufferedImage[total];

        // Urutan HARUS sama dengan urutan enum di BuildingType.java
        String[] normalImages = {
            "/asset/Toolbar/house_normal.png",        // HOUSE
            "/asset/Toolbar/building2x2_normals.png",  // BUILDING_2X2
            "/asset/Toolbar/building2x4_normal.png"   // BUILDING_2X4
        };

        String[] hoverImages = {
            "/asset/Toolbar/house_hover.png",
            "/asset/Toolbar/building2x2_hover.png",
            "/asset/Toolbar/building2x4_hover.png"
        };

        for (int i = 0; i < total; i++) {
            btnNormal[i] = loadImage(normalImages[i]);
            btnHover[i] = loadImage(hoverImages[i]);
        }
    }

    private BufferedImage loadImage(String resourcePath) {
        try (InputStream stream = getClass().getResourceAsStream(resourcePath)) {
            if (stream != null) {
                return ImageIO.read(stream);
            }
        } catch (IOException e) {
            throw new RuntimeException("Gagal load resource classpath: " + resourcePath, e);
        }

        String filePath = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalStateException("Gambar toolbar tidak ditemukan: " + resourcePath
                + " (classpath) dan " + file.getPath() + " (filesystem)");
        }

        try {
            return ImageIO.read(file);
        } catch (IOException e) {
            throw new RuntimeException("Gagal load gambar dari filesystem: " + file.getPath(), e);
        }
    }


    
    public void update(int mouseX, int mouseY) {
        // Reserved for hover/animation updates if needed.
    }

    public void draw(Graphics2D g2, int screenWidth, int screenHeight, int mouseX, int mouseY, Gameplay gameplay) {
        toolbarPanel.draw(g2, 0, screenHeight - toolbarHeight, screenWidth, toolbarHeight);
        
        for (int i = 0; i < BuildingType.values().length; i++) {
            int x = btnPadding + i * (btnSize + btnPadding);
            int y = screenHeight - toolbarHeight + 8;
            
            button.drawButton(g2, btnNormal[i], btnHover[i], x, y, btnSize, btnSize, mouseX, mouseY);
        }

        // Draw tooltip for whichever button is currently hovered
        BuildingType[] buildings = BuildingType.values();
        for (int i = 0; i < buildings.length; i++) {
            int x = btnPadding + i * (btnSize + btnPadding);
            int y = screenHeight - toolbarHeight + 8;
            if (button.isHovering(x, y, btnSize, btnSize, mouseX, mouseY)) {
                tooltip.draw(g2, buildings[i], x + btnSize / 2, y, screenWidth);
                break;
            }
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

    public void clearSelection() {
        selectedBuilding = null;
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