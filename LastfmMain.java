package lastfm;

import java.util.ArrayList;

public class LastfmMain {
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
				
			}
		}
	}
}
