package com.xtremand.activity.bom;

public enum NoteVisibilityType {
	
	PRIVATE("PRIVATE"),PUBLIC("PUBLIC");
	
	protected String noteType;
	
	private NoteVisibilityType(String noteVisibilityType) {
		this.noteType = noteVisibilityType;
	}
	
	public String getNoteVisibilityType() {
		return noteType;
	}
}