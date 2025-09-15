package com.xtremand.partnership.service.impl;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xtremand.campaign.bom.ModuleAccess;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.CompanyProfile.CompanyNameStatus;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.Criteria.OPERATION_NAME;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Module;
import com.xtremand.common.bom.ModuleCustom;
import com.xtremand.common.bom.PartnerTeamMemberViewType;
import com.xtremand.company.service.CompanyProfileService;
import com.xtremand.dam.dao.DamDao;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.domain.bom.DomainModuleNameType;
import com.xtremand.domain.dao.hibernate.HibernateDomainDao;
import com.xtremand.formbeans.AddPartnerResponseDTO;
import com.xtremand.formbeans.ReferedVendorDTO;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.gdpr.setting.service.GdprSettingService;
import com.xtremand.mail.service.AsyncComponent;
import com.xtremand.mail.service.EmailConstants;
import com.xtremand.mail.service.MailService;
import com.xtremand.module.dao.ModuleDao;
import com.xtremand.partner.bom.PartnershipStatusHistory;
import com.xtremand.partnership.bom.PartnerTeamGroupMapping;
import com.xtremand.partnership.bom.Partnership;
import com.xtremand.partnership.bom.Partnership.PartnershipSource;
import com.xtremand.partnership.bom.Partnership.PartnershipStatus;
import com.xtremand.partnership.dao.PartnershipDAO;
import com.xtremand.team.dao.TeamDao;
import com.xtremand.team.member.dto.TeamMemberDTO;
import com.xtremand.team.member.dto.TeamMemberGroupDTO;
import com.xtremand.team.member.group.bom.TeamMemberGroupUserMapping;
import com.xtremand.team.member.group.dao.TeamMemberGroupDao;
import com.xtremand.team.member.group.service.TeamMemberGroupService;
import com.xtremand.user.bom.LegalBasis;
import com.xtremand.user.bom.ModulesDisplayType;
import com.xtremand.user.bom.Role;
import com.xtremand.user.bom.TeamMember;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.User.UserDefaultPage;
import com.xtremand.user.bom.User.UserStatus;
import com.xtremand.user.bom.UserList;
import com.xtremand.user.bom.UserSource;
import com.xtremand.user.bom.UserUserList;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.service.UserService;
import com.xtremand.userlist.dao.UserListDAO;
import com.xtremand.userlist.service.UserListService;
import com.xtremand.util.GenerateRandomPassword;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.AdminAndTeamMemberDetailsDTO;
import com.xtremand.util.dto.ModulesEmailNotification;
import com.xtremand.util.dto.UserListOperationsAsyncDTO;
import com.xtremand.util.service.UtilService;

@Component
public class PartnershipServiceHelper {

	private static final Logger logger = LoggerFactory.getLogger(PartnershipServiceHelper.class);

	@Autowired
	private UserListDAO userListDAO;

	@Autowired
	UserService userService;

	@Autowired
	PartnershipDAO partnershipDAO;

	@Autowired
	private GenericDAO genericDAO;

	@Autowired
	private MailService mailService;

	@Autowired
	GdprSettingService gdprSettingService;

	@Autowired
	private UtilService utilService;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private TeamMemberGroupDao teamMemberGroupDao;

	@Autowired
	private TeamDao teamDao;

	@Autowired
	private DamDao damDao;

	@Autowired
	private ModuleDao moduleDao;

	@Autowired
	@Lazy
	private AsyncComponent asyncComponent;

	@Autowired
	private TeamMemberGroupService teamMemberGroupService;

	@Autowired
	UserListService userListService;

	@Autowired
	CompanyProfileService companyProfileService;

	@Autowired
	private HibernateDomainDao domainDao;

	private static final String ADD_PARTNER_STATUS_SUCCESS = "SUCCESS";
	private static final String ADD_PARTNER_STATUS_SKIPPED = "SKIPPED";
	private static final String MESSAGE_EXISTING_TEAMMEMBER = "You can't refer a member of the same organization";
	private static final String MESSAGE_ONBOARD_EXISTING_TEAMMEMBER = "You can't add a member of your organization as partner";
	private static final String VENDOR_INVITATION_SKIPPED = "VENDOR_INVITATION_SKIPPED";
	private static final String VENDOR_INVITATION_SUCCESS = "VENDOR_INVITATION_SUCCESS";

	public Set<UserDTO> getNonExistingUserList(Collection<UserDTO> partners) {
		Set<UserDTO> nonExistingUsers = new HashSet<>();
		if (partners != null && !partners.isEmpty()) {
			partners.removeAll(Collections.singleton(null));
			for (UserDTO partner : partners) {
				if (partner != null && !isUserExist(partner.getEmailId())) {
					partner.setExistingUser(false);
					nonExistingUsers.add(partner);
				}
			}
		}
		return nonExistingUsers;
	}

	public Set<UserDTO> getExistingUserList(Collection<UserDTO> partners) {
		Set<UserDTO> existingUsers = new HashSet<>();
		if (partners != null && !partners.isEmpty()) {
			partners.removeAll(Collections.singleton(null));
			for (UserDTO partner : partners) {
				if (partner != null && isUserExist(partner.getEmailId())) {
					partner.setExistingUser(true);
					existingUsers.add(partner);
				}
			}
		}
		return existingUsers;
	}

	public boolean isUserExist(String emailId) {
		boolean result = false;
		if (emailId != null && !emailId.trim().isEmpty()) {
			User user = userService.loadUser(
					Arrays.asList(new Criteria("emailId", OPERATION_NAME.eq, emailId.trim().toLowerCase())),
					new FindLevel[] { FindLevel.SHALLOW });
			if (user != null) {
				result = true;
			}
		}
		return result;
	}

	/********** XNFR-81 *********/
	public List<AddPartnerResponseDTO> processNonExistingUserList(Set<UserDTO> nonExistingUsers, UserList userList,
			User representingVendor, UserList defaultPartnerList, List<User> sendPartnerMailsList, boolean isGdprOn,
			UserList inActiveMasterPartnerList, UserList activeMasterPartnerList, List<String> inActivePartners,
			List<String> activePartners, UserListOperationsAsyncDTO userListOperationsAsyncDTO) {
		List<AddPartnerResponseDTO> responseDTOList = null;
		if (nonExistingUsers != null && !nonExistingUsers.isEmpty()) {
			responseDTOList = new ArrayList<>();
			for (UserDTO nonExistingUser : nonExistingUsers) {
				// create User(Partner) -> Establish Partnership -> Add to
				// userUserList -> Add to Partnership Status History
				if (nonExistingUser != null) {
					User representingPartner = createPartner(nonExistingUser, representingVendor.getUserId());
					if (representingPartner != null) {
						nonExistingUser.setId(representingPartner.getUserId());
						nonExistingUser.setAlias(representingPartner.getAlias());
						nonExistingUser.setUserListId(userList.getId());
						nonExistingUser.setValidEmail(representingPartner.isEmailValid());
						if (nonExistingUser.getContactCompany() != null) {

							CompanyProfile companyProfile = companyProfileService.createPartnerCompany(nonExistingUser,
									representingVendor.getCompanyProfile().getId());
							/*
							 * CompanyProfile companyProfile = userDao
							 * .getCompanyProfileByCompanyName(nonExistingUser.getContactCompany().trim());
							 */
							/*
							 * if (companyProfile == null) { companyProfile =
							 * companyProfileService.createPartnerCompany(nonExistingUser,
							 * representingVendor.getCompanyProfile().getId()); }
							 */
							if (companyProfile != null) {
								representingPartner.setCompanyProfile(companyProfile);
								setCompanyProfileToUserDTO(nonExistingUser, companyProfile);
							}
						}
						AddPartnerResponseDTO responseDTO = establishPartnerShip(representingPartner,
								representingVendor, userList, nonExistingUser, defaultPartnerList, sendPartnerMailsList,
								isGdprOn, inActiveMasterPartnerList, activeMasterPartnerList, inActivePartners,
								activePartners, userListOperationsAsyncDTO);

						if (responseDTO != null) {
							if (nonExistingUser.isLoginUsingSAMLSSO()) {
								responseDTO.setUserAlias(representingPartner.getAlias());
							} else if (nonExistingUser.isLoginUsingOauthSSO()) {
								responseDTO.setUserId(representingPartner.getUserId());
							} else {
								responseDTO.setUserId(representingPartner.getUserId());
							}
							responseDTO.setModuleDTOs(nonExistingUser.getDefaultModules());
							responseDTOList.add(responseDTO);
						}
					}
				}
			}
		}
		return responseDTOList;
	}

