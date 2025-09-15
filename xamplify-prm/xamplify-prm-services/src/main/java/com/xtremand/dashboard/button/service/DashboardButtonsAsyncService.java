package com.xtremand.dashboard.button.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.Criteria.OPERATION_NAME;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.dashboard.button.dao.DashboardButtonDao;
import com.xtremand.dashboard.buttons.bom.DashboardButton;
import com.xtremand.dashboard.buttons.bom.DashboardButtonsPartnerGroupMapping;
import com.xtremand.dashboard.buttons.dto.DashboardButtonsPartnersDTO;
import com.xtremand.dashboard.buttons.dto.PublishedDashboardButtonDetailsDTO;
import com.xtremand.exception.EmailNotificationException;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.partnership.bom.Partnership;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.UserUserList;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.service.UserService;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.XamplifyUtilDao;
import com.xtremand.util.dto.UserListOperationsAsyncDTO;
import com.xtremand.util.service.ThymeLeafService;

@Service("dashboardButtonsAsyncService")
public class DashboardButtonsAsyncService {

	private static final Logger logger = LoggerFactory.getLogger(DashboardButtonsAsyncService.class);

	@Autowired
	private DashboardButtonDao dashboardButtonDao;

	@Autowired
	private ThymeLeafService thymeLeafService;

	@Autowired
	private UserService userService;

	@Autowired
	private XamplifyUtilDao xamplifyUtilDao;

