package com.xtremand.activity.bom;

public enum NoteAssociationType {
	
	CONTACT("CONTACT");
	
	protected String associationType;
	
	private NoteAssociationType(String associationType) {
		this.associationType = associationType;
	}
	
	public String getAssociationType() {
		return associationType;
	}
}
