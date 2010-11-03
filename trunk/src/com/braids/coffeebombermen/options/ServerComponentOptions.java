package com.braids.coffeebombermen.options;

import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import com.braids.coffeebombermen.Consts;
import com.braids.coffeebombermen.options.OptConsts.GameTypes;
import com.braids.coffeebombermen.options.OptConsts.KillsBelongTos;
import com.braids.coffeebombermen.options.OptConsts.NetworkLatencies;
import com.braids.coffeebombermen.options.model.ServerOptions;
import com.braids.coffeebombermen.utils.GeneralUtilities;

/**
 * This class makes the server options available for viewing and changing on the
 * screen.<br>
 * The manageable server options is passed to the constructor.<br>
 * For handling level options of server options uses a level component options
 * object.
 */
public class ServerComponentOptions extends ComponentOptions<ServerOptions> {

	/** Index of the randomly generated level in the level names combo. */
	private static final int              RANDOMLY_GENERATED_LEVEL_INDEX                      = 0;
	/** Name of the randomly generated level in the level names combo. */
	public static final String            RANDOMLY_GENERATED_LEVEL_NAME                       = "<randomly generated>";

	/** Default value of level name. */
	private static final String           DEFAULT_LEVEL_NAME                                  = RANDOMLY_GENERATED_LEVEL_NAME;
	/** Default value of game type option. */
	private static final GameTypes        DEFAULT_GAME_TYPE                                   = GameTypes.FREE_FOR_ALL;
	/** Minimum value of round time limit option in seconds. */
	private static final int              MINIMUM_ROUND_TIME_LIMIT                            = 0;                                                            // 0
	// can
	// mean
	// no
	// limit
	/** Default value of round time limit option in seconds. */
	private static final int              DEFAULT_ROUND_TIME_LIMIT                            = 90;
	/** Maximum value of round time limit option in seconds. */
	private static final int              MAXIMUM_ROUND_TIME_LIMIT                            = 3600;
	/** Minimum value of game point limit option. */
	private static final int              MINIMUM_GAME_POINT_LIMIT                            = 1;
	/** Default value of game point limit option. */
	private static final int              DEFAULT_GAME_POINT_LIMIT                            = 5;
	/** Maximum value of game point limit option. */
	private static final int              MAXIMUM_GAME_POINT_LIMIT                            = 1000;
	/** Default value of password option. */
	private static final String           DEFAULT_PASSWORD                                    = "";

	/** Minimum value of damage of whole bomb fire option. */
	private static final int              MINIMUM_DAMAGE_OF_WHOLE_BOMB_FIRE                   = 0;
	/** Default value of damage of whole bomb fire option. */
	private static final int              DEFAULT_DAMAGE_OF_WHOLE_BOMB_FIRE                   = 100;
	/** Maximum value of damage of whole bomb fire option. */
	private static final int              MAXIMUM_DAMAGE_OF_WHOLE_BOMB_FIRE                   = 10000;                                                        // Maximum
	// must
	// be
	// big
	// enough
	// to
	// kill
	// bomberman
	// in
	// 1
	// iteration!
	/** Default value of explosion annihilates diseases option. */
	private static final boolean          DEFAULT_EXPLOSION_ANNIHILATES_DISEASES              = true;
	/** Default value of fire doesn't hurt teammantes option. */
	private static final boolean          DEFAULT_FIRE_DOESNT_HURT_TEAMMATES                  = false;
	/** Default value of new random positions after each round option. */
	private static final boolean          DEFAULT_NEW_RANDOM_POSITIONS_AFTER_ROUNDS           = true;
	/** Default value of bombs explode after one remained option. */
	private static final boolean          DEFAULT_BOMBS_EXPLODE_AFTER_ONE_REMAINED            = false;
	/** Default value of building up walls stops after one remained option. */
	private static final boolean          DEFAULT_BUILDING_UP_WALLS_STOPS_AFTER_ONE_REMAINED  = true;
	/** Default value of items stop rolling bombs option. */
	private static final boolean          DEFAULT_ITEMS_STOP_ROLLING_BOMBS                    = false;
	/** Default value of punched bombs come back at the opposite end option. */
	private static final boolean          DEFAULT_PUNCHED_BOMBS_COME_BACK_AT_THE_OPPOSITE_END = true;
	/** Default value of multiple fire. */
	private static final boolean          DEFAULT_MULTIPLE_FIRE                               = true;
	/** Default value of swap only living player. */
	private static final boolean          DEFAULT_SWAP_ONLY_LIVING_PLAYER                     = true;
	/** Default value of auto restart game. */
	private static final boolean          DEFAULT_AUTO_RESTART_GAME                           = true;
	/** Default value of kills belong to option. */
	private static final KillsBelongTos   DEFAULT_KILLS_BELONG_TO                             = KillsBelongTos.OWNER_OF_TRIGGERER_BOMB;

