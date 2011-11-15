package lastfm;



import java.util.ArrayList;

import javax.ws.rs.core.Response;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class LastfmObjects {
	private static final String BASE_URL = "http://ws.audioscrobbler.com/2.0/?";
	Client client = JerseyClient.getClient();

	public String getTopArtistsByCountry(String key, String country) {
		try{
			String url = BASE_URL+"method=geo.gettopartists&country=spain&api_key="+key+"&limit=200";
			System.out.println(url);
			WebResource webResource = client.resource(url);
			ClientResponse cr =  webResource.get(ClientResponse.class);;
			System.out.println(cr.toString());
			if (Response.Status.Family.SUCCESSFUL.equals(cr.getClientResponseStatus().getFamily())) {
				String resString = cr.getEntity(String.class);
				//String fileName = "/home/neera/topArtists";
				String fileName = LastfmObjectUtil.writeXMLToFile(resString, "/home/neera/topArtists");
				if(fileName != null){
					Element docEle = LastfmObjectUtil.parseXmlFile(fileName);
					NodeList nl = docEle.getElementsByTagName("artist");
					if(nl != null && nl.getLength() > 0) {
						for(int i = 0 ; i < nl.getLength();i++) {
							Element el = (Element)nl.item(i);
							System.out.println(LastfmObjectUtil.getTextValue(el, "name"));
							//TODO populate artist object here....
						}
					}
				}
			}
			else{
				System.err.println("error in fetching us top artists");
				return null;
			}
		}catch(Exception e){
			System.err.println("exception caught ");
			e.printStackTrace();
		}
		return null;
	}

	public ArrayList<String>  getEventsByLocation(String key, String location){
		try{
			String url = BASE_URL+"method=geo.getevents&location="+location+"&api_key="+key;
			System.out.println(url);
			ArrayList<String> events = new ArrayList<String>();
			WebResource webResource = client.resource(url);
			ClientResponse cr =  webResource.get(ClientResponse.class);;
			System.out.println(cr.toString());
			if (Response.Status.Family.SUCCESSFUL.equals(cr.getClientResponseStatus().getFamily())) {
				String resString = cr.getEntity(String.class);
				String fileName = LastfmObjectUtil.writeXMLToFile(resString, "/home/neera/eventsAt"+location);
				if(fileName != null){
					Element docEle = LastfmObjectUtil.parseXmlFile(fileName);
					NodeList nl = docEle.getElementsByTagName("event");
					if(nl != null && nl.getLength() > 0) {
						for(int i = 0 ; i < nl.getLength();i++) {
							Element el = (Element)nl.item(i);
							//System.out.println(LastfmObjectUtil.getTextValue(el, "title"));
							events.add(LastfmObjectUtil.getTextValue(el, "id"));

						}
					}
				}
				return events;
			}
			else{
				System.err.println("error in fetching us top artists");
				return null;
			}
		}catch(Exception e){
			System.err.println("exception caught ");
			e.printStackTrace();
			return null;
		}
	}

	public ArrayList<String> getAttendeesByEvents(String key, String eventId) {
		try{
			String url = BASE_URL+"method=event.getattendees&event="+eventId+"&api_key="+key;
			System.out.println(url);
			ArrayList<String> attendees = new ArrayList<String>();
			WebResource webResource = client.resource(url);
			ClientResponse cr =  webResource.get(ClientResponse.class);;
			//System.out.println(cr.toString());
			if (Response.Status.Family.SUCCESSFUL.equals(cr.getClientResponseStatus().getFamily())) {
				String resString = cr.getEntity(String.class);
				String fileName = LastfmObjectUtil.writeXMLToFile(resString, "/home/neera/AttendeesForEventId"+eventId);
				if(fileName != null){
					Element docEle = LastfmObjectUtil.parseXmlFile(fileName);
					NodeList nl = docEle.getElementsByTagName("user");
					if(nl != null && nl.getLength() > 0) {
						for(int i = 0 ; i < nl.getLength();i++) {
							Element el = (Element)nl.item(i);
							System.out.println(LastfmObjectUtil.getTextValue(el, "name"));
							attendees.add(LastfmObjectUtil.getTextValue(el, "name"));
						}
					}
				}
				return attendees;
			}
			else{
				System.err.println("error in fetching us top artists");
				return null;
			}
		}catch(Exception e){
			System.err.println("exception caught ");
			e.printStackTrace();
			return null;
		}
	}

	//http://ws.audioscrobbler.com/2.0/?method=geo.gettopartists&country=spain&api_key=b25b959554ed76...

}
