/*
 * Created on April 1, 2005
 */

package classes.client.gamecore.view;

import static classes.client.gamecore.Consts.BOMB_FLYING_ASCENDENCE_PRIMARY;
import static classes.client.gamecore.Consts.BOMB_FLYING_ASCENDENCE_SECONDARY;
import static classes.client.gamecore.Consts.BOMB_FLYING_DISTANCE;
import static classes.client.gamecore.Consts.BOMB_FLYING_SPEED;
import static classes.client.gamecore.Consts.BOMB_ITERATIONS;
import static classes.client.gamecore.Consts.FIRE_ITERATIONS;
import static classes.client.gamecore.Consts.LEVEL_COMPONENT_GRANULARITY;
import static classes.client.gamecore.Consts.MAX_PLAYER_VITALITY;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

import javax.swing.JComponent;

import classes.client.Client;
import classes.client.gamecore.Consts.Activities;
import classes.client.gamecore.Consts.BombPhases;
import classes.client.gamecore.model.BombModel;
import classes.client.gamecore.model.FireModel;
import classes.client.gamecore.model.ModelProvider;
import classes.client.gamecore.model.PlayerModel;
import classes.client.gamecore.model.level.LevelComponent;
import classes.client.graphics.GraphicsManager;
import classes.client.graphics.ImageHandler;
import classes.options.OptionsChangeListener;
import classes.options.OptionsManager;
import classes.options.Consts.Diseases;
import classes.options.Consts.Walls;
import classes.options.model.ClientOptions;
import classes.options.model.PublicClientOptions;

/**
 * This is the game scene. Game will be displayed on this component.
 * 
 * @author Andras Belicza
 */
public class GameSceneComponent extends JComponent implements KeyListener, OptionsChangeListener<ClientOptions> {

	/** A string containing only a space. Used several times on keyboard events. */
	private static final String                 SPACE_STRING       = " ";

	/** Reference to the client options manager. */
	private final OptionsManager<ClientOptions> clientOptionsManager;
	/** (Reference to) the control keys of players. */
	private int[][]                             playersControlKeys;
	/**
	 * States of the keys of players. Stored becase we want to send only the
	 * changes (pressed and hold key causes keyPressed() being called
	 * repeatedly).
	 */
	private boolean[][]                         playersControlKeyStates;

	/** Reference to the handlers of wall images. */
	private ImageHandler[]                      wallImageHandlers;
	/** Reference to the handlers of item images. */
	private ImageHandler[]                      itemImageHandlers;
	/** References to the handlers of the bomb phases. */
	private ImageHandler[][]                    bombPhaseHandlers;
	/** References to the handlers of the fire phases. */
	private ImageHandler[][]                    firePhaseHandlers;
	/** References to the handlers of the burning phases. */
	private ImageHandler[]                      burningPhaseHandlers;

	/** Reference to a model provider, this model will be displayed. */
	private ModelProvider                       modelProvider;
	/**
	 * The sequence of actions made by the users on this component required for
	 * the game.
	 */
	private String                              actions;

	// Working parameters:
	/** Displayable size of the level components. */
	private int                                 levelComponentSize;

	private List<PlayerGraphic>                 playerGraphics;

	private Color[]                             playerHudColor;

	private final Client                        client;

