package com.xtremand.highlevel.analytics.bom;

public enum DownloadModule {

	HIGH_LEVEL_DASHBOARD_ANALYTICS("HIGH_LEVEL_DASHBOARD_ANALYTICS");
	protected String moduleName;
	
	public String getModuleName() {
		return moduleName;
	}
	
	private DownloadModule(String moduleName) {
		this.moduleName = moduleName;
	}
}

