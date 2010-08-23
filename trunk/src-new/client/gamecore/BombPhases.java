package classes.client.gamecore;

/**
 * Phases of the bombs.
 */
public enum BombPhases {
	/** The flying bomb phase, the bomb is punched or thrown away. */
	FLYING,
	/** The rolling bomb phase, the bomb has been kicked. */
	ROLLING,
	/** The standing bomb phase, the bomb is not bothered. */
	STANDING
}