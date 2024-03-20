package util;
import java.io.Serializable;

public class Pair<X, Y> implements Serializable { 
	  public X x; 
	  public Y y; 
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
	 
	  public boolean equals(Pair<X, Y> o) {
		  Pair<X, Y> other = (Pair<X, Y>) o;
		  return this.x.equals(other.x) && this.y.equals(other.y);  
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
