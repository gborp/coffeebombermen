package classes.options.model;

import static classes.utils.GeneralStringTokenizer.GENERAL_SEPARATOR_CHAR;
import classes.options.Consts.GameTypes;
import classes.options.Consts.KillsBelongTos;
import classes.options.Consts.NetworkLatencies;
import classes.utils.GeneralStringTokenizer;

/**
 * Holds all the server options; contains a reference to a level options object.
 */
public class ServerOptions extends Options<ServerOptions> {

	/** Level options for randomly generated levels. */
	private LevelOptions     levelOptions  = new LevelOptions();

	/**
	 * Which level to play on. This is the name of the level file without
	 * extension. Empty string indicates random level
	 */
	private String           levelName     = new String();
	/** Type of the game. */
	private GameTypes        gameType      = GameTypes.values()[0];
	/** Round time limit in seconds. */
	private int              roundTimeLimit;
	/** Game point limit. Reaching this means end of game. */
	private int              gamePointLimit;
	/** Password for password protected games. */
	private String           password      = "";

	/**
	 * Damage of the whole fire of a bomb in percent. 100 % means to kill
	 * exactly a healthy bomberman.
	 */
	private int              damageOfWholeBombFire;
	/**
	 * Tells whether explosion ahhihilates diseases when reaches them. (If not,
	 * they'll be replaced on the level.)
	 */
	private boolean          explosionAnnihilatesDiseases;
	/** Tells whether fire doesn't hurt teammates. */
	private boolean          fireDoesntHurtTeammates;
	/**
	 * Tells whether we have to determine new random positions after each round.
	 */
	private boolean          newRandomPositionsAfterRounds;
	/** Tells whether bombs should explode after one remained (team or player). */
	private boolean          bombsExplodeAfterOneRemained;
	/**
	 * Tells whether building up walls should stop after one remained (team or
	 * player).
	 */
	private boolean          buildingUpWallsStopsAfterOneRemained;
	/**
	 * Tells whether items stop rolling bombs (if false, items disappear when
	 * bombs roll over them).
	 */
	private boolean          itemsStopRollingBombs;
	/**
	 * Tells whether punched or thrown away bombs come back at the opposite end
	 * of the level.
	 */
	private boolean          punchedBombsComeBackAtTheOppositeEnd;
	/**
	 * Tells whether fire damaging to players is multiple in case of multiple
	 * fire.
	 */
	private boolean          multipleFire;
	/** Tells who gets the killing points when bombermen die. */
	private KillsBelongTos   killsBelongTo = KillsBelongTos.values()[0];

	/** Amount of brick walls in percent of non-concrete walls. */
	private int              amountOfBrickWalls;
	/** Probability of getting itme when a wall brick has been exploded. */
	private int              gettingItemProbability;

	/** Game cycle frequency in 1/s (Hz). */
	private int              gameCycleFrequency;
	/** Game server port. */
	private int              gamePort;
	/** The network latency. */
	private NetworkLatencies networkLatency;

	/**
	 * Packs this object to a String so it can be transferred or stored. Enums
	 * are packed by their ordinals.
	 * 
	 * @return a compact string representing this server options
	 */
	public String packToString() {
		StringBuilder buffer = new StringBuilder();

		buffer.append(getLevelName()).append(GENERAL_SEPARATOR_CHAR);
		buffer.append(getGameType().ordinal()).append(GENERAL_SEPARATOR_CHAR);
		buffer.append(getRoundTimeLimit()).append(GENERAL_SEPARATOR_CHAR);
		buffer.append(getGamePointLimit()).append(GENERAL_SEPARATOR_CHAR);
		buffer.append(getPassword()).append(GENERAL_SEPARATOR_CHAR);

		buffer.append(getDamageOfWholeBombFire()).append(GENERAL_SEPARATOR_CHAR);
		buffer.append(isExplosionAnnihilatesDiseases()).append(GENERAL_SEPARATOR_CHAR);
		buffer.append(isFireDoesntHurtTeammates()).append(GENERAL_SEPARATOR_CHAR);
		buffer.append(isNewRandomPositionsAfterRounds()).append(GENERAL_SEPARATOR_CHAR);
		buffer.append(isBombsExplodeAfterOneRemained()).append(GENERAL_SEPARATOR_CHAR);
		buffer.append(isBuildingUpWallsStopsAfterOneRemained()).append(GENERAL_SEPARATOR_CHAR);
		buffer.append(isItemsStopRollingBombs()).append(GENERAL_SEPARATOR_CHAR);
		buffer.append(isPunchedBombsComeBackAtTheOppositeEnd()).append(GENERAL_SEPARATOR_CHAR);
		buffer.append(isMultipleFire()).append(GENERAL_SEPARATOR_CHAR);
		buffer.append(getKillsBelongTo().ordinal()).append(GENERAL_SEPARATOR_CHAR);

		buffer.append(getAmountOfBrickWalls()).append(GENERAL_SEPARATOR_CHAR);
		buffer.append(getGettingItemProbability()).append(GENERAL_SEPARATOR_CHAR);

		buffer.append(getGameCycleFrequency()).append(GENERAL_SEPARATOR_CHAR);
		buffer.append(getGamePort()).append(GENERAL_SEPARATOR_CHAR);
		buffer.append(getNetworkLatency().ordinal()).append(GENERAL_SEPARATOR_CHAR);

		buffer.append(getLevelOptions().packToString()); // This ends with
		// GENERAL_SEPARATOR_CHAR

		return buffer.toString();
	}

