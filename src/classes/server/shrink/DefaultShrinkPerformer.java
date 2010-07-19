package classes.server.shrink;

import classes.options.model.LevelOptions;
import classes.server.Server;
import classes.utils.MathHelper;


public class DefaultShrinkPerformer implements ShrinkPerformer{

	private static final double NORMAL_SPEED = 500;
	private static final double TURBO_SPEED = 10;
	
	private ShrinkDirection lastShrinkDirection;
	private int lastNewWallX;
	private int lastNewWallY;
	private long lastShrinkOperationAt;
	private int shrinkMinX;
	private int shrinkMinY;
	private int shrinkMaxX;
	private int shrinkMaxY;
	private final Server server;
	private double delayShrinkingGameArea;
	private int turbostep;

	private enum ShrinkDirection {
		RIGHT, DOWN, LEFT, UP
	}

	public DefaultShrinkPerformer(Server server) {
		this.server = server;
	}

	public void init() {
		lastShrinkDirection = ShrinkDirection.RIGHT;
		lastNewWallX = 0;
		lastNewWallY = 0;
		lastShrinkOperationAt = 0;
		delayShrinkingGameArea = NORMAL_SPEED;
		turbostep = 0;
	}

	public void shrink(StringBuilder newClientsActions) {
		long now = System.currentTimeMillis();
		if (lastShrinkOperationAt == 0
				|| ((now - lastShrinkOperationAt) > delayShrinkingGameArea)) {

			int newWallX = lastNewWallX;
			int newWallY = lastNewWallY;

			LevelOptions levelOptions = server.getServerOptionsManager().getOptions().levelOptions;
			int width = levelOptions.levelWidth;
			int height = levelOptions.levelHeight;

			if (lastShrinkOperationAt == 0) {

				newWallX = 0;
				newWallY = 0;
				shrinkMinX = 0;
				shrinkMinY = 1;
				shrinkMaxX = width - 1;
				shrinkMaxY = height - 1;
			} else {
				if (shrinkMaxX <= shrinkMinX && shrinkMaxY <= shrinkMinY) {
					newWallX = -1;
				} else {
					switch (lastShrinkDirection) {
					case RIGHT:
						newWallX++;
						if (newWallX == shrinkMaxX) {
							lastShrinkDirection = ShrinkDirection.DOWN;
							shrinkMaxX--;
						}
						break;
					case DOWN:
						newWallY++;
						if (newWallY == shrinkMaxY) {
							lastShrinkDirection = ShrinkDirection.LEFT;
							shrinkMaxY--;
						}
						break;
					case LEFT:
						newWallX--;
						if (newWallX == shrinkMinX) {
							lastShrinkDirection = ShrinkDirection.UP;
							shrinkMinX++;
						}
						break;
					case UP:
						newWallY--;
						if (newWallY == shrinkMinY) {
							lastShrinkDirection = ShrinkDirection.RIGHT;
							shrinkMinY++;
						}
						break;
					}
				}
			}

			if (newWallX >= 0 && newWallX < width && newWallY >= 0
					&& newWallY < height) {
				newClientsActions.append(Helper.wall(newWallX, newWallY));
			}
			lastShrinkOperationAt = now;

			lastNewWallX = newWallX;
			lastNewWallY = newWallY;

			if (turbostep == -1) {
				int changeSpeed = MathHelper.randomInt(99);
				if (changeSpeed < 10) {
					// goto turbo
					turbostep = 0;
					delayShrinkingGameArea = TURBO_SPEED;
				}
			} else if (turbostep == 10) {
				turbostep = -1;
				delayShrinkingGameArea = NORMAL_SPEED;
			} else {
				turbostep++;
			}
		}
	}

}

