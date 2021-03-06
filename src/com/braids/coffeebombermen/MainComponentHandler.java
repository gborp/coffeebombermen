package com.braids.coffeebombermen;

/**
 * Defines services needed to be implemented for an object who handles the main
 * component.
 */
public interface MainComponentHandler {

	/**
	 * Called when reinitiation of main component is needed.
	 */
	void reinitMainComponent();

	/**
	 * Called when a new graphical theme has been loaded.
	 */
	void graphicalThemeChanged();

	/**
	 * Called when handler of main component is being replaced.
	 */
	void releaseMainComponent();

}
