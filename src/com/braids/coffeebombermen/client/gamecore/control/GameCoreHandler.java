package com.braids.coffeebombermen.client.gamecore.control;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.StringTokenizer;

import com.braids.coffeebombermen.GameManager;
import com.braids.coffeebombermen.MainFrame;
import com.braids.coffeebombermen.client.gamecore.Activities;
import com.braids.coffeebombermen.client.gamecore.BombPhases;
import com.braids.coffeebombermen.client.gamecore.CoreConsts;
import com.braids.coffeebombermen.client.gamecore.Directions;
import com.braids.coffeebombermen.client.gamecore.FireShapes;
import com.braids.coffeebombermen.client.gamecore.model.BombModel;
import com.braids.coffeebombermen.client.gamecore.model.FireModel;
import com.braids.coffeebombermen.client.gamecore.model.PlayerModel;
import com.braids.coffeebombermen.client.gamecore.model.level.LevelComponent;
import com.braids.coffeebombermen.client.gamecore.model.level.LevelModel;
import com.braids.coffeebombermen.client.shrink.BinaryShrinkPerformer;
import com.braids.coffeebombermen.client.shrink.BinaryWalkingShrinkPerformer;
import com.braids.coffeebombermen.client.shrink.BombShrinkPerformer;
import com.braids.coffeebombermen.client.shrink.DefaultShrinkPerformer;
import com.braids.coffeebombermen.client.shrink.DiseaseShrinkPerformer;
import com.braids.coffeebombermen.client.shrink.MassKillShrinkPerformer;
import com.braids.coffeebombermen.client.shrink.ShrinkPerformer;
import com.braids.coffeebombermen.client.shrink.SpiderBombShrinkPerformer;
import com.braids.coffeebombermen.client.sound.SoundEffect;
import com.braids.coffeebombermen.options.Diseases;
import com.braids.coffeebombermen.options.OptConsts.Items;
import com.braids.coffeebombermen.options.OptConsts.PlayerControlKeys;
import com.braids.coffeebombermen.options.OptConsts.Walls;
import com.braids.coffeebombermen.options.ServerComponentOptions;
import com.braids.coffeebombermen.options.Shrinkers;
import com.braids.coffeebombermen.options.model.PublicClientOptions;
import com.braids.coffeebombermen.options.model.ServerOptions;
import com.braids.coffeebombermen.utils.GeneralStringTokenizer;
import com.braids.coffeebombermen.utils.MathHelper;

/**
 * The class which handles the core of the game: manages rounds and the running
 * of a game.
 */
public class GameCoreHandler {

	/** in tick */
	private static final int                LAST_PLAYER_COUNT_DOWN_BEFORE_WIN       = 30;

	private static final int                MATCH_WON_SPIDER_BOMB_ROUNDS            = 16;

	private static final long               MATCH_WON_HAPPY_PLAYER_ACTION_FREQUENCY = 64;

	/** Reference to the game manager. */
	private final GameManager               gameManager;
	/** Reference to the main frame. */
	private final MainFrame                 mainFrame;
	/** The global server options. */
	private final ServerOptions             globalServerOptions;
	/**
	 * Received level model from the server (if level is set to randomly
	 * generated, this can be null).
	 */
	private final LevelModel                receivedLevelModel;
	/** Random object to be used when we generate random datas. */
	private final Random                    random;
	/** Vector of public client options of the clients. */
	private final List<PublicClientOptions> clientsPublicClientOptions;
	/** Our client index, our place among the clients. */
	private int                             ourClientIndex;

	/** Level of the game. */
	private Level                           level;
	/**
	 * The Player objects of the game. Every client has a player array, and this
	 * is the vector of these arrays.
	 */
	private List<Player[]>                  clientsPlayers;
	/** This is a shortcut for the models of the players of all clients. */
	private List<PlayerModel[]>             clientsPlayerModels;
	/** The bombs of the game. */
	private List<Bomb>                      bombs;
	/** Shortcut for the models of the bombs. */
	private List<BombModel>                 bombModels;

	// /** Handler of the main component being the draw game animation
	// component. */
	// private final MainComponentHandler drawGameAnimationMainComponentHandler;
	// /** Handler of the main component being the losing animation component.
	// */
	// private final MainComponentHandler losingAnimationMainComponentHandler;
	// /** Handler of the main component being the winning animation component.
	// */
	// private final MainComponentHandler winningAnimationMainComponentHandler;

	private long                            tick;
	private ShrinkPerformer[]               shrinkPerformers;
	private ShrinkPerformer                 shrinkPerformer;
	private boolean                         hasMoreThanOneAlivePlayer;
	private long                            lastPlayerCountDownStartedAt;

	private PlayerModelComparatorByPoint    playerModelComparatorByPoint            = new PlayerModelComparatorByPoint();

