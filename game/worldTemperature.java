package game;

import java.util.Random;

public class worldTemperature {

    // mensimulasikan perubahan suhu sepanjang hari berdasarkan siklus siang-malam
    // suhu mengikuti kurva cosinus: puncak di jam 14:00, lembah di jam 02:00
    private static final float PEAK_HOUR = 14.0f;

    private float dailyHigh; // hari ini akan seberapa panas (°C)
    private float dailyLow;  // hari ini akan seberapa dingin (°C), tapi tidak pernah di bawah 10 °C
    private float previousHour;
    private final Random random = new Random();

    public worldTemperature() {
        previousHour = 6.0f;
        generateDailyTemps();
    }

    /**
     * memanggil update setiap tick untuk mendeteksi pergantian hari (midnight rollover).
     * Ketika jam melonjak dari 22:00 ke 00:00, kita generate suhu baru untuk hari itu.
     */
    public void update(float currentHour) {
        // deteksi midnight rollover: jika jam sebelumnya 22:00 atau lebih, dan sekarang kurang dari 2:00, berarti sudah masuk hari baru
        if (previousHour >= 22.0f && currentHour < 2.0f) {
            generateDailyTemps();
        }
        previousHour = currentHour;
    }

    /** mengambil secara acak suhu tinggi/rendah untuk hari ini. */
    private void generateDailyTemps() {
        // hari ini akan seberapa panas: 22–40 °C, tapi tidak pernah di bawah 10 °C
        dailyHigh = 22.0f + random.nextFloat() * 19.0f;
        // selisih antara suhu tinggi dan rendah: 8–20 °C, tapi suhu rendah tidak pernah di bawah 10 °C
        float spread = 8.0f + random.nextFloat() * 12.0f;
        dailyLow = Math.max(10.0f, dailyHigh - spread);
    }

    /**
     * temperature mengikuti kurva cosinus: puncak di jam 14:00, lembah di jam 02:00.
     * rumusnya: t = (1 + cos(2π * (hour - PEAK_HOUR) / 24)) / 2
     * lalu suhu = dailyLow + (dailyHigh - dailyLow) * t
     * pakai rumus ini untuk mendapatkan suhu saat ini berdasarkan jam, dengan dailyHigh dan dailyLow yang sudah di-generate untuk hari itu.
     */
    public float getCurrentTemp(float hour) {
        double t = (1.0 + Math.cos(2.0 * Math.PI * (hour - PEAK_HOUR) / 24.0)) / 2.0;
        return dailyLow + (dailyHigh - dailyLow) * (float) t;
    }

    /** mengformat label suhu, misal "28°C". */
    public String getTempLabel(float hour) {
        return String.format("%.0f\u00b0C", getCurrentTemp(hour));
    }

    public float getDailyHigh() { return dailyHigh; }
    public float getDailyLow()  { return dailyLow; }

    /** memuat kembali state suhu dari file save. */
    public void load(float high, float low, float currentHour) {
        this.dailyHigh    = high;
        this.dailyLow     = Math.max(10.0f, Math.min(low, high));
        this.previousHour = currentHour;
    }
}
