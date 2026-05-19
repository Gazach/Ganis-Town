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
    // Only buildings shown in the toolbar (DECORATION buildings are excluded)
    private BuildingType[] toolbarBuildings;
    
    private BuildingType selectedBuilding = null;
    private panel toolbarPanel;
    private tooltips_toolbar tooltip;
    private BufferedImage trashNormal, trashHover, trashPress;
    private boolean isDeleteMode = false;
    private int scrollOffset = 0;
    
    int toolbarHeight = 80;
    int btnSize = 64;
    int btnPadding = 10;

    public Toolbar() {
        toolbarPanel = new panel();
        tooltip = new tooltips_toolbar();
        initToolbarBuildings();
        loadImages(); 
    }

    /** All buildings shown in the toolbar (including DECORATION/trees). */
    private void initToolbarBuildings() {
        toolbarBuildings = BuildingType.values();
    }

    private void loadImages() {
        int total = toolbarBuildings.length;
        btnNormal = new BufferedImage[total];
        btnHover  = new BufferedImage[total];

        // Order MUST match BuildingType.values() order exactly
        String[] normalImages = {
            "/asset/Toolbar/house_normal.png",           // HOUSE
            "/asset/Toolbar/WheatFarm_normal.png",       // WHEAT
            "/asset/Toolbar/corn_normal.png",            // CORN
            "/asset/Toolbar/tomato_normal.png",          // TOMATO
            "/asset/Toolbar/pumpkin_normal.png",         // PUMPKIN
            "/asset/Toolbar/carrot_normal.png",          // CARROT
            "/asset/Toolbar/building2x2_normal.png",     // BUILDING_2X2
            "/asset/Toolbar/building2x3_normal.png",     // WINDMILL
            "/asset/Toolbar/barn_normal.png",            // BARN
            "/asset/RoadPath/Road.png",                  // ROAD
            "/asset/Toolbar/shop_normal.png",            // SHOP
            "/asset/Toolbar/upgradeBuilding_normal.png", // UPGRADE_BUILDING
            "/asset/Toolbar/house2_normal.png",          // HOUSE2
            "/asset/Buildings/tree1_frame1.png",         // TREE1
            "/asset/Buildings/tree2_frame1.png",         // TREE2
            "/asset/Buildings/tree3_frame1.png"          // TREE3
        };

        String[] hoverImages = {
            "/asset/Toolbar/house_hover.png",            // HOUSE
            "/asset/Toolbar/WheatFarm_hover.png",        // WHEAT
            "/asset/Toolbar/corn_hover.png",             // CORN
            "/asset/Toolbar/tomato_hover.png",           // TOMATO
            "/asset/Toolbar/pumpkin_hover.png",          // PUMPKIN
            "/asset/Toolbar/carrot_hover.png",           // CARROT
            "/asset/Toolbar/building2x2_hover.png",      // BUILDING_2X2
            "/asset/Toolbar/building2x3_hover.png",      // WINDMILL
            "/asset/Toolbar/barn_hover.png",             // BARN
            "/asset/RoadPath/Road.png",                  // ROAD
            "/asset/Toolbar/shop_hover.png",             // SHOP
            "/asset/Toolbar/upgradeBuilding_hover.png",  // UPGRADE_BUILDING
            "/asset/Toolbar/house2_hover.png",           // HOUSE2
            "/asset/Buildings/tree1_frame1.png",         // TREE1
            "/asset/Buildings/tree2_frame1.png",         // TREE2
            "/asset/Buildings/tree3_frame1.png"          // TREE3
        };

        for (int i = 0; i < total; i++) {
            btnNormal[i] = loadImage(normalImages[i]);
            btnHover[i] = loadImage(hoverImages[i]);
        }
        try {
            trashNormal = loadImage("/asset/gameplayUI/Trash_button.png");
            trashHover  = loadImage("/asset/gameplayUI/Trash_button_hover.png");
            trashPress  = loadImage("/asset/gameplayUI/Trash_button_press.png");
        } catch (Exception e) {
            System.out.println("Failed to load trash button images: " + e.getMessage());
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

        // Clip buttons to the toolbar rectangle so they don't bleed outside
        java.awt.Shape oldClip = g2.getClip();
        g2.setClip(0, screenHeight - toolbarHeight, screenWidth, toolbarHeight);

        for (int i = 0; i < toolbarBuildings.length; i++) {
            int x = btnPadding + i * (btnSize + btnPadding) - scrollOffset;
            int y = screenHeight - toolbarHeight + 8;
            if (x + btnSize < 0 || x > screenWidth) continue; // skip fully off-screen
            button.drawButton(g2, btnNormal[i], btnHover[i], x, y, btnSize, btnSize, mouseX, mouseY);
        }

        g2.setClip(oldClip);

        // Scroll-edge shadow hints
        int totalWidth = toolbarBuildings.length * (btnSize + btnPadding) + btnPadding;
        int maxScroll = Math.max(0, totalWidth - screenWidth);
        if (scrollOffset > 0) drawScrollArrow(g2, screenWidth, screenHeight, false);          // left arrow
        if (scrollOffset < maxScroll) drawScrollArrow(g2, screenWidth, screenHeight, true);   // right arrow

        // Draw trash/delete button — above toolbar, right side
        int trashX = screenWidth - btnSize - btnPadding;
        int trashY = screenHeight - toolbarHeight - btnSize - btnPadding;
        if (trashNormal != null) {
            button.drawButton(g2,
                trashNormal,
                trashHover != null ? trashHover : trashNormal,
                trashPress != null ? trashPress : trashNormal,
                trashX, trashY, btnSize, btnSize, mouseX, mouseY, isDeleteMode);
        }

        // Draw tooltip for whichever visible button is currently hovered
        for (int i = 0; i < toolbarBuildings.length; i++) {
            int x = btnPadding + i * (btnSize + btnPadding) - scrollOffset;
            int y = screenHeight - toolbarHeight + 8;
            if (x < 0 || x + btnSize > screenWidth) continue; // skip partially hidden
            if (button.isHovering(x, y, btnSize, btnSize, mouseX, mouseY)) {
                tooltip.draw(g2, toolbarBuildings[i], x + btnSize / 2, y, screenWidth);
                break;
            }
        }
    }

    /** Draw a small arrow indicator at the left or right edge of the toolbar. */
    private void drawScrollArrow(Graphics2D g2, int screenWidth, int screenHeight, boolean right) {
        int ay = screenHeight - toolbarHeight;
        int midY = ay + toolbarHeight / 2;
        int halfH = 10;
        // Left arrow near x=12, right arrow near x=screenWidth-12
        int cx = right ? screenWidth - 12 : 12;
        int[] xp = right ? new int[]{cx - 8, cx - 8, cx} : new int[]{cx + 8, cx + 8, cx};
        int[] yp = {midY - halfH, midY + halfH, midY};
        g2.setColor(new java.awt.Color(255, 255, 255, 180));
        g2.fillPolygon(xp, yp, 3);
        g2.setColor(new java.awt.Color(0, 0, 0, 80));
        g2.drawPolygon(xp, yp, 3);
    }

    public void handleClick(int mouseX, int mouseY, int screenHeight, Gameplay gameplay) {
        BuildingType clicked = getClickedBuilding(mouseX, mouseY, screenHeight);
        if (clicked != null) {
            isDeleteMode = false; // exit delete mode when a building is selected
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
        isDeleteMode = false;
    }

    public boolean isDeleteMode() { return isDeleteMode; }

    public boolean handleDeleteButtonClick(int mouseX, int mouseY, int screenWidth, int screenHeight, Gameplay gameplay) {
        int x = screenWidth - btnSize - btnPadding;
        int y = screenHeight - toolbarHeight - btnSize - btnPadding;
        if (!button.isHovering(x, y, btnSize, btnSize, mouseX, mouseY)) return false;
        isDeleteMode = !isDeleteMode;
        if (isDeleteMode) {
            selectedBuilding = null;
            if (!gameplay.isGridVisible()) gameplay.toggleGrid();
        } else {
            if (gameplay.isGridVisible()) gameplay.toggleGrid();
        }
        return true;
    }

    public BufferedImage getBuildingImage(BuildingType building) {
        if (building == null) return null;
        for (int i = 0; i < toolbarBuildings.length; i++) {
            if (toolbarBuildings[i] == building) return btnNormal[i];
        }
        return null;
    }

    public boolean isInsideToolbar(int mouseY, int screenHeight) {
        return mouseY >= screenHeight - toolbarHeight;
    }
    
    public BuildingType getClickedBuilding(int mouseX, int mouseY, int screenHeight) {
        for (int i = 0; i < toolbarBuildings.length; i++) {
            int x = btnPadding + i * (btnSize + btnPadding) - scrollOffset;
            int y = screenHeight - toolbarHeight + 8;
            if (button.isHovering(x, y, btnSize, btnSize, mouseX, mouseY)) {
                return toolbarBuildings[i];
            }
        }
        return null;
    }

    /**
     * Scroll the toolbar horizontally.
     * @param delta positive = scroll right (content moves left), negative = scroll left
     * @param screenWidth current screen/panel width for clamping
     */
    public void handleScroll(int delta, int screenWidth) {
        int totalWidth = toolbarBuildings.length * (btnSize + btnPadding) + btnPadding;
        int maxScroll = Math.max(0, totalWidth - screenWidth);
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset + delta * (btnSize + btnPadding)));
    }
}