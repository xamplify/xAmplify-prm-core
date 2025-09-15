package com.xtremand.domain.bom;

public enum DomainModuleNameType {

	TEAM_MEMBER("TEAM_MEMBER"), PARTNER("PARTNER");

	protected String domainModule;

	private DomainModuleNameType(String domainModule) {
		this.domainModule = domainModule;
	}

	public String getDomainModule() {
		return domainModule;
	}

}
