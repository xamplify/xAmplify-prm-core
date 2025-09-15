package com.xtremand.util.bom;

public enum ModuleType {

	DAM("DAM"), TRACK("TRACK"), PLAYBOOK("PLAYBOOK");

	protected String type;

	private ModuleType(String type) {
		this.type = type;
	}

	public String getModuleType() {
		return type;
	}
}
