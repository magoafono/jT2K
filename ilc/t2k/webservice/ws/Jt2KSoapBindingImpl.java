/**
 * Jt2KSoapBindingImpl.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC2 Nov 16, 2004 (12:19:44 EST) WSDL2Java emitter.
 */

package ilc.t2k.webservice.ws;

import ilc.t2k.webservice.JT2KWS;

public class Jt2KSoapBindingImpl implements ilc.t2k.webservice.ws.JT2JWSInt{
	
	JT2KWS ws = new JT2KWS();

	public java.lang.String jt2KRun(java.lang.String in0) throws java.rmi.RemoteException {
		return ws.jt2kRun(in0);
    }

    public java.lang.String jt2KRunNoConf() throws java.rmi.RemoteException {
		return ws.jt2kRunNoConf();
    }

}
