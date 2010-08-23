package classes.client.graphics;

/**
 * Thrown when loading a graphical theme fails because there are missing or
 * corrupt graphics resources.<br>
 */
public class CorruptGraphicalThemeException extends Exception {

	/**
	 * Creates a new CorruptGraphicalThemeException.
	 * 
	 * @param message
	 *            message/details of the exception
	 */
	public CorruptGraphicalThemeException(final String message) {
		super(message);
	}

}
