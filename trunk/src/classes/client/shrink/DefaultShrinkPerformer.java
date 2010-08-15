package classes.client.shrink;

import classes.client.gamecore.control.GameCoreHandler;
import classes.client.sound.SoundEffect;
import classes.options.Consts.Walls;
import classes.options.model.ServerOptions;
import classes.utils.MathHelper;

public class DefaultShrinkPerformer extends AbstractShrinkPerformer {

	private static final int   MAX_SPEEDUP_STEPS   = 12;
	private static final int   SPEEDUP_RATIO       = 30;
	private static final float SPEEDUP_POSSIBILITY = 0.1f;
	private static final int   PRE_SPEEDUP_TICKS   = 2;

	private ShrinkDirection    lastShrinkDirection;
	private int                lastNewWallX;
	private int                lastNewWallY;
	private int                shrinkMinX;
	private int                shrinkMinY;
	private int                shrinkMaxX;
	private int                shrinkMaxY;
	private ShrinkType         shrinkType;
	private int                speedupSteps;
	private int                preSpeedupWarn;

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
		int shrinkTypeRandom = getRandom().nextInt(2);
		if (shrinkTypeRandom == 0) {
			shrinkType = ShrinkType.CLOCKWISE_SPIRAL;
		} else if (shrinkTypeRandom == 1) {
			shrinkType = ShrinkType.ANTICLOCKWISE_SPIRAL;
		}
		speedupSteps = -1;
		preSpeedupWarn = -1;
	}

	protected void nextIterationImpl() {
		ServerOptions gso = getGlobalServerOptions();
		// return getTick() > gso.roundTimeLimit * gso.gameCycleFrequency;

		if (preSpeedupWarn > 0) {
			if ((getTick() & 2) == 0) {
				getLevel().getModel().getComponent(lastNewWallX, lastNewWallY).setWall(Walls.DEATH_WARN);
			} else {
				getLevel().getModel().getComponent(lastNewWallX, lastNewWallY).setWall(Walls.DEATH);
			}
		}

		if (isTimeToShrink()) {

			if (isTimeToFirstShrink() || isTimeToNextShrink((gso.getGameCycleFrequency() / (speedupSteps > 0 ? SPEEDUP_RATIO : 1)))) {

				if (speedupSteps <= 0 && preSpeedupWarn <= 0 && lastNewWallX != -1 && MathHelper.checkRandomEvent(SPEEDUP_POSSIBILITY)) {
					preSpeedupWarn = PRE_SPEEDUP_TICKS;
				}
				if (preSpeedupWarn > 0) {
					preSpeedupWarn--;
					if (preSpeedupWarn == 0) {
						speedupSteps = MAX_SPEEDUP_STEPS;
					}
					setLastShrinkOperationAt();
					return;
				}

				if (speedupSteps > 0) {
					speedupSteps--;
				}

				int newWallX = lastNewWallX;
				int newWallY = lastNewWallY;

				if (isTimeToFirstShrink()) {

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
				setLastShrinkOperationAt();

				lastNewWallX = newWallX;
				lastNewWallY = newWallY;
			}
		}
	}

}
