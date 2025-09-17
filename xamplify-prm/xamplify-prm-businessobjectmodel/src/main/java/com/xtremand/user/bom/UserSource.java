package com.xtremand.user.bom;

public enum UserSource {
	SIGNUP("SIGNUP"), ADMIN("ADMIN"), ALLBOUND("ALLBOUND"), SAMLSSO("SAMLSSO"), OAUTHSSO("OAUTHSSO"), INVITATION("INVITATION");
	protected String source;

	private UserSource(String source) {
		this.source = source;
	}

	public String getSource() {
		return source;
	}
}
