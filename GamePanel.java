package main;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JPanel;
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
		this.setBackground(Color.black);
		this.setDoubleBuffered(true);
	}
	public void startGameThread(){

		gameThread = new Thread(this);
		gameThread.start();
	}
	@Override
	public void run(){
		
	}
}
