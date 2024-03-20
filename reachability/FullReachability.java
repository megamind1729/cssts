package reachability;

import util.Pair;


public interface FullReachability extends IncrementalReachability {
	
	public boolean deleteEdge(Pair<Integer, Integer> from, Pair<Integer, Integer> to);
}
