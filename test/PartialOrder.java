package test;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cssts.CSSTs;
import cssts.IncrementalCSSTs;
import reachability.IncrementalReachability;
import sts.STs;
import util.Pair;


public class PartialOrder {
	private CSSTs cssts;
	private IncrementalCSSTs incCSSTs;
	private STs sts;
	public Set<IncrementalReachability> reachabilityEngines;
	public Map<String, Integer> resultMatchMap;
	
	private static boolean VERBOSE = false;
	
	public PartialOrder(int[] lengths) {
		sts = new STs(lengths);
		cssts = new CSSTs(lengths);
		incCSSTs = new IncrementalCSSTs(lengths);
		
		reachabilityEngines = new HashSet<>();
		resultMatchMap = new HashMap<>();
		reachabilityEngines.add(cssts);
		reachabilityEngines.add(sts);
		reachabilityEngines.add(incCSSTs);
	}
	
	private Object checkResult(String method, Set<Pair<String, Object>> resultSet) {
		Object oldResult = null;
		for (Pair<String, Object> r : resultSet) {
			if (oldResult == null) {
				oldResult = r.y;
			} else {
				if (!oldResult.equals(r.y)) {
					System.out.println("Results do not match! method: " + method);
					System.out.println(resultSet);
					assert(false);
				}
			}
		}
		if (resultMatchMap.containsKey(method))
			resultMatchMap.put(method, resultMatchMap.get(method)+1);
		else 
			resultMatchMap.put(method, 1);

		return oldResult;
	}
	
	public int getSuccessor(Pair<Integer, Integer> p, int i) {
		if (VERBOSE) System.out.println(String.format("%s, p: %s, i: %d", "getSuccessor", p, i));
	
		Set<Pair<String, Object>> resultSet = new HashSet<>();
		for (IncrementalReachability e : this.reachabilityEngines) {
			resultSet.add(new Pair<>(e.getClass().getName(), e.getSuccessor(p, i)));
		}
		
		return (Integer) this.checkResult("getSuccessor", resultSet); 
	}

	public int getPredecessor(Pair<Integer, Integer> p, int i) {
		if (VERBOSE) System.out.println(String.format("%s, p: %s, i: %d", "getPredecessor", p, i));
		
		Set<Pair<String, Object>> resultSet = new HashSet<>();
		for (IncrementalReachability e : this.reachabilityEngines) {
			resultSet.add(new Pair<>(e.getClass().getName(), e.getPredecessor(p, i)));
		}
		
		return (Integer) this.checkResult("getPredecessor", resultSet);
	}

	public boolean unordered(Pair<Integer, Integer> u, Pair<Integer, Integer> v) {
		if (VERBOSE) System.out.println(String.format("%s, u: %s, v: %d", "unordered", u, v));
		
		Set<Pair<String, Object>> resultSet = new HashSet<>();
		for (IncrementalReachability e : this.reachabilityEngines) {
			resultSet.add(new Pair<>(e.getClass().getName(), e.unordered(u, v)));
		}
		
		return (Boolean) this.checkResult("unordered", resultSet);
	}

	public int[] getSuccessors(Pair<Integer, Integer> p) {
		if (VERBOSE) System.out.println(String.format("%s, p: %s", "getSuccessors", p));
		
		Set<Pair<String, Object>> resultSet = new HashSet<>();
		for (IncrementalReachability e : this.reachabilityEngines) {
			resultSet.add(new Pair<>(e.getClass().getName(), e.getSuccessors(p)));
		}
		this.checkResult("getSuccessors", resultSet);
		
		for (IncrementalReachability e : this.reachabilityEngines) {
			return e.getSuccessors(p);
		}
		return null;
	}

	public int[] getPredecessors(Pair<Integer, Integer> p) {
		if (VERBOSE) System.out.println(String.format("%s, p: %s", "getPredecessors", p));
		
		Set<Pair<String, Object>> resultSet = new HashSet<>();
		for (IncrementalReachability e : this.reachabilityEngines) {
			resultSet.add(new Pair<>(e.getClass().getName(), e.getPredecessors(p)));
		}
		this.checkResult("getPredecessors", resultSet);
		
		for (IncrementalReachability e : this.reachabilityEngines) {
			return e.getPredecessors(p);
		}
		return null;
	}

	public boolean existsEdge(Pair<Integer, Integer> from, Pair<Integer, Integer> to) {
		if (VERBOSE) System.out.println(String.format("%s, from: %s, to: %s", "existsEdge", from, to));
		
		Set<Pair<String, Object>> resultSet = new HashSet<>();
		for (IncrementalReachability e : this.reachabilityEngines) {
			resultSet.add(new Pair<>(e.getClass().getName(), e.reachable(from, to)));
		}
		
		return (Boolean) this.checkResult("existsEdge", resultSet);
	}

	public void addEdge(Pair<Integer, Integer> from, Pair<Integer, Integer> to) {
		if (VERBOSE) System.out.println(String.format("%s, from: %s, to: %s", "addEdge", from, to));
		
		for (IncrementalReachability e : this.reachabilityEngines) {
			e.insertEdge(from, to);
		}
	}

}
