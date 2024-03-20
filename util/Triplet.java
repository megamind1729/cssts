package util;

public class Triplet<X, Y, Z> { 
	  public X x; 
	  public Y y; 
	  public Z z; 
	  public Triplet(X x, Y y, Z z) { 
	    this.x = x; 
	    this.y = y; 
	    this.z = z;
	  }
	  
	  public X getX(){
	  		return this.x;
	  }
	  
	  public Y getY() {
	  		return this.y;
	  }
	  
	  public Z getZ() {
	  		return this.z;
	  }
	  
	  public boolean equals(Object o) {
		  Triplet<X, Y, Z> other = (Triplet<X, Y, Z>) o;
		  return this.x.equals(other.x) && this.y.equals(other.y);  
	  }
	  
	  public String toString() {
		  return "(" + this.x.toString() + "," + this.y.toString() + ", " + this.z.toString() + ")";
	  }
	  
	  public int hashCode() {
		  return y.hashCode()*100 + x.hashCode() + z.hashCode()*13;
	  }
}
