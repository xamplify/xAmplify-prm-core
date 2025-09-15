package com.xtremand.contacts.v2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.ArrayUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.sql.JoinType;
import org.hibernate.transform.Transformers;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.Criteria.OPERATION_NAME;
import com.xtremand.common.bom.Pagination;
import com.xtremand.company.bom.Company;
import com.xtremand.contacts.dto.ContactFieldsDTO;
import com.xtremand.contacts.dto.ContactsRequestDTO;
import com.xtremand.flexi.fields.bom.FlexiField;
import com.xtremand.flexi.fields.bom.UserListFlexiField;
import com.xtremand.flexi.fields.dto.FlexiFieldRequestDTO;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.user.bom.LegalBasis;
import com.xtremand.user.bom.ModulesDisplayType;
import com.xtremand.user.bom.Role;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.User.UserDefaultPage;
import com.xtremand.user.bom.User.UserStatus;
import com.xtremand.user.bom.UserList;
import com.xtremand.user.bom.UserList.ContactListTypeValue;
import com.xtremand.user.bom.UserList.SocialNetwork;
import com.xtremand.user.bom.UserList.TYPE;
import com.xtremand.user.bom.UserUserList;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.list.dto.ProcessingUserListsDTO;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.XamplifyUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.SortColumnDTO;
import com.xtremand.util.dto.UserUserListDTO;
import com.xtremand.util.dto.XamplifyConstants;
import com.xtremand.util.service.UtilService;

@Repository
public class HibernateContactsV2Dao implements ContactsV2Dao {

	private static final Logger logger = LoggerFactory.getLogger(HibernateContactsV2Dao.class);

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private XamplifyUtil xamplifyUtil;

	@Autowired
	private PaginationUtil paginationUtil;

	@Value("${zerobounce_api_url}")
	String zeroBounceApiUrl;

	@Value("${zerobounce_api_access_key}")
	String zeroBounceApiAccessKey;

	@Value("${mailboxlayer_api_url}")
	String mailBoxLayerApiUrl;

	@Value("${mailboxlayer_api_access_key}")
	String mailBoxLayerApiAccessKey;

	@Value("${zerobounce.bulk.validate.api}")
	String zerobounceBulkValidateApi;

	@Value("${partner.details.query}")
	private String partnerDetailsQuery;

	@Value("${contact.details.query}")
	private String contactDetailsQuery;

	@Value("${contacts.count.query}")
	private String contactsCountQuery;

	@Value("${teamMemberSql}")
	private String teamMemberSql;

	@Autowired
	private UtilService utilService;

	private static final String VALID = "valid";

	private static final String EMAIL_ID = "emailId";

	private static final String PARTNERS = "PARTNERS";

	private static final String XUPEMAILID = "xup.email_id";

	private static final String CONTACT_COMPANY = "contactCompany";

	private static final String XUULCONTACTCOMPANY = "xuul.contact_company";

	@Override
	public void save(ContactsRequestDTO contactsRequestDTO, UserList userList, UserList defaultMasterContactList,
			Set<Integer> allUserListIds) {
		List<ContactFieldsDTO> contactFieldsDTOs = new ArrayList<>(contactsRequestDTO.getUsers());
		int total = contactFieldsDTOs.size();
		int count = 1;
		for (int i = 0; i < total; i++) {
			ContactFieldsDTO contactFieldsDTO = contactFieldsDTOs.get(i);
			contactFieldsDTO.setLoggedInUserId(contactsRequestDTO.getLoggedInUserId());
			Integer userId = saveContactAsUser(contactFieldsDTO, contactsRequestDTO);
			contactsRequestDTO.getInsertedUserIds().add(userId);
			saveIntoUserListAndUserUserListAndCompanyTables(userList, defaultMasterContactList, count, contactFieldsDTO,
					userId, allUserListIds, contactsRequestDTO);

			double completedPercentage = (double) Math.round(count * 100.0) / total;

			String debugMessage = count + "/" + total + "(" + completedPercentage + "%)" + " uploaded into "
					+ userList.getName() + "(" + userList.getId() + ") And Master Contact List ("
					+ defaultMasterContactList.getId() + ")";

			logger.debug(debugMessage);
			count++;
		}
	}

	private Integer getExisitingCompanyContactUserListId(Integer companyId, Integer contactCompanyId) {
		Session session = sessionFactory.openSession();
		Integer userListId = null;
		try {
			String queryString = "select user_list_id from xt_user_list where company_id = :companyId and associated_company_id = :associatedCompanyId";
			Query query = session.createSQLQuery(queryString);
			query.setParameter(XamplifyConstants.COMPANY_ID, companyId);
			query.setParameter("associatedCompanyId", contactCompanyId);
			userListId = (Integer) query.uniqueResult();
			session.flush();
			session.clear();
		} catch (HibernateException e) {
			e.printStackTrace();
		} finally {
			session.close();
		}
		return userListId;
	}

