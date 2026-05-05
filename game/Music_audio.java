package game;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.BufferedInputStream;
import java.io.InputStream;
import system.Audio;

/**
 * Loads and plays a WAV file as looping background music.
 * Volume is driven by Audio.getEffectiveMusicVolume().
 */
public class Music_audio {

    private Clip clip;
    private FloatControl gainControl;

    public Music_audio(String resourcePath) {
        try {
            InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is == null) {
                System.out.println("[Music] File not found: " + resourcePath);
                return;
            }
            AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
            clip = AudioSystem.getClip();
            clip.open(ais);
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            }
        } catch (Exception e) {
            System.out.println("[Music] Failed to load: " + e.getMessage());
        }
    }

    /** Start (or restart) the music, looping continuously. */
    public void play() {
        if (clip == null) return;
        applyVolume();
        clip.setFramePosition(0);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    /** Stop the music without closing the clip so it can be replayed. */
    public void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    /**
     * Re-apply the current Audio volume levels to the running clip.
     * Call this after changing Audio.setMasterVolume / setMusicVolume.
     */
    public void applyVolume() {
        if (gainControl == null) return;
        float db = Audio.toDecibels(Audio.getEffectiveMusicVolume());
        db = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), db));
        gainControl.setValue(db);
    }
}
