package util;

import java.util.Random;

public class RandomEngine {

	Random random;
	public RandomEngine(int seed) {
		this.random = new Random(seed);
	}
	
	public boolean getRandomBoolean() {
		return this.random.nextBoolean();
	}
	
	public int getRandomIntInRange(int start, int end) {
		//Random int in range [start, end)
		return start + random.nextInt(end-start);
	}
}
