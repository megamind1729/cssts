package test;

public class Main {

	public static void main(String[] args) {
		int numThreads = 10;
		int numEvents = 100000;
		int numUpdates = 100000;
		int numQueries = 100000;
		
		RandomTesting test = new RandomTesting(numThreads, numEvents, numUpdates, numQueries);
		test.test();
	}

}
