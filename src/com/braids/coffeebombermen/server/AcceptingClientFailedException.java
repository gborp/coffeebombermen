package com.braids.coffeebombermen.server;

/**
 * Represents an exception occuring when accepting a new client fails.<br>
 * This exception occurs on the server side when a client tries to connect to a
 * Bombermen server, but fails.<br>
 * (Some) possible causes: incompatible versions, server is locked, passwords
 * does not match...
 */
public class AcceptingClientFailedException extends Exception {

	/**
	 * Creates a new AcceptingClientFailedException.
	 * 
	 * @param message
	 *            message/details of the exception
	 */
	public AcceptingClientFailedException(final String message) {
		super(message);
	}

}
