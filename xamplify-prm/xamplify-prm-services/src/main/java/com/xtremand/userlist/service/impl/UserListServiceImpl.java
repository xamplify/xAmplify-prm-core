package com.xtremand.userlist.service.impl;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.activity.dto.EmailActivityDTO;
import com.xtremand.analytics.dao.PartnerAnalyticsDAO;
import com.xtremand.campaign.bom.DownloadDataInfo;
import com.xtremand.campaign.bom.DownloadDataInfo.DownloadItem;
import com.xtremand.campaign.bom.UnsubscribeUserDTO;
import com.xtremand.campaign.bom.UnsubscribedUser;
import com.xtremand.campaign.dto.ReceiverMergeTagsDTO;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.CompanyProfile.CompanyNameStatus;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.Criteria.OPERATION_NAME;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.company.bom.Company;
import com.xtremand.company.dao.CompanyProfileDao;
import com.xtremand.company.dto.CompanyProfileDTO;
import com.xtremand.company.service.CompanyProfileService;
import com.xtremand.dam.dao.DamDao;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.flexi.fields.bom.UserListFlexiField;
import com.xtremand.flexi.fields.dao.FlexiFieldDao;
import com.xtremand.flexi.fields.dto.FlexiFieldRequestDTO;
import com.xtremand.flexi.fields.dto.FlexiFieldResponseDTO;
import com.xtremand.form.bom.Form;
import com.xtremand.formbeans.AddPartnerResponseDTO;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.UserListDTO;
import com.xtremand.formbeans.UserListPaginationWrapper;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.gdpr.setting.service.GdprSettingService;
import com.xtremand.lead.dto.ContactLeadResponseDTO;
import com.xtremand.lms.dao.LMSDAO;
import com.xtremand.log.service.XamplifyLogService;
import com.xtremand.mail.service.AsyncComponent;
import com.xtremand.mail.service.EmailConstants;
import com.xtremand.mail.service.MailService;
import com.xtremand.module.dao.ModuleDao;
import com.xtremand.partner.bom.UpdatedContactsHistory;
import com.xtremand.partnership.bom.Partnership;
import com.xtremand.partnership.bom.PartnershipDTO;
import com.xtremand.partnership.dao.PartnershipDAO;
import com.xtremand.partnership.dto.PartnerTeamMemberGroupDTO;
import com.xtremand.partnership.service.PartnershipService;
import com.xtremand.partnership.service.impl.PartnershipServiceHelper;
import com.xtremand.team.dao.TeamDao;
import com.xtremand.team.member.group.dao.TeamMemberGroupDao;
import com.xtremand.team.member.group.service.TeamMemberGroupService;
import com.xtremand.user.bom.LegalBasis;
import com.xtremand.user.bom.Role;
import com.xtremand.user.bom.ShareLeadsDTO;
import com.xtremand.user.bom.ShareListPartner;
import com.xtremand.user.bom.SharedDetailsDTO;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.UserList;
import com.xtremand.user.bom.UserList.SocialNetwork;
import com.xtremand.user.bom.UserList.TYPE;
import com.xtremand.user.bom.UserListDetails;
import com.xtremand.user.bom.UserListUsersCount;
import com.xtremand.user.bom.UserPaginationWrapper;
import com.xtremand.user.bom.UserUserList;
import com.xtremand.user.bom.UserUserListWrapper;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.exception.UserDataAccessException;
import com.xtremand.user.list.dto.ContactsCSVDTO;
import com.xtremand.user.list.dto.ContactsCountDTO;
import com.xtremand.user.list.dto.CopyGroupUsersDTO;
import com.xtremand.user.service.UserService;
import com.xtremand.user.validation.UserListValidator;
import com.xtremand.userlist.dao.UserListDAO;
import com.xtremand.userlist.exception.UserListException;
import com.xtremand.userlist.service.UserListService;
import com.xtremand.util.DateUtils;
import com.xtremand.util.FileUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.ModuleCustomDTO;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.UserListOperationsAsyncDTO;
import com.xtremand.util.dto.XamplifyConstants;
import com.xtremand.util.service.CsvUtilService;
import com.xtremand.util.service.EmailValidatorService;
import com.xtremand.util.service.UtilService;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;
import com.xtremand.workflow.dao.WorkflowDAO;

@Service("userListService")
@Transactional
public class UserListServiceImpl implements UserListService {

	private static final String WHERE_UP_EMAIL_VALIDATION_IND_TRUE_AND_U_USER_LIST_ID_USER_LIST_ID = "where up.email_validation_ind=true  and u.user_list_id = :userListId";

	private static final String FROM_XT_USER_LIST_U_LEFT_JOIN_XT_USER_USERLIST_UUL_ON_U_USER_LIST_ID_UUL_USER_LIST_ID = "from xt_user_list u left join xt_user_userlist uul on u.user_list_id =uul.user_list_id ";

	private static final Logger logger = LoggerFactory.getLogger(UserListServiceImpl.class);

	@Autowired
	private UserListDAO userListDAO;

	@Autowired
	UserService userService;

	@Autowired
	private MailService mailService;

	@Autowired
	UserListValidator userListValidator;

	@Autowired
	private GenericDAO genericDAO;

	@Autowired
	private CompanyProfileDao companyDao;

	@Autowired
	EmailValidatorService emailValidatorService;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	GdprSettingService gdprSettingService;

	@Autowired
	@Lazy
	private AsyncComponent asyncComponent;

	@Autowired
	PartnerAnalyticsDAO partnerAnalyticsDAO;

	@Autowired
	private UtilService utilService;

	@Autowired
	private PartnershipService partnershipService;

	@Autowired
	PartnershipServiceHelper partnershipServiceHelper;

	@Autowired
	private PartnershipDAO partnershipDao;

	@Autowired
	private DamDao damDao;

	@Autowired
	private TeamDao teamDao;

	@Autowired
	private CsvUtilService csvUtilService;

	@Autowired
	private TeamMemberGroupService teamMemberGroupService;

	@Autowired
	private ModuleDao moduleDao;

	@Autowired
	private LMSDAO lmsDao;

	@Autowired
	private TeamMemberGroupDao teamMemberGroupDao;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	XamplifyLogService xamplifyLogService;

	@Autowired
	private WorkflowDAO workflowDao;

	@Autowired
	private CompanyProfileService companyProfileService;

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	@Autowired
	private UserDAO userDao;

	@Value("${duplicate.partner.email.id}")
	private String duplicatePartnerEmailIdMessage;

	@Value("${duplicate.partner.email.ids}")
	private String duplicatePartnerEmailIdsMessage;

	@Value("${partnership.already.exists.single}")
	private String partnershipAlreadyExists;

	@Value("${partnership.already.exists.multiple}")
	private String partnershipsAlreadyExists;

	@Value("${duplicate.organization.partners}")
	private String duplicateOranizationPartnersErrorMessage;

	@Value("${active.partner.list.name}")
	private String activePartnerListName;

	@Value("${inactive.partner.list.name}")
	private String inactivePartnerListName;

	@Value("${userlist.users.async.limit.min}")
	private Integer userListUsersAsyncMinLimit;

	@Autowired
	private FileUtil fileUtil;

	String messageTemplate = "<span><strong><u><contactlistname></u></strong> is being used in one or more campaigns ( <campaigns_names> ) which haven't been launched. Please launch or delete those campaigns first.</span>";

	private static final String FAILED = "Failed";
	private static final String INVALID_INPUT = "Invalid Input";

	@Autowired
	private FlexiFieldDao flexiFieldDao;

	@Override
	public UserList findByPrimaryKey(Serializable pk, FindLevel[] levels) throws UserListException {
		return userListDAO.findByPrimaryKey(pk, levels);
	}

	@Override
	public List<UserList> find(List<Criteria> criterias, FindLevel[] levels) throws UserListException {
		return (List<UserList>) userListDAO.find(criterias, levels);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse removeUserList(Integer id, Integer userId, boolean deleteTeamMemberPartnerList)
			throws UserListException {
		XtremandResponse xtremandResponse = new XtremandResponse();
		Criteria criteria = new Criteria("id", OPERATION_NAME.eq, id);
		List<Criteria> criterias = Arrays.asList(criteria);
		Collection<UserList> userLists = userListDAO.find(criterias,
				new FindLevel[] { FindLevel.CAMPAIGNS, FindLevel.COMPANY_PROFILE });
		UserList userList = userLists.iterator().next();
		boolean access = false;
		if (userList.getModuleName().equalsIgnoreCase("SHARE LEADS")) {
			access = utilService.hasShareLeadsModuleAccess(userId);
		} else {
			access = getAccess(userList.isPartnerUserList(), userId, false);
		}
		if (Boolean.TRUE.equals(userList.isPartnerUserList())) {
			userList.setDeleteTeamMemberPartnerList(deleteTeamMemberPartnerList);
			xtremandResponse = removePartnerList(access, userList, xtremandResponse);
		} else if (userList.getModuleName().equalsIgnoreCase("SHARE LEADS")) {
			xtremandResponse = removeSharedLeadsList(access, userList, userId, xtremandResponse);
		} else {
			xtremandResponse = removeContactList(access, userList, xtremandResponse);
		}
		damDao.deleteFromDamPartnerGroupMappingAndDamPartnerByUserListId(id, userId);
		return xtremandResponse;
	}

	private XtremandResponse removePartnerList(boolean access, UserList userList, XtremandResponse xtremandResponse) {
		if (access) {
			partnershipService.deletePartnerList(userList);
			xtremandResponse.setAccess(true);
		} else {
			xtremandResponse.setAccess(false);
		}
		return xtremandResponse;
	}

	@Override
	public XtremandResponse removeMarketoMasterContactList(Integer companyId) throws UserListException {
		UserList userList = userListDAO.getMarketoMasterContactList(companyId);
		return removeContactList(true, userList, new XtremandResponse());
	}

	@Override
	public XtremandResponse removeContactList(boolean access, UserList userList, XtremandResponse xtremandResponse)
			throws UserListException {
		if (access) {
			if (!(userList.isDefaultPartnerList())) {
				userListDAO.deleteByPrimaryKey(userList.getId());
			}
			xtremandResponse.setAccess(true);
		} else {
			xtremandResponse.setAccess(false);
		}
		return xtremandResponse;
	}

	public XtremandResponse removeSharedLeadsList(boolean access, UserList userList, Integer vendorId,
			XtremandResponse xtremandResponse) throws UserListException {
		if (access) {
			Integer userListId = userList.getId();
			if (userList.getCompany() != null) {
				Integer partnerCompanyId = userList.getCompany().getId();
				Integer vendorCompanyId = userDAO.getCompanyIdByUserId(vendorId);
				Integer partnershipId = partnershipDao
						.getPartnershipIdByVendorCompanyIdAndPartnerCompanyId(vendorCompanyId, partnerCompanyId);
				List<Integer> sharedPartnerIds = userListDAO.getSharedPartnerIds(userListId, partnershipId);
				Integer shareListPartnerId = userListDAO.getShareListPartnerId(partnershipId, userListId);
				userListDAO.deleteShareListPartnerMapping(shareListPartnerId);
				userListDAO.deleteShareListPartner(shareListPartnerId);
				if (!(userList.isDefaultPartnerList())) {
					deleteByPrimaryKey(userList.getId());
				}

				for (Integer partnerId : sharedPartnerIds) {
					User partner = userService.loadUser(
							Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, partnerId)),
							new FindLevel[] { FindLevel.SHALLOW });
					mailService.sendLeadsListDeleteMail(partner, userList, "lead_list_delete_message", null);
				}
			} else {
				deleteByPrimaryKey(userList.getId());
			}

			xtremandResponse.setAccess(true);
		} else {
			xtremandResponse.setAccess(false);
		}
		return xtremandResponse;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public Map<String, Object> updateUserList(UserUserListWrapper userUserListWrapper, Integer customerId,
			String companyProfileName, UserListOperationsAsyncDTO userListOperationsAsyncDTO) throws UserListException {
		String debugMessage = "Entered into updateUserList(userListId :" + userUserListWrapper.getUserList().getId()
				+ " - loggedInUserId : " + customerId + ")";
		logger.debug(debugMessage);
		Map<String, Object> resultMap = new HashMap<>();
		List<AddPartnerResponseDTO> addPartnerResponse = new ArrayList<>();
		UserListDTO userListDto = userUserListWrapper.getUserList();
		Set<UserDTO> users = userUserListWrapper.getUsers();
		Integer statusCode = null;
		List<String> invalidEmailIds = new ArrayList<>();
		if (users != null && !users.isEmpty() && userListDto.getId() != null && userListDto.getId() > 0
				&& customerId != null && customerId > 0) {
			UserList userList = userListDAO.findByPrimaryKey(userListDto.getId(),
					new FindLevel[] { FindLevel.COMPANY_PROFILE, FindLevel.USER_USER_LIST });
			boolean assignLeads = false;
			if (userList.getModuleName().equalsIgnoreCase("SHARE LEADS")) {
				assignLeads = true;
			}
			boolean access = getAccess(userList.isPartnerUserList(), customerId, assignLeads);
			if (!access) {
				resultMap.put("access", false);
				return resultMap;
			}
			if (userList != null) {

				if ((userList.isPartnerUserList() != null && !userList.isPartnerUserList())
						&& (userList.getForm() != null)) {
					UserDTO userDTO = users.iterator().next();
					boolean isUserExists = userListDAO.isUserExists(userListDto.getId(), userDTO.getEmailId());
					if (isUserExists) {
						return getUpdateContactListResult();
					}
				}

				String csvPath = "";
				if (userList.getModuleName().equalsIgnoreCase("SHARE LEADS")) {
					csvPath = fileUtil.uploadContactsCsvFile(userList.getName(), users,
							userList.getAssignedCompany().getId());
				} else {
					csvPath = fileUtil.uploadContactsCsvFile(userList.getName(), users, userList.getCompany().getId());
				}
				userList.setCsvPath(csvPath);
				String csvPathMessage = "Updated Csv Uploaded To " + csvPath + " For User List Id "
						+ userListDto.getId();
				logger.debug(csvPathMessage);
				userService.removeNullEmailUserDTOs(users);
				if (userList.isPartnerUserList() != null && userList.isPartnerUserList()) {
					userListOperationsAsyncDTO.setPartnerList(true);
					Map<String, Object> map = partnershipService.updatePartnerList(userList, users, customerId,
							companyProfileName, userListOperationsAsyncDTO);
					addPartnerResponse = (List<AddPartnerResponseDTO>) map.get("responseDTOList");
					invalidEmailIds = (List<String>) map.get("invalidEmailAddresses");
					statusCode = (Integer) map.get("statusCode");
					userListOperationsAsyncDTO.setStatusCode(statusCode);
				} else {
					if (userList.getForm() != null) {
						UserDTO userDTO = users.iterator().next();
						boolean isUserExists = userListDAO.isUserExists(userListDto.getId(), userDTO.getEmailId());
						if (isUserExists) {
							return getUpdateContactListResult();
						}
					}
					XtremandResponse xtremandResponse = new XtremandResponse();
					xtremandResponse = saveContactList(users, customerId, userListDto, xtremandResponse);
					resultMap.put("statusCode", xtremandResponse.getStatusCode());
					if (xtremandResponse.getStatusCode() == 200
							&& userListDto.getModuleName().equalsIgnoreCase("SHARE LEADS")) {
						resultMap.put("message", "LEADS LIST UPDATED SUCCESSFULLY");
					} else if (xtremandResponse.getStatusCode() == 200
							&& userListDto.getModuleName().equalsIgnoreCase("CONTACTS")) {
						resultMap.put("message", "CONTACT LIST UPDATED SUCCESSFULLY");
					} else if (xtremandResponse.getStatusCode() == 402) {
						resultMap.put("message", xtremandResponse.getMessage() + " " + xtremandResponse.getData());
					} else if (xtremandResponse.getStatusCode() == 401) {
						resultMap.put("message", xtremandResponse.getMessage());
					}
					resultMap.put("access", true);
					return resultMap;
				}
			}
		}

		Pagination pagination = new Pagination();
		pagination.setPageIndex(1);
		pagination.setMaxResults(12);
		if (addPartnerResponse != null && !addPartnerResponse.isEmpty()) {
			resultMap.put("detailedResponse", addPartnerResponse);
			boolean skipped = false;
			if (addPartnerResponse.size() == 1) {
				AddPartnerResponseDTO addPartnerResponseDTO = addPartnerResponse.get(0);
				if (addPartnerResponseDTO.getStatus().equals("SKIPPED")) {
					resultMap.put("statusCode", 417);
					skipped = true;
				}
			}
			if (!skipped) {
				resultMap.put("message", "LIST UPDATED SUCCESSFULLY");
				resultMap.put("statusCode", 200);
				userListOperationsAsyncDTO.setStatusCode(200);
			}
		}

		if (statusCode != null && statusCode == 418) {
			resultMap.put("statusCode", 418);
		}
		resultMap.put("invalidEmailIds", invalidEmailIds);
		resultMap.put("access", true);
		return resultMap;
	}

