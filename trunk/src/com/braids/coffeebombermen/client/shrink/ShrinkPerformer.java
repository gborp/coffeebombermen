package classes.client.shrink;

import classes.options.Shrinkers;

public interface ShrinkPerformer {

	Shrinkers getType();

	void initNextRound();

	void nextIteration();
}
