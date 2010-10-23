package com.braids.coffeebombermen.client.shrink;

import java.util.ArrayList;
import java.util.LinkedList;

import com.braids.coffeebombermen.client.gamecore.control.GameCoreHandler;
import com.braids.coffeebombermen.options.Shrinkers;
import com.braids.coffeebombermen.utils.MathHelper;

public class BinaryWalkingShrinkPerformer extends AbstractShrinkPerformer {

	private static final int        GAME_CYCLE_FREQUENCY_MULTIPLIER = 2;
	private static final int        WALKING_SPEED_DIVIDER           = 5;

	private LinkedList<Area>        lstArea;
	private LinkedList<WalkingLine> lstWalkingLine;

	public BinaryWalkingShrinkPerformer(GameCoreHandler gameCoreHandler) {
		super(gameCoreHandler);
	}

	public Shrinkers getType() {
		return Shrinkers.BinaryWalking;
	}

	protected void initNextRoundImpl() {
		lstArea = new LinkedList<Area>();
		lstWalkingLine = new LinkedList<WalkingLine>();
	}

	protected void nextIterationImpl() {
		if (isTimeToShrink()) {
			if ((getTick() % WALKING_SPEED_DIVIDER) == 0) {
				for (WalkingLine line : new ArrayList<WalkingLine>(lstWalkingLine)) {
					if (line.isFinished()) {
						lstWalkingLine.remove(line);
					} else {
						line.walk();
					}
				}
			}
			if (isTimeToFirstShrink() || isTimeToNextShrink(getGlobalServerOptions().getGameCycleFrequency() * GAME_CYCLE_FREQUENCY_MULTIPLIER)) {
				if (isTimeToFirstShrink()) {
					lstArea.add(new Area(1, 1, getWidth() - 2, getHeight() - 2));
				} else {
					float shrinkChance = 0.75f;
					ArrayList<Area> lstAreaCopy = new ArrayList<Area>(lstArea);
					for (Area a : lstAreaCopy) {
						if (MathHelper.checkRandomEvent(shrinkChance)) {
							split(a);
						}
					}
				}
				setLastShrinkOperationAt();
			}
		}
	}

	private void split(Area a) {
		lstArea.remove(a);
		// increase the possibility to split in the center
		double positionChance = MathHelper.halfHasMoreChancePossibility();
		if (a.width > a.height) {
			int splitAt = (int) (a.width * positionChance);
			lstWalkingLine.add(new WalkingLine(a.x + splitAt, a.y, a.height, false));
			if (splitAt != 0) {
				lstArea.add(new Area(a.x, a.y, splitAt, a.height));
			}
			if (splitAt != a.width - 1) {
				lstArea.add(new Area(a.x + splitAt + 1, a.y, a.width - splitAt - 1, a.height));
			}
		} else {
			int splitAt = (int) (a.height * positionChance);
			lstWalkingLine.add(new WalkingLine(a.x, a.y + splitAt, a.width, true));
			if (splitAt != 0) {
				lstArea.add(new Area(a.x, a.y, a.width, splitAt));
			}
			if (splitAt != a.height - 1) {
				lstArea.add(new Area(a.x, a.y + splitAt + 1, a.width, a.height - splitAt - 1));
			}
		}
	}

	private static class Area {

		private final int x;
		private final int y;
		private final int width;
		private final int height;

		Area(int x, int y, int width, int height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

		public String toString() {
			return "Area x:" + x + " y:" + y + " width:" + width + " height: " + height;
		}
	}

	private class WalkingLine {

		private int           x;
		private int           y;
		private final boolean horizontal;
		private int           size;

		WalkingLine(int x, int y, int size, boolean horizontal) {
			this.x = x;
			this.y = y;
			this.size = size;
			this.horizontal = horizontal;
		}

		private void walk() {
			addDeathWall(x, y);
			if (horizontal) {
				x++;
			} else {
				y++;
			}
			size--;
		}

		private boolean isFinished() {
			return size <= 0;
		}

	}

}
