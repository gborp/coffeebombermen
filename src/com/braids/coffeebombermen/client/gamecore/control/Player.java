package com.braids.coffeebombermen.client.gamecore.control;

import java.util.EnumSet;
import java.util.List;

import com.braids.coffeebombermen.client.gamecore.Activities;
import com.braids.coffeebombermen.client.gamecore.BombPhases;
import com.braids.coffeebombermen.client.gamecore.BombTypes;
import com.braids.coffeebombermen.client.gamecore.CoreConsts;
import com.braids.coffeebombermen.client.gamecore.Directions;
import com.braids.coffeebombermen.client.gamecore.model.BombModel;
import com.braids.coffeebombermen.client.gamecore.model.PlayerModel;
import com.braids.coffeebombermen.client.gamecore.model.level.LevelComponent;
import com.braids.coffeebombermen.client.gamecore.model.level.LevelModel;
import com.braids.coffeebombermen.client.gamecore.robot.IRobot;
import com.braids.coffeebombermen.client.gamecore.robot.RobotTypes;
import com.braids.coffeebombermen.client.gamecore.robot.SimpleRobot;
import com.braids.coffeebombermen.client.sound.SoundEffect;
import com.braids.coffeebombermen.options.Diseases;
import com.braids.coffeebombermen.options.OptConsts;
import com.braids.coffeebombermen.options.OptConsts.Items;
import com.braids.coffeebombermen.options.OptConsts.PlayerControlKeys;
import com.braids.coffeebombermen.options.OptConsts.Walls;
import com.braids.coffeebombermen.utils.MathHelper;

/**
 * The class implements the control of a player of the GAME (NOT the the
 * application): its calculation, simulation during working and playing.
 */
public class Player {

	private static final int      PLACEABLE_WALLS     = 5;
	private static final long     SPIDER_BOMB_LATENCY = 6;
	/** The client index where this player belogns to. */
	private int                   clientIndex;
	/** The player index inside of its client. */
	private final int             playerIndex;
	/** The model of the player. */
	private final PlayerModel     model               = new PlayerModel();
	/** The robot of the player. */
	private IRobot                robot               = null;
	/** Reference to a model provider. */
	private final GameCoreHandler gameCoreHandler;

	private final boolean         ourClient;
	private long                  lastSpiderBomb;
	private boolean               detonatingOnHit;
	private boolean               useDeadBomb;
	private float                 explodingTimeMultiplier;

	/**
	 * Creates a new Player.
	 * 
	 * @param clientIndex
	 *            the client index where this player belongs to
	 * @param playerIndex
	 *            the player index inside of its client
	 * @param modelProvider
	 *            reference to a model provider
	 * @param modelController
	 *            reference to a model controller
	 */
	public Player(boolean ourClient, final int clientIndex, final int playerIndex, final GameCoreHandler gameCoreHandler, String name) {
		this.ourClient = ourClient;
		this.clientIndex = clientIndex;
		this.playerIndex = playerIndex;
		this.gameCoreHandler = gameCoreHandler;
		explodingTimeMultiplier = 1;
		model.setName(name);
	}

	/**
	 * Sets the client index where this player belongs to.
	 * 
	 * @param clientIndex
	 *            client index to be set
	 */
	public void setClientIndex(final int clientIndex) {
		this.clientIndex = clientIndex;
	}

	/**
	 * Returns the model of the player.
	 * 
	 * @return the model of the player
	 */
	public PlayerModel getModel() {
		return model;
	}

	/**
	 * Returns the robot of the player.
	 * 
	 * @return the robot of the player
	 */
	public IRobot getRobot() {
		return robot;
	}

	public void setRobot(int index, PlayerModel playerModel, RobotTypes type) {
		switch (type) {
			case SIMPLE:
				robot = new SimpleRobot(gameCoreHandler, index, playerModel);
				break;
			default:
				break;
		}
	}

	/**
	 * Makes basic initializations for starting a new round.
	 * 
	 * @param posX
	 *            the x coordinate of the initial position
	 * @param posY
	 *            the y coordinate of the initial position
	 */
	public void initForNextRound(final int posX, final int posY) {
		model.setPosX(posX);
		model.setPosY(posY);

		model.setDirection(Directions.DOWN);
		model.setActivity(Activities.STANDING);
		model.setVitality(CoreConsts.MAX_PLAYER_VITALITY);
		model.setPickedUpBombModel(null);

		model.accumulateableItemQuantitiesMap.putAll(gameCoreHandler.getLevelModel().getLevelOptions().getAccumulateableItemQuantitiesMap());
		model.setAllNonAccumItems(gameCoreHandler.getLevelModel().getLevelOptions().getHasNonAccumulateableItemsMap());
		model.pickedUpAccumulateableItems.clear();
		model.pickedUpNonAccumulateableItems.clear();
		model.setPlaceableWalls(0);

		model.setPlacableTriggeredBombs(model.hasNonAccumItem(Items.TRIGGER) ? model.accumulateableItemQuantitiesMap.get(Items.BOMB) : 0);

		for (final PlayerControlKeys playerControlKey : PlayerControlKeys.values()) {
			// Twice, so we delete the last key state also
			model.setControlKeyState(playerControlKey, false);
			model.setControlKeyState(playerControlKey, false);
		}

		if (getRobot() != null) {
			getRobot().initForNextRound();
		}

	}

	/**
	 * Performs operations which are requried by passing the time. Increases the
	 * number of iterations during the current activity, and switches to a new
	 * activity if it is needed to.
	 */
	public void nextIteration() {
		handleSpiderBomb(model.getActivity() == Activities.DYING);
		if (model.getActivity() == Activities.DYING) {
			model.nextIteration(); // We're just counting, we will drop out the
			// picked up items.

			if (model.getIterationCounter() == CoreConsts.DEAD_ITERATIONS_BEFORE_REPLACING_ITEMS) {
				for (final Items item : model.pickedUpNonAccumulateableItems) {
					gameCoreHandler.replaceItemOnLevel(item);
				}
				for (final Items item : model.pickedUpAccumulateableItems) {
					gameCoreHandler.replaceItemOnLevel(item);
				}
				// spider bomb in the place of the died bomberman

				int spiderBombsOnDeath = gameCoreHandler.getGlobalServerOptions().getThrowSpiderOnDeath();
				if (spiderBombsOnDeath > 0) {
					model.setSpiderBombEnabled(true);
					model.setSpiderBombRounds(spiderBombsOnDeath);
				}
			}
		} else {

			processDiseaseEffects();
			processActionsAndHandleActivityTransitions();

			stepPlayer(0);

			if (model.getIterationCounter() + 1 < model.getActivity().activityIterations) {
				model.nextIteration();
			} else if (model.getActivity().repeatable) {
				model.setIterationCounter(0);
			}

			// These keys can be interpreted as transitions also, we have to
			// care about last states by "stepping" them (implemented by
			// resetting them)
			model.setControlKeyState(PlayerControlKeys.FUNCTION1, model.getControlKeyState(PlayerControlKeys.FUNCTION1));
			model.setControlKeyState(PlayerControlKeys.FUNCTION2, model.getControlKeyState(PlayerControlKeys.FUNCTION2));
		}
	}

