package game;

import java.util.HashMap;
import java.util.Map;

public class playerUpgrade {

    private static final int MAX_LEVEL = 10;

    /** Upgrade levels for production buildings (excluding SHOP). */
    private final Map<BuildingType, Integer> levels = new HashMap<>();

    /** SHOP sell-capacity upgrade level: max crops doubles each level (500 × 2^level). */
    private int shopCapLevel  = 0;

    /** SHOP coin-rate upgrade level: coins per crop multiplied by 1.7 each level (0.5 × 1.7^level). */
    private int shopRateLevel = 0;

    // ── Production buildings ──────────────────────────────────────────────────

    public int getLevel(BuildingType type) {
        return levels.getOrDefault(type, 0);
    }

    public boolean canUpgrade(BuildingType type) {
        return getLevel(type) < MAX_LEVEL;
    }

    public void upgrade(BuildingType type) {
        int cur = getLevel(type);
        if (cur < MAX_LEVEL) levels.put(type, cur + 1);
    }

    /** Cost: floor(0.7 × price × (currentLevel+1)!) */
    public static long getUpgradeCost(BuildingType type, int currentLevel) {
        return (long)(0.7 * type.getPrice() * factorial(currentLevel + 1));
    }

    /** Production multiplier: 1.7^level (level 0 = 1.0×, level 1 = 1.7×, level 10 ≈ 20×). */
    public static double getProductionMultiplier(int level) {
        return Math.pow(1.7, level);
    }

    // ── SHOP capacity upgrade ─────────────────────────────────────────────────

    public int  getShopCapLevel()       { return shopCapLevel; }
    public boolean canUpgradeShopCap()  { return shopCapLevel  < MAX_LEVEL; }
    public void upgradeShopCap()        { if (shopCapLevel  < MAX_LEVEL) shopCapLevel++; }

    /** Max crops a single SHOP sells per 5s tick: 500 × 2^capLevel. */
    public static int getShopMaxCrops(int capLevel) {
        return (int)(500 * Math.pow(2, capLevel));
    }

    public static long getShopCapUpgradeCost(int currentLevel) {
        return (long)(0.7 * BuildingType.SHOP.getPrice() * factorial(currentLevel + 1));
    }

    // ── SHOP coin-rate upgrade ────────────────────────────────────────────────

    public int  getShopRateLevel()      { return shopRateLevel; }
    public boolean canUpgradeShopRate() { return shopRateLevel < MAX_LEVEL; }
    public void upgradeShopRate()       { if (shopRateLevel < MAX_LEVEL) shopRateLevel++; }

    /** Coins earned per crop sold: 0.5 × 1.7^rateLevel. */
    public static double getShopCoinRate(int rateLevel) {
        return 0.5 * Math.pow(1.7, rateLevel);
    }

    public static long getShopRateUpgradeCost(int currentLevel) {
        return (long)(0.7 * BuildingType.SHOP.getPrice() * factorial(currentLevel + 1));
    }

    // ── Common ────────────────────────────────────────────────────────────────

    public static int getMaxLevel() { return MAX_LEVEL; }

    public void clear() {
        levels.clear();
        shopCapLevel  = 0;
        shopRateLevel = 0;
    }

    private static long factorial(int n) {
        if (n <= 1) return 1;
        long r = 1;
        for (int i = 2; i <= n; i++) r *= i;
        return r;
    }
}
