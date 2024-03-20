package example;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import cssts.IncrementalCSSTs;
import util.Pair;
import util.RandomEngine;

public class IncrementalAnalysis {
	
	private int[] chainLengths;
	private IncrementalCSSTs partialOrder;
	private RandomEngine randomEngine;
	
	private static int SEED = 100;
	 
	public IncrementalAnalysis(int[] chainLengths) {
		this.chainLengths = Arrays.copyOf(chainLengths, chainLengths.length);
		this.partialOrder = new IncrementalCSSTs(this.chainLengths);
		this.randomEngine = new RandomEngine(SEED);
	}
		
	private int getNumThreads() {
		return this.chainLengths.length;
	}
	
	private int getNumEventsOfThread(int threadId) {
		if (threadId >= this.chainLengths.length)
           throw new IllegalArgumentException("Invalid thread id!");
		return this.chainLengths[threadId];
	}
	
	private boolean shouldAddEdges(Pair<Integer, Integer> event) {
	     return this.randomEngine.getRandomBoolean();
	}
	
	public void doAnalysis() {
		System.out.println("Performing analysis\n");
		for (int threadId = 0; threadId < this.getNumThreads(); threadId++) {
			for (int eventId = 0; eventId < this.getNumEventsOfThread(threadId); eventId++) {
				this.processEvent(new Pair<>(threadId, eventId));
			}
		}
	}
	
	void processEvent(Pair<Integer, Integer> currentEvent) {
		System.out.println("Processing the event: " + currentEvent);
		if (shouldAddEdges(currentEvent)) {
			for (Pair<Integer, Integer> conflictingEvent : findConflictingEvents(currentEvent)) {
				
				if (!this.partialOrder.reachable(conflictingEvent, currentEvent)) {
					// Ensure that the new edge will not form a cycle
					
					System.out.println(String.format("\tInserting the edge %s -> %s", currentEvent, conflictingEvent));
					this.partialOrder.insertEdge(currentEvent, conflictingEvent);
				}
			}
		}
		System.out.println("");
	}
	
	private Set<Pair<Integer, Integer>> findConflictingEvents(Pair<Integer, Integer> event) {
		Set<Pair<Integer, Integer>> conflictingEvents = new HashSet<>();
		
		do {
			int conflictingThread;
			do {
				conflictingThread = randomEngine.getRandomIntInRange(0, this.getNumThreads());
			} while(conflictingThread == event.x);
			
			// Perform queries on the partial order to determine which node to add edge
			int pred = this.partialOrder.getPredecessor(event, conflictingThread);
			int succ = this.partialOrder.getSuccessor(event, conflictingThread);
			
			succ = succ == -1 ? this.getNumEventsOfThread(conflictingThread)-1 : succ;
			pred = pred == -1 ? 0 : pred;
			int conflictingIndex = succ;
			if (succ != pred)
				conflictingIndex = this.randomEngine.getRandomIntInRange(pred, succ);
			conflictingEvents.add(new Pair<>(conflictingThread, conflictingIndex));
		} while(this.randomEngine.getRandomBoolean());

		return conflictingEvents;
	}
	
	
	public static void main(String[] args) {
		int [] chainLengths = {100, 100, 100, 100, 100};
		IncrementalAnalysis analysis = new IncrementalAnalysis(chainLengths);
		analysis.doAnalysis();
	}
}