	private void saveIntoUserListAndUserUserListAndCompanyTables(UserList userList, UserList defaultMasterContactList,
			int count, ContactFieldsDTO contactFieldsDTO, Integer userId, Set<Integer> allUserListIds,
			ContactsRequestDTO contactsRequestDTO) {
		Session contactListUsersSession = sessionFactory.openSession();
		try {
			Integer companyId = userList.getCompany().getId();

			boolean hasCompanyName = XamplifyUtils.isValidString(contactFieldsDTO.getContactCompany());
			/**** Insert Into xt_company ******/
			Integer contactCompanyId = hasCompanyName ? saveCompany(userList, contactFieldsDTO, companyId) : null;

			contactFieldsDTO.setContactCompanyId(contactCompanyId);

			/*** Insert Into xt_user_userlist ***/
			saveIntoUserUserList(userList, contactFieldsDTO, contactListUsersSession, userId, companyId);
			/*** Insert Into Default Master Contact List **********/
			if (XamplifyUtils.isValidInteger(defaultMasterContactList.getId())) {
				saveIntoUserUserList(defaultMasterContactList, contactFieldsDTO, contactListUsersSession, userId,
						companyId);
			}

			if (hasCompanyName) {
				/*** Insert Into xt_user_list company list ***/
				Integer existingContactCompanyUserListId = saveContactCompanyUserList(userList, contactFieldsDTO,
						companyId, contactCompanyId);
				allUserListIds.add(existingContactCompanyUserListId);
				/*** Insert Into xt_user_user_list for company contact list ****/
				saveContactCompanyUserUserList(contactFieldsDTO, userId, contactCompanyId,
						existingContactCompanyUserListId);
			}
			updateUserListHistory(count, contactsRequestDTO.getUserListUploadHistoryId(), contactListUsersSession);

			contactListUsersSession.flush();
			contactListUsersSession.clear();

		} catch (ConstraintViolationException constraintViolationException) {
			String errorMessage = "Contact List Session ConstraintViolationException ";
			logger.info(errorMessage);
		} catch (HibernateException hibernateException) {
			String errorMessage = "Contact List Session HibernateException ";
			logger.info(errorMessage);
			hibernateException.printStackTrace();
		} catch (DataIntegrityViolationException dataIntegrityViolationException) {
			String errorMessage = "Contact List Session DataIntegrityViolationException ";
			logger.debug(errorMessage);
		} finally {
			contactListUsersSession.close();
		}
	}

	private void updateUserListHistory(int count, Integer userListUploadHistoryId, Session contactListUsersSession) {
		Query query = contactListUsersSession.createSQLQuery(
				"update xt_user_list_upload_history set inserted_count = :insertedCount where id = :id");
		query.setParameter("insertedCount", count);
		query.setParameter("id", userListUploadHistoryId);
		query.executeUpdate();
	}

	private Integer saveCompany(UserList userList, ContactFieldsDTO contactFieldsDTO, Integer companyId) {
		String contactCompanyName = contactFieldsDTO.getContactCompany();
		Integer contactCompanyId = getContactCompanyId(companyId, contactCompanyName);
		boolean isContactCompanyIdExists = XamplifyUtils.isValidInteger(contactCompanyId);
		boolean hasError = false;
		if (!isContactCompanyIdExists) {
			Session contactCompanySession = sessionFactory.openSession();
			try {
				Company companyContact = new Company();
				companyContact.setName(contactCompanyName);
				companyContact.setCreatedBy(userList.getUpdatedBy());
				companyContact.setCreatedTime(new Date());
				companyContact.setUpdatedTime(new Date());
				companyContact.setUpdatedBy(userList.getUpdatedBy());
				companyContact.setCompanyProfile(userList.getCompany());
				String websiteDomain = contactFieldsDTO.getEmailId().substring(contactFieldsDTO.getEmailId().indexOf("@") + 1);
				companyContact.setWebsite(websiteDomain);
				contactCompanyId = (Integer) contactCompanySession.save(companyContact);
				contactCompanySession.flush();
				contactCompanySession.clear();
			} catch (ConstraintViolationException constraintViolationException) {
				hasError = true;
				String errorMessage = " ConstraintViolationException ";
				logger.info(errorMessage);
			} catch (HibernateException hibernateException) {
				String errorMessage = " HibernateException ";
				logger.info(errorMessage);
				hibernateException.printStackTrace();
			} catch (DataIntegrityViolationException dataIntegrityViolationException) {
				String errorMessage = "  DataIntegrityViolationException ";
				logger.debug(errorMessage);
			} finally {
				if (hasError) {
					contactCompanyId = getContactCompanyId(companyId, contactCompanyName);
				}
				contactCompanySession.close();
			}
		}
		return contactCompanyId;
	}

	private Integer getContactCompanyId(Integer companyId, String contactCompanyName) {
		Integer contactCompanyId = null;
		Session session = sessionFactory.openSession();
		try {
			String queryString = "select id from xt_company where company_id = :companyId and lower(trim(name)) = LOWER(TRIM(:companyName))";
			Query query = session.createSQLQuery(queryString).setParameter(XamplifyConstants.COMPANY_ID, companyId)
					.setParameter("companyName", contactCompanyName);
			@SuppressWarnings("unchecked")
			List<Integer> ids = query.list();
			if (XamplifyUtils.isNotEmptyList(ids)) {
				contactCompanyId = ids.get(0);
			}
			session.flush();
			session.clear();
		} catch (HibernateException e) {
			e.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			session.close();
		}
		return contactCompanyId;
	}

	private void saveIntoUserUserList(UserList userList, ContactFieldsDTO contactFieldsDTO,
			Session contactListUsersSession, Integer userId, Integer companyId) {
		if (doesUserExistsInUserUserList(userList.getId(), userId)) {
			updateContactCompany(contactFieldsDTO, userId, companyId);
		} else {
			setUserUserListPropertiesAndSave(userList, contactFieldsDTO, contactListUsersSession, userId);
		}
	}

