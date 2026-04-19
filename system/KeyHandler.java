package system;

import java.awt.event.KeyEvent; //untuk mengetahui tombol apa yang dipencet
import java.awt.event.KeyListener; //Untuk mengambil input keyboard

public class KeyHandler implements KeyListener{
    // untuk mengetahui tombol apa yang dipencet (wasd)
    public boolean upPressed, downPressed, leftPressed, rightPressed;

    public boolean enterPressed; // untuk tombol enter
    public boolean escapePressed; // untuk kembali ke main menu

    @Override
    public void keyTyped(KeyEvent e){//belom ini
    }

    @Override
    public void keyPressed(KeyEvent e){ //biar bisa memberi input kepada game 
    // untuk gerak kemana habis tombol dipencet

        int code = e.getKeyCode();

        // WASD untuk gerak
        if(code == KeyEvent.VK_W){
            upPressed = true;
        }
        if(code == KeyEvent.VK_S){
            downPressed = true;
        }
        if(code == KeyEvent.VK_A){
            leftPressed = true;
        }
        if(code == KeyEvent.VK_D){
            rightPressed = true;
        }

        // ESC untuk pergi ke gameplay
        if(code == KeyEvent.VK_ENTER){
            enterPressed = true;
        }
        if(code == KeyEvent.VK_ESCAPE){
            escapePressed = true;
        }
    }

        @Override
        public void keyReleased(KeyEvent e) { // mengetahui tombol apa yang dipencet 
        // jadi kagak gerak selamanya
            int code = e.getKeyCode();
            if(code == KeyEvent.VK_W){
                upPressed = false;
        }
            if(code == KeyEvent.VK_S){
                downPressed = false;
            }
            if(code == KeyEvent.VK_A){
                leftPressed = false;
            }
            if(code == KeyEvent.VK_D){
                rightPressed = false;
            }
            if(code == KeyEvent.VK_ENTER){
                enterPressed = false;
            }
            if(code == KeyEvent.VK_ESCAPE){
                escapePressed = false;
            }
        }

    
}
