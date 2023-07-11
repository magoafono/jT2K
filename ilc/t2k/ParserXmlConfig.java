/**
 * $Id: ParserXmlConfig.java,v 1.21 2008/01/10 18:06:15 simone Exp $
 * 
 */
package ilc.t2k;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ParserXmlConfig{

	/**
	 * @param args
	 */
	 static class MyHandler extends DefaultHandler {
		 
			public void startElement(String uri, String localName, 
					String qName, Attributes attributes) throws SAXException {
				// TODO Auto-generated method stub
				//super.startElement(uri, localName, qName, attributes);
				
				//System.out.println("uri : "+uri);
				//System.out.println("localName : "+localName);
				//System.out.println("qName : "+qName);
				if(qName.equals(Consts.THRESHOLD)){
					parseThresholdTag(attributes);
				}
				if(qName.equals(Consts.DATABASE)){
					parseDatabaseTag(attributes);
				}				
				if(qName.equals(Consts.INFO)){
					parseInfoTag(attributes);
				}				
			}
			/* (non-Javadoc)
			 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
			 */
			public void characters(char[] ch, int start, int length) throws SAXException {
				// TODO Auto-generated method stub
				//String s = new String(ch, start, length);
				//System.out.println("chars : "+s);
			}

     }
	public String parseXMLFile(String fileName){
		String ret = "ok";
 		 try {	
             // Create a builder factory
             SAXParserFactory factory = SAXParserFactory.newInstance();
             //factory.setValidating(true);
 
             MyHandler handler = new MyHandler();
			// Create the builder and parse the file
             factory.newSAXParser().parse(new File(fileName), handler);
         } catch (SAXException e) {
        	 Consts.logger.severe(e.getMessage());
        	 System.err.println("parseXMLFile 1");
        	// System.exit(-1);
        	 ret = e.getMessage();
             // A parsing error occurred; the xml input is not valid
         } catch (ParserConfigurationException e) {
        	 Consts.logger.severe(e.getMessage());
        	 System.err.println("parseXMLFile 2");
        	 //System.exit(-1);
        	 ret = e.getMessage();
         } catch (IOException e) {
        	 Consts.logger.severe("File "+fileName+" doesn't exist!\n Please, provide a configuration file");
        	 System.err.println("parseXMLFile 3");
        	 //System.exit(-1);
        	 ret = e.getMessage();
         }catch (Exception e) {
        	 Consts.logger.severe("File "+fileName+" doesn't exist!\n Please, provide a configuration file");
        	 System.err.println("parseXMLFile 4");
        	 ret = e.getMessage();
         }
         Consts.logger.info("parseXMLFile: "+ret);
         return ret;
	}

	public void parseXMLString(String xmlString){
		StringReader sr;
		InputSource sr_input;

		try {	
			// Create a builder factory
			SAXParserFactory factory = SAXParserFactory.newInstance();
			//factory.setValidating(true);
			
			MyHandler handler = new MyHandler();
			// Create the builder and parse the file
			sr = new StringReader(xmlString);
			sr_input = new InputSource(sr);
			factory.newSAXParser().parse(sr_input, handler);
		} catch (SAXException e) {
			Consts.logger.log(Level.SEVERE,e.getMessage());
			//System.exit(-1);
			return;
			// A parsing error occurred; the xml input is not valid
		} catch (ParserConfigurationException e) {
			Consts.logger.log(Level.SEVERE,e.getMessage());
			//System.exit(-1);
			return;
		} catch (IOException e) {
			Consts.logger.log(Level.SEVERE,"Error reading XML configuration stream");
			//System.exit(-1);
			return;
        }
	}
	/* (non-Javadoc)
	 */
	
	public static void parseThresholdTag(Attributes attributes){
		for (int i = 0; i < attributes.getLength(); i++){
			if(attributes.getValue(i).equals(Consts.GLOSSARIO)){
				i++;
				Config.soglia_glossario = Integer.parseInt(attributes.getValue(i));
			}
			if(attributes.getValue(i).equals(Consts.BIGRAMS)){
				i++;
				Config.soglia_bigrammi= Integer.parseInt(attributes.getValue(i));
			}
			if(attributes.getValue(i).equals(Consts.T_SIMPLE)){
				i++;
				Config.sogliaTerminiSemplici = Integer.parseInt(attributes.getValue(i));
			}
			if(attributes.getValue(i).equals(Consts.T_COMPLEX)){
				i++;
				Config.sogliaTerminiComplessi= Integer.parseInt(attributes.getValue(i));
			}
			if(attributes.getValue(i).equals(Consts.T_RELATED)){
				i++;
				Config.sogliaTerminiRelated= Integer.parseInt(attributes.getValue(i));
			}
			if(attributes.getValue(i).equals(Consts.SOGLIA_VARIANTS)){
				i++;
				Config.sogliaVarianti = Integer.parseInt(attributes.getValue(i));
			}

			if(attributes.getValue(i).equals(Consts.MEASURE_TYPE)){
				i++;
				Config.measureType = attributes.getValue(i);
			}

			//System.out.print("parseThresholdTag attributes : "+attributes.getQName(i));
			//System.out.println(": "+attributes.getValue(i));
		}
	}

	public static void parseDatabaseTag(Attributes attributes){
		for (int i = 0; i < attributes.getLength(); i++){
			if(attributes.getValue(i).equals(Consts.DB_SERVERNAME)){
				i++;
				Config.dbServerName = attributes.getValue(i);
			}
			if(attributes.getValue(i).equals(Consts.DB_PORT)){
				i++;
				Config.dbPortNumber = attributes.getValue(i);
			}
			if(attributes.getValue(i).equals(Consts.DB_NAME)){
				i++;
				Config.dbName = attributes.getValue(i);
			}
			if(attributes.getValue(i).equals(Consts.DB_LOGIN)){
				i++;
				Config.dbLogin = attributes.getValue(i);
			}
			if(attributes.getValue(i).equals(Consts.DB_PASSWD)){
				i++;
				Config.dbPassword = attributes.getValue(i);
			}
			if(attributes.getValue(i).equals(Consts.DB_DRIVER)){
				i++;
				Config.dbDriverName = attributes.getValue(i);
			}
			
			//System.out.print("parseDatabaseTag attributes : "+attributes.getQName(i));
			//System.out.println(": "+attributes.getValue(i));
		}

	}
	public static void parseInfoTag(Attributes attributes){
		for (int i = 0; i < attributes.getLength(); i++){
			//System.out.print(i+" qname: "+attributes.getQName(i)+": value=");
			//System.out.println(attributes.getValue(i));
			
			if(attributes.getValue(i).equals(Consts.EMAIL)){
				i++;
				Config.email = attributes.getValue(i);
			}
			if(attributes.getValue(i).equals(Consts.DOC_REPOSITORY)){
				i++;
				if(!attributes.getValue(i).endsWith(System.getProperty("file.separator"))){
					Config.docRepository = attributes.getValue(i)+System.getProperty("file.separator");
				}else{
					Config.docRepository = attributes.getValue(i);
				}
			}
			if(attributes.getValue(i).equals(Consts.DATA_REPOSITORY)){
				i++;
				//System.out.println(": "+attributes.getValue(i));
				if(!attributes.getValue(i).endsWith(System.getProperty("file.separator"))){
					Config.dataRepository = attributes.getValue(i)+System.getProperty("file.separator");
				}else{
					Config.dataRepository = attributes.getValue(i);
				}
				//System.out.println(": "+Config.dataRepository);
			}
			if(attributes.getValue(i).equals(Consts.TMPDIR)){
				i++;
				//System.out.println(": "+attributes.getValue(i));
				if(!attributes.getValue(i).endsWith(System.getProperty("file.separator"))){
					Config.tempOutDir = attributes.getValue(i)+System.getProperty("file.separator");
				}else{
					Config.tempOutDir = attributes.getValue(i);
				}
				//System.out.println(": "+Config.dataRepository);
			}
			if(attributes.getValue(i).equals(Consts.TABLE_SUFFIX)){
				i++;
				Config.tableSuffix = attributes.getValue(i);
				Config.dbTableDocument = Config.dbTableDocument.concat(Config.tableSuffix);
				Config.dbTableGlossario = Config.dbTableGlossario.concat(Config.tableSuffix);
				Config.dbTableGlossarioBT = Config.dbTableGlossarioBT.concat(Config.tableSuffix);
				Config.dbTableGlossarioNT = Config.dbTableGlossarioNT.concat(Config.tableSuffix);
				Config.dbTableGlossarioRT = Config.dbTableGlossarioRT.concat(Config.tableSuffix);
				Config.dbTableGlossarioFreq = Config.dbTableGlossarioFreq.concat(Config.tableSuffix);
				
			}
				
			if(attributes.getValue(i).equals(Consts.EMAIL)){
				i++;
				Config.email = attributes.getValue(i);
			}
			if(attributes.getValue(i).equals(Consts.EMAILADM)){
				i++;
				Config.emaildmin = attributes.getValue(i);
			}
			if(attributes.getValue(i).equals(Consts.MAILSERVER)){
				i++;
				Config.mailserver = attributes.getValue(i);
			}
			
			if(attributes.getValue(i).equals(Consts.ID_REQ)){
				i++;
				Config.idRequest = attributes.getValue(i);
			}
			if(attributes.getValue(i).equals(Consts.ENABLE_COORD)){
				i++;
				if(("yes".equals(attributes.getValue(i))) || ("si".equals(attributes.getValue(i)))){ 
					Config.enableCoord = true;
				}else{
					Config.enableCoord = false;
				}
			}
			if(attributes.getValue(i).equals(Consts.ENABLE_RELATED)){
				i++;
				if (("yes".equals(attributes.getValue(i))) || ("si".equals(attributes.getValue(i)))){ 
					Config.enableRelated = true;
				}else{
					Config.enableRelated = false;
				}
			}
			if(attributes.getValue(i).equals(Consts.ENABLE_UPDATE)){
				i++;
				if (("yes".equals(attributes.getValue(i))) || ("si".equals(attributes.getValue(i)))){ 
					Config.enableUpdate = true;
				}else{
					Config.enableUpdate = false;
				}
			}
			if(attributes.getValue(i).equals(Consts.ENABLE_STOP)){
				i++;
				if (("yes".equals(attributes.getValue(i))) || ("si".equals(attributes.getValue(i)))){ 
					Config.enableStop = true;
				}else{
					Config.enableStop = false;
				}
			}
			if(attributes.getValue(i).equals(Consts.RELATED_ON_CHUG)){
				i++;
				if (("yes".equals(attributes.getValue(i))) || ("si".equals(attributes.getValue(i)))){ 
					Config.relatedOnChug = true;
				}else{
					Config.relatedOnChug = false;
				}
			}
			if(attributes.getValue(i).equals(Consts.ENABLE_CUSTOMIZED_NOUNS)){
				i++;
				if (("yes".equals(attributes.getValue(i))) || ("si".equals(attributes.getValue(i)))){ 
					Config.customizedNouns = true;
				}else{
					Config.customizedNouns = false;
				}
			}
			if(attributes.getValue(i).equals(Consts.ENABLE_VARIANTS)){
				i++;
				if (("yes".equals(attributes.getValue(i))) || ("si".equals(attributes.getValue(i)))){ 
					Config.enableVariants = true;
				}else{
					Config.enableVariants = false;
				}
			}

			if(attributes.getValue(i).equals(Consts.ADJ_POTGOV)){
				i++;
				if (("yes".equals(attributes.getValue(i))) || ("si".equals(attributes.getValue(i)))){ 
					Config.adjPotgov = true;
				}else{
					Config.adjPotgov = false;
				}
				
			}
			if(attributes.getValue(i).equals(Consts.ENABLE_DOCINDEX)){
				i++;
				if (("yes".equals(attributes.getValue(i))) || ("si".equals(attributes.getValue(i)))){ 
					Config.enableDocIndex = true;
				}else{
					Config.enableDocIndex = false;
				}
			}
				//System.out.print("parseThresholdTag attributes : "+attributes.getQName(i));
			//System.out.println(": "+attributes.getValue(i));
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ParserXmlConfig parser = new ParserXmlConfig();
		//parser.parseXMLFile(args[0]);
		parser.parseXMLString("<config-t2k> <threshold type=\"glossario\" value=\"3\"/>" +
				"<database type=\"servername\" value=\"192.168.1.2\"/>		" +
				"<database type=\"port\" value=\"\"/>" +
				"<database type=\"name\" value=\"t2k\"/>" +
				"<database type=\"login\" value=\"t2k\" />" +
				"<database type=\"password\" value=\"t2k\"/>" +
				"<database type=\"driver\" value=\"jdbc:jtds:sqlserver\"/>" +
				"</config-t2k>");
	}

	
}
