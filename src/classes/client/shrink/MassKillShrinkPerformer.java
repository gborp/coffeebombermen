package classes.client.shrink;

import classes.client.gamecore.control.GameCoreHandler;

public class MassKillShrinkPerformer extends AbstractShrinkPerformer {

	public MassKillShrinkPerformer(GameCoreHandler gameCoreHandler) {
		super(gameCoreHandler);
	}

	protected void initNextRoundImpl() {
	}

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
}