	/**
	 * Creates a new GameCoreHandler. A new GameCoreHandler is created for every
	 * new game (but used only one for the rounds of a game).<br>
	 * 
	 * @param gameManager
	 *            reference to the game manager
	 * @param mainFrame
	 *            reference to the main frame
	 * @param globalServerOptions
	 *            the global server options
	 * @param levelModel
	 *            level model where game should be played (if level is set to
	 *            randomly generated, this can be null)
	 * @param random
	 *            a random object to be used when we generate random data (these
	 *            are seeded to the same value at all clients)
	 * @param clientsPublicClientOptions
	 *            vector of public client options of the clients
	 * @param ourClientIndex
	 *            our client index, our place among the clients
	 */
	public GameCoreHandler(final GameManager gameManager, final MainFrame mainFrame, final ServerOptions globalServerOptions, final LevelModel levelModel,
	        final Random random, final List<PublicClientOptions> clientsPublicClientOptions, final int ourClientIndex) {
		this.gameManager = gameManager;
		this.mainFrame = mainFrame;
		this.globalServerOptions = globalServerOptions;
		this.receivedLevelModel = levelModel;
		this.random = random;
		MathHelper.setRandom(random);
		this.clientsPublicClientOptions = clientsPublicClientOptions;
		this.ourClientIndex = ourClientIndex;
		this.shrinkPerformers = new ShrinkPerformer[] { new DefaultShrinkPerformer(this), new BombShrinkPerformer(this), new BinaryShrinkPerformer(this),
		        new BinaryWalkingShrinkPerformer(this), new SpiderBombShrinkPerformer(this), new MassKillShrinkPerformer(this),
		        new DiseaseShrinkPerformer(this) };

		clientsPlayers = new ArrayList<Player[]>(this.clientsPublicClientOptions.size());
		clientsPlayerModels = new ArrayList<PlayerModel[]>(this.clientsPublicClientOptions.size());
		for (int i = 0; i < this.clientsPublicClientOptions.size(); i++) {
			final PublicClientOptions publicClientOptions = this.clientsPublicClientOptions.get(i);

			final Player[] players = new Player[publicClientOptions.playerNames.length];
			final PlayerModel[] playerModels = new PlayerModel[publicClientOptions.playerNames.length];

			for (int j = 0; j < players.length; j++) {
				players[j] = new Player(ourClientIndex == i, i, j, this, publicClientOptions.playerNames[j]);
				playerModels[j] = players[j].getModel();
				playerModels[j].setColor(publicClientOptions.playerColors[j]);
			}

			clientsPlayers.add(players);
			clientsPlayerModels.add(playerModels);
		}

		// drawGameAnimationMainComponentHandler = new
		// AbstractAnimationMainComponentHandler(this.mainFrame) {
		//
		// protected AnimationDatas getNewAnimationDatas() {
		// return
		// GraphicsManager.getCurrentManager().getWaitingAnimationDatas();
		// }
		// };
		// losingAnimationMainComponentHandler = new
		// AbstractAnimationMainComponentHandler(this.mainFrame) {
		//
		// protected AnimationDatas getNewAnimationDatas() {
		// return
		// GraphicsManager.getCurrentManager().getWaitingAnimationDatas();
		// }
		// };
		// winningAnimationMainComponentHandler = new
		// AbstractAnimationMainComponentHandler(this.mainFrame) {
		//
		// protected AnimationDatas getNewAnimationDatas() {
		// return
		// GraphicsManager.getCurrentManager().getWaitingAnimationDatas();
		// }
		// };
	}

	/**
	 * This method is called when a client leaves the game.
	 * 
	 * @param clientIndex
	 *            index of the client who has just left the game
	 */
	public void aClientHasLeftTheGame(final int clientIndex) {
		clientsPlayers.remove(clientIndex);
		clientsPlayerModels.remove(clientIndex);

		if (ourClientIndex > clientIndex) {
			ourClientIndex--;
		}

		for (int i = clientIndex; i < clientsPlayers.size(); i++) {
			for (final Player player : clientsPlayers.get(i)) {
				player.setClientIndex(i);
			}
		}
	}

