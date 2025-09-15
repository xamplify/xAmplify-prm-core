package com.xtremand.mail.bom;

public enum EmailTemplateSource {
	MANUAL("MANUAL"), HUBSPOT("HUBSPOT"), MARKETO("MARKETO"), PARDOT("PARDOT");
	protected String source;

	private EmailTemplateSource(String source) {
		this.source = source;
	}

	public String getSource() {
		return source;
	}
}
