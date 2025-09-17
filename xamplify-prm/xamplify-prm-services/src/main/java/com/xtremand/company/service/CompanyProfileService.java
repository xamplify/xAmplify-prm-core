package com.xtremand.company.service;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.company.dto.EmailNotificationSettingsDTO;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.XtremandResponse;

public interface CompanyProfileService {
	
	
	public XtremandResponse getEmailNotificationSettings(Integer userId);

	public XtremandResponse updateEmailNotificationSettings(Integer userId,
			EmailNotificationSettingsDTO emailNotificationSettingsDTO);

	public XtremandResponse findAllUsers(Integer loggedInUserId);
	
	public CompanyProfile createPartnerCompany(UserDTO nonExistingUser, Integer addedAdminCompanyId);
	
	public boolean isCompanyNameExists(String companyName);

}
