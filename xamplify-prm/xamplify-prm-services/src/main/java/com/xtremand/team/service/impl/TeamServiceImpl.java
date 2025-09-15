package com.xtremand.team.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.hibernate.HibernateException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xtremand.approve.dao.ApproveDAO;
import com.xtremand.approve.dto.ContentReApprovalDTO;
import com.xtremand.approve.service.ApproveService;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.Criteria.OPERATION_NAME;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.common.bom.PartnerTeamMemberViewType;
import com.xtremand.dam.bom.ApprovalStatusHistory;
import com.xtremand.dam.bom.DamTag;
import com.xtremand.dam.dao.DamDao;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.domain.bom.DomainModuleNameType;
import com.xtremand.domain.dao.DomainDao;
import com.xtremand.exception.DuplicateEntryException;
import com.xtremand.form.dao.FormDao;
import com.xtremand.formbeans.ReferedVendorDTO;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.VendorInvitationDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.mail.service.AsyncComponent;
import com.xtremand.mail.service.MailService;
import com.xtremand.mail.service.StatusCodeConstants;
import com.xtremand.partnership.bom.PartnerTeamGroupMapping;
import com.xtremand.partnership.bom.Partnership;
import com.xtremand.partnership.dao.PartnershipDAO;
import com.xtremand.partnership.service.PartnershipService;
import com.xtremand.signup.dto.SignUpRequestDTO;
import com.xtremand.team.dao.TeamDao;
import com.xtremand.team.member.dto.DeleteTeamMemberPartnersRequestDTO;
import com.xtremand.team.member.dto.PartnerPrimaryAdminUpdateDto;
import com.xtremand.team.member.dto.TeamMemberAddType;
import com.xtremand.team.member.dto.TeamMemberAndPartnerIdsAndUserListIdDTO;
import com.xtremand.team.member.dto.TeamMemberDTO;
import com.xtremand.team.member.dto.TeamMemberGroupDTO;
import com.xtremand.team.member.dto.TeamMemberListDTO;
import com.xtremand.team.member.group.bom.TeamMemberGroup;
import com.xtremand.team.member.group.bom.TeamMemberGroupRoleMapping;
import com.xtremand.team.member.group.bom.TeamMemberGroupUserMapping;
import com.xtremand.team.member.group.dao.TeamMemberGroupDao;
import com.xtremand.team.service.TeamService;
import com.xtremand.user.bom.LegalBasis;
import com.xtremand.user.bom.ModulesDisplayType;
import com.xtremand.user.bom.Role;
import com.xtremand.user.bom.TeamMember;
import com.xtremand.user.bom.TeamMemberStatus;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.User.UserDefaultPage;
import com.xtremand.user.bom.User.UserStatus;
import com.xtremand.user.bom.UserList;
import com.xtremand.user.bom.UserSource;
import com.xtremand.user.bom.UserUserList;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.exception.TeamMemberDataAccessException;
import com.xtremand.user.service.UserService;
import com.xtremand.userlist.dao.UserListDAO;
import com.xtremand.util.BadRequestException;
import com.xtremand.util.CustomValidatonException;
import com.xtremand.util.GenerateRandomPassword;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.XamplifyUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dao.XamplifyUtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.QueryParameterListDTO;
import com.xtremand.util.dto.SortColumnDTO;
import com.xtremand.util.dto.XamplifyConstants;
import com.xtremand.util.service.EmailValidatorService;
import com.xtremand.util.service.UtilService;
import com.xtremand.vanity.email.templates.bom.CustomDefaultEmailTemplate;
import com.xtremand.vanity.email.templates.bom.DefaultEmailTemplate;
import com.xtremand.vanity.email.templates.bom.DefaultEmailTemplateType;
import com.xtremand.vanity.url.dao.VanityURLDao;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;
import com.xtremand.video.bom.VideoTag;
import com.xtremand.video.dao.VideoDao;

@Service
@Transactional
public class TeamServiceImpl implements TeamService {

	private static final String ERROR = "error";

	private static final String TEAM_MEMBER_EMAIL_IDS = "teamMemberEmailIds";

	private static final String TEAM_MEMBER_DTO = "teamMemberDTO";

	private static final Logger logger = LoggerFactory.getLogger(TeamServiceImpl.class);

	private static final String VIDEO_TAGS_TO_SAVE_MAP_KEY = "videoTagsToSave";

	private static final String VIDEO_IDS_TO_DELETE_MAP_KEY = "videoIdsToDelete";

	private static final String WHITE_LABELED_RE_APPROVAL_DAM_IDS = "whiteLabeledReApprovalDamIds";

	@Value("${team.member.errorMessage}")
	private String teamMemberErrorMessage;

	@Value("${team.members.errorMessage}")
	private String teamMembersErrorMessage;

	@Autowired
	private TeamDao teamDao;

	@Autowired
	private GenericDAO genericDao;

	@Autowired
	private TeamMemberGroupDao teamMemberGroupDao;

	@Autowired
	private UserService userService;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private UtilService utilService;

	@Autowired
	private MailService mailService;

	@Autowired
	private UserListDAO userListDao;

	@Autowired
	private DamDao damDao;

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	@Autowired
	private PartnershipService partnershipService;

	@Autowired
	@Lazy
	private AsyncComponent asyncComponent;

	@Autowired
	private FormDao formDao;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private DomainDao domainDao;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private EmailValidatorService emailValidatorService;

	@Autowired
	private XamplifyUtil xamplifyUtil;

	@Autowired
	private ApproveService approveService;

	@Autowired
	private PartnershipDAO partnershipDao;

	@Autowired
	private XamplifyUtilDao xamplifyUtilDao;

	@Autowired
	private VideoDao videoDAO;

	@Value("${role.orgadmin}")
	private String orgAdminRole;

	@Value("${role.vendor}")
	private String vendorRole;

	@Value("${role.partner}")
	private String partnerRole;

	@Value("${role.teamMember}")
	private String teamMemberRole;

	@Value("${role.orgadmin.partner}")
	private String orgAdminAndPartnerRole;

	@Value("${role.vendor.partner}")
	private String vendorAndPartnerRole;

	@Value("${role.user}")
	private String userRole;

	@Value("${role.distributor}")
	private String distributorRole;

	@Value("${role.vendor.tier}")
	private String vendorTierRole;

	@Value("${role.vendor.tier.partner}")
	private String vendorTierAndPartnerRole;

	@Value("${role.marketing.partner}")
	private String marketingAndPartnerRole;

	@Value("${role.marketing}")
	private String marketingRole;

	@Value("${role.prm}")
	private String prmRole;

	@Value("${role.prm.partner}")
	private String prmAndPartnerRole;

	@Value("${inactive.team.member}")
	private String inActiveTeamMemberMessage;

	@Value("${no.access.login.as}")
	private String noAccessForLoginAs;

	@Value("${server_url}")
	private String serverUrl;

	@Value("${images.folder}")
	private String vod;

	@Value("${replace.company.logo}")
	private String replaceCompanyLogo;

	@Value("${server_path}")
	String serverPath;

	@Autowired
	private PaginationUtil paginationUtil;

	@Autowired
	private ApproveDAO approveDao;

	@Autowired
	private VanityURLDao vanityURLDao;

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse saveAll(TeamMemberDTO teamMemberDTO) {
		try {
			XtremandResponse response = new XtremandResponse();
			if (teamMemberDTO.getTeamMemberDTOs() != null && !teamMemberDTO.getTeamMemberDTOs().isEmpty()
					&& teamMemberDTO.getUserId() != null && teamMemberDTO.getUserId() > 0) {
				List<TeamMemberDTO> teamMemberDTOs = teamMemberDTO.getTeamMemberDTOs();
				teamMemberDTOs.forEach(member -> member.setNewTeamMember(true));
				List<String> teamMemberEmailIds = teamMemberDTOs.stream().map(TeamMemberDTO::getEmailId)
						.collect(Collectors.toList());
				Set<String> duplicateEmailIds = XamplifyUtils.findDuplicateStrings(teamMemberEmailIds);
				if (duplicateEmailIds.isEmpty()) {
					validateTeamMemberGroupsAndEmailIdsAndAddTeamMembers(teamMemberDTO, response, teamMemberDTOs,
							teamMemberEmailIds);
					if (response.getStatusCode() == 200) {
						asyncComponent.publishLMSToNewTeamMembers(teamMemberDTOs, teamMemberDTO.getUserId());
					}
				} else {
					response.setStatusCode(400);
					response.setMessage("Below are duplicate emailIds");
					response.setData(duplicateEmailIds);
				}
			} else {
				response.setStatusCode(400);
				response.setMessage("userId / teamMemberDTOs are missing");
			}
			return response;
		} catch (DuplicateEntryException e) {
			throw new DuplicateEntryException(e.getMessage());
		} catch (HibernateException | TeamMemberDataAccessException e) {
			throw new TeamMemberDataAccessException(e.getMessage());
		} catch (Exception ex) {
			throw new TeamMemberDataAccessException(ex.getMessage());
		}
	}

	private void validateTeamMemberGroupsAndEmailIdsAndAddTeamMembers(TeamMemberDTO teamMemberDTO,
			XtremandResponse response, List<TeamMemberDTO> teamMemberDTOs, List<String> teamMemberEmailIds) {
		validateTeamMemberGroups(response, teamMemberDTOs);
		if (response.getStatusCode() != 401) {
			validateEmailIds(response, teamMemberEmailIds);
			if (response.getStatusCode() != 413) {
				validateExtraAdminAndAddTeamMembers(teamMemberDTO, response, teamMemberDTOs, teamMemberEmailIds);
			}
		}

	}

	private void validateExtraAdminAndAddTeamMembers(TeamMemberDTO teamMemberDTO, XtremandResponse response,
			List<TeamMemberDTO> teamMemberDTOs, List<String> teamMemberEmailIds) {
		User loggedInUser = userDao.findByPrimaryKey(teamMemberDTO.getUserId(),
				new FindLevel[] { FindLevel.ROLES, FindLevel.COMPANY_PROFILE });
		Integer secondOrgAdminsCount = teamMemberDTOs.stream().filter(TeamMemberDTO::isSecondAdmin)
				.collect(Collectors.toList()).size();
		/**** XNFR-139 ******/
		Integer companyId = loggedInUser.getCompanyProfile().getId();
		Integer maxAdminsLimit = utilDao.findMaxAdminsCountByCompanyId(loggedInUser.getCompanyProfile().getId());
		Integer availedAdminsCount = teamDao.getSecondAdminsCountByCompanyId(companyId) + 1;
		Integer availableAdminsLimit = maxAdminsLimit - availedAdminsCount;

		if (secondOrgAdminsCount != null && secondOrgAdminsCount > availableAdminsLimit) {
			setErrorMessageForMaximumAdminsLimitReached(response, maxAdminsLimit, availableAdminsLimit);
		} else {
			Set<TeamMemberDTO> secondOrgAdminTeamMemberDtos = teamMemberDTOs.stream()
					.filter(TeamMemberDTO::isSecondAdmin).collect(Collectors.toSet());
			if (secondOrgAdminTeamMemberDtos != null && !secondOrgAdminTeamMemberDtos.isEmpty()) {
				TeamMemberDTO secondAdminTeamMemberDto = secondOrgAdminTeamMemberDtos.iterator().next();
				List<Integer> roleIds = teamMemberGroupDao
						.findRoleIdsByTeamMemberGroupId(secondAdminTeamMemberDto.getTeamMemberGroupId());
				if (roleIds.indexOf(Role.ALL_ROLES.getRoleId()) > -1) {
					showSuccessMessage(teamMemberDTO, response, teamMemberDTOs, teamMemberEmailIds, loggedInUser);
				} else {
					response.setStatusCode(StatusCodeConstants.INVALID_TEAM_MEMBER_GROUP);
					response.setMessage(
							"Enable As An Admin option is not available for the selected team member group");
				}
			} else {
				showSuccessMessage(teamMemberDTO, response, teamMemberDTOs, teamMemberEmailIds, loggedInUser);
			}

		}

	}

	private void setErrorMessageForMaximumAdminsLimitReached(XtremandResponse response, Integer maxAdminsCount,
			Integer availableLimit) {
		response.setStatusCode(StatusCodeConstants.MORE_THAN_TWO_ORG_ADMINS_FOUND);
		if (availableLimit != null && availableLimit.equals(0)) {
			response.setMessage("Maximum limit (" + maxAdminsCount
					+ ") for 'Enable As An Admin' is reached.Please contact admin to upgrade.");
		} else {
			response.setMessage("Maximum limit for 'Enable As An Admin' : " + availableLimit);
		}
	}

	private void showSuccessMessage(TeamMemberDTO teamMemberDTO, XtremandResponse response,
			List<TeamMemberDTO> teamMemberDTOs, List<String> teamMemberEmailIds, User loggedInUser) {
		List<TeamMember> teamMembers = new ArrayList<>();
		List<User> newUsers = new ArrayList<>();
		iterateDtosAndAddTeamMembers(teamMemberDTOs, loggedInUser, teamMembers, newUsers);
		response.setStatusCode(200);
		Map<String, Object> map = new HashMap<>();
		map.put(TEAM_MEMBER_DTO, teamMemberDTO);
		map.put(TEAM_MEMBER_EMAIL_IDS, teamMemberEmailIds);
		response.setMap(map);
		if (teamMemberEmailIds.size() > 1) {
			response.setMessage("Team Members added successfully.");
		} else {
			response.setMessage("Team Member added successfully.");
		}
	}

