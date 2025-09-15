package com.xtremand.contacts.v2;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.text.WordUtils;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtremand.aws.AmazonWebService;
import com.xtremand.aws.CopiedFileDetails;
import com.xtremand.campaign.bom.DownloadDataInfo;
import com.xtremand.campaign.bom.DownloadDataInfo.DownloadItem;
import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.CompanyProfile.CompanyNameStatus;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.Criteria.OPERATION_NAME;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.company.bom.Company;
import com.xtremand.contacts.dto.ContactFieldsDTO;
import com.xtremand.contacts.dto.ContactsRequestDTO;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.flexi.fields.bom.UserListFlexiField;
import com.xtremand.flexi.fields.dto.FlexiFieldRequestDTO;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.UserListDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.mail.bom.EmailTemplate;
import com.xtremand.mail.service.EmailConstants;
import com.xtremand.mail.service.MailService;
import com.xtremand.mail.service.MailService.EmailBuilder;
import com.xtremand.partnership.dto.PartnerTeamMemberGroupDTO;
import com.xtremand.team.member.group.dao.TeamMemberGroupDao;
import com.xtremand.user.bom.LegalBasis;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.UserList;
import com.xtremand.user.bom.UserList.SocialNetwork;
import com.xtremand.user.bom.UserList.TYPE;
import com.xtremand.user.bom.UserListUploadHistory;
import com.xtremand.user.bom.UserUserList;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.list.dto.ContactsCountDTO;
import com.xtremand.user.list.dto.ProcessingUserListsDTO;
import com.xtremand.user.service.UserService;
import com.xtremand.userlist.dao.UserListDAO;
import com.xtremand.util.FileUtil;
import com.xtremand.util.XamplifyUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.XamplifyConstants;
import com.xtremand.util.service.DownloadDataUtilService;
import com.xtremand.util.service.UtilService;

@Service
public class ContactsV2ServiceImpl implements ContactsV2Service {

	private static final Logger logger = LoggerFactory.getLogger(ContactsV2ServiceImpl.class);

	private static final String DOWNLOAD_CSV_RESPONSE_MESSAGE = "We are processing your list(s) reports. We will send it over an email when the report is ready";

	@Value("${fromEmail}")
	private String fromEmail;

	@Value("${mail.sender}")
	String fromName;

	@Value("${separator}")
	String sep;

	@Value("${partner_list_folder}")
	String partnerList;

	@Value("${contact_list_folder}")
	String contactList;

	@Value("${amazon.base.url}")
	String amazonBaseUrl;

	@Value("${amazon.bucket.name}")
	String amazonBucketName;

	@Value("${amazon.env.folder}")
	String amazonEnvFolder;

	@Autowired
	private GenericDAO genericDao;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private FileUtil fileUtil;

	@Autowired
	private UserListDAO userListDao;

	@Autowired
	private ContactsV2Dao contactsV2Dao;

	@Autowired
	private UserService userService;

	@Autowired
	private MailService mailService;

	@Autowired
	private UtilService utilService;

	@Autowired
	private XamplifyUtil xamplifyUtil;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private TeamMemberGroupDao teamMemberGroupDao;

	@Autowired
	private AmazonWebService amazonWebService;

	@Autowired
	private SpringTemplateEngine templateEngine;

	@Autowired
	private DownloadDataUtilService downloadDataUtilService;

