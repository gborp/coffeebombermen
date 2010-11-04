package com.braids.coffeebombermen.client.gamecore.control;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import com.braids.coffeebombermen.client.gamecore.model.level.LevelComponent;
import com.braids.coffeebombermen.client.gamecore.model.level.LevelModel;
import com.braids.coffeebombermen.options.OptConsts.Items;
import com.braids.coffeebombermen.options.OptConsts.Walls;
import com.braids.coffeebombermen.options.model.LevelOptions;
import com.braids.coffeebombermen.utils.GeneralUtilities;

/**
 * The control layer of the level.
 */
public class Level {

	/** The model of the level. */
	private final LevelModel      model;
	/**
	 * References to the fires taking places on the level. There is a vector for
	 * each component.
	 */
	private final List<Fire>[][]  fireVectorss;

	private final GameCoreHandler gameCoreHandler;

	/**
	 * Creates a new Level.<br>
	 * Implementation simply calls the other constructor with a new level model.
	 * 
	 * @param levelOptions
	 *            options of this level
	 * @param modelProvider
	 *            reference to a model provider
	 * @param modelController
	 *            reference to a model controller
	 */
	public Level(final LevelOptions levelOptions, final GameCoreHandler gameCoreHandler) {
		this(new LevelModel(levelOptions), gameCoreHandler);
	}

	/**
	 * Creates a new Level from the specified level model.
	 * 
	 * @param levelModel
	 *            level model to be used
	 * @param modelProvider
	 *            reference to a model provider
	 * @param modelController
	 *            reference to a model controller
	 */
	public Level(final LevelModel levelModel, final GameCoreHandler gameCoreHandler) {
		model = levelModel;
		this.gameCoreHandler = gameCoreHandler;

		fireVectorss = (ArrayList<Fire>[][]) Array.newInstance(new ArrayList<Fire>().getClass(), new int[] { model.getHeight(), model.getWidth() });
		for (final List<Fire>[] fireVectors : fireVectorss) {
			for (int i = 0; i < fireVectors.length; i++) {
				fireVectors[i] = new ArrayList<Fire>();
			}
		}
	}

	/**
	 * Returns the model of the level.
	 * 
	 * @return the model of the level
	 */
	public LevelModel getModel() {
		return model;
	}

	/**
	 * Adds fire to a component position.
	 * 
	 * @param fire
	 *            fire to be set
	 * @param componentPosX
	 *            x coordinate of the component to set the fire on
	 * @param componentPosY
	 *            y coordinate of the component to set the fire on
	 */
	public void addFireToComponentPos(final Fire fire, final int componentPosX, final int componentPosY) {
		fireVectorss[componentPosY][componentPosX].add(fire);

		final LevelComponent levelComponent = gameCoreHandler.getLevelModel().getComponent(componentPosX, componentPosY);
		levelComponent.addFire(fire.getModel());

		// We decide what item and if there will be an item after the fire,
		// cause it has to be appeared from the middle of the fire.
		if ((levelComponent.getWall() == Walls.BRICK) && (levelComponent.getItem() == null)) {
			// Item has to be generated once (see: time delayed multiple fire).
			if (gameCoreHandler.getGlobalServerOptions().getGettingItemProbability() > gameCoreHandler.getRandom().nextInt(100)) {
				levelComponent.setItem(Items.values()[GeneralUtilities.pickWeightedRandom(gameCoreHandler.getGlobalServerOptions().getLevelOptions()
				        .getItemWeights(), gameCoreHandler.getRandom())]);
			}
		}
	}

	/**
	 * Removes a fire from a specified component position.
	 * 
	 * @param fire
	 *            fire to be removed
	 * @param componentPosX
	 *            x coordinate of the component to remove the fire from
	 * @param componentPosY
	 *            y coordinate of the component to remove the fire from
	 */
	public void removeFireFromComponentPos(final Fire fire, final int componentPosX, final int componentPosY) {
		fireVectorss[componentPosY][componentPosX].remove(fire);
		model.getComponent(componentPosX, componentPosY).removeFire(fire.getModel());
	}

	/**
	 * Performs operations which are required by passing the time.
	 */
	public void nextIteration() {
		for (final List<Fire>[] fireVectors : fireVectorss) {
			for (final List<Fire> fireVector : fireVectors) {
				for (int i = fireVector.size() - 1; i >= 0; i--) {
					// Cannot be enhanced: fire can remove itself. And because
					// it can remove itself, cycle must be downward...
					fireVector.get(i).nextIteration();
				}
			}
		}
	}

}
