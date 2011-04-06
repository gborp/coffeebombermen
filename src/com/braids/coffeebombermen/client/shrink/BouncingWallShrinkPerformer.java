package com.braids.coffeebombermen.client.shrink;

import java.util.ArrayList;

import com.braids.coffeebombermen.client.gamecore.control.GameCoreHandler;
import com.braids.coffeebombermen.options.OptConsts.Walls;
import com.braids.coffeebombermen.options.Shrinkers;
import com.braids.coffeebombermen.utils.Position;

public class BouncingWallShrinkPerformer extends AbstractShrinkPerformer {
	
	private ArrayList<Slot> lstSlot;
	private int nextWallLatency;

	public BouncingWallShrinkPerformer(GameCoreHandler gameCoreHandler) {
		super(gameCoreHandler);
	}

	public Shrinkers getType() {
		return Shrinkers.BouncingWall;
	}

	protected void initNextRoundImpl() {
		lstSlot = new ArrayList<Slot>();
	}

	protected void nextIterationImpl() {
		if (isTimeToShrink()) {
			if (isTimeToFirstShrink() || isTimeToNextShrink(getGlobalServerOptions().getGameCycleFrequency())) {
				for (Slot s : lstSlot) {
					s.draw(Walls.EMPTY);
				}
				
				for (Slot s : lstSlot) {
					s.movePosition();
				}
				
				for (Slot s : lstSlot) {
					s.draw(Walls.DEATH);
				}
				

				if (nextWallLatency == 0) {
					lstSlot.add(new Slot(lstSlot.size()));
					nextWallLatency = BouncingWallShrinkPerformer.this.getWidth() / 3;
				} else {
					nextWallLatency--;
				}
				setLastShrinkOperationAt();
			}
		}
	}
	
	private class Slot {
		private int xMove;
		private int yMove;
		private Position position;

		private Slot(int index){
			this.position = new Position(1 + index % (BouncingWallShrinkPerformer.this.getWidth() - 2) , 1);
			this.xMove = 1;
			this.yMove = 1;

			draw(Walls.CONCRETE);
		}

		private void movePosition() {
			if (xMove < 0) {
				if (position.getX() == 1) {
					xMove = 1;
				}
			} else {
				if (position.getX() == BouncingWallShrinkPerformer.this.getWidth() - 1 - getWidth()) {
					xMove = -1;
				}
			}
			if (yMove < 0) {
				if (position.getY() == 1) {
					yMove = 1;
				}
			} else {
				if (position.getY() == BouncingWallShrinkPerformer.this.getHeight() - 1 - getHeight()) {
					yMove = -1;
				}
			}
			
			position.setX(position.getX() + xMove);
			position.setY(position.getY() + yMove);
		}

		private void draw(Walls wall) {
			addWall(position.getX(), position.getY(), wall);
			addWall(position.getX() + 1, position.getY(), wall);
			addWall(position.getX(), position.getY() + 1, wall);
			addWall(position.getX() + 1, position.getY() + 1, wall);
		}

		private int getWidth() {
			return 2;
		}

		private int getHeight() {
			return 2;
		}
	}
}