	public void iterateDtosAndAddTeamMembers(List<TeamMemberDTO> teamMemberDTOs, User loggedInUser,
			List<TeamMember> teamMembers, List<User> newUsers) {
		Integer companyId = loggedInUser.getCompanyProfile().getId();
		for (TeamMemberDTO teamMemberDTO : teamMemberDTOs) {
			Integer teamMemberGroupId = teamMemberDTO.getTeamMemberGroupId();
			List<Integer> roleIds = teamMemberGroupDao.findRoleIdsByTeamMemberGroupId(teamMemberGroupId);
			String emailId = teamMemberDTO.getEmailId();
			if (StringUtils.hasText(emailId)) {
				User existingUser = userDao.getUserByEmail(emailId.trim().toLowerCase());
				UserList teamMemberUserList = new UserList();
				if (existingUser != null) {
					TeamMember teamMember = new TeamMember();
					teamMember.setTeamMember(existingUser);
					existingUser.setFirstName(teamMemberDTO.getFirstName());
					existingUser.setLastName(teamMemberDTO.getLastName());
					existingUser.setCompanyProfile(loggedInUser.getCompanyProfile());
					teamMember.setAddedThroughSignUpLink(teamMemberDTO.isAddedThroughSignUpLink());
					teamMember.setAddedThroughOAuthSSO(teamMemberDTO.isAddedThroughOAuthSSO());
					teamMember.setAddedThroughInvitation(teamMemberDTO.isAddedThroughInvitation());
					setTeamMemberOrUserStatusByCondition(existingUser, teamMember, teamMemberDTO.getPassword());
					setTeamMemberAndTeamMemberGroupUserMappingUtilData(loggedInUser, teamMemberDTO, existingUser,
							teamMember, teamMemberGroupId, roleIds, teamMemberUserList);
					teamMember.setTeamMemberUserList(teamMemberUserList);
					/** XNFR-821 **/
					setApprovalAuthoritiesForAdminsAndSupervisors(roleIds, teamMember);
					teamMembers.add(teamMember);
				} else {
					setNewUsersAndTeamMembersData(loggedInUser, newUsers, teamMemberDTO, roleIds, teamMemberGroupId,
							teamMemberUserList);
				}
			}
		}
		addNewUsersAndTeamMembers(newUsers, companyId);
		addTeamMembers(teamMembers, companyId);

	}

	private void setViewTypeForTeamMember(User loggedInUser, TeamMember teamMember) {
		Set<PartnerTeamMemberViewType> partnerTeamMemberViewTypes = new HashSet<>();
		Integer loggedInCompanyId = loggedInUser.getCompanyProfile().getId();
		boolean isOnlyPartner = utilDao.isOnlyPartnerCompany(loggedInUser.getUserId());
		if (!isOnlyPartner) {
			saveTeamMemberViewType(loggedInCompanyId, teamMember, null, partnerTeamMemberViewTypes);
		}
		List<Partnership> partnerships = partnershipDao
				.getApprovedPartnershipsByPartnerCompany(loggedInUser.getCompanyProfile());
		for (Partnership partnership : partnerships) {
			if (partnership.getVendorCompany() != null) {
				saveTeamMemberViewType(partnership.getVendorCompany().getId(), teamMember, partnership,
						partnerTeamMemberViewTypes);
			}
		}
		teamMember.setPartnerTeamMemberViewType(partnerTeamMemberViewTypes);
	}

	private void saveTeamMemberViewType(Integer companyId, TeamMember teamMember, Partnership partnership,
			Set<PartnerTeamMemberViewType> partnerTeamMemberViewTypes) {
		Integer primaryAdminId = teamDao.findPrimaryAdminIdByCompanyId(companyId);
		if (!XamplifyUtils.isValidInteger(primaryAdminId)) {
			return;
		}
		try {
			String modulesDisplayType = userDao.getAdminOrPartnerOrTeamMemberViewType(primaryAdminId, null, null);
			PartnerTeamMemberViewType partnerTeamMemberViewType = new PartnerTeamMemberViewType();
			User admin = new User();
			admin.setUserId(primaryAdminId);
			partnerTeamMemberViewType.setAdmin(admin);
			if (partnership != null) {
				Partnership pship = new Partnership();
				pship.setId(partnership.getId());
				partnerTeamMemberViewType.setPartnership(pship);
			}
			partnerTeamMemberViewType.setTeamMember(teamMember);
			partnerTeamMemberViewType.setVendorViewType(ModulesDisplayType.findByName(modulesDisplayType));
			partnerTeamMemberViewType.setCreatedTime(new Date());
			partnerTeamMemberViewType.setViewUpdated(false);
			partnerTeamMemberViewType.setUpdatedTime(new Date());
			partnerTeamMemberViewTypes.add(partnerTeamMemberViewType);
		} catch (Exception e) {
			logger.error("Error occurred while setting team-member view type data", e);
		}
	}

	private void setTeamMemberOrUserStatusByCondition(User existingUser, TeamMember teamMember, String password) {
		if (teamMember.isAddedThroughSignUpLink()) {
			teamMember.setTeamMemberStatus(TeamMemberStatus.APPROVE);
			existingUser.setUserStatus(UserStatus.APPROVED);
			existingUser.setPassword(password);
			existingUser.setSource(UserSource.SIGNUP);
			existingUser.setRegisteredTime(new Date());
			existingUser.setActivatedTime(new Date());
		} else if (teamMember.isAddedThroughOAuthSSO()) {
			teamMember.setTeamMemberStatus(TeamMemberStatus.APPROVE);
			if (existingUser.getUserStatus().equals(UserStatus.UNAPPROVED)) {
				existingUser.setUserStatus(UserStatus.APPROVED);
			}
			if (existingUser.getSource() == null) {
				existingUser.setSource(UserSource.OAUTHSSO);
			}
			existingUser.setRegisteredTime(new Date());
			existingUser.setActivatedTime(new Date());
		} else {
			if (existingUser.getUserStatus().equals(UserStatus.APPROVED)) {
				teamMember.setTeamMemberStatus(TeamMemberStatus.APPROVE);
			} else if (existingUser.getUserStatus().equals(UserStatus.UNAPPROVED)) {
				teamMember.setTeamMemberStatus(TeamMemberStatus.UNAPPROVED);
			} else if (existingUser.getUserStatus().equals(UserStatus.DECLINE)) {
				teamMember.setTeamMemberStatus(TeamMemberStatus.APPROVE);
				existingUser.setUserStatus(UserStatus.APPROVED);
			}
		}
	}

	private void addTeamMembers(List<TeamMember> teamMembers, Integer companyId) {
		if (!teamMembers.isEmpty()) {
			teamDao.saveAll(teamMembers, companyId);
		}
	}

	private void addNewUsersAndTeamMembers(List<User> newUsers, Integer companyId) {
		if (!newUsers.isEmpty()) {
			teamDao.saveUsersAndTeamMembers(newUsers, companyId);
		}
	}

