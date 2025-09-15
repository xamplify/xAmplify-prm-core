package com.xtremand.user.bom;

public enum ModulesDisplayType {
	
	LIST ("LIST"),
	GRID ("GRID"),
	FOLDER_LIST ("FOLDER_LIST"),
	FOLDER_GRID ("FOLDER_GRID");
	
	
	protected String displayType;

	private ModulesDisplayType(String displayType) {
		this.displayType = displayType;
	}

	
	public String getDisplayType() {
		return displayType;
	}
	
	public static ModulesDisplayType findByName(String displayType) {
        for (ModulesDisplayType viewType : values()) {
            if (viewType.name().equalsIgnoreCase(displayType)) {
                return viewType;
            }
        }
        return null;
    }

}