	@Override
	public XtremandResponse saveContactList(ContactsRequestDTO contactsRequestDTO) {
		boolean hasError = false;
		XtremandResponse xtremandResponse = new XtremandResponse();
		try {
			Integer userId = contactsRequestDTO.getLoggedInUserId();
			String debugMessageSuffix = "(" + contactsRequestDTO.getContactListName() + ", " + userId + ") at "
					+ new Date();
			String debugMessage = "Entered Into saveContactList " + debugMessageSuffix;
			logger.debug(debugMessage);
			contactsRequestDTO.setAddingNewContactList(true);
			xtremandResponse.setStatusCode(400);
			xtremandResponse.setMessage(XamplifyConstants.FAILED);
			xtremandResponse.setAccess(true);
			Integer companyId = userDao.getCompanyIdByUserId(userId);
			boolean isDuplicateListName = false;
			String userListName = contactsRequestDTO.getContactListName();
			String moduleName = "CONTACTS";
			isDuplicateListName = userListDao.isDuplicateListName(userListName, companyId, moduleName);
			if (!isDuplicateListName) {
				String contactListNameValidatedDebugMessage = "Validating Contact List Name :  " + userListName
						+ "  at " + new Date();
				logger.debug(contactListNameValidatedDebugMessage);
				Set<ContactFieldsDTO> users = contactsRequestDTO.getUsers();
				String filePath = writeDataIntoCsvFile(users, companyId, userListName);
				UserList userList = new UserList();
				userList.setName(userListName);
				userList.setUploadedDate(new Date());
				userList.setCreatedTime(new Date());
				userList.setUpdatedTime(new Date());
				userList.setSocialNetwork(
						UserList.getSocialNetworkEnum(contactsRequestDTO.getSocialNetwork().toUpperCase()));
				userList.setContactType(UserList.getContactTypeEnum(contactsRequestDTO.getContactType()));
				User createdBy = new User();
				createdBy.setUserId(userId);
				userList.setOwner(createdBy);
				userList.setUploadedDate(new Date());
				userList.setSocialNetwork(SocialNetwork.MANUAL);
				userList.setContactType(TYPE.CONTACT);
				CompanyProfile companyProfile = new CompanyProfile();
				companyProfile.setId(companyId);
				userList.setCompany(companyProfile);
				userList.setPublicList(contactsRequestDTO.isPublicList());
				userList.setPartnerUserList(false);
				userList.setModuleName("CONTACTS");
				userList.setUploadInProgress(true);
				userList.setUpdatedBy(userId);
				userList.setCsvPath(filePath);
				Long externalListId = contactsRequestDTO.getExternalListId();
				if (externalListId != null && externalListId > 0) {
					userList.setExternalListId(externalListId);
				}
				genericDao.save(userList);

				Integer userListId = userList.getId();

				saveUserListUploadHistory(contactsRequestDTO, filePath, userListId);

				XamplifyUtils.addSuccessStatus(xtremandResponse);
				String userListCreatedDebugMessage = "Contact List Name : " + userListName
						+ " has been succesfully created with id : " + userListId;
				logger.debug(userListCreatedDebugMessage);
			} else {
				xtremandResponse.setStatusCode(401);
				hasError = true;
				xtremandResponse.setMessage("list name already exists");
				String duplicateContactListNameDebugMessage = "Duplcate Contact List Name : " + debugMessageSuffix;
				logger.debug(duplicateContactListNameDebugMessage);
			}

		} catch (HibernateException he) {
			logger.error(he.getMessage());
			hasError = true;
		} catch (XamplifyDataAccessException xe) {
			logger.error(xe.getMessage());
			hasError = true;
		} catch (Exception ex) {
			logger.error(ex.getMessage());
			hasError = true;
		} finally {
			if (!hasError) {
				String debugMessage = "Sending Email Notification About Contact List Processing " + new Date();
				logger.debug(debugMessage);
				sendEmailNotificationToLoggedInUserAboutContactListStatus(contactsRequestDTO,
						EmailConstants.CONTACT_LIST_IN_PROCESS);
			}

		}
		return xtremandResponse;

	}

	private void saveUserListUploadHistory(ContactsRequestDTO contactsRequestDTO, String filePath, Integer userListId) {
		contactsRequestDTO.setUserListId(userListId);
		UserListUploadHistory userListUploadHistory = new UserListUploadHistory();
		userListUploadHistory.setUserListId(userListId);
		userListUploadHistory.setCreatedTime(new Date());
		userListUploadHistory.setCsvRowsCount(contactsRequestDTO.getUsers().size());
		userListUploadHistory.setCsvPath(filePath);
		genericDao.save(userListUploadHistory);
		contactsRequestDTO.setUserListUploadHistoryId(userListUploadHistory.getId());
	}

	private String writeDataIntoCsvFile(Set<ContactFieldsDTO> users, Integer companyId, String userListName) {
		String csvDebugMessage = "Writing Data Into Csv File For Contact List Name : " + userListName + " at "
				+ new Date();
		logger.debug(csvDebugMessage);
		String filePath = fileUtil.uploadContactsToCsvFile(userListName, users, companyId);
		String csvFileCreatedSucessfullyDebugMessage = "Data successfully written into csv file for the Contact List Name : "
				+ userListName + " at " + new Date();
		logger.debug(csvFileCreatedSucessfullyDebugMessage);
		return filePath;
	}

