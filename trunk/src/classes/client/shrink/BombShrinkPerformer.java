package classes.client.shrink;

import classes.client.gamecore.Consts.BombPhases;
import classes.client.gamecore.Consts.Directions;
import classes.client.gamecore.control.GameCoreHandler;
import classes.client.gamecore.control.Level;
import classes.options.model.LevelOptions;
import classes.options.model.ServerOptions;
import classes.server.Server;
import classes.utils.MathHelper;

public class BombShrinkPerformer extends AbstractShrinkPerformer {

	private static final int GAME_CYCLE_FREQUENCY_MULTIPLIER = 10;

	private long lastShrinkOperationAt;
	private float numberOfBombs;
	private int maxRange;

	public BombShrinkPerformer(GameCoreHandler gameCoreHandler) {
		super(gameCoreHandler);
	}

	protected void initNextRoundImpl() {
		lastShrinkOperationAt = 0;
		numberOfBombs = 1;
		maxRange = 1;
	}

	protected void nextIterationImpl() {
		ServerOptions gso = getGlobalServerOptions();
		if (getTick() > gso.roundTimeLimit
				* gso.gameCycleFrequency) {
			if (lastShrinkOperationAt == 0
					|| ((getTick() - lastShrinkOperationAt) > (gso.gameCycleFrequency * GAME_CYCLE_FREQUENCY_MULTIPLIER))) {
				for (int i = 0; i < numberOfBombs; i++) {
					int x = (int) Math.round(Math.random() * (getWidth() - 3) + 1);
					int y = (int) Math.round(Math.random() * (getHeight() - 3) + 1);
					int direction = 0;
					direction = MathHelper.randomInt(3);
					addBomb(x, y, MathHelper.randomInt(1, maxRange), BombPhases.ROLLING, Directions.values()[direction]);
				}
				lastShrinkOperationAt = getTick();
				if (numberOfBombs < getWidth() * getHeight()) {
					numberOfBombs = numberOfBombs * 1.2f;
				}

				if (MathHelper.randomBoolean()) {
					maxRange++;
				}
			}
		}
	}
}
