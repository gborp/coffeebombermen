package com.braids.coffeebombermen;

/**
 * Defines a method to handle new messages.
 */
public interface MessageHandler {

	/**
	 * Handles a messages.
	 * 
	 * @param message
	 *            message to be handled
	 */
	void handleMessage(final String message);

}
