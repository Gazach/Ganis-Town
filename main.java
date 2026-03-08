import javax.swing.JFrame;
import system.GamePanel;
public class main {
    public static void main(String[] args) {
        
        //Membuat window atau frame untuk game
        JFrame window = new JFrame();
        // konfigurasi window
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //Agar ketika di close programnya juga berhenti
        window.setResizable(true); //Agar ukuran window bisa diubah-ubah
        window.setTitle("Gani's Town"); //Judul game


        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);

        window.pack();

        window.setLocationRelativeTo(null);
        window.setVisible(true);


        //Ngejalanin game loop atau game thread
        gamePanel.startGameThread();
    }
}