	@Override
	public void saveContacts(ContactsRequestDTO contactsRequestDTO) {
		boolean hasError = false;
		try {
			Set<Integer> allUserListIds = new HashSet<>();
			Integer loggedInUserId = contactsRequestDTO.getLoggedInUserId();
			Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
			UserList userList = new UserList();
			userList.setId(contactsRequestDTO.getUserListId());
			userList.setName(contactsRequestDTO.getContactListName());
			CompanyProfile companyProfile = new CompanyProfile();
			companyProfile.setId(companyId);
			userList.setCompany(companyProfile);
			userList.setUpdatedBy(loggedInUserId);
			User loggedInUser = new User();
			loggedInUser.setUserId(loggedInUserId);
			userList.setOwner(loggedInUser);
			Integer defaultMasterContactListId = contactsV2Dao.getDefaultMasterContactListIdByCompanyId(companyId);
			UserList defaultMasterContactList = new UserList();
			if (XamplifyUtils.isValidInteger(defaultMasterContactListId)) {
				defaultMasterContactList.setId(defaultMasterContactListId);
				allUserListIds.add(defaultMasterContactListId);
			} else {
				String defaultContactListNotFoundErrorMessage = "Default Contact List Not Found For " + companyId
						+ ".Adding contacts will be skipped for default contact list.";
				logger.error(defaultContactListNotFoundErrorMessage);
			}
			Integer userListId = userList.getId();
			allUserListIds.add(userListId);
			contactsV2Dao.save(contactsRequestDTO, userList, defaultMasterContactList, allUserListIds);
			List<Integer> allUserListIdsArrayList = XamplifyUtils.convertSetToList(allUserListIds);
			String validatingEmailIdsDebugMessage = "Contacts Uploaded Successfully And Updating Email Validation Properties For "
					+ allUserListIdsArrayList.size() + " User lists";
			logger.debug(validatingEmailIdsDebugMessage);
			contactsV2Dao.updateUserListStatusAsCompleted(allUserListIdsArrayList);
			String validatedAndUploadedDebugMessage = "Contacts Validated And Uploaded Successfully To "
					+ userList.getName() + "(" + userListId + ")";
			logger.debug(validatedAndUploadedDebugMessage);
		} catch (HibernateException he) {
			logger.error(he.getMessage());
			hasError = true;
		} catch (XamplifyDataAccessException xe) {
			logger.error(xe.getMessage());
			hasError = true;
		} catch (Exception ex) {
			logger.error(ex.getMessage());
			hasError = true;
		} finally {
			if (!hasError) {
				Integer emailTemplateId = contactsRequestDTO.isUpdatingContactList()
						? EmailConstants.CONTACT_LIST_UPDATED
						: EmailConstants.CONTACT_LIST_CREATED;
				sendEmailNotificationToLoggedInUserAboutContactListStatus(contactsRequestDTO, emailTemplateId);
			}

		}

	}

	@Override
	public XtremandResponse updateContactList(ContactsRequestDTO contactsRequestDTO) {
		XtremandResponse response = new XtremandResponse();
		Integer userListId = contactsRequestDTO.getUserListId();
		Integer loggedInUserId = contactsRequestDTO.getLoggedInUserId();
		String debugMessage = "Entered Into updateContactList " + userListId;
		logger.debug(debugMessage);
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		Set<ContactFieldsDTO> users = contactsRequestDTO.getUsers();
		ProcessingUserListsDTO userListDTO = contactsV2Dao.getUserListNameById(userListId);
		String userListName = userListDTO.getUserListName();
		String filePath = writeDataIntoCsvFile(users, companyId, userListName);
		contactsRequestDTO.setUserListId(userListId);
		contactsRequestDTO.setContactListName(userListName);
		UserListUploadHistory userListUploadHistory = new UserListUploadHistory();
		userListUploadHistory.setUserListId(userListId);
		userListUploadHistory.setCreatedTime(new Date());
		userListUploadHistory.setCsvRowsCount(contactsRequestDTO.getUsers().size());
		userListUploadHistory.setCsvPath(filePath);
		genericDao.save(userListUploadHistory);
		Integer userListUploadHistoryId = userListUploadHistory.getId();
		contactsRequestDTO.setUserListUploadHistoryId(userListUploadHistoryId);
		contactsV2Dao.updateContactList(userListId, loggedInUserId, filePath);
		XamplifyUtils.addSuccessStatus(response);
		response.setMessage("CONTACT LIST UPDATED SUCCESSFULLY");
		return response;
	}