	/** Minimum value of amount of brick walls option. */
	private static final int              MINIMUM_AMOUNT_OF_BRICK_WALLS                       = 0;
	/** Default value of amount of brick walls option. */
	private static final int              DEFAULT_AMOUNT_OF_BRICK_WALLS                       = 90;
	/** Maximum value of amount of brick walls option. */
	private static final int              MAXIMUM_AMOUNT_OF_BRICK_WALLS                       = 100;
	/** Minimum value of getting item probability option. */
	private static final int              MINIMUM_GETTING_ITEM_PROBABILITY                    = 0;
	/** Default value of getting item probability option. */
	private static final int              DEFAULT_GETTING_ITEM_PROBABILITY                    = 40;
	/** Maximum value of getting item probability option. */
	private static final int              MAXIMUM_GETTING_ITEM_PROBABILITY                    = 100;

	/** Minimum value of max gateway number option. */
	private static final int              MINIMUM_MAX_GATEWAY_NUMBER                          = 0;
	/** Default value of max gateway number option. */
	private static final int              DEFAULT_MAX_GATEWAY_NUMBER                          = 1;
	/** Maximum value of max gateway number option. */
	private static final int              MAXIMUM_MAX_GATEWAY_NUMBER                          = 10;

	/** Minimum value of game cycle frequency option in 1/s (Hz). */
	private static final int              MINIMUM_GAME_CYCLE_FREQUENCY                        = 1;
	/** Default value of game cycle frequency option in 1/s (Hz). */
	private static final int              DEFAULT_GAME_CYCLE_FREQUENCY                        = 30;
	/** Maximum value of game cycle frequency option in 1/s (Hz). */
	private static final int              MAXIMUM_GAME_CYCLE_FREQUENCY                        = 100;
	/** Default value of network latency. */
	private static final NetworkLatencies DEFAULT_NETWORK_LATENCY                             = NetworkLatencies.LOW;

	// Port constants are imported from classes.options.Consts!

	/** Component for graphical theme option. */
	private final JComboBox               levelName_c                                         = new JComboBox(GeneralUtilities
	                                                                                                  .getFileNamesWithoutExtension(
	                                                                                                          Consts.LEVELS_DIRECTORY_NAME,
	                                                                                                          Consts.LEVEL_FILE_EXTENSION));
	/** Component for game type option. */
	private final JComboBox               gameType_c                                          = new JComboBox(GameTypes.values());
	/** Component for round time limit option. */
	private final JSpinner                roundTimeLimit_c                                    = new JSpinner(new SpinnerNumberModel(DEFAULT_ROUND_TIME_LIMIT,
	                                                                                                  MINIMUM_ROUND_TIME_LIMIT, MAXIMUM_ROUND_TIME_LIMIT, 1));
	/** Component for game point limit option. */
	private final JSpinner                gamePointLimit_c                                    = new JSpinner(new SpinnerNumberModel(DEFAULT_GAME_POINT_LIMIT,
	                                                                                                  MINIMUM_GAME_POINT_LIMIT, MAXIMUM_GAME_POINT_LIMIT, 1));
	/** Component for password option. */
	private final JTextField              password_c                                          = new JTextField(15);

