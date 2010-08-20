/*
 * Created on November 28, 2005
 */

package classes.client.gamecore.control;

import static classes.client.gamecore.Consts.BOMB_DETONATION_ITERATIONS;
import static classes.client.gamecore.Consts.BOMB_FLYING_SPEED;
import static classes.client.gamecore.Consts.BOMB_ITERATIONS;
import static classes.client.gamecore.Consts.BOMB_ROLLING_SPEED;
import static classes.client.gamecore.Consts.LEVEL_COMPONENT_GRANULARITY;
import classes.client.gamecore.Consts.BombPhases;
import classes.client.gamecore.Consts.BombTypes;
import classes.client.gamecore.Consts.Directions;
import classes.client.gamecore.model.BombModel;
import classes.client.gamecore.model.PlayerModel;
import classes.client.gamecore.model.level.LevelComponent;
import classes.options.Consts.Walls;
import classes.utils.MathHelper;

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
			if (model.getIterationCounter() + 1 < BOMB_ITERATIONS)
				model.nextIteration();
			else
				model.setIterationCounter(0); // Bomb phases are repeatables.

			if (model.getType() != BombTypes.TRIGGERED) {
				model.setTickingIterations(model.getTickingIterations() + 1);
				if (model.getTickingIterations() >= BOMB_DETONATION_ITERATIONS)
					model.setAboutToDetonate(true);
			}
		}

	}

	/**
	 * Steps the bomb.
	 */
	private void stepBomb() {
		switch (model.getPhase()) {

			case FLYING:
				int newPosX = model.getPosX() + model.getDirectionXMultiplier() * BOMB_FLYING_SPEED;
				int newPosY = model.getPosY() + model.getDirectionYMultiplier() * BOMB_FLYING_SPEED;

				// We have to check the new positions: might be they're out of
				// level, then we have to replace them to the opposite end.
				// TODO: check game rules!!
				final LevelComponent[][] levelComponents = gameCoreHandler.getLevelModel().getComponents();
				if (gameCoreHandler.getGlobalServerOptions().isPunchedBombsComeBackAtTheOppositeEnd()) {
					if (newPosX < 0)
						model.setPosX(levelComponents[0].length * LEVEL_COMPONENT_GRANULARITY - 1);
					else if (newPosX > levelComponents[0].length * LEVEL_COMPONENT_GRANULARITY - 1)
						model.setPosX(0);
					else
						model.setPosX(newPosX);
					if (newPosY < 0)
						model.setPosY(levelComponents.length * LEVEL_COMPONENT_GRANULARITY - 1);
					else if (newPosY > levelComponents.length * LEVEL_COMPONENT_GRANULARITY - 1)
						model.setPosY(0);
					else
						model.setPosY(newPosY);
				} else {
					if (newPosX < 0 || newPosY < 0 || newPosX > levelComponents[0].length * LEVEL_COMPONENT_GRANULARITY - 1
					        || newPosY > levelComponents.length * LEVEL_COMPONENT_GRANULARITY - 1) {
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
				if (model.getDirectionXMultiplier() != 0 && Math.abs(model.getPosX() - model.getFlyingTargetPosX()) > LEVEL_COMPONENT_GRANULARITY)
					;
				else if (model.getDirectionYMultiplier() != 0 && Math.abs(model.getPosY() - model.getFlyingTargetPosY()) > LEVEL_COMPONENT_GRANULARITY)
					;
				else {
					if (model.getDirection() == Directions.LEFT && model.getPosX() <= model.getFlyingTargetPosX())
						reachedPotentialTargetPosition = true;
					if (model.getDirection() == Directions.RIGHT && model.getPosX() >= model.getFlyingTargetPosX())
						reachedPotentialTargetPosition = true;
					if (model.getDirection() == Directions.UP && model.getPosY() <= model.getFlyingTargetPosY())
						reachedPotentialTargetPosition = true;
					if (model.getDirection() == Directions.DOWN && model.getPosY() >= model.getFlyingTargetPosY())
						reachedPotentialTargetPosition = true;
				}

				if (reachedPotentialTargetPosition) {
					boolean permanentTargetPosition = true;
					final LevelComponent levelComponent = levelComponents[model.getComponentPosY()][model.getComponentPosX()];
					if (levelComponent.getWall() != Walls.EMPTY || levelComponent.getWall() == Walls.EMPTY && levelComponent.getItem() != null)
						permanentTargetPosition = false;
					else if (gameCoreHandler.isBombAtComponentPosition(model.getComponentPosX(), model.getComponentPosY()))
						permanentTargetPosition = false;
					else if (gameCoreHandler.isPlayerAtComponentPositionExcludePlayer(model.getComponentPosX(), model.getComponentPosY(), null))
						permanentTargetPosition = false;

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
							model.setDirection(Directions.values()[gameCoreHandler.getRandom().nextInt(Directions.values().length)]);
						}
						gameCoreHandler.validateAndSetFlyingTargetPosX(model, model.getFlyingTargetPosX() + model.getDirectionXMultiplier()
						        * LEVEL_COMPONENT_GRANULARITY);
						gameCoreHandler.validateAndSetFlyingTargetPosY(model, model.getFlyingTargetPosY() + model.getDirectionYMultiplier()
						        * LEVEL_COMPONENT_GRANULARITY);
					}
				}
				break;

			case ROLLING:
				if (gameCoreHandler.canBombRollToComponentPosition(model, model.getComponentPosX() + model.getDirectionXMultiplier(), model.getComponentPosY()
				        + model.getDirectionYMultiplier())) {
					if (MathHelper.checkRandomEvent(model.getCrazyPercent() / (LEVEL_COMPONENT_GRANULARITY / BOMB_ROLLING_SPEED))) {
						Directions newDirection = Directions.values()[gameCoreHandler.getRandom().nextInt(Directions.values().length)];
						if (newDirection.equals(model.getDirection()) || newDirection.equals(model.getDirection().getOpposite())) {
							newDirection = newDirection.getTurnLeft();
						}
						if (gameCoreHandler.canBombRollToComponentPosition(model, model.getComponentPosX() + newDirection.getXMultiplier(), model
						        .getComponentPosY()
						        + newDirection.getYMultiplier())) {
							model.setDirection(newDirection);
						}
					}

					model.setPosX(model.getPosX() + BOMB_ROLLING_SPEED * model.getDirectionXMultiplier());
					model.setPosY(model.getPosY() + BOMB_ROLLING_SPEED * model.getDirectionYMultiplier());

					final LevelComponent levelComponent = gameCoreHandler.getLevelModel().getComponents()[model.getComponentPosY()][model.getComponentPosX()];
					if (levelComponent.getItem() != null)
						levelComponent.setItem(null);
				} else {
					if (model.isDetonatingOnHit()) {
						model.setAboutToDetonate(true);
					} else {
						model.alignPosXToComponentCenter();
						model.alignPosYToComponentCenter();

						if (model.getType() == BombTypes.JELLY)
							model.setDirection(model.getDirection().getOpposite());
						else
							model.setPhase(BombPhases.STANDING);
					}
				}
				break;
		}
	}
}
