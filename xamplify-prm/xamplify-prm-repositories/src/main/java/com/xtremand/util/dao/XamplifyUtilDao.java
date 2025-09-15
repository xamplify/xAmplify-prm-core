package com.xtremand.util.dao;

import java.util.Date;
import java.util.HashSet;
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
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.CompanyProfile.CompanyNameStatus;
import com.xtremand.common.bom.PartnerTeamMemberViewType;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.partner.bom.PartnershipStatusHistory;
import com.xtremand.partnership.bom.Partnership;
import com.xtremand.partnership.bom.Partnership.PartnershipSource;
import com.xtremand.partnership.bom.Partnership.PartnershipStatus;
import com.xtremand.team.member.dto.TeamMemberModuleDTO;
import com.xtremand.team.member.group.bom.TeamMemberGroup;
import com.xtremand.team.member.group.bom.TeamMemberGroupRoleMapping;
import com.xtremand.user.bom.LegalBasis;
import com.xtremand.user.bom.ModulesDisplayType;
import com.xtremand.user.bom.Role;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.User.UserDefaultPage;
import com.xtremand.user.bom.User.UserStatus;
import com.xtremand.user.bom.UserList;
import com.xtremand.user.bom.UserUserList;
import com.xtremand.util.GenerateRandomPassword;
import com.xtremand.util.XamplifyUtils;

@Repository
public class XamplifyUtilDao {

	private static final Logger logger = LoggerFactory.getLogger(XamplifyUtilDao.class);

	@Autowired
	private SessionFactory sessionFactory;

	public <T> void saveAll(List<T> list, String moduleName) {
		int total = list.size();
		int count = 1;
		String prefixMessage = StringUtils.hasText(moduleName) ? moduleName : " Records ";
		String totalRecordsDebugMessage = "Total " + prefixMessage + " : " + total;
		logger.debug(totalRecordsDebugMessage);
		for (int i = 0; i < total; i++) {
			String debugMessage = count + "/" + total + " " + moduleName + " inserted.";
			logger.debug(debugMessage);
			Session session = sessionFactory.openSession();
			try {
				Integer id = (Integer) session.save(list.get(i));
				session.flush();
				session.clear();
				String rowInsertedSuccessfullyDebugMessage = "Row Added Successfully Into " + moduleName + " With Id : "
						+ id;
				logger.debug(rowInsertedSuccessfullyDebugMessage);
			} catch (ConstraintViolationException constraintViolationException) {
				String errorMessage = "ConstraintViolationException for " + moduleName;
				logger.info(errorMessage);
			} catch (HibernateException hibernateException) {
				String errorMessage = "HibernateException for " + moduleName;
				logger.info(errorMessage);
			} catch (DataIntegrityViolationException dataIntegrityViolationException) {
				String errorMessage = "DataIntegrityViolationException for " + moduleName;
				logger.info(errorMessage);
			} finally {
				session.close();
			}
			count++;
		}
	}

	public void deleteAll(List<Integer> ids, String tableName, String columnName) {
		if (XamplifyUtils.isNotEmptyList(ids) && XamplifyUtils.isValidString(tableName)
				&& XamplifyUtils.isValidString(columnName)) {
			int total = ids.size();
			int count = 1;
			String prefixMessage = StringUtils.hasText(tableName) ? tableName : " Records ";
			String totalRecordsDebugMessage = "Total " + prefixMessage + " : " + total;
			logger.debug(totalRecordsDebugMessage);
			for (int i = 0; i < total; i++) {
				Session session = sessionFactory.openSession();
				try {
					session.createSQLQuery("delete from  " + tableName + " where  " + columnName + " = " + ids.get(i))
							.executeUpdate();
					session.flush();
					session.clear();
					String rowDeletedSuccessfully = "Row Deleted Successfully From  " + tableName;
					logger.debug(rowDeletedSuccessfully);
				} catch (ConstraintViolationException constraintViolationException) {
					String errorMessage = "ConstraintViolationException for " + tableName;
					logger.info(errorMessage);
				} catch (HibernateException hibernateException) {
					String errorMessage = "HibernateException for " + tableName;
					logger.info(errorMessage);
				} catch (DataIntegrityViolationException dataIntegrityViolationException) {
					String errorMessage = "DataIntegrityViolationException for " + tableName;
					logger.info(errorMessage);
				} finally {
					session.close();
				}
				String debugMessage = count + "/" + total + " " + tableName + " deleted.";
				logger.debug(debugMessage);
				count++;
			}
		}

	}

