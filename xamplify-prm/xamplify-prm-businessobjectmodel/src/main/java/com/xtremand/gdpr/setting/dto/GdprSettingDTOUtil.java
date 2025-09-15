package com.xtremand.gdpr.setting.dto;

public class GdprSettingDTOUtil {
	
	
	private Integer id;

	private boolean gdprStatus;

	private boolean unsubscribeStatus;

	private boolean formStatus;

	private boolean termsAndConditionStatus;

	private boolean deleteContactStatus;

	private boolean eventStatus;
	
	private boolean allowMarketingEmails;
	
	

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public boolean isGdprStatus() {
		return gdprStatus;
	}

	public void setGdprStatus(boolean gdprStatus) {
		this.gdprStatus = gdprStatus;
	}

	public boolean isUnsubscribeStatus() {
		return unsubscribeStatus;
	}

	public void setUnsubscribeStatus(boolean unsubscribeStatus) {
		this.unsubscribeStatus = unsubscribeStatus;
	}

	public boolean isFormStatus() {
		return formStatus;
	}

	public void setFormStatus(boolean formStatus) {
		this.formStatus = formStatus;
	}

	public boolean isTermsAndConditionStatus() {
		return termsAndConditionStatus;
	}

	public void setTermsAndConditionStatus(boolean termsAndConditionStatus) {
		this.termsAndConditionStatus = termsAndConditionStatus;
	}

	public boolean isDeleteContactStatus() {
		return deleteContactStatus;
	}

	public void setDeleteContactStatus(boolean deleteContactStatus) {
		this.deleteContactStatus = deleteContactStatus;
	}

	public boolean isEventStatus() {
		return eventStatus;
	}

	public void setEventStatus(boolean eventStatus) {
		this.eventStatus = eventStatus;
	}

	public boolean isAllowMarketingEmails() {
		return allowMarketingEmails;
	}

	public void setAllowMarketingEmails(boolean allowMarketingEmails) {
		this.allowMarketingEmails = allowMarketingEmails;
	}

	

}
