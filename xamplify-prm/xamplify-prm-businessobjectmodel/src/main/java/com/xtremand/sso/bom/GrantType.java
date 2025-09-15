package com.xtremand.sso.bom;

public enum GrantType {
	authorization_code("authorization_code");
	
	protected String type;
	
	private GrantType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