	public Partnership createPartnership(UserDTO user, CompanyProfile vendorCompany, User representingVendor,
			Integer vendorAdminId, UserList defaultPartnerList, UserList inActiveMasterPartnerList, String viewType,
			boolean isGdprOn, List<LegalBasis> legalBasisList) {
		Session session = sessionFactory.openSession();
		Partnership partnership = null;
		try {

			// Creating Parter
			User newPartner = new User();
			newPartner.setEmailId(user.getEmailId().toLowerCase());
			newPartner.setFirstName(user.getFirstName());
			newPartner.setUserStatus(UserStatus.UNAPPROVED);
			newPartner.setUserName(newPartner.getEmailId());
			newPartner.setUserDefaultPage(UserDefaultPage.WELCOME);
			newPartner.getRoles().add(Role.USER_ROLE);
			newPartner.getRoles().add(Role.COMPANY_PARTNER);
			newPartner.setEmailValid(true);
			newPartner.setEmailValidationInd(true);
			newPartner.setContactsLimit(user.getContactsLimit());
			newPartner.setMdfAmount(user.getMdfAmount());
			newPartner.setNotifyPartners(user.isNotifyPartners());
			newPartner.setSelectedTeamMemberIds(user.getSelectedTeamMemberIds());
			newPartner.initialiseCommonFields(true, vendorAdminId);
			GenerateRandomPassword randomPassword = new GenerateRandomPassword();
			newPartner.setAlias(randomPassword.getPassword());
			session.save(newPartner);

			// Create Company Profile

			CompanyProfile companyProfile = new CompanyProfile();
			companyProfile.setCompanyName(user.getContactCompany().trim());
			companyProfile.setCompanyNameStatus(CompanyNameStatus.INACTIVE);
			companyProfile.setAddedAdminCompanyId(vendorCompany.getId());
			companyProfile.setCity(user.getCity());
			companyProfile.setState(user.getState());
			companyProfile.setZip(user.getZipCode());
			companyProfile.setCountry(user.getCountry());
			companyProfile.setStreet(user.getAddress());
			session.save(companyProfile);

			if (companyProfile != null) {
				newPartner.setCompanyProfile(companyProfile);
			}

			// Create PartnerShip
			partnership = new Partnership();
			PartnershipSource partnershipSource = PartnershipSource.ONBOARD;
			partnership.setVendorCompany(representingVendor.getCompanyProfile());
			partnership.setRepresentingVendor(representingVendor);
			partnership.setRepresentingPartner(newPartner);
			partnership.setPartnerCompany(newPartner.getCompanyProfile());
			partnership.setSource(partnershipSource);
			partnership.setStatus(PartnershipStatus.APPROVED);
			partnership.setContactsLimit(newPartner.getContactsLimit());
			partnership.setNotifyPartners(newPartner.isNotifyPartners());
			partnership.setCreatedBy(representingVendor.getUserId());
			partnership.initialiseCommonFields(true, representingVendor.getUserId());
			Set<PartnerTeamMemberViewType> partnerTeamMemberViewTypes = new HashSet<>();
			// Creating partnerTeamMemberViewTypes
			PartnerTeamMemberViewType partnerTeamMemberViewType = new PartnerTeamMemberViewType();
			User primaryUser = new User();
			primaryUser.setUserId(vendorAdminId);
			partnerTeamMemberViewType.setAdmin(primaryUser);
			partnerTeamMemberViewType.setPartnership(partnership);
			partnerTeamMemberViewType.setVendorViewType(ModulesDisplayType.findByName(viewType));
			partnerTeamMemberViewType.setViewUpdated(false);
			partnerTeamMemberViewType.setCreatedTime(new Date());
			partnerTeamMemberViewType.setUpdatedTime(new Date());
			partnerTeamMemberViewTypes.add(partnerTeamMemberViewType);
			partnership.setPartnerTeamMemberViewType(partnerTeamMemberViewTypes);
			session.save(partnership);

			/* Add partner to Default Partner List */
			addPartnerToList(session, newPartner, defaultPartnerList, user, isGdprOn, legalBasisList);
			addPartnerToList(session, newPartner, inActiveMasterPartnerList, user, isGdprOn, legalBasisList);

			// Add partnership History
			if (partnership != null) {
				PartnershipStatusHistory partnershipStatusHistory = new PartnershipStatusHistory();
				partnershipStatusHistory.setPartnership(partnership);
				partnershipStatusHistory.setStatus(PartnershipStatus.APPROVED);
				partnershipStatusHistory.setCreatedBy(vendorAdminId);
				partnershipStatusHistory.setCreatedTime(new Date());
				session.save(partnershipStatusHistory);
			}

			logger.debug("****************************************************************************");
			String partnerAddedDebugMessage = newPartner.getEmailId() + " is onboarded " + newPartner.getUserId();
			logger.debug(partnerAddedDebugMessage);
			logger.debug("****************************************************************************");
			session.flush();
			session.clear();
		} catch (HibernateException hibernateException) {
			String errorMessage = "HibernateException";
			logger.info(errorMessage);
		} finally {
			session.close();
		}
		return partnership;

	}

