package lastfm;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class LastFm {
	//HashMap<String, HashSet<String>> hmFriends;
	//HashMap<String, User> hmUser;	

	static HashMap<String, HashSet<String>>  hmFriends;
	static HashMap<String, User> hmUser;	
	static HashMap<String, Integer> hmUserID = new HashMap<String, Integer>();

	public static Date parseDate(String tp){
		if (tp == null)
			return null;

		//System.out.println("Time = "+tp);

		//2008-03-10 04:32
		String dateFormat = "EEE MMM dd HH:mm:ss zzz yyyy";
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		try {
			//System.out.println("Date = "+tp);
			return sdf.parse(tp);
		} catch (ParseException e) {
			String dateFormat2 = "yyyy-mm-dd HH:mm";
			SimpleDateFormat sdf2 = new SimpleDateFormat(dateFormat2);			
			try {
				//System.out.println("Date processed under sdf2 = "+sdf2.parse(tp));
				return sdf2.parse(tp);				
			} catch (ParseException e1) {
				String dateFormat3 = "dd MMM yyyy, HH:mm";
				SimpleDateFormat sdf3 = new SimpleDateFormat(dateFormat3);
				try {
					//System.out.println("Date processed under sdf3 = "+sdf3.parse(tp));
					return sdf3.parse(tp);
				} catch (ParseException e2) {				
					e1.printStackTrace();
				}
			}

			e.printStackTrace();
		}
		return null;
	}	

	// Calculate influence score of User A on User B
	public static double calculateInfluence(User A, User B){
		double influence = 0.0;
		int count = 0;
		double days = 0.0;

		if ((A == null) || (B == null))
			return 0.0;

		// Get Common Tracks played by User A and User B
		HashMap<Object, ArrayList<String>> aTracks = A.getHsTracks();
		HashMap<Object, ArrayList<String>> bTracks = B.getHsTracks();

		HashSet<Object> intersect = new HashSet<Object>();
		intersect.addAll(aTracks.keySet());
		intersect.retainAll(bTracks.keySet());

		ArrayList<String> aTimeList, bTimeList;
		double minDays = -99;
		// For each of the common tracks calculate a cumulative influence
		for (Object t : intersect) {
			aTimeList = aTracks.get(t);
			bTimeList = bTracks.get(t);

			for (int i=0; i<aTimeList.size();i++) {
				minDays = -99;
				for (int j=0; j<bTimeList.size();j++) {
					Date aDate = parseDate(aTimeList.get(i));
					Date bDate = parseDate(bTimeList.get(j));

					if(aDate.before(bDate)){						
						days = (bDate.getTime() - aDate.getTime())/(1000*60*60*24);

						if (minDays == -99)
							minDays = days;
						else if (days < minDays)
							minDays = days;
					}										
				}

				if (minDays != -99) {
					if (minDays == 0)
						influence += 1;
					else influence += Math.exp(-1 * minDays);

					count++;
				}
			}
		}
		/**		

		Iterator<Track> it =  aTracks.keySet().iterator();
		while(it.hasNext()){
			Track track = it.next();
			ArrayList<String> aTimeOfPlay = aTracks.get(track);
			if(aTimeOfPlay != null){
				if(bTracks.containsKey(track)){
					ArrayList<String> bTimeOfPlay = bTracks.get(track);
					if(bTimeOfPlay != null){
						Date aDate = parseDate(aTimeOfPlay.get(0));
						Date bDate = parseDate(bTimeOfPlay.get(0));
						if(aDate.before(bDate)){
							days = (bDate.getTime() - aDate.getTime())/(1000*60*60*24);
							if (days == 0)
								influence += 1;
							else influence += Math.exp(-1 * days);
						}
					}
				}
			}	
		}

		if (influence > 0)
			influence = influence / B.getHsTracks().size();	
		influence = Math.rint(influence * 1000.0d) / 1000.0d;


		 **/

		if ((influence > 0) && (count > 0))
			influence = (double)influence / count;	
		influence = Math.rint(influence * 1000.0d) / 1000.0d;

		return influence;		
	}


	// Calculate overall influence score of User A
	public static double calculateOverallInfluence(User A){
		double influence = 0.0;
		double weightSum = 0.0;
		User B;

		// Get User A's friends
		HashSet<String> friends = null;
		if (A != null)
			friends = hmFriends.get(A.getUserID());
		else return 0.0;

		if (friends != null && (friends.size() > 0)){
			// For each of User A's friend B, calculate influence(A, B)
			for (String friend : friends){
				B = hmUser.get(friend);
				if ((A != null) && (B != null)){
					influence += (B.getHsTracks().size() * calculateInfluence(A, B));
					weightSum += B.getHsTracks().size();
				}
			}

			if (influence > 0)
				influence = influence / weightSum;
		}
		else influence = 0;

		influence = Math.rint(influence * 1000.0d) / 1000.0d;

		return influence;		
	}


	// Calculate average influence of common neighbors of User A and User B
	public static double calculateCommonNeighborAvgInfluence(User A, User B){
		double influence = 0.0;

		if ((A==null) || (B==null))
			return 0.0;

		HashSet<String> common = new HashSet<String>();
		if(hmFriends.get(A.getUserID()) != null)
			common.addAll(hmFriends.get(A.getUserID()));
		if(hmFriends.get(B.getUserID()) != null)
			common.retainAll(hmFriends.get(B.getUserID()));

		if (common.size() > 0){
			for (String c : common){
				influence += calculateOverallInfluence(hmUser.get(c));
			}

			if (influence > 0)
				influence = influence / common.size();
		}


		return influence;
	}


	// Calculate average influence of common neighbors of User A and User B on User A/User B
	public static double calculateInfluenceOfCommonNeighborAvgOn(User A, User B, String On){
		double influence = 0.0;
		User OnNode = null;

		if (On.toUpperCase().equals("A"))
			OnNode = A;
		else if (On.toUpperCase().equals("B"))
			OnNode = B;

		if ((A==null) || (B==null))
			return 0.0;

		HashSet<String> common = new HashSet<String>();
		if(hmFriends.get(A.getUserID()) != null)
			common.addAll(hmFriends.get(A.getUserID()));
		if(hmFriends.get(B.getUserID()) != null)
			common.retainAll(hmFriends.get(B.getUserID()));

		if (common.size() > 0){
			for (String c : common){
				influence += calculateInfluence(hmUser.get(c), OnNode);
			}

			if (influence > 0)
				influence = influence / common.size();
		}

		influence = Math.rint(influence * 1000.0d) / 1000.0d;
		return influence;
	}




	// Calculate User A's concentration of influence on common neighbors with User B vs. all neighbors
	public static double calculateInfluenceOnCommonNeighbors(User A, User B){
		double influence = 0.0;
		double totalInfluence = 0.0;

		if ((A==null) || (B==null))
			return 0.0;

		HashSet<String> common = new HashSet<String>();
		if(hmFriends.get(A.getUserID()) != null)
			common.addAll(hmFriends.get(A.getUserID()));
		if(hmFriends.get(B.getUserID()) != null)
			common.retainAll(hmFriends.get(B.getUserID()));

		if (common.size() > 0){
			for (String c : common){
				influence += calculateInfluence(A, hmUser.get(c));
			}

			for (String nbhA : hmFriends.get(A.getUserID())){
				totalInfluence += calculateInfluence(A, hmUser.get(nbhA));
			}

			if (influence > 0)
				influence = influence / totalInfluence;
		}

		influence = Math.rint(influence * 1000.0d) / 1000.0d;

		return influence;
	}	

	// Calculate Average 2-hop Influence path length between User A and User B
	public static double calculateAvg2HopInfluencePathLength(User A, User B){
		double influence = 0.0;

		if ((A==null) || (B==null))
			return 0.0;

		HashSet<String> common = new HashSet<String>();
		if(hmFriends.get(A.getUserID()) != null)
			common.addAll(hmFriends.get(A.getUserID()));
		if(hmFriends.get(B.getUserID()) != null)
			common.retainAll(hmFriends.get(B.getUserID()));

		if (common.size() > 0){
			for (String c : common){
				influence += (calculateInfluence(hmUser.get(c), A) + calculateInfluence(B, hmUser.get(c)));
			}

			if (influence > 0)
				influence = influence / common.size();
		}

		influence = Math.rint(influence * 1000.0d) / 1000.0d;
		return influence;
	}


	// Calculate Average 3-hop Influence path length between User A and User B
	public static double calculateAvg3HopInfluencePathLength(User A, User B){
		double influence = 0.0;

		if ((A==null) || (B==null))
			return 0.0;

		// Get User A's neighbors that are not neighbors of User B
		HashSet<String> noncommon = new HashSet<String>();
		if(hmFriends.get(A.getUserID()) != null)
			noncommon.addAll(hmFriends.get(A.getUserID()));
		if(hmFriends.get(B.getUserID()) != null)
			noncommon.removeAll(hmFriends.get(B.getUserID()));

		int count = 0;
		if (noncommon.size() > 0) {
			for (String ncA : noncommon){
				for (String nbhB : hmFriends.get(A.getUserID())){
					if ((hmUser.get(ncA) != null) && (hmUser.get(nbhB) != null)){
						if (hmFriends.get(ncA).contains(nbhB)){
							count++;
							influence += (calculateInfluence(hmUser.get(ncA), A) + calculateInfluence(hmUser.get(nbhB), hmUser.get(ncA)) + calculateInfluence(B, hmUser.get(nbhB)));
						}
					}
				}

			}

			if (influence > 0)
				influence = influence / count;
		}

		influence = Math.rint(influence * 1000.0d) / 1000.0d;
		return influence;
	}

	// Calculate Track Similarity between 2 users
	public static double calculateTrackSimilarity(User A, User B, String wrto){
		double sim = 0.0;

		if ((A==null) || (B==null))
			return 0.0;

		if ((A.getHsTracks().size() == 0) || (B.getHsTracks().size() == 0))
			return 0.0;

		// Intersection set for numerator for Jaccard sim calculation		
		HashSet<Object> intersect = new HashSet<Object>();
		intersect.addAll(A.getHsTracks().keySet());
		intersect.retainAll(B.getHsTracks().keySet());

		if (wrto.toUpperCase().equals("ALL")){
			HashSet<Object> union = new HashSet<Object>();
			union.addAll(A.getHsTracks().keySet());
			union.addAll(B.getHsTracks().keySet());

			sim = (double)intersect.size()/union.size();
		}
		else if (wrto.toUpperCase().equals("A")){
			sim = (double)intersect.size()/A.getHsTracks().size();
		}
		else if (wrto.toUpperCase().equals("B")){
			sim = (double)intersect.size()/B.getHsTracks().size();
		}

		sim = Math.rint(sim * 1000.0d) / 1000.0d;

		return sim;
	}

	// Calculate similarity between users based on tag similarity
	public static double calculateTagSimilarity(User A, User B, String wrto){
		double sim = 0.0;

		if ((A==null) || (B==null))
			return 0.0;

		if ((A.getHsTracks().size() == 0) || (B.getHsTracks().size() == 0))
			return 0.0;

		HashSet<String> ATags = new HashSet<String>();
		HashSet<String> BTags = new HashSet<String>();

		// Add all unique tags related to tracks listened to by User A to ATags
		for (Object t : A.getHsTracks().keySet()) {
			if (((Track) t).getTagName() != null) {
				StringTokenizer str = new StringTokenizer(((Track) t).getTagName(), ";");
				while(str.hasMoreElements())
					ATags.add(str.nextToken());
			} 		
		}

		// Add all unique tags related to tracks listened to by User B to BTags
		for (Object t : B.getHsTracks().keySet()) {
			if (((Track) t).getTagName() != null) {
				StringTokenizer str = new StringTokenizer(((Track) t).getTagName(), ";");
				while(str.hasMoreElements())
					BTags.add(str.nextToken());
			} 		
		}

		if ((ATags.size() == 0) || (BTags.size() == 0))
			return 0.0;

		// Intersection set for numerator for Jaccard sim calculation		
		HashSet<String> intersect = new HashSet<String>();
		intersect.addAll(ATags);
		intersect.retainAll(BTags);

		if (wrto.toUpperCase().equals("ALL")){
			HashSet<String> union = new HashSet<String>();
			union.addAll(ATags);
			union.addAll(BTags);

			sim = (double)intersect.size()/union.size();
		}
		else if (wrto.toUpperCase().equals("A")){
			sim = (double)intersect.size()/ATags.size();
		}
		else if (wrto.toUpperCase().equals("B")){
			sim = (double)intersect.size()/BTags.size();
		}

		sim = Math.rint(sim * 1000.0d) / 1000.0d;

		return sim;
	}


	// Calculate similarity between users based on Artist similarity
	public static double calculateArtistSimilarity(User A, User B, String wrto){
		double sim = 0.0;

		if ((A==null) || (B==null))
			return 0.0;

		if ((A.getHsTracks().size() == 0) || (B.getHsTracks().size() == 0))
			return 0.0;

		HashSet<String> Aartists = new HashSet<String>();
		HashSet<String> Bartists = new HashSet<String>();

		// Add all unique artists related to tracks listened to by User A to Aartists
		for (Object t : A.getHsTracks().keySet()) {
			if (((Track) t).getArtist() != null)
				Aartists.add(((Track) t).getArtist().toLowerCase());
		}

		// Add all unique artists related to tracks listened to by User B to Aartists
		for (Object t : B.getHsTracks().keySet()) {
			if (((Track) t).getArtist() != null)
				Bartists.add(((Track) t).getArtist().toLowerCase());
		}

		if ((Aartists.size() == 0) || (Bartists.size() == 0))
			return 0.0;


		// Intersection set for numerator for Jaccard sim calculation
		HashSet<String> intersect = new HashSet<String>();
		intersect.addAll(Aartists);
		intersect.retainAll(Bartists);

		if (wrto.toUpperCase().equals("ALL")){
			HashSet<String> union = new HashSet<String>();
			union.addAll(Aartists);
			union.addAll(Bartists);

			sim = (double)intersect.size()/union.size();
		}
		else if (wrto.toUpperCase().equals("A")){
			sim = (double)intersect.size()/Aartists.size();
		}
		else if (wrto.toUpperCase().equals("B")){
			sim = (double)intersect.size()/Bartists.size();
		}

		sim = Math.rint(sim * 1000.0d) / 1000.0d;

		return sim;
	}	


	// Calculate similarity between users based on Album similarity
	public static double calculateAlbumSimilarity(User A, User B, String wrto){
		double sim = 0.0;

		if ((A==null) || (B==null))
			return 0.0;

		if ((A.getHsTracks().size() == 0) || (B.getHsTracks().size() == 0))
			return 0.0;

		HashSet<String> Aalbums = new HashSet<String>();
		HashSet<String> Balbums = new HashSet<String>();

		// Add all unique albums related to tracks listened to by User A to Aalbums
		for (Object t : A.getHsTracks().keySet()) {
			if (((Track) t).getAlbum() != null) {
				if (((Track) t).getAlbum() != null)
					Aalbums.add(((Track) t).getAlbum().toLowerCase());
			}
		}

		// Add all unique albums related to tracks listened to by User B to Aartists
		for (Object t : B.getHsTracks().keySet()) {
			if (((Track) t).getAlbum() != null) {
				if (((Track) t).getAlbum() != null)
					Balbums.add(((Track) t).getAlbum().toLowerCase());
			}
		}

		if ((Aalbums.size() == 0) || (Balbums.size() == 0))
			return 0.0;


		// Intersection set for numerator for Jaccard sim calculation
		HashSet<String> intersect = new HashSet<String>();
		intersect.addAll(Aalbums);
		intersect.retainAll(Balbums);

		if (wrto.toUpperCase().equals("ALL")){
			HashSet<String> union = new HashSet<String>();
			union.addAll(Aalbums);
			union.addAll(Balbums);

			sim = (double)intersect.size()/union.size();
		}
		else if (wrto.toUpperCase().equals("A")){
			sim = (double)intersect.size()/Aalbums.size();
		}
		else if (wrto.toUpperCase().equals("B")){
			sim = (double)intersect.size()/Balbums.size();
		}

		sim = Math.rint(sim * 1000.0d) / 1000.0d;

		return sim;
	}	



	// Calculate similarity between users based on Friend similarity
	public static double calculateFriendSimilarity(User A, User B, String wrto){
		double sim = 0.0;

		if ((A==null) || (B==null))
			return 0.0;

		if ((hmFriends.get(A.getUserID()) == null) || (hmFriends.get(B.getUserID()) == null))
			return 0.0;

		if ((hmFriends.get(A.getUserID()).size() == 0) || (hmFriends.get(B.getUserID()).size() == 0))
			return 0.0;


		// Intersection set for numerator for Jaccard sim calculation
		HashSet<String> intersect = new HashSet<String>();
		intersect.addAll(hmFriends.get(A.getUserID()));
		intersect.retainAll(hmFriends.get(B.getUserID()));

		if (wrto.toUpperCase().equals("ALL")){
			HashSet<String> union = new HashSet<String>();
			union.addAll(hmFriends.get(A.getUserID()));
			union.addAll(hmFriends.get(B.getUserID()));

			sim = (double)intersect.size()/union.size();
		}
		else if (wrto.toUpperCase().equals("A")){
			sim = (double)intersect.size()/hmFriends.get(A.getUserID()).size();
		}
		else if (wrto.toUpperCase().equals("B")){
			sim = (double)intersect.size()/hmFriends.get(B.getUserID()).size();
		}

		sim = Math.rint(sim * 1000.0d) / 1000.0d;

		return sim;
	}		

	public static String readFileAsString(String filePath) throws java.io.IOException{
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


	@SuppressWarnings("unchecked")
	public static HashMap<String, HashSet<String>> readUserFriends(String filePath) throws IOException{
		System.out.println("Processing : "+filePath);
		HashMap<String, ArrayList<String>> tmp;
		HashMap<String, HashSet<String>> hmUF = new HashMap<String, HashSet<String>>();  

		String jsonString = readFileAsString(filePath);
		tmp = (HashMap<String, ArrayList<String>>)new Gson().fromJson(jsonString, HashMap.class);

		for (String key : tmp.keySet()){
			System.out.println("key = "+key);
			if (tmp.get(key) == null)
				hmUF.put(key, new HashSet<String>());
			else hmUF.put(key, new HashSet<String>(tmp.get(key)));
		}

		return hmUF;
	}

	public static HashMap<String, User> readUser(String filePath) throws IOException{
		Type hmUType = new TypeToken<HashMap<String, User>>() {}.getType();
		HashMap<String, User> hmU = new HashMap<String, User>();

		String jsonString = readFileAsString(filePath);
		hmU = new Gson().fromJson(jsonString, hmUType);

		for (String key : hmU.keySet()){
			hmU.get(key).setUserID(key);
			// System.out.printf("User: %s, Tracks played=%d \n", key, hmU.get(key).getHsTracks().size());
		}

		return hmU;
	}	


	public static int getUserID(String userid) {
		if (hmUserID.size() > 0) {
			if (hmUserID.get(userid) != null)
				return hmUserID.get(userid);
			else {
				hmUserID.put(userid, Collections.max(hmUserID.values()) + 1);
				return Collections.max(hmUserID.values()) + 1;
			}
		}
		else {
			hmUserID.put(userid, 0);
			return 0;
		}
	}

	public static void generateEdgeListFile(String filePath) throws IOException {
		FileWriter fstream = null;
		BufferedWriter out = null;

		try{
			fstream = new FileWriter(filePath);
			out = new BufferedWriter(fstream);
			for (String user : hmFriends.keySet()) {
				for (String friend : hmFriends.get(user))
					out.write(getUserID(user) + "\t" + getUserID(friend) + "\n");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			out.close();
		}
	}

	public static void main(String[] args) throws IOException{				
		String prefixPath = "C:\\Users\\beladia\\workspace\\lastfm\\src\\lastfm\\data\\spain-2200-users\\";
		String filePath = prefixPath + "hmUser_spain_sample_20users_newformat";
		hmUser = readUser(filePath);

		/**		
		hmUser.putAll(readUser(prefixPath + "hmUser_spain_3"));
		hmUser.putAll(readUser(prefixPath + "hmUser_spain_4"));
		hmUser.putAll(readUser(prefixPath + "hmUser_spain_6"));

		filePath = prefixPath + "hmfriends_spain_2";
		hmFriends = readUserFriends(filePath);		
		hmFriends.putAll(readUserFriends(prefixPath + "hmfriends_spain_3"));
		hmFriends.putAll(readUserFriends(prefixPath + "hmfriends_spain_4"));
		hmFriends.putAll(readUserFriends(prefixPath + "hmfriends_spain_6"));

		//String filePath = "/home/neera/lastfm-data/dumps/UK-data/hmUser_uk_1300users";
		//hmUser = readUser(filePath);

		//filePath = "/home/neera/lastfm-data/dumps/UK-data/hmfriends_uk_1300users";
		//hmFriends = readUserFriends(filePath);		

		//System.out.println("Average No. of Friends in the Network = " + LastfmStats.getAverageNumFriends());
		//LastfmStats.getDegreeDistribution(prefixPath + "FriendDistribution_uk1300.dat");
		//LastfmStats.getActivityDistribution(prefixPath + "TrackDistribution_uk1300.dat", prefixPath + "ActivityDistribution_uk1300.dat");

		filePath = prefixPath + "EdgeList_US.dat";
		generateEdgeListFile(filePath);

		filePath = prefixPath + "traindata_tags_spain234.dat";
		generateTrainData(filePath, 10000);

		 **/		
	}
	

	static HashMap<String, Boolean> topGenreList = new HashMap<String, Boolean>();
	public static void getTopTrackGenre(int count){
		HashMap<String, Integer> topGenre = new HashMap<String, Integer>();
		lastfm.LastFm.ValueComparator bvc =  new LastFm().new ValueComparator(topGenre);
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
		int distance = 0;
		while(it.hasNext()){
			String key = it.next();
			if(BScore.containsKey(key)){
				Integer aVal = AScore.get(key);
				Integer bVal = BScore.get(key);
				int diff = aVal - bVal;
				distance = distance + diff*diff;
			}
		}
		
		return (Math.sqrt(distance));
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
	
	public static void generateTrainData(String outpath, int trainSz) throws IOException{
		// Get List of all Users
		List<String> users = new ArrayList<String>(hmUser.keySet());
		HashSet<String> edgeSet = new HashSet<String>();
		int u1, u2, conn0, conn1, conn0Cnt, conn1Cnt, countLoop;
		String edgePair = null;
		String user1, user2, connected;

		FileWriter fstream = null;
		BufferedWriter out = null;
		Random rndGenerator = new Random(19580427);

		try{
			fstream = new FileWriter(outpath);
			out = new BufferedWriter(fstream);
			int count = 0;

			conn1 = 0;
			conn0 = 0;
			conn0Cnt = (int)trainSz/2;
			conn1Cnt = (int)trainSz/2;

			while((conn0 < conn0Cnt) || (conn1 < conn1Cnt)){
				countLoop = 0;
				while(true){
					countLoop++;
					// Randomly pick 2 Users
					u1 = rndGenerator.nextInt(users.size());
					u2 = rndGenerator.nextInt(users.size());


					if (u1 < u2){
						user1 = users.get(u1);
						user2 = users.get(u2);
						edgePair = hmUserID.get(user1) + "#" + hmUserID.get(user2);
					}
					else {
						user1 = users.get(u2);
						user2 = users.get(u1);
						edgePair = hmUserID.get(user2) + "#" + hmUserID.get(user1);
					}

					try{
						if ((hmFriends.get(user1) != null) && (hmFriends.get(user2) != null)) {
							if ((hmFriends.get(user1).contains(user2) || hmFriends.get(user2).contains(user1)) && (conn1 >= conn1Cnt))
								continue;
							if (!(hmFriends.get(user1).contains(user2) || hmFriends.get(user2).contains(user1)) && (conn0 >= conn0Cnt))
								continue;						
						}

					}
					catch (Exception e){
						e.printStackTrace();
					}

					/*if (conn0 >= conn0Cnt){
						if (hmFriends.get(user1) != null) {
							user2 = (String) hmFriends.get(user1).toArray()[rndGenerator.nextInt(hmFriends.get(user1).size())];
							break;
						}
					}*/

					//System.out.printf("Count : %d, U1=%d, U2=%d \n", countLoop, u1, u2);
					if (((u1 != u2) && !edgeSet.contains(edgePair)) || (countLoop > 1000)) 
						break;

				}

				edgeSet.add(edgePair);
				//System.out.printf("Count0 = %d, Count1 = %d \n", conn0, conn1);

				count++;
				if(count % 100 == 0){
					System.out.println("iteration number "+count);
					System.out.printf("Count0 = %d, Count1 = %d \n", conn0, conn1);
				}

				if (countLoop > 1000) {
					conn0 = conn0Cnt + 1;
					conn1 = conn1Cnt + 1;
				}

				// Generate features for training data
				if (count == 1)
					out.write("edgePr \t infA \t infB \t " +
							"infCmnNbhAB \t infCmnNbhOnA \t infCmnNbhOnB \t " +
							"avg2hopAB \t avg2hopBA \t avg3hopAB \t avg3hopBA \t " +
							"infConcAonCmnNbh \t infConcBonCmnNbh \t " +
							"infAonB \t infBonA \t " +
							"frndSimALL \t frndSimA \t frndSimB \t " +
							"trackSimALL \t trackSimA \t trackSimB \t " +
							"tagSimALL \t tagSimA \t tagSimB \t " +
							"artistSimALL \t artistSimA \t artistSimB \t " +
							"albumSimALL \t albumSimA \t albumSimB \t " +
							"connected \n");

				if ((hmFriends.get(user1) != null) || (hmFriends.get(user2) != null)) {
					if (hmFriends.get(user1).contains(user2) || hmFriends.get(user2).contains(user1)){
						connected = "1";
						conn1++;
					}
					else {
						connected = "0";
						conn0++;
					}					
				}
				else {
					connected = "0";
					conn0++;
				}

				out.write(user1 + "#" + user2 + " \t " +
						calculateOverallInfluence(hmUser.get(user1)) + " \t " +
						calculateOverallInfluence(hmUser.get(user2)) + " \t " +
						calculateCommonNeighborAvgInfluence(hmUser.get(user1), hmUser.get(user2)) + " \t " +
						calculateInfluenceOfCommonNeighborAvgOn(hmUser.get(user1), hmUser.get(user2), "A") + " \t " +
						calculateInfluenceOfCommonNeighborAvgOn(hmUser.get(user1), hmUser.get(user2), "B") + " \t " +
						calculateAvg2HopInfluencePathLength(hmUser.get(user1), hmUser.get(user2)) + " \t " +
						calculateAvg2HopInfluencePathLength(hmUser.get(user2), hmUser.get(user1)) + " \t " +
						calculateAvg3HopInfluencePathLength(hmUser.get(user1), hmUser.get(user2)) + " \t " +
						calculateAvg3HopInfluencePathLength(hmUser.get(user2), hmUser.get(user1)) + " \t " +
						calculateInfluenceOnCommonNeighbors(hmUser.get(user1), hmUser.get(user2)) + " \t " +
						calculateInfluenceOnCommonNeighbors(hmUser.get(user2), hmUser.get(user1)) + " \t " +
						calculateInfluence(hmUser.get(user1), hmUser.get(user2)) + " \t " +
						calculateInfluence(hmUser.get(user2), hmUser.get(user1)) + " \t " +
						// Friend Similarity features						
						calculateFriendSimilarity(hmUser.get(user2), hmUser.get(user1), "ALL") + " \t " +
						calculateFriendSimilarity(hmUser.get(user2), hmUser.get(user1), "A") + " \t " +
						calculateFriendSimilarity(hmUser.get(user2), hmUser.get(user1), "B") + " \t " +
						// Track Similarity features						
						calculateTrackSimilarity(hmUser.get(user2), hmUser.get(user1), "ALL") + " \t " +
						calculateTrackSimilarity(hmUser.get(user2), hmUser.get(user1), "A") + " \t " +
						calculateTrackSimilarity(hmUser.get(user2), hmUser.get(user1), "B") + " \t " +
						// Tag Similarity features
						calculateTagSimilarity(hmUser.get(user2), hmUser.get(user1), "ALL") + " \t " +
						calculateTagSimilarity(hmUser.get(user2), hmUser.get(user1), "A") + " \t " +
						calculateTagSimilarity(hmUser.get(user2), hmUser.get(user1), "B") + " \t " +
						// Artist Similarity features
						calculateArtistSimilarity(hmUser.get(user2), hmUser.get(user1), "ALL") + " \t " +
						calculateArtistSimilarity(hmUser.get(user2), hmUser.get(user1), "A") + " \t " +
						calculateArtistSimilarity(hmUser.get(user2), hmUser.get(user1), "B") + " \t " +
						// Album Similarity features
						calculateAlbumSimilarity(hmUser.get(user2), hmUser.get(user1), "ALL") + " \t " +
						calculateAlbumSimilarity(hmUser.get(user2), hmUser.get(user1), "A") + " \t " +
						calculateAlbumSimilarity(hmUser.get(user2), hmUser.get(user1), "B") + " \t " +						
						// Response : connected(1) or not-connected(0) for a pair of users
						connected +
						" \n"
						);
			}

		}
		catch(IOException e){
			e.printStackTrace();
		}
		finally{
			out.close();
		}
	} 
}
