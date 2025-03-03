package util;

public class Pair<X, Y> { 
	  public X x; 
	  public Y y;
	  private static final int PAIR_VERBOSE_LEVEL = 1;

	  public Pair(X x, Y y) { 
	    this.x = x; 
	    this.y = y; 
	  }
	  
	  public X getX(){
	  		return this.x;
	  }
	  
	  public Y getY() {
	  		return this.y;
	  }
	 
	  @Override
	  public boolean equals(Object o) {
		  if (this == o) return true;
		  if (o == null || getClass() != o.getClass()) return false;
		  Pair<?, ?> pair = (Pair<?, ?>) o;
		  boolean result = x.equals(pair.x) && y.equals(pair.y);
		  if(PAIR_VERBOSE_LEVEL >= 3) { System.out.println("[ Pair ]: Comparing " + this + " with " + pair + ": " + result); }
		  return result;
	  }
	  
	  public String toString() {
		  if (x != null && y != null)
			  return "(" + this.x.toString() + ", " + this.y.toString() + ")";
		  else if (x != null)
			  return "(" + this.x.toString() + ", null)";
		  else if (y != null)
			  return "(null ," + this.y.toString() + ")";
		  else
			  return "(null, null)";
	  }
	  
	  public int hashCode() {
		  return y.hashCode()*100 + x.hashCode();
	  }
}
