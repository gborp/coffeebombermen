package classes.client.gamecore.control;

import java.util.Comparator;

import classes.client.gamecore.model.PlayerModel;

public class PlayerModelComparatorByPoint implements Comparator<PlayerModel> {

	public int compare(PlayerModel o1, PlayerModel o2) {
		if (o1.getPoints() == o2.getPoints()) {
			return 0;
		} else if (o1.getPoints() < o2.getPoints()) {
			return 1;
		} else {
			return -1;
		}
	}

}