	/********** XNFR-81 *********/
	private AddPartnerResponseDTO establishPartnerShip(User representingPartner, User representingVendor,
			UserList userList, UserDTO nonExistingUser, UserList defaultPartnerList, List<User> sendPartnerMailsList,
			boolean isGdprOn, UserList inActiveMasterPartnerList, UserList activeMasterPartnerList,
			List<String> inActivePartners, List<String> activePartners,
			UserListOperationsAsyncDTO userListOperationsAsyncDTO) {

		AddPartnerResponseDTO responseDTO = null;
		boolean isSignedUpUsingVendorLink = userList.isSignUpUsingVendorLink();
		/*** XNFR-506 *****/
		PartnershipSource partnershipSource = PartnershipSource.ONBOARD;
		if (isSignedUpUsingVendorLink) {
			partnershipSource = PartnershipSource.SIGN_UP_LINK;
		}
		if (userList.isLoginUsingSAMLSSO()) {
			partnershipSource = PartnershipSource.SAML_SSO;
		} else if (userList.isLoginUsingOauthSSO()) {
			partnershipSource = PartnershipSource.OAUTH_SSO;
		}

		/*** XNFR-506 *****/
		Partnership partnership = createPartnership(representingVendor, representingPartner, PartnershipStatus.APPROVED,
				partnershipSource);
		if (partnership != null) {
			createParnerCompanyDomains(representingPartner, nonExistingUser, partnership);
			/****** XNFR-889 **********/
			if (partnershipSource.equals(PartnershipSource.ONBOARD)) {
				List<String> domains = new ArrayList<>();
				String domain = XamplifyUtils.getEmailDomain(representingPartner.getEmailId().trim().toLowerCase());
				domains.add(domain);
				List<String> domainNames = domainDao.findAllDomainNames(representingVendor.getCompanyProfile().getId(),
						DomainModuleNameType.PARTNER);
				if (domainNames.indexOf(domain) < 0) {
					utilService.saveDomain(representingVendor.getUserId(), representingVendor.getCompanyProfile(),
							domains, DomainModuleNameType.PARTNER, true);
				}
			}
			addPartnerToList(representingPartner, userList, nonExistingUser, isGdprOn);
			/* Also add partner to Default Partner List */
			if (!userList.isDefaultPartnerList() && defaultPartnerList != null) {
				addPartnerToList(representingPartner, defaultPartnerList, nonExistingUser, isGdprOn);
				userListOperationsAsyncDTO.getAllPartnerListIds().add(defaultPartnerList.getId());
			}
			/****** XNFR-81 **********/
			Integer partnerId = representingPartner.getUserId();
			Integer companyId = representingVendor.getCompanyProfile().getId();

			insertIntoInActiveAndActiveMasterPartnerList(representingPartner, nonExistingUser, isGdprOn,
					inActiveMasterPartnerList, activeMasterPartnerList, partnerId, companyId, inActivePartners,
					activePartners, userListOperationsAsyncDTO);
			/****** XNFR-81 **********/

			addPartnershipStatusHistory(partnership, PartnershipStatus.APPROVED, representingVendor.getUserId());
			populateMailList(sendPartnerMailsList, partnership.getRepresentingPartner(), nonExistingUser);
			responseDTO = getPartnerResponseDTO(representingPartner.getEmailId(), ADD_PARTNER_STATUS_SUCCESS, null);
			/****** XNFR-276 **********/

			addModuleCustomsForPartner(partnership, representingPartner);

			nonExistingUser.setPartnershipId(partnership.getId());
			responseDTO.setPartnershipId(partnership.getId());

		}
		return responseDTO;
	}

	public void addModuleCustomsForPartner(Partnership partnership, User representingPartner) {
		if (representingPartner.getCompanyProfile() != null) {
			List<Module> modules = moduleDao.findModuleNames();
			CompanyProfile partnerCompany = representingPartner.getCompanyProfile();
			if (partnerCompany != null) {
				List<ModuleCustom> moduleCustomsForVendor = utilService.getModuleCustomsForVendor(partnership,
						partnerCompany.getId(), modules);
				if (moduleCustomsForVendor != null && !moduleCustomsForVendor.isEmpty()) {
					utilDao.saveAll(moduleCustomsForVendor);
				}
			}

		}
	}

	/****** XNFR-81 **********/
	private void insertIntoInActiveAndActiveMasterPartnerList(User representingPartner, UserDTO nonExistingUser,
			boolean isGdprOn, UserList inActiveMasterPartnerList, UserList activeMasterPartnerList, Integer partnerId,
			Integer companyId, List<String> inActivePartners, List<String> activePartners,
			UserListOperationsAsyncDTO userListOperationsAsyncDTO) {
		if (inActiveMasterPartnerList != null && activeMasterPartnerList != null) {
			if (partnerId != null) {
				boolean isActivePartner = utilDao.isActivePartner(partnerId, companyId);
				if (isActivePartner) {
					insertIntoActiveMasterPartnerList(representingPartner, nonExistingUser, isGdprOn,
							activeMasterPartnerList, partnerId, companyId, activePartners, userListOperationsAsyncDTO);
				} else {
					insertIntoInActiveMasterPartnerList(representingPartner, nonExistingUser, isGdprOn,
							inActiveMasterPartnerList, partnerId, companyId, inActivePartners);
				}
			} else {
				addPartnerToList(representingPartner, inActiveMasterPartnerList, nonExistingUser, isGdprOn);
				String message = representingPartner.getEmailId() + " has been added to InActive Master Partner Group("
						+ inActiveMasterPartnerList.getId() + ")";
				inActivePartners.add(message);
			}
		}

	}

	/****** XNFR-81 **********/
	private void insertIntoActiveMasterPartnerList(User representingPartner, UserDTO nonExistingUser, boolean isGdprOn,
			UserList activeMasterPartnerList, Integer partnerId, Integer companyId, List<String> activePartners,
			UserListOperationsAsyncDTO userListOperationsAsyncDTO) {
		boolean isPartnerExistsInActiveMasterPartnerList = utilDao.isPartnerAddedToActivePartnerList(partnerId,
				companyId);
		if (!isPartnerExistsInActiveMasterPartnerList) {
			addPartnerToList(representingPartner, activeMasterPartnerList, nonExistingUser, isGdprOn);
			String message = representingPartner.getEmailId() + "(" + representingPartner.getUserId()
					+ ") has been added to Active Master Partner Group(" + activeMasterPartnerList.getId() + ")";
			activePartners.add(message);
			userListOperationsAsyncDTO.getAllPartnerListIds().add(activeMasterPartnerList.getId());
		}
	}

	/****** XNFR-81 **********/
	private void insertIntoInActiveMasterPartnerList(User representingPartner, UserDTO nonExistingUser,
			boolean isGdprOn, UserList inActiveMasterPartnerList, Integer partnerId, Integer companyId,
			List<String> inActivePartners) {
		boolean isPartnerExistsInInActiveMasterPartnerList = utilDao.isPartnerAddedToInActivePartnerList(partnerId,
				companyId);
		if (!isPartnerExistsInInActiveMasterPartnerList) {
			addPartnerToList(representingPartner, inActiveMasterPartnerList, nonExistingUser, isGdprOn);
			String message = representingPartner.getEmailId() + "(" + representingPartner.getUserId()
					+ ") has been added to InActive Master Partner Group(" + inActiveMasterPartnerList.getId() + ")";
			inActivePartners.add(message);
		}
	}

	public void populateMailList(List<User> sendPartnerMailsList, User representingPartner) {
		CompanyProfile company = representingPartner.getCompanyProfile();
		if (company != null && company.getCompanyNameStatus() == CompanyNameStatus.ACTIVE) {
			List<User> owners = partnershipDAO.getOwners(company.getId());
			for (User owner : owners) {
				if (representingPartner.getUserId() != owner.getUserId()) {
					sendPartnerMailsList.add(owner);
				}
			}
		}
		sendPartnerMailsList.add(representingPartner);
	}

	public void populateMailList(List<User> sendPartnerMailsList, User representingPartner, UserDTO nonExistingUser) {
		CompanyProfile company = representingPartner.getCompanyProfile();
		if (company != null && company.getCompanyNameStatus() == CompanyNameStatus.ACTIVE) {
			List<User> owners = partnershipDAO.getOwners(company.getId());
			if (owners != null && !owners.isEmpty()) {
				for (User owner : owners) {
					if (representingPartner.getUserId() != owner.getUserId()) {
						User partnerUser = new User();
						partnerUser.setUserId(owner.getUserId());
						partnerUser.setAlias(owner.getAlias());
						partnerUser.setPassword(owner.getPassword());
						partnerUser.setEmailId(owner.getEmailId());
						partnerUser.setUserStatus(owner.getUserStatus());
						partnerUser.setFirstName(
								StringUtils.hasText(owner.getFirstName()) ? owner.getFirstName() : "there");
						partnerUser
								.setLastName(StringUtils.hasText(owner.getLastName()) ? owner.getLastName() : "there");
						partnerUser.setNotifyPartners(nonExistingUser.isNotifyPartners());
						sendPartnerMailsList.add(partnerUser);
					}
				}
			}
			// for representing partner
			User partnerUser = new User();
			partnerUser.setUserId(representingPartner.getUserId());
			partnerUser.setAlias(representingPartner.getAlias());
			partnerUser.setPassword(representingPartner.getPassword());
			partnerUser.setEmailId(representingPartner.getEmailId());
			partnerUser.setUserStatus(representingPartner.getUserStatus());
			partnerUser.setFirstName(
					StringUtils.hasText(representingPartner.getFirstName()) ? representingPartner.getFirstName()
							: "there");
			partnerUser.setLastName(
					StringUtils.hasText(representingPartner.getLastName()) ? representingPartner.getLastName()
							: "there");
			partnerUser.setNotifyPartners(nonExistingUser.isNotifyPartners());
			sendPartnerMailsList.add(partnerUser);
		} else {
			User partnerUser = new User();
			partnerUser.setUserId(representingPartner.getUserId());
			partnerUser.setAlias(representingPartner.getAlias());
			partnerUser.setPassword(representingPartner.getPassword());
			partnerUser.setEmailId(representingPartner.getEmailId());
			partnerUser.setUserStatus(representingPartner.getUserStatus());
			partnerUser.setNotifyPartners(representingPartner.isNotifyPartners());
			partnerUser.setFirstName(nonExistingUser.getFirstName());
			partnerUser.setLastName(nonExistingUser.getLastName());
			sendPartnerMailsList.add(partnerUser);
		}
	}