	/**
	 * Inits the next round.
	 */
	public void initNextRound() {
		level = globalServerOptions.getLevelName().equals(ServerComponentOptions.RANDOMLY_GENERATED_LEVEL_NAME) ? RandomLevelBuilder.generateRandomLevel(
		        globalServerOptions, this, random) : new Level(receivedLevelModel.cloneLevel(), this);

		LevelModel levelModel = level.getModel();

		// The level is surrounded with concrete walls, we dont even try
		// there...
		final int maxComponentPosX = levelModel.getWidth() - 2;
		final int maxComponentPosY = levelModel.getHeight() - 2;

		final ArrayList<int[]> generatedStartPositions = new ArrayList<int[]>();

		tick = 0;
		lastPlayerCountDownStartedAt = -1;
		hasMoreThanOneAlivePlayer = true;

		// This is the quality of how perfectly can the players be positioned on
		// the level.
		// 0 is the best, and if the algorithm increases, will demand less and
		// less from the potential positions.
		// the 4th quality is the worst: if we reach it, we stop immediately, we
		// dont care if even there is concrete wall in that position
		int positioningAlgoritmQuality = 0;

		for (final Player[] players : clientsPlayers) {
			for (final Player player : players) {
				// Now we generate random position

				int componentPosX = 1 + getRandom().nextInt(maxComponentPosX);
				int componentPosY = 1 + getRandom().nextInt(maxComponentPosY);

				qualityCycle: while (true) {
					trialCycle: for (int trialsCount = (maxComponentPosX - 1) * (maxComponentPosY - 1); trialsCount >= 0; trialsCount--) {
						if (--componentPosX < 1) {
							componentPosX = maxComponentPosX;
							if (--componentPosY < 1) {
								componentPosY = maxComponentPosY;
							}
						}

						if (positioningAlgoritmQuality < 4) {
							if (levelModel.getComponent(componentPosX, componentPosY).getWall() == Walls.CONCRETE) {
								continue; // Obvious...
							}
						}

						if (positioningAlgoritmQuality < 2) {
							if ((levelModel.getComponent(componentPosX - 1, componentPosY).getWall() == Walls.CONCRETE)
							        && (levelModel.getComponent(componentPosX + 1, componentPosY).getWall() == Walls.CONCRETE)) {
								continue; // Horizontally not open the component
							}
							if ((levelModel.getComponent(componentPosX, componentPosY - 1).getWall() == Walls.CONCRETE)
							        && (levelModel.getComponent(componentPosX, componentPosY + 1).getWall() == Walls.CONCRETE)) {
								continue; // Vertically not open the component
							}
						}

						for (final int[] startPosition : generatedStartPositions) {
							if (Math.abs(componentPosX - startPosition[0]) + Math.abs(componentPosY - startPosition[1]) < 4 - positioningAlgoritmQuality) {
								continue trialCycle; // Too close to another
								// player
							}
						}

						generatedStartPositions.add(new int[] { componentPosX, componentPosY });
						break qualityCycle;
					}

					positioningAlgoritmQuality++; // We couldnt place the
					// player, we will try to
					// place to worse positions
				}

				// We clear the level around the player.
				final int[][] DELTA_COORDS = new int[][] { { -1, 0 }, { 0, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } }; // Delta
				// coordinates of the clearable components
				for (int[] element : DELTA_COORDS) {
					final LevelComponent levelComponent = levelModel.getComponent(componentPosX + element[1], componentPosY + element[0]);
					if ((levelComponent.getWall() != Walls.CONCRETE) && (levelComponent.getWall() != Walls.GATEWAY)) {
						levelComponent.setWall(Walls.EMPTY);
						levelComponent.setItem(null);
					}
				}

				player.initForNextRound(CoreConsts.LEVEL_COMPONENT_GRANULARITY * componentPosX + CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2,
				        CoreConsts.LEVEL_COMPONENT_GRANULARITY * componentPosY + CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2);
			}
		}

		bombs = new ArrayList<Bomb>();
		bombModels = new ArrayList<BombModel>();

		shrinkPerformer = getRandomShrinkPerformer();

		shrinkPerformer.initNextRound();
		SoundEffect.START_MATCH.play();
	}

	private ShrinkPerformer getRandomShrinkPerformer() {
		int[] shrinkWeights = globalServerOptions.getLevelOptions().getShrinkerWeights();
		Shrinkers shrinkType = Shrinkers.values()[MathHelper.getWeightedRandom(shrinkWeights)];
		for (ShrinkPerformer li : shrinkPerformers) {
			if (li.getType() == shrinkType) {
				return li;
			}
		}
		throw new RuntimeException("this should never happen");
	}

	private void playerInfectPlayer(int noPlayers) {
		List<Player> lstPlayer = new ArrayList<Player>(noPlayers);
		List<Rectangle> lstPlayerBounds = new ArrayList<Rectangle>(noPlayers);

		boolean hasInfectedPlayer = false;

		for (final Player[] players : clientsPlayers) {
			for (final Player player : players) {
				PlayerModel model = player.getModel();
				if (model.getActivity() == Activities.DYING) {
					// dead player can't infect or get infected
					continue;
				}

				lstPlayer.add(player);
				lstPlayerBounds.add(new Rectangle(model.getPosX(), model.getPosY(), 900, 1300));

				// remove the expired diseases
				for (Entry<Diseases, Long> entry : new ArrayList<Entry<Diseases, Long>>(model.getOwnedDiseases().entrySet())) {
					if (entry.getValue() < tick) {
						model.expireDisease(entry.getKey());
					}
				}
				if (!hasInfectedPlayer && model.hasDiseases()) {
					hasInfectedPlayer = true;
				}
			}
		}

		if (hasInfectedPlayer) {
			// who infect who?
			for (int i = 0; i < lstPlayerBounds.size() - 1; i++) {
				for (int j = i + 1; j < lstPlayerBounds.size(); j++) {
					if (lstPlayerBounds.get(i).intersects(lstPlayerBounds.get(j))) {
						Player player1 = lstPlayer.get(i);
						Player player2 = lstPlayer.get(j);
						PlayerModel playerModel1 = player1.getModel();
						PlayerModel playerModel2 = player2.getModel();

						for (Entry<Diseases, Long> entry : playerModel1.getOwnedDiseases().entrySet()) {
							playerModel2.addDisease(entry.getKey(), entry.getValue());
						}
						for (Entry<Diseases, Long> entry : playerModel2.getOwnedDiseases().entrySet()) {
							playerModel1.addDisease(entry.getKey(), entry.getValue());
						}
					}
				}
			}
		}
	}

