package system;

/**
 * Central audio volume manager.
 * Semua volume diatur melalui kelas ini agar mudah di manage, tinggal panggil Audio.setMasterVolume / setMusicVolume / setSFXVolume untuk mengubah levelnya.
 *
 * Usage example:
 *   Audio.setMasterVolume(0.8f);   // lower everything
 *   Audio.setMusicVolume(0.5f);    // quieter background music
 *   Audio.setSFXVolume(1.0f);      // sound effects at full
 *
 * After changing volumes, call applyVolume() on any active Music_audio
 * instance to apply the new level immediately.
 */
public class Audio {

    private static float masterVolume = 1.0f;
    private static float musicVolume  = 0.2f;
    private static float sfxVolume    = 1.2f;

    // --- Setters ---

    public static void setMasterVolume(float v) { masterVolume = clamp(v); }
    public static void setMusicVolume(float v)  { musicVolume  = clamp(v); }
    public static void setSFXVolume(float v)    { sfxVolume    = clamp(v); }

    // --- Getters ---

    public static float getMasterVolume() { return masterVolume; }
    public static float getMusicVolume()  { return musicVolume;  }
    public static float getSFXVolume()    { return sfxVolume;    }

    /** Effective music volume = master * music. */
    public static float getEffectiveMusicVolume() { return masterVolume * musicVolume; }

    /** Effective SFX volume = master * sfx. */
    public static float getEffectiveSFXVolume()   { return masterVolume * sfxVolume;   }

    /**
     * Converts a linear volume (0.0 – 1.0) to decibels for use with
     * javax.sound.sampled.FloatControl.Type.MASTER_GAIN.
     */
    public static float toDecibels(float volume) {
        if (volume <= 0f) return -80f;
        return 20f * (float) Math.log10(volume);
    }

    private static float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
