package classes.client.shrink;

import classes.client.gamecore.control.GameCoreHandler;
import classes.client.gamecore.control.Level;
import classes.client.sound.SoundEffect;
import classes.options.Consts.Walls;
import classes.options.model.ServerOptions;
import classes.utils.MathHelper;

public class DefaultShrinkPerformer extends AbstractShrinkPerformer {

	private static final int MAX_SPEEDUP_STEPS = 20;
	private static final int SPEEDUP_RATIO = 20;
	private static final float SPEEDUP_POSSIBILITY = 0.02f;
	
	private ShrinkDirection lastShrinkDirection;
	private int lastNewWallX;
	private int lastNewWallY;
	private long lastShrinkOperationAt;
	private int shrinkMinX;
	private int shrinkMinY;
	private int shrinkMaxX;
	private int shrinkMaxY;
	private ShrinkType shrinkType;
	private int speedupSteps;

	private enum ShrinkDirection {
		RIGHT, DOWN, LEFT, UP
	}

	private static enum ShrinkType {
		CLOCKWISE_SPIRAL, ANTICLOCKWISE_SPIRAL
	}

	public DefaultShrinkPerformer(GameCoreHandler gameCoreHandler) {
		super(gameCoreHandler);
	}
	
	protected void initNextRoundImpl() {
		lastShrinkDirection = ShrinkDirection.RIGHT;
		lastNewWallX = 0;
		lastNewWallY = 0;
		lastShrinkOperationAt = 0;
		int shrinkTypeRandom = getRandom().nextInt(2);
		if (shrinkTypeRandom == 0) {
			shrinkType = ShrinkType.CLOCKWISE_SPIRAL;
		} else if (shrinkTypeRandom == 1) {
			shrinkType = ShrinkType.ANTICLOCKWISE_SPIRAL;
		}
	}

	protected void nextIterationImpl() {
		ServerOptions gso = getGlobalServerOptions();
		if (getTick() > gso.roundTimeLimit * gso.gameCycleFrequency) {
			if (lastShrinkOperationAt == 0 || ((getTick() - lastShrinkOperationAt) > (gso.gameCycleFrequency / (speedupSteps > 0 ? SPEEDUP_RATIO : 1)))) {
				if (speedupSteps <= 0 && MathHelper.checkRandomEvent(SPEEDUP_POSSIBILITY)) {
					speedupSteps = MAX_SPEEDUP_STEPS;
				}
				speedupSteps--;
				int newWallX = lastNewWallX;
				int newWallY = lastNewWallY;

				if (lastShrinkOperationAt == 0) {

					switch (shrinkType) {
						case CLOCKWISE_SPIRAL:
							newWallX = 0;
							newWallY = 0;
							shrinkMinX = 0;
							shrinkMinY = 1;
							shrinkMaxX = getWidth() - 1;
							shrinkMaxY = getHeight() - 1;
							lastShrinkDirection = ShrinkDirection.RIGHT;
							break;

						case ANTICLOCKWISE_SPIRAL:
							newWallX = 0;
							newWallY = 0;
							shrinkMinX = 1;
							shrinkMinY = 0;
							shrinkMaxX = getWidth() - 1;
							shrinkMaxY = getHeight() - 1;
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

				if (newWallX >= 0 && newWallX < getWidth() && newWallY >= 0 && newWallY < getHeight()) {
					getLevel().getModel().getComponents()[newWallY][newWallX].setItem(null);
					getLevel().getModel().getComponents()[newWallY][newWallX].setWall(Walls.DEATH);
					SoundEffect.DEATH_WALL.play();
				}
				lastShrinkOperationAt = getTick();

				lastNewWallX = newWallX;
				lastNewWallY = newWallY;
			}
		}
	}

}