	private void updateContactCompany(ContactFieldsDTO contactFieldsDTO, Integer userId, Integer companyId) {
		List<UserUserListDTO> userUserListDtos = getExistingCompanyDetailsForContacts(userId, companyId);

		List<Integer> contactCompanyIds = userUserListDtos.stream().map(UserUserListDTO::getContactCompanyId)
				.collect(Collectors.toList());

		Integer contactCompanyIdFromDb = null;
		if (XamplifyUtils.isNotEmptyList(contactCompanyIds)) {
			contactCompanyIdFromDb = contactCompanyIds.get(0);
		}
		Integer contactCompanyId = contactFieldsDTO.getContactCompanyId();
		boolean isValidContactCompanyId = XamplifyUtils.isValidInteger(contactCompanyId);
		boolean isContactCompanyIdUpdated = isValidContactCompanyId && !contactCompanyId.equals(contactCompanyIdFromDb);
		if (isContactCompanyIdUpdated) {
			for (UserUserListDTO userUserListDTO : userUserListDtos) {
				updateOrDeleteUserUserList(contactFieldsDTO, contactCompanyId, userUserListDTO);
			}
		}
	}

	private void updateOrDeleteUserUserList(ContactFieldsDTO contactFieldsDTO, Integer contactCompanyId,
			UserUserListDTO userUserListDTO) {
		Session session = sessionFactory.openSession();
		Integer userUserListId = userUserListDTO.getUserUserListId();
		try {
			String queryString = userUserListDTO.isCompanyContactList()
					? "delete from xt_user_userlist where id = :userUserListId"
					: "update xt_user_userlist set contact_company = :contactCompany, contact_company_id = :contactCompanyId where id = :userUserListId";
			Query query = session.createSQLQuery(queryString).setParameter("userUserListId", userUserListId);
			if (!userUserListDTO.isCompanyContactList()) {
				query.setParameter(CONTACT_COMPANY, contactFieldsDTO.getContactCompany())
						.setParameter("contactCompanyId", contactCompanyId);
			}
			query.executeUpdate();
			session.flush();
			session.clear();
		} catch (HibernateException e) {
			e.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			session.close();
		}
	}

	@SuppressWarnings("unchecked")
	private List<UserUserListDTO> getExistingCompanyDetailsForContacts(Integer userId, Integer companyId) {
		List<UserUserListDTO> userUserListDtos = new ArrayList<>();
		Session session = sessionFactory.openSession();
		try {
			String queryString = "select xuu.id as \"userUserListId\",xuu.user_id as \"userId\",xuu.user_list_id as \"userListId\",\n"
					+ "xuu.contact_company as \"contactCompany\",xuu.contact_company_id as \"contactCompanyId\", case when xul.associated_company_id is not null then true else false end as \"companyContactList\" \n"
					+ "from xt_user_userlist xuu,xt_user_list xul,xt_user_profile xup\n"
					+ "where xul.user_list_id  = xuu.user_list_id and xul.company_id  = :companyId and xuu.user_id  = :userId\n"
					+ "and xup.user_id  = xuu.user_id and xuu.contact_company_id  is not null\n";

			Query query = session.createSQLQuery(queryString).setParameter(XamplifyConstants.COMPANY_ID, companyId)
					.setParameter(XamplifyConstants.USER_ID, userId);

			userUserListDtos = (List<UserUserListDTO>) paginationUtil.getListDTO(UserUserListDTO.class, query);

			session.flush();
			session.clear();

		} catch (HibernateException e) {
			e.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			session.close();
		}
		return userUserListDtos;
	}

	private boolean doesUserExistsInUserUserList(Integer userListId, Integer userId) {
		Session session = sessionFactory.openSession();
		boolean result = false;
		try {
			String queryString = "select case when count(*) > 0 then true else false end from xt_user_userlist where user_list_id = :userListId and user_id = :userId";
			Query query = session.createSQLQuery(queryString).setParameter(XamplifyConstants.USER_LIST_ID, userListId)
					.setParameter("userId", userId);
			result = (boolean) query.uniqueResult();
			session.flush();
			session.clear();
		} catch (HibernateException e) {
			e.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			session.close();
		}
		return result;
	}

	private void setUserUserListPropertiesAndSave(UserList userList, ContactFieldsDTO contactFieldsDTO,
			Session contactListUsersSession, Integer userId) {
		Integer contactCompanyId = contactFieldsDTO.getContactCompanyId();

		UserUserList userUserList = setUserUserListProperties(userList, contactFieldsDTO, userId, contactCompanyId);

		contactListUsersSession.save(userUserList);
	}

