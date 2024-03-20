package cssts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import util.Pair;


public class CSSTs implements reachability.FullReachability {

	private int width;
	private int[] lengths;
	private List<Map<Integer, Pair<ArrayList<PriorityQueue<Integer>>, SparseSegmentTree>>> ssts;

	public CSSTs(int[] lengths) {
		this.width = lengths.length;
		this.lengths = Arrays.copyOf(lengths, lengths.length);

		this.ssts = new ArrayList<>();
		for (int i = 0; i < this.width; i++) {
			this.lengths[i] = lengths[i];
			this.ssts.add(new HashMap<>());
			for (int j = 0; j < this.width; j++) {
				if (i != j) {
					ArrayList<PriorityQueue<Integer>> queues = new ArrayList<PriorityQueue<Integer>>();
					for (int k = 0; k < this.lengths[i]; k++)
						queues.add(new PriorityQueue<Integer>());
					if (this.lengths[i] == 0)
						this.ssts.get(i).put(j, new Pair<>(queues, new SparseSegmentTree(this.lengths[1], false)));
					else
						this.ssts.get(i).put(j, new Pair<>(queues, new SparseSegmentTree(this.lengths[i], false)));
				}
			}
		}
	}
	
	@Override
	public int getSuccessor(Pair<Integer, Integer> p, int i) {
		if (p.x.equals(i)) {
			return p.y < this.lengths[p.x] - 1 ? p.y + 1 : -1;
		} 

		int[] closure = new int[this.width];
		for (int t = 0; t < this.width; t++) {
			if (t != p.x)
				closure[t] = this.ssts.get(p.x).get(t).y.sumRange(p.y, this.getChainLength(p.x));
			else
				closure[t] = p.y;
		}

		boolean changed;
		do {
			changed = false;
			for (int i1 = 0; i1 < this.width; i1++) {
				for (int i2 = 0; i2 < this.width; i2++) {
					if (i1 == i2 || closure[i2] < 0)
						continue;
					
					int v = this.ssts.get(i2).get(i1).y.sumRange(closure[i2], this.getChainLength(i2));
					
					if ((v >= 0 && v < closure[i1]) || (v >= 0 && closure[i1] < 0)) {
						closure[i1] = v;
						changed = true;
					}
				}
			}
		} while (changed);

		return closure[i] < Integer.MAX_VALUE ? closure[i] : -1;
	}
	
	
	@Override
	public int getPredecessor(Pair<Integer, Integer> p, int i) {
		if (p.x.equals(i)) {
			return p.y < this.lengths[p.x] - 1 ? p.y + 1 : -1;
		} 

		int[] closure = new int[this.width];
		for (int t = 0; t < this.width; t++) {
			if (t != p.x)
				closure[t] = this.ssts.get(t).get(p.x).y.argMin(p.y);
			else
				closure[t] = p.y;
		}
		
		boolean changed;
		do {
			changed = false;
			for (int i1 = 0; i1 < this.width; i1++) {
				for (int i2 = 0; i2 < this.width; i2++) {
					if (i1 == i2 || closure[i2] < 0)
						continue;
					
					int v = this.ssts.get(i1).get(i2).y.argMin(closure[i2]);
					if ((v >= 0 && v > closure[i1]) || (v >= 0 && closure[i1] < 0)) {
						closure[i1] = v;
						changed = true;
					}
				}
			}
		} while (changed);
		
		return closure[i] > Integer.MIN_VALUE ? closure[i] : -1;
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

	@Override
	public Set<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> insertEdge(Pair<Integer, Integer> from, Pair<Integer, Integer> to) {
		Set<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> addedEdges = new HashSet<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>>();

		Pair<ArrayList<PriorityQueue<Integer>>, SparseSegmentTree> succ = this.ssts.get(from.x).get(to.x);
		PriorityQueue<Integer> edgeHeap = succ.x.get(from.y);
		Integer v = edgeHeap.peek();
		if (v == null || v > to.y) {
			succ.y.update(from.y, to.y);
		}
			
		if (edgeHeap.add(to.y)) {
			addedEdges.add(new Pair<>(from, to));
		}
		
		return addedEdges;
	}

	@Override
	public boolean deleteEdge(Pair<Integer, Integer> from, Pair<Integer, Integer> to) {
		Pair<ArrayList<PriorityQueue<Integer>>, SparseSegmentTree> succ = this.ssts.get(from.x).get(to.x);
		PriorityQueue<Integer> edgeHeap = succ.x.get(from.y);

		Integer min = edgeHeap.peek();
		if (min == null)
			return false;
		
		if (min.equals(to.y)) {
			edgeHeap.poll();
			Integer head = edgeHeap.peek();
			if (head == null)
				succ.y.update(from.y, Integer.MAX_VALUE);
			else
				succ.y.update(from.y, head);
			return true;
		} 
		
		return edgeHeap.remove(to.y);
	}
	
	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getChainLength(int i) {
		return this.lengths[i];
	}

}