	/** Component for damage of whole bomb fire option. */
	private final JSpinner                damageOfWholeBombFire_c                             = new JSpinner(new SpinnerNumberModel(
	                                                                                                  DEFAULT_DAMAGE_OF_WHOLE_BOMB_FIRE,
	                                                                                                  MINIMUM_DAMAGE_OF_WHOLE_BOMB_FIRE,
	                                                                                                  MAXIMUM_DAMAGE_OF_WHOLE_BOMB_FIRE, 1));
	/** Component for explosion annihilates diseases option. */
	private final JCheckBox               explosionAnnihilatesDiseases_c                      = new JCheckBox("Explosion annihilates diseases");
	/** Component for fire doesn't hurt teammantes option. */
	private final JCheckBox               fireDoesntHurtTeammates_c                           = new JCheckBox("Fire doesn't hurt teammates");
	/** Component for new random positions after each round option. */
	private final JCheckBox               newRandomPositionsAfterRounds_c                     = new JCheckBox("New random positions after each round");
	/** Component for bombs explode after one remained option. */
	private final JCheckBox               bombsExplodeAfterOneRemained_c                      = new JCheckBox("Bombs explode after one remained");
	/** Component for building up walls stops after one remained option. */
	private final JCheckBox               buildingUpWallsStopsAfterOneRemained_c              = new JCheckBox("Building up walls stops after one remained");
	/** Component for items stop rolling bombs option. */
	private final JCheckBox               itemsStopRollingBombs_c                             = new JCheckBox("Items stop rolling bombs");
	/** Component for punched bombs come back at the opposite end option. */
	private final JCheckBox               punchedBombsComeBackAtTheOppositeEnd_c              = new JCheckBox("Punched bombs come back at the opposite end");
	/** Component for multiple fire. */
	private final JCheckBox               multipleFire_c                                      = new JCheckBox("Multiple fire");
	/** Component for swap only living player option. */
	private final JCheckBox               swapOnlyLivingPlayer_c                              = new JCheckBox("Swap only living player");
	/** Component for auto restart game option. */
	private final JCheckBox               autoRestartGame_c                                   = new JCheckBox("Auto restart game");
	/** Component for kills belong to option. */
	private final ButtonGroup             killsBelongTo_c                                     = new ButtonGroup();

	/** Component for amount of brick walls option. */
	private final JSpinner                amountOfBrickWalls_c                                = new JSpinner(new SpinnerNumberModel(
	                                                                                                  DEFAULT_AMOUNT_OF_BRICK_WALLS,
	                                                                                                  MINIMUM_AMOUNT_OF_BRICK_WALLS,
	                                                                                                  MAXIMUM_AMOUNT_OF_BRICK_WALLS, 1));
	/** Component for getting item probability option. */
	private final JSpinner                gettingItemProbability_c                            = new JSpinner(new SpinnerNumberModel(
	                                                                                                  DEFAULT_GETTING_ITEM_PROBABILITY,
	                                                                                                  MINIMUM_GETTING_ITEM_PROBABILITY,
	                                                                                                  MAXIMUM_GETTING_ITEM_PROBABILITY, 1));

	/** Component for maximum gateway number option. */
	private final JSpinner                maxGatewayNumber_c                                  = new JSpinner(
	                                                                                                  new SpinnerNumberModel(DEFAULT_MAX_GATEWAY_NUMBER,
	                                                                                                          MINIMUM_MAX_GATEWAY_NUMBER,
	                                                                                                          MAXIMUM_MAX_GATEWAY_NUMBER, 1));

	/** Component for game cycle frequency option. */
	private final JSpinner                gameCycleFrequency_c                                = new JSpinner(new SpinnerNumberModel(
	                                                                                                  DEFAULT_GAME_CYCLE_FREQUENCY,
	                                                                                                  MINIMUM_GAME_CYCLE_FREQUENCY,
	                                                                                                  MAXIMUM_GAME_CYCLE_FREQUENCY, 1));
	/** Component for game port option. */
	private final JSpinner                gamePort_c                                          = new JSpinner(new SpinnerNumberModel(
	                                                                                                  OptConsts.DEFAULT_GAME_PORT, OptConsts.MINIMUM_GAME_PORT,
	                                                                                                  OptConsts.MAXIMUM_GAME_PORT, 1));
	/** Component for network latency. */
	private final JComboBox               networkLatency_c                                    = new JComboBox(NetworkLatencies.values());

	/**
	 * Level component options to handle the level options of the server options
	 * (level options of random levels).
	 */
	private final LevelComponentOptions   levelComponentOptions;

	/**
	 * Creates a new ServerComponentOptions.<br>
	 * The new server component options will contain changable options.
	 * 
	 * @param serverOptions
	 *            the server options object to be handled
	 */
	public ServerComponentOptions(final ServerOptions serverOptions) {
		this(serverOptions, false);
	}