	private UserUserList setUserUserListProperties(UserList userList, ContactFieldsDTO contactFieldsDTO, Integer userId,
			Integer contactCompanyId) {
		UserUserList userUserList = new UserUserList();
		User user = new User();
		user.setUserId(userId);
		userUserList.setUser(user);
		userUserList.setUserList(userList);
		if (XamplifyUtils.isValidInteger(contactCompanyId)) {
			Company company = new Company();
			company.setId(contactCompanyId);
			userUserList.setCompany(company);
		}
		userUserList.setCountry(contactFieldsDTO.getCountry());
		userUserList.setCity(contactFieldsDTO.getCity());
		userUserList.setAddress(contactFieldsDTO.getAddress());
		userUserList.setContactCompany(contactFieldsDTO.getContactCompany());
		userUserList.setJobTitle(contactFieldsDTO.getJobTitle());
		userUserList.setFirstName(contactFieldsDTO.getFirstName());
		userUserList.setLastName(contactFieldsDTO.getLastName());
		userUserList.setMobileNumber(contactFieldsDTO.getMobileNumber());
		userUserList.setState(contactFieldsDTO.getState());
		userUserList.setZipCode(contactFieldsDTO.getZipCode());
		List<LegalBasis> legalBasisList = new ArrayList<>();
		List<Integer> legalBasisIds = contactFieldsDTO.getLegalBasis();
		for (Integer legalBasisId : legalBasisIds) {
			LegalBasis legalBasis = new LegalBasis();
			legalBasis.setId(legalBasisId);
			legalBasisList.add(legalBasis);
		}
		userUserList.setLegalBasis(legalBasisList);
		userUserList.setCountryCode(contactFieldsDTO.getCountryCode());
		Integer contactStatusId = XamplifyUtils.isValidInteger(contactFieldsDTO.getContactStatusId())
				? contactFieldsDTO.getContactStatusId()
				: findContactStatusStageId(contactFieldsDTO.getLoggedInUserId());
		userUserList.setContactStatusId(contactStatusId);
		Set<UserListFlexiField> userListFlexiFields = setFlexiFieldsProperties(contactFieldsDTO, userUserList);
		userUserList.setUserListFlexiFields(userListFlexiFields);

		return userUserList;
	}

	private Set<UserListFlexiField> setFlexiFieldsProperties(ContactFieldsDTO contactFieldsDTO,
			UserUserList userUserList) {
		List<FlexiFieldRequestDTO> flexiFieldRequestDTOs = contactFieldsDTO.getFlexiFields();
		Set<UserListFlexiField> userListFlexiFields = new HashSet<>();
		if (XamplifyUtils.isNotEmptyList(flexiFieldRequestDTOs)) {
			for (FlexiFieldRequestDTO flexiFieldRequestDTO : flexiFieldRequestDTOs) {
				if (XamplifyUtils.isValidString(flexiFieldRequestDTO.getFieldValue())) {
					UserListFlexiField userListFlexiField = new UserListFlexiField();
					userListFlexiField.setUserUserList(userUserList);
					FlexiField flexiField = new FlexiField();
					flexiField.setId(flexiFieldRequestDTO.getId());
					userListFlexiField.setFlexiField(flexiField);
					userListFlexiField.setFlexiFieldValue(flexiFieldRequestDTO.getFieldValue());
					userListFlexiField.setCreatedTime(new Date());
					userListFlexiFields.add(userListFlexiField);
				}
			}
		}
		return userListFlexiFields;
	}

	private void saveContactCompanyUserUserList(ContactFieldsDTO contactFieldsDTO, Integer userId,
			Integer contactCompanyId, Integer existingContactCompanyUserListId) {

		boolean isUserExistsInCompanyUserUserList = doesUserExistsInUserUserList(existingContactCompanyUserListId,
				userId);
		if (!isUserExistsInCompanyUserUserList) {
			Session contactCompanyUserUserListSession = sessionFactory.openSession();
			try {
				UserList userList = new UserList();
				userList.setId(existingContactCompanyUserListId);

				UserUserList userUserList = setUserUserListProperties(userList, contactFieldsDTO, userId,
						contactCompanyId);

				contactCompanyUserUserListSession.save(userUserList);

				contactCompanyUserUserListSession.flush();
				contactCompanyUserUserListSession.clear();
			} catch (ConstraintViolationException constraintViolationException) {
				constraintViolationException.printStackTrace();
			} catch (HibernateException hibernateException) {
				hibernateException.printStackTrace();
			} catch (DataIntegrityViolationException dataIntegrityViolationException) {
				dataIntegrityViolationException.printStackTrace();
			} finally {
				contactCompanyUserUserListSession.close();
			}
		}
	}

	private Integer saveContactCompanyUserList(UserList userList, ContactFieldsDTO contactFieldsDTO, Integer companyId,
			Integer contactCompanyId) {
		UserList contactCompanyUserList = new UserList();

		Integer existingContactCompanyUserListId = getExisitingCompanyContactUserListId(companyId, contactCompanyId);

		boolean isCompanyContactUserListExists = XamplifyUtils.isValidInteger(existingContactCompanyUserListId);
		if (!isCompanyContactUserListExists) {
			boolean hasError = false;
			Session contactCompanyUserListSession = sessionFactory.openSession();
			try {
				String companyContactListName = contactFieldsDTO.getContactCompany() + "  - Company List";
				Company companyContact = new Company();
				companyContact.setId(contactCompanyId);
				contactCompanyUserList.setAssociatedCompany(companyContact);
				contactCompanyUserList.setName(companyContactListName);
				contactCompanyUserList.setCreatedTime(new Date());
				contactCompanyUserList.setOwner(userList.getOwner());
				contactCompanyUserList.setUploadedDate(new Date());
				contactCompanyUserList.setSocialNetwork(SocialNetwork.MANUAL);
				contactCompanyUserList.setContactType(TYPE.CONTACT);
				contactCompanyUserList.setCompany(userList.getCompany());
				contactCompanyUserList.setPublicList(true);
				contactCompanyUserList.setModuleName("CONTACTS");
				contactCompanyUserList.setUpdatedBy(userList.getUpdatedBy());
				contactCompanyUserList.setUpdatedTime(new Date());
				contactCompanyUserList.setPartnerUserList(false);
				contactCompanyUserList.setUploadInProgress(true);
				existingContactCompanyUserListId = (Integer) contactCompanyUserListSession.save(contactCompanyUserList);
				contactCompanyUserListSession.flush();
				contactCompanyUserListSession.clear();
			} catch (ConstraintViolationException constraintViolationException) {
				hasError = true;
			} catch (HibernateException hibernateException) {
				hibernateException.printStackTrace();
			} catch (DataIntegrityViolationException dataIntegrityViolationException) {
				dataIntegrityViolationException.printStackTrace();
			} finally {
				if (hasError) {
					existingContactCompanyUserListId = getExisitingCompanyContactUserListId(companyId,
							contactCompanyId);
				}
				contactCompanyUserListSession.close();
			}
		}
		return existingContactCompanyUserListId;
	}

