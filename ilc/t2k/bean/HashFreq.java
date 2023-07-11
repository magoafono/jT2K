/**
 * $Id: HashFreq.java,v 1.4 2006/12/11 15:58:54 simone Exp $
 */
package ilc.t2k.bean;

import java.util.HashMap;

public class HashFreq {

	
	private HashMap hash;
	private int freq;


	public HashFreq(HashMap _hash, int _freq){
		hash = _hash;
		freq = _freq;
	}
	/**
	 * @return Returns the freq.
	 */
	public int getFreq() {
		return freq;
	}

	/**
	 * @param freq The freq to set.
	 */
	public void setFreq(int freq) {
		this.freq = freq;
	}

	/**
	 * @return Returns the hash.
	 */
	public HashMap getHash() {
		return hash;
	}

	/**
	 * @param hash The hash to set.
	 */
	public void setHash(HashMap hash) {
		this.hash = hash;
	}

	public void clear() {
		hash.clear();
		hash = null;
	}
	
	
	
}
