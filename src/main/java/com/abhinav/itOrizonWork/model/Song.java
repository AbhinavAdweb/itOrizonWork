package com.abhinav.itOrizonWork.model;

// POJO class for the song name and id read from the uploaded file
public class Song {
	private String id;
	private String songName;
	
	public Song(String id, String songName) {
		super();
		this.id = id;
		this.songName = songName;
	}

	public String getId() {
		return id;
	}

	public String getSongName() {
		return songName;
	}

	@Override
	public String toString() {
		return "Song [id=" + id + ", songName=" + songName + "]";
	}
}
