package com.braids.coffeebombermen.client.gamecore.robot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Stack;

import com.braids.coffeebombermen.client.gamecore.Activities;
import com.braids.coffeebombermen.client.gamecore.BombPhases;
import com.braids.coffeebombermen.client.gamecore.CoreConsts;
import com.braids.coffeebombermen.client.gamecore.control.GameCoreHandler;
import com.braids.coffeebombermen.client.gamecore.model.BombModel;
import com.braids.coffeebombermen.client.gamecore.model.FireModel;
import com.braids.coffeebombermen.client.gamecore.model.PlayerModel;
import com.braids.coffeebombermen.client.gamecore.model.level.LevelComponent;
import com.braids.coffeebombermen.client.gamecore.model.level.LevelModel;
import com.braids.coffeebombermen.options.OptConsts.Items;
import com.braids.coffeebombermen.options.OptConsts.Walls;

public class SimpleRobot implements IRobot {

	private static int      COST_FIRE      = 50;
	private static int      COST_FIRE_LINE = 5;

	private GameCoreHandler gameCoreHandler;
	private int             index;
	private PlayerModel     playerModel;

	private AStarNode[][]   lastNodes;
	private AStarPath       lastPath;
	private String          lastKey;
	private boolean         lastBomb;
	private boolean         hasOwnBomb;
	private boolean         startNextRound;

	public SimpleRobot(GameCoreHandler gameCoreHandler, int index, PlayerModel playerModel) {
		this.gameCoreHandler = gameCoreHandler;
		this.index = index;
		this.playerModel = playerModel;
	}

	public AStarNode[][] getLastNodes() {
		return lastNodes;
	}

	public int getComponentCenterPos(int pos) {
		return pos / CoreConsts.LEVEL_COMPONENT_GRANULARITY;
		// return (pos - (CoreConsts.LEVEL_COMPONENT_GRANULARITY / 2))
		// / CoreConsts.LEVEL_COMPONENT_GRANULARITY;
	}

