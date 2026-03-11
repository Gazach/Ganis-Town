// ==================================================================
// Game State adalah sistem yang merepresentasikan 
// bagian dari game mana yang sedang aktif, yang menentukan logika 
// apa yang berjalan dan apa yang ditampilkan di layar.
// ==================================================================

// intinya buat ngehandle kalo mau ganti layar game misalnya dari menu ke gameplay, atau dari gameplay ke pause menu, dll.
// biar gak ribet kalo mau ganti layar, tinggal ganti state nya aja

package system;

public class GameStateManager {

    // Game States
    // Game state saat ini di lambangkan dengan angka, misalnya 0 untuk menu,
    // 1 untuk gameplay, 2 untuk pause, dll.
    // angka ini disesuaikan dengan urutan agar mudah di manage

    public static final int MENU_STATE  =   0; // state untuk menu utama
    public static final int PLAY_STATE  =   1; // state untuk gameplay
    public static final int PAUSE_STATE =   2; // state untuk pause menu

    private int currentState;

    public GameStateManager() {
        // secara default game akan muncul di menu saat pertama kali dijalankan
        currentState = MENU_STATE; 
    }

    public void setState(int state) {
        // method untuk mengganti state game sesuai dengan parameter yang diberikan
        currentState = state;
    }

    public int getState() {
        // method untuk mendapatkan state game saat ini, bisa digunakan untuk logika update dan render
        return currentState;
    }

    // list gamestate :
    public boolean isMainMenu() {
        return currentState == MENU_STATE;
    }

    public boolean isPlaying() {
        return currentState == PLAY_STATE;
    }

    public boolean isPaused() {
        return currentState == PAUSE_STATE;
    }

}