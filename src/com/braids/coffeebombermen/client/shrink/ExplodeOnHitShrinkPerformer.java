package com.braids.coffeebombermen.client.shrink;

import com.braids.coffeebombermen.client.gamecore.control.GameCoreHandler;
import com.braids.coffeebombermen.client.gamecore.control.Player;
import com.braids.coffeebombermen.options.Shrinkers;
import com.braids.coffeebombermen.options.OptConsts.Items;

public class ExplodeOnHitShrinkPerformer extends AbstractShrinkPerformer {

	private boolean started;

	public ExplodeOnHitShrinkPerformer(GameCoreHandler gameCoreHandler) {
		super(gameCoreHandler);
	}

	public Shrinkers getType() {
		return Shrinkers.ExplodeOnHit;
	}

	protected void initNextRoundImpl() {
		started = false;
	}

	protected void nextIterationImpl() {
		if (isTimeToShrink() && !started) {
			started = true;
			for (Player player : getGameCoreHandler().getPlayers().get(getGameCoreHandler().getOurClientIndex())) {
				player.setDetonatingOnHit(true);
				player.setExplodingTimeMultiplier(10);
				player.getModel().accumulateableItemQuantitiesMap.put(Items.BOMB, 25);
			}
		}
	}
}
