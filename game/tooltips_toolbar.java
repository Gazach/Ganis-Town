package game;

import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import system.panel;

public class tooltips_toolbar {

    private panel skin;
    private Font font;
    private BufferedImage coinImage;

    private static final int PADDING_X = 10;
    private static final int PADDING_Y  = 6;
    private static final int LINE_GAP   = 3;
    private static final int COIN_SIZE  = 14;
    private static final int COIN_GAP   = 4;

    public tooltips_toolbar() {
        skin = new panel("/asset/Panel/butmore");
        loadFont();
        loadCoinImage();
    }

    private void loadFont() {
        String fontPath = "/asset/font/terminal-grotesque.grotesque-regular.ttf";
        try (InputStream stream = getClass().getResourceAsStream(fontPath)) {
            if (stream != null) {
                font = Font.createFont(Font.TRUETYPE_FONT, stream).deriveFont(Font.BOLD, 13f);
                return;
            }
        } catch (IOException | FontFormatException e) { /* fall through */ }

        String filePath = fontPath.startsWith("/") ? fontPath.substring(1) : fontPath;
        File file = new File(filePath);
        if (file.exists()) {
            try {
                font = Font.createFont(Font.TRUETYPE_FONT, file).deriveFont(Font.BOLD, 13f);
                return;
            } catch (IOException | FontFormatException e) { /* fall through */ }
        }
        font = new Font("Dialog", Font.BOLD, 13);
    }

    private void loadCoinImage() {
        try {
            InputStream stream = getClass().getResourceAsStream("/asset/gameplayUI/coin.png");
            if (stream != null) { coinImage = ImageIO.read(stream); return; }
            File file = new File("asset/gameplayUI/coin.png");
            if (file.exists()) { coinImage = ImageIO.read(file); }
        } catch (IOException e) { /* coin optional */ }
    }

    /**
     * Draw the tooltip centered on buttonCenterX, just above buttonTopY.
     */
    public void draw(Graphics2D g2, BuildingType building, int buttonCenterX, int buttonTopY, int screenWidth) {
        if (building == null) return;

        Font prevFont = g2.getFont();
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();

        // Building name: capitalise first letter
        String rawName = building.name().toLowerCase().replace('_', ' ');
        String name = rawName.isEmpty() ? rawName
            : Character.toUpperCase(rawName.charAt(0)) + rawName.substring(1);
        String priceStr = formatMoney(building.getPrice());
        boolean isPath = building.getCategory() == BuildingType.BuildingCategory.PATH;
        if (isPath) priceStr += " /tile";

        int nameW  = fm.stringWidth(name);
        int coinSlot = (coinImage != null) ? COIN_SIZE + COIN_GAP : 0;
        int priceW = coinSlot + fm.stringWidth(priceStr);
        int contentW  = Math.max(nameW, priceW);

        int lineH    = fm.getAscent() + fm.getDescent();
        int tooltipW = contentW + PADDING_X * 2;
        int tooltipH = PADDING_Y * 2 + lineH * 2 + LINE_GAP;

        // Position centered above button
        int tooltipX = buttonCenterX - tooltipW / 2;
        int tooltipY = buttonTopY - tooltipH - 6;

        // Clamp to screen edges
        tooltipX = Math.max(4, Math.min(tooltipX, screenWidth - tooltipW - 4));

        skin.draw(g2, tooltipX, tooltipY, tooltipW, tooltipH);

        int textX  = tooltipX + PADDING_X;
        int nameY  = tooltipY + PADDING_Y + fm.getAscent();
        int priceY = nameY + lineH + LINE_GAP;

        drawStrokedText(g2, name, textX, nameY, Color.WHITE, new Color(8, 8, 8, 220), 1.5f);

        int priceTextX = textX;
        if (coinImage != null) {
            int coinY = priceY - (fm.getAscent() + COIN_SIZE) / 2;
            g2.drawImage(coinImage, priceTextX, coinY, COIN_SIZE, COIN_SIZE, null);
            priceTextX += COIN_SIZE + COIN_GAP;
        }
        drawStrokedText(g2, priceStr, priceTextX, priceY, new Color(255, 220, 60), new Color(8, 8, 8, 220), 1.5f);

        g2.setFont(prevFont);
    }

    private String formatMoney(long amount) {
        if (amount >= 1_000_000L) return String.format("%.1fmil", amount / 1_000_000.0);
        if (amount >= 1_000L)     return String.format("%.1fk",   amount / 1_000.0);
        return String.valueOf(amount);
    }

    private void drawStrokedText(Graphics2D g2, String text, int x, int y,
                                  Color fillColor, Color strokeColor, float strokeWidth) {
        if (text == null || text.isEmpty()) return;
        TextLayout layout = new TextLayout(text, g2.getFont(), g2.getFontRenderContext());
        Shape outline = layout.getOutline(AffineTransform.getTranslateInstance(x, y));
        Color oldColor   = g2.getColor();
        Stroke oldStroke = g2.getStroke();
        g2.setColor(strokeColor);
        g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(outline);
        g2.setColor(fillColor);
        g2.fill(outline);
        g2.setColor(oldColor);
        g2.setStroke(oldStroke);
    }
}
