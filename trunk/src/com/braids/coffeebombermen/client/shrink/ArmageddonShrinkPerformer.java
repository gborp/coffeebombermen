package com.braids.coffeebombermen.client.shrink;

import java.util.ArrayList;

import com.braids.coffeebombermen.client.gamecore.FireShapes;
import com.braids.coffeebombermen.client.gamecore.control.Fire;
import com.braids.coffeebombermen.client.gamecore.control.GameCoreHandler;
import com.braids.coffeebombermen.client.gamecore.model.FireModel;
import com.braids.coffeebombermen.client.gamecore.model.level.LevelComponent;
import com.braids.coffeebombermen.options.Shrinkers;
import com.braids.coffeebombermen.options.OptConsts.Walls;
import com.braids.coffeebombermen.utils.MathHelper;
import com.braids.coffeebombermen.utils.Position;

public class ArmageddonShrinkPerformer extends AbstractShrinkPerformer {

	private ArrayList<Position> lstOpen;

	public ArmageddonShrinkPerformer(GameCoreHandler gameCoreHandler) {
		super(gameCoreHandler);
	}

	public Shrinkers getType() {
		return Shrinkers.Armageddon;
	}

	protected void initNextRoundImpl() {
		lstOpen = new ArrayList<Position>();
	}

	protected void nextIterationImpl() {
		if (isTimeToShrink()) {
			if (isTimeToFirstShrink()) {
				int x = -1;
				int y = -1;
				GameCoreHandler gch = getGameCoreHandler();
				while ((x < 0) || (y < 0) || !Walls.EMPTY.equals(gch.getWall(x, y))) {
					x = MathHelper.randomInt(2, getWidth() - 2);
					y = MathHelper.randomInt(2, getHeight() - 2);
				}
				addFire(x, y, FireShapes.CROSSING);
				checkNeibours(x, y);
				setLastShrinkOperationAt();
			} else if (isTimeToNextShrink(getGlobalServerOptions().getGameCycleFrequency())) {
				for (Position p : new ArrayList<Position>(lstOpen)) {
					if (MathHelper.randomBoolean()) {
						lstOpen.remove(p);
						addFire(p.getX(), p.getY(), FireShapes.CROSSING);
						checkNeibours(p.getX(), p.getY());
					}
				}
				setLastShrinkOperationAt();
			}
		}
	}

	private void checkNeibours(int x, int y) {
		checkNeibourAt(x - 1, y);
		checkNeibourAt(x + 1, y);
		checkNeibourAt(x, y - 1);
		checkNeibourAt(x, y + 1);
	}

	private void checkNeibourAt(int x, int y) {
		if (canGoFireTo(x, y)) {
			lstOpen.add(new Position(x, y));
		}
	}

	private boolean canGoFireTo(int x, int y) {
		if ((x < 1) || (x > getWidth() - 2) || (y < 1) || (y > getHeight() - 2)) {
			return false;
		}

		if (isFireIn(x, y)) {
			return false;
		}

		Walls wall = getGameCoreHandler().getWall(x, y);
		if (!(Walls.EMPTY.equals(wall) || Walls.BRICK.equals(wall))) {
			return false;
		}

		return true;
	}

	private void addFire(int x, int y, FireShapes shape) {
		GameCoreHandler gch = getGameCoreHandler();
		final Fire fire = new Fire(x, y, gch);
		final FireModel fireModel = fire.getModel();
		fireModel.setIterationCounter(Integer.MIN_VALUE);
		fireModel.setShape(shape);
		gch.getLevel().addFireToComponentPos(fire, x, y);
	}

	private boolean isFireIn(int x, int y) {
		LevelComponent c = getGameCoreHandler().getLevelModel().getComponent(x, y);
		for (int i = 0; i < c.getFireCount(); i++) {
			// attila hack: how should i determine armageddon styled fire?
			if ((c.getFire(i).getIterationCounter() < 100) && (c.getFire(i).getIterationCounter() >= 0)) {
				return true;
			}
		}

		return false;
	}
}
