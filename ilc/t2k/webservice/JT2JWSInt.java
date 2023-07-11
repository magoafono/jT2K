/*
 * $Id: JT2JWSInt.java,v 1.2 2008/01/10 18:06:15 simone Exp $  
 */
package ilc.t2k.webservice;

/**
 * @author simone
 *
 */
public interface JT2JWSInt {

	public String jt2kRun(String xmlConfigStream);
	
	public String jt2kRunNoConf();
	
}
