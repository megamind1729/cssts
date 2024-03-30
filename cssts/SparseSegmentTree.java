package cssts;


public class SparseSegmentTree implements reachability.SegmentTree {

	private static final int BLOCK_SIZE = 32;
	
	class SegmentTreeNode {
		public int start, end;
		public SegmentTreeNode left, right;
		public int min;
		public int pos;
		public int activeStart, activeEnd;
		public int level;
		public Block block;
		
		public SegmentTreeNode(int start, int end, int level) {
			this.start = start;
			this.end = end;
			this.left = null;
			this.right = null;
			this.min = Integer.MAX_VALUE;
			this.pos = -1;
			this.activeStart = Integer.MIN_VALUE;
			this.activeEnd = Integer.MIN_VALUE;
			this.block = null;
			this.level = level;
		}
		
		public SegmentTreeNode(int start, int end, int pos, int val, int level) {
			this.start = start;
			this.end = end;
			this.left = null;
			this.right = null;
			this.min = val;
			this.pos = pos;
			this.activeStart = pos;
			this.activeEnd = pos;
			this.level = level;
		}
		
		public SegmentTreeNode(int start, int end, int pos, int val, int level, int activeStart, int activeEnd) {
			this.start = start;
			this.end = end;
			this.left = null;
			this.right = null;
			this.min = val;
			this.pos = pos;
			this.activeStart = activeStart;
			this.activeEnd = activeEnd;
			this.level = level;
		}
		
		public String toString() {
			return "node: " + System.identityHashCode(this)  + 
				   ", left: " + System.identityHashCode(this.left) + 
				   ", right: " +  System.identityHashCode(this.right) + 
				   ", min:" + this.min + ", pos: " + this.pos +	
				   ", start: " + this.start + ", end: " + this.end +
				   ", activeStart: " + this.activeStart + ", activeEnd: " + this.activeEnd;
		}
	}

	private SegmentTreeNode root = null;
	private int maxLevel; 
	private boolean isIncremental;
	
	public SparseSegmentTree(int length, boolean isIncremental) {
		this.isIncremental = isIncremental;
		int v = length / BLOCK_SIZE;
		int height = (int)(Math.log(v) / Math.log(2));
		height = height < 1 ? 1 : height;
		this.maxLevel = height;
		root = new SegmentTreeNode(0, length-1, 0);
	}
	
	public SparseSegmentTree(SparseSegmentTree other) {
		this.maxLevel = other.maxLevel;
		root = buildTree(other, other.root);
	}

	private SegmentTreeNode buildTree(SparseSegmentTree other, SegmentTreeNode otherCurrent) {
		if (otherCurrent == null) {
			return null;
		} else {
			SegmentTreeNode thisCurrent = new SegmentTreeNode(otherCurrent.start, otherCurrent.end, 
															  otherCurrent.pos, otherCurrent.min, 
															  otherCurrent.level, otherCurrent.activeStart,
															  otherCurrent.activeEnd);
			
			if (otherCurrent.block != null) 
				thisCurrent.block = new Block(otherCurrent.block);

			if (otherCurrent.left != null) {
				thisCurrent.left = this.buildTree(other, otherCurrent.left);
			}

			if (otherCurrent.right != null) {
				thisCurrent.right = this.buildTree(other, otherCurrent.right);
			}

			return thisCurrent;
		}
	}
	
	@Override
	public void update(int i, int val) {
		update(root, i, val);
	}
	
	private void update(SegmentTreeNode root, int pos, int val) {
		assert(root == null || root.pos == -1 || (root.pos >= root.start && root.pos <= root.end));
		
		if (root.block != null) {
			root.block.update(pos, val);
			root.min = root.block.root.min;
			root.pos = root.block.root.pos + root.block.offset;
			if (pos < root.activeStart)
				root.activeStart = pos;
			if (pos > root.activeEnd)
				root.activeEnd = pos;
			return;
		}
		
		if (root.pos == pos) {
			if (val <= root.min)
				root.min = val;
			else if (val > root.min && this.isIncremental)
				return;
			else {
				if (!this.isIncremental) {
					if (root.left == null && root.right == null) {
						root.min = val;
					} else {
						root = deleteNode(root, pos);
						if (root != null && pos >= root.start && pos <= root.end)
							update(root, pos, val);
						else
							update(this.root, pos, val);
					}
				} else {
					assert(false);
				}
			}
		} else if (root.pos == -1) {
			// the current node does not store any position
			root.min = val;
			root.pos = pos;
			root.activeStart = pos;
			root.activeEnd = pos;
			return;
		}  else {
			if (pos < root.activeStart)
				root.activeStart = pos;
			if (pos > root.activeEnd)
				root.activeEnd = pos;
			
			if (val < root.min || (val == root.min && pos > root.pos)) {
				int oldMin = root.min;
				int oldPos = root.pos;
				root.min = val;
				root.pos = pos;
				val = oldMin;
				pos = oldPos;
			}
			
			int mid = root.start + (root.end - root.start) / 2;
			if (pos <= mid) {
				if (root.left == null)
					root.left = createIntermediateNode(root.start, mid, pos, val, root.level+1);
				else
					update(root.left, pos, val);
			} else {
				if (root.right == null)
					root.right = createIntermediateNode(mid+1, root.end, pos, val, root.level+1);
				else
					update(root.right, pos, val);
			}
		}
	}


