package lastfm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class GenreSimilarity {
	static HashMap<String, User> hmUser;	
	
	
	static HashMap<String, Boolean> topGenreList = new HashMap<String, Boolean>();
	public static void getTopTrackGenre(int count){
		HashMap<String, Integer> topGenre = new HashMap<String, Integer>();
		ValueComparator bvc =  new GenreSimilarity().new ValueComparator(topGenre);
		TreeMap<String, Integer> sortedTopGenre = new TreeMap(bvc);
		Iterator<User> it = hmUser.values().iterator();
		while(it.hasNext()){
			User user = it.next();
			Set<Object> userTracks = user.getHsTracks().keySet();
			Iterator<Object> trackIt = userTracks.iterator();
			while(trackIt.hasNext()){
				Track tr = (Track) trackIt.next();
				if(tr != null){
					String tagString = tr.getTagName();
					//System.out.println("tag is "+tagString);
					String[] tags = tagString.split(";");
					for(String t : tags){
						//System.out.println("tag after split is "+t);
						if(topGenre.containsKey(t)){
							topGenre.put(t, topGenre.get(t)+1);
						}else{
							topGenre.put(t, 1);
						}
					}
				}
			}
		}
		System.out.println("tag list size is "+topGenre.size());
		sortedTopGenre.putAll(topGenre);
		String[] genre = sortedTopGenre.keySet().toArray(new String[0]);
		for(int i =0 ; i < count; i++){
			topGenreList.put(genre[i], true);
		}
	
	}

	public static HashMap<String, Integer> getUserGenreScore(Set<Object> aTracks){
	HashMap<String, Integer> GenreScore = new HashMap<String, Integer>();
	for(Object at : aTracks){
		String[] tags = ((Track) at).getTagName().split(";");
		for(String t : tags){
			if(GenreScore.containsKey(t)){
				GenreScore.put(t, GenreScore.get(t)+1);
			}
			else{
				if(topGenreList.containsKey(t)){
					GenreScore.put(t, 1);
				}
			}
		}
	}
	return GenreScore;
	}
	
	public static double getGenreSimilarity(User A, User B){
		Set<Object> ATracks = A.getHsTracks().keySet();
		Set<Object> BTracks = B.getHsTracks().keySet();
		HashMap<String, Integer> AScore = getUserGenreScore(ATracks);
		HashMap<String, Integer> BScore = getUserGenreScore(BTracks);
		Iterator<String> it = AScore.keySet().iterator();
		int numerator = 0;
		while(it.hasNext()){
			String key = it.next();
			if(BScore.containsKey(key)){
				Integer aVal = AScore.get(key);
				Integer bVal = BScore.get(key);
				numerator = numerator + aVal*bVal;
			}
		}
		
		return (numerator/( cosineDenominator(AScore.values()) + cosineDenominator(AScore.values()) ));
	}
	
	public static double cosineDenominator(Collection<Integer> values){
		Iterator<Integer> valItr = values.iterator();
		int sqrdSum =0;
		while(valItr.hasNext()){
			int value = valItr.next();
			sqrdSum = sqrdSum + value*value;
		}
		return Math.sqrt(sqrdSum);
	}
	
	public class ValueComparator implements Comparator {
		Map base;
		public ValueComparator(Map base) {
			this.base = base;
		}

		public int compare(Object a, Object b) {
			if((Integer)base.get(a) < (Integer)base.get(b)) {
				return 1;
			} else if((Integer)base.get(a) == (Integer)base.get(b)) {
				return 0;
			} else {
				return -1;
			}
		}
	}

	public static HashMap<String, User> loadUserMap(String filePath) throws JsonParseException, JsonMappingException, IOException{
		ObjectMapper objectMapper = new ObjectMapper();
		HashMap<String, User> userMap = new HashMap<String, User>(); 
		HashMap<String, Object> result =objectMapper.readValue(new File(filePath), new TypeReference<HashMap<String,Object>>(){});
		Iterator it = result.keySet().iterator();
		BufferedWriter writer =  new BufferedWriter(new FileWriter(new File("out2")));
		while(it.hasNext()){
			String id = it.next().toString();
			User u = new User();
			Map uMap = (Map) result.get(id);
			if(result.containsKey("name"))
				u.setName(uMap.get("name").toString());
			if(result.containsKey("age"))
				u.setAge(Integer.parseInt(result.get("age").toString()));
			if(uMap.containsKey("hsTracks")){
				String tracks = uMap.get("hsTracks").toString();
				System.out.println(tracks);
				writer.write(tracks.toString());
				HashMap<Object, ArrayList<String>> hsTracks = new ObjectMapper().readValue( tracks, new TypeReference<HashMap<String,ArrayList<String>>>(){});
				System.out.println(hsTracks.toString());
				u.setHsTracks(hsTracks);
			}
			userMap.put(id, u);
			writer.flush();
			writer.close();
		}
		return userMap;
	}

	public static void main(String[] args){
		String filepath = "/home/neera/workspace/last.fm/src/lastfm/data/spain-2200-users/hmUser_spain_0";
		GenreSimilarity genreSim = new GenreSimilarity();
		try {
			hmUser = LastFm.readUser(filepath);
			System.out.println("users  "+hmUser.size());
			Iterator it = hmUser.keySet().iterator();
			while(it.hasNext()){
				String key = (String) it.next();
				HashMap<Object, ArrayList<String>> tracks = hmUser.get(key).getHsTracks();
				Iterator trackit = tracks.keySet().iterator();
				while(trackit.hasNext()){
					String t = trackit.next().toString();
					Gson gson = new Gson();
					Track userTrack = gson.fromJson(t, Track.class);   
					System.out.println(userTrack.getName());
					System.out.println(userTrack.getTimeofPlay());
					System.out.println(tracks.get(t));
					
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