	/**
	 * Called when next iteration starts.<br>
	 * Calculates the next iteration.
	 * 
	 * @param newClientsActions
	 *            new, unprocess actions of all clients (including us), null
	 *            means there are no new unprocessed actions
	 */
	public void nextIteration(final String newClientsActions) {
		tick++;

		boolean newHasMoreThanOneALivePlayer = hasMoreThanOneAlivePlayer;

		if (hasMoreThanOneAlivePlayer && (getAlivePlayerCount() <= 1)) {
			if (lastPlayerCountDownStartedAt == -1) {
				lastPlayerCountDownStartedAt = tick;
			} else if ((tick - lastPlayerCountDownStartedAt) > LAST_PLAYER_COUNT_DOWN_BEFORE_WIN) {
				// match is just won
				newHasMoreThanOneALivePlayer = false;
				matchJustWon();
			}
		}
		hasMoreThanOneAlivePlayer = newHasMoreThanOneALivePlayer;

		if (newClientsActions != null) {
			processNewClientsActions(newClientsActions);
		}

		int noPlayers = 0;

		for (final Player[] players : clientsPlayers) {
			for (final Player player : players) {
				player.nextIteration();
				noPlayers++;
			}
		}
		playerInfectPlayer(noPlayers);

		level.nextIteration();

		for (final Bomb bomb : bombs) {
			bomb.nextIteration();
		}

		// We remove the bombs just went dead.
		for (int i = bombs.size() - 1; i >= 0; i--) {
			final BombModel bombModel = bombModels.get(i);
			if (bombModel.isDead()) {
				bombModel.getOwnerPlayer().accumulateableItemQuantitiesMap.put(Items.BOMB, bombModel.getOwnerPlayer().accumulateableItemQuantitiesMap
				        .get(Items.BOMB) + 1);
				bombs.remove(i);
				bombModels.remove(i);
			}
		}

		checkAndHandleBombDetonations();

		// We remove the bombs that just had been detonated.
		for (int i = bombs.size() - 1; i >= 0; i--) {
			final BombModel bombModel = bombModels.get(i);
			if (bombModels.get(i).isDetonated()) {
				if (bombModel.getOwnerPlayer() != null) {
					bombModel.getOwnerPlayer().accumulateableItemQuantitiesMap.put(Items.BOMB, bombModel.getOwnerPlayer().accumulateableItemQuantitiesMap
					        .get(Items.BOMB) + 1);
				}
				bombs.remove(i);
				bombModels.remove(i);

			}
		}

		if (hasMoreThanOneAlivePlayer) {
			shrinkPerformer.nextIteration();

			// Now we damage players being in fire.
			for (PlayerModel playerModel : getAllPlayerModels()) {
				if (playerModel.getActivity() != Activities.DYING) {
					LevelComponent comp = getLevelModel().getComponent(playerModel.getComponentPosX(), playerModel.getComponentPosY());
					int firesCount = comp.getFireCount();
					if (!globalServerOptions.isMultipleFire() && (firesCount > 1)) {
						firesCount = 1;
					}
					if (firesCount > 0) {
						int damage = firesCount
						        * (int) (CoreConsts.MAX_PLAYER_VITALITY * globalServerOptions.getDamageOfWholeBombFire() / (100.0 * CoreConsts.FIRE_ITERATIONS) + 0.5); // +
						// 0.5
						// for ceiling (can't flooring, cause 100% damage might
						// cause remainder, would let the player live!)
						playerModel.setVitality(Math.max(0, playerModel.getVitality() - damage));
						if (playerModel.getVitality() <= 0) {
							killPlayer(playerModel);
						}
					}

					// the shrinking game area can cause the player die
					if (comp.getWall() == Walls.DEATH) {
						killPlayer(playerModel);
					}
				}
			}
		} else {
			if (tick % MATCH_WON_HAPPY_PLAYER_ACTION_FREQUENCY == 0) {
				PlayerModel lastPlayer = getTheLastRemainingPlyer();
				if (lastPlayer != null) {
					lastPlayer.setSpiderBombEnabled(true);
				}
			}
		}
	}

	public List<PlayerModel> getAllPlayerModels() {
		ArrayList<PlayerModel> result = new ArrayList<PlayerModel>();
		for (PlayerModel[] playerModels : clientsPlayerModels) {
			for (PlayerModel playerModel : playerModels) {
				result.add(playerModel);
			}
		}

		return result;
	}

	public List<PlayerModel> getAllLivingPlayerModels() {
		ArrayList<PlayerModel> result = new ArrayList<PlayerModel>();
		for (PlayerModel[] playerModels : clientsPlayerModels) {
			for (PlayerModel playerModel : playerModels) {
				if (playerModel.getVitality() > 0) {
					result.add(playerModel);
				}
			}
		}

		return result;
	}