	private SegmentTreeNode deleteNode(SegmentTreeNode root, int pos) {
		if (pos < root.start || pos > root.end) {
			return root;
		}
		
		if (root.left != null && pos >= root.left.start && pos <= root.left.end) {
			root.left = deleteNode(root.left, pos);
		} else if (root.right != null && pos >= root.right.start && pos <= root.right.end) {
			root.right = deleteNode(root.right, pos);
		}
			
		if (root.pos == pos) {
			if (root.left == null && root.right == null) {
				if (root == this.root) {
					root.pos = -1;
					root.min = Integer.MAX_VALUE;
					return root;
				} else
					return null;
			} else if (root.left != null && root.right == null) {
				if (this.root != root) {
					root.start = root.left.start;
					root.end = root.left.end;
				}
				root.min = root.left.min;
				root.pos = root.left.pos;
				root.right = root.left.right;
				root.left = root.left.left;
			} else if (root.left == null && root.right != null) {
				if (this.root != root) {
					root.start = root.right.start;
					root.end = root.right.end;
				}
				root.min = root.right.min;
				root.pos = root.right.pos;
				root.left = root.right.left;
				root.right = root.right.right;
			} else {
				if (root.right.min <= root.left.min) {
					root.min = root.right.min;
					root.pos = root.right.pos;
				} else {
					root.min = root.left.min;
					root.pos = root.left.pos;
				}
			}
		}
		assert(root.pos != pos);
		return root;
	}
	
	private SegmentTreeNode createIntermediateNode(int start, int end, int pos, int val, int level) {
		if (level >= this.maxLevel) {
			SegmentTreeNode transitionNode = new SegmentTreeNode(start, end, pos, val, level);
			transitionNode.block = new Block(end-start+1, 0, start);
			transitionNode.block.update(pos, val);
			return transitionNode;
		} else {
			if (start > end)
				return null;
			else
				return new SegmentTreeNode(start, end, pos, val, level);
		}
	}

	@Override
	public int argMin(int x) {
		if (root.min <= x) {
			int result = argMin(root, x);
			return result;
		} else 
			return -1;
	}

	private int argMin(SegmentTreeNode root, int x) {
		if (root.block != null)
			return root.block.argMin(x);
		
		int mid = root.start + (root.end - root.start) / 2;

		if (root.start == root.end || root.min == x || (root.right == null && root.left == null))
			return root.pos;
		else if (root.right == null && root.left != null && root.pos >= root.left.end && root.min <= x)
			return root.pos;
		else if (root.right != null && root.pos >= root.right.end && root.min <= x)
			return root.pos;
		else if (root.right != null && root.right.min <= x)
			return Math.max(root.pos, argMin(root.right, x));
		else if (root.right != null && root.right.min > x && root.min <= x && root.pos != root.right.pos && root.pos > mid)
			return root.pos;
		else {
			if (root.left == null || root.left.min > x)
				return root.pos;
			else
				return Math.max(root.pos, argMin(root.left, x));
		}
	}

	@Override
	public int sumRange(int i, int j) {
		return sumRange(root, i);
	}
	
	private int sumRange(SegmentTreeNode root, int index) {
		if (root == null || root.min == Integer.MAX_VALUE || root.activeEnd < index)
			return Integer.MAX_VALUE;
		
		if (root.block != null)
			return root.block.sumRange(index, root.end);
		
		if (root.pos >= index)
			return root.min;
		else {
			int l = sumRange(root.left, index);
			int r = sumRange(root.right, index);
			return l < r ? l : r;
		}
	}
	
	public void print() {
		print(root, 0);
	}

	private void print(SegmentTreeNode node, int indentation) {
		if (node == null) {
			if (root == node)
				System.out.println("Root is null!");
			else
				System.out.println("Node is null!");
			return;
		}
		
		for (int i = 0; i < indentation; i++) 
			System.out.print("\t");
		
		System.out.println(node);
		
		if (node.left != null)
			print(node.left, indentation + 1);
		if (node.right != null)
			print(node.right, indentation + 1);
	}

}