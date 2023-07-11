/*
 * $Id: JT2KWS.java,v 1.6 2008/01/10 18:06:15 simone Exp $
 */
package ilc.t2k.webservice;

import java.io.IOException;
import java.util.logging.Logger;

import ilc.t2k.MainT2k;
import ilc.t2k.Utils;

/**
 * Web Service di jt2k.
 * @author simone
 *
 */
public class JT2KWS implements JT2JWSInt{

	private static MainT2k mainT2K = new MainT2k();
//	private static boolean lastRunIsDefault = false;
	private static Logger logger = Logger.getLogger("jt2k");

	/**
	 * Invoca jt2k con in input la stringa che rappresenta il file di configurazione.
	 * @param xmlConfigStream Stringa rappresentante il file di configurazione XML
	 * @return
	 */
	public String jt2kRun(String xmlConfigStream) {
		logger.info("Primo run... *" + xmlConfigStream + "*");
		String result = null;
		String sep = System.getProperty("file.separator");
		String xmlConfigFileName = System.getProperty("catalina.base") + sep + "conf" + sep + "default.xml";;
		if (xmlConfigStream == null) {
			logger.warning("Configuration file not found! Trying to load " + System.getProperty("catalina.base") + sep + "conf" + sep + "default.xml" );
		} else {
			try {
				Utils.write2File(System.getProperty("catalina.base") + sep + "conf" + sep + "temp.xml", xmlConfigStream.getBytes());
				xmlConfigFileName = System.getProperty("catalina.base") + sep + "conf" + sep + "temp.xml";
			} catch (IOException e) {
				logger.severe(e.getMessage());
				return e.getMessage(); 
			}
		}
		mainT2K.preinit();
		mainT2K.readConfiguration(xmlConfigFileName);
		mainT2K.initT2k();

		result = mainT2K.startT2k();
		return result;

	}

	/**
	 * Invoca il web service senza specificare una configurazione particolare. Verra' usato
	 * il file di configurazione denominato default.xml
	 * @return
	 */
	public String jt2kRunNoConf() {
		return jt2kRun(null);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JT2KWS jws = new JT2KWS();
		jws.jt2kRun(null);
	}

}