	public void setDetonatingOnHit(boolean detonatingOnHit) {
		this.detonatingOnHit = detonatingOnHit;
	}

	public boolean isDetonatingOnHit() {
		return detonatingOnHit;
	}

	public void setUseDeadBomb(boolean useDeadBomb) {
		this.useDeadBomb = useDeadBomb;
	}

	public boolean isUseDeadBomb() {
		return useDeadBomb;
	}

	public void setExplodingTimeMultiplier(float explodingTimeMultiplier) {
		this.explodingTimeMultiplier = explodingTimeMultiplier;
	}

	public float getExplodingTimeMultiplier() {
		return explodingTimeMultiplier;
	}

	private void handleSpiderBomb(boolean useDeadBombs) {
		long now = gameCoreHandler.getTick();
		if (!model.isSpiderBombEnabled() || (lastSpiderBomb + SPIDER_BOMB_LATENCY > now)) {
			return;
		}

		throwSpiderBomb(Directions.UP, useDeadBombs);
		throwSpiderBomb(Directions.RIGHT, useDeadBombs);
		throwSpiderBomb(Directions.DOWN, useDeadBombs);
		throwSpiderBomb(Directions.LEFT, useDeadBombs);

		int rounds = model.getSpiderBombRounds();

		if (rounds == 1) {
			model.setSpiderBombEnabled(false);
		} else {
			model.setSpiderBombRounds(rounds - 1);
		}

		lastSpiderBomb = now;
	}

	private void throwSpiderBomb(Directions direction, boolean useDeadBombs) {
		if (direction.equals(Directions.UP)) {
			if (model.getSpiderBombRounds() % 4 != 0) {
				return;
			}
		} else if (direction.equals(Directions.RIGHT)) {
			if (model.getSpiderBombRounds() % 4 != 1) {
				return;
			}
		} else if (direction.equals(Directions.DOWN)) {
			if (model.getSpiderBombRounds() % 4 != 2) {
				return;
			}
		} else if (direction.equals(Directions.LEFT)) {
			if (model.getSpiderBombRounds() % 4 != 3) {
				return;
			}
		}

		model.accumulateableItemQuantitiesMap.put(Items.BOMB, Math.max(0, model.accumulateableItemQuantitiesMap.get(Items.BOMB) - 1));
		final int playerComponentPosX = model.getComponentPosX();
		final int playerComponentPosY = model.getComponentPosY();
		int componentPosX = playerComponentPosX;
		int componentPosY = playerComponentPosY;

		final Bomb newBomb = new Bomb(model, gameCoreHandler);
		final BombModel newBombModel = newBomb.getModel();
		newBombModel.setDeadBomb(useDeadBombs);
		newBombModel
		        .setRange(model.hasNonAccumItem(Items.SUPER_FIRE) ? CoreConsts.SUPER_FIRE_RANGE : model.accumulateableItemQuantitiesMap.get(Items.FIRE) + 1);
		newBombModel.setPosX(componentPosX * CoreConsts.LEVEL_COMPONENT_GRANULARITY + CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2);
		newBombModel.setPosY(componentPosY * CoreConsts.LEVEL_COMPONENT_GRANULARITY + CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2);

		if (model.hasNonAccumItem(Items.JELLY)) {
			newBombModel.setType(BombTypes.JELLY);
		} else {
			newBombModel.setType(BombTypes.NORMAL);
		}
		newBombModel.setDirection(direction);
		newBombModel.setTickingIterations(CoreConsts.BOMB_DETONATION_ITERATIONS / 2);
		newBombModel.setPhase(BombPhases.FLYING);

		gameCoreHandler.validateAndSetFlyingTargetPosX(newBombModel, newBombModel.getPosX() + newBombModel.getDirectionXMultiplier()
		        * CoreConsts.BOMB_FLYING_DISTANCE);
		gameCoreHandler.validateAndSetFlyingTargetPosY(newBombModel, newBombModel.getPosY() + newBombModel.getDirectionYMultiplier()
		        * CoreConsts.BOMB_FLYING_DISTANCE);

		gameCoreHandler.addNewBomb(newBomb);
	}

	private void processDiseaseEffects() {
		if (model.hasDisease(Diseases.BOMB_SHITTING)) {
			handleFunction1WithoutBomb();
		}
	}

	/**
	 * Proccesses the player actions and handles the activitry transitions.
	 */
	private void processActionsAndHandleActivityTransitions() {
		switch (model.getActivity()) {

			case STANDING:
				if (model.isDirectionKeyPressed()) {
					model.setActivity(Activities.WALKING);
				}
				if ((model.getControlKeyState(PlayerControlKeys.FUNCTION1) && !model.getLastControlKeyState(PlayerControlKeys.FUNCTION1))) {
					handleFunction1WithoutBomb();
				} else if (model.getControlKeyState(PlayerControlKeys.FUNCTION2) && !model.getLastControlKeyState(PlayerControlKeys.FUNCTION2)) {
					handleFunction2();
				}
				break;

			case STANDING_WITH_BOMB:
				if (model.isDirectionKeyPressed()) {
					model.setActivity(Activities.WALKING_WITH_BOMB);
				}
				if (!model.getControlKeyState(PlayerControlKeys.FUNCTION1)) {
					throwBombAway();
				}
				break;

			case WALKING:
				if (!model.isDirectionKeyPressed()) {
					model.setActivity(Activities.STANDING);
				}
				if (model.getControlKeyState(PlayerControlKeys.FUNCTION1) && !model.getLastControlKeyState(PlayerControlKeys.FUNCTION1)) {
					handleFunction1WithoutBomb();
				} else if (model.getControlKeyState(PlayerControlKeys.FUNCTION2) && !model.getLastControlKeyState(PlayerControlKeys.FUNCTION2)) {
					handleFunction2();
				}
				break;

			case WALKING_WITH_BOMB:
				if (!model.isDirectionKeyPressed()) {
					model.setActivity(Activities.STANDING_WITH_BOMB);
				}
				if (!model.getControlKeyState(PlayerControlKeys.FUNCTION1)) {
					throwBombAway();
				}
				break;

			case KICKING:
				if (model.getIterationCounter() == model.getActivity().activityIterations - 1) {
					model.setActivity(model.isDirectionKeyPressed() ? Activities.WALKING : Activities.STANDING);
				}
				break;

			case KICKING_WITH_BOMB:
				if (model.getIterationCounter() == model.getActivity().activityIterations - 1) {
					model.setActivity(model.isDirectionKeyPressed() ? Activities.WALKING_WITH_BOMB : Activities.STANDING_WITH_BOMB);
				}
				break;

			case PUNCHING:
				if (model.getIterationCounter() == model.getActivity().activityIterations - 1) {
					model.setActivity(model.isDirectionKeyPressed() ? Activities.WALKING : Activities.STANDING);
				}
				break;

			case PICKING_UP:
				if (model.getIterationCounter() == model.getActivity().activityIterations - 1) {
					model.setActivity(model.isDirectionKeyPressed() ? Activities.WALKING_WITH_BOMB : Activities.STANDING_WITH_BOMB);
				}
				break;

			case DYING:
				break;

		}
	}

