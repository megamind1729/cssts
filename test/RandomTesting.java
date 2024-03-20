package test;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import util.Pair;


public class RandomTesting {

	public enum Operation {
		  INSERT_EDGE,
		  SUCCESSOR,
		  PREDECESSOR,
		  REACHABLE
	};
	
	private class OperationLimit {
		Operation op;
		int numPerformed;
		int limit;
		
		OperationLimit(Operation op, int numPerformed, int limit) {
			this.op = op;
			this.numPerformed = numPerformed;
			this.limit = limit;
		}
		
		public boolean isFinished() {
			return this.limit < this.numPerformed;
		}
		
		public String toString() {
			return "op: " + op + ", #performed ops: " + this.numPerformed + ", limit: " + this.limit;
		}
	}
	
	private static class Action implements Serializable {
		Operation op;
		Pair<Integer, Integer> source, target;

		Action(Operation op, Pair<Integer, Integer> source,
			   Pair<Integer, Integer> target) {
			this.op = op;
			this.source = source;
			this.target = target;
		}
	}
	
	
	private static int SEED = 100;
	private Random randomGenerator;
	private OperationLimit opCounters[];
	private PartialOrder po;
	private int numEvents, numThreads, numUpdates, numQuery;
	
	public RandomTesting(int numThreads, int numEvents, int numUpdates, int numQuery) {
		this.numThreads = numThreads;
		this.numEvents = numEvents;
		this.numUpdates = numUpdates;
		this.numQuery = numQuery;
		
		this.randomGenerator = new Random(SEED);
	
		opCounters = new OperationLimit[Operation.values().length];
		int counter = 0;
		for (Operation op : Operation.values()) {
			if (op == Operation.INSERT_EDGE)
				opCounters[counter++] = new OperationLimit(op, 0, numUpdates);
			 else 
				opCounters[counter++] = new OperationLimit(op, 0, numQuery);
		}
		
		int lengths[] = new int[numThreads];
		for (int i = 0; i < numThreads; i++)
			lengths[i] = numEvents;
		
		this.po = new PartialOrder(lengths);
	}
	
	private int getRandomInt(int startRange, int endRange) {
		return startRange + randomGenerator.nextInt(endRange-startRange);
	}
	
	private List<Integer> getUnfinishedOpIndexes() {
		List<Integer> indexes = new LinkedList<Integer>();
		for (int i = 0; i < opCounters.length; i++) {
			if (!opCounters[i].isFinished())
				indexes.add(i);
		}
		return indexes;
	}
	
	private Operation getNextOp() {
		List<Integer> indexes = getUnfinishedOpIndexes();
		if (indexes.size() == 0)
			return null;
		else {
			int random = this.getRandomInt(0, indexes.size());
			opCounters[indexes.get(random)].numPerformed++;
			return opCounters[indexes.get(random)].op;
		}
	}
	
	
	private Pair<Integer, Integer> getNextSource(boolean isSkewed) {
		int thread = getRandomInt(0, this.numThreads);
		
		int index = getRandomInt(0, this.numEvents);

		return new Pair<Integer, Integer>(thread, index);
	}
	
	private Pair<Integer, Integer> getNextTarget(int sourceThread, int sourceIndex, boolean isSkewed) {
		int thread = getRandomInt(0, this.numThreads);
		while (thread == sourceThread)
			thread = getRandomInt(0, this.numThreads);
		
		int index = getRandomInt(0, this.numEvents);

		return new Pair<Integer, Integer>(thread, index);
	}
	

	private void performAction(Action action) {
		if (action.op == Operation.INSERT_EDGE) {
			int targetSourceSuc = this.po.getSuccessor(action.target, action.source.x);
			boolean formsCycle = targetSourceSuc > action.source.y || targetSourceSuc == -1 ? false : true;
			if (!formsCycle) {
				this.po.addEdge(action.source, action.target);
			}
				
		} else if (action.op == Operation.REACHABLE) {
			this.po.existsEdge(action.source, action.target);
		} else if (action.op == Operation.SUCCESSOR) {
			this.po.getSuccessor(action.source, action.target.x);
		} else if (action.op == Operation.PREDECESSOR) {
			this.po.getPredecessor(action.source, action.target.x);
		}
	}
	
	public void test() {
		while (true) {
			Operation op = getNextOp();
			Pair<Integer, Integer> source, target;
			
			if (op == null)
				break;
			else if (op == Operation.INSERT_EDGE) {
				source = getNextSource(false);
				target = getNextTarget(source.x, source.y, false);
			} else {
				source = getNextSource(false);
				target = getNextTarget(source.x, source.y, false);
			}
			
			this.performAction(new Action(op, source, target));
		}
		
		System.out.println("Matching results: " + this.po.resultMatchMap);
	}

}