	public List<PlayerModel> getAllPlayerModelsOrderedByPoint() {
		List<PlayerModel> result = getAllPlayerModels();
		Collections.sort(result, playerModelComparatorByPoint);
		return result;
	}

	public PlayerModel getTheLastRemainingPlyer() {
		for (PlayerModel[] playerModels : clientsPlayerModels) {
			for (PlayerModel playerModel : playerModels) {
				if (playerModel.isAlive()) {
					return playerModel;
				}
			}
		}
		return null;
	}

	private void matchJustWon() {

		gameManager.showTrayMessage("The match is over...");

		PlayerModel lastPlayerModel = getTheLastRemainingPlyer();
		if (lastPlayerModel == null) {
			mainFrame.receiveMessage("Everyone died.");
		} else {
			// playerModel.setSpiderBombEnabled(true);
			// playerModel.setSpiderBombRounds(MATCH_WON_SPIDER_BOMB_ROUNDS);
			lastPlayerModel.setPoints(lastPlayerModel.getPoints() + 1);
			mainFrame.receiveMessage("Match is won by " + lastPlayerModel.getName() + " !");
		}

		for (int i = bombs.size() - 1; i >= 0; i--) {
			final BombModel bombModel = bombModels.get(i);
			if (bombModel.getOwnerPlayer() != null) {
				bombModel.getOwnerPlayer().accumulateableItemQuantitiesMap.put(Items.BOMB, bombModel.getOwnerPlayer().accumulateableItemQuantitiesMap
				        .get(Items.BOMB) + 1);
			}
			bombs.remove(i);
			bombModels.remove(i);
		}

		LevelModel levelModel = level.getModel();
		for (int y = 0; y < levelModel.getHeight(); y++) {
			for (int x = 0; x < levelModel.getWidth(); x++) {
				LevelComponent comp = levelModel.getComponent(x, y);
				if ((comp.getWall() != Walls.EMPTY) && (comp.getWall() != Walls.BRICK)) {
					comp.setWall(Walls.DEATH_WARN);
				}
			}
		}

	}

	private int getAlivePlayerCount() {
		int aliveCount = 0;
		for (PlayerModel[] playerModels : clientsPlayerModels) {
			for (PlayerModel playerModel : playerModels) {
				if (playerModel.isAlive()) {
					aliveCount++;
				}
			}
		}
		return aliveCount;
	}

	private void killPlayer(PlayerModel playerModel) {
		playerModel.setVitality(0);
		playerModel.setActivity(Activities.DYING);
	}

	/**
	 * Processes the new actions of all clients.
	 * 
	 * @param newClientsActions
	 *            new clients actions to be processed
	 */
	private void processNewClientsActions(final String newClientsActions) {
		if (newClientsActions.isEmpty()) {
			return;
		}
		final GeneralStringTokenizer clientsActionsTokenizer = new GeneralStringTokenizer(newClientsActions);

		while (clientsActionsTokenizer.hasRemainingString()) {
			final StringTokenizer clientActionsTokenizer = new StringTokenizer(clientsActionsTokenizer.nextStringToken(), " ");

			String commandTarget = clientActionsTokenizer.nextToken();

			if ("player".equals(commandTarget)) {

				final int clientIndex = Integer.parseInt(clientActionsTokenizer.nextToken());

				do {
					final int playerIndex = Integer.parseInt(clientActionsTokenizer.nextToken());
					final PlayerControlKeys playerControlKey = PlayerControlKeys.values()[Integer.parseInt(clientActionsTokenizer.nextToken())];
					final boolean playerControlKeyPressed = clientActionsTokenizer.nextToken().charAt(0) == 'p' ? true : false;

					clientsPlayers.get(clientIndex)[playerIndex].getModel().setControlKeyState(playerControlKey, playerControlKeyPressed);

				} while (clientActionsTokenizer.hasMoreTokens());
			}
		}
	}