	/**
	 * Handles Function 1 key when we don't hold a bomb.<br>
	 * This means we place a bomb, if there is none at the component where we
	 * stand at (and we have bomb of course), or optionally places a lot more in
	 * front of us if there is one bomb under us and if we have
	 * Items.BOMB_SPRINKLE or if we dont have Items.BOMB_SPRINKLE but we have
	 * Items.BLUE_GLOVES, then we pick up the bomb being under us.
	 */
	private void handleFunction1WithoutBomb() {
		if (model.hasDisease(Diseases.CEASEFIRE)) {
			return;
		}

		final int playerComponentPosX = model.getComponentPosX();
		final int playerComponentPosY = model.getComponentPosY();
		int componentPosX = playerComponentPosX;
		int componentPosY = playerComponentPosY;
		int maxPlacableBombs = Math.min(1, model.accumulateableItemQuantitiesMap.get(Items.BOMB));

		if (gameCoreHandler.getLevelModel().getComponent(componentPosX, componentPosY).hasFire()) {
			return;
		}

		final Integer bombIndexAtComponentPosition = gameCoreHandler.getBombIndexAtComponentPosition(componentPosX, componentPosY);

		if (bombIndexAtComponentPosition != null) {
			if (model.hasNonAccumItem(Items.BLUE_GLOVES)) {
				if (gameCoreHandler.getBombModels().get(bombIndexAtComponentPosition).getOwnerPlayer() != model) {
					return; // We can only pick up our own bombs
				}
				model.setPickedUpBombModel(gameCoreHandler.getBombModels().get(bombIndexAtComponentPosition));
				gameCoreHandler.removeBombAtIndex(bombIndexAtComponentPosition);
				model.setActivity(Activities.PICKING_UP);
				return;
			}
			if (model.hasNonAccumItem(Items.BOMB_SPRINKLE)) {
				maxPlacableBombs = model.accumulateableItemQuantitiesMap.get(Items.BOMB);
				// The position of the first bomb is ahead of us.
				componentPosX += model.getDirectionXMultiplier();
				componentPosY += model.getDirectionYMultiplier();
			}
		}

		LevelModel levelModel = gameCoreHandler.getLevelModel();

		int bombsCount = model.accumulateableItemQuantitiesMap.get(Items.BOMB);
		for (int i = 0; i < maxPlacableBombs; i++) {
			LevelComponent comp = levelModel.getComponent(componentPosX, componentPosY);
			final Walls wallInPosition = comp.getWall();

			if (gameCoreHandler.isBombAtComponentPosition(componentPosX, componentPosY) || (wallInPosition != Walls.EMPTY)
			        || ((wallInPosition == Walls.EMPTY) && (comp.getItem() != null))) {
				break;
			}
			if ((componentPosX != playerComponentPosX) || (componentPosY != playerComponentPosY)) {
				if (gameCoreHandler.isPlayerAtComponentPositionExcludePlayer(componentPosX, componentPosY, model)) {
					break;
				}
			}

			model.accumulateableItemQuantitiesMap.put(Items.BOMB, --bombsCount);
			final Bomb newBomb = new Bomb(model, gameCoreHandler);
			final BombModel newBombModel = newBomb.getModel();
			newBombModel.setDetonatingOnHit(detonatingOnHit);
			newBombModel.setDeadBomb(useDeadBomb);
			newBombModel.setExplodingTimeMultiplier(explodingTimeMultiplier);

			int bombRange = model.hasNonAccumItem(Items.SUPER_FIRE) ? CoreConsts.SUPER_FIRE_RANGE : model.accumulateableItemQuantitiesMap.get(Items.FIRE) + 1;

			if (model.getOwnedDiseases().containsKey(Diseases.SHORT_RANGE)) {
				bombRange = 2;
			}

			newBombModel.setRange(bombRange);
			newBombModel.setPosX(componentPosX * CoreConsts.LEVEL_COMPONENT_GRANULARITY + CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2);
			newBombModel.setPosY(componentPosY * CoreConsts.LEVEL_COMPONENT_GRANULARITY + CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2);

			if (model.hasNonAccumItem(Items.JELLY)) {
				newBombModel.setType(BombTypes.JELLY);
			} else if (model.hasNonAccumItem(Items.TRIGGER) && (model.getPlacableTriggeredBombs() > 0)) {
				newBombModel.setType(BombTypes.TRIGGERED);
				model.setPlacableTriggeredBombs(model.getPlacableTriggeredBombs() - 1);
			} else {
				newBombModel.setType(BombTypes.NORMAL);
			}

			if (model.getOwnedDiseases().containsKey(Diseases.FAST_DETONATION)) {
				newBombModel.setExplodingTimeMultiplier(0.25f);
			}

			gameCoreHandler.addNewBomb(newBomb);

			componentPosX += model.getDirectionXMultiplier();
			componentPosY += model.getDirectionYMultiplier();
		}
	}

