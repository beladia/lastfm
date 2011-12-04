package lastfm;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class GenerateTempDataSets {

	static HashMap<String, UserOld> hmUser;	
	static HashMap<String, HashSet<String>>  hmFriends;
	static HashMap<String, Boolean>  userIds = new HashMap<String, Boolean>();
	static HashSet<String> seedUserIds = new HashSet<String>();

	@SuppressWarnings("unchecked")
	public static HashMap<String, UserOld> readUser(String filePath) throws IOException{
		Type hmUType = new TypeToken<HashMap<String, UserOld>>() {}.getType();
		HashMap<String, UserOld> hmU = new HashMap<String, UserOld>();

		String jsonString = readFileAsString(filePath);
		hmU = new Gson().fromJson(jsonString, hmUType);

		for (String key : hmU.keySet()){
			hmU.get(key).setUserID(key);
		}
		return hmU;
	}	


	private static String readFileAsString(String filePath) throws java.io.IOException{
		byte[] buffer = new byte[(int) new File(filePath).length()];
		BufferedInputStream f = null;
		try {
			f = new BufferedInputStream(new FileInputStream(filePath));
			f.read(buffer);
		} finally {
			if (f != null) try { f.close(); } catch (IOException ignored) { }
		}
		return new String(buffer);
	}	

	public static HashMap<String, HashSet<String>> readUserFriends(String filePath) throws IOException{
		HashMap<String, ArrayList<String>> tmp;
		HashMap<String, HashSet<String>> hmUF = new HashMap<String, HashSet<String>>();  

		String jsonString = readFileAsString(filePath);
		tmp = (HashMap<String, ArrayList<String>>)new Gson().fromJson(jsonString, HashMap.class);

		for (String key : tmp.keySet()){
			hmUF.put(key, new HashSet<String>(tmp.get(key)));
		}

		return hmUF;
	}

	public static void main(String[] args) throws IOException{				
		String prefixPath = "/home/neera/Downloads/spain-latest-with-trackbugfix/";
		String filePath = prefixPath + "hmUser_spain_54";
		String key = "e77094ac5bf414726b355017e631d048";
		Authorization auth = new Authorization();
		hmUser = readUser(filePath);

		Iterator<String> itr = hmUser.keySet().iterator();
		while(itr.hasNext()){
			String user = itr.next();
			userIds.put(user, true);
			seedUserIds.add(user);
		}

		LastfmObjects lastfmObj = new LastfmObjects();
		ArrayList<String> spainEvents = lastfmObj.getEventsByLocation(key, "spain");

		for(int x = 10 ; x < spainEvents.size(); x++){
			ArrayList<String> attendees = lastfmObj.getAttendeesByEvents(key, spainEvents.get(x));
			Iterator<String> attItr = attendees.iterator();
			while(attItr.hasNext()){
				String userId = attItr.next();
				userIds.put(userId, true);
				seedUserIds.add(userId);
			}

		}

		filePath = prefixPath + "hmfriends_spain_54";
		hmFriends = readUserFriends(filePath);
		Iterator<String> it = seedUserIds.iterator();
		String user = null;
		int count =0;
		while(it.hasNext()){
			count ++;
			System.out.println("count ="+count);
			user = it.next();
			if(!hmFriends.containsKey(user)){
				HashSet<String> friends = lastfmObj.getUserFriends(key, user);
				if(friends != null){
					hmFriends.put(user, friends);
					for (String f : friends){
						userIds.put(f, true);
						if(!hmFriends.containsKey(f)){
							HashSet<String> fr = lastfmObj.getUserFriends(key, f);
							if(fr != null){
								hmFriends.put(f, fr);
								for (String fof : fr){
									userIds.put(fof, true);
								}
							}
						}
					}
				}
			}
			if(count % 100 == 0){
			Iterator<String> userIdsItr = userIds.keySet().iterator();
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File("/home/neera/lastfm-data/newdata/userIds-spain_"+count)));
			while(it.hasNext()){
				writer.write(it.next()+"\n");
			}
			//writer.flush();
			writer.close();
			JSONObject friendsJsonObject = new JSONObject( hmFriends );
			FileWriter friendsFile = new FileWriter("/home/neera/lastfm-data/newdata/friendsMap-spain_"+count);
			friendsFile.write(friendsJsonObject.toString());
			//friendsFile.flush();
			friendsFile.close();
		 }
		}

	}

}
