package classes.server.shrink;

import java.awt.Point;

import classes.options.model.LevelOptions;
import classes.server.Server;


public class DrawShrinkPerformer implements ShrinkPerformer{

	private static final long DELAY_SCHRINK_STEP = 500;
	private double DELAY_USER_REAGATE = 1000;
	
	private long lastShrinkOperationAt;
	private final Server server;

	private int width;

	private int height;

	private boolean waitForUserReagate;

	public DrawShrinkPerformer(Server server) {
		this.server = server;
	}

	public void init() {
		lastShrinkOperationAt = 0;
		LevelOptions levelOptions = server.getServerOptionsManager().getOptions().levelOptions;
		width = levelOptions.levelWidth;
		height = levelOptions.levelHeight;
		waitForUserReagate = false;
//		wallPositions = new Point[server.get];
	}

	public void shrink(StringBuilder newClientsActions) {
		long now = System.currentTimeMillis();
		if (lastShrinkOperationAt == 0
				|| ((now - lastShrinkOperationAt) > DELAY_SCHRINK_STEP)) {

			if (lastShrinkOperationAt == 0) {
				for (int i = 0 ; i < width; i++) {
					newClientsActions.append(Helper.wall(i, 0));
					newClientsActions.append(Helper.wall(i, height - 1));
				}
				for (int i = 0 ; i < height; i++) {
					newClientsActions.append(Helper.wall(0, i));
					newClientsActions.append(Helper.wall(width - 1, i));
				}
				
				for (int i = 1 ; i < width - 1; i++) {
					for (int j = 1 ; j < height - 1; j++) {
						newClientsActions.append(Helper.wallRemove(i, j));
					}
				}

				waitForUserReagate = true;
				
				lastShrinkOperationAt = now;
			} else {
				
				// let the users realize that they have to move always bcause the walls are appearing on they positions
				if (waitForUserReagate) {
					if ((now - lastShrinkOperationAt) > DELAY_USER_REAGATE) {
						waitForUserReagate = false;	
					}
				} else {
					
				}
			}

		}
	}

}

