package com.braids.coffeebombermen.client.shrink;

import java.util.Random;

import com.braids.coffeebombermen.client.gamecore.control.GameCoreHandler;
import com.braids.coffeebombermen.options.Shrinkers;
import com.braids.coffeebombermen.options.OptConsts.Items;
import com.braids.coffeebombermen.utils.MathHelper;

public class DiseaseShrinkPerformer extends AbstractShrinkPerformer {

	private float              chance;
	private Random             randomGenerator;
	private static final int   GAME_CYCLE_FREQUENCY_MULTIPLIER = 2;
	private static final float PLACE_DISEASE_CHANCE            = 0.09f;

	public DiseaseShrinkPerformer(GameCoreHandler gameCoreHandler) {
		super(gameCoreHandler);
	}

	public Shrinkers getType() {
		return Shrinkers.Desease;
	}

	protected void initNextRoundImpl() {
		chance = PLACE_DISEASE_CHANCE;

	}

	protected void nextIterationImpl() {
		if (isTimeToShrink()) {
			if (isTimeToFirstShrink() || isTimeToNextShrink(getGlobalServerOptions().getGameCycleFrequency() * GAME_CYCLE_FREQUENCY_MULTIPLIER)) {
				System.out.println("DiseaseShrinkPerformer.nextIterationImpl()" + chance);
				for (int i = 1; i < getWidth() - 1; i++) {
					for (int j = 1; j < getHeight() - 1; j++) {
						if (MathHelper.checkRandomEvent(chance)) {
							if (MathHelper.checkRandomEvent(chance)) {
								setItem(i, j, Items.DISEASE);
							} else {
								setItem(i, j, Items.SUPER_DISEASE);
							}
						}
					}
				}

				// increase chance for laying spider bomb
				if ((chance < 1.0f) && MathHelper.checkRandomEvent(0.002f)) {
					chance = Math.min(chance + PLACE_DISEASE_CHANCE, 1.0f);
				}
				setLastShrinkOperationAt();
			} else {}
		}

	}

}
