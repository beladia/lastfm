package lastfm;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class LastfmStats {

	// Method to calculate average # of friends a user has in the network 
	public static double getAverageNumFriends(){
		double avgFriends = 0.0;

		for (String user : LastFm.hmUser.keySet()){
			if (LastFm.hmFriends.get(user) != null)
				avgFriends += LastFm.hmFriends.get(user).size();
		}

		avgFriends = (double)avgFriends / LastFm.hmUser.keySet().size();  

		return avgFriends;
	}


	public static void printDistribution(HashMap<Integer, Integer> hmDist, String xTitle, String yTitle, String outFile) throws IOException {
		// write out the degree distribution to outFile
		FileWriter fstream = null;
		BufferedWriter out = null;
		try {
			fstream = new FileWriter(outFile);
			out = new BufferedWriter(fstream);

			out.write(xTitle + " \t " + yTitle + " \n");
			for (int key : hmDist.keySet())
				out.write(key + " \t " + hmDist.get(key) + " \n ");			
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			out.close();
		}			
	}


	// Save the degree distribution i.e. <degree, # of users> in outFile
	// Degree here represents # of friends a user has
	public static void getDegreeDistribution(String outFile) throws IOException{
		HashMap<Integer, Integer> hmDegreeDistn = new HashMap<Integer, Integer>();
		int friends = 0;

		for (String user : LastFm.hmUser.keySet()){
			if (LastFm.hmFriends.get(user) != null)
				friends =  LastFm.hmFriends.get(user).size();

			if (hmDegreeDistn.get(friends) != null){
				hmDegreeDistn.put(friends, hmDegreeDistn.get(friends)+1);
			}
			else hmDegreeDistn.put(friends, 1);			
		}


		printDistribution(hmDegreeDistn, "Degree", "Count", outFile);		
	}


	// Save the distribution of user Tracks vs. count i.e. <# of tracks, # of users> in outFile1
	// Save the distribution of how active a user is vs. count i.e. <range in days of Track timeOfPlay, # of users> in outFile2 	
	@SuppressWarnings("deprecation")
	public static void getActivityDistribution(String outFile1, String outFile2) throws IOException{
		HashMap<Integer, Integer> hmTrackDistn = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> hmActiveDistn = new HashMap<Integer, Integer>();
		ArrayList<String> arrTimeOfPlay;
		int tracks = 0;
		int rangeDays = 0;
		Date tmpDate, minDate, maxDate, referenceDate;
		int days;

		referenceDate = new Date(1900, 1, 1);

		for (String user : LastFm.hmUser.keySet()){
			minDate = referenceDate;
			maxDate = referenceDate;
			if (LastFm.hmUser.get(user) != null) {
				tracks =  LastFm.hmUser.get(user).getHsTracks().size();

				// Update track count in Track Distribution
				if (hmTrackDistn.get(tracks) != null){
					hmTrackDistn.put(tracks, hmTrackDistn.get(tracks)+1);
				}
				else hmTrackDistn.put(tracks, 1);	

				// Calculate Min and Max Dates of User played Tracks
				for (Object t : LastFm.hmUser.get(user).getHsTracks().keySet()) {
					arrTimeOfPlay = LastFm.hmUser.get(user).getHsTracks().get(t);
					for (int i=0; i<arrTimeOfPlay.size();i++) {
						tmpDate = LastFm.parseDate(arrTimeOfPlay.get(i));

						if (minDate.equals(referenceDate) && (tmpDate != null)) {
							minDate = tmpDate;
							maxDate = tmpDate;
						}
						else {
							if (tmpDate != null) {
								if (tmpDate.before(minDate)) 
									minDate = tmpDate;
								if (tmpDate.after(maxDate))
									maxDate = tmpDate;
							}
						}					
					}
				}

				// Days of activity
				days = (int)((maxDate.getTime() - minDate.getTime())/(1000*60*60*24));
				days = days + 1;
				if (days == 1114)
					System.out.println("Min Date = " + minDate + ", Max Date = " + maxDate + ", Days = " + days);

				// Update Activity(days) count in Active Distribution
				if (hmActiveDistn.get(days) != null){
					hmActiveDistn.put(days, hmActiveDistn.get(days)+1);
				}
				else hmActiveDistn.put(days, 1);					
			}					
		}


		printDistribution(hmTrackDistn, "numTracks", "Count", outFile1);
		printDistribution(hmActiveDistn, "numDays", "Count", outFile2);
	}	
}
