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

    public void draw(Graphics2D g2) {
        if (alpha <= 0f) return;
        var old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(alpha, 1f)));
        g2.setColor(color);
        g2.fillOval((int)(x - size / 2), (int)(y - size / 2), (int)size, (int)size);
        g2.setComposite(old);
    }

    public boolean isDead() { return alpha <= 0f; }
}