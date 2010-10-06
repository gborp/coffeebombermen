package com.braids.coffeebombermen.client.shrink;

import com.braids.coffeebombermen.client.gamecore.control.GameCoreHandler;
import com.braids.coffeebombermen.options.Shrinkers;
import com.braids.coffeebombermen.options.model.ServerOptions;

public class MassKillShrinkPerformer extends AbstractShrinkPerformer {

	public MassKillShrinkPerformer(GameCoreHandler gameCoreHandler) {
		super(gameCoreHandler);
	}

	public Shrinkers getType() {
		return Shrinkers.MassKill;
	}

	protected void initNextRoundImpl() {}

	protected void nextIterationImpl() {
		if (isTimeToShrink() && isTimeToFirstShrink()) {
			for (int i = 1; i < getWidth() - 1; i++) {
				for (int j = 1; j < getHeight() - 1; j++) {
					addDeathWall(i, j);
				}
			}
			setLastShrinkOperationAt();
		}
	}

	protected boolean isTimeToShrink() {
		ServerOptions gso = getGlobalServerOptions();
		return getTick() > gso.getRoundTimeLimit() * 2 * gso.getGameCycleFrequency();
	}
}
