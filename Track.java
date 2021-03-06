package lastfm;

public class Track {
	private String ID, name;
	private long duration;
	private int listeners, playCount;
	private String artist;
	private String album;
	private String timeofPlay;
	private String tagName;

	public String getTagName() {
		return tagName;
	}
	public void setTagName(String tagName2) {
		this.tagName = tagName2;
	}
	public String getTimeofPlay() {
		return timeofPlay;
	}
	public void setTimeofPlay(String timeofPlay) {
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
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public String getAlbum() {
		return album;
	}
	public void setAlbum(String album) {
		this.album = album;
	}

	public boolean equals(Object o) {
		if (o instanceof Track)
			return (this.name.toLowerCase().equals(((Track) o).getName().toLowerCase()) && this.artist.toLowerCase().equals(((Track) o).getArtist().toLowerCase()));
		else return false;
	}

	public int hashCode() {
		return this.name.hashCode();
	}
			
}
