package classes;

/**
 * The main class of Bombermen. Holds the public static main method, and does
 * nothing else but creates a classes.GameManager object.
 */
public class Bombermen {

	/**
	 * This is the entry point of the program. Makes a GameManager instance and
	 * thats all.
	 * 
	 * @param arguments
	 *            used to take arguments from the running environment - not used
	 *            yet
	 */
	public static void main(final String[] arguments) {
		new GameManager();
	}

}
