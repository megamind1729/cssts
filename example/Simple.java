package example;

import java.util.Arrays;

import cssts.IncrementalCSSTs;
import util.Pair;

public class Simple {

	private int[] chainLengths;
	private IncrementalCSSTs partialOrder;

	public Simple(int chainLengths[]) {
		this.chainLengths = Arrays.copyOf(chainLengths, chainLengths.length);
		this.partialOrder = new IncrementalCSSTs(this.chainLengths);
	}
	
	private Pair<Integer, Integer> getNode(int chainId, int nodeId) {
		if (chainId >= this.chainLengths.length)
			 throw new IllegalArgumentException(String.format("Invalid chain id %d!", chainId));
		if (nodeId >= this.chainLengths[chainId])
			throw new IllegalArgumentException(String.format("Invalid node id %d! Chain %d contains %d nodes.", 
															 nodeId, chainId, this.chainLengths[chainId]));	
		
		return new Pair<>(chainId, nodeId);
	}
	
	private void insertEdge(Pair<Integer, Integer> fromNode, Pair<Integer, Integer> toNode) {
		this.partialOrder.insertEdge(fromNode, toNode);
		System.out.println(String.format("\nInserted edge: %s -> %s", fromNode, toNode));
	}
	
	private int getSuccessor(Pair<Integer, Integer> fromNode, int chainId) {
		int result = this.partialOrder.getSuccessor(fromNode, chainId);
		System.out.println(String.format("Earliest successor of %s in chain %d: %d", fromNode, chainId, result));
		return result;
	}
	
	private int getPredecessor(Pair<Integer, Integer> fromNode, int chainId) {
		int result = this.partialOrder.getPredecessor(fromNode, chainId);
		System.out.println(String.format("Latest predecessor of %s in chain %d: %d", fromNode, chainId, result));
		return result;
	}
	
	private boolean reachable(Pair<Integer, Integer> fromNode, Pair<Integer, Integer> toNode) {
		boolean result = this.partialOrder.reachable(fromNode, toNode);
		System.out.println(String.format("Reachable %s ->* %s: %b", fromNode, toNode, result));
		return result;
	}
	
	public static void main(String[] args) {
		int [] chainLengths = {100, 200, 300};
		Simple g = new Simple(chainLengths);
		
		g.reachable(g.getNode(0, 5), g.getNode(2, 200));
		g.reachable(g.getNode(0, 15), g.getNode(2, 200));
		
		g.insertEdge(g.getNode(0, 10), g.getNode(2, 110));
		g.getSuccessor(g.getNode(0, 10), 2);
		g.getPredecessor(g.getNode(2, 105), 0);
		
		g.insertEdge(g.getNode(0, 10), g.getNode(1, 30));
		g.getSuccessor(g.getNode(0, 10), 2);
		
		g.insertEdge(g.getNode(1, 50), g.getNode(2, 100));
		g.getSuccessor(g.getNode(0, 10), 2);
		g.getPredecessor(g.getNode(2, 105), 0);
		
		g.reachable(g.getNode(0, 5), g.getNode(2, 200));
		g.reachable(g.getNode(0, 15), g.getNode(2, 200));
	}
}
