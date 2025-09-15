package com.xtremand.dao.util;

import java.io.Serializable;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.CompanyProfile.CompanyNameStatus;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.partnership.bom.Partnership;
import com.xtremand.partnership.bom.Partnership.PartnershipSource;
import com.xtremand.partnership.bom.Partnership.PartnershipStatus;
import com.xtremand.user.bom.ModulesDisplayType;
import com.xtremand.user.bom.Role;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.User.UserDefaultPage;
import com.xtremand.user.bom.User.UserStatus;
import com.xtremand.user.bom.UserList;
import com.xtremand.user.bom.UserUserList;
import com.xtremand.util.GenerateRandomPassword;

@Repository
@Transactional
public class GenericDAO {

	private static final Logger logger = LoggerFactory.getLogger(GenericDAO.class);

	@Autowired
	SessionFactory sessionFactory;

	public <T> T get(Class<T> clazz, Serializable pk) {
		return sessionFactory.getCurrentSession().get(clazz, pk);
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> load(Class<T> clazz) {
		return sessionFactory.getCurrentSession().createCriteria(clazz).setCacheable(true).list();
	}

	public Integer save(Object clazz) {
		Session session;
		try {
			session = sessionFactory.getCurrentSession();
		} catch (HibernateException e) {
			session = sessionFactory.openSession();
		}
		return (Integer) session.save(clazz);
	}

	public void saveObject(Object clazz) {
		Session session = null;
		Transaction transaction = null;
		try {
			session = sessionFactory.openSession();
			transaction = session.beginTransaction();
			session.save(clazz);
			transaction.commit();
		} catch (HibernateException e) {
			transaction.rollback();
		} finally {
			session.close();
		}
	}

	public void update(Object clazz) {
		sessionFactory.getCurrentSession().update(clazz);
	}

	public void saveOrUpdate(Object clazz) {
		sessionFactory.getCurrentSession().saveOrUpdate(clazz);
	}

	public void flushCurrentSession() {
		sessionFactory.getCurrentSession().flush();

	}

	public <T> void remove(T entity) {
		sessionFactory.getCurrentSession().delete(entity);
		flushCurrentSession();
	}

	public <T> void merge(T entity) {
		sessionFactory.getCurrentSession().merge(entity);
		flushCurrentSession();
	}

	public Integer getCount(String queryString) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(queryString);
		return ((BigInteger) query.uniqueResult()).intValue();
	}

	public void saveContacts(UserList userList, Integer limit, Integer userId, String type) {
		Session session = sessionFactory.getCurrentSession();
		String emailSuffix = new SimpleDateFormat("ddMMMMyyyy").format(new Date());
		for (int i = 0; i < limit; i++) {
			String emailId = type + "_" + System.currentTimeMillis() + "_" + i + "@email.com";
			User contact = new User();
			contact.setEmailId(emailId.toLowerCase());
			contact.setEmailValid(true);
			contact.setEmailValidationInd(true);
			contact.setUserStatus(UserStatus.UNAPPROVED);
			contact.setModulesDisplayType(ModulesDisplayType.LIST);
			contact.getRoles().add(Role.USER_ROLE);
			contact.setCreatedTime(new Date());
			contact.setUpdatedTime(new Date());
			contact.setUpdatedBy(userId);
			GenerateRandomPassword password = new GenerateRandomPassword();
			contact.setAlias(password.getPassword());
			UserUserList userUserList = new UserUserList();
			userUserList.setUser(contact);
			userUserList.setUserList(userList);
			contact.getUserUserLists().add(userUserList);
			userList.getUserUserLists().add(userUserList);
			CompanyProfile companyProfile = new CompanyProfile();
			companyProfile.setCompanyName("Test Partner Company - " + emailSuffix);
			companyProfile.setCompanyNameStatus(CompanyNameStatus.INACTIVE);
			contact.setCompanyProfile(companyProfile);
			session.save(contact);
			if ("partner".equals(type)) {
				Partnership partnership = new Partnership();
				partnership.setRepresentingPartner(contact);
				User representingVendor = new User();
				representingVendor.setUserId(userId);
				partnership.setRepresentingVendor(representingVendor);
				partnership.setVendorCompany(userList.getCompany());
				partnership.setSource(PartnershipSource.ONBOARD);
				partnership.setStatus(PartnershipStatus.APPROVED);
				partnership.setCreatedBy(userId);
				partnership.setUpdatedBy(userId);
				partnership.setCreatedTime(new Date());
				partnership.setUpdatedTime(new Date());
				partnership.setContactsLimit(1);
				session.save(partnership);
			}

			if (i % 30 == 0) {
				session.flush();
				session.clear();
			}
			System.err.println(i + ":::::::" + contact.getUserId() + " added");
		}
	}

