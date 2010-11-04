package com.braids.coffeebombermen.options.model;

import java.util.EnumMap;

import com.braids.coffeebombermen.options.Diseases;
import com.braids.coffeebombermen.options.OptConsts;
import com.braids.coffeebombermen.options.OptConsts.Items;
import com.braids.coffeebombermen.options.Shrinkers;
import com.braids.coffeebombermen.utils.GeneralStringTokenizer;
import com.braids.coffeebombermen.utils.MathHelper;

/**
 * This class holds the options of a complete level options (not included
 * options needed only for generating random levels).
 */
public class LevelOptions extends Options<LevelOptions> {

	/** Width of level in level component. */
	private int                     levelWidth;
	/** Height of level in level component. */
	private int                     levelHeight;

	/** Quantities of the accumulateable items at the beginning. */
	private EnumMap<Items, Integer> accumulateableItemQuantitiesMap = new EnumMap<Items, Integer>(Items.class);
	/** Tells whether we have the non-accumulateable items at the beginning. */
	private EnumMap<Items, Boolean> hasNonAccumulateableItemsMap    = new EnumMap<Items, Boolean>(Items.class);

	/** The weights of the items. */
	private int[]                   itemWeights                     = new int[Items.values().length];
	/** The weights of the diseases. */
	private int[]                   diseaseWeights                  = new int[Diseases.values().length];

	private int[]                   shrinkerWeights                 = new int[Shrinkers.values().length];

	/**
	 * Creates a new LevelOptions. Adds entries to
	 * accumulateableItemQuantitiesMap and hasNonAccumulateableItemsMap.
	 */
	public LevelOptions() {
		for (final Items accumulateableItem : OptConsts.ACCUMULATEABLE_ITEMS) {
			getAccumulateableItemQuantitiesMap().put(accumulateableItem, new Integer(0));
		}

		for (final Items item : Items.values()) {
			if (!OptConsts.ACCUMULATEABLE_ITEMS.contains(item)) {
				getHasNonAccumulateableItemsMap().put(item, new Boolean(false));
			}
		}
	}

	public Diseases getRandomDisease() {
		return Diseases.values()[MathHelper.getWeightedRandom(getDiseaseWeights())];
	}

	/**
	 * Packs this object to a String so it can be transferred or stored.
	 * 
	 * @return a compact string representing this level options
	 */
	public String packToString() {
		StringBuilder buffer = new StringBuilder();

		buffer.append(getLevelWidth()).append(GeneralStringTokenizer.GENERAL_SEPARATOR_CHAR);
		buffer.append(getLevelHeight()).append(GeneralStringTokenizer.GENERAL_SEPARATOR_CHAR);

		for (final Integer accumulateableItemQuantity : getAccumulateableItemQuantitiesMap().values()) {
			buffer.append(accumulateableItemQuantity.intValue()).append(GeneralStringTokenizer.GENERAL_SEPARATOR_CHAR);
		}

		for (final Boolean hasNonAccumulateableItem : getHasNonAccumulateableItemsMap().values()) {
			buffer.append(hasNonAccumulateableItem.booleanValue()).append(GeneralStringTokenizer.GENERAL_SEPARATOR_CHAR);
		}

		for (final int itemWeight : getItemWeights()) {
			buffer.append(itemWeight).append(GeneralStringTokenizer.GENERAL_SEPARATOR_CHAR);
		}

		for (final int diseaseWeight : getDiseaseWeights()) {
			buffer.append(diseaseWeight).append(GeneralStringTokenizer.GENERAL_SEPARATOR_CHAR);
		}

		for (int shrinkerWeight : getShrinkerWeights()) {
			buffer.append(shrinkerWeight).append(GeneralStringTokenizer.GENERAL_SEPARATOR_CHAR);
		}

		return buffer.toString();
	}

	/**
	 * Parses a level options object from a string.
	 * 
	 * @param source
	 *            the String representing the parsable level options
	 * @return a new LevelOptions created from the source string
	 */
	public static LevelOptions parseFromString(final String source) {
		final LevelOptions levelOptions = new LevelOptions();
		final GeneralStringTokenizer optionsTokenizer = new GeneralStringTokenizer(source);

		levelOptions.setLevelWidth(optionsTokenizer.nextIntToken());
		levelOptions.setLevelHeight(optionsTokenizer.nextIntToken());

		for (final Items accumulateableItem : OptConsts.ACCUMULATEABLE_ITEMS) {
			levelOptions.getAccumulateableItemQuantitiesMap().put(accumulateableItem, optionsTokenizer.nextIntToken());
		}

		for (final Items item : Items.values()) {
			if (!OptConsts.ACCUMULATEABLE_ITEMS.contains(item)) {
				levelOptions.getHasNonAccumulateableItemsMap().put(item, optionsTokenizer.nextBooleanToken());
			}
		}

		for (int i = 0; i < levelOptions.getItemWeights().length; i++) {
			levelOptions.getItemWeights()[i] = optionsTokenizer.nextIntToken();
		}

		for (int i = 0; i < levelOptions.getDiseaseWeights().length; i++) {
			if (optionsTokenizer.hasRemainingString()) {
				levelOptions.getDiseaseWeights()[i] = optionsTokenizer.nextIntToken();
			}
		}

		for (int i = 0; i < levelOptions.getShrinkerWeights().length; i++) {
			if (optionsTokenizer.hasRemainingString()) {
				levelOptions.getShrinkerWeights()[i] = optionsTokenizer.nextIntToken();
			}
		}

		return levelOptions;
	}

	/**
	 * Parses a level options object from a string.<br>
	 * Simply returns the object created by parseFromString().
	 * 
	 * @param source
	 *            the String representing the parsable level options
	 * @return a new LevelOptions created from the source string
	 */
	public LevelOptions dynamicParseFromString(final String source) {
		return parseFromString(source);
	}

	public void setLevelWidth(int levelWidth) {
		this.levelWidth = levelWidth;
	}

	public int getLevelWidth() {
		return levelWidth;
	}

	public void setLevelHeight(int levelHeight) {
		this.levelHeight = levelHeight;
	}

	public int getLevelHeight() {
		return levelHeight;
	}

	public void setAccumulateableItemQuantitiesMap(EnumMap<Items, Integer> accumulateableItemQuantitiesMap) {
		this.accumulateableItemQuantitiesMap = accumulateableItemQuantitiesMap;
	}

	public EnumMap<Items, Integer> getAccumulateableItemQuantitiesMap() {
		return accumulateableItemQuantitiesMap;
	}

	public void setHasNonAccumulateableItemsMap(EnumMap<Items, Boolean> hasNonAccumulateableItemsMap) {
		this.hasNonAccumulateableItemsMap = hasNonAccumulateableItemsMap;
	}

	public EnumMap<Items, Boolean> getHasNonAccumulateableItemsMap() {
		return hasNonAccumulateableItemsMap;
	}

	public void setItemWeights(int[] itemWeights) {
		this.itemWeights = itemWeights;
	}

	public int[] getItemWeights() {
		return itemWeights;
	}

	public void setDiseaseWeights(int[] diseaseWeights) {
		this.diseaseWeights = diseaseWeights;
	}

	public int[] getDiseaseWeights() {
		return diseaseWeights;
	}

	public void forbidDisease(Diseases disease) {
		diseaseWeights[disease.ordinal()] = 0;
	}

	public int[] getShrinkerWeights() {
		return this.shrinkerWeights;
	}

	public void setShrinkerWeights(int[] shrinkerWeights) {
		this.shrinkerWeights = shrinkerWeights;
	}

}
