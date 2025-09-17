package com.xtremand.util.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.PartnerTeamMemberViewType;
import com.xtremand.dashboard.button.dao.DashboardButtonDao;
import com.xtremand.dashboard.buttons.bom.DashboardButton;
import com.xtremand.dashboard.buttons.bom.DashboardButtonsPartnerGroupMapping;
import com.xtremand.dashboard.buttons.dto.DashboardButtonsPartnersDTO;
import com.xtremand.domain.bom.Domain;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.partnership.bom.Partnership;
import com.xtremand.partnership.bom.PartnershipDTO;
import com.xtremand.team.dao.TeamDao;
import com.xtremand.user.bom.Role;
import com.xtremand.user.bom.TeamMember;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.UserUserList;
import com.xtremand.user.exception.TeamMemberDataAccessException;
import com.xtremand.userlist.dao.UserListDAO;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dto.CompanyAndRolesDTO;
import com.xtremand.util.dto.ViewTypePatchRequestDTO;

@Service
public class UtilServiceWithOutTransactional {

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private TeamDao teamDao;

	@Autowired
	private PaginationUtil paginationUtil;

	@Autowired
	private UserListDAO userListDao;

	@Autowired
	private DashboardButtonDao dashboardButtonDao;

	private static final Logger logger = LoggerFactory.getLogger(UtilServiceWithOutTransactional.class);

	public XtremandResponse save() {
		XtremandResponse response = new XtremandResponse();
		List<CompanyAndRolesDTO> companyDetailsDTOs = teamDao.findCompanyDetailsAndRoles();
		List<Integer> companyIds = new ArrayList<>();
		for (CompanyAndRolesDTO companyDetailsDTO : companyDetailsDTOs) {
			Integer userId = companyDetailsDTO.getUserId();
			Integer companyId = companyDetailsDTO.getCompanyId();
			if (XamplifyUtils.isNotEmptyList(companyIds) && companyIds.indexOf(companyId) < 0) {
				String adminDomain = XamplifyUtils.getEmailDomain(companyDetailsDTO.getEmailId()).trim().toLowerCase();
				Domain domain = new Domain();
				domain.setDomainName(adminDomain);
				CompanyProfile company = new CompanyProfile();
				company.setId(companyId);
				domain.setCompany(company);
				domain.setCreatedUserId(userId);
				domain.setCreatedTime(new Date());
				domain.setUpdatedTime(domain.getCreatedTime());
				domain.setUpdatedUserId(userId);
				Session session = sessionFactory.openSession();
				try {
					Integer id = (Integer) session.save(domain);
					session.flush();
					session.clear();
					String domainAddedSuccessfully = adminDomain + " Added Successfully For " + companyId + " And Id : "
							+ id;
					logger.debug(domainAddedSuccessfully);
				} catch (ConstraintViolationException e) {
				} catch (HibernateException e) {
				} catch (DataIntegrityViolationException e) {
				} finally {
					session.close();
				}
			}
			companyIds.add(companyId);
		}
		response.setStatusCode(200);
		response.setMessage("Domains Added Successfully");
		return response;
	}

	public XtremandResponse updatePartnerViewTypes() {
		XtremandResponse response = new XtremandResponse();
		Session session = sessionFactory.openSession();
		List<PartnershipDTO> partnerships = findAllPartnerships(session);
		int totalRecords = partnerships.size();
		String debugMessage = "Total Records Found " + totalRecords;
		logger.debug(debugMessage);
		int loggerCounter = 1;
		for (PartnershipDTO partnershipDTO : partnerships) {
			String userProcessingMessage = loggerCounter + "/" + totalRecords
					+ " . Inserting View Types For Partnership Id " + partnershipDTO.getId();
			logger.debug(userProcessingMessage);
			Partnership partnership = new Partnership();
			partnership.setId(partnershipDTO.getId());
			Integer partnerCompanyId = partnershipDTO.getPartnerCompanyId();
			if (XamplifyUtils.isValidInteger(partnerCompanyId)) {
				ViewTypePatchRequestDTO primaryAdminDTO = findPrimaryAdminIdAndViewTypeByCompanyId(partnerCompanyId,
						session);
				if (primaryAdminDTO != null) {
					Integer adminId = primaryAdminDTO.getUserId();
					User admin = new User();
					admin.setUserId(adminId);
					insertViewTypesForTeamMembers(session, partnership, partnerCompanyId, admin);
					insertViewTypeForPartnerAdmin(session, partnership, primaryAdminDTO, admin);
				}

			}
			loggerCounter++;
		}
		XamplifyUtils.addSuccessStatusWithMessage(response, "View Types Added Successfully");
		return response;
	}

