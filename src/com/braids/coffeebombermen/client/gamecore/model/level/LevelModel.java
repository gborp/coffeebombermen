package com.braids.coffeebombermen.client.gamecore.model.level;

import java.util.ArrayList;
import java.util.List;

import com.braids.coffeebombermen.options.model.LevelOptions;

/**
 * The model of the level where bombermen take their fight.
 */
public class LevelModel {

	/** Options of this level. */
	private final LevelOptions       levelOptions;
	/** The components of the level. */
	private final LevelComponent[][] components;
	/** X position for the gateway. */
	private final List<Integer>      gatewayExitPositionX;
	/** Y position for the gateway. */
	private final List<Integer>      gatewayExitPositionY;

	/**
	 * Creates a new Level.
	 * 
	 * @param levelOptions
	 *            options of this level
	 */
	public LevelModel(final LevelOptions levelOptions) {
		this.levelOptions = levelOptions;
		components = new LevelComponent[levelOptions.getLevelHeight()][levelOptions.getLevelWidth()];
		for (final LevelComponent[] componentRow : components) {
			for (int i = 0; i < componentRow.length; i++) {
				componentRow[i] = new LevelComponent();
			}
		}
		gatewayExitPositionX = new ArrayList<Integer>();
		gatewayExitPositionY = new ArrayList<Integer>();
	}

	/**
	 * Returns the level options.
	 * 
	 * @return the level options
	 */
	public LevelOptions getLevelOptions() {
		return levelOptions;
	}

	public int getWidth() {
		return levelOptions.getLevelWidth();
	}

	public int getHeight() {
		return levelOptions.getLevelHeight();
	}

	public LevelComponent getComponent(int x, int y) {
		return components[y][x];
	}

	public void setComponent(LevelComponent component, int x, int y) {
		components[y][x] = component;
	}

	/**
	 * Packs this object to a String so it can be transferred or stored.
	 * 
	 * @return a compact string representing this level
	 */
	public String packToString() {
		return null;
	}

	/**
	 * Parses a level object from a string.
	 * 
	 * @param source
	 *            the String representing the parsable level
	 * @return a new Level created from the source string
	 */
	public static LevelModel parseFromString(final String source) {
		return null;
	}

	/**
	 * Clones and returns a clone of this level.
	 * 
	 * @return a clone of this level
	 */
	public LevelModel cloneLevel() {
		return parseFromString(packToString());
	}

	public void addGatewayExitPosition(int posX, int posY) {
		gatewayExitPositionX.add(posX);
		gatewayExitPositionY.add(posY);
	}

	public Integer getNofGatewayExit() {
		return gatewayExitPositionX.size();
	}

	public Integer getGatewayExitPositionX(int posX) {
		return gatewayExitPositionX.get(posX);
	}

	public Integer getGatewayExitPositionY(int posY) {
		return gatewayExitPositionY.get(posY);
	}
}