	/**
	 * Checks and handles bomb detonations.<br>
	 * This includes setting and spreading the fire and detonating reached other
	 * bombs.
	 */
	private void checkAndHandleBombDetonations() {
		LevelModel levelModel = getLevelModel();
		final ArrayList<BombModel> detonatableBombModels = new ArrayList<BombModel>();
		boolean checkedAllBombModels;

		// First we check the fire triggered bombs...
		for (final BombModel bombModel : bombModels) {
			LevelComponent levelComp = levelModel.getComponent(bombModel.getComponentPosX(), bombModel.getComponentPosY());
			if ((bombModel.getPhase() != BombPhases.FLYING) && !bombModel.isAboutToDetonate() && levelComp.hasFire()) {
				bombModel.setAboutToDetonate(true);
				bombModel.setTriggererPlayer(levelComp.getLastFire().getTriggererPlayer());
			}
		}
		do {
			checkedAllBombModels = true;
			// The fact that we didn't check all of 'em will be known if we find
			// one that hasn't been checked out yet.

			for (final BombModel bombModel : bombModels) {
				if (!bombModel.isDetonated() && bombModel.isAboutToDetonate()) {
					bombModel.setTriggererPlayer(bombModel.getOwnerPlayer());
					detonatableBombModels.add(bombModel);
					checkedAllBombModels = false;
					break;
				}
			}

			for (int i = 0; i < detonatableBombModels.size(); i++) {
				// Not enhanced for: we may add new detonatable bombs inside the
				// cycle (so it must be upward)!
				final BombModel detonatedBombModel = detonatableBombModels.get(i);
				final int detonatedBombComponentPosX = detonatedBombModel.getComponentPosX();
				final int detonatedBombComponentPosY = detonatedBombModel.getComponentPosY();

				for (final Directions direction : Directions.values()) {
					for (int range = direction == Directions.values()[0] ? 0 : 1; range < detonatedBombModel.getRange(); range++) {
						if ((range > 0) && detonatedBombModel.excludedDetonationDirections.contains(direction)) {
							break;
						}

						final int componentPosX = detonatedBombComponentPosX + direction.getXMultiplier() * range;
						final int componentPosY = detonatedBombComponentPosY + direction.getYMultiplier() * range;

						if ((componentPosX < 0) || (componentPosX > levelModel.getWidth() - 1) || (componentPosY < 0)
						        || (componentPosY > levelModel.getHeight() - 1)) {
							break;
						}
						LevelComponent levelComponent = levelModel.getComponent(componentPosX, componentPosY);

						if ((levelComponent.getWall() == Walls.CONCRETE) || (levelComponent.getWall() == Walls.DEATH)
						        || (levelComponent.getWall() == Walls.DEATH_WARN) || (levelComponent.getWall() == Walls.GATEWAY)) {
							break;
						}

						final Integer bombIndexAtComponentPos = range == 0 ? null : getBombIndexAtComponentPosition(componentPosX, componentPosY);

						if (bombIndexAtComponentPos != null) {
							final BombModel bombModelAtComponentPos = bombModels.get(bombIndexAtComponentPos);
							bombModelAtComponentPos.excludedDetonationDirections.add(direction.getOpposite());
							if (!bombModelAtComponentPos.isDetonated() && !detonatableBombModels.contains(bombModelAtComponentPos)) {
								bombModelAtComponentPos.setTriggererPlayer(detonatedBombModel.getTriggererPlayer());
								detonatableBombModels.add(bombModelAtComponentPos);
							}
							break;
						} else {
							// Now here we can set the fire...
							final Fire fire = new Fire(componentPosX, componentPosY, this);
							final FireModel fireModel = fire.getModel();

							fireModel.setShape(range == 0 ? FireShapes.CROSSING : (direction.getXMultiplier() != 0 ? FireShapes.HORIZONTAL
							        : FireShapes.VERTICAL));
							fireModel.setOwnerPlayer(detonatedBombModel.getOwnerPlayer());
							fireModel.setTriggererPlayer(detonatedBombModel.getTriggererPlayer());

							level.addFireToComponentPos(fire, componentPosX, componentPosY);

							if ((levelComponent.getWall() == Walls.BRICK) || ((levelComponent.getWall() == Walls.EMPTY) && (levelComponent.getItem() != null))) {
								break;
							}
						}
					}
				}

				detonatedBombModel.setDetonated(true);
			}

			detonatableBombModels.clear();

		} while (!checkedAllBombModels);
	}

	/* ======================== MODEL PROVIDER =========================== */

	/**
	 * Returns the level model of the game.
	 * 
	 * @return the level model of the game
	 */
	public LevelModel getLevelModel() {
		return level == null ? null : level.getModel();
	}

	public Level getLevel() {
		return level;
	}

	/**
	 * Returns the players of all clients.
	 * 
	 * @return the players of all clients
	 */
	public List<PlayerModel[]> getClientsPlayerModels() {
		return clientsPlayerModels;
	}

	/**
	 * Returns the bomb models of the game.
	 * 
	 * @return the bomb models of the game
	 */
	public List<BombModel> getBombModels() {
		return bombModels;
	}

	/**
	 * Returns our client index.
	 * 
	 * @return our client index
	 */
	public int getOurClientIndex() {
		return ourClientIndex;
	}

	/**
	 * Returns the vector of public client options of the clients.
	 * 
	 * @return the vector of public client options of the clients
	 */
	public List<PublicClientOptions> getClientsPublicClientOptions() {
		return clientsPublicClientOptions;
	}

	/**
	 * Tells whether there is a bomb at a component position or whether there is
	 * one that hangs down into that component.<br>
	 * Implementation simply calls getBombIndexAtComponentPosition() and
	 * examines its return value.
	 * 
	 * @param componentPosX
	 *            x coordinate of the component
	 * @param componentPosY
	 *            y coordinate of the component
	 * @return true if there is a bomb at the specified position or there is one
	 *         that hangs down into it; false otherwise
	 */
	public boolean isBombAtComponentPosition(final int componentPosX, final int componentPosY) {
		return getBombIndexAtComponentPosition(componentPosX, componentPosY) != null;
	}

