package com.braids.coffeebombermen.client.gamecore;

/**
 * Holds constants for defining and calculating the game core.
 */
public class CoreConsts {

	/**
	 * The granularity of a level component. This is the logical size of the
	 * level components.<br>
	 * This SHOULD (MUST) be odd, so level components can have a perfect center
	 * point. I determined a value which can be divided by many numbers for
	 * practical reasons. (For example I say bomberman should step through a
	 * level component by 9 steps...) All other space measurement, speed,
	 * dimension and lengths are given based on this.
	 */
	public static final int LEVEL_COMPONENT_GRANULARITY             = 1155;                                // =
	// 3*5*7*11

	/** Basic speed of a bomberman (no ROLLER_SKATES, no DISEASE). */
	public static final int BOMBERMAN_BASIC_SPEED                   = LEVEL_COMPONENT_GRANULARITY / 6;
	/** Bomberman speed increment for a roller skates item. */
	public static final int BOBMERMAN_ROLLER_SKATES_SPEED_INCREMENT = BOMBERMAN_BASIC_SPEED * 15 / 100;
	/** Maximum speed of a bomberman (no ROLLER_SKATES, no DISEASE). */
	public static final int BOBMERMAN_MAX_SPEED                     = BOMBERMAN_BASIC_SPEED * 3;
	/** Maximum value of player vitality, the completely healthy state. */
	public static final int MAX_PLAYER_VITALITY                     = 1000;
	/**
	 * Sensitivity of movement correction. Movement correction will affect if
	 * the bomberman is at least as near to the end of a component as this.
	 * Usable value is between 0 and LEVEL_COMPONENT_GRANULARITY/2 (there are 2
	 * ends of a component).
	 */
	public static final int MOVEMENT_CORRECTION_SENSITIVITY1        = LEVEL_COMPONENT_GRANULARITY * 5 / 12;
	/**
	 * Number of iteratinos before replacing the picked up itmes of a player
	 * after he dies.
	 */
	public static final int DEAD_ITERATIONS_BEFORE_REPLACING_ITEMS  = 80;

	/** Vitality of a heart item. */
	public static final int HEART_VITALITY                          = MAX_PLAYER_VITALITY / 3;

	/** Number of game iterations of a one-time play of bomb phases. */
	public static final int BOMB_ITERATIONS                         = 30;
	/** Flying speed of a bomb. */
	public static final int BOMB_FLYING_SPEED                       = LEVEL_COMPONENT_GRANULARITY / 5;
	/** Flying distance of bombs, the minimal distance where they can fall down. */
	public static final int BOMB_FLYING_DISTANCE                    = LEVEL_COMPONENT_GRANULARITY * 3;     // Bombs
	// fly
	// 3
	// components
	/** Primary flying ascendence of a bomb. */
	public static final int BOMB_FLYING_ASCENDENCE_PRIMARY          = LEVEL_COMPONENT_GRANULARITY * 3 / 2;
	/** Secondary flying ascendence of a bomb. */
	public static final int BOMB_FLYING_ASCENDENCE_SECONDARY        = LEVEL_COMPONENT_GRANULARITY / 2;
	/** Flying speed of a bomb. */
	public static final int BOMB_ROLLING_SPEED                      = LEVEL_COMPONENT_GRANULARITY / 5;
	/** Number of iterations of a bomb detonation (time until a bomb detonates). */
	public static final int BOMB_DETONATION_ITERATIONS              = 60;

	/** Number of game iterations of a fire (detonation duration of a bomb). */
	public static final int FIRE_ITERATIONS                         = 18;
	/** Range of the super fire (substituting infinite). */
	public static final int SUPER_FIRE_RANGE                        = Integer.MAX_VALUE;
}
