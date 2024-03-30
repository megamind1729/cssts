package cssts;
import java.util.Arrays;

import util.Pair;

public class Block {

	class SegmentTreeNode {
		public int start, end;
		public SegmentTreeNode left, right;
		public int min, pos;
		int[] block;
		public boolean isLeaf;

		public SegmentTreeNode(int start, int end) {
			this.start = start;
			this.end = end;
			this.left = null;
			this.right = null;
			this.min = Integer.MAX_VALUE;
			this.pos = -1;
			this.isLeaf = false;
		}

		@Override
		public String toString() {
			String str;
			str = "Block node: " + System.identityHashCode(this) + ", n.start: " + start + ", n.end: " + 
				   end + ", offset: " + offset + ", pos: " + pos + ", min: " + min + ", isLeaf: " + 
				   isLeaf + ", left: " + System.identityHashCode(left) + ", right: " + 
				   System.identityHashCode(right);
			if (isLeaf) {
				if (this.block != null) {
					str += " - array size: " + this.block.length + ", array=["; 
					if (this.block.length < 100) {
						for (int i = 0; i < this.block.length; i++) {
							if (this.block[i] != Integer.MAX_VALUE)
								str += Integer.toString(i) + ": " + Integer.toString(this.block[i]) + ", ";
						}
					}
					str += "]";
				}
				else
					str += " - array is null";
			}
			return str;
		}
	}

	public int offset;
	public SegmentTreeNode root = null;
	public int maxLevel;
	public int length;

	public Block(int length, int maxLevel, int offset) {
		this.maxLevel = maxLevel;
		this.length = length;
		root = buildTree(0, length - 1, 0);
		this.offset = offset;
	}
	
	public Block(int[] nums, int maxLevel) {
		this.maxLevel = maxLevel;
		root = buildTree(nums, 0, nums.length - 1, 0);
	}

	public Block(Block other) {
		this.maxLevel = other.maxLevel;
		this.offset = other.offset;
		root = buildTree(other, other.root);
	}

	private SegmentTreeNode buildTree(Block other, SegmentTreeNode otherCurrent) {
		if (otherCurrent == null) {
			return null;
		} else {
			SegmentTreeNode thisCurrent = new SegmentTreeNode(otherCurrent.start, otherCurrent.end);
			thisCurrent.min = otherCurrent.min;
			thisCurrent.isLeaf = otherCurrent.isLeaf;
			thisCurrent.pos = otherCurrent.pos;
			
			if (otherCurrent.block != null)
				thisCurrent.block = Arrays.copyOf(otherCurrent.block, otherCurrent.block.length);

			if (otherCurrent.left != null) {
				thisCurrent.left = this.buildTree(other, otherCurrent.left);
			}
			if (otherCurrent.right != null) {
				thisCurrent.right = this.buildTree(other, otherCurrent.right);
			}
			return thisCurrent;
		}
	}

	private SegmentTreeNode buildTree(int start, int end, int level) {
		if (start > end) {
			return null;
		} else {
			SegmentTreeNode ret = new SegmentTreeNode(start, end);

			if (level >= maxLevel) {
				ret.min = Integer.MAX_VALUE;
				ret.pos = -1;
				ret.isLeaf = true;
				return ret;
			} else {
				if (start == end) {
					ret.min = Integer.MAX_VALUE;
					ret.pos = start;
					ret.isLeaf = true;
				} else {
					int mid = start + (end - start) / 2;
					ret.left = buildTree(start, mid, level + 1);
					ret.right = buildTree(mid + 1, end, level + 1);
					ret.min = ret.left.min < ret.right.min ? ret.left.min : ret.right.min;
					ret.pos = ret.left.min < ret.right.min ? ret.left.pos : ret.right.pos;
				}
				return ret;
			}
		}
	}
	
