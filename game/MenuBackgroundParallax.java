package game;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;

import system.GamePanel;

public class MenuBackgroundParallax {
    // variable untuk menyimpan gambar, posisi, dan kecepatan setiap layer
    private BufferedImage[] background;
    private double[] bgX;
    private double[] bgSpeed;

    private GamePanel gp;

    public MenuBackgroundParallax(GamePanel gp) {
        this.gp = gp; // biar dapet akses ke ukuran layar dari GamePanel
    }

    public void getImages() {
        try {
            // load gambar untuk setiap layer parallax
            background = new BufferedImage[4];

            background[0] = ImageIO.read(getClass().getResourceAsStream("/asset/MainMenuBackground/1.png"));
            background[1] = ImageIO.read(getClass().getResourceAsStream("/asset/MainMenuBackground/2.png"));
            background[2] = ImageIO.read(getClass().getResourceAsStream("/asset/MainMenuBackground/3.png"));
            background[3] = ImageIO.read(getClass().getResourceAsStream("/asset/MainMenuBackground/4.png"));

            // inisialisasi posisi dan kecepatan untuk setiap layer
            bgX = new double[background.length];
            // kecepatan yang berbeda untuk menciptakan efek parallax
            bgSpeed = new double[]{0.5, 1.0, 1.5, 2.0};

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // update posisi setiap layer berdasarkan kecepatan masing-masing
    public void update() {
        // update posisi setiap layer
        for (int i = 0; i < bgX.length; i++) {
            // geser layer ke kiri berdasarkan kecepatan
            bgX[i] -= bgSpeed[i];
            // jika layer sudah keluar dari layar, reset posisinya ke kanan
            // atau ngulang gambar untuk menciptakan efek loop
            if (bgX[i] <= -gp.besarLayar) {
                bgX[i] += gp.besarLayar;
            }
        }
    }

    public void drawParallax(Graphics2D g2) {
        // gambar setiap layer berdasarkan posisi yang sudah diupdate
        for (int i = 0; i < background.length; i++) {
            // gambar layer pertama kali di posisi bgX[i]
            int x = (int)Math.round(bgX[i]);
            // gambar layer kedua kali di posisi bgX[i] + lebar layar untuk menciptakan efek loop
            g2.drawImage(background[i], x, 0, gp.besarLayar, gp.tinggiLayar, null);
            g2.drawImage(background[i], x + gp.besarLayar, 0, gp.besarLayar, gp.tinggiLayar, null);

        }
    }
}