	/**
	 * Throws away the picked up bomb.
	 */
	private void throwBombAway() {
		final BombModel bombModel = model.getPickedUpBombModel();

		// bombModel.setTickingIterations(0); // Thrown away bombs start ticking
		// from the beginning again.
		bombModel.setDirection(model.getDirection()); // We throw in our
		// direction
		bombModel.setPosX(model.getComponentPosX() * CoreConsts.LEVEL_COMPONENT_GRANULARITY + CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2);
		bombModel.setPosY(model.getComponentPosY() * CoreConsts.LEVEL_COMPONENT_GRANULARITY + CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2);

		bombModel.setPhase(BombPhases.FLYING);

		gameCoreHandler.validateAndSetFlyingTargetPosX(bombModel, bombModel.getPosX() + bombModel.getDirectionXMultiplier() * CoreConsts.BOMB_FLYING_DISTANCE);
		gameCoreHandler.validateAndSetFlyingTargetPosY(bombModel, bombModel.getPosY() + bombModel.getDirectionYMultiplier() * CoreConsts.BOMB_FLYING_DISTANCE);

		gameCoreHandler.addNewBomb(new Bomb(bombModel, gameCoreHandler));
		model.setPickedUpBombModel(null);

		model.setActivity(model.getActivity() == Activities.STANDING_WITH_BOMB ? Activities.STANDING : Activities.WALKING);
	}

	/**
	 * Handles Function 2.<br>
	 * This entirely depens on which of the items we have.<br>
	 * If we have Items.BOXING_GLOVES, we punch the bomb in front of us. If we
	 * have Items.TRIGGER, we detonate the earliest triggered bomb. If we have
	 * Items.WALL_BUILDING, we build a wall in front of us.
	 */
	private void handleFunction2() {
		// First of all, function 2 stops normal rolling bombs and triggers
		// triggerable bombs.
		for (final BombModel bombModel : gameCoreHandler.getBombModels()) {
			if ((bombModel.getOwnerPlayer() == model) && (bombModel.getType() == BombTypes.NORMAL) && (bombModel.getPhase() == BombPhases.ROLLING)) {
				bombModel.setPhase(BombPhases.STANDING);
				bombModel.alignPosXToComponentCenter();
				bombModel.alignPosYToComponentCenter();
			}
		}

		if (model.hasNonAccumItem(Items.BOXING_GLOVES)) {
			model.setActivity(Activities.PUNCHING);

			final Integer bombIndexAhead = gameCoreHandler.getBombIndexAtComponentPosition(model.getComponentPosX() + model.getDirectionXMultiplier(), model
			        .getComponentPosY()
			        + model.getDirectionYMultiplier());
			if (bombIndexAhead != null) {
				final BombModel bombModel = gameCoreHandler.getBombModels().get(bombIndexAhead);
				bombModel.setDirection(model.getDirection()); // We punch in our
				// direction
				bombModel.setPosX(bombModel.getComponentPosX() * CoreConsts.LEVEL_COMPONENT_GRANULARITY + CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2);
				bombModel.setPosY(bombModel.getComponentPosY() * CoreConsts.LEVEL_COMPONENT_GRANULARITY + CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2);
				bombModel.setPhase(BombPhases.FLYING);

				gameCoreHandler.validateAndSetFlyingTargetPosX(bombModel, bombModel.getPosX() + bombModel.getDirectionXMultiplier()
				        * CoreConsts.BOMB_FLYING_DISTANCE);
				gameCoreHandler.validateAndSetFlyingTargetPosY(bombModel, bombModel.getPosY() + bombModel.getDirectionYMultiplier()
				        * CoreConsts.BOMB_FLYING_DISTANCE);
			}
		}

		else if (model.hasNonAccumItem(Items.TRIGGER)) {
			for (final BombModel bombModel : gameCoreHandler.getBombModels()) {
				if ((bombModel.getOwnerPlayer() == model) && (bombModel.getType() == BombTypes.TRIGGERED) && (bombModel.getPhase() != BombPhases.FLYING)) {
					bombModel.setAboutToDetonate(true);
					break; // Function2 only detonates 1 triggered bomb
				}
			}
		}

		else if (model.hasNonAccumItem(Items.WALL_BUILDING)) {
			final int componentPosX = model.getComponentPosX() + model.getDirectionXMultiplier();
			final int componentPosY = model.getComponentPosY() + model.getDirectionYMultiplier();
			if (isComponentPositionFreeForWallBuilding(componentPosX, componentPosY) && (model.getPlaceableWalls() > 0)) {
				gameCoreHandler.getLevelModel().getComponent(componentPosX, componentPosY).setWall(Walls.BRICK);
				model.setPlaceableWalls(model.getPlaceableWalls() - 1);
				SoundEffect.PLACE_WALL.play();
				if (model.getPlaceableWalls() == 0) {
					model.setNonAccumItem(Items.WALL_BUILDING, false);
				}
			}
		}
	}

	/**
	 * Tells whether a component is free for building a brick wall on it.<br>
	 * A brick wall can be built in a position if it's empty (no bombs, no
	 * players, no item can be found on that), and no object is hanging down
	 * into it (bombs and players can't even hang down into it).
	 * 
	 * @param componentPosX
	 *            x coordinate of the component to check
	 * @param componentPosY
	 *            y coordinate of the component to check
	 * @return true, if a brick wall can be built in the specified position;
	 *         false otherwise
	 */
	private boolean isComponentPositionFreeForWallBuilding(final int componentPosX, final int componentPosY) {
		final LevelComponent levelComponent = gameCoreHandler.getLevelModel().getComponent(componentPosX, componentPosY);

		if ((levelComponent.getWall() != Walls.EMPTY) || ((levelComponent.getWall() == Walls.EMPTY) && (levelComponent.getItem() != null))) {
			return false;
		}

		if (gameCoreHandler.isBombAtComponentPosition(componentPosX, componentPosY)) {
			return false;
		}

		if (gameCoreHandler.isPlayerAtComponentPositionExcludePlayer(componentPosX, componentPosY, null)) {
			return false;
		}

		return true;
	}

