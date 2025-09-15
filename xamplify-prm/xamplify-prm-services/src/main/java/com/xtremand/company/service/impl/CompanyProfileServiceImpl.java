package com.xtremand.company.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.CompanyProfile.CompanyNameStatus;
import com.xtremand.company.dao.CompanyProfileDao;
import com.xtremand.company.dto.EmailNotificationSettingsDTO;
import com.xtremand.company.service.CompanyProfileService;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.service.UserService;
import com.xtremand.util.XamplifyUtils;

@Service
@Transactional
public class CompanyProfileServiceImpl implements CompanyProfileService {

	@Value("${settings.updated}")
	private String settingsUpdated;
	
	@Value("#{'${company.profile.name.suffixes}'.split(',')}")
	private List<String> companyProfileNameSuffixesForAutoFilling;

	@Autowired
	private CompanyProfileDao companyDao;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private UserService userService;
	
	@Autowired
	private GenericDAO genericDAO;

	@Override
	public XtremandResponse getEmailNotificationSettings(Integer userId) {
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		EmailNotificationSettingsDTO emailNotificationSettingsDTO = companyDao.getEmailNotificationSettings(companyId);
		XtremandResponse response = new XtremandResponse();
		response.setData(emailNotificationSettingsDTO);
		XamplifyUtils.addSuccessStatus(response);
		return response;
	}

	@Override
	public XtremandResponse updateEmailNotificationSettings(Integer userId,
			EmailNotificationSettingsDTO emailNotificationSettingsDTO) {
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		XtremandResponse response = new XtremandResponse();
		companyDao.updateEmailNotificationSettings(companyId, emailNotificationSettingsDTO);
		XamplifyUtils.addSuccessStatusWithMessage(response, settingsUpdated);
		return response;
	}

	@Override
	public XtremandResponse findAllUsers(Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		response.setData(userService.listTeamMembers(loggedInUserId));
		return response;
	}
	
	@Override
	public CompanyProfile createPartnerCompany(UserDTO nonExistingUser, Integer addedAdminCompanyId) {
		CompanyProfile companyProfile = new CompanyProfile();
		if(companyDao.isDuplicateCompanyNameExistWithAddedAdminCompanyId(addedAdminCompanyId, nonExistingUser.getContactCompany().trim())) {
		    String companyName = generateCompanyNameWithSuffix(addedAdminCompanyId, nonExistingUser.getContactCompany().trim());
			companyProfile.setCompanyName(companyName);
		} else {
			companyProfile.setCompanyName(nonExistingUser.getContactCompany().trim());
		}
		companyProfile.setCompanyNameStatus(CompanyNameStatus.INACTIVE);
		companyProfile.setAddedAdminCompanyId(addedAdminCompanyId);
		companyProfile.setCity(nonExistingUser.getCity());
		companyProfile.setState(nonExistingUser.getState());
		companyProfile.setZip(nonExistingUser.getZipCode());
		companyProfile.setCountry(nonExistingUser.getCountry());
		companyProfile.setStreet(nonExistingUser.getAddress());
		/** XNFR-929 **/
		genericDAO.save(companyProfile);
		return companyProfile;
	}
	
	@Override
	public boolean isCompanyNameExists(String companyName) {
		return companyDao.companyNameExists(companyName);
	}
	
	private String generateCompanyNameWithSuffix(Integer addedAdminCompanyId, String companyName) {

		if (!XamplifyUtils.isValidString(companyName) || companyName == null) {
			return "";
		}

		String regeneratedName = companyName;

		companyName = companyName.replaceAll("[^a-zA-Z0-9]", "").toLowerCase().trim();
		companyName = companyName.substring(0, Math.min(50, companyName.length()));

		try {
			List<String> companyNameSuffixes = companyProfileNameSuffixesForAutoFilling;
			for (String suffix : companyNameSuffixes) {
				String truncatedName = companyName.length() > 44 ? companyName.substring(0, 44) : companyName;
				String companyNameSuffix = truncatedName + suffix;
				if (!(companyDao.isDuplicateCompanyNameExistWithAddedAdminCompanyId(addedAdminCompanyId,
						companyNameSuffix))) {
					return companyNameSuffix;
				}
			}
		} catch (Exception e) {
			return regeneratedName;
		}
		return regeneratedName;
	}

}
