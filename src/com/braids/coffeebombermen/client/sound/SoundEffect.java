package com.braids.coffeebombermen.client.sound;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import com.braids.coffeebombermen.Consts;
import com.braids.coffeebombermen.utils.GeneralUtilities;
import com.braids.coffeebombermen.utils.MathHelper;

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
	BOOM("boom", true), PICKUP("pickup", true), WOUND("wound", false), KICK("kick", true), HEAL("heal", false), THROW("throw", true), DIE("die", true), PLACE_BOMB(
	        "placebomb", true), START_MATCH("startmatch", true), DEATH_WALL("deathwall", false), PLACE_WALL("placewall", true);

	// Nested class for specifying volume
	public static enum Volume {
		MUTE, LOW, MEDIUM, HIGH
	}

	public static Volume  volume      = Volume.HIGH;

	/**
	 * If some effect should be played very frequent, then some must be ignored.
	 * its in milisec
	 */
	private static int    MIN_LATENCY = 1000000000;

	// Each sound effect has its own clip, loaded with its own sound file.
	private List<Clip>    lstClip;
	private long          lastPlayTime;

	private String        soundDirName;
	private int           lastPlayIndex;

	private final boolean allowParalell;

	// Constructor to construct each element of the enum with its own sound
	// file.
	SoundEffect(String soundDirName, boolean allowParalell) {
		this(soundDirName, allowParalell, true);
	}

	/**
	 * @param soundDirName
	 * @param roundRobin
	 *            Some sounds like wall are not cool when changing. Some like
	 *            wound are cool.
	 */
	SoundEffect(String soundDirName, boolean allowParalell, boolean roundRobin) {
		this.soundDirName = soundDirName;
		this.allowParalell = allowParalell;
		lstClip = new ArrayList<Clip>();
		lastPlayIndex = -1;
		addClips();
	}

	// Play or Re-play the sound effect from the beginning, by rewinding.
	public void play() {
		if (!SoundManager.isEnableSounds()) {
			return;
		}
		if (!allowParalell && (lastPlayTime + MIN_LATENCY > System.nanoTime())) {
			return;
		}
		lastPlayTime = System.nanoTime();
		// no sound loaded
		if ((lstClip == null) || lstClip.isEmpty()) {
			return;
		}
		int size = lstClip.size();
		for (int i = 1; i <= size; i++) {
			int playIndex = (lastPlayIndex + i) % size;
			Clip c = lstClip.get(playIndex);
			if (!c.isRunning()) {
				play(c);
				lastPlayIndex = playIndex;
				return;
			}
		}

		// no free sound found -> add the same files again.
		addClips();

		// it is a stange situation, that first time we found some sound files,
		// but here not. But to be sure:
		if (size == lstClip.size()) {
			return;
		}

		play(lstClip.get(size));
		lastPlayIndex = size;
	}

	private void play(Clip c) {
		if (volume != Volume.MUTE) {

			if (!c.isRunning()) {
				// clip.stop();
				c.setFramePosition(0);
				c.start();
				return;
			}
		}
	}

	// Optional static method to pre-load all the sound files.
	static void init() {
		values(); // calls the constructor for all the elements
	}

	private void addClips() {
		String path = Consts.SOUND_DIRECTORY_NAME + SoundManager.getActiveSoundTheme() + "/" + soundDirName + "/";
		String[] fileNames = GeneralUtilities.getFileNames(path);
		if (fileNames.length == 0) {
			System.err.println("No file found while loading sound files from: " + path);
			return;
		}
		for (String fileName : fileNames) {
			String file = "<null>";
			try {
				file = path + fileName;
				// Use URL (instead of File) to read from disk and JAR.
				// URL url = this.getClass().getClassLoader().getResource(
				// soundFileName);
				// Set up an audio input stream piped from the sound file.
				AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(file));
				// Get a clip resource.
				Clip clip = AudioSystem.getClip();
				// Open audio clip and load samples from the audio input stream.
				clip.open(audioInputStream);
				lstClip.add(clip);
			} catch (Exception ex) {
				System.err.println("Cannot load sound file: " + file + " (" + ex.getLocalizedMessage() + ")");
			}
		}
		if (lstClip.size() == 0) {
			System.err.println("All files ignored while loading sound files from: " + path);
			return;
		}
		if (lastPlayIndex < 0) {
			lastPlayIndex = MathHelper.randomInt(lstClip.size() - 1);
		}
	}
}
