package com.braids.coffeebombermen.client.shrink;

import java.util.List;

import com.braids.coffeebombermen.client.gamecore.control.GameCoreHandler;
import com.braids.coffeebombermen.client.gamecore.model.PlayerModel;
import com.braids.coffeebombermen.client.gamecore.model.level.LevelModel;
import com.braids.coffeebombermen.client.sound.SoundEffect;
import com.braids.coffeebombermen.options.Shrinkers;
import com.braids.coffeebombermen.options.OptConsts.Walls;
import com.braids.coffeebombermen.options.model.ServerOptions;
import com.braids.coffeebombermen.utils.MathHelper;

public class MassKillShrinkPerformer extends AbstractShrinkPerformer {

	private static final int GAME_CYCLE_FREQUENCY_MULTIPLIER = 4;

	public boolean           preWarning;

	private boolean          firstTimeMassKill;

	/**
	 * 0: all player die at once, 2: all but one dies, 1: one survives... for a
	 * while.
	 */
	private int              survivingPossibility;

	private int              survivorX;

	private int              survivorY;

	public MassKillShrinkPerformer(GameCoreHandler gameCoreHandler) {
		super(gameCoreHandler);
	}

	public Shrinkers getType() {
		return Shrinkers.MassKill;
	}

	protected void initNextRoundImpl() {
		firstTimeMassKill = true;
		survivorX = -1;
		survivorY = -1;
	}

	protected void nextIterationImpl() {
		if (isTimeToShrink()) {
			if (isTimeToFirstShrink()) {
				preWarning = true;
			}

			if (isTimeToMassKill()) {
				if (firstTimeMassKill) {
					firstTimeMassKill = false;
					survivingPossibility = MathHelper.randomInt(2);
					if (survivingPossibility != 0) {
						List<PlayerModel> lstPlayer = getGameCoreHandler().getAllLivingPlayerModels();
						PlayerModel survivor = lstPlayer.get(MathHelper.randomInt(lstPlayer.size() - 1));
						survivorX = survivor.getComponentPosX();
						survivorY = survivor.getComponentPosY();
					}

					for (int i = 0; i < getWidth(); i++) {
						for (int j = 0; j < getHeight(); j++) {
							if ((survivingPossibility == 0) || !((i == survivorX) && (j == survivorY))) {
								addDeathWall(i, j);
							}
						}
					}
				} else if ((survivingPossibility == 1) && isTimeToLastKill()) {
					addDeathWall(survivorX, survivorY);
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

	private boolean isTimeToLastKill() {
		ServerOptions gso = getGlobalServerOptions();
		return getTick() > gso.getRoundTimeLimit() * 2 * gso.getGameCycleFrequency() + GameCoreHandler.LAST_PLAYER_COUNT_DOWN_BEFORE_WIN - 2;
	}

	protected boolean isTimeToShrink() {
		ServerOptions gso = getGlobalServerOptions();
		return getTick() > gso.getRoundTimeLimit() * gso.getGameCycleFrequency();
	}
}
