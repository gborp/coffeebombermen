package classes.server.shrink;

import classes.options.model.LevelOptions;
import classes.server.Server;


public class MassKillShrinkPerformer implements ShrinkPerformer{

	private final Server server;
	private int width;
	private int height;

	public MassKillShrinkPerformer(Server server) {
		this.server = server;
	}

	public void init() {
		LevelOptions levelOptions = server.getServerOptionsManager().getOptions().levelOptions;
		width = levelOptions.levelWidth;
		height = levelOptions.levelHeight;
	}

	public void shrink(StringBuilder newClientsActions) {
		for (int i = 1; i < width - 1; i++) {
			for (int j = 1; j < height - 1; j++) {
				newClientsActions.append(Helper.wall(i, j));
			}
		}
	}

}

