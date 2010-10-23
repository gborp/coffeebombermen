package com.braids.coffeebombermen.client.shrink;

import java.util.ArrayList;
import java.util.List;

import com.braids.coffeebombermen.client.gamecore.Directions;
import com.braids.coffeebombermen.client.gamecore.control.GameCoreHandler;
import com.braids.coffeebombermen.client.gamecore.model.level.LevelModel;
import com.braids.coffeebombermen.options.Shrinkers;
import com.braids.coffeebombermen.options.OptConsts.Walls;
import com.braids.coffeebombermen.utils.MathHelper;

public class BombShrinkPerformer extends AbstractShrinkPerformer {

	private static final int  GAME_CYCLE_FREQUENCY_MULTIPLIER = 8;
	private static final int  PRE_SPEEDUP_TICKS               = 32;

	private float             numberOfBombs;
	private int               maxRange;
	int                       preSpeedupWarn;

	private List<NewBombSlot> lstBombsToAppear;

	public BombShrinkPerformer(GameCoreHandler gameCoreHandler) {
		super(gameCoreHandler);
	}

	public Shrinkers getType() {
		return Shrinkers.Bomb;
	}

	protected void initNextRoundImpl() {
		numberOfBombs = 4;
		maxRange = 2;
		lstBombsToAppear = new ArrayList<NewBombSlot>();
		preSpeedupWarn = 0;
	}

	protected void nextIterationImpl() {
		if (isTimeToShrink()) {

			if (preSpeedupWarn > 0) {
				boolean odd = (getTick() & 2) == 0;

				for (NewBombSlot slot : lstBombsToAppear) {
					if (odd) {
						getLevel().getModel().getComponent(slot.x, slot.y).setWall(Walls.DEATH_WARN);
					} else {
						getLevel().getModel().getComponent(slot.x, slot.y).setWall(Walls.DEATH);
					}
				}
				preSpeedupWarn--;

				if (preSpeedupWarn == 0) {
					for (NewBombSlot slot : lstBombsToAppear) {
						getLevel().getModel().getComponent(slot.x, slot.y).setWall(Walls.DEATH);
						addCrazyBomb(slot.x, slot.y, MathHelper.randomInt(2, maxRange) + 1, slot.direction);
					}

					if (numberOfBombs < getWidth() * getHeight()) {
						numberOfBombs = numberOfBombs * 1.5f;
					}

					maxRange++;
				}
			}

			if (isTimeToFirstShrink() || isTimeToNextShrink(getGlobalServerOptions().getGameCycleFrequency() * GAME_CYCLE_FREQUENCY_MULTIPLIER)) {

				if (isTimeToFirstShrink()) {
					LevelModel levelModel = getLevel().getModel();
					for (int x = 0; x < levelModel.getWidth(); x++) {
						levelModel.getComponent(x, 0).setWall(Walls.DEATH);
						levelModel.getComponent(x, levelModel.getHeight() - 1).setWall(Walls.DEATH);
					}
					for (int y = 0; y < levelModel.getHeight(); y++) {
						levelModel.getComponent(0, y).setWall(Walls.DEATH);
						levelModel.getComponent(levelModel.getWidth() - 1, y).setWall(Walls.DEATH);
					}
				}

				lstBombsToAppear.clear();
				for (int i = 0; i < numberOfBombs; i++) {

					NewBombSlot slot = new NewBombSlot();
					slot.direction = MathHelper.getRandomDirection();
					switch (slot.direction) {
						case UP:
							slot.x = getRandom().nextInt(getWidth() - 3) + 1;
							slot.y = getHeight() - 1;
							break;
						case DOWN:
							slot.x = getRandom().nextInt(getWidth() - 3) + 1;
							slot.y = 0;
							break;
						case LEFT:
							slot.x = getWidth() - 1;
							slot.y = getRandom().nextInt(getHeight() - 3) + 1;
							break;
						case RIGHT:
							slot.x = 0;
							slot.y = getRandom().nextInt(getHeight() - 3) + 1;
							break;
					}
					lstBombsToAppear.add(slot);
				}
				preSpeedupWarn = PRE_SPEEDUP_TICKS;
				setLastShrinkOperationAt();

			}
		}
	}

	private static class NewBombSlot {

		int        x;
		int        y;
		Directions direction;
	}
}
