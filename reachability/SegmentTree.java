package reachability;


public interface SegmentTree {

	public int argMin(int i);
	public int sumRange(int i, int j);
	public void update(int i, int val);

}