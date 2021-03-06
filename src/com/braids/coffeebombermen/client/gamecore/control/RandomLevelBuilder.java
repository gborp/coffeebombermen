package com.braids.coffeebombermen.client.gamecore.control;

import com.braids.coffeebombermen.client.gamecore.model.level.LevelModel;
import com.braids.coffeebombermen.options.OptConsts.Walls;
import com.braids.coffeebombermen.options.model.LevelOptions;
import com.braids.coffeebombermen.options.model.ServerOptions;
import com.braids.coffeebombermen.utils.MathHelper;

public class RandomLevelBuilder {

	private enum LEVEL_GEN_ITEM {
		CONCRETE, UNKNOWN, ACCESSIBLE
	}

	private static int maxGatewayEntranceNumber;
	private static int maxGatewayExitNumber;
	private static int chanceForGatewayEntrance;
	private static int chanceForGatewayExit;
	private static int nofGatewayEntrance;
	private static int nofGatewayExit;

	/**
	 * Generates and returns a random level specified by the global server
	 * options.
	 * 
	 * @return a random level specified by the global server options
	 */
	public static Level generateRandomLevel(ServerOptions globalServerOptions, GameCoreHandler gameCoreHandler) {
		final LevelOptions levelOptions = globalServerOptions.getLevelOptions();
		final Level level = new Level(levelOptions, gameCoreHandler);
		LevelModel levelModel = level.getModel();

		final int levelWidth = levelModel.getWidth();
		final int levelHeight = levelModel.getHeight();

		maxGatewayEntranceNumber = gameCoreHandler.getGlobalServerOptions().getMaxGatewayEntranceNumber();
		maxGatewayExitNumber = gameCoreHandler.getGlobalServerOptions().getMaxGatewayExitNumber();

		if ((maxGatewayEntranceNumber == 0) || (maxGatewayExitNumber == 0)) {
			chanceForGatewayEntrance = 0;
			chanceForGatewayExit = 0;
			maxGatewayEntranceNumber = 0;
			maxGatewayExitNumber = 0;
		} else {
			chanceForGatewayEntrance = (levelWidth + levelHeight - 4) / maxGatewayEntranceNumber;
			chanceForGatewayExit = (levelWidth + levelHeight - 4) / maxGatewayExitNumber;
		}

		nofGatewayEntrance = 0;
		nofGatewayExit = 0;

		Walls wall;
		for (int y = 1; y < levelHeight - 1; y++) {
			for (int x = 1; x < levelWidth - 1; x++) {
				if (MathHelper.nextInt(100) > 95) {
					wall = MathHelper.nextInt(100) > 30 ? Walls.BRICK : Walls.CONCRETE;
				} else if (((x & 0x01) == 0) && ((y & 0x01) == 0)) {
					wall = Walls.CONCRETE; // Inner concrete matrix
				} else {
					wall = MathHelper.nextInt(100) < globalServerOptions.getAmountOfBrickWalls() ? Walls.BRICK : Walls.EMPTY;
				}

				levelModel.getComponent(x, y).setWall(wall);
			}
		}

		levelModel.getComponent(0, 0).setWall(Walls.CONCRETE);
		levelModel.getComponent(0, levelHeight - 1).setWall(Walls.CONCRETE);
		levelModel.getComponent(levelWidth - 1, 0).setWall(Walls.CONCRETE);
		levelModel.getComponent(levelWidth - 1, levelHeight - 1).setWall(Walls.CONCRETE);

		for (int y = 1; y < levelHeight - 1; y++) {
			createBorder(levelModel, 0, y);
			createBorder(levelModel, levelWidth - 1, y);
		}

		for (int x = 1; x < levelWidth - 1; x++) {
			createBorder(levelModel, x, 0);
			createBorder(levelModel, x, levelHeight - 1);
		}

		if ((nofGatewayEntrance == 0) && (nofGatewayExit != 0)) {
			createRandomBorder(levelModel, Walls.GATEWAY_ENTRANCE);
			nofGatewayEntrance++;
		}

		if ((nofGatewayEntrance != 0) && (nofGatewayExit == 0)) {
			createRandomBorder(levelModel, Walls.GATEWAY_EXIT);
			nofGatewayExit++;
		}

		levelModel.getComponent(1, 1).setWall(Walls.EMPTY);

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
					if ((x == 1) && (y == 1)) {
						row[1] = LEVEL_GEN_ITEM.ACCESSIBLE;
						continue;
					}
					if (row[x] == LEVEL_GEN_ITEM.UNKNOWN) {
						boolean hasAccessibleNeighbour = (row[x - 1] == LEVEL_GEN_ITEM.ACCESSIBLE) || (row[x + 1] == LEVEL_GEN_ITEM.ACCESSIBLE)
						        || (aboveRow[x] == LEVEL_GEN_ITEM.ACCESSIBLE) || (accessible[y + 1][x] == LEVEL_GEN_ITEM.ACCESSIBLE);

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
		while (neededToModify && (counter < 1000)) {
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

						if ((x > 1) && (vert || (!vert && !horz))) {
							levelModel.getComponent(x - 1, y).setWall(Walls.BRICK);
							isNowAccessible = isNowAccessible || (row[x - 1] == LEVEL_GEN_ITEM.ACCESSIBLE);
						}
						if ((x < width - 2) && (vert || (!vert && !horz))) {
							levelModel.getComponent(x + 1, y).setWall(Walls.BRICK);
							isNowAccessible = isNowAccessible || (row[x + 1] == LEVEL_GEN_ITEM.ACCESSIBLE);
						}
						if ((y > 1) && (horz || (!horz && !vert))) {
							levelModel.getComponent(x, y - 1).setWall(Walls.BRICK);
							isNowAccessible = isNowAccessible || (aboveRow[x] == LEVEL_GEN_ITEM.ACCESSIBLE);
						}
						if ((y < height - 2) && (horz || (!horz && !vert))) {
							levelModel.getComponent(x, y + 1).setWall(Walls.BRICK);
							isNowAccessible = isNowAccessible || (accessible[y + 1][x] == LEVEL_GEN_ITEM.ACCESSIBLE);
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

	private static void createBorder(LevelModel levelModel, int posX, int posY) {
		if (isBadPositionForGateway(levelModel, posX, posY)) {
			levelModel.getComponent(posX, posY).setWall(Walls.CONCRETE);
		} else if ((nofGatewayEntrance < maxGatewayEntranceNumber) && (MathHelper.nextInt(chanceForGatewayEntrance) == 0)) {
			levelModel.getComponent(posX, posY).setWall(Walls.GATEWAY_ENTRANCE);
			nofGatewayEntrance++;
		} else if ((nofGatewayExit < maxGatewayExitNumber) && (MathHelper.nextInt(chanceForGatewayExit) == 0)) {
			levelModel.getComponent(posX, posY).setWall(Walls.GATEWAY_EXIT);
			levelModel.addGatewayExitPosition(posX, posY);
			nofGatewayExit++;
		} else {
			levelModel.getComponent(posX, posY).setWall(Walls.CONCRETE);
		}
	}

	private static void createRandomBorder(LevelModel levelModel, Walls wall) {
		int posX;
		int posY;
		do {
			if (MathHelper.nextInt(2) == 0) {
				posX = 0;
				posY = 1 + MathHelper.nextInt(levelModel.getHeight() - 3);
			} else {
				posX = 1 + MathHelper.nextInt(levelModel.getWidth() - 3);
				posY = 0;
			}
		} while ((levelModel.getComponent(posX, posY).getWall() != Walls.CONCRETE) || (isBadPositionForGateway(levelModel, posX, posY)));
		levelModel.getComponent(posX, posY).setWall(wall);
		if (Walls.GATEWAY_EXIT.equals(wall)) {
			levelModel.addGatewayExitPosition(posX, posY);
		}
	}

	private static boolean isBadPositionForGateway(LevelModel levelModel, int posX, int posY) {
		if (posX == 0) {
			if (levelModel.getComponent(1, posY).getWall() == Walls.CONCRETE) {
				return true;
			}
		} else if (posX == levelModel.getWidth() - 1) {
			if (levelModel.getComponent(posX - 1, posY).getWall() == Walls.CONCRETE) {
				return true;
			}

		} else if (posY == 0) {
			if (levelModel.getComponent(posX, 1).getWall() == Walls.CONCRETE) {
				return true;
			}

		} else if (posY == levelModel.getHeight() - 1) {
			if (levelModel.getComponent(posX, posY - 1).getWall() == Walls.CONCRETE) {
				return true;
			}
		}
		return false;
	}
}
