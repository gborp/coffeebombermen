package classes.client.shrink;

import java.util.Random;

import classes.client.gamecore.BombPhases;
import classes.client.gamecore.BombTypes;
import classes.client.gamecore.Consts;
import classes.client.gamecore.Directions;
import classes.client.gamecore.control.Bomb;
import classes.client.gamecore.control.GameCoreHandler;
import classes.client.gamecore.control.Level;
import classes.client.gamecore.model.BombModel;
import classes.client.gamecore.model.level.LevelComponent;
import classes.client.gamecore.model.level.LevelModel;
import classes.client.sound.SoundEffect;
import classes.options.Consts.Items;
import classes.options.Consts.Walls;
import classes.options.model.ServerOptions;
import classes.utils.MathHelper;

public abstract class AbstractShrinkPerformer implements ShrinkPerformer {

	private GameCoreHandler gameCoreHandler;
	private long            lastShrinkOperationAt;

	public AbstractShrinkPerformer(GameCoreHandler gameCoreHandler) {
		this.gameCoreHandler = gameCoreHandler;
	}

	public void initNextRound() {
		lastShrinkOperationAt = 0;
		initNextRoundImpl();
	}

	public void nextIteration() {
		nextIterationImpl();
	}

	protected void setLastShrinkOperationAt() {
		lastShrinkOperationAt = getTick();
	}

	public long getLastShrinkOperationAt() {
		return lastShrinkOperationAt;
	}

	protected GameCoreHandler getGameCoreHandler() {
		return gameCoreHandler;
	}

	protected ServerOptions getGlobalServerOptions() {
		return gameCoreHandler.getGlobalServerOptions();
	}

	protected Level getLevel() {
		return gameCoreHandler.getLevel();
	}

	protected int getWidth() {
		return getLevel().getModel().getWidth();
	}

	protected int getHeight() {
		return getLevel().getModel().getHeight();
	}

	protected Random getRandom() {
		return getGameCoreHandler().getRandom();
	}

	protected long getTick() {
		return gameCoreHandler.getTick();
	}

	protected boolean isTimeToFirstShrink() {
		return getLastShrinkOperationAt() == 0;
	}

	protected boolean isTimeToNextShrink(int frequency) {
		return ((getTick() - getLastShrinkOperationAt()) > frequency);
	}

	protected boolean isTimeToShrink() {
		ServerOptions gso = getGlobalServerOptions();
		return getTick() > gso.getRoundTimeLimit() * gso.getGameCycleFrequency();
	}

	protected abstract void nextIterationImpl();

	protected abstract void initNextRoundImpl();

	protected void addDeathWall(int x, int y) {
		LevelModel levelModel = getLevel().getModel();
		int width = levelModel.getWidth();
		int height = levelModel.getHeight();

		if (x >= 0 && x < width && y >= 0 && y < height) {
			LevelComponent comp = levelModel.getComponent(x, y);
			comp.setItem(null);
			comp.setWall(Walls.DEATH);
			SoundEffect.DEATH_WALL.play();
		}
	}

	protected void addBomb(int x, int y, int range, BombPhases phase, Directions direction) {
		Bomb newBomb = new Bomb(new BombModel(null), getGameCoreHandler());
		final BombModel newBombModel = newBomb.getModel();
		newBombModel.setType(BombTypes.JELLY);
		newBombModel.setPosX(x * Consts.LEVEL_COMPONENT_GRANULARITY + Consts.LEVEL_COMPONENT_GRANULARITY / 2);
		newBombModel.setPosY(y * Consts.LEVEL_COMPONENT_GRANULARITY + Consts.LEVEL_COMPONENT_GRANULARITY / 2);
		newBombModel.setRange(range);
		newBombModel.setPhase(phase);
		newBombModel.setDirection(direction);
		getGameCoreHandler().addNewBomb(newBomb);
	}

