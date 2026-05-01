package system;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

// gamestate
import game.gamestate.Gameplay;
import game.gamestate.MainMenu;
import game.tile.TileManager;

public class GamePanel extends JPanel implements Runnable{

	//LAYAR AWAL
	//Untuk per tile 
	final int originalTileSize = 16; //JADI INI 16X16
	final int scale = 3;//INI SCALE NYA JADI 16X3 ITU KAN 48 JADI 48 PIXEL 
	public final int tileSize = originalTileSize * scale; //Perhitungan untuk yang atas
	//Untuk besar layarnya
	public final int maxScreenCol = 20;
	public final int maxScreenRow = 14;
	public final int besarLayar = tileSize * maxScreenCol; //Jadi besarnya 864
	public final int tinggiLayar = tileSize * maxScreenRow; //Tingginya 576
	//WORLD SETTINGS
	// ini itu untuk seberapa gede dunianya
	public final int maxWorldCol = 100;
	public final int maxWorldRow = 100 ;
	public final int worldWidth = tileSize * maxWorldCol;
	public final int worldHeight = tileSize * maxWorldRow;
	//
	public TileManager tileM = new TileManager(this);
	KeyHandler keyH = new KeyHandler();
	MouseHandler mouseH = new MouseHandler(this);
	public int cameraWorldX = tileSize * 50; // spawn in middle of 100x100 map
	public int cameraWorldY = tileSize * 50;
	GameStateManager gsm = new GameStateManager(); // buat ngehandle state game
	

	// buat ngehandle gamestate
	Gameplay gameplay = new Gameplay(keyH, mouseH, this); 
	MainMenu mainMenu = new MainMenu(keyH, mouseH, gsm, this, gameplay); 
	Thread gameThread;
	// Pre-allocated once to avoid per-frame GC pressure and transparent-pixel black gaps
	private BufferedImage gameBuffer = new BufferedImage(besarLayar, tinggiLayar, BufferedImage.TYPE_INT_RGB);
	//posisi awal spawn tersebut

	public GamePanel(){
		this.setPreferredSize(new Dimension(besarLayar, tinggiLayar));
		this.setBackground(Color.black);
		this.setDoubleBuffered(true);
		this.addKeyListener(keyH);
		this.addMouseListener(mouseH);
		this.addMouseMotionListener(mouseH);
		this.setFocusable(true);
		this.requestFocusInWindow();

		Player_SaveFile.createDatabase(); // seed DB once on startup
		System.out.println("Database initialized.");
		gsm.setGameplay(gameplay);        // give GSM the gameplay reference for auto-save

		gsm.setState(GameStateManager.MENU_STATE);
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
		// buat bikin logika update game di sini, misalnya untuk menggerakkan karakter
		if (gsm.isPlaying()) {
			if (keyH.escapePressed) {
				gsm.setState(GameStateManager.MENU_STATE);
				keyH.escapePressed = false;
				return;
			}
			gameplay.updateGameplay();
		}
		if (gsm.isMainMenu()) {
			mainMenu.updateMenu();
		}

	}

	// Method paintComponent / repaint() untuk ngegambar grafik di gamePanel
	// intinya buat munculin gambar di layar. kyk karakter/background

	@Override
	public void paintComponent(Graphics g) {

		// ===================================
		// JANGAN DI SENTUH SELAIN KALO MAU NAMBAH STATE BARU, KALAU MAU NAMBAH STATE BARU TINGGAL TAMBAHIN ELSE IF DI BAWAH INI AJA
		// ===================================
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		

		// Scaling untuk menyesuaikan ukuran layar, biar gak pecah gambarnya
		int panelWidth = getWidth();
		int panelHeight = getHeight();

		double scaleX = 	(double) panelWidth / besarLayar;
		double scaleY = 	(double) panelHeight / tinggiLayar;
		double scale = 		Math.min(scaleX, scaleY);

		int drawWidth = 	(int)(besarLayar * scale);
		int drawHeight = 	(int)(tinggiLayar * scale);

		int xOffset = (panelWidth - drawWidth) / 2;
		int yOffset = (panelHeight - drawHeight) / 2;

		// background hitam biar gak aneh pas di scale, nanti gambar game nya yang di scale bukan panelnya, jadi gak pecah
		g2.setColor(Color.black);
		g2.fillRect(0, 0, panelWidth, panelHeight);

		// Reuse the pre-allocated buffer; clear it first so stale pixels don't bleed through
		Graphics2D bg = gameBuffer.createGraphics();
		bg.setColor(Color.black);
		bg.fillRect(0, 0, besarLayar, tinggiLayar);

		// render game berdasarkan state saat ini, misalnya menu atau gameplay
		// kalo mau di ubah yang ini aja. jadi kalo mau nambah state baru tinggal nambahin else if nya aja
		// biar gak pusing anjg

		if (gsm.isPlaying()) {
			gameplay.drawGameplay(bg);
		} else if (gsm.isMainMenu()) {
			mainMenu.drawMenu(bg);
		}

		// dispose graphics untuk gameBuffer setelah selesai menggambar
		bg.dispose();

		// Draw the buffer scaled to the panel — nearest-neighbour keeps pixel art crisp
		// and prevents dark seam lines between tiles caused by bilinear interpolation.
		g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
			java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g2.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING,
			java.awt.RenderingHints.VALUE_RENDER_SPEED);
		g2.drawImage(gameBuffer, xOffset, yOffset, drawWidth, drawHeight, null);

		g2.dispose();
	}

	public void saveGame() {
		gameplay.saveGame();
	}
}

