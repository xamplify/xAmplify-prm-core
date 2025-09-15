package com.xtremand.partnership.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.jcodec.common.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.Criteria.OPERATION_NAME;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.dam.dao.DamDao;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.domain.bom.DomainStatusLog;
import com.xtremand.domain.dao.DomainDao;
import com.xtremand.formbeans.AddPartnerResponseDTO;
import com.xtremand.formbeans.ReferedVendorDTO;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.UserListDTO;
import com.xtremand.formbeans.VendorInvitationDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.gdpr.setting.service.GdprSettingService;
import com.xtremand.lms.dao.LMSDAO;
import com.xtremand.mail.service.AsyncComponent;
import com.xtremand.mail.service.EmailConstants;
import com.xtremand.partner.bom.PartnerDTO;
import com.xtremand.partnership.bom.Partnership;
import com.xtremand.partnership.bom.Partnership.PartnershipSource;
import com.xtremand.partnership.bom.Partnership.PartnershipStatus;
import com.xtremand.partnership.bom.PartnershipDTO;
import com.xtremand.partnership.bom.PartnershipStatusLog;
import com.xtremand.partnership.dao.PartnershipDAO;
import com.xtremand.partnership.dto.VendorInvitationReport;
import com.xtremand.partnership.service.PartnershipService;
import com.xtremand.team.dao.TeamDao;
import com.xtremand.user.bom.LegalBasis;
import com.xtremand.user.bom.TeamMember;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.UserList;
import com.xtremand.user.bom.UserUserList;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.service.UserService;
import com.xtremand.user.validation.UserListValidator;
import com.xtremand.userlist.dao.UserListDAO;
import com.xtremand.userlist.service.UserListService;
import com.xtremand.util.XamplifyUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.AdminAndTeamMemberDetailsDTO;
import com.xtremand.util.dto.PartnerCompanyDTO;
import com.xtremand.util.dto.UserListOperationsAsyncDTO;
import com.xtremand.util.service.UtilService;
import com.xtremand.vanity.url.dao.VanityURLDao;

@Service("PartnershipService")
@Transactional
public class PartnershipServiceImpl implements PartnershipService {

	@Autowired
	private UserListDAO userListDAO;

	@Autowired
	PartnershipDAO partnershipDAO;

	@Autowired
	UserService userService;

	@Autowired
	UserListService userListService;

	@Autowired
	PartnershipServiceHelper partnershipServiceHelper;

	@Autowired
	UserListValidator userListValidator;

	@Autowired
	@Lazy
	private AsyncComponent asyncComponent;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private DamDao damDao;

	@Autowired
	private LMSDAO lmsDao;

	@Autowired
	GdprSettingService gdprSettingService;

	@Autowired
	private XamplifyUtil xamplifyUtil;

	@Autowired
	private UtilDao utilDao;

	@Value("${active.partner.list.name}")
	private String activePartnerListName;

	@Value("${inactive.partner.list.name}")
	private String inactivePartnerListName;

	@Autowired
	private GenericDAO genericDAO;

	@Autowired
	private TeamDao teamDao;

	@Autowired
	private VanityURLDao vanityUrlDao;

	@Autowired
	private UtilService utilService;

	@Autowired
	private DomainDao domainDao;

	/*
	 * Adds list of partners to a partner list validateEmailIds ->
	 * getNonExistingUsersList -> processNonExistingUsers -> getExistingUsers ->
	 * processExistingUsers -> buildResponse
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> updatePartnerList(UserList userList, Set<UserDTO> partners, Integer userId,
			String companyProfileName, UserListOperationsAsyncDTO userListOperationsAsyncDTO) {
		Map<String, Object> resultMap = new HashMap<>();
		List<AddPartnerResponseDTO> responseDTOList = null;
		UserList defaultPartnerList = null;
		if (userList != null && userList.isPartnerUserList() != null && userList.isPartnerUserList() && partners != null
				&& !partners.isEmpty() && userId != null && userId > 0) {
			User loggedInUser = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, userId)),
					new FindLevel[] { FindLevel.COMPANY_PROFILE, FindLevel.ROLES });
			userListOperationsAsyncDTO.setVendorCompanyId(loggedInUser.getCompanyProfile().getId());
			userListOperationsAsyncDTO.setDefaultPartnerList(userList.isDefaultPartnerList());
			userListOperationsAsyncDTO.setUserListId(userList.getId());
			userListOperationsAsyncDTO.getAllPartnerListIds().add(userList.getId());
			if (loggedInUser != null) {
				CompanyProfile userCompany = loggedInUser.getCompanyProfile();
				loggedInUser.setCompanyProfileName(companyProfileName);
				CompanyProfile userListCompany = userList.getCompany();
				if (userCompany != null && userListCompany != null) {
					if (userCompany.getId().equals(userListCompany.getId())) {
						Map<String, Object> validateMap = userListValidator.validateEmailIds(partners);
						if (validateMap != null && validateMap.get("statusCode") != null
								&& ((Integer) validateMap.get("statusCode")).intValue() == 200) {
							resultMap.put("invalidEmailAddresses", null);
							resultMap.put("statusCode", 200);
						} else if (validateMap != null && validateMap.get("statusCode") != null
								&& ((Integer) validateMap.get("statusCode")).intValue() == 409) {
							List<String> invalidEmailIds = ((List<String>) validateMap.get("emailAddresses")).stream()
									.distinct().collect(Collectors.toList());
							List<String> inputEmailIds = partners.stream().map(UserDTO::getEmailId).distinct()
									.collect(Collectors.toList());
							if (invalidEmailIds.size() != inputEmailIds.size()) {
								validateMap.put("statusCode", 200);
								resultMap.put("statusCode", 200);
								partners.removeIf(user -> invalidEmailIds.contains(user.getEmailId()));
							} else {
								resultMap.put("statusCode", 418);
							}
							resultMap.put("invalidEmailAddresses", invalidEmailIds);
						}
						if (validateMap != null && validateMap.get("statusCode") != null
								&& ((Integer) validateMap.get("statusCode")).intValue() == 200) {
							responseDTOList = new ArrayList<>();
							List<User> sendPartnerMailsList = new ArrayList<>();

							if (!userList.isDefaultPartnerList()) {
								defaultPartnerList = userListDAO.getDefaultPartnerList(userCompany.getId());
								userListOperationsAsyncDTO.setDefaultPartnerListId(defaultPartnerList.getId());
							}

							Set<UserDTO> nonExistingUsers = partnershipServiceHelper.getNonExistingUserList(partners);
							Set<UserDTO> existingUsers = partnershipServiceHelper.getExistingUserList(partners);

							boolean isGdprOn = gdprSettingService.isGdprEnabled(userListCompany.getId());

							UserList inActiveMasterPartnerList = null;
							UserList activeMasterPartnerList = null;
							List<String> inActivePartners = new ArrayList<>();
							List<String> activePartners = new ArrayList<>();
							/* Processing Non Existing Users */
							List<AddPartnerResponseDTO> nonExistingResponse = partnershipServiceHelper
									.processNonExistingUserList(nonExistingUsers, userList, loggedInUser,
											defaultPartnerList, sendPartnerMailsList, isGdprOn,
											inActiveMasterPartnerList, activeMasterPartnerList, inActivePartners,
											activePartners, userListOperationsAsyncDTO);
							if (nonExistingResponse != null) {
								responseDTOList.addAll(nonExistingResponse);
							}
							/* Processing Existing Users */
							List<AddPartnerResponseDTO> existingResponse = partnershipServiceHelper
									.processExistingUserList(existingUsers, userList, loggedInUser, defaultPartnerList,
											sendPartnerMailsList, isGdprOn, inActiveMasterPartnerList,
											activeMasterPartnerList, inActivePartners, activePartners, userId,
											userListOperationsAsyncDTO);
							if (existingResponse != null) {
								responseDTOList.addAll(existingResponse);
							}
							userList.initialiseCommonFields(false, loggedInUser.getUserId());
							userList.setEmailValidationInd(true);
							sendEmailNotificationsToPartnersAndVendors(userList, loggedInUser, sendPartnerMailsList);

