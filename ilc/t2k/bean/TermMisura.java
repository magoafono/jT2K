package ilc.t2k.bean;

public class TermMisura implements Comparable{

	protected String termine;
	protected double misura;
	
	public TermMisura(String _termine, double _misura){
		termine = _termine;
		misura = _misura;
	}
	
	/**
	 * @return Returns the misura.
	 */
	public double getMisura() {
		return misura;
	}
	/**
	 * @param misura The misura to set.
	 */
	public void setMisura(double misura) {
		this.misura = misura;
	}
	/**
	 * @return Returns the termine.
	 */
	public String getTermine() {
		return termine;
	}
	/**
	 * @param termine The termine to set.
	 */
	public void setTermine(String termine) {
		this.termine = termine;
	}

	
	public boolean equals(Object confronto){
		if ( this.misura == (((TermMisura)confronto).misura))
			return true;
		return false;
	}
	
	public int hashCode(){        
		return 0;
	}
	
	public int compareTo(Object obj) {
		return (int) ((((TermMisura)obj).misura - misura ) * 100000000);
	}

	
	
}
