package com.braids.coffeebombermen.client.gamecore.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;

import com.braids.coffeebombermen.client.gamecore.Activities;
import com.braids.coffeebombermen.client.sound.SoundEffect;
import com.braids.coffeebombermen.options.Diseases;
import com.braids.coffeebombermen.options.LevelComponentOptions;
import com.braids.coffeebombermen.options.OptConsts.Items;
import com.braids.coffeebombermen.options.OptConsts.PlayerColors;
import com.braids.coffeebombermen.options.OptConsts.PlayerControlKeys;

/**
 * The class represents the model of a player of the GAME (NOT the the
 * application): the figure controlled by a player called bomberman and all the
 * informations needed to calculate, simulate its working and its playing.
 */
public class PlayerModel extends PositionedIterableObject {

	/** game core tick frequency is 30hz */
	public static final long              DISEASE_DURATION                = 6 * 30;

	/** game core tick frequency is 30hz */
	public static final long              SUPER_DISEASE_DURATION          = DISEASE_DURATION * 2;

	private static final int              SPIDER_BOMB_ROUNDS              = 4;

	private String                        name;

	/** Vitality of the player. */
	private int                           vitality;
	/** The current activity of the player. */
	private Activities                    activity;
	/** Number of placable triggered bombs. */
	private int                           placableTriggeredBombs;

	private int                           placableWalls;
	/** Model of the picked up bomb, or null, if there is no picked up bomb. */
	private BombModel                     pickedUpBombModel;

	/** Quantities of the accumulateable items owned by the player. */
	public final EnumMap<Items, Integer>  accumulateableItemQuantitiesMap = new EnumMap<Items, Integer>(Items.class);
	/** Tells whether we have the non-accumulateable items owned by the player. */
	private final EnumMap<Items, Boolean> hasNonAccumulateableItemsMap    = new EnumMap<Items, Boolean>(Items.class);

	// I seperate the next two entity in order to determine fast whether we
	// picked up a non accumulateable item and in order to be able to remove it
	// fast.
	/** Quantities of the accumulateable items picked up by the player. */
	public final ArrayList<Items>         pickedUpAccumulateableItems     = new ArrayList<Items>();
	/**
	 * Tells whether we have the non-accumulateable items picked up by the
	 * player.
	 */
	public final ArrayList<Items>         pickedUpNonAccumulateableItems  = new ArrayList<Items>();

	/** disease - when will it expire */
	public final HashMap<Diseases, Long>  mapOwnedDiseases                = new HashMap<Diseases, Long>();

	/** The states of the control keys of the player. */
	private boolean[]                     controlKeyStates                = new boolean[PlayerControlKeys.values().length];
	/** The previous states of the control keys of the player. */
	private boolean[]                     lastControlKeyStates            = new boolean[PlayerControlKeys.values().length];
	private int                           regenerateWeight;
	private boolean                       autoDropBombEnabled;
	private boolean                       spiderBombEnabled;
	private int                           spiderBombRounds;
	private int                           points;

	private PlayerColors                  color;

	/**
	 * Returns the vitality of the player.
	 * 
	 * @return the vitality of the player
	 */
	public int getVitality() {
		return vitality;
	}

	/**
	 * Sets the vitality of the player.
	 * 
	 * @param vitality
	 *            the vitality of the player to be set
	 */
	public void setVitality(final int vitality) {
		if (vitality > this.vitality) {
			// no sound for regenerate
			if (vitality > this.vitality + 100) {
				SoundEffect.HEAL.play();
			}
		} else {
			SoundEffect.WOUND.play();
		}
		this.vitality = vitality;
	}

	/**
	 * Returns the current activity of the player.
	 * 
	 * @return the current activity of the player
	 */
	public Activities getActivity() {
		return activity;
	}

	/**
	 * Sets the current activity of the player.
	 * 
	 * @param activity
	 *            activity to be set
	 */
	public void setActivity(final Activities activity) {
		this.activity = activity;
		setIterationCounter(0);
		switch (activity) {
			case KICKING:
				SoundEffect.KICK.play();
				break;
			case PUNCHING:
				SoundEffect.THROW.play();
				break;
			case DYING:
				SoundEffect.DIE.play();
				mapOwnedDiseases.clear();
				break;
		}
	}

	/**
	 * Returns the number of placable triggered bombs.
	 * 
	 * @return the number of placable triggered bombs
	 */
	public int getPlacableTriggeredBombs() {
		return placableTriggeredBombs;
	}

	/**
	 * Sets the number of placable triggered bombs.
	 * 
	 * @param placableTriggeredBombs
	 *            number of placable triggered bombs to be set
	 */
	public void setPlacableTriggeredBombs(final int placableTriggeredBombs) {
		this.placableTriggeredBombs = placableTriggeredBombs;
	}

	public int getPlaceableWalls() {
		return placableWalls;
	}

	public void setPlaceableWalls(int placableWalls) {
		this.placableWalls = placableWalls;
	}

	/**
	 * Returns the model of the picked up bomb.
	 * 
	 * @return the model of the picked up bomb
	 */
	public BombModel getPickedUpBombModel() {
		return pickedUpBombModel;
	}

