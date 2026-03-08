package system;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JPanel;

import java.awt.Graphics;
import java.awt.Graphics2D;

public class GamePanel extends JPanel implements Runnable{

	//LAYAR AWAL
	//Untuk per tile 
	final int originalTileSize = 16; //JADI INI 16X16
	final int scale = 3;//INI SCALE NYA JADI 16X3 ITU KAN 48 JADI 48 PIXEL 
	final int tileSize = originalTileSize * scale; //Perhitungan untuk yang atas
	//Untuk besar layarnya
	final int maxScreenCol = 16;
	final int maxScreenRow = 12;
	final int besarLayar = tileSize * maxScreenCol; //Jadi besarnya 768
	final int tinggiLayar = tileSize * maxScreenRow; //Tingginya 576

	Thread gameThread;

	public GamePanel(){

		this.setPreferredSize(new Dimension(besarLayar, tinggiLayar));
		this.setBackground(Color.white);
		this.setDoubleBuffered(true);
	}
	public void startGameThread(){

		gameThread = new Thread(this);
		gameThread.start();
	}

	//Game Loop
	@Override
	public void run() {
		// mengatur untuk update dan render game setiap 60 FPS
		double drawInterval = 1000000000 / 60; // 60 FPS
		double delta = 0;
		long lastTime = System.nanoTime();
		long currentTime;
		
		// Game loop utama
		while(gameThread != null){
			// Menghitung waktu yang sejak frame terakhir
			currentTime = System.nanoTime();

			// Menambahkan waktu yang telah berlalu ke delta
			delta += (currentTime - lastTime) / drawInterval;
			lastTime = currentTime;

			if(delta >= 1){

				update(); // Memanggil method update untuk logika game

				// baru memanggil repaint untuk render/ngegambar grafik di gamePanel
				// repaint() itu fungsi paintComponent yang ada di bawah
				repaint(); // agak anomali emang, tapi ya gitu lah

				delta--;
			}
		}
	}

	public void update() {
		// buat bikin logika update game di sini, misalnya untuk menggerakkan karakter
	}


	// Method paintComponent / repaint() untuk ngegambar grafik di gamePanel
	// intinya buat munculin gambar di layar. kyk karakter/background
	@Override
	public void paintComponent(java.awt.Graphics g) {
		super.paintComponent(g);
		// Render grafis game di sini
		Graphics2D g2 = (Graphics2D) g;
		// Contoh menggambar kotak biru di layar
		g2.setColor(Color.blue);

		g2.fillRect(0, 0, 100, 100);

		g2.dispose();
	}
}