	/*
	 * return String + Space + Player index + key {0 - up, 1 - down, 2 - right,
	 * 3 - left, 4 - bomb, 5 - shift} + {p - pressed, r - released}}
	 */
	@Override
	public String getNextAction() {

		if (!gameCoreHandler.getHasMoreThanOneAlivePlayer()) {
			return "";
		}

		if (Activities.DYING.equals(playerModel.getActivity())) {
			return "";
		}

		// get item targets
		PriorityQueue<AStarNode> pqTarget1 = new PriorityQueue<AStarNode>(20, new AStarNodeComparator());
		// get empty target
		PriorityQueue<AStarNode> pqTarget2 = new PriorityQueue<AStarNode>(20, new AStarNodeComparator());

		hasOwnBomb = false;
		lastNodes = getAStarNode(pqTarget1, pqTarget2);

		int x = getComponentCenterPos(playerModel.getPosX());
		int y = getComponentCenterPos(playerModel.getPosY());
		AStarNode source = lastNodes[x][y];

		AStarNode target = pqTarget1.poll();
		startNextRound = false;
		boolean fromLastPath = false;

		// search a target where I find an item
		AStarPath path = null;
		while ((path == null || !path.isSafe()) && target != null) {
			path = searchPath(lastNodes, source, target);
			pqTarget1.remove(target);
			target = pqTarget1.poll();
		}

		// I am in safe and I can blow up a wall
		if (path == null || !path.isSafe()) {
			if (source.isInSafe() && hasOwnBomb) {
				boolean canBlowUpAWall = false;
				List<AStarNode> neighbors = getNeighbors(lastNodes, source);
				for (AStarNode neighbor : neighbors) {
					if (neighbor.isBrick) {
						canBlowUpAWall = true;
						break;
					}
				}
				if (canBlowUpAWall) {
					path = new AStarPath();
					path.addNode(source);
				}

			}
		}

		// previous target
		if (path == null || !path.isSafe()) {
			if (lastPath != null && (!lastPath.getTarget().equals(source))) {
				AStarPath pathPrevious = searchPath(lastNodes, source, lastPath.getTarget());
				if (pathPrevious != null && pathPrevious.isSafe()) {
					path = pathPrevious;
					fromLastPath = true;
				}
			}
		}

		// get target where I am in safe
		if (path == null || !path.isSafe()) {

			// search target where I can blow up a wall
			AStarPath path2 = null;

			// search target where I'm in safe
			List<AStarPath> lstSafePath1 = new ArrayList<AStarPath>();
			List<AStarPath> lstSafePath2 = new ArrayList<AStarPath>();

			pqTarget2.remove(source);
			target = pqTarget2.poll();
			while (target != null) {
				path2 = searchPath(lastNodes, source, target);
				if (path2 != null) {
					if ((target.x != source.x) && (target.y != source.y)) {

						if (path2.isSafe()) {
							lstSafePath1.add(path2);
						}

						boolean canBlowUpAWall = false;
						List<AStarNode> neighbors = getNeighbors(lastNodes, target);
						for (AStarNode neighbor : neighbors) {
							if (neighbor.isBrick) {
								canBlowUpAWall = true;
								break;
							}
						}
						if (canBlowUpAWall) {
							if (path2.isSafe()) {
								path = path2;
								break;
							} else if (!path2.goAcrossFire) {
								lstSafePath2.add(0, path2);
							}
						} else if (!path2.goAcrossFire) {
							lstSafePath2.add(path2);
						}

					} else {
						if (!path2.goAcrossFire) {
							lstSafePath2.add(path2);
						}
					}
				}
				pqTarget2.remove(target);
				target = pqTarget2.poll();
			}

			if (path == null) {
				if (lstSafePath1.size() > 0) {
					path = lstSafePath1.get(0);
				} else if (lstSafePath2.size() > 0) {
					if (hasOwnBomb) {
						if (!source.isInSafe()) {
							path = lstSafePath2.get(0);
							// } else {
							// // previous target
							// if (lastPath != null) {
							// path2 = searchPath(lastNodes, source, lastPath
							// .getTarget());
							// if (path2 != null && path2.isSafe()) {
							// path = path2;
							// fromLastPath = true;
							// }
							// }
						}
					} else if ((!source.isInSafe()) || lstSafePath2.get(0).isSafe()) {
						path = lstSafePath2.get(0);
					}
				}
			}

		}

		if (!fromLastPath) {
			lastPath = path;
		}

		ArrayList<String> commands = new ArrayList<String>(5);
		if (lastBomb) {
			// release bomb
			lastBomb = false;
			commands.add(" " + index + " 4 r");
		}

		if (path != null) {
			if (path.size() > 1) {
				target = path.getTarget();
				if (!lastBomb) {
					if ((path.isSafe()) && (!hasOwnBomb) && (target.x != source.x) && (target.y != source.y)) {
						// press bomb
						lastBomb = true;
						commands.add(" " + index + " 4 p");
						lastPath = null;
					} else {

						// TODO maybe not handle if we send 3 command
						String key = getKey(path.get(0), path.get(1));
						if (key == null) {
							if (lastKey != null) {
								commands.add(" " + index + " " + lastKey + " r");
							}
						} else {
							if (lastKey != null && (!lastKey.equals(key))) {
								commands.add(" " + index + " " + lastKey + " r");
								commands.add(" " + index + " " + key + " p");
							} else {
								commands.add(" " + index + " " + key + " p");
							}
						}
						lastKey = key;
					}
				}
			} else if (lastKey != null) {
				commands.add(" " + index + " " + lastKey + " r");
				lastKey = null;
			}
		} else if (lastKey != null) {
			commands.add(" " + index + " " + lastKey + " r");
			lastKey = null;
		}

		String result = "";
		for (String cmd : commands) {
			result += cmd;
		}

		if (result != "") {
			System.out.println("source: " + source.toString() + " / target: " + (target == null ? "" : target.toString()) + " / result: " + result);
		}
		return result;
	}

	/*
	 * return key {0 - up, 1 - down, 2 - right, 3 - left, 4 - bomb, 5 - shift}
	 */
	private String getKey(AStarNode source, AStarNode target) {
		if (source.x == target.x) {
			if (source.y == target.y) {
				return null;
			} else if (source.y < target.y) {
				return "1";
			} else {
				return "0";
			}
		} else if (source.x < target.x) {
			return "2";
		} else {
			return "3";
		}

	}

	private static double calcManhattanDistance(AStarNode node1, AStarNode node2) {
		return calcManhattanDistance(node1.x, node1.y, node2.x, node2.y);
	}

	private static double calcManhattanDistance(int x1, int y1, int x2, int y2) {
		return Math.abs(x1 - x2) + Math.abs(y1 - y2);
	}

