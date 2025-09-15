package com.xtremand.company.dao;

import com.xtremand.company.dto.EmailNotificationSettingsDTO;
import com.xtremand.util.dto.CompanyDetailsDTO;

public interface CompanyProfileDao {

	EmailNotificationSettingsDTO getEmailNotificationSettings(Integer companyId);

	void updateEmailNotificationSettings(Integer companyId, EmailNotificationSettingsDTO emailNotificationSettingsDTO);

	boolean isAssetPublishedEmailNotificationByUserId(Integer userId);

	boolean isAssetPublishedEmailNotificationByCompanyProfileName(String companyProfileName);

	boolean isTrackPublishedEmailNotificationByUserId(Integer userId);

	boolean isTrackPublishedEmailNotificationByCompanyProfileName(String companyProfileName);

	boolean isPlaybookPublishedEmailNotificationByUserId(Integer userId);

	boolean isPlaybookPublishedEmailNotificationByCompanyProfileName(String companyProfileName);

	boolean isDashboardButtonPublishedEmailNotificationByUserId(Integer userId);

	boolean isDashboardButtonPublishedEmailNotificationByCompanyProfileName(String companyProfileName);

	boolean isDashboardBannerPublishedEmailNotificationByUserId(Integer userId);

	boolean isDashboardBannerPublishedEmailNotificationByCompanyProfileName(String companyProfileName);

	boolean isNewsAndAnnouncementsPublishedEmailNotificationByUserId(Integer userId);

	boolean isNewsAndAnnouncementsPublishedEmailNotificationByCompanyProfileName(String companyProfileName);

	public boolean companyNameExists(String companyName);

	public boolean companyNameExists(String companyName, Integer partnerCompanyId);

	boolean isPartnerOnBoardVendorEmailNotificationEnabledByCompanyId(Integer companyId);

	public void turnOffEmailNotificationSettingsOptionForAssetsModuleByCompanyId(Integer companyId);

	public void turnOffEmailNotificationSettingsOptionForTracksModuleByCompanyId(Integer companyId);

	public void turnOffEmailNotificationSettingsOptionForPlayBooksModuleByCompanyId(Integer companyId);

	public boolean companyNameExists(Integer companyId, String companyName);

	/*** XNFR-832 ****/
	CompanyDetailsDTO findCompanyDetailsByLoggedInUserId(Integer loggedInUserId);

	public boolean isDuplicateCompanyNameExistWithAddedAdminCompanyId(Integer addedAdminCompanyId, String companyName);

}
