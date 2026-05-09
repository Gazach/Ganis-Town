package game;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;

public class Particle {
    public float x, y;
    private float vx, vy;       // velocity
    private float alpha;        // opacity 0.0 - 1.0
    private float size;
    private float decay;        // seberapa cepat partikel hilang
    private Color color;

    public Particle(float x, float y) {
        this.x = x;
        this.y = y;
        // Arah menyebar ke samping dengan sedikit ke bawah
        this.vx = (float)(Math.random() * 4 - 2) * 1.2f;  // -2.4 ~ +2.4 (lebih menyebar)
        this.vy = (float)(Math.random() * -1 + 0.3f);    // 0.3 ~ 1.1 (ke bawah)
        this.alpha = 0.8f + (float)(Math.random() * 0.2f);
        this.size  = 6 + (float)(Math.random() * 10);
        this.decay = 0.012f + (float)(Math.random() * 0.008f);
        // Warna asep: putih keabu-abuan
        int gray = 180 + (int)(Math.random() * 60);
        this.color = new Color(gray, gray, gray);
    }

    /** Heavy destruction smoke — bigger, darker, slower decay, wider spread. */
    public Particle(float x, float y, boolean heavy) {
        this.x = x;
        this.y = y;
        if (heavy) {
            this.vx = (float)(Math.random() * 8 - 4) * 1.4f;
            this.vy = (float)(Math.random() * -2.5f + 0.5f);
            this.alpha = 0.85f + (float)(Math.random() * 0.15f);
            this.size  = 14 + (float)(Math.random() * 18);
            this.decay = 0.006f + (float)(Math.random() * 0.006f);
            int gray = 80 + (int)(Math.random() * 70);
            this.color = new Color(gray, gray, gray);
        } else {
            this.vx = (float)(Math.random() * 4 - 2) * 1.2f;
            this.vy = (float)(Math.random() * -1 + 0.3f);
            this.alpha = 0.8f + (float)(Math.random() * 0.2f);
            this.size  = 6 + (float)(Math.random() * 10);
            this.decay = 0.012f + (float)(Math.random() * 0.008f);
            int gray = 180 + (int)(Math.random() * 60);
            this.color = new Color(gray, gray, gray);
        }
    }

    // return false kalau partikel sudah hilang (alpha <= 0)
    public boolean update() {
        x += vx;
        y += vy;
        vy *= 0.99f;        // friction biar sedikit melambat ke bawah
        vx *= 0.97f;        // friction horizontal tetap sama
        size += 0.2f;       // asep melebar lebih lambat
        alpha -= decay;
        return alpha > 0f;
    }

    /** Draw using world coordinates, converting to screen each frame so the particle stays anchored to the world. */
    public void draw(Graphics2D g2, int cameraWorldX, int cameraWorldY, int halfScreenW, int halfScreenH) {
        if (alpha <= 0f) return;
        float screenX = x - cameraWorldX + halfScreenW;
        float screenY = y - cameraWorldY + halfScreenH;
        var old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(alpha, 1f)));
        g2.setColor(color);
        g2.fillOval((int)(screenX - size / 2), (int)(screenY - size / 2), (int)size, (int)size);
        g2.setComposite(old);
    }

    public boolean isDead() { return alpha <= 0f; }
}