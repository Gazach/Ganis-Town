package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Pedestrian NPCs that walk tile-by-tile through connected road tiles only.
 *
 * Movement rule: each NPC moves to a cardinally adjacent road tile when it
 * arrives at its current target.  This naturally keeps it within its road
 * connected-component – no component map required.
 *
 * Visual: 3 stacked horizontal boxes  (head=black, shirt=random, pants=random).
 * Count : 75 % of total world population, capped at MAX_NPCS.
 * Render: frustum-culled fillRect – essentially free.
 */
public class NpcSpawn {

    private static final int BOX_W  = 6;
    private static final int BOX_H  = 4;
    private static final int TOTAL_H = BOX_H * 3;
    private static final int MAX_NPCS = 200;

    // Cardinal direction vectors
    private static final int[] DX = { 0,  0, 1, -1 };
    private static final int[] DY = { 1, -1, 0,  0 };

    // ── Parallel per-NPC arrays (cache-friendly, zero object overhead) ────────
    private final float[] wx     = new float[MAX_NPCS]; // current world-pixel X
    private final float[] wy     = new float[MAX_NPCS]; // current world-pixel Y
    private final float[] tgtWx  = new float[MAX_NPCS]; // target  world-pixel X
    private final float[] tgtWy  = new float[MAX_NPCS]; // target  world-pixel Y
    private final int[]   curTX  = new int[MAX_NPCS];   // current tile grid X
    private final int[]   curTY  = new int[MAX_NPCS];   // current tile grid Y
    private final int[]   tgtTX  = new int[MAX_NPCS];   // target  tile grid X
    private final int[]   tgtTY  = new int[MAX_NPCS];   // target  tile grid Y
    private final float[] spd    = new float[MAX_NPCS]; // px / frame
    private final Color[] midCol = new Color[MAX_NPCS]; // shirt colour
    private final Color[] botCol = new Color[MAX_NPCS]; // pants colour

    private int count = 0;

    private final List<int[]> roadTiles = new ArrayList<>();
    private BuildingType[][] buildingsMap;
    private int cols, rows;
    private final int tileSize;
    private final Random rng = new Random();

    public NpcSpawn(int tileSize) {
        this.tileSize = tileSize;
    }

    // ── State management ─────────────────────────────────────────────────────

