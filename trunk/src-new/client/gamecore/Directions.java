package classes.client.gamecore;

/**
 * The directions of an entity (can be a player or a bomb).
 */
public enum Directions {
	/** The down direction. */
	DOWN,
	/** The up direction. */
	UP,
	/** The right direction. */
	RIGHT,
	/** The left direction. */
	LEFT;

	/**
	 * Returns the opposite of this direction.
	 * 
	 * @return the opposite of this direction
	 */
	public Directions getOpposite() {
		switch (this) {
			case DOWN:
				return UP;
			case UP:
				return DOWN;
			case RIGHT:
				return LEFT;
			case LEFT:
				return RIGHT;
		}
		throw new RuntimeException("WTF?!? Check added new directions!!!");
	}

	public Directions getTurnLeft() {
		switch (this) {
			case DOWN:
				return RIGHT;
			case UP:
				return LEFT;
			case RIGHT:
				return UP;
			case LEFT:
				return DOWN;
		}
		throw new RuntimeException("WTF?!? Check added new directions!!!");
	}

	public Directions getTurnRight() {
		switch (this) {
			case DOWN:
				return LEFT;
			case UP:
				return RIGHT;
			case RIGHT:
				return DOWN;
			case LEFT:
				return UP;
		}
		throw new RuntimeException("WTF?!? Check added new directions!!!");
	}

	public static Directions get(int direction) {
		return Directions.values()[direction];
	}

	/**
	 * Returns an integer which can be used to identify the horizontal
	 * component of the direction, and can be used to calculate positions
	 * ahead in the direction.
	 * 
	 * @return an integer identifying the horizontal component of the
	 *         direction:<br>
	 *         -1, if this is LEFT, 1, if this is RIGHT 0 otherwise
	 */

	public int getXMultiplier() {
		return this == LEFT ? -1 : (this == RIGHT ? 1 : 0);
	}

	/**
	 * Returns an integer which can be used to identify the vertical
	 * component of the direction, and can be used to calculate positions
	 * ahead in the direction.
	 * 
	 * @return an integer identifying the vertical component of the
	 *         direction:<br>
	 *         -1, if this is UP, 1, if this is DOWN 0 otherwise
	 */
	public int getYMultiplier() {
		return this == UP ? -1 : (this == DOWN ? 1 : 0);
	}

}