	/**
	 * Creates a new ServerComponentOptions.
	 * 
	 * @param serverOptions
	 *            the server options object to be handled
	 * @param viewOnly
	 *            tells whether we just want to view the options but not to
	 *            modify
	 */
	public ServerComponentOptions(final ServerOptions serverOptions, final boolean viewOnly) {
		super(serverOptions);

		// Must be instantiated before calling buildOptionsTabbedPane(), because
		// that method
		// uses the options tabbed pane of the level component options!
		levelComponentOptions = new LevelComponentOptions(options.getLevelOptions(), viewOnly);

		levelName_c.insertItemAt(RANDOMLY_GENERATED_LEVEL_NAME, RANDOMLY_GENERATED_LEVEL_INDEX);
		levelName_c.setSelectedIndex(RANDOMLY_GENERATED_LEVEL_INDEX);

		for (final KillsBelongTos killsBelongTo : KillsBelongTos.values()) {
			final JRadioButton radioButton = new JRadioButton(killsBelongTo.toString());
			radioButton.setActionCommand(killsBelongTo.name());
			killsBelongTo_c.add(radioButton);
		}

		buildOptionsTabbedPane(viewOnly);
	}

	/**
	 * Builds the options tabbed pane.
	 * 
	 * @param viewOnly
	 *            tells whether we just want to view the options but not to
	 *            modify
	 */
	private void buildOptionsTabbedPane(final boolean viewOnly) {
		final boolean componentsEnabled = viewOnly ? false : true;
		JPanel panel;
		Box box;

		box = Box.createVerticalBox();
		panel = new JPanel();
		panel.add(createLabel("Level:", componentsEnabled));
		levelName_c.setEnabled(componentsEnabled);
		panel.add(levelName_c);
		box.add(panel);
		panel = new JPanel();
		panel.add(createLabel("Game type:", componentsEnabled));
		gameType_c.setEnabled(componentsEnabled);
		panel.add(gameType_c);
		box.add(panel);
		panel = new JPanel();
		panel.add(createLabel("Round time limit:", componentsEnabled));
		roundTimeLimit_c.setToolTipText("0 means no limit");
		roundTimeLimit_c.setEnabled(componentsEnabled);
		panel.add(roundTimeLimit_c);
		panel.add(createLabel("sec.", componentsEnabled));
		box.add(panel);
		panel = new JPanel();
		panel.add(createLabel("Game point limit:", componentsEnabled));
		gamePointLimit_c.setEnabled(componentsEnabled);
		panel.add(gamePointLimit_c);
		box.add(panel);
		panel = new JPanel();
		panel.add(createLabel("Password:", componentsEnabled));
		password_c.setEnabled(componentsEnabled);
		panel.add(password_c);
		box.add(panel);
		panel = new JPanel();
		panel.add(box);
		optionsTabbedPane.addTab("Main", panel);

		box = Box.createVerticalBox();
		panel = new JPanel();
		panel.add(createLabel("Damage of the whole fire of a bomb:", componentsEnabled));
		damageOfWholeBombFire_c.setToolTipText("100 % means to kill exactly a healthy bomberman.");
		damageOfWholeBombFire_c.setEnabled(componentsEnabled);
		panel.add(damageOfWholeBombFire_c);
		panel.add(createLabel("%.", componentsEnabled));
		box.add(panel);
		panel = new JPanel();
		explosionAnnihilatesDiseases_c.setToolTipText("If unchecked, exploding diseases will be replaced on the level.");
		explosionAnnihilatesDiseases_c.setEnabled(componentsEnabled);
		panel.add(explosionAnnihilatesDiseases_c);
		box.add(panel);
		panel = new JPanel();
		fireDoesntHurtTeammates_c.setEnabled(componentsEnabled);
		panel.add(fireDoesntHurtTeammates_c);
		box.add(panel);
		panel = new JPanel();
		newRandomPositionsAfterRounds_c.setEnabled(componentsEnabled);
		panel.add(newRandomPositionsAfterRounds_c);
		box.add(panel);
		panel = new JPanel();
		bombsExplodeAfterOneRemained_c.setEnabled(componentsEnabled);
		panel.add(bombsExplodeAfterOneRemained_c);
		box.add(panel);
		panel = new JPanel();
		buildingUpWallsStopsAfterOneRemained_c.setEnabled(componentsEnabled);
		panel.add(buildingUpWallsStopsAfterOneRemained_c);
		box.add(panel);
		panel = new JPanel();
		itemsStopRollingBombs_c.setToolTipText("If unchecked, items will disappear.");
		itemsStopRollingBombs_c.setEnabled(componentsEnabled);
		panel.add(itemsStopRollingBombs_c);
		box.add(panel);
		panel = new JPanel();
		punchedBombsComeBackAtTheOppositeEnd_c.setToolTipText("Punched and thrown away bombs...");
		punchedBombsComeBackAtTheOppositeEnd_c.setEnabled(componentsEnabled);
		panel.add(punchedBombsComeBackAtTheOppositeEnd_c);
		box.add(panel);
		panel = new JPanel();
		multipleFire_c.setEnabled(componentsEnabled);
		panel.add(multipleFire_c);
		box.add(panel);
		panel = new JPanel();
		swapOnlyLivingPlayer_c.setEnabled(componentsEnabled);
		panel.add(swapOnlyLivingPlayer_c);
		box.add(panel);
		panel = new JPanel();
		autoRestartGame_c.setEnabled(componentsEnabled);
		panel.add(autoRestartGame_c);
		box.add(panel);
		panel = new JPanel();
		final Box verticalBox = Box.createVerticalBox();
		for (final Enumeration<AbstractButton> abstractButtons = killsBelongTo_c.getElements(); abstractButtons.hasMoreElements();) {
			final AbstractButton killsBelongTo_button = abstractButtons.nextElement();
			killsBelongTo_button.setEnabled(componentsEnabled);
			verticalBox.add(killsBelongTo_button);
		}
		verticalBox.setBorder(BorderFactory.createTitledBorder("Kills belong to"));
		panel.add(verticalBox);
		box.add(panel);
		panel = new JPanel();
		panel.add(box);
		optionsTabbedPane.addTab("Game rules", panel);

		JTabbedPane levelOptionsTabbedPane = levelComponentOptions.getOptionsTabbedPane();
		box = Box.createVerticalBox();
		panel = new JPanel();
		panel.add(createLabel("Amount of brick walls:", componentsEnabled));
		amountOfBrickWalls_c.setEnabled(componentsEnabled);
		panel.add(amountOfBrickWalls_c);
		panel.add(createLabel("%.", componentsEnabled));
		box.add(panel);
		panel = new JPanel();
		panel.add(createLabel("Probability of getting item:", componentsEnabled));
		gettingItemProbability_c.setEnabled(componentsEnabled);
		panel.add(gettingItemProbability_c);
		panel.add(createLabel("%.", componentsEnabled));
		box.add(panel);
		panel = new JPanel();
		panel.add(createLabel("Max gateway number:", componentsEnabled));
		maxGatewayNumber_c.setEnabled(componentsEnabled);
		panel.add(maxGatewayNumber_c);
		box.add(panel);
		panel = new JPanel();
		panel.add(box);
		levelOptionsTabbedPane.addTab("Wall generating", panel);
		optionsTabbedPane.addTab("Random level", levelOptionsTabbedPane);

		box = Box.createVerticalBox();
		panel = new JPanel();
		panel.add(createLabel("Game cycle frequency:", componentsEnabled));
		gameCycleFrequency_c.setToolTipText("This determines the game speed.");
		gameCycleFrequency_c.setEnabled(componentsEnabled);
		panel.add(gameCycleFrequency_c);
		panel.add(createLabel("1/s.", componentsEnabled));
		box.add(panel);
		panel = new JPanel();
		panel.add(createLabel("Game port:", componentsEnabled));
		gamePort_c.setEnabled(componentsEnabled);
		panel.add(gamePort_c);
		box.add(panel);
		panel = new JPanel();
		panel.add(createLabel("Network latency:", componentsEnabled));
		networkLatency_c.setEnabled(componentsEnabled);
		networkLatency_c.setToolTipText("Determines the delay of network data sending.");
		panel.add(networkLatency_c);
		box.add(panel);
		panel = new JPanel();
		panel.add(box);
		optionsTabbedPane.addTab("Extra", panel);
	}