	private void addPartnerToList(Session session, User representingPartner, UserList userList, UserDTO userDetails,
			boolean isGdprOn, List<LegalBasis> legalBasisList) {
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
			userUserList.setLegalBasis(legalBasisList);
			session.save(userUserList);
			representingPartner.setEmailValid(true);
			representingPartner.setEmailValidationInd(true);
		}

	}

	public void createPartnerGroup(List<TeamMemberModuleDTO> modules, Integer companyId) {
		Session session = sessionFactory.openSession();
		try {

			if (modules != null && !modules.isEmpty()) {
				Set<Integer> moduleIds = modules.stream().map(TeamMemberModuleDTO::getRoleId)
						.collect(Collectors.toSet());
				TeamMemberGroup teamMemberGroup = new TeamMemberGroup();
				teamMemberGroup.setName("Partner Account Manager");
				teamMemberGroup.setCreatedUserId(1);
				teamMemberGroup.setUpdatedUserId(1);
				teamMemberGroup.setCompanyId(companyId);
				teamMemberGroup.setCreatedTime(new Date());
				teamMemberGroup.setUpdatedTime(new Date());
				teamMemberGroup.setDefaultGroup(true);
				GenerateRandomPassword password = new GenerateRandomPassword();
				teamMemberGroup.setAlias(password.getPassword());
				Set<TeamMemberGroupRoleMapping> teamMemberGroupRoleMappings = new HashSet<>();
				for (Integer moduleId : moduleIds) {
					boolean partners = moduleId != null && moduleId.equals(Role.PARTNERS.getRoleId());
					boolean stats = moduleId != null && moduleId.equals(Role.STATS_ROLE.getRoleId());
					if (!partners && !stats) {
						TeamMemberGroupRoleMapping teamMemberGroupRoleMapping = new TeamMemberGroupRoleMapping();
						teamMemberGroupRoleMapping.setTeamMemberGroup(teamMemberGroup);
						teamMemberGroupRoleMapping.setRoleId(moduleId);
						teamMemberGroupRoleMapping.setCreatedTime(new Date());
						teamMemberGroupRoleMappings.add(teamMemberGroupRoleMapping);
					}
				}
				teamMemberGroup.setTeamMemberGroupRoleMappings(teamMemberGroupRoleMappings);
				session.save(teamMemberGroup);
				logger.debug("Partner Account Manager has been added successfully");
				session.flush();
				session.clear();
			}

		} catch (HibernateException hibernateException) {
			String errorMessage = "HibernateException for ";
			logger.info(errorMessage);
		} finally {
			session.close();
		}
	}

}
