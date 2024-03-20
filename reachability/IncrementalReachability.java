package reachability;

import java.util.Set;
import util.Pair;


public interface IncrementalReachability {

	public int getSuccessor(Pair<Integer, Integer> p, int i);
	public int getPredecessor(Pair<Integer, Integer> p, int i);
	public boolean reachable(Pair<Integer, Integer> from, Pair<Integer, Integer> to);
	public Set<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> insertEdge(Pair<Integer, Integer> from, Pair<Integer, Integer> to);
	public int getWidth();
	public int getChainLength(int i);


	public default boolean unordered(Pair<Integer, Integer> u, Pair<Integer, Integer> v) {
		return !(this.reachable(u, v) || this.reachable(v, u));
	}

	// Returns the earliest successor p has on each thread
	public default int[] getSuccessors(Pair<Integer, Integer> p) {
		int s[] = new int[this.getWidth()];
		for (int i = 0; i < this.getWidth(); i++) {
			s[i] = this.getSuccessor(p, i);
		}
		return s;
	}

	// Returns the latest predecessor p has on each thread
	public default int[] getPredecessors(Pair<Integer, Integer> p) {
		int s[] = new int[this.getWidth()];
		for (int i = 0; i < this.getWidth(); i++) {
			s[i] = this.getPredecessor(p, i);
		}
		return s;
	}

	public default boolean semanticEquality(Object o) {
		IncrementalReachability other = (IncrementalReachability) o;

		for (int i = 0; i < this.getWidth(); i++) {
			for (int j = 0; j < this.getChainLength(i); j++) {
				Pair<Integer, Integer> u = new Pair<Integer, Integer>(i, j);
				for (int k = 0; k < this.getWidth(); k++) {
					int thisSucc = this.getSuccessor(u, k);
					int otherSucc = other.getSuccessor(u, k);
					if (thisSucc != otherSucc) {
						return false;
					}
				}
			}
		}
		return true;
	}

	public default void print() {
		System.out.println("==================================");
		System.out.println("Partial Order. Width: " + this.getWidth());
		System.out.println("==================================");
		for (int i1 = 0; i1 < this.getWidth(); i1++) {
			for (int j1 = 0; j1 < this.getChainLength(i1); j1++) {
				for (int i2 = 0; i2 < this.getWidth(); i2++) {
					if (i1 != i2) {
						Pair<Integer, Integer> u = new Pair<Integer, Integer>(i1, j1);
						Pair<Integer, Integer> v = new Pair<Integer, Integer>(i2, this.getSuccessor(u, i2));
						System.out.println(u.toString() + "\t->\t" + v.toString());
					}
				}
			}
		}
	}
	
}
