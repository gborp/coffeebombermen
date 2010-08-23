package com.braids.coffeebombermen.client.gamecore;

/**
 * Activities of the bombermen.
 */
public enum Activities {
	/** Standing activity. */
	STANDING(1, true),
	/** Standing activity. */
	STANDING_WITH_BOMB(1, true),
	/** Walking activity. */
	WALKING(10, true),
	/** Walking with bomb activity. */
	WALKING_WITH_BOMB(10, true),
	/** Kicking activity. */
	KICKING(6, false),
	/** Kicking activity. */
	KICKING_WITH_BOMB(5, false),
	/** Punching activity. */
	PUNCHING(5, false),
	/** Picking up activity. */
	PICKING_UP(6, false),
	/** Dying activity. */
	DYING(30, false);

	/**
	 * The number of game iterations of the activity for a one-time play. After
	 * that, it may or may not be repeated based on the repeatable attribute.
	 */
	public final int     activityIterations;
	/**
	 * Tells whether this activity is repeatable by itself if player input
	 * doesn't change.
	 */
	public final boolean repeatable;

	/**
	 * Creates a new Activities.
	 * 
	 * @param activityIterations
	 *            the number of game iterations of the activity for a one-time
	 *            play
	 * @param repeatable
	 *            tells whether this activity is repeatable once it has been
	 *            played over
	 */
	private Activities(final int activityIterations, final boolean repeatable) {
		this.activityIterations = activityIterations;
		this.repeatable = repeatable;
	}
}