							Set<UserDTO> allPartners = new HashSet<>();
							allPartners.addAll(existingUsers);
							allPartners.addAll(nonExistingUsers);
							userListOperationsAsyncDTO.getPartners().addAll(allPartners);
							userListOperationsAsyncDTO.getAllPartners().addAll(allPartners);
							/**************** XNFR-98 ***************/
							createOrUpateTeamMemberMasterPartnerList(userId, loggedInUser, allPartners,
									userList.getId(), userListOperationsAsyncDTO);
						}
					}
				}
			}
		}
		resultMap.put("responseDTOList", responseDTOList);
		return resultMap;
	}

	private void sendEmailNotificationsToPartnersAndVendors(UserList userList, User loggedInUser,
			List<User> sendPartnerMailsList) {
		if (!sendPartnerMailsList.isEmpty()) {
			sendEmailNotificationsToOnBoardedPartners(userList, loggedInUser, sendPartnerMailsList);
			/*** XNFR-506 ****/
			boolean partnerOnBoardVendorEmailNotification = loggedInUser.getCompanyProfile()
					.isPartnerOnBoardVendorEmailNotification();
			if (partnerOnBoardVendorEmailNotification) {
				String debugMessage = "Sending email notification to vendor about onboared partner " + new Date();
				Logger.debug(debugMessage);
				sendEmailNotificationToVendorAboutOnBoardedPartners(userList, loggedInUser);
			} else {
				String debugMessage = "No email notifications will be sent to vendor about onboared partner "
						+ new Date();
				Logger.debug(debugMessage);
			}

		}
	}

	private void sendEmailNotificationToVendorAboutOnBoardedPartners(UserList userList, User loggedInUser) {
		boolean isSignedUpUsingVendorLink = userList.isSignUpUsingVendorLink();
		if (!isSignedUpUsingVendorLink && !userList.isLoginUsingSAMLSSO() && !userList.isLoginUsingOauthSSO()) {
			partnershipServiceHelper.sendPartnerListUpdatedMail(loggedInUser, userList);
		}
	}

	private void sendEmailNotificationsToOnBoardedPartners(UserList userList, User loggedInUser,
			List<User> sendPartnerMailsList) {
		asyncComponent.sendPartnerMail(sendPartnerMailsList, EmailConstants.PRM_PARTNER_EMAIL, loggedInUser,
				userList.getId());
	}

	/****************
	 * XNFR-98 *
	 ***************/
	private void createOrUpateTeamMemberMasterPartnerList(Integer userId, User loggedInUser, Set<UserDTO> allPartners,
			Integer userListId, UserListOperationsAsyncDTO userListOperationsAsyncDTO) {
		Set<Set<Integer>> teamMemberGroupIdsInSet = allPartners.stream()
				.filter(u -> (u.getSelectedTeamMemberIds() != null && !u.getSelectedTeamMemberIds().isEmpty()))
				.map(UserDTO::getSelectedTeamMemberIds).collect(Collectors.toSet());
		List<Integer> selectedTeamMemberGroupUserMappingIds = new ArrayList<>();
		for (Set<Integer> teamMemberGroupIdInSet : teamMemberGroupIdsInSet) {
			for (Integer teamMemberGroupMappingId : teamMemberGroupIdInSet) {
				selectedTeamMemberGroupUserMappingIds.add(teamMemberGroupMappingId);
				UserList teamMemberUserList = new UserList();
				boolean createUserList = false;
				AdminAndTeamMemberDetailsDTO adminAndTeamMemberDetailsDTO = teamDao
						.getTeamMemberPartnerMasterListName(teamMemberGroupMappingId);
				Integer teamMemberId = adminAndTeamMemberDetailsDTO.getId();
				Integer teamMemberPartnerListId = userListDAO.findUserListIdByTeamMemberId(teamMemberId,
						loggedInUser.getCompanyProfile().getId());
				if (teamMemberPartnerListId != null) {
					createUserList = false;
					teamMemberUserList.setId(teamMemberPartnerListId);
					userListOperationsAsyncDTO.getPartnerListIds().add(teamMemberPartnerListId);
				} else {
					String partnerListName = adminAndTeamMemberDetailsDTO.getFullName() + "-Master Partner Group";
					createUserList = true;
					teamMemberUserList.setName(partnerListName);
					TeamMember teamMember = new TeamMember();
					teamMember.setId(teamMemberId);
					XamplifyUtils.setTeamMemberUserList(userId, loggedInUser, teamMember, teamMemberUserList);
				}

				iteratePartnersAndSaveUserList(allPartners, teamMemberGroupMappingId, teamMemberUserList,
						createUserList, teamMemberPartnerListId, userListOperationsAsyncDTO);

			}
		}

		/********** Publish DAM **************/
		addPartnerListIds(allPartners, userListId, userListOperationsAsyncDTO);

		/*********
		 * Delete From Team Member Master Partner Group And Legal Basis
		 ************/
		deleteFromUserUserList(loggedInUser, allPartners, teamMemberGroupIdsInSet);

	}

	private void addPartnerListIds(Set<UserDTO> allPartners, Integer userListId,
			UserListOperationsAsyncDTO userListOperationsAsyncDTO) {
		if (userListOperationsAsyncDTO.getPartnerListIds() != null
				&& !userListOperationsAsyncDTO.getPartnerListIds().isEmpty()) {
			for (UserDTO partner : allPartners) {
				UserDTO partnerDto = new UserDTO();
				partnerDto.setEmailId(partner.getEmailId());
				userListOperationsAsyncDTO.getPartners().add(partnerDto);
			}
		}
		userListOperationsAsyncDTO.getPartnerListIds().add(userListId);
		// userListOperationsAsyncDTO.getAllPartnerListIds().add(userListId);
	}

	private void iteratePartnersAndSaveUserList(Set<UserDTO> allPartners, Integer teamMemberGroupMappingId,
			UserList teamMemberUserList, boolean createUserList, Integer teamMemberPartnerListId,
			UserListOperationsAsyncDTO userListOperationsAsyncDTO) {
		Set<UserUserList> teamMemberUserUserLists = new HashSet<>();
		for (UserDTO partnerDTO : allPartners) {
			List<Integer> selectedTeamMemberGroupIds = XamplifyUtils
					.convertSetToList(partnerDTO.getSelectedTeamMemberIds());
			if (selectedTeamMemberGroupIds.indexOf(teamMemberGroupMappingId) > -1) {
				addOrUpdateUserUserLists(teamMemberUserList, teamMemberUserUserLists, partnerDTO, createUserList,
						teamMemberPartnerListId, userListOperationsAsyncDTO);
			}
		}
		if (createUserList) {
			if (!teamMemberUserUserLists.isEmpty()) {
				teamMemberUserList.setUserUserLists(teamMemberUserUserLists);
			}
			genericDAO.save(teamMemberUserList);
			userListOperationsAsyncDTO.getAllPartnerListIds().add(teamMemberUserList.getId());
		}
	}

	private void deleteFromUserUserList(User loggedInUser, Set<UserDTO> allPartners,
			Set<Set<Integer>> teamMemberGroupIdsInSet) {
		if (teamMemberGroupIdsInSet.isEmpty()) {
			Integer companyId = loggedInUser.getCompanyProfile().getId();
			List<Integer> teamMemberPartnerListIds = userListDAO.findTeamMemberPartnerListIdsByCompanyId(companyId);
			if (teamMemberPartnerListIds != null && !teamMemberPartnerListIds.isEmpty()) {
				/******* Check if user is part of the user user list table **********/
				List<Integer> partnerIds = new ArrayList<>();
				for (UserDTO partner : allPartners) {
					partnerIds.add(partner.getId());
				}
				userListDAO.deleteFromUserUserListByUserIdAndUserListIds(partnerIds, teamMemberPartnerListIds);
			}
		}
	}

	private void addOrUpdateUserUserLists(UserList teamMemberUserList, Set<UserUserList> teamMemberUserUserLists,
			UserDTO partnerDTO, boolean createUserList, Integer userListId,
			UserListOperationsAsyncDTO userListOperationsAsyncDTO) {
		User partner = new User();
		Integer partnerId = partnerDTO.getId();
		partner.setUserId(partnerId);
		if (createUserList) {
			createTeamMemberPartnerList(teamMemberUserList, teamMemberUserUserLists, partnerDTO, partner, false);
		} else {
			boolean isPartnerExistsTeamMemberPartnerList = userListDAO.isPartnerExistsInPartnerList(partnerId,
					userListId);
			if (isPartnerExistsTeamMemberPartnerList) {
				updateTeamMemberPartnerList(teamMemberUserList, partnerDTO, userListId, partner);
			} else {
				createTeamMemberPartnerList(teamMemberUserList, teamMemberUserUserLists, partnerDTO, partner, true);
				userListOperationsAsyncDTO.getAllPartnerListIds().add(teamMemberUserList.getId());
			}
		}
	}

	private void createTeamMemberPartnerList(UserList teamMemberUserList, Set<UserUserList> teamMemberUserUserLists,
			UserDTO partnerDTO, User partner, boolean masterPartnerListExists) {
		UserUserList userUserList = new UserUserList();
		userUserList.setUser(partner);
		userUserList.setUserList(teamMemberUserList);
		userUserList.setCountry(partnerDTO.getCountry());
		userUserList.setCity(partnerDTO.getCity());
		userUserList.setAddress(partnerDTO.getAddress());
		userUserList.setContactCompany(partnerDTO.getContactCompany());
		userUserList.setJobTitle(partnerDTO.getJobTitle());
		userUserList.setFirstName(partnerDTO.getFirstName());
		userUserList.setLastName(partnerDTO.getLastName());
		userUserList.setMobileNumber(partnerDTO.getMobileNumber());
		userUserList.setState(partnerDTO.getState());
		userUserList.setZipCode(partnerDTO.getZipCode());
		userUserList.setVertical(partnerDTO.getVertical());
		userUserList.setRegion(partnerDTO.getRegion());
		userUserList.setPartnerType(partnerDTO.getPartnerType());
		userUserList.setCategory(partnerDTO.getCategory());
		List<LegalBasis> legalBasisList = new ArrayList<>();
		for (Integer legalBasisId : partnerDTO.getLegalBasis()) {
			LegalBasis legalBasis = new LegalBasis();
			legalBasis.setId(legalBasisId);
			legalBasisList.add(legalBasis);
		}
		userUserList.setLegalBasis(legalBasisList);
		if (!masterPartnerListExists) {
			teamMemberUserUserLists.add(userUserList);
		} else {
			userListDAO.saveUserUserList(userUserList);
			userListDAO.updateTeamMemberPartnerList(teamMemberUserList.getId());
		}

	}

	private void updateTeamMemberPartnerList(UserList teamMemberUserList, UserDTO partnerDTO, Integer userListId,
			User partner) {
		UserList userList = genericDAO.get(UserList.class, userListId);
		Set<UserUserList> userUserLists = userList.getUserUserLists();
		for (UserUserList userUserList : userUserLists) {
			if (userUserList.getUser() != null && partner != null && userUserList.getUserList() != null
					&& userUserList.getUser().getUserId().intValue() == partner.getUserId().intValue()
					&& userUserList.getUserList().getId().intValue() == userListId.intValue()) {
				userUserList.setFirstName(partnerDTO.getFirstName());
				userUserList.setLastName(partnerDTO.getLastName());
				userUserList.setContactCompany(partnerDTO.getContactCompany());
				userUserList.setJobTitle(partnerDTO.getJobTitle());
				userUserList.setAddress(partnerDTO.getAddress());
				userUserList.setCity(partnerDTO.getCity());
				userUserList.setMobileNumber(partnerDTO.getMobileNumber());
				userUserList.setDescription(partnerDTO.getDescription());
				userUserList.setCountry(partnerDTO.getCountry());
				userUserList.setState(partnerDTO.getState());
				userUserList.setZipCode(partnerDTO.getZipCode());
				userUserList.setVertical(partnerDTO.getVertical());
				userUserList.setRegion(partnerDTO.getRegion());
				userUserList.setPartnerType(partnerDTO.getPartnerType());
				userUserList.setCategory(partnerDTO.getCategory());
				updateLegalBasis(userUserList, partnerDTO.getLegalBasis());
				break;
			}
		}
		userListDAO.updateTeamMemberPartnerList(teamMemberUserList.getId());
	}

	@SuppressWarnings("null")
	private void updateLegalBasis(UserUserList userUserList, List<Integer> newLegalBasisList) {
		if (userUserList != null && newLegalBasisList != null && !newLegalBasisList.isEmpty()) {
			List<LegalBasis> existingLegalBaseList = userUserList.getLegalBasis();
			List<LegalBasis> removeLegalBasis = new ArrayList<>();
			if (existingLegalBaseList != null && !existingLegalBaseList.isEmpty()) {
				for (LegalBasis legalBasis : existingLegalBaseList) {
					if (legalBasis != null) {
						if (!newLegalBasisList.contains(legalBasis.getId())) {
							removeLegalBasis.add(legalBasis);
						} else {
							newLegalBasisList.remove(legalBasis.getId());
						}
					}
				}
			}
			existingLegalBaseList.removeAll(removeLegalBasis);

			for (Integer legalBasisId : newLegalBasisList) {
				LegalBasis legalBasis = new LegalBasis();
				legalBasis.setId(legalBasisId);
				existingLegalBaseList.add(legalBasis);
			}
		}
	}

	@Override
	public XtremandResponse saveAsPartnerList(Set<UserDTO> partners, Integer userId, UserListDTO userListDTO,
			XtremandResponse xtremandResponse, String csvPath) {
		String userListName = userListDTO.getName();
		if (userListName != null && userListName.trim() != "" && userId != null && userId > 0
				&& ((partners != null && !partners.isEmpty()) || (userListDTO.isCopyList()))) {
			User loggedInUser = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, userId)),
					new FindLevel[] { FindLevel.COMPANY_PROFILE });
			if (loggedInUser != null) {
				CompanyProfile company = loggedInUser.getCompanyProfile();
				if (company != null) {
					boolean validateListName = userListDAO.validateListName(userListName, company.getId(), "PARTNERS");
					if (validateListName) {
						// create new partner list
						UserList createdList = userListService.createPartnerList(loggedInUser, userListName, false,
								csvPath);
						createdList.setUploadInProgress(true);
						xtremandResponse.setStatusCode(200);
						asyncComponent.addPartnerToList(partners, createdList, company.getId(), userListDTO);
					} else {
						xtremandResponse.setStatusCode(401);
						xtremandResponse.setMessage("list name already exists");
					}

				}
			}
		}
		return xtremandResponse;
	}

	public Partnership getPartnershipByPartnerComapny(CompanyProfile partnerCompany, CompanyProfile vendorCompany) {
		return partnershipDAO.getPartnershipByPartnerCompany(partnerCompany, vendorCompany);
	}

	public Partnership getPartnershipByRepresentingPartner(User representingPartner, CompanyProfile vendorCompany) {
		return partnershipDAO.getPartnershipByRepresentingPartner(representingPartner, vendorCompany);
	}

	@Override
	public List<UserList> getAllPartnerLists(Integer companyId) {
		Criteria criteria1 = new Criteria("company.id", OPERATION_NAME.eq, companyId);
		Criteria criteria2 = new Criteria("isPartnerUserList", OPERATION_NAME.eq, true);
		List<Criteria> criteriasObj = new ArrayList<Criteria>();
		criteriasObj.add(criteria1);
		criteriasObj.add(criteria2);
		return (List<UserList>) userListDAO.find(criteriasObj, new FindLevel[] { FindLevel.SHALLOW });
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse deletePartnersFromUserList(UserList userList, Integer customerId,
			List<Integer> removePartnerIds) {
		XtremandResponse xtremandResponse = new XtremandResponse();
		if (userList != null && customerId != null && customerId > 0 && removePartnerIds != null
				&& !removePartnerIds.isEmpty()) {
			User loggedinUser = userService.findByPrimaryKey(customerId,
					new FindLevel[] { FindLevel.COMPANY_PROFILE, FindLevel.ROLES });
			List<User> removePartnersList = new ArrayList<>();
			for (Integer userId : removePartnerIds) {
				User removePartner = userService.findByPrimaryKey(userId,
						new FindLevel[] { FindLevel.COMPANY_PROFILE, FindLevel.ROLES });
				removePartnersList.add(removePartner);
			}
			if (userList.isDefaultPartnerList()) {
				Integer companyId = loggedinUser.getCompanyProfile().getId();
				List<Integer> partnerListIds = userListDAO.getUserListIdsByRemovePartnerIds(companyId,
						removePartnerIds);
				userListDAO.deleteShareList(customerId, removePartnerIds);
				partnershipServiceHelper.deletePartnersFromPartnerLists(removePartnerIds, partnerListIds);
				userDAO.deletePartnerData(removePartnerIds, loggedinUser.getCompanyProfile().getId());
				partnershipServiceHelper.deletePartnership(removePartnersList, loggedinUser.getCompanyProfile());
				partnershipServiceHelper.deletePartnerRoleAndUpdateModules(removePartnersList,
						loggedinUser.getCompanyProfile().getId());
				Set<Integer> partnerListIdsSet = partnerListIds.stream().collect(Collectors.toSet());
				xtremandResponse.setData(partnerListIdsSet);
			} else {
				List<Integer> userListIds = new ArrayList<>();
				userListIds.add(userList.getId());
				if (XamplifyUtils.isNotEmptyList(removePartnerIds) && XamplifyUtils.isNotEmptyList(userListIds)) {
					userListDAO.deletePartnersFromPartnerLists(removePartnerIds, userListIds);
				}
				Set<Integer> partnerListIdsSet = userListIds.stream().collect(Collectors.toSet());
				xtremandResponse.setData(partnerListIdsSet);
			}
		}
		return xtremandResponse;
	}

	@Override
	public void deletePartnerList(UserList userList) {
		if (userList != null) {
			/************ XNFR-98 ************/
			if (!userList.isDefaultPartnerList() && !userList.isTeamMemberPartnerList()) {
				userListDAO.deleteByPrimaryKey(userList.getId());
			}
			if (userList.isTeamMemberPartnerList() && !userList.isDeleteTeamMemberPartnerList()) {
				userListDAO.deleteUserUserListByUserListId(userList.getId());
			} else if (userList.isTeamMemberPartnerList() && userList.isDeleteTeamMemberPartnerList()) {
				userListDAO.deleteByPrimaryKey(userList.getId());
			}
			lmsDao.clearVisbilityOrphans();
			lmsDao.unPublishLearningTracksWithEmptyVisbility();
			damDao.unPublishDamAssets();
		}
	}

	@Override
	public XtremandResponse sendVendorInvitation(Integer senderId, VendorInvitationDTO vendorInvitationDTO) {
		XtremandResponse xtremandResponse = new XtremandResponse();
		List<AddPartnerResponseDTO> responseDTOList = new ArrayList<AddPartnerResponseDTO>();
		List<User> sendVendorMailsList = new ArrayList<User>();

		User sender = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, senderId)),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });

		List<String> emailIds = vendorInvitationDTO.getEmailIds();
		Set<UserDTO> vendors = new HashSet<UserDTO>();
		for (String emailId : emailIds) {
			UserDTO user = new UserDTO();
			user.setEmailId(emailId.toLowerCase());
			vendors.add(user);
		}
		Set<UserDTO> nonExistingUsers = partnershipServiceHelper.getNonExistingUserList(vendors);
		Set<UserDTO> existingUsers = partnershipServiceHelper.getExistingUserList(vendors);

		List<AddPartnerResponseDTO> nonExistingResponse = partnershipServiceHelper
				.processNonExistingVendors(nonExistingUsers, sender, sendVendorMailsList);
		if (nonExistingResponse != null) {
			responseDTOList.addAll(nonExistingResponse);
		}

		List<AddPartnerResponseDTO> existingResponse = partnershipServiceHelper.processExistingVendors(existingUsers,
				sender, sendVendorMailsList);
		if (existingResponse != null) {
			responseDTOList.addAll(existingResponse);
		}

		if (!sendVendorMailsList.isEmpty()) {
			asyncComponent.sendVendorInvitation(vendorInvitationDTO, sender, sendVendorMailsList);
		}

		if (responseDTOList.size() == 1) {
			AddPartnerResponseDTO addPartnerResponseDTO = responseDTOList.get(0);
			if (addPartnerResponseDTO.getStatus().equals("VENDOR_INVITATION_SKIPPED")) {
				xtremandResponse.setStatusCode(417);
			} else {
				xtremandResponse.setStatusCode(200);
			}

		} else {
			xtremandResponse.setStatusCode(200);
		}

		asyncComponent.sendVendorInvitationEmailToSuperAdmin(vendorInvitationDTO, sender);
		xtremandResponse.setData(responseDTOList);

		return xtremandResponse;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getApprovalSectionPartnersDetails(Pagination pagination) {
		pagination.setCompanyId(userDAO.getCompanyIdByUserId(pagination.getUserId()));
		Map<String, Object> resultMap = partnershipDAO.getApprovalSectionPartnersDetails(pagination);
		List<Object[]> list = (List<Object[]>) resultMap.get("approvePartnerList");
		List<PartnerDTO> partnerDTOList = new ArrayList<PartnerDTO>();
		for (Object[] row : list) {
			PartnerDTO partnerDTO = new PartnerDTO();
			partnerDTO.setPartnerId((Integer) row[0]);
			partnerDTO.setFirstName((String) row[1]);
			partnerDTO.setLastName((String) row[2]);
			partnerDTO.setFullName((String) row[3]);
			partnerDTO.setEmailId((String) row[4]);
			partnerDTO.setPartnerCompanyName((String) row[5]);
			partnerDTO.setInvitationStatus((String) row[6]);
			partnerDTOList.add(partnerDTO);
		}
		resultMap.put("approvePartnerList", partnerDTOList);
		return resultMap;
	}

	@Override
	public XtremandResponse approvePartner(Integer vendorId, Integer partnerId, VendorInvitationDTO vendorInvitationDTO,
			UserListOperationsAsyncDTO userListOperationsAsyncDTO) {
		XtremandResponse response = new XtremandResponse();
		User vendor = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, vendorId)),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		Integer vendorCompanyId = vendor.getCompanyProfile().getId();
		User partner = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, partnerId)),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		List<String> deactivatedDomains = partnershipDAO.findDeactivatedDomainsByCompanyId(vendorCompanyId);
		String partnerEmail = partner.getEmailId();
		String domain = partnerEmail.substring(partnerEmail.indexOf("@") + 1);
		boolean isDomainDeactivated = deactivatedDomains.contains(domain);
		if (isDomainDeactivated) {
			response.setMessage("The domain has been deactivated, and the partner cannot be approved.");
			response.setStatusCode(401);
		} else {
			UserList defaultPartnerList = userListService.getDefaultPartnerList(vendor);
			partnershipServiceHelper.addPartnerToListThroughVendorInvitation(partner, defaultPartnerList);
			/*********** XNFR-81 *************/
			insertIntoInActiveMasterPartnerList(partnerId, vendorCompanyId, partner);
			/*********** XNFR-81 *************/
			Partnership partnership = partnershipDAO.getPartnershipByPartnerCompany(partner.getCompanyProfile(),
					vendor.getCompanyProfile());
			partnership.setStatus(PartnershipStatus.APPROVED);
			partnership.setUpdatedTime(new Date());
			partnershipServiceHelper.addPartnershipStatusHistory(partnership, PartnershipStatus.APPROVED,
					vendor.getUserId());
			partnershipServiceHelper.addPartnerRoleAndUpdateModules(partner, vendor);
			/***** XBI-4042 *****/
			partnershipServiceHelper.addModuleCustomsForPartner(partnership, partner);
			List<User> sendPartnerMailsList = new ArrayList<>();
			partnershipServiceHelper.populateMailList(sendPartnerMailsList, partner);
			asyncComponent.sendVendorInvitation(vendorInvitationDTO, vendor, sendPartnerMailsList);
			response.setMessage("Status has been changed to approved successfully");
			response.setStatusCode(200);
			prepareInputForShareAssets(partner, vendorCompanyId, defaultPartnerList, userListOperationsAsyncDTO);
		}
		return response;
	}

	private void insertIntoInActiveMasterPartnerList(Integer partnerId, Integer vendorCompanyId, User partner) {
		UserList inActiveMasterPartnerList = userListDAO.findInActivePartnerListByCompanyId(vendorCompanyId);
		if (inActiveMasterPartnerList != null) {
			boolean isPartnerExistsInInActiveMasterPartnerList = utilDao.isPartnerAddedToInActivePartnerList(partnerId,
					vendorCompanyId);
			if (!isPartnerExistsInInActiveMasterPartnerList) {
				partnershipServiceHelper.addPartnerToListThroughVendorInvitation(partner, inActiveMasterPartnerList);
			}
		}
	}

	@Override
	public XtremandResponse declinePartner(Integer vendorId, Integer partnerId,
			VendorInvitationDTO vendorInvitationDTO) {
		XtremandResponse response = new XtremandResponse();
		User partner = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, partnerId)),
				new FindLevel[] { FindLevel.SHALLOW });
		User vendor = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, vendorId)),
				new FindLevel[] { FindLevel.SHALLOW });
		Partnership partnership = partnershipDAO.getPartnershipByPartnerCompany(partner.getCompanyProfile(),
				vendor.getCompanyProfile());
		partnership.setStatus(PartnershipStatus.DECLINED);
		partnership.setUpdatedTime(new Date());
		partnershipServiceHelper.addPartnershipStatusHistory(partnership, PartnershipStatus.DECLINED,
				vendor.getUserId());
		List<User> sendPartnerMailsList = new ArrayList<>();
		partnershipServiceHelper.populateMailList(sendPartnerMailsList, partner);
		asyncComponent.sendVendorInvitation(vendorInvitationDTO, vendor, sendPartnerMailsList);
		response.setMessage("Status has been changed to declined successfully");
		response.setStatusCode(200);
		return response;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> referVendorAnalytics(Pagination pagination) {
		Map<String, Object> resultMap = new HashMap<>();
		User loggedInUser = userService.loadUser(
				Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, pagination.getUserId())),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		if (loggedInUser != null) {
			CompanyProfile partnerCompany = loggedInUser.getCompanyProfile();
			if (partnerCompany != null) {
				PartnershipStatus status = getFilterBy(pagination.getFilterBy());
				Map<String, Object> map = partnershipDAO.getAllPartnershipsByPartnerCompany(partnerCompany,
						PartnershipSource.VENDOR_INVITATION, pagination, status);
				List<Partnership> partnerships = (List<Partnership>) map.get("data");
				resultMap.put("totalRecords", map.get("totalRecords"));
				resultMap.put("referredVendors", partnershipServiceHelper.getReferredVendorListDTO(partnerships));
			}
		}
		return resultMap;
	}

	public PartnershipStatus getFilterBy(String filterBy) {
		PartnershipStatus status = null;

		switch (filterBy) {
		case "INVITED":
			status = PartnershipStatus.INVITED;
			break;
		case "APPROVED":
			status = PartnershipStatus.APPROVED;
			break;
		case "DECLINED":
			status = PartnershipStatus.DECLINED;
			break;
		case "ALL":
			status = null;
			break;
		}
		return status;
	}

	@Override
	public Map<String, Object> vendorInvitationsCount(Integer partnerId) {
		Map<String, Object> resultMap = new HashMap<>();
		User loggedInUser = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, partnerId)),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		if (loggedInUser != null) {
			CompanyProfile partnerCompany = loggedInUser.getCompanyProfile();
			if (partnerCompany != null) {
				List<PartnershipStatus> partnershipStatusList = getpartnershipStatusList();
				for (PartnershipStatus status : partnershipStatusList) {
					Long totalRecords = partnershipDAO.getAllPartnershipsCountByPartnerCompany(partnerCompany,
							PartnershipSource.VENDOR_INVITATION, status);
					if (status != null) {
						resultMap.put(status.name(), totalRecords);
					} else {
						resultMap.put("ALL", totalRecords);
					}
				}
			}
		}
		return resultMap;
	}

	private List<PartnershipStatus> getpartnershipStatusList() {
		List<PartnershipStatus> partnershipStatusList = new ArrayList<>();
		partnershipStatusList.add(PartnershipStatus.INVITED);
		partnershipStatusList.add(PartnershipStatus.DECLINED);
		partnershipStatusList.add(PartnershipStatus.APPROVED);
		partnershipStatusList.add(null);
		return partnershipStatusList;
	}

	@Override
	public XtremandResponse findPartnerCompanies(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		findExistingPartnershipIds(pagination);
		response.setData(partnershipDAO.findPartnerCompanies(pagination));
		response.setStatusCode(200);
		return response;
	}

	private void findExistingPartnershipIds(Pagination pagination) {
		List<Integer> findPublishedPartnershipIds = new ArrayList<>();
		if ("lms".equals(pagination.getType())) {
			findPublishedPartnershipIds = findPublishedPartnershipIdsForLMS(pagination, findPublishedPartnershipIds);
		} else if ("dam".equals(pagination.getType())) {
			findPublishedPartnershipIds = findPublishedPartnershipIdsForDAM(pagination);
		} else if ("Dashboard Buttons".equals(pagination.getType())) {
			findPublishedPartnershipIds = vanityUrlDao
					.findPublishedPartnershipIdsByDashboardButtonId(pagination.getCampaignId());
		}
		if (XamplifyUtils.isNotEmptyList(findPublishedPartnershipIds)) {
			pagination.setFiltertedEmailTempalteIds(findPublishedPartnershipIds);
		}
	}

	private List<Integer> findPublishedPartnershipIdsForDAM(Pagination pagination) {
		List<Integer> findPublishedPartnershipIds;
		findPublishedPartnershipIds = damDao
				.findPartnershipIdsForPartnerCompaniesOptionByDamId(pagination.getCampaignId());
		return findPublishedPartnershipIds;
	}

	private List<Integer> findPublishedPartnershipIdsForLMS(Pagination pagination,
			List<Integer> findPublishedPartnershipIds) {
		if (pagination.getLearningTrackId() != null && pagination.getLearningTrackId() > 0) {
			findPublishedPartnershipIds = lmsDao.getAllPartnershipIds(pagination.getLearningTrackId());
		}
		return findPublishedPartnershipIds;
	}

	@Override
	public XtremandResponse findPartnerGroups(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		findPublishedPartnerGroupIds(pagination);
		response.setData(partnershipDAO.findPartnerGroups(pagination));
		response.setStatusCode(200);
		return response;
	}

	private void findPublishedPartnerGroupIds(Pagination pagination) {
		List<Integer> findPublishedPartnerGroupIds = new ArrayList<>();
		if ("lms".equals(pagination.getType())) {
			findPublishedPartnerGroupIds = findPublishedPartnerGroupIdsForLMS(pagination, findPublishedPartnerGroupIds);
		} else if ("dam".equals(pagination.getType())) {
			findPublishedPartnerGroupIds = damDao.findPublishedPartnerGroupIdsByDamId(pagination.getCampaignId());
		} else if ("Dashboard Buttons".equals(pagination.getType())) {
			findPublishedPartnerGroupIds = vanityUrlDao
					.findPublishedPartnerGroupIdsByDashboardButtonId(pagination.getCampaignId());
		}
		if (XamplifyUtils.isNotEmptyList(findPublishedPartnerGroupIds)) {
			pagination.setFiltertedEmailTempalteIds(findPublishedPartnerGroupIds);
		}
	}

	private List<Integer> findPublishedPartnerGroupIdsForLMS(Pagination pagination,
			List<Integer> findPublishedPartnerGroupIds) {
		if (pagination.getLearningTrackId() != null && pagination.getLearningTrackId() > 0) {
			findPublishedPartnerGroupIds = lmsDao.getAllGroupIds(pagination.getLearningTrackId());
		}
		return findPublishedPartnerGroupIds;
	}

	@SuppressWarnings("unchecked")
	@Override
	public XtremandResponse findVendorInvitationReports(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		Map<String, Object> map = partnershipDAO.findVendorInvitationReports(pagination);
		List<VendorInvitationReport> vendorInvitationReports = (List<VendorInvitationReport>) map.get("list");
		String imagesPrefixPath = xamplifyUtil.getImagesPrefixPath();
		for (VendorInvitationReport vendorInvitationReport : vendorInvitationReports) {
			if (StringUtils.hasText(vendorInvitationReport.getInviteeCompanyLogo())) {
				vendorInvitationReport
						.setInviteeCompanyLogo(imagesPrefixPath + vendorInvitationReport.getInviteeCompanyLogo());
			} else {
				vendorInvitationReport.setInviteeCompanyLogo("-");
			}
			vendorInvitationReport
					.setInviterCompanyLogo(imagesPrefixPath + vendorInvitationReport.getInviterCompanyLogo());
			vendorInvitationReport.setInviteeRole(utilDao.findRoleByUserId(vendorInvitationReport.getInviteeId()));
		}
		response.setData(map);
		response.setStatusCode(200);
		return response;
	}

	@Override
	public XtremandResponse findPartnerCompanies(Pagination pagination, Integer userId) {
		XtremandResponse response = new XtremandResponse();
		Integer vendorCompanyId = userDAO.getCompanyIdByUserId(userId);
		pagination.setVendorCompanyId(vendorCompanyId);
		response.setData(partnershipDAO.findPartnerCompanies(pagination));
		response.setStatusCode(200);
		return response;
	}

	public void deletePartnersFromDefaultUserList(UserList userList, Integer customerId,
			List<Integer> removePartnerIds) {
		if (userList != null && customerId != null && customerId > 0 && removePartnerIds != null
				&& !removePartnerIds.isEmpty()) {
			User loggedinUser = userService.findByPrimaryKey(customerId,
					new FindLevel[] { FindLevel.COMPANY_PROFILE, FindLevel.ROLES });
			List<User> removePartnersList = new ArrayList<>();
			for (Integer userId : removePartnerIds) {
				User removePartner = userService.findByPrimaryKey(userId,
						new FindLevel[] { FindLevel.COMPANY_PROFILE, FindLevel.ROLES });
				removePartnersList.add(removePartner);
			}
			if (userList.isDefaultPartnerList()) {
				Collection<UserList> allPartnerLists = getAllPartnerLists(loggedinUser.getCompanyProfile().getId());
				List<Integer> activeAndInActiveMasterPartnerListIds = new ArrayList<>();
				for (UserList userListObj : allPartnerLists) {
					boolean isActiveMasterPartnerList = userListObj.getName().equals(activePartnerListName);
					boolean isInActiveMasterPartnerList = userListObj.getName().equals(inactivePartnerListName);
					boolean isTeamMemberPartnerList = userListObj.isTeamMemberPartnerList();
					if (!userListObj.isDefaultPartnerList() && !isActiveMasterPartnerList
							&& !isInActiveMasterPartnerList && !isTeamMemberPartnerList) {
						deletePartnerList(userListObj);
					}
					if (isActiveMasterPartnerList || isInActiveMasterPartnerList) {
						activeAndInActiveMasterPartnerListIds.add(userListObj.getId());
					}

				}
				lmsDao.clearVisbilityOrphans();
				lmsDao.unPublishLearningTracksWithEmptyVisbility();
				List<Integer> userListIds = new ArrayList<>();
				userListIds.add(userList.getId());
				/**** XNFR-81 **********/
				userListIds.addAll(activeAndInActiveMasterPartnerListIds);
				if (XamplifyUtils.isNotEmptyList(removePartnerIds) && XamplifyUtils.isNotEmptyList(userListIds)) {
					userListDAO.deletePartnersFromPartnerLists(removePartnerIds, userListIds);
				}
				userListDAO.deleteShareList(customerId, removePartnerIds);
				userDAO.deletePartnerData(removePartnerIds, loggedinUser.getCompanyProfile().getId());
				partnershipServiceHelper.deletePartnership(removePartnersList, loggedinUser.getCompanyProfile());
				partnershipServiceHelper.deletePartnerRoleAndUpdateModules(removePartnersList,
						loggedinUser.getCompanyProfile().getId());

			}
		}
	}

	@Override
	public void publishDAMAndLMSToNewlyAddedPartners(Set<Integer> userListIds, Integer loggedInUserId,
			Set<UserDTO> allPartners) {
		User loggedInUser = userService.loadUser(
				Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, loggedInUserId)),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		/********* Share DAM Content To This Users *************************/
		asyncComponent.publishDamToTeamMemberPartnerListPartners(userListIds, loggedInUser, allPartners);

		/********* Share LMS Content To This Users (Swathi) *************************/
		asyncComponent.publishLMSToNewUsersInUserList(userListIds, loggedInUser, allPartners);
	}

	@Override
	public XtremandResponse findPartnerCompaniesForSharingWhiteLabeledContent(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		findExistingPartnershipIds(pagination);
		response.setData(partnershipDAO.findPartnerCompaniesForSharingWhiteLabeledContent(pagination));
		response.setStatusCode(200);
		return response;
	}

	@Override
	public List<String> getExistingPartnerCompanyNamesByVendorCompanyId(Integer vendorCompanyId) {
		return partnershipDAO.getExistingPartnerCompanyNamesByVendorCompanyId(vendorCompanyId);
	}

	public XtremandResponse findVendorCompanies(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		findExistingPartnershipIds(pagination);
		response.setData(partnershipDAO.findVendorCompaniesPagination(pagination, null));
		response.setStatusCode(200);
		return response;
	}

	public void prepareInputForShareAssets(User partner, Integer vendorCompanyId, UserList defaultPartnerList,
			UserListOperationsAsyncDTO userListOperationsAsyncDTO) {
		UserDTO userDTO = new UserDTO();
		userDTO.setExistingUser(true);
		userDTO.setId(partner.getUserId());
		userDTO.setAlias(partner.getAlias());
		userDTO.setValidEmail(partner.isEmailValid());
		userDTO.setFirstName(partner.getFirstName());
		userDTO.setLastName(partner.getLastName());
		userDTO.setEmailId(partner.getEmailId());
		userDTO.setCompanyName(partner.getCompanyName());
		userDTO.setAddress(partner.getAddress());
		userDTO.setZipCode(partner.getZipCode());
		userDTO.setCity(partner.getCity());
		userDTO.setState(partner.getState());
		userDTO.setCountry(partner.getCountry());
		userDTO.setMobileNumber(partner.getMobileNumber());
		userDTO.setContactCompany(partner.getCompanyName());
		userDTO.setUnsubscribed(partner.isUnsubscribed());
		userDTO.setPartnerCompanyId(partner.getCompanyProfile().getId());

		userListOperationsAsyncDTO.setVendorCompanyId(vendorCompanyId);
		userListOperationsAsyncDTO.setDefaultPartnerList(true);
		userListOperationsAsyncDTO.setUserListId(defaultPartnerList.getId());
		userListOperationsAsyncDTO.getPartnerListIds().add(defaultPartnerList.getId());
		userListOperationsAsyncDTO.getAllPartnerListIds().add(defaultPartnerList.getId());
		userListOperationsAsyncDTO.setDefaultPartnerListId(defaultPartnerList.getId());
		userListOperationsAsyncDTO.getPartners().add(userDTO);
		userListOperationsAsyncDTO.getAllPartners().add(userDTO);
		userListOperationsAsyncDTO.setPartnerList(true);
		userListOperationsAsyncDTO.setStatusCode(200);
	}

	public void addPartnerToList(Set<UserDTO> partners, UserList createdList, Integer companyId) {
		boolean isGdprOn = gdprSettingService.isGdprEnabled(companyId);
		// Add partners to list
		for (UserDTO partner : partners) {
			if (partner != null) {
				User partnerUser = userService.loadUser(
						Arrays.asList(new Criteria("emailId", OPERATION_NAME.eq, partner.getEmailId())),
						new FindLevel[] { FindLevel.COMPANY_PROFILE });
				partnershipServiceHelper.addPartnerToList(partnerUser, createdList, partner, isGdprOn);
			}
		}
	}

	@Override
	public XtremandResponse updatePartnerShipStatusForPartner(String partnerStatus, List<Integer> partnershipIds) {
		XtremandResponse response = new XtremandResponse();
		boolean deactivated = partnerStatus.equals("approved") ? true : false;
		partnershipDAO.updatePartnerShipStatusForPartner(partnerStatus, partnershipIds);
		if (partnershipIds != null) {
			for (Integer partnershipId : partnershipIds) {
				PartnershipDTO partnershipDTO = partnershipDAO
						.findPartnerIdAndPartnerCompanyIdByPartnershipId(partnershipId);
				if (partnershipDTO != null && XamplifyUtils.isValidInteger(partnershipDTO.getPartnerCompanyId())) {
					if (partnerStatus.equals("deactivated")) {
						utilService.revokeAccessTokensByCompanyId(partnershipDTO.getPartnerCompanyId());
					}
					utilService.updateNonVanityAccessForPartnerCompany(partnershipDTO.getPartnerCompanyId(),
							deactivated);
				}
			}
		}
		String responseMessage = "deactivated".equalsIgnoreCase(partnerStatus) ? "Partner deactivated successfully"
				: "Partner activated successfully";
		XamplifyUtils.addSuccessStatusWithMessage(response, responseMessage);
		return response;
	}

	@Override
	public XtremandResponse findPartnerCompaniesByDomain(PartnershipDTO partnershipDTO, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		if (XamplifyUtils.isValidInteger(loggedInUserId)) {
			Integer companyId = userDAO.getCompanyIdByUserId(loggedInUserId);
			partnershipDTO.setVendorCompanyId(companyId);
			response.setData(partnershipDAO.findPartnerCompaniesByDomain(partnershipDTO));
			response.setStatusCode(200);
		}
		return response;
	}

	@Override
	public XtremandResponse updatePartnerCompaniesByDomain(PartnershipDTO partnershipDTO, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		if (XamplifyUtils.isValidInteger(loggedInUserId)) {
			Integer companyId = userDAO.getCompanyIdByUserId(loggedInUserId);
			boolean deactivated = partnershipDTO.getStatus().equals("approved") ? true : false;
			partnershipDTO.setVendorCompanyId(companyId);
			Date updatedOn = new Date();
			partnershipDAO.updatePartnerCompaniesByDomain(partnershipDTO, updatedOn);
			updateDomainStatusLog(partnershipDTO.getDomainName(), companyId, loggedInUserId, updatedOn, !deactivated);
			List<Integer> partnerCompanyIds = partnershipDAO.getPartnerCompanyIdsByDomain(partnershipDTO);
			if (partnerCompanyIds != null) {
				for (Integer partnerCompanyId : partnerCompanyIds) {
					if (partnerCompanyId != null) {
						if (!partnershipDTO.isDomainDeactivated()) {
							utilService.revokeAccessTokensByCompanyId(partnerCompanyId);
						}
						utilService.updateNonVanityAccessForPartnerCompany(partnerCompanyId, deactivated);
					}
					updatePartnershipStatusLog(partnerCompanyId, companyId, loggedInUserId, updatedOn,
							partnershipDTO.getStatus());
				}
			}
			String responseMessage = partnershipDTO.isDomainDeactivated() ? "Domain activated successfully"
					: "Domain deactivated successfully";
			XamplifyUtils.addSuccessStatusWithMessage(response, responseMessage);
		}
		return response;
	}

	@Override
	public XtremandResponse deactivatePartnerCompanies(List<Integer> deactivateUserIds, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		if (XamplifyUtils.isValidInteger(loggedInUserId) && deactivateUserIds != null) {
			Integer vendorCompanyId = userDAO.getCompanyIdByUserId(loggedInUserId);
			Date updatedOn = new Date();
			partnershipDAO.deactivatePartnerCompanies(deactivateUserIds, vendorCompanyId, updatedOn);
			for (Integer userId : deactivateUserIds) {
				Integer companyId = userDAO.getCompanyIdByUserId(userId);
				if (companyId != null) {
					utilService.revokeAccessTokensByCompanyId(companyId);
					utilService.updateNonVanityAccessForPartnerCompany(companyId, false);
				}
				updatePartnershipStatusLog(companyId, vendorCompanyId, loggedInUserId, updatedOn, "deactivated");
			}
			response.setMessage("Partners deactivated successfully");
			response.setStatusCode(200);
		}
		return response;
	}

	@Override
	public List<String> findDeactivedPartnersByCompanyId(Integer companyId) {
		return partnershipDAO.findDeactivatedPartnersByCompanyId(companyId);
	}

	public void updatePartnershipStatusLog(Integer partnerCompanyId, Integer vendorCompanyId, Integer vendorId,
			Date updatedOn, String status) {
		if (partnerCompanyId == null) {
			return;
		}
		PartnershipStatusLog partnershipStatusLog = new PartnershipStatusLog();
		PartnershipStatus partnershipStatus = PartnershipStatus.DEACTIVATED.name().equals(status)
				? PartnershipStatus.DEACTIVATED
				: PartnershipStatus.APPROVED;
		Integer partnershipId = partnershipDAO.getPartnershipIdByVendorCompanyIdAndPartnerCompanyId(vendorCompanyId,
				partnerCompanyId);
		partnershipStatusLog.setVendorId(vendorId);
		partnershipStatusLog.setCompanyId(vendorCompanyId);
		partnershipStatusLog.setPartnershipId(partnershipId);
		partnershipStatusLog.setUpdatedOn(updatedOn);
		partnershipStatusLog.setStatus(partnershipStatus);

		genericDAO.save(partnershipStatusLog);
	}

	public void updateDomainStatusLog(String domainName, Integer vendorCompanyId, Integer vendorId, Date updatedOn,
			boolean deactivated) {

		Integer domainId = domainDao.getDomainIdByDomainNameAndVendorCompanyId(vendorCompanyId, domainName);
		if (domainId == null) {
			return;
		}

		DomainStatusLog domainStatusLog = new DomainStatusLog();
		domainStatusLog.setVendorId(vendorId);
		domainStatusLog.setCompanyId(vendorCompanyId);
		domainStatusLog.setDomainId(domainId);
		domainStatusLog.setUpdatedOn(updatedOn);
		domainStatusLog.setDeactivated(deactivated);
		genericDAO.save(domainStatusLog);
	}

	public XtremandResponse findTeamMemberPartnerCompany(Pagination pagination, Integer teamMemberGroupId) {
		XtremandResponse response = new XtremandResponse();
		List<Integer> partnershipIds = new ArrayList<Integer>();
		if (pagination.getTeamMemberId() != null && pagination.getTeamMemberId() > 0) {
			partnershipDAO.findPartnershipsByTeamMemberGroupIdAndTeamMemberId(pagination.getTeamMemberId(),
					teamMemberGroupId);
		}
		pagination.setFiltertedEmailTempalteIds(partnershipIds);
		response.setData(partnershipDAO.findTeamMemberPartnerCompanies(pagination, teamMemberGroupId));
		response.setStatusCode(200);
		return response;
	}

	public XtremandResponse findTeamMemberPartnerCompanyByTeamMemberGroupIdAndTeamMemberId(Integer teamMemberId,
			Integer teamMemberGroupId) {
		XtremandResponse response = new XtremandResponse();
		Map<String, Object> resultMap = new HashMap<>();

		List<PartnerCompanyDTO> partnerCompanyDTOs = partnershipDAO
				.findTeamMemberPartnerCompanyByTeamMemberGroupIdAndTeamMemberId(teamMemberId, teamMemberGroupId);
		List<Integer> partnershipIds = partnershipDAO.findPartnershipsByTeamMemberGroupIdAndTeamMemberId(teamMemberId,
				teamMemberGroupId);
		partnerCompanyDTOs = XamplifyUtils.isNotEmptyList(partnerCompanyDTOs) ? partnerCompanyDTOs : new ArrayList<>();
		partnershipIds = XamplifyUtils.isNotEmptyList(partnershipIds) ? partnershipIds : new ArrayList<>();

		resultMap.put("partnerCompanyDTOs", partnerCompanyDTOs);
		resultMap.put("partnershipIds", partnershipIds);
		response.setMap(resultMap);

		response.setStatusCode(200);
		return response;
	}

	// XNFR-1108
	@SuppressWarnings("unchecked")
	@Override
	public void referVendorAnalyticsDownloadCsv(Pagination pagination, HttpServletResponse response) {
		try {
			Map<String, Object> resultMap = new HashMap<>();
			resultMap = referVendorAnalytics(pagination);
			List<ReferedVendorDTO> referredVendors = (List<ReferedVendorDTO>) resultMap.get("referredVendors");
			List<String[]> data = new ArrayList<>();
			data.add(new String[] { "EMAIL ID", "COMPANY NAME", "STATUS", "INVITED ON" });
			for (ReferedVendorDTO vendor : referredVendors) {
				String email = vendor.getEmailId();
				String companyName = vendor.getCompanyName();
				String status = vendor.getStatus();
				String invitedOn = vendor.getReferredDateUTC();
				data.add(new String[] { email, companyName, status, invitedOn });
			}
			String fileName = "invite-a-vendor-analytics.csv";
			XamplifyUtils.generateCSV(fileName, response, data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void approvePartnersDownloadCsv(Pagination pagination, HttpServletResponse response) {
		try {
			Map<String, Object> resultMap = new HashMap<>();
			resultMap = getApprovalSectionPartnersDetails(pagination);
			List<PartnerDTO> approvePartners = (List<PartnerDTO>) resultMap.get("approvePartnerList");
			List<String[]> data = new ArrayList<>();
			data.add(new String[] { "NAME", "EMAIL ID", "STATUS" });
			for (PartnerDTO approvePartner : approvePartners) {
				String firstName = approvePartner.getFirstName() != null ? approvePartner.getFirstName() : "";
				String lastName = approvePartner.getLastName() != null ? approvePartner.getLastName() : "";
				String fullName = (firstName + " " + lastName).trim();

				String email = approvePartner.getEmailId() != null ? approvePartner.getEmailId() : "";
				String status = approvePartner.getInvitationStatus() != null ? approvePartner.getInvitationStatus()
						: "";
				data.add(new String[] { fullName, email, status });
			}
			String fileName = "approve-partner.csv";
			XamplifyUtils.generateCSV(fileName, response, data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}