package com.braids.coffeebombermen.client.gamecore.control;

import com.braids.coffeebombermen.client.gamecore.BombPhases;
import com.braids.coffeebombermen.client.gamecore.BombTypes;
import com.braids.coffeebombermen.client.gamecore.CoreConsts;
import com.braids.coffeebombermen.client.gamecore.Directions;
import com.braids.coffeebombermen.client.gamecore.model.BombModel;
import com.braids.coffeebombermen.client.gamecore.model.PlayerModel;
import com.braids.coffeebombermen.client.gamecore.model.level.LevelComponent;
import com.braids.coffeebombermen.client.gamecore.model.level.LevelModel;
import com.braids.coffeebombermen.options.OptConsts.Walls;
import com.braids.coffeebombermen.utils.MathHelper;

/**
 * The control layer of the bombs.
 */
public class Bomb {

	/** The model of the bomb. */
	private final BombModel       model;
	private final GameCoreHandler gameCoreHandler;

	/**
	 * Creates a new Bomb.<br>
	 * Implementation simply calls the other constructor with a new bomb model.
	 * 
	 * @param ownerPlayer
	 *            the owner player
	 * @param modelProvider
	 *            reference to a model provider
	 * @param modelController
	 *            reference to a model controller
	 */
	public Bomb(final PlayerModel ownerPlayer, final GameCoreHandler gameCoreHandler) {
		this(new BombModel(ownerPlayer), gameCoreHandler);
	}

	/**
	 * Creates a new Bomb.
	 * 
	 * @param model
	 *            the model of the bomb
	 * @param modelProvider
	 *            reference to a model provider
	 * @param modelController
	 *            reference to a model controller
	 */
	public Bomb(final BombModel model, final GameCoreHandler gameCoreHandler) {
		this.model = model;
		this.gameCoreHandler = gameCoreHandler;
	}

	/**
	 * Returns the model of the bomb.
	 * 
	 * @return the model of the bomb
	 */
	public BombModel getModel() {
		return model;
	}

	/**
	 * Performs operations which are requried by passing the time.
	 */
	public void nextIteration() {
		stepBomb();

		model.incrementIterationsDuringPhase();

		if (model.getPhase() != BombPhases.FLYING) { // Flying bombs aren't
			// ticking, and their
			// picture aren't changing.
			if (model.getIterationCounter() + 1 < CoreConsts.BOMB_ITERATIONS) {
				model.nextIteration();
			} else {
				model.setIterationCounter(0); // Bomb phases are repeatables.
			}

			if ((model.getType() != BombTypes.TRIGGERED) && !model.isDeadBomb()) {
				model.setTickingIterations(model.getTickingIterations() + 1);
				if (model.getTickingIterations() >= CoreConsts.BOMB_DETONATION_ITERATIONS * model.getExplodingTimeMultiplier()) {
					model.setAboutToDetonate(true);
				}
			}
		}

	}