	@Override
	public void setNewUsersAndTeamMembersData(User adminUser, List<User> newUsers, TeamMemberDTO teamMemberDTO,
			List<Integer> roleIds, Integer teamMemberGroupId, UserList teamMemberUserList) {
		User teamMemberUser = new User();
		if (teamMemberDTO.isAddedThroughSignUpLink()) {
			teamMemberUser.setUserStatus(UserStatus.APPROVED);
		} else {
			teamMemberUser.setUserStatus(UserStatus.UNAPPROVED);
		}
		TeamMember teamMember = new TeamMember();
		if (teamMemberDTO.isAddedThroughSignUpLink()) {
			teamMember.setTeamMemberStatus(TeamMemberStatus.APPROVE);
			teamMember.setAddedThroughSignUpLink(true);
		} else if (teamMemberDTO.isAddedThroughOAuthSSO()) {
			teamMember.setTeamMemberStatus(TeamMemberStatus.APPROVE);
			teamMember.setAddedThroughOAuthSSO(true);
		} else if (teamMemberDTO.isAddedThroughInvitation()) {
			teamMember.setTeamMemberStatus(TeamMemberStatus.UNAPPROVED);
			teamMember.setAddedThroughInvitation(true);
		} else {
			teamMember.setTeamMemberStatus(TeamMemberStatus.UNAPPROVED);
		}

		String emailId = teamMemberDTO.getEmailId().trim().toLowerCase();
		teamMemberUser.setEmailId(emailId);
		if (StringUtils.hasText(teamMemberDTO.getFirstName())) {
			teamMemberUser.setFirstName(teamMemberDTO.getFirstName().trim());
		}
		if (StringUtils.hasText(teamMemberDTO.getLastName())) {
			teamMemberUser.setLastName(teamMemberDTO.getLastName().trim());
		}
		teamMemberUser.initialiseCommonFields(true, 0);
		teamMemberUser.setCompanyProfile(adminUser.getCompanyProfile());
		List<TeamMember> newTeamMembers = new ArrayList<>();
		setTeamMemberAndTeamMemberGroupUserMappingUtilData(adminUser, teamMemberDTO, teamMemberUser, teamMember,
				teamMemberGroupId, roleIds, teamMemberUserList);
		teamMember.setTeamMemberUserList(teamMemberUserList);
		/** XNFR-821 **/
		setApprovalAuthoritiesForAdminsAndSupervisors(roleIds, teamMember);
		newTeamMembers.add(teamMember);
		teamMemberUser.setTeamMembers(newTeamMembers);
		GenerateRandomPassword password = new GenerateRandomPassword();
		teamMemberUser.setAlias(password.getPassword());
		teamMemberUser.setUserName(emailId);
		teamMemberUser.setUserDefaultPage(UserDefaultPage.WELCOME);
		/*** XNFR-454 ***/
		if (teamMemberDTO.isAddedThroughSignUpLink() || teamMemberDTO.isAddedThroughOAuthSSO()
				|| teamMemberDTO.isAddedThroughInvitation()) {
			if (teamMemberDTO.isAddedThroughSignUpLink()) {
				teamMemberUser.setPassword(teamMemberDTO.getPassword());
				teamMemberUser.setSource(UserSource.SIGNUP);
			} else if (teamMemberDTO.isAddedThroughOAuthSSO()) {
				teamMemberUser.setSource(UserSource.OAUTHSSO);
			} else if (teamMemberDTO.isAddedThroughInvitation()) {
				teamMemberUser.setSource(UserSource.INVITATION);
			}
			teamMemberUser.setRegisteredTime(new Date());
			teamMemberUser.setActivatedTime(new Date());
			teamMemberUser.setUserName(emailId);
			if (!xamplifyUtil.isDev()) {
				try {
					validateEmailAddress(teamMemberUser);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		/*** XNFR-454 ***/
		newUsers.add(teamMemberUser);
	}

	/**** XNFR-454 ****/
	private void validateEmailAddress(User teamMemberUser) throws IOException {
		JSONObject jsonObject = emailValidatorService.validate(teamMemberUser.getEmailId().trim().toLowerCase(), null,
				"", 1, 1);
		if (jsonObject.has(ERROR)
				&& jsonObject.getString(ERROR).equalsIgnoreCase("Invalid API Key or your account ran out of credits")) {
			teamMemberUser.setEmailValid(false);
			teamMemberUser.setEmailValidationInd(false);
		} else if (jsonObject.has(ERROR)) {
			teamMemberUser.setEmailValid(false);
			teamMemberUser.setEmailValidationInd(true);
			teamMemberUser.setEmailCategory("invalid");
		} else {
			String status = jsonObject.getString("status");
			teamMemberUser.setEmailValid((status.equalsIgnoreCase("valid") || status.equalsIgnoreCase("catch-all")));
			teamMemberUser.setEmailValidationInd(true);
			teamMemberUser.setEmailCategory(status);
		}
	}

	private void addRoles(List<Integer> roleIds, User user, boolean secondAdmin, Integer userId) {
		user.getRoles().add(Role.USER_ROLE);
		if (secondAdmin) {
			Integer roleId = getAdminRoleByUserId(userId);
			if (Role.PRM_ROLE.getRoleId().equals(roleId)) {
				user.getRoles().add(Role.PRM_ROLE);
			} else if (Role.COMPANY_PARTNER.getRoleId().equals(roleId)) {
				user.getRoles().add(Role.COMPANY_PARTNER);
			}
		}
		for (Integer roleId : roleIds) {
			addRolesByRoleId(user, roleId);
		}
	}

	private void addRolesByRoleId(User user, Integer roleId) {
		if (Role.ALL_ROLES.getRoleId().equals(roleId)) {
			user.getRoles().add(Role.ALL_ROLES);
		} else if (Role.PARTNERS.getRoleId().equals(roleId)) {
			user.getRoles().add(Role.PARTNERS);
		} else if (Role.VIDEO_UPLOAD_ROLE.getRoleId().equals(roleId)) {
			user.getRoles().add(Role.VIDEO_UPLOAD_ROLE);
		} else if (Role.SHARE_LEADS.getRoleId().equals(roleId)) {
			user.getRoles().add(Role.SHARE_LEADS);
		} else if (Role.OPPORTUNITY.getRoleId().equals(roleId)) {
			user.getRoles().add(Role.OPPORTUNITY);
		} else if (Role.STATS_ROLE.getRoleId().equals(roleId)) {
			user.getRoles().add(Role.STATS_ROLE);
		} else if (Role.MDF.getRoleId().equals(roleId)) {
			user.getRoles().add(Role.MDF);
		} else if (Role.DAM.getRoleId().equals(roleId)) {
			user.getRoles().add(Role.DAM);
		} else if (Role.LEARNING_TRACK.getRoleId().equals(roleId)) {
			user.getRoles().add(Role.LEARNING_TRACK);
		} else if (Role.PLAY_BOOK.getRoleId().equals(roleId)) {
			user.getRoles().add(Role.PLAY_BOOK);
		}
	}

	private void setTeamMemberAndTeamMemberGroupUserMappingUtilData(User loggedInUser, TeamMemberDTO teamMemberDTO,
			User teamMemberUser, TeamMember teamMember, Integer teamMemberGroupId, List<Integer> roleIds,
			UserList teamMemberUserList) {
		addRoles(roleIds, teamMemberUser, teamMemberDTO.isSecondAdmin(), loggedInUser.getUserId());
		teamMember.setTeamMember(teamMemberUser);
		Integer companyId = loggedInUser.getCompanyProfile().getId();
		Integer primaryAdminId = teamDao.findPrimaryAdminIdByCompanyId(companyId);
		User primaryAdmin = new User();
		primaryAdmin.setUserId(primaryAdminId);
		teamMember.setOrgAdmin(primaryAdmin);
		teamMember.setSecondAdmin(teamMemberDTO.isSecondAdmin());
		teamMember.setCreatedTime(new Date());
		teamMember.setCompanyId(companyId);
		setTeamMemberGroupAndGroupUserMappingData(teamMember, teamMemberGroupId, loggedInUser, teamMemberUserList,
				teamMemberDTO);
		setViewTypeForTeamMember(loggedInUser, teamMember);
	}

	private void setTeamMemberGroupAndGroupUserMappingData(TeamMember teamMember, Integer teamMemberGroupId,
			User loggedInUser, UserList teamMemberUserList, TeamMemberDTO teamMemberDTO) {
		Integer createdUserId = loggedInUser.getUserId();
		TeamMemberGroup teamMemberGroup = new TeamMemberGroup();
		teamMemberGroup.setId(teamMemberGroupId);
		TeamMemberGroupUserMapping teamMemberGroupUserMapping = new TeamMemberGroupUserMapping();
		teamMemberGroupUserMapping.setCreatedTime(new Date());
		teamMemberGroupUserMapping.setCreatedUserId(createdUserId);
		teamMemberGroupUserMapping.setUpdatedTime(new Date());
		teamMemberGroupUserMapping.setUpdatedUserId(createdUserId);
		teamMemberGroupUserMapping.setTeamMember(teamMember);
		teamMemberGroupUserMapping.setTeamMemberGroup(teamMemberGroup);
		teamMember.setTeamMemberGroupUserMapping(teamMemberGroupUserMapping);
		/********* XNFR-107 **********/
		addPartners(teamMemberGroupId, teamMemberGroupUserMapping, teamMember, loggedInUser, teamMemberUserList,
				teamMemberDTO);

	}

	/********* XNFR-107 **********/
	private void addPartners(Integer teamMemberGroupId, TeamMemberGroupUserMapping teamMemberGroupUserMapping,
			TeamMember teamMember, User loggedInUser, UserList teamMemberUserList, TeamMemberDTO teamMemberDTO) {
		List<UserUserList> teamMemberPartners = new ArrayList<>();
		findPartnershipIdsAndAddToPartnerTeamGroupMapping(teamMemberGroupId, teamMemberGroupUserMapping,
				teamMemberUserList, teamMemberPartners, teamMemberDTO);
		teamMemberUserList.setTeamMemberPartners(teamMemberPartners);
		XamplifyUtils.setTeamMemberUserList(loggedInUser.getUserId(), loggedInUser, teamMember, teamMemberUserList);

	}

	/********* XNFR-107 **********/
	private void findPartnershipIdsAndAddToPartnerTeamGroupMapping(Integer teamMemberGroupId,
			TeamMemberGroupUserMapping teamMemberGroupUserMapping, UserList teamMemberUserList,
			List<UserUserList> teamMemberPartners, TeamMemberDTO teamMemberDTO) {
		List<Integer> partnershipIds = teamMemberGroupDao
				.findAssociatedPartnershipIdsByTeamMemberGroupId(teamMemberGroupId);
		Set<PartnerTeamGroupMapping> partnerTeamGroupMappings = new HashSet<>();
		if (!teamMemberDTO.isNewTeamMember()
				|| (teamMemberDTO.isNewTeamMember() && teamMemberDTO.isNewAndSinglePartner())) {
			if (XamplifyUtils.isNotEmptyList(teamMemberDTO.getSelectedPartnershipIds())) {
				partnershipIds = partnershipIds.stream()
						.filter(id -> teamMemberDTO.getSelectedPartnershipIds().contains(id))
						.collect(Collectors.toList());
			} else {
				partnershipIds = new ArrayList<>();
			}
		}

		if (partnershipIds != null && !partnershipIds.isEmpty()) {
			logger.debug("***Partners Available For Selected Group***********");
			for (Integer partnershipId : partnershipIds) {
				PartnerTeamGroupMapping partnerTeamGroupMapping = new PartnerTeamGroupMapping();
				partnerTeamGroupMapping.setTeamMemberGroupUserMapping(teamMemberGroupUserMapping);
				Partnership partnership = new Partnership();
				partnership.setId(partnershipId);
				partnerTeamGroupMapping.setPartnership(partnership);
				partnerTeamGroupMapping.setCreatedTime(new Date());
				partnerTeamGroupMapping.setUpdatedTime(new Date());
				partnerTeamGroupMappings.add(partnerTeamGroupMapping);
				setTeamMemberPartnersInfo(teamMemberUserList, teamMemberPartners, partnershipId);
			}
			teamMemberGroupUserMapping.setPartnerTeamGroupMappings(partnerTeamGroupMappings);
		}

	}

	/********* XNFR-107 **********/
	private void setTeamMemberPartnersInfo(UserList teamMemberUserList, List<UserUserList> teamMemberPartners,
			Integer partnershipId) {
		UserDTO partnerDTO = userListDao.findPartnerDetailsByPartnershipId(partnershipId);
		UserUserList teamMemberPartner = new UserUserList();
		User partner = new User();
		partner.setUserId(partnerDTO.getId());
		teamMemberPartner.setUser(partner);
		teamMemberPartner.setUserList(teamMemberUserList);
		teamMemberPartner.setCountry(partnerDTO.getCountry());
		teamMemberPartner.setCity(partnerDTO.getCity());
		teamMemberPartner.setAddress(partnerDTO.getAddress());
		teamMemberPartner.setContactCompany(partnerDTO.getContactCompany());
		teamMemberPartner.setJobTitle(partnerDTO.getJobTitle());
		teamMemberPartner.setFirstName(partnerDTO.getFirstName());
		teamMemberPartner.setLastName(partnerDTO.getLastName());
		teamMemberPartner.setMobileNumber(partnerDTO.getMobileNumber());
		teamMemberPartner.setState(partnerDTO.getState());
		teamMemberPartner.setZipCode(partnerDTO.getZipCode());
		teamMemberPartner.setVertical(partnerDTO.getVertical());
		teamMemberPartner.setRegion(partnerDTO.getRegion());
		teamMemberPartner.setPartnerType(partnerDTO.getPartnerType());
		teamMemberPartner.setCategory(partnerDTO.getCategory());
		List<LegalBasis> legalBasisList = new ArrayList<>();
		for (Integer legalBasisId : partnerDTO.getLegalBasis()) {
			LegalBasis legalBasis = new LegalBasis();
			legalBasis.setId(legalBasisId);
			legalBasisList.add(legalBasis);
		}
		teamMemberPartner.setLegalBasis(legalBasisList);
		teamMemberPartners.add(teamMemberPartner);
	}

	private void validateEmailIds(XtremandResponse response, List<String> teamMemberEmailIds) {
		List<String> allEmailIds = new ArrayList<>();
		allEmailIds.addAll(userService.listAllOrgAdminEmailIds());
		allEmailIds.addAll(userService.listAllPartnerEmailIds());
		allEmailIds.addAll(teamDao.listTeamMemberEmailIds());
		List<String> duplicateEmailIds = new ArrayList<>(allEmailIds);
		duplicateEmailIds.retainAll(teamMemberEmailIds);
		if (!duplicateEmailIds.isEmpty()) {
			response.setStatusCode(413);
			Set<String> distinctDuplicateEmailIds = XamplifyUtils.convertListToSet(duplicateEmailIds);
			response.setData(distinctDuplicateEmailIds);
			if (distinctDuplicateEmailIds.size() == 1) {
				response.setMessage(teamMemberErrorMessage);
			} else {
				response.setMessage(teamMembersErrorMessage);
			}
		}
	}

	private void validateTeamMemberGroups(XtremandResponse response, List<TeamMemberDTO> teamMemberDTOs) {
		List<String> emailIds = teamMemberDTOs.stream().filter(u -> u.getTeamMemberGroupId().equals(0))
				.map(TeamMemberDTO::getEmailId).collect(Collectors.toList());
		if (emailIds != null && !emailIds.isEmpty()) {
			response.setStatusCode(401);
			response.setMessage("Please assign groups for below emailIds");
			response.setData(emailIds);
		}
	}

	@Override
	public XtremandResponse findAll(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		if (pagination.isPartnerJourneyFilter()) {
			Integer adminId = utilDao.findAdminIdByCompanyId(pagination.getPartnerCompanyId());
			pagination.setUserId(adminId);
			utilService.setDateFilters(pagination);
		}
		response.setData(teamDao.findAll(pagination));
		return response;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse update(TeamMemberDTO teamMemberDTO, XtremandResponse response) {
		try {
			Integer teamMemberId = teamMemberDTO.getId();
			if (teamMemberId != null && teamMemberId > 0) {
				Integer teamMemberGroupId = teamMemberDTO.getTeamMemberGroupId();
				TeamMember teamMember = genericDao.get(TeamMember.class, teamMemberId);
				TeamMemberGroupUserMapping teamMemberGroupUserMapping = teamMember.getTeamMemberGroupUserMapping();
				TeamMemberGroup teamMemberGroup = new TeamMemberGroup();
				if (teamMemberGroupUserMapping != null) {
					teamMemberGroup = teamMemberGroupUserMapping.getTeamMemberGroup();
				} else {
					teamMemberGroup.setId(0);
				}
				Set<Integer> roleIdsByGroupId = new HashSet<>();
				User teamMemberUser = teamMember.getTeamMember();
				boolean secondAdmin = teamMemberDTO.isSecondAdmin();
				boolean secondAdminUpdated = teamMember.isSecondAdmin() != secondAdmin;
				boolean declineStatus = (!teamMember.getTeamMemberStatus().equals(teamMemberDTO.getStatus()))
						&& teamMemberDTO.getStatus().equals(TeamMemberStatus.DECLINE);
				/************ XNFR-85 *********/

				if (!teamMemberGroup.getId().equals(teamMemberGroupId)) {
					teamMemberDTO.setNewPartnerGroupRequired(true);
				}
				boolean isTeamMemberGroupAssignedToPartner = teamMemberGroupDao
						.isTeamMemberGroupAssignedToPartnerByTeamMemberGroupId(teamMemberGroup.getId(), teamMemberId);
				List<Integer> partnerTeamGroupMappingIds = new ArrayList<>();
				if (isTeamMemberGroupAssignedToPartner && !teamMemberGroup.getId().equals(teamMemberGroupId)) {
					partnerTeamGroupMappingIds = teamMemberGroupDao.getPartnerTeamGroupMapping(teamMemberGroup.getId(),
							teamMemberId);
					deletePartnerTeamGroupMapping(partnerTeamGroupMappingIds);
				}
				if (secondAdmin) {
					validateSecondAdminAndSave(teamMemberDTO, response, teamMemberId, teamMemberGroupId, teamMember,
							teamMemberGroup, roleIdsByGroupId, teamMemberUser);
				} else {
					updateTeamMemberRolesAndStatus(teamMemberDTO, response, teamMemberGroupId, roleIdsByGroupId,
							teamMember, teamMemberUser, teamMemberGroup);
				}
				if (XamplifyUtils.isNotEmptyList(teamMemberDTO.getDeletedPartnershipIds())) {
					List<Integer> removedPartnerTeamGroupMappingIds = teamDao
							.findPartnerTeamGroupMappingIdsByPartnershipIds(teamMemberDTO.getDeletedPartnershipIds(),
									teamMemberId);
					if (XamplifyUtils.isNotEmptyList(removedPartnerTeamGroupMappingIds)) {
						DeleteTeamMemberPartnersRequestDTO deleteTeamMemberPartnersRequestDTO = new DeleteTeamMemberPartnersRequestDTO();
						deleteTeamMemberPartnersRequestDTO.setLoggedInUserId(teamMemberDTO.getUserId());
						deleteTeamMemberPartnersRequestDTO
								.setPartnerTeamGroupMappingIds(removedPartnerTeamGroupMappingIds);
						deleteTeamMemberPartners(deleteTeamMemberPartnersRequestDTO);
					}
				}

				boolean groupUpdated = !teamMemberGroup.getId().equals(teamMemberGroupId);
				if (declineStatus || groupUpdated || secondAdminUpdated) {
					utilService.revokeAccessTokenByUserId(teamMemberUser.getUserId());
				}
				Map<String, Object> map = new HashMap<>();
				map.put("partnerTeamGroupMappingIds", partnerTeamGroupMappingIds);
				response.setMap(map);
				return response;
			} else {
				throw new TeamMemberDataAccessException("Id is null to update team member");
			}

		} catch (DuplicateEntryException e) {
			throw new DuplicateEntryException(e.getMessage());
		} catch (BadRequestException e) {
			throw new BadRequestException(e.getMessage());
		} catch (HibernateException | TeamMemberDataAccessException e) {
			throw new TeamMemberDataAccessException(e.getMessage());
		} catch (Exception ex) {
			throw new TeamMemberDataAccessException(ex.getMessage());
		}
	}

	private void validateSecondAdminAndSave(TeamMemberDTO teamMemberDTO, XtremandResponse response,
			Integer teamMemberId, Integer teamMemberGroupId, TeamMember teamMember, TeamMemberGroup teamMemberGroup,
			Set<Integer> roleIdsByGroupId, User teamMemberUser) {
		/**** XNFR-139 ******/
		Integer companyId = userDao.getCompanyIdByUserId(teamMemberDTO.getUserId());
		Integer maxAdminsLimit = utilDao.findMaxAdminsCountByCompanyId(companyId);
		Integer assignedSecondAdminsCount = teamDao.getSecondAdminsCountByCompanyId(companyId) + 1;
		Integer availableAdminsLimit = maxAdminsLimit - assignedSecondAdminsCount;
		List<Integer> secondAdminTeamMemberIds = teamDao.findSecondAdminTeamMemberIds(companyId);
		if (availableAdminsLimit.equals(0) && secondAdminTeamMemberIds.indexOf(teamMemberId) < 0) {
			setErrorMessageForMaximumAdminsLimitReached(response, maxAdminsLimit, availableAdminsLimit);
		} else {
			validateSecondAdminOption(teamMemberDTO, response, teamMemberGroupId, roleIdsByGroupId, teamMember,
					teamMemberUser, teamMemberGroup);
		}
	}

	private void validateSecondAdminOption(TeamMemberDTO teamMemberDTO, XtremandResponse response,
			Integer teamMemberGroupId, Set<Integer> roleIdsByGroupId, TeamMember teamMember, User teamMemberUser,
			TeamMemberGroup teamMemberGroup) {
		List<Integer> roleIds = teamMemberGroupDao.findRoleIdsByTeamMemberGroupId(teamMemberGroupId);
		if (roleIds.indexOf(Role.ALL_ROLES.getRoleId()) > -1) {
			updateTeamMemberRolesAndStatus(teamMemberDTO, response, teamMemberGroupId, roleIdsByGroupId, teamMember,
					teamMemberUser, teamMemberGroup);
		} else {
			response.setStatusCode(StatusCodeConstants.INVALID_TEAM_MEMBER_GROUP);
			response.setMessage("Enable As An Admin option is not available for the selected team member group");
		}
	}

	@SuppressWarnings("unchecked")
	private void updateTeamMemberRolesAndStatus(TeamMemberDTO teamMemberDTO, XtremandResponse response,
			Integer teamMemberGroupId, Set<Integer> roleIdsByGroupId, TeamMember teamMember, User teamMemberUser,
			TeamMemberGroup teamMemberGroup) {
		Integer teamMemberUserId = teamMemberUser.getUserId();
		Integer userId = teamMemberDTO.getUserId();
		if (teamMemberGroup.getId().equals(teamMemberGroupId)) {
			addAdminRole(teamMemberDTO, roleIdsByGroupId);
			roleIdsByGroupId.addAll(teamMemberGroup.getTeamMemberGroupRoleMappings().stream()
					.map(TeamMemberGroupRoleMapping::getRoleId).collect(Collectors.toSet()));
		} else {
			addAdminRole(teamMemberDTO, roleIdsByGroupId);
			roleIdsByGroupId.addAll(teamMemberGroupDao.findRoleIdsByTeamMemberGroupId(teamMemberGroupId));
		}
		updateRoles(roleIdsByGroupId, teamMember, teamMemberUserId, teamMemberDTO.isSecondAdmin());
		updateTeamMemberDetailsAndUserStatus(teamMemberDTO, teamMember, teamMemberUser);
		updateTeamMemberGroupUserMapping(teamMemberGroupId, userId, teamMember);
		/** XNFR-821 **/
		updateApprovalAuthorities(teamMemberGroupId, teamMember, teamMemberDTO);
		/*********** XNFR-107 ****************/
		saveOrUpdateTeamMemerPartnerList(teamMemberGroupId, teamMember, teamMemberUser, userId, teamMemberDTO);
		/** XNFR-821 **/
		Map<String, Object> approvalsMap = autoApprovePendingContentAndReturnHistoryList(teamMemberDTO.getUserId(),
				teamMember.getCompanyId(), teamMember.getTeamMember().getUserId(), teamMemberDTO.isApprovalManager(),
				false);
		if (approvalsMap.containsKey(WHITE_LABELED_RE_APPROVAL_DAM_IDS)) {
			teamMemberDTO.setWhiteLabeledReApprovalDamIds(
					(List<Integer>) approvalsMap.get(WHITE_LABELED_RE_APPROVAL_DAM_IDS));
			Integer companyId = userDao.getCompanyIdByUserId(userId);
			teamMemberDTO.setCompanyId(companyId);
		}

		performApprovalInsertionOperations(approvalsMap);

		response.setStatusCode(200);
		response.setMessage("Team member details updated successfully.");
	}

	private void saveOrUpdateTeamMemerPartnerList(Integer teamMemberGroupId, TeamMember teamMember, User teamMemberUser,
			Integer userId, TeamMemberDTO teamMemberDTO) {
		TeamMemberGroupUserMapping teamMemberGroupUserMapping = teamMember.getTeamMemberGroupUserMapping();
		if (teamMemberGroupUserMapping != null && (teamMemberGroupUserMapping.getPartnerTeamGroupMappings().isEmpty()
				|| teamMemberDTO.isNewPartnersAdded())) {
			Integer userListId = userListDao.findUserListIdByTeamMemberId(teamMember.getId());
			User loggedInUser = userDao.findByPrimaryKey(userId, new FindLevel[] { FindLevel.COMPANY_PROFILE });
			if (userListId != null && !teamMemberDTO.isNewPartnerGroupRequired()) {
				logger.debug("*****Master Partner Group Already Exists*********");
				UserList teamMemberUserList = genericDao.get(UserList.class, userListId);
				List<UserUserList> teamMemberPartners = new ArrayList<>();
				findPartnershipIdsAndAddToPartnerTeamGroupMapping(teamMemberGroupId, teamMemberGroupUserMapping,
						teamMemberUserList, teamMemberPartners, teamMemberDTO);
				if (!teamMemberPartners.isEmpty()) {
					logger.debug("*****Partners Availble For This Group*********");
					teamDao.saveTeamMemberUserUserListsAndUpdateTeamMemberPartnerList(teamMemberPartners,
							teamMemberUserList, teamMemberUser, teamMember);
				}
			} else {
				if (teamMemberDTO.isNewPartnerGroupRequired() && XamplifyUtils.isValidInteger(userListId)) {
					userListDao.updatePreviousUsersListByUserListId(userListId);
				}
				logger.debug("*****Master Partner Group Does Not Exists*********");
				createTeamMemberPartnerListOnUpdatingTeamMember(teamMemberGroupId, teamMember, teamMemberUser,
						teamMemberGroupUserMapping, loggedInUser, teamMemberDTO);
			}
		}
	}

	private void createTeamMemberPartnerListOnUpdatingTeamMember(Integer teamMemberGroupId, TeamMember teamMember,
			User teamMemberUser, TeamMemberGroupUserMapping teamMemberGroupUserMapping, User loggedInUser,
			TeamMemberDTO teamMemberDTO) {
		UserList teamMemberUserList = new UserList();
		addPartners(teamMemberGroupId, teamMemberGroupUserMapping, teamMember, loggedInUser, teamMemberUserList,
				teamMemberDTO);
		teamMember.setTeamMemberUserList(teamMemberUserList);
		teamDao.saveTeamMemberPartnerList(teamMember, teamMemberUser, teamMemberDTO.isNewPartnerGroupRequired());
	}

	private void addAdminRole(TeamMemberDTO teamMemberDTO, Set<Integer> roleIdsByGroupId) {
		if (teamMemberDTO.isSecondAdmin()) {
			Integer userId = teamMemberDTO.getUserId();
			Integer roleId = getAdminRoleByUserId(userId);
			roleIdsByGroupId.add(roleId);
		}
	}

	/***** XNFR-139 *****/
	private Integer getAdminRoleByUserId(Integer userId) {
		Integer roleId = 0;
		if (utilDao.isPrmCompany(userId)) {
			roleId = Role.PRM_ROLE.getRoleId();
		} else if (utilDao.isOnlyPartnerCompany(userId)) {
			roleId = Role.COMPANY_PARTNER.getRoleId();
		}
		return roleId;
	}

	private void updateRoles(Set<Integer> roleIdsByGroupId, TeamMember teamMember, Integer teamMemberUserId,
			boolean isSecondAdmin) {
		Set<Integer> userRoles = findUserRoles(teamMember);
		Set<Integer> existingRoleIds = new HashSet<>();
		existingRoleIds.addAll(userRoles);
		if (!userRoles.equals(roleIdsByGroupId)) {
			existingRoleIds.removeAll(roleIdsByGroupId);
			List<Integer> teamMemberUserIds = new ArrayList<>();
			teamMemberUserIds.add(teamMemberUserId);
			teamDao.deleteUnMappedRoleIds(teamMemberUserIds, existingRoleIds);
			addNewRoles(teamMemberUserId, teamMember, roleIdsByGroupId, isSecondAdmin);
		}
	}

	private void addNewRoles(Integer teamMemberId, TeamMember teamMember, Set<Integer> roleIdsToUpdate,
			boolean isSecondAdmin) {
		Set<Integer> existingRoleIds = findUserRoles(teamMember);
		if (isSecondAdmin) {
			Integer roleId = getAdminRoleByUserId(teamMemberId);
			roleIdsToUpdate.add(roleId);
		}
		roleIdsToUpdate.removeAll(existingRoleIds);
		teamDao.addNewRoles(teamMemberId, roleIdsToUpdate);
	}

	private Set<Integer> findUserRoles(TeamMember teamMember) {
		return teamMember.getTeamMember().getRoles().stream()
				.filter(x -> !x.getRoleId().equals(Role.USER_ROLE.getRoleId())).map(Role::getRoleId)
				.collect(Collectors.toSet());
	}

	private void updateTeamMemberGroupUserMapping(Integer teamMemberGroupId, Integer userId, TeamMember teamMember) {
		if (teamMember.getTeamMemberGroupUserMapping() != null) {
			TeamMemberGroupUserMapping teamMemberGroupUserMapping = teamMember.getTeamMemberGroupUserMapping();
			teamMemberGroupUserMapping.setUpdatedTime(new Date());
			teamMemberGroupUserMapping.setUpdatedUserId(userId);
			TeamMemberGroup teamMemberGroup = new TeamMemberGroup();
			teamMemberGroup.setId(teamMemberGroupId);
			teamMemberGroupUserMapping.setTeamMemberGroup(teamMemberGroup);
		} else {
			TeamMemberGroupUserMapping teamMemberGroupUserMapping = new TeamMemberGroupUserMapping();
			TeamMemberGroup teamMemberGroup = new TeamMemberGroup();
			teamMemberGroup.setId(teamMemberGroupId);
			teamMemberGroupUserMapping.setTeamMemberGroup(teamMemberGroup);
			teamMemberGroupUserMapping.setTeamMember(teamMember);
			teamMemberGroupUserMapping.setCreatedTime(new Date());
			teamMemberGroupUserMapping.setCreatedUserId(userId);
			teamMemberGroupUserMapping.setUpdatedTime(new Date());
			teamMemberGroupUserMapping.setUpdatedUserId(userId);
			genericDao.save(teamMemberGroupUserMapping);
		}

	}

	private void updateTeamMemberDetailsAndUserStatus(TeamMemberDTO teamMemberDTO, TeamMember teamMember,
			User teamMemberUser) {
		teamMember.setTeamMemberStatus(teamMemberDTO.getStatus());
		teamMember.setSecondAdmin(teamMemberDTO.isSecondAdmin());
		teamMember.setUpdatedTime(new Date());
		teamMemberUser.setFirstName(teamMemberDTO.getFirstName());
		teamMemberUser.setLastName(teamMemberDTO.getLastName());
		/***** XNFR-98 ******/
		updateTeamMemberPartnerListName(teamMemberDTO, teamMember);
		if (!teamMemberUser.getUserStatus().name().equals(UserStatus.UNAPPROVED.name())) {
			if (teamMember.getTeamMemberStatus().name().equals(TeamMemberStatus.APPROVE.name())) {
				teamMemberUser.setUserStatus(UserStatus.APPROVED);
			} else {
				teamMemberUser.setUserStatus(UserStatus.DECLINE);
			}
		}

	}

	private void updateTeamMemberPartnerListName(TeamMemberDTO teamMemberDTO, TeamMember teamMember) {
		String fullName = teamMemberDTO.getFirstName() + " " + teamMemberDTO.getLastName();
		String teamMemberPartnerListNamePrefix = StringUtils.hasText(fullName) ? fullName
				: teamMember.getTeamMember().getEmailId().split("@")[0];
		userListDao.updateTeamMemberPartnerListName(teamMember.getId(), teamMemberPartnerListNamePrefix);
	}

	@Override
	public List<UserDTO> findUsersToTransferData(Integer userId) {
		List<Integer> roleIds = userDao.getRoleIdsByUserId(userId);
		boolean isPartner = roleIds.size() == 2 && roleIds.indexOf(Role.COMPANY_PARTNER.getRoleId()) > -1
				&& roleIds.indexOf(Role.USER_ROLE.getRoleId()) > -1;
		if (isPartner) {
			return userService.listPartnerAndHisTeamMembers(userId);
		} else {
			return userService.findAllAdminsAndSupervisors(userId);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse delete(TeamMemberDTO teamMemberDTO) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setAccess(utilService.hasTeamMemberAccess(teamMemberDTO.getUserId()));
			if (response.isAccess()) {
				Integer teamMemberId = teamDao.getPrimaryKeyId(teamMemberDTO.getId());
				Integer userListId = userListDao.findUserListIdByTeamMemberId(teamMemberId);
				teamDao.delete(teamMemberDTO);
				/******* Remove Team Member Master Partner Group ************/
				deleteTeamMemberMasterPartnerList(userListId, teamMemberDTO.getUserId());
				/******** Remove the accesstoken for this user *************/
				response.setAccess(true);
				utilService.revokeAccessTokenByUserId(teamMemberDTO.getId());

				/** XNFR-885 **/
				Integer companyId = userDao.getCompanyIdByUserId(teamMemberDTO.getUserId());
				Map<String, Object> approvalsMap = autoApprovePendingContentAndReturnHistoryList(
						teamMemberDTO.getUserId(), companyId, teamMemberDTO.getOrgAdminId(), true, true);

				if (approvalsMap.containsKey(WHITE_LABELED_RE_APPROVAL_DAM_IDS)) {
					teamMemberDTO.setWhiteLabeledReApprovalDamIds(
							(List<Integer>) approvalsMap.get(WHITE_LABELED_RE_APPROVAL_DAM_IDS));
					teamMemberDTO.setCompanyId(companyId);
				}

				performApprovalInsertionOperations(approvalsMap);
			}
			return response;
		} catch (TeamMemberDataAccessException e) {
			throw new TeamMemberDataAccessException(e);
		}
	}

	private void deleteTeamMemberMasterPartnerList(Integer userListId, Integer loggedInUserId) {
		if (userListId != null) {
			Criteria criteria = new Criteria("id", OPERATION_NAME.eq, userListId);
			List<Criteria> criterias = Arrays.asList(criteria);
			Collection<UserList> userLists = userListDao.find(criterias,
					new FindLevel[] { FindLevel.CAMPAIGNS, FindLevel.COMPANY_PROFILE });
			UserList userList = userLists.iterator().next();
			userList.setDeleteTeamMemberPartnerList(true);
			partnershipService.deletePartnerList(userList);
			damDao.deleteFromDamPartnerGroupMappingAndDamPartnerByUserListId(userListId, loggedInUserId);
			/********** Swathi Code Comes Here ********/
		}
	}

	@SuppressWarnings("unchecked")
	public void sendEmailsToTeamMembers(Map<String, Object> map) {
		List<String> teamMemberEmailIds = (List<String>) map.get(TEAM_MEMBER_EMAIL_IDS);
		TeamMemberDTO teamMemberDTO = (TeamMemberDTO) map.get(TEAM_MEMBER_DTO);
		User loggedInUser = userDao.findByPrimaryKey(teamMemberDTO.getUserId(),
				new FindLevel[] { FindLevel.ROLES, FindLevel.COMPANY_PROFILE });
		/*********** Sending Emails To All TeamMembers **********/
		List<User> updatedTeamMembers = new ArrayList<>();
		for (String emailId : teamMemberEmailIds) {
			User teamMember = userService.loadUser(Arrays.asList(new Criteria("emailId", OPERATION_NAME.eq, emailId)),
					new FindLevel[] { FindLevel.SHALLOW });
			if (teamMember != null) {
				Integer primayKeyId = teamDao.getPrimaryKeyId(teamMember.getUserId());
				teamMember.setTeamMemerPkId(primayKeyId);
				updatedTeamMembers.add(teamMember);
			}
		}

		if (teamMemberDTO.isVanityUrlFilter()) {
			loggedInUser.setCompanyProfileName(teamMemberDTO.getVendorCompanyProfileName());
			/****** XNFR-805 *****/
		}

		mailService.sendTeamMemberEmails(updatedTeamMembers, loggedInUser, false);
	}

	@Override
	public XtremandResponse resendTeamMemberInvitation(TeamMemberDTO teamMemberInputDTO) {
		XtremandResponse response = new XtremandResponse();
		User teamMember = userService.loadUser(
				Arrays.asList(new Criteria("emailId", OPERATION_NAME.eq, teamMemberInputDTO.getEmailId())),
				new FindLevel[] { FindLevel.SHALLOW });
		if (teamMember != null && UserStatus.UNAPPROVED.equals(teamMember.getUserStatus())) {
			VanityUrlDetailsDTO vanityUrlDetailsDTO = utilService.getVanityUrlFilteredData(
					teamMemberInputDTO.getUserId(), teamMemberInputDTO.isVanityUrlFilter(),
					teamMemberInputDTO.getVendorCompanyProfileName());
			User loggedInUser = userDao.findByPrimaryKey(teamMemberInputDTO.getUserId(),
					new FindLevel[] { FindLevel.ROLES, FindLevel.COMPANY_PROFILE });
			if (vanityUrlDetailsDTO.isVanityUrlFilter()) {
				loggedInUser.setCompanyProfileName(vanityUrlDetailsDTO.getVendorCompanyProfileName());
			}
			Integer teamMemberPrimayKeyId = teamDao.getPrimaryKeyId(teamMember.getUserId());
			teamMember.setTeamMemerPkId(teamMemberPrimayKeyId);
			List<User> teamMembers = new ArrayList<>();
			teamMembers.add(teamMember);
			mailService.sendTeamMemberEmails(teamMembers, loggedInUser, true);
			response.setStatusCode(200);
		} else {
			response.setStatusCode(400);
			response.setMessage("Email cannot be sent as this team member account is already created");
		}
		return response;

	}

	@Override
	public XtremandResponse getVanityUrlRoles(TeamMemberDTO teamMemberInputDTO) {
		XtremandResponse response = new XtremandResponse();
		String emailId = teamMemberInputDTO.getEmailId();
		Set<Role> updatedRoles = new HashSet<>();
		boolean isWelcomePageEnabled = false;
		if (StringUtils.hasText(emailId)) {
			UserDTO userDto = userService.getUserDTO(teamMemberInputDTO.getEmailId(), false);
			boolean isUserLoggedInThroughVanityUrl = teamMemberInputDTO.isVanityUrlFilter();
			String vanityUrlDomainName = teamMemberInputDTO.getVendorCompanyProfileName();
			VanityUrlDetailsDTO vanityUrlDetailsDTO = utilService.getVanityUrlFilteredData(userDto.getId(),
					isUserLoggedInThroughVanityUrl, vanityUrlDomainName);
			if (vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl()) {
				Set<Role> roles = userDto.getRoles().stream()
						.filter(role -> role.equals(Role.USER_ROLE) || role.equals(Role.COMPANY_PARTNER)
								|| role.equals(Role.ALL_ROLES) || role.equals(Role.OPPORTUNITY) || role.equals(Role.DAM)
								|| role.equals(Role.LEARNING_TRACK) || role.equals(Role.PLAY_BOOK)
								|| role.equals(Role.SHARE_LEADS))
						.collect(Collectors.toCollection(LinkedHashSet::new));
				updatedRoles.addAll(roles);
			} else {
				Set<Role> roles = userDto.getRoles().stream().filter(role -> !role.equals(Role.COMPANY_PARTNER))
						.collect(Collectors.toCollection(LinkedHashSet::new));
				updatedRoles.addAll(roles);
				isWelcomePageEnabled = false;
				userService.setTeamMemberPartnerFilter(userDto, userDto.getId());
			}
			userDto.setRoles(updatedRoles);
			response.setData(userDto);
		} else {
			UserDTO userDto = new UserDTO();
			userDto.setRoles(updatedRoles);
			response.setData(userDto);

		}
		Map<String, Object> map = new HashMap<>();
		map.put("isWelcomePageEnabled", isWelcomePageEnabled);
		response.setMap(map);

		response.setStatusCode(200);
		return response;
	}

	@Override
	public void changeTeamMemberStatus(Integer teamMemberId) {
		TeamMember teamMember = teamDao.getByTeamMemberId(teamMemberId);
		if (teamMember != null) {
			teamMember.setTeamMemberStatus(TeamMemberStatus.APPROVE);
		}
	}

	@Override
	public XtremandResponse findById(Integer id) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		TeamMemberListDTO teamMemberListDto = teamDao.findById(id);
		Integer teamMemberGroupId = teamMemberListDto.getTeamMemberGroupId();
		if (teamMemberGroupId != null && teamMemberGroupId > 0) {
			boolean enableSecondAdminOption = teamMemberGroupDao.hasSuperVisorRole(teamMemberGroupId);
			List<Integer> partnershipIds = teamMemberGroupDao
					.findAssociatedPartnershipIdsByTeamMemberGroupId(teamMemberGroupId);
			if (partnershipIds != null) {
				teamMemberListDto.setTeamMemberGroupPartnersCount(partnershipIds.size());
			}
			teamMemberListDto.setEnableOption(enableSecondAdminOption);
		}
		response.setData(teamMemberListDto);
		return response;
	}

	@Override
	public XtremandResponse findTeamMemberDetailsByTeamMemberGroupId(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		/******* XNFR-108 *********/
		findExistingTeamMemberIds(pagination);
		/******* XNFR-108 ENDS *********/
		response.setData(teamDao.findTeamMemberDetailsByTeamMemberGroupId(pagination));
		return response;
	}

	/******* XNFR-108 *********/

	private void findExistingTeamMemberIds(Pagination pagination) {
		List<Integer> findSelecetedTeamMemberIds = new ArrayList<>();
		if ("form".equals(pagination.getType()) && pagination.getFormId() > 0 && pagination.getCategoryId() > 0) {
			findSelecetedTeamMemberIds = formDao.getAllSelecetedTeamMemberIds(pagination.getFormId(),
					pagination.getCategoryId());
		}
		if (findSelecetedTeamMemberIds != null && !findSelecetedTeamMemberIds.isEmpty()) {
			pagination.setFiltertedEmailTempalteIds(findSelecetedTeamMemberIds);
		}
	}

	/******* XNFR-108 ENDS *********/

	@Override
	public XtremandResponse findPartners(Pagination pagination) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			response.setData(teamDao.findPartners(pagination));
			return response;
		} catch (HibernateException | TeamMemberDataAccessException u) {
			throw new TeamMemberDataAccessException(u);
		} catch (Exception ex) {
			throw new TeamMemberDataAccessException(ex);
		}
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse deleteTeamMemberPartners(
			DeleteTeamMemberPartnersRequestDTO deleteTeamMemberPartnersRequestDTO) {
		try {
			XtremandResponse response = new XtremandResponse();
			List<Integer> partnerTeamGroupMappingIds = deleteTeamMemberPartnersRequestDTO
					.getPartnerTeamGroupMappingIds();
			Integer loggedInUserId = deleteTeamMemberPartnersRequestDTO.getLoggedInUserId();
			TeamMemberAndPartnerIdsAndUserListIdDTO teamMemberAndPartnerIdsDTO = teamDao
					.findTeamMemberIdAndPartnerIds(partnerTeamGroupMappingIds);
			teamDao.deletePartnerTeamGroupMappingIds(partnerTeamGroupMappingIds);
			if (teamMemberAndPartnerIdsDTO != null) {
				List<Integer> partnerIds = teamMemberAndPartnerIdsDTO.getPartnerIds();
				Integer userListId = teamMemberAndPartnerIdsDTO.getUserListId();
				/****** Delete from xt_user_userlist *********/
				userListDao.deleteUsersFromUserListByUserListIdAndUserIds(userListId, partnerIds);
				/****** Delete from xt_campaign_user_userlist *********/
				List<Integer> userListIds = new ArrayList<>();
				userListIds.add(userListId);
				/******** Deleting rows from xt_dam_partner_group_mapping *********/
				damDao.deleteFromDamPartnerGroupMappingAndDamPartnerByUserListIdAndUserIds(partnerIds, userListId,
						loggedInUserId);
				/***** XBI-2070 ***********/
				List<Integer> learningTrackIds = findVisibilityIdsAndDeleteTrackAssociatedData(partnerIds, userListId);
				response.setData(learningTrackIds);
				/***** XBI-2070 ***********/
			}
			response.setStatusCode(200);
			response.setMessage("Record(s) deleted successfully");
			return response;
		} catch (HibernateException | TeamMemberDataAccessException u) {
			throw new TeamMemberDataAccessException(u);
		} catch (Exception ex) {
			throw new TeamMemberDataAccessException(ex);
		}
	}

	private List<Integer> findVisibilityIdsAndDeleteTrackAssociatedData(List<Integer> partnerIds, Integer userListId) {
		List<Integer> partnershipIds = findPartnershipIds(partnerIds, userListId);
		List<Integer> learningTrackIds = findLearningTrackIds(partnerIds, userListId);
		List<Integer> visibilityIds = findVisibilityIds(userListId, partnershipIds);
		if (XamplifyUtils.isNotEmptyList(visibilityIds)) {
			HibernateSQLQueryResultRequestDTO deleteLmsVisibilityQueryDTO = new HibernateSQLQueryResultRequestDTO();
			deleteLmsVisibilityQueryDTO.setQueryString("delete  from xt_learning_track_visibility where id in (:ids)");
			deleteLmsVisibilityQueryDTO.getQueryParameterListDTOs()
					.add(new QueryParameterListDTO("ids", visibilityIds));
			hibernateSQLQueryResultUtilDao.update(deleteLmsVisibilityQueryDTO);
			if (XamplifyUtils.isNotEmptyList(learningTrackIds)) {
				HibernateSQLQueryResultRequestDTO learingTrackVisibilityQueryDTO = new HibernateSQLQueryResultRequestDTO();
				learingTrackVisibilityQueryDTO.setQueryString(
						"select case when count(*)>0 then false else true end from xt_learning_track_visibility xltv where xltv.learning_track_id in (:ids)");
				learingTrackVisibilityQueryDTO.getQueryParameterListDTOs()
						.add(new QueryParameterListDTO("ids", learningTrackIds));
				boolean isEmptyList = hibernateSQLQueryResultUtilDao.returnBoolean(learingTrackVisibilityQueryDTO);
				if (isEmptyList) {
					HibernateSQLQueryResultRequestDTO updateLearningTrackStatusQueryDTO = new HibernateSQLQueryResultRequestDTO();
					updateLearningTrackStatusQueryDTO
							.setQueryString("update xt_learning_track set is_published = false where id in (:ids)");
					updateLearningTrackStatusQueryDTO.getQueryParameterListDTOs()
							.add(new QueryParameterListDTO("ids", learningTrackIds));
					hibernateSQLQueryResultUtilDao.update(updateLearningTrackStatusQueryDTO);

				}
			}
		}
		return learningTrackIds;
	}

	@SuppressWarnings("unchecked")
	private List<Integer> findLearningTrackIds(List<Integer> partnerIds, Integer userListId) {
		List<Integer> learningTrackIds = new ArrayList<>();
		if (XamplifyUtils.isValidInteger(userListId) && XamplifyUtils.isNotEmptyList(partnerIds)) {
			HibernateSQLQueryResultRequestDTO learningTrackIdsQueryDTO = new HibernateSQLQueryResultRequestDTO();
			learningTrackIdsQueryDTO.setQueryString(
					"select distinct  xltv.learning_track_id  from xt_learning_track_visibility xltv,xt_learning_track_visibility_group xltvg \r\n"
							+ "where xltvg.user_list_id  = :userListId and xltv.id = xltvg.visibility_id \r\n"
							+ "and xltv.user_id in (:partnerIds)");
			setParnterIdsAndUserListIdQueryParameters(partnerIds, userListId, learningTrackIdsQueryDTO);
			learningTrackIds = (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(learningTrackIdsQueryDTO);
		}
		return learningTrackIds;
	}

	private void setParnterIdsAndUserListIdQueryParameters(List<Integer> partnerIds, Integer userListId,
			HibernateSQLQueryResultRequestDTO learningTrackIdsQueryDTO) {
		learningTrackIdsQueryDTO.getQueryParameterListDTOs().add(new QueryParameterListDTO("partnerIds", partnerIds));
		learningTrackIdsQueryDTO.getQueryParameterDTOs().add(new QueryParameterDTO("userListId", userListId));
	}

	@SuppressWarnings("unchecked")
	private List<Integer> findVisibilityIds(Integer userListId, List<Integer> partnershipIds) {
		List<Integer> visibilityIds = new ArrayList<>();
		if (XamplifyUtils.isNotEmptyList(partnershipIds) && XamplifyUtils.isValidInteger(userListId)) {
			HibernateSQLQueryResultRequestDTO visibilityIdsRequestDTO = new HibernateSQLQueryResultRequestDTO();
			visibilityIdsRequestDTO.setQueryString(
					"select distinct xltv.id  from xt_learning_track_visibility xltv,xt_learning_track_visibility_group xltvg \r\n"
							+ "where xltvg.user_list_id  = :userListId and xltv.id = xltvg.visibility_id and xltv.partnership_id in(:partnershipIds)");
			visibilityIdsRequestDTO.getQueryParameterListDTOs()
					.add(new QueryParameterListDTO("partnershipIds", partnershipIds));
			visibilityIdsRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("userListId", userListId));
			visibilityIds = (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(visibilityIdsRequestDTO);
		}
		return visibilityIds;
	}

	@SuppressWarnings("unchecked")
	private List<Integer> findPartnershipIds(List<Integer> partnerIds, Integer userListId) {
		List<Integer> partnershipIds = new ArrayList<>();
		if (XamplifyUtils.isValidInteger(userListId) && XamplifyUtils.isNotEmptyList(partnerIds)) {
			HibernateSQLQueryResultRequestDTO partnershipIdsRequestDTO = new HibernateSQLQueryResultRequestDTO();
			partnershipIdsRequestDTO.setQueryString(
					"select distinct  xltv.partnership_id  from xt_learning_track_visibility xltv,xt_learning_track_visibility_group xltvg \r\n"
							+ "where xltvg.user_list_id  = :userListId and xltv.id = xltvg.visibility_id \r\n"
							+ "and xltv.user_id  in(:partnerIds)");
			setParnterIdsAndUserListIdQueryParameters(partnerIds, userListId, partnershipIdsRequestDTO);
			partnershipIds = (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(partnershipIdsRequestDTO);
		}
		return partnershipIds;
	}

	@Override
	public XtremandResponse findMaximumAdminsLimitDetails(Integer loggedInUserId) {
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		return findMaximumAdminsLimitDetailsByCompanyId(companyId);
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse updatePrimaryAdmin(Integer loggedInUserId, Integer teamMemberUserId,
			XtremandResponse response, TeamMemberDTO teamMemberDTO) {
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		Integer existingPrimaryAdminId = teamDao.findPrimaryAdminIdByCompanyId(companyId);
		teamDao.updatePrimaryAdminId(existingPrimaryAdminId, teamMemberUserId);
		utilService.revokeAccessTokensByCompanyId(companyId);

		/** XNFR-885 **/
		Map<String, Object> approvalsMap = autoApprovePendingContentAndReturnHistoryList(loggedInUserId, companyId,
				teamMemberUserId, true, true);
		if (approvalsMap.containsKey(WHITE_LABELED_RE_APPROVAL_DAM_IDS)) {
			teamMemberDTO.setWhiteLabeledReApprovalDamIds(
					(List<Integer>) approvalsMap.get(WHITE_LABELED_RE_APPROVAL_DAM_IDS));
			teamMemberDTO.setCompanyId(companyId);
		}

		performApprovalInsertionOperations(approvalsMap);

		response.setStatusCode(200);
		response.setMessage("Primary Admin Updated Successfully");
		return response;
	}

	@Override
	public XtremandResponse findMaximumAdminsLimitDetailsByCompanyId(Integer companyId) {
		XtremandResponse response = new XtremandResponse();
		response.setData(teamDao.findMaxAdminAnalyticsByCompanyId(companyId));
		response.setStatusCode(200);
		return response;
	}

	@Override
	public XtremandResponse findPrimaryAdminAndExtraAdmins(Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		response.setData(teamDao.findPrimaryAdminAndExtraAdmins(companyId));
		response.setStatusCode(200);
		return response;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse addTeamMemberUsingSignUpLink(SignUpRequestDTO signUpRequestDto) {
		try {
			XtremandResponse response = new XtremandResponse();
			Integer companyId = userDao.getCompanyIdByProfileName(signUpRequestDto.getCompanyProfileName());
			signUpRequestDto.setCompanyId(companyId);
			String emailId = signUpRequestDto.getEmailId().trim();
			List<String> domains = domainDao.findAllDomainNames(companyId, DomainModuleNameType.TEAM_MEMBER);
			String domainName = XamplifyUtils.getEmailDomain(emailId);
			boolean isValidDomain = XamplifyUtils.isNotEmptyList(domains) && domains.indexOf(domainName) > -1;
			if (isValidDomain) {
				List<String> teamMemberEmailIds = new ArrayList<>();
				String updatedEmailId = emailId.toLowerCase();
				teamMemberEmailIds.add(updatedEmailId);
				validateEmailIds(response, teamMemberEmailIds);
				if (response.getStatusCode() == 413) {
					throw new BadRequestException(teamMemberErrorMessage);
				} else {
					setPropertiesAndAddAsTeamMember(signUpRequestDto, response, companyId, updatedEmailId,
							TeamMemberAddType.SIGNUP_LINK);
				}
			} else {
				String companySupportEmailId = userDao.getSupportEmailIdByCompanyId(companyId);
				String invalidDomainEmailErrorMessage = XamplifyConstants.INVALID_DOMAIN_EMAIL;
				if (StringUtils.hasText(companySupportEmailId)) {
					invalidDomainEmailErrorMessage += " or contact  " + companySupportEmailId;
				}
				throw new BadRequestException(invalidDomainEmailErrorMessage);
			}
			return response;
		} catch (BadRequestException e) {
			throw new BadRequestException(e.getMessage());
		} catch (CustomValidatonException e) {
			throw new CustomValidatonException(e.getMessage());
		} catch (HibernateException | TeamMemberDataAccessException u) {
			throw new TeamMemberDataAccessException(u);
		} catch (Exception ex) {
			throw new TeamMemberDataAccessException(ex);
		}
	}

	private void setPropertiesAndAddAsTeamMember(SignUpRequestDTO signUpRequestDto, XtremandResponse response,
			Integer companyId, String updatedEmailId, String teamMemberAddType) {
		List<TeamMember> teamMembers = new ArrayList<>();
		List<User> newUsers = new ArrayList<>();
		Integer primaryAdminId = teamDao.findPrimaryAdminIdByCompanyId(companyId);
		User loggedInUser = userDao.findByPrimaryKey(primaryAdminId,
				new FindLevel[] { FindLevel.ROLES, FindLevel.COMPANY_PROFILE });
		List<TeamMemberDTO> teamMemberDTOs = new ArrayList<>();
		TeamMemberDTO teamMemberDTO = new TeamMemberDTO();
		teamMemberDTO.setEmailId(updatedEmailId);
		if (TeamMemberAddType.SIGNUP_LINK.equals(teamMemberAddType)) {
			teamMemberDTO.setFirstName(signUpRequestDto.getFirstName());
			teamMemberDTO.setLastName(signUpRequestDto.getLastName());
			teamMemberDTO.setAddedThroughSignUpLink(true);
			teamMemberDTO.setPassword(passwordEncoder.encode(signUpRequestDto.getPassword()));
		}
		String groupAlias = signUpRequestDto.getGroupAlias();
		Integer teamMemberGroupId = XamplifyUtils.isValidString(groupAlias)
				? teamDao.findTeamMemberGroupIdByAlias(groupAlias)
				: fetchDefaultTeamMemberGroupIdByCompanyId(companyId);
		if (XamplifyUtils.isValidInteger(teamMemberGroupId)) {
			teamMemberDTO.setTeamMemberGroupId(teamMemberGroupId);
			teamMemberDTO.setUserId(primaryAdminId);
			teamMemberDTOs.add(teamMemberDTO);
			iterateDtosAndAddTeamMembers(teamMemberDTOs, loggedInUser, teamMembers, newUsers);
			response.setStatusCode(200);
			response.setMessage("Team Member Added Successfully");
		} else {
			String supportEmailId = userDao.getSupportEmailIdByCompanyId(companyId);
			String invalidSignUpUrlErrorMessage = XamplifyUtils.isValidString(supportEmailId)
					? XamplifyConstants.INVALID_SIGN_UP_URL + " Please contact <a href=\"mailto:" + supportEmailId
							+ "\">" + supportEmailId + "</a>"
					: XamplifyConstants.INVALID_SIGN_UP_URL;
			throw new CustomValidatonException(invalidSignUpUrlErrorMessage);
		}
	}

	private Integer fetchDefaultTeamMemberGroupIdByCompanyId(Integer companyId) {
		return teamDao.findChannelAccountManagerIdByCompanyId(companyId);
	}

	@Override
	public XtremandResponse findPrimaryAdmin(Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		String queryString = "select u.user_id as \"teamMemberUserId\",coalesce(u.firstname,'') as \"firstName\",coalesce(u.lastname,'') as \"lastName\",\n"
				+ "u.email_id as \"emailId\",u.mobile_number as \"mobileNumber\",cast(u.status as text) as \"status\"\n"
				+ "from xt_company_profile c,xt_user_role ur,xt_user_profile u left join\n"
				+ "xt_team_member t on t.team_member_id = u.user_id where c.company_id = u.company_id and u.user_id = ur.user_id\n"
				+ "and role_id in (" + Role.getAllAdminRolesAndPartnerRoleInString()
				+ ")   and c.company_id  = (select company_id from xt_user_profile xup where xup.user_id = :userId)\n"
				+ "group by c.company_id,u.user_id having CAST(count(t.id)AS integer) = 0\n";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("userId", loggedInUserId));
		TeamMemberListDTO teamMemberListDTO = (TeamMemberListDTO) hibernateSQLQueryResultUtilDao
				.getDto(hibernateSQLQueryResultRequestDTO, TeamMemberListDTO.class);
		if (teamMemberListDTO != null) {
			response.setStatusCode(200);
			response.setData(teamMemberListDTO);
		} else {
			response.setStatusCode(404);
			response.setMessage("Primary Admin Not Found");
		}
		return response;
	}

	@Override
	public XtremandResponse getInviteTeamMemberTemplate(Integer userId, String companyProfileName, Integer templateId) {
		XtremandResponse response = new XtremandResponse();
		boolean hasVanityAccess = utilDao.hasVanityAccessByUserId(userId);
		if (XamplifyUtils.isValidString(companyProfileName) || hasVanityAccess) {
			fetchCustomTemplate(userId, templateId, response);
		} else {
			fetchDefaultTemplate(userId, templateId, response);
		}
		return response;
	}

	private void fetchCustomTemplate(Integer userId, Integer templateId, XtremandResponse response) {
	}

	private void fetchDefaultTemplate(Integer userId, Integer templateId, XtremandResponse response) {
	}

	@Override
	public XtremandResponse saveInviteTeamMembersData(Integer loggedInUserId, VendorInvitationDTO vendorInvitationDTO) {
		XtremandResponse response = new XtremandResponse();
		List<String> teamMemberEmailIds = vendorInvitationDTO.getEmailIds();
		String vendorCompanyProfileName = vendorInvitationDTO.getVanityURL();
		try {
			Set<String> duplicateEmailIds = XamplifyUtils.findDuplicateStrings(teamMemberEmailIds);
			boolean hasVanity = XamplifyUtils.isValidString(vendorCompanyProfileName);
			boolean hasUserId = XamplifyUtils.isValidInteger(loggedInUserId);
			if (XamplifyUtils.isNotEmptySet(duplicateEmailIds)) {
				response.setStatusCode(400);
				response.setData(duplicateEmailIds);
				response.setMessage("Please remove duplicate email id(s)");
			} else if (!hasUserId) {
				response.setStatusCode(401);
				response.setMessage("userId is missing");
			} else {
				validateEmailIds(response, vendorInvitationDTO.getEmailIds());
				if (response.getStatusCode() != 413) {
					TeamMemberDTO teamMemberDTO = new TeamMemberDTO();
					teamMemberDTO.setVanityUrlFilter(hasVanity);
					teamMemberDTO.setUserId(loggedInUserId);
					teamMemberDTO.setAddedThroughInvitation(true);
					teamMemberDTO.setSubjectLine(vendorInvitationDTO.getSubject());
					teamMemberDTO.setVendorCompanyProfileName(vendorCompanyProfileName);
					teamMemberDTO.setTeamMemberGroupId(vendorInvitationDTO.getTeamMemberGroupId());
					iterateAndSaveTeamMembersData(loggedInUserId, teamMemberEmailIds, teamMemberDTO);
					Map<String, Object> map = new HashMap<>();
					map.put(TEAM_MEMBER_EMAIL_IDS, teamMemberEmailIds);
					map.put(TEAM_MEMBER_DTO, teamMemberDTO);
					response.setStatusCode(200);
					response.setMap(map);
					if (teamMemberEmailIds.size() > 1) {
						response.setMessage("TeamMember invitations has been sent successfully.");
					} else {
						response.setMessage("TeamMember invitation has been sent successfully.");
					}
				}
			}
			return response;
		} catch (BadRequestException e) {
			throw new BadRequestException(e.getMessage());
		} catch (HibernateException | TeamMemberDataAccessException u) {
			throw new TeamMemberDataAccessException(u);
		} catch (Exception ex) {
			throw new TeamMemberDataAccessException(ex);
		}
	}

	private void iterateAndSaveTeamMembersData(Integer loggedInUserId, List<String> teamMemberEmailIds,
			TeamMemberDTO teamMemberDTO) {
		User loggedInUser = userDao.findByPrimaryKey(loggedInUserId,
				new FindLevel[] { FindLevel.ROLES, FindLevel.COMPANY_PROFILE });
		List<User> newUsers = new ArrayList<>();
		List<TeamMember> teamMembers = new ArrayList<>();
		Integer companyId = loggedInUser.getCompanyProfile().getId();
		/*** XBI-4057 ***/
		Integer teamMemberGroupId = teamMemberDTO.getTeamMemberGroupId();
		for (String emailId : teamMemberEmailIds) {
			teamMemberDTO.setEmailId(emailId);
			UserList teamMemberUserList = new UserList();
			User existingUser = userDao.getUserByEmail(emailId.trim().toLowerCase());
			List<Integer> roleIds = teamMemberGroupDao.findRoleIdsByTeamMemberGroupId(teamMemberGroupId);
			if (existingUser != null) {
				TeamMember teamMember = new TeamMember();
				teamMember.setTeamMember(existingUser);
				teamMember.setAddedThroughInvitation(true);
				setTeamMemberOrUserStatusByCondition(existingUser, teamMember, teamMemberDTO.getPassword());
				existingUser.setActivatedTime(new Date());
				existingUser.setRegisteredTime(new Date());
				existingUser.setSource(UserSource.INVITATION);
				existingUser.setCompanyProfile(loggedInUser.getCompanyProfile());
				setTeamMemberAndTeamMemberGroupUserMappingUtilData(loggedInUser, teamMemberDTO, existingUser,
						teamMember, teamMemberGroupId, roleIds, teamMemberUserList);
				teamMember.setTeamMemberUserList(teamMemberUserList);
				/** XNFR-821 **/
				setApprovalAuthoritiesForAdminsAndSupervisors(roleIds, teamMember);
				teamMembers.add(teamMember);
			} else {
				setNewUsersAndTeamMembersData(loggedInUser, newUsers, teamMemberDTO, roleIds, teamMemberGroupId,
						teamMemberUserList);
			}
		}
		addNewUsersAndTeamMembers(newUsers, companyId);
		addTeamMembers(teamMembers, companyId);
	}

	@Override
	public Map<String, Object> inviteTeamMembersCount(Integer userId) {
		Map<String, Object> resultMap = new HashMap<>();
		if (XamplifyUtils.isValidInteger(userId)) {
			Integer companyId = userDao.getCompanyIdByUserId(userId);
			List<String> teamMemberStatusList = Arrays.asList("APPROVE", "UNAPPROVED", "DECLINE", null);
			for (String statusType : teamMemberStatusList) {
				String queryString = "select cast(count(*) as integer) from xt_team_member xtm \n"
						+ "where xtm.is_added_through_invitation = true and xtm.company_id = :companyId";
				queryString = (statusType != null) ? (queryString + " and xtm.status='" + statusType + "'")
						: queryString;
				HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
				hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
				hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
						.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
				Integer totalRecords = (Integer) hibernateSQLQueryResultUtilDao
						.getUniqueResult(hibernateSQLQueryResultRequestDTO);
				String type = (statusType != null) ? statusType : "ALL";
				resultMap.put(type, totalRecords);
			}
		}
		return resultMap;
	}

	@Override
	public Map<String, Object> inviteTeamMemberAnalytics(Integer userId, String statusType, Pageable pageable) {
		Pagination pagination = utilService.setPageableParameters(pageable, userId);
		Integer companyId = pagination.getCompanyId();
		String queryString = "SELECT xup.firstname AS \"firstName\", xup.lastname AS \"lastName\", xup.email_id AS \"emailId\", \n"
				+ "xcp.company_name AS \"companyName\", xtm.status AS \"status\", xup1.email_id AS \"invitedBy\", \n"
				+ "TO_CHAR(xtm.created_time, 'YYYY-MM-DD HH24:MI:SS') AS \"referredDateUTC\" FROM xt_team_member xtm, \n"
				+ "xt_user_profile xup, xt_company_profile xcp, xt_user_profile xup1, xt_team_member_emails_history xtmeh \n"
				+ "where xup.user_id = xtm.team_member_id and xcp.company_id = xtm.company_id and xtm.id = xtmeh.team_member_id \n"
				+ "and xup1.user_id = xtmeh.sent_by and xtm.is_added_through_invitation = true and xtm.company_id = :companyId \n";
		String filterQueryString = addFilterQuery(pageable);
		queryString += (!"ALL".equalsIgnoreCase(statusType))
				? " and xtm.status='" + statusType + "' " + filterQueryString
				: filterQueryString;
		String sortQueryString = addSortColumns(pagination);
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
		hibernateSQLQueryResultRequestDTO.setSortQueryString(sortQueryString);
		hibernateSQLQueryResultRequestDTO.setClassInstance(ReferedVendorDTO.class);
		List<String> searchColumns = new ArrayList<>();
		searchColumns.add("xup.firstname");
		searchColumns.add("xup.lastname");
		searchColumns.add("xup.email_id");
		searchColumns.add("xcp.company_name");
		searchColumns.add("xup1.email_id");
		hibernateSQLQueryResultRequestDTO.setSearchColumns(searchColumns);
		Map<String, Object> resultMap = hibernateSQLQueryResultUtilDao
				.returnPaginatedDTOList(hibernateSQLQueryResultRequestDTO, pagination, pagination.getSearchKey());
		@SuppressWarnings("unchecked")
		List<ReferedVendorDTO> list = (List<ReferedVendorDTO>) resultMap.get("list");
		Set<ReferedVendorDTO> uniqueSet = new LinkedHashSet<>(list);
		resultMap.put("list", uniqueSet);
		return resultMap;
	}

	private String addSortColumns(Pagination pagination) {
		List<SortColumnDTO> sortColumnDTOs = new ArrayList<>();
		SortColumnDTO createdTimeSortOption = new SortColumnDTO(XamplifyConstants.CREATED_TIME, "xtm.created_time",
				false, true, false);
		SortColumnDTO emailSortOption = new SortColumnDTO(XamplifyConstants.EMAIL_ID, "xup.email_id", false, true,
				false);
		sortColumnDTOs.add(createdTimeSortOption);
		sortColumnDTOs.add(emailSortOption);
		return paginationUtil.generateSortQuery(pagination, sortColumnDTOs, "desc");
	}

	private String addFilterQuery(Pageable pageable) {
		StringBuilder filterQuery = new StringBuilder(" ");
		String filterBy = pageable.getFilterBy();
		if (XamplifyUtils.isValidString(filterBy)) {
			String[] emailIds = filterBy.split(",");
			String emails = Arrays.stream(emailIds).filter(XamplifyUtils::isValidString)
					.map(emailId -> "'" + emailId + "'").collect(Collectors.joining(","));
			if (XamplifyUtils.isValidString(emails)) {
				filterQuery.append(" and xup1.email_id in (").append(emails).append(") ");
			}
		}
		String fromDate = pageable.getFromDateFilterString();
		String toDate = pageable.getToDateFilterString();
		if (XamplifyUtils.isValidString(fromDate) && XamplifyUtils.isValidString(toDate)) {
			filterQuery.append(" and (xtm.created_time >= '").append(fromDate).append(" 00:00:00' ")
					.append("and xtm.created_time < '").append(toDate).append(" 23:59:59') ");
		}
		return String.valueOf(filterQuery);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void inviteTeamMemberDownloadCsv(Integer userId, String type, Pageable pageable,
			HttpServletResponse response) {
		try {
			Map<String, Object> resultMap = inviteTeamMemberAnalytics(userId, type, pageable);
			Set<ReferedVendorDTO> teamMembers = (Set<ReferedVendorDTO>) resultMap.get("list");
			List<String[]> data = new ArrayList<>();
			data.add(new String[] { "FIRSTNAME", "LASTNAME", "EMAIL ID", "COMPANY NAME", "INVITED BY", "STATUS",
					"INVITED DATE AND TIME" });
			for (ReferedVendorDTO teamMember : teamMembers) {
				String status = teamMember.getStatus();
				if ("APPROVE".equalsIgnoreCase(status)) {
					status = "ACTIVATED";
				} else if ("UNAPPROVED".equalsIgnoreCase(status)) {
					status = "INVITED";
				} else if ("DECLINE".equalsIgnoreCase(status)) {
					status = "DEACTIVATED";
				}
				data.add(new String[] { teamMember.getFirstName(), teamMember.getLastName(), teamMember.getEmailId(),
						teamMember.getCompanyName(), teamMember.getInvitedBy(), status,
						teamMember.getReferredDateUTC() });
			}
			String fileName = "invite-team-member-analytics.csv";
			XamplifyUtils.generateCSV(fileName, response, data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** XNFR-821 **/
	private void setApprovalAuthoritiesForAdminsAndSupervisors(List<Integer> roleIds, TeamMember teamMember) {
		if (roleIds.indexOf(Role.ALL_ROLES.getRoleId()) > -1) {
			setAllApprovalPrivileges(teamMember, true);
		}
	}

	private void updateApprovalAuthorities(Integer teamMemberGroupId, TeamMember teamMember,
			TeamMemberDTO teamMemberDTO) {
		if (XamplifyUtils.isValidInteger(teamMemberGroupId) && teamMember != null) {
			List<Integer> updatedRoleIds = teamMemberGroupDao.findRoleIdsByTeamMemberGroupId(teamMemberGroupId);
			Set<Integer> existingRoleIds = findUserRoles(teamMember);
			boolean previouslyHadAllRole = existingRoleIds.contains(Role.ALL_ROLES.getRoleId());
			boolean currentlyHasAllRole = updatedRoleIds.contains(Role.ALL_ROLES.getRoleId());

			if (!previouslyHadAllRole && currentlyHasAllRole) {
				setAllApprovalPrivileges(teamMember, true);
				teamMemberDTO.setApprovalManager(true);
				return;
			}

			if (previouslyHadAllRole && !currentlyHasAllRole) {
				setAllApprovalPrivileges(teamMember, false);
				return;
			}

			revokeRoleSpecificApprovals(teamMember, updatedRoleIds, existingRoleIds);
		}
	}

	private void revokeRoleSpecificApprovals(TeamMember teamMember, List<Integer> updatedRoleIds,
			Set<Integer> existingRoleIds) {
		if (existingRoleIds.contains(Role.DAM.getRoleId()) && !updatedRoleIds.contains(Role.DAM.getRoleId())) {
			teamMember.setAssetApprover(false);
		}
		if (existingRoleIds.contains(Role.LEARNING_TRACK.getRoleId())
				&& !updatedRoleIds.contains(Role.LEARNING_TRACK.getRoleId())) {
			teamMember.setTrackApprover(false);
		}
		if (existingRoleIds.contains(Role.PLAY_BOOK.getRoleId())
				&& !updatedRoleIds.contains(Role.PLAY_BOOK.getRoleId())) {
			teamMember.setPlaybookApprover(false);
		}
	}

	private void setAllApprovalPrivileges(TeamMember teamMember, boolean access) {
		teamMember.setAssetApprover(access);
		teamMember.setTrackApprover(access);
		teamMember.setPlaybookApprover(access);
	}

	/** XNFR-821 **/
	@SuppressWarnings("unchecked")
	private Map<String, Object> autoApprovePendingContentAndReturnHistoryList(Integer loggedInUserId, Integer companyId,
			Integer createdById, boolean isApprovalManager, boolean performOnlyReApprovalOperation) {
		Map<String, Object> resultMap = new HashMap<>();
		if (isApprovalManager && XamplifyUtils.isValidInteger(loggedInUserId) && XamplifyUtils.isValidInteger(companyId)
				&& XamplifyUtils.isValidInteger(createdById)) {
			List<Integer> createdByIds = new ArrayList<>();
			List<DamTag> allDamTagsToSave = new ArrayList<>();
			createdByIds.add(createdById);
			List<ApprovalStatusHistory> approvalHistoryList = new ArrayList<>();

			if (!performOnlyReApprovalOperation) {
				approvalHistoryList = approveService.processAndSaveApprovalTimelineHistory(createdByIds,
						loggedInUserId);
				approveDao.autoApprovePendingAssets(loggedInUserId, companyId, createdByIds);
				approveDao.autoApprovePendingLMS(createdByIds, companyId, loggedInUserId, null);
			}

			List<Integer> reApprovalVersionDamIds = approveDao.getReApprovalVersionDamIdsByUserIds(createdByIds,
					companyId);

			if (XamplifyUtils.isNotEmptyList(reApprovalVersionDamIds)) {
				List<ContentReApprovalDTO> contentReApprovalDTOs = damDao
						.getAssetDetailsForReApproval(reApprovalVersionDamIds);

				List<Integer> whiteLabeledReApprovalDamIds = contentReApprovalDTOs.stream()
						.filter(ContentReApprovalDTO::isWhiteLabeledAssetSharedWithPartners)
						.map(ContentReApprovalDTO::getApprovalReferenceId).collect(Collectors.toList());
				resultMap.put(WHITE_LABELED_RE_APPROVAL_DAM_IDS, whiteLabeledReApprovalDamIds);

				List<ContentReApprovalDTO> pdfTypeAssetContentDetails = contentReApprovalDTOs.stream()
						.filter(dto -> "pdf".equalsIgnoreCase(dto.getAssetType())).collect(Collectors.toList());

				contentReApprovalDTOs.forEach(dto -> dto.setLoggedInUserId(loggedInUserId));
				List<ContentReApprovalDTO> videoTypeAssetContentDetails = contentReApprovalDTOs.stream()
						.filter(dto -> dto.getVideoId() != null).collect(Collectors.toList());
				List<ContentReApprovalDTO> nonVideoTypeAssetContentDetails = contentReApprovalDTOs.stream()
						.filter(dto -> dto.getVideoId() == null).collect(Collectors.toList());

				List<Integer> damIdsToDelete = approveService.processNonVideoAssetReApprovalAndGetIds(loggedInUserId,
						allDamTagsToSave, nonVideoTypeAssetContentDetails, approvalHistoryList, "");
				resultMap.put("damIdsToDelete", damIdsToDelete);
				resultMap.put("allDamTagsToSave", allDamTagsToSave);

				Map<String, Object> videoMap = approveService.handleReApprovalVersionForVideoTypeAsset(loggedInUserId,
						videoTypeAssetContentDetails, approvalHistoryList, "");
				List<VideoTag> videoTagsToSave = (List<VideoTag>) videoMap.get(VIDEO_TAGS_TO_SAVE_MAP_KEY);
				List<Integer> videoIdsToDelete = (List<Integer>) videoMap.get(VIDEO_IDS_TO_DELETE_MAP_KEY);
				resultMap.put(VIDEO_TAGS_TO_SAVE_MAP_KEY, videoTagsToSave);
				resultMap.put(VIDEO_IDS_TO_DELETE_MAP_KEY, videoIdsToDelete);

				approveService.handleSharedAssetPathForPdfTypeAssets(pdfTypeAssetContentDetails);
			}
			resultMap.put("approvalHistoryList", approvalHistoryList);
		}
		return resultMap;
	}

	/** XNFR-885 **/
	@SuppressWarnings("unchecked")
	private void performApprovalInsertionOperations(Map<String, Object> approvalsMap) {

		List<ApprovalStatusHistory> approvalHistoryList = (List<ApprovalStatusHistory>) approvalsMap
				.get("approvalHistoryList");
		List<Integer> videoIdsToDelete = (List<Integer>) approvalsMap.get(VIDEO_IDS_TO_DELETE_MAP_KEY);
		List<Integer> damIdsToDelete = (List<Integer>) approvalsMap.get("damIdsToDelete");
		List<DamTag> allDamTagsToSave = (List<DamTag>) approvalsMap.get("allDamTagsToSave");
		List<VideoTag> videoTagsToSave = (List<VideoTag>) approvalsMap.get(VIDEO_IDS_TO_DELETE_MAP_KEY);

		if (XamplifyUtils.isNotEmptyList(approvalHistoryList)) {
			xamplifyUtilDao.saveAll(approvalHistoryList, "Approval Status History");
		}

		if (XamplifyUtils.isNotEmptyList(videoIdsToDelete)) {
			videoDAO.deleteVideoRecordsByIds(videoIdsToDelete);
		}

		if (XamplifyUtils.isNotEmptyList(damIdsToDelete)) {
			damDao.deleteByDamIds(damIdsToDelete);
		}

		if (XamplifyUtils.isNotEmptyList(allDamTagsToSave)) {
			xamplifyUtilDao.saveAll(allDamTagsToSave, "Dam Tag for Re-Approval");
		}

		if (XamplifyUtils.isNotEmptyList(videoTagsToSave)) {
			xamplifyUtilDao.saveAll(videoTagsToSave, "VideoTag for Re-Approval");
		}
	}

	/*** XNFR-878 ***/
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse updatePartnerCompanyPrimaryAdmin(
			PartnerPrimaryAdminUpdateDto partnerPrimaryAdminUpdateDto) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(403);

		Integer vendorCompanyUserId = partnerPrimaryAdminUpdateDto.getVendorCompanyUserId();
		Integer partnerCompanyNewPrimaryAdminId = partnerPrimaryAdminUpdateDto.getPartnerCompanyTeamMemberUserId();
		Integer vendorCompanyId = userDao.getCompanyIdByUserId(vendorCompanyUserId);
		Integer partnerCompanyId = userDao.getCompanyIdByUserId(partnerCompanyNewPrimaryAdminId);
		Integer partnerCompanyExistingPrimaryAdminId = teamDao.findPrimaryAdminIdByCompanyId(partnerCompanyId);

		String validationError = validateRequest(vendorCompanyUserId, vendorCompanyId, partnerCompanyId,
				partnerCompanyExistingPrimaryAdminId, partnerCompanyNewPrimaryAdminId);
		if (validationError != null) {
			return buildErrorResponse(response, validationError);
		}
		teamDao.updatePrimaryAdminId(partnerCompanyExistingPrimaryAdminId, partnerCompanyNewPrimaryAdminId);
		utilService.revokeAccessTokensByCompanyId(partnerCompanyId);
		response.setStatusCode(200);
		response.setMessage("Primary Admin Updated Successfully");
		return response;
	}

	private String validateRequest(Integer loggedInUserId, Integer loggedInUserCompanyId, Integer partnerCompanyId,
			Integer partnerCompanyExistingPrimaryAdminId, Integer partnerCompanyNewPrimaryAdminId) {
		if (isSameCompany(loggedInUserCompanyId, partnerCompanyId)) {
			return "The logged-in user's company cannot be the same as the partner company.";
		}
		if (!isVendorAllowedToChangeAdmin(loggedInUserCompanyId)) {
			return "This option has been disabled by xAmplify Admin. Please contact support team.";
		}

		if (!isValidPartnership(loggedInUserCompanyId, partnerCompanyId)) {
			return "No existing partnership found.";
		}
		if (!isAdminOrSupervisor(loggedInUserId)) {
			return "Only an admin or supervisor can perform this action.";
		}

		TeamMemberDTO teamMemberDTO = teamDao.getTeamMemberStatus(partnerCompanyNewPrimaryAdminId);
		if (teamMemberDTO != null && !TeamMemberStatus.APPROVE.getStatus().equals(teamMemberDTO.getUserStatus())) {
			return "Selected partner company team member status is inactive.Please choose a differnet team member.";
		}

		if (partnerCompanyExistingPrimaryAdminId.equals(partnerCompanyNewPrimaryAdminId)) {
			return "Invalid request: The selected partner company team member is already the Primary Admin. Please choose a different team member.";
		}

		return null; // No errors, request is valid
	}

	private boolean isVendorAllowedToChangeAdmin(Integer companyId) {
		return utilDao.isAllowVendorToChangeThePartnerAdminOptionEnabledByCompanyId(companyId);
	}

	private boolean isSameCompany(Integer company1, Integer company2) {
		return company1.equals(company2);
	}

	private boolean isValidPartnership(Integer vendorCompanyId, Integer partnerCompanyId) {
		return partnershipDao.isPartnershipEstablishedAndApporved(vendorCompanyId, partnerCompanyId);
	}

	private boolean isAdminOrSupervisor(Integer userId) {
		return utilDao.isSuperVisorOrAnyAdmin(userId);
	}

	private XtremandResponse buildErrorResponse(XtremandResponse response, String message) {
		response.setMessage(message);
		return response;
	}

	@Override
	public XtremandResponse sendTeamMemberReminder(Pagination pagination, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		User loggedInUser = userService.loadUser(
				Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, loggedInUserId)),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		boolean hasVanityAccess = utilDao.hasVanityAccessByUserId(pagination.getUserId());
		String subject;
		String body;

		if (hasVanityAccess && XamplifyUtils.isValidString(loggedInUser.getCompanyProfile().getCompanyProfileName())) {
			CompanyProfile cp = vanityURLDao
					.getCompanyProfileByCompanyProfileName(loggedInUser.getCompanyProfile().getCompanyProfileName());
			DefaultEmailTemplate vanityDefaultEmailTemplate = vanityURLDao
					.getVanityDefaultEmailTemplateByType(DefaultEmailTemplateType.TEAM_MEMBER_PORTAL);

			CustomDefaultEmailTemplate vanityEmailTemplate = vanityURLDao
					.getVanityETByDefVanityETIdAndCompanyId(vanityDefaultEmailTemplate.getId(), cp);

			if (vanityEmailTemplate != null) {
				subject = vanityEmailTemplate.getSubject();
				body = genericDao.get(CustomDefaultEmailTemplate.class, vanityEmailTemplate.getId()).getHtmlBody();
			} else {
				subject = vanityDefaultEmailTemplate.getSubject();
				body = genericDao.get(DefaultEmailTemplate.class, vanityDefaultEmailTemplate.getId()).getHtmlBody();
			}
			mailService.sendPartnerTeamMemberEmail(pagination, subject, body, loggedInUser);
			response.setStatusCode(StatusCodeConstants.TEST_EMAIL_SENT_SUCCESSFULLY);
		} else {
			DefaultEmailTemplate vanityDefaultEmailTemplate = vanityURLDao
					.getVanityDefaultEmailTemplateByType(DefaultEmailTemplateType.TEAM_MEMBER_PORTAL);
			subject = vanityDefaultEmailTemplate.getSubject();
			body = genericDao.get(DefaultEmailTemplate.class, vanityDefaultEmailTemplate.getId()).getHtmlBody();

			mailService.sendPartnerTeamMemberEmail(pagination, subject, body, loggedInUser);
			response.setStatusCode(StatusCodeConstants.TEST_EMAIL_SENT_SUCCESSFULLY);
		}
		return response;

	}

	@Override
	public void addSourceCompanyMembersToDestination(Integer sourceCompanyId, Integer destinationCompanyId) {
		List<String> sourceCompanyUserEmails = userDao.findEmailIdsByCompanyId(sourceCompanyId);
		List<TeamMember> teamMembers = new ArrayList<>();
		List<User> newUsers = new ArrayList<>();
		Integer destinationCompanyPrimaryAdminId = teamDao.findPrimaryAdminIdByCompanyId(destinationCompanyId);
		User loggedInUser = userDao.findByPrimaryKey(destinationCompanyPrimaryAdminId,
				new FindLevel[] { FindLevel.ROLES, FindLevel.COMPANY_PROFILE });

		List<TeamMemberDTO> teamMemberDTOs = new ArrayList<>();
		for (String sourceCompanyUserEmail : sourceCompanyUserEmails) {
			TeamMemberDTO teamMemberDTO = new TeamMemberDTO();
			teamMemberDTO.setEmailId(sourceCompanyUserEmail.toLowerCase().trim());
			List<TeamMemberGroupDTO> defaultTeamMemerGroups = teamMemberGroupDao
					.findDefaultGroupsByCompanyId(destinationCompanyId);
			Integer teamMemberGroupId = defaultTeamMemerGroups.get(0).getId();
			teamMemberDTO.setTeamMemberGroupId(teamMemberGroupId);
			teamMemberDTO.setUserId(destinationCompanyPrimaryAdminId);
			teamMemberDTOs.add(teamMemberDTO);
		}
		iterateDtosAndAddTeamMembers(teamMemberDTOs, loggedInUser, teamMembers, newUsers);

	}

	public void deletePartnerTeamGroupMapping(List<Integer> partnerTeamGroupMappingIds) {
		if (XamplifyUtils.isNotEmptyList(partnerTeamGroupMappingIds)) {
			teamMemberGroupDao.deletePartnerTeamGroupMapping(partnerTeamGroupMappingIds);
		}
	}

}
