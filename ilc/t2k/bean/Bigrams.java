/**
 * $Id: Bigrams.java,v 1.6 2005/09/27 16:11:46 felice Exp $
 * 
 */
package ilc.t2k.bean;

public class Bigrams implements Comparable{

	protected String left;
	protected String right;
	/**
	 * @return Returns the left.
	 */
	public String getLeft() {
		return left;
	}
	/**
	 * @param left The left to set.
	 */
	public void setLeft(String left) {
		this.left = left;
	}
	/**
	 * @return Returns the right.
	 */
	public String getRight() {
		return right;
	}
	/**
	 * @param right The right to set.
	 */
	public void setRight(String right) {
		this.right = right;
	}
	
	public int hashCode(){        
        return  left.hashCode() ^ right.hashCode();
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object pp) {
		if(left.equals(((Bigrams)pp).left))
			if(right.equals(((Bigrams)pp).right))
				return true;
		return false;
	}
	public int compareTo(Object pp) {
		if(equals(pp)){
			return 0;
		}else{
			return -1;
		}
			
	}
	
	
	
	
}
