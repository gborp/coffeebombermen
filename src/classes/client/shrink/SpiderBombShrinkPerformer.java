package classes.client.shrink;

import classes.client.gamecore.control.GameCoreHandler;
import classes.options.Consts.Items;
import classes.utils.MathHelper;

public class SpiderBombShrinkPerformer extends AbstractShrinkPerformer {

	private float chance;
	private static final int GAME_CYCLE_FREQUENCY_MULTIPLIER = 2;
	private static final float PLACE_SPIDER_CHANCE = 0.05f;

	public SpiderBombShrinkPerformer(GameCoreHandler gameCoreHandler) {
		super(gameCoreHandler);
	}

	protected void initNextRoundImpl() {
		chance = PLACE_SPIDER_CHANCE;
	}

	protected void nextIterationImpl() {
		if (isTimeToShrink()) {
			if (isTimeToFirstShrink()
					|| isTimeToNextShrink(getGlobalServerOptions().getGameCycleFrequency()
							* GAME_CYCLE_FREQUENCY_MULTIPLIER)) {
				System.out
						.println("SpiderBombShrinkPerformer.nextIterationImpl()" + chance);
				for (int i = 1; i < getWidth() - 1; i++) {
					for (int j = 1; j < getHeight() - 1; j++) {
						if (MathHelper.checkRandomEvent(chance)) {
							setItem(i, j, Items.SPIDER_BOMB);
						}
					}
				}

				// increase chance for laying spider bomb
				if (chance < 1.0f && MathHelper.checkRandomEvent(0.002f)) {
					chance = Math.min(chance + PLACE_SPIDER_CHANCE, 1.0f);
				}
				setLastShrinkOperationAt();
			}
		}
	}
}
