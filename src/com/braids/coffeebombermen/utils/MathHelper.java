package com.braids.coffeebombermen.utils;

import java.util.Random;

import com.braids.coffeebombermen.client.gamecore.Directions;

public class MathHelper {

	private static final float HALF = 0.5f;
	private static Random      random;

	public static int randomInt(int max) {
		return (int) Math.floor(random.nextDouble() * (max + 1));
	}

	public static int randomInt(int min, int max) {
		return (int) Math.floor(random.nextDouble() * (max - min + 1)) + min;
	}

	public static boolean randomBoolean() {
		return random.nextBoolean();
	}

	public static boolean checkRandomEvent(float possibility) {
		return random.nextFloat() < possibility;
	}

	public static double halfHasMoreChancePossibility() {
		// power: result has more chance to be near of 0 as near of 0.5;
		double dif = Math.pow(random.nextDouble(), 3) / 2;
		if (randomBoolean()) {
			dif = -1 * dif;
		}

		return HALF + dif;
	}

	public static Directions getRandomDirection() {
		return Directions.get(random.nextInt(Directions.values().length));
	}

	public static void setRandom(Random random) {
		MathHelper.random = random;
	}

	public static int getWeightedRandom(int[] arWeights) {
		int allWeight = 0;
		for (int w : arWeights) {
			allWeight += w;
		}

		int whichDisease = MathHelper.randomInt(allWeight);

		int i = 0;
		for (int w : arWeights) {
			whichDisease -= w;
			if (whichDisease <= 0) {
				return i;
			}
			i++;
		}
		throw new RuntimeException("This should never happen");
	}

}
