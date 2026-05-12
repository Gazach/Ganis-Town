package game;

import java.util.ArrayList;
import java.util.List;

public class roadpath_system {

    /**
     * menghitung jalur tile untuk jalan dari titik awal ke titik akhir, dengan aturan:
     * Menyelaraskan arah ke horizontal, vertikal, atau diagonal 45 derajat berdasarkan
     * sudut drag — mirip dengan alat jalan lurus sederhana di Cities Skylines.
     *
     * @return daftar terurut pasangan [gridX, gridY] yang membentuk jalur jalan
     */
    public static List<int[]> computeRoadTiles(int startX, int startY, int endX, int endY) {
        List<int[]> tiles = new ArrayList<>();

        int dx = endX - startX;
        int dy = endY - startY;
        int absDx = Math.abs(dx);
        int absDy = Math.abs(dy);

        if (absDx == 0 && absDy == 0) {
            tiles.add(new int[]{startX, startY});
            return tiles;
        }

        int stepX = dx >= 0 ? 1 : -1;
        int stepY = dy >= 0 ? 1 : -1;

        if (absDx > absDy * 2) {
            // Horizontal: keep Y fixed, walk X
            for (int x = startX; ; x += stepX) {
                tiles.add(new int[]{x, startY});
                if (x == endX) break;
            }
        } else if (absDy > absDx * 2) {
            // Vertical: keep X fixed, walk Y
            for (int y = startY; ; y += stepY) {
                tiles.add(new int[]{startX, y});
                if (y == endY) break;
            }
        } else {
            // Diagonal (45°): step both axes together
            int len = Math.max(absDx, absDy);
            for (int i = 0; i <= len; i++) {
                tiles.add(new int[]{startX + stepX * i, startY + stepY * i});
            }
        }

        return tiles;
    }
}