	public Map<String, Object> updateContactList(UserList userList, Set<UserDTO> users, Integer customerId) {
		String updatingContactListDebugMessage = "updateContactList(" + userList.getId() + ") started...";
		logger.debug(updatingContactListDebugMessage);
		if (userList.getForm() != null) {
			UserDTO userDTO = users.iterator().next();
			boolean isUserExists = userListDAO.isUserExists(userList.getId(), userDTO.getEmailId());
			if (isUserExists) {
				return getUpdateContactListResult();
			}
		}
		userList.initialiseCommonFields(false, customerId);
		User customer = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, customerId)),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		List<User> allUsers = new ArrayList<>();
		List<User> nonProcessedUsers = new ArrayList<>();
		/* XNFR-211 */
		Set<String> insertedEmails = new HashSet<>();
		if (!users.isEmpty()) {
			int totalUsers = users.size();
			int counter = 1;
			for (UserDTO userDTO : users) {
				String emailId = userDTO.getEmailId();
				String userProcessingMessage = counter + "/" + totalUsers + " . Uploading " + emailId;
				logger.debug(userProcessingMessage);
				/* XNFR-211 */
				if (!insertedEmails.contains(emailId)) {
					boolean isNonProcessedUser = userService.isNonProcessedUser(emailId);
					logger.debug("createUserWithEmail() is called.");
					User insertedUser = userService.createUserWithEmail(userDTO, userList, customerId);
					String newUserCreatedDebugMessage = counter + "/" + totalUsers + " . New Account Created For "
							+ emailId;
					logger.debug(newUserCreatedDebugMessage);
					allUsers.add(insertedUser);
					if (isNonProcessedUser) {
						nonProcessedUsers.add(insertedUser);
					}
				}
				counter++;
			}
		}

		genericDAO.flushCurrentSession();

		if (userList.getModuleName().equalsIgnoreCase("SHARE LEADS")) {
			List<Integer> totalUnsubscribedUserIds = userListDAO
					.getUnsubscribedUsers(userList.getAssignedCompany().getId());
			if (!userList.isEmailValidationInd() || !(nonProcessedUsers.isEmpty())) {
				mailService.sendLeadsListMail(customer, EmailConstants.LEADS_LIST_IN_PROCESS, userList,
						totalUnsubscribedUserIds);
			} else if (userList.isEmailValidationInd()) {
				mailService.sendLeadsListMail(customer, EmailConstants.LEADS_LIST_UPDATED, userList,
						totalUnsubscribedUserIds);
			}
		} else {
			List<Integer> totalUnsubscribedUserIds = userListDAO.getUnsubscribedUsers(userList.getCompany().getId());
			if (!userList.isEmailValidationInd() || !(nonProcessedUsers.isEmpty())) {
				mailService.sendContactListMail(customer, EmailConstants.CONTACT_LIST_IN_PROCESS, userList,
						totalUnsubscribedUserIds);
			} else if (userList.isEmailValidationInd()) {
				mailService.sendContactListMail(customer, EmailConstants.CONTACT_LIST_UPDATED, userList,
						totalUnsubscribedUserIds);
			}
		}

		Map<String, Object> resultMap = getUpdateContactListResult();
		if (userList.getModuleName().equalsIgnoreCase("SHARE LEADS")) {
			resultMap.put("message", "LEADS LIST UPDATED SUCCESSFULLY");
		} else {
			resultMap.put("message", "CONTACT LIST UPDATED SUCCESSFULLY");
		}
		String updatingContactListCompletedMessage = "updateContactList(" + userList.getId() + ") ended...";
		logger.debug(updatingContactListCompletedMessage);
		// xnfr-490
		return resultMap;
	}

	public Map<String, Object> getUpdateContactListResult() {
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("statusCode", 200);
		resultMap.put("access", true);
		return resultMap;
	}

	public boolean getAccess(boolean isPartnerUserList, Integer userId, boolean assignLeads) {
		boolean access = false;
		if (assignLeads) {
			access = utilService.hasShareLeadsModuleAccess(userId);
		} else if (isPartnerUserList) {
			access = utilService.hasPartnerModuleAccess(userId);
		}
		return access;
	}

	@Override
	/***********
	 * XNFR-85 (Added @Transactional Annotation By Sravan For XNFR-85)
	 **********/
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public Map<String, Object> updateUserDetails(UserPaginationWrapper userPaginationWrapper, Integer userListId,
			Integer userId) throws UserDataAccessException {
		Map<String, Object> resultMap = new HashMap<>();
		User customer = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, userId)),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		UserList userList = genericDAO.get(UserList.class, userListId);
		boolean access = findAccess(userId, userList);
		UserListOperationsAsyncDTO userListOperationsAsyncDTO = new UserListOperationsAsyncDTO();
		userListOperationsAsyncDTO.setUserListId(userListId);
		userListOperationsAsyncDTO.setVendorCompanyId(customer.getCompanyProfile().getId());
		userListOperationsAsyncDTO.setEditPartner(true);
		userListOperationsAsyncDTO.setPartnerList(true);
		userListOperationsAsyncDTO.getAllPartnerListIds().add(userListId);
		try {
			if (access) {
				User user = userPaginationWrapper.getUser();
				Pagination pagination = userPaginationWrapper.getPagination();
				Set<UserUserList> userUserLists = userList.getUserUserLists();
				List<Integer> legalBasisIds = new ArrayList<>();
				legalBasisIds.addAll(user.getLegalBasis());
				if (user.getUserId() != null) {
					iterateUserUserListAndUpdateProperties(userListId, customer, userList, user, userUserLists);
					/*********** Update Contacts Limit & MDF Amount *****************/
					updateSetValues(userPaginationWrapper, userId, customer, userList, user, pagination, legalBasisIds,
							userListOperationsAsyncDTO);

					resultMap.put("access", true);
				}
			} else {
				resultMap.put("access", false);
			}
			resultMap.put("message", "User Details UPDATED SUCCESSFULLY");
			return resultMap;

		} catch (UserDataAccessException e) {
			throw new UserDataAccessException(e.getMessage());
		} catch (Exception e) {
			throw new UserDataAccessException(e.getMessage());
		}
	}

	private void iterateUserUserListAndUpdateProperties(Integer userListId, User customer, UserList userList, User user,
			Set<UserUserList> userUserLists) throws IllegalAccessException, InvocationTargetException {
		for (UserUserList userUserList : userUserLists) {
			if (userUserList.getUser() != null && user != null && userUserList.getUserList() != null
					&& userUserList.getUser().getUserId().intValue() == user.getUserId().intValue()
					&& userUserList.getUserList().getId().intValue() == userListId.intValue()) {
				setUserListProperties(user, userUserList);
				boolean isContactsModule = validateModuleAndUpdateLegalBasisOptions(customer, userList, user,
						userUserList);
				if (isContactsModule) {
					UserDTO userDTO = new UserDTO();
					BeanUtils.copyProperties(userDTO, user);
				}
				break;
			}
		}
	}

	private boolean findAccess(Integer userId, UserList userList) {
		boolean access = false;
		if (userList.getModuleName().equalsIgnoreCase("SHARE LEADS")) {
			access = utilService.hasShareLeadsModuleAccess(userId);
		} else {
			access = getAccess(userList.isPartnerUserList(), userId, false);
		}
		return access;
	}

	private void setUserListProperties(User user, UserUserList userUserList) {
		userUserList.setFirstName(user.getFirstName());
		userUserList.setLastName(user.getLastName());
		String contactCompany = user.getContactCompany();
		if (org.springframework.util.StringUtils.hasText(contactCompany)) {
			userUserList.setContactCompany(contactCompany.trim());
		} else {
			userUserList.setContactCompany(contactCompany);
		}
		userUserList.setJobTitle(user.getJobTitle());
		userUserList.setAddress(user.getAddress());
		userUserList.setCity(user.getCity());
		userUserList.setMobileNumber(user.getMobileNumber());
		userUserList.setDescription(user.getDescription());
		userUserList.setCountry(user.getCountry());
		userUserList.setState(user.getState());
		userUserList.setZipCode(user.getZipCode());
		userUserList.setVertical(user.getVertical());
		userUserList.setRegion(user.getRegion());
		userUserList.setPartnerType(user.getPartnerType());
		userUserList.setCategory(user.getCategory());
		userUserList.setAccountName(user.getAccountName());
		userUserList.setAccountOwner(user.getAccountOwner());
		userUserList.setAccountSubType(user.getAccountSubType());
		userUserList.setCompanyDomain(user.getCompanyDomain());
		userUserList.setTerritory(user.getTerritory());
		userUserList.setWebsite(user.getWebsite());
		Company company = null;
		Integer contactCompanyId = user.getContactCompanyId();
		if (XamplifyUtils.isValidInteger(contactCompanyId)) {
			company = genericDAO.get(Company.class, contactCompanyId);
		}
		userUserList.setCompany(company);
		userUserList.setCountryCode(user.getCountryCode());
		userUserList.setContactStatusId(user.getContactStatusId());
		// XNFR-680
		updateFlexiFields(user, userUserList);
	}

	private void updateFlexiFields(User user, UserUserList userUserList) {
		Set<UserListFlexiField> userListFlexiFields = userUserList.getUserListFlexiFields();
		List<FlexiFieldRequestDTO> dtoFlexiFields = user.getFlexiFields();
		Set<UserListFlexiField> updatedFlexiFields = new HashSet<>();
		if (XamplifyUtils.isNotEmptySet(userListFlexiFields)) {
			updateExistingFlexiFields(userUserList, userListFlexiFields, dtoFlexiFields, updatedFlexiFields);
		} else {
			userService.setUserListFlexiFields(userUserList, dtoFlexiFields, updatedFlexiFields);
		}
		userUserList.setUserListFlexiFields(updatedFlexiFields);
	}

	private void updateExistingFlexiFields(UserUserList userUserList, Set<UserListFlexiField> userListFlexiFields,
			List<FlexiFieldRequestDTO> dtoFlexiFields, Set<UserListFlexiField> updatedFlexiFields) {
		dtoFlexiFields.forEach(dtoFlexiField -> {
			UserListFlexiField userListFlexiField = findMatchingFlexiField(userListFlexiFields, dtoFlexiField);
			if (userListFlexiField != null) {
				userListFlexiField.setFlexiFieldValue(dtoFlexiField.getFieldValue());
				updatedFlexiFields.add(userListFlexiField);
			} else {
				userService.createUserListFlexiField(userUserList, updatedFlexiFields, dtoFlexiField);
			}
		});
	}

	private UserListFlexiField findMatchingFlexiField(Set<UserListFlexiField> userListFlexiFields,
			FlexiFieldRequestDTO dtoFlexiField) {
		return userListFlexiFields.stream().filter(userListFlexiField -> dtoFlexiField.getFieldName()
				.equals(userListFlexiField.getFlexiField().getFieldName())).findFirst().orElse(null);
	}

	private boolean validateModuleAndUpdateLegalBasisOptions(User customer, UserList userList, User user,
			UserUserList userUserList) {
		boolean isGdprEnabled = false;
		boolean isShareLeadsModule = userList.getModuleName().equalsIgnoreCase("SHARE LEADS");
		boolean isContactsModule = userList.getModuleName().equalsIgnoreCase("CONTACTS");
		boolean isPartnersModule = userList.getModuleName().equalsIgnoreCase("PARTNERS");
		if (isShareLeadsModule) {
			isGdprEnabled = gdprSettingService.isGdprEnabled(customer.getCompanyProfile().getId());
		} else if (isContactsModule || isPartnersModule) {
			isGdprEnabled = gdprSettingService.isGdprEnabled(userList.getCompany().getId());
		}
		boolean isLegalBasisCanBeUpated = isGdprEnabled && (isShareLeadsModule || isContactsModule || isPartnersModule);
		if (isLegalBasisCanBeUpated) {
			updateLegalBasis(userUserList, user.getLegalBasis());
		}
		return isContactsModule;
	}

	private void updateSetValues(UserPaginationWrapper userPaginationWrapper, Integer userId, User customer,
			UserList userList, User user, Pagination pagination, List<Integer> legalBasisIds,
			UserListOperationsAsyncDTO userListOperationsAsyncDTO) {
		Boolean partnerList = userList.isPartnerUserList();
		if (Boolean.TRUE.equals(partnerList)) {
			Integer vendorCompanyId = userList.getCompany().getId();
			Integer partnerId = user.getUserId();
			/*********** XNFR-85 & XNFR-98 *****************/
			Integer partnerCompanyId = pagination.getPartnerCompanyId();
			if (partnerCompanyId != null && partnerCompanyId > 0) {
				CompanyProfile partnerCompanyProfile = genericDAO.get(CompanyProfile.class, partnerCompanyId);
				if (partnerCompanyProfile.getCompanyNameStatus() == CompanyNameStatus.INACTIVE
						&& Integer.compare(vendorCompanyId, partnerCompanyProfile.getAddedAdminCompanyId()) == 0) {
					partnerCompanyProfile.setCompanyName(user.getContactCompany().trim());
				}
			} else if (partnerCompanyId == 0) {
				UserDTO userDTO = new UserDTO();
				userDTO.setContactCompany(user.getContactCompany().trim());
				userDTO.setCity(user.getCity());
				userDTO.setState(user.getState());
				userDTO.setZipCode(user.getZipCode());
				userDTO.setCountry(user.getCountry());
				userDTO.setAddress(user.getAddress());
				CompanyProfile companyProfile = companyProfileService.createPartnerCompany(userDTO, vendorCompanyId);
				User representingPartner = genericDAO.get(User.class, partnerId);
				Partnership partnership = partnershipServiceHelper.getPartnership(representingPartner,
						customer.getCompanyProfile());
				partnership.setPartnerCompany(companyProfile);
				user.setCompanyProfile(companyProfile);
				userDAO.updateTeamMembersCompanyProfile(partnerId, companyProfile.getId());

			}

			partnershipServiceHelper.updatePartnerTeamMemberGroupMapping(
					userPaginationWrapper.getUser().getSelectedTeamMemberIds(), vendorCompanyId, partnerId, userId,
					true, user, legalBasisIds, userPaginationWrapper.getUser().getTeamMemberGroupId(),
					userListOperationsAsyncDTO);

		}
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

	@Override
	public XtremandResponse renameUserList(UserListDTO userListDTO, Integer userId) throws UserListException {
		try {
			XtremandResponse xtremandResponse = new XtremandResponse();
			String userListName = userListDTO.getName().trim();
			String modifiedUserListName = userListName.replaceAll("'", "''");
			userListDTO.setName(modifiedUserListName);
			Integer userCompanyId = userDAO.getCompanyIdByUserId(userId);
			userListDTO.setCompanyId(userCompanyId);
			boolean isUserListNameExists = userListDAO.isExistingUserListNameExists(userListDTO);
			if (!isUserListNameExists) {
				UserList userList = genericDAO.get(UserList.class, userListDTO.getId());
				userList.setName(userListName);
				userList.setPublicList(userListDTO.getPublicList());
				xtremandResponse.setStatusCode(200);
				xtremandResponse.setMessage("List name updated successfully");
			} else {
				xtremandResponse.setStatusCode(1001);
				xtremandResponse.setMessage("This list name is already taken.");
			}
			return xtremandResponse;
		} catch (Exception e) {
			logger.error("Unable to Update userList Details", e);
			throw new UserDataAccessException(e.getMessage());
		}
	}

	@Override
	public Map<String, Object> userlists(Integer userId, Pagination pagination) throws UserListException {
		User user = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, userId)),
				new FindLevel[] { FindLevel.ROLES, FindLevel.COMPANY_PROFILE });

		List<Integer> userListIds = new ArrayList<>();
		List<Criterion> criterions = null;

		if (pagination.getFilterKey() != null && pagination.getFilterKey().equals("isPartnerUserList")
				&& !Boolean.valueOf(pagination.getFilterValue().toString())) {
			criterions = userListDAO.getContactsCriterias(user, pagination);
		} else {
			List<Criteria> criterias = new ArrayList<>();

			if (user.getRoles().stream().anyMatch((role) -> (role.getRoleId()) == Role.COMPANY_PARTNER.getRoleId())
					|| user.getRoles().stream()
							.anyMatch((role) -> ((role.getRoleId()) == Role.ALL_ROLES.getRoleId()
									|| (role.getRoleId()) == Role.PARTNERS.getRoleId()
									|| Role.PRM_ROLE.getRoleId().equals(role.getRoleId())))) {
				Criteria criteria = new Criteria("company.id", OPERATION_NAME.eq, user.getCompanyProfile().getId());
				criterias.add(criteria);
			}

			if (pagination.getFilterKey() != null && pagination.getFilterKey().equals("isPartnerUserList")) {
				Criteria criteria2 = new Criteria("isPartnerUserList", OPERATION_NAME.eq,
						Boolean.valueOf(pagination.getFilterValue().toString()));
				criterias.add(criteria2);
			}
			criterions = userListDAO.generateCriteria(criterias);
		}

		if (criterions != null) {
			userListIds = userListDAO.getUserListIds(criterions, pagination);
		}

		Map<String, Object> resultMap = new HashMap<>();

		if (pagination.isSharedLeads()) {
			resultMap = listSharedLists(user, pagination);
			/**** XNFR-266 ***/
			boolean downloadAccess = false;
			boolean vanityUrlFilter = pagination.isVanityUrlFilter();
			String vendorCompanyProfileName = utilDao.getPrmCompanyProfileName();
			VanityUrlDetailsDTO vanityUrlDetailsDTO = new VanityUrlDetailsDTO();
			vanityUrlDetailsDTO.setUserId(userId);
			vanityUrlDetailsDTO.setVendorCompanyProfileName(vendorCompanyProfileName);
			vanityUrlDetailsDTO.setVanityUrlFilter(vanityUrlFilter);
			utilService.isVanityUrlFilterApplicable(vanityUrlDetailsDTO);
			if (vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl()) {
				Integer vendorCompanyId = vanityUrlDetailsDTO.getVendorCompanyId();
				downloadAccess = utilDao.isPrmByVendorCompanyId(vendorCompanyId);
			}
			resultMap.put("downloadAccess", downloadAccess);
			/**** XNFR-266 ***/
		} else if (userListIds != null && !userListIds.isEmpty()) {
			resultMap = listUserLists(userListIds, pagination);
		} else {
			resultMap.put("listOfUserLists", new ArrayList<>());
			resultMap.put("totalRecords", 0);
			resultMap.put("downloadAccess", false);
		}
		return resultMap;
	}

	private UserListDTO convertUserListDTO(UserList userList) {
		UserListDTO userListDto = new UserListDTO();
		if (userList != null) {
			userListDto.setId(userList.getId());
			userListDto.setName(userList.getName());

			if (userList.getModuleName().equalsIgnoreCase("SHARE LEADS")) {
				userListDto.setUploadedBy(getCustomerFullName(userList.getAssignedBy()));
				userListDto.setCompanyName(userList.getAssignedBy().getCompanyProfile().getCompanyName());
				userListDto.setUploadedUserId(userList.getAssignedBy().getUserId());
				userListDto.setAssignedLeadsList(true);
				userListDto.setAssignedToPartner(userList.getOwner() != null ? true : false);
				userListDto.setAssignedDate(DateUtils.getUtcString(userList.getAssignedDate()));
			} else {
				userListDto.setUploadedBy(getCustomerFullName(userList.getOwner()));
				userListDto.setCompanyName(userList.getOwner().getCompanyProfile().getCompanyName());
				userListDto.setUploadedUserId(userList.getOwner().getUserId());
				userListDto.setAssignedLeadsList(false);
			}

			userListDto.setCreatedDate(DateUtils.getUtcString(userList.getCreatedTime()));
			userListDto.setSocialNetwork(userList.getSocialNetwork().name());
			userListDto.setContactType(userList.getContactType().name());
			userListDto.setAlias(userList.getAlias());
			userListDto.setNoOfContacts(userList.getUsers().stream().filter(u -> u.isEmailValidationInd() == true)
					.collect(Collectors.toList()).size());
			userListDto.setSynchronisedList(userList.isSynchronisedList());
			userListDto.setPartnerUserList(userList.isPartnerUserList());
			userListDto.setDefaultPartnerList(userList.isDefaultPartnerList());
			userListDto.setEmailValidationInd(userList.isEmailValidationInd());
			userListDto.setPublicList(userList.getPublicList());
			userListDto.setMarketoMasterList(userList.isMarketoMasterList());
			userListDto.setMarketoSyncComplete(userList.isMarketoSyncComplete());
			userListDto.setTeamMemberPartnerList(userList.isTeamMemberPartnerList());
		}
		return userListDto;
	}

	private String getCustomerFullName(User user) {
		String name = null;
		if (user.getFirstName() != null) {
			if (user.getLastName() != null) {
				name = user.getFirstName() + " " + user.getLastName();
			} else {
				name = user.getFirstName();
			}
		} else if (user.getLastName() != null) {
			name = user.getLastName();
		} else {
			name = user.getEmailId();
		}
		return name;
	}

	@Override
	public Map<String, Object> listAllUserListContacts(UserListPaginationWrapper userListPaginationWrapper,
			Integer userId) {
		User user = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, userId)),
				new FindLevel[] { FindLevel.ROLES, FindLevel.COMPANY_PROFILE });
		Pagination pagination = userListPaginationWrapper.getPagination();
		UserListDTO userListDTO = userListPaginationWrapper.getUserList();
		List<Integer> userListIds = getUserListIds(user, userListDTO);
		pagination.setUserId(userId);
		pagination.setTeamMemberAnalytics(true);
		if (userListIds.size() > 10000 && userListDTO.getModuleName().equalsIgnoreCase("contacts")) {
			userListIds = utilDao.getDefaultMasterContactListIdByUserId(userId);
		}
		if (userListIds != null && !userListIds.isEmpty()) {
			pagination.setCampaignName(userListDTO.getModuleName());
			/** XBI-2959 ***/
			setLoggedInThroughVanityUrlOption(pagination);
			return listUserListscontacts(pagination, userListIds, user, userListDTO);
		} else {
			Map<String, Object> resultMap = new HashMap<>();
			List<UserDTO> emptyList = new ArrayList<>();
			resultMap.put("listOfUsers", emptyList);
			resultMap.put("totalRecords", 0);
			return resultMap;
		}
	}

	/** XBI-2959 ***/
	private void setLoggedInThroughVanityUrlOption(Pagination pagination) {
		String vendorCompanyProfileName = utilDao.getPrmCompanyProfileName();
		if (XamplifyUtils.isValidString(vendorCompanyProfileName)) {
			VanityUrlDetailsDTO vanityUrlDetailsDTO = new VanityUrlDetailsDTO();
			vanityUrlDetailsDTO.setVanityUrlFilter(true);
			vanityUrlDetailsDTO.setVendorCompanyProfileName(vendorCompanyProfileName);
			vanityUrlDetailsDTO.setUserId(pagination.getUserId());
			utilService.isVanityUrlFilterApplicable(vanityUrlDetailsDTO);
			pagination.setLoggedInThroughOwnVanityUrl(vanityUrlDetailsDTO.isVendorLoggedInThroughOwnVanityUrl());
			pagination.setLoggedInThroughVendorVanityUrl(vanityUrlDetailsDTO.isPartnerLoggedInThroughVanityUrl());
			pagination.setVendorCompanyId(vanityUrlDetailsDTO.getVendorCompanyId());
		}
	}

	/************ XNFR-117 **************/
	private List<Integer> findContactsListsForDeletedPartners(Integer userId, List<Integer> userListIds,
			List<Criteria> criterias, User user) {
		List<Integer> roleIds = user.getRoles().stream().map(Role::getRoleId).collect(Collectors.toList());
		if (roleIds.size() == 1 && roleIds.indexOf(Role.USER_ROLE.getRoleId()) > -1) {
			List<Integer> userIdList = userService.getCompanyUserIds(userId);
			Integer[] userIdArray = ArrayUtils.toObject(userIdList.stream().mapToInt(i -> i).toArray());
			Criteria criteria = new Criteria("owner.userId", OPERATION_NAME.in, userIdArray);
			criterias.add(criteria);
			List<Criterion> criterions = userListDAO.generateCriteria(criterias);
			userListIds = userListDAO.getUserListIds(criterions);
		}
		return userListIds;
	}

	@SuppressWarnings("unused")
	@Override
	public Map<String, Object> listuserListContacts(UserListPaginationWrapper userListPaginationWrapper,
			Integer userId) {
		User loggedInUser = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, userId)),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		Pagination pagination = userListPaginationWrapper.getPagination();
		UserListDTO userListDTO = userListPaginationWrapper.getUserList();
		List<Integer> userListIds = new ArrayList<>();
		userListIds.add(userListDTO.getId());
		if (userListDTO != null) {
			return listUserListscontacts(pagination, userListIds, loggedInUser, userListDTO);
		} else {
			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("listOfUsers", Collections.emptyList());
			resultMap.put("totalRecords", 0);
			return resultMap;
		}

	}

	public Map<String, Object> getContactsCount(List<Integer> userListIds, Map<String, Object> resultMap, User user,
			UserListDTO userListDTO) {
		ContactsCountDTO contactsCountDTO = null;
		if (userListIds != null && !userListIds.isEmpty()) {
			contactsCountDTO = userListDAO.getContactsCount(userListIds, user.getCompanyProfile().getId(), userListDTO,
					user.getUserId());
			resultMap.put("allcontacts", contactsCountDTO.getAllCounts());
			resultMap.put("activecontacts", contactsCountDTO.getActiveCount());
			resultMap.put("nonactiveUsers", contactsCountDTO.getInActiveCount());
			resultMap.put("invalidUsers", contactsCountDTO.getInValidCount());
			resultMap.put("unsubscribedUsers", contactsCountDTO.getUnSubscribedCount());
			resultMap.put("excluded", contactsCountDTO.getExcludedCount());

		} else {
			resultMap.put("allcontacts", 0);
			resultMap.put("activecontacts", 0);
			resultMap.put("nonactiveUsers", 0);
			resultMap.put("invalidUsers", 0);
			resultMap.put("unsubscribedUsers", 0);
			resultMap.put("excluded", 0);
		}
		return resultMap;
	}

	public Map<String, Object> userListUsersCount(Integer userListId, Map<String, Object> resultMap) {
		UserListUsersCount userListUsersCount = userListDAO.getUserListUsersCount(userListId);
		resultMap.put("allcontacts", userListUsersCount.getAllUsersCount());
		resultMap.put("activecontacts", userListUsersCount.getActiveUsersCount());
		resultMap.put("nonactiveUsers", userListUsersCount.getNonActiveUsersCount());
		resultMap.put("invalidUsers", userListUsersCount.getInvalidUsersCount());
		resultMap.put("unsubscribedUsers", userListUsersCount.getUnsubscribedUsersCount());
		Integer activecontacts = (Integer) resultMap.get("activecontacts");
		Integer nonactiveUsers = (Integer) resultMap.get("nonactiveUsers");
		resultMap.put("validContactsCount", activecontacts + nonactiveUsers);
		return resultMap;
	}

	public Map<String, List<User>> getSubscribedAndUnsubscribedUsers(List<User> exUsers) {
		List<UnsubscribedUser> totalUnsubscribedUsers = genericDAO.load(UnsubscribedUser.class);
		List<Integer> totalUnsubscribedUserIds = totalUnsubscribedUsers.stream().map(UnsubscribedUser::getUserId)
				.collect(Collectors.toList());
		List<User> unsubscribedUsers = exUsers.stream()
				.filter(exuser -> totalUnsubscribedUserIds.contains(exuser.getUserId())).collect(Collectors.toList());
		List<Integer> unsubscribedUserIds = unsubscribedUsers.stream().map(User::getUserId)
				.collect(Collectors.toList());
		exUsers.removeIf(user -> unsubscribedUserIds.contains(user.getUserId()));
		Map<String, List<User>> map = new HashMap<>();
		map.put("subscribedUsers", exUsers.stream().distinct().collect(Collectors.toList()));
		map.put("unsubscribedUsers", unsubscribedUsers.stream().distinct().collect(Collectors.toList()));
		return map;
	}

	public Map<String, List<User>> getSubscribedAndUnsubscribedUsers(List<User> exUsers, Integer customerCompanyId) {
		List<UnsubscribedUser> totalUnsubscribedUsers = genericDAO.load(UnsubscribedUser.class);
		List<Integer> totalUnsubscribedUserIds = totalUnsubscribedUsers.stream()
				.filter(unsubUser -> Integer.compare(unsubUser.getCustomerCompanyId(), customerCompanyId) == 0)
				.map(UnsubscribedUser::getUserId).collect(Collectors.toList());
		List<User> unsubscribedUsers = exUsers.stream()
				.filter(exuser -> totalUnsubscribedUserIds.contains(exuser.getUserId())).collect(Collectors.toList());
		List<Integer> unsubscribedUserIds = unsubscribedUsers.stream().map(User::getUserId)
				.collect(Collectors.toList());
		exUsers.removeIf(user -> unsubscribedUserIds.contains(user.getUserId()));
		Map<String, List<User>> map = new HashMap<>();
		map.put("subscribedUsers", exUsers.stream().distinct().collect(Collectors.toList()));
		map.put("unsubscribedUsers", unsubscribedUsers.stream().distinct().collect(Collectors.toList()));
		return map;
	}

	@Override
	public Map<String, Object> getContactsCount(Integer userId, UserListDTO userListDTO) throws UserListException {
		Map<String, Object> resultMap = new HashMap<>();
		User user = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, userId)),
				new FindLevel[] { FindLevel.ROLES, FindLevel.COMPANY_PROFILE });
		List<Integer> userListIds = getUserListIds(user, userListDTO);
		resultMap = getContactsCount(userListIds, resultMap, user, userListDTO);
		Integer activecontacts = (Integer) resultMap.get("activecontacts");
		Integer nonactiveUsers = (Integer) resultMap.get("nonactiveUsers");
		resultMap.put("validContactsCount", activecontacts + nonactiveUsers);
		return resultMap;
	}

	@Override
	public XtremandResponse sendPartnerMail(Pagination pagination) throws UserDataAccessException {
		XtremandResponse xtremandResponse = new XtremandResponse();
		boolean access = getAccess(true, pagination.getUserId(), false);
		try {
			if (access || pagination.isFromPartnerAnalytics()) {
				User partner = userService.loadUser(
						Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, pagination.getPartnerId())),
						new FindLevel[] { FindLevel.SHALLOW });
				/**/
				User customer = userService.loadUser(
						Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, pagination.getUserId())),
						new FindLevel[] { FindLevel.COMPANY_PROFILE });

				if (pagination.isVanityUrlFilter()) {
					customer.setCompanyProfileName(utilDao.getPrmCompanyProfileName());
				}
				Object[] result = userDAO.getSignUpDetails(partner.getAlias(), pagination.getUserListId());
				if (result != null && result.length > 2) {
					partner.setFirstName((String) result[1]);
					partner.setLastName((String) result[2]);
				}
				partnershipDao.updateNotifyPartners(pagination.getPartnerId(), customer.getCompanyProfile().getId());
				List<User> partnersList = new ArrayList<>();
				partner.setNotifyPartners(true);
				partnersList.add(partner);
				boolean isUnsubscribed = userDAO.isUnsubscribedUserByCompanyIdAndUserId(
						customer.getCompanyProfile().getId(), partner.getUserId());
				List<Integer> deactivatedPartners = utilDao
						.findDeactivedPartnersByCompanyId(customer.getCompanyProfile().getId());
				boolean isDeactivated = XamplifyUtils.isNotEmptyList(deactivatedPartners)
						&& deactivatedPartners.contains(pagination.getPartnerId());
				if (isDeactivated) {
					xtremandResponse.setAccess(true);
					xtremandResponse.setStatusCode(400);
					xtremandResponse
							.setMessage(partner.getEmailId() + " is already Deactivated for receving the emails from "
									+ customer.getCompanyProfile().getCompanyName());
				} else if (isUnsubscribed) {
					xtremandResponse.setAccess(true);
					xtremandResponse.setStatusCode(400);
					xtremandResponse
							.setMessage(partner.getEmailId() + " is already Unsubscribed for receving the emails from "
									+ customer.getCompanyProfile().getCompanyName());
				} else {
					asyncComponent.sendPartnerMail(partnersList, EmailConstants.PARTNER_EMAIL, customer,
							pagination.getUserListId());
					xtremandResponse.setAccess(true);
					xtremandResponse.setStatusCode(200);
					xtremandResponse.setMessage("Email sent successfully.");
				}
			} else {
				xtremandResponse.setAccess(false);
			}
		} catch (Exception e) {
			throw new UserDataAccessException(e.getMessage());
		}
		return xtremandResponse;
	}

	/*
	 * Deprecated as companyId has added on UserList
	 * 
	 * @Override public UserList getUserListByCustomerAndName(Integer customerId,
	 * String name) { return userListDAO.getUserListByCustomerAndName(customerId,
	 * name); }
	 */

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void resubscribeUser(Integer userId, Integer customerId) {
		User customer = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, customerId)),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		userListDAO.resubscribeUser(userId, customer.getCompanyProfile().getId());
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public Map<String, Object> removeUsersFromUserList(Integer userListId, Integer customerId,
			List<Integer> removeUserIdsList) throws UserListException {
		Map<String, Object> resultMap = new HashMap<>();
		Criteria criteria = new Criteria("id", OPERATION_NAME.eq, userListId);
		List<Criteria> criterias = Arrays.asList(criteria);
		Collection<UserList> userLists = userListDAO.find(criterias,
				new FindLevel[] { FindLevel.CAMPAIGNS, FindLevel.USERS });
		UserList userList = userLists.iterator().next();
		removeUserIdsList.removeIf(userId -> userId == null);
		boolean access = false;
		if (userList.getModuleName().equalsIgnoreCase("SHARE LEADS")) {
			access = utilService.hasShareLeadsModuleAccess(customerId);
		} else {
			access = getAccess(userList.isPartnerUserList(), customerId, false);
		}
		if (!access) {
			resultMap.put("access", false);
			resultMap.put("message", "CONTACT LIST UPDATED SUCCESSFULLY");
			resultMap.put("statusCode", 201);
		} else {
			boolean defaultPartnerList = userList.isDefaultPartnerList();
			boolean usersSizeMatched = userList.getUsers().size() == removeUserIdsList.size();
			boolean isAssociatedCompanyDoesNotExists = userList.getAssociatedCompany() == null;
			boolean isNotForm = userList.getForm() == null;
			if (!defaultPartnerList && usersSizeMatched && isNotForm && isAssociatedCompanyDoesNotExists) {
				return deleteUserList(userListId, customerId, removeUserIdsList, resultMap, userList);
			}
			if (defaultPartnerList && usersSizeMatched) {
				return deletePartnerFromDefaultMasterPartnerList(userListId, customerId, removeUserIdsList, resultMap,
						userList);
			} else {
				resultMap = deleteUsersFromUserList(userListId, customerId, removeUserIdsList, resultMap, userList,
						access, usersSizeMatched);
			}

		}
		Pagination pagination = new Pagination();
		pagination.setPageIndex(1);
		pagination.setMaxResults(12);
		return resultMap;

	}

	private Map<String, Object> deleteUsersFromUserList(Integer userListId, Integer customerId,
			List<Integer> partnerUserIds, Map<String, Object> resultMap, UserList userList, boolean access,
			boolean usersSizeMatched) {
		boolean isEmptyFormList = false;
		if (usersSizeMatched && userList.getForm() != null) {
			isEmptyFormList = true;
		}
		resultMap = removeUsersFromUserList(resultMap, access, userList, customerId, partnerUserIds);
		if (!userList.isDefaultPartnerList()) {
			deletePartnerMappingsTracksAndPlayBooksAndCampaigns(userListId, customerId, partnerUserIds, userList);
		}

		if (isEmptyFormList) {
			resultMap.put("access", true);
			resultMap.put("isEmptyFormList", true);
			resultMap.put("message", "CONTACT LIST UPDATED SUCCESSFULLY");
			resultMap.put("statusCode", 200);
		} else {
			resultMap.put("message", "CONTACT LIST UPDATED SUCCESSFULLY");
			resultMap.put("statusCode", 201);
		}
		return resultMap;
	}

	private void deletePartnerMappingsTracksAndPlayBooksAndCampaigns(Integer userListId, Integer customerId,
			List<Integer> partnerUserIds, UserList userList) {
		/******** Deleting rows from xt_dam_partner_group_mapping *********/
		if (Boolean.TRUE.equals(userList.isPartnerUserList())) {
			damDao.deleteFromDamPartnerGroupMappingAndDamPartnerByUserListIdAndUserIds(partnerUserIds, userListId,
					customerId);
			/**** Deleting Tracks/PlayBooks Added On 10/12/2024. *****/
			unPublishLearningTracksAndPlaybooks(userListId, partnerUserIds, userList);
			damDao.unPublishDamAssets();
		}

	}

	/**** Added By Sravan on 12/12/2024 ***/
	private void unPublishLearningTracksAndPlaybooks(Integer userListId, List<Integer> partnerUserIds,
			UserList userList) {
		Integer companyId = userList.getCompany().getId();

		List<Integer> partnershipIds = new ArrayList<>();
		for (Integer partnerUserId : partnerUserIds) {
			PartnershipDTO partnershipDTO = partnershipDao.getPartnerShipByParnterIdAndVendorCompanyId(partnerUserId,
					companyId);
			Integer partnershipId = partnershipDTO != null ? partnershipDTO.getId() : null;
			partnershipIds.add(partnershipId);
		}
		XamplifyUtils.removeNullsFromList(partnershipIds);

		List<Integer> visibilityIds = lmsDao.findVisibilityIdsByPartnershipIds(partnershipIds, userListId);
		String visibilityIdsDebugMessage = "Total visbility ids found : " + visibilityIds.size() + " for userlist id "
				+ userListId;
		logger.debug(visibilityIdsDebugMessage);
		lmsDao.deleteLearningTrackVisibilityGroups(visibilityIds, userListId);
		lmsDao.unPublishLearningTracksWithEmptyVisbility();

	}

	private Map<String, Object> deleteUserList(Integer userListId, Integer customerId, List<Integer> partnerUserIds,
			Map<String, Object> resultMap, UserList userList) {
		removeUserList(userListId, customerId, false);
		resultMap.put("access", true);
		resultMap.put("message", "CONTACT LIST DELETED SUCCESSFULLY");
		resultMap.put("statusCode", 200);
		deletePartnerMappingsTracksAndPlayBooksAndCampaigns(userListId, customerId, partnerUserIds, userList);
		return resultMap;
	}

	private Map<String, Object> deletePartnerFromDefaultMasterPartnerList(Integer userListId, Integer customerId,
			List<Integer> partnerUserIds, Map<String, Object> resultMap, UserList userList) {
		partnershipService.deletePartnersFromDefaultUserList(userList, customerId, partnerUserIds);
		resultMap.put("access", true);
		resultMap.put("message", "CONTACT LIST DELETED SUCCESSFULLY");
		resultMap.put("statusCode", 200);
		deletePartnerMappingsTracksAndPlayBooksAndCampaigns(userListId, customerId, partnerUserIds, userList);
		return resultMap;
	}

	public Map<String, Object> removeUsersFromUserList(Map<String, Object> resultMap, boolean access, UserList userList,
			Integer customerId, List<Integer> removeUserIdsList) {
		if (Boolean.TRUE.equals(userList.isPartnerUserList()) && access) {
			XtremandResponse xtremandResponse = partnershipService.deletePartnersFromUserList(userList, customerId,
					removeUserIdsList);
			removeZeroUsersLists(xtremandResponse);
			resultMap.put("access", true);
		} else if (Boolean.TRUE.equals(!userList.isPartnerUserList() && access)
				&& userList.getModuleName().equalsIgnoreCase("SHARE LEADS")) {
			deleteLeadsFromLeadsList(userList, customerId, removeUserIdsList);
			resultMap.put("access", true);
		} else if (Boolean.TRUE.equals(!userList.isPartnerUserList()) && access) {
			deleteUsersFromUserList(userList, customerId, removeUserIdsList);
			resultMap.put("access", true);
		} else {
			resultMap.put("access", false);
		}
		return resultMap;
	}

	@Override
	public void deleteUsersFromUserList(UserList userList, Integer customerId, List<Integer> removeUserIdsList) {
		if (userList.getAssociatedCompany() != null) {
			Integer companyId = userDAO.getCompanyIdByUserId(customerId);
			userListDAO.updateCompanyOnAllUserUserlists(null, null, removeUserIdsList, companyId);
		}
		userList.initialiseCommonFields(false, customerId);
		List<Integer> userListIds = new ArrayList<>();
		userListIds.add(userList.getId());
		if (XamplifyUtils.isNotEmptyList(removeUserIdsList) && XamplifyUtils.isNotEmptyList(userListIds)) {
			userListDAO.deletePartnersFromPartnerLists(removeUserIdsList, userListIds);
		}
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	private void deletePartnerCompany(List<Integer> partnerIds, Integer companyId) {
		userDAO.deletePartnerCompany(partnerIds, companyId);
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public HttpServletResponse downloadUserList(UserListPaginationWrapper userListPaginationWrapper,
			HttpServletResponse response) {
		List<FlexiFieldResponseDTO> flexiFieldNames = null;
		UserListDTO userListDTO = userListPaginationWrapper.getUserList();
		Pagination pagination = userListPaginationWrapper.getPagination();
		userListPaginationWrapper.getPagination().setMaxResults(pagination.getTotalRecords());
		userListPaginationWrapper.getUserList().setIsDownload(true);
		Integer userListId = userListDTO.getId();
		try {
			Map<String, Object> map = listAllUserListContacts(userListPaginationWrapper, pagination.getUserId());
			boolean isPartnerUserList = userListDTO.isPartnerUserList();
			List<String[]> data = new ArrayList<>();

			if (isPartnerUserList) {
				data.add(new String[] { "FIRSTNAME", "LASTNAME", "ACCOUNT NAME", "ACCOUNT OWNER", "ACCOUNT SUB TYPE",
						"COMPANY", "COMPANY DOMAIN", "JOBTITLE", "EMAILID", "WEBSITE", "VERTICAL", "REGION",
						"TERRITORY", "TYPE", "CATEGORY", "ADDRESS", "CITY", "STATE", "ZIP", "COUNTRY", "MOBILE NUMBER",
						"SIGNUP STATUS", "COMPANY PROFILE", "ONBOARDED ON" });
			} else {
				List<String> csvHeaderColumns = new ArrayList<>();
				csvHeaderColumns.addAll(XamplifyUtils.defaultContactCsvHeaderColumns());
				if (XamplifyUtils.CONTACTS.equalsIgnoreCase(userListDTO.getModuleName())) {
					csvHeaderColumns.add("CONTACT STATUS");
					flexiFieldNames = flexiFieldDao.findAll(pagination.getUserId());
					if (XamplifyUtils.isNotEmptyList(flexiFieldNames)) {
						csvHeaderColumns.addAll(XamplifyUtils.getFlexiFieldNames(flexiFieldNames));
					}
				}
				data.add(csvHeaderColumns.toArray(new String[0]));
			}

			List<UserDTO> totalUsersList = (List<UserDTO>) map.get("listOfUsers");
			if (pagination.isPartnerTeamMemberGroupFilter()) {
				List<Integer> filteredPartnersIdsList = userListDAO
						.getTeamMemberGroupedPartnerIds(pagination.getUserId());
				if (filteredPartnersIdsList != null && !filteredPartnersIdsList.isEmpty()) {
					List<UserDTO> filteredUsersList = totalUsersList.stream()
							.filter(exuser -> filteredPartnersIdsList.contains(exuser.getId()))
							.collect(Collectors.toList());
					totalUsersList = new ArrayList<>(filteredUsersList);
				} else {
					totalUsersList = new ArrayList<>();
				}
			}

			for (UserDTO user : totalUsersList) {
				if (user.isEmailValidationInd()) {
					if (isPartnerUserList) {
						String signUpStatus = user.getUserStatus().equalsIgnoreCase("APPROVE") ? "Yes" : "No";
						String companyNameStatus = (user.getCompanyNameStatus() != null
								&& user.getCompanyNameStatus().equalsIgnoreCase(CompanyNameStatus.ACTIVE.toString()))
										? "Yes"
										: "No";
						String onboardedOn = XamplifyUtils.isValidString(user.getCreatedTime()) ? DateUtils
								.convertDateToString(DateUtils.convertStringToDate24Format(user.getCreatedTime()))
								: "-";
						data.add(new String[] { user.getFirstName(), user.getLastName(), user.getAccountName(),
								user.getAccountOwner(), user.getAccountSubType(), user.getContactCompany(),
								user.getCompanyDomain(), user.getJobTitle(), user.getEmailId(), user.getWebsite(),
								user.getVertical(), user.getRegion(), user.getTerritory(), user.getPartnerType(),
								user.getCategory(), user.getAddress(), user.getCity(), user.getState(),
								user.getZipCode(), user.getCountry(), user.getMobileNumber(), signUpStatus,
								companyNameStatus, onboardedOn });
					} else {
						List<String> csvHeaderColumnValues = new ArrayList<>();
						csvHeaderColumnValues.addAll(defaultContactsCsvHeaderColumnValues(user));
						if (XamplifyUtils.CONTACTS.equalsIgnoreCase(userListDTO.getModuleName())) {
							csvHeaderColumnValues.add(user.getContactStatus());
							List<FlexiFieldRequestDTO> flexiFieldValues = flexiFieldDao
									.findFlexiFieldsBySelectedUserIdAndUserListId(userListId, user.getId());
							if (XamplifyUtils.isNotEmptyList(flexiFieldValues)) {
								csvHeaderColumnValues.addAll(getFlexiFieldValues(flexiFieldNames, flexiFieldValues));
							}
						}
						data.add(csvHeaderColumnValues.toArray(new String[0]));
					}
				}
			}

			String fileName = "UserList_" + userListId;
			return XamplifyUtils.generateCSV(fileName, response, data);
		} catch (IOException ioe) {
			String message = "error occured in downloadUser List with userListId : " + userListId;
			logger.error(message);
			throw new UserListException(ioe.getMessage());
		}
	}

	private List<String> getFlexiFieldValues(List<FlexiFieldResponseDTO> flexiFieldNames,
			List<FlexiFieldRequestDTO> flexiFieldValues) {
		List<String> data = new ArrayList<>();
		Map<String, String> flexiFieldValueMap = flexiFieldValues.stream()
				.collect(Collectors.toMap(FlexiFieldRequestDTO::getFieldName, FlexiFieldRequestDTO::getFieldValue));

		flexiFieldNames.forEach(flexiFieldName -> {
			String fieldValue = flexiFieldValueMap.getOrDefault(flexiFieldName.getFieldName(), "");
			data.add(fieldValue);
		});

		return data;
	}

	private List<String> defaultContactsCsvHeaderColumnValues(UserDTO user) {
		return Arrays.asList(user.getFirstName(), user.getLastName(), user.getContactCompany(), user.getJobTitle(),
				user.getEmailId(), user.getAddress(), user.getCity(), user.getState(), user.getZipCode(),
				user.getCountry(), user.getMobileNumber());
	}

	@Override
	public XtremandResponse removeInvalidUsers(Integer userId, List<Integer> removeUserIds, boolean assignLeads) {
		XtremandResponse xtremandResponse = new XtremandResponse();
		boolean access = getAccess(false, userId, assignLeads);

		if (access) {
			Set<Integer> userListIds = removeInvalidUsers(removeUserIds, userId, assignLeads);
			xtremandResponse.setData(userListIds);
			xtremandResponse.setAccess(true);
		} else {
			xtremandResponse.setAccess(false);
		}
		return xtremandResponse;
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse removeZeroUsersLists(XtremandResponse xtremandResponse) {
		if (xtremandResponse.getData() != null) {
			Set<Integer> userListIds = (Set<Integer>) xtremandResponse.getData();
			for (Integer listId : userListIds) {
				UserList userList = userListDAO.findByPrimaryKey(listId, new FindLevel[] { FindLevel.USERS });
				if (userList != null) {
					boolean isActiveMasterPartnerList = userList.getName().equals(activePartnerListName);
					boolean isInActiveMasterPartnerList = userList.getName().equals(inactivePartnerListName);
					boolean isTeamMemberPartnerList = userList.isTeamMemberPartnerList();
					boolean defaultPartnerList = userList.isDefaultPartnerList();
					if (userList.getUsers().isEmpty() && !defaultPartnerList && !isActiveMasterPartnerList
							&& !isInActiveMasterPartnerList && !isTeamMemberPartnerList) {
						userListDAO.deleteByPrimaryKey(userList.getId());
					}
				}
			}
		}
		xtremandResponse.setMessage("success");
		xtremandResponse.setStatusCode(200);
		xtremandResponse.setData(null);
		return xtremandResponse;
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public Set<Integer> removeInvalidUsers(List<Integer> removeUserIds, Integer userId, boolean assignLeads) {
		Integer companyId = userDAO.getCompanyIdByUserId(userId);
		Set<Integer> userListIds = new HashSet<>();
		for (Integer removeUserId : removeUserIds) {
			List<Integer> contactListIds = new ArrayList<>();
			if (assignLeads) {
				contactListIds = userListDAO.getUserListIdsBYLeadId(false, removeUserId, companyId);
			} else {
				contactListIds = userListDAO.getUserListIdsBYUserId(false, removeUserId, companyId);
			}
			for (Integer listId : contactListIds) {
				userListIds.add(listId);
				removeInvalidUsersFromUserLists(removeUserId, listId, userId);
			}
		}
		return userListIds;
	}

	public void removeInvalidUsersFromUserLists(Integer removeUserId, Integer listId, Integer customerId) {
		UserList userList = userListDAO.findByPrimaryKey(listId, new FindLevel[] { FindLevel.USERS });
		List<Integer> removeUserIdsList = new ArrayList<>();
		removeUserIdsList.add(removeUserId);
		if (userList.getUsers().size() == 1 && !userList.isDefaultPartnerList()) {
			// userListDAO.deleteLegalBasis(removeUserIdsList, listId);
			userListDAO.deleteByPrimaryKey(userList.getId());

		} else {
			// userListDAO.deleteLegalBasis(removeUserIdsList, listId);
			userListDAO.removeInvalidUser(removeUserId, listId);
		}
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void unlinkAccount(Integer userId, SocialNetwork socialNetwork, boolean deleteContactList)
			throws UserListException {
		List<Criterion> criterions = new ArrayList<>();
		Criterion creterion1 = Restrictions.and(Restrictions.eq("owner.userId", userId),
				Restrictions.eq("socialNetwork", socialNetwork), Restrictions.eq("moduleName", "CONTACTS"));
		Criterion creterion2 = Restrictions.and(Restrictions.eq("assignedBy.userId", userId),
				Restrictions.eq("socialNetwork", socialNetwork), Restrictions.eq("moduleName", "SHARE LEADS"));
		Criterion condition = Restrictions.or(creterion1, creterion2);
		criterions.add(condition);

		List<UserList> userLists = (List<UserList>) userListDAO.findLists(criterions,
				new FindLevel[] { FindLevel.CAMPAIGNS });
		if (deleteContactList) {
			for (UserList userList : userLists) {
				if (!userList.isDefaultPartnerList()) {
					userListDAO.deleteByPrimaryKey(userList.getId());
				}
			}
		} else if (!deleteContactList) {
			for (UserList userList : userLists) {
				userList.setSynchronisedList(false);
				genericDAO.saveOrUpdate(userList);
			}
		}
		User user = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, userId)),
				new FindLevel[] { FindLevel.SHALLOW });
		if (socialNetwork == SocialNetwork.GOOGLE) {
			user.setGoogleRefreshToken(null);
			user.setGoogleAccessToken(null);
			user.setGoogleProfileEmail(null);
		} else if (socialNetwork == SocialNetwork.SALESFORCE) {
			user.setSalesforceRefreshToken(null);
			user.setSalesforceAccessToken(null);
			user.setSalesforceProfileEmail(null);
		} else if (socialNetwork == SocialNetwork.ZOHO) {
			user.setZohoRefreshToken(null);
			user.setZohoAccessToken(null);
			user.setZohoProfileEmail(null);
		}
	}

	@Override
	public Map<String, Boolean> checkAuthentications(Integer userId) {
		return null;
	}

	@Override
	public List<String> listUserlistNames(Integer userId) throws UserListException {
		List<Integer> userIdList = userService.getCompanyUserIds(userId);
		Integer[] userIdArray = ArrayUtils.toObject(userIdList.stream().mapToInt(i -> i).toArray());
		return userListDAO.listUserlistNames(userIdArray);
	}

	public List<UserList> getAllContactLists(Integer companyId) {
		Criteria criteria1 = new Criteria("company.id", OPERATION_NAME.eq, companyId);
		Criteria criteria2 = new Criteria("isPartnerUserList", OPERATION_NAME.eq, false);
		List<Criteria> criteriasObj = new ArrayList<Criteria>();
		criteriasObj.add(criteria1);
		criteriasObj.add(criteria2);
		return (List<UserList>) userListDAO.find(criteriasObj, new FindLevel[] { FindLevel.SHALLOW });
	}

	/*
	 * This method is deprecated, but DO NOT DELETE, need to be modified. It works
	 * for contact list - when isPartnerUserList is false. future.
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	private XtremandResponse saveContactList_Old(Set<UserDTO> users, Integer userId, UserListDTO userlistDTO,
			XtremandResponse xtremandResponse) {
		List<String> nonExistingEmailIds = userService
				.getNonExistingUsers(users.stream().map(UserDTO::getEmailId).collect(Collectors.toList()));
		User user = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, userId)),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		boolean isPartnerUserList = userlistDTO.isPartnerUserList();

		UserList userList = new UserList();
		userList.setName(userlistDTO.getName());
		userList.setOwner(user);
		userList.setUploadedDate(new Date());
		userList.setSocialNetwork(UserList.getSocialNetworkEnum(userlistDTO.getSocialNetwork()));
		userList.setContactType(UserList.getContactTypeEnum(userlistDTO.getContactType()));
		userList.setSynchronisedList(userlistDTO.isSynchronisedList());
		userList.setPartnerUserList(userlistDTO.isPartnerUserList());
		userList.setCompany(user.getCompanyProfile());
		userList.setPublicList(userlistDTO.getPublicList());
		userList.setModuleName(userlistDTO.getModuleName());
		userList.setAlias(userlistDTO.getAlias());
		userList.initialiseCommonFields(true, userId);

		userService.removeNullEmailUserDTOs(users);
		xtremandResponse = validateContactList(users, userlistDTO.getName(), user, xtremandResponse);
		if (xtremandResponse.getStatusCode() == 200) {
			if (!nonExistingEmailIds.isEmpty()) {
			}
			if (!(nonExistingEmailIds.isEmpty()) && !isPartnerUserList) {
			} else if (nonExistingEmailIds.isEmpty() && !isPartnerUserList) {
				if (userList.getUsers().stream().anyMatch(u -> u.isEmailValidationInd() == false)) {
					userList.setEmailValidationInd(false);
				} else if (userList.getUsers().stream().allMatch(u -> u.isEmailValidationInd() == true)) {
					userList.setEmailValidationInd(true);
				}
			}
			if (!isPartnerUserList) {
				List<Integer> totalUnsubscribedUserIds = userListDAO
						.getUnsubscribedUsers(userList.getCompany().getId());
				sendContactListMail(user, userList, totalUnsubscribedUserIds);
			}
		}
		logger.info("UserList created successfully..!! ");
		return xtremandResponse;
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse saveUserList_Old(Integer userId, UserUserListWrapper userUserListWrapper)
			throws UserListException {
		XtremandResponse xtremandResponse = new XtremandResponse();
		UserListDTO userListDTO = userUserListWrapper.getUserList();
		Set<UserDTO> users = userUserListWrapper.getUsers();
		boolean access = getAccess(userListDTO.isPartnerUserList(), userId, false);

		if (users != null && !users.isEmpty() && userId != null && userId > 0) {
			if (userListDTO.isPartnerUserList()) {
				xtremandResponse = savePartnerList(users, userId, userListDTO, access, xtremandResponse, null);
			} else {
				xtremandResponse = saveContactList_Old(users, userId, userListDTO, access, xtremandResponse);
			}
		}
		logger.info("UserList created successfully..!! ");
		return xtremandResponse;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse saveUserList(Integer userId, UserUserListWrapper userUserListWrapper)
			throws UserListException {
		XtremandResponse xtremandResponse = new XtremandResponse();
		xtremandResponse.setStatusCode(400);
		xtremandResponse.setMessage(FAILED);
		if (userUserListWrapper != null && userId != null) {
			UserListDTO userListDTO = userUserListWrapper.getUserList();
			Set<UserDTO> users = userUserListWrapper.getUsers();
			if (userListDTO != null && users != null && !users.isEmpty()) {
				if (userListDTO.getModuleName().equalsIgnoreCase("SHARE LEADS")) {
					userListDTO.setAssignedLeadsList(utilService.hasShareLeadsModuleAccess(userId));
				}
				boolean access = getAccess(userListDTO.isPartnerUserList(), userId, userListDTO.isAssignedLeadsList());
				if (access) {
					/********* Writing Data To Csv *********/
					Integer companyId = userDAO.getCompanyIdByUserId(userId);
					String filePath = fileUtil.uploadContactsCsvFile(userUserListWrapper.getUserList().getName(), users,
							companyId);
					userListDTO.setCsvPath(filePath);
					/********* Writing Data To Csv *********/
					if (userListDTO.isPartnerUserList()) {
						userService.removeNullEmailUserDTOs(users);
						xtremandResponse = partnershipService.saveAsPartnerList(users, userId, userListDTO,
								xtremandResponse, filePath);
					} else {
						xtremandResponse = saveContactList(users, userId, userListDTO, xtremandResponse);
					}
					xtremandResponse.setAccess(true);
				} else {
					xtremandResponse.setAccess(false);
				}
			} else {
				xtremandResponse.setStatusCode(500);
				xtremandResponse.setMessage(INVALID_INPUT);
			}
		} else {
			xtremandResponse.setStatusCode(500);
			xtremandResponse.setMessage(INVALID_INPUT);
		}
		logger.info("UserList created successfully..!! ");
		return xtremandResponse;
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	private XtremandResponse saveContactList(Set<UserDTO> userDTOs, Integer userId, UserListDTO userlistDTO,
			XtremandResponse xtremandResponse) {
		User loggedInUser = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, userId)),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		if (loggedInUser != null) {
			CompanyProfile company = loggedInUser.getCompanyProfile();
			if (company != null) {
				boolean validateListName = false;
				boolean isCreate = false;
				if (!userlistDTO.isCopyList() && userlistDTO.getId() != null && userlistDTO.getId() > 0) {
					validateListName = true;
				} else {
					validateListName = userlistDTO.getModuleName().equalsIgnoreCase("CONTACTS")
							? validateListName(userlistDTO.getName(), company.getId(), userlistDTO.getModuleName())
							: validateAssignedListName(userlistDTO.getName(), company.getId(),
									userlistDTO.getModuleName());
				}
				if (validateListName) {
					boolean validateEmails = userlistDTO.isCopyList() ? true
							: validateEmails(userDTOs, xtremandResponse);

					if (validateEmails) {
						userlistDTO.setUploadInProgress(true);
						UserList userList = null;
						if (!userlistDTO.isCopyList() && userlistDTO.getId() != null && userlistDTO.getId() > 0) {
							userList = userListDAO.findByPrimaryKey(userlistDTO.getId(),
									new FindLevel[] { FindLevel.COMPANY_PROFILE });
							userList.setEmailValidationInd(false);
							userList.setUploadInProgress(true);
							userList.addCreatedAndUpdatedTime(isCreate, userId);
						} else {
							userList = createUserList(userlistDTO, loggedInUser, company);
							if (userlistDTO.isCopyList()) {
								userList.setSynchronisedList(false);
							}
							isCreate = true;
						}
						if (userList != null) {
							if (userlistDTO.getModuleName() != null) {
								if (userlistDTO.getModuleName().equalsIgnoreCase("CONTACTS")) {
									List<Integer> totalUnsubscribedUserIds = userListDAO
											.getUnsubscribedUsers(userList.getCompany().getId());
									sendContactListMail(loggedInUser, userList, totalUnsubscribedUserIds);
								} else if (userlistDTO.getModuleName().equalsIgnoreCase("SHARE LEADS")) {
									List<Integer> totalUnsubscribedUserIds = userListDAO
											.getUnsubscribedUsers(company.getId());
									utilService.sendLeadsListMail(loggedInUser, userList, isCreate,
											totalUnsubscribedUserIds);
								}

								asyncComponent.createUsersInUserList(userDTOs, userList.getId(),
										loggedInUser.getUserId(), isCreate, userlistDTO);
								xtremandResponse.setStatusCode(200);
							}
						}
					}
				} else {
					xtremandResponse.setStatusCode(401);
					xtremandResponse.setMessage("list name already exists");
				}
			}
		}
		logger.info("UserList created successfully..!! ");
		return xtremandResponse;
	}

	public void createUsersInUserList(Set<UserDTO> users, UserList userList, User loggedInUser) {
		if (users != null) {
			List<User> nonProcessedUsers = new ArrayList<User>();
			boolean isGdprOn = false;
			if (userList.getModuleName().equalsIgnoreCase("SHARE LEADS")) {
				isGdprOn = gdprSettingService.isGdprEnabled(loggedInUser.getCompanyProfile().getId());
			} else {
				isGdprOn = gdprSettingService.isGdprEnabled(userList.getCompany().getId());
			}
			for (UserDTO userDTO : users) {
				if (userDTO != null && StringUtils.isNotBlank(userDTO.getEmailId().trim())) {
					User user = userService.saveOrUpdateUser(userDTO, loggedInUser.getUserId());

					if (user != null) {
						if (userDTO.getContactCompanyId() != null && userDTO.getContactCompanyId() != 0) {
						}
						createUserUserList(user, userList, userDTO, loggedInUser, isGdprOn);
						if (user.isEmailValidationInd()) {
							nonProcessedUsers.add(user);
						}
					}
				}
			}
			List<Integer> totalUnsubscribedUserIds = userListDAO.getUnsubscribedUsers(userList.getCompany().getId());
			sendContactListMail(loggedInUser, userList, totalUnsubscribedUserIds);
		}
	}

	public UserUserList createUserUserList(User user, UserList userList, UserDTO userDTO, User loggedInUser,
			boolean isGdprOn) {
		UserUserList userUserList = getUserUserList(user, userList, userDTO, loggedInUser, isGdprOn);
		genericDAO.save(userUserList);
		String debugMessage = user.getEmailId() + " uploaded into " + userList.getName() + "(" + userList.getId() + ")";
		logger.debug(debugMessage);
		userList.getUserUserLists().add(userUserList);
		return userUserList;
	}

	public UserUserList getUserUserList(User user, UserList userList, UserDTO userDTO, User loggedInUser,
			boolean isGdprOn) {

		UserUserList userUserList = new UserUserList();
		userUserList.setUser(user);
		userUserList.setUserList(userList);
		userUserList.setFirstName(userDTO.getFirstName());
		userUserList.setLastName(userDTO.getLastName());
		userUserList.setContactCompany(userDTO.getContactCompany());
		userUserList.setJobTitle(userDTO.getJobTitle());
		userUserList.setMobileNumber(userDTO.getMobileNumber());
		userUserList.setDescription(userDTO.getDescription());
		userUserList.setAddress(userDTO.getAddress());
		userUserList.setCity(userDTO.getCity());
		userUserList.setCountry(userDTO.getCountry());
		userUserList.setState(userDTO.getState());
		userUserList.setZipCode(userDTO.getZipCode());
		userUserList.setVertical(userDTO.getVertical());
		userUserList.setRegion(userDTO.getRegion());
		userUserList.setPartnerType(userDTO.getPartnerType());
		userUserList.setCategory(userDTO.getCategory());
		userUserList.setCountryCode(userDTO.getCountryCode());
		userUserList.setContactStatusId(userDTO.getContactStatusId());
		setLegalBasis(userUserList, userDTO.getLegalBasis(), isGdprOn);

		// XNFR-680
		Set<UserListFlexiField> userListFlexiFields = new HashSet<>();
		userService.setUserListFlexiFields(userUserList, userDTO.getFlexiFields(), userListFlexiFields);
		userUserList.setUserListFlexiFields(userListFlexiFields);

		return userUserList;
	}

	private void setLegalBasis(UserUserList userUserList, List<Integer> legalBasisIds, boolean isGdprOn) {
		if (userUserList != null) {
			List<LegalBasis> legalBasisList = new ArrayList<>();
			if (isGdprOn && legalBasisIds != null && !legalBasisIds.isEmpty()) {
				for (Integer legalBasisId : legalBasisIds) {
					LegalBasis legalBasis = genericDAO.get(LegalBasis.class, legalBasisId);
					if (legalBasis != null) {
						legalBasisList.add(legalBasis);
					}
				}
			} else {
				List<LegalBasis> legalBasisDefaultList = gdprSettingService.getSelectByDefaultLegalBasis();
				for (LegalBasis legalBasis : legalBasisDefaultList) {
					if (legalBasis != null) {
						legalBasisList.add(legalBasis);
					}
				}
			}
			userUserList.setLegalBasis(legalBasisList);
		}
	}

	private UserList createUserList(UserListDTO userlistDTO, User loggedInUser, CompanyProfile companyProfile) {
		UserList userList = new UserList();
		userList.setName(userlistDTO.getName().trim());
		userList.setUploadedDate(new Date());
		userList.setSocialNetwork(UserList.getSocialNetworkEnum(userlistDTO.getSocialNetwork().toUpperCase()));
		userList.setContactType(UserList.getContactTypeEnum(userlistDTO.getContactType()));
		userList.setSynchronisedList(userlistDTO.isSynchronisedList());
		userList.setPartnerUserList(userlistDTO.isPartnerUserList());
		userList.setPublicList(userlistDTO.getPublicList());
		userList.setModuleName(userlistDTO.getModuleName());
		userList.setAlias(userlistDTO.getAlias());
		userList.setUploadInProgress(userlistDTO.isUploadInProgress());
		if (userlistDTO.getModuleName() != null) {
			if (userlistDTO.getModuleName().equalsIgnoreCase("CONTACTS")) {
				userList.setOwner(loggedInUser);
				userList.setCompany(companyProfile);
			} else if (userlistDTO.getModuleName().equalsIgnoreCase("SHARE LEADS")) {
				userList.setAssignedBy(loggedInUser);
				userList.setAssignedCompany(companyProfile);
			}
		}
		Long externalListId = userlistDTO.getExternalListId();
		if (externalListId != null && externalListId > 0) {
			userList.setExternalListId(externalListId);
		}
		userList.initialiseCommonFields(true, loggedInUser.getUserId());
		if (org.springframework.util.StringUtils.hasText(userlistDTO.getCsvPath())) {
			userList.setCsvPath(userlistDTO.getCsvPath());
		}
		genericDAO.save(userList);
		return userList;
	}

	private boolean validateEmails(Set<UserDTO> users, XtremandResponse xtremandResponse) {
		boolean validEmails = false;
		Map<String, Object> resultMap = userListValidator.validateEmailIds(users);
		if (resultMap != null) {
			int statusCode = (int) resultMap.get("statusCode");
			if (statusCode == 200) {
				validEmails = true;
			} else if (statusCode == 409) {
				xtremandResponse.setStatusCode(402);
				xtremandResponse.setMessage("Email addresses in your list that aren't formatted properly");
				xtremandResponse.setData(resultMap.get("emailAddresses"));
				xtremandResponse.setMap(resultMap);
			}
		}
		return validEmails;
	}

	/*
	 * private boolean validateListName(String listName, Integer companyId, String
	 * module) { boolean isDuplicate = false; if (StringUtils.isNotBlank(listName))
	 * { isDuplicate = userListDAO.isDuplicateListName(listName, companyId, module);
	 * } return !isDuplicate; }
	 */

	private boolean validateListName(String listName, Integer companyId, String module) {
		return userListDAO.validateListName(listName, companyId, module);
	}

	private boolean validateAssignedListName(String listName, Integer companyId, String module) {
		return userListDAO.validateAssignedListName(listName, companyId, module);
	}

	public XtremandResponse savePartnerList(Set<UserDTO> users, Integer userId, UserListDTO userListDTO, boolean access,
			XtremandResponse xtremandResponse, String csvPath) throws UserListException {
		if (access) {
			userService.removeNullEmailUserDTOs(users);
			xtremandResponse = partnershipService.saveAsPartnerList(users, userId, userListDTO, xtremandResponse,
					csvPath);
			xtremandResponse.setAccess(true);
		} else {
			xtremandResponse.setAccess(false);
		}
		return xtremandResponse;
	}

	public XtremandResponse saveContactList_Old(Set<UserDTO> users, Integer userId, UserListDTO userListDTO,
			boolean access, XtremandResponse xtremandResponse) throws UserListException {
		if (access) {
			xtremandResponse = saveContactList_Old(users, userId, userListDTO, xtremandResponse);
			xtremandResponse.setAccess(true);
		} else {
			xtremandResponse.setAccess(false);
		}
		return xtremandResponse;
	}

	@Override
	public void processContacts(List<User> nonProcessedUsers, boolean isPartnerUserList, UserList userList, User user) {
		if (!(nonProcessedUsers.isEmpty()) && !isPartnerUserList) {
		} else if (nonProcessedUsers.isEmpty() && !isPartnerUserList) {
			if (userList.getUsers().stream().anyMatch(u -> u.isEmailValidationInd() == false)) {
				userList.setEmailValidationInd(false);
				genericDAO.saveOrUpdate(userList);
			} else if (userList.getUsers().stream().allMatch(u -> u.isEmailValidationInd() == true)) {
				userList.setEmailValidationInd(true);
				genericDAO.saveOrUpdate(userList);
			}
		} else if (isPartnerUserList) {
			userList.setEmailValidationInd(true);
			mailService.sendPartnerlistMail(user, EmailConstants.PARTNER_LIST_CREATED, userList);
		}
	}

	public void sendContactListMail(User user, UserList userList, List<Integer> totalUnsubscribedUserIds) {
		if (userList.isEmailValidationInd() && userList.getCompany() != null) {
			mailService.sendContactListMail(user, EmailConstants.CONTACT_LIST_CREATED, userList,
					totalUnsubscribedUserIds);
		} else if (!userList.isEmailValidationInd() && userList.getCompany() != null) {
			mailService.sendContactListMail(user, EmailConstants.CONTACT_LIST_IN_PROCESS, userList,
					totalUnsubscribedUserIds);
		}
	}

	public void sendLeadsListMail(User user, UserList userList, boolean isCreate,
			List<Integer> totalUnsubscribedUserIds) {
		if (isCreate && userList.isEmailValidationInd() && userList.getCompany() != null) {
			mailService.sendLeadsListMail(user, EmailConstants.LEADS_LIST_PROCESSED_SHARED, userList,
					totalUnsubscribedUserIds);
		} else if (isCreate && userList.isEmailValidationInd() && userList.getCompany() == null) {
			mailService.sendLeadsListMail(user, EmailConstants.LEADS_LIST_PROCESSED_NOT_SHARED, userList,
					totalUnsubscribedUserIds);
		} else if (!isCreate && userList.isEmailValidationInd() && userList.getCompany() == null) {
			mailService.sendLeadsListMail(user, EmailConstants.LEADS_LIST_UPDATED, userList, totalUnsubscribedUserIds);
		} else if (!userList.isEmailValidationInd()) {
			mailService.sendLeadsListMail(user, EmailConstants.LEADS_LIST_IN_PROCESS, userList,
					totalUnsubscribedUserIds);
		}
	}

	@Override
	public Object listPartners(Integer userId, Pagination pagination) {
		User loggedInUser = userDAO.findByPrimaryKey(userId,
				new FindLevel[] { FindLevel.COMPANY_PROFILE, FindLevel.ROLES });
		CompanyProfile companyProfile = loggedInUser.getCompanyProfile();
		if (companyProfile != null) {
			UserList defaultPartnerList = userListDAO.getDefaultPartnerList(companyProfile.getId());
			if (defaultPartnerList != null && defaultPartnerList.isPartnerUserList()) {
				// List All the contacts belongs to the given userList
				UserListDTO userListDTO = new UserListDTO();
				userListDTO.setLoginAsUserId(pagination.getLoginAsUserId());
				userListDTO.setPartnerUserList(true);
				userListDTO.setDefaultPartnerList(true);
				userListDTO.setContactType("All");
				List<Integer> userListIds = new ArrayList<Integer>();
				userListIds.add(defaultPartnerList.getId());
				return listUserListscontacts(pagination, userListIds, loggedInUser, userListDTO);
				// return listuserListContacts(pagination, "all", defaultPartnerList.getId(),
				// userId);
			} else {
				// saveDefaultPartnerList(orgAdminId, userListName);
				createDefaultPartnerList(loggedInUser);
				return null;
			}
		} else {
			throw new UserListException("Company Profile is not linked with the given User");
		}
	}

	private UserList createDefaultPartnerList(User loggedinUser) {
		UserList userList = null;
		if (loggedinUser != null && loggedinUser.getCompanyProfile() != null) {
			CompanyProfile company = loggedinUser.getCompanyProfile();
			String userListName = company.getCompanyProfileName() + "'s Master Partners Group";
			userList = createPartnerList(loggedinUser, userListName, true, null);
		}
		return userList;
	}

	@Override
	public UserList createPartnerList(User loggedinUser, String userListName, boolean isDefault, String csvPath) {
		UserList userList = null;
		if (loggedinUser != null && loggedinUser.getCompanyProfile() != null && userListName != null
				&& userListName.trim() != "") {
			userList = new UserList();
			Date date = new Date();
			userList.setName(userListName);
			userList.setOwner(loggedinUser);
			userList.setCompany(loggedinUser.getCompanyProfile());
			userList.setUpdatedBy(loggedinUser.getUserId());
			userList.setCreatedTime(date);
			userList.setUpdatedTime(date);
			userList.setSocialNetwork(SocialNetwork.MANUAL);
			userList.setContactType(TYPE.CONTACT);
			userList.setPartnerUserList(true);
			userList.setDefaultPartnerList(isDefault);
			if (isDefault) {
				userList.setEmailValidationInd(true);
			} else {
				userList.setUploadInProgress(true);
			}
			userList.setModuleName("PARTNERS");
			userList.setPublicList(true);
			if (org.springframework.util.StringUtils.hasText(csvPath)) {
				userList.setCsvPath(csvPath);
			}
			genericDAO.save(userList);
		}
		return userList;
	}

	@Override
	public UserListDTO getOrCreateDefaultPartnerList(Integer userId) {
		UserListDTO userListDTO = null;
		User loggedinUser = userDAO.findByPrimaryKey(userId, new FindLevel[] { FindLevel.COMPANY_PROFILE });
		UserList defaultPartnerList = getDefaultPartnerList(loggedinUser);
		if (defaultPartnerList == null) {
			defaultPartnerList = createDefaultPartnerList(loggedinUser);
		}
		userListDTO = convertUserListDTO(defaultPartnerList);
		return userListDTO;
	}

	@Override
	public UserList CreateOrGetDefaultPartnerList(User loggedinUser) {
		UserList defaultPartnerList = null;
		if (loggedinUser != null) {
			defaultPartnerList = getDefaultPartnerList(loggedinUser);
			if (defaultPartnerList == null) {
				defaultPartnerList = createDefaultPartnerList(loggedinUser);
			}
		}
		return defaultPartnerList;
	}

	@Override
	public UserList getDefaultPartnerList(User user) {
		UserList defaultPartnerList = null;
		if (user != null) {
			CompanyProfile companyProfile = user.getCompanyProfile();
			if (companyProfile != null) {
				defaultPartnerList = userListDAO.getDefaultPartnerList(companyProfile.getId());
			}
		}
		return defaultPartnerList;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void startProcessUserLists() {
	}

	public void startProcessUserLists(Collection<UserList> userLists) {
		for (UserList userList : userLists) {

			Set<User> allUsers = userList.getUsersWithoutIntermediateTabeData();
			List<User> nonProcessedUsers = new ArrayList<User>();
			if (!allUsers.isEmpty()) {
				nonProcessedUsers = allUsers.stream().filter(exuser -> exuser.isEmailValidationInd() == false)
						.collect(Collectors.toList());
			}

			if (!(nonProcessedUsers.isEmpty())) {
			} else if (nonProcessedUsers.isEmpty()) {
				if (allUsers.stream().anyMatch(u -> u.isEmailValidationInd() == false)) {
					userList.setEmailValidationInd(false);
				} else if (allUsers.stream().allMatch(u -> u.isEmailValidationInd() == true)) {
					userList.setEmailValidationInd(true);
				}
			}
		}
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void processUserList(Integer userListId) {
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> listCampaignUserLists(Pagination pagination) {
		try {
			List<Criteria> criterias = new ArrayList<>();
			List<Integer> userListIds = new ArrayList<>();
			for (Integer id : pagination.getCampaignUserListIds()) {
				userListIds.add(id);
			}
			Map<String, Object> resultMapObj = userListDAO.findCampaignUserLists(criterias,
					new FindLevel[] { FindLevel.USERS }, pagination, userListIds);
			Map<String, Object> resultMap = new HashMap<>();
			List<UserListDTO> updatedUserList = new ArrayList<>();
			List<UserList> userLists = (List<UserList>) resultMapObj.get("userLists");

			if (userLists != null && !userLists.isEmpty()) {
				Iterator<UserList> itr = userLists.iterator();
				while (itr.hasNext()) {
					UserList userList = itr.next();
					UserListDTO userListDto = convertUserListDTO(userList);
					updatedUserList.add(userListDto);
				}
			}
			resultMap.put("listOfUserLists", updatedUserList);
			resultMap.put("totalRecords", resultMapObj.get("totalRecords"));
			return resultMap;
		} catch (UserListException e) {
			logger.error("listCampaignUserLists()", e);
			throw new UserListException(e.getMessage());
		} catch (Exception e) {
			logger.error("listCampaignUserLists()", e);
			throw new UserListException(e.getMessage());
		}
	}

	@Override
	public UserUserList getByIdAndUserId(Integer id, Integer userId) {
		return userListDAO.getByIdAndUserId(id, userId);
	}

	@Override
	public User updateUserDetailsWithCampaignUserListData(Integer campaignId, User user) {
		User contactOrPartner = new User();
		ReceiverMergeTagsDTO receiverMergeTagsDTO = userListDAO.findReceiverMergeTagsInfo(campaignId, user.getUserId());
		if (receiverMergeTagsDTO != null) {
			contactOrPartner.setUserId(user.getUserId());
			contactOrPartner.setFirstName(receiverMergeTagsDTO.getFirstName());
			contactOrPartner.setLastName(receiverMergeTagsDTO.getLastName());
			contactOrPartner.setContactCompany(receiverMergeTagsDTO.getCompanyName());
			contactOrPartner.setEmailId(user.getEmailId());
			contactOrPartner.setAddress(receiverMergeTagsDTO.getAddress());
			contactOrPartner.setZipCode(receiverMergeTagsDTO.getZip());
			contactOrPartner.setCity(receiverMergeTagsDTO.getCity());
			contactOrPartner.setState(receiverMergeTagsDTO.getState());
			contactOrPartner.setCountry(receiverMergeTagsDTO.getCountry());
			contactOrPartner.setMobileNumber(receiverMergeTagsDTO.getMobileNumber());
			contactOrPartner.setAlias(user.getAlias());
			contactOrPartner.setEmailValid(user.isEmailValid());
		} else {
			contactOrPartner.setEmailId(user.getEmailId());
			contactOrPartner.setUserId(user.getUserId());
			contactOrPartner.setAlias(user.getAlias());
			contactOrPartner.setEmailValid(user.isEmailValid());
		}
		return contactOrPartner;
	}

	@Override
	public List<Integer> partnerCompanyIds(Integer userId, Integer userListId) {
		return userListDAO.getPartnerCompanyIds(userId, userListId);
	}

	@Override
	public XtremandResponse makeContactsValid(List<Integer> userIds, Integer customerId, boolean assignLeads) {

		XtremandResponse response = new XtremandResponse();
		boolean access = getAccess(false, customerId, assignLeads);

		if (access) {
			if (!userIds.isEmpty()) {
				userListDAO.makeContactsValid(userIds);

				List<UpdatedContactsHistory> updatedContactsHistoryList = new ArrayList<>();
				for (Integer userId : userIds) {
					UpdatedContactsHistory updatedContactsHistory = new UpdatedContactsHistory();
					updatedContactsHistory.setUserId(userId);
					updatedContactsHistory.setUpdatedBy(customerId);
					updatedContactsHistory.setUpdatedTime(new Date());
					updatedContactsHistoryList.add(updatedContactsHistory);
				}
				userListDAO.updatedContactsHistory(updatedContactsHistoryList);
			}
			response.setAccess(true);
		} else {
			response.setAccess(false);
		}
		return response;

	}

	@Override
	public Map<String, Object> validContactsCount(List<Integer> userListIds, Integer customerId) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		User user = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, customerId)),
				new FindLevel[] { FindLevel.ROLES, FindLevel.COMPANY_PROFILE });

		if (userListIds != null && !userListIds.isEmpty()) {
			Integer allUsersCount = userListDAO.allUsersCount(userListIds);
			Integer count = userListDAO.validContactsCount(userListIds, user.getCompanyProfile().getId());
			resultMap.put("validContactsCount", count);
			resultMap.put("allContactsCount", allUsersCount);
		} else {
			resultMap.put("validContactsCount", 0);
			resultMap.put("allContactsCount", 0);
		}
		return resultMap;
	}

	@Override
	public XtremandResponse hasAccess(boolean isPartnerUserList, Integer userId, boolean assignLeads) {
		XtremandResponse response = new XtremandResponse();
		boolean access = getAccess(isPartnerUserList, userId, assignLeads);
		response.setAccess(access);
		return response;
	}

	@Override
	public XtremandResponse validatePartners(List<User> users, Integer userListId) {
		XtremandResponse response = new XtremandResponse();
		if (!users.isEmpty() && userListId != null && userListId > 0) {
			List<String> emailIds = users.stream().map(User::getEmailId).collect(Collectors.toList());
			List<UserDTO> userDtos = userListDAO.listPartnersByUserListId(userListId);
			List<String> existingEmailIds = userDtos.stream().map(UserDTO::getEmailId).collect(Collectors.toList());
			List<String> duplicateEmailIds = utilService.findCommonStrings(
					users.stream().map(User::getEmailId).collect(Collectors.toList()), existingEmailIds);
			Set<String> removeCommonEmailIds = new HashSet<>();
			removeCommonEmailIds.addAll(duplicateEmailIds);
			if (!removeCommonEmailIds.isEmpty()) {
				validateDuplicateEmailIds(response, removeCommonEmailIds);
			} else {
				validateDeactivatedPartners(response, emailIds, userListId);
			}
		}
		return response;
	}

	private void validateDeactivatedPartners(XtremandResponse response, List<String> emailIds, Integer userListId) {
		Set<String> emailsWithDeactivatedDomains = new HashSet<>();
		Integer vendorCompanyId = userListDAO.getCompanyIdByUserListId(userListId);
		Set<String> deactivatedDomains = new HashSet<>(
				partnershipDao.findDeactivatedDomainsByCompanyId(vendorCompanyId));
		for (String email : emailIds) {
			String domain = email.substring(email.indexOf("@") + 1);
			if (deactivatedDomains.contains(domain)) {
				emailsWithDeactivatedDomains.add(email);
			}
		}
		if (XamplifyUtils.isNotEmptySet(emailsWithDeactivatedDomains)) {
			response.setStatusCode(400);
			response.setData(emailsWithDeactivatedDomains);
			if (emailsWithDeactivatedDomains.size() == 1) {
				response.setMessage("Following email domain has been deactivated");
			} else {
				response.setMessage("Following email domains has been deactivated");
			}
		} else {
			validateDuplicatePartnersAndPartnership(userListId, response, emailIds, vendorCompanyId);
		}
	}

	private void validateDuplicatePartnersAndPartnership(Integer userListId, XtremandResponse response,
			List<String> emailIds, Integer vendorCompanyId) {
		List<UserDTO> partners = new ArrayList<>();
		for (Object[] object : userDAO.getCompanyIdsByEmailIds(emailIds)) {
			UserDTO partner = new UserDTO();
			partner.setCompanyId((Integer) object[0]);
			partner.setEmailId((String) object[1]);
			partners.add(partner);
		}
		if (!partners.isEmpty()) {
			List<Integer> partnerCompanyIds = partners.stream().map(UserDTO::getCompanyId).collect(Collectors.toList());
			List<String> sameOrganizationPartnerEmailIds = new ArrayList<>();
			/******
			 * If partners belongs to same organization then throw validation
			 ****************/
			List<UserDTO> sameOrganizationPartners = partners.stream()
					.filter(partner -> partner.getCompanyId().equals(vendorCompanyId)).collect(Collectors.toList());
			if (sameOrganizationPartners.size() > 0) {
				sameOrganizationPartnerEmailIds.addAll(
						sameOrganizationPartners.stream().map(UserDTO::getEmailId).collect(Collectors.toList()));
			}
			if (sameOrganizationPartnerEmailIds.isEmpty()) {
				validateDuplicatePartnership(userListId, response, partners, partnerCompanyIds, vendorCompanyId);
			} else {
				response.setStatusCode(402);
				response.setMessage(duplicateOranizationPartnersErrorMessage);
				response.setData(sameOrganizationPartnerEmailIds);
			}
		} else {
			response.setStatusCode(200);
		}
	}

	private void validateDuplicatePartnership(Integer userListId, XtremandResponse response, List<UserDTO> partners,
			List<Integer> partnerCompanyIds, Integer vendorCompanyId) {
		List<PartnershipDTO> existingPartnersFromPartnershipTable = partnershipDao
				.listPartnerDetailsByVendorCompanyId(vendorCompanyId);
		List<Integer> existingPartnerCompanyIds = existingPartnersFromPartnershipTable.stream()
				.map(PartnershipDTO::getPartnerCompanyId).collect(Collectors.toList());
		List<Integer> duplicatePartnerCompanyIds = utilService.findCommonElements(partnerCompanyIds,
				existingPartnerCompanyIds);
		if (!duplicatePartnerCompanyIds.isEmpty()) {
			boolean isDefaultPartnerList = userListDAO.isDefaultPartnerList(userListId);
			if (isDefaultPartnerList) {
				addDuplicatePartnershipErrorMessage(response, partners, duplicatePartnerCompanyIds);
			} else {
				List<String> inputEmailIds = partners.stream()
						.filter(partner -> duplicatePartnerCompanyIds.indexOf(partner.getCompanyId()) > -1)
						.map(UserDTO::getEmailId).collect(Collectors.toList());
				List<String> existingPartnerEmailIds = existingPartnersFromPartnershipTable.stream()
						.filter(partner -> duplicatePartnerCompanyIds.indexOf(partner.getPartnerCompanyId()) > -1)
						.map(PartnershipDTO::getPartnerEmailId).collect(Collectors.toList());
				List<String> commonEmailIds = utilService.findCommonStrings(partners.stream()
						.filter(partner -> duplicatePartnerCompanyIds.indexOf(partner.getCompanyId()) > -1)
						.map(UserDTO::getEmailId).collect(Collectors.toList()), existingPartnerEmailIds);
				if (inputEmailIds.size() == commonEmailIds.size()) {
					response.setStatusCode(200);
				} else {
					addDuplicatePartnershipErrorMessage(response, partners, duplicatePartnerCompanyIds);
				}
			}

		} else {
			response.setStatusCode(200);
		}
	}

	private void addDuplicatePartnershipErrorMessage(XtremandResponse response, List<UserDTO> partners,
			List<Integer> duplicatePartnerCompanyIds) {
		response.setStatusCode(401);
		if (duplicatePartnerCompanyIds.size() == 1) {
			response.setMessage(partnershipAlreadyExists);
		} else {
			response.setMessage(partnershipsAlreadyExists);
		}
		List<String> duplicatePartnerEmailIds = partners.stream()
				.filter(line -> duplicatePartnerCompanyIds.indexOf(line.getCompanyId()) > -1).map(UserDTO::getEmailId)
				.collect(Collectors.toList());
		response.setData(duplicatePartnerEmailIds);
	}

	private void validateDuplicateEmailIds(XtremandResponse response, Set<String> duplicateEmailIds) {
		response.setStatusCode(400);
		if (duplicateEmailIds.size() == 1) {
			response.setMessage(duplicatePartnerEmailIdMessage);
		} else {
			response.setMessage(duplicatePartnerEmailIdsMessage);

		}
		response.setData(duplicateEmailIds);
	}

	@Override
	public XtremandResponse getContactsLimit(List<User> users, Integer userId) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = userDAO.getCompanyIdByUserId(userId);
		for (User user : users) {
			PartnershipDTO partnershipDto = partnershipDao
					.findContactsLimitAndNotifyPartnersByEmailIdAndVendorCompanyId(user.getEmailId(), companyId);
			user.setContactsLimit(partnershipDto.getContactsLimit());
			user.setDisableNotifyPartnersOption(partnershipDto.isDisableNotifyPartnersOption());
			user.setNotifyPartners(partnershipDto.isNotifyPartners());
			/*********** XNFR-85 *************/
			setTeamMemberGroupDetails(user, partnershipDto.getId());
		}
		response.setStatusCode(200);
		response.setData(users);
		/********** XNFR-85 *************/
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("teamMemberGroups", teamMemberGroupService.findAllGroupIdsAndNamesByCompanyId(companyId, true));
		response.setMap(map);
		return response;
	}

	/*********** XNFR-85 *************/
	private void setTeamMemberGroupDetails(User user, Integer partnershipId) {
		List<PartnerTeamMemberGroupDTO> partnerTeamMemberGroupDTOs = teamMemberGroupDao
				.findTeamMemberGroupIdByPartnershipId(partnershipId);
		if (partnerTeamMemberGroupDTOs != null && !partnerTeamMemberGroupDTOs.isEmpty()) {
			if (partnerTeamMemberGroupDTOs.size() > 1) {
				user.setMultipleTeamMemberGroupsAssigned(true);
				setDefaultTeamMemberGroupData(user);
				String message = partnerTeamMemberGroupDTOs.stream().map(PartnerTeamMemberGroupDTO::getId)
						.collect(Collectors.toList()) + " and partnershipId:-" + partnershipId;
				logger.error("setTeamMemberGroupDetails();Multiple Groups Assigned:-" + message);
			} else {
				PartnerTeamMemberGroupDTO partnerTeamMemberGroupDTO = partnerTeamMemberGroupDTOs.get(0);
				user.setTeamMemberGroupId(partnerTeamMemberGroupDTO.getId());
				user.setSelectedTeamMembersCount(partnerTeamMemberGroupDTO.getCount());
				user.setSelectedTeamMemberIds(
						teamMemberGroupDao.findSelectedTeamMemberGroupUserMappingIdsByPartnershipId(partnershipId));
				user.setSelectedTeamMemberGroupName(partnerTeamMemberGroupDTO.getTeamMemberGroupName());
			}
		} else {
			setDefaultTeamMemberGroupData(user);
		}
	}

	/*************** XNFR-85 *****************/
	private void setDefaultTeamMemberGroupData(User user) {
		user.setTeamMemberGroupId(0);
		user.setSelectedTeamMembersCount(0);
		user.setSelectedTeamMemberIds(new HashSet<Integer>());
		user.setSelectedTeamMemberGroupName("");
	}

	@Override
	public XtremandResponse listPartnerEmailsByOrganization(Integer userId) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = userDAO.getCompanyIdByUserId(userId);
		List<String> emailIds = userListDAO.listPartnerEmailsByOrganization(companyId);
		response.setMessage("success");
		response.setStatusCode(200);
		response.setData(emailIds);
		return response;
	}

	@Override
	public XtremandResponse saveLeadsList(UserUserListWrapper userUserListWrapper, Integer userId) {
		XtremandResponse response = new XtremandResponse();
		boolean hasAccess = utilService.hasShareLeadsModuleAccess(userId);
		if (hasAccess) {
			response = assignList(userUserListWrapper, userId, response);
			response.setAccess(true);
		} else {
			response.setAccess(false);
		}
		return response;
	}

	@Override
	public XtremandResponse saveAsShareLeadsList(Integer userId, UserListDTO userListDTO) {
		XtremandResponse response = new XtremandResponse();
		User loggedInUser = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, userId)),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		boolean hasAccess = utilService.hasShareLeadsModuleAccess(userId);
		if (hasAccess) {
			UserList existingUserList = userListDAO.findByPrimaryKey(userListDTO.getId(),
					new FindLevel[] { FindLevel.USERS });
			Set<UserDTO> users = existingUserList.getUserDTOs();
			List<String> nonExistingEmailIds = userService
					.getNonExistingUsers(users.stream().map(UserDTO::getEmailId).collect(Collectors.toList()));
			for (UserDTO exuser : users) {
				if (userListDTO.getId() != null) {
					exuser.setLegalBasis(
							userListDAO.listLegalBasisByContactListIdAndUserId(userListDTO.getId(), exuser.getId()));
				}
			}
			UserList userList = new UserList();
			userList.setUploadedDate(new Date());
			SocialNetwork socialNetwork = existingUserList.getSocialNetwork();
			userList.setSocialNetwork(socialNetwork);
			userList.setSynchronisedList(socialNetwork == SocialNetwork.MANUAL ? false : true);
			userList.setPartnerUserList(false);
			userList.initialiseCommonFields(true, userId);
			userList.setName(userListDTO.getName().trim());
			userList.setContactType(existingUserList.getContactType());
			userList.setPublicList(existingUserList.getPublicList());
			userList.setAssignedBy(loggedInUser);
			userList.setAssignedCompany(loggedInUser.getCompanyProfile());
			userList.setPublicList(true);
			userList.setModuleName("SHARE LEADS");
			userList.setAlias(existingUserList.getAlias());
			response = validateAssignedList(users, userListDTO.getName(), loggedInUser, response);
			if (response.getStatusCode() == 200) {
				if (!nonExistingEmailIds.isEmpty()) {
				}
				if (!(nonExistingEmailIds.isEmpty())) {
				} else if (nonExistingEmailIds.isEmpty() && !userListDTO.isPartnerUserList()) {
					if (userList.getUsers().stream().anyMatch(u -> u.isEmailValidationInd() == false)) {
						userList.setEmailValidationInd(false);
					} else if (userList.getUsers().stream().allMatch(u -> u.isEmailValidationInd() == true)) {
						userList.setEmailValidationInd(true);
					}
				}
				List<Integer> totalUnsubscribedUserIds = userListDAO
						.getUnsubscribedUsers(loggedInUser.getCompanyProfile().getId());
				utilService.sendLeadsListMail(loggedInUser, userList, true, totalUnsubscribedUserIds);
				logger.info("UserList created successfully..!! ");
				response.setMessage("Your lead list has been saved successfully.");
			}
			response.setAccess(true);
		} else {
			response.setAccess(false);
		}
		return response;
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	private XtremandResponse assignList(UserUserListWrapper userUserListWrapper, Integer userId,
			XtremandResponse xtremandResponse) {
		Set<UserDTO> users = userUserListWrapper.getUsers();
		userService.removeNullEmailUserDTOs(users);
		UserListDTO userListDTO = userUserListWrapper.getUserList();
		List<String> nonExistingEmailIds = userService
				.getNonExistingUsers(users.stream().map(UserDTO::getEmailId).collect(Collectors.toList()));
		User vendor = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, userId)),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		UserList userList = getUserList(userListDTO, vendor);
		userList.setPublicList(true);
		userList.setModuleName("SHARE LEADS");
		userList.setAlias(userListDTO.getAlias());
		Long externalListId = userListDTO.getExternalListId();
		if (externalListId != null && externalListId > 0) {
			userList.setExternalListId(externalListId);
		}
		xtremandResponse = validateAssignedList(users, userListDTO.getName(), vendor, xtremandResponse);
		if (xtremandResponse.getStatusCode() == 200) {
			if (!nonExistingEmailIds.isEmpty()) {
			}
			if (!(nonExistingEmailIds.isEmpty())) {
				userList.setAssignedCompany(vendor.getCompanyProfile());
			} else if (nonExistingEmailIds.isEmpty() && !userListDTO.isPartnerUserList()) {
				if (userList.getUsers().stream().anyMatch(u -> u.isEmailValidationInd() == false)) {
					userList.setEmailValidationInd(false);
				} else if (userList.getUsers().stream().allMatch(u -> u.isEmailValidationInd() == true)) {
					userList.setEmailValidationInd(true);
				}
			}

			List<Integer> totalUnsubscribedUserIds = userListDAO
					.getUnsubscribedUsers(vendor.getCompanyProfile().getId());
			utilService.sendLeadsListMail(vendor, userList, true, totalUnsubscribedUserIds);
			logger.info("UserList created successfully..!! ");
			xtremandResponse.setMessage("Your leads list has been created successfully and it is being processed.");
		}
		return xtremandResponse;
	}

	private UserList getUserList(UserListDTO userListDTO, User vendor) {
		UserList userList = new UserList();
		userList.setUploadedDate(new Date());
		SocialNetwork socialNetwork = UserList.getSocialNetworkEnum(userListDTO.getSocialNetwork().toUpperCase());
		userList.setSocialNetwork(socialNetwork);
		if (userListDTO.getId() != null) {
			userList.setSynchronisedList(false);
		} else {
			userList.setSynchronisedList(socialNetwork == SocialNetwork.MANUAL ? false : true);
		}
		userList.setPartnerUserList(false);
		userList.initialiseCommonFields(true, vendor.getUserId());
		userList.setName(userListDTO.getName().trim());
		userList.setContactType(UserList.getContactTypeEnum(userListDTO.getContactType()));
		userList.setPublicList(userListDTO.getPublicList());
		userList.setAssignedBy(vendor);
		userList.setAssignedCompany(vendor.getCompanyProfile());
		return userList;
	}

	public XtremandResponse validateAssignedList(Set<UserDTO> users, String listName, User vendor,
			XtremandResponse xtremandResponse) {
		Criteria criteria1 = new Criteria("assignedCompany.id", OPERATION_NAME.eq, vendor.getCompanyProfile().getId());
		Criteria criteria2 = new Criteria("moduleName", OPERATION_NAME.eq, "SHARE LEADS");
		List<Criteria> criterias = new ArrayList<Criteria>();
		criterias.add(criteria1);
		criterias.add(criteria2);
		List<Criterion> criterions = userListDAO.generateCriteria(criterias);
		List<String> names = userListDAO.listUserlistNames(criterions);
		xtremandResponse = userListValidator.validateList(users, listName, names, xtremandResponse);
		return xtremandResponse;
	}

	@Override
	public Map<String, Object> assignedLeadLists(Integer userId, Pagination pagination) throws UserListException {
		Map<String, Object> resultMap = new HashMap<>();
		User user = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, userId)),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		if (userId != null) {
			resultMap = listShareLists(user.getCompanyProfile().getId(), pagination);
		} else {
			resultMap.put("listOfUserLists", Collections.emptyList());
			resultMap.put("totalRecords", 0);
		}
		return resultMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> listPartnersByVendorCompanyId(Integer userId, Pagination pagination) {
		Map<String, Object> resultMap = new HashMap<>();
		Integer companyId = userDAO.getCompanyIdByUserId(userId);
		Map<String, Object> resultMapObj = partnershipDao.listPartnersByVendorCompanyId(companyId, pagination);
		List<UserDTO> partners = (List<UserDTO>) resultMapObj.get("data");
		resultMap.put("partners", partners);
		resultMap.put("totalRecords", resultMapObj.get("totalRecords"));
		return resultMap;
	}

	private void deleteLeadsFromLeadsList(UserList userList, Integer customerId, List<Integer> removeUserIdsList) {
		userList.initialiseCommonFields(false, customerId);
		List<Integer> userListIds = new ArrayList<>();
		userListIds.add(userList.getId());
		if (XamplifyUtils.isNotEmptyList(removeUserIdsList) && XamplifyUtils.isNotEmptyList(userListIds)) {
			userListDAO.deletePartnersFromPartnerLists(removeUserIdsList, userListIds);
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> listUserListscontacts(Pagination pagination, List<Integer> userListIds,
			User loggedInUser, UserListDTO userListDTO) {
		Map<String, Object> resultMap = new HashMap<>();
		Criteria[] criterias = pagination.getCriterias();
		String searchHQL = "";
		boolean isPartnerUserList = userListDTO.isPartnerUserList();
		String contactType = "";
		Integer companyId = loggedInUser.getCompanyProfile().getId();

		if (ArrayUtils.isNotEmpty(criterias) && !criterias[0].getProperty().equalsIgnoreCase("Field Name*")) {
			StringBuilder sb = new StringBuilder();
			sb.append(" and (");
			for (int i = 0; i < criterias.length; i++) {
				Criteria criteria = criterias[i];
				criteria.setOperationName(Criteria.getOperationNameEnum(criteria.getOperation()));

				if (!criteria.getProperty().equalsIgnoreCase("Field Name*")) {
					if (i != 0) {
						sb.append(" or ");
					}

					if (criteria.getProperty().equalsIgnoreCase("country")
							&& criteria.getOperationName() == OPERATION_NAME.eq) {
						sb.append(" lower(uul.country)='" + criteria.getValue1().toString().toLowerCase() + "' ");
					} else if (criteria.getProperty().equalsIgnoreCase("country")
							&& criteria.getOperationName() == OPERATION_NAME.like) {
						sb.append(
								" lower(uul.country) like '%" + criteria.getValue1().toString().toLowerCase() + "%' ");
					} else if (criteria.getProperty().equalsIgnoreCase("contactCompany")
							&& criteria.getOperationName() == OPERATION_NAME.eq) {
						sb.append(
								" lower(uul.contact_company)='" + criteria.getValue1().toString().toLowerCase() + "' ");
					} else if (criteria.getProperty().equalsIgnoreCase("contactCompany")
							&& criteria.getOperationName() == OPERATION_NAME.like) {
						sb.append(" lower(uul.contact_company) like '%" + criteria.getValue1().toString().toLowerCase()
								+ "%' ");
					} else if (criteria.getProperty().equalsIgnoreCase("jobTitle")
							&& criteria.getOperationName() == OPERATION_NAME.eq) {
						sb.append(" lower(uul.job_title)='" + criteria.getValue1().toString().toLowerCase() + "' ");
					} else if (criteria.getProperty().equalsIgnoreCase("jobTitle")
							&& criteria.getOperationName() == OPERATION_NAME.like) {
						sb.append(" lower(uul.job_title) like '%" + criteria.getValue1().toString().toLowerCase()
								+ "%' ");
					} else if (criteria.getProperty().equalsIgnoreCase("firstName")
							&& criteria.getOperationName() == OPERATION_NAME.eq) {
						sb.append(" lower(uul.firstname)='" + criteria.getValue1().toString().toLowerCase() + "' ");
					} else if (criteria.getProperty().equalsIgnoreCase("firstName")
							&& criteria.getOperationName() == OPERATION_NAME.like) {
						sb.append(" lower(uul.firstname) like '%" + criteria.getValue1().toString().toLowerCase()
								+ "%' ");
					} else if (criteria.getProperty().equalsIgnoreCase("lastName")
							&& criteria.getOperationName() == OPERATION_NAME.eq) {
						sb.append(" lower(uul.lastname)='" + criteria.getValue1().toString().toLowerCase() + "' ");
					} else if (criteria.getProperty().equalsIgnoreCase("lastName")
							&& criteria.getOperationName() == OPERATION_NAME.like) {
						sb.append(
								" lower(uul.lastname) like '%" + criteria.getValue1().toString().toLowerCase() + "%' ");
					} else if (criteria.getProperty().equalsIgnoreCase("city")
							&& criteria.getOperationName() == OPERATION_NAME.eq) {
						sb.append(" lower(uul.city)='" + criteria.getValue1().toString().toLowerCase() + "' ");
					} else if (criteria.getProperty().equalsIgnoreCase("city")
							&& criteria.getOperationName() == OPERATION_NAME.like) {
						sb.append(" lower(uul.city) like '%" + criteria.getValue1().toString().toLowerCase() + "%' ");
					} else if (criteria.getProperty().equalsIgnoreCase("mobileNumber")
							&& criteria.getOperationName() == OPERATION_NAME.eq) {
						sb.append(" lower(uul.mobile_number)='" + criteria.getValue1().toString().toLowerCase() + "' ");
					} else if (criteria.getProperty().equalsIgnoreCase("mobileNumber")
							&& criteria.getOperationName() == OPERATION_NAME.like) {
						sb.append(" lower(uul.mobile_number) like '%" + criteria.getValue1().toString().toLowerCase()
								+ "%' ");
					} else if (criteria.getProperty().equalsIgnoreCase("description")
							&& criteria.getOperationName() == OPERATION_NAME.eq) {
						sb.append(" lower(uul.description)='" + criteria.getValue1().toString().toLowerCase() + "' ");
					} else if (criteria.getProperty().equalsIgnoreCase("description")
							&& criteria.getOperationName() == OPERATION_NAME.like) {
						sb.append(" lower(uul.description) like '%" + criteria.getValue1().toString().toLowerCase()
								+ "%' ");
					} else if (criteria.getProperty().equalsIgnoreCase("emailId")
							&& criteria.getOperationName() == OPERATION_NAME.eq) {
						sb.append(" lower(up.email_id) = '" + criteria.getValue1().toString().toLowerCase() + "' ");
					} else if (criteria.getProperty().equalsIgnoreCase("emailId")
							&& criteria.getOperationName() == OPERATION_NAME.like) {
						sb.append(
								" lower(up.email_id) like '%" + criteria.getValue1().toString().toLowerCase() + "%' ");
					} else if (criteria.getProperty().equalsIgnoreCase("state")
							&& criteria.getOperationName() == OPERATION_NAME.eq) {
						sb.append(" lower(uul.state)='" + criteria.getValue1().toString().toLowerCase() + "' ");
					} else if (criteria.getProperty().equalsIgnoreCase("state")
							&& criteria.getOperationName() == OPERATION_NAME.like) {
						sb.append(" lower(uul.state) like '%" + criteria.getValue1().toString().toLowerCase() + "%' ");
					} else if (criteria.getProperty().equalsIgnoreCase("Account Name")
							&& criteria.getOperationName() == OPERATION_NAME.eq) {
						sb.append(" lower(uul.account_name)='" + criteria.getValue1().toString().toLowerCase() + "' ");
					} else if (criteria.getProperty().equalsIgnoreCase("Account Name")
							&& criteria.getOperationName() == OPERATION_NAME.like) {
						sb.append(" lower(uul.account_name) like '%" + criteria.getValue1().toString().toLowerCase()
								+ "%' ");
					} else if (criteria.getProperty().equalsIgnoreCase("Account Sub Type")
							&& criteria.getOperationName() == OPERATION_NAME.eq) {
						sb.append(" lower(uul.account_sub_type)='" + criteria.getValue1().toString().toLowerCase()
								+ "' ");
					} else if (criteria.getProperty().equalsIgnoreCase("Account Sub Type")
							&& criteria.getOperationName() == OPERATION_NAME.like) {
						sb.append(" lower(uul.account_sub_type) like '%" + criteria.getValue1().toString().toLowerCase()
								+ "%' ");
					} else if (criteria.getProperty().equalsIgnoreCase("Territory")
							&& criteria.getOperationName() == OPERATION_NAME.eq) {
						sb.append(" lower(uul.territory)='" + criteria.getValue1().toString().toLowerCase() + "' ");
					} else if (criteria.getProperty().equalsIgnoreCase("Territory")
							&& criteria.getOperationName() == OPERATION_NAME.like) {
						sb.append(" lower(uul.territory) like '%" + criteria.getValue1().toString().toLowerCase()
								+ "%' ");
					} else if (criteria.getProperty().equalsIgnoreCase("Company Domain")
							&& criteria.getOperationName() == OPERATION_NAME.eq) {
						sb.append(
								" lower(uul.company_domain)='" + criteria.getValue1().toString().toLowerCase() + "' ");
					} else if (criteria.getProperty().equalsIgnoreCase("Company Domain")
							&& criteria.getOperationName() == OPERATION_NAME.like) {
						sb.append(" lower(uul.company_domain) like '%" + criteria.getValue1().toString().toLowerCase()
								+ "%' ");
					} else if (criteria.getProperty().equalsIgnoreCase("Account Owner")
							&& criteria.getOperationName() == OPERATION_NAME.eq) {
						sb.append(" lower(uul.account_owner)='" + criteria.getValue1().toString().toLowerCase() + "' ");
					} else if (criteria.getProperty().equalsIgnoreCase("Account Owner")
							&& criteria.getOperationName() == OPERATION_NAME.like) {
						sb.append(" lower(uul.account_owner) like '%" + criteria.getValue1().toString().toLowerCase()
								+ "%' ");
					} else if (criteria.getProperty().equalsIgnoreCase("Website")
							&& criteria.getOperationName() == OPERATION_NAME.eq) {
						sb.append(" lower(uul.website)='" + criteria.getValue1().toString().toLowerCase() + "' ");
					} else if (criteria.getProperty().equalsIgnoreCase("Website")
							&& criteria.getOperationName() == OPERATION_NAME.like) {
						sb.append(
								" lower(uul.website) like '%" + criteria.getValue1().toString().toLowerCase() + "%' ");
					} else if (criteria.getProperty().equalsIgnoreCase("Vertical")
							&& criteria.getOperationName() == OPERATION_NAME.eq) {
						sb.append(" lower(uul.vertical)='" + criteria.getValue1().toString().toLowerCase() + "' ");
					} else if (criteria.getProperty().equalsIgnoreCase("Vertical")
							&& criteria.getOperationName() == OPERATION_NAME.like) {
						sb.append(
								" lower(uul.vertical) like '%" + criteria.getValue1().toString().toLowerCase() + "%' ");
					} else if (criteria.getProperty().equalsIgnoreCase("Type")
							&& criteria.getOperationName() == OPERATION_NAME.eq) {
						sb.append(" lower(uul.partner_type)='" + criteria.getValue1().toString().toLowerCase() + "' ");
					} else if (criteria.getProperty().equalsIgnoreCase("Type")
							&& criteria.getOperationName() == OPERATION_NAME.like) {
						sb.append(" lower(uul.partner_type) like '%" + criteria.getValue1().toString().toLowerCase()
								+ "%' ");
					} else if (criteria.getProperty().equalsIgnoreCase("Region")
							&& criteria.getOperationName() == OPERATION_NAME.eq) {
						sb.append(" lower(uul.region)='" + criteria.getValue1().toString().toLowerCase() + "' ");
					} else if (criteria.getProperty().equalsIgnoreCase("Region")
							&& criteria.getOperationName() == OPERATION_NAME.like) {
						sb.append(" lower(uul.region) like '%" + criteria.getValue1().toString().toLowerCase() + "%' ");
					} else if (criteria.getProperty().equalsIgnoreCase("Category")
							&& criteria.getOperationName() == OPERATION_NAME.eq) {
						sb.append(" lower(uul.category)='" + criteria.getValue1().toString().toLowerCase() + "' ");
					} else if (criteria.getProperty().equalsIgnoreCase("Category")
							&& criteria.getOperationName() == OPERATION_NAME.like) {
						sb.append(
								" lower(uul.category) like '%" + criteria.getValue1().toString().toLowerCase() + "%' ");
					} else if (criteria.getProperty().equalsIgnoreCase("Zip Code")
							&& criteria.getOperationName() == OPERATION_NAME.eq) {
						sb.append(" lower(uul.zip)='" + criteria.getValue1().toString().toLowerCase() + "' ");
					} else if (criteria.getProperty().equalsIgnoreCase("Zip Code")
							&& criteria.getOperationName() == OPERATION_NAME.like) {
						sb.append(" lower(uul.zip) like '%" + criteria.getValue1().toString().toLowerCase() + "%' ");
					} else if (criteria.getProperty().equalsIgnoreCase("Contact Status")
							&& criteria.getOperationName() == OPERATION_NAME.eq) {
						sb.append(" lower(xcs.stage_name)='" + criteria.getValue1().toString().toLowerCase() + "' ");
					} else if (criteria.getProperty().equalsIgnoreCase("Contact Status")
							&& criteria.getOperationName() == OPERATION_NAME.like) {
						sb.append(" lower(xcs.stage_name) like '%" + criteria.getValue1().toString().toLowerCase()
								+ "%' ");
					}
				}
			}
			sb.append(" ) ");
			searchHQL = sb.toString();
		}

		if (XamplifyUtils.isValidString(pagination.getSearchKey())) {
			String searchKey = pagination.getSearchKey().toLowerCase();
			searchHQL += " and (LOWER(up.email_id) like '%" + searchKey + "%' " + "or  LOWER(uul.firstname) like '%"
					+ searchKey + "%' " + "or LOWER(uul.lastname) like '%" + searchKey + "%' "
					+ "or LOWER(uul.contact_company) like '%" + searchKey + "%' "
					+ "or REPLACE(LOWER(uul.firstname || uul.lastname), ' ', '') like '%" + searchKey.replace(" ", "")
					+ "%' " + "or REPLACE(LOWER(uul.lastname || uul.firstname), ' ','') like '%"
					+ searchKey.replace(" ", "") + "%' " + "or LOWER(xcs.stage_name) ilike '%" + searchKey + "%'";
			if (Boolean.TRUE.equals(isPartnerUserList)) {
				searchHQL += "or LOWER(xcp.company_name) like '%" + searchKey + "%') ";
			} else {
				searchHQL += ") ";
			}
		}

		utilService.setDateFilters(pagination);
		searchHQL += XamplifyUtils.frameDateFilterQuery(pagination, "ps.created_time");

		String sortSQL = "";
		String sortColumn = "";
		String sortColumnSQL = "";
		String sortingOrder = "";
		String dataSortSQL = "";
		if (XamplifyUtils.isValidString(pagination.getSortcolumn())
				&& XamplifyUtils.isValidString(pagination.getSortingOrder())) {
			if (pagination.getSortcolumn().equalsIgnoreCase("emailId")
					&& pagination.getSortingOrder().toLowerCase().equalsIgnoreCase("asc")) {
				sortSQL = " order by up.email_id asc NULLS LAST ";
				dataSortSQL = "order by a.\"emailId\" asc NULLS LAST";
				sortColumn = " up.email_id ";
			} else if (pagination.getSortcolumn().equalsIgnoreCase("emailId")
					&& pagination.getSortingOrder().toLowerCase().equalsIgnoreCase("desc")) {
				sortSQL = "order by up.email_id desc NULLS LAST ";
				dataSortSQL = "order by a.\"emailId\" desc NULLS LAST";
				sortColumn = " up.email_id ";
			} else if (pagination.getSortcolumn().equalsIgnoreCase("firstName")
					&& pagination.getSortingOrder().toLowerCase().equalsIgnoreCase("asc")) {
				sortSQL = "order by uul.firstname asc NULLS LAST ";
				sortColumn = " uul.firstname ";
				dataSortSQL = "order by a.\"firstName\" asc NULLS LAST";
			} else if (pagination.getSortcolumn().equalsIgnoreCase("firstName")
					&& pagination.getSortingOrder().toLowerCase().equalsIgnoreCase("desc")) {
				sortSQL = " order by uul.firstname desc NULLS LAST ";
				sortColumn = " uul.firstname ";
				dataSortSQL = "order by a.\"firstName\" desc NULLS LAST";
			} else if (pagination.getSortcolumn().equalsIgnoreCase("lastName")
					&& pagination.getSortingOrder().toLowerCase().equalsIgnoreCase("asc")) {
				sortSQL = "order by uul.lastName asc NULLS LAST ";
				sortColumn = " uul.lastName ";
				dataSortSQL = "order by a.\"lastName\" asc NULLS LAST";
			} else if (pagination.getSortcolumn().equalsIgnoreCase("lastName")
					&& pagination.getSortingOrder().toLowerCase().equalsIgnoreCase("desc")) {
				sortSQL = " order by uul.lastName desc NULLS LAST ";
				sortColumn = " uul.lastName ";
				dataSortSQL = "order by a.\"lastName\" desc NULLS LAST";
			} else if (pagination.getSortcolumn().equalsIgnoreCase("contactCompany")
					&& pagination.getSortingOrder().toLowerCase().equalsIgnoreCase("asc")) {
				sortSQL = "order by uul.contact_company asc NULLS LAST ";
				sortColumn = " uul.contact_company ";
				dataSortSQL = "order by a.\"contactCompany\" asc NULLS LAST";
			} else if (pagination.getSortcolumn().equalsIgnoreCase("contactCompany")
					&& pagination.getSortingOrder().toLowerCase().equalsIgnoreCase("desc")) {
				sortSQL = "order by uul.contact_company desc NULLS LAST ";
				sortColumn = " uul.contact_company ";
				dataSortSQL = "order by a.\"contactCompany\" desc NULLS LAST";
			} else if (pagination.getSortcolumn().equalsIgnoreCase("vertical")
					&& pagination.getSortingOrder().toLowerCase().equalsIgnoreCase("asc")) {
				sortSQL = "order by uul.vertical asc NULLS LAST ";
				sortColumn = " uul.vertical ";
				dataSortSQL = "order by a.\"vertical\" asc NULLS LAST";
			} else if (pagination.getSortcolumn().equalsIgnoreCase("vertical")
					&& pagination.getSortingOrder().toLowerCase().equalsIgnoreCase("desc")) {
				sortSQL = "order by uul.vertical desc NULLS LAST ";
				sortColumn = " uul.vertical ";
				dataSortSQL = "order by a.\"vertical\" desc NULLS LAST";
			} else if (pagination.getSortcolumn().equalsIgnoreCase("region")
					&& pagination.getSortingOrder().toLowerCase().equalsIgnoreCase("asc")) {
				sortSQL = "order by uul.region asc NULLS LAST ";
				sortColumn = " uul.region ";
				dataSortSQL = "order by a.\"region\" asc NULLS LAST";
			} else if (pagination.getSortcolumn().equalsIgnoreCase("region")
					&& pagination.getSortingOrder().toLowerCase().equalsIgnoreCase("desc")) {
				sortSQL = "order by uul.region desc NULLS LAST ";
				sortColumn = " uul.region ";
				dataSortSQL = "order by a.\"region\" desc NULLS LAST";
			} else if (pagination.getSortcolumn().equalsIgnoreCase("partnerType")
					&& pagination.getSortingOrder().toLowerCase().equalsIgnoreCase("asc")) {
				sortSQL = "order by uul.partner_type asc NULLS LAST ";
				sortColumn = " uul.partner_type ";
				dataSortSQL = "order by a.\"partnerType\" asc NULLS LAST";
			} else if (pagination.getSortcolumn().equalsIgnoreCase("partnerType")
					&& pagination.getSortingOrder().toLowerCase().equalsIgnoreCase("desc")) {
				sortSQL = "order by uul.partner_type desc NULLS LAST ";
				sortColumn = " uul.partner_type ";
				dataSortSQL = "order by a.\"partnerType\" desc NULLS LAST";
			} else if (pagination.getSortcolumn().equalsIgnoreCase("category")
					&& pagination.getSortingOrder().toLowerCase().equalsIgnoreCase("asc")) {
				sortSQL = "order by uul.category asc NULLS LAST ";
				sortColumn = " uul.category ";
				dataSortSQL = "order by a.\"category\" asc NULLS LAST";
			} else if (pagination.getSortcolumn().equalsIgnoreCase("category")
					&& pagination.getSortingOrder().toLowerCase().equalsIgnoreCase("desc")) {
				sortSQL = "order by uul.category desc NULLS LAST ";
				sortColumn = " uul.category ";
				dataSortSQL = "order by a.\"category\" desc NULLS LAST";
			} else if (pagination.getSortcolumn().equalsIgnoreCase("id")
					&& pagination.getSortingOrder().toLowerCase().equalsIgnoreCase("asc")) {
				sortSQL = "order by u.user_list_id asc NULLS LAST ";
				sortColumn = " u.user_list_id ";
				dataSortSQL = "order by a.\"userListId\" asc NULLS LAST";
			} else if (pagination.getSortcolumn().equalsIgnoreCase("id")
					&& pagination.getSortingOrder().toLowerCase().equalsIgnoreCase("desc")) {
				sortSQL = "order by u.user_list_id desc NULLS LAST ";
				sortColumn = " u.user_list_id ";
				dataSortSQL = "order by a.\"userListId\" desc NULLS LAST";
			} else if (pagination.getSortcolumn().equalsIgnoreCase("createdTime")
					&& pagination.getSortingOrder().toLowerCase().equalsIgnoreCase("asc")) {
				sortSQL = "order by ps.created_time asc NULLS LAST ";
				sortColumn = " ps.created_time ";
				dataSortSQL = "order by a.\"createdTime\" asc NULLS LAST";
			} else if (pagination.getSortcolumn().equalsIgnoreCase("createdTime")
					&& pagination.getSortingOrder().toLowerCase().equalsIgnoreCase("desc")) {
				sortSQL = "order by ps.created_time desc NULLS LAST ";
				sortColumn = " ps.created_time ";
				dataSortSQL = "order by a.\"createdTime\" desc NULLS LAST";
			}

			if (userListDTO.getContactType() != null && userListDTO.getContactType().equalsIgnoreCase("excluded")) {
				sortColumn = "max(" + sortColumn + ")";
			}
			sortColumnSQL = " , " + sortColumn + "as sortColumn ";
			sortingOrder = "  order by au.sortColumn  " + pagination.getSortingOrder().toLowerCase();
		}

		Map<String, Object> map = userListDAO.fetchUserListUsers(loggedInUser, userListIds, searchHQL, sortSQL,
				dataSortSQL, sortColumnSQL, sortingOrder, pagination, userListDTO);

		List<UserDTO> userDTOs = (List<UserDTO>) map.get("userDTOList");

		Integer totalRecords = (Integer) map.get("totalRecords");

		for (UserDTO userDTO : userDTOs) {
			userDTO.setCountry(WordUtils.capitalizeFully(userDTO.getCountry()));
			if (Boolean.FALSE.equals(userListDTO.getIsDownload())) {
				setContactsLimitAndMdfAmount(isPartnerUserList, companyId, userDTO);
				if (contactType != null && contactType.equalsIgnoreCase("invalid")
						&& Boolean.TRUE.equals(!isPartnerUserList)) {
					ArrayList<Integer> contactListIds = userListDAO.getUserListIdsBYUserId(isPartnerUserList,
							userDTO.getId(), companyId);
					userDTO.setUserListIds(contactListIds);
				}
				boolean isSignup = false;
				isSignup = ((userDTO.getPassword() != null) || ((userDTO.getPassword() == null)
						&& ((userDTO.getUserStatus()).equalsIgnoreCase("APPROVE"))));
				userDTO.setSignedUp(isSignup);
				if (userDTO.getUserListId() != null) {
					userDTO.setLegalBasis(userListDAO.listLegalBasisByContactListIdAndUserId(userDTO.getUserListId(),
							userDTO.getId()));
				}

				if (Boolean.TRUE.equals(isPartnerUserList)) {
					setContactCompany(userDTO);
				}
			}
			// XNFR-680
			mapFlexiFieldsToUserDTO(userListDTO, userDTO);
		}

		resultMap.put("listOfUsers", userDTOs);
		resultMap.put("totalRecords", totalRecords);
		return resultMap;
	}

	private void mapFlexiFieldsToUserDTO(UserListDTO userListDTO, UserDTO userDTO) {
		String moduleName = XamplifyUtils.isValidString(userListDTO.getModuleName())
				? userListDTO.getModuleName().toLowerCase()
				: null;
		if (XamplifyUtils.CONTACTS.equalsIgnoreCase(moduleName)) {
			List<FlexiFieldRequestDTO> flexiFields = flexiFieldDao
					.findFlexiFieldsBySelectedUserIdAndUserListId(userDTO.getUserListId(), userDTO.getId());
			userDTO.setFlexiFields(flexiFields);
		}
	}

	private void setContactCompany(UserDTO userDTO) {
		if (userDTO.getContactCompany() != null && userDTO.getPartnerCompanyName() != null && userDTO
				.getCompanyNameStatus().toLowerCase().equalsIgnoreCase(CompanyNameStatus.ACTIVE.name().toLowerCase())) {
			String updatedContactCompany = userDTO.getPartnerCompanyName() + ", aka-" + userDTO.getContactCompany();
			userDTO.setDisplayContactCompany(updatedContactCompany);
		} else {
			userDTO.setDisplayContactCompany(userDTO.getContactCompany());
		}
	}

	private void setContactsLimitAndMdfAmount(Boolean isPartnerUserList, Integer companyId, UserDTO userDTO) {
		if (Boolean.TRUE.equals(isPartnerUserList)) {
			Partnership partnership = partnershipDao
					.getMdfAmountAndContactsLimitAndNotifyPartnersByVendorCompanyIdAndPartnerId(companyId,
							userDTO.getId());
			if (partnership != null) {
				userDTO.setContactsLimit(partnership.getContactsLimit());
				userDTO.setNotifyPartners(partnership.isNotifyPartners());
				/*************** XNFR-85 *****************/
				setTeamMemberGroupDetails(userDTO, partnership.getId());
				/****** XNFR-224 ******/
				CompanyProfile partnerCompany = partnership.getPartnerCompany();
				if (partnerCompany != null) {
					userDTO.setCompanyId(partnerCompany.getId());
				} else {
					userDTO.setCompanyId(0);
				}
				userDTO.setLoginAsPartnerOptionEnabledForVendor(partnership.isLoginAsPartnerOptionEnabledForVendor());
				userDTO.setPartnershipId(partnership.getId());
				/****** XNFR-224 ******/

			}
		}
	}

	/*************** XNFR-85 *****************/
	private void setTeamMemberGroupDetails(UserDTO userDTO, Integer partnershipId) {
		List<PartnerTeamMemberGroupDTO> partnerTeamMemberGroupDTOs = teamMemberGroupDao
				.findTeamMemberGroupIdByPartnershipId(partnershipId);
		if (partnerTeamMemberGroupDTOs != null && !partnerTeamMemberGroupDTOs.isEmpty()) {
			if (partnerTeamMemberGroupDTOs.size() > 1) {
				userDTO.setMultipleTeamMemberGroupsAssigned(true);
				setDefaultTeamMemberGroupData(userDTO);
				String message = partnerTeamMemberGroupDTOs.stream().map(PartnerTeamMemberGroupDTO::getId)
						.collect(Collectors.toList()) + " and partnershipId:-" + partnershipId;
				logger.error("setTeamMemberGroupDetails();Multiple Groups Assigned:-" + message);
			} else {
				PartnerTeamMemberGroupDTO partnerTeamMemberGroupDTO = partnerTeamMemberGroupDTOs.get(0);
				userDTO.setTeamMemberGroupId(partnerTeamMemberGroupDTO.getId());
				userDTO.setSelectedTeamMembersCount(partnerTeamMemberGroupDTO.getCount());
				userDTO.setSelectedTeamMemberIds(
						teamMemberGroupDao.findSelectedTeamMemberGroupUserMappingIdsByPartnershipId(partnershipId));
				userDTO.setSelectedTeamMemberGroupName(partnerTeamMemberGroupDTO.getTeamMemberGroupName());
			}
		} else {
			setDefaultTeamMemberGroupData(userDTO);
		}
	}

	/*************** XNFR-85 *****************/
	private void setDefaultTeamMemberGroupData(UserDTO userDTO) {
		userDTO.setTeamMemberGroupId(0);
		userDTO.setSelectedTeamMembersCount(0);
		userDTO.setSelectedTeamMemberIds(new HashSet<Integer>());
		userDTO.setSelectedTeamMemberGroupName("");
	}

	public void deleteByPrimaryKey(Integer userListId) {
		userListDAO.deleteByPrimaryKey(userListId);
	}

	@Override
	public Integer shareLeadsListsAvailable(Integer userId, Integer vendorCompanyId) {
		Integer companyId = userService.getCompanyIdByUserId(userId);
		return userListDAO.shareLeadsListsAvailable(companyId, vendorCompanyId);
	}

	@Override
	public XtremandResponse findUsersByUserListId(Pagination pagination) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			response.setData(userListDAO.findUsersByUserListId(pagination));
			return response;
		} catch (UserListException mex) {
			throw new UserListException(mex);
		} catch (Exception e) {
			throw new UserListException(e);
		}
	}

	@Override
	public XtremandResponse findContactAndPartnerLists(Pagination pagination) {
		try {
			XtremandResponse response = new XtremandResponse();
			response.setStatusCode(200);
			if ("workflow".equals(pagination.getType())) {
				List<Integer> selectedPartnerListIds = workflowDao
						.findSelectedPartnerGroupIdsByWorkflowId(pagination.getCampaignId(), true);
				pagination.setFiltertedEmailTempalteIds(selectedPartnerListIds);
			} else {
				List<Integer> userListIdsByCampaignId = userListDAO
						.findSelectedUserListIdsByCampaignIds(pagination.getCampaignId());
				pagination.setFiltertedEmailTempalteIds(userListIdsByCampaignId);
			}
			response.setData(userListDAO.findContactAndPartnerLists(pagination));
			return response;
		} catch (UserListException mex) {
			throw new UserListException(mex);
		} catch (Exception e) {
			throw new UserListException(e);
		}
	}

	@Override
	public Map<String, Object> findAllAndValidUsersCount(List<Integer> userListIds, Integer loggedInUserId) {
		try {
			Integer allUsers = userListDAO.findAllUsersCountByUserListIds(userListIds);
			Integer companyId = userDAO.getCompanyIdByUserId(loggedInUserId);
			Integer validUsers = userListDAO.validContactsCount(userListIds, companyId);
			List<String> userListNames = userListDAO.findUserListNamesByUserListIds(userListIds);
			Map<String, Object> map = new HashMap<>();
			map.put("allUsersCount", allUsers);
			map.put("validUsersCount", validUsers);
			map.put("userListNames", userListNames);
			return map;
		} catch (UserListException mex) {
			throw new UserListException(mex);
		} catch (Exception e) {
			throw new UserListException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> listUserLists(List<Integer> userListIdsList, Pagination pagination) {
		Map<String, Object> map = new HashMap<>();
		Set<UserListDetails> users = new LinkedHashSet<>();
		List<Criteria> criterias = new ArrayList<>();
		Integer totalRecords = 0;
		if (userListIdsList.size() < 10000) {
			List<List<Integer>> chunkedUserListIds = XamplifyUtils.getChunkedList(userListIdsList);
			for (List<Integer> chunkedUserListId : chunkedUserListIds) {
				Criteria criteria = new Criteria("id", OPERATION_NAME.in, chunkedUserListId.toArray());
				criterias.add(criteria);
				Map<String, Object> resultMap = userListDAO.listUserLists(criterias, pagination);
				List<UserListDetails> userListDetailsList = (List<UserListDetails>) resultMap.get("list");
				Set<UserListDetails> userListDetailsSet = new LinkedHashSet<>(userListDetailsList);
				getuserListDetailsSet(userListDetailsSet);
				users.addAll(userListDetailsSet);
				Integer filteredTotalRecords = (Integer) resultMap.get(XamplifyConstants.TOTAL_RECORDS);
				totalRecords += filteredTotalRecords;
			}
			map.put("listOfUserLists", users);
			map.put("totalRecords", totalRecords);
		} else {
			Integer startIndex = ((pagination.getPageIndex() - 1) * pagination.getMaxResults());
			Integer endIndex = ((pagination.getPageIndex() - 1) * pagination.getMaxResults())
					+ pagination.getMaxResults();

			if (endIndex > userListIdsList.size()) {
				endIndex = userListIdsList.size();
			}
			Criteria criteria = new Criteria("id", OPERATION_NAME.in,
					userListIdsList.subList(startIndex, endIndex).toArray());
			criterias.add(criteria);
			List<UserListDetails> userListDetailsList = userListDAO.onlylistUserLists(criterias, pagination);
			Set<UserListDetails> userListDetailsSet = new LinkedHashSet<>(userListDetailsList);
			getuserListDetailsSet(userListDetailsSet);
			users.addAll(userListDetailsSet);
			map.put("listOfUserLists", users);
			map.put("totalRecords", userListIdsList.size());
		}
		return map;
	}

	private void getuserListDetailsSet(Set<UserListDetails> userListDetailsSet) {
		for (UserListDetails userListDetails : userListDetailsSet) {
			userListDetails.setCreatedDate(DateUtils.getUtcString(userListDetails.getCreatedTime()));
			userListDetails.setAssignedLeadsList(false);
			/**** XNFR-98 *************/
			userListDetails.setTeamMemberPartnerList(userListDAO.isTeamMemberPartnerList(userListDetails.getId()));
			UserDTO userDTO = userDAO.getFullNameAndEmailIdAndCompanyNameByUserId(userListDetails.getUploadedUserId());
			String uploadedBy = userDTO.getFullName();
			userListDetails.setUploadedBy(uploadedBy);
		}
	}

	/******** Old Code For listUserLists() *********/
	@SuppressWarnings({ "unused", "unchecked" })
	@Deprecated
	private Map<String, Object> deprecatedUserListCode(List<Integer> userListIdsList, Pagination pagination) {
		List<Criteria> criterias = new ArrayList<>();
		Criteria criteria = new Criteria("id", OPERATION_NAME.in, userListIdsList.toArray());
		criterias.add(criteria);
		if (org.springframework.util.StringUtils.hasText(pagination.getFilterBy())) {
			if ("FORM-LEADS".equals(pagination.getFilterBy())) {
				Criteria formLeadsCriteria = new Criteria("formList", OPERATION_NAME.eq, true);
				criterias.add(formLeadsCriteria);
			} else if ("COMPANY-CONTACTS".equals(pagination.getFilterBy())) {
				Criteria formLeadsCriteria = new Criteria("companyList", OPERATION_NAME.eq, true);
				criterias.add(formLeadsCriteria);
			} else if ("MY-CONTACTS".equals(pagination.getFilterBy())) {
				Criteria myContactsCriteria = new Criteria("formList", OPERATION_NAME.eq, false);
				criterias.add(myContactsCriteria);
				Criteria companyContactsCriteria = new Criteria("companyList", OPERATION_NAME.eq, false);
				criterias.add(companyContactsCriteria);
			}
		}
		Map<String, Object> resultMap = userListDAO.listUserLists(criterias, pagination);
		List<UserListDetails> userListDetailsList = (List<UserListDetails>) resultMap.get("list");
		Set<UserListDetails> userListDetailsSet = new LinkedHashSet<>(userListDetailsList);
		for (UserListDetails userListDetails : userListDetailsSet) {
			userListDetails.setCreatedDate(DateUtils.getUtcString(userListDetails.getCreatedTime()));
			userListDetails.setAssignedLeadsList(false);
			/**** XNFR-98 *************/
			userListDetails.setTeamMemberPartnerList(userListDAO.isTeamMemberPartnerList(userListDetails.getId()));
		}
		resultMap.put("listOfUserLists", userListDetailsSet);
		return resultMap;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> listShareLists(Integer companyId, Pagination pagination) {
		pagination.setCompanyId(companyId);
		Map<String, Object> resultMap = userListDAO.listShareLists(pagination);
		List<UserListDTO> shareLeadsList = (List<UserListDTO>) resultMap.get("listOfUserLists");
		for (UserListDTO userListDTO : shareLeadsList) {
			userListDTO.setCreatedDate(DateUtils.getUtcString(userListDTO.getCreatedTime()));
			userListDTO.setAssignedDate(DateUtils.getUtcString(userListDTO.getSharedDate()));
			userListDTO.setAssignedLeadsList(true);
			userListDTO.setPublicList(true);
			userListDTO.setPartnerUserList(false);
		}
		resultMap.put("listOfUserLists", shareLeadsList);
		return resultMap;
	}

	public void createFormContactList(Form form, String listName) {
		createFormContactList(form, listName, form.getCreatedUserId());
	}

	@Override
	public void createFormContactList(Form form, String listName, Integer listOwnerUserId) {
		saveFormContactList(form, listName, listOwnerUserId);
	}

	@Override
	public UserList getUserListByNameAndCompany(String name, Integer companyId) {
		return userListDAO.getUserListByNameAndCompany(name, companyId);
	}

	@Override
	public UserList getFormContactListForOrgAdmin(Integer formId, Integer companyId) {
		return userListDAO.getFormContactListForOrgAdmin(formId, companyId);
	}

	@Override
	public UserList getFormContactListByLandingPageId(Integer formId, Integer companyId, Integer landingPageId) {
		return userListDAO.getFormContactListByLandingPageId(formId, companyId, landingPageId);
	}

	@Override
	public UserList getFormContactListByCampaignId(Integer formId, Integer campaignId) {
		return userListDAO.getFormContactListByCampaignId(formId, campaignId);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> listSharedLists(User user, Pagination pagination) {
		Integer loginAsUserId = pagination.getLoginAsUserId();
		boolean isLoginAsPartner = XamplifyUtils.isLoginAsPartner(loginAsUserId);
		Integer vendorCompanyId = 0;
		if (isLoginAsPartner) {
			vendorCompanyId = userDAO.getCompanyIdByUserId(loginAsUserId);
			pagination.setVanityUrlFilter(true);
		} else {
			vendorCompanyId = userDAO.getCompanyIdByProfileName(utilDao.getPrmCompanyProfileName());
		}
		pagination.setPartnerCompanyId(user.getCompanyProfile().getId());
		pagination.setVendorCompanyId(vendorCompanyId);
		pagination.setUserId(user.getUserId());
		Map<String, Object> resultMap = userListDAO.listSharedLists(pagination);
		List<UserListDTO> shareLeadsList = (List<UserListDTO>) resultMap.get("listOfUserLists");
		for (UserListDTO userListDTO : shareLeadsList) {
			userListDTO.setCreatedDate(DateUtils.getUtcString(userListDTO.getCreatedTime()));
			userListDTO.setAssignedDate(DateUtils.getUtcString(userListDTO.getSharedDate()));
			userListDTO.setAssignedLeadsList(true);
			userListDTO.setPublicList(true);
		}
		resultMap.put("listOfUserLists", shareLeadsList);
		return resultMap;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse shareLeadsListToPartners(ShareLeadsDTO shareLeadsDTO) {
		XtremandResponse response = new XtremandResponse();
		Integer loggedInUserCompanyId = userDAO.getCompanyIdByUserId(shareLeadsDTO.getUserId());
		Integer userListId = shareLeadsDTO.getUserListId();
		UserList userList = userListDAO.findByPrimaryKey(userListId, new FindLevel[] { FindLevel.SHALLOW });
		String sharedLeadListName = userList.getName();
		response.setAccess(true);
		if (!shareLeadsDTO.getPartnerIds().isEmpty()) {
			Integer partnerId = shareLeadsDTO.getPartnerIds().iterator().next();
			User partner = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, partnerId)),
					new FindLevel[] { FindLevel.COMPANY_PROFILE });
			String partnerStatus = partnershipDao.findPartnerShipStatusByPartnerCompanyIdAndVendorCompanyId(
					partner.getCompanyProfile().getId(), loggedInUserCompanyId);
			boolean isDeactivated = "DEACTIVATED".equalsIgnoreCase(partnerStatus);
			Integer partnershipId = partnershipDao.getPartnershipIdByVendorCompanyIdAndPartnerCompanyId(
					loggedInUserCompanyId, partner.getCompanyProfile().getId());
			Integer partnershipIdByUserListId = userListDAO
					.findOneClickLaunchCampaignPartnershipIdByUserListId(userListId);
			if (isDeactivated) {
				response.setStatusCode(403);
				response.setMessage("The partnership has been deactivated and can no longer be used to share leads.");
			} else if (partnershipId.equals(partnershipIdByUserListId) || partnershipIdByUserListId == null) {
				Integer approvedTeamMembersCount = teamDao
						.getApprovedTeamMembersCount(partner.getCompanyProfile().getId());
				Integer partnerCompanyUsersCount = approvedTeamMembersCount + 1;
				boolean sharedToCompany = false;
				if (shareLeadsDTO.getPartnerIds().size() == partnerCompanyUsersCount) {
					sharedToCompany = true;
				}
				ShareListPartner shareListPartner = utilService.saveShareListPartner(userList, partnershipId,
						sharedToCompany);
				utilService.saveShareListPartnerMapping(shareLeadsDTO.getPartnerIds(), shareListPartner, false);
				asyncComponent.sendShareLeadEmailNotificationToPartner(shareLeadsDTO, sharedLeadListName);
				userList.setCompany(partner.getCompanyProfile());
				response.setStatusCode(200);
				response.setMessage("Your leads list has been shared successfully");
			} else {
				response.setStatusCode(403);
				response.setMessage(
						"List cannot be shared.Because list is associated with another company in One-Click Launch Campaign(s)");
			}
		}
		return response;
	}

	// This method needs to be removed
	public XtremandResponse updateShareListData() {
		XtremandResponse xtremandResponse = new XtremandResponse();
		List<Criteria> criterias = new ArrayList<>();
		Criteria criteria1 = new Criteria("moduleName", OPERATION_NAME.eq, "SHARE LEADS");
		Criteria criteria2 = new Criteria("owner.userId", OPERATION_NAME.isNotNull);
		Criteria criteria3 = new Criteria("company.id", OPERATION_NAME.isNotNull);
		criterias.add(criteria1);
		criterias.add(criteria2);
		criterias.add(criteria3);
		List<UserList> userlists = find(criterias, new FindLevel[] { FindLevel.SHALLOW });
		for (UserList userList : userlists) {
			Integer partnerCompanyId = userList.getCompany().getId();
			Integer vendorCompanyId = userList.getAssignedCompany().getId();
			Integer partnershipId = partnershipDao.getPartnershipIdByVendorCompanyIdAndPartnerCompanyId(vendorCompanyId,
					partnerCompanyId);
			if (partnershipId != null) {
				ShareListPartner shareListPartner = saveShareListPartner(userList, partnershipId,
						userList.getAssignedDate());
				Set<Integer> partnerIds = new HashSet<Integer>();
				partnerIds.add(userList.getOwner().getUserId());
				ShareLeadsDTO shareLeadsDTO = new ShareLeadsDTO();
				shareLeadsDTO.setPartnerIds(partnerIds);
				utilService.saveShareListPartnerMapping(shareLeadsDTO.getPartnerIds(), shareListPartner, false);
				userListDAO.updateUserList(userList.getId());
			}
		}
		xtremandResponse.setStatusCode(200);
		xtremandResponse.setMessage("success");
		return xtremandResponse;
	}

	// This method needs to be removed
	public ShareListPartner saveShareListPartner(UserList userList, Integer partnershipId, Date assignedDate) {
		ShareListPartner shareListPartner = new ShareListPartner();
		shareListPartner.setUserList(userList);
		Partnership partnership = partnershipDao.getPartnershipById(partnershipId);
		shareListPartner.setPartnership(partnership);
		shareListPartner.setCreatedTime(assignedDate);
		genericDAO.save(shareListPartner);
		return shareListPartner;
	}

	@SuppressWarnings("unchecked")
	@Override
	public XtremandResponse listSharedDetails(Integer userListId, Pagination pagination) {
		XtremandResponse xtremandResponse = new XtremandResponse();
		Map<String, Object> map = userListDAO.getListSharedCompanyDetails(userListId, pagination);
		List<SharedDetailsDTO> list = (List<SharedDetailsDTO>) map.get("list");
		for (SharedDetailsDTO sharedDetailsDTO : list) {
			sharedDetailsDTO.setCreatedTime(DateUtils.getUtcString(sharedDetailsDTO.getCreatedDate()));
			sharedDetailsDTO.setSharedTime(DateUtils.getUtcString(sharedDetailsDTO.getSharedDate()));
		}
		map.put("list", list);
		xtremandResponse.setStatusCode(200);
		xtremandResponse.setData(map);
		return xtremandResponse;
	}

	@Override
	public void shareLeadsListToNewlyAddedTeamMembers(Integer teamMemberId, Integer teamMemberCompanyId) {
		CompanyProfile companyProfile = genericDAO.get(CompanyProfile.class, teamMemberCompanyId);
		List<Partnership> partnerships = partnershipDao.getApprovedPartnershipsByPartnerCompany(companyProfile);
		for (Partnership partnership : partnerships) {
			Integer partnershipId = partnership.getId();
			List<ShareListPartner> shareListPartnerList = userListDAO.getShareListPartner(partnershipId);
			Set<Integer> partnerIds = new HashSet<>();
			partnerIds.add(teamMemberId);
			for (ShareListPartner shareListPartner : shareListPartnerList) {
				utilService.saveShareListPartnerMapping(partnerIds, shareListPartner, false);
			}
		}
	}

	@Override
	public List<Integer> getUnsubscribedUsers(Integer customerCompanyId) {
		return userListDAO.getUnsubscribedUsers(customerCompanyId);
	}

	@Override
	public XtremandResponse saveAsNewUserList(Integer userId, UserListDTO userListDTO) {
		XtremandResponse xtremandResponse = new XtremandResponse();
		userListDTO.setSourceUserListId(userListDTO.getId());
		userListDTO.setId(null);
		userListDTO.setCopyList(true);
		if (userListDTO.getModuleName().equalsIgnoreCase("CONTACTS")) {
			xtremandResponse = saveContactList(null, userId, userListDTO, xtremandResponse);
		} else if (userListDTO.getModuleName().equalsIgnoreCase("PARTNERS")) {
			xtremandResponse = partnershipService.saveAsPartnerList(null, userId, userListDTO, xtremandResponse, null);
			xtremandResponse.setAccess(true);
		} else if (userListDTO.getModuleName().equalsIgnoreCase("SHARE LEADS")) {
			userListDTO.setAssignedLeadsList(utilService.hasShareLeadsModuleAccess(userId));
			boolean access = getAccess(userListDTO.isPartnerUserList(), userId, userListDTO.isAssignedLeadsList());
			if (access) {
				xtremandResponse = saveContactList(null, userId, userListDTO, xtremandResponse);
				xtremandResponse.setAccess(true);
			} else {
				xtremandResponse.setAccess(false);
			}
		}
		return xtremandResponse;
	}

	public XtremandResponse validateContactList(Set<UserDTO> users, String listName, User loggedInUser,
			XtremandResponse xtremandResponse) {
		Criteria criteria1 = new Criteria("company.id", OPERATION_NAME.eq, loggedInUser.getCompanyProfile().getId());
		Criteria criteria2 = new Criteria("moduleName", OPERATION_NAME.eq, "CONTACTS");
		List<Criteria> criterias = new ArrayList<>();
		criterias.add(criteria1);
		criterias.add(criteria2);
		List<Criterion> criterions = userListDAO.generateCriteria(criterias);
		List<String> names = userListDAO.listUserlistNames(criterions);
		xtremandResponse = userListValidator.validateList(users, listName, names, xtremandResponse);
		return xtremandResponse;
	}

	@Override
	public List<String> getPartnerListNamesByCompany(Integer companyId) {
		List<Criteria> criterias = new ArrayList<Criteria>();
		Criteria criteria1 = new Criteria("company.id", OPERATION_NAME.eq, companyId);
		Criteria criteria2 = new Criteria("moduleName", OPERATION_NAME.eq, "PARTNERS");
		criterias.add(criteria1);
		criterias.add(criteria2);
		List<Criterion> criterions = userListDAO.generateCriteria(criterias);
		return userListDAO.listUserlistNames(criterions);
	}

	@Override
	public HttpServletResponse downloadPartnerListCsv(HttpServletResponse httpServletResponse, Integer userId) {
		Integer companyId = userDAO.getCompanyIdByUserId(userId);
		ModuleCustomDTO moduleCustomNameDTO = moduleDao.findPartnerModuleByCompanyId(companyId);
		return csvUtilService.downloadPartnerListCsv("Upload-" + moduleCustomNameDTO.getCustomName(),
				httpServletResponse);
	}

	/************* XNFR-98 ****************/
	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void deleteTeamMemberPartnerList(Integer userListId, Integer userId) {
		XtremandResponse xtremandResponse = new XtremandResponse();
		Criteria criteria = new Criteria("id", OPERATION_NAME.eq, userListId);
		List<Criteria> criterias = Arrays.asList(criteria);
		Collection<UserList> userLists = userListDAO.find(criterias,
				new FindLevel[] { FindLevel.CAMPAIGNS, FindLevel.COMPANY_PROFILE });
		UserList userList = userLists.iterator().next();
		userList.setDeleteTeamMemberPartnerList(true);
		xtremandResponse = removePartnerList(true, userList, xtremandResponse);
		damDao.deleteFromDamPartnerGroupMappingAndDamPartnerByUserListId(userListId, userId);
	}

	/************* XNFR-211 ****************/
	public Map<String, Object> updateContactList_new(UserList userList, Set<UserDTO> users, Integer customerId) {

		if (userList != null) {
			if (userList.getForm() != null) {
				UserDTO userDTO = users.iterator().next();
				boolean isUserExists = userListDAO.isUserExists(userList.getId(), userDTO.getEmailId());
				if (isUserExists) {
					return getUpdateContactListResult();
				}
			}
			userList.setUploadInProgress(true);
			userList.setEmailValidationInd(false);
			userList.initialiseCommonFields(false, customerId);
			User loggedInUser = userService.loadUser(
					Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, customerId)),
					new FindLevel[] { FindLevel.COMPANY_PROFILE });
			List<Integer> totalUnsubscribedUserIds = userListDAO.getUnsubscribedUsers(userList.getCompany().getId());
			sendContactListMail(loggedInUser, userList, totalUnsubscribedUserIds);
			asyncComponent.createUsersInUserList(users, userList.getId(), loggedInUser.getUserId(), false, null);
		}

		Map<String, Object> resultMap = getUpdateContactListResult();
		if (userList.getModuleName().equalsIgnoreCase("SHARE LEADS")) {
			resultMap.put("message", "LEADS LIST UPDATED SUCCESSFULLY");
		} else {
			resultMap.put("message", "CONTACT LIST UPDATED SUCCESSFULLY");
		}
		return resultMap;
	}

	/************ XNFR-278 ********/
	@Override
	public XtremandResponse findPartnerGroupsForMerging(Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		Map<String, Object> map = userListDAO.findGroupsForMerging(pagination);
		response.setStatusCode(200);
		response.setData(map);
		response.setAccess(true);
		return response;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse copyGroupUsers(CopyGroupUsersDTO copyGroupUsersDTO) {
		XtremandResponse response = new XtremandResponse();
		String responseMessage = "";
		Map<String, Object> map = userListDAO.copyUsersToUserGroups(copyGroupUsersDTO);
		Integer statusCode = (Integer) map.get("statusCode");
		response.setStatusCode(statusCode);
		if (statusCode.equals(200)) {
			responseMessage = "Added Successfully.";
		} else {
			responseMessage = generateUserAlreadyExistsMessage(copyGroupUsersDTO);
		}
		response.setMessage(responseMessage);
		response.setMap(map);
		return response;
	}

	private String generateUserAlreadyExistsMessage(CopyGroupUsersDTO copyGroupUsersDTO) {
		String responseMessage;
		if (copyGroupUsersDTO.getModuleName().equalsIgnoreCase("partners")) {
			responseMessage = "Selected user(s) already part of the selected group(s)";
		} else {
			responseMessage = "Selected user(s) already part of the selected list(s)";
		}
		return responseMessage;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public Map<String, Object> unsubscribeOrResubscribeUser(Integer loggedInUserId,
			UnsubscribeUserDTO unsubscribeUserDTO) {
		CompanyProfile companyProfile = userService.getCompanyProfileByUser(loggedInUserId);
		unsubscribeUserDTO.setCompanyId(companyProfile.getId());
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("message", " has been successfully " + unsubscribeUserDTO.getType()
				+ " for receiving the emails from the company: " + companyProfile.getCompanyName());
		return resultMap;
	}

	@Override
	public XtremandResponse validatePartners(Integer loggedInUserId, Integer userListId, List<User> partners) {
		XtremandResponse response = validatePartners(partners, userListId);
		if (response.getStatusCode() == 200) {
			response = validatePartnerCompany(loggedInUserId, partners, response, false, true);
		}
		return response;
	}

	@Override
	public CompanyProfileDTO getPartnerDetails(Integer partnerId) {
		return userListDAO.getPartnerDetails(partnerId);
	}

	@Override
	public void downloadTotalContactsOfAllPartnersByVendorCompanyId(Integer vendorCompanyId,
			HttpServletResponse httpServletResponse, String fileName) {
		List<Integer> partnerCompanyIds = partnershipDao.findPartnerCompanyIdsByVendorCompanyId(vendorCompanyId);
		List<ContactsCSVDTO> contacts = userListDAO.findAllContactsOfAllPartnersByVendorCompanyId(vendorCompanyId,
				partnerCompanyIds);
		LinkedHashMap<String, String> fieldHeaderMapping = new LinkedHashMap<>();
		fieldHeaderMapping.put("Partner Company Name", "getPartnerCompanyName");
		fieldHeaderMapping.put("FIRSTNAME", "getFirstName");
		fieldHeaderMapping.put("LASTNAME", "getLastName");
		fieldHeaderMapping.put("COMPANY", "getCompany");
		fieldHeaderMapping.put("JOBTITLE", "getJobTitle");
		fieldHeaderMapping.put("EMAILID", "getEmailId");
		fieldHeaderMapping.put("ADDRESS", "getAddress");
		fieldHeaderMapping.put("CITY", "getCity");
		fieldHeaderMapping.put("STATE", "getState");
		fieldHeaderMapping.put("ZIP CODE", "getZipCode");
		fieldHeaderMapping.put("COUNTRY", "getCountry");
		fieldHeaderMapping.put("MOBILE NUMBER", "getMobileNumber");
		csvUtilService.downLoadToCSV(httpServletResponse, fileName, fieldHeaderMapping, contacts);

	}

	@Override
	public void downloadTotalContactsCountOfAllPartners(Integer companyId, HttpServletResponse httpServletResponse,
			String fileName) {
		List<Integer> partnerCompanyIds = partnershipDao.findPartnerCompanyIdsByVendorCompanyId(companyId);
		LinkedHashMap<String, String> fieldHeaderMapping = new LinkedHashMap<>();
		fieldHeaderMapping.put("Partner Company Name", "getPartnerCompanyName");
		fieldHeaderMapping.put("Contacts Count", "getContactsCountInString");
		List<ContactsCSVDTO> contacts = userListDAO.findAllContactsCountOfAllPartnersByVendorCompanyId(companyId,
				partnerCompanyIds);
		csvUtilService.downLoadToCSV(httpServletResponse, fileName, fieldHeaderMapping, contacts);
	}

	@Override
	public XtremandResponse validatePartnerCompany(Integer loggedInUserId, User partner, Integer partnerCompanyId,
			boolean isCompanyProfileSubmit, boolean isAdd) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		CompanyProfile companyProfile = genericDAO.get(CompanyProfile.class, partnerCompanyId);
		if ((companyProfile != null) && ((companyProfile.getCompanyName().trim().toLowerCase())
				.equalsIgnoreCase(partner.getContactCompany().trim().toLowerCase()))) {
			response.setStatusCode(200);
		} else {
			List<User> partners = new ArrayList<User>();
			partners.add(partner);
			response = validatePartnerCompany(loggedInUserId, partners, response, isCompanyProfileSubmit, isAdd);
		}
		return response;
	}

	public boolean companyNameExists(String companyName) {
		return companyDao.companyNameExists(companyName.trim());
	}

	@Override
	public XtremandResponse excludedUserMakeAsValid(Integer customerId, UserDTO userDTO) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = userDAO.getCompanyIdByUserId(customerId);

		if (!userListDAO.checkDomineExcluded(companyId, userDTO.getEmailId())) {
			userListDAO.deleteUserFromExcludedUser(companyId, userDTO.getId());
			response.setStatusCode(200);
			response.setMessage(userDTO.getEmailId() + " has been removed from exclused list");
		} else {
			response.setStatusCode(400);
			response.setMessage("Email cannot be removed from excluded user as domain name is excluded");
		}
		response.setAccess(true);
		return response;
	}

	/************* XBI-1149 **************/
	@Override
	public void updateFormContactList(Form form) {
		userListDAO.updateFormContactListName(form.getId(), form.getFormName());
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse downloadUserList(Integer userId, String moduleName) {
		XtremandResponse response = new XtremandResponse();
		DownloadItem listName = null;
		if (moduleName.equalsIgnoreCase("partners")) {
			listName = DownloadItem.PARTNER_LIST;
		} else if (moduleName.equalsIgnoreCase("contacts")) {
			listName = DownloadItem.CONTACT_LIST;
		} else if (moduleName.equalsIgnoreCase("leads")) {
			listName = DownloadItem.SHARE_LEADS;
		} else if (moduleName.equalsIgnoreCase("sharedleads")) {
			listName = DownloadItem.SHARED_LEADS;
		} else {
			listName = DownloadItem.MASTER_PARTNER_LIST;
		}
		DownloadDataInfo downloadDataInfo = utilDao.getDownloadDataInfo(userId, listName);
		if (downloadDataInfo == null || !downloadDataInfo.isDownloadInProgress()) {
			downloadDataInfo = utilDao.updateDownloadDataInfo(userId, downloadDataInfo, listName);
			if (moduleName.equalsIgnoreCase("contacts") || moduleName.equalsIgnoreCase("sharedleads")
					|| moduleName.equalsIgnoreCase("leads")) {
				response.setMessage(
						"We are processing your list(s) reports. We will send it over an email when the report is ready");
			} else {
				response.setMessage(
						"We are processing your group(s) reports. We will send it over an email when the report is ready");
			}

			response.setStatusCode(200);

		} else {
			response.setMessage("Please wait until the previous request is processed...!");
			response.setStatusCode(401);
		}
		response.setData(downloadDataInfo.getId());
		return response;
	}

	public XtremandResponse getUserListByUserListId(Integer userListId) {
		XtremandResponse response = new XtremandResponse();
		response.setStatusCode(200);
		response.setData(userListDAO.getUserListByUserListId(userListId));
		return response;
	}

	public XtremandResponse validatePartnerCompany(Integer loggedInUserId, List<User> inputPartners,
			XtremandResponse response, boolean isCompanyProfileSubmit, boolean isAdd) {
		Integer vendorCompanyId = userService.getCompanyIdByUserId(loggedInUserId);
		response = utilService.validatePartnerCompany(inputPartners, response, isCompanyProfileSubmit, isAdd,
				vendorCompanyId);
		return response;
	}

	@Override
	public void findUsersAndProcessTheList(Integer id) {
		String methodStartedDebugMessage = "Entered Into findUsersAndProcessTheList(" + id + ")";
		logger.debug(methodStartedDebugMessage);
		String queryString = "select cast(count(distinct xup.user_id) as int) from xt_user_list xul,xt_user_profile xup,xt_user_userlist xuu\n"
				+ "where xul.user_list_id  = xuu.user_list_id and xuu.user_id  = xup.user_id\n"
				+ "and xul.user_list_id  = :userListId and xup.email_validation_ind  = false\n";
		HibernateSQLQueryResultRequestDTO findInvalidEmailUserIds = new HibernateSQLQueryResultRequestDTO();
		findInvalidEmailUserIds.setQueryString(queryString);
		findInvalidEmailUserIds.getQueryParameterDTOs().add(new QueryParameterDTO(XamplifyConstants.USER_LIST_ID, id));
		Integer count = (Integer) hibernateSQLQueryResultUtilDao.getUniqueResult(findInvalidEmailUserIds);
		if (XamplifyUtils.isValidInteger(count)) {
			String processEmailIdsDebugMessage = "Invalid Emails (" + count + ") Found For User List Id :  " + id
					+ ".ZeroBounce API is about to validate emails";
			logger.debug(processEmailIdsDebugMessage);
			updateValidationInProgressProperty(id);
		} else {
			HibernateSQLQueryResultRequestDTO updateUserListQueryDTO = new HibernateSQLQueryResultRequestDTO();
			String updateUserListQueryString = "update xt_user_list set email_validation_ind = true,validation_in_progress = false,upload_in_progress = false where user_list_id = :userListId";
			updateUserListQueryDTO.setQueryString(updateUserListQueryString);
			updateUserListQueryDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(XamplifyConstants.USER_LIST_ID, id));
			hibernateSQLQueryResultUtilDao.update(updateUserListQueryDTO);
			String validEmailIdsDebugMessage = "All Emails Are Valid For User List Id : " + id
					+ ".No processing will be done further.";
			logger.debug(validEmailIdsDebugMessage);
		}
	}

	private void updateValidationInProgressProperty(Integer id) {
		HibernateSQLQueryResultRequestDTO updateValidationInProgressQueryDTO = new HibernateSQLQueryResultRequestDTO();
		String updateValidationInProgressQueryString = "update xt_user_list set validation_in_progress = true where user_list_id = :userListId";
		updateValidationInProgressQueryDTO.setQueryString(updateValidationInProgressQueryString);
		updateValidationInProgressQueryDTO.getQueryParameterDTOs().add(new QueryParameterDTO("userListId", id));
		hibernateSQLQueryResultUtilDao.update(updateValidationInProgressQueryDTO);
	}

	/**** Added By Sravan To Fix Production Issue On 24/04/2024 *****/
	@Override
	public Integer findAllUserListUsersCount(Integer companyId) {
		if (XamplifyUtils.isValidInteger(companyId)) {
			String sqlString = "select  cast(count(distinct up.user_id) as integer) as all\n"
					+ "from xt_user_list u, xt_user_userlist uul, xt_user_profile up\n"
					+ "where u.user_list_id =uul.user_list_id and uul.user_id=up.user_id\n"
					+ "and up.email_validation_ind=true\n"
					+ "and u.company_id = :companyId and u.module_name  = 'CONTACTS' \n";
			HibernateSQLQueryResultRequestDTO findAllUserListUsersCountQueryDTO = new HibernateSQLQueryResultRequestDTO();
			findAllUserListUsersCountQueryDTO.setQueryString(sqlString);
			findAllUserListUsersCountQueryDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
			return (Integer) hibernateSQLQueryResultUtilDao.getUniqueResult(findAllUserListUsersCountQueryDTO);
		} else {
			return 0;
		}
	}

	@Override
	public Integer findAllActiveUserListUsersCount(Integer companyId) {
		if (XamplifyUtils.isValidInteger(companyId)) {
			List<Integer> allActiveUserIds = findAllActiveUserIds(companyId);
			if (XamplifyUtils.isNotEmptyList(allActiveUserIds)) {
				List<Integer> unsubscribedUserIds = findUnsubscribeUserIds(companyId);
				allActiveUserIds.removeAll(unsubscribedUserIds);
			}
			return allActiveUserIds.size();
		} else {
			return 0;
		}

	}

	@SuppressWarnings("unchecked")
	private List<Integer> findUnsubscribeUserIds(Integer companyId) {
		String sqlString = "select distinct xuu.user_id from xt_unsubscribed_user xuu  where xuu.customer_company_id=:companyId\n";
		HibernateSQLQueryResultRequestDTO unsubscribedUserIdsQueryDTO = new HibernateSQLQueryResultRequestDTO();
		unsubscribedUserIdsQueryDTO.setQueryString(sqlString);
		unsubscribedUserIdsQueryDTO.getQueryParameterDTOs().add(new QueryParameterDTO("companyId", companyId));
		return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(unsubscribedUserIdsQueryDTO);
	}

	@SuppressWarnings("unchecked")
	private List<Integer> findAllActiveUserIds(Integer companyId) {
		String sqlString = "select distinct up.user_id from xt_user_list u, xt_user_userlist uul, xt_user_profile up where u.user_list_id =uul.user_list_id and uul.user_id=up.user_id\n"
				+ "and  u.company_id = :companyId and up.is_email_valid and up.email_validation_ind and up.status = 'APPROVE' and u.module_name = 'CONTACTS' \n";
		HibernateSQLQueryResultRequestDTO findAllUserListUserIdsQueryDTO = new HibernateSQLQueryResultRequestDTO();
		findAllUserListUserIdsQueryDTO.setQueryString(sqlString);
		findAllUserListUserIdsQueryDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
		return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(findAllUserListUserIdsQueryDTO);
	}

	@Override
	public Integer findAllInActiveUserListUsersCount(Integer companyId) {
		if (XamplifyUtils.isValidInteger(companyId)) {
			List<Integer> allInActiveUserIds = findAllInActiveUserIds(companyId);
			if (XamplifyUtils.isNotEmptyList(allInActiveUserIds)) {
				List<Integer> unsubscribedUserIds = findUnsubscribeUserIds(companyId);
				allInActiveUserIds.removeAll(unsubscribedUserIds);
			}
			return allInActiveUserIds.size();
		} else {
			return 0;
		}
	}

	@SuppressWarnings("unchecked")
	private List<Integer> findAllInActiveUserIds(Integer companyId) {
		String sqlString = "select distinct up.user_id from xt_user_list u, xt_user_userlist uul, xt_user_profile up where u.user_list_id =uul.user_list_id and uul.user_id=up.user_id\n"
				+ "and u.company_id = :companyId and up.is_email_valid  and up.email_validation_ind "
				+ "and up.status = 'UnApproved' and u.module_name = 'CONTACTS' \n";
		HibernateSQLQueryResultRequestDTO findAllUserListUserIdsQueryDTO = new HibernateSQLQueryResultRequestDTO();
		findAllUserListUserIdsQueryDTO.setQueryString(sqlString);
		findAllUserListUserIdsQueryDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
		return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(findAllUserListUserIdsQueryDTO);
	}

	@Override
	public Integer findAllInvalidUserListUsersCount(Integer companyId) {
		if (XamplifyUtils.isValidInteger(companyId)) {
			List<Integer> allInvalidUserIds = findAllInvalidUserIds(companyId);
			if (XamplifyUtils.isNotEmptyList(allInvalidUserIds)) {
				List<Integer> unsubscribedUserIds = findUnsubscribeUserIds(companyId);
				allInvalidUserIds.removeAll(unsubscribedUserIds);
			}
			return allInvalidUserIds.size();
		} else {
			return 0;
		}
	}

	@SuppressWarnings("unchecked")
	private List<Integer> findAllInvalidUserIds(Integer companyId) {
		String sqlString = "select distinct up.user_id from xt_user_list u, xt_user_userlist uul, xt_user_profile up where u.user_list_id =uul.user_list_id and uul.user_id=up.user_id\n"
				+ "and u.company_id = :companyId and up.is_email_valid = false  and up.email_validation_ind "
				+ "and  u.module_name = 'CONTACTS' \n";
		HibernateSQLQueryResultRequestDTO findAllUserListUserIdsQueryDTO = new HibernateSQLQueryResultRequestDTO();
		findAllUserListUserIdsQueryDTO.setQueryString(sqlString);
		findAllUserListUserIdsQueryDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
		return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(findAllUserListUserIdsQueryDTO);
	}

	@Override
	public Integer findAllUnsubscribeUserListUsersCount(Integer companyId) {
		if (XamplifyUtils.isValidInteger(companyId)) {
			List<Integer> allUnsubscribeUserIds = findUnsubscribeUserIds(companyId);
			return allUnsubscribeUserIds.size();
		} else {
			return 0;
		}
	}

	@Override
	public Integer findAllExcludedUserListUsersCount(Integer companyId) {
		return null;
	}

	public List<Integer> getUserListIds(User user, UserListDTO userListDTO) {
		Integer userId = user.getUserId();
		Boolean isPartnerUserList = userListDTO.getIsPartnerUserList();
		Integer loggedInUserCompanyId = user.getCompanyProfile().getId();
		List<Integer> roleIds = user.getRoles().stream().map(Role::getRoleId).collect(Collectors.toList());
		boolean deletedPartner = false;
		if (roleIds.size() == 1 && roleIds.indexOf(Role.USER_ROLE.getRoleId()) > -1) {
			deletedPartner = true;
		}

		List<Integer> userListIds = new ArrayList<>();
		List<Criteria> criterias = new ArrayList<Criteria>();
		if (deletedPartner) {
			/************ XNFR-117 **************/
			userListIds = findContactsListsForDeletedPartners(userId, userListIds, criterias, user);
		} else if (!deletedPartner) {
			if (userListDTO.getId() != null) {
				userListIds.add(userListDTO.getId());
			} else {
				if (userListDTO.isSharedLeads()) {
					Integer loginAsUserId = userListDTO.getLoginAsUserId();
					boolean isLoginAsPartner = XamplifyUtils.isLoginAsPartner(loginAsUserId);
					Integer vendorCompanyId = null;
					Integer partnerCompanyId = userDAO.getCompanyIdByUserId(userId);
					if (isLoginAsPartner) {
						vendorCompanyId = userDAO.getCompanyIdByUserId(loginAsUserId);
					} else if (userListDTO.isVanityUrlFilter()) {
						vendorCompanyId = userDAO.getCompanyIdByProfileName(userListDTO.getVendorCompanyProfileName());
					}
					userListIds = userListDAO.sharedListUserIds(userId, partnerCompanyId, vendorCompanyId,
							userListDTO.isVanityUrlFilter(), isLoginAsPartner);
				} else if (userListDTO.isAssignedLeadsList()) {
					Criteria criteria1 = new Criteria("assignedCompany.id", OPERATION_NAME.eq, loggedInUserCompanyId);
					Criteria criteria2 = new Criteria("moduleName", OPERATION_NAME.eq, "SHARE LEADS");
					Criteria criteria3 = new Criteria("emailValidationInd", OPERATION_NAME.eq, true);
					criterias.add(criteria1);
					criterias.add(criteria2);
					criterias.add(criteria3);
					List<Criterion> criterions = userListDAO.generateCriteria(criterias);
					userListIds = userListDAO.getUserListIds(criterions);
				} else if (Boolean.FALSE.equals(isPartnerUserList)) {
					List<Criterion> criterions = userListDAO.getContactsCriterias(user, null);
					criterions.add(Restrictions.eq("emailValidationInd", true));
					userListIds = userListDAO.getUserListIds(criterions);
				} else {
					Criteria criteria1 = new Criteria("company.id", OPERATION_NAME.eq, loggedInUserCompanyId);
					Criteria criteria2 = new Criteria("emailValidationInd", OPERATION_NAME.eq, true);
					Criteria criteria3 = new Criteria("isPartnerUserList", OPERATION_NAME.eq, true);
					Criteria criteria4 = new Criteria("moduleName", OPERATION_NAME.eq, "PARTNERS");
					criterias.add(criteria1);
					criterias.add(criteria2);
					criterias.add(criteria3);
					criterias.add(criteria4);
					List<Criterion> criterions = userListDAO.generateCriteria(criterias);
					userListIds = userListDAO.getUserListIds(criterions);
				}
			}
		}
		return userListIds;
	}

	@Override
	public XtremandResponse deleteAllContacts(Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		return response;
	}

	@Override
	public HttpServletResponse downloadContactListCsv(Integer userId, HttpServletResponse httpServletResponse) {
		try {
			List<FlexiFieldResponseDTO> flexiFields = flexiFieldDao.findAll(userId);
			return csvUtilService.downloadContactListCsv("UPLOAD_USER_LIST_EMPTY", flexiFields, httpServletResponse);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return httpServletResponse;
	}

	/*** XNFR-553 ***/
	@Override
	public XtremandResponse findUserByUserIdAndUserListId(Integer userId, Integer userListId, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		if (XamplifyUtils.isValidInteger(userId) && XamplifyUtils.isValidInteger(userListId)) {
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			String userDetailsQueryString = "select up.user_id as \"id\",up.email_id as \"emailId\", uul.firstname as\"firstName\",uul.lastname as \"lastName\", "
					+ "c.name as \"contactCompany\", uul.job_title as \"jobTitle\", uul.mobile_number as \"mobileNumber\", uul.description as \"description\", "
					+ "uul.address as \"address\", uul.city as \"city\", uul.country as \"country\", uul.state as \"state\", uul.zip as \"zipCode\", "
					+ "uul.vertical as \"vertical\", uul.region as \"region\", uul.partner_type as \"partnerType\", uul.category as \"category\", "
					+ "uul.account_name as \"accountName\", uul.account_sub_type as \"accountSubType\", uul.territory as \"territory\", uul.id as \"userUserListId\", "
					+ "uul.company_domain as \"companyDomain\", uul.account_owner as \"accountOwner\", uul.website as \"website\", up.is_email_valid as \"validEmail\", "
					+ "up.email_category as \"emailCategory\", uul.user_list_id as \"userListId\", up.password as \"password\", up.status as \"userStatus\", "
					+ "uul.country_code \"countryCode\", uul.contact_company_id as \"contactCompanyId\", xcs.stage_name as \"contactStatus\" \n FROM xt_user_userlist uul JOIN xt_user_profile up "
					+ "ON uul.user_id = up.user_id \n left join xt_company c on c.id = uul.contact_company_id \n left join xt_contact_status xcs "
					+ "on xcs.id = uul.contact_status_id \n WHERE uul.user_list_id = :userListId and up.user_id = :userId";
			hibernateSQLQueryResultRequestDTO.setQueryString(userDetailsQueryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("userListId", userListId));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("userId", userId));
			UserDTO userDTO = (UserDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO,
					UserDTO.class);
			userDTO.setLegalBasis(
					userListDAO.listLegalBasisByContactListIdAndUserId(userDTO.getUserListId(), userDTO.getId()));
			userDTO.setFlexiFields(findAndAddFlexiFieldsData(userId, userListId, loggedInUserId));

			response.setData(userDTO);
			XamplifyUtils.addSuccessStatus(response);
		}
		return response;
	}

	private List<FlexiFieldRequestDTO> findAndAddFlexiFieldsData(Integer selectedUserId, Integer userListId,
			Integer loggedInUserId) {
		List<FlexiFieldResponseDTO> flexiFieldNames = flexiFieldDao.findAll(loggedInUserId);
		List<FlexiFieldRequestDTO> flexiFieldValues = flexiFieldDao
				.findFlexiFieldsBySelectedUserIdAndUserListId(userListId, selectedUserId);
		Map<String, String> fieldValueMap = flexiFieldValues.stream()
				.collect(Collectors.toMap(FlexiFieldRequestDTO::getFieldName, FlexiFieldRequestDTO::getFieldValue,
						(existing, replacement) -> existing));

		List<FlexiFieldRequestDTO> finalFields = new ArrayList<>();
		for (FlexiFieldResponseDTO flexiField : flexiFieldNames) {
			FlexiFieldRequestDTO fieldRequestDTO = new FlexiFieldRequestDTO();
			String fieldName = flexiField.getFieldName();
			fieldRequestDTO.setId(flexiField.getId());
			fieldRequestDTO.setFieldName(fieldName);
			fieldRequestDTO.setFieldValue(fieldValueMap.getOrDefault(fieldName, null));
			finalFields.add(fieldRequestDTO);
		}
		return finalFields;
	}

	@Override
	public XtremandResponse findUserListDetails(Integer userListId, boolean isFromCompanyModule) {
		XtremandResponse response = new XtremandResponse();
		if (XamplifyUtils.isValidInteger(userListId)) {
			String additionalFieldsQueryString = "";
			String joinQueryString = "";
			if (isFromCompanyModule) {
				additionalFieldsQueryString = ", c.name as \"companyName\", ul.is_public as \"publicList\" ";
				joinQueryString = " join xt_company c on ul.associated_company_id = c.id";
			} else {
				Integer associatedCompanyId = checkUserListHasAssociateCompanyId(userListId);
				additionalFieldsQueryString = ", ul.is_partner_userlist as \"isPartnerUserList\", case when ul.form_id is not null then true else false end as \"isFormList\", "
						+ "case when ul.associated_company_id is not null then true else false end as \"companyList\", "
						+ "case when ul.contact_list_type is not null then true else false end as \"isDefaultContactList\", "
						+ "ul.is_master_contactlist_sync as \"isMasterContactListSync\"";
				if (XamplifyUtils.isValidInteger(associatedCompanyId)) {
					joinQueryString = " join xt_company c on ul.associated_company_id = c.id";
					additionalFieldsQueryString += ", c.name as \"companyName\"";
				}
			}
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			String userListDetailsQueryString = "select ul.user_list_name as \"name\", ul.is_synchronized_list as \"synchronisedList\", "
					+ "ul.is_default_partnerlist as \"isDefaultPartnerList\", ul.is_team_member_partner_list as \"teamMemberPartnerList\", "
					+ "ul.associated_company_id as \"associatedCompanyId\", ul.customer_id as \"uploadedUserId\""
					+ additionalFieldsQueryString + " from xt_user_list ul" + joinQueryString
					+ " where ul.user_list_id = :userListId";
			hibernateSQLQueryResultRequestDTO.setQueryString(userListDetailsQueryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(XamplifyConstants.USER_LIST_ID, userListId));
			UserListDTO userListDTO = (UserListDTO) hibernateSQLQueryResultUtilDao
					.getDto(hibernateSQLQueryResultRequestDTO, UserListDTO.class);
			response.setData(userListDTO);
			XamplifyUtils.addSuccessStatus(response);
		}
		return response;
	}

	private Integer checkUserListHasAssociateCompanyId(Integer userListId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlQuery = "select associated_company_id from xt_user_list where user_list_id = :userListId";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlQuery);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.USER_LIST_ID, userListId));
		return (Integer) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

	/*** XNFR-553 ***/

	@Override
	public UserList createFormContactListAndGetContactListId(Form form, String listName) {
		return saveFormContactList(form, listName, form.getCreatedUserId());
	}

	private UserList saveFormContactList(Form form, String listName, Integer listOwnerUserId) {
		User listOwner = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, listOwnerUserId)),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		UserList userList = new UserList();
		userList.setName(listName);
		userList.setOwner(listOwner);
		userList.setUploadedDate(new Date());
		userList.setSocialNetwork(UserList.getSocialNetworkEnum("MANUAL"));
		userList.setContactType(UserList.getContactTypeEnum("CONTACT"));
		userList.setSynchronisedList(false);
		userList.setPartnerUserList(false);
		userList.setCompany(listOwner.getCompanyProfile());
		userList.setPublicList(true);
		userList.setForm(form);
		userList.setModuleName("CONTACTS");
		userList.setEmailValidationInd(true);
		userList.initialiseCommonFields(true, form.getCreatedUserId());
		genericDAO.save(userList);
		return userList;
	}

	@Override
	public Integer getDefaultPartnerListIdByCompanyId(Integer companyId) {
		return userListDAO.getDefaultPartnerListIdByCompanyId(companyId);
	}

	@Override
	public XtremandResponse fetchContactsAndCountByUserListId(Integer userListId) {
		XtremandResponse response = new XtremandResponse();
		if (XamplifyUtils.isValidInteger(userListId)) {
			Map<String, Object> map = fetchContactsAndCount(userListId);
			response.setData(map);
			XamplifyUtils.addSuccessStatus(response);
		} else {
			XamplifyUtils.addErorMessageWithStatusCode(response, "Failed to fetch the contacts", 401);
		}
		return response;
	}

	private Map<String, Object> fetchContactsAndCount(Integer userListId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "select distinct cast((up.user_id) as integer) as \"contactId\", concat((case when uul.firstname is not null and uul.firstname not like ' ' then concat(uul.firstname, ' ') else '' end), uul.lastname) as \"fullName\", up.email_id as \"emailId\", uul.mobile_number as \"mobileNumber\" "
				+ FROM_XT_USER_LIST_U_LEFT_JOIN_XT_USER_USERLIST_UUL_ON_U_USER_LIST_ID_UUL_USER_LIST_ID
				+ "left join  xt_user_profile up on uul.user_id=up.user_id left join  xt_company_profile xcp on xcp.company_id = up.company_id "
				+ "where up.email_validation_ind=true  and u.associated_company_id = :userListId";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("userListId", userListId));
		hibernateSQLQueryResultRequestDTO.setClassInstance(ContactLeadResponseDTO.class);
		hibernateSQLQueryResultRequestDTO
				.setRowCountQueryString("select cast(count(distinct cast((up.user_id) as integer)) as int) "
						+ FROM_XT_USER_LIST_U_LEFT_JOIN_XT_USER_USERLIST_UUL_ON_U_USER_LIST_ID_UUL_USER_LIST_ID
						+ "left join  xt_user_profile up on uul.user_id=up.user_id left join  xt_company_profile xcp on xcp.company_id = up.company_id "
						+ "where up.email_validation_ind=true  and u.associated_company_id = :userListId");
		Pagination pagination = new Pagination();
		pagination.setMaxResults(4);
		return hibernateSQLQueryResultUtilDao.returnPaginatedDTOList(hibernateSQLQueryResultRequestDTO, pagination, "");
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> fetchUserIdsByUserListId(Integer userListId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "select distinct cast((up.user_id) as integer) "
				+ FROM_XT_USER_LIST_U_LEFT_JOIN_XT_USER_USERLIST_UUL_ON_U_USER_LIST_ID_UUL_USER_LIST_ID
				+ "left join  xt_user_profile up on uul.user_id=up.user_id left join xt_company_profile xcp on xcp.company_id = up.company_id "
				+ WHERE_UP_EMAIL_VALIDATION_IND_TRUE_AND_U_USER_LIST_ID_USER_LIST_ID;
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("userListId", userListId));
		return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public XtremandResponse getWelcomeEmailsList(Integer userId, Pagination pagination) {
		XtremandResponse response = new XtremandResponse();
		Integer loggedInUserCompanyId = userDao.getCompanyIdByUserId(userId);
		response.setData(userDAO.getWelcomeEmailsList(loggedInUserCompanyId, pagination));
		response.setStatusCode(200);
		return response;
	}

	@Override
	public HttpServletResponse downloadWelcomeEmailsList(Pageable pageable, Integer userId,
			HttpServletResponse response) {
		Pagination pagination = new Pagination();
		Integer loggedInUserCompanyId = userDao.getCompanyIdByUserId(userId);
		pagination.setUserId(loggedInUserCompanyId);
		String sort = pageable.getSort();
		if (sort != null && org.springframework.util.StringUtils.hasText(sort)) {
			String sortColumn = sort.split(",")[0];
			String sortOrder = sort.split(",")[1];
			pagination.setSortcolumn(sortColumn);
			pagination.setSortingOrder(sortOrder);
		}
		pagination.setSearchKey(pageable.getSearch());
		String fileName = "Welcome Emails Report";
		if (pagination != null && pagination.getUserId() != null && pagination.getUserId() > 0) {
			Map<String, Object> resultMap = userDao.downloadWelcomeEmailsList(pagination);
			@SuppressWarnings("unchecked")
			List<EmailActivityDTO> emailsList = (List<EmailActivityDTO>) resultMap.get("data");
			return frameSendWelcomeEmailCSVData(response, fileName, emailsList, pagination);
		}
		return null;
	}

	private HttpServletResponse frameSendWelcomeEmailCSVData(HttpServletResponse response, String fileName,
			List<EmailActivityDTO> userDTOList, Pagination pagination) {
		try {
			List<String[]> row = new ArrayList<>();
			List<String> headerList = new ArrayList<>();
			headerList.add("Email Subject");
			headerList.add("Status");
			headerList.add("Welcome Mail Sent On");
			headerList.add("Recipients");
			headerList.add("CC");
			headerList.add("BCC");
			headerList.add("Attachments");
			row.add(headerList.toArray(new String[0]));
			for (EmailActivityDTO emailData : userDTOList) {
				String publishedDate = checkIfDateIsNull(emailData.getSentOn());
				List<String> dataList = new ArrayList<>();
				dataList.add(emailData.getSubject());
				dataList.add(emailData.getStatus());
				dataList.add(publishedDate);
				dataList.add(emailData.getToEmailIdsString());
				dataList.add(emailData.getCcEmailIdsString());
				dataList.add(emailData.getBccEmailIdsString());
				dataList.add(emailData.getFileNames());
				row.add(dataList.toArray(new String[0]));
			}
			return XamplifyUtils.generateCSV(fileName, response, row);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	public static String checkIfFullNameIsNull(String firstName, String lastName) {
		StringBuilder fullName = new StringBuilder();

		if (firstName != null && !firstName.trim().isEmpty()) {
			fullName.append(firstName.trim());
		}

		if (lastName != null && !lastName.trim().isEmpty()) {
			if (fullName.length() > 0) {
				fullName.append(" ");
			}
			fullName.append(lastName.trim());
		}

		return fullName.toString();
	}

	private String checkIfDateIsNull(Date date) {
		String dateToString = "";
		if (date != null) {
			dateToString = DateUtils.convertDateToString(date);
		}
		return dateToString;
	}

	@SuppressWarnings("unchecked")
	@Override
	public XtremandResponse deleteContactFromAllContactLists(List<Integer> contactIds, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		boolean isOwnContact = false;
		for (Integer contactId : contactIds) {
			Map<String, Object> map = userListDAO
					.deleteContactFromAllContactLists(contactId, loggedInUserId);
		}
		response.setStatusCode(isOwnContact ? 200 : 400);
		return response;
	}
	
	@Override
	public XtremandResponse findDefaultContactList(Integer loggedInUserId, String moduleName) {
		XtremandResponse response = new XtremandResponse();
		Integer companyId = userDAO.getCompanyIdByUserId(loggedInUserId);
		response.setData(userListDAO.findDefaultContactListIdByCompanyId(companyId, moduleName));
		response.setStatusCode(200);
		return response;
	}
}
