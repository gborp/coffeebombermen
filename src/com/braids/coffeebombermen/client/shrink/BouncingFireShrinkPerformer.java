package com.braids.coffeebombermen.client.shrink;

import java.util.ArrayList;

import com.braids.coffeebombermen.client.gamecore.FireShapes;
import com.braids.coffeebombermen.client.gamecore.control.GameCoreHandler;
import com.braids.coffeebombermen.options.Shrinkers;
import com.braids.coffeebombermen.utils.Position;

public class BouncingFireShrinkPerformer extends AbstractShrinkPerformer {

	private ArrayList<Slot> lstSlot;
	private int             nextWallLatency;

	public BouncingFireShrinkPerformer(GameCoreHandler gameCoreHandler) {
		super(gameCoreHandler);
	}

	public Shrinkers getType() {
		return Shrinkers.BouncingFire;
	}

	protected void initNextRoundImpl() {
		lstSlot = new ArrayList<Slot>();
	}

	protected void nextIterationImpl() {
		if (isTimeToShrink()) {
			if (isTimeToFirstShrink() || isTimeToNextShrink(getGlobalServerOptions().getGameCycleFrequency())) {
				for (Slot s : lstSlot) {
					s.ash();
				}

				for (Slot s : lstSlot) {
					s.movePosition();
				}

				for (Slot s : lstSlot) {
					s.fire();
				}

				if (nextWallLatency == 0) {
					lstSlot.add(new Slot(lstSlot.size()));
					nextWallLatency = BouncingFireShrinkPerformer.this.getWidth() * 2;
				} else {
					nextWallLatency--;
				}
				setLastShrinkOperationAt();
			}
		}
	}

	private class Slot {

		private int      xMove;
		private int      yMove;
		private Position position;

		private Slot(int index) {
			this.position = new Position(1 + index % (BouncingFireShrinkPerformer.this.getWidth() - 2), 1);
			this.xMove = 1;
			this.yMove = 1;
		}

		private void movePosition() {
			if (xMove < 0) {
				if (position.getX() == 1) {
					xMove = 1;
				}
			} else {
				if (position.getX() == BouncingFireShrinkPerformer.this.getWidth() - 1 - getWidth()) {
					xMove = -1;
				}
			}
			if (yMove < 0) {
				if (position.getY() == 1) {
					yMove = 1;
				}
			} else {
				if (position.getY() == BouncingFireShrinkPerformer.this.getHeight() - 1 - getHeight()) {
					yMove = -1;
				}
			}

			position.setX(position.getX() + xMove);
			position.setY(position.getY() + yMove);
		}

		private void ash() {
			removeFire(position.getX(), position.getY());
			removeFire(position.getX() + 1, position.getY());
			removeFire(position.getX(), position.getY() + 1);
			removeFire(position.getX() + 1, position.getY() + 1);
		}

		private void fire() {
			addFire(position.getX(), position.getY(), FireShapes.CROSSING);
			addFire(position.getX() + 1, position.getY(), FireShapes.CROSSING);
			addFire(position.getX(), position.getY() + 1, FireShapes.CROSSING);
			addFire(position.getX() + 1, position.getY() + 1, FireShapes.CROSSING);
		}

		private int getWidth() {
			return 2;
		}

		private int getHeight() {
			return 2;
		}
	}
}
