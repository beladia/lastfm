package lastfm;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

	public void setTimeofPlay(String tp){
		if (tp == null) 
			this.timeofPlay = null;
		//2008-03-10 04:32
		String dateFormat = "EEE MMM dd HH:mm:ss zzz yyyy";
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		try {
			this.timeofPlay = sdf.parse(tp);
		} catch (ParseException e) {
			String dateFormat2 = "yyyy-mm-dd HH:mm";
			SimpleDateFormat sdf2 = new SimpleDateFormat(dateFormat2);
			try {
				this.timeofPlay = sdf2.parse(tp);
			} catch (ParseException e1) {
				String dateFormat3 = "dd MMM yyyy, HH:mm";
				SimpleDateFormat sdf3 = new SimpleDateFormat(dateFormat3);
				try {
					this.timeofPlay = sdf3.parse(tp);
				} catch (ParseException e2) {				
					e1.printStackTrace();
				}
			}
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
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
			return (this.name.toLowerCase().equals(((Track) o).getName().toLowerCase()) && this.artist.getName().toLowerCase().equals(((Track) o).getArtist().getName().toLowerCase()));
		else return false;
	}

	public int hashCode() {
		return this.name.hashCode();
	}	
}
