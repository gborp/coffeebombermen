package classes.client.sound;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import classes.utils.MathHelper;

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
	BOOM("sound/boom.wav"), PICKUP("sound/pickup.wav"), WOUND(
			"sound/wound.wav", "sound/wound2.wav", "sound/wound3.wav",
			"sound/wound4.wav", "sound/wound5.wav", "sound/wound6.wav"), KICK(
			"sound/kick.wav"), HEAL("sound/heal.wav"), THROW("sound/throw.wav"), DIE(
			"sound/die.wav", "sound/die2.wav", "sound/die3.wav",
			"sound/die4.wav", "sound/die5.wav", "sound/die6.wav",
			"sound/die7.wav", "sound/die8.wav"), PLACE_BOMB(
			"sound/placebomb.wav"), START_MATCH("sound/startmatch.wav"), DEATH_WALL(
			"sound/deathwall.wav"), PLACE_WALL("sound/placewall.wav");

	// Nested class for specifying volume
	public static enum Volume {
		MUTE, LOW, MEDIUM, HIGH
	}

	public static Volume volume = Volume.HIGH;

	private static int LATENCY = 100000;
	
	// Each sound effect has its own clip, loaded with its own sound file.
	private List<Clip> lstClip;
	private long lastPlayed;

	private String[] soundFileName;

	// Constructor to construct each element of the enum with its own sound
	// file.
	SoundEffect(String... soundFileNames) {
		this.soundFileName = soundFileNames;
		lstClip = new ArrayList<Clip>(); 
		addClip();
	}

	// Play or Re-play the sound effect from the beginning, by rewinding.
	public void play() {
		if (lastPlayed + LATENCY > System.nanoTime()) {
			return;
		}
			for (Clip c : lstClip) {
				if (!c.isRunning()) {
					play(c);
					return;
				}
			}
			addClip();
			play(lstClip.get(lstClip.size() - 1));
	}

	private void play(Clip c) {
		if (volume != Volume.MUTE) {
			
			if (!c.isRunning()) {
				// clip.stop();
				c.setFramePosition(0);
				lastPlayed = System.nanoTime();
				c.start();
				return;
			}
		}
	}

	// Optional static method to pre-load all the sound files.
	static void init() {
		values(); // calls the constructor for all the elements
	}
	
	private void addClip(){
		try {
			// Use URL (instead of File) to read from disk and JAR.
			// URL url = this.getClass().getClassLoader().getResource(
			// soundFileName);
			// Set up an audio input stream piped from the sound file.
			AudioInputStream audioInputStream = AudioSystem
					.getAudioInputStream(new File(soundFileName[MathHelper.randomInt(soundFileName.length - 1)]));
			// Get a clip resource.
			Clip clip = AudioSystem.getClip();
			// Open audio clip and load samples from the audio input stream.
			clip.open(audioInputStream);
			lstClip.add(clip);
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
