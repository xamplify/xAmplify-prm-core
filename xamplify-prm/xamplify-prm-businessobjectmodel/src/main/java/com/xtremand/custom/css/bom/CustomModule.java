package com.xtremand.custom.css.bom;

public enum CustomModule {

	LEFT_SIDE_MENU("LEFT_SIDE_MENU"), TOP_NAVIGATION_BAR("TOP_NAVIGATION_BAR"), FOOTER("FOOTER"),
	MAIN_CONTENT("MAIN_CONTENT"), BUTTON_CUSTOMIZE("BUTTON_CUSTOMIZE");

	protected String moduleName;

	public String getModuleName() {
		return moduleName;
	}

	private CustomModule(String moduleName) {
		this.moduleName = moduleName;
	}

}
