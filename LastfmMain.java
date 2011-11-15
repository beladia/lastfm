package lastfm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class LastfmMain {
	public static String outpath = "/home/neera/lastfm-data/";
	//user -> list of friends
	public static HashMap<String, ArrayList<String>> hmFriends = new HashMap<String, ArrayList<String>>();
	//user -> userobject;
	public static HashMap<String, User> hmUser = new HashMap<String, User>();
	
	public static void main(String[] args){
		String key = "e77094ac5bf414726b355017e631d048";
		Authorization auth = new Authorization();
		LastfmObjects lastfmObj = new LastfmObjects();
		
		String token = auth.getAuthorization(key);
		System.out.println(token);
		//lastfmObj.getTopArtistsByCountry(key, "US");
		
		//geo get event
		ArrayList<String> events = lastfmObj.getEventsByLocation(key, "spain");
		if(events != null && events.size() > 0){
			for(String e : events.toArray(new String[0])){
				System.out.println("processing for event id:: "+e);
				//get attendees for each events
				ArrayList<String> attendees = lastfmObj.getAttendeesByEvents(key, e);
				for(String u : attendees.toArray(new String[0])){
					ArrayList<String> friends = lastfmObj.getUserFriends(key, u);
					hmFriends.put(u, friends);
					User userInfo = lastfmObj.getUserInfo(key, u);
					hmUser.put(u, userInfo);
				}
				
			}
		}
	System.out.println("size of friends map "+hmFriends.size());
	System.out.println("size of user map "+hmUser.size());
	
	}
}
