package lastfm;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

public class GetUserInfoData {


	HashMap<String, String> hmTracktags = new HashMap<String, String>();
	static ArrayList<String> userIds = new ArrayList<String>();
	public static HashMap<String, User> hmUser = new HashMap<String, User>();
	
	private static final String BASE_URL = "http://ws.audioscrobbler.com/2.0/?";
	Client client = JerseyClient.getClient();
	//load unserIds from file
	public void loadUsersIdsFromFile(String filename) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));	
		String line = null;
		while((line = reader.readLine()) != null){
			userIds.add(line);
		}
	}

	//for each user Id call getInfo
	public User getUserInfo(String key, String u) {
		try{
			String url = BASE_URL+"method=user.getinfo&user="+u+"&api_key="+key;
			HashMap<Track, ArrayList<String>> tracks = getUserTracks(key, u); 
			User userInfo = new User();
			WebResource webResource = client.resource(url);
			ClientResponse cr =  webResource.get(ClientResponse.class);;
			if (Response.Status.Family.SUCCESSFUL.equals(cr.getClientResponseStatus().getFamily())) {
				String resString = cr.getEntity(String.class);
				if(resString != null){
					ByteArrayInputStream bs = new ByteArrayInputStream(resString.getBytes("UTF-8"));
					Element docEle = LastfmObjectUtil.parseXml(bs);
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
								userInfo.setHsTracks(tracks);
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

	//call get tracks to get time of play
	public HashMap<Track, ArrayList<String>> getUserTracks(String key, String u) {
		try{
			if(LastfmMain.hmUser.containsKey(u)){
				return LastfmMain.hmUser.get(u).getHsTracks();
			}
			String url = BASE_URL+"method=user.getrecenttracks&user="+u+"&api_key="+key+"&limit=200";
			HashMap<Track, ArrayList<String>> tracks = new HashMap<Track, ArrayList<String>>();
			WebResource webResource = client.resource(url);
			ClientResponse cr =  webResource.get(ClientResponse.class);;
			if (Response.Status.Family.SUCCESSFUL.equals(cr.getClientResponseStatus().getFamily())) {
				String resString = cr.getEntity(String.class);
				if(resString != null){
					ByteArrayInputStream bs = new ByteArrayInputStream(resString.getBytes("UTF-8"));
					Element docEle = LastfmObjectUtil.parseXml(bs);
					NodeList nl = docEle.getElementsByTagName("track");
					if(nl != null && nl.getLength() > 0) {
						for(int i = 0 ; i < nl.getLength();i++) {
							Element el = (Element)nl.item(i);
							Track track = new Track();
							track.setTimeofPlay(LastfmObjectUtil.getTextValue(el, "date"));
							String trackName = LastfmObjectUtil.getTextValue(el, "name");
							track.setName(trackName);
							String artist = LastfmObjectUtil.getTextValue(el, "artist");
							String tagName = getTrackTagName(trackName, artist, key);
							track.setTagName(tagName);
							String album = LastfmObjectUtil.getTextValue(el, "album");
							track.setArtist(getArtistInfo(key, artist));
							track.setAlbum(getAlbumInfo(key, album));
							if(tracks.containsKey(track)){
								ArrayList<String> timeOfPlayList = tracks.get(track);
								String newTimeOfPlay = track.getTimeofPlay();
								timeOfPlayList.add(newTimeOfPlay);
								tracks.put(track, timeOfPlayList);
							}else{
								ArrayList<String> timeOfPlayList = new ArrayList();
								timeOfPlayList.add(track.getTimeofPlay());
								tracks.put(track, timeOfPlayList);	
							}
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


	//use hashmap to store tags
	private String getTrackTagName(String trackName, String artist, String key) throws UnsupportedEncodingException, ClientHandlerException, UniformInterfaceException, JSONException {
		if(LastfmMain.hmTrackTags.containsKey(trackName)){
			return LastfmMain.hmTrackTags.get(trackName);
		}
		String subStr = "track="+trackName+"&artist="+artist;
		String url = BASE_URL+"method=track.getinfo&api_key="+key+"&track="+URLEncoder.encode(trackName, "UTF-8")+"&artist="+URLEncoder.encode(artist, "UTF-8");
		StringBuffer trackTags = new StringBuffer();
		WebResource webResource = client.resource(url);
		ClientResponse cr =  webResource.get(ClientResponse.class);;
		if (Response.Status.Family.SUCCESSFUL.equals(cr.getClientResponseStatus().getFamily())) {
			String resString = cr.getEntity(String.class);
			if(resString != null){
				ByteArrayInputStream bs = new ByteArrayInputStream(resString.getBytes("UTF-8"));
				Element docEle = LastfmObjectUtil.parseXml(bs);
				NodeList nl = docEle.getElementsByTagName("track");
				if(nl != null && nl.getLength() > 0) {
					for(int i = 0 ; i < nl.getLength();i++) {
						Element el = (Element) nl.item(i);
						NodeList tagName  =  el.getElementsByTagName("tag");
						for(int j = 0; j < tagName.getLength(); j++){
							Element tag = (Element)tagName.item(j);
							String trkTag =  LastfmObjectUtil.getTextValue(tag, "name");
							if(trkTag != null){
								trackTags.append(trkTag+";");
							}
						}
					}
				}
			}
			LastfmMain.hmTrackTags.put(trackName, trackTags.toString());
			return trackTags.toString();
		}else{
			System.err.println("error in fetching user tracks...ignoring");
			return null;	
		}
	}

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
	
	
	public static void dumpUserData(String country, String outPath, int count){
		System.out.println("size of user map "+hmUser.size());
		HashMap<String, JSONObject> hmUserForJson = new HashMap<String, JSONObject>();
		Iterator<String> it = hmUser.keySet().iterator();
		while(it.hasNext()){
			String user = it.next();
			User userInfo = hmUser.get(user);
			if(userInfo != null){
				HashMap<Track, ArrayList<String>> tracks = userInfo.getHsTracks();
				HashMap<JSONObject, ArrayList<String>> tracksJson = new HashMap<JSONObject, ArrayList<String>>();
				ArrayList<String> top = new ArrayList<String>();
				if(tracks != null && tracks.size() > 0){
					for(Track t : tracks.keySet()){
						TrackJson trk = new TrackJson();
						trk.setAlbum(new JSONObject(t.getAlbum()));
						trk.setArtist(new JSONObject(t.getArtist()));
						trk.setName(t.getName());
						trk.setID(t.getID());
						trk.setDuration(t.getDuration());
						trk.setListeners(t.getListeners());
						trk.setPlayCount(t.getPlayCount());
						trk.setTimeofPlay(t.getTimeofPlay());
						trk.setTagName(t.getTagName());
						JSONObject trackJson = new JSONObject(trk);
						top.add(trk.getTimeofPlay());
						tracksJson.put(trackJson, top);
					}
				}
				UserJson userJson = new UserJson();
				userJson.setHsTracks(tracksJson);

				JSONObject userInfoJson = new JSONObject(userJson);
				hmUserForJson.put(user, userInfoJson);
			}
		}

		JSONObject userJsonObject = new JSONObject( hmUserForJson );
		try {	
			FileWriter userFile = new FileWriter(outPath+"hmUser_"+country+"_"+count);
			userFile.write(userJsonObject.toString());
			userFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args){
		try{
			String key = "e77094ac5bf414726b355017e631d048";
			String USER_ID_FILE = "/home/neera/lastfm-data/newdata/userIds-spain_100";
			String outPath = "/home/neera/lastfm-data/newdata/";
			String country = "spain";
			
			GetUserInfoData userInfo = new GetUserInfoData();
			userInfo.loadUsersIdsFromFile(USER_ID_FILE);
			Iterator<String> it = userIds.iterator();
			int count =0;
			while(it.hasNext()){
				count ++;
				System.out.println("count = "+count);
				String userId = it.next();
				User user = userInfo.getUserInfo(key, userId);
				hmUser.put(userId, user);
				if(count %20 == 0){
					dumpUserData(country, outPath, count);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}

	}
}
