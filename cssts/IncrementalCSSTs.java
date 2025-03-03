package cssts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import util.Pair;


public class IncrementalCSSTs implements reachability.IncrementalReachability {

	private int width;
	private int[] lengths;
	public List<Map<Integer, SparseSegmentTree>> ssts;
	private static final int VERBOSE_LEVEL = 1;
	

	public IncrementalCSSTs(int[] lengths) {
		this.width = lengths.length;
		this.lengths = Arrays.copyOf(lengths, lengths.length);

		this.ssts = new ArrayList<Map<Integer, SparseSegmentTree>>();
		for (int i = 0; i < this.width; i++) {
			this.ssts.add(new HashMap<Integer, SparseSegmentTree>());
			for (int j = 0; j < this.width; j++) {
				if (i != j) {
					if (this.lengths[i] == 0)
						this.ssts.get(i).put(j, new SparseSegmentTree(1, true));
					else
						this.ssts.get(i).put(j, new SparseSegmentTree(this.lengths[i], true));
				}
			}
		}
	}

	public IncrementalCSSTs(IncrementalCSSTs other) {
		this.width = other.width;
		this.lengths = Arrays.copyOf(other.lengths, other.lengths.length);

		this.ssts = new ArrayList<Map<Integer, SparseSegmentTree>>();
		for (int i = 0; i < this.width; i++) {
			this.ssts.add(new HashMap<Integer, SparseSegmentTree>());
			for (int j = 0; j < this.width; j++) {
				if (i != j) {
					this.ssts.get(i).put(j, new SparseSegmentTree(other.ssts.get(i).get(j)));
				}
			}
		}
	}
	
	@Override
	public int getSuccessor(Pair<Integer, Integer> p, int i) {
		if (p.x.equals(i)) {
			return p.y < this.lengths[p.x] - 1 ? p.y + 1 : -1;
		} else {
			int v = this.ssts.get(p.x).get(i).sumRange(p.y, this.getChainLength(p.x)-1);
			return v < Integer.MAX_VALUE ? v : -1;
		}
	}

	@Override
	public int getPredecessor(Pair<Integer, Integer> p, int i) {
		if (p.x.equals(i)) {
			return p.y > 0 ? p.y - 1 : -1;
		} else {
			int v = this.ssts.get(i).get(p.x).argMin(p.y);
			return v > Integer.MIN_VALUE ? v : -1;
		}
	}

	@Override
	public boolean reachable(Pair<Integer, Integer> from, Pair<Integer, Integer> to) {
		if (from.x.equals(to.x) && from.y < to.y) {
			return true;
		} else {
			int v = this.getSuccessor(from, to.x);
			return v >= 0 && v <= to.y;
		}
	}

	private void addSuccessor(Pair<Integer, Integer> from, Pair<Integer, Integer> to) {
		if (!from.x.equals(to.x))
			this.ssts.get(from.x).get(to.x).update(from.y, to.y);
	}

	@Override
	public Set<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> insertEdge(Pair<Integer, Integer> from, Pair<Integer, Integer> to) {
		// Adds edge and takes transitive closure. Assumes that edge does not create cycle
		// Returns the set of edges added due to transitive closure.
		Set<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> addedEdges = new HashSet<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>>();

		if (this.reachable(from, to)) {
			if (VERBOSE_LEVEL >= 3) { System.out.println("[ IncrementalCSSTs ]: Edge already exists"); }
			return addedEdges;
		}

		Stack<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> q = new Stack<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>>();
		q.push(new Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>(from, to));
		while (!q.isEmpty()) {
			Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> e = q.pop();
			Pair<Integer, Integer> f = e.x;
			Pair<Integer, Integer> t = e.y;
			if (this.reachable(f, t)) {
				continue;
			}

			this.addSuccessor(f, t);
			addedEdges.add(e);

			int[] succ = this.getSuccessors(t);
			int[] pred = this.getPredecessors(f);
			for (int i = 0; i < this.width; i++) {
				if (i != f.x && i != t.x) {
					Pair<Integer, Integer> tt = new Pair<Integer, Integer>(i, succ[i]);
					if (succ[i] >= 0) {
						q.push(new Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>(f, tt));
					}
					Pair<Integer, Integer> ff = new Pair<Integer, Integer>(i, pred[i]);
					if (pred[i] >= 0) {
						q.push(new Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>(ff, t));
					}
				}
			}
		}
		return addedEdges;
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getChainLength(int i) {
		return this.lengths[i];
	}
	
	@Override
	public void print() {
		for (int i1 = 0; i1 < this.getWidth(); i1++) {
			for (int i2 = 0; i2 < this.getWidth(); i2++) {
				if (i1 != i2) {
					System.out.println("i1: " + i1 + ", i2: " + i2);
					this.ssts.get(i1).get(i2).print();
					System.out.println("\n");
				}
			}
		}
	}
}