	/**
	 * Parses a server options object from a string.
	 * 
	 * @param source
	 *            the String representing the parsable server options
	 * @return a new ServerOptions created from the source string
	 */
	public static ServerOptions parseFromString(final String source) {
		final ServerOptions serverOptions = new ServerOptions();
		final GeneralStringTokenizer optionsTokenizer = new GeneralStringTokenizer(source);

		serverOptions.setLevelName(optionsTokenizer.nextStringToken());
		serverOptions.setGameType(GameTypes.values()[optionsTokenizer.nextIntToken()]);
		serverOptions.setRoundTimeLimit(optionsTokenizer.nextIntToken());
		serverOptions.setGamePointLimit(optionsTokenizer.nextIntToken());
		serverOptions.setPassword(optionsTokenizer.nextStringToken());

		serverOptions.setDamageOfWholeBombFire(optionsTokenizer.nextIntToken());
		serverOptions.setExplosionAnnihilatesDiseases(optionsTokenizer.nextBooleanToken());
		serverOptions.setFireDoesntHurtTeammates(optionsTokenizer.nextBooleanToken());
		serverOptions.setNewRandomPositionsAfterRounds(optionsTokenizer.nextBooleanToken());
		serverOptions.setBombsExplodeAfterOneRemained(optionsTokenizer.nextBooleanToken());
		serverOptions.setBuildingUpWallsStopsAfterOneRemained(optionsTokenizer.nextBooleanToken());
		serverOptions.setItemsStopRollingBombs(optionsTokenizer.nextBooleanToken());
		serverOptions.setPunchedBombsComeBackAtTheOppositeEnd(optionsTokenizer.nextBooleanToken());
		serverOptions.setMultipleFire(optionsTokenizer.nextBooleanToken());
		serverOptions.setKillsBelongTo(KillsBelongTos.values()[optionsTokenizer.nextIntToken()]);

		serverOptions.setAmountOfBrickWalls(optionsTokenizer.nextIntToken());
		serverOptions.setGettingItemProbability(optionsTokenizer.nextIntToken());

		serverOptions.setGameCycleFrequency(optionsTokenizer.nextIntToken());
		serverOptions.setGamePort(optionsTokenizer.nextIntToken());
		serverOptions.setNetworkLatency(NetworkLatencies.values()[optionsTokenizer.nextIntToken()]);

		serverOptions.setLevelOptions(LevelOptions.parseFromString(optionsTokenizer.remainingString()));

		return serverOptions;
	}

	/**
	 * Parses a server options object from a string. Simply returns the object
	 * created by parseFromString().
	 * 
	 * @param source
	 *            the String representing the parsable server options
	 * @return a new ServerOptions created from the source string
	 */
	public ServerOptions dynamicParseFromString(final String source) {
		return parseFromString(source);
	}

	public void setGamePort(int gamePort) {
		this.gamePort = gamePort;
	}

	public int getGamePort() {
		return gamePort;
	}

	public void setGameCycleFrequency(int gameCycleFrequency) {
		this.gameCycleFrequency = gameCycleFrequency;
	}

	public int getGameCycleFrequency() {
		return gameCycleFrequency;
	}