	public void sendEmailNotificationToLoggedInUserAboutContactListStatus(String userListName, Integer loggedInUserId,
			Integer emailTemplateId, Integer userListId) {
		User user = userService.findByPrimaryKey(loggedInUserId, new FindLevel[] { FindLevel.SHALLOW });
		EmailTemplate template = genericDao.get(EmailTemplate.class, emailTemplateId);
		String body = template.getBody();
		String subject = utilService.addPerfixToSubject(template.getSubject());
		body = XamplifyUtils.replaceCustomerFullName(user, body);
		body = body.replace("<contactlist_title>", XamplifyUtils.escapeDollarSequece(userListName));
		body = notifyContactListCreatedOrUpdated(emailTemplateId, userListId, body, subject, null);
		mailService.sendMail(new EmailBuilder().from(fromEmail).senderName(fromName).to(user.getEmailId())
				.subject(subject).body(body).build());
	}

	public void sendEmailNotificationToLoggedInUserAboutContactListStatus(ContactsRequestDTO contactsRequestDTO,
			int emailTemplateId) {
		Integer loggedInUserId = contactsRequestDTO.getLoggedInUserId();
		String userListName = contactsRequestDTO.getContactListName();
		Integer userListId = contactsRequestDTO.getUserListId();
		User user = userService.findByPrimaryKey(loggedInUserId, new FindLevel[] { FindLevel.SHALLOW });
		EmailTemplate template = genericDao.get(EmailTemplate.class, emailTemplateId);
		String body = template.getBody();
		String subject = utilService.addPerfixToSubject(template.getSubject());
		body = XamplifyUtils.replaceCustomerFullName(user, body);
		body = body.replace("<contactlist_title>", XamplifyUtils.escapeDollarSequece(userListName));
		body = notifyContactListCreatedOrUpdated(emailTemplateId, userListId, body, subject,
				contactsRequestDTO.getInsertedUserIds());
		mailService.sendMail(new EmailBuilder().from(fromEmail).senderName(fromName).to(user.getEmailId())
				.subject(subject).body(body).build());
	}

	private String notifyContactListCreatedOrUpdated(Integer emailTemplateId, Integer userListId, String body,
			String subject, Set<Integer> insertedUserIds) {
		boolean isContactListCreated = EmailConstants.CONTACT_LIST_CREATED == emailTemplateId;
		boolean isContactListUpdated = EmailConstants.CONTACT_LIST_UPDATED == emailTemplateId;
		Integer validContacts = 0;
		Integer invalidContacts = 0;
		if (isContactListCreated) {
			validContacts = userListDao.getValidContactsCountByUserListId(userListId);
			invalidContacts = userListDao.getInvalidContactsCountByUserList(userListId);
		} else if (isContactListUpdated) {
			validContacts = userListDao.getValidContactsCountByUserListIdAndUserIds(userListId, insertedUserIds);
			invalidContacts = userListDao.getInvalidContactsCountByUserListIdAndUserIds(userListId, insertedUserIds);
		}
		if (isContactListCreated || isContactListUpdated) {
			body = body.replace("<valid_contacts_count>", String.valueOf(validContacts));
			body = body.replace("<Invalid_contacts_count>", String.valueOf(invalidContacts));
		}
		return body;
	}