	/**
	 * Sets the model of the picked up bomb.
	 * 
	 * @param pickedUpBombModel
	 *            model of the picked up bomb to be set
	 */
	public void setPickedUpBombModel(final BombModel pickedUpBombModel) {
		this.pickedUpBombModel = pickedUpBombModel;
	}

	/**
	 * Checks and returns whether any of the direction keys is pressed.
	 * 
	 * @return true if any of the direction keys is pressed; false otherwise
	 */
	public boolean isDirectionKeyPressed() {
		return controlKeyStates[PlayerControlKeys.DOWN.ordinal()] || controlKeyStates[PlayerControlKeys.UP.ordinal()]
		        || controlKeyStates[PlayerControlKeys.LEFT.ordinal()] || controlKeyStates[PlayerControlKeys.RIGHT.ordinal()];
	}

	/**
	 * Returns the state of a player control key.
	 * 
	 * @param playerControlKey
	 *            player control key whose state to be returned
	 * @return the state of a player control key
	 */
	public boolean getControlKeyState(PlayerControlKeys playerControlKey) {
		if (mapOwnedDiseases.containsKey(Diseases.REVERSE)) {
			switch (playerControlKey) {
				case DOWN:
					playerControlKey = PlayerControlKeys.UP;
					break;
				case UP:
					playerControlKey = PlayerControlKeys.DOWN;
					break;
				case LEFT:
					playerControlKey = PlayerControlKeys.RIGHT;
					break;
				case RIGHT:
					playerControlKey = PlayerControlKeys.LEFT;
					break;
			}
		}
		return controlKeyStates[playerControlKey.ordinal()];
	}

	/**
	 * Sets the state of a player control key.<br>
	 * Before setting the new state, stores the old state to the
	 * lastControlKeyStates attribue.
	 * 
	 * @param playerControlKey
	 *            player control key whose state to be set
	 * @param playerControlKeyPressed
	 *            tells whether the specified player control key is pressed
	 */
	public void setControlKeyState(final PlayerControlKeys playerControlKey, final boolean playerControlKeyPressed) {
		lastControlKeyStates[playerControlKey.ordinal()] = controlKeyStates[playerControlKey.ordinal()];
		controlKeyStates[playerControlKey.ordinal()] = playerControlKeyPressed;
	}

	/**
	 * Returns the last state of a player control key.
	 * 
	 * @param playerControlKey
	 *            player control key whose last state to be returned
	 * @return the last state of a player control key
	 */
	public boolean getLastControlKeyState(final PlayerControlKeys playerControlKey) {
		return lastControlKeyStates[playerControlKey.ordinal()];
	}

	public void setSpiderBombEnabled(boolean spiderBombEnabled) {
		this.spiderBombEnabled = spiderBombEnabled;
		spiderBombRounds = spiderBombEnabled ? spiderBombRounds + SPIDER_BOMB_ROUNDS : 0;
	}

	public boolean isSpiderBombEnabled() {
		return spiderBombEnabled;
	}

	public int getSpiderBombRounds() {
		return spiderBombRounds;
	}

	public void setSpiderBombRounds(int spyderBombRounds) {
		this.spiderBombRounds = spyderBombRounds;
	}

	public boolean hasNonAccumItem(Items i) {
		return hasNonAccumulateableItemsMap.get(i);
	}

	public void setAllNonAccumItems(EnumMap<Items, Boolean> allNonAccumItems) {
		hasNonAccumulateableItemsMap.putAll(allNonAccumItems);
	}

	public void setNonAccumItem(Items wallBuilding, boolean b) {
		hasNonAccumulateableItemsMap.put(wallBuilding, b);
	}

	public void addDisease(Diseases disease, Long expire) {
		if (!mapOwnedDiseases.containsKey(disease) || (mapOwnedDiseases.get(disease) < expire)) {
			mapOwnedDiseases.put(disease, expire);
		}
	}

	public boolean hasDiseases() {
		return !mapOwnedDiseases.isEmpty();
	}

	public boolean hasDisease(Diseases disease) {
		return mapOwnedDiseases.containsKey(disease);
	}

	public HashMap<Diseases, Long> getOwnedDiseases() {
		return mapOwnedDiseases;
	}

	public void expireDisease(Diseases key) {
		mapOwnedDiseases.remove(key);
	}

	/** picked up rollerskates +- diseases */
	public Integer getEffectiveRollerSkates() {
		Integer rollerSkates = accumulateableItemQuantitiesMap.get(Items.ROLLER_SKATES);
		if (rollerSkates == null) {
			rollerSkates = 0;
		}
		if (mapOwnedDiseases.containsKey(Diseases.SCUDING)) {
			rollerSkates = LevelComponentOptions.MAXIMUM_ROLLER_SKATES_QUANTITY;
		}
		if (mapOwnedDiseases.containsKey(Diseases.TODDLING)) {
			rollerSkates = -5;
		}
		return rollerSkates;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isAlive() {
		return vitality > 0;
	}

	public int getPoints() {
		return this.points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public PlayerColors getColor() {
		return color;
	}

	public void setColor(PlayerColors color) {
		this.color = color;
	}

}
