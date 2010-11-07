package com.braids.coffeebombermen.client.shrink;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.braids.coffeebombermen.client.gamecore.FireShapes;
import com.braids.coffeebombermen.client.gamecore.control.Fire;
import com.braids.coffeebombermen.client.gamecore.control.GameCoreHandler;
import com.braids.coffeebombermen.client.gamecore.model.FireModel;
import com.braids.coffeebombermen.client.gamecore.model.PlayerModel;
import com.braids.coffeebombermen.options.OptConsts.Walls;
import com.braids.coffeebombermen.options.model.ServerOptions;
import com.braids.coffeebombermen.options.Shrinkers;
import com.braids.coffeebombermen.utils.Position;

public class DrawWallShrinkPerformer extends AbstractShrinkPerformer {
	private static final int        WAITING_FOR_PLAYER_TO_MOVE           = 30;

	
	private ArrayList<SimpleWallSlot> lstSimpleWall;

	public DrawWallShrinkPerformer(GameCoreHandler gameCoreHandler) {
		super(gameCoreHandler);
	}

	public Shrinkers getType() {
		return Shrinkers.DrawWall;
	}

	protected void initNextRoundImpl() {
		lstSimpleWall = new ArrayList<SimpleWallSlot>();
	}

	protected void nextIterationImpl() {
		if (isTimeToShrink()) {
			GameCoreHandler gch = getGameCoreHandler();
			if (isTimeToFirstShrink()) {
				// warn, remove walls except one per line. Its good for the gameplay too.
				for (int x = 1; x < getWidth() - 1; x++) {
					boolean concrateWallFound = false;
					for (int y = 0; y < getHeight() - 1; y++) {
						Walls wall = gch.getWall(x, y);
						if (Walls.CONCRETE.equals(wall)) {
							if (concrateWallFound) {
								addWall(x, y, Walls.EMPTY);
							} else {
								concrateWallFound = true;
							}
						} else if (Walls.BRICK.equals(wall)) {
							final Fire fire = new Fire(x, y, gch);
							final FireModel fireModel = fire.getModel();

							fireModel.setShape(FireShapes.CROSSING);
							fireModel.setOwnerPlayer(null);
							fireModel.setTriggererPlayer(null);
							gch.getLevel().addFireToComponentPos(fire, x, y);
						}
					}
				}
			}
			if (isTimeToStartReal()) {
				Iterator<SimpleWallSlot> si = lstSimpleWall.iterator();
				while (si.hasNext()) {
					SimpleWallSlot s = si.next();
					if (s.tick + WAITING_FOR_PLAYER_TO_MOVE == getTick()) {
						addDeathWall(s.p.getX(), s.p.getY());
						si.remove();
					}
				}

				List<PlayerModel[]> lstPlayer = gch.getClientsPlayerModels();
				for (PlayerModel[] ps : lstPlayer) {
					for (PlayerModel p : ps) {
						if (p.isAlive()) {
							Position pos = new Position(p.getComponentPosX(),
									p.getComponentPosY());
							// attila gch.getWall(pos.getX(), pos.getY()) ==
							// null
							// somehow without this check the death wall is
							// removed before checking the death of the player
							if (Walls.EMPTY.equals(gch.getWall(pos.getX(),
									pos.getY()))
									&& getSlotByPosition(pos) == null) {
								addWall(pos.getX(), pos.getY(), Walls.CONCRETE);
								lstSimpleWall.add(new SimpleWallSlot(pos,
										getTick()));
							}
						}
					}
				}
			}
			setLastShrinkOperationAt();
		}
	}
	
	private SimpleWallSlot getSlotByPosition(Position p) {
		for (SimpleWallSlot s : lstSimpleWall) {
			if (s.p.equals(p)) {
				return s;
			}
		}

		return null;
	}

	private boolean isTimeToStartReal() {
		ServerOptions gso = getGlobalServerOptions();
		return getTick() > gso.getRoundTimeLimit() * 1.2 * gso.getGameCycleFrequency();
	}
		
	private static class SimpleWallSlot {
		private final Position p;
		private final long tick;

		private SimpleWallSlot(Position p, long tick){
			this.p = p;
			this.tick = tick;
		}

	}
}
