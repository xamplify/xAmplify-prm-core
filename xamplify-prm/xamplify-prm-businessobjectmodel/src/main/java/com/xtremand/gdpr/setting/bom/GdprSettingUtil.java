package com.xtremand.gdpr.setting.bom;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class GdprSettingUtil implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7849986551001518758L;

	@Column(name="gdpr_status")
	private boolean gdprStatus;
	
	@Column(name="unsubscirbe_status")
	private boolean unsubscribeStatus;
	
	@Column(name="form_status")
	private boolean formStatus;
	
	@Column(name="terms_condition_status")
	private boolean termsAndConditionStatus;
	
	@Column(name="delete_contact_status")
	private boolean deleteContactStatus;
	
	@Column(name="event_status")
	private boolean eventStatus;
	
	@Column(name="allow_marketing_emails")
	private boolean allowMarketingEmails;

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