    /**
     * Rebuild road tile list from buildingsMap.
     * Also evicts any NPC whose current tile is no longer a road.
     */
    public void rebuildRoadList(BuildingType[][] buildingsMap, int cols, int rows) {
        this.buildingsMap = buildingsMap;
        this.cols  = cols;
        this.rows  = rows;

        roadTiles.clear();
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                BuildingType bt = buildingsMap[x][y];
                if (bt != null && bt.getCategory() == BuildingType.BuildingCategory.PATH) {
                    roadTiles.add(new int[]{x, y});
                }
            }
        }

        // Evict NPCs that landed on a demolished road tile
        int i = 0;
        while (i < count) {
            if (!isRoad(curTX[i], curTY[i])) {
                removeNpc(i);   // fills the slot with the last NPC
            } else {
                i++;
            }
        }
    }

    /** Sync NPC count to ⌊population × 0.75⌋, capped at MAX_NPCS. */
    public void syncNpcCount(int totalPopulation) {
        int target = Math.min((int)(totalPopulation * 0.75f), MAX_NPCS);
        if (roadTiles.isEmpty()) { count = 0; return; }
        if (count > target)     { count = target; return; }

        while (count < target) {
            int[] tile = roadTiles.get(rng.nextInt(roadTiles.size()));
            spawnNpc(tile[0], tile[1]);
        }
    }

    /** Remove all NPCs (call on New Game / load). */
    public void clear() {
        count = 0;
        roadTiles.clear();
    }

    // ── Per-frame logic ───────────────────────────────────────────────────────

    public void update() {
        if (count == 0 || buildingsMap == null) return;

        for (int i = 0; i < count; i++) {
            float dx = tgtWx[i] - wx[i];
            float dy = tgtWy[i] - wy[i];

            if (dx * dx + dy * dy < 1.0f) {
                // Snap to tile center and pick the next adjacent road tile
                wx[i]    = tgtWx[i];
                wy[i]    = tgtWy[i];
                curTX[i] = tgtTX[i];
                curTY[i] = tgtTY[i];
                assignNextTile(i);
            } else {
                float inv = spd[i] / (float) Math.sqrt(dx * dx + dy * dy);
                wx[i] += dx * inv;
                wy[i] += dy * inv;
            }
        }
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    public void draw(Graphics2D g2,
                     int camX, int camY,
                     int halfW, int halfH,
                     int screenW, int screenH) {
        if (count == 0) return;

        for (int i = 0; i < count; i++) {
            int sx = (int)(wx[i] - camX + halfW) - (BOX_W >> 1);
            int sy = (int)(wy[i] - camY + halfH) - (TOTAL_H >> 1);

            // Frustum cull
            if (sx + BOX_W < 0 || sx > screenW || sy + TOTAL_H < 0 || sy > screenH) continue;

            g2.setColor(Color.BLACK);
            g2.fillRect(sx, sy, BOX_W, BOX_H);

            g2.setColor(midCol[i]);
            g2.fillRect(sx, sy + BOX_H, BOX_W, BOX_H);

            g2.setColor(botCol[i]);
            g2.fillRect(sx, sy + BOX_H * 2, BOX_W, BOX_H);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Spawn a new NPC on tile (gx, gy) and pick its first target. */
    private void spawnNpc(int gx, int gy) {
        wx[count]    = tileCX(gx);
        wy[count]    = tileCY(gy);
        curTX[count] = gx;
        curTY[count] = gy;
        spd[count]   = 0.4f + rng.nextFloat() * 0.6f;
        midCol[count] = randomColor();
        botCol[count] = randomColor();
        assignNextTile(count);
        count++;
    }

    /**
     * Choose a random cardinally adjacent road tile as the next target for NPC i.
     * Adds a small perpendicular lane-offset so NPCs spread across the tile width.
     * If the tile is isolated (no road neighbours), the NPC stays on the same tile.
     */
    private void assignNextTile(int i) {
        int gx = curTX[i], gy = curTY[i];

        // Collect valid road neighbours
        int validCount = 0;
        int[] dirs = new int[4];
        for (int d = 0; d < 4; d++) {
            if (isRoad(gx + DX[d], gy + DY[d])) dirs[validCount++] = d;
        }

        int ngx, ngy;
        if (validCount == 0) {
            // Isolated tile – stay put
            ngx = gx;
            ngy = gy;
        } else {
            int d = dirs[rng.nextInt(validCount)];
            ngx = gx + DX[d];
            ngy = gy + DY[d];
        }

        tgtTX[i] = ngx;
        tgtTY[i] = ngy;

        // Lane offset perpendicular to direction of travel (±25 % of tile)
        float laneOff = (rng.nextFloat() - 0.5f) * tileSize * 0.5f;
        boolean horizontal = (ngx != gx);
        tgtWx[i] = tileCX(ngx) + (horizontal ? 0f : laneOff);
        tgtWy[i] = tileCY(ngy) + (horizontal ? laneOff : 0f);
    }

    /** Remove NPC at index i by overwriting with the last slot. */
    private void removeNpc(int i) {
        int last = count - 1;
        wx[i]     = wx[last];    wy[i]     = wy[last];
        tgtWx[i]  = tgtWx[last]; tgtWy[i]  = tgtWy[last];
        curTX[i]  = curTX[last]; curTY[i]  = curTY[last];
        tgtTX[i]  = tgtTX[last]; tgtTY[i]  = tgtTY[last];
        spd[i]    = spd[last];
        midCol[i] = midCol[last]; botCol[i] = botCol[last];
        count--;
    }

    private boolean isRoad(int gx, int gy) {
        if (buildingsMap == null || gx < 0 || gx >= cols || gy < 0 || gy >= rows) return false;
        BuildingType bt = buildingsMap[gx][gy];
        return bt != null && bt.getCategory() == BuildingType.BuildingCategory.PATH;
    }

    private float tileCX(int gx) { return (gx + 0.5f) * tileSize; }
    private float tileCY(int gy) { return (gy + 0.5f) * tileSize; }

    private Color randomColor() {
        return new Color(55 + rng.nextInt(200), 55 + rng.nextInt(200), 55 + rng.nextInt(200));
    }
}

