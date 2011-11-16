package lastfm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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
			//System.out.println(url);
			WebResource webResource = client.resource(url);
			ClientResponse cr =  webResource.get(ClientResponse.class);;
			System.out.println(cr.toString());
			if (Response.Status.Family.SUCCESSFUL.equals(cr.getClientResponseStatus().getFamily())) {
				String resString = cr.getEntity(String.class);
				//String fileName = "/home/neera/topArtists";
				String fileName = LastfmObjectUtil.writeXMLToFile(resString, LastfmMain.outpath+"topArtists");
				if(fileName != null){
					Element docEle = LastfmObjectUtil.parseXmlFile(fileName);
					NodeList nl = docEle.getElementsByTagName("artist");
					if(nl != null && nl.getLength() > 0) {
						for(int i = 0 ; i < nl.getLength();i++) {
							Element el = (Element)nl.item(i);
							//System.out.println(LastfmObjectUtil.getTextValue(el, "name"));
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
			//System.out.println(url);
			ArrayList<String> events = new ArrayList<String>();
			WebResource webResource = client.resource(url);
			ClientResponse cr =  webResource.get(ClientResponse.class);;
			//System.out.println(cr.toString());
			if (Response.Status.Family.SUCCESSFUL.equals(cr.getClientResponseStatus().getFamily())) {
				String resString = cr.getEntity(String.class);
				String fileName = LastfmObjectUtil.writeXMLToFile(resString, LastfmMain.outpath+"eventsAt"+location);
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
				String fileName = LastfmObjectUtil.writeXMLToFile(resString, LastfmMain.outpath+"AttendeesForEventId"+eventId);
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

	public HashSet<String> getUserFriends(String key, String u) {
		try{
			// http://ws.audioscrobbler.com/2.0/?method=user.getfriends&user=rj&api_key=b25b959554ed76058ac220.
			String url = BASE_URL+"method=user.getfriends&user="+u+"&api_key="+key;
			//System.out.println(url);
			HashSet<String> friends = new HashSet<String>();
			WebResource webResource = client.resource(url);
			ClientResponse cr =  webResource.get(ClientResponse.class);;
			//System.out.println(cr.toString());
			if (Response.Status.Family.SUCCESSFUL.equals(cr.getClientResponseStatus().getFamily())) {
				String resString = cr.getEntity(String.class);
				String fileName = LastfmObjectUtil.writeXMLToFile(resString, LastfmMain.outpath+"FriendListOf-"+u);
				if(fileName != null){
					Element docEle = LastfmObjectUtil.parseXmlFile(fileName);
					NodeList nl = docEle.getElementsByTagName("user");
					if(nl != null && nl.getLength() > 0) {
						for(int i = 0 ; i < nl.getLength();i++) {
							Element el = (Element)nl.item(i);
							friends.add(LastfmObjectUtil.getTextValue(el, "name"));
						}
					}
				}
				return friends;
			}
			else{
				System.err.println("error in fetching friends");
				return null;
			}
		}catch(Exception e){
			System.err.println("exception caught ");
			e.printStackTrace();
			return null;
		}	}

	public ArrayList<Track> getUserTracks(String key, String u) {
		try{
			// http://ws.audioscrobbler.com/2.0/?method=user.getrecenttracks&user=rj&api_key=b25b959554ed76058
			String url = BASE_URL+"method=user.getrecenttracks&user="+u+"&api_key="+key;
			//System.out.println(url);
			ArrayList<Track> tracks = new ArrayList<Track>();
			WebResource webResource = client.resource(url);
			ClientResponse cr =  webResource.get(ClientResponse.class);;
			//System.out.println(cr.toString());
			if (Response.Status.Family.SUCCESSFUL.equals(cr.getClientResponseStatus().getFamily())) {
				String resString = cr.getEntity(String.class);
				String fileName = LastfmObjectUtil.writeXMLToFile(resString, LastfmMain.outpath+"tracksOf-"+u);
				if(fileName != null){
					Element docEle = LastfmObjectUtil.parseXmlFile(fileName);
					NodeList nl = docEle.getElementsByTagName("track");
					if(nl != null && nl.getLength() > 0) {
						for(int i = 0 ; i < nl.getLength();i++) {
							Element el = (Element)nl.item(i);
							Track track = new Track();
							//System.out.println("track date "+LastfmObjectUtil.getDateValue(el, "date"));
							track.setTimeofPlay(LastfmObjectUtil.getDateValue(el, "date"));
							track.setName(LastfmObjectUtil.getTextValue(el, "name"));
							String artist = LastfmObjectUtil.getTextValue(el, "artist");
							String album = LastfmObjectUtil.getTextValue(el, "album");
							track.setArtist(getArtistInfo(key, artist));
							track.setAlbum(getAlbumInfo(key, album));
							tracks.add(track);
						}
					}
				}
				return tracks;
			}
			else{
				System.err.println("error in fetching user tracks...ignoring");
				return null;
			}
		}catch(Exception e){
			System.err.println("exception caught ");
			e.printStackTrace();
			return null;
		}	}


	public Artist getArtistInfo(String key, String artist){
		Artist ar = new Artist();
		ar.setName(artist);
		return ar;
	}

	public Album getAlbumInfo(String key, String album){
		Album ab = new Album();
		ab.setName(album);
		return ab;
	}

	public User getUserInfo(String key, String u) {
		try{
			// http://ws.audioscrobbler.com/2.0/?method=user.getfriends&user=rj&api_key=b25b959554ed76058ac220.
			String url = BASE_URL+"method=user.getinfo&user="+u+"&api_key="+key;
			Collection<Track> tracks = getUserTracks(key, u); 
			//System.out.println("user "+u +" tracks "+ tracks.size());
			//System.out.println(url);
			User userInfo = new User();
			WebResource webResource = client.resource(url);
			ClientResponse cr =  webResource.get(ClientResponse.class);;
			//System.out.println(cr.toString());
			if (Response.Status.Family.SUCCESSFUL.equals(cr.getClientResponseStatus().getFamily())) {
				String resString = cr.getEntity(String.class);
				String fileName = LastfmObjectUtil.writeXMLToFile(resString, LastfmMain.outpath+"InfoOfUser-"+u);
				if(fileName != null){
					Element docEle = LastfmObjectUtil.parseXmlFile(fileName);
					NodeList nl = docEle.getElementsByTagName("user");
					if(nl != null && nl.getLength() > 0) {
						for(int i = 0 ; i < nl.getLength();i++) {
							Element el = (Element)nl.item(i);
							userInfo.setUserID(LastfmObjectUtil.getTextValue(el, "id"));
							userInfo.setName(LastfmObjectUtil.getTextValue(el, "name"));
							userInfo.setRealname(LastfmObjectUtil.getTextValue(el, "realname"));
							userInfo.setCountry(LastfmObjectUtil.getTextValue(el, "country"));
							userInfo.setGender(LastfmObjectUtil.getTextValue(el, "gender"));
							userInfo.setAge(LastfmObjectUtil.getIntValue(el, "age"));
							userInfo.setPlayCount(LastfmObjectUtil.getIntValue(el, "playcount"));
							userInfo.setPlayLists(LastfmObjectUtil.getIntValue(el, "playlists"));
							userInfo.setRegistrationDate(LastfmObjectUtil.getDateValue(el, "registered"));
							if(tracks != null)
								userInfo.setHsTracks(new HashSet(tracks));
						}
					}
				}
				return userInfo;
			}
			else{
				System.err.println("error in fetching user info");
				return null;
			}
		}catch(Exception e){
			System.err.println("exception caught ");
			e.printStackTrace();
			return null;
		}
	}
}