	/*
	 * Validate User -> Check Partnership Existed -> If NO, Establish Partnership ->
	 * Add to userUserList -> Add to Partnership Status History -> If YES, add user
	 * to list if current list is not default partner list
	 */
	public List<AddPartnerResponseDTO> processExistingUserList(Set<UserDTO> existingUsers, UserList userList,
			User representingVendor, UserList defaultPartnerList, List<User> sendPartnerMailsList, boolean isGdprOn,
			UserList inActiveMasterPartnerList, UserList activeMasterPartnerList, List<String> inActivePartners,
			List<String> activePartners, Integer loggedInUserId,
			UserListOperationsAsyncDTO userListOperationsAsyncDTO) {

		List<AddPartnerResponseDTO> responseDTOList = null;
		if (existingUsers != null && !existingUsers.isEmpty()) {
			responseDTOList = new ArrayList<>();
			for (UserDTO existingUser : existingUsers) {
				AddPartnerResponseDTO responseDTO = null;
				User representingPartner = userService.loadUser(
						Arrays.asList(new Criteria("emailId", OPERATION_NAME.eq,
								existingUser.getEmailId().trim().toLowerCase())),
						new FindLevel[] { FindLevel.COMPANY_PROFILE, FindLevel.ROLES });
				existingUser.setId(representingPartner.getUserId());
				existingUser.setUnsubscribed(representingPartner.isUnsubscribed());
				existingUser.setUserListId(userList.getId());
				existingUser.setValidEmail(representingPartner.isEmailValid());
				existingUser.setAlias(representingPartner.getAlias());
				representingPartner.setAccountId(existingUser.getAccountId());
				representingPartner.setAccountName(existingUser.getAccountName());
				CompanyProfile partnerCompany = representingPartner != null ? representingPartner.getCompanyProfile()
						: null;
				if (partnerCompany != null) {
					setCompanyProfileToUserDTO(existingUser, partnerCompany);
				}
				if (representingPartner != null && Boolean.TRUE.equals(userList.isPartnerUserList())) {
					representingPartner.setContactsLimit(existingUser.getContactsLimit());
					representingPartner.setMdfAmount(existingUser.getMdfAmount());
					representingPartner.setNotifyPartners(existingUser.isNotifyPartners());
					/*************** XNFR-85 ***************/
					representingPartner.setSelectedTeamMemberIds(existingUser.getSelectedTeamMemberIds());
				}

				boolean isTeamMember = false;
				if (partnerCompany != null) {
					if (representingVendor.getCompanyProfile().getId() == partnerCompany.getId()) {
						responseDTO = getPartnerResponseDTO(representingPartner.getEmailId(),
								ADD_PARTNER_STATUS_SKIPPED, MESSAGE_ONBOARD_EXISTING_TEAMMEMBER);
						isTeamMember = true;
					}
				}
				if (!isTeamMember) {
					Partnership partnership = getPartnership(representingPartner,
							representingVendor.getCompanyProfile());
					if (representingPartner.getUserStatus() == UserStatus.UNAPPROVED
							&& representingPartner.getPassword() == null
							&& representingPartner.getCompanyProfile() == null
							&& existingUser.getContactCompany() != null) {
						CompanyProfile companyProfile = companyProfileService.createPartnerCompany(existingUser,
								representingVendor.getCompanyProfile().getId());
						representingPartner.setCompanyProfile(companyProfile);
						setCompanyProfileToUserDTO(existingUser, companyProfile);
					}

					if (partnership != null) {
						// -> If partnership exist, just add user to list if
						// current list is not default partner list
						if (!userList.isDefaultPartnerList()) {
							if (representingPartner.getUserId() != partnership.getRepresentingPartner().getUserId()) {
								String errorMsg = frameExistingPartnerErrorMessage(partnership,
										defaultPartnerList.getId());
								responseDTO = getPartnerResponseDTO(representingPartner.getEmailId(),
										ADD_PARTNER_STATUS_SKIPPED, errorMsg);
							} else {
								addPartnerToList(representingPartner, userList, existingUser, isGdprOn);
								defaultPartnerList.initialiseCommonFields(false, representingVendor.getUserId());
								responseDTO = getPartnerResponseDTO(representingPartner.getEmailId(),
										ADD_PARTNER_STATUS_SUCCESS, null);
								/************** Update xt_partnership table with two values *****************/
								partnershipDAO.updateContactsLimitAndMDFAmountAndTeamMembers(
										representingVendor.getCompanyProfile().getId(), representingPartner.getUserId(),
										representingPartner.getContactsLimit(), representingPartner.getMdfAmount());
								/*********** XNFR-85 *****************/
								updatePartnerTeamMemberGroupMapping(existingUser.getSelectedTeamMemberIds(),
										representingVendor.getCompanyProfile().getId(), representingPartner.getUserId(),
										loggedInUserId, false, null, null, existingUser.getTeamMemberGroupId(), null);
								existingUser.setPartnershipId(partnership.getId());
							}
						} else {
							String errorMsg = frameExistingPartnerErrorMessage(partnership, userList.getId());
							responseDTO = getPartnerResponseDTO(representingPartner.getEmailId(),
									ADD_PARTNER_STATUS_SKIPPED, errorMsg);
						}
					} else {
						if (representingPartner.getCompanyProfile() == null) {
							if (existingUser.getContactCompany() != null) {
								CompanyProfile companyProfile = userDao.getCompanyProfile(
										existingUser.getContactCompany(),
										representingVendor.getCompanyProfile().getId());
								if (companyProfile != null
										&& companyProfile.getCompanyNameStatus().equals(CompanyNameStatus.ACTIVE)) {
									representingPartner.setCompanyProfile(companyProfile);
								} else {
									companyProfile = companyProfileService.createPartnerCompany(existingUser,
											representingVendor.getCompanyProfile().getId());
									representingPartner.setCompanyProfile(companyProfile);
								}
								setCompanyProfileToUserDTO(existingUser, companyProfile);
							}
						}
						/*********** XNFR-2 *******/
						addPartnerRoleAndUpdateModules(representingPartner, representingVendor);
						responseDTO = establishPartnerShip(representingPartner, representingVendor, userList,
								existingUser, defaultPartnerList, sendPartnerMailsList, isGdprOn,
								inActiveMasterPartnerList, activeMasterPartnerList, inActivePartners, activePartners,
								userListOperationsAsyncDTO);
					}
				}
				if (responseDTO != null) {
					responseDTO.setModuleDTOs(existingUser.getDefaultModules());
					responseDTOList.add(responseDTO);
				}

				/********* Remove Access Tokens **********/
				if (partnerCompany != null) {
					utilService.revokeAccessTokensByCompanyId(partnerCompany.getId());
				} else {
					utilService.revokeAccessTokenByUserId(existingUser.getId());
				}
			}
		}
		return responseDTOList;
	}

	private void setCompanyProfileToUserDTO(UserDTO existingUser, CompanyProfile partnerCompany) {
		// CompanyProfileDTO companyProfileDTO = new CompanyProfileDTO();
		// BeanUtils.copyProperties(partnerCompany, companyProfileDTO);
		// existingUser.setCompanyProfileDTO(companyProfileDTO);
		existingUser.setPartnerCompanyId(partnerCompany.getId());
	}

