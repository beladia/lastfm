package lastfm;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.json.JSONObject;

public class LastfmMain {
	//public static String outpath = "/home/neera/lastfm-data/";
	public static String outpath = "/home/neera/lastfm-data/";
	public static String COUNTRY = "germany";
	//user -> list of friends
	public static HashMap<String, HashSet<String>> hmFriends = new HashMap<String, HashSet<String>>();
	//user -> userobject;
	public static HashMap<String, User> hmUser = new HashMap<String, User>();
	public static HashMap<String, String> hmTrackTags = new HashMap<String, String>();


	public static void main(String[] args){
		String key = "e77094ac5bf414726b355017e631d048";
		Authorization auth = new Authorization();
		LastfmObjects lastfmObj = new LastfmObjects();

		String token = auth.getAuthorization(key);
		//geo get event
		int eventCount = 0;
		int userCnt = 0;
		//ArrayList<String> events = new ArrayList<String>(); 
		ArrayList<String> events = lastfmObj.getEventsByLocation(key, COUNTRY);
		//ArrayList<String> franceEvents = lastfmObj.getEventsByLocation(key, "france");
		//ArrayList<String> usEvents = lastfmObj.getEventsByLocation(key, "US");
		//ArrayList<String> ukEvents = lastfmObj.getEventsByLocation(key, "UK");
		System.out.println("number of "+COUNTRY+" events "+events);
		events.addAll(events);
		//events.addAll(franceEvents);
		//events.addAll(usEvents);
		//events.addAll(ukEvents);
		if(events != null && events.size() > 0){
			int eventNo = events.size();
			
			//for(int x = eventNo-10 ; x > 0; x--){
			for(int x = 4 ; x < eventNo-1; x++){
				//get attendees for each events
				String e = events.get(x);
				System.out.println("processing for event id:: "+e);
				ArrayList<String> attendees = lastfmObj.getAttendeesByEvents(key, e);
				System.out.println("got attendees"+attendees.size());
				for(String u : attendees.toArray(new String[0])){
					HashSet<String> friends = lastfmObj.getUserFriends(key, u);
					//System.out.println("got friends"+friends.size());
					hmFriends.put(u, friends);
					User userInfo = lastfmObj.getUserInfo(key, u);
					System.out.println("got user info");
					hmUser.put(u, userInfo);
					userCnt ++;
					//if(userCnt % 1 == 0){
						dumpMaps(userCnt);
					//}
					for (String f : friends){
						User info = lastfmObj.getUserInfo(key, f);
						hmUser.put(f, info);

						HashSet<String> fr = lastfmObj.getUserFriends(key, f);
						hmFriends.put(f, fr);

					}

				}
				eventCount ++;

			}
		}

	}

	public static void dumpMaps(int count){
		System.out.println("size of friends map "+hmFriends.size());
		System.out.println("size of user map "+hmUser.size());

		HashMap<String, JSONObject> hmUserForJson = new HashMap<String, JSONObject>();
		Iterator<String> it = hmUser.keySet().iterator();
		while(it.hasNext()){
			String user = it.next();
			User userInfo = hmUser.get(user);
			if(userInfo != null){
				HashMap<Object, ArrayList<String>> tracks = userInfo.getHsTracks();
				HashMap<TrackJson, ArrayList<String>> tracksJson = new HashMap<TrackJson, ArrayList<String>> ();
				ArrayList<String> timeOfPlay = new ArrayList<String>();
				if(tracks != null && tracks.size() > 0){
					for(Object tObj :  tracks.keySet()){
						Track t = (Track) tObj;
						TrackJson trk = new TrackJson();
						trk.setAlbum((t.getAlbum()));
						trk.setArtist(t.getArtist());
						trk.setName(t.getName());
						trk.setID(t.getID());
						trk.setDuration(t.getDuration());
						trk.setListeners(t.getListeners());
						trk.setPlayCount(t.getPlayCount());
						trk.setTimeofPlay(t.getTimeofPlay());
						trk.setTagName(t.getTagName());
						timeOfPlay.add(trk.getTimeofPlay());
						JSONObject trkJsonObj = new JSONObject(trk);
						tracksJson.put(trk, timeOfPlay);
					}
				}
				UserJson userJson = new UserJson ();
				userJson.setHsTracks(tracksJson);

				JSONObject userInfoJson = new JSONObject(userJson);
				hmUserForJson.put(user, userInfoJson);
			}
		}

		JSONObject friendsJsonObject = new JSONObject( hmFriends );
		JSONObject userJsonObject = new JSONObject( hmUserForJson );
		try {	
			FileWriter friendsFile = new FileWriter(outpath+"dumps/hmfriends_"+COUNTRY+"_"+count);
			friendsFile.write(friendsJsonObject.toString());
			friendsFile.flush();
			friendsFile.close();

			FileWriter userFile = new FileWriter(outpath+"dumps/hmUser_"+COUNTRY+"_"+count);
			userFile.write(userJsonObject.toString());
			userFile.flush();
			userFile.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}

