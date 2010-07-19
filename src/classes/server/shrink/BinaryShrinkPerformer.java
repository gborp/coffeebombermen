package classes.server.shrink;

import java.util.ArrayList;
import java.util.LinkedList;

import classes.options.model.LevelOptions;
import classes.server.Server;
import classes.utils.MathHelper;


public class BinaryShrinkPerformer implements ShrinkPerformer{

	private static final long DELAY_SCHRINK_STEP = 5000;
	
	private long lastShrinkOperationAt;
	private final Server server;
	private LinkedList<Area> lstArea;

	public BinaryShrinkPerformer(Server server) {
		this.server = server;
	}

	public void init() {
		lastShrinkOperationAt = 0;
		lstArea = new LinkedList<Area>();
	}

	public void shrink(StringBuilder newClientsActions) {
		long now = System.currentTimeMillis();
		if (lastShrinkOperationAt == 0
				|| ((now - lastShrinkOperationAt) > DELAY_SCHRINK_STEP)) {
			
			if (lastShrinkOperationAt == 0) {
				LevelOptions levelOptions = server.getServerOptionsManager().getOptions().levelOptions;
				int width = levelOptions.levelWidth;
				int height = levelOptions.levelHeight;
				lstArea.add(new Area(1, 1, width - 2, height - 2));
			} else {
				float shringChance = 0.75f;
				ArrayList<Area> lstAreaCopy = new ArrayList<Area>(lstArea);
				for (Area a : lstAreaCopy) {
					if (MathHelper.checkRandomEvent(shringChance)) {
						split(a, newClientsActions);
					}
				}
			}
			lastShrinkOperationAt = now;
		}
	}
	
	private void split(Area a, StringBuilder newClientsActions) {
		lstArea.remove(a);
		// increase the possibility to split in the center
		double positionChance = MathHelper.halfHasMoreChancePossibility();
		if (a.width > a.height) {
			int splitAt = (int) (a.width * positionChance);
			for (int i = 0; i < a.height; i++) {
				newClientsActions.append(Helper.wall(a.x + splitAt, a.y + i));
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
				newClientsActions.append(Helper.wall(a.x + i, a.y + splitAt));
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

