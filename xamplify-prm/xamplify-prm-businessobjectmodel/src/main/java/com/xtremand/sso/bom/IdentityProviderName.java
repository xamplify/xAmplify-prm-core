package com.xtremand.sso.bom;

public enum IdentityProviderName {
	MICROSOFT_AZURE("MICROSOFT_AZURE"), VERSA("VERSA");
	
	protected String type;
	private IdentityProviderName(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