	private Integer saveContactAsUser(ContactFieldsDTO contactFieldsDTO, ContactsRequestDTO contactsRequestDTO) {
		String emailId = contactFieldsDTO.getEmailId();
		Integer userId = 0;
		UserDTO existingUser = getEmailIdAndEmailValidationIndStatusByEmailId(emailId);
		boolean isUserExists = existingUser != null && XamplifyUtils.isValidInteger(existingUser.getId());
		if (!isUserExists) {
			Session contactSession = sessionFactory.openSession();
			User contact = new User();
			contact.setEmailId(emailId);
			contact.setFirstName(contactFieldsDTO.getFirstName());
			contact.setLastName(contactFieldsDTO.getLastName());
			contact.setAlias(XamplifyUtils.generateAlias());
			contact.setUserStatus(UserStatus.UNAPPROVED);
			contact.setUserDefaultPage(UserDefaultPage.WELCOME);
			contact.setModulesDisplayType(ModulesDisplayType.LIST);
			contact.setCreatedTime(new Date());
			contact.setUpdatedTime(contact.getCreatedTime());
			contact.setUpdatedBy(contactsRequestDTO.getLoggedInUserId());
			contact.setUserName(contact.getEmailId());
			contact.getRoles().add(Role.USER_ROLE);
			boolean hasError = false;
			try {
				userId = (Integer) contactSession.save(contact);
				contactSession.flush();
				contactSession.clear();
			} catch (ConstraintViolationException constraintViolationException) {
				hasError = true;
				String errorMessage = " ConstraintViolationException ";
				logger.info(errorMessage);
			} catch (HibernateException hibernateException) {
				String errorMessage = " HibernateException ";
				logger.info(errorMessage);
				hibernateException.printStackTrace();
			} catch (DataIntegrityViolationException dataIntegrityViolationException) {
				String errorMessage = "  DataIntegrityViolationException ";
				logger.debug(errorMessage);
			} finally {
				if (hasError) {
					userId = userDao.getUserIdByEmail(emailId);
				}
				contactSession.close();
				validateEmailIdUsingZeroBounceAPI(emailId);
			}
		} else {
			userId = existingUser.getId();
			if (!existingUser.isValidEmail()) {
				String revalidatingEmailAddressDebugMessage = "Re-validating " + emailId + " using zerobounce API at "
						+ new Date();
				logger.debug(revalidatingEmailAddressDebugMessage);
				validateEmailIdUsingZeroBounceAPI(emailId);
			}
		}
		contactFieldsDTO.setUserId(userId);
		return userId;
	}

	@SuppressWarnings("unused")
	private Integer getUserId(Session contactSession, String emailId) {
		Query getUserIdQuery = contactSession.createSQLQuery(
				"select user_id from xt_user_profile where LOWER(TRIM(email_id)) = LOWER(TRIM(:emailId))");
		getUserIdQuery.setParameter(XamplifyConstants.EMAIL_ID, emailId);
		return (Integer) getUserIdQuery.uniqueResult();
	}

	@Override
	public void updateUserListStatusAsCompleted(List<Integer> allUserListIdsArrayList) {
		String queryString = "update xt_user_list set upload_in_progress = false,validation_in_progress = false,email_validation_ind = true where user_list_id in (:ids)";
		hibernateSQLQueryResultUtilDao.updateChunkedIds(queryString, allUserListIdsArrayList);
	}

