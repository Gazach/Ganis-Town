package game;

public enum BuildingType {
    // bangunan dan ukurannya (width x height dalam tile)
    // tinggal tambahin bangunan baru di enum ini, terus buat gambar normal dan hover nya di Toolbar.java
    // dan button otomatis akan muncul di toolbar, tinggal atur urutannya aja di enum ini
    //
    // Kategori:
    //   HOUSING  → bangunan tempat tinggal, memberikan populasi (minPeople–maxPeople) per unit, incomePerSecond = 0
    //   PRODUCTION → bangunan produksi, menghasilkan uang per detik, minPeople/maxPeople = 0
    //
    //                          w  h    price   category                minP maxP maxW income/s
    HOUSE(          1, 1,   500, BuildingCategory.HOUSING,    2,   4,  0,  0),
    BUILDING_2X2(   2, 2,  1500, BuildingCategory.PRODUCTION, 0,   0,  4, 10),
    BUILDING_2X4(   2, 4,  3000, BuildingCategory.PRODUCTION, 0,   0,  6, 25);

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

    BuildingType(int width, int height, int price, BuildingCategory category,
                 int minPeople, int maxPeople, int maxWorkers, int incomePerSecond) {
        this.width          = width;
        this.height         = height;
        this.price          = price;
        this.category       = category;
        this.minPeople      = minPeople;
        this.maxPeople      = maxPeople;
        this.maxWorkers     = maxWorkers;
        this.incomePerSecond = incomePerSecond;
    }

    public int getWidth()            { return width; }
    public int getHeight()           { return height; }
    public int getPrice()            { return price; }
    public BuildingCategory getCategory() { return category; }
    public int getMinPeople()        { return minPeople; }
    public int getMaxPeople()        { return maxPeople; }
    public int getMaxWorkers()       { return maxWorkers; }
    public int getIncomePerSecond()  { return incomePerSecond; }
}
