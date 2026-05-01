package game;

import java.awt.Color;

public class dayCycle {

    // 5 menit real time = 24 jam in-game, jadi 1 menit real time = 4.8 jam in-game
    private static final long CYCLE_DURATION_MS = 5 * 60 * 1000L;

    private float currentHour; // 0.0 – 24.0
    private long lastUpdateTime;

    public dayCycle() {
        this.currentHour = 6.0f; // start at 6 AM
        this.lastUpdateTime = System.currentTimeMillis();
    }

    /** update waktu siang/malam */
    public void update() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastUpdateTime;
        lastUpdateTime = now;

        // konversi waktu yang berlalu ke dalam jam in-game, dan update currentHour
        float hoursAdvanced = (elapsed / (float) CYCLE_DURATION_MS) * 24.0f;
        currentHour = (currentHour + hoursAdvanced) % 24.0f;
    }

    /** set jam saat ini (0.0 – 24.0). Digunakan saat memuat save. */
    public void setHour(float hour) {
        this.currentHour = ((hour % 24.0f) + 24.0f) % 24.0f;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public float getHour() {
        return currentHour;
    }

    /**
     * Mengembalikan faktor kegelapan 0.0–1.0.
     * 0 = siang penuh, 1 = malam penuh.
     */
    public float getDarkness() {
        float h = currentHour;
        // Dawn  05–07: darkness fades from 1→0
        // Day   07–18: darkness = 0
        // Dusk  18–20: darkness grows from 0→1
        // Night 20–05: darkness = 1
        if (h >= 7f && h < 18f) {
            return 0f;
        } else if (h >= 5f && h < 7f) {
            return 1f - ((h - 5f) / 2f);
        } else if (h >= 18f && h < 20f) {
            return (h - 18f) / 2f;
        } else {
            return 1f;
        }
    }

    /**
     * Semi-transparent dark-blue overlay untuk efek siang/malam. Semakin gelap semakin tebal overlay-nya.
     * Alpha maksimal sekitar 170/255 agar masih bisa lihat pemandangan di malam hari.
     */
    public Color getOverlayColor() {
        float darkness = getDarkness();
        if (darkness <= 0f) return new Color(0, 0, 0, 0);
        int alpha = (int) (darkness * 170); // max alpha ~170/255
        return new Color(0, 8, 35, alpha);
    }

    /** mengembalikan label waktu dalam format jam:menit AM/PM */
    public String getTimeLabel() {
        int h = (int) currentHour;
        int m = (int) ((currentHour - h) * 60);
        String period = (h < 12) ? "AM" : "PM";
        int displayH = h % 12;
        if (displayH == 0) displayH = 12;
        return String.format("%02d:%02d %s", displayH, m, period);
    }

    /** mengembalikan nama periode waktu saat ini (Malam / Fajar / Siang / Senja). */
    public String getPeriodName() {
        float h = currentHour;
        if (h >= 7f && h < 18f)  return "Day";
        if (h >= 5f && h < 7f)   return "Dawn";
        if (h >= 18f && h < 20f) return "Dusk";
        return "Night";
    }
}
