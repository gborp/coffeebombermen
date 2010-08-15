package classes;

/**
 * Defines methods must be known by a message console.
 */
interface MessageConsole {

	/**
	 * Receives a message, appends it to the messages.
	 * 
	 * @param message
	 *            message to be appended
	 */
	void receiveMessage(final String message);

	/**
	 * Clears all messages from the console.
	 */
	void clearMessages();

}
