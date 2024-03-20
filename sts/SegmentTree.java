package sts;
import java.util.Arrays;


public class SegmentTree implements reachability.SegmentTree {

	public int[] nums;
	
	class SegmentTreeNode {
		public int start, end;
		public SegmentTreeNode left, right;
		public int min;

		public SegmentTreeNode(int start, int end) {
			this.start = start;
			this.end = end;
			this.left = null;
			this.right = null;
			this.min = Integer.MAX_VALUE;
		}
		
        @Override
        public String toString() {
            String str;
            str = "node: " + System.identityHashCode(this) + ", n.start: " + start + ", n.end: " + end + ", min: " + min;
            return str;
        }
	}

	public SegmentTreeNode root = null;

	public SegmentTree(int[] nums) {
		this.nums = Arrays.copyOf(nums, nums.length);
		this.root = buildTree(this.nums, 0, this.nums.length-1);
	}

	public SegmentTree(SegmentTree other) {
		this.nums = Arrays.copyOf(other.nums, other.nums.length);
		this.root = buildTree(this.nums, 0, this.nums.length-1);
	}

	public int get(int i) {
		return this.sumRange(root, i, i);
	}

	private SegmentTreeNode buildTree(int[] nums, int start, int end) {
		if (start > end) {
			return null;
		} else {
			SegmentTreeNode ret = new SegmentTreeNode(start, end);
			if (start == end) {
				ret.min = nums[start];
			} else {
				int mid = start  + (end - start) / 2;             
				ret.left = buildTree(nums, start, mid);
				ret.right = buildTree(nums, mid + 1, end);
				ret.min = ret.left.min < ret.right.min ? ret.left.min : ret.right.min;
			}         
			return ret;
		}
	}

	@Override
	public void update(int i, int val) {
		update(root, i, val);
	}

	private void update(SegmentTreeNode root, int pos, int val) {
		if (root.start == root.end) {
			if (root.min > val) {
				nums[root.start] = val;
				root.min = val;
			}
		} else {
			int mid = root.start + (root.end - root.start) / 2;
			if (pos <= mid) {
				update(root.left, pos, val);
			} else {
				update(root.right, pos, val);
			}
			root.min = root.left.min < root.right.min ? root.left.min : root.right.min;
		}
	}

	@Override
	public int argMin(int x) {
		if (this.root == null)
			return -1;
		else if(this.root.min <= x)
			return this.argMin(this.root, x)-1;
		else
			return -1;
	}

	private int argMin(SegmentTreeNode root, int x) {
		if(root.left == root.right) {
			return 1;
		}
		if(root.right.min <= x) {
			return this.argMin(root.right, x) + root.left.end - root.left.start + 1;
		}
		else {
			return this.argMin(root.left, x);
		}
	}

	@Override
	public int sumRange(int i, int j) {
		return sumRange(root, i, j);
	}

	private int sumRange(SegmentTreeNode root, int start, int end) {
		if (root.end == end && root.start == start) {
			return root.min;
		} else {
			int mid = root.start + (root.end - root.start) / 2;
			if (end <= mid) {
				return sumRange(root.left, start, end);
			} else if (start >= mid+1) {
				return sumRange(root.right, start, end);
			}  else {
				int l = sumRange(root.left, start, mid);
				int r = sumRange(root.right, mid+1, end);
				return  l < r ? l : r ;
			}
		}
	}
	
    public void print()
    {
        print(root, 0);
    }

    private void print(SegmentTreeNode node, int indentation)
    {
        for (int i = 0; i < indentation; i++) {
            System.out.print("\t");
        }

        System.out.println(node);

        // Recursively call the child nodes.
        if (node.left != null)
            print(node.left, indentation + 1); // Increment the indentation counter.
        if (node.right != null)
            print(node.right, indentation + 1); // Increment the indentation counter.
    }
}