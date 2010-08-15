package classes.client.shrink;

import classes.client.gamecore.control.GameCoreHandler;
import classes.options.Shrinkers;
import classes.utils.MathHelper;

public class BombShrinkPerformer extends AbstractShrinkPerformer {

	private static final int GAME_CYCLE_FREQUENCY_MULTIPLIER = 8;

	private float            numberOfBombs;
	private int              maxRange;

	public BombShrinkPerformer(GameCoreHandler gameCoreHandler) {
		super(gameCoreHandler);
	}

	public Shrinkers getType() {
		return Shrinkers.Bomb;
	}

	protected void initNextRoundImpl() {
		numberOfBombs = 1;
		maxRange = 1;
	}

	protected void nextIterationImpl() {
		if (isTimeToShrink()) {
			if (isTimeToFirstShrink() || isTimeToNextShrink(getGlobalServerOptions().getGameCycleFrequency() * GAME_CYCLE_FREQUENCY_MULTIPLIER)) {
				for (int i = 0; i < numberOfBombs; i++) {
					int x = getRandom().nextInt(getWidth() - 3) + 1;
					int y = getRandom().nextInt(getHeight() - 3) + 1;
					addCrazyBomb(x, y, MathHelper.randomInt(1, maxRange));
				}
				setLastShrinkOperationAt();
				if (numberOfBombs < getWidth() * getHeight()) {
					numberOfBombs = numberOfBombs * 1.3f;
				}

				if (MathHelper.randomBoolean()) {
					maxRange++;
				}
			}
		}
	}
}
