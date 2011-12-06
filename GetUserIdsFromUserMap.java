package lastfm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class GetUserIdsFromUserMap {
	
	public static HashMap<String, UserOld> readUser(String filePath) throws IOException{
		Type hmUType = new TypeToken<HashMap<String, UserOld>>() {}.getType();
		HashMap<String, UserOld> hmU = new HashMap<String, UserOld>();

		String jsonString = LastFm.readFileAsString(filePath);
		hmU = new Gson().fromJson(jsonString, hmUType);

		for (String key : hmU.keySet()){
			hmU.get(key).setUserID(key);
			// System.out.printf("User: %s, Tracks played=%d \n", key, hmU.get(key).getHsTracks().size());
		}

		return hmU;
	}	

	public static void main(String[] args){
		try{
			String userMapPath = args[0];//"/home/neera/lastfm-data/dumps/UK-data/hmUser_uk_1300users";
			String userIdFilePath = args[1]; //"/home/neera/lastfm-data/dumps/UK-data/userIds_uk_1300users";
			System.out.println("user map location : "+userMapPath);
			System.out.println("user id file output location : "+userIdFilePath);
			HashMap<String, UserOld> hmUser = readUser(userMapPath);
			System.out.println("user map size "+hmUser.size());
			Iterator<String> it = hmUser.keySet().iterator();
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(userIdFilePath)));
			while(it.hasNext()){
				writer.write(it.next()+"\n");
			}
			writer.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}