	/**
	 * Determines the direction of the player, and makes a step.<br>
	 * This method can invoke itself if it realizes that bomberman step had to
	 * be cut in order of movement correction to be able to turn in a direction.
	 * By compensation of the cut, the player will be stepped again.
	 * 
	 * @param invocationDepth
	 *            the invocation depth 'cause this method can invoce itself
	 */
	private void stepPlayer(final int invocationDepth) {
		boolean wasStepCutInOrderToTurn = false;

		final Activities activity = model.getActivity(); // Shortcut to the
		// players activity

		// if player can step based on the activity
		if ((activity == Activities.WALKING) || (activity == Activities.WALKING_WITH_BOMB)
		        || ((activity == Activities.PUNCHING) && model.isDirectionKeyPressed())) {
			// First of all we determine in what direction we will face and/or
			// move to
			boolean movementCorrectionActivated = determineNewDirection();

			int speed = CoreConsts.BOMBERMAN_BASIC_SPEED + model.getEffectiveRollerSkates() * CoreConsts.BOBMERMAN_ROLLER_SKATES_SPEED_INCREMENT;
			if (speed > CoreConsts.BOBMERMAN_MAX_SPEED) {
				speed = CoreConsts.BOBMERMAN_MAX_SPEED;
			}

			boolean needsToBeContained = false;
			final int posXAhead = model.getPosX() + model.getDirectionXMultiplier() * CoreConsts.LEVEL_COMPONENT_GRANULARITY;
			final int posYAhead = model.getPosY() + model.getDirectionYMultiplier() * CoreConsts.LEVEL_COMPONENT_GRANULARITY;
			if (movementCorrectionActivated || (!movementCorrectionActivated && !canPlayerStepToPosition(posXAhead, posYAhead))) {
				// cannot also, if we have obstruction ahead
				needsToBeContained = true;
			}

			if (needsToBeContained) {
				int newSpeed = -1;
				final boolean bombAhead = gameCoreHandler.isBombAtComponentPosition(posXAhead / CoreConsts.LEVEL_COMPONENT_GRANULARITY, posYAhead
				        / CoreConsts.LEVEL_COMPONENT_GRANULARITY);

				switch (model.getDirection()) {
					case LEFT:
						if (bombAhead && (model.getPosX() % CoreConsts.LEVEL_COMPONENT_GRANULARITY < CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2)) {
							newSpeed = 0;
						} else {
							newSpeed = speed
							        - (CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2 - (model.getPosX() - speed) % CoreConsts.LEVEL_COMPONENT_GRANULARITY);
						}
						break;
					case RIGHT:
						if (bombAhead && (model.getPosX() % CoreConsts.LEVEL_COMPONENT_GRANULARITY > CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2)) {
							newSpeed = 0;
						} else {
							newSpeed = speed
							        - ((model.getPosX() + speed) % CoreConsts.LEVEL_COMPONENT_GRANULARITY - CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2);
						}
						break;
					case UP:
						if (bombAhead && (model.getPosY() % CoreConsts.LEVEL_COMPONENT_GRANULARITY < CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2)) {
							newSpeed = 0;
						} else {
							newSpeed = speed
							        - (CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2 - (model.getPosY() - speed) % CoreConsts.LEVEL_COMPONENT_GRANULARITY);
						}
						break;
					case DOWN:
						if (bombAhead && (model.getPosY() % CoreConsts.LEVEL_COMPONENT_GRANULARITY > CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2)) {
							newSpeed = 0;
						} else {
							newSpeed = speed
							        - ((model.getPosY() + speed) % CoreConsts.LEVEL_COMPONENT_GRANULARITY - CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2);
						}
						break;
				}

				if ((newSpeed >= 0) && (newSpeed < speed)) {
					// If it's negative, it's beyond one level component, but
					// that case we surly don't need to change speed
					if (movementCorrectionActivated) {
						wasStepCutInOrderToTurn = true;
					}
					speed = newSpeed;
				}
			}

			LevelModel levelModel = gameCoreHandler.getLevelModel();
			if (speed > 0) { // If we can, we step...
				// And finally we make the step
				model.setPosX(model.getPosX() + model.getDirectionXMultiplier() * speed);
				model.setPosY(model.getPosY() + model.getDirectionYMultiplier() * speed);
				checkAndHandleItemPickingUp();
			} else
			// ...else if there is a gateway ahead...
			if (Walls.GATEWAY_ENTRANCE == levelModel.getComponent(posXAhead / CoreConsts.LEVEL_COMPONENT_GRANULARITY,
			        posYAhead / CoreConsts.LEVEL_COMPONENT_GRANULARITY).getWall()) {

				int pos = MathHelper.randomInt(levelModel.getNofGatewayExit() - 1);
				Integer posX = levelModel.getGatewayExitPositionX(pos);
				Integer posY = levelModel.getGatewayExitPositionY(pos);

				int nextPosX;
				int nextPosY;
				Directions direction;
				if (posX == 0) {
					nextPosX = 1;
					nextPosY = posY;
					direction = Directions.RIGHT;
				} else if (posX == levelModel.getWidth() - 1) {
					nextPosX = levelModel.getWidth() - 2;
					nextPosY = posY;
					direction = Directions.LEFT;
				} else if (posY == 0) {
					nextPosX = posX;
					nextPosY = 1;
					direction = Directions.DOWN;
				} else {
					nextPosX = posX;
					nextPosY = levelModel.getHeight() - 2;
					direction = Directions.UP;
				}

				Walls wall = levelModel.getComponent(nextPosX, nextPosY).getWall();
				if ((wall == Walls.EMPTY) || ((wall == Walls.BRICK) && model.hasNonAccumItem(Items.WALL_CLIMBING))) {
					// move player to the other side
					model.setPosY(nextPosY * CoreConsts.LEVEL_COMPONENT_GRANULARITY + CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2);
					model.setPosX(nextPosX * CoreConsts.LEVEL_COMPONENT_GRANULARITY + CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2);
					model.setDirection(direction);
					checkAndHandleItemPickingUp();
				}
			} else {// ...else we check for kick
				if (model.hasNonAccumItem(Items.BOOTS) || model.hasNonAccumItem(Items.CRAZY_BOOTS)) {
					tryToKick();
				}
			}
		}

		if (wasStepCutInOrderToTurn && (invocationDepth == 0)) {
			stepPlayer(invocationDepth + 1); // Without this it gives a feeling
			// of stucking for a moment on
			// turns!!
		}
	}

