package com.xtremand.category.bom;

public enum CategoryModuleEnum {

	EMAIL_TEMPLATE("EMAIL_TEMPLATE"), LANDING_PAGE("LANDING_PAGE"), FORM("FORM"), CAMPAIGN("CAMPAIGN"),
	LEARNING_TRACK("LEARNING_TRACK"), DAM("DAM"), PLAY_BOOK("PLAY_BOOK");

	protected String type;

	private CategoryModuleEnum(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

}
