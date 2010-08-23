package com.braids.coffeebombermen.client.gamecore.control;

import com.braids.coffeebombermen.client.gamecore.CoreConsts;
import com.braids.coffeebombermen.client.gamecore.model.FireModel;

/**
 * The control layer of the fire.<br>
 * Stores the x and y coordinates of the component this fire takes place on for
 * fast accessing the level component.
 */
public class Fire {

	/** The model of the fire. */
	private final FireModel       model;
	/** X coordinate of the component where this fire takes place on. */
	private final int             componentPosX;
	/** Y coordinate of the component where this fire takes place on. */
	private final int             componentPosY;

	private final GameCoreHandler gameCoreHandler;

	/**
	 * Creates a new Fire.<br>
	 * 
	 * @param componentPosX
	 *            x coordinate of the component where this fire takes place on
	 * @param componentPosY
	 *            y coordinate of the component where this fire takes place on
	 * @param modelProvider
	 *            reference to a model provider
	 * @param modelController
	 *            reference to a model controller
	 */
	public Fire(final int componentPosX, final int componentPosY, final GameCoreHandler gameCoreHandler) {
		this.componentPosX = componentPosX;
		this.componentPosY = componentPosY;
		this.gameCoreHandler = gameCoreHandler;

		model = new FireModel();
	}

	/**
	 * Returns the model of the bomb.
	 * 
	 * @return the model of the bomb
	 */
	public FireModel getModel() {
		return model;
	}

	/**
	 * Performs operations which are requried by passing the time.
	 */
	public void nextIteration() {
		if (model.getIterationCounter() + 1 < CoreConsts.FIRE_ITERATIONS) {
			model.nextIteration();
		} else {
			gameCoreHandler.removeFireFromComponentPos(this, componentPosX, componentPosY);
		}
	}

}