	/**
	 * Check whether we stand on an item, and handles the picking up.
	 */
	private void checkAndHandleItemPickingUp() {
		final LevelComponent levelComponent = gameCoreHandler.getLevelModel().getComponent(model.getComponentPosX(), model.getComponentPosY());

		if ((levelComponent.getWall() == Walls.EMPTY) && (levelComponent.getItem() != null) && !levelComponent.hasFire()) {
			final Items item = levelComponent.getItem();

			if (ourClient) {
				SoundEffect.PICKUP.play();
			}

			if (OptConsts.ACCUMULATEABLE_ITEMS.contains(item)) {
				if (item != Items.HEART) { // We don't accumulate HEARTs.
					model.accumulateableItemQuantitiesMap.put(item, model.accumulateableItemQuantitiesMap.get(item) + 1);
					model.pickedUpAccumulateableItems.add(item);
				}
			} else {
				model.setNonAccumItem(item, true);
				if (!model.pickedUpNonAccumulateableItems.contains(item)) {
					model.pickedUpNonAccumulateableItems.add(item);
				}

				final EnumSet<Items> neutralizedItems = OptConsts.NEUTRALIZER_ITEMS_MAP.get(item);
				if (neutralizedItems != null) {
					for (final Items neutralizedItem : neutralizedItems) {
						if (model.hasNonAccumItem(neutralizedItem)) {

							model.setNonAccumItem(neutralizedItem, false);
							if (model.pickedUpNonAccumulateableItems.remove(neutralizedItem)) {
								gameCoreHandler.replaceItemOnLevel(neutralizedItem);
							}

							// If Trigger has been thrown out the window, we
							// have to transform triggered bombs back to normal
							// or jelly.
							if (neutralizedItem == Items.TRIGGER) {
								for (final BombModel bombModel : gameCoreHandler.getBombModels()) {
									if ((bombModel.getOwnerPlayer() == model) && (bombModel.getType() == BombTypes.TRIGGERED)) {
										bombModel.setType(model.hasNonAccumItem(Items.JELLY) ? BombTypes.JELLY : BombTypes.NORMAL);
										bombModel.setIterationCounter(0);
										bombModel.setTickingIterations(0);
									}
								}
							}
						}
					}
				}
			}

			levelComponent.setItem(null);

			// Special things to do when an item is picked up
			switch (item) {
				case TRIGGER:
					model.setPlacableTriggeredBombs(model.accumulateableItemQuantitiesMap.get(Items.BOMB));
					for (final BombModel bombModel : gameCoreHandler.getBombModels()) {
						if (bombModel.getOwnerPlayer() == model) {
							model.setPlacableTriggeredBombs(model.getPlacableTriggeredBombs() + 1);
						}
					}
					break;
				case BOMB:
					if (model.hasNonAccumItem(Items.TRIGGER)) {
						model.setPlacableTriggeredBombs(model.getPlacableTriggeredBombs() + 1);
					}
					break;
				case HEART:
					model.setVitality(Math.min(model.getVitality() + CoreConsts.HEART_VITALITY, CoreConsts.MAX_PLAYER_VITALITY * 2));
					break;
				case WALL_BUILDING:
					model.setPlaceableWalls(PLACEABLE_WALLS);
					break;
				case SPIDER_BOMB:
					model.setSpiderBombEnabled(true);
					break;
				case DISEASE: {
					getModel().addDisease(gameCoreHandler.getGlobalServerOptions().getLevelOptions().getRandomDisease(),
					        gameCoreHandler.getTick() + PlayerModel.DISEASE_DURATION);
					break;
				}
				case SUPER_DISEASE: {
					int numberOfMassDiseases = MathHelper.randomInt(2) + 1;

					for (int massDiseaseCounter = 0; massDiseaseCounter < numberOfMassDiseases; massDiseaseCounter++) {
						getModel().addDisease(gameCoreHandler.getGlobalServerOptions().getLevelOptions().getRandomDisease(),
						        gameCoreHandler.getTick() + PlayerModel.SUPER_DISEASE_DURATION);
					}
					break;
				}
				case SWAP_TELEPORT:
					List<PlayerModel> lstPlayers;
					if (gameCoreHandler.getGlobalServerOptions().isSwapOnlyLivingPlayer()) {
						lstPlayers = gameCoreHandler.getAllLivingPlayerModels();
					} else {
						lstPlayers = gameCoreHandler.getAllPlayerModels();
					}
					if (lstPlayers.size() <= 1) {
						break;
					}
					int swapWith = playerIndex;
					while (swapWith == playerIndex) {
						swapWith = MathHelper.randomInt(lstPlayers.size() - 1);
					}
					PlayerModel swapModel = lstPlayers.get(swapWith);

					int myPosX = getModel().getPosX();
					int myPosY = getModel().getPosY();

					getModel().setPosX(swapModel.getPosX());
					getModel().setPosY(swapModel.getPosY());

					swapModel.setPosX(myPosX);
					swapModel.setPosY(myPosY);
					break;
			}
		}
	}

