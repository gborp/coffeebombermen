package com.braids.coffeebombermen.client.gamecore.view;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import com.braids.coffeebombermen.client.Client;
import com.braids.coffeebombermen.client.gamecore.Activities;
import com.braids.coffeebombermen.client.gamecore.BombPhases;
import com.braids.coffeebombermen.client.gamecore.CoreConsts;
import com.braids.coffeebombermen.client.gamecore.control.GameCoreHandler;
import com.braids.coffeebombermen.client.gamecore.model.BombModel;
import com.braids.coffeebombermen.client.gamecore.model.FireModel;
import com.braids.coffeebombermen.client.gamecore.model.PlayerModel;
import com.braids.coffeebombermen.client.gamecore.model.level.LevelComponent;
import com.braids.coffeebombermen.client.gamecore.model.level.LevelModel;
import com.braids.coffeebombermen.client.graphics.GraphicsManager;
import com.braids.coffeebombermen.client.graphics.ImageHandler;
import com.braids.coffeebombermen.options.Diseases;
import com.braids.coffeebombermen.options.OptConsts.Items;
import com.braids.coffeebombermen.options.OptConsts.Walls;
import com.braids.coffeebombermen.options.OptionsChangeListener;
import com.braids.coffeebombermen.options.OptionsManager;
import com.braids.coffeebombermen.options.model.ClientOptions;
import com.braids.coffeebombermen.options.model.PublicClientOptions;

/**
 * This is the game scene. Game will be displayed on this component.
 */
public class GameSceneComponent extends JComponent implements KeyListener, OptionsChangeListener<ClientOptions> {

	/** A string containing only a space. Used several times on keyboard events. */
	private static final String                 SPACE_STRING           = " ";

	private static final Color                  PLAYER_GFX_FLASH_1     = Color.WHITE;
	private static final Color                  PLAYER_GFX_FLASH_2     = Color.BLACK;

	private static final Color                  PLAYER_GFX_COLOR_BLIND = Color.BLACK;

	private static final long                   FLASH_EVERY_NTH_TICK   = 6;

	private static final int                    MAX_VISIBILITY_IN_FOG  = 10;

	public static final Color                   COLORIZATION_COLOR     = new Color(255, 0, 255);

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

	private GameCoreHandler                     gameCoreHandler;
	/**
	 * The sequence of actions made by the users on this component required for
	 * the game.
	 */
	private String                              actions;

	// Working parameters:
	/** Displayable size of the level components. */
	private int                                 levelComponentSize;

	private PlayerGraphic                       playerGraphics;

	private final Client                        client;