	/**
	 * Restores the default values of the server options to the option
	 * components.
	 */
	public void restoreDefaultValuesToComponents() {
		levelComponentOptions.restoreDefaultValuesToComponents();

		levelName_c.setSelectedItem(DEFAULT_LEVEL_NAME);
		gameType_c.setSelectedItem(DEFAULT_GAME_TYPE);
		roundTimeLimit_c.setValue(DEFAULT_ROUND_TIME_LIMIT);
		gamePointLimit_c.setValue(DEFAULT_GAME_POINT_LIMIT);
		password_c.setText(DEFAULT_PASSWORD);

		damageOfWholeBombFire_c.setValue(DEFAULT_DAMAGE_OF_WHOLE_BOMB_FIRE);
		explosionAnnihilatesDiseases_c.setSelected(DEFAULT_EXPLOSION_ANNIHILATES_DISEASES);
		fireDoesntHurtTeammates_c.setSelected(DEFAULT_FIRE_DOESNT_HURT_TEAMMATES);
		newRandomPositionsAfterRounds_c.setSelected(DEFAULT_NEW_RANDOM_POSITIONS_AFTER_ROUNDS);
		bombsExplodeAfterOneRemained_c.setSelected(DEFAULT_BOMBS_EXPLODE_AFTER_ONE_REMAINED);
		buildingUpWallsStopsAfterOneRemained_c.setSelected(DEFAULT_BUILDING_UP_WALLS_STOPS_AFTER_ONE_REMAINED);
		itemsStopRollingBombs_c.setSelected(DEFAULT_ITEMS_STOP_ROLLING_BOMBS);
		punchedBombsComeBackAtTheOppositeEnd_c.setSelected(DEFAULT_PUNCHED_BOMBS_COME_BACK_AT_THE_OPPOSITE_END);
		multipleFire_c.setSelected(DEFAULT_MULTIPLE_FIRE);
		swapOnlyLivingPlayer_c.setSelected(DEFAULT_SWAP_ONLY_LIVING_PLAYER);
		autoRestartGame_c.setSelected(DEFAULT_AUTO_RESTART_GAME);
		for (final Enumeration<AbstractButton> abstractButtons = killsBelongTo_c.getElements(); abstractButtons.hasMoreElements();) {
			final AbstractButton abstractButton = abstractButtons.nextElement();
			if (abstractButton.getActionCommand().equals(DEFAULT_KILLS_BELONG_TO.name())) {
				abstractButton.setSelected(true);
				break;
			}
		}

		amountOfBrickWalls_c.setValue(DEFAULT_AMOUNT_OF_BRICK_WALLS);
		gettingItemProbability_c.setValue(DEFAULT_GETTING_ITEM_PROBABILITY);
		maxGatewayNumber_c.setValue(DEFAULT_MAX_GATEWAY_NUMBER);

		gameCycleFrequency_c.setValue(DEFAULT_GAME_CYCLE_FREQUENCY);
		gamePort_c.setValue(OptConsts.DEFAULT_GAME_PORT);
		networkLatency_c.setSelectedItem(DEFAULT_NETWORK_LATENCY);
	}

