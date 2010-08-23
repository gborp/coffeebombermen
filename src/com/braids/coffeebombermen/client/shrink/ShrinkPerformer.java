package com.braids.coffeebombermen.client.shrink;

import com.braids.coffeebombermen.options.Shrinkers;

public interface ShrinkPerformer {

	Shrinkers getType();

	void initNextRound();

	void nextIteration();
}
