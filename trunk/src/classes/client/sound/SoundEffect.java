package classes.client.sound;

import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * This enum encapsulates all the sound effects of a game, so as to separate the
 * sound playing codes from the game codes. 1. Define all your sound effect
 * names and the associated wave file. 2. To play a specific sound, simply
 * invoke SoundEffect.SOUND_NAME.play(). 3. You might optionally invoke the
 * static method SoundEffect.init() to pre-load all the sound files, so that the
 * play is not paused while loading the file for the first time. 4. You can use
 * the static variable SoundEffect.volume to mute the sound.
 */
public enum SoundEffect {
	BOOM("sound/boom.wav"), PICKUP("sound/pickup.wav"), WOUND("sound/wound.wav"), KICK(
			"sound/kick.wav"), HEAL("sound/heal.wav"), THROW("sound/throw.wav"), DIE(
			"sound/die.wav");

	// Nested class for specifying volume
	public static enum Volume {
		MUTE, LOW, MEDIUM, HIGH
	}

	public static Volume volume = Volume.HIGH;

	// Each sound effect has its own clip, loaded with its own sound file.
	private Clip clip;

	// Constructor to construct each element of the enum with its own sound
	// file.
	SoundEffect(String soundFileName) {
		try {
			// Use URL (instead of File) to read from disk and JAR.
			// URL url = this.getClass().getClassLoader().getResource(
			// soundFileName);
			// Set up an audio input stream piped from the sound file.
			AudioInputStream audioInputStream = AudioSystem
					.getAudioInputStream(new File(soundFileName));
			// Get a clip resource.
			clip = AudioSystem.getClip();
			// Open audio clip and load samples from the audio input stream.
			clip.open(audioInputStream);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	// Play or Re-play the sound effect from the beginning, by rewinding.
	public void play() {
		try {
			if (volume != Volume.MUTE) {
				if (!clip.isRunning()) {
					// clip.stop();
					clip.setFramePosition(0);
					clip.start();
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	// Optional static method to pre-load all the sound files.
	static void init() {
		values(); // calls the constructor for all the elements
	}
}