	/***********
	 * XNFR-85 / XNFR-98
	 * 
	 * @param teamMemberGroupId TODO
	 ***************/
	public void updatePartnerTeamMemberGroupMapping(Set<Integer> selectedTeamMemberGroupUserMappingIdsSet,
			Integer vendorCompanyId, Integer partnerId, Integer loggedInUserId,
			boolean applyTeamMemberMasterPartnerListLogic, User partner, List<Integer> legalBasisIds,
			Integer teamMemberGroupId, UserListOperationsAsyncDTO userListOperationsAsyncDTO) {
		List<Integer> selectedTeamMemberGroupUserMappingIds = XamplifyUtils
				.convertSetToList(selectedTeamMemberGroupUserMappingIdsSet);
		List<Integer> updatedTeamMemerGroupUserMappingIds = new ArrayList<Integer>();

		for (Integer selectedTeamMemberGroupUserMappingId : selectedTeamMemberGroupUserMappingIds) {
			Integer teamMemberGroupIdByTeamMemberUserMappingId = teamDao
					.getTeamMemberGroupIdById(selectedTeamMemberGroupUserMappingId);
			if (teamMemberGroupId.equals(teamMemberGroupIdByTeamMemberUserMappingId)) {
				updatedTeamMemerGroupUserMappingIds.add(selectedTeamMemberGroupUserMappingId);
			} else {
				logger.debug(teamMemberGroupId + " & " + teamMemberGroupIdByTeamMemberUserMappingId
						+ " does not match.So " + selectedTeamMemberGroupUserMappingId + " Skipped." + new Date());
			}

		}

		Integer partnershipId = partnershipDAO.findPartnershipIdByPartnerIdAndVendorCompanyId(partnerId,
				vendorCompanyId);

		List<Integer> assignedTeamMemberGroupUserMappingIds = XamplifyUtils.convertSetToList(
				teamMemberGroupDao.findSelectedTeamMemberGroupUserMappingIdsByPartnershipId(partnershipId));
		Collections.sort(updatedTeamMemerGroupUserMappingIds);
		Collections.sort(assignedTeamMemberGroupUserMappingIds);
		if (!updatedTeamMemerGroupUserMappingIds.equals(assignedTeamMemberGroupUserMappingIds)) {
			/****
			 * Find unchecked teammemberGroupUserMappingIds and remove them from database
			 * from user input
			 *********/
			assignedTeamMemberGroupUserMappingIds.removeAll(updatedTeamMemerGroupUserMappingIds);
			partnershipDAO.findAndDeletePartnerTeamMemberGroupMappingByPartnershipIdAndTeamMemberGroupMappingIds(
					partnershipId, assignedTeamMemberGroupUserMappingIds);
			deleteFromTeamMemberPartnerListAndDam(partnerId, loggedInUserId, assignedTeamMemberGroupUserMappingIds);
			/*** Swathi Code Comes Here To Delete Data From LMS **/
			/******
			 * Finally get all new selected teammemberGroupUserMappingIds to insert into
			 * table
			 *********/
			removeUnselectedTeamMemberIds(partnershipId, updatedTeamMemerGroupUserMappingIds);
			if (updatedTeamMemerGroupUserMappingIds != null && !updatedTeamMemerGroupUserMappingIds.isEmpty()) {
				User loggedInUser = userService.loadUser(
						Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, loggedInUserId)),
						new FindLevel[] { FindLevel.COMPANY_PROFILE });
				Partnership partnership = new Partnership();
				partnership.setId(partnershipId);
				Set<PartnerTeamGroupMapping> partnerTeamGroupMappings = new HashSet<PartnerTeamGroupMapping>();
				Set<Integer> userListIds = new HashSet<Integer>();
				for (Integer teamMemberGroupUserMappingId : updatedTeamMemerGroupUserMappingIds) {
					boolean isPartnerTeamMemberGroupMappingExists = partnershipDAO
							.isPartnerTeamMemberGroupMappingExists(partnershipId, teamMemberGroupUserMappingId);
					if (!isPartnerTeamMemberGroupMappingExists) {
						PartnerTeamGroupMapping partnerTeamGroupMapping = new PartnerTeamGroupMapping();
						partnerTeamGroupMapping.setPartnership(partnership);
						TeamMemberGroupUserMapping teamMemberGroupUserMapping = new TeamMemberGroupUserMapping();
						teamMemberGroupUserMapping.setId(teamMemberGroupUserMappingId);
						partnerTeamGroupMapping.setTeamMemberGroupUserMapping(teamMemberGroupUserMapping);
						partnerTeamGroupMapping.setCreatedTime(new Date());
						partnerTeamGroupMapping.setUpdatedTime(new Date());
						partnerTeamGroupMappings.add(partnerTeamGroupMapping);
					}
					/************ XNFR-98 ******************/
					if (applyTeamMemberMasterPartnerListLogic) {
						AdminAndTeamMemberDetailsDTO adminAndTeamMemberDetailsDTO = teamDao
								.getTeamMemberPartnerMasterListName(teamMemberGroupUserMappingId);
						Integer teamMemberId = adminAndTeamMemberDetailsDTO.getId();
						Integer userListId = userListDAO.findUserListIdByTeamMemberId(teamMemberId, vendorCompanyId);
						if (userListId != null) {
							userListIds.add(userListId);
						}
						userListOperationsAsyncDTO.setPartnerList(true);
						addPartnersToTeamMemberPartnerList(vendorCompanyId, partnerId, loggedInUserId,
								teamMemberGroupUserMappingId, partner, legalBasisIds, loggedInUser,
								adminAndTeamMemberDetailsDTO, userListId, userListOperationsAsyncDTO);
					}

				}
				partnershipDAO.savePartnerTeamMemberGroupMapping(partnerTeamGroupMappings);
				if (applyTeamMemberMasterPartnerListLogic && userListIds != null && !userListIds.isEmpty()) {
					UserDTO partnerDto = new UserDTO();
					partnerDto.setEmailId(partner.getEmailId());
					HashSet<UserDTO> allPartners = new HashSet<>();
					allPartners.add(partnerDto);
					/********* Share DAM Content To This Users *************************/
					asyncComponent.publishDamToTeamMemberPartnerListPartners(userListIds, loggedInUser, allPartners);
					/********* Share LMS Content To This Users (Swathi) *************************/

					User partnerObj = userService.loadUser(
							Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, partnerId)),
							new FindLevel[] { FindLevel.COMPANY_PROFILE });
					partnerDto.setId(partnerObj.getUserId());
					partnerDto.setUnsubscribed(partnerObj.isUnsubscribed());
					partnerDto.setPartnerCompanyId(partnerObj.getCompanyProfile().getId());
					userListOperationsAsyncDTO.setPartner(partnerDto);

				}

			}

		}
	}

	private void deleteFromTeamMemberPartnerListAndDam(Integer partnerId, Integer loggedInUserId,
			List<Integer> assignedTeamMemberGroupUserMappingIds) {
		/*********** XNFR-98 ***************/
		List<Integer> teamMemberIds = teamMemberGroupDao
				.findTeamMemberIdsByTeamMemberGroupUserMappingIds(assignedTeamMemberGroupUserMappingIds);
		userListDAO.deletePartnerFromTeamMemberPartnerListByPartnerIdAndTeamMemberIds(partnerId, teamMemberIds);
		/******* Deleting rows from xt_dam_partner_group_mapping ***********/
		List<Integer> teamMemberParnterListIds = userListDAO.findUserListIdsByTeamMemberIds(teamMemberIds);
		List<Integer> removeUserIdsList = new ArrayList<>();
		removeUserIdsList.add(partnerId);
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		damDao.deleteDamPartnerGroupMappingsByUserListIdsAndUserIds(removeUserIdsList, teamMemberParnterListIds);
		damDao.delateDamPartnersByCompanyId(companyId);
	}

	/************ XNFR-98 ******************/
	private void addPartnersToTeamMemberPartnerList(Integer vendorCompanyId, Integer partnerId, Integer loggedInUserId,
			Integer teamMemberGroupUserMappingId, User partner, List<Integer> legalBasisIds, User loggedInUser,
			AdminAndTeamMemberDetailsDTO adminAndTeamMemberDetailsDTO, Integer userListId,
			UserListOperationsAsyncDTO userListOperationsAsyncDTO) {
		UserList teamMemberUserList = new UserList();
		boolean createUserList = false;
		if (userListId != null) {
			createUserList = false;
			teamMemberUserList.setId(userListId);
		} else {
			createUserList = true;
			String partnerListName = adminAndTeamMemberDetailsDTO.getFullName() + "-Master Partner Group";
			teamMemberUserList.setName(partnerListName);
			TeamMember teamMember = new TeamMember();
			teamMember.setId(adminAndTeamMemberDetailsDTO.getId());
			XamplifyUtils.setTeamMemberUserList(loggedInUserId, loggedInUser, teamMember, teamMemberUserList);
		}
		Set<UserUserList> teamMemberUserUserLists = new HashSet<UserUserList>();
		addOrUpdateTeamMemberMasterPartnerList(teamMemberUserList, teamMemberUserUserLists, partner, createUserList,
				legalBasisIds, userListId, userListOperationsAsyncDTO);
		if (createUserList) {
			if (!teamMemberUserUserLists.isEmpty()) {
				teamMemberUserList.setUserUserLists(teamMemberUserUserLists);
			}
			genericDAO.save(teamMemberUserList);
			userListOperationsAsyncDTO.getAllPartnerListIds().add(teamMemberUserList.getId());
		}
	}

	/************ XNFR-98 ******************/
	private void addOrUpdateTeamMemberMasterPartnerList(UserList teamMemberUserList,
			Set<UserUserList> teamMemberUserUserLists, User partnerDTO, boolean createUserList,
			List<Integer> legalBasisIds, Integer userListId, UserListOperationsAsyncDTO userListOperationsAsyncDTO) {
		User partner = new User();
		Integer partnerId = userDao.getUserIdByEmail(partnerDTO.getEmailId());
		partner.setUserId(partnerId);
		if (createUserList) {
			createTeamMemberPartnerList(teamMemberUserList, teamMemberUserUserLists, partnerDTO, legalBasisIds, partner,
					false);
		} else {
			boolean isPartnerExistsTeamMemberPartnerList = userListDAO.isPartnerExistsInPartnerList(partnerId,
					userListId);
			if (isPartnerExistsTeamMemberPartnerList) {
				updateTeamMemberPartnerList(teamMemberUserList, partnerDTO, legalBasisIds, userListId, partner);
			} else {
				createTeamMemberPartnerList(teamMemberUserList, teamMemberUserUserLists, partnerDTO, legalBasisIds,
						partner, true);
				userListOperationsAsyncDTO.getAllPartnerListIds().add(teamMemberUserList.getId());
			}
		}
	}

	private void updateTeamMemberPartnerList(UserList teamMemberUserList, User partnerDTO, List<Integer> legalBasisIds,
			Integer userListId, User partner) {
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
				updateLegalBasis(userUserList, legalBasisIds);
				break;
			}
		}
		userListDAO.updateTeamMemberPartnerList(teamMemberUserList.getId());
	}

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

	private void createTeamMemberPartnerList(UserList teamMemberUserList, Set<UserUserList> teamMemberUserUserLists,
			User partnerDTO, List<Integer> legalBasisIds, User partner, boolean masterPartnerListExists) {
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
		for (Integer legalBasisId : legalBasisIds) {
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

	private void removeUnselectedTeamMemberIds(Integer partnershipId, List<Integer> teamMemberIds) {
		List<Integer> existingSelectedTeamMemberIds = XamplifyUtils.convertSetToList(
				teamMemberGroupDao.findSelectedTeamMemberGroupUserMappingIdsByPartnershipId(partnershipId));
		if (!teamMemberIds.equals(existingSelectedTeamMemberIds)) {
			teamMemberIds.removeAll(existingSelectedTeamMemberIds);
		}

	}

	private String frameExistingPartnerErrorMessage(Partnership partnership, Integer defaultPartnerListId) {
		String message = "";
		if (partnership != null) {
			List<Integer> userListIds = new ArrayList<>();
			userListIds.add(defaultPartnerListId);
			UserUserList userUserList = userListDAO.getUserUserList(partnership.getRepresentingPartner().getUserId(),
					userListIds);
			message += "Partnership with ";
			String partnerCompany = userUserList != null ? userUserList.getContactCompany() : null;
			if (partnerCompany != null) {
				// message += partnerCompany.getCompanyName()+ " already established via " ;
				message += partnerCompany + " already established via ";
				message += partnership.getRepresentingPartner().getEmailId();
			} else {
				message += partnership.getRepresentingPartner().getEmailId() + " already established";
			}
		}
		return message;
	}

	private String frameExistingVendorErrorMessage(Partnership partnership, CompanyProfile vendorCompany) {
		String message = "";
		if (partnership != null) {

			message += "Partnership with ";
			if (vendorCompany != null) {
				message += vendorCompany.getCompanyProfileName() + " already established via ";
				message += partnership.getRepresentingVendor().getEmailId();
			} else {
				message += partnership.getRepresentingVendor().getEmailId() + " already established";
			}
		}
		return message;
	}

	private AddPartnerResponseDTO getPartnerResponseDTO(String emailId, String status, String message) {
		AddPartnerResponseDTO addPartnerResponseDTO = null;
		if (emailId != null && status != null) {
			addPartnerResponseDTO = new AddPartnerResponseDTO();
			addPartnerResponseDTO.setEmailId(emailId);
			addPartnerResponseDTO.setMessage(message);
			addPartnerResponseDTO.setStatus(status);
		}
		return addPartnerResponseDTO;
	}

	public void addPartnershipStatusHistory(Partnership partnership, PartnershipStatus status, Integer createdBy) {
		if (partnership != null && status != null && createdBy != null && createdBy > 0) {
			PartnershipStatusHistory partnershipStatusHistory = new PartnershipStatusHistory();
			partnershipStatusHistory.setPartnership(partnership);
			partnershipStatusHistory.setStatus(status);
			partnershipStatusHistory.setCreatedBy(createdBy);
			partnershipStatusHistory.setCreatedTime(new Date());
			genericDAO.save(partnershipStatusHistory);
		}
	}

	public void addPartnerToList(User representingPartner, UserList userList, UserDTO userDetails, boolean isGdprOn) {
		UserUserList userUserList = null;
		if (representingPartner != null && userList != null && userDetails != null) {
			userUserList = new UserUserList();
			userUserList.setUser(representingPartner);
			String contactCompany = userDetails.getContactCompany();
			if (contactCompany == null || contactCompany.isEmpty()) {
				// this block is executed when partner is added through refer a vendor concept
				CompanyProfile partnerCompanyProfile = representingPartner.getCompanyProfile();
				if (partnerCompanyProfile != null) {
					contactCompany = partnerCompanyProfile.getCompanyName();
				}
			}
			userUserList.setContactCompany(contactCompany);
			userUserList.setUserList(userList);
			userUserList.setFirstName(userDetails.getFirstName());
			userUserList.setLastName(userDetails.getLastName());
			userUserList.setJobTitle(userDetails.getJobTitle());
			userUserList.setMobileNumber(userDetails.getMobileNumber());
			userUserList.setDescription(userDetails.getDescription());
			userUserList.setAddress(userDetails.getAddress());
			userUserList.setCity(userDetails.getCity());
			userUserList.setCountry(userDetails.getCountry());
			userUserList.setState(userDetails.getState());
			userUserList.setZipCode(userDetails.getZipCode());
			userUserList.setVertical(userDetails.getVertical());
			userUserList.setRegion(userDetails.getRegion());
			userUserList.setPartnerType(userDetails.getPartnerType());
			userUserList.setCategory(userDetails.getCategory());
			userUserList.setAccountName(userDetails.getAccountName());
			userUserList.setAccountSubType(userDetails.getAccountSubType());
			userUserList.setAccountOwner(userDetails.getAccountOwner());
			userUserList.setCompanyDomain(userDetails.getCompanyDomain());
			userUserList.setTerritory(userDetails.getTerritory());
			userUserList.setWebsite(userDetails.getWebsite());
			userUserList.setCountryCode(userDetails.getCountryCode());
			setLegalBasis(userUserList, userDetails.getLegalBasis(), isGdprOn, representingPartner.getUserId());
			userListDAO.saveUserUserList(userUserList);
			representingPartner.setEmailValid(true);
			representingPartner.setEmailValidationInd(true);
		}
	}

	private void setLegalBasis(UserUserList userUserList, List<Integer> legalBasisIds, boolean isGdprOn,
			Integer userId) {
		if (userUserList != null) {
			List<LegalBasis> legalBasisList = new ArrayList<>();
			if (isGdprOn && legalBasisIds != null && !legalBasisIds.isEmpty()) {
				for (Integer legalBasisId : legalBasisIds) {
					boolean isLegalBasisExists = userListDAO.isLegalBasisExists(userId,
							userUserList.getUserList().getId(), legalBasisId);
					if (!isLegalBasisExists) {
						LegalBasis legalBasis = new LegalBasis();
						legalBasis.setId(legalBasisId);
						legalBasisList.add(legalBasis);
					}
				}
			} else {
				legalBasisList = gdprSettingService.getSelectByDefaultLegalBasis();
			}
			userUserList.setLegalBasis(legalBasisList);
		}
	}

	public void addPartnerToListThroughVendorInvitation(User representingPartner, UserList userList) {
		UserUserList userUserList = null;
		if (representingPartner != null && userList != null) {
			userUserList = new UserUserList();
			userUserList.setUser(representingPartner);
			String contactCompany = "";
			CompanyProfile partnerCompanyProfile = representingPartner.getCompanyProfile();
			if (partnerCompanyProfile != null) {
				contactCompany = partnerCompanyProfile.getCompanyName();
			}
			userUserList.setContactCompany(contactCompany);
			userUserList.setUserList(userList);
			// Send false to isGdprOn to select default(Not Applicable) legal basis for
			// partners
			setLegalBasis(userUserList, null, false, representingPartner.getUserId());

			userListDAO.saveUserUserList(userUserList);
			representingPartner.setEmailValid(true);
			representingPartner.setEmailValidationInd(true);
		}
	}

	private Partnership createPartnership(User representingVendor, User representingPartner, PartnershipStatus status,
			PartnershipSource source) {
		Partnership partnership = null;
		if (representingVendor != null && representingPartner != null) {
			partnership = new Partnership();
			partnership.setVendorCompany(representingVendor.getCompanyProfile());
			partnership.setRepresentingVendor(representingVendor);
			partnership.setRepresentingPartner(representingPartner);
			partnership.setPartnerCompany(representingPartner.getCompanyProfile());
			partnership.setSource(source);
			partnership.setStatus(status);
			partnership.setContactsLimit(representingPartner.getContactsLimit());
			partnership.setNotifyPartners(representingPartner.isNotifyPartners());
			partnership.setPartnerSalesforceAccountId(representingPartner.getAccountId());
			partnership.setPartnerSalesforceAccountName(representingPartner.getAccountName());

			initialiseFieldsByPartnershipSource(representingVendor, representingPartner, source, partnership);

			/****** XNFR-85 ***********/
			addPartnerTeamMemberGroupMapping(representingPartner, partnership);
			if (!(source.equals(PartnershipSource.VENDOR_INVITATION))) {
				Set<PartnerTeamMemberViewType> partnerTeamMemberViewTypes = new HashSet<>();
				partnerTeamMemberViewTypes = setViewTypeForPartner(representingVendor, partnership,
						partnerTeamMemberViewTypes);
				partnership.setPartnerTeamMemberViewType(partnerTeamMemberViewTypes);
			}
			genericDAO.save(partnership);

		}
		return partnership;
	}

	private void createParnerCompanyDomains(User representingPartner, UserDTO nonExistingUser,
			Partnership partnership) {
		nonExistingUser.setId(representingPartner.getUserId());
		utilService.createPartnerCompanyDomains(nonExistingUser, partnership.getId());
	}

	private void initialiseFieldsByPartnershipSource(User representingVendor, User representingPartner,
			PartnershipSource source, Partnership partnership) {
		if (source == PartnershipSource.ONBOARD || source.equals(PartnershipSource.SIGN_UP_LINK)) {
			partnership.setCreatedBy(representingVendor.getUserId());
			partnership.initialiseCommonFields(true, representingVendor.getUserId());
		} else if (source == PartnershipSource.VENDOR_INVITATION) {
			partnership.setCreatedBy(representingPartner.getUserId());
			partnership.initialiseCommonFields(true, representingPartner.getUserId());
		} else if (source.equals(PartnershipSource.SAML_SSO) || source.equals(PartnershipSource.OAUTH_SSO)) {
			partnership.setCreatedBy(representingVendor.getUserId());
			partnership.initialiseCommonFields(true, representingVendor.getUserId());
			partnership.setContactsLimit(1);
		}
	}

	private void addPartnerTeamMemberGroupMapping(User representingPartner, Partnership partnership) {
		Set<Integer> teamMemberIds = representingPartner.getSelectedTeamMemberIds();
		if (teamMemberIds != null && !teamMemberIds.isEmpty()) {
			Set<PartnerTeamGroupMapping> partnerTeamGroupMappings = new HashSet<PartnerTeamGroupMapping>();
			for (Integer teamMemberId : teamMemberIds) {
				PartnerTeamGroupMapping partnerTeamGroupMapping = new PartnerTeamGroupMapping();
				partnerTeamGroupMapping.setCreatedTime(new Date());
				partnerTeamGroupMapping.setUpdatedTime(new Date());
				partnerTeamGroupMapping.setPartnership(partnership);
				TeamMemberGroupUserMapping teamMemberGroupUserMapping = new TeamMemberGroupUserMapping();
				teamMemberGroupUserMapping.setId(teamMemberId);
				partnerTeamGroupMapping.setTeamMemberGroupUserMapping(teamMemberGroupUserMapping);
				partnerTeamGroupMappings.add(partnerTeamGroupMapping);
			}
			partnership.setPartnerTeamGroupMappings(partnerTeamGroupMappings);
		}
	}

	public Set<PartnerTeamMemberViewType> setViewTypeForPartner(User representingVendor, Partnership partnership,
			Set<PartnerTeamMemberViewType> partnerTeamMemberViewTypes) {
		if (XamplifyUtils.isValidInteger(representingVendor.getCompanyProfile().getId())) {
			Integer companyId = representingVendor.getCompanyProfile().getId();
			Integer primaryAdminId = teamDao.findPrimaryAdminIdByCompanyId(companyId);
			String viewType = userDao.getAdminOrPartnerOrTeamMemberViewType(primaryAdminId, null, null);
			PartnerTeamMemberViewType partnerTeamMemberViewType = new PartnerTeamMemberViewType();
			User user = new User();
			user.setUserId(primaryAdminId);
			partnerTeamMemberViewType.setAdmin(user);
			partnerTeamMemberViewType.setPartnership(partnership);
			partnerTeamMemberViewType.setVendorViewType(ModulesDisplayType.findByName(viewType));
			partnerTeamMemberViewType.setViewUpdated(false);
			partnerTeamMemberViewType.setCreatedTime(new Date());
			partnerTeamMemberViewType.setUpdatedTime(new Date());
			partnerTeamMemberViewTypes.add(partnerTeamMemberViewType);
			setViewTypeForPartnerTeamMembers(viewType, primaryAdminId, partnership, partnerTeamMemberViewTypes);
		}
		return partnerTeamMemberViewTypes;
	}

	private void setViewTypeForPartnerTeamMembers(String viewType, Integer primaryAdminId, Partnership partnership,
			Set<PartnerTeamMemberViewType> partnerTeamMemberViewTypes) {
		List<Integer> teamMemberIds = teamDao
				.getAllTeamMemberIdsByOrgAdmin(partnership.getRepresentingPartner().getUserId());
		if (XamplifyUtils.isNotEmptyList(teamMemberIds)) {
			for (Integer teamMemberId : teamMemberIds) {
				PartnerTeamMemberViewType partnerTeamMemberViewType = new PartnerTeamMemberViewType();
				TeamMember tMember = new TeamMember();
				tMember.setId(teamDao.getPrimaryKeyId(teamMemberId));
				User user = new User();
				user.setUserId(primaryAdminId);
				partnerTeamMemberViewType.setAdmin(user);
				partnerTeamMemberViewType.setPartnership(partnership);
				partnerTeamMemberViewType.setTeamMember(tMember);
				partnerTeamMemberViewType.setVendorViewType(ModulesDisplayType.findByName(viewType));
				partnerTeamMemberViewType.setViewUpdated(false);
				partnerTeamMemberViewType.setCreatedTime(new Date());
				partnerTeamMemberViewType.setUpdatedTime(new Date());
				partnerTeamMemberViewTypes.add(partnerTeamMemberViewType);
			}
		}
	}

	private User createPartner(UserDTO partner, Integer createdBy) {
		User newPartner = null;
		if (partner != null && createdBy != null && createdBy > 0) {
			newPartner = new User();
			newPartner.setEmailId(partner.getEmailId().toLowerCase());
			/*** XNFR-506 ****/
			if (partner.isSignUpUsingVendorLink()) {
				newPartner.setUserStatus(UserStatus.APPROVED);
				newPartner.setRegisteredTime(new Date());
				newPartner.setActivatedTime(new Date());
				newPartner.setPassword(partner.getPassword());
				newPartner.setFirstName(partner.getFirstName());
				newPartner.setLastName(partner.getLastName());
			} else if (partner.isLoginUsingSAMLSSO() || partner.isLoginUsingOauthSSO()) {
				newPartner.setUserStatus(UserStatus.APPROVED);
				newPartner.setRegisteredTime(new Date());
				newPartner.setActivatedTime(new Date());
				newPartner.setFirstName(partner.getFirstName());
				newPartner.setLastName(partner.getLastName());
				if (partner.isLoginUsingSAMLSSO()) {
					newPartner.setSource(UserSource.SAMLSSO);
				} else if (partner.isLoginUsingOauthSSO()) {
					newPartner.setSource(UserSource.OAUTHSSO);
				}
			} else {
				newPartner.setUserStatus(UserStatus.UNAPPROVED);
			}
			/*** XNFR-506 ****/
			newPartner.setUserName(newPartner.getEmailId());
			newPartner.setUserDefaultPage(UserDefaultPage.WELCOME);
			newPartner.getRoles().add(Role.USER_ROLE);
			newPartner.getRoles().add(Role.COMPANY_PARTNER);
			newPartner.setEmailValid(true);
			newPartner.setEmailValidationInd(true);
			newPartner.setContactsLimit(partner.getContactsLimit());
			newPartner.setMdfAmount(partner.getMdfAmount());
			newPartner.setNotifyPartners(partner.isNotifyPartners());
			/*************** XNFR-85 ***************/
			newPartner.setSelectedTeamMemberIds(partner.getSelectedTeamMemberIds());
			newPartner.initialiseCommonFields(true, createdBy);
			genericDAO.save(newPartner);
			// XNFR-697
			newPartner.setAccountId(partner.getAccountId());
			newPartner.setAccountName(partner.getAccountName());
			/**** Below Line is added for production null issue on 08/03/2022 ***********/
			partner.setId(newPartner.getUserId());
			logger.debug("****************************************************************************");
			String partnerAddedDebugMessage = partner.getEmailId() + " is onboarded(" + partner.getId() + ") by "
					+ createdBy;
			logger.debug(partnerAddedDebugMessage);
			logger.debug("****************************************************************************");
			/**** ************ ***********/
			GenerateRandomPassword randomPassword = new GenerateRandomPassword();
			newPartner.setAlias(randomPassword.getPassword());
		}

		return newPartner;
	}

	public Partnership getPartnership(User representingPartner, CompanyProfile vendorCompany) {
		Partnership partnership = null;
		if (representingPartner.getCompanyProfile() != null) {
			partnership = partnershipDAO.getPartnershipByPartnerCompany(representingPartner.getCompanyProfile(),
					vendorCompany);
			if (partnership == null) {
				partnership = partnershipDAO.getPartnershipByRepresentingPartner(representingPartner, vendorCompany);
			}
		} else {
			partnership = partnershipDAO.getPartnershipByRepresentingPartner(representingPartner, vendorCompany);
		}
		return partnership;
	}

	public Partnership getPartnershipAsPartner(User representingVendor, CompanyProfile partnerCompany) {
		Partnership partnership = null;
		if (representingVendor.getCompanyProfile() != null) {
			partnership = partnershipDAO.getPartnershipByPartnerCompany(partnerCompany,
					representingVendor.getCompanyProfile());
		} else {
			partnership = partnershipDAO.getPartnershipByRepresentingVendor(representingVendor, partnerCompany);
		}
		return partnership;
	}

	public void deletePartnership(List<User> removePartnersList, CompanyProfile vendorCompany) {
		for (User removePartner : removePartnersList) {
			CompanyProfile partnerCompany = removePartner.getCompanyProfile();
			if (partnerCompany != null) {
				partnershipDAO.deletePartnerShipByPartnerCompany(partnerCompany, vendorCompany);
			} else {
				partnershipDAO.deletePartnerShipByRepresentingPartner(removePartner, vendorCompany);
			}
		}
	}

	public void sendPartnerListUpdatedMail(User loggedInUser, UserList userList) {
		CompanyProfile company = loggedInUser.getCompanyProfile();
		if (company != null) {
			List<User> orgAdminsOrVendors = partnershipDAO
					.getOrgAdminsOrVendorsOrVendorTiersOrMarketing(company.getId());
			for (User orgAdminOrVendor : orgAdminsOrVendors) {
				if (!loggedInUser.getUserId().equals(orgAdminOrVendor.getUserId())) {
					orgAdminOrVendor.setCompanyProfileName(loggedInUser.getCompanyProfileName());
					mailService.sendPartnerlistMail(orgAdminOrVendor, EmailConstants.PARTNER_LIST_UPDATED, userList);
				}
			}
			mailService.sendPartnerlistMail(loggedInUser, EmailConstants.PARTNER_LIST_UPDATED, userList);
		}
	}

	public void deletePartnersFromPartnerLists(List<Integer> removePartnerIds, List<Integer> userListIds) {
		if (XamplifyUtils.isNotEmptyList(removePartnerIds) && XamplifyUtils.isNotEmptyList(userListIds)) {
			userListDAO.deletePartnersFromPartnerLists(removePartnerIds, userListIds);
		}
	}

	public List<AddPartnerResponseDTO> processNonExistingVendors(Set<UserDTO> nonExistingUsers,
			User representingPartner, List<User> sendVendorMailsList) {
		List<AddPartnerResponseDTO> responseDTOList = null;
		if (nonExistingUsers != null && !nonExistingUsers.isEmpty()) {
			responseDTOList = new ArrayList<>();
			for (UserDTO vendor : nonExistingUsers) {
				User representingVendor = createVendor(vendor);
				if (representingVendor != null) {
					AddPartnerResponseDTO responseDTO = establishPartnerShipThroughVendorInvitation(representingPartner,
							representingVendor, sendVendorMailsList);
					if (responseDTO != null) {
						responseDTOList.add(responseDTO);
					}
				}
			}
		}
		return responseDTOList;
	}

	private User createVendor(UserDTO vendor) {
		User newVendor = null;
		if (vendor != null) {
			newVendor = new User();
			newVendor.setEmailId(vendor.getEmailId().toLowerCase());
			newVendor.setUserStatus(UserStatus.UNAPPROVED);
			newVendor.getRoles().add(Role.USER_ROLE);
			newVendor.initialiseCommonFields(true, 0);
			genericDAO.save(newVendor);

			GenerateRandomPassword randomPassword = new GenerateRandomPassword();
			newVendor.setAlias(randomPassword.getPassword());
		}
		return newVendor;
	}

	private AddPartnerResponseDTO establishPartnerShipThroughVendorInvitation(User representingPartner,
			User representingVendor, List<User> sendVendorMailsList) {
		AddPartnerResponseDTO responseDTO = null;
		Partnership partnership = createPartnership(representingVendor, representingPartner, PartnershipStatus.INVITED,
				PartnershipSource.VENDOR_INVITATION);
		if (partnership != null) {
			addPartnershipStatusHistory(partnership, PartnershipStatus.INVITED, representingPartner.getUserId());
			populateMailList(sendVendorMailsList, representingVendor);
			responseDTO = getPartnerResponseDTO(representingVendor.getEmailId(), VENDOR_INVITATION_SUCCESS, null);
		}
		return responseDTO;
	}

	public List<AddPartnerResponseDTO> processExistingVendors(Set<UserDTO> existingUsers, User representingPartner,
			List<User> sendVendorMailsList) {
		List<AddPartnerResponseDTO> responseDTOList = null;

		if (existingUsers != null && !existingUsers.isEmpty()) {
			responseDTOList = new ArrayList<>();
			for (UserDTO existingUser : existingUsers) {
				AddPartnerResponseDTO responseDTO = null;
				User representingVendor = userService.loadUser(
						Arrays.asList(
								new Criteria("emailId", OPERATION_NAME.eq, existingUser.getEmailId().toLowerCase())),
						new FindLevel[] { FindLevel.COMPANY_PROFILE, FindLevel.ROLES });
				CompanyProfile vendorCompany = representingVendor != null ? representingVendor.getCompanyProfile()
						: null;
				boolean isTeamMember = false;
				if (vendorCompany != null) {
					if (representingPartner.getCompanyProfile().getId() == vendorCompany.getId()) {
						responseDTO = getPartnerResponseDTO(representingVendor.getEmailId(), VENDOR_INVITATION_SKIPPED,
								MESSAGE_EXISTING_TEAMMEMBER);
						isTeamMember = true;
					}
				}
				if (!isTeamMember) {
					Partnership partnership = getPartnershipAsPartner(representingVendor,
							representingPartner.getCompanyProfile());
					if (partnership != null) {
						if (partnership.getStatus() == PartnershipStatus.APPROVED) {
							responseDTO = getPartnerResponseDTO(representingVendor.getEmailId(),
									VENDOR_INVITATION_SKIPPED,
									frameExistingVendorErrorMessage(partnership, vendorCompany));
						} else {
							// resend email
							// check whether representing vendor should be changed in partnership table if
							// company exists
							populateMailList(sendVendorMailsList, representingVendor);
						}

					} else {
						responseDTO = establishPartnerShipThroughVendorInvitation(representingPartner,
								representingVendor, sendVendorMailsList);
					}
				}
				if (responseDTO != null) {
					responseDTOList.add(responseDTO);
				}
			}
		}
		return responseDTOList;
	}

	// ******************************** PATCH
	// *****************************************

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void processExistingPartnerLists() {
		// get all master partner groups
		List<UserList> defaultUserLists = partnershipDAO.getAllDefaultPartnerLists();
		if (defaultUserLists != null && !defaultUserLists.isEmpty()) {
			for (UserList defaultUserList : defaultUserLists) {
				partnershipDAO.processExistingPartnerList(defaultUserList);
			}
		}
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void processExistingPartnerListsHQL() {
		// get all master partner groups
		List<UserList> defaultUserLists = partnershipDAO.getAllDefaultPartnerLists();
		if (defaultUserLists != null && !defaultUserLists.isEmpty()) {
			for (UserList defaultUserList : defaultUserLists) {
				User representingVendor = defaultUserList.getOwner();
				Set<User> partners = defaultUserList.getUsers();
				if (partners != null && !partners.isEmpty()) {

					for (User representingPartner : partners) {
						// push to partnership table and status history table
						if (representingPartner != null) {
							Partnership partnership = getPartnership(representingPartner,
									representingVendor.getCompanyProfile());
							if (partnership != null) {
								// skip
							} else {
								partnership = createPartnership(representingVendor, representingPartner,
										PartnershipStatus.APPROVED, PartnershipSource.ONBOARD);
								if (partnership != null) {
									addPartnershipStatusHistory(partnership, PartnershipStatus.APPROVED,
											representingVendor.getUserId());
								}
							}
						}
					}
				}
			}
		}
	}

	public List<ReferedVendorDTO> getReferredVendorListDTO(List<Partnership> partnerships) {
		List<ReferedVendorDTO> referredVendors = null;
		if (partnerships != null) {
			referredVendors = new ArrayList<>();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			for (Partnership partnership : partnerships) {
				if (partnership != null) {
					ReferedVendorDTO referredVendor = new ReferedVendorDTO();
					referredVendor.setEmailId(partnership.getRepresentingVendor().getEmailId());
					if (partnership.getCreatedTime() != null) {
						String formattedDate = partnership.getCreatedTime().toInstant().atZone(ZoneId.systemDefault())
								.format(formatter);
						referredVendor.setReferredDateUTC(formattedDate);
					}
					referredVendor.setStatus(partnership.getStatus().name());
					CompanyProfile vendorCompany = partnership.getVendorCompany();
					if (vendorCompany != null) {
						referredVendor.setCompanyName(vendorCompany.getCompanyName());
					}
					referredVendors.add(referredVendor);
				}
			}
		}
		return referredVendors;
	}

	/************************* XNFR-2 **************************/
	public void addPartnerRoleAndUpdateModules(User representingPartner, User representingVendor) {
		if (representingPartner != null) {
			CompanyProfile partnerCompanyProfile = representingPartner.getCompanyProfile();
			if (partnerCompanyProfile != null) {
				Integer partnerCompanyId = partnerCompanyProfile.getId();
				Integer partnerId = representingPartner.getUserId();
				boolean isTeamMember = teamDao.isTeamMember(partnerId);
				if (userService.isOnlyUser(representingPartner)) {
					if (isTeamMember) {
						/**** Find The Admin And Update Modules *********/
						Integer adminId = teamDao.getOrgAdminIdByTeamMemberId(partnerId);
						if (!XamplifyUtils.isValidInteger(adminId)) {
							adminId = teamDao.findOrgAdminIdByTeamMemberId(partnerId);
						}
						User partnerAdmin = userService.loadUser(
								Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, adminId)),
								new FindLevel[] { FindLevel.COMPANY_PROFILE, FindLevel.ROLES });
						partnerAdmin.getRoles().add(Role.COMPANY_PARTNER);
						updateModules(partnerAdmin, representingVendor, partnerCompanyId, false);
					} else {
						representingPartner.getRoles().add(Role.COMPANY_PARTNER);
						updateModules(representingPartner, representingVendor, partnerCompanyId, false);
					}
				} else {
					updateModules(representingPartner, representingVendor, partnerCompanyId, true);
				}
			} else {
				representingPartner.getRoles().add(Role.COMPANY_PARTNER);
			}
		}

	}

	/*********** XNFR-2 *******/
	private void updateModules(User representingPartner, User representingVendor, Integer partnerCompanyId,
			boolean addPartnerRole) {
		List<Integer> roleIds = representingVendor.getRoles().stream().map(Role::getRoleId)
				.collect(Collectors.toList());
		ModuleAccess moduleAccess = userDao.getAccessByCompanyId(representingVendor.getCompanyProfile().getId());
		List<User> admins = partnershipDAO.getOwners(partnerCompanyId);
		for (User admin : admins) {
			boolean isTeamMember = teamDao.isTeamMember(admin.getUserId());
			if (!isTeamMember) {
				List<TeamMemberGroupDTO> teamMemberGroupDtos = teamMemberGroupDao
						.findTeamMemberGroupIdsAndNamesByRoleIdAndCompanyId(partnerCompanyId,
								Role.ALL_ROLES.getRoleId());

				updateModule(partnerCompanyId, moduleAccess.isMdf(), teamMemberGroupDtos, Role.MDF.getRoleId());
				updateModule(partnerCompanyId, moduleAccess.isDam(), teamMemberGroupDtos, Role.DAM.getRoleId());
				updateModule(partnerCompanyId, moduleAccess.isLms(), teamMemberGroupDtos,
						Role.LEARNING_TRACK.getRoleId());
				updateModule(partnerCompanyId, moduleAccess.isPlaybooks(), teamMemberGroupDtos,
						Role.PLAY_BOOK.getRoleId());
				updateModule(partnerCompanyId, moduleAccess.isShareLeads(), teamMemberGroupDtos,
						Role.SHARE_LEADS.getRoleId());
				updateModule(partnerCompanyId, moduleAccess.isEnableLeads(), teamMemberGroupDtos,
						Role.OPPORTUNITY.getRoleId());
				setModuleIdsAndAddRolesToDefaultGroups(partnerCompanyId, moduleAccess);
				if (addPartnerRole) {
					addPartnerRole(representingPartner, admin);
				}
			}

		}
	}

	private void setModuleIdsAndAddRolesToDefaultGroups(Integer partnerCompanyId, ModuleAccess moduleAccess) {
		List<Integer> moduleIds = new ArrayList<>();
		if (moduleAccess.isMdf()) {
			moduleIds.add(Role.MDF.getRoleId());
		}
		if (moduleAccess.isDam()) {
			moduleIds.add(Role.DAM.getRoleId());
		}

		if (moduleAccess.isLms()) {
			moduleIds.add(Role.LEARNING_TRACK.getRoleId());
		}

		if (moduleAccess.isPlaybooks()) {
			moduleIds.add(Role.PLAY_BOOK.getRoleId());
		}

		if (moduleAccess.isShareLeads()) {
			moduleIds.add(Role.SHARE_LEADS.getRoleId());
		}

		if (moduleAccess.isEnableLeads()) {
			moduleIds.add(Role.OPPORTUNITY.getRoleId());
		}

		utilService.addRolesToDefaultGroups(moduleIds, partnerCompanyId, new ModulesEmailNotification());
	}

	/*********** XNFR-2 *******/
	void updateModule(Integer partnerCompanyId, boolean moduleAccess, List<TeamMemberGroupDTO> teamMemberGroupDtos,
			Integer roleId) {
		if (moduleAccess) {
			addRoleToTeamMemberGroupsAndTeamMembers(partnerCompanyId, roleId, teamMemberGroupDtos);
		}

	}

	/*********** XNFR-2 *******/
	private void addRoleToTeamMemberGroupsAndTeamMembers(Integer partnerCompanyId, Integer roleId,
			List<TeamMemberGroupDTO> teamMemberGroupDtos) {
		utilService.addRoleToTeamMemberGroupsAndTeamMembers(partnerCompanyId, roleId, null, teamMemberGroupDtos);

	}

	/*********** XNFR-2 *******/
	private void addPartnerRole(User representingPartner, User orgAdminOrVendor) {
		if (!representingPartner.getUserId().equals(orgAdminOrVendor.getUserId())) {
			orgAdminOrVendor.getRoles().add(Role.COMPANY_PARTNER);
		} else {
			representingPartner.getRoles().add(Role.COMPANY_PARTNER);
		}
	}

	/*********** XNFR-2 *******/
	public void deletePartnerRoleAndUpdateModules(List<User> partners, Integer vendorCompanyId) {
		for (User partner : partners) {
			CompanyProfile partnerCompany = partner.getCompanyProfile();
			if (partnerCompany == null) {
				findEmptyPartnershipAndRemovePartnerRole(partner);
			} else {
				Integer partnerCompanyId = partnerCompany.getId();
				Set<Partnership> partnerships = partnerCompany.getPartnershipsAsPartner();
				if (partnerships == null || partnerships.isEmpty()) {
					findAdminsAndDeletePartnerRoleAndUpdateModules(partner, partnerCompanyId);
				} else {
					ModuleAccess moduleAccess = userDao.getAccessByCompanyId(partnerCompanyId);
					Integer partnerId = partner.getUserId();
					deleteDynamicModules(vendorCompanyId, partnerCompanyId, moduleAccess);
				}

			}
		}
	}

	/*********** XNFR-2 *******/
	private void deleteDynamicModules(Integer vendorCompanyId, Integer partnerCompanyId, ModuleAccess moduleAccess) {
		if (moduleAccess != null && XamplifyUtils.isValidInteger(vendorCompanyId)
				&& XamplifyUtils.isValidInteger(partnerCompanyId)) {
			removeModule(vendorCompanyId, partnerCompanyId, moduleAccess.isMdf(), null, Role.MDF.getRoleId());
			removeModule(vendorCompanyId, partnerCompanyId, moduleAccess.isDam(), null, Role.DAM.getRoleId());
			removeModule(vendorCompanyId, partnerCompanyId, moduleAccess.isLms(), null,
					Role.LEARNING_TRACK.getRoleId());
			removeModule(vendorCompanyId, partnerCompanyId, moduleAccess.isPlaybooks(), null,
					Role.PLAY_BOOK.getRoleId());
			removeModule(vendorCompanyId, partnerCompanyId, moduleAccess.isShareLeads(), null,
					Role.SHARE_LEADS.getRoleId());
			removeModule(vendorCompanyId, partnerCompanyId, moduleAccess.isEnableLeads(), null,
					Role.OPPORTUNITY.getRoleId());

		}

	}

	/*********** XNFR-2 *******/
	private void removeModule(Integer vendorCompanyId, Integer partnerCompanyId, boolean moduleAccess,
			ModulesEmailNotification modulesEmailNotification, Integer moduleId) {
		boolean moduleAccessAsPartner = utilDao.hasModuleAccessForPartnerByPartnerCompanyId(partnerCompanyId,
				vendorCompanyId, moduleId);
		if (!moduleAccess && !moduleAccessAsPartner) {
			deleteModule(partnerCompanyId, moduleId, modulesEmailNotification);
		}
	}

	/*********** XNFR-2 *******/
	private void findAdminsAndDeletePartnerRoleAndUpdateModules(User removePartner, Integer partnerCompanyId) {
		List<User> owners = partnershipDAO.getOwners(partnerCompanyId);
		for (User owner : owners) {
			if (owner != null && owner.getUserId().equals(removePartner.getUserId())) {
				owner = removePartner;
				utilService.revokeAccessTokenByUserId(owner.getUserId());
			}
			if (isOnlyPartnerRole(owner)) {
				deletePartnerTeamMemberRoles(partnerCompanyId);
				deleteAllModules(partnerCompanyId);
				/**** XNFR-117 ******/
				teamMemberGroupService.findAndDeleteTeamMemberGroupUserMappingsByCompanyId(partnerCompanyId);
			} else {
				ModuleAccess moduleAccess = userDao.getAccessByCompanyId(partnerCompanyId);
				deleteMDFModule(partnerCompanyId, moduleAccess);
				deleteDAMModule(partnerCompanyId, moduleAccess);
				deleteLearningTracksModule(partnerCompanyId, moduleAccess);
				deletePlayBooksModule(partnerCompanyId, moduleAccess);
				deleteOpportunitiesModule(partnerCompanyId, moduleAccess);
				deleteShareLeadsModule(partnerCompanyId, moduleAccess);
			}
			removePartnerRole(owner);
		}
	}

	private void deleteShareLeadsModule(Integer partnerCompanyId, ModuleAccess moduleAccess) {
		if (!moduleAccess.isShareLeads()) {
			deleteModule(partnerCompanyId, Role.SHARE_LEADS.getRoleId(), null);
		}
	}

	private void deleteOpportunitiesModule(Integer partnerCompanyId, ModuleAccess moduleAccess) {
		if (!moduleAccess.isEnableLeads()) {
			deleteModule(partnerCompanyId, Role.OPPORTUNITY.getRoleId(), null);
		}
	}

	private void deletePlayBooksModule(Integer partnerCompanyId, ModuleAccess moduleAccess) {
		if (!moduleAccess.isPlaybooks()) {
			deleteModule(partnerCompanyId, Role.PLAY_BOOK.getRoleId(), null);
		}
	}

	private void deleteLearningTracksModule(Integer partnerCompanyId, ModuleAccess moduleAccess) {
		if (!moduleAccess.isLms()) {
			deleteModule(partnerCompanyId, Role.LEARNING_TRACK.getRoleId(), null);
		}
	}

	private void deleteDAMModule(Integer partnerCompanyId, ModuleAccess moduleAccess) {
		if (!moduleAccess.isDam()) {
			deleteModule(partnerCompanyId, Role.DAM.getRoleId(), null);
		}
	}

	private void deleteMDFModule(Integer partnerCompanyId, ModuleAccess moduleAccess) {
		if (!moduleAccess.isMdf()) {
			deleteModule(partnerCompanyId, Role.MDF.getRoleId(), null);
		}
	}

	public void deleteAllModules(Integer partnerCompanyId) {
		deleteModule(partnerCompanyId, Role.MDF.getRoleId(), null);
		deleteModule(partnerCompanyId, Role.DAM.getRoleId(), null);
		deleteModule(partnerCompanyId, Role.LEARNING_TRACK.getRoleId(), null);
		deleteModule(partnerCompanyId, Role.PLAY_BOOK.getRoleId(), null);
		deleteModule(partnerCompanyId, Role.SHARE_LEADS.getRoleId(), null);
		deleteModule(partnerCompanyId, Role.OPPORTUNITY.getRoleId(), null);
	}

	private void deleteModule(Integer partnerCompanyId, Integer roleId,
			ModulesEmailNotification modulesEmailNotification) {
		utilService.removeRoleFromTeamMemberGroupRoleMappingAndTeamMember(partnerCompanyId, roleId,
				modulesEmailNotification);

	}

	public void deletePartnerTeamMemberRoles(Integer companyId) {
		List<TeamMember> teamMembers = teamDao.findAllByCompanyId(companyId);
		List<Integer> teamMemberUserIds = new ArrayList<Integer>();
		for (TeamMember teamMember : teamMembers) {
			Integer teamMemberUserId = teamMember.getTeamMember().getUserId();
			if (teamMemberUserId != null) {
				TeamMemberDTO teamMemberDTO = new TeamMemberDTO();
				teamMemberDTO.setId(teamMemberUserId);
				teamMemberUserIds.add(teamMemberUserId);
				utilService.revokeAccessTokenByUserId(teamMemberUserId);
			}
		}
		userDao.deleteAllTeamMemberRoles(teamMemberUserIds);
	}

	/*********** XNFR-2 *******/
	private void findEmptyPartnershipAndRemovePartnerRole(User removePartner) {
		Set<Partnership> partnerships = removePartner.getPartnershipsAsPartner();
		if (partnerships == null || partnerships.isEmpty()) {
			removePartnerRole(removePartner);
		}
	}

	public void removePartnerRole(User user) {
		user.getRoles().remove(Role.COMPANY_PARTNER);
	}

	public boolean isOnlyPartnerRole(User user) {
		boolean isOnlyPartnerRole = false;
		List<Integer> roleIdsList = user.getRoles().stream().map((role) -> (role.getRoleId()))
				.collect(Collectors.toList());
		if (roleIdsList.contains(Role.COMPANY_PARTNER.getRoleId())
				&& !roleIdsList.contains(Role.PRM_ROLE.getRoleId())) {
			isOnlyPartnerRole = true;
		}
		return isOnlyPartnerRole;
	}

	public boolean removeCampaignAndContactAccess(User user) {
		List<Integer> roleIdsList = user.getRoles().stream().map(Role::getRoleId).collect(Collectors.toList());
		return roleIdsList.contains(Role.COMPANY_PARTNER.getRoleId())
				&& roleIdsList.contains(Role.PRM_ROLE.getRoleId());
	}

	public void addPartnerToList(Set<UserDTO> partners, UserList createdList, Integer companyId) {
		boolean isGdprOn = gdprSettingService.isGdprEnabled(companyId);
		// Add partners to list
		for (UserDTO partner : partners) {
			if (partner != null) {
				User partnerUser = userService.loadUser(
						Arrays.asList(new Criteria("emailId", OPERATION_NAME.eq, partner.getEmailId())),
						new FindLevel[] { FindLevel.COMPANY_PROFILE });
				addPartnerToList(partnerUser, createdList, partner, isGdprOn);
			}
		}
		createdList.setUploadInProgress(false);
		createdList.setValidationInProgress(false);
		createdList.setEmailValidationInd(true);
		userListDAO.updateUserListProcessingStatus(createdList);
	}

}