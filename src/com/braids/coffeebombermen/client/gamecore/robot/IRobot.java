package com.braids.coffeebombermen.client.gamecore.robot;

import com.braids.coffeebombermen.client.gamecore.robot.SimpleRobot.AStarNode;

public interface IRobot {

	String getNextAction();

	public AStarNode[][] getLastNodes();

	void initForNextRound();

}
