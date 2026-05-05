package game;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import java.io.BufferedInputStream;
import java.io.InputStream;
import system.Audio;

/**
 * Plays a WAV sound effect once.
 * Each call to play() opens a fresh clip so effects can overlap.
 * Volume is driven by Audio.getEffectiveSFXVolume().
 */
public class SoundEffect_audio {

    private final String resourcePath;

    public SoundEffect_audio(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    /** Play the sound effect at the current SFX volume level. */
    public void play() {
        try {
            InputStream is = getClass().getResourceAsStream(resourcePath);
            if (is == null) {
                System.out.println("[SFX] File not found: " + resourcePath);
                return;
            }
            AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
            Clip clip = AudioSystem.getClip();
            clip.open(ais);

            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float db = Audio.toDecibels(Audio.getEffectiveSFXVolume());
                db = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), db));
                gain.setValue(db);
            }

            clip.start();
            // Auto-close once the clip finishes playing
            clip.addLineListener(e -> {
                if (e.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
        } catch (Exception e) {
            System.out.println("[SFX] Failed to play: " + e.getMessage());
        }
    }
}