	/**
	 * Creates a new server options object, stores into that the actual states
	 * of the option components, and returns it.
	 * 
	 * @return the ServerOptions object holding the values/states of the option
	 *         components
	 */
	public ServerOptions getOptionsFromComponents() {
		final ServerOptions serverOptions = new ServerOptions();

		serverOptions.setLevelOptions(levelComponentOptions.getOptionsFromComponents());

		serverOptions.setLevelName((String) levelName_c.getSelectedItem());
		serverOptions.setGameType((GameTypes) gameType_c.getSelectedItem());
		serverOptions.setRoundTimeLimit((Integer) roundTimeLimit_c.getValue());
		serverOptions.setGamePointLimit((Integer) gamePointLimit_c.getValue());
		serverOptions.setPassword(password_c.getText());

		serverOptions.setDamageOfWholeBombFire((Integer) damageOfWholeBombFire_c.getValue());
		serverOptions.setExplosionAnnihilatesDiseases(explosionAnnihilatesDiseases_c.isSelected());
		serverOptions.setFireDoesntHurtTeammates(fireDoesntHurtTeammates_c.isSelected());
		serverOptions.setNewRandomPositionsAfterRounds(newRandomPositionsAfterRounds_c.isSelected());
		serverOptions.setBombsExplodeAfterOneRemained(bombsExplodeAfterOneRemained_c.isSelected());
		serverOptions.setBuildingUpWallsStopsAfterOneRemained(buildingUpWallsStopsAfterOneRemained_c.isSelected());
		serverOptions.setItemsStopRollingBombs(itemsStopRollingBombs_c.isSelected());
		serverOptions.setPunchedBombsComeBackAtTheOppositeEnd(punchedBombsComeBackAtTheOppositeEnd_c.isSelected());
		serverOptions.setMultipleFire(multipleFire_c.isSelected());
		serverOptions.setSwapOnlyLivingPlayer(swapOnlyLivingPlayer_c.isSelected());
		serverOptions.setAutoRestartGame(autoRestartGame_c.isSelected());
		serverOptions.setKillsBelongTo(KillsBelongTos.valueOf(killsBelongTo_c.getSelection().getActionCommand()));

		serverOptions.setAmountOfBrickWalls((Integer) amountOfBrickWalls_c.getValue());
		serverOptions.setGettingItemProbability((Integer) gettingItemProbability_c.getValue());
		serverOptions.setMaxGatewayNumber((Integer) maxGatewayNumber_c.getValue());

		serverOptions.setGameCycleFrequency((Integer) gameCycleFrequency_c.getValue());
		serverOptions.setGamePort((Integer) gamePort_c.getValue());
		serverOptions.setNetworkLatency((NetworkLatencies) networkLatency_c.getSelectedItem());

		return serverOptions;
	}