	public void setGettingItemProbability(int gettingItemProbability) {
		this.gettingItemProbability = gettingItemProbability;
	}

	public int getGettingItemProbability() {
		return gettingItemProbability;
	}

	public void setLevelOptions(LevelOptions levelOptions) {
		this.levelOptions = levelOptions;
	}

	public LevelOptions getLevelOptions() {
		return levelOptions;
	}

	public void setLevelName(String levelName) {
		this.levelName = levelName;
	}

	public String getLevelName() {
		return levelName;
	}

	public void setGameType(GameTypes gameType) {
		this.gameType = gameType;
	}

	public GameTypes getGameType() {
		return gameType;
	}

	public void setRoundTimeLimit(int roundTimeLimit) {
		this.roundTimeLimit = roundTimeLimit;
	}

	public int getRoundTimeLimit() {
		return roundTimeLimit;
	}

	public void setGamePointLimit(int gamePointLimit) {
		this.gamePointLimit = gamePointLimit;
	}

	public int getGamePointLimit() {
		return gamePointLimit;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public void setDamageOfWholeBombFire(int damageOfWholeBombFire) {
		this.damageOfWholeBombFire = damageOfWholeBombFire;
	}

	public int getDamageOfWholeBombFire() {
		return damageOfWholeBombFire;
	}

	public void setExplosionAnnihilatesDiseases(boolean explosionAnnihilatesDiseases) {
		this.explosionAnnihilatesDiseases = explosionAnnihilatesDiseases;
	}

	public boolean isExplosionAnnihilatesDiseases() {
		return explosionAnnihilatesDiseases;
	}

	public void setFireDoesntHurtTeammates(boolean fireDoesntHurtTeammates) {
		this.fireDoesntHurtTeammates = fireDoesntHurtTeammates;
	}

	public boolean isFireDoesntHurtTeammates() {
		return fireDoesntHurtTeammates;
	}

	public void setNewRandomPositionsAfterRounds(boolean newRandomPositionsAfterRounds) {
		this.newRandomPositionsAfterRounds = newRandomPositionsAfterRounds;
	}

	public boolean isNewRandomPositionsAfterRounds() {
		return newRandomPositionsAfterRounds;
	}

	public void setBombsExplodeAfterOneRemained(boolean bombsExplodeAfterOneRemained) {
		this.bombsExplodeAfterOneRemained = bombsExplodeAfterOneRemained;
	}

	public boolean isBombsExplodeAfterOneRemained() {
		return bombsExplodeAfterOneRemained;
	}

	public void setBuildingUpWallsStopsAfterOneRemained(boolean buildingUpWallsStopsAfterOneRemained) {
		this.buildingUpWallsStopsAfterOneRemained = buildingUpWallsStopsAfterOneRemained;
	}

	public boolean isBuildingUpWallsStopsAfterOneRemained() {
		return buildingUpWallsStopsAfterOneRemained;
	}

	public void setItemsStopRollingBombs(boolean itemsStopRollingBombs) {
		this.itemsStopRollingBombs = itemsStopRollingBombs;
	}

	public boolean isItemsStopRollingBombs() {
		return itemsStopRollingBombs;
	}

	public void setPunchedBombsComeBackAtTheOppositeEnd(boolean punchedBombsComeBackAtTheOppositeEnd) {
		this.punchedBombsComeBackAtTheOppositeEnd = punchedBombsComeBackAtTheOppositeEnd;
	}

	public boolean isPunchedBombsComeBackAtTheOppositeEnd() {
		return punchedBombsComeBackAtTheOppositeEnd;
	}

	public void setMultipleFire(boolean multipleFire) {
		this.multipleFire = multipleFire;
	}

	public boolean isMultipleFire() {
		return multipleFire;
	}

	public void setKillsBelongTo(KillsBelongTos killsBelongTo) {
		this.killsBelongTo = killsBelongTo;
	}

	public KillsBelongTos getKillsBelongTo() {
		return killsBelongTo;
	}

	public void setAmountOfBrickWalls(int amountOfBrickWalls) {
		this.amountOfBrickWalls = amountOfBrickWalls;
	}

	public int getAmountOfBrickWalls() {
		return amountOfBrickWalls;
	}

	public void setNetworkLatency(NetworkLatencies networkLatency) {
		this.networkLatency = networkLatency;
	}

	public NetworkLatencies getNetworkLatency() {
		return networkLatency;
	}

}