	/**
	 * Steps the bomb.
	 */
	private void stepBomb() {
		LevelModel levelModel = gameCoreHandler.getLevelModel();

		switch (model.getPhase()) {

			case FLYING:
				int newPosX = model.getPosX() + model.getDirectionXMultiplier() * CoreConsts.BOMB_FLYING_SPEED;
				int newPosY = model.getPosY() + model.getDirectionYMultiplier() * CoreConsts.BOMB_FLYING_SPEED;

				// We have to check the new positions: might be they're out of
				// level, then we have to replace them to the opposite end.
				// TODO: check game rules!!

				if (gameCoreHandler.getGlobalServerOptions().isPunchedBombsComeBackAtTheOppositeEnd()) {
					if (newPosX < 0) {
						model.setPosX(levelModel.getWidth() * CoreConsts.LEVEL_COMPONENT_GRANULARITY - 1);
					} else if (newPosX > levelModel.getWidth() * CoreConsts.LEVEL_COMPONENT_GRANULARITY - 1) {
						model.setPosX(0);
					} else {
						model.setPosX(newPosX);
					}
					if (newPosY < 0) {
						model.setPosY(levelModel.getHeight() * CoreConsts.LEVEL_COMPONENT_GRANULARITY - 1);
					} else if (newPosY > levelModel.getHeight() * CoreConsts.LEVEL_COMPONENT_GRANULARITY - 1) {
						model.setPosY(0);
					} else {
						model.setPosY(newPosY);
					}
				} else {
					if ((newPosX < 0) || (newPosY < 0) || (newPosX > levelModel.getWidth() * CoreConsts.LEVEL_COMPONENT_GRANULARITY - 1)
					        || (newPosY > levelModel.getHeight() * CoreConsts.LEVEL_COMPONENT_GRANULARITY - 1)) {
						model.setDead(true);
						break;
					} else {
						model.setPosX(newPosX);
						model.setPosY(newPosY);
					}
				}

				boolean reachedPotentialTargetPosition = false;
				// If target is at the opposite end, we're not there though
				// relation between pos and target might be correct.
				if ((model.getDirectionXMultiplier() != 0)
				        && (Math.abs(model.getPosX() - model.getFlyingTargetPosX()) > CoreConsts.LEVEL_COMPONENT_GRANULARITY)) {
					;
				} else if ((model.getDirectionYMultiplier() != 0)
				        && (Math.abs(model.getPosY() - model.getFlyingTargetPosY()) > CoreConsts.LEVEL_COMPONENT_GRANULARITY)) {
					;
				} else {
					if ((model.getDirection() == Directions.LEFT) && (model.getPosX() <= model.getFlyingTargetPosX())) {
						reachedPotentialTargetPosition = true;
					}
					if ((model.getDirection() == Directions.RIGHT) && (model.getPosX() >= model.getFlyingTargetPosX())) {
						reachedPotentialTargetPosition = true;
					}
					if ((model.getDirection() == Directions.UP) && (model.getPosY() <= model.getFlyingTargetPosY())) {
						reachedPotentialTargetPosition = true;
					}
					if ((model.getDirection() == Directions.DOWN) && (model.getPosY() >= model.getFlyingTargetPosY())) {
						reachedPotentialTargetPosition = true;
					}
				}

				if (reachedPotentialTargetPosition) {
					boolean permanentTargetPosition = true;
					final LevelComponent levelComponent = levelModel.getComponent(model.getComponentPosX(), model.getComponentPosY());
					if ((levelComponent.getWall() != Walls.EMPTY) || ((levelComponent.getWall() == Walls.EMPTY) && (levelComponent.getItem() != null))) {
						permanentTargetPosition = false;
					} else if (gameCoreHandler.isBombAtComponentPosition(model.getComponentPosX(), model.getComponentPosY())) {
						permanentTargetPosition = false;
					} else if (gameCoreHandler.isPlayerAtComponentPositionExcludePlayer(model.getComponentPosX(), model.getComponentPosY(), null)) {
						permanentTargetPosition = false;
					}

					if (permanentTargetPosition) {
						model.setPhase(BombPhases.STANDING);
						// We align the bomb to the center of its component.
						model.setPosX(model.getFlyingTargetPosX());
						model.setPosY(model.getFlyingTargetPosY());
					} else {
						if (model.getType() == BombTypes.JELLY) { // If it's a
							// jelly bomb,
							// it goes
							// forward in
							// a random
							// direction
							model.setPosX(model.getFlyingTargetPosX()); // We
							// position
							// the
							// bomb
							// to
							// center
							// 'cause
							// of
							// the
							// new
							// direction.
							model.setPosY(model.getFlyingTargetPosY());

							model.setDirection(Directions.values()[MathHelper.nextInt(Directions.values().length)]);
						}
						gameCoreHandler.validateAndSetFlyingTargetPosX(model, model.getFlyingTargetPosX() + model.getDirectionXMultiplier()
						        * CoreConsts.LEVEL_COMPONENT_GRANULARITY);
						gameCoreHandler.validateAndSetFlyingTargetPosY(model, model.getFlyingTargetPosY() + model.getDirectionYMultiplier()
						        * CoreConsts.LEVEL_COMPONENT_GRANULARITY);
					}
				}
				break;

			case ROLLING:
				if (gameCoreHandler.canBombRollToComponentPosition(model, model.getComponentPosX() + model.getDirectionXMultiplier(), model.getComponentPosY()
				        + model.getDirectionYMultiplier())) {
					if (MathHelper.checkRandomEvent(model.getCrazyPercent() / (CoreConsts.LEVEL_COMPONENT_GRANULARITY / CoreConsts.BOMB_ROLLING_SPEED))) {
						Directions newDirection = Directions.values()[MathHelper.nextInt(Directions.values().length)];
						if (newDirection.equals(model.getDirection()) || newDirection.equals(model.getDirection().getOpposite())) {
							newDirection = newDirection.getTurnLeft();
						}
						if (gameCoreHandler.canBombRollToComponentPosition(model, model.getComponentPosX() + newDirection.getXMultiplier(), model
						        .getComponentPosY()
						        + newDirection.getYMultiplier())) {
							model.setDirection(newDirection);
						}
					}

					model.setPosX(model.getPosX() + CoreConsts.BOMB_ROLLING_SPEED * model.getDirectionXMultiplier());
					model.setPosY(model.getPosY() + CoreConsts.BOMB_ROLLING_SPEED * model.getDirectionYMultiplier());

					final LevelComponent levelComponent = levelModel.getComponent(model.getComponentPosX(), model.getComponentPosY());
					if (levelComponent.getItem() != null) {
						levelComponent.setItem(null);
					}
				} else {
					if (model.isDetonatingOnHit()) {
						model.setAboutToDetonate(true);
					} else {
						model.alignPosXToComponentCenter();
						model.alignPosYToComponentCenter();

						if (model.getType() == BombTypes.JELLY) {
							model.setDirection(model.getDirection().getOpposite());
						} else {
							model.setPhase(BombPhases.STANDING);
						}
					}
				}
				break;
		}
	}
}
