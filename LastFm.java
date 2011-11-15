package lastfm;

import java.util.*;

public class LastFm {
	HashMap<String, HashSet<String>> hmFriends;
	HashMap<String, User> hmUser;	
	
	// Calculate influence score of User A on User B
	public double calculateInfluence(User A, User B){
		double influence = 0.0;
		
		// Get Common Tracks played by User A and User B
		for (Track at : A.getTracks()){
			for (Track bt : B.getTracks()){
				if (at.getTimeofPlay().before(bt.getTimeofPlay())){
					influence += Math.exp(-1 * (at.getTimeofPlay().getTime() - bt.getTimeofPlay().getTime())/(1000*60*60*24));
				}
			}
		}
		
		influence = influence / B.getTracks().size();		
		return influence;		
	}
	
	
	// Calculate overall influence score of User A
	public double calculateOverallInfluence(User A){
		double influence = 0.0;
		double weightSum = 0.0;
		User B;
		
		// Get User A's friends
		HashSet<String> friends = hmFriends.get(A.getID());
		
		// For each of User A's friend B, calculate influence(A, B)
		for (String friend : friends){
			B = hmUser.get(friend);
			influence += (B.getTracks().size() * calculateInfluence(A, B));
			weightSum += B.getTracks().size();
		}
		
		influence = influence / weightSum;
			
		return influence;		
	}
	
	
	// Calculate average influence of common neighbors of User A and User B
	public double calculateCommonNeighborAvgInfluence(User A, User B){
		double influence = 0.0;
		
		HashSet<String> common = new HashSet<String>();
		common.addAll(hmFriends.get(A.getID()));
		common.retainAll(hmFriends.get(B.getID()));
		
		for (String c : common){
			influence += calculateOverallInfluence(hmUser.get(c));
		}
		
		influence = influence / common.size();
		
		return influence;
	}
	
	// Calculate User A's concentration of influence on common neighbors with User B vs. all neighbors
	public double calculateInfluenceOnCommonNeighbors(User A, User B){
		double influence = 0.0;
		double totalInfluence = 0.0;
		
		HashSet<String> common = new HashSet<String>();
		common.addAll(hmFriends.get(A.getID()));
		common.retainAll(hmFriends.get(B.getID()));
		
		for (String c : common){
			influence += calculateInfluence(A, hmUser.get(c));
		}
		
		for (String nbhA : hmFriends.get(A.getID())){
			totalInfluence += calculateInfluence(A, hmUser.get(nbhA));
		}
		
		influence = influence / totalInfluence;
		
		return influence;
	}	
	
	// Calculate Average 2-hop Influence path length between User A and User B
	public double calculateAvg2HopInfluencePathLength(User A, User B){
		double influence = 0.0;
		
		HashSet<String> common = new HashSet<String>();
		common.addAll(hmFriends.get(A.getID()));
		common.retainAll(hmFriends.get(B.getID()));
		
		for (String c : common){
			influence += (calculateInfluence(hmUser.get(c), A) + calculateInfluence(B, hmUser.get(c)));
		}
		
		influence = influence / common.size();
		
		return influence;
	}
	
	
	public static void main(String[] args){
	} 
}