	protected void addCrazyBomb(int x, int y, int range) {
		addCrazyBomb(x, y, range, MathHelper.getRandomDirection());
	}

	protected void addCrazyBomb(int x, int y, int range, Directions direction) {
		LevelModel levelModel = gameCoreHandler.getLevel().getModel();

		Bomb newBomb = new Bomb(new BombModel(null), getGameCoreHandler());
		final BombModel newBombModel = newBomb.getModel();
		newBombModel.setType(BombTypes.JELLY);
		newBombModel.setCrazyPercent(0.5f);
		newBombModel.setPosX(x * Consts.LEVEL_COMPONENT_GRANULARITY + Consts.LEVEL_COMPONENT_GRANULARITY / 2);
		newBombModel.setPosY(y * Consts.LEVEL_COMPONENT_GRANULARITY + Consts.LEVEL_COMPONENT_GRANULARITY / 2);
		newBombModel.setRange(range);
		newBombModel.setPhase(BombPhases.ROLLING);
		gameCoreHandler.addNewBomb(newBomb);

		if (x > 0 && y > 0 && x < levelModel.getWidth() - 2 && y > levelModel.getHeight() - 2) {

			// search for a direction open for roll
			int directionDif = 0;
			while (directionDif < 4) {
				Directions d = Directions.get((direction.ordinal() + directionDif) % 4);
				if (gameCoreHandler.canBombRollToComponentPosition(newBombModel, newBombModel.getComponentPosX() + d.getXMultiplier(), newBombModel
				        .getComponentPosY()
				        + d.getYMultiplier())) {
					newBombModel.setDirection(d);
					return;
				}
				directionDif++;
			}
			newBombModel.setDirection(direction);
		} else {
			newBombModel.setDirection(direction);
			newBombModel.setPhase(BombPhases.FLYING);
			gameCoreHandler.validateAndSetFlyingTargetPosX(newBombModel, newBombModel.getPosX() + newBombModel.getDirectionXMultiplier()
			        * Consts.LEVEL_COMPONENT_GRANULARITY);
			gameCoreHandler.validateAndSetFlyingTargetPosY(newBombModel, newBombModel.getPosY() + newBombModel.getDirectionYMultiplier()
			        * Consts.LEVEL_COMPONENT_GRANULARITY);
		}

	}

	protected void setItem(int x, int y, Items item) {
		getGameCoreHandler().getLevel().getModel().getComponent(x, y).setItem(item);
	}

	protected void setItem(Items item) {
		getGameCoreHandler().replaceItemOnLevel(item);
	}

	protected void addDetonatingOnHitBomb(int x, int y, int range) {
		Bomb newBomb = new Bomb(new BombModel(null), getGameCoreHandler());
		final BombModel newBombModel = newBomb.getModel();
		newBombModel.setType(BombTypes.JELLY);
		newBombModel.setDetonatingOnHit(true);
		newBombModel.setPosX(x * Consts.LEVEL_COMPONENT_GRANULARITY + Consts.LEVEL_COMPONENT_GRANULARITY / 2);
		newBombModel.setPosY(y * Consts.LEVEL_COMPONENT_GRANULARITY + Consts.LEVEL_COMPONENT_GRANULARITY / 2);
		newBombModel.setRange(range);
		newBombModel.setPhase(BombPhases.ROLLING);
		getGameCoreHandler().addNewBomb(newBomb);

		// search for a direction open for roll
		int direction = getRandom().nextInt(4);
		GameCoreHandler mc = getGameCoreHandler();
		int directionDif = 0;
		while (directionDif < 4) {
			Directions d = Directions.get((direction + directionDif) % 4);
			if (mc.canBombRollToComponentPosition(newBombModel, newBombModel.getComponentPosX() + d.getXMultiplier(), newBombModel.getComponentPosY()
			        + d.getYMultiplier())) {
				newBombModel.setDirection(d);
				return;
			}
			directionDif++;
		}
		newBombModel.setDirection(Directions.get(direction));
	}

}
