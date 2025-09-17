package com.xtremand.lms.bom;

public enum LearningTrackType {
	TRACK("TRACK"), PLAYBOOK("PLAYBOOK");
	protected String type;

	private LearningTrackType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