	private AStarNode[][] getAStarNode(PriorityQueue<AStarNode> pqTarget1, PriorityQueue<AStarNode> pqTarget2) {
		LevelModel levelModel = gameCoreHandler.getLevelModel();

		int height = levelModel.getHeight();
		int width = levelModel.getWidth();

		AStarNode[][] result = new AStarNode[width][height];

		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				final LevelComponent levelComponent = levelModel.getComponent(j, i);

				AStarNode node = new AStarNode(j, i);
				result[j][i] = node;

				Walls wall = levelComponent.getWall();
				Items item = levelComponent.getItem();
				if (!levelComponent.hasFire()) {
					if (wall == Walls.EMPTY && item != null) {
						// item
						node.isItem = true;
						switch (item) {
							case BOMB:
							case BOMB_SPRINKLE:
							case BLUE_GLOVES:
							case BOOTS:
							case BOXING_GLOVES:
							case CRAZY_BOOTS:
								// case DISEASE:
							case FIRE:
							case HEART:
							case JELLY:
							case ROLLER_SKATES:
							case SPIDER_BOMB:
								// case SUPER_DISEASE:
							case SUPER_FIRE:
							case SWAP_TELEPORT:
							case TRIGGER:
							case WALL_BUILDING:
							case WALL_CLIMBING:
								pqTarget1.add(node);
								break;
							default:
								break;
						}
					} else {
						// wall
						if (!wall.equals(Walls.EMPTY)) {
							node.isWall = true;
							node.isBrick = Walls.BRICK.equals(wall);
						} else {
							pqTarget2.add(node);
						}
					}
				} else {
					FireModel fireModel = levelComponent.getLastFire();
					if (playerModel.equals(fireModel.getOwnerPlayer())) {
						hasOwnBomb = true;
					}
					if (wall == Walls.EMPTY && item == null) {
						// fire
						node.isInFire = true;
						node.cost = COST_FIRE;
					} else {
						// fired wall
						node.isInFire = true;
						node.cost = COST_FIRE;
					}
				}

			}
		}

		// gameCoreHandler.isBombAtComponentPosition
		final List<BombModel> bombModels = gameCoreHandler.getBombModels();
		if (bombModels != null) {
			for (int b = 0; b < bombModels.size(); b++) {
				// Easy with the enhanced for: modifying is possible during a
				// paint()
				final BombModel bombModel = bombModels.get(b);
				if (playerModel.equals(bombModel.getOwnerPlayer())) {
					hasOwnBomb = true;
				}

				if (bombModel.getPhase() == BombPhases.ROLLING || (bombModel.getPhase() == BombPhases.STANDING)) {

					// AStarNode node =
					// result[bombModel.getComponentCenterPosX()][bombModel.getComponentCenterPosY()];
					// node.cost = 5;
					int posX = getComponentCenterPos(bombModel.getPosX());
					int posY = getComponentCenterPos(bombModel.getPosY());
					for (int y = posY; y >= 0; y--) {
						AStarNode nodeY = result[posX][y];
						nodeY.isInFireLine = true;
						nodeY.cost += COST_FIRE_LINE;
						pqTarget2.remove(nodeY);
						if (nodeY.isWall || nodeY.isItem) {
							break;
						}
					}
					for (int y = posY; y < height; y++) {
						AStarNode nodeY = result[posX][y];
						nodeY.isInFireLine = true;
						nodeY.cost += COST_FIRE_LINE;
						pqTarget2.remove(nodeY);
						if (nodeY.isWall || nodeY.isItem) {
							break;
						}
					}
					for (int x = posX; x >= 0; x--) {
						AStarNode nodeX = result[x][posY];
						nodeX.isInFireLine = true;
						nodeX.cost += COST_FIRE_LINE;
						pqTarget2.remove(nodeX);
						if (nodeX.isWall || nodeX.isItem) {
							break;
						}
					}
					for (int x = posX; x < width; x++) {
						AStarNode nodeX = result[x][posY];
						nodeX.isInFireLine = true;
						nodeX.cost += COST_FIRE_LINE;
						pqTarget2.remove(nodeX);
						if (nodeX.isWall || nodeX.isItem) {
							break;
						}
					}

				}
			}
		}

		return result;
	}

	public class AStarPath {

		List<AStarNode> lstNode;
		AStarNode       target;
		int             cost;
		boolean         goAcrossFire;
		boolean         goAcrossFireLine;

		public AStarPath() {
			super();
			lstNode = new ArrayList<AStarNode>();
			cost = 0;
		}

		public AStarNode get(int i) {
			return lstNode.get(i);
		}

		public int size() {
			return lstNode.size();
		}

		public void addNode(AStarNode node) {
			lstNode.add(node);
			target = node;
			cost += node.cost;
			if (size() > 1) {
				goAcrossFire |= node.isInFire;
				goAcrossFireLine |= node.isInFireLine;
			}
		}

		public AStarNode getTarget() {
			return target;
		}

		public boolean isSafe() {
			return !(goAcrossFire || goAcrossFireLine);
		}
	}

	public class AStarNode {

		private int       x;
		private int       y;
		private double    cost;
		private boolean   isItem;
		private boolean   isWall;
		private boolean   isBrick;
		private boolean   isInFire;
		private boolean   isInFireLine;

		// used to construct the path after the search is done
		private AStarNode cameFrom;

		// Distance from source along optimal path
		private double    g;

		// Heuristic estimate of distance from the current node to the target
		// node
		private double    h;

		public AStarNode(int posX, int posY) {
			this.x = posX;
			this.y = posY;
			this.cost = 1;
			this.g = 0;
			this.h = 0;
		}

		public boolean isInSafe() {
			return (!isInFire) && (!isInFireLine);
		}

		public double getF() {
			return g + h + cost;
		}

		public String getId() {
			return x + "_" + y;
		}

		public String toString() {
			return x + "," + y;
		}

		public boolean equals(Object obj) {
			if (obj instanceof AStarNode) {
				AStarNode node = (AStarNode) obj;
				return (node.x == this.x) && (node.y == this.y);
			}
			return false;
		}

	}

	public static class AStarNodeComparator implements Comparator<AStarNode> {

		public int compare(AStarNode first, AStarNode second) {
			if (first.getF() < second.getF()) {
				return -1;
			} else if (first.getF() > second.getF()) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	// AStarAlgorithm
	public AStarPath searchPath(AStarNode[][] nodes, AStarNode source, AStarNode target) {

		Map<String, AStarNode> openSet = new HashMap<String, AStarNode>();
		PriorityQueue<AStarNode> pQueue = new PriorityQueue<AStarNode>(20, new AStarNodeComparator());
		Map<String, AStarNode> closeSet = new HashMap<String, AStarNode>();

		openSet.put(source.getId(), source);
		pQueue.add(source);

		AStarNode goal = null;
		while (openSet.size() > 0) {
			AStarNode x = pQueue.poll();
			openSet.remove(x.getId());
			if (x.getId().equals(target.getId())) {
				// found
				goal = x;
				break;
			} else {
				closeSet.put(x.getId(), x);
				List<AStarNode> neighbors = getNeighbors(nodes, x);
				for (AStarNode neighbor : neighbors) {
					AStarNode visited = closeSet.get(neighbor.getId());
					if (visited == null && !neighbor.isWall) {
						double g = x.g + calcManhattanDistance(x, neighbor);
						AStarNode n = openSet.get(neighbor.getId());

						if (n == null) {
							// not in the open set
							n = neighbor;
							n.cameFrom = x;
							n.g = g;
							openSet.put(neighbor.getId(), n);
							pQueue.add(n);
						} else if (g < n.g) {
							// Have a better route to the current node,
							// change its parent
							n.cameFrom = x;
							n.g = g;
							n.h = calcManhattanDistance(neighbor, target);
						}
					}
				}
			}
		}

		// after found the target, start to construct the path
		if (goal != null) {
			Stack<AStarNode> stack = new Stack<AStarNode>();
			AStarPath path = new AStarPath();
			stack.push(goal);
			AStarNode parent = goal.cameFrom;
			while (parent != null) {
				stack.push(parent);
				parent = parent.cameFrom;
			}
			while (stack.size() > 0) {
				path.addNode(stack.pop());
			}
			return path;
		}

		return null;
	}

	private static List<AStarNode> getNeighbors(AStarNode[][] nodes, AStarNode node) {
		List<AStarNode> result = new ArrayList<AStarNode>();

		if (node.x > 0) {
			result.add(nodes[node.x - 1][node.y]);
		}
		if (node.x < nodes.length - 1) {
			result.add(nodes[node.x + 1][node.y]);
		}
		if (node.y > 0) {
			result.add(nodes[node.x][node.y - 1]);
		}
		if (node.y < nodes[node.x].length - 1) {
			result.add(nodes[node.x][node.y + 1]);
		}

		return result;
	}

	public void initForNextRound() {
		this.startNextRound = true;
		// lastKey = null;
	}
}
