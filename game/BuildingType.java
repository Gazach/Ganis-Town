package game;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public enum BuildingType {
    // bangunan dan ukurannya (width x height dalam tile)
    // tinggal tambahin bangunan baru di enum ini, terus buat gambar normal dan hover nya di Toolbar.java
    // dan button otomatis akan muncul di toolbar, tinggal atur urutannya aja di enum ini
    //
    // Kategori:
    //   HOUSING  → bangunan tempat tinggal, memberikan populasi (minPeople–maxPeople) per unit, incomePerSecond = 0
    //   PRODUCTION → bangunan produksi, menghasilkan uang per detik, minPeople/maxPeople = 0
    //
    //                          w  h    price   category                minP maxP maxW income/s  animFrameCount  animSpeed  assetName
    HOUSE(          1, 1,   500, BuildingCategory.HOUSING,    2,   4,  0,  0,  0,  1,  "building_1x1"),
    WHEAT(          2, 2,   800, BuildingCategory.HOUSING,    4,   8,  0,  0,  5,  12, "wheat"),
    BUILDING_2X2(   2, 2,  1500, BuildingCategory.PRODUCTION, 0,   0,  4, 10,  5,  12, "building_2x2"),  // 60/6 = 10 fps
    WINDMILL(       2, 3,  3000, BuildingCategory.PRODUCTION, 0,   0,  6, 25,  3,  6,  "windmill"),       // 60/6 = 10 fps
    BARN(           2, 4,  2000, BuildingCategory.PRODUCTION, 0,   0,  4, 10,  5,  12, "barn");
    // -------------------------------------------------------------------------
    public enum BuildingCategory {
        HOUSING,
        PRODUCTION
    }

    // -------------------------------------------------------------------------
    private final int width;
    private final int height;
    private final int price;
    private final BuildingCategory category;
    private final int minPeople;
    private final int maxPeople;
    private final int maxWorkers;
    private final int incomePerSecond;
    private final int animationFrameCount;
    private final int animationSpeed;
    private final String assetName;
    private BufferedImage[] animationFrames;

    BuildingType(int width, int height, int price, BuildingCategory category,
                 int minPeople, int maxPeople, int maxWorkers, int incomePerSecond, int animationFrameCount, int animationSpeed, String assetName) {
        this.width          = width;
        this.height         = height;
        this.price          = price;
        this.category       = category;
        this.minPeople      = minPeople;
        this.maxPeople      = maxPeople;
        this.maxWorkers     = maxWorkers;
        this.incomePerSecond = incomePerSecond;
        this.animationFrameCount = animationFrameCount;
        this.animationSpeed = animationSpeed;
        this.assetName      = assetName;
        this.animationFrames = new BufferedImage[animationFrameCount];
        loadAnimationFrames();
    }

    private void loadAnimationFrames() { //buat animasi bangunan bisa di load
        String baseName = this.assetName;
        for (int i = 0; i < animationFrameCount; i++) {
            try {
                String resourcePath = "/asset/Buildings/" + baseName + "_frame" + (i + 1) + ".png";
                var inputStream = BuildingType.class.getResourceAsStream(resourcePath);
                if (inputStream != null) {
                    this.animationFrames[i] = ImageIO.read(inputStream);
                    System.out.println("✓ Loaded animation frame: " + resourcePath);
                } else {
                    this.animationFrames[i] = null;
                    System.out.println("✗ Animation frame not found: " + resourcePath);
                }
            } catch (IOException | IllegalArgumentException e) {
                System.out.println("✗ Error loading animation frame for " + baseName + " frame " + (i + 1) + ": " + e.getMessage());
                this.animationFrames[i] = null;
            }
        }
    }

    public int getWidth()            { return width; }
    public int getHeight()           { return height; }
    public int getPrice()            { return price; }
    public BuildingCategory getCategory() { return category; }
    public int getMinPeople()        { return minPeople; }
    public int getMaxPeople()        { return maxPeople; }
    public int getMaxWorkers()       { return maxWorkers; }
    public int getIncomePerSecond()  { return incomePerSecond; }
    public int getAnimationFrameCount() { return animationFrameCount; }
    public int getAnimationSpeed()       { return animationSpeed; }
    public String getAssetName()         { return assetName; }
    public BufferedImage[] getAnimationFrames() { return animationFrames; }
}