	/**
	 * Determines and sets the new direction of the player.
	 * 
	 * @return true if movement correction have to be used for the new
	 *         direction; false otherwise
	 */
	private boolean determineNewDirection() {
		final int posX = model.getPosX(); // Shortcut to the posX of the player
		final int posY = model.getPosY(); // Shortcut to the posY of the player

		// There are 2 kinds of movement correction:
		// The first is taking us away from the component where we stand on in
		// order to turn on the next component.
		// The second is taking us toward the center of the component we're
		// standing on in order to be able to turn on it.

		final int movementCorrectionSensitivity = CoreConsts.LEVEL_COMPONENT_GRANULARITY
		        * gameCoreHandler.getClientsPublicClientOptions().get(clientIndex).movementCorrectionSensitivities[playerIndex] / 200;

		if (model.getControlKeyState(PlayerControlKeys.DOWN)) {
			model.setDirection(Directions.DOWN);

			// The first kind of movement correction
			if (!canPlayerStepToPosition(posX, posY + (CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2 + 1))) {
				if (posX % CoreConsts.LEVEL_COMPONENT_GRANULARITY < movementCorrectionSensitivity) {
					if (canPlayerStepToPosition(posX - CoreConsts.LEVEL_COMPONENT_GRANULARITY, posY)
					        && canPlayerStepToPosition(posX - CoreConsts.LEVEL_COMPONENT_GRANULARITY, posY + CoreConsts.LEVEL_COMPONENT_GRANULARITY)) {
						model.setDirection(Directions.LEFT);
						return true; // Movement correction is ACTIVATED
					}
				}

				if (posX % CoreConsts.LEVEL_COMPONENT_GRANULARITY >= CoreConsts.LEVEL_COMPONENT_GRANULARITY - movementCorrectionSensitivity) {

					if (canPlayerStepToPosition(posX + CoreConsts.LEVEL_COMPONENT_GRANULARITY, posY)
					        && canPlayerStepToPosition(posX + CoreConsts.LEVEL_COMPONENT_GRANULARITY, posY + CoreConsts.LEVEL_COMPONENT_GRANULARITY)) {
						model.setDirection(Directions.RIGHT);
						return true; // Movement correction is ACTIVATED
					}
				}
			}
			// The second kind of movement correction
			else { // We could move the specified direction, but we have a side
				// obstrucion and we're closer than
				// CoreConsts. LEVEL_COMPONENT_GRANULARITY/2
				if (posX % CoreConsts.LEVEL_COMPONENT_GRANULARITY < CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2) {
					if (!canPlayerStepToPosition(posX - CoreConsts.LEVEL_COMPONENT_GRANULARITY, posY + CoreConsts.LEVEL_COMPONENT_GRANULARITY)) {
						model.setDirection(Directions.RIGHT);
						return true; // Movement correction is ACTIVATED
					}
				}
				if (posX % CoreConsts.LEVEL_COMPONENT_GRANULARITY > CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2) {
					if (!canPlayerStepToPosition(posX + CoreConsts.LEVEL_COMPONENT_GRANULARITY, posY + CoreConsts.LEVEL_COMPONENT_GRANULARITY)) {
						model.setDirection(Directions.LEFT);
						return true; // Movement correction is ACTIVATED
					}
				}
			}
		} else if (model.getControlKeyState(PlayerControlKeys.UP)) {
			model.setDirection(Directions.UP);

			if (!canPlayerStepToPosition(posX, posY - (CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2 + 1))) { // The

				if (posX % CoreConsts.LEVEL_COMPONENT_GRANULARITY < movementCorrectionSensitivity) {
					if (canPlayerStepToPosition(posX - CoreConsts.LEVEL_COMPONENT_GRANULARITY, posY)
					        && canPlayerStepToPosition(posX - CoreConsts.LEVEL_COMPONENT_GRANULARITY, posY - CoreConsts.LEVEL_COMPONENT_GRANULARITY)) {
						model.setDirection(Directions.LEFT);
						return true; // Movement correction is ACTIVATED
					}
				}

				if (posX % CoreConsts.LEVEL_COMPONENT_GRANULARITY >= CoreConsts.LEVEL_COMPONENT_GRANULARITY - movementCorrectionSensitivity) {
					if (canPlayerStepToPosition(posX + CoreConsts.LEVEL_COMPONENT_GRANULARITY, posY)
					        && canPlayerStepToPosition(posX + CoreConsts.LEVEL_COMPONENT_GRANULARITY, posY - CoreConsts.LEVEL_COMPONENT_GRANULARITY)) {
						// ...and the position where the movement correction
						// wants to take us to is allowed
						model.setDirection(Directions.RIGHT);
						return true; // Movement correction is ACTIVATED
					}
				}
			}
			// The second kind of movement correction
			else { // We could move the specified direction, but we have a side
				// obstrucion and we're closer than
				// CoreConsts. LEVEL_COMPONENT_GRANULARITY/2
				if (posX % CoreConsts.LEVEL_COMPONENT_GRANULARITY < CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2) {
					if (!canPlayerStepToPosition(posX - CoreConsts.LEVEL_COMPONENT_GRANULARITY, posY - CoreConsts.LEVEL_COMPONENT_GRANULARITY)) {
						model.setDirection(Directions.RIGHT);
						return true; // Movement correction is ACTIVATED
					}
				}
				if (posX % CoreConsts.LEVEL_COMPONENT_GRANULARITY > CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2) {
					if (!canPlayerStepToPosition(posX + CoreConsts.LEVEL_COMPONENT_GRANULARITY, posY - CoreConsts.LEVEL_COMPONENT_GRANULARITY)) {
						model.setDirection(Directions.LEFT);
						return true; // Movement correction is ACTIVATED
					}
				}
			}
		} else if (model.getControlKeyState(PlayerControlKeys.LEFT)) {
			model.setDirection(Directions.LEFT);

			if (!canPlayerStepToPosition(posX - (CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2 + 1), posY)) { // The
				// specifieddirection is unreachable for movement, try the
				// movement correction function...
				if (posY % CoreConsts.LEVEL_COMPONENT_GRANULARITY < movementCorrectionSensitivity) {

					if (canPlayerStepToPosition(posX, posY - CoreConsts.LEVEL_COMPONENT_GRANULARITY)
					        && canPlayerStepToPosition(posX - CoreConsts.LEVEL_COMPONENT_GRANULARITY, posY - CoreConsts.LEVEL_COMPONENT_GRANULARITY)) {
						model.setDirection(Directions.UP);
						return true; // Movement correction is ACTIVATED
					}
				}

				if (posY % CoreConsts.LEVEL_COMPONENT_GRANULARITY >= CoreConsts.LEVEL_COMPONENT_GRANULARITY - movementCorrectionSensitivity) {

					if (canPlayerStepToPosition(posX, posY + CoreConsts.LEVEL_COMPONENT_GRANULARITY)
					        && canPlayerStepToPosition(posX - CoreConsts.LEVEL_COMPONENT_GRANULARITY, posY + CoreConsts.LEVEL_COMPONENT_GRANULARITY)) {
						model.setDirection(Directions.DOWN);
						return true; // Movement correction is ACTIVATED
					}
				}
			}
			// The second kind of movement correction
			else { // We could move the specified direction, but we have a side
				// obstrucion and we're closer than
				// CoreConsts. LEVEL_COMPONENT_GRANULARITY/2
				if (posY % CoreConsts.LEVEL_COMPONENT_GRANULARITY < CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2) {
					if (!canPlayerStepToPosition(posX - CoreConsts.LEVEL_COMPONENT_GRANULARITY, posY - CoreConsts.LEVEL_COMPONENT_GRANULARITY)) {
						model.setDirection(Directions.DOWN);
						return true; // Movement correction is ACTIVATED
					}
				}
				if (posY % CoreConsts.LEVEL_COMPONENT_GRANULARITY > CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2) {
					if (!canPlayerStepToPosition(posX - CoreConsts.LEVEL_COMPONENT_GRANULARITY, posY + CoreConsts.LEVEL_COMPONENT_GRANULARITY)) {
						model.setDirection(Directions.UP);
						return true; // Movement correction is ACTIVATED
					}
				}
			}
		} else if (model.getControlKeyState(PlayerControlKeys.RIGHT)) {
			model.setDirection(Directions.RIGHT);

			if (!canPlayerStepToPosition(posX + (CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2 + 1), posY)) {
				if (posY % CoreConsts.LEVEL_COMPONENT_GRANULARITY < movementCorrectionSensitivity) {
					if (canPlayerStepToPosition(posX, posY - CoreConsts.LEVEL_COMPONENT_GRANULARITY)
					        && canPlayerStepToPosition(posX + CoreConsts.LEVEL_COMPONENT_GRANULARITY, posY - CoreConsts.LEVEL_COMPONENT_GRANULARITY)) {
						model.setDirection(Directions.UP);
						return true; // Movement correction is ACTIVATED
					}
				}

				if (posY % CoreConsts.LEVEL_COMPONENT_GRANULARITY >= CoreConsts.LEVEL_COMPONENT_GRANULARITY - movementCorrectionSensitivity) {
					if (canPlayerStepToPosition(posX, posY + CoreConsts.LEVEL_COMPONENT_GRANULARITY) // If
					        && canPlayerStepToPosition(posX + CoreConsts.LEVEL_COMPONENT_GRANULARITY, posY + CoreConsts.LEVEL_COMPONENT_GRANULARITY)) { // ...and
						model.setDirection(Directions.DOWN);
						return true; // Movement correction is ACTIVATED
					}
				}
			}
			// The second kind of movement correction
			else { // We could move the specified direction, but we have a side
				// obstrucion and we're closer than
				// CoreConsts.LEVEL_COMPONENT_GRANULARITY/2
				if (posY % CoreConsts.LEVEL_COMPONENT_GRANULARITY < CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2) {
					if (!canPlayerStepToPosition(posX + CoreConsts.LEVEL_COMPONENT_GRANULARITY, posY - CoreConsts.LEVEL_COMPONENT_GRANULARITY)) {
						model.setDirection(Directions.DOWN);
						return true; // Movement correction is ACTIVATED
					}
				}
				if (posY % CoreConsts.LEVEL_COMPONENT_GRANULARITY > CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2) {
					if (!canPlayerStepToPosition(posX + CoreConsts.LEVEL_COMPONENT_GRANULARITY, posY + CoreConsts.LEVEL_COMPONENT_GRANULARITY)) {
						model.setDirection(Directions.UP);
						return true; // Movement correction is ACTIVATED
					}
				}
			}
		}

		return false; // No movement correction have to be used
	}

