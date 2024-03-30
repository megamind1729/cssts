package example;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import cssts.CSSTs;
import util.Pair;
import util.RandomEngine;

public class ConcurrencyAnalysisFullyDynamic {
	
	private int[] chainLengths;
	private CSSTs partialOrder;
	private RandomEngine randomEngine;
	Set<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> insertedEdges;
	
	private static int SEED = 100;
	 
	public ConcurrencyAnalysisFullyDynamic(int[] chainLengths) {
		this.chainLengths = Arrays.copyOf(chainLengths, chainLengths.length);
		this.partialOrder = new CSSTs(this.chainLengths);
		this.randomEngine = new RandomEngine(SEED);
		this.insertedEdges = new HashSet<>();
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
	
	private boolean shouldDeleteEdges(Pair<Integer, Integer> event) {
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
					insertedEdges.add(new Pair<>(currentEvent, conflictingEvent));
				}
			}
		} 
		
		if (shouldDeleteEdges(currentEvent)) {
			for (Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> eventPair : findRemoveEdges(currentEvent)) {
				System.out.println(String.format("\tDeleting the edge %s -> %s", eventPair.x, eventPair.y));
				this.partialOrder.deleteEdge(eventPair.x, eventPair.y);
			}
		}
		
		System.out.println("");
	}
	
	private Set<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> findRemoveEdges(Pair<Integer, Integer> event) {
		Set<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> removeEdges = new HashSet<>();
		
		if (this.insertedEdges.size() == 0)
			return removeEdges;
		
		do {
			int index = this.randomEngine.getRandomIntInRange(0, this.insertedEdges.size());
			Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> removedEdge = null;
			for(Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> eventPair : insertedEdges) {
			    if (index == 0) {
			    	removeEdges.add(eventPair);
			    	removedEdge = eventPair;
			    	break;
			    }
			    index--;
			}
			insertedEdges.remove(removedEdge);
		} while(this.randomEngine.getRandomBoolean() && this.insertedEdges.size() > 0);

		return removeEdges;
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
		ConcurrencyAnalysisFullyDynamic analysis = new ConcurrencyAnalysisFullyDynamic(chainLengths);
		analysis.doAnalysis();
	}
}
