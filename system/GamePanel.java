package system;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JPanel;

import system.entity.Player;
import java.awt.Graphics;
import java.awt.Graphics2D;


import game.gamestate.Gameplay;

public class GamePanel extends JPanel implements Runnable{

	//LAYAR AWAL
	//Untuk per tile 
	final int originalTileSize = 16; //JADI INI 16X16
	final int scale = 3;//INI SCALE NYA JADI 16X3 ITU KAN 48 JADI 48 PIXEL 
	public final int tileSize = originalTileSize * scale; //Perhitungan untuk yang atas
	//Untuk besar layarnya
	final int maxScreenCol = 16;
	final int maxScreenRow = 12;
	final int besarLayar = tileSize * maxScreenCol; //Jadi besarnya 768
	final int tinggiLayar = tileSize * maxScreenRow; //Tingginya 576

	//
	KeyHandler keyH = new KeyHandler();
	GameStateManager gsm = new GameStateManager(); // buat ngehandle state game

	Gameplay gameplay = new Gameplay(keyH);

	Thread gameThread;
	Player player = new Player(this,keyH);
	//posisi awal spawn tersebut

	public GamePanel(){

		this.setPreferredSize(new Dimension(besarLayar, tinggiLayar));
		this.setBackground(Color.white);
		this.setDoubleBuffered(true);
		this.addKeyListener(keyH);
		this.setFocusable(true);
		
		gsm.setState(GameStateManager.PLAY_STATE);
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
		long timer = 0;
		int drawCount = 0;
		
		// Game loop utama
		while(gameThread != null){
			// Menghitung waktu yang sejak frame terakhir
			currentTime = System.nanoTime();

			// Menambahkan waktu yang telah berlalu ke delta
			delta += (currentTime - lastTime) / drawInterval;
			timer += (currentTime - lastTime);
			lastTime = currentTime;

			if(delta >= 1){

				update(); // Memanggil method update untuk logika game

				// baru memanggil repaint untuk render/ngegambar grafik di gamePanel
				// repaint() itu fungsi paintComponent yang ada di bawah
				repaint(); // agak anomali emang, tapi ya gitu lah

				delta--;

				drawCount++;
			}
			if (timer >= 1000000000){
				System.out.println("FPS:" + drawCount);
				drawCount = 0;
				timer = 0;
			}
		}
	}

	public void update() {
<<<<<<< HEAD
		// buat bikin logika update game di sini, misalnya untuk menggerakkan karakter
		if (gsm.isPlaying()) {
			gameplay.updateGameplay();
		}
=======

		player.update();
		
>>>>>>> c854513233239fa744fe1e2e0f937912b4cb5eb9
	}


	// Method paintComponent / repaint() untuk ngegambar grafik di gamePanel
	// intinya buat munculin gambar di layar. kyk karakter/background
	@Override
	public void paintComponent(java.awt.Graphics g) {
		// Render grafis game di sini
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
<<<<<<< HEAD

		// jika state game saat ini adalah PLAY_STATE, maka gambar gameplay
		if (gsm.isPlaying()) {
			gameplay.drawGameplay(g2);
		}
=======
		// Contoh menggambar kotak biru di layar
		player.draw(g2);
		g2.dispose();
>>>>>>> c854513233239fa744fe1e2e0f937912b4cb5eb9
	}
}

