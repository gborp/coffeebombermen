package classes.client.shrink;

import classes.client.gamecore.control.Level;
import classes.client.sound.SoundEffect;
import classes.options.Consts.Walls;

/**
 * Helper for this package only
 */
class Helper {

	static void addDeathWall(Level level, int x, int y) {
		int width = level.getModel().getWidth();
		int height = level.getModel().getHeight();

		if (x >= 0 && x < width && y >= 0 && y < height) {
			level.getModel().getComponents()[y][x].setItem(null);
			level.getModel().getComponents()[y][x].setWall(Walls.DEATH);
			SoundEffect.DEATH_WALL.play();
		}
	}

	

//	static String wall(int x, int y) {
//		StringBuilder newClientsActions = new StringBuilder();
//		newClientsActions.append(CommandTargets.WALL+ " ");
//		newClientsActions.append(Integer.toString(x));
//		newClientsActions.append(' ');
//		newClientsActions.append(Integer.toString(y));
//		newClientsActions.append(' ');
//		newClientsActions.append('d');
//		newClientsActions
//				.append(GeneralStringTokenizer.GENERAL_SEPARATOR_CHAR);
//		return newClientsActions.toString();
//
//	}
//
//	static String wallRemove(int x, int y) {
//		StringBuilder newClientsActions = new StringBuilder();
//		newClientsActions.append(CommandTargets.WALL_REMOVE+ " ");
//		newClientsActions.append(Integer.toString(x));
//		newClientsActions.append(' ');
//		newClientsActions.append(Integer.toString(y));
//		newClientsActions.append(' ');
//		newClientsActions.append('d');
//		newClientsActions
//				.append(GeneralStringTokenizer.GENERAL_SEPARATOR_CHAR);
//		return newClientsActions.toString();
//	}
//	
//	/**
//	 * @param x
//	 * @param y
//	 * @param range
//	 * @param direction 0 no, 1 up, 2 right, 3 down, 4 left
//	 * @return
//	 */
//	static String bomb(int x, int y, int range, int direction) {
//		StringBuilder newClientsActions = new StringBuilder();
//		newClientsActions.append(CommandTargets.BOMB +" ");
//		newClientsActions.append(Integer.toString(x));
//		newClientsActions.append(' ');
//		newClientsActions.append(Integer.toString(y));
//		newClientsActions.append(' ');
//		// Range
//		newClientsActions.append(range);
//		newClientsActions.append(' ');
//		if (direction == 0) {
//			// Standing bomb
//			newClientsActions.append("S");
//		} else {
//			if (direction == 1) {
//				newClientsActions.append("U");
//			} else if (direction == 2) {
//				newClientsActions.append("R");
//			} else if (direction == 3) {
//				newClientsActions.append("D");
//			} else if (direction == 4) {
//				newClientsActions.append("L");
//			}
//		}
//		newClientsActions.append(' ');
//		newClientsActions.append('d');
//		newClientsActions
//				.append(GeneralStringTokenizer.GENERAL_SEPARATOR_CHAR);
//	return newClientsActions.toString();
//
//	}
//
//	public static Object item(int x, int y, Items item) {
//		StringBuilder newClientsActions = new StringBuilder();
//		newClientsActions.append(CommandTargets.ITEM+ " ");
//		newClientsActions.append(Integer.toString(x));
//		newClientsActions.append(' ');
//		newClientsActions.append(Integer.toString(y));
//		newClientsActions.append(' ');
//		newClientsActions.append(item.toString());
//		newClientsActions.append(' ');
//		newClientsActions.append('d');
//		newClientsActions
//				.append(GeneralStringTokenizer.GENERAL_SEPARATOR_CHAR);
//		return newClientsActions.toString();
//	}

}
