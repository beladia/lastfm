package lastfm;
import java.util.*;

public class User {
	private String ID, name, realName, country, gender;
	private int age, playCount, playLists;
	private Date registrationDate;
	private HashSet<Track> hsTracks;
	
	
	public String getUserID() {
		return ID;
	}
	public void setUserID(String ID) {
		this.ID = ID;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getRealName() {
		return realName;
	}
	public void setRealname(String realName) {
		this.realName = realName;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public int getPlayCount() {
		return playCount;
	}
	public void setPlayCount(int playCount) {
		this.playCount = playCount;
	}
	public int getPlayLists() {
		return playLists;
	}
	public void setPlayLists(int playLists) {
		this.playLists = playLists;
	}
	public Date getRegistrationDate() {
		return registrationDate;
	}
	public void setRegistrationDate(Date registrationDate) {
		this.registrationDate = registrationDate;
	}
	public HashSet<Track> getHsTracks() {
		return hsTracks;
	}
	public void setHsTracks(HashSet<Track> hsTracks) {
		this.hsTracks = hsTracks;
	}
	
	public boolean addTrack(Track track){
		return this.hsTracks.add(track);
	}
	public boolean removeTrack(Track track){
		return this.hsTracks.remove(track);
	}	
}
