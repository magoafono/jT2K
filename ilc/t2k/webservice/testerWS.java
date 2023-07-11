/*
 * $Id: testerWS.java,v 1.5 2008/01/10 18:06:15 simone Exp $
 */
package ilc.t2k.webservice;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import ilc.t2k.webservice.ws.JT2JWSInt;
import ilc.t2k.webservice.ws.JT2JWSIntService;
import ilc.t2k.webservice.ws.JT2JWSIntServiceLocator;

import javax.xml.rpc.ServiceException;

public class testerWS {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String conf = null;
		if(args.length > 0){
			System.err.println("Reading " + args[0] + " as configuration file");
			conf = readFromFile(args[0]);
		}
		
		//System.err.println("conf " + conf);
		JT2JWSIntService service = new JT2JWSIntServiceLocator();
		System.err.println("Trying to connect to " + service.getjt2kAddress());
		String result = null;
		try {
			JT2JWSInt ws = service.getjt2k();
			if(conf == null){
				result = ws.jt2KRunNoConf();
			}else{
				result = ws.jt2KRun(conf);
			}
		} catch (ServiceException e)
		{ 
			// TODO Auto-generated catch block
			System.err.println("Errore in getjt2k()");
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			System.err.println("Errore nel run()");
			e.printStackTrace();
		}
		catch (Exception e){
			System.err.println("Errore generico");
			e.printStackTrace();
	    }
	    System.out.println(result);

	}

	public static String readFromFile(String fileName){
		File file = new File (fileName);
		InputStream is;
		try {
			is = new FileInputStream(file);
			// Get the size of the file
			long length = file.length();
			
			// You cannot create an array using a long type.
			// It needs to be an int type.
			// Before converting to an int type, check
			// to ensure that file is not larger than Integer.MAX_VALUE.
			if (length > Integer.MAX_VALUE) {
				// File is too large
				System.err.println("File too long: "+fileName);
				return null;
			}
			
			// Create the byte array to hold the data
			byte[] bytes = new byte[(int)length];
			
			// Read in the bytes
			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length
					&& (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
				offset += numRead;
			}
			
			// Ensure all the bytes have been read in
			if (offset < bytes.length) {
				throw new IOException("Could not completely read file "+file.getName());
			}
			// Close the input stream and return bytes
			is.close();
			return new String(bytes,"ISO8859_1");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
		
	}

	
	
}