	public Integer getUserIdByEmail(String emailId) {
		Session session = sessionFactory.getCurrentSession();
		SQLQuery query = session.createSQLQuery("select user_id from xt_user_profile where email_id=:emailId");
		query.setParameter("emailId", emailId);
		return (Integer) query.uniqueResult();
	}

	public void createUsersInUserList(UserList userList, Set<UserDTO> userDTOs, Integer loggedInUserId,
			boolean isGdprOn) {
		Session session = sessionFactory.getCurrentSession();
		int counter = 0;
		for (UserDTO userDTO : userDTOs) {
			Integer userId = getUserIdByEmail(userDTO.getEmailId().trim().toLowerCase());
			User user = new User();
			if (userId == null || userId <= 0) {
				user.setEmailId(userDTO.getEmailId().trim().toLowerCase());
				user.setUserName(userDTO.getEmailId().trim());
				user.setUserDefaultPage(UserDefaultPage.WELCOME);
				user.setUserStatus(UserStatus.UNAPPROVED);
				user.setModulesDisplayType(ModulesDisplayType.LIST);
				user.getRoles().add(Role.USER_ROLE);
				GenerateRandomPassword randomPassword = new GenerateRandomPassword();
				user.setAlias(randomPassword.getPassword());
				user.initialiseCommonFields(true, loggedInUserId);
				session.save(user);
			} else {
				user.setUserId(userId);
			}

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
			session.save(userUserList);

			if (counter % 30 == 0) {
				session.flush();
				session.clear();
			}
			counter++;
		}
	}

	public void createUsersInUserList1(UserList userList, Set<UserDTO> userDTOs, Integer loggedInUserId,
			boolean isGdprOn) {
		Session session = sessionFactory.getCurrentSession();
		int counter = 0;
		for (UserDTO userDTO : userDTOs) {
			User user = new User();
			user.setEmailId(userDTO.getEmailId().trim().toLowerCase());
			user.setUserName(userDTO.getEmailId().trim());
			user.setUserDefaultPage(UserDefaultPage.WELCOME);
			user.setUserStatus(UserStatus.UNAPPROVED);
			user.setModulesDisplayType(ModulesDisplayType.LIST);
			user.getRoles().add(Role.USER_ROLE);
			GenerateRandomPassword randomPassword = new GenerateRandomPassword();
			user.setAlias(randomPassword.getPassword());
			user.initialiseCommonFields(true, loggedInUserId);

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
			user.getUserUserLists().add(userUserList);
			session.save(user);

			if (counter % 30 == 0) {
				session.flush();
				session.clear();
			}
			counter++;
		}
	}

	public void clearCurrentSession() {
		sessionFactory.getCurrentSession().clear();

	}

	public <T> void saveAll(List<T> list, String moduleName) {
		try {
			int total = list.size();
			int count = 1;
			String prefixMessage = StringUtils.hasText(moduleName) ? moduleName : " Records ";
			String totalRecordsDebugMessage = "Total " + prefixMessage + " To Be Inserted : " + total;
			logger.debug(totalRecordsDebugMessage);
			Session session = sessionFactory.getCurrentSession();
			int size = list.size();
			for (int i = 0; i < size; i++) {
				String debugMessage = count + "/" + total + " " + moduleName + " inserted.";
				logger.debug(debugMessage);
				session.save(list.get(i));
				if (i % 30 == 0) {
					session.flush();
					session.clear();
				}
				count++;
			}
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e.getMessage());
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex.getMessage());
		}

	}

}