	private AlphaComposite                      normalComposit     = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);
	private AlphaComposite                      infectedComposite  = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .50f);
	private AlphaComposite                      darknessComposite  = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .90f);
	private AlphaComposite                      fireLightComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .25f);

	/**
	 * Creates a new GameSceneComponent.
	 * 
	 * @param client
	 * @param clientOptionsManager
	 *            reference to the client options manager
	 */
	public GameSceneComponent(Client client, final OptionsManager<ClientOptions> clientOptionsManager) {
		this.client = client;
		this.clientOptionsManager = clientOptionsManager;
		playersControlKeys = this.clientOptionsManager.getOptions().playersControlKeys;
		playersControlKeyStates = new boolean[0][playersControlKeys[0].length]; // The
		// 2nd
		// dimension
		// could
		// be
		// 0
		// size
		// as
		// well,
		// but
		// this
		// is
		// the
		// proper
		// solution.

		this.clientOptionsManager.registerOptionsChangeListener(this);

		addKeyListener(this);

		refreshGraphicDatas();
	}

	private long blackOutDuration = 1000;
	private long flashDuration    = 100;
	private long nextFlashStart   = -1;

	/**
	 * Paints the actual view of the component: paints the actual view of the
	 * game scene.
	 * 
	 * @param graphics
	 *            graphics context in which to paint
	 */
	public void paintComponent(final Graphics graphics) {
		if (GraphicsManager.getCurrentManager() == null) // No graphics theme
			// loaded
			return;
		if (modelProvider == null) // No game started yet
			return;
		if (modelProvider.getLevelModel() == null) // No level created yet
			return;

		long now = System.currentTimeMillis();

		boolean blackOut = false;

		int ourIndex = client.getOurIndex();
		List<PlayerModel[]> clientPlayerModels = modelProvider.getClientsPlayerModels();

		for (int i = 0; i < clientPlayerModels.size(); i++) {

			if (ourIndex == i) {

				for (PlayerModel playerModel : clientPlayerModels.get(i)) {
					if (playerModel.getOwnedDiseases().containsKey(Diseases.BLACK_OUT)) {
						blackOut = true;
					}
				}
			}
		}

		if (blackOut) {
			if (nextFlashStart == -1) {
				nextFlashStart = now;
			}

			if (now >= nextFlashStart && now < (nextFlashStart + flashDuration)) {
				blackOut = false;
			} else {
				if (now >= (nextFlashStart + flashDuration)) {
					nextFlashStart = now + blackOutDuration;
				}
				blackOut = true;
			}
		}

		setWorkingParameters(graphics);

		paintLevel(graphics);
		paintBombs(graphics);
		paintBombermen(graphics);

		if (blackOut) {
			paintBlackout(graphics);
		}
	}

	private void paintBlackout(Graphics graphics) {

		graphics.setColor(Color.BLACK);
		Graphics2D g2 = (Graphics2D) graphics;

		g2.setComposite(darknessComposite);

		if (!hasActualDetonation()) {
			g2.fillRect(0, 0, modelProvider.getLevelModel().getWidth() * levelComponentSize, modelProvider.getLevelModel().getHeight() * levelComponentSize);
			g2.setComposite(normalComposit);
			return;
		}

		final LevelComponent[][] levelComponents = modelProvider.getLevelModel().getComponents();

		for (int i = 0, y = 0; i < levelComponents.length; i++, y += levelComponentSize) {
			final LevelComponent[] levelComponentLine = levelComponents[i];
			for (int j = 0, x = 0; j < levelComponentLine.length; j++, x += levelComponentSize) {
				LevelComponent levelComponent = levelComponentLine[j];
				if (!levelComponent.fireModelVector.isEmpty()) {
					continue;
				}

				if (levelComponent.getWall() == Walls.EMPTY) {
					g2.fillRect(x, y, levelComponentSize, levelComponentSize);
					continue;
				}
				// wall
				boolean fwTop = false;
				boolean fwLeft = false;
				int fwWidth = levelComponentSize;
				int fwHeight = levelComponentSize;

				if (i < levelComponents.length - 1 && !levelComponents[i + 1][j].fireModelVector.isEmpty()) {
					fwHeight -= levelComponentSize / 4;
				}
				if (i > 0 && !levelComponents[i - 1][j].fireModelVector.isEmpty()) {
					fwTop = true;
					fwHeight -= levelComponentSize / 4;
				}
				if (j < levelComponentLine.length - 1 && !levelComponents[i][j + 1].fireModelVector.isEmpty()) {
					fwWidth -= levelComponentSize / 4;
				}

				if (j > 1 && !levelComponents[i][j - 1].fireModelVector.isEmpty()) {
					fwLeft = true;
					fwWidth -= levelComponentSize / 4;
				}

				g2.fillRect(x + (fwLeft ? levelComponentSize / 4 : 0), y + (fwTop ? levelComponentSize / 4 : 0), fwWidth, fwHeight);
			}
		}

		g2.setComposite(normalComposit);
	}

	private boolean hasActualDetonation() {
		final LevelComponent[][] levelComponents = modelProvider.getLevelModel().getComponents();

		for (int i = 0, y = 0; i < levelComponents.length; i++, y += levelComponentSize) {
			final LevelComponent[] levelComponentLine = levelComponents[i];
			for (int j = 0, x = 0; j < levelComponentLine.length; j++, x += levelComponentSize) {
				final LevelComponent levelComponent = levelComponentLine[j];

				if (!levelComponent.fireModelVector.isEmpty()) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Calculates and sets the working parameters of painting.<br>
	 * Calculates the size of level components, and translates the origo of
	 * coordinate system of graphics to the origo of the level.
	 * 
	 * @param graphics
	 *            graphics context which will be used to paint in
	 */
	private void setWorkingParameters(final Graphics graphics) {
		final LevelComponent[][] levelComponents = modelProvider.getLevelModel().getComponents();
		final int originalLevelComponentSize = wallImageHandlers[0].getOriginalWidth(); // Equals
		// to
		// wallImageHandlers[
		// 0
		// ].getOriginalHeight()

		final int sceneWidth = getWidth();
		final int sceneHeight = getHeight();

		final int levelWidth = levelComponents[0].length;
		final int levelHeight = levelComponents.length;
		final float zoomFactor = Math.min((float) sceneWidth / (levelWidth * originalLevelComponentSize), (float) sceneHeight
		        / (levelHeight * originalLevelComponentSize));

		levelComponentSize = (int) (originalLevelComponentSize * zoomFactor);

		graphics.translate((sceneWidth - levelWidth * levelComponentSize) / 2, (sceneHeight - levelHeight * levelComponentSize) / 2);
	}

	/**
	 * Paints the level.
	 * 
	 * @param g
	 *            graphics context in which to paint
	 */
	private void paintLevel(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		LevelComponent[][] levelComponents = modelProvider.getLevelModel().getComponents();
		float wallScaleFactor = (float) levelComponentSize / wallImageHandlers[0].getOriginalWidth();
		float itemScaleFactor = (float) levelComponentSize / itemImageHandlers[0].getOriginalWidth();
		float fireScaleFactor = (float) levelComponentSize / firePhaseHandlers[0][0].getOriginalWidth();
		float burningScaleFactor = (float) levelComponentSize / burningPhaseHandlers[0].getOriginalWidth();

		for (int i = 0, y = 0; i < levelComponents.length; i++, y += levelComponentSize) {
			LevelComponent[] levelComponentLine = levelComponents[i];
			for (int j = 0, x = 0; j < levelComponentLine.length; j++, x += levelComponentSize) {
				final LevelComponent levelComponent = levelComponentLine[j];

				if (levelComponent.fireModelVector.isEmpty()) {
					if (levelComponent.getWall() == Walls.EMPTY && levelComponent.getItem() != null) {
						// item
						g.drawImage(itemImageHandlers[levelComponent.getItem().ordinal()].getScaledImage(itemScaleFactor), x, y, null);
					} else {
						// wall
						g.drawImage(wallImageHandlers[levelComponent.getWall().ordinal()].getScaledImage(wallScaleFactor), x, y, null);
						if (!levelComponent.getWall().equals(Walls.EMPTY)) {
							boolean fwTop = false;
							boolean fwBottom = false;
							boolean fwLeft = false;
							boolean fwRight = false;
							int fwLeftHeight = levelComponentSize;
							int fwRightHeight = levelComponentSize;

							if (i < levelComponents.length - 1 && !levelComponents[i + 1][j].fireModelVector.isEmpty()) {
								fwTop = true;
								fwLeftHeight -= levelComponentSize / 4;
								fwRightHeight -= levelComponentSize / 4;
							}
							if (i > 0 && !levelComponents[i - 1][j].fireModelVector.isEmpty()) {
								fwBottom = true;
								fwLeftHeight -= levelComponentSize / 4;
								fwRightHeight -= levelComponentSize / 4;
							}
							if (j < levelComponentLine.length - 1 && !levelComponents[i][j + 1].fireModelVector.isEmpty()) {
								fwRight = true;
							}

							if (j > 1 && !levelComponents[i][j - 1].fireModelVector.isEmpty()) {
								fwLeft = true;
							}

							if (fwBottom || fwTop || fwLeft || fwRight) {
								g2.setComposite(fireLightComposite);
								g.setColor(Color.YELLOW);
								if (fwBottom) {
									g.fillRect(x, y, levelComponentSize, levelComponentSize / 4);
								}
								if (fwTop) {
									g.fillRect(x, y + levelComponentSize * 3 / 4, levelComponentSize, levelComponentSize / 4);
								}
								if (fwLeft) {
									g.fillRect(x, y + (fwBottom ? (levelComponentSize / 4) : (0)), levelComponentSize / 4, fwLeftHeight);
								}
								if (fwRight) {
									g.fillRect(x + levelComponentSize * 3 / 4, y + (fwBottom ? (levelComponentSize / 4) : (0)), levelComponentSize / 4,
									        fwRightHeight);
								}
								g2.setComposite(normalComposit);
							}
						}
					}
				} else {
					final FireModel fireModel = levelComponent.fireModelVector.get(levelComponent.fireModelVector.size() - 1);
					final int firePhasesCount = firePhaseHandlers[fireModel.getShape().ordinal()].length;

					if (levelComponent.getWall() == Walls.EMPTY && levelComponent.getItem() == null) {
						g.drawImage(wallImageHandlers[levelComponent.getWall().ordinal()].getScaledImage(wallScaleFactor), x, y, null);
						g.drawImage(firePhaseHandlers[fireModel.getShape().ordinal()][firePhasesCount * fireModel.getIterationCounter() / FIRE_ITERATIONS]
						        .getScaledImage(fireScaleFactor), x, y, null);
					} else {
						if (fireModel.getIterationCounter() < FIRE_ITERATIONS / 2) {
							// The original wall or item is burning.
							if (levelComponent.getWall() == Walls.EMPTY) {
								// item
								g.drawImage(itemImageHandlers[levelComponent.getItem().ordinal()].getScaledImage(itemScaleFactor), x, y, null);
							} else {
								// wall
								g.drawImage(wallImageHandlers[levelComponent.getWall().ordinal()].getScaledImage(wallScaleFactor), x, y, null);
							}
						} else {
							// Now the item or the empty wall what will remain
							// after the burning is visible through the burning.
							if (levelComponent.getWall() != Walls.EMPTY && levelComponent.getItem() != null) {
								// An item will remain
								g.drawImage(itemImageHandlers[levelComponent.getItem().ordinal()].getScaledImage(itemScaleFactor), x, y, null);
							} else {
								// Empty wall will remain
								g.drawImage(wallImageHandlers[Walls.EMPTY.ordinal()].getScaledImage(wallScaleFactor), x, y, null);
							}
						}
						g.drawImage(burningPhaseHandlers[burningPhaseHandlers.length * fireModel.getIterationCounter() / FIRE_ITERATIONS]
						        .getScaledImage(burningScaleFactor), x, y, null);
					}
				}
			}
		}
		g2.setComposite(normalComposit);
	}

	/**
	 * Paints the bombs.
	 * 
	 * @param graphics
	 *            graphics context in which to paint
	 */
	private void paintBombs(final Graphics graphics) {
		final List<BombModel> bombModels = modelProvider.getBombModels();

		if (bombModels == null) {
			return;
		}

		final float scaleFactor = (float) levelComponentSize / bombPhaseHandlers[0][0].getOriginalWidth();

		for (int i = 0; i < bombModels.size(); i++) {
			// Easy with the enhanced for: modifying is possible during a
			// paint()
			final BombModel bombModel = bombModels.get(i);
			final int phasesCount = bombPhaseHandlers[bombModel.getType().ordinal()].length;
			final Image bombImage = bombPhaseHandlers[bombModel.getType().ordinal()][phasesCount * bombModel.getIterationCounter() / BOMB_ITERATIONS]
			        .getScaledImage(scaleFactor);

			int posYCorrection = 0;
			if (bombModel.getPhase() == BombPhases.FLYING) {
				if (bombModel.getIterationsDuringPhase() * BOMB_FLYING_SPEED < BOMB_FLYING_DISTANCE)
					posYCorrection = -(int) (BOMB_FLYING_ASCENDENCE_PRIMARY * Math.sin(Math.PI * bombModel.getIterationsDuringPhase() * BOMB_FLYING_SPEED
					        / BOMB_FLYING_DISTANCE));
				else {
					final int posXInComponent = bombModel.getPosX() % LEVEL_COMPONENT_GRANULARITY;
					posYCorrection = -(int) (BOMB_FLYING_ASCENDENCE_SECONDARY * Math.sin(Math.PI
					        * (posXInComponent + (posXInComponent < LEVEL_COMPONENT_GRANULARITY / 2 ? LEVEL_COMPONENT_GRANULARITY / 2
					                : -LEVEL_COMPONENT_GRANULARITY / 2)) / LEVEL_COMPONENT_GRANULARITY));
				}
			}

			graphics.drawImage(bombImage, bombModel.getPosX() * levelComponentSize / LEVEL_COMPONENT_GRANULARITY - levelComponentSize / 2,
			        (bombModel.getPosY() + posYCorrection) * levelComponentSize / LEVEL_COMPONENT_GRANULARITY - levelComponentSize / 2, null);
		}
	}

	/**
	 * Paints the bombermen.
	 * 
	 * @param graphics
	 *            graphics context in which to paint
	 */
	private void paintBombermen(final Graphics graphics) {
		final List<PlayerModel[]> clientPlayerModels = modelProvider.getClientsPlayerModels();

		final float scaleFactor = (float) levelComponentSize / playerGraphics.get(0).getOriginalWidth();
		final ClientOptions clientOptions = clientOptionsManager.getOptions();

		int ourIndex = client.getOurIndex();
		int playerNumberForGfx = 0;
		for (int i = 0; i < clientPlayerModels.size(); i++) {
			// Easy with the enhanced for: modifying is possible during a
			// paint()

			final PlayerModel[] playerModels = clientPlayerModels.get(i);

			if (ourIndex != i) {
				PublicClientOptions publicClientOptions = modelProvider.getClientsPublicClientOptions().get(i);
				paintOneBomberMan(graphics, clientOptions, publicClientOptions, playerModels, playerNumberForGfx, scaleFactor);
			}

			playerNumberForGfx += playerModels.length;
		}
		playerNumberForGfx = 0;
		for (int i = 0; i < clientPlayerModels.size(); i++) { // Easy with the
			// enhanced for: modifying is possible during a paint()

			final PlayerModel[] playerModels = clientPlayerModels.get(i);

			if (ourIndex == i) {
				PublicClientOptions publicClientOptions = modelProvider.getClientsPublicClientOptions().get(i);
				paintOneBomberMan(graphics, clientOptions, publicClientOptions, playerModels, playerNumberForGfx, scaleFactor);
			}

			playerNumberForGfx += playerModels.length;
		}
	}

	public void paintOneBomberMan(Graphics graphics, ClientOptions clientOptions, PublicClientOptions publicClientOptions, PlayerModel[] playerModels,
	        int playerNumberForGfx, float scaleFactor) {

		Graphics2D g2 = (Graphics2D) graphics;

		for (int j = 0; j < playerModels.length; j++) {
			playerNumberForGfx++;
			if (playerNumberForGfx == playerGraphics.size()) {
				playerNumberForGfx = 0;
			}
			final PlayerModel playerModel = playerModels[j];
			if (playerModel.getActivity() == Activities.DYING && playerModel.getIterationCounter() + 1 >= playerModel.getActivity().activityIterations) {
				// This is a dead player, must not be painted.
				continue;
			}

			if (playerModel.getActivity() == null) {
				continue;
			}

			final Image bombermanImage = playerGraphics.get(playerNumberForGfx).getImage(playerModel, scaleFactor);

			if (playerModel.hasDisease()) {
				if ((modelProvider.getTick() & 8) == 0) {
					g2.setComposite(infectedComposite);
				} else {
					g2.setComposite(normalComposit);
				}
			} else {
				g2.setComposite(normalComposit);
			}

			int playerX = playerModel.getPosX() * levelComponentSize / LEVEL_COMPONENT_GRANULARITY;
			int playerY = playerModel.getPosY() * levelComponentSize / LEVEL_COMPONENT_GRANULARITY;

			// Position is tricky: head of bomberman may take place on the
			// row over the position of bobmerman
			graphics.drawImage(bombermanImage, playerX - levelComponentSize / 2, playerY + levelComponentSize / 2 - bombermanImage.getHeight(null), null);
			g2.setComposite(normalComposit);
			if (clientOptions.showPlayerNames) {
				final String playerName = publicClientOptions.playerNames[j];
				final int stringPosX = playerX - graphics.getFontMetrics().stringWidth(playerName) / 2;
				final int stringPosY = playerY + levelComponentSize / 2 - bombermanImage.getHeight(null);

				graphics.setColor(Color.BLACK);
				graphics.drawString(playerName, stringPosX, stringPosY);
				graphics.setColor(playerHudColor[playerNumberForGfx]);
				graphics.drawString(playerName, stringPosX - 1, stringPosY - 1);
			}
			if (clientOptions.showBombermenLives) {

				boolean uberMaxVitality = false;
				int vitality = playerModel.getVitality();
				if (vitality > MAX_PLAYER_VITALITY) {
					vitality = MAX_PLAYER_VITALITY;
					uberMaxVitality = true;
				}

				graphics.setColor(Color.BLACK);
				graphics.drawRect(playerX - levelComponentSize / 2, playerY + levelComponentSize / 2 + 2, levelComponentSize, 5);
				graphics.setColor(Color.WHITE);
				graphics.fillRect(playerX - levelComponentSize / 2 + 1, playerY + levelComponentSize / 2 + 3, levelComponentSize - 1, 4);

				if (uberMaxVitality) {
					graphics.setColor(Color.YELLOW);
				} else {
					graphics.setColor(Color.RED);
				}

				graphics.fillRect(playerX - levelComponentSize / 2 + 1, playerY + levelComponentSize / 2 + 3, (levelComponentSize - 1) * vitality
				        / MAX_PLAYER_VITALITY, 4);

			}
		}
	}

	/**
	 * Called when a new graphical theme has been loaded.
	 */
	public void graphicalThemeChanged() {
		refreshGraphicDatas();
	}

	/**
	 * Refreshes the references to the graphic datas. Regets the references to
	 * the graphic datas from the current graphics manager.
	 */
	private void refreshGraphicDatas() {
		final GraphicsManager graphicsManager = GraphicsManager.getCurrentManager();
		if (graphicsManager == null)
			return;

		wallImageHandlers = graphicsManager.getWallImageHandlers();
		itemImageHandlers = graphicsManager.getItemImageHandlers();
		playerGraphics = graphicsManager.getPlayerGraphics();
		bombPhaseHandlers = graphicsManager.getBombPhaseHandlers();
		firePhaseHandlers = graphicsManager.getFirePhaseHandlers();
		burningPhaseHandlers = graphicsManager.getBurningPhaseHandlers();

		playerHudColor = new Color[256];
		for (int i = 0; i < 256; i++) {
			playerHudColor[i] = new Color((i & 1) * 255, ((i & 2) >> 1) * 255, ((i & 4) >> 2) * 255);
		}
	}

	/**
	 * Sets the model provider that should be displayed.
	 * 
	 * @param modelProvider
	 *            the model provider that should be displayed
	 */
	public void setModelProvider(final ModelProvider modelProvider) {
		this.modelProvider = modelProvider;
	}

	/**
	 * Returns and clears before that the new actions since the last call of
	 * this method.
	 * 
	 * @return the new actions since the last call of this method.
	 */
	public String getAndClearNewActions() {
		final String actions_ = actions;
		actions = "";
		return actions_;
	}

	/**
	 * Closes the game scene, releases its resources.
	 */
	public void close() {
		removeKeyListener(this);
		clientOptionsManager.unregisterOptionsChangeListener(this);
	}

	/**
	 * Called when a key has been typed.
	 * 
	 * @param keyEvent
	 *            details of the key event
	 */
	public void keyTyped(final KeyEvent keyEvent) {}

	/**
	 * Called when a key has been pressed.
	 * 
	 * @param keyEvent
	 *            details of the key event
	 */
	public void keyPressed(final KeyEvent keyEvent) {
		final int keyCode = keyEvent.getKeyCode();

		final int gamePlayersFromHost = playersControlKeyStates.length;
		// Might not equal to the one at cilentOptions...
		for (int i = 0; i < gamePlayersFromHost; i++)
			for (int j = 0; j < playersControlKeys[i].length; j++)
				if (keyCode == playersControlKeys[i][j] && playersControlKeyStates[i][j] == false) {
					playersControlKeyStates[i][j] = true;
					if (actions.length() > 0)
						actions += SPACE_STRING;
					actions += i + SPACE_STRING + j + SPACE_STRING + 'p';
				}
	}

	/**
	 * Called when a key has been released.
	 * 
	 * @param keyEvent
	 *            details of the key event
	 */
	public void keyReleased(final KeyEvent keyEvent) {
		final int keyCode = keyEvent.getKeyCode();

		final int gamePlayersFromHost = playersControlKeyStates.length; // Might
		// not
		// equals
		// to
		// the
		// one
		// at
		// cilentOptions...
		for (int i = 0; i < gamePlayersFromHost; i++)
			for (int j = 0; j < playersControlKeys[i].length; j++)
				if (keyCode == playersControlKeys[i][j] && playersControlKeyStates[i][j] == true) {
					playersControlKeyStates[i][j] = false;
					if (actions.length() > 0)
						actions += SPACE_STRING;
					actions += i + SPACE_STRING + j + SPACE_STRING + 'r';
				}
	}

	/**
	 * Method to be called when client options may have been changed.
	 * 
	 * @param oldOptions
	 *            the old client options before the change signed by calling
	 *            this method
	 * @param newOptions
	 *            the new client options are about to become effective
	 */
	public void optionsChanged(final ClientOptions oldOptions, final ClientOptions newOptions) {
		final int gamePlayersFromHost = playersControlKeyStates.length; // Might
		// not
		// equals
		// to
		// the
		// one
		// at
		// cilentOptions...

		cycle1: for (int i = 0; i < gamePlayersFromHost; i++)
			for (int j = 0; j < newOptions.playersControlKeys[i].length; j++)
				if (newOptions.playersControlKeys[i][j] != oldOptions.playersControlKeys[i][j]) {
					playersControlKeys = newOptions.playersControlKeys;
					break cycle1;
				}
	}

	/**
	 * Called when new game starts.
	 */
	public void handleGameStarting() {
		final int playersFromHost = clientOptionsManager.getOptions().playersFromHost; // Number
		// of
		// players
		// from
		// host
		// cannot
		// (must
		// not)
		// be
		// change
		// during
		// a
		// game,
		// but
		// can
		// be
		// changed
		// between
		// games.
		playersControlKeyStates = new boolean[playersFromHost][playersControlKeys[0].length];
		actions = "";
	}

}
