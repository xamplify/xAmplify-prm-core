
package com.xtremand.upgrade.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.deal.service.DealService;
import com.xtremand.exception.DuplicateEntryException;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.mail.service.AsyncComponent;
import com.xtremand.team.member.group.dao.TeamMemberGroupDao;
import com.xtremand.team.member.group.service.TeamMemberGroupService;
import com.xtremand.upgrade.bom.UpgradeRole;
import com.xtremand.upgrade.bom.UpgradeRoleRequestStatus;
import com.xtremand.upgrade.dao.UpgradeRoleDao;
import com.xtremand.upgrade.dto.UpgradeRoleDTO;
import com.xtremand.upgrade.dto.UpgradeRoleGetDTO;
import com.xtremand.upgrade.service.UpgradeRoleService;
import com.xtremand.user.bom.Role;
import com.xtremand.user.bom.User;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.service.UserService;
import com.xtremand.util.BadRequestException;
import com.xtremand.util.XamplifyUtil;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.UpgradeRoleEmailNotification;
import com.xtremand.util.service.UtilService;
import com.xtremand.video.bom.VideoDefaultSettings;
import com.xtremand.video.dao.VideoDao;

@Service
public class UpgradeRoleServiceImpl implements UpgradeRoleService {

	@Autowired
	private UpgradeRoleDao upgradeRoleDao;

	@Autowired
	private GenericDAO genericDao;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private XamplifyUtil xamplifyUtil;

	@Autowired
	private UtilDao utildao;

	@Autowired
	private TeamMemberGroupDao teamMemberGroupDao;

	@Autowired
	private UserService userService;

	@Autowired
	private TeamMemberGroupService teamMemberGroupService;

	@Autowired
	private UtilService utilService;

	@Autowired
	private AsyncComponent asyncComponent;

	@Autowired
	private DealService dealService;

	@Autowired
	private VideoDao videoDao;

	@Value("${default.player.color}")
	private String playerDefaultColor;

