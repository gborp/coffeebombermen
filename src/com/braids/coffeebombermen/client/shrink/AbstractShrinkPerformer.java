package com.braids.coffeebombermen.client.shrink;

import com.braids.coffeebombermen.client.gamecore.BombPhases;
import com.braids.coffeebombermen.client.gamecore.BombTypes;
import com.braids.coffeebombermen.client.gamecore.CoreConsts;
import com.braids.coffeebombermen.client.gamecore.Directions;
import com.braids.coffeebombermen.client.gamecore.FireShapes;
import com.braids.coffeebombermen.client.gamecore.control.Bomb;
import com.braids.coffeebombermen.client.gamecore.control.Fire;
import com.braids.coffeebombermen.client.gamecore.control.GameCoreHandler;
import com.braids.coffeebombermen.client.gamecore.control.Level;
import com.braids.coffeebombermen.client.gamecore.model.BombModel;
import com.braids.coffeebombermen.client.gamecore.model.FireModel;
import com.braids.coffeebombermen.client.gamecore.model.level.LevelComponent;
import com.braids.coffeebombermen.client.gamecore.model.level.LevelModel;
import com.braids.coffeebombermen.options.OptConsts.Items;
import com.braids.coffeebombermen.options.OptConsts.Walls;
import com.braids.coffeebombermen.options.model.ServerOptions;
import com.braids.coffeebombermen.utils.MathHelper;

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
		addWall(x, y, Walls.DEATH);
	}

	protected void addWall(int x, int y, Walls wall) {
		LevelModel levelModel = getLevel().getModel();
		int width = levelModel.getWidth();
		int height = levelModel.getHeight();

		if ((x >= 0) && (x < width) && (y >= 0) && (y < height)) {
			LevelComponent comp = levelModel.getComponent(x, y);
			comp.setItem(null);
			gameCoreHandler.setWall(x, y, wall);
		}
	}

	protected void addBomb(int x, int y, int range, BombPhases phase, Directions direction) {
		Bomb newBomb = new Bomb(new BombModel(null), getGameCoreHandler());
		final BombModel newBombModel = newBomb.getModel();
		newBombModel.setType(BombTypes.JELLY);
		newBombModel.setPosX(x * CoreConsts.LEVEL_COMPONENT_GRANULARITY + CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2);
		newBombModel.setPosY(y * CoreConsts.LEVEL_COMPONENT_GRANULARITY + CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2);
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
		newBombModel.setPosX(x * CoreConsts.LEVEL_COMPONENT_GRANULARITY + CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2);
		newBombModel.setPosY(y * CoreConsts.LEVEL_COMPONENT_GRANULARITY + CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2);
		newBombModel.setRange(range);
		newBombModel.setPhase(BombPhases.ROLLING);
		gameCoreHandler.addNewBomb(newBomb);

		if ((x > 0) && (y > 0) && (x < levelModel.getWidth() - 2) && (y > levelModel.getHeight() - 2)) {

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
			        * CoreConsts.LEVEL_COMPONENT_GRANULARITY);
			gameCoreHandler.validateAndSetFlyingTargetPosY(newBombModel, newBombModel.getPosY() + newBombModel.getDirectionYMultiplier()
			        * CoreConsts.LEVEL_COMPONENT_GRANULARITY);
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
		newBombModel.setPosX(x * CoreConsts.LEVEL_COMPONENT_GRANULARITY + CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2);
		newBombModel.setPosY(y * CoreConsts.LEVEL_COMPONENT_GRANULARITY + CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2);
		newBombModel.setRange(range);
		newBombModel.setPhase(BombPhases.ROLLING);
		getGameCoreHandler().addNewBomb(newBomb);

		// search for a direction open for roll
		int direction = MathHelper.nextInt(4);
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

	protected void removeFire(int x, int y) {
		GameCoreHandler gch = getGameCoreHandler();
		gch.getLevel().removeAllFireFromComponentPos(x, y);
	}

	protected void addFire(int x, int y, FireShapes shape) {
		GameCoreHandler gch = getGameCoreHandler();
		final Fire fire = new Fire(x, y, gch);
		final FireModel fireModel = fire.getModel();
		fireModel.setIterationCounter(Integer.MIN_VALUE);
		fireModel.setShape(shape);
		gch.getLevel().addFireToComponentPos(fire, x, y);
	}

}
