/**
 * $Id: BigramMisura.java,v 1.7 2005/09/30 16:03:22 simone Exp $
 * 
 */
package ilc.t2k.statistics;

import ilc.t2k.bean.Bigrams;

public class BigramMisura extends Bigrams implements Comparable {
	
	protected double logLikelihood;
	
	public boolean equals(Object confronto){
		if ( this.logLikelihood == (((BigramMisura)confronto).logLikelihood))
			return true;
		return false;
	}
	
	public int hashCode(){        
		return 0;
	}
	
	public int compareTo(Object obj) {
		return (int) ((((BigramMisura)obj).logLikelihood - logLikelihood ) * 100000);
	}
	//inserire la Misura statistica
	public void setLog(double misura) {
		this.logLikelihood = misura;
	}
	
	//estrarre la misura statistica
	public double getLog() {
		return logLikelihood;
	}
	
}