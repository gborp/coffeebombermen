package com.braids.coffeebombermen.client.shrink;

import com.braids.coffeebombermen.client.gamecore.control.GameCoreHandler;
import com.braids.coffeebombermen.client.gamecore.model.level.LevelModel;
import com.braids.coffeebombermen.client.sound.SoundEffect;
import com.braids.coffeebombermen.options.OptConsts.Walls;
import com.braids.coffeebombermen.options.Shrinkers;
import com.braids.coffeebombermen.options.model.ServerOptions;

public class MassKillShrinkPerformer extends AbstractShrinkPerformer {

	private static final int GAME_CYCLE_FREQUENCY_MULTIPLIER = 4;

	public boolean           preWarning;

	public MassKillShrinkPerformer(GameCoreHandler gameCoreHandler) {
		super(gameCoreHandler);
	}

	public Shrinkers getType() {
		return Shrinkers.MassKill;
	}

	protected void initNextRoundImpl() {}

	protected void nextIterationImpl() {
		if (isTimeToShrink()) {
			if (isTimeToFirstShrink()) {
				preWarning = true;
			}

			if (isTimeToMassKill()) {
				for (int i = 0; i < getWidth(); i++) {
					for (int j = 0; j < getHeight(); j++) {
						addDeathWall(i, j);
					}
				}
				setLastShrinkOperationAt();
			} else if (isTimeToNextShrink(getGlobalServerOptions().getGameCycleFrequency() / GAME_CYCLE_FREQUENCY_MULTIPLIER)) {
				Walls wall;
				if (preWarning) {
					SoundEffect.DEATH_WALL.play();
					wall = Walls.DEATH_WARN;
				} else {
					wall = Walls.DEATH;
				}

				LevelModel levelModel = getLevel().getModel();
				int width = levelModel.getWidth();
				int height = levelModel.getHeight();

				levelModel.getComponent(0, 0).setWall(wall);
				levelModel.getComponent(width - 1, 0).setWall(wall);
				levelModel.getComponent(0, height - 1).setWall(wall);
				levelModel.getComponent(width - 1, height - 1).setWall(wall);
				preWarning = !preWarning;
				setLastShrinkOperationAt();
			}

		}

	}

	private boolean isTimeToMassKill() {
		ServerOptions gso = getGlobalServerOptions();
		return getTick() > gso.getRoundTimeLimit() * 2 * gso.getGameCycleFrequency();
	}

	protected boolean isTimeToShrink() {
		ServerOptions gso = getGlobalServerOptions();
		return getTick() > gso.getRoundTimeLimit() * gso.getGameCycleFrequency();
	}
}
