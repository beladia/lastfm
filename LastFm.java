package lastfm;

import java.util.*;

public class LastFm {
	//HashMap<String, HashSet<String>> hmFriends;
	//HashMap<String, User> hmUser;	
	
	// Calculate influence score of User A on User B
	public double calculateInfluence(User A, User B){
		double influence = 0.0;
		
		// Get Common Tracks played by User A and User B
		for (Track at : A.getHsTracks()){
			for (Track bt : B.getHsTracks()){
				if (at.getTimeofPlay().before(bt.getTimeofPlay())){
					influence += Math.exp(-1 * (at.getTimeofPlay().getTime() - bt.getTimeofPlay().getTime())/(1000*60*60*24));
				}
			}
		}
		
		influence = influence / B.getHsTracks().size();		
		return influence;		
	}
	
	
	// Calculate overall influence score of User A
	public double calculateOverallInfluence(User A){
		double influence = 0.0;
		double weightSum = 0.0;
		User B;
		
		// Get User A's friends
		HashSet<String> friends = LastfmMain.hmFriends.get(A.getUserID());
		
		// For each of User A's friend B, calculate influence(A, B)
		for (String friend : friends){
			B = LastfmMain.hmUser.get(friend);
			influence += (B.getHsTracks().size() * calculateInfluence(A, B));
			weightSum += B.getHsTracks().size();
		}
		
		influence = influence / weightSum;
			
		return influence;		
	}
	
	
	// Calculate average influence of common neighbors of User A and User B
	public double calculateCommonNeighborAvgInfluence(User A, User B){
		double influence = 0.0;
		
		HashSet<String> common = new HashSet<String>();
		common.addAll(LastfmMain.hmFriends.get(A.getUserID()));
		common.retainAll(LastfmMain.hmFriends.get(B.getUserID()));
		
		for (String c : common){
			influence += calculateOverallInfluence(LastfmMain.hmUser.get(c));
		}
		
		influence = influence / common.size();
		
		return influence;
	}
	
	// Calculate User A's concentration of influence on common neighbors with User B vs. all neighbors
	public double calculateInfluenceOnCommonNeighbors(User A, User B){
		double influence = 0.0;
		double totalInfluence = 0.0;
		
		HashSet<String> common = new HashSet<String>();
		common.addAll(LastfmMain.hmFriends.get(A.getUserID()));
		common.retainAll(LastfmMain.hmFriends.get(B.getUserID()));
		
		for (String c : common){
			influence += calculateInfluence(A, LastfmMain.hmUser.get(c));
		}
		
		for (String nbhA : LastfmMain.hmFriends.get(A.getUserID())){
			totalInfluence += calculateInfluence(A, LastfmMain.hmUser.get(nbhA));
		}
		
		influence = influence / totalInfluence;
		
		return influence;
	}	
	
	// Calculate Average 2-hop Influence path length between User A and User B
	public double calculateAvg2HopInfluencePathLength(User A, User B){
		double influence = 0.0;
		
		HashSet<String> common = new HashSet<String>();
		common.addAll(LastfmMain.hmFriends.get(A.getUserID()));
		common.retainAll(LastfmMain.hmFriends.get(B.getUserID()));
		
		for (String c : common){
			influence += (calculateInfluence(LastfmMain.hmUser.get(c), A) + calculateInfluence(B, LastfmMain.hmUser.get(c)));
		}
		
		influence = influence / common.size();
		
		return influence;
	}

	
	// Calculate Average 3-hop Influence path length between User A and User B
	public double calculateAvg3HopInfluencePathLength(User A, User B){
		double influence = 0.0;
		
		// Get User A's neighbors that are not neighbors of User B
		HashSet<String> noncommon = new HashSet<String>();
		noncommon.addAll(LastfmMain.hmFriends.get(A.getUserID()));
		noncommon.removeAll(LastfmMain.hmFriends.get(B.getUserID()));
		
		int count = 0;
		for (String ncA : noncommon){
			for (String nbhB : LastfmMain.hmFriends.get(A.getUserID())){
				if (LastfmMain.hmFriends.get(ncA).contains(nbhB)){
					count++;
					influence += (calculateInfluence(LastfmMain.hmUser.get(ncA), A) + calculateInfluence(LastfmMain.hmUser.get(nbhB), LastfmMain.hmUser.get(ncA)) + calculateInfluence(B, LastfmMain.hmUser.get(nbhB)));
				}
			}
			
		}
		
		influence = influence / count;
		
		return influence;
	}
	
	
	public static void main(String[] args){
	} 
}