	private void insertViewTypeForPartnerAdmin(Session session, Partnership partnership,
			ViewTypePatchRequestDTO primaryAdminDTO, User admin) {
		Session openSession = sessionFactory.openSession();
		PartnerTeamMemberViewType adminViewType = new PartnerTeamMemberViewType();
		adminViewType.setAdmin(admin);
		adminViewType.setCreatedTime(new Date());
		adminViewType.setPartnership(partnership);
		adminViewType.setVendorViewType(primaryAdminDTO.getModulesDisplayType());
		adminViewType.setAdmin(admin);
		adminViewType.setUpdatedTime(new Date());
		try {
			Integer id = (Integer) openSession.save(adminViewType);
			session.flush();
			session.clear();
			String viewTypeAddedSuccessfully = " View Type Added Successfully For User Id " + admin.getUserId()
					+ " And Id : " + id;
			logger.debug(viewTypeAddedSuccessfully);
		} catch (ConstraintViolationException | DataIntegrityViolationException xx) {
			// Do Nothing
		} catch (HibernateException hex) {
			// Do Nothing Here
		} finally {
			openSession.close();
		}
	}

	private void insertViewTypesForTeamMembers(Session session, Partnership partnership, Integer partnerCompanyId,
			User admin) {
		List<ViewTypePatchRequestDTO> teamMembersAndPartnerIds = findTeamMemberIdsAndUserIdsAndViewTypesByCompanyId(
				partnerCompanyId, session);
		for (ViewTypePatchRequestDTO viewTypePatchRequestDTO : teamMembersAndPartnerIds) {
			Session openSession = sessionFactory.openSession();
			Integer teamMemberId = viewTypePatchRequestDTO.getTeamMemberId();
			PartnerTeamMemberViewType partnerTeamMemberViewType = new PartnerTeamMemberViewType();
			partnerTeamMemberViewType.setAdmin(admin);
			partnerTeamMemberViewType.setCreatedTime(new Date());
			partnerTeamMemberViewType.setPartnership(partnership);
			TeamMember teamMember = new TeamMember();
			teamMember.setId(viewTypePatchRequestDTO.getTeamMemberId());
			partnerTeamMemberViewType.setTeamMember(teamMember);
			partnerTeamMemberViewType.setVendorViewType(viewTypePatchRequestDTO.getModulesDisplayType());
			partnerTeamMemberViewType.setUpdatedTime(new Date());

			try {
				Integer id = (Integer) openSession.save(partnerTeamMemberViewType);
				openSession.flush();
				openSession.clear();
				String viewTypeAddedSuccessfully = " View Type Added Successfully For Team Member Id " + teamMemberId
						+ " And Id : " + id;
				logger.debug(viewTypeAddedSuccessfully);
			} catch (ConstraintViolationException | DataIntegrityViolationException xx) {
				// Do Nothing
			} catch (HibernateException hex) {
				// Do Nothing Here
			} finally {
				openSession.close();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private ViewTypePatchRequestDTO findPrimaryAdminIdAndViewTypeByCompanyId(Integer partnerCompanyId,
			Session session) {

		List<ViewTypePatchRequestDTO> primaryAdmins = (List<ViewTypePatchRequestDTO>) paginationUtil.getListDTO(
				ViewTypePatchRequestDTO.class,
				session.createSQLQuery(
						"select distinct u.user_id as \"userId\", cast(u.modules_display_type as text) as \"viewType\" from xt_company_profile c,xt_user_role ur,xt_user_profile u left join xt_team_member t on t.team_member_id = u.user_id where c.company_id = u.company_id and u.user_id = ur.user_id and \r\n"
								+ "role_id in (:roleIds) group by c.company_id,u.user_id having  CAST(count(t.id)AS integer) = 0 and c.company_id = :companyId")
						.setParameter("companyId", partnerCompanyId)
						.setParameterList("roleIds", Role.getAllAdminRoleIds()));
		if (XamplifyUtils.isNotEmptyList(primaryAdmins)) {
			ViewTypePatchRequestDTO viewTypePatchRequestDTO = primaryAdmins.get(0);
			if (primaryAdmins.size() > 1) {
				String debugMessage = "Duplicate Primary Admins Are : "
						+ primaryAdmins.stream().map(ViewTypePatchRequestDTO::getUserId).collect(Collectors.toList());
				logger.debug(debugMessage);
			}
			return viewTypePatchRequestDTO;
		} else {
			return null;
		}

	}

	private List<PartnershipDTO> findAllPartnerships(Session session) {
		String partnershipQueryString = "select distinct id as \"id\",partner_company_id as \"partnerCompanyId\", partner_id as \"representingPartnerId\",vendor_company_id as \"vendorCompanyId\" from xt_partnership where partner_company_id is not null  order by vendor_company_id  asc";
		@SuppressWarnings("unchecked")
		List<PartnershipDTO> partnerships = (List<PartnershipDTO>) paginationUtil.getListDTO(PartnershipDTO.class,
				session.createSQLQuery(partnershipQueryString));
		return partnerships;
	}

	@SuppressWarnings("unchecked")
	private List<ViewTypePatchRequestDTO> findTeamMemberIdsAndUserIdsAndViewTypesByCompanyId(Integer partnerCompanyId,
			Session session) {
		return (List<ViewTypePatchRequestDTO>) paginationUtil.getListDTO(ViewTypePatchRequestDTO.class,
				session.createSQLQuery(
						" select distinct u.user_id as \"userId\", t.id as \"teamMemberId\",cast(u.modules_display_type as text) as \"viewType\" from xt_company_profile c,xt_user_profile u left join xt_team_member t on t.team_member_id = u.user_id where c.company_id = u.company_id and c.company_id=:companyId and t.id is not null group by u.user_id,t.id ")
						.setParameter("companyId", partnerCompanyId));
	}

	public XtremandResponse publish(Integer userListId, Integer dashboardButtonId) {
		XtremandResponse response = new XtremandResponse();
		XamplifyUtils.addSuccessStatusWithMessage(response, "Dashboard Button Published Successfully");
		return response;
	}

	public XtremandResponse uploadBulkDummyDashboardButtons(Integer companyId, Integer size) {
		XtremandResponse response = new XtremandResponse();
		Integer primayAdminId = teamDao.findPrimaryAdminIdByCompanyId(companyId);
		List<DashboardButton> dashboardButtons = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			int suffix = i + 1;
			DashboardButton dashboardButton = new DashboardButton();
			dashboardButton.setButtonTitle("Dashboard Button-" + suffix);
			dashboardButton.setButtonLink("https://myntra.com");
			dashboardButton.setButtonIcon("fa fa-bug");
			CompanyProfile companyProfile = new CompanyProfile();
			companyProfile.setId(companyId);
			dashboardButton.setCompanyProfile(companyProfile);
			dashboardButton.setOrder(suffix);
			dashboardButton.setTimestamp(new Date());
			dashboardButton.setUpdatedTime(new Date());
			User user = new User();
			user.setUserId(primayAdminId);
			dashboardButton.setUser(user);
			dashboardButtons.add(dashboardButton);
		}

		LinkedHashSet<DashboardButton> dashboardButtonsHashSet = new LinkedHashSet<>(dashboardButtons);

		int counter = 1;
		for (DashboardButton dashboardButton : dashboardButtonsHashSet) {
			Session session = sessionFactory.openSession();
			try {
				session.save(dashboardButton);
				session.flush();
				session.clear();
			} catch (ConstraintViolationException con) {
				// Do Nothing
			} catch (HibernateException | TeamMemberDataAccessException e) {
				// Do Nothing
			} catch (Exception ex) {
				// Do Nothing
			} finally {
				session.close();
			}

			double completedPercentage = (double) Math.round(counter * 100.0) / size;

			String debugMessage = counter + "/" + size + "(" + completedPercentage + "%)" + " dashboard button addded. "
					+ dashboardButton.getButtonTitle() + "(" + dashboardButton.getId() + ")";

			logger.debug(debugMessage);
			counter++;

		}

		XamplifyUtils.addSuccessStatusWithMessage(response, "Dashboard Buttons Added Successfully");
		return response;
	}

	public XtremandResponse publishAllDashboardButtonsToDefaultPartnerGroups(Integer companyId) {
		XtremandResponse response = new XtremandResponse();
		Integer defaultPartnerListId = userListDao.getDefaultPartnerListIdByCompanyId(companyId);
		Set<Integer> partnerGroupIds = new HashSet<>();
		partnerGroupIds.add(defaultPartnerListId);
		List<DashboardButtonsPartnersDTO> dashboardButtonsPartnerGroupsDTOs = userListDao
				.findUserListIdsAndPartnerIdsAndPartnershipIdsByUserListIds(partnerGroupIds);
		List<Integer> unPublishedDashboardButtonIds = dashboardButtonDao
				.findUnPublishedDashboardButtonIdsByCompanyId(companyId);
		Integer primayAdminId = teamDao.findPrimaryAdminIdByCompanyId(companyId);
		dashboardButtonDao.updateStatus(XamplifyUtils.convertListToSetElements(unPublishedDashboardButtonIds), true);

		int dashboardCounter = 1;
		int totalDashboardButtons = unPublishedDashboardButtonIds.size();
		for (Integer dashboardButtonId : unPublishedDashboardButtonIds) {
			int dataCounter = 1;
			int totalPartners = dashboardButtonsPartnerGroupsDTOs.size();
			for (DashboardButtonsPartnersDTO dashboardButtonsPartnersDTO : dashboardButtonsPartnerGroupsDTOs) {
				dataCounter = insertIntoDashboardButtonPartnerGroupMapping(primayAdminId, dashboardButtonId,
						dataCounter, totalPartners, dashboardButtonsPartnersDTO, dashboardCounter,
						totalDashboardButtons);

			}
			dashboardButtonDao.updateStatus(dashboardButtonId, false);
			dashboardCounter++;
		}

		XamplifyUtils.addSuccessStatusWithMessage(response, "Dashboard Buttons Published Successfully");
		return response;
	}

	private int insertIntoDashboardButtonPartnerGroupMapping(Integer primayAdminId, Integer dashboardButtonId,
			int dataCounter, int totalPartners, DashboardButtonsPartnersDTO dashboardButtonsPartnersDTO,
			int dashboardCounter, int totalDashboardButtons) {
		DashboardButtonsPartnerGroupMapping dashboardButtonsPartnerGroupMapping = new DashboardButtonsPartnerGroupMapping();
		DashboardButton dashboardButton = new DashboardButton();
		dashboardButton.setId(dashboardButtonId);
		dashboardButtonsPartnerGroupMapping.setDashboardButton(dashboardButton);
		UserUserList userUserList = new UserUserList();
		userUserList.setId(dashboardButtonsPartnersDTO.getUserListId());
		dashboardButtonsPartnerGroupMapping.setUserUserList(userUserList);
		Partnership partnership = new Partnership();
		partnership.setId(dashboardButtonsPartnersDTO.getPartnershipId());
		dashboardButtonsPartnerGroupMapping.setPartnership(partnership);
		dashboardButtonsPartnerGroupMapping.setPublishedOn(new Date());
		User publishedBy = new User();
		publishedBy.setUserId(primayAdminId);
		dashboardButtonsPartnerGroupMapping.setPublishedBy(publishedBy);

		Session session = sessionFactory.openSession();
		try {
			session.save(dashboardButtonsPartnerGroupMapping);
			session.flush();
			session.clear();
		} catch (ConstraintViolationException con) {
			// Do Nothing For ConstraintViolationException
		} catch (HibernateException he) {
			// Do Nothing For HibernateException
		} catch (Exception ex) {
			// Do Nothing For Exception
		} finally {
			session.close();
		}

		double completedPercentageOfPublishedPartners = (double) Math.round(dataCounter * 100.0) / totalPartners;
		String patnerRowsInsertedDebugMessage = dataCounter + "/" + totalPartners + "("
				+ completedPercentageOfPublishedPartners + "%)" + " rows inserted";

		double completedPercentageOfPublishedDashboardButtons = (double) Math.round(dashboardCounter * 100.0)
				/ totalDashboardButtons;

		String dashboardButtonDebugMessage = dashboardCounter + "/" + totalDashboardButtons + "("
				+ completedPercentageOfPublishedDashboardButtons + "%)" + " buttons published for " + totalPartners
				+ " partners and " + patnerRowsInsertedDebugMessage + " at " + new Date();

		logger.debug(dashboardButtonDebugMessage);

		dataCounter++;
		return dataCounter;
	}

}
