package classes.client.shrink;

import classes.client.gamecore.control.GameCoreHandler;
import classes.client.gamecore.control.Level;
import classes.client.sound.SoundEffect;
import classes.options.Consts.Walls;
import classes.options.model.ServerOptions;

public class DefaultShrinkPerformer implements ShrinkPerformer{

	private ShrinkDirection lastShrinkDirection;
	private int lastNewWallX;
	private int lastNewWallY;
	private long lastShrinkOperationAt;
	private int shrinkMinX;
	private int shrinkMinY;
	private int shrinkMaxX;
	private int shrinkMaxY;
	private Level level;
	private ShrinkType shrinkType;
	private final GameCoreHandler gameCoreHandler;

	private enum ShrinkDirection {
		RIGHT, DOWN, LEFT, UP
	}

	private static enum ShrinkType {
		CLOCKWISE_SPIRAL, ANTICLOCKWISE_SPIRAL
	}

	public DefaultShrinkPerformer(GameCoreHandler gameCoreHandler) {
		this.gameCoreHandler = gameCoreHandler;
	}
	
	public void initNextRound() {
		this.level = gameCoreHandler.getLevel();
		lastShrinkDirection = ShrinkDirection.RIGHT;
		lastNewWallX = 0;
		lastNewWallY = 0;
		lastShrinkOperationAt = 0;
		int shrinkTypeRandom = gameCoreHandler.getRandom().nextInt(2);
		if (shrinkTypeRandom == 0) {
			shrinkType = ShrinkType.CLOCKWISE_SPIRAL;
		} else if (shrinkTypeRandom == 1) {
			shrinkType = ShrinkType.ANTICLOCKWISE_SPIRAL;
		}
	}

	public void nextIteration() {
		ServerOptions gso = gameCoreHandler.getGlobalServerOptions();
		if (gameCoreHandler.getTick() > gso.roundTimeLimit * gso.gameCycleFrequency) {
			if (lastShrinkOperationAt == 0 || ((gameCoreHandler.getTick() - lastShrinkOperationAt) > (gso.gameCycleFrequency))) {

				int newWallX = lastNewWallX;
				int newWallY = lastNewWallY;

				int width = level.getModel().getWidth();
				int height = level.getModel().getHeight();

				if (lastShrinkOperationAt == 0) {

					switch (shrinkType) {
						case CLOCKWISE_SPIRAL:
							newWallX = 0;
							newWallY = 0;
							shrinkMinX = 0;
							shrinkMinY = 1;
							shrinkMaxX = width - 1;
							shrinkMaxY = height - 1;
							lastShrinkDirection = ShrinkDirection.RIGHT;
							break;

						case ANTICLOCKWISE_SPIRAL:
							newWallX = 0;
							newWallY = 0;
							shrinkMinX = 1;
							shrinkMinY = 0;
							shrinkMaxX = width - 1;
							shrinkMaxY = height - 1;
							lastShrinkDirection = ShrinkDirection.DOWN;
							break;
					}

				} else {
					if (shrinkMaxX <= shrinkMinX && shrinkMaxY <= shrinkMinY) {
						newWallX = -1;
					} else {
						if (shrinkType == ShrinkType.CLOCKWISE_SPIRAL) {
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
						} else if (shrinkType == ShrinkType.ANTICLOCKWISE_SPIRAL) {
							switch (lastShrinkDirection) {
								case RIGHT:
									newWallX++;
									if (newWallX == shrinkMaxX) {
										lastShrinkDirection = ShrinkDirection.UP;
										shrinkMaxX--;
									}
									break;
								case DOWN:
									newWallY++;
									if (newWallY == shrinkMaxY) {
										lastShrinkDirection = ShrinkDirection.RIGHT;
										shrinkMaxY--;
									}
									break;
								case LEFT:
									newWallX--;
									if (newWallX == shrinkMinX) {
										lastShrinkDirection = ShrinkDirection.DOWN;
										shrinkMinX++;
									}
									break;
								case UP:
									newWallY--;
									if (newWallY == shrinkMinY) {
										lastShrinkDirection = ShrinkDirection.LEFT;
										shrinkMinY++;
									}
									break;
							}
						}
					}
				}

				if (newWallX >= 0 && newWallX < width && newWallY >= 0 && newWallY < height) {
					level.getModel().getComponents()[newWallY][newWallX].setItem(null);
					level.getModel().getComponents()[newWallY][newWallX].setWall(Walls.DEATH);
					SoundEffect.DEATH_WALL.play();
				}
				lastShrinkOperationAt = gameCoreHandler.getTick();

				lastNewWallX = newWallX;
				lastNewWallY = newWallY;
			}
		}
	}
}

