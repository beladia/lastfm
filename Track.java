package lastfm;

import java.util.Date;

public class Track {
	private String ID, name;
	private long duration;
	private int listeners, playCount;
	private Artist artist;
	private Album album;
	private Date timeofPlay;
	
	public Date getTimeofPlay() {
		return timeofPlay;
	}
	public void setTimeofPlay(Date timeofPlay) {
		this.timeofPlay = timeofPlay;
	}
	public String getID() {
		return ID;
	}
	public void setID(String ID) {
		this.ID = ID;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}
	public int getListeners() {
		return listeners;
	}
	public void setListeners(int listeners) {
		this.listeners = listeners;
	}
	public int getPlayCount() {
		return playCount;
	}
	public void setPlayCount(int playCount) {
		this.playCount = playCount;
	}
	public Artist getArtist() {
		return artist;
	}
	public void setArtist(Artist artist) {
		this.artist = artist;
	}
	public Album getAlbum() {
		return album;
	}
	public void setAlbum(Album album) {
		this.album = album;
	}
	
	public boolean equals(Object o) {
		if (o instanceof Track)
			return this.ID.equals(((Track) o).getID());
		else return false;
	}
	
	public int hashCode() {
		return this.ID.hashCode();
	}	
}