	@Override
	public Integer getContactCompanyIdByNameAndCompanyId(String contactCompanyName, Integer companyId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(
				"select id from xt_company where company_id = :companyId and lower(trim(name)) = lower(trim(':contactCompanyName'))");
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("contactCompanyName", contactCompanyName));
		hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
		return companyId;
	}

	@Override
	public Integer getDefaultMasterContactListIdByCompanyId(Integer companyId) {
		String queryString = "select distinct user_list_id from xt_user_list where company_id = :companyId and cast(contact_list_type as text)  = :contactListType";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("contactListType", ContactListTypeValue.DEFAULT_CONTACT_LIST.name()));
		return (Integer) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public ProcessingUserListsDTO getUserListNameById(Integer userListId) {
		String queryString = "select distinct xul.user_list_id as \"userListId\",cast(count(distinct xuul.user_id) as int) as \"usersCount\",\r\n"
				+ "TRIM(xul.user_list_name) as \"userListName\" from xt_user_list xul \r\n"
				+ "left join xt_user_userlist xuul on xuul.user_list_id = xul.user_list_id\r\n"
				+ "where xul.user_list_id = :userListId group by 1\r\n" + "";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.USER_LIST_ID, userListId));
		return (ProcessingUserListsDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO,
				ProcessingUserListsDTO.class);
	}

	@Override
	public void updateContactList(Integer userListId, Integer loggedInUserId, String csvFilePath) {
		String queryString = "update xt_user_list set updated_time = :updatedTime,uploaded_date = :updatedTime, updated_by = :updatedBy,upload_in_progress = :uploadInProgress,csv_path = :csvFilePath where user_list_id = :userListId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("updatedTime", new Date()));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.USER_LIST_ID, userListId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("uploadInProgress", true));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("csvFilePath", csvFilePath));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("updatedBy", loggedInUserId));
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);

	}

	private void validateEmailIdUsingZeroBounceAPI(String emailId) {
		if (xamplifyUtil.checkIsZeroBounceApiEnabled()) {
			/*** Validate Email Address Using Zero Bounce API ********/
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			try {
				String targetURL = zeroBounceApiUrl + "?api_key=" + zeroBounceApiAccessKey + "&email=" + emailId
						+ "&ip_address=";
				JSONObject jsonObject = executeZeroBounceRestAPI(emailId, httpClient, targetURL);

				if (jsonObject.has(XamplifyConstants.ERROR) && jsonObject.getString(XamplifyConstants.ERROR)
						.equalsIgnoreCase("Invalid API Key or your account ran out of credits")) {
					logger.debug(jsonObject.getString(XamplifyConstants.ERROR));
					updateEmailAddressStatus(emailId, VALID, true, true);
				} else if (jsonObject.has(XamplifyConstants.ERROR)) {
					updateEmailAddressStatus(emailId, VALID, true, true);
				} else {
					String status = jsonObject.getString("status");
					boolean isEmailValid = status.equalsIgnoreCase(VALID) || status.equalsIgnoreCase("catch-all");
					updateEmailAddressStatus(emailId, status, true, isEmailValid);
				}

			} catch (Exception e) {
				// Do Nothing
				updateEmailAddressStatus(emailId, VALID, true, true);
			}
		} else {
			updateEmailAddressStatus(emailId, VALID, true, true);
		}
	}

	private void updateEmailAddressStatus(String emailId, String emailCategory, boolean emailValidationInd,
			boolean isEmailValid) {
		Session session = sessionFactory.openSession();
		try {
			Query query = session.createSQLQuery(
					"update xt_user_profile set is_email_valid = :isEmailValid,email_validation_ind=:emailValidationInd, email_category = :emailCategory where LOWER(TRIM(email_id)) = :emailId");
			query.setParameter("isEmailValid", isEmailValid);
			query.setParameter("emailValidationInd", emailValidationInd);
			query.setParameter("emailCategory", emailCategory);
			query.setParameter(EMAIL_ID, emailId);
			query.executeUpdate();
			session.flush();
			session.clear();
		} catch (ConstraintViolationException constraintViolationException) {
			String errorMessage = "ConstraintViolationException updateEmailAddressStatus(" + emailId + ") ";
			logger.info(errorMessage);
		} catch (HibernateException hibernateException) {
			String errorMessage = "HibernateException updateEmailAddressStatus(" + emailId + ")";
			logger.info(errorMessage);
			hibernateException.printStackTrace();
		} catch (DataIntegrityViolationException dataIntegrityViolationException) {
			String errorMessage = "DataIntegrityViolationException updateEmailAddressStatus(" + emailId + ")";
			logger.debug(errorMessage);
		} finally {
			session.close();
		}
	}

	private JSONObject executeZeroBounceRestAPI(String emailId, CloseableHttpClient httpClient, String targetURL)
			throws IOException {
		JSONObject jsonObject = new JSONObject();
		HttpGet getRequest = null;
		try {
			getRequest = new HttpGet(targetURL);
			HttpResponse response = httpClient.execute(getRequest);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				String debugMessage = emailId + " validated successfully using zerobounce api";
				logger.debug(debugMessage);
				HttpEntity entity = response.getEntity();
				String responseString = EntityUtils.toString(entity);
				jsonObject = new JSONObject(responseString);
			} else {
				jsonObject = new JSONObject();
				HttpEntity entity = response.getEntity();
				String responseString = EntityUtils.toString(entity);
				String errorMessage = emailId + " could not proceesed. " + responseString
						+ ".This email can be overlooked. The application is running smoothly";
				logger.error(errorMessage);
			}

		} catch (Exception ex) {
			jsonObject = new JSONObject();
			jsonObject.put(XamplifyConstants.ERROR, ex.getMessage());
			String errorMessage = "Exception occured for " + emailId
					+ " validation.This email can be overlooked. The application is running smoothly";
			logger.error(errorMessage);
		} finally {
			httpClient.close();
		}
		return jsonObject;
	}

	@Override
	public UserDTO getEmailIdAndEmailValidationIndStatusByEmailId(String emailId) {
		Session session = sessionFactory.openSession();
		UserDTO userDto = null;
		try {
			SQLQuery query = session.createSQLQuery(
					"select user_id as \"id\", email_validation_ind as \"validEmail\"  from xt_user_profile where LOWER(email_id)=LOWER(:emailId) order by user_id asc limit 1 ");
			query.setParameter(EMAIL_ID, emailId);
			userDto = (UserDTO) query.setResultTransformer(Transformers.aliasToBean(UserDTO.class)).uniqueResult();
			session.flush();
			session.clear();
		} catch (Exception e) {
			// Do Nothing
		} finally {
			session.close();
		}
		return userDto;
	}

	private Integer findContactStatusStageId(Integer userId) {
		Session session = sessionFactory.openSession();
		Integer stageId = null;
		try {
			stageId = findContactStagesList(userId, session);
			if (!XamplifyUtils.isValidInteger(stageId)) {
				stageId = findContactStagesList(1, session);
			}
			session.flush();
			session.clear();
		} catch (Exception e) {
			// Do Nothing
		} finally {
			session.close();
		}
		return stageId;
	}

	private Integer findContactStagesList(Integer userId, Session session) {
		SQLQuery query = session.createSQLQuery(
				"select id from xt_contact_status where is_default = :defaultStage "
				+ "and company_id = (select company_id from xt_user_profile where user_id = :userId)");
		query.setParameter("defaultStage", true);
		query.setParameter(XamplifyConstants.USER_ID, userId);
		return (Integer) query.uniqueResult();
	}

	@Override
	public UserUserList findUserUserListsByUserIdAndUserList(Integer userId, Integer userListId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(UserUserList.class, "UUL");
		criteria.createAlias("UUL.userList", "UL", JoinType.INNER_JOIN);
		criteria.add(Restrictions.eq("UUL.user.userId", userId));
		criteria.add(Restrictions.eq("UL.id", userListId));
		return (UserUserList) criteria.uniqueResult();
	}

	@Override
	public UserUserList findCompanyUserUserList(Integer userId, Integer vendorCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(UserUserList.class, "UUL");
		criteria.createAlias("UUL.userList", "UL", JoinType.INNER_JOIN);
		criteria.add(Restrictions.eq("UUL.user.userId", userId));
		criteria.add(Restrictions.eq("UL.company.id", vendorCompanyId));
		criteria.add(Restrictions.isNotNull("UL.associatedCompany.id"));
		criteria.add(Restrictions.eq("UL.moduleName", "CONTACTS"));
		return (UserUserList) criteria.uniqueResult();
	}

	public void updateUserUserListFields(String contactCompanyName, Integer contactCompanyId, Integer vendorCompanyId,
			Integer userId) {
		if (!XamplifyUtils.isValidInteger(contactCompanyId)) {
	        return;
	    }
		String queryString = "UPDATE xt_user_userlist xuul SET contact_company = :contactCompany,\n"
				+ "contact_company_id = :contactCompanyId FROM xt_user_list xul\n"
				+ "WHERE xuul.user_list_id = xul.user_list_id AND xul.company_id = :companyId AND xuul.user_id = :userId";
		HibernateSQLQueryResultRequestDTO request = new HibernateSQLQueryResultRequestDTO();
		request.setQueryString(queryString);
		request.getQueryParameterDTOs().add(new QueryParameterDTO(CONTACT_COMPANY, contactCompanyName));
		request.getQueryParameterDTOs().add(new QueryParameterDTO("contactCompanyId", contactCompanyId));
		request.getQueryParameterDTOs().add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, vendorCompanyId));
		request.getQueryParameterDTOs().add(new QueryParameterDTO(XamplifyConstants.USER_ID, userId));
		hibernateSQLQueryResultUtilDao.update(request);
	}

	@Override
	public void deleteCompanyUserUserList(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "delete from xt_user_userlist where id = :id";
		Query query = session.createSQLQuery(queryString);
		query.setParameter("id", id);
		query.executeUpdate();
	}

	@Override
	public Integer findCompanyUserUserListId(Integer associatedCompanyId) {
		String queryString = "select user_list_id from xt_user_list where associated_company_id = :companyId";
		HibernateSQLQueryResultRequestDTO request = new HibernateSQLQueryResultRequestDTO();
		request.setQueryString(queryString);
		request.getQueryParameterDTOs().add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, associatedCompanyId));
		return (Integer) hibernateSQLQueryResultUtilDao.getUniqueResult(request);
	}

	@Override
	public Map<String, Object> findUserListContacts(Pagination pagination) {
		String moduleName = pagination.getModuleName();
		String contactType = pagination.getType().toUpperCase();
		if (pagination.isExportToExcel()) {
			pagination.setExcludeLimit(true);
		}
		String filterQueryString = addFilterQuery(pagination);
		String sortQueryString = addSortColumns(pagination);
		String baseQuery = PARTNERS.equalsIgnoreCase(moduleName) ? partnerDetailsQuery : contactDetailsQuery;
		String finalQueryString = baseQuery + filterQueryString;
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		if (pagination.isPartnerTeamMemberGroupFilter() && PARTNERS.equalsIgnoreCase(moduleName)) {
			finalQueryString = finalQueryString.replace("teamMemberSql", teamMemberSql);
			finalQueryString = finalQueryString.replace("tp.partner_id=up.user_id", "tp.partner_id=xup.user_id");
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("teamMemberId", pagination.getUserId()));
		} else {
			finalQueryString = finalQueryString.replace("teamMemberSql", "");
		}
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.USER_LIST_ID, pagination.getUserListId()));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, pagination.getCompanyId()));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.TYPE, contactType));
		hibernateSQLQueryResultRequestDTO.setQueryString(finalQueryString);
		hibernateSQLQueryResultRequestDTO.setSortQueryString(sortQueryString);
		hibernateSQLQueryResultRequestDTO.setClassInstance(UserDTO.class);
		List<String> searchColumns = new ArrayList<>();
		searchColumns.add(XUPEMAILID);
		searchColumns.add("xuul.lastname");
		searchColumns.add("xuul.firstname");
		searchColumns.add(XUULCONTACTCOMPANY);
		if (XamplifyUtils.CONTACTS.equalsIgnoreCase(moduleName)) {
			searchColumns.add("LOWER(xcs.stage_name)");
		}
		searchColumns.add("LOWER(xcp.company_name)");
		hibernateSQLQueryResultRequestDTO.setSearchColumns(searchColumns);
		return hibernateSQLQueryResultUtilDao.returnPaginatedDTOList(hibernateSQLQueryResultRequestDTO, pagination,
				pagination.getSearchKey());
	}

	private String addFilterQuery(Pagination pagination) {
		StringBuilder filterQuery = new StringBuilder();
		Criteria[] criterias = pagination.getCriterias();
		if (ArrayUtils.isNotEmpty(criterias) && !"Field Name*".equalsIgnoreCase(criterias[0].getProperty())) {
			Map<String, String> fieldToColumnMap = new HashMap<>();
			fieldToColumnMap.put(EMAIL_ID, XUPEMAILID);
			fieldToColumnMap.put(CONTACT_COMPANY, XUULCONTACTCOMPANY);
			fieldToColumnMap.put("firstName", "xuul.firstname");
			fieldToColumnMap.put("lastName", "xuul.lastname");
			fieldToColumnMap.put("Account Name", "xuul.account_name");
			fieldToColumnMap.put("Account Owner", "xuul.account_owner");
			fieldToColumnMap.put("Account Sub Type", "xuul.account_sub_type");
			fieldToColumnMap.put("Company Domain", "xuul.company_domain");
			fieldToColumnMap.put("jobTitle", "xuul.job_title");
			fieldToColumnMap.put("Website", "xuul.website");
			fieldToColumnMap.put("Territory", "xuul.territory");
			fieldToColumnMap.put("Vertical", "xuul.vertical");
			fieldToColumnMap.put("Region", "xuul.region");
			fieldToColumnMap.put("type", "xuul.partner_type");
			fieldToColumnMap.put("Category", "xuul.category");
			fieldToColumnMap.put("description", "xuul.description");
			fieldToColumnMap.put("city", "xuul.city");
			fieldToColumnMap.put("State", "xuul.state");
			fieldToColumnMap.put("Zip Code", "xuul.zip");
			fieldToColumnMap.put("country", "xuul.country");
			fieldToColumnMap.put("mobileNumber", "xuul.mobile_number");
			fieldToColumnMap.put("Contact Status", "xcs.stage_name");

			filterQuery.append(" AND( ");
			boolean firstCondition = true;
			for (int i = 0; i < criterias.length; i++) {
				Criteria criteria = criterias[i];
				criteria.setOperationName(Criteria.getOperationNameEnum(criteria.getOperation()));
				String fieldName = criteria.getProperty();
				String columnName = fieldToColumnMap.get(fieldName);
				String value = criteria.getValue1().toString().toLowerCase();
				if (columnName == null || "Field Name*".equalsIgnoreCase(fieldName)) {
					continue;
				}

				if (!firstCondition) {
					filterQuery.append(" or ");
				}

				if (criteria.getOperationName() == OPERATION_NAME.eq) {
					filterQuery.append("lower(").append(columnName).append(") = '").append(value).append("'");
				} else if (criteria.getOperationName() == OPERATION_NAME.like) {
					filterQuery.append(columnName).append(" ilike '%").append(value).append("%'");
				}
				firstCondition = false;
			}
			filterQuery.append(" ) ");
		}
		utilService.setDateFilters(pagination);
		filterQuery.append(XamplifyUtils.frameDateFilterQuery(pagination, "xps.created_time"));
		return String.valueOf(filterQuery);
	}

	private String addSortColumns(Pagination pagination) {
		List<SortColumnDTO> sortColumnDTOs = new ArrayList<>();
		SortColumnDTO emailSortOption = new SortColumnDTO(XamplifyConstants.EMAIL_ID, XUPEMAILID, false, true, false);
		SortColumnDTO firstNameSortOption = new SortColumnDTO("firstName", "xuul.firstName", false, true, false);
		SortColumnDTO lastNameSortOption = new SortColumnDTO("lastName", "xuul.lastName", false, true, false);
		SortColumnDTO companySortOption = new SortColumnDTO(CONTACT_COMPANY, XUULCONTACTCOMPANY, false, true, false);
		SortColumnDTO verticalTimeSortOption = new SortColumnDTO("vertical", "xuul.vertical", false, true, false);
		SortColumnDTO regionSortOption = new SortColumnDTO("region", "xuul.region", false, true, false);
		SortColumnDTO typeSortOption = new SortColumnDTO("partnerType", "xuul.partner_type", false, true, false);
		SortColumnDTO idSortOption = new SortColumnDTO("id", "xuul.user_list_id", false, true, false);
		SortColumnDTO categorySortOption = new SortColumnDTO("category", "xuul.category", false, true, false);
		String defaultOrder = PARTNERS.equalsIgnoreCase(pagination.getModuleName()) ? "xps.created_time" : "xup.created_time";
			SortColumnDTO createdTimeSortOption = new SortColumnDTO(XamplifyConstants.CREATED_TIME, defaultOrder,
					true, true, false);
		sortColumnDTOs.add(emailSortOption);
		sortColumnDTOs.add(firstNameSortOption);
		sortColumnDTOs.add(lastNameSortOption);
		sortColumnDTOs.add(companySortOption);
		sortColumnDTOs.add(verticalTimeSortOption);
		sortColumnDTOs.add(regionSortOption);
		sortColumnDTOs.add(typeSortOption);
		sortColumnDTOs.add(idSortOption);
		sortColumnDTOs.add(categorySortOption);
		sortColumnDTOs.add(createdTimeSortOption);
		return paginationUtil.generateSortQuery(pagination, sortColumnDTOs, "desc");
	}

	@Override
	public Integer findUserListContactsCount(Integer companyId, Integer userListId, String contactType) {
		String queryString = contactsCountQuery;
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.USER_LIST_ID, userListId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.TYPE, contactType));
		return (Integer) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

}