	@Override
	public void updateContacts(ContactsRequestDTO contactsRequestDTO) {
		contactsRequestDTO.setUpdatingContactList(true);
		saveContacts(contactsRequestDTO);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse updateEditContact(ContactsRequestDTO contactsRequestDTO) {
		XtremandResponse response = new XtremandResponse();
		List<ContactFieldsDTO> contactFieldsDTOs = new ArrayList<>(contactsRequestDTO.getUsers());
		Integer userListId = contactsRequestDTO.getUserListId();
		ContactFieldsDTO contactFieldsDTO = contactFieldsDTOs.get(0);
		Integer userId = contactFieldsDTO.getUserId();
		Integer loggedInUserId = contactsRequestDTO.getLoggedInUserId();
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		UserUserList userUserList = contactsV2Dao.findUserUserListsByUserIdAndUserList(userId, userListId);
		setUserUserListProperties(userUserList, contactFieldsDTO);
		if (XamplifyUtils.CONTACTS.equalsIgnoreCase(userUserList.getUserList().getModuleName())) {
			setCompanyUserUserListProperties(contactFieldsDTO, userUserList, companyId);
		}
		XamplifyUtils.addSuccessStatus(response);
		logger.info("Contact details updated successfully for userId={}, userListId={}", userId, userListId);
		response.setMessage("CONTACT LIST UPDATED SUCCESSFULLY");
		return response;
	}

	private void setUserUserListProperties(UserUserList userUserList, ContactFieldsDTO user) {
		userUserList.setFirstName(user.getFirstName());
		userUserList.setLastName(user.getLastName());
		userUserList.setContactCompany(user.getContactCompany());
		userUserList.setJobTitle(user.getJobTitle());
		userUserList.setAddress(user.getAddress());
		userUserList.setCity(user.getCity());
		userUserList.setState(user.getState());
		userUserList.setZipCode(user.getZipCode());
		userUserList.setCountry(user.getCountry());
		userUserList.setCountryCode(user.getCountryCode());
		userUserList.setMobileNumber(user.getMobileNumber());
		userUserList.setContactStatusId(user.getContactStatusId());
		if (XamplifyUtils.isValidInteger(user.getContactCompanyId())
				&& XamplifyUtils.isValidString(user.getContactCompany())) {
			Company company = new Company();
			company.setId(user.getContactCompanyId());
			userUserList.setCompany(company);
		} else {
			userUserList.setCompany(null);
		}
		List<LegalBasis> legalBasisList = new ArrayList<>();
		List<Integer> legalBasisIds = user.getLegalBasis();
		for (Integer legalBasisId : legalBasisIds) {
			LegalBasis legalBasis = new LegalBasis();
			legalBasis.setId(legalBasisId);
			legalBasisList.add(legalBasis);
		}
		userUserList.setLegalBasis(legalBasisList);

		/***** Flexi-Fields *****/
		if (XamplifyUtils.CONTACTS.equalsIgnoreCase(userUserList.getUserList().getModuleName())) {
			updateFlexiFields(user, userUserList);
		}
	}

	private void updateFlexiFields(ContactFieldsDTO user, UserUserList userUserList) {
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

	private void setCompanyUserUserListProperties(ContactFieldsDTO contactFieldsDTO, UserUserList userUserList,
			Integer vendorCompanyId) {
		String contactCompanyName = contactFieldsDTO.getContactCompany();
		Integer contactCompanyId = contactFieldsDTO.getContactCompanyId();
		Integer userId = contactFieldsDTO.getUserId();

		UserUserList existCompanyUserUserList = contactsV2Dao.findCompanyUserUserList(userId, vendorCompanyId);
		contactsV2Dao.updateUserUserListFields(contactCompanyName, contactCompanyId, vendorCompanyId, userId);

		boolean hasCompanyContact = false;
		if (existCompanyUserUserList != null) {
			UserList userList = existCompanyUserUserList.getUserList();
			if (userList != null && userList.getAssociatedCompany() != null
					&& XamplifyUtils.isValidInteger(userList.getAssociatedCompany().getId())) {
				Integer existingCompanyId = userList.getAssociatedCompany().getId();
				Integer updatedCompanyId = (userUserList.getCompany() != null) ? userUserList.getCompany().getId()
						: null;
				if (!Objects.equals(existingCompanyId, updatedCompanyId)) {
					contactsV2Dao.deleteCompanyUserUserList(existCompanyUserUserList.getId());
				} else {
					hasCompanyContact = true;
				}
			}
		}

		createCompanyUserUserList(contactFieldsDTO, userUserList, hasCompanyContact);
	}

	private void createCompanyUserUserList(ContactFieldsDTO contactFieldsDTO, UserUserList userUserList,
			boolean hasCompanyContact) {
		if (!hasCompanyContact && userUserList.getCompany() != null) {
			Integer companyUserListId = contactsV2Dao.findCompanyUserUserListId(userUserList.getCompany().getId());
			if (XamplifyUtils.isValidInteger(companyUserListId)) {
				UserUserList newCompanyUserUserList = new UserUserList();
				User user = new User();
				user.setUserId(contactFieldsDTO.getUserId());
				newCompanyUserUserList.setUser(user);
				UserList userList = new UserList();
				userList.setId(companyUserListId);
				newCompanyUserUserList.setUserList(userList);
				setUserUserListProperties(newCompanyUserUserList, contactFieldsDTO);
				updateFlexiFields(contactFieldsDTO, newCompanyUserUserList);
				genericDao.save(newCompanyUserUserList);
			}
		}
	}

	@Override
	@Transactional
	public Map<String, Object> findUserListContacts(Pagination pagination) {
		Map<String, Object> resultMap = new HashMap<>();

		if (!XamplifyUtils.isValidInteger(pagination.getUserId())) {
			throw new IllegalArgumentException("userID is required");
		}

		if (!XamplifyUtils.isValidInteger(pagination.getUserListId())) {
			throw new IllegalArgumentException("userListID is required");
		}

		Integer companyId = userDao.getCompanyIdByUserId(pagination.getUserId());
		pagination.setCompanyId(companyId);

		Map<String, Object> map = contactsV2Dao.findUserListContacts(pagination);

		@SuppressWarnings("unchecked")
		List<UserDTO> userDTOs = (List<UserDTO>) map.get("list");
		Integer totalRecords = (Integer) map.get("totalRecords");

		frameUserUserListDetails(pagination, userDTOs);

		resultMap.put("listOfUsers", userDTOs);
		resultMap.put("totalRecords", totalRecords);
		return resultMap;
	}

	private void frameUserUserListDetails(Pagination pagination, List<UserDTO> userDTOs) {
		Integer companyId = pagination.getCompanyId();
		String moduleName = pagination.getModuleName();
		boolean isLoginAsPartnerOptionAvailableForVendorCompany = utilDao.hasLoginAsPartnerAccessByCompanyId(companyId);
		for (UserDTO userDTO : userDTOs) {
			userDTO.setCountry(WordUtils.capitalizeFully(userDTO.getCountry()));
			boolean isSignup = (userDTO.getPassword() != null
					|| (userDTO.getPassword() == null && "APPROVE".equalsIgnoreCase(userDTO.getUserStatus())));
			userDTO.setSignedUp(isSignup);
			if (!pagination.isExportToExcel()) {
				userDTO.setLegalBasis(
						userListDao.listLegalBasisByContactListIdAndUserId(userDTO.getUserListId(), userDTO.getId()));

				if (userDTO.isPartnerUserList()) {
					setContactCompany(userDTO);
					setTeamMemberGroupDetails(userDTO);
					userDTO.setLoginAsPartner(isLoginAsPartnerOptionAvailableForVendorCompany);
				}
			}

			if (XamplifyUtils.CONTACTS.equalsIgnoreCase(moduleName)) {
				addContactsFlexiFields(userDTO);
			}
		}
	}

	private void addContactsFlexiFields(UserDTO userDTO) {
		if (XamplifyUtils.isValidString(userDTO.getFlexiFieldsJson())) {
			try {
				List<FlexiFieldRequestDTO> flexiFields = new ObjectMapper().readValue(userDTO.getFlexiFieldsJson(),
						new TypeReference<List<FlexiFieldRequestDTO>>() {
						});
				userDTO.setFlexiFields(flexiFields);
			} catch (Exception e) {
				userDTO.setFlexiFields(Collections.emptyList());
			}
		} else {
			userDTO.setFlexiFields(Collections.emptyList());
		}
	}

	private void setContactCompany(UserDTO userDTO) {
		if (XamplifyUtils.isValidString(userDTO.getContactCompany())
				&& XamplifyUtils.isValidString(userDTO.getPartnerCompanyName()) && userDTO.getCompanyNameStatus()
						.toLowerCase().equalsIgnoreCase(CompanyNameStatus.ACTIVE.name().toLowerCase())) {
			String updatedContactCompany = userDTO.getPartnerCompanyName() + ", aka-" + userDTO.getContactCompany();
			userDTO.setDisplayContactCompany(updatedContactCompany);
		} else {
			userDTO.setDisplayContactCompany(userDTO.getContactCompany());
		}
	}

	private void setTeamMemberGroupDetails(UserDTO userDTO) {
		Integer partnershipId = userDTO.getPartnershipId();
		List<PartnerTeamMemberGroupDTO> partnerTeamMemberGroupDTOs = teamMemberGroupDao
				.findTeamMemberGroupIdByPartnershipId(partnershipId);
		if (XamplifyUtils.isNotEmptyList(partnerTeamMemberGroupDTOs)) {
			if (partnerTeamMemberGroupDTOs.size() > 1) {
				userDTO.setMultipleTeamMemberGroupsAssigned(true);
				setDefaultTeamMemberGroupData(userDTO);
				String message = partnerTeamMemberGroupDTOs.stream().map(PartnerTeamMemberGroupDTO::getId)
						.collect(Collectors.toList()) + " and partnershipId:-" + partnershipId;
				String errorMessage = "setTeamMemberGroupDetails();Multiple Groups Assigned:-" + message;
				logger.error(errorMessage);
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

	private void setDefaultTeamMemberGroupData(UserDTO userDTO) {
		userDTO.setTeamMemberGroupId(0);
		userDTO.setSelectedTeamMembersCount(0);
		userDTO.setSelectedTeamMemberIds(new HashSet<>());
		userDTO.setSelectedTeamMemberGroupName("");
	}

	@Override
	public XtremandResponse findUserListContactsCount(Integer loggedInUserId, Integer userListId, String moduleName) {
		XtremandResponse response = new XtremandResponse();
		ContactsCountDTO contactsCountDTO = new ContactsCountDTO();
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		if (XamplifyUtils.CONTACTS.equalsIgnoreCase(moduleName)) {
			contactsCountDTO.setValidCount(contactsV2Dao.findUserListContactsCount(companyId, userListId, "VALID"));
			contactsCountDTO
					.setExcludedCount(contactsV2Dao.findUserListContactsCount(companyId, userListId, "EXCLUDED"));
			contactsCountDTO.setInValidCount(contactsV2Dao.findUserListContactsCount(companyId, userListId, "INVALID"));
			contactsCountDTO.setUnSubscribedCount(
					contactsV2Dao.findUserListContactsCount(companyId, userListId, "UNSUBSCRIBED"));
			contactsCountDTO.setAllCounts(contactsCountDTO.getValidCount() + contactsCountDTO.getExcludedCount()
					+ contactsCountDTO.getInValidCount() + contactsCountDTO.getUnSubscribedCount());
		} else {
			contactsCountDTO.setActiveCount(contactsV2Dao.findUserListContactsCount(companyId, userListId, "ACTIVE"));
			contactsCountDTO
					.setDeactivatedCount(contactsV2Dao.findUserListContactsCount(companyId, userListId, "DEACTIVATED"));
			contactsCountDTO
					.setInActiveCount(contactsV2Dao.findUserListContactsCount(companyId, userListId, "NON-ACTIVE"));
			contactsCountDTO.setUnSubscribedCount(
					contactsV2Dao.findUserListContactsCount(companyId, userListId, "PARTNER-UNSUBSCRIBED"));
			contactsCountDTO.setAllCounts(contactsCountDTO.getActiveCount() + contactsCountDTO.getDeactivatedCount()
					+ contactsCountDTO.getInActiveCount() + contactsCountDTO.getUnSubscribedCount());
		}
		response.setData(contactsCountDTO);
		XamplifyUtils.addSuccessStatus(response);
		return response;
	}

	@Override
	public XtremandResponse updateDownloadDataInfoStatus(Pagination pagination, XtremandResponse response) {
		Integer userId = pagination.getUserId();
		String moduleName = pagination.getModuleName();
		DownloadItem listName = XamplifyUtils.CONTACTS.equalsIgnoreCase(moduleName) ? DownloadItem.CONTACT_LIST
				: DownloadItem.PARTNER_LIST;
		DownloadDataInfo downloadDataInfo = utilDao.getDownloadDataInfo(userId, listName);
		if (downloadDataInfo == null || !downloadDataInfo.isDownloadInProgress()) {
			downloadDataInfo = utilDao.updateDownloadDataInfo(userId, downloadDataInfo, listName);
			response.setStatusCode(200);
			String responseMessage = XamplifyUtils.CONTACTS.equalsIgnoreCase(moduleName) ? DOWNLOAD_CSV_RESPONSE_MESSAGE
					: DOWNLOAD_CSV_RESPONSE_MESSAGE.replace("list", "group");
			response.setMessage(responseMessage);
		} else {
			response.setMessage("Please wait until the previous request is processed...!");
			response.setStatusCode(401);
		}
		response.setData(downloadDataInfo);
		return response;
	}

	@Override
	public void downloadListOfContacts(Pagination pagination, DownloadDataInfo dataInfo) {
		Integer userId = pagination.getUserId();
		String contactType = pagination.getType();
		String moduleName = pagination.getModuleName();
		try {
			Integer companyId = userDao.getCompanyIdByUserId(userId);
			pagination.setCompanyId(companyId);

			Map<String, Object> map = contactsV2Dao.findUserListContacts(pagination);

			@SuppressWarnings("unchecked")
			List<UserDTO> userDTOs = (List<UserDTO>) map.get("list");

			frameUserUserListDetails(pagination, userDTOs);

			UserListDTO userListDTO = new UserListDTO();
			userListDTO.setModuleName(moduleName);
			userListDTO.setContactType(contactType);

			String folderName = XamplifyUtils.CONTACTS.equalsIgnoreCase(moduleName) ? contactList : partnerList;
			String fileName = moduleName + "_list_";

			CopiedFileDetails copiedFileDetails = pagination.isEditCampaign()
					? downloadDataUtilService.editListCsv(userId, userDTOs, folderName, fileName, userListDTO)
					: downloadDataUtilService.userListCsv(userId, userDTOs, folderName, fileName, userListDTO);
			uploadDownloadedDataIntoAws(userId, folderName, copiedFileDetails, dataInfo);
		} catch (Exception e) {
			utilDao.updateDownloadDataInfo(userId, "", dataInfo.getType(), dataInfo.getId());
		}
	}

	private void uploadDownloadedDataIntoAws(Integer userId, String folderName, CopiedFileDetails copiedFileDetails,
			DownloadDataInfo dataInfo) {
		DownloadItem type = dataInfo.getType();
		String date = new SimpleDateFormat("ddMMyyyy").format(new Date());
		String amazonFilePath = amazonEnvFolder + folderName + userId + sep + date + sep
				+ copiedFileDetails.getUpdatedFileName();
		String localFilePath = copiedFileDetails.getCompleteName();
		amazonWebService.uploadDataToAws(amazonFilePath, localFilePath);
		String completeAmazonFilePath = amazonBaseUrl + amazonBucketName + sep + amazonFilePath;
		String cloudFrontUrl = xamplifyUtil.replaceS3WithCloudfrontViceVersa(completeAmazonFilePath);

		utilDao.updateDownloadDataInfo(userId, cloudFrontUrl, type, dataInfo.getId());
		sendEmailNotificationToUser(userId, cloudFrontUrl, type);
	}

	private void sendEmailNotificationToUser(Integer userId, String completeAmazonFilePath, DownloadItem dataType) {
		User loggedInUser = userService.loadUser(
				Arrays.asList(new Criteria(XamplifyConstants.USER_ID, OPERATION_NAME.eq, userId)),
				new FindLevel[] { FindLevel.SHALLOW });
		Context context = new Context();
		Map<String, Object> model = new HashMap<>();
		String firstName = XamplifyUtils.isValidString(loggedInUser.getFirstName()) ? loggedInUser.getFirstName()
				: "there";
		String subject = "Hi " + firstName + " Your Module Report is Ready! 🚀";
		model.put("firstName", firstName);
		model.put("completeAmazonFilePath", completeAmazonFilePath);
		if (dataType.equals(DownloadItem.PARTNER_LIST)) {
			subject = subject.replace("Module", "Partners");
			model.put("moduleName", "partners");
		} else if (dataType.equals(DownloadItem.CONTACT_LIST)) {
			subject = subject.replace("Module", "Contacts");
			model.put("moduleName", "contacts");
		}
		context.setVariables(model);
		String htmlBody = templateEngine.process("download-csv", context);
		subject = utilService.addPerfixToSubject(subject);
		mailService.sendMail(new EmailBuilder().from(fromEmail).senderName(fromName).to(loggedInUser.getEmailId())
				.subject(subject).body(htmlBody).build());
	}

}
