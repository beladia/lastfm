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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
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
			HashMap<Object, ArrayList<String>> tracks = getUserTracks(key, u); 
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
							userInfo.setRegistrationDate(LastfmObjectUtil.getTextValue(el, "registered"));
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
	public HashMap<Object, ArrayList<String>> getUserTracks(String key, String u) {
		try{
			if(LastfmMain.hmUser.containsKey(u)){
				return LastfmMain.hmUser.get(u).getHsTracks();
			}
			String url = BASE_URL+"method=user.getrecenttracks&user="+u+"&api_key="+key+"&limit=200";
			HashMap<Object, ArrayList<String>> tracks = new HashMap<Object, ArrayList<String>>();
			WebResource webResource = client.resource(url);
			ClientResponse cr =  webResource.get(ClientResponse.class);;
			if (Response.Status.Family.SUCCESSFUL.equals(cr.getClientResponseStatus().getFamily())) {
				String resString = cr.getEntity(String.class);
				if(resString != null){
					System.out.println(resString);
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
							track.setArtist(artist);
							track.setAlbum(album);
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
		ObjectMapper mapper = new ObjectMapper();
		System.out.println("size of user map "+hmUser.size());
		Iterator<String> it = hmUser.keySet().iterator();
		while(it.hasNext()){
			String user = it.next();
			User userInfo = hmUser.get(user);
			HashMap<Object, ArrayList<String>> tracks = userInfo.getHsTracks();
			HashMap<Object, ArrayList<String>> tracksJson = new HashMap<Object, ArrayList<String>>();
			if(tracks != null){
				Iterator<Object> trkIt = tracks.keySet().iterator();
				while(trkIt.hasNext()){
					Object tk =  trkIt.next();
					Object trkJson = new JSONObject(tk);
					tracksJson.put(trkJson, tracks.get(tk));
				}
			}
			userInfo.setHsTracks(tracksJson);
			hmUser.put(user, userInfo);
		}
		Map<String, Object> userInMap = new HashMap<String, Object>();
		try {
			// write JSON to a file
			mapper.writeValue(new File(outPath+"hmUser_"+country+"_"+count), hmUser);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*	HashMap<String, JSONObject> hmUserForJson = new HashMap<String, JSONObject>();
	Iterator<String> it = hmUser.keySet().iterator();
	while(it.hasNext()){
		String user = it.next();
		User userInfo = hmUser.get(user);
		if(userInfo != null){
			HashMap<Track, ArrayList<String>> tracks = userInfo.getHsTracks();
			HashMap<TrackJson, ArrayList<String>> tracksJson = new HashMap<TrackJson, ArrayList<String>>();
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
					top.add(trk.getTimeofPlay());
					JSONObject trkJsonObj = new JSONObject(trk);
					tracksJson.put(trk, top);
				}
			}
			UserJson userJson = new UserJson();
			userJson.setHsTracks(tracksJson);
			userJson.setAge(userInfo.getAge());
			userJson.setCountry(userInfo.getCountry());
			userJson.setName(userInfo.getName());
			userJson.setGender(userInfo.getGender());
			JSONObject userInfoJson = new JSONObject(userJson);
			hmUserForJson.put(user, userInfoJson);
		}
	}
	 */
	/*	JSONObject userJsonObject = new JSONObject( hmUserForJson );
	try {	
		FileWriter userFile = new FileWriter(outPath+"hmUser_"+country+"_"+count);
		userFile.write(userJsonObject.toString());
		userFile.close();
	} catch (IOException e) {
		e.printStackTrace();
	}
	 */
	//}

	public static void main(String[] args){
		try{
			String key = "e77094ac5bf414726b355017e631d048";
			String USER_ID_FILE = args[0];//"/home/neera/lastfm-data/newdata/userIds-spain_100";
			String outPath = args[1];//"/home/neera/lastfm-data/newdata/";
			String country = args[2];//"spain";
			System.out.println("job started");
			System.out.println("user id file location : "+ USER_ID_FILE);
			System.out.println("output path for user map dump : "+ outPath);
			System.out.println("country name(used for filename) : "+ country);
			GetUserInfoData userInfo = new GetUserInfoData();
			userInfo.loadUsersIdsFromFile(USER_ID_FILE);
			String[] userIdArray = userIds.toArray(new String[0]);
			int count =userIdArray.length;
			for(int i = count-1; i >= 0; i--){
				System.out.println("count = "+i);
				User user = userInfo.getUserInfo(key, userIdArray[i]);
				hmUser.put(userIdArray[i], user);
				//if(i %20 == 0){
				dumpUserData(country, outPath, i);
				//}
			}
		}catch(Exception e){
			e.printStackTrace();
		}

	}
}
