package com.xtremand.company.dto;

import java.io.Serializable;

import lombok.Data;

@Data
/*** XNFR-326 ****/
public class EmailNotificationSettingsDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 6192172123536595583L;

	private boolean notifyPartners;

	private boolean partnerOnBoardVendorEmailNotification;

	private boolean assetPublishedEmailNotification;

	private boolean trackPublishedEmailNotification;

	private boolean playbookPublishedEmailNotification;

	private boolean dashboardButtonsEmailNotification;

	private boolean dashboardBannersEmailNotification;

	private boolean newsAndAnnouncementsEmailNotification;

	/**** XNFR-688 *****/
	private boolean assetPublishVendorEmailNotification;

	private boolean trackPublishVendorEmailNotification;

	private boolean playbookPublishVendorEmailNotification;

	private boolean dashboardButtonPublishVendorEmailNotification;

}
