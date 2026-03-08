import game.playersave;
import system.window;

public class main {
    public static void main(String[] args) {
        playersave ps = new playersave();
        ps.save();

        window w = new window();
        w.createWindow();
    }
}
