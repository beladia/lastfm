package lastfm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class LastfmMain {
	public static String outpath = "/home/neera/lastfm-data/";
	//user -> list of friends
	public static HashMap<String, HashSet<String>> hmFriends = new HashMap<String, HashSet<String>>();
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
		int eventCount = 0;
		int attendeeCnt = 0;
		ArrayList<String> events = lastfmObj.getEventsByLocation(key, "spain");
		if(events != null && events.size() > 0){
			for(String e : events.toArray(new String[0])){
				System.out.println("processing for event id:: "+e);
				//get attendees for each events
				ArrayList<String> attendees = lastfmObj.getAttendeesByEvents(key, e);
				for(String u : attendees.toArray(new String[0])){
					HashSet<String> friends = lastfmObj.getUserFriends(key, u);
					hmFriends.put(u, friends);
					User userInfo = lastfmObj.getUserInfo(key, u);
					hmUser.put(u, userInfo);
					attendeeCnt ++;
					if(attendeeCnt == 30)break;
				}
				eventCount ++;
				if(eventCount == 3)break;
			}
		}
		System.out.println("size of friends map "+hmFriends.size()); 
		Iterator<String> it = hmFriends.keySet().iterator();
		while(it.hasNext()){
			String key1 = it.next();
			String[] friends = hmFriends.get(key1).toArray(new String[0]);
			System.out.print(key1+" friends ");
			for(int k =0 ; k < friends.length; k++)
				System.out.print("  "+friends[k]);
			System.out.println();
		}
 		
		System.out.println("size of user map "+hmUser.size());
		try {
			LastFm.generateTrainData(outpath+"train.dat", 1000);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	
	}
}
