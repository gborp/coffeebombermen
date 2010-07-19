package classes.server.shrink;

import classes.options.Consts.Items;
import classes.options.model.LevelOptions;
import classes.server.Server;
import classes.utils.MathHelper;


public class SpiderBombShrinkPerformer implements ShrinkPerformer{

	private static final double SPEED = 5000;

	private Server server;
	private int width;
	private int height;

	private float chance;

	private long lastShrinkOperationAt;

	public SpiderBombShrinkPerformer(Server server) {
		this.server = server;
	}

	public void init() {
		lastShrinkOperationAt = 0;
		LevelOptions levelOptions = server.getServerOptionsManager().getOptions().levelOptions;
		width = levelOptions.levelWidth;
		height = levelOptions.levelHeight;
		chance= 0.1f;
	}

	public void shrink(StringBuilder newClientsActions) {
		long now = System.currentTimeMillis();
		if (lastShrinkOperationAt == 0
				|| ((now - lastShrinkOperationAt) > SPEED)) {
			for (int i = 1; i < width - 1; i++) {
				for (int j = 1; j < height - 1; j++) {
					if (MathHelper.checkRandomEvent(chance)) {
						newClientsActions.append(Helper.item(i, j,
								Items.SPIDER_BOMB));
					}
				}
			}
			
			// increase chance for laying spider bomb
			if (chance < 1.0f && MathHelper.checkRandomEvent(0.1f)) {
				chance = Math.min(chance + 0.1f, 1.0f);
			}
			lastShrinkOperationAt = now;
		}
	}

}

