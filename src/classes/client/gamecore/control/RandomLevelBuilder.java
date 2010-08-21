package classes.client.gamecore.control;

import java.util.Random;

import classes.client.gamecore.model.level.LevelModel;
import classes.options.Consts.Walls;
import classes.options.model.LevelOptions;
import classes.options.model.ServerOptions;

public class RandomLevelBuilder {

	private enum LEVEL_GEN_ITEM {
		CONCRETE, UNKNOWN, ACCESSIBLE
	}

	/**
	 * Generates and returns a random level specified by the global server
	 * options.
	 * 
	 * @return a random level specified by the global server options
	 */
	public static Level generateRandomLevel(ServerOptions globalServerOptions, GameCoreHandler gameCoreHandler, Random random) {
		final LevelOptions levelOptions = globalServerOptions.getLevelOptions();
		final Level level = new Level(levelOptions, gameCoreHandler);
		LevelModel levelModel = level.getModel();

		final int levelWidth = levelModel.getWidth();
		final int levelHeight = levelModel.getHeight();

		for (int y = 0; y < levelHeight; y++) {
			for (int x = 0; x < levelWidth; x++) {
				Walls wall;

				if (y == 0 || y == levelHeight - 1 || x == 0 || x == levelWidth - 1) {
					wall = Walls.CONCRETE; // Border
				} else if (random.nextInt(100) > 95) {
					wall = random.nextInt(100) > 30 ? Walls.BRICK : Walls.CONCRETE;
				} else if ((x & 0x01) == 0 && (y & 0x01) == 0) {
					wall = Walls.CONCRETE; // Inner concrete matrix
				} else {
					wall = random.nextInt(100) < globalServerOptions.getAmountOfBrickWalls() ? Walls.BRICK : Walls.EMPTY;
				}

				levelModel.getComponent(x, y).setWall(wall);
			}
		}
		levelModel.getComponent(1, 1).setWall(random.nextInt(100) > 50 ? Walls.BRICK : Walls.EMPTY);

		deblockLevel(levelModel, levelWidth, levelHeight);

		return level;
	}

	private static void deblockLevel(LevelModel levelModel, int width, int height) {
		LEVEL_GEN_ITEM[][] accessible = new LEVEL_GEN_ITEM[height][width];
		for (int y = 0; y < height - 0; y++) {
			LEVEL_GEN_ITEM[] row = accessible[y];
			for (int x = 0; x < width - 0; x++) {
				if (levelModel.getComponent(x, y).getWall() != Walls.CONCRETE) {
					row[x] = LEVEL_GEN_ITEM.UNKNOWN;
				} else {
					row[x] = LEVEL_GEN_ITEM.CONCRETE;
				}
			}
		}

		boolean changed = true;

		while (changed) {
			changed = false;
			LEVEL_GEN_ITEM[] aboveRow = accessible[0];
			for (int y = 1; y < height - 1; y++) {
				LEVEL_GEN_ITEM[] row = accessible[y];
				for (int x = 1; x < width - 1; x++) {
					if (x == 1 && y == 1) {
						row[1] = LEVEL_GEN_ITEM.ACCESSIBLE;
						continue;
					}
					if (row[x] == LEVEL_GEN_ITEM.UNKNOWN) {
						boolean hasAccessibleNeighbour = row[x - 1] == LEVEL_GEN_ITEM.ACCESSIBLE || row[x + 1] == LEVEL_GEN_ITEM.ACCESSIBLE
						        || aboveRow[x] == LEVEL_GEN_ITEM.ACCESSIBLE || accessible[y + 1][x] == LEVEL_GEN_ITEM.ACCESSIBLE;

						if (hasAccessibleNeighbour) {
							accessible[y][x] = LEVEL_GEN_ITEM.ACCESSIBLE;
							changed = true;
						}
					}
				}
				aboveRow = row;
			}
		}

		int counter = 0;
		boolean neededToModify = true;
		while (neededToModify && counter < 1000) {
			counter++;
			neededToModify = false;
			LEVEL_GEN_ITEM[] aboveRow = accessible[0];
			for (int y = 1; y < height - 1; y++) {
				LEVEL_GEN_ITEM[] row = accessible[y];
				for (int x = 1; x < width - 1; x++) {
					if (row[x] == LEVEL_GEN_ITEM.UNKNOWN) {
						boolean isNowAccessible = false;
						boolean vert = (y & 0x01) == 1;
						boolean horz = (x & 0x01) == 1;

						if (x > 1 && (vert || (!vert && !horz))) {
							levelModel.getComponent(x - 1, y).setWall(Walls.BRICK);
							isNowAccessible = isNowAccessible || row[x - 1] == LEVEL_GEN_ITEM.ACCESSIBLE;
						}
						if (x < width - 2 && (vert || (!vert && !horz))) {
							levelModel.getComponent(x + 1, y).setWall(Walls.BRICK);
							isNowAccessible = isNowAccessible || row[x + 1] == LEVEL_GEN_ITEM.ACCESSIBLE;
						}
						if (y > 1 && (horz || (!horz && !vert))) {
							levelModel.getComponent(x, y - 1).setWall(Walls.BRICK);
							isNowAccessible = isNowAccessible || aboveRow[x] == LEVEL_GEN_ITEM.ACCESSIBLE;
						}
						if (y < height - 2 && (horz || (!horz && !vert))) {
							levelModel.getComponent(x, y + 1).setWall(Walls.BRICK);
							isNowAccessible = isNowAccessible || accessible[y + 1][x] == LEVEL_GEN_ITEM.ACCESSIBLE;
						}
						if (isNowAccessible) {
							row[x] = LEVEL_GEN_ITEM.ACCESSIBLE;
						}
						neededToModify = true;
					}
				}
			}
		}
	}
}
