import system.GamePanel;
import javax.swing.JFrame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
public class Main {
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

        // Save game when window is closing
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                gamePanel.saveGame();
                System.out.println("Game Berhasil di save!.");
            }
        });

        //Ngejalanin game loop atau game thread
        gamePanel.startGameThread();
    }
}