	public BombModel getBombAtComponentPosition(final int componentPosX, final int componentPosY) {
		for (int i = bombModels.size() - 1; i >= 0; i--) {
			final BombModel bombModel = bombModels.get(i);
			if (bombModel.getPhase() != BombPhases.FLYING) {
				// Flying bombs "aren't" in the level.
				if ((bombModel.getComponentPosX() == componentPosX) && (bombModel.getComponentPosY() == componentPosY)) {
					return bombModel;
				}
			}
		}

		return null;
	}

	/**
	 * Returns the bomb being at a component position or the one hanging down
	 * into the component.
	 * 
	 * @param componentPosX
	 *            x coordinate of the component
	 * @param componentPosY
	 *            y coordinate of the component
	 * @return the bomb being at a component position or the one hanging down
	 *         into the component; or null if there is no bomb there
	 */
	public Integer getBombIndexAtComponentPosition(final int componentPosX, final int componentPosY) {
		for (int i = bombModels.size() - 1; i >= 0; i--) {
			final BombModel bombModel = bombModels.get(i);
			if (bombModel.getPhase() != BombPhases.FLYING) {
				// Flying bombs "aren't" in the level.
				if ((bombModel.getComponentPosX() == componentPosX) && (bombModel.getComponentPosY() == componentPosY)) {
					return i;
				}
			}
		}

		return null;
	}

