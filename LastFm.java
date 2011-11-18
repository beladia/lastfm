package lastfm;

import java.io.*;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;;

public class LastFm {
	//HashMap<String, HashSet<String>> hmFriends;
	//HashMap<String, User> hmUser;	
	
	static HashMap<String, HashSet<String>>  hmFriends;
	static HashMap<String, User> hmUser;	
	
	public static Date parseDate(String tp){
		//2008-03-10 04:32
		String dateFormat = "EEE MMM dd HH:mm:ss zzz yyyy";
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		try {
			return sdf.parse(tp);
		} catch (ParseException e) {
			String dateFormat2 = "yyyy-mm-dd HH:mm";
			SimpleDateFormat sdf2 = new SimpleDateFormat(dateFormat2);
			try {
				return sdf2.parse(tp);
			} catch (ParseException e1) {
				String dateFormat3 = "dd MMM yyyy, HH:mm";
				SimpleDateFormat sdf3 = new SimpleDateFormat(dateFormat3);
				try {
					return sdf3.parse(tp);
				} catch (ParseException e2) {				
					e1.printStackTrace();
				}
			}
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}	

	// Calculate influence score of User A on User B
	public static double calculateInfluence(User A, User B){
		double influence = 0.0;
		double days;
		
		if ((A == null) || (B == null))
			return 0.0;

		// Get Common Tracks played by User A and User B
		//System.out.println("A's tracks "+ A.getHsTracks().size());
		//System.out.println("B's tracks "+ B.getHsTracks().size());
		for (Track at : A.getHsTracks()){
			for (Track bt : B.getHsTracks()){
				if(at.getTimeofPlay() != null && bt.getTimeofPlay() != null ){
					if (at.equals(bt) && parseDate(at.getTimeofPlay()).before(parseDate(bt.getTimeofPlay()))){
						//System.out.println("track matched");
						//System.out.println(bt.getTimeofPlay().getTime() - at.getTimeofPlay().getTime());
						days = (parseDate(bt.getTimeofPlay()).getTime() - parseDate(at.getTimeofPlay()).getTime())/(1000*60*60*24);
						if (days == 0)
							influence += 1;
						else influence += Math.exp(-1 * days);
					}
				}
			}
		}

		if (influence > 0)
			influence = influence / B.getHsTracks().size();	
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
					//System.out.printf("UserA = %s, User B = %s \n", A.getUserID(), B.getUserID());
					//System.out.printf("UserA = %s, Tracks Played = %d.  User B = %s, Tracks Played = %d \n", A.getUserID(), A.getHsTracks().size(), B.getUserID(), B.getHsTracks().size());
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
	
	
	@SuppressWarnings("unchecked")
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
	
	@SuppressWarnings("unchecked")
	public static HashMap<String, User> readUser(String filePath) throws IOException{
		Type hmUType = new TypeToken<HashMap<String, User>>() {}.getType();
		HashMap<String, User> hmU = new HashMap<String, User>();
		
		String jsonString = readFileAsString(filePath);
		hmU = new Gson().fromJson(jsonString, hmUType);
		
		for (String key : hmU.keySet()){
			hmU.get(key).setUserID(key);
			System.out.printf("User: %s, Tracks played=%d \n", key, hmU.get(key).getHsTracks().size());
		}
		
		return hmU;
	}	
	
	public static void main(String[] args) throws IOException{				
		String filePath = "C:\\Users\\beladia\\workspace\\lastfm\\hmUser_spain_21";
		hmUser = readUser(filePath);
		
		filePath = "C:\\Users\\beladia\\workspace\\lastfm\\hmfriends_spain_21";
		hmFriends = readUserFriends(filePath);		
		
		filePath = "C:\\Users\\beladia\\workspace\\lastfm\\traindata.dat";
		generateTrainData(filePath, 50000);
	}


	public static void generateTrainData(String outpath, int trainSz) throws IOException{
		// Get List of all Users
		List<String> users = new ArrayList<String>(hmUser.keySet());
		HashSet<String> edgeSet = new HashSet<String>();
		int u1, u2, conn0, conn1, conn0Cnt, conn1Cnt;
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
			
			int countLoop;
			while((conn0 < conn0Cnt) || (conn1 < conn1Cnt)){
				countLoop = 0;
				while(true){
					// Randomly pick 2 Users
					u1 = rndGenerator.nextInt(users.size());
					u2 = rndGenerator.nextInt(users.size());

					
					if (u1 < u2){
						user1 = users.get(u1);
						user2 = users.get(u2);
						edgePair = u1 + "#" + u2;
					}
					else {
						user1 = users.get(u2);
						user2 = users.get(u1);
						edgePair = u2 + "#" + u1;
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
					
					if (conn0 >= conn0Cnt){
						if (hmFriends.get(user1) != null) {
							user2 = (String) hmFriends.get(user1).toArray()[rndGenerator.nextInt(hmFriends.get(user1).size())];
							//System.out.println("HERE :" + user2 +" isfriend="+hmFriends.get(user1).contains(user2));
							break;
						}
					}

					//System.out.printf("Count : %d, U1=%d, U2=%d \n", countLoop, u1, u2);
					countLoop++;
					if ((u1 != u2) && !edgeSet.contains(edgePair)) 
						break;
					
				}

				//edgeSet.add(edgePair);
				System.out.printf("Count0 = %d, Count1 = %d \n", conn0, conn1);
				
				count++;

				// Generate features for training data
				if (count == 1)
					out.write("edgePr \t infA \t infB \t infCmnNbhAB \t avg2hopAB \t avg2hopBA \t avg3hopAB \t avg3hopBA \t infConcAonCmnNbh \t infConcBonCmnNbh \t infAonB \t infBonA \t connected \n");

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
						calculateAvg2HopInfluencePathLength(hmUser.get(user1), hmUser.get(user2)) + " \t " +
						calculateAvg2HopInfluencePathLength(hmUser.get(user2), hmUser.get(user1)) + " \t " +
						calculateAvg3HopInfluencePathLength(hmUser.get(user1), hmUser.get(user2)) + " \t " +
						calculateAvg3HopInfluencePathLength(hmUser.get(user2), hmUser.get(user1)) + " \t " +
						calculateInfluenceOnCommonNeighbors(hmUser.get(user1), hmUser.get(user2)) + " \t " +
						calculateInfluenceOnCommonNeighbors(hmUser.get(user2), hmUser.get(user1)) + " \t " +
						calculateInfluence(hmUser.get(user1), hmUser.get(user2)) + " \t " +
						calculateInfluence(hmUser.get(user2), hmUser.get(user1)) + " \t " +
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
