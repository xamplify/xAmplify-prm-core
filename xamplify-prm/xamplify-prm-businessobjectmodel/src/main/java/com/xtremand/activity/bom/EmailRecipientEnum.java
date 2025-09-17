package com.xtremand.activity.bom;

public enum EmailRecipientEnum {

	TO("TO"),CC("CC"),BCC("BCC");
	
	protected String emailRecipientType;
	
	private EmailRecipientEnum(String emailRecipientType) {
		this.emailRecipientType = emailRecipientType;
	}
	
	public String getEmailRecipientType() {
		return emailRecipientType;
	}
}
