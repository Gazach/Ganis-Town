package system;

import game.BuildingInstance;
import game.BuildingType;

/**
 * Handles environmental decoration placement during world generation.
 * Currently places trees (TREE1/TREE2/TREE3) on grass tiles using Perlin noise
 * to create natural-looking clusters.
 */
public class envMapGeneration {

    private static final BuildingType[] TREE_TYPES = {
        BuildingType.TREE1, BuildingType.TREE2, BuildingType.TREE3
    };

    /**
     * Randomly places trees across the world map after terrain has been generated.
     *
     * @param buildingDataMap  the per-tile building instance grid (modified in-place)
     * @param buildingsMap     the per-tile building type grid (modified in-place)
     * @param worldMap         tile type map (0=grass, 2=water)
     * @param maxCols          world width in tiles
     * @param maxRows          world height in tiles
     * @param seed             seed used for noise (pass the same seed as terrain generation)
     */
    public static void placeTrees(
            BuildingInstance[][] buildingDataMap,
            BuildingType[][]     buildingsMap,
            boolean[][]          buildingOccupiedMap,
            int[][]              worldMap,
            int maxCols, int maxRows,
            long seed) {

        // Use an offset seed so tree noise is independent from terrain noise
        PerlinNoise noise = new PerlinNoise(seed + 9_999_991L);
        java.util.Random rng = new java.util.Random(seed);

        for (int x = 0; x < maxCols; x++) {
            for (int y = 0; y < maxRows; y++) {
                // Only place on empty grass tiles
                if (worldMap[x][y] != 0) continue;
                if (buildingDataMap[x][y] != null) continue;

                // Noise drives clustering (~40% of grass in "forest" regions)
                double density = noise.octaveNoise(x * 0.12, y * 0.12, 3, 0.5);
                if (density < 0.15) continue;

                // Additional random thinning within dense regions (~35% of eligible)
                if (rng.nextDouble() > 0.35) continue;

                BuildingType treeType = TREE_TYPES[rng.nextInt(TREE_TYPES.length)];

                // Check ALL tiles in the tree's footprint are in-bounds, empty grass
                int tw = treeType.getWidth();
                int th = treeType.getHeight();
                if (x + tw > maxCols || y + th > maxRows) continue;
                boolean footprintClear = true;
                outer:
                for (int fx = x; fx < x + tw; fx++) {
                    for (int fy = y; fy < y + th; fy++) {
                        if (worldMap[fx][fy] != 0
                                || buildingDataMap[fx][fy] != null
                                || buildingOccupiedMap[fx][fy]) {
                            footprintClear = false;
                            break outer;
                        }
                    }
                }
                if (!footprintClear) continue;

                buildingDataMap[x][y] = new BuildingInstance(treeType, null, 0);
                buildingsMap[x][y]    = treeType;
                // Mark the full footprint as occupied so trees don't overlap each other
                for (int fx = x; fx < x + tw; fx++) {
                    for (int fy = y; fy < y + th; fy++) {
                        buildingOccupiedMap[fx][fy] = true;
                    }
                }
            }
        }
    }
}
