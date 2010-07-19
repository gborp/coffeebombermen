package classes.server.shrink;

import classes.options.model.LevelOptions;
import classes.server.Server;
import classes.utils.MathHelper;

public class BombAndWallShrinkPerformer implements ShrinkPerformer {

	private static final long DELAY_SHRINKING_GAME_AREA = 4000;

	private long lastShrinkOperationAt;
	private final Server server;
	private float numberOfItems;
	private int width;
	private int height;

	private int maxRange;

	private float chanceForStandingBomb;

	public BombAndWallShrinkPerformer(Server server) {
		this.server = server;
	}

	public void init() {
		lastShrinkOperationAt = 0;
		numberOfItems = 1;
		maxRange = 1;
		chanceForStandingBomb = 1.0f;
		LevelOptions levelOptions = server.getServerOptionsManager()
				.getOptions().levelOptions;
		width = levelOptions.levelWidth;
		height = levelOptions.levelHeight;
	}

	public void shrink(StringBuilder newClientsActions) {
		long now = System.currentTimeMillis();
		if (lastShrinkOperationAt == 0
				|| ((now - lastShrinkOperationAt) > DELAY_SHRINKING_GAME_AREA)) {
			for (int i = 0; i < numberOfItems; i++) {
				int x = (int) Math.round(Math.random() * (width - 3) + 1);
				int y = (int) Math.round(Math.random() * (height - 3) + 1);
				if (MathHelper.randomInt(9) < 3) {
					newClientsActions.append(Helper.wall(x, y));
				} else {
					int direction = 0;
					if (!MathHelper.checkRandomEvent(chanceForStandingBomb)) {
						direction = MathHelper.randomInt(1, 4);
					}
					newClientsActions.append(Helper.bomb(x, y, MathHelper.randomInt(1, maxRange), direction));
				}
			}
			lastShrinkOperationAt = now;
			if (numberOfItems < width * height) {
				numberOfItems = numberOfItems * 1.2f;
			}

			if (MathHelper.randomBoolean()) {
				maxRange++;
				chanceForStandingBomb *= 0.9f;
			}
		}
	}
}