	private SegmentTreeNode buildTree(int[] nums, int start, int end, int level) {
		if (start > end) {
			return null;
		} else {
			SegmentTreeNode ret = new SegmentTreeNode(start, end);

			if (level >= maxLevel) {
				ret.block = Arrays.copyOfRange(nums, start, end + 1);
				Pair<Integer, Integer> minPos = this.getBlockMin(ret, start, end);
				ret.min = minPos.x;
				ret.pos = minPos.y;
				ret.isLeaf = true;
				return ret;
			} else {
				if (start == end) {
					ret.min = nums[start];
					ret.pos = start;
					ret.block = Arrays.copyOfRange(nums, start, end + 1);
					ret.isLeaf = true;
				} else {
					int mid = start + (end - start) / 2;
					ret.left = buildTree(nums, start, mid, level + 1);
					ret.right = buildTree(nums, mid + 1, end, level + 1);
					ret.min = ret.left.min < ret.right.min ? ret.left.min : ret.right.min;
					ret.pos = ret.left.min < ret.right.min ? ret.left.pos : ret.right.pos;
				}
				return ret;
			}
		}
	}

	private Pair<Integer, Integer> getBlockMin(SegmentTreeNode node, int start, int end) {
		int min = Integer.MAX_VALUE;
		int pos = -1;
		
		if (node.block != null) {
			for (int i = end - node.start; i >= start - node.start; i--) {
				int v = node.block[i];
				if (v < min) {
					min = v;
					pos = i;
				}
			}
			pos = pos == -1 ? end : node.start + pos;
		}
		return new Pair<>(min, pos);
	}

	private int getBlockArgMin(SegmentTreeNode node, int x) {
		for (int i = node.block.length - 1; i >= 0; i--) {
			int v = node.block[i];
			if (v <= x) {
				return node.start + i;
			}
		}
		return -1;
	}

	void update(int i, int val) {
		i -= offset;
		update(root, i, val);
	}

	void update(SegmentTreeNode root, int pos, int val) {
		if (root.isLeaf) {
			if (root.block == null) {
				root.block = new int[(root.end + 1 - root.start)];
				Arrays.fill(root.block, Integer.MAX_VALUE);
			}
			if (val > root.block[pos - root.start])
				return;
						
			root.block[pos - root.start] = val;

			if (val < root.min) {
				root.min = val;
				root.pos = pos;
			}
			if (val == root.min && pos > root.pos) {
				root.pos = pos;
			}
			if (root.pos == pos) {
				Pair<Integer, Integer> minPos = getBlockMin(root, root.start, root.end);
				root.min = minPos.x;
				root.pos = minPos.y;
			}
		} else {
			int mid = root.start + (root.end - root.start) / 2;
			if (pos <= mid) {
				update(root.left, pos, val);
			} else {
				update(root.right, pos, val);
			}
			root.min = root.left.min < root.right.min ? root.left.min : root.right.min;
			root.pos = root.left.min < root.right.min ? root.left.pos : root.right.pos;
		}
	}

	public int argMin(int x) {
		if (this.root.min <= x) {
			return this.argMin(this.root, x) + offset;
		} else {
			return -1;
		}
	}

	public int argMin(SegmentTreeNode root, int x) {
		if (root.isLeaf) {
			return getBlockArgMin(root, x);
		}
		if (root.right.min <= x) {
			return this.argMin(root.right, x);
		} else {
			return this.argMin(root.left, x);
		}
	}

	public int sumRange(int i, int j) {
		i -= offset;
		j -= offset;
		return sumRange(root, i, j);
	}

	public int sumRange(SegmentTreeNode root, int start, int end) {
		if (root.end == end && root.start == start) {
			return root.min;
		} else if (root.isLeaf) {
			if (root.pos >= start && root.end <= end)
				return root.min;
			return getBlockMin(root, start, end).x;
		} else {
			int mid = root.start + (root.end - root.start) / 2;
			if (end <= mid) {
				return sumRange(root.left, start, end);
			} else if (start >= mid + 1) {
				return sumRange(root.right, start, end);
			} else {
				int l = sumRange(root.left, start, mid);
				int r = sumRange(root.right, mid + 1, end);
				return l < r ? l : r;
			}
		}
	}

	public void print() {
		print(root, 0);
	}

	public void print(SegmentTreeNode node, int indentation) {
		// This prefixes the value with the necessary amount of indentation
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