/**
 * $Id: Statistic.java,v 1.3 2005/09/16 14:34:15 felice Exp $
 * 
 */
package ilc.t2k.statistics;

import java.util.HashMap;
import java.util.Vector;

public interface Statistic {
	public Vector elabora(HashMap sinistro, HashMap destro, HashMap bigram, int totBigram); 
	
}
