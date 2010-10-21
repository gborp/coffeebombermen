package com.braids.coffeebombermen.client.sound;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.braids.coffeebombermen.Consts;
import com.braids.coffeebombermen.utils.GeneralUtilities;

/**
 * Manages the sound resources of the game.<br>
 * This includes getting a list of available sound themes, and loading them.
 */
public class SoundManager {

	private static boolean         enableSounds = true;
	private static ExecutorService exSounds     = Executors.newCachedThreadPool();

	/**
	 * This private SoundManager constructor disables the creation of instances.
	 */
	private SoundManager() {}

	/**
	 * Returns array of the names of available sound themes.<br>
	 * Returns array the subdirectory names within the SOUND_DIRECTORY_NAME
	 * directory.
	 * 
	 * @return array of the names of available sound themes
	 */
	public static String[] getAvailableSoundThemes() {
		return GeneralUtilities.getSubdirectoryNames(Consts.SOUND_DIRECTORY_NAME);
	}

	public static String getActiveSoundTheme() {
		return "classic";
	}

	public static void setEnableSounds(boolean enableSounds) {
		SoundManager.enableSounds = enableSounds;
	}

	public static boolean isEnableSounds() {
		return enableSounds;
	}

	public static void sound() {

	}

}
