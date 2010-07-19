package classes.utils;

public class MathHelper {

	private static final float HALF = 0.5f;

	public static int randomInt(int max) {
		return (int)Math.floor(Math.random()*( max + 1));
	}

	public static int randomInt(int min, int max) {
		return (int)Math.floor(Math.random()*(max - min + 1)) + min;
	}
	
	public static boolean randomBoolean() {
		return Math.random() < 0.5f;
	}
	
	public static boolean checkRandomEvent(float possibility) {
		return Math.random() < possibility;
	}

	public static double halfHasMoreChancePossibility() {
		// power: result has more chance to be near of 0 as near of 0.5;
		double dif = Math.pow(Math.random(), 3) / 2; 
		if (randomBoolean()) {
			dif = -1 * dif;
		}
		
		return HALF + dif;
	}

}