	/**
	 * Stores the values of the server option attributes to the appropriate
	 * components.
	 */
	public void synchronizeComponentsToOptions() {
		levelComponentOptions.synchronizeComponentsToOptions();

		levelName_c.setSelectedItem(options.getLevelName());
		gameType_c.setSelectedItem(options.getGameType());
		roundTimeLimit_c.setValue(options.getRoundTimeLimit());
		gamePointLimit_c.setValue(options.getGamePointLimit());
		password_c.setText(options.getPassword());

		damageOfWholeBombFire_c.setValue(options.getDamageOfWholeBombFire());
		explosionAnnihilatesDiseases_c.setSelected(options.isExplosionAnnihilatesDiseases());
		fireDoesntHurtTeammates_c.setSelected(options.isFireDoesntHurtTeammates());
		newRandomPositionsAfterRounds_c.setSelected(options.isNewRandomPositionsAfterRounds());
		bombsExplodeAfterOneRemained_c.setSelected(options.isBombsExplodeAfterOneRemained());
		buildingUpWallsStopsAfterOneRemained_c.setSelected(options.isBuildingUpWallsStopsAfterOneRemained());
		itemsStopRollingBombs_c.setSelected(options.isItemsStopRollingBombs());
		punchedBombsComeBackAtTheOppositeEnd_c.setSelected(options.isPunchedBombsComeBackAtTheOppositeEnd());
		multipleFire_c.setSelected(options.isMultipleFire());
		swapOnlyLivingPlayer_c.setSelected(options.isSwapOnlyLivingPlayer());
		autoRestartGame_c.setSelected(options.isAutoRestartGame());
		for (final Enumeration<AbstractButton> abstractButtons = killsBelongTo_c.getElements(); abstractButtons.hasMoreElements();) {
			final AbstractButton abstractButton = abstractButtons.nextElement();
			if (abstractButton.getActionCommand().equals(options.getKillsBelongTo().name())) {
				abstractButton.setSelected(true);
				break;
			}
		}

		amountOfBrickWalls_c.setValue(options.getAmountOfBrickWalls());
		gettingItemProbability_c.setValue(options.getGettingItemProbability());
		maxGatewayNumber_c.setValue(options.getMaxGatewayNumber());

		gameCycleFrequency_c.setValue(options.getGameCycleFrequency());
		gamePort_c.setValue(options.getGamePort());
		networkLatency_c.setSelectedItem(options.getNetworkLatency());
	}

	/**
	 * Sets the options to be handled. Overrides the method implemented at
	 * ComponentOptions, because we have to set level options too to our level
	 * component options.
	 * 
	 * @param options
	 *            options to be set to be handled
	 */
	public void setOptions(final ServerOptions options) {
		super.setOptions(options);
		levelComponentOptions.setOptions(options.getLevelOptions());
	}

}
