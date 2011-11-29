package lastfm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class LastfmObjectUtil {
	public static String writeXMLToFile(String resString, String fileName) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileName)));
			writer.write(resString);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return fileName;
	}

	public static  Document loadXMLFromString(String xml) throws Exception
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource is = new InputSource(new StringReader(xml));
		return builder.parse(is);
	}

	public static Element parseXmlFile(String fileName){
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
		
			Document dom = db.parse(fileName);
			Element docEle = dom.getDocumentElement();

			//NodeList nl = docEle.getElementsByTagName("artist");
			return docEle;

		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
		return null;
	}

	public static Element parseXml(InputStream stream){
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
		
			Document dom = db.parse(stream);
			Element docEle = dom.getDocumentElement();

			//NodeList nl = docEle.getElementsByTagName("artist");
			return docEle;

		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
		return null;
	}

	public static String getTextValue(Element ele, String tagName) {
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if(nl != null && nl.getLength() > 0) {
			Element el = (Element)nl.item(0);
			if(el == null) return null;
			if(el.getFirstChild() == null )return null;
			textVal = el.getFirstChild().getNodeValue();
		}

		return textVal;
	}
	
	public static int getIntValue(Element ele, String tagName) {
		String textVal = getTextValue(ele, tagName);
		if(textVal == null) return 0;
		return Integer.parseInt(textVal);
	}
	
	public static Date getDateValue(Element ele, String tagName) {
		String textVal = getTextValue(ele, tagName);
		if(textVal == null) return null;
		//2008-03-10 04:32
		String dateFormat = "dd MMM yyyy, HH:mm";
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		try {
			return sdf.parse(textVal);
		} catch (ParseException e) {
			String dateFormat2 = "yyyy-mm-dd HH:mm";
			SimpleDateFormat sdf2 = new SimpleDateFormat(dateFormat2);
			try {
				return sdf2.parse(textVal);
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
	public static Date parseDate(String tp){
		//2008-03-10 04:32
		String dateFormat = "EEE MMM dd HH:mm:ss zzz yyyy";
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		try {
			return sdf.parse(tp);
		} catch (ParseException e) {
			String dateFormat2 = "yyyy-mm-dd HH:mm";
			SimpleDateFormat sdf2 = new SimpleDateFormat(dateFormat2);
			try {
				return sdf2.parse(tp);
			} catch (ParseException e1) {
				String dateFormat3 = "dd MMM yyyy, HH:mm";
				SimpleDateFormat sdf3 = new SimpleDateFormat(dateFormat3);
				try {
					return sdf3.parse(tp);
				} catch (ParseException e2) {				
					e1.printStackTrace();
				}
			}
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	

}