	private void pushWall(int cx, int cy, int wx, int wy, int dx, int dy) {
		LevelComponent compToPush = gameCoreHandler.getLevelModel().getComponent(wx, wy);

		boolean explosionsAtPos = compToPush.hasFire();
		boolean bombAtPos = gameCoreHandler.isBombAtComponentPosition(wx, wy);
		boolean playerAtPos = gameCoreHandler.isPlayerAtComponentPositionExcludePlayer(wx, wy, model);

		if (!bombAtPos && !playerAtPos && !explosionsAtPos) {
			if (compToPush.getWall() == Walls.EMPTY) {
				LevelComponent oldComp = gameCoreHandler.getLevelModel().getComponent(cx, cy);
				gameCoreHandler.getLevelModel().setComponent(oldComp, wx, wy);
				gameCoreHandler.getLevelModel().setComponent(compToPush, cx, cy);
			} else if (compToPush.getWall() == Walls.BRICK) {
				pushWall(cx, cy, wx + dx, wy + dy, dx, dy);
			}
		}
	}

	/**
	 * Checks whether the player can step onto a component of the level
	 * specified by position.<br>
	 * Implementation is simply calling the canPlayerStepOntoComponent() with
	 * the component coordinates specified by the position.
	 * 
	 * @param posX
	 *            x coordinate of the position
	 * @param posY
	 *            y coordinate of the position
	 * @return true, if the player can step onto the component specified by the
	 *         position; false otherwise
	 */
	private boolean canPlayerStepToPosition(int posX, int posY) {
		int componentPosX = posX / CoreConsts.LEVEL_COMPONENT_GRANULARITY;
		int componentPosY = posY / CoreConsts.LEVEL_COMPONENT_GRANULARITY;

		Walls wall = gameCoreHandler.getLevelModel().getComponent(componentPosX, componentPosY).getWall();

		if (model.hasDisease(Diseases.BODY_BUILDER)) {
			int dx = componentPosX - model.getComponentPosX();
			int dy = componentPosY - model.getComponentPosY();

			if (((dx != 0) || (dy != 0)) && (wall == Walls.BRICK) && (componentPosX > 1) && (componentPosY > 1)
			        && (componentPosX < gameCoreHandler.getLevelModel().getWidth() - 2) && (componentPosY < gameCoreHandler.getLevelModel().getHeight() - 2)) {

				int wx = componentPosX + dx;
				int wy = componentPosY + dy;
				pushWall(componentPosX, componentPosY, wx, wy, dx, dy);
			}
		}

		if (model.hasNonAccumItem(Items.WALL_CLIMBING)) {
			if ((wall == Walls.CONCRETE) || (wall == Walls.DEATH) || (wall == Walls.DEATH_WARN) || (wall == Walls.GATEWAY_ENTRANCE)
			        || (wall == Walls.GATEWAY_EXIT)) {
				return false;
			}
		} else {
			if (wall != Walls.EMPTY) {
				return false;
			}
		}

		if (gameCoreHandler.isBombAtComponentPosition(componentPosX, componentPosY)) {
			return false;
		}

		return true;
	}

	/**
	 * Tries to kick.
	 */
	private void tryToKick() {
		final int componentPosXAhead = model.getComponentPosX() + model.getDirectionXMultiplier();
		final int componentPosYAhead = model.getComponentPosY() + model.getDirectionYMultiplier();

		final Integer bombIndexAhead = gameCoreHandler.getBombIndexAtComponentPosition(componentPosXAhead, componentPosYAhead);
		if (bombIndexAhead == null) {
			return;
		}

		final BombModel bombModel = gameCoreHandler.getBombModels().get(bombIndexAhead);

		final int componentPosXAheadAhead = componentPosXAhead + model.getDirectionXMultiplier();
		final int componentPosYAheadAhead = componentPosYAhead + model.getDirectionYMultiplier();

		if (!gameCoreHandler.canBombRollToComponentPosition(bombModel, componentPosXAheadAhead, componentPosYAheadAhead)) {
			return;
		}

		// Activity can be PUNCHING!!!!
		model.setActivity(model.getActivity() == Activities.WALKING_WITH_BOMB ? Activities.KICKING_WITH_BOMB : Activities.KICKING);
		bombModel.setPhase(BombPhases.ROLLING);
		bombModel.setDirection(model.getDirection()); // We punch in our
		// direction
		if (getModel().hasNonAccumItem(Items.CRAZY_BOOTS)) {
			bombModel.setCrazyPercent(0.2f);
		} else {
			bombModel.setCrazyPercent(0);
		}

		// We align the bomb to the center based on the kicking direction
		if (bombModel.getDirectionXMultiplier() != 0) {
			bombModel.alignPosYToComponentCenter();
		}
		if (bombModel.getDirectionYMultiplier() != 0) {
			bombModel.alignPosXToComponentCenter();
		}
	}

}