	/**
	 * Tells whether there is a player at a component position or whether there
	 * is one that hangs down into that component.
	 * 
	 * @param componentPosX
	 *            x coordinate of the component
	 * @param componentPosY
	 *            y coordinate of the component
	 * @param playerModelToExclude
	 *            model of player to be excluded, can be null
	 * @return true if there is a player at the specified position or there is
	 *         one that hangs down into it; false otherwise
	 */
	public boolean isPlayerAtComponentPositionExcludePlayer(final int componentPosX, final int componentPosY, final PlayerModel playerModelToExclude) {
		for (final PlayerModel[] playerModels : clientsPlayerModels) {
			for (final PlayerModel playerModel : playerModels) {
				if (playerModel != playerModelToExclude) {
					if (playerModel.getActivity() != Activities.DYING) {
						// players
						// doesn't
						// count...
						if ((playerModel.getComponentPosX() == componentPosX) && (playerModel.getComponentPosY() == componentPosY)) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	/* ======================== MODEL CONTROLLER ========================= */

	/**
	 * Returns the global server options.
	 * 
	 * @return the global server options
	 */
	public ServerOptions getGlobalServerOptions() {
		return globalServerOptions;
	}

	/**
	 * Adds a new bomb to the game model.
	 * 
	 * @param bomb
	 *            bomb to be added
	 */
	public void addNewBomb(final Bomb bomb) {
		SoundEffect.PLACE_BOMB.play();
		bombs.add(bomb);
		bombModels.add(bomb.getModel());
	}

	/**
	 * Removes a bomb specified by its index.
	 * 
	 * @param bombIndex
	 *            index of bomb to be removed
	 */
	public void removeBombAtIndex(final int bombIndex) {
		bombs.remove(bombIndex);
		bombModels.remove(bombIndex);
	}

	/**
	 * Returns the Random object to be used for generating random datas.
	 * 
	 * @return the Random object to be used for generating random datas
	 */
	public Random getRandom() {
		return random;
	}

	/**
	 * Checks and sets the x coordiante of the target position in case of a
	 * flying bomb.
	 * 
	 * @param bombModel
	 *            bomb model whos target position to be validated and set
	 * @param flyingTargetPosX
	 *            the whished x coordiante of the target position in case of a
	 *            flying bomb
	 */
	public void validateAndSetFlyingTargetPosX(final BombModel bombModel, final int flyingTargetPosX) {
		LevelModel levelModel = getLevelModel();

		if (flyingTargetPosX < 0) {
			bombModel.setFlyingTargetPosX((levelModel.getWidth() - 1) * CoreConsts.LEVEL_COMPONENT_GRANULARITY + CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2);
		} else if (flyingTargetPosX > levelModel.getWidth() * CoreConsts.LEVEL_COMPONENT_GRANULARITY) {
			bombModel.setFlyingTargetPosX(CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2);
		} else {
			bombModel.setFlyingTargetPosX(flyingTargetPosX);
		}
	}

	/**
	 * Checks and sets the y coordiante of the target position in case of a
	 * flying bomb.
	 * 
	 * @param bombModel
	 *            bomb model whos target position to be validated and set
	 * @param flyingTargetPosY
	 *            the whished y coordiante of the target position in case of a
	 *            flying bomb
	 */
	public void validateAndSetFlyingTargetPosY(final BombModel bombModel, final int flyingTargetPosY) {
		LevelModel levelModel = getLevelModel();

		if (flyingTargetPosY < 0) {
			bombModel.setFlyingTargetPosY((levelModel.getHeight() - 1) * CoreConsts.LEVEL_COMPONENT_GRANULARITY + CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2);
		} else if (flyingTargetPosY > levelModel.getHeight() * CoreConsts.LEVEL_COMPONENT_GRANULARITY) {
			bombModel.setFlyingTargetPosY(CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2);
		} else {
			bombModel.setFlyingTargetPosY(flyingTargetPosY);
		}
	}

	/**
	 * Tells whether a specified bomb can roll to a component position.
	 * 
	 * @param bombModel
	 *            model of bomb to be checked
	 * @param componentPosX
	 *            x coordinate of the position of the desired component
	 * @param componentPosY
	 *            y coordinate of the position of the desired component
	 * @return true, if the specified bomb can roll to the component; false
	 *         otherwise
	 */
	public boolean canBombRollToComponentPosition(final BombModel bombModel, final int componentPosX, final int componentPosY) {

		LevelModel levelModel = getLevelModel();
		if ((componentPosX < 0) || (componentPosX >= levelModel.getWidth()) || (componentPosY < 0) || (componentPosY >= levelModel.getHeight())) {
			return false;
		}

		final LevelComponent levelComponentAheadAhead = levelModel.getComponent(componentPosX, componentPosY);
		if (levelComponentAheadAhead.getWall() != Walls.EMPTY) {
			return false;
		}
		if ((levelComponentAheadAhead.getWall() == Walls.EMPTY) && (levelComponentAheadAhead.getItem() != null)
		        && getGlobalServerOptions().isItemsStopRollingBombs()) {
			return false;
		}

		// Collision with players:
		for (final PlayerModel[] playerModels : getClientsPlayerModels()) {
			for (final PlayerModel playerModel : playerModels) {
				if (playerModel.getActivity() != Activities.DYING) {
					// "Dead" players doesn't count...
					if ((playerModel.getComponentPosX() == componentPosX) && (playerModel.getComponentPosY() == componentPosY)) {
						if ((playerModel.getComponentPosX() != bombModel.getComponentPosX())
						        || (playerModel.getComponentPosY() != bombModel.getComponentPosY())) {
							return false;
						}
					}
				}
			}
		}

		final Integer bombIndexAhead = getBombIndexAtComponentPosition(componentPosX, componentPosY);
		if ((bombIndexAhead != null) && (getBombModels().get(bombIndexAhead) != bombModel)) {
			// another
			// bomb
			return false;
		}

		return true;
	}

	/**
	 * Replaces an item to a random position in the level.
	 * 
	 * @param item
	 *            item to be replaced
	 */
	public void replaceItemOnLevel(final Items item) {
		LevelModel levelModel = getLevelModel();

		// The level is surrounded with concrete walls, we dont even try
		// there...
		final int maxComponentPosX = levelModel.getWidth() - 2;
		final int maxComponentPosY = levelModel.getHeight() - 2;

		int componentPosX = 1 + getRandom().nextInt(maxComponentPosX);
		int componentPosY = 1 + getRandom().nextInt(maxComponentPosY);

		for (int trialsCount = (maxComponentPosX - 1) * (maxComponentPosY - 1); trialsCount >= 0; trialsCount--) {
			if (--componentPosX < 1) {
				componentPosX = maxComponentPosX;
				if (--componentPosY < 1) {
					componentPosY = maxComponentPosY;
				}
			}

			final LevelComponent levelComponent = levelModel.getComponent(componentPosX, componentPosY);

			if ((levelComponent.getWall() != Walls.EMPTY) || (levelComponent.getItem() != null)) {
				continue;
			}

			if (isBombAtComponentPosition(componentPosX, componentPosY)) {
				continue;
			}

			if (isPlayerAtComponentPositionExcludePlayer(componentPosX, componentPosY, null)) {
				continue;
			}

			levelComponent.setItem(item);
			break;
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
		final LevelComponent levelComponent = getLevelModel().getComponent(componentPosX, componentPosY);

		if (levelComponent.getWall() == Walls.BRICK) {
			levelComponent.setWall(Walls.EMPTY);
		} else if ((levelComponent.getWall() == Walls.EMPTY) && (levelComponent.getItem() != null)) {
			final Items item = levelComponent.getItem();
			levelComponent.setItem(null);
			if (((item == Items.DISEASE) || (item == Items.SUPER_DISEASE)) && !getGlobalServerOptions().isExplosionAnnihilatesDiseases()) {
				replaceItemOnLevel(item);
			}
		}

		level.removeFireFromComponentPos(fire, componentPosX, componentPosY);
	}

	public long getTick() {
		return tick;
	}

	public boolean getHasMoreThanOneAlivePlayer() {
		return hasMoreThanOneAlivePlayer;
	}

	public Map<String, Integer> getPoints() {
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		for (PlayerModel pm : getAllPlayerModels()) {
			result.put(pm.getName(), pm.getPoints());
		}
		return result;
	}

	public void setPoints(Map<String, Integer> mapPoints) {
		for (PlayerModel pm : getAllPlayerModels()) {
			Integer points = mapPoints.get(pm.getName());
			if (points != null) {
				pm.setPoints(points);
			}
		}
	}

	public void setWall(int x, int y, Walls wall) {
		BombModel bomb = getBombAtComponentPosition(x, y);
		if (bomb != null) {
			bomb.setAboutToDetonate(true);
		}
		getLevel().getModel().getComponent(x, y).setWall(wall);
	}
}