	@Value("${default.controller.color}")
	private String controllerDefaultColor;

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse saveRequest(Integer userId) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			UpgradeRole upgradeRole = new UpgradeRole();
			CompanyProfile requestedCompany = new CompanyProfile();
			Integer companyId = userDao.getCompanyIdByUserId(userId);
			if (!upgradeRoleDao.isRequestExists(companyId)) {
				requestedCompany.setId(companyId);
				upgradeRole.setRequestedCompany(requestedCompany);
				upgradeRole.setUpgradeRoleRequestStatus(UpgradeRoleRequestStatus.REQUESTED);
				upgradeRole.setCreatedBy(userId);
				upgradeRole.setCreatedTime(new Date());
				upgradeRole.setUpdatedTime(new Date());
				upgradeRole.setUpdatedBy(userId);
				genericDao.save(upgradeRole);
				return response;
			} else {
				throw new DuplicateEntryException("Duplicate Request");
			}
		} catch (DuplicateEntryException e) {
			throw new DuplicateEntryException(e.getMessage());
		} catch (XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		}
	}

	@Override
	public XtremandResponse isRequestExists(Integer userId) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			Integer companyId = userDao.getCompanyIdByUserId(userId);
			response.setData(upgradeRoleDao.isRequestExists(companyId));
			return response;
		} catch (XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public XtremandResponse findAll(Pagination pagination) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			Map<String, Object> map = upgradeRoleDao.findAll(pagination);
			List<UpgradeRoleDTO> requests = (List<UpgradeRoleDTO>) map.get("list");
			String imagesPrefixPath = xamplifyUtil.getImagesPrefixPath();
			for (UpgradeRoleDTO request : requests) {
				String companyLogo = request.getCompanyLogo();
				if (StringUtils.hasText(companyLogo)) {
					request.setCompanyLogo(imagesPrefixPath + companyLogo);
				} else {
					request.setCompanyLogo("-");
				}
			}
			response.setData(map);
			return response;
		} catch (XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		}
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse upgradeToMarketing(Integer requestId) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			boolean isRequestApproved = upgradeRoleDao.isRequestApproved(requestId);
			if (isRequestApproved) {
				throw new BadRequestException("This Request Already Approved");
			} else {
				return checkUserRoleAndUpgrade(requestId, response);
			}
		} catch (BadRequestException e) {
			throw new BadRequestException(e.getMessage());
		} catch (XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e);
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		}
	}

	private XtremandResponse checkUserRoleAndUpgrade(Integer requestId, XtremandResponse response) {
		UpgradeRoleGetDTO upgradeRoleGetDTO = upgradeRoleDao.findCompanyIdAndCreatedBy(requestId);
		Integer companyId = upgradeRoleGetDTO.getCompanyId();
		Integer userId = upgradeRoleGetDTO.getUserId();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("userId", userId);
		response.setMap(map);
		List<Integer> roleIds = utildao.findRoleIdsByCompanyId(companyId);
		boolean isOnlyUser = roleIds.indexOf(Role.USER_ROLE.getRoleId()) > -1 && roleIds.size() == 1;
		if (isOnlyUser) {
			/***** Upgrade Role To Marketing & Add Default Group **********/
			User loggedinUser = userService.findByPrimaryKey(userId,
					new FindLevel[] { FindLevel.ROLES, FindLevel.COMPANY_PROFILE });
			/**** Change Default Group ****/
			teamMemberGroupDao.changeDefaultGroup(companyId, userId);
			/*** Delete Data From TeamMemberGroupRoleMapping ***********/
			teamMemberGroupService.findAndDeleteTeamMemberGroupRoleMappingsByCompanyId(companyId);
			/**** Add Default Lead & Deal *************/
			dealService.createDefaultDealPipeline(loggedinUser.getCompanyProfile(), userId);
			dealService.createDefaultLeadPipeline(loggedinUser.getCompanyProfile(), userId);

			/******* Add Video Default Settings XBI-246 *******/
			CompanyProfile companyProfile = new CompanyProfile();
			companyProfile.setId(companyId);
			updateVideoDefaultSettings(companyProfile);

			/**** Update The Request ************/
			upgradeRoleDao.approveRequest(requestId);
			utilService.revokeAccessTokensByCompanyId(companyId);
			response.setStatusCode(200);
			response.setMessage("Upgraded Successfully To Marketing Role");
		} else {
			throw new BadRequestException("This Request Cannot Be Upgraded.Because This Account Has Been Upgraded.");
		}
		return response;
	}

	private void updateVideoDefaultSettings(CompanyProfile companyProfile) {
		VideoDefaultSettings videoDefaultSettingsFromDb = videoDao.getVideoDefaultSettings(companyProfile.getId());
		if (videoDefaultSettingsFromDb == null) {
			videoDefaultSettingsFromDb = new VideoDefaultSettings();
			videoDefaultSettingsFromDb.setCompanyProfile(companyProfile);
			updateVideoSettingOptions(videoDefaultSettingsFromDb);
			genericDao.save(videoDefaultSettingsFromDb);
		} else {
			updateVideoSettingOptions(videoDefaultSettingsFromDb);
		}

	}

	private void updateVideoSettingOptions(VideoDefaultSettings videoDefaultSettingsFromDb) {
		videoDefaultSettingsFromDb.setPlayerColor(playerDefaultColor);
		videoDefaultSettingsFromDb.setEnableVideoController(true);
		videoDefaultSettingsFromDb.setControllerColor(controllerDefaultColor);
		videoDefaultSettingsFromDb.setAllowSharing(true);
		videoDefaultSettingsFromDb.setEnableSettings(true);
		videoDefaultSettingsFromDb.setAllowFullscreen(true);
		videoDefaultSettingsFromDb.setAllowComments(true);
		videoDefaultSettingsFromDb.setAllowLikes(true);
		videoDefaultSettingsFromDb.setEnableCasting(true);
		videoDefaultSettingsFromDb.setAllowEmbed(true);
		videoDefaultSettingsFromDb.setTransparency(100);
		videoDefaultSettingsFromDb.setIs360video(false);
	}

	@Override
	public void sendEmailNotificationToSuperAdmin(Integer userId) {
	}

	@Override
	public void sendUpgradeSuccessEmailNotification(Integer userId) {
	}

	@Override
	public void sendUpgradeSuccessEmailNotification(UpgradeRoleEmailNotification upgradeRoleEmailNotification) {
	}

}