	private AlphaComposite                      normalComposit         = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);
	private AlphaComposite                      infectedComposite      = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);
	private AlphaComposite                      blackoutComposite      = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .80f);
	private AlphaComposite                      fireLightComposite     = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .25f);
	private AlphaComposite                      hallOfFameComposite    = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .75f);

	private AlphaComposite                      fogOfWar1Composite     = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .90f);
	private AlphaComposite                      fogOfWar2Composite     = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .80f);
	private AlphaComposite                      fogOfWar3Composite     = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .70f);
	private AlphaComposite                      fogOfWar4Composite     = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .60f);
	private AlphaComposite                      fogOfWar5Composite     = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .50f);
	private AlphaComposite                      fogOfWar6Composite     = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .40f);
	private AlphaComposite                      fogOfWar7Composite     = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .30f);
	private AlphaComposite                      fogOfWar8Composite     = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .20f);
	private AlphaComposite                      fogOfWar9Composite     = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .10f);

	private long                                blackOutDuration       = 30;
	private long                                flashDuration          = 3;
	private long                                nextFlashStart         = -1;

	private float                               hallOfFameX;
	private float                               hallOfFameY;
	private int                                 hallOfFameWidth;
	private int                                 hallOfFameHeight;

	private int[][]                             mVisibility;

	private int                                 levelWidth;

	private int                                 levelHeight;

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
		playersControlKeyStates = new boolean[0][playersControlKeys[0].length];
		// The 2nd dimension could be 0 size as well, but this is the proper
		// solution.

		this.clientOptionsManager.registerOptionsChangeListener(this);

		addKeyListener(this);

		refreshGraphicDatas();
	}

	/**
	 * Paints the actual view of the component: paints the actual view of the
	 * game scene.
	 * 
	 * @param g
	 *            graphics context in which to paint
	 */
	public void paintComponent(Graphics g) {
		if (GraphicsManager.getCurrentManager() == null) {
			// loaded
			return;
		}
		if (gameCoreHandler == null) {
			return;
		}
		if (gameCoreHandler.getLevelModel() == null) {
			return;
		}

		g.setColor(Color.GRAY);
		g.fillRect(0, 0, getWidth(), getHeight());

		long now = gameCoreHandler.getTick();

		boolean blackOut = false;
		boolean fogOfWar = false;
		boolean colorBlind = false;

		int ourIndex = client.getOurIndex();
		List<PlayerModel[]> clientPlayerModels = gameCoreHandler.getClientsPlayerModels();

		for (int i = 0; i < clientPlayerModels.size(); i++) {

			if (ourIndex == i) {
				for (PlayerModel playerModel : clientPlayerModels.get(i)) {
					if (playerModel.hasDisease(Diseases.BLACK_OUT)) {
						blackOut = true;
					}
					if (playerModel.hasDisease(Diseases.FOG_OF_WAR)) {
						fogOfWar = true;
					}
					if (playerModel.getOwnedDiseases().containsKey(Diseases.COLOR_BLIND)) {
						colorBlind = true;
					}
				}
			}
		}

		if (blackOut) {
			if (nextFlashStart == -1) {
				nextFlashStart = now;
			}

			if ((now >= nextFlashStart) && (now < (nextFlashStart + flashDuration))) {
				blackOut = false;
			} else {
				if (now >= (nextFlashStart + flashDuration)) {
					nextFlashStart = now + blackOutDuration;
				}
				blackOut = true;
			}
		}

		setWorkingParameters(g);

		paintLevel(g);
		paintBombs(g);
		paintBombermen(g, colorBlind);

		if (gameCoreHandler.getHasMoreThanOneAlivePlayer()) {
			if (fogOfWar) {
				paintFogOfWar(g);
			}

			if (blackOut) {
				paintBlackout(g);
			}
		} else {
			paintHallOfFrame(g);
		}
	}

	private void paintFogOfWar(Graphics graphics) {

		graphics.setColor(Color.BLACK);
		Graphics2D g2 = (Graphics2D) graphics;

		final List<PlayerModel[]> clientPlayerModels = gameCoreHandler.getClientsPlayerModels();
		LevelModel levelModel = gameCoreHandler.getLevelModel();

		if (mVisibility == null) {
			mVisibility = new int[levelModel.getHeight()][levelModel.getWidth()];
			for (int y = 0; y < levelModel.getHeight(); y++) {
				for (int x = 0; x < levelModel.getWidth(); x++) {
					mVisibility[y][x] = MAX_VISIBILITY_IN_FOG;
				}
			}
		}

		int ourIndex = client.getOurIndex();

		PlayerModel ownPlayerModel = null;
		for (int i = 0; i < clientPlayerModels.size(); i++) {
			// Easy with the enhanced for: modifying is possible during a
			// paint()

			final PlayerModel[] playerModels = clientPlayerModels.get(i);

			if (ourIndex == i) {
				ownPlayerModel = playerModels[0];
			}
		}

		if (ownPlayerModel == null) {
			return;
		}

		int px = ownPlayerModel.getComponentPosX();
		int py = ownPlayerModel.getComponentPosY();
		for (int y = -1; y < 2; y++) {
			for (int x = -1; x < 2; x++) {
				mVisibility[py + y][px + x] = MAX_VISIBILITY_IN_FOG;
			}
		}

		int dy = -2;
		while ((py + dy >= 0) && (levelModel.getComponent(px, py + dy + 1).getWall() == Walls.EMPTY)) {
			mVisibility[py + dy][px] = MAX_VISIBILITY_IN_FOG;
			dy--;
		}
		dy = 2;
		while ((py + dy < levelModel.getHeight()) && (levelModel.getComponent(px, py + dy - 1).getWall() == Walls.EMPTY)) {
			mVisibility[py + dy][px] = MAX_VISIBILITY_IN_FOG;
			dy++;
		}

		int dx = -2;
		while ((px + dx >= 0) && (levelModel.getComponent(px + dx + 1, py).getWall() == Walls.EMPTY)) {
			mVisibility[py][px + dx] = MAX_VISIBILITY_IN_FOG;
			dx--;
		}
		dx = 2;
		while ((px + dx < levelModel.getWidth()) && (levelModel.getComponent(px + dx - 1, py).getWall() == Walls.EMPTY)) {
			mVisibility[py][px + dx] = MAX_VISIBILITY_IN_FOG;
			dx++;
		}

		for (int i = 0, y = 0; i < levelModel.getHeight(); i++, y += levelComponentSize) {
			for (int j = 0, x = 0; j < levelModel.getWidth(); j++, x += levelComponentSize) {
				int visibility = mVisibility[i][j];
				if (visibility == 0) {
					g2.setComposite(normalComposit);
				} else if (visibility == 1) {
					g2.setComposite(fogOfWar1Composite);
				} else if (visibility == 2) {
					g2.setComposite(fogOfWar2Composite);
				} else if (visibility == 3) {
					g2.setComposite(fogOfWar3Composite);
				} else if (visibility == 4) {
					g2.setComposite(fogOfWar4Composite);
				} else if (visibility == 5) {
					g2.setComposite(fogOfWar5Composite);
				} else if (visibility == 6) {
					g2.setComposite(fogOfWar6Composite);
				} else if (visibility == 7) {
					g2.setComposite(fogOfWar7Composite);
				} else if (visibility == 8) {
					g2.setComposite(fogOfWar8Composite);
				} else if (visibility == 9) {
					g2.setComposite(fogOfWar9Composite);
				}

				if (visibility != MAX_VISIBILITY_IN_FOG) {
					g2.fillRect(x, y, levelComponentSize, levelComponentSize);
				}
				if ((visibility > 0) && ((gameCoreHandler.getTick() & 7) == 0)) {
					mVisibility[i][j] = visibility - 1;
				}
			}
		}
	}

	private void paintHallOfFrame(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setComposite(hallOfFameComposite);
		Font oldFont = g2.getFont();

		PlayerModel playerWon = gameCoreHandler.getTheLastRemainingPlyer();

		List<String> lstMessage = new ArrayList<String>();
		if (playerWon != null) {
			lstMessage.add(playerWon.getName() + " has won!");
		} else {
			lstMessage.add("Draw game.");
		}
		for (PlayerModel pm : gameCoreHandler.getAllPlayerModelsOrderedByPoint()) {
			lstMessage.add(pm.getPoints() + " - " + pm.getName());
		}

		float fontSize = getHeight() / 4 / lstMessage.size();
		g2.setFont(oldFont.deriveFont(fontSize).deriveFont(Font.BOLD));

		FontMetrics fm = g2.getFontMetrics();

		int maxWidth = -1;
		for (String line : lstMessage) {
			int width = fm.stringWidth(line);
			if (width > maxWidth) {
				maxWidth = width;
			}
		}
		hallOfFameWidth = maxWidth;
		hallOfFameHeight = (int) (lstMessage.size() * fontSize * 1.5 - 0.5 * fontSize);

		int y = (int) hallOfFameY;
		g2.setColor(Color.BLACK);
		g2.drawLine((int) hallOfFameX, (int) (y + fontSize * 1.5), (int) hallOfFameX + hallOfFameWidth, (int) (y + fontSize * 1.5));
		g2.setColor(Color.WHITE);
		g2.drawLine((int) hallOfFameX, (int) (y + fontSize * 1.5) + 1, (int) hallOfFameX + hallOfFameWidth, (int) (y + fontSize * 1.5) + 1);

		for (String line : lstMessage) {
			g2.setColor(Color.BLACK);
			g2.drawString(line, hallOfFameX, y + fontSize);
			g2.setColor(Color.WHITE);
			g2.drawString(line, hallOfFameX + 2, y + fontSize + 2);
			y += fontSize * 1.5;
		}

		g2.setFont(oldFont);
		g2.setComposite(normalComposit);
		moveHallOfFame(playerWon);
	}

	private void moveHallOfFame(PlayerModel playerWon) {
		int tx = 0;
		int ty = 0;
		if (playerWon == null) {
			tx = levelWidth * levelComponentSize / 2 - hallOfFameWidth / 2;
			ty = levelHeight * levelComponentSize / 2 - hallOfFameHeight / 2;
		} else {
			tx = (playerWon.getPosX() + 0) * levelComponentSize / CoreConsts.LEVEL_COMPONENT_GRANULARITY;
			ty = (playerWon.getPosY() + 0) * levelComponentSize / CoreConsts.LEVEL_COMPONENT_GRANULARITY;

			tx = tx - hallOfFameWidth / 2;
			ty = ty - hallOfFameHeight / 2;
			if (tx < 0) {
				tx = 0;
			} else if (tx > levelWidth * levelComponentSize - hallOfFameWidth) {
				tx = levelWidth * levelComponentSize - hallOfFameWidth;
			}

			if (ty < 0) {
				ty = 0;
			} else if (ty > levelHeight * levelComponentSize - hallOfFameHeight) {
				ty = levelHeight * levelComponentSize - hallOfFameHeight;
			}
		}
		hallOfFameX = (hallOfFameX * 14 + tx) / 15;
		hallOfFameY = (hallOfFameY * 14 + ty) / 15;
	}

	private void paintBlackout(Graphics graphics) {

		graphics.setColor(Color.BLACK);
		Graphics2D g2 = (Graphics2D) graphics;

		g2.setComposite(blackoutComposite);

		if (!hasActualDetonation()) {
			g2.fillRect(0, 0, gameCoreHandler.getLevelModel().getWidth() * levelComponentSize, gameCoreHandler.getLevelModel().getHeight() * levelComponentSize);
			g2.setComposite(normalComposit);
			return;
		}

		LevelModel levelModel = gameCoreHandler.getLevelModel();

		for (int i = 0, y = 0; i < levelModel.getHeight(); i++, y += levelComponentSize) {
			for (int j = 0, x = 0; j < levelModel.getWidth(); j++, x += levelComponentSize) {
				LevelComponent levelComponent = levelModel.getComponent(j, i);
				if (levelComponent.hasFire()) {
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

				if ((i < levelModel.getHeight() - 1) && levelModel.getComponent(j, i + 1).hasFire()) {
					fwHeight -= levelComponentSize / 4;
				}
				if ((i > 0) && levelModel.getComponent(j, i - 1).hasFire()) {
					fwTop = true;
					fwHeight -= levelComponentSize / 4;
				}
				if ((j < levelModel.getWidth() - 1) && levelModel.getComponent(j + 1, i).hasFire()) {
					fwWidth -= levelComponentSize / 4;
				}

				if ((j > 1) && levelModel.getComponent(j - 1, i).hasFire()) {
					fwLeft = true;
					fwWidth -= levelComponentSize / 4;
				}

				g2.fillRect(x + (fwLeft ? levelComponentSize / 4 : 0), y + (fwTop ? levelComponentSize / 4 : 0), fwWidth, fwHeight);
			}
		}

		g2.setComposite(normalComposit);
	}

	private boolean hasActualDetonation() {
		LevelModel levelModel = gameCoreHandler.getLevelModel();

		for (int i = 0, y = 0; i < levelModel.getHeight(); i++, y += levelComponentSize) {
			for (int j = 0, x = 0; j < levelModel.getWidth(); j++, x += levelComponentSize) {

				if (levelModel.getComponent(j, i).hasFire()) {
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
		LevelModel levelModel = gameCoreHandler.getLevelModel();
		int originalLevelComponentSize = wallImageHandlers[0].getOriginalWidth();
		// Equals to wallImageHandlers[0].getOriginalHeight()

		int sceneWidth = getWidth();
		int sceneHeight = getHeight();

		levelWidth = levelModel.getWidth();
		levelHeight = levelModel.getHeight();
		float zoomFactor = Math.min((float) sceneWidth / (levelWidth * originalLevelComponentSize), (float) sceneHeight
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
		LevelModel levelModel = gameCoreHandler.getLevelModel();
		float wallScaleFactor = (float) levelComponentSize / wallImageHandlers[0].getOriginalWidth();
		float itemScaleFactor = (float) levelComponentSize / itemImageHandlers[0].getOriginalWidth();
		float fireScaleFactor = (float) levelComponentSize / firePhaseHandlers[0][0].getOriginalWidth();
		float burningScaleFactor = (float) levelComponentSize / burningPhaseHandlers[0].getOriginalWidth();

		int height = levelModel.getHeight();
		int width = levelModel.getWidth();

		for (int i = 0, y = 0; i < height; i++, y += levelComponentSize) {
			for (int j = 0, x = 0; j < width; j++, x += levelComponentSize) {
				final LevelComponent levelComponent = levelModel.getComponent(j, i);
				Walls wall = levelComponent.getWall();
				Items item = levelComponent.getItem();
				if (!levelComponent.hasFire()) {
					if ((wall == Walls.EMPTY) && (item != null)) {
						// item
						g.drawImage(itemImageHandlers[item.ordinal()].getScaledImage(itemScaleFactor, false), x, y, null);
					} else {
						// wall
						g.drawImage(wallImageHandlers[wall.ordinal()].getScaledImage(wallScaleFactor, false), x, y, null);
						if (!wall.equals(Walls.EMPTY)) {
							boolean fwTop = false;
							boolean fwBottom = false;
							boolean fwLeft = false;
							boolean fwRight = false;
							int fwLeftHeight = levelComponentSize;
							int fwRightHeight = levelComponentSize;

							if ((i < levelModel.getHeight() - 1) && levelModel.getComponent(j, i + 1).hasFire()) {
								fwTop = true;
								fwLeftHeight -= levelComponentSize / 4;
								fwRightHeight -= levelComponentSize / 4;
							}
							if ((i > 0) && levelModel.getComponent(j, i - 1).hasFire()) {
								fwBottom = true;
								fwLeftHeight -= levelComponentSize / 4;
								fwRightHeight -= levelComponentSize / 4;
							}
							if ((j < levelModel.getWidth() - 1) && levelModel.getComponent(j + 1, i).hasFire()) {
								fwRight = true;
							}

							if ((j > 1) && levelModel.getComponent(j - 1, i).hasFire()) {
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
					final FireModel fireModel = levelComponent.getLastFire();

					if ((wall == Walls.EMPTY) && (item == null)) {
						g.drawImage(wallImageHandlers[wall.ordinal()].getScaledImage(wallScaleFactor, false), x, y, null);
						paintFire(g, fireModel, x, y, fireScaleFactor);
					} else {
						if (fireModel.getIterationCounter() < CoreConsts.FIRE_ITERATIONS / 2) {
							// The original wall or item is burning.
							if (wall == Walls.EMPTY) {
								// item
								g.drawImage(itemImageHandlers[item.ordinal()].getScaledImage(itemScaleFactor, false), x, y, null);
							} else {
								// wall
								g.drawImage(wallImageHandlers[wall.ordinal()].getScaledImage(wallScaleFactor, false), x, y, null);
							}
						} else {
							// Now the item or the empty wall what will remain
							// after the burning is visible through the burning.
							if ((wall != Walls.EMPTY) && (item != null)) {
								// An item will remain
								g.drawImage(itemImageHandlers[item.ordinal()].getScaledImage(itemScaleFactor, false), x, y, null);
							} else {
								// Empty wall will remain
								g.drawImage(wallImageHandlers[Walls.EMPTY.ordinal()].getScaledImage(wallScaleFactor, false), x, y, null);
							}
						}
						g.drawImage(burningPhaseHandlers[burningPhaseHandlers.length * fireModel.getIterationCounter() / CoreConsts.FIRE_ITERATIONS]
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
		final List<BombModel> bombModels = gameCoreHandler.getBombModels();

		if (bombModels == null) {
			return;
		}

		final float scaleFactor = (float) levelComponentSize / bombPhaseHandlers[0][0].getOriginalWidth();

		for (int i = 0; i < bombModels.size(); i++) {
			// Easy with the enhanced for: modifying is possible during a
			// paint()
			final BombModel bombModel = bombModels.get(i);
			final int phasesCount = bombPhaseHandlers[bombModel.getType().ordinal()].length;

			PlayerModel ownerPlayer = bombModel.getOwnerPlayer();
			Color color;
			if (ownerPlayer != null) {
				color = bombModel.getOwnerPlayer().getColor().value;
			} else {
				color = Color.BLACK;
			}

			final Image bombImage = bombPhaseHandlers[bombModel.getType().ordinal()][bombModel.isDeadBomb() ? 0 : phasesCount * bombModel.getIterationCounter()
			        / CoreConsts.BOMB_ITERATIONS].getScaledImage(scaleFactor, true, COLORIZATION_COLOR, color);

			int posYCorrection = 0;
			if (bombModel.getPhase() == BombPhases.FLYING) {
				if (bombModel.getIterationsDuringPhase() * CoreConsts.BOMB_FLYING_SPEED < CoreConsts.BOMB_FLYING_DISTANCE) {
					posYCorrection = -(int) (CoreConsts.BOMB_FLYING_ASCENDENCE_PRIMARY * Math.sin(Math.PI * bombModel.getIterationsDuringPhase()
					        * CoreConsts.BOMB_FLYING_SPEED / CoreConsts.BOMB_FLYING_DISTANCE));
				} else {
					final int posXInComponent = bombModel.getPosX() % CoreConsts.LEVEL_COMPONENT_GRANULARITY;
					posYCorrection = -(int) (CoreConsts.BOMB_FLYING_ASCENDENCE_SECONDARY * Math.sin(Math.PI
					        * (posXInComponent + (posXInComponent < CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2 ? CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2
					                : -CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2)) / CoreConsts.LEVEL_COMPONENT_GRANULARITY));
				}
			}

			graphics.drawImage(bombImage, bombModel.getPosX() * levelComponentSize / CoreConsts.LEVEL_COMPONENT_GRANULARITY - levelComponentSize / 2,
			        (bombModel.getPosY() + posYCorrection) * levelComponentSize / CoreConsts.LEVEL_COMPONENT_GRANULARITY - levelComponentSize / 2, null);
		}
	}

	/**
	 * Paints the bombermen.
	 * 
	 * @param graphics
	 *            graphics context in which to paint
	 */
	private void paintBombermen(final Graphics graphics, boolean colorBlind) {
		final List<PlayerModel[]> clientPlayerModels = gameCoreHandler.getClientsPlayerModels();

		final float scaleFactor = (float) levelComponentSize / playerGraphics.getOriginalWidth();
		final ClientOptions clientOptions = clientOptionsManager.getOptions();

		int ourIndex = client.getOurIndex();
		for (int i = 0; i < clientPlayerModels.size(); i++) {
			if (ourIndex != i) {
				final PlayerModel[] playerModels = clientPlayerModels.get(i);
				PublicClientOptions publicClientOptions = gameCoreHandler.getClientsPublicClientOptions().get(i);
				paintOneBomberMan(graphics, clientOptions, publicClientOptions, playerModels, scaleFactor, colorBlind);
			}

		}
		for (int i = 0; i < clientPlayerModels.size(); i++) { // Easy with the
			if (ourIndex == i) {
				final PlayerModel[] playerModels = clientPlayerModels.get(i);
				PublicClientOptions publicClientOptions = gameCoreHandler.getClientsPublicClientOptions().get(i);
				paintOneBomberMan(graphics, clientOptions, publicClientOptions, playerModels, scaleFactor, colorBlind);
			}
		}
	}

	public void paintOneBomberMan(Graphics graphics, ClientOptions clientOptions, PublicClientOptions publicClientOptions, PlayerModel[] playerModels,
	        float scaleFactor, boolean colorBlind) {

		Graphics2D g2 = (Graphics2D) graphics;

		for (int j = 0; j < playerModels.length; j++) {

			final PlayerModel playerModel = playerModels[j];
			if ((playerModel.getActivity() == Activities.DYING) && (playerModel.getIterationCounter() + 1 >= playerModel.getActivity().activityIterations)) {
				// This is a dead player, must not be painted.
				continue;
			}

			if (playerModel.getActivity() == null) {
				continue;
			}

			Color effectivePlayerColor = playerModel.getColor().value;

			if (colorBlind) {
				effectivePlayerColor = PLAYER_GFX_COLOR_BLIND;
			}

			if (playerModel.hasDiseases()) {
				if ((gameCoreHandler.getTick() % FLASH_EVERY_NTH_TICK) == 0) {
					g2.setComposite(infectedComposite);
					effectivePlayerColor = PLAYER_GFX_FLASH_1;
				} else if ((gameCoreHandler.getTick() % FLASH_EVERY_NTH_TICK) == FLASH_EVERY_NTH_TICK / 2) {
					g2.setComposite(infectedComposite);
					effectivePlayerColor = PLAYER_GFX_FLASH_2;
				}
			}

			Image bombermanImage = playerGraphics.getImage(playerModel, scaleFactor, effectivePlayerColor);

			g2.setComposite(normalComposit);

			int playerX = playerModel.getPosX() * levelComponentSize / CoreConsts.LEVEL_COMPONENT_GRANULARITY;
			int playerY = playerModel.getPosY() * levelComponentSize / CoreConsts.LEVEL_COMPONENT_GRANULARITY;

			// Position is tricky: head of bomberman may take place on the
			// row over the position of bomberman
			graphics.drawImage(bombermanImage, playerX - levelComponentSize / 2, playerY + levelComponentSize / 2 - bombermanImage.getHeight(null), null);
			g2.setComposite(normalComposit);
			if (clientOptions.showPlayerNames && !colorBlind) {
				final String playerName = publicClientOptions.playerNames[j];
				final int stringPosX = playerX - graphics.getFontMetrics().stringWidth(playerName) / 2;
				final int stringPosY = playerY + levelComponentSize / 2 - bombermanImage.getHeight(null);

				graphics.setColor(Color.BLACK);
				graphics.drawString(playerName, stringPosX, stringPosY);
				graphics.setColor(playerModel.getColor().value);
				graphics.drawString(playerName, stringPosX - 1, stringPosY - 1);
			}
			if (clientOptions.showBombermenLives) {

				boolean uberMaxVitality = false;
				int vitality = playerModel.getVitality();
				if (vitality > CoreConsts.MAX_PLAYER_VITALITY) {
					vitality = CoreConsts.MAX_PLAYER_VITALITY;
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
				        / CoreConsts.MAX_PLAYER_VITALITY, 4);

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
		if (graphicsManager == null) {
			return;
		}

		wallImageHandlers = graphicsManager.getWallImageHandlers();
		itemImageHandlers = graphicsManager.getItemImageHandlers();
		playerGraphics = graphicsManager.getPlayerGraphics();
		bombPhaseHandlers = graphicsManager.getBombPhaseHandlers();
		firePhaseHandlers = graphicsManager.getFirePhaseHandlers();
		burningPhaseHandlers = graphicsManager.getBurningPhaseHandlers();

	}

	/**
	 * Sets the model provider that should be displayed.
	 * 
	 * @param modelProvider
	 *            the model provider that should be displayed
	 */
	public void setGameCoreHandler(final GameCoreHandler gameCoreHandler) {
		this.gameCoreHandler = gameCoreHandler;
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
		for (int i = 0; i < gamePlayersFromHost; i++) {
			for (int j = 0; j < playersControlKeys[i].length; j++) {
				if ((keyCode == playersControlKeys[i][j]) && (playersControlKeyStates[i][j] == false)) {
					playersControlKeyStates[i][j] = true;
					if (actions.length() > 0) {
						actions += SPACE_STRING;
					}
					actions += i + SPACE_STRING + j + SPACE_STRING + 'p';
				}
			}
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
		for (int i = 0; i < gamePlayersFromHost; i++) {
			for (int j = 0; j < playersControlKeys[i].length; j++) {
				if ((keyCode == playersControlKeys[i][j]) && (playersControlKeyStates[i][j] == true)) {
					playersControlKeyStates[i][j] = false;
					if (actions.length() > 0) {
						actions += SPACE_STRING;
					}
					actions += i + SPACE_STRING + j + SPACE_STRING + 'r';
				}
			}
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
		final int gamePlayersFromHost = playersControlKeyStates.length;
		// Might not equals to the one at cilentOptions...

		cycle1: for (int i = 0; i < gamePlayersFromHost; i++) {
			for (int j = 0; j < newOptions.playersControlKeys[i].length; j++) {
				if (newOptions.playersControlKeys[i][j] != oldOptions.playersControlKeys[i][j]) {
					playersControlKeys = newOptions.playersControlKeys;
					break cycle1;
				}
			}
		}
	}

	/**
	 * Called when new game starts.
	 */
	public void handleGameStarting() {
		final int playersFromHost = clientOptionsManager.getOptions().playersFromHost;
		// Number of players from host cannot (must not) be change during a
		// game, but can be changed between games.
		playersControlKeyStates = new boolean[playersFromHost][playersControlKeys[0].length];
		actions = "";
		mVisibility = null;
	}

	public void paintFire(Graphics g, FireModel fireModel, int x, int y, float fireScaleFactor) {
		int firePhasesCount = firePhaseHandlers[fireModel.getShape().ordinal()].length;
		PlayerModel ownerPlayer = fireModel.getOwnerPlayer();
		Color color;
		if (ownerPlayer != null) {
			color = fireModel.getOwnerPlayer().getColor().value;
		} else {
			color = Color.BLACK;
		}
		g.drawImage(firePhaseHandlers[fireModel.getShape().ordinal()][firePhasesCount * fireModel.getIterationCounter() / CoreConsts.FIRE_ITERATIONS]
		        .getScaledImage(fireScaleFactor, true, COLORIZATION_COLOR, color), x, y, null);
	}

}
