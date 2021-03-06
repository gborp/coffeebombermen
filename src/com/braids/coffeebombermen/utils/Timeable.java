package com.braids.coffeebombermen.utils;

/**
 * Defines a method which can be called to time the object if it is timeable
 * (usually calling it frequently with a frequency).
 */
public interface Timeable {

	/**
	 * Method to be called when the timeable object must be timed. Signs that
	 * new iteration may begin now.
	 */
	void signalingNextIteration();

}