	@Autowired
	private UserDAO userDao;

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class, noRollbackFor = {
			DataIntegrityViolationException.class, EmailNotificationException.class })
	public void findPartnerDetailsAndPublishWithNewlyAddedPartners(
			UserListOperationsAsyncDTO userListOperationsAsyncDTO, Integer userId,
			List<PublishedDashboardButtonDetailsDTO> publishedDashboardButtons) {
		if (XamplifyUtils.isNotEmptyList(publishedDashboardButtons)) {
			int totalNumberOfDashboardButtons = publishedDashboardButtons.size();
			String dashboardButtonsDTOLoggerMessage = "Total Number Of Dashboard Buttons To Publish : "
					+ totalNumberOfDashboardButtons;
			logger.debug(dashboardButtonsDTOLoggerMessage);
			Set<Integer> partnerUserIds = addPartnerIds(userListOperationsAsyncDTO);
			Set<Integer> distinctPublishedPartnerGroupIds = new HashSet<>();
			for (PublishedDashboardButtonDetailsDTO publishedDashboardButtonDetailsDTO : publishedDashboardButtons) {
				List<Integer> publishedPartnerGroupIds = publishedDashboardButtonDetailsDTO.getPartnerGroupIds();
				distinctPublishedPartnerGroupIds.addAll(publishedPartnerGroupIds);
			}

			List<DashboardButtonsPartnersDTO> dashboardButtonsPartnerGroupsDTOs = new ArrayList<>();
			for (Integer partnerGroupId : distinctPublishedPartnerGroupIds) {
				dashboardButtonsPartnerGroupsDTOs.addAll(
						dashboardButtonDao.findUserListIdsAndPartnerIdsAndPartnershipIdsByPartnerGroupIdAndPartnerIds(
								partnerGroupId, partnerUserIds));
			}
			if (XamplifyUtils.isNotEmptyList(dashboardButtonsPartnerGroupsDTOs)) {
				iterateListAndPublishToNewlyAddedPartners(userId, publishedDashboardButtons,
						totalNumberOfDashboardButtons, partnerUserIds, dashboardButtonsPartnerGroupsDTOs);
				User loggedInUser = userService.loadUser(
						Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, userId)),
						new FindLevel[] { FindLevel.COMPANY_PROFILE });
				CompanyProfile loggedInUserCompany = loggedInUser.getCompanyProfile();
				Integer companyId = loggedInUserCompany.getId();
				boolean isDashboardButtonEmailNotificationsEnabled = loggedInUserCompany
						.isDashboardButtonsEmailNotification();
				Set<String> dashboardButtonTitles = publishedDashboardButtons.stream()
						.map(PublishedDashboardButtonDetailsDTO::getButtonTitle).collect(Collectors.toSet());
				List<Integer> publishedPartnerUserIdsList = XamplifyUtils.convertSetToList(partnerUserIds);
				thymeLeafService.iterateDashboardButtonPublishedPartnersAndSendEmails(companyId, loggedInUser, "",
						publishedPartnerUserIdsList, dashboardButtonTitles, true,
						isDashboardButtonEmailNotificationsEnabled);

			}

		}

	}

	private Set<Integer> addPartnerIds(UserListOperationsAsyncDTO userListOperationsAsyncDTO) {
		Set<Integer> partnerUserIds = new HashSet<>();
		Set<UserDTO> partners = userListOperationsAsyncDTO.getPartners();
		if (XamplifyUtils.isNotEmptySet(partners)) {
			for (UserDTO partner : partners) {
				Integer partnerId = userDao.getUserIdByEmail(partner.getEmailId());
				if (XamplifyUtils.isValidInteger(partnerId)) {
					partnerUserIds.add(partnerId);
				}
			}
		}
		return partnerUserIds;
	}

	/* XNFR-597 */
	private void iterateListAndPublishToNewlyAddedPartners(Integer loggedInUserId,
			List<PublishedDashboardButtonDetailsDTO> publishedDashboardButtons, int totalNumberOfDashboardButtons,
			Set<Integer> partnerUserIds, List<DashboardButtonsPartnersDTO> dashboardButtonsPartnerGroupsDTOs) {
		int totalNumberOfPartners = partnerUserIds.size();
		String totalNumberOfPartnersLoggerMessage = "Total Number Of Partners : " + totalNumberOfPartners;
		logger.debug(totalNumberOfPartnersLoggerMessage);
		int dashboardButtonsCounter = 1;
		List<DashboardButtonsPartnerGroupMapping> dashboardButtonsPartnerGroupMappings = new ArrayList<>();
		for (PublishedDashboardButtonDetailsDTO dashboardButtonsDTO : publishedDashboardButtons) {
			List<Integer> publishedPartnerGroupIds = dashboardButtonsDTO.getPartnerGroupIds();
			String debugMessage = "Adding " + dashboardButtonsCounter + "/" + totalNumberOfDashboardButtons
					+ " Dashboard Buttons ";
			logger.debug(debugMessage);
			Integer dashBoardButtonId = dashboardButtonsDTO.getId();
			int partnersCounter = 1;
			for (DashboardButtonsPartnersDTO dashboardButtonsPartnersDTO : dashboardButtonsPartnerGroupsDTOs) {
				Integer partnerGroupId = dashboardButtonsPartnersDTO.getUserListId();
				boolean isPartnerGroupMatched = publishedPartnerGroupIds.indexOf(partnerGroupId) > -1;
				if (isPartnerGroupMatched) {
					String partnersDebugMessage = "Adding " + partnersCounter + "/" + totalNumberOfPartners
							+ " Partners ";
					logger.debug(partnersDebugMessage);
					DashboardButtonsPartnerGroupMapping dashboardButtonsPartnerGroupMapping = new DashboardButtonsPartnerGroupMapping();
					DashboardButton dashboardButton = new DashboardButton();
					dashboardButton.setId(dashBoardButtonId);
					dashboardButtonsPartnerGroupMapping.setDashboardButton(dashboardButton);
					UserUserList userUserList = new UserUserList();
					Integer userUserListId = dashboardButtonsPartnersDTO.getUserUserListId();
					userUserList.setId(userUserListId);
					dashboardButtonsPartnerGroupMapping.setUserUserList(userUserList);
					Partnership partnership = new Partnership();
					partnership.setId(dashboardButtonsPartnersDTO.getPartnershipId());
					dashboardButtonsPartnerGroupMapping.setPartnership(partnership);
					dashboardButtonsPartnerGroupMapping.setPublishedOn(new Date());
					User publishedBy = new User();
					publishedBy.setUserId(loggedInUserId);
					dashboardButtonsPartnerGroupMapping.setPublishedBy(publishedBy);
					dashboardButtonsPartnerGroupMappings.add(dashboardButtonsPartnerGroupMapping);
					partnersCounter++;
				} else {
					String unPublishedPartnerGroupMessage = dashboardButtonsDTO.getButtonTitle()
							+ " does not published to " + partnerGroupId;
					logger.debug(unPublishedPartnerGroupMessage);
				}

			}
			dashboardButtonsCounter++;
		}
		xamplifyUtilDao.saveAll(dashboardButtonsPartnerGroupMappings, "Dashboard Button Partner Group Mapping");
	}

}
