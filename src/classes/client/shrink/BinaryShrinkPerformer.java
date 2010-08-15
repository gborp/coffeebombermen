package classes.client.shrink;

import java.util.ArrayList;
import java.util.LinkedList;

import classes.client.gamecore.control.GameCoreHandler;
import classes.options.Shrinkers;
import classes.utils.MathHelper;

public class BinaryShrinkPerformer extends AbstractShrinkPerformer {

	private static final int GAME_CYCLE_FREQUENCY_MULTIPLIER = 2;

	private LinkedList<Area> lstArea;

	public BinaryShrinkPerformer(GameCoreHandler gameCoreHandler) {
		super(gameCoreHandler);
	}

	public Shrinkers getType() {
		return Shrinkers.Binary;
	}

	protected void initNextRoundImpl() {
		lstArea = new LinkedList<Area>();
	}

	protected void nextIterationImpl() {
		if (isTimeToShrink()) {
			if (isTimeToFirstShrink() || isTimeToNextShrink(getGlobalServerOptions().getGameCycleFrequency() * GAME_CYCLE_FREQUENCY_MULTIPLIER)) {
				if (isTimeToFirstShrink()) {
					lstArea.add(new Area(1, 1, getWidth() - 2, getHeight() - 2));
				} else {
					float shringChance = 0.75f;
					ArrayList<Area> lstAreaCopy = new ArrayList<Area>(lstArea);
					for (Area a : lstAreaCopy) {
						if (MathHelper.checkRandomEvent(shringChance)) {
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
			for (int i = 0; i < a.height; i++) {
				addDeathWall(a.x + splitAt, a.y + i);
			}
			if (splitAt != 0) {
				lstArea.add(new Area(a.x, a.y, splitAt, a.height));
			}
			if (splitAt != a.width - 1) {
				lstArea.add(new Area(a.x + splitAt + 1, a.y, a.width - splitAt - 1, a.height));
			}
		} else {
			int splitAt = (int) (a.height * positionChance);
			for (int i = 0; i < a.width; i++) {
				addDeathWall(a.x + i, a.y + splitAt);
			}
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

}
