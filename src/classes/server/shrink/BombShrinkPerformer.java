package classes.server.shrink;

import classes.options.model.LevelOptions;
import classes.server.Server;
import classes.utils.MathHelper;


public class BombShrinkPerformer implements ShrinkPerformer{

	private static final long DELAY_SHRINKING_GAME_AREA = 4000;

	private long lastShrinkOperationAt;
	private final Server server;
	private float numberOfBombs;
	private int width;
	private int height;
	private int maxRange;
	private float chanceForStandingBomb;

	public BombShrinkPerformer(Server server) {
		this.server = server;
	}

	public void init() {
		lastShrinkOperationAt = 0;
		numberOfBombs = 1;
		maxRange = 1;
		chanceForStandingBomb = 1.0f;
		LevelOptions levelOptions = server.getServerOptionsManager().getOptions().levelOptions;
		width = levelOptions.levelWidth;
		height = levelOptions.levelHeight;
	}

	public void shrink(StringBuilder newClientsActions) {
		long now = System.currentTimeMillis();
		if (lastShrinkOperationAt == 0
				|| ((now - lastShrinkOperationAt) > DELAY_SHRINKING_GAME_AREA)) {
			for (int i = 0; i < numberOfBombs; i++) {
				int x = (int)Math.round(Math.random() * (width - 3) + 1);
				int y = (int)Math.round(Math.random() * (height - 3) + 1);
				int direction = 0;
				if (!MathHelper.checkRandomEvent(chanceForStandingBomb)) {
					direction = MathHelper.randomInt(1, 4);
				}
				newClientsActions.append(Helper.bomb(x, y, MathHelper.randomInt(1, maxRange), direction));
			}
			lastShrinkOperationAt = now;
			if (numberOfBombs < width*height) {
				numberOfBombs = numberOfBombs * 1.3f;
			}
			
			if (MathHelper.randomBoolean()) {
				maxRange++;
				chanceForStandingBomb*= 0.9f; 
			}
		}
	}

}

