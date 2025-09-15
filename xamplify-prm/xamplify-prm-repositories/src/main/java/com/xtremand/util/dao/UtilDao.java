package com.xtremand.util.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.campaign.bom.DownloadDataInfo;
import com.xtremand.campaign.bom.DownloadDataInfo.DownloadItem;
import com.xtremand.campaign.bom.ModuleAccess;
import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.category.bom.CategoryModuleEnum;
import com.xtremand.common.bom.ModuleCustom;
import com.xtremand.company.dto.CompanyProfileDTO;
import com.xtremand.company.dto.DomainColorDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.partner.journey.bom.TriggerComponent;
import com.xtremand.partner.journey.bom.TriggerComponentType;
import com.xtremand.partnership.dto.LoginAsPartnerDTO;
import com.xtremand.partnership.dto.PartnerTeamMemberGroupDTO;
import com.xtremand.team.dao.TeamDao;
import com.xtremand.team.member.dto.TeamMemberListDTO;
import com.xtremand.team.member.group.dao.TeamMemberGroupDao;
import com.xtremand.user.bom.Role;
import com.xtremand.user.bom.TeamMember;
import com.xtremand.user.bom.TeamMemberPartnerFilterOption;
import com.xtremand.user.bom.TeamMemberPartnerFilterType;
import com.xtremand.user.bom.User;
import com.xtremand.user.bom.UserList.ContactListTypeValue;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.user.list.dto.ProcessingUserListsDTO;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.bom.ActiveQueryInfo;
import com.xtremand.util.dto.ActionRoleDTO;
import com.xtremand.util.dto.CompanyDetailsDTO;
import com.xtremand.util.dto.DeletedPartnerDTO;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.LeftSideNavigationBarItem;
import com.xtremand.util.dto.ModuleCustomDTO;
import com.xtremand.util.dto.PrmOrVendorTierPartnership;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.TeamMemberFilterDTO;
import com.xtremand.util.dto.UserUserListDTO;
import com.xtremand.util.dto.XamplifyConstants;

@Repository
@Transactional
public class UtilDao {

	@Value("${vendorCompanyUserRolesQuery}")
	private String vendorCompanyUserRolesQuery;

	@Value("${dbName}")
	private String dbName;

	@Value("${hostIp}")
	private String hostIp;

	@Value("${query.findAllTeamMembers}")
	private String findAllTeamMembers;

	@Value("${query.groupByfindAllTeamMembers}")
	private String groupByfindAllTeamMembers;

	@Value("${query.orderByfindAllTeamMembers}")
	private String sortByQueryFindAllTeamMembers;

	@Value("${query.primaryAdmin}")
	private String primaryAdmin;

	@Value("${query.findPrimaryAdminGroupBy}")
	private String primaryAdminGroupByQuery;

	@Value("${query.orderByPrimaryAdmin}")
	private String orderByPrimaryAdmin;

	@Value("${active.partner.list.name}")
	private String activePartnerListName;

	@Value("${inactive.partner.list.name}")
	private String inActivePartnerListName;

	@Value("${domain}")
	private String domain;

	@Value("${total.contacts.subscription.used.by.company}")
	private String totalContactsSubscriptionUsedByCompany;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private TeamDao teamDao;

	@Autowired
	private TeamMemberGroupDao teamMemberGroupDao;

	@Autowired
	private PaginationUtil paginationUtil;

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	private static final String COMPANY_ID = "companyId";

	private static final String USER_ID = "userId";

	private static final String PARTNER_ID = "partnerId";

	private static final String APPORVED_QUERY = " and status = 'approved')";

	private static final String PLAY_BOOKS = "play_books";

	private static final String ENABLE_LEADS = "enable_leads";

	private static final String MDF = "mdf";

	private static final String SHARE_LEADS = "share_leads";

	private static final String DAM = "dam";

	private static final String LMS = "lms";

	private static final String FORM = "form";

	private static final String SELECT = "select ";

	/**** XNFR-255 *******/
	private static final String WHITE_LABELED = "share_white_labeled_content";

	/**** XNFR-276 *******/
	private static final String VANITY = "vanity_url_domain";

	private static final String CREATE_WORKFLOW = "is_create_workflow";

	private static final String PARTNER = "Partner";

	private static final String PARTNER_COMPANY_ID = "partnerCompanyId";

	private static final String VENDOR_COMPANY_ID = "vendorCompanyId";

	private static final String APPROVALS = "approvals";

	private static final String APPROVAL_HUB = "approval_hub";

	private static final String INSIGHTS = "insights";

	private static final String UNLOCK_MDF_FUNDING = "unlock_mdf_funding_enabled";

	private static final String ALLOW_VENDOR_TO_CHANGE_THE_PRIMARY_ADMIN = "allow_vendor_to_change_partner_primary_admin";

	/** XNFR-1062 ***/
	private static final String IS_MAILS_ENABLED = "mails_enabled";

	public boolean isAdminByRoleId(Integer userId, Integer roleId) {
		boolean isAdmin = false;
		boolean isTeamMember = teamDao.isTeamMember(userId);
		if (isTeamMember) {
			Integer superiorId = teamDao.getOrgAdminIdByTeamMemberId(userId);
			List<Integer> superiorRoleIds = userDao.getRoleIdsByUserId(superiorId);
			if (superiorRoleIds != null && !superiorRoleIds.isEmpty()) {
				isAdmin = superiorRoleIds.indexOf(roleId) > -1;
			}
		} else {
			List<Integer> roleIds = userDao.getRoleIdsByUserId(userId);
			isAdmin = roleIds.indexOf(roleId) > -1;
		}
		return isAdmin;

	}

	public boolean isPartnerCompany(Integer userId) {
		List<Integer> roleIds = userDao.getRoleIdsByUserId(userId);
		return roleIds != null && !roleIds.isEmpty() && roleIds.indexOf(Role.COMPANY_PARTNER.getRoleId()) > -1;
	}

	public boolean isPrmCompany(Integer userId) {
		return isAdminByRoleId(userId, Role.PRM_ROLE.getRoleId());
	}

	public boolean isOnlyPartnerCompany(Integer userId) {
		boolean isOnlyPartner = false;
		boolean isTeamMember = teamDao.isTeamMember(userId);
		if (isTeamMember) {
			Integer superiorId = teamDao.getOrgAdminIdByTeamMemberId(userId);
			List<Integer> superiorRoleIds = userDao.getRoleIdsByUserId(superiorId);
			if (superiorRoleIds != null && !superiorRoleIds.isEmpty()) {
				isOnlyPartner = Role.isOnlyPartnerCompanyByRoleIds(superiorRoleIds);
			}
		} else {
			List<Integer> roleIds = userDao.getRoleIdsByUserId(userId);
			isOnlyPartner = Role.isOnlyPartnerCompanyByRoleIds(roleIds);
		}
		return isOnlyPartner;
	}

	public String findRoleByUserId(Integer userId) {
		String role = "";
		if (isPrmCompany(userId)) {
			role = "PRM";
		} else if (isOnlyPartnerCompany(userId)) {
			role = PARTNER;
		} else {
			role = "User";
		}
		return role;
	}

	public boolean mdfAccessForPartner(Integer partnerId) {
		if (partnerId != null && partnerId != 1) {
			String sqlString = " select mdf from xt_module_access where company_id in (select vendor_company_id from xt_partnership where partner_company_id = (select company_id from xt_user_profile where user_id =:partnerId )"
					+ APPORVED_QUERY;
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createSQLQuery(sqlString).setParameter(PARTNER_ID, partnerId);
			return returnAccess(query);
		} else {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	private boolean returnAccess(Query query) {
		List<Boolean> list = query.list();
		if (list.isEmpty()) {
			return false;
		} else {
			return list.indexOf(true) > -1;
		}
	}

	private boolean hasModuleAccessByPartnerIdAndModuleName(Integer partnerId, String moduleName) {
		if (partnerId != null && partnerId != 1) {
			String sqlString = SELECT + moduleName
					+ " from xt_module_access where company_id in (select vendor_company_id from xt_partnership where partner_company_id = (select company_id from xt_user_profile where user_id =:partnerId )"
					+ APPORVED_QUERY;
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createSQLQuery(sqlString).setParameter(PARTNER_ID, partnerId);
			return returnAccess(query);
		} else {
			return false;
		}
	}

	public boolean hasModuleAccessByPartnerCompanyIdAndModuleId(Integer partnerCompanyId, Integer roleId) {
		if (partnerCompanyId != null && partnerCompanyId > 0) {
			String moduleName = findModuleNameByRoleId(roleId);
			String sqlString = " select " + moduleName
					+ " from xt_module_access where company_id in (select vendor_company_id from xt_partnership where partner_company_id = :partnerCompanyId "
					+ APPORVED_QUERY;
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createSQLQuery(sqlString).setParameter(PARTNER_COMPANY_ID, partnerCompanyId);
			return returnAccess(query);
		} else {
			return false;
		}
	}

	public boolean hasModuleAccessForPartnerByPartnerCompanyId(Integer partnerCompanyId,
			Integer excludedVendorCompanyId, Integer roleId) {
		if (partnerCompanyId != null && partnerCompanyId != 1 && excludedVendorCompanyId != null
				&& excludedVendorCompanyId > 0) {
			String moduleName = findModuleNameByRoleId(roleId);
			String sqlString = " select " + moduleName
					+ " from xt_module_access where company_id in (select vendor_company_id from xt_partnership where partner_company_id = :partnerCompanyId and vendor_company_id!=:vendorCompanyId "
					+ APPORVED_QUERY;
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createSQLQuery(sqlString).setParameter(PARTNER_COMPANY_ID, partnerCompanyId)
					.setParameter(VENDOR_COMPANY_ID, excludedVendorCompanyId);
			return returnAccess(query);
		} else {
			return false;
		}
	}

	private String findModuleNameByRoleId(Integer roleId) {
		String moduleName = "";
		if (Role.OPPORTUNITY.getRoleId().equals(roleId)) {
			moduleName = ENABLE_LEADS;
		} else if (Role.MDF.getRoleId().equals(roleId)) {
			moduleName = MDF;
		} else if (Role.DAM.getRoleId().equals(roleId)) {
			moduleName = DAM;
		} else if (Role.LEARNING_TRACK.getRoleId().equals(roleId)) {
			moduleName = LMS;
		} else if (Role.PLAY_BOOK.getRoleId().equals(roleId)) {
			moduleName = PLAY_BOOKS;
		} else if (Role.SHARE_LEADS.getRoleId().equals(roleId)) {
			moduleName = SHARE_LEADS;
		}
		return moduleName;
	}

	public String findModuleByRoleId(Integer roleId) {
		String moduleName = "";
		if (Role.OPPORTUNITY.getRoleId().equals(roleId)) {
			moduleName = "Leads (Opportunities)";
		} else if (Role.MDF.getRoleId().equals(roleId)) {
			moduleName = "MDF";
		} else if (Role.DAM.getRoleId().equals(roleId)) {
			moduleName = "DAM";
		} else if (Role.LEARNING_TRACK.getRoleId().equals(roleId)) {
			moduleName = "LMS";
		} else if (Role.PLAY_BOOK.getRoleId().equals(roleId)) {
			moduleName = "PlayBooks";
		} else if (Role.SHARE_LEADS.getRoleId().equals(roleId)) {
			moduleName = "Share Leads";
		}
		return moduleName;
	}

	/************ Enable Leads (Opportunities) *****************/
	public boolean enableLeadsForPartner(Integer partnerId) {
		return hasModuleAccessByPartnerIdAndModuleName(partnerId, ENABLE_LEADS);
	}

	public boolean hasEnableLeadsAccessByCompanyId(Integer companyId) {
		return hasModuleAccessByCompanyId(companyId, ENABLE_LEADS);
	}

	public boolean hasEnableLeadsAccessByUserId(Integer userId) {
		return hasModuleAccessByUserId(userId, ENABLE_LEADS);

	}

	/************** MDF ***********************/
	public boolean hasMdfAccessByUserId(Integer userId) {
		return hasModuleAccessByUserId(userId, MDF);

	}

	public boolean hasMdfAccessByCompanyId(Integer companyId) {
		return hasModuleAccessByCompanyId(companyId, MDF);
	}

	/************** DAM ********************************/
	public boolean damAccessForPartner(Integer partnerId) {
		return hasModuleAccessByPartnerIdAndModuleName(partnerId, DAM);
	}

	public boolean hasDamAccessByUserId(Integer userId) {
		return hasModuleAccessByUserId(userId, DAM);

	}

	public boolean hasDamAccessByCompanyId(Integer companyId) {
		return hasModuleAccessByCompanyId(companyId, DAM);

	}

	/************** LMS ************************/
	public boolean lmsAccessForPartner(Integer partnerId) {
		return hasModuleAccessByPartnerIdAndModuleName(partnerId, LMS);
	}

	public boolean hasLmsAccessByUserId(Integer userId) {
		return hasModuleAccessByUserId(userId, LMS);
	}

	public boolean hasLmsAccessByCompanyId(Integer companyId) {
		return hasModuleAccessByCompanyId(companyId, LMS);
	}

	/********** PLAY_BOOKS ***********************/
	public boolean playbookAccessForPartner(Integer partnerId) {
		return hasModuleAccessByPartnerIdAndModuleName(partnerId, PLAY_BOOKS);
	}

	public boolean hasPlaybookAccessByUserId(Integer userId) {
		return hasModuleAccessByUserId(userId, PLAY_BOOKS);

	}

	public boolean hasPlaybookAccessByCompanyId(Integer companyId) {
		return hasModuleAccessByCompanyId(companyId, PLAY_BOOKS);

	}

	// XNFR-820
	public boolean hasApprovalAccessByUserId(Integer userId) {
		return hasModuleAccessByUserId(userId, APPROVALS);

	}

	public boolean hasApprovalHubAccessByUserId(Integer userId) {
		return hasModuleAccessByUserId(userId, APPROVAL_HUB);

	}

	public boolean hasInsightsAccessByUserId(Integer userId) {
		return hasModuleAccessByUserId(userId, INSIGHTS);

	}

	/******************* FORM ****************************/
	public boolean formAccessForPartner(Integer partnerId) {
		return hasModuleAccessByPartnerIdAndModuleName(partnerId, FORM);
	}

	public boolean hasFormAccessByUserId(Integer userId) {
		return hasModuleAccessByUserId(userId, FORM);
	}

	public boolean hasFormAccessByCompanyId(Integer companyId) {
		return hasModuleAccessByCompanyId(companyId, FORM);
	}

	/************** SHARED_LEADS **************************/
	public boolean sharedLeadsAccessForPartner(Integer partnerId) {
		return hasModuleAccessByPartnerIdAndModuleName(partnerId, SHARE_LEADS);
	}

	public boolean hasShareLeadsAccessByUserId(Integer userId) {
		return hasModuleAccessByUserId(userId, SHARE_LEADS);

	}

	/************** SHARE_LEADS **************************/
	public boolean hasShareLeadsAccessByCompanyId(Integer companyId) {
		return hasModuleAccessByCompanyId(companyId, SHARE_LEADS);
	}

	/******* White-Labeled Option (XNFR-255) *******/
	public boolean hasShareWhiteLabeledContentAccessByCompanyId(Integer companyId) {
		return hasModuleAccessByCompanyId(companyId, WHITE_LABELED);
	}

	public boolean hasShareWhiteLabeledContentAccessByUserId(Integer userId) {
		return hasModuleAccessByUserId(userId, WHITE_LABELED);

	}

	/******* Create Workflow *******/
	public boolean hasCreateWorkflowAccessByCompanyId(Integer companyId) {
		return hasModuleAccessByCompanyId(companyId, CREATE_WORKFLOW);
	}

	public boolean hasCreateWorkflowAccessByUserId(Integer userId) {
		return hasModuleAccessByUserId(userId, CREATE_WORKFLOW);

	}

	public boolean isPartnershipEstablishedOnlyWithPrm(Integer userId) {
		return checkPrmRole(userId, vendorCompanyUserRolesQuery, USER_ID);
	}

	@SuppressWarnings("unchecked")
	public boolean isPartnershipEstablishedWithPrm(Integer userId) {
		boolean partnershipEstablishedWithPrmCompany = false;
		if (userId != null) {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createSQLQuery(vendorCompanyUserRolesQuery).setParameter(USER_ID, userId);
			List<Integer> list = query.list();
			if (list != null && !list.isEmpty()) {
				partnershipEstablishedWithPrmCompany = list.indexOf(Role.PRM_ROLE.getRoleId()) > -1;
			}
		}
		return partnershipEstablishedWithPrmCompany;
	}

	public boolean isPrmByVendorCompanyId(Integer companyId) {
		String sqlString = " select ur.role_id from xt_user_role ur,xt_user_profile u where u.user_id = ur.user_id and u.user_id in ( "
				+ " select user_id from xt_user_profile where company_id = :companyId)";
		return checkPrmRole(companyId, sqlString, COMPANY_ID);
	}

	@SuppressWarnings("unchecked")
	private boolean checkPrmRole(Integer userId, String sqlString, String parameter) {
		boolean onlyPrmRole = false;
		if (userId != null) {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createSQLQuery(sqlString).setParameter(parameter, userId);
			List<Integer> list = query.list();
			if (list != null && !list.isEmpty()) {
				onlyPrmRole = list.indexOf(Role.PRM_ROLE.getRoleId()) > -1;

			}
		}
		return onlyPrmRole;
	}

	private boolean hasModuleAccessByUserId(Integer userId, String module) {
		if (userId != null) {
			String table = "xt_module_access";
			String sqlString = "select CASE WHEN  count(*) > 0 THEN true ELSE false END from " + table + " where "
					+ module + " and company_id = (select company_id from xt_user_profile where user_id=:userId)";
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createSQLQuery(sqlString).setParameter(USER_ID, userId);
			return (boolean) query.uniqueResult();
		} else {
			return false;
		}
	}

	private boolean hasModuleAccessByCompanyId(Integer companyId, String module) {
		if (companyId != null) {
			String table = "xt_module_access";
			String sqlString = "select CASE WHEN  count(*) > 0 THEN true ELSE false END from " + table + " where "
					+ module + " and company_id = :companyId";
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createSQLQuery(sqlString).setParameter(COMPANY_ID, companyId);
			return (boolean) query.uniqueResult();
		} else {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public List<String> findAllDomains() {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select distinct LOWER(substring(email_id from '@.*')) from xt_user_profile where substring(email_id from '@.*')is not null";
		return session.createSQLQuery(queryString).list();
	}

	@SuppressWarnings("unchecked")
	public List<Integer> findAdminRoleIdsByCompanyIds(Set<Integer> companyIds) {
		if (companyIds != null && !companyIds.isEmpty()) {
			String sql = " select distinct ur.role_id from xt_user_role ur,xt_user_profile u where u.user_id = ur.user_id and u.company_id in (:companyIds)"
					+ " and ur.role_id in (" + Role.getAllAdminRolesInString() + ")";
			Session session = sessionFactory.getCurrentSession();
			return session.createSQLQuery(sql).setParameterList("companyIds", companyIds).list();
		} else {
			return new ArrayList<>();
		}
	}

	public PrmOrVendorTierPartnership findPartnershipDetails(Set<Integer> companyIds) {
		PrmOrVendorTierPartnership prmOrVendorTierPartnership = new PrmOrVendorTierPartnership();
		List<Integer> adminRoleIds = findAdminRoleIdsByCompanyIds(companyIds);
		if (!adminRoleIds.isEmpty()) {
			boolean isPrm = adminRoleIds.indexOf(Role.PRM_ROLE.getRoleId()) > -1;
			prmOrVendorTierPartnership.setPartnershipEstablishedOnlyWithPrm(isPrm);
		}
		return prmOrVendorTierPartnership;

	}

	public boolean isSuperVisorOrAdmin(Integer userId) {
		List<Integer> roleIds = userDao.getRoleIdsByUserId(userId);
		boolean superVisor = roleIds.indexOf(Role.ALL_ROLES.getRoleId()) > -1;
		boolean prm = roleIds.indexOf(Role.PRM_ROLE.getRoleId()) > -1;
		return superVisor || prm;
	}

	public void addPartnerRoleToSecondAdmins(Integer userId, List<Integer> roleIds) {
		boolean isTeamMember = teamDao.isTeamMember(userId);
		List<Integer> roleIdsByUserId = userDao.getRoleIdsByUserId(userId);
		boolean extraPrmAdmin = roleIdsByUserId.indexOf(Role.PRM_ROLE.getRoleId()) > -1;
		boolean extraPartnerAdmin = roleIdsByUserId.indexOf(Role.COMPANY_PARTNER.getRoleId()) > -1;
		boolean extraAdmin = extraPrmAdmin || extraPartnerAdmin;
		if (isTeamMember && extraAdmin) {
			Integer superiorId = teamDao.getOrgAdminIdByTeamMemberId(userId);
			List<Integer> superiorRoles = userDao.getRoleIdsByUserId(superiorId);
			boolean isPartner = superiorRoles.indexOf(Role.COMPANY_PARTNER.getRoleId()) > -1;
			if (isPartner) {
				roleIds.add(Role.COMPANY_PARTNER.getRoleId());
			}
		}
	}

	public boolean isAnyAdmin(List<Integer> roleIds) {
		boolean isPartner = roleIds.indexOf(Role.COMPANY_PARTNER.getRoleId()) > -1 && roleIds.size() == 2;
		boolean isPrm = roleIds.indexOf(Role.PRM_ROLE.getRoleId()) > -1;
		return isPartner || isPrm;
	}

	public boolean findOpportunitiesAccessByRedistributedCampaignId(Integer campaignId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select distinct ma.enable_leads from xt_campaign c,xt_module_access ma,xt_user_profile u,xt_campaign p where c.campaign_id = :campaignId "
				+ " and p.customer_id = u.user_id and c.parent_campaign_id = p.campaign_id and u.company_id = ma.company_id";
		Query query = session.createSQLQuery(sql).setParameter("campaignId", campaignId);
		if (query.uniqueResult() != null) {
			return (boolean) query.uniqueResult();
		} else {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public List<Integer> findAllRoleIdsByCompanyId(Integer companyId) {
		if (companyId != null && companyId > 0) {
			String sql = " select distinct ur.role_id from xt_user_role ur,xt_user_profile u where u.user_id = ur.user_id and u.company_id =:companyId";
			Session session = sessionFactory.getCurrentSession();
			return session.createSQLQuery(sql).setParameter(COMPANY_ID, companyId).list();
		} else {
			return new ArrayList<>();
		}
	}

	public UserUserListDTO findPartnerInfoFromDefaultPartnerList(Integer userId, Integer companyId) {
		String sql = " select uul.user_id as \"userId\",uul.user_list_id as \"userListId\",uul.country as \"country\", "
				+ " uul.city as \"city\",uul.address as \"address\",uul.contact_company as \"contactCompany\",uul.job_title as \"jobTitle\","
				+ " uul.firstname as \"firstName\",uul.lastname as \"lastName\",uul.mobile_number as \"mobileNumber\", uul.state as \"state\""
				+ ",uul.zip as \"zip\",uul.vertical as \"vertical\",uul.region as \"region\",uul.partner_type as \"partnerType\",uul.category as \"category\", "
				+ " string_agg(distinct cast(ulb.legal_basis_id as text), ',') as \"legalBasisString\" "
				+ " from xt_user_userlist uul,xt_user_list ul,xt_user_legal_basis ulb "
				+ " where ul.user_list_id = uul.user_list_id and ul.company_id = :companyId "
				+ " and ul.is_default_partnerlist  and uul.user_id = :partnerId "
				+ " and ulb.user_userlist_id = uul.id "
				+ " group by uul.user_id,uul.user_list_id,uul.country,uul.city,uul.address, "
				+ " uul.contact_company,uul.job_title,uul.firstname,uul.lastname,uul.mobile_number, "
				+ " uul.state,uul.zip,uul.vertical,uul.region,uul.partner_type,uul.category";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sql).setParameter(PARTNER_ID, userId).setParameter(COMPANY_ID, companyId);
		return (UserUserListDTO) query.setResultTransformer(Transformers.aliasToBean(UserUserListDTO.class))
				.uniqueResult();

	}

	public void saveAll(List<ModuleCustom> moduleCustomNames) {
		try {
			Session session = sessionFactory.getCurrentSession();
			int size = moduleCustomNames.size();
			for (int i = 0; i < size; i++) {
				ModuleCustom moduleCustomName = moduleCustomNames.get(i);
				session.save(moduleCustomName);
				if (i % 30 == 0) {
					session.flush();
					session.clear();
				}
			}
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e.getMessage());
		} catch (Exception ex) {
			throw new XamplifyDataAccessException(ex.getMessage());
		}

	}

	@SuppressWarnings("unchecked")
	public List<Integer> findPartnerTeamMemberGroupPartnershipIdsByLoggedInUserId(Integer loggedInUserId) {
		String sqlString = "select distinct ptgm.partnership_id from xt_team_member_group tg,xt_team_member t,xt_team_member_group_user_mapping tmgum,xt_partner_team_group_mapping ptgm where t.team_member_id = :loggedInUserId and tmgum.team_member_group_id = tg.id and tmgum.team_member_id = t.id and ptgm.team_member_group_user_mapping_id = tmgum.id";
		Session session = sessionFactory.getCurrentSession();
		SQLQuery sqlQuery = session.createSQLQuery(sqlString);
		sqlQuery.setParameter("loggedInUserId", loggedInUserId);
		return sqlQuery.list();
	}

	public TeamMemberFilterDTO applyFilterConditions(Integer loggedInUserId, boolean teamMemberFilter,
			boolean findPartnershipIds) {
		TeamMemberFilterDTO teamMemberFilterDTO = new TeamMemberFilterDTO();
		boolean isTeamMember = teamDao.isTeamMember(loggedInUserId);
		if (isTeamMember) {
			List<Integer> partnershipIdsOrPartnerCompanies = new ArrayList<>();
			if (findPartnershipIds) {
				partnershipIdsOrPartnerCompanies
						.addAll(findPartnerTeamMemberGroupPartnershipIdsByLoggedInUserId(loggedInUserId));
			} else {
				partnershipIdsOrPartnerCompanies.addAll(findPartnerCompanyIdsByLoggedInUserId(loggedInUserId));
			}

			boolean applyTeamMemberFilter = !partnershipIdsOrPartnerCompanies.isEmpty() && teamMemberFilter;
			boolean emptyFilter = teamMemberFilter && partnershipIdsOrPartnerCompanies.isEmpty();
			teamMemberFilterDTO.setApplyTeamMemberFilter(applyTeamMemberFilter);
			teamMemberFilterDTO.setPartnershipIdsOrPartnerCompanyIds(partnershipIdsOrPartnerCompanies);
			teamMemberFilterDTO.setEmptyFilter(emptyFilter);
		}
		return teamMemberFilterDTO;

	}

	public void applyPartnershipIdsParameterList(boolean applyPartnershipFilter,
			List<Integer> partnershipIdsByLoggedInUserId, SQLQuery query) {
		if (applyPartnershipFilter) {
			query.setParameterList("partnershipIds", partnershipIdsByLoggedInUserId);
		}
	}

	public void applyPartnerCompanyIdsParameterList(boolean applyPartnershipFilter, List<Integer> partnerCompanyIds,
			SQLQuery query) {
		if (applyPartnershipFilter) {
			query.setParameterList("partnerCompanyIds", partnerCompanyIds);
		}
	}

	@SuppressWarnings("unchecked")
	public List<Integer> findPartnerCompanyIdsByLoggedInUserId(Integer loggedInUserId) {
		String sqlString = "select distinct p.partner_company_id from xt_team_member_group tg,xt_team_member t,xt_team_member_group_user_mapping tmgum,xt_partner_team_group_mapping ptgm,xt_partnership p where t.team_member_id = :loggedInUserId and tmgum.team_member_group_id = tg.id and tmgum.team_member_id = t.id and ptgm.team_member_group_user_mapping_id = tmgum.id and p.id = ptgm.partnership_id and p.partner_company_id is not null";
		Session session = sessionFactory.getCurrentSession();
		SQLQuery sqlQuery = session.createSQLQuery(sqlString);
		sqlQuery.setParameter("loggedInUserId", loggedInUserId);
		return sqlQuery.list();
	}

	public DeletedPartnerDTO getDeletedPartnerDTOByRoleIds(Integer userId, List<Integer> roleIds) {
		boolean isOnlyUser = roleIds.indexOf(Role.USER_ROLE.getRoleId()) > -1 && roleIds.size() == 1;
		return getDeletedPartnerDTO(userId, isOnlyUser);
	}

	public DeletedPartnerDTO getDeletedPartnerDTOByRoleNames(Integer userId, List<String> roleNames) {
		boolean isOnlyUser = roleNames.indexOf(Role.USER_ROLE.getRoleName()) > -1 && roleNames.size() == 1;
		return getDeletedPartnerDTO(userId, isOnlyUser);
	}

	private DeletedPartnerDTO getDeletedPartnerDTO(Integer userId, boolean isOnlyUser) {
		DeletedPartnerDTO deletedPartnerDTO = new DeletedPartnerDTO();
		boolean isPrmCompany = isPrmCompany(userId);
		boolean isOnlyPartnerCompany = isOnlyPartnerCompany(userId);
		boolean hasCompany = userDao.hasCompany(userId);
		boolean deletedPartnerCompany = !isPrmCompany && !isOnlyPartnerCompany && hasCompany;
		boolean deletedPartnerCompanyUser = deletedPartnerCompany && isOnlyUser;
		deletedPartnerDTO.setOnlyUser(isOnlyUser);
		deletedPartnerDTO.setDeletedPartnerCompanyUser(deletedPartnerCompanyUser);
		return deletedPartnerDTO;
	}

	@SuppressWarnings("unchecked")
	public List<Integer> findRoleIdsByCompanyId(Integer companyId) {
		String sqlString = "select distinct ur.role_id from xt_user_role ur,xt_user_profile up where up.company_id = :companyId and up.user_id = ur.user_id";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sqlString);
		query.setParameter(COMPANY_ID, companyId);
		return query.list();
	}

	/****** XNFR-125 *********/
	public boolean isShareListSharedByListIdAndPartnerId(Integer shareListId, Integer partnerId) {
		String sqlString = " select case when count(*)>0 then true else false end as listShared from xt_sharelist_partner_mapping where partner_id = :partnerId and sharelist_partner_id = :shareListId";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sqlString);
		query.setParameter(PARTNER_ID, partnerId);
		query.setParameter("shareListId", shareListId);
		return (boolean) query.uniqueResult();
	}

	/**** XNFR-127 ******/
	public Integer getCountByUserId(Session session, Integer userId, String queryString) {
		SQLQuery query = session.createSQLQuery(queryString);
		query.setParameter(USER_ID, userId);
		return (Integer) query.uniqueResult();
	}

	/**** XNFR-83 ******/
	public Integer findAdminIdByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select u.user_id  from xt_company_profile c,xt_user_role ur,xt_user_profile u\r\n"
				+ " left join xt_team_member t on t.team_member_id = u.user_id  where c.company_id = u.company_id\r\n"
				+ " and u.user_id = ur.user_id and role_id in (" + Role.getAllAdminRolesAndPartnerRoleInString()
				+ ")  group by c.company_id,u.user_id having  CAST(count(t.id)AS integer) = 0 and c.company_id = :companyId";
		SQLQuery query = session.createSQLQuery(queryString);
		query.setParameter(COMPANY_ID, companyId);
		return (Integer) query.uniqueResult();
	}

	/**** XNFR-83 ******/
	public boolean isAnyAdminCompany(String emailId) {
		Integer userId = userDao.getUserIdByEmail(emailId);
		if (userId != null && userId > 0) {
			boolean isPrmCompany = isPrmCompany(userId);
			boolean isOnlyPartnerCompany = isOnlyPartnerCompany(userId);
			return isOnlyPartnerCompany || isPrmCompany;
		} else {
			return false;
		}
	}

	/**** XNFR-83 ******/
	@SuppressWarnings("unchecked")
	public List<Integer> findAllUsersByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		SQLQuery query = session.createSQLQuery("select user_id from xt_user_profile where company_id = :companyId");
		query.setParameter(COMPANY_ID, companyId);
		return query.list();
	}

	/**** XNFR-83 ******/
	@SuppressWarnings("unchecked")
	public ActionRoleDTO getRolesByLoggedInUserId(Integer loggedInUserId) {
		ActionRoleDTO actionsDTO = new ActionRoleDTO();
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery("select distinct role_id from xt_user_role where user_id = :userId")
				.setParameter(USER_ID, loggedInUserId);
		List<Integer> roleIds = query.list();
		boolean prm = roleIds.indexOf(Role.PRM_ROLE.getRoleId()) > -1;
		boolean isAnyAdmin = prm;
		actionsDTO.setAnyAdmin(isAnyAdmin);
		actionsDTO.setSuperVisorTeamMember(roleIds.indexOf(Role.ALL_ROLES.getRoleId()) > -1);
		if (!actionsDTO.isAnyAdmin() && !actionsDTO.isSuperVisorTeamMember()) {
			boolean isTeamMember = teamDao.isTeamMember(loggedInUserId);
			actionsDTO.setTeamMember(isTeamMember);
		}
		return actionsDTO;
	}

	public void updateCategoryModuleTypeToPlayBooks(List<Integer> categoryModuleIds) {
		String playBookInString = "'" + CategoryModuleEnum.PLAY_BOOK.name() + "'";
		String sqlQueryString = "update xt_category_module set category_module_type = " + playBookInString
				+ " where id in (:ids)";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sqlQueryString);
		query.setParameterList("ids", categoryModuleIds);
		query.executeUpdate();
	}

	public void flushCurrentSession() {
		Session session = sessionFactory.getCurrentSession();
		session.flush();
		session.clear();
	}

	public void isOnlyPartnerOrPartnerCompany(Integer userId, LeftSideNavigationBarItem leftSideNavigationBarItem) {
		boolean isTeamMember = teamDao.isTeamMember(userId);
		boolean isOnlyPartnerCompany = false;
		boolean isPartnerCompany = false;
		boolean isAnyAdminAndPartnerCompany = false;
		if (isTeamMember) {
			Integer superiorId = teamDao.getOrgAdminIdByTeamMemberId(userId);
			List<Integer> superiorRoleIds = userDao.getRoleIdsByUserId(superiorId);
			if (superiorRoleIds != null && !superiorRoleIds.isEmpty()) {
				isOnlyPartnerCompany = Role.isOnlyPartnerCompanyByRoleIds(superiorRoleIds);
				isPartnerCompany = superiorRoleIds.indexOf(Role.COMPANY_PARTNER.getRoleId()) > -1;
				isAnyAdminAndPartnerCompany = Role.isAnyAdminAndPartnerCompany(superiorRoleIds);
			}
		} else {
			List<Integer> roleIds = userDao.getRoleIdsByUserId(userId);
			isOnlyPartnerCompany = Role.isOnlyPartnerCompanyByRoleIds(roleIds);
			isPartnerCompany = roleIds.indexOf(Role.COMPANY_PARTNER.getRoleId()) > -1;
			isAnyAdminAndPartnerCompany = Role.isAnyAdminAndPartnerCompany(roleIds);
		}
		leftSideNavigationBarItem.setOnlyPartnerCompany(isOnlyPartnerCompany);
		leftSideNavigationBarItem.setPartnerCompany(isPartnerCompany);
		leftSideNavigationBarItem.setAdminAndPartnerCompany(isAnyAdminAndPartnerCompany);
	}

	public User getUser(String userAlias) {
		Session session = sessionFactory.getCurrentSession();
		return (User) session.createCriteria(User.class)
				.add(Restrictions.or(Restrictions.eq("alias", userAlias), Restrictions.eq("userAlias", userAlias)))
				.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	public List<ActiveQueryInfo> findActiveQueries() {
		String sql = "select datname as \"dataBaseName\",pid as \"processId\",cast(client_addr as text) as \"ipAddress\",query as \"query\",cast(DATE_TRUNC('second', timestamptz(query_start)) as text) as \"queryStartedOn\","
				+ " case " + "when cast(client_addr as text) like '%" + hostIp
				+ " %' then 'xAmplify-Application' else 'Other' end as \"queryAccessedFrom\", state as \"status\" from pg_catalog.pg_stat_activity where datname = :dbName \r\n"
				+ " and   query!='COMMIT' and query not like '%token%' order by state";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sql);
		query.setParameter("dbName", dbName);
		return (List<ActiveQueryInfo>) paginationUtil.getListDTO(ActiveQueryInfo.class, query);

	}

	/***************** XNFR-224 *************/
	public boolean hasLoginAsPartnerAccessByCompanyId(Integer companyId) {
		return hasModuleAccessByCompanyId(companyId, "login_as_partner");
	}

	/***************** XNFR-224 *************/
	public LoginAsPartnerDTO findLoginAsPartnerSettingsOptions(String vendorCompanyProfileName,
			Integer partnerCompanyUserId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlString = "select p.is_login_as_partner_option_enabled_for_vendor as \"loginAsPartnerOptionEnabledForVendor\","
				+ " p.is_login_as_partner_email_notification_enabled as \"loginAsPartnerEmailNotificationEnabled\"  from \r\n"
				+ " xt_partnership p,xt_company_profile cp,xt_user_profile partner\r\n"
				+ " where p.partner_company_id = partner.company_id and partner.user_id = :partnerCompanyUserId and p.vendor_company_id = cp.company_id\r\n"
				+ " and cp.company_profile_name = :vendorCompanyProfileName";
		Query query = session.createSQLQuery(sqlString);
		query.setParameter("partnerCompanyUserId", partnerCompanyUserId);
		query.setParameter("vendorCompanyProfileName", vendorCompanyProfileName);
		return (LoginAsPartnerDTO) paginationUtil.getDto(LoginAsPartnerDTO.class, query);
	}

	/***************** XNFR-224 *************/
	public void updateLoginAsPartnerOptionEnabledForVendor(LoginAsPartnerDTO loginAsPartnerDTO) {
		Session session = sessionFactory.getCurrentSession();
		String sqlString = "update xt_partnership set is_login_as_partner_option_enabled_for_vendor=:loginAsPartnerOptionEnabledForVendor,is_login_as_partner_email_notification_enabled=:loginAsPartnerEmailNotificationEnabled"
				+ " where vendor_company_id=:vendorCompanyId " + " and partner_company_id=:partnerCompanyId";
		Query query = session.createSQLQuery(sqlString);
		query.setParameter(PARTNER_COMPANY_ID, loginAsPartnerDTO.getPartnerCompanyId());
		query.setParameter(VENDOR_COMPANY_ID, loginAsPartnerDTO.getVendorCompanyId());
		query.setParameter("loginAsPartnerOptionEnabledForVendor",
				loginAsPartnerDTO.isLoginAsPartnerOptionEnabledForVendor());
		query.setParameter("loginAsPartnerEmailNotificationEnabled",
				loginAsPartnerDTO.isLoginAsPartnerEmailNotificationEnabled());
		query.executeUpdate();
	}

	/***************** XNFR-224 *************/
	public boolean isLoginAsPartnerOptionEnabledForVendorByVendorCompanyIdAndPartnerCompanyId(Integer vendorCompanyId,
			Integer partnerCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlString = "select is_login_as_partner_option_enabled_for_vendor from \r\n"
				+ " xt_partnership where vendor_company_id = :vendorCompanyId and partner_company_id = :partnerCompanyId ";
		Query query = session.createSQLQuery(sqlString);
		query.setParameter(PARTNER_COMPANY_ID, partnerCompanyId);
		query.setParameter(VENDOR_COMPANY_ID, vendorCompanyId);
		return query.uniqueResult() != null && (boolean) query.uniqueResult();
	}

	/***************** XNFR-224 *************/
	public LoginAsPartnerDTO findLoginAsPartnerSettingsOptionsByVendorCompanyIdAndPartnerCompanyId(
			Integer vendorCompanyId, Integer partnerCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlString = "select p.is_login_as_partner_option_enabled_for_vendor as \"loginAsPartnerOptionEnabledForVendor\","
				+ " p.is_login_as_partner_email_notification_enabled as \"loginAsPartnerEmailNotificationEnabled\"  from \r\n"
				+ " xt_partnership p where p.partner_company_id = :partnerCompanyId and p.vendor_company_id = :vendorCompanyId";
		Query query = session.createSQLQuery(sqlString);
		query.setParameter(VENDOR_COMPANY_ID, vendorCompanyId);
		query.setParameter(PARTNER_COMPANY_ID, partnerCompanyId);
		return (LoginAsPartnerDTO) paginationUtil.getDto(LoginAsPartnerDTO.class, query);
	}

	/********** XNFR-255 *******/
	@SuppressWarnings("unchecked")
	public List<Integer> filterOnlyPartnerCompanyIds(List<Integer> companyIds) {
		List<Integer> onlyPartnerCompanyIds = new ArrayList<>();
		if (companyIds != null && !companyIds.isEmpty()) {
			Session session = sessionFactory.getCurrentSession();
			for (Integer companyId : companyIds) {
				String sqlString = "select distinct ur.role_id from xt_user_role ur,\r\n"
						+ " xt_user_profile up where up.company_id = :companyId \r\n" + " and ur.user_id = up.user_id";
				Query query = session.createSQLQuery(sqlString);
				query.setParameter(COMPANY_ID, companyId);
				List<Integer> roleIds = query.list();
				if (roleIds != null && !roleIds.isEmpty() && Role.isOnlyPartnerCompanyByRoleIds(roleIds)) {
					onlyPartnerCompanyIds.add(companyId);
				}
			}
			return onlyPartnerCompanyIds;
		} else {
			return Collections.emptyList();
		}

	}

	/********** XNFR-255 *******/
	public boolean isAnyVendorAdminCompany(Integer userId) {
		boolean isPrmCompany = isPrmCompany(userId);
		return isPrmCompany;
	}

	public XtremandResponse findCompanyDetailsByEmailIdOrUserId(String emailIdOrUserId) {
		XtremandResponse response = new XtremandResponse();
		Map<String, Object> map = new HashMap<>();
		Session session = sessionFactory.getCurrentSession();
		String companyQueryStringPrefix = "select c.company_name as \"companyName\",c.company_profile_name as \"companyProfileName\",c.company_id as \"companyId\",u.email_id as \"emailId\",u.user_id as \"userId\","
				+ " cast(c.company_name_status as text) as \"companyStatus\" from xt_user_profile u"
				+ " left join xt_company_profile c on c.company_id = u.company_id where u.email_id  = LOWER(TRIM(:emailIdOrUserId))";
		Query companyQuery = session.createSQLQuery(companyQueryStringPrefix);
		companyQuery.setParameter("emailIdOrUserId", emailIdOrUserId);
		CompanyDetailsDTO companyDetailsDTO = (CompanyDetailsDTO) paginationUtil.getDto(CompanyDetailsDTO.class,
				companyQuery);
		if (companyDetailsDTO != null) {
			int statusCode = companyDetailsDTO.getCompanyId() != null && companyDetailsDTO.getCompanyId() > 0 ? 200
					: 400;
			boolean isCompanyProfileNotCreated = "inactive".equals(companyDetailsDTO.getCompanyStatus());
			if (isCompanyProfileNotCreated) {
				statusCode = 400;
			}
			response.setStatusCode(statusCode);
			if (statusCode == 400) {
				response.setMessage("Company Profile Not Created For This Account.");
			}
			setCompanyInfo(map, companyDetailsDTO);
			if (statusCode == 200) {
				findAdminsAndTeamMemebers(map, session, companyDetailsDTO);
			}
			response.setData(map);
		} else {
			response.setStatusCode(404);
			response.setMessage("No Account Found.");
		}
		return response;
	}

	private void setCompanyInfo(Map<String, Object> map, CompanyDetailsDTO companyDetailsDTO) {
		Integer userId = companyDetailsDTO.getUserId();
		Integer companyId = companyDetailsDTO.getCompanyId();
		String companyType = findRoleByUserId(userId);
		if (companyId != null && companyId > 0 && !PARTNER.equals(companyType) && !"User".equals(companyType)) {
			List<Integer> roleIds = findRoleIdsByCompanyId(companyId);
			boolean isPartner = roleIds != null && !roleIds.isEmpty()
					&& roleIds.indexOf(Role.COMPANY_PARTNER.getRoleId()) > -1;
			if (isPartner) {
				companyType = companyType + " & " + PARTNER;
			}
		}
		companyDetailsDTO.setCompanyType(companyType);
		String vanityUrl = "https://" + companyDetailsDTO.getCompanyProfileName() + "." + domain;
		companyDetailsDTO.setVanityUrl(vanityUrl);
		map.put("companyDetails", companyDetailsDTO);
	}

	@SuppressWarnings("unchecked")
	private void findAdminsAndTeamMemebers(Map<String, Object> map, Session session,
			CompanyDetailsDTO companyDetailsDTO) {
		String adminsAndTeamMembersQueryString = primaryAdmin + " " + primaryAdminGroupByQuery
				+ findAllTeamMembers.replace("and t.team_member_id!= :userId", " ") + " " + groupByfindAllTeamMembers
				+ " " + orderByPrimaryAdmin + " ";
		adminsAndTeamMembersQueryString = adminsAndTeamMembersQueryString.replace("{teamMemberFilter}", "");
		adminsAndTeamMembersQueryString = adminsAndTeamMembersQueryString.replace("{teamMembersFilter}", "");
		adminsAndTeamMembersQueryString = adminsAndTeamMembersQueryString.replace("{dateFilterQuery}", "");
		SQLQuery adminsAndTeamMembersQuery = session.createSQLQuery(adminsAndTeamMembersQueryString);
		adminsAndTeamMembersQuery.setParameter(COMPANY_ID, companyDetailsDTO.getCompanyId());
		adminsAndTeamMembersQuery.setParameterList("roleIds", Role.getAllAdminRoleIds());
		adminsAndTeamMembersQuery.setParameter(USER_ID, companyDetailsDTO.getUserId());
		List<TeamMemberListDTO> adminsAndTeamMembers = (List<TeamMemberListDTO>) paginationUtil
				.getListDTO(TeamMemberListDTO.class, adminsAndTeamMembersQuery);
		map.put("adminsAndTeamMembers", adminsAndTeamMembers);
	}

	public void deleteDefaultDealOrLeadPipeLinesByCompanyId(Integer companyId) {
		String sqlString = "delete from xt_pipeline where company_id = :companyId";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sqlString);
		query.setParameter(COMPANY_ID, companyId);
		query.executeUpdate();
	}

	public void addActiveAndInActiveMasterPartnerListsToParameterList(SQLQuery query) {
		List<String> activeAndInactivePartnerLists = new ArrayList<>();
		activeAndInactivePartnerLists.add(activePartnerListName);
		activeAndInactivePartnerLists.add(inActivePartnerListName);
		query.setParameterList("activeAndInactivePartnerList", activeAndInactivePartnerLists);
	}

	/****** XNFR-276 ****/
	public boolean hasVanityAccessByUserId(Integer userId) {
		return hasModuleAccessByUserId(userId, VANITY);
	}

	public boolean hasVanityAccessByCompanyId(Integer companyId) {
		return hasModuleAccessByCompanyId(companyId, VANITY);
	}

	@SuppressWarnings("unchecked")
	public void insertTeamMemberPartnerFilterOptions() {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(TeamMember.class);
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		List<TeamMember> teamMembers = criteria.list();
		for (TeamMember teamMember : teamMembers) {
			TeamMemberPartnerFilterOption teamMemberPartnerFilterOption = new TeamMemberPartnerFilterOption();
			teamMemberPartnerFilterOption.setTeamMember(teamMember);
			teamMemberPartnerFilterOption.setTeamMemberPartnerFilterType(TeamMemberPartnerFilterType.MY_PARTNERS);
			teamMemberPartnerFilterOption.setCreatedTime(new Date());
			teamMemberPartnerFilterOption.setUpdatedTime(new Date());
			session.save(teamMemberPartnerFilterOption);
		}

	}

	public void insertQueryBuilderData() {
		Session session = sessionFactory.getCurrentSession();
		List<TriggerComponent> triggerComponents = new ArrayList<>();
		TriggerComponentType triggerComponentType = TriggerComponentType.FILTER_PROPERTY;
		addTriggerComponent(triggerComponents, "job_title", "Job Title", triggerComponentType);
		addTriggerComponent(triggerComponents, "address", "Address", triggerComponentType);
		addTriggerComponent(triggerComponents, "city", "City/Town", triggerComponentType);
		addTriggerComponent(triggerComponents, "state", "State", triggerComponentType);
		addTriggerComponent(triggerComponents, "zip", "Zip Code", triggerComponentType);
		addTriggerComponent(triggerComponents, "country", "Country", triggerComponentType);

		int total = triggerComponents.size();
		int count = 0;
		for (TriggerComponent triggerComponent : triggerComponents) {
			session.save(triggerComponent);
			count++;
			int itemsLeft = total - count;
			System.out.println("Items Left:-" + itemsLeft);
		}
		System.out.println("Data Inserted Successfully");

	}

	private void addTriggerComponent(List<TriggerComponent> triggerComponents, String key, String value,
			TriggerComponentType triggerComponentType) {
		TriggerComponent triggerComponent = new TriggerComponent();
		triggerComponent.setCreatedBy(1);
		triggerComponent.setUpdatedBy(1);
		triggerComponent.setCreatedTime(new Date());
		triggerComponent.setUpdatedTime(new Date());
		triggerComponent.setKey(key);
		triggerComponent.setValue(value);
		triggerComponent.setType(triggerComponentType);
		triggerComponents.add(triggerComponent);
	}

	public void insertTriggerOptions() {
		Session session = sessionFactory.getCurrentSession();
		List<TriggerComponent> triggerComponents = new ArrayList<>();
		TriggerComponentType triggerComponentType = TriggerComponentType.ACTION;
		addTriggerComponent(triggerComponents, "activated", "Activated", triggerComponentType);
		addTriggerComponent(triggerComponents, "signed_up", "Signed Up", triggerComponentType);
		addTriggerComponent(triggerComponents, "redistributed_campaign", "Redistributed Campaign",
				triggerComponentType);
		addTriggerComponent(triggerComponents, "created_company_profile", "Created Company Profile",
				triggerComponentType);
		addTriggerComponent(triggerComponents, "created_lead", "Created Lead", triggerComponentType);
		addTriggerComponent(triggerComponents, "created_deal", "Created Deal", triggerComponentType);
		addTriggerComponent(triggerComponents, "converted_lead", "Converted Lead", triggerComponentType);
		addTriggerComponent(triggerComponents, "closed_deal", "Closed Deal", triggerComponentType);
		addTriggerComponent(triggerComponents, "added_team_member", "Added Team Member", triggerComponentType);
		addTriggerComponent(triggerComponents, "added_contact", "Added Contact", triggerComponentType);
		addTriggerComponent(triggerComponents, "completed_track", "Completed Track", triggerComponentType);
		addTriggerComponent(triggerComponents, "completed_playbook", "Completed Playbook", triggerComponentType);
		addTriggerComponent(triggerComponents, "viewed_track", "Viewed Track", triggerComponentType);
		addTriggerComponent(triggerComponents, "viewed_playbook", "Viewed Playbook", triggerComponentType);
		addTriggerComponent(triggerComponents, "viewed_pages", "Viewed Pages", triggerComponentType);
		addTriggerComponent(triggerComponents, "requested_mdf", "Requested MDF", triggerComponentType);
		addTriggerComponent(triggerComponents, "redistributed_sharelead", "Redistributed Share Lead",
				triggerComponentType);
		int total = triggerComponents.size();
		int count = 0;
		for (TriggerComponent triggerComponent : triggerComponents) {
			session.save(triggerComponent);
			count++;
			int itemsLeft = total - count;
			System.out.println("Items Left:-" + itemsLeft);
		}
		System.out.println("Data Inserted Successfully");

	}

	public void insertTimePhrases() {
		Session session = sessionFactory.getCurrentSession();
		List<TriggerComponent> triggerComponents = new ArrayList<>();
		TriggerComponentType triggerComponentType = TriggerComponentType.TIME_PHRASE;
		addTriggerComponent(triggerComponents, "in_the_past_day", "In the past day", triggerComponentType);
		addTriggerComponent(triggerComponents, "in_the_past_7_days", "In the past 7 days", triggerComponentType);
		addTriggerComponent(triggerComponents, "in_the_past_14_days", "In the past 14 days", triggerComponentType);
		addTriggerComponent(triggerComponents, "in_the_past_28_days", "In the past 28 days", triggerComponentType);
		addTriggerComponent(triggerComponents, "in_the_past_30_days", "In the past 30 days", triggerComponentType);
		addTriggerComponent(triggerComponents, "in_the_past_60_days", "In the past 60 days", triggerComponentType);
		addTriggerComponent(triggerComponents, "in_the_past_90_days", "In the past 90 days", triggerComponentType);
		addTriggerComponent(triggerComponents, "in_the_past_year", "In the past year", triggerComponentType);
		addTriggerComponent(triggerComponents, "custom", "Custom-In the past custom days", triggerComponentType);
		int total = triggerComponents.size();
		int count = 0;
		for (TriggerComponent triggerComponent : triggerComponents) {
			session.save(triggerComponent);
			count++;
			int itemsLeft = total - count;
			System.out.println("Items Left:-" + itemsLeft);
		}
		System.out.println("Data Inserted Successfully");

	}

	public void insertSubjects() {
		Session session = sessionFactory.getCurrentSession();
		List<TriggerComponent> triggerComponents = new ArrayList<>();
		TriggerComponentType triggerComponentType = TriggerComponentType.SUBJECT;
		addTriggerComponent(triggerComponents, "partner_has", "Partner has", triggerComponentType);
		addTriggerComponent(triggerComponents, "partner_has_not", "Partner has not", triggerComponentType);
		int total = triggerComponents.size();
		int count = 0;
		for (TriggerComponent triggerComponent : triggerComponents) {
			session.save(triggerComponent);
			count++;
			int itemsLeft = total - count;
			System.out.println("Items Left:-" + itemsLeft);
		}
		System.out.println("Data Inserted Successfully");
	}

	public XtremandResponse findDuplicateTeamMemberGroups() {
		XtremandResponse response = new XtremandResponse();
		List<Integer> partnershipIds = teamDao.findPartnershipIdsFromPartnerTeamGroupMapping();
		List<PartnerTeamMemberGroupDTO> updatedPartnerTeamMemberGroupDtos = new ArrayList<>();
		Set<Integer> duplicatePartnershipIds = new HashSet<>();
		for (Integer partnershipId : partnershipIds) {
			List<PartnerTeamMemberGroupDTO> partnerTeamMemberGroupDtos = teamMemberGroupDao
					.findTeamMemberGroupIdByPartnershipId(partnershipId);
			if (partnerTeamMemberGroupDtos.size() > 1) {
				updatedPartnerTeamMemberGroupDtos.addAll(partnerTeamMemberGroupDtos);
				duplicatePartnershipIds.add(partnershipId);
			}
		}
		response.setStatusCode(200);
		Map<String, Object> map = new HashMap<>();
		map.put("dtos", updatedPartnerTeamMemberGroupDtos);
		map.put("duplicatePartnershipIds", duplicatePartnershipIds);
		response.setMap(map);
		return response;

	}

	public Integer getCompanyIdByUserId(Integer userId, Session session) {
		Query query = session.createSQLQuery("select company_id from xt_user_profile where user_id = :userId");
		query.setParameter(USER_ID, userId);
		return (Integer) query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	public List<ProcessingUserListsDTO> findProcessingUserLists() {
		String queryString = "select user_list_id as \"userListId\",user_list_name as \"userListName\",\n"
				+ "created_time as \"createdTime\",updated_time as \"updatedTime\",\n"
				+ "upload_in_progress as \"uploadInProgress\",validation_in_progress as \"zeroBounceEmailValidationInProgress\",\n"
				+ "csv_path as \"csvPath\", email_validation_ind as \"userListProcessed\" \n"
				+ "from xt_user_list where upload_in_progress or validation_in_progress order by created_time desc\n";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.setClassInstance(ProcessingUserListsDTO.class);
		return (List<ProcessingUserListsDTO>) hibernateSQLQueryResultUtilDao
				.returnDTOList(hibernateSQLQueryResultRequestDTO);

	}

	public boolean isUpdateModulesFromMyProfileOptionEnabled(Integer companyId) {
		if (XamplifyUtils.isValidInteger(companyId)) {
			String sqlString = "select update_modules_from_my_profile from xt_company_profile where company_id = :companyId ";
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(sqlString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(COMPANY_ID, companyId));
			return hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
		} else {
			return false;
		}

	}

	@SuppressWarnings("unchecked")
	public List<Integer> findRoleIdsByUserId(Integer userId) {
		String sqlString = "select distinct ur.role_id from xt_user_role ur where ur.user_id = :userId";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sqlString);
		query.setParameter(USER_ID, userId);
		return query.list();
	}

	public boolean isPartnerRole(Integer userId) {
		List<Integer> roleIds = userDao.getRoleIdsByUserId(userId);
		return roleIds != null && !roleIds.isEmpty() && roleIds.indexOf(Role.PARTNERS.getRoleId()) > -1;
	}

	public boolean isSuperVisorOrAnyAdmin(Integer userId) {
		List<Integer> roleIds = userDao.getRoleIdsByUserId(userId);
		boolean prm = roleIds.indexOf(Role.PRM_ROLE.getRoleId()) > -1;
		boolean superVisor = roleIds.indexOf(Role.ALL_ROLES.getRoleId()) > -1;
		boolean isPartner = roleIds.indexOf(Role.COMPANY_PARTNER.getRoleId()) > -1;
		return superVisor || prm || isPartner;
	}

	@SuppressWarnings("unchecked")
	public List<Integer> getDefaultMasterContactListIdByUserId(Integer userId) {
		String queryString = "SELECT DISTINCT ul.user_list_id\r\n" + "FROM xt_user_list ul\r\n"
				+ "JOIN xt_user_profile up ON ul.company_id = up.company_id\r\n" + "WHERE up.user_id =:userId\r\n"
				+ "AND CAST(ul.contact_list_type AS TEXT) =:contactListType\r\n";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.USER_ID, userId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("contactListType", ContactListTypeValue.DEFAULT_CONTACT_LIST.name()));
		return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

	/** XNFR-781 **/
	public boolean isAnyAdmin(Integer userId) {
		List<Integer> roleIds = userDao.getRoleIdsByUserId(userId);
		boolean prm = roleIds.indexOf(Role.PRM_ROLE.getRoleId()) > -1;
		boolean isPartner = roleIds.indexOf(Role.COMPANY_PARTNER.getRoleId()) > -1;
		return prm || isPartner;
	}

	/************** UNLOCK_MDF_FUNDING ********************************/
	public boolean hasUnlockMdfFundingAccessByUserId(Integer userId) {
		return hasModuleAccessByUserId(userId, UNLOCK_MDF_FUNDING);

	}

	public boolean hasUnlockMdfFundingAccessByCompanyId(Integer companyId) {
		return hasModuleAccessByCompanyId(companyId, UNLOCK_MDF_FUNDING);

	}

	/**************
	 * ALLOW_VENDOR_TO_CHANGE_THE_PRIMARY_ADMIN
	 ********************************/
	public boolean isAllowVendorToChangeThePartnerAdminOptionEnabledByUserId(Integer userId) {
		return hasModuleAccessByUserId(userId, ALLOW_VENDOR_TO_CHANGE_THE_PRIMARY_ADMIN);

	}

	public boolean isAllowVendorToChangeThePartnerAdminOptionEnabledByCompanyId(Integer companyId) {
		return hasModuleAccessByCompanyId(companyId, ALLOW_VENDOR_TO_CHANGE_THE_PRIMARY_ADMIN);

	}

	public boolean fetchModuleAccessForPartnerByModuleIdAndVendorCompanyIdAndUserId(Integer vendorCompanyId,
			Integer moduleId, Integer userId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "select coalesce( (select mc.can_partner_access_module from xt_module_custom mc "
				+ "join xt_partnership p on p.id = mc.partnership_id join xt_user_profile up on p.partner_company_id = up.company_id "
				+ "where p.vendor_company_id = :vendorCompanyId and up.user_id = :userId and mc.module_id = :moduleId), true )";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(VENDOR_COMPANY_ID, vendorCompanyId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("moduleId", moduleId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(USER_ID, userId));
		return hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

	public boolean hasModuleAccessByUserId(Integer userId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "select case when (regular or video or social or event or landing_page_campaign or survey)"
				+ " then true else false end from xt_module_access ma join xt_user_profile up "
				+ "on up.company_id = ma.company_id where up.user_id = :userId";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(USER_ID, userId));
		return hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

	public boolean hasModuleAccessByCompanyId(Integer companyId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "select case when (regular or video or social or event or landing_page_campaign or survey) "
				+ "then true else false end from xt_module_access where company_id = :companyId";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(COMPANY_ID, companyId));
		return hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

	// XNFR-914
	@SuppressWarnings("unchecked")
	public List<ModuleCustomDTO> getModulesAccessGivenByVendorForPartners(Integer vendorCompanyId,
			Integer partnerCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlString = "select xmc.module_id as \"moduleId\",xmc.custom_name as \"customName\",xmc.can_partner_access_module as \"partnerAccessModule\" from xt_module_custom xmc join xt_partnership p on xmc.partnership_id  = p.id "
				+ " where p.partner_company_id = :partnerCompanyId and p.vendor_company_id = :vendorCompanyId";
		Query query = session.createSQLQuery(sqlString);
		query.setParameter(VENDOR_COMPANY_ID, vendorCompanyId);
		query.setParameter(PARTNER_COMPANY_ID, partnerCompanyId);
		return (List<ModuleCustomDTO>) paginationUtil.getListDTO(ModuleCustomDTO.class, query);
	}

	public boolean isPartnerSignatureEnabled(String assetName) {
		Session session = sessionFactory.openSession();
		boolean result = false;
		try {
			String sql = "SELECT is_partner_signature_required FROM xt_dam WHERE asset_name = :assetName";
			Query query = session.createSQLQuery(sql).setParameter("assetName", assetName);
			Object queryResult = query.uniqueResult();

			if (queryResult != null) {
				result = (boolean) queryResult;
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
		return result;
	}

	/** XNFR-952 **/
	public Integer getTotalContactSubscriptionUsedByCompanyAndPartners(Integer companyId) {
		Integer totalContactQuota = null;
		if (XamplifyUtils.isValidInteger(companyId)) {
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			String queryString = totalContactsSubscriptionUsedByCompany;
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(COMPANY_ID, companyId));
			totalContactQuota = (Integer) hibernateSQLQueryResultUtilDao
					.getUniqueResult(hibernateSQLQueryResultRequestDTO);
		}
		return totalContactQuota == null ? 0 : totalContactQuota;
	}

	public boolean hasInsightsAccessByCompanyId(Integer companyId) {
		return hasModuleAccessByCompanyId(companyId, INSIGHTS);
	}

	@SuppressWarnings("unchecked")
	public List<Integer> findDeactivedPartnersByCompanyId(Integer companyId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "SELECT partner_id FROM xt_partnership WHERE vendor_company_id = :companyId \r\n"
				+ "AND status = 'deactivated' \r\n UNION\r\n SELECT tm.team_member_id FROM xt_partnership p \r\n"
				+ "JOIN xt_team_member tm ON tm.org_admin_id = p.partner_id WHERE p.vendor_company_id = :companyId \n"
				+ "AND p.status = 'deactivated' AND tm.team_member_id != p.partner_id";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
		return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

	public DomainColorDTO getDomainColors(Integer userId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlString = "select xdc.background_color as \"backgroundColor\", xdc.header_color as \"headerColor\","
				+ "xdc.footer_color as \"footerColor\", xdc.text_color as \"textColor\" , xdc.button_color as \"buttonColor\","
				+ "xdc.header_text_color as \"headertextColor\", xdc.footer_text_color as \"footertextColor\" "
				+ ",xdc.company_website as \"website\" , xdc.logo_color1 as \"logoColor1\" , xdc.logo_color2 as \"logoColor2\", xdc.logo_color3 as \"logoColor3\""
				+ "from xt_domain_colors xdc left join xt_user_profile xup on xup.company_id = xdc.company_id where xup.user_id = :userId";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.USER_ID, userId));
		return (DomainColorDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO,
				DomainColorDTO.class);
	}

	public CompanyProfileDTO getCompanyIdAndWebsiteByUserId(Integer userId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlString = "select up.company_id as \"id\", cp.website as \"website\" from xt_user_profile up"
				+ " join  xt_company_profile cp ON up.company_id = cp.company_id" + " where up.user_id = :userId";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.USER_ID, userId));
		return (CompanyProfileDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO,
				CompanyProfileDTO.class);
	}

	public void updateDomainColors(Integer userId, DomainColorDTO dto) {
		List<String> addQuery = new ArrayList<>();
		List<QueryParameterDTO> params = new ArrayList<>();
		if (dto.getBackgroundColor() != null) {
			addQuery.add("background_color = :backgroundColor");
			params.add(new QueryParameterDTO("backgroundColor", dto.getBackgroundColor()));
		}
		if (dto.getHeaderColor() != null) {
			addQuery.add("header_color = :headerColor");
			params.add(new QueryParameterDTO("headerColor", dto.getHeaderColor()));
		}
		if (dto.getFooterColor() != null) {
			addQuery.add("footer_color = :footerColor");
			params.add(new QueryParameterDTO("footerColor", dto.getFooterColor()));
		}
		if (dto.getTextColor() != null) {
			addQuery.add("text_color = :textColor");
			params.add(new QueryParameterDTO("textColor", dto.getTextColor()));
		}
		if (dto.getButtonColor() != null) {
			addQuery.add("button_color = :buttonColor");
			params.add(new QueryParameterDTO("buttonColor", dto.getButtonColor()));
		}
		if (dto.getWebsite() != null) {
			addQuery.add("company_website = :website");
			params.add(new QueryParameterDTO("website", dto.getWebsite()));
		}
		if (dto.getLogoColor1() != null) {
			addQuery.add("logo_color1 = :logoColor1");
			params.add(new QueryParameterDTO("logoColor1", dto.getLogoColor1()));
		}
		if (dto.getLogoColor2() != null) {
			addQuery.add("logo_color2 = :logoColor2");
			params.add(new QueryParameterDTO("logoColor2", dto.getLogoColor2()));
		}
		if (dto.getLogoColor3() != null) {
			addQuery.add("logo_color3 = :logoColor3");
			params.add(new QueryParameterDTO("logoColor3", dto.getLogoColor3()));
		}
		if (dto.getHeadertextColor() != null) {
			addQuery.add("header_text_color = :headertextColor");
			params.add(new QueryParameterDTO("headertextColor", dto.getHeadertextColor()));
		}
		if (dto.getFootertextColor() != null) {
			addQuery.add("footer_text_color = :footertextColor");
			params.add(new QueryParameterDTO("footertextColor", dto.getFootertextColor()));
		}
		if (addQuery.isEmpty()) {
			return;
		}
		String queryString = String.join(", ", addQuery);
		String sql = "UPDATE xt_domain_colors xdc SET " + queryString
				+ " FROM xt_user_profile xup WHERE xup.company_id = xdc.company_id AND xup.user_id = :userId";
		params.add(new QueryParameterDTO(XamplifyConstants.USER_ID, userId));
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(sql);
		hibernateSQLQueryResultRequestDTO.setQueryParameterDTOs(params);
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
	}

	public boolean hasMailsEnabledAccessByUserId(Integer userId) {
		return hasModuleAccessByUserId(userId, IS_MAILS_ENABLED);
	}

	public boolean hasMailsEnabledAccessAccessByCompanyId(Integer companyId) {
		return hasModuleAccessByCompanyId(companyId, IS_MAILS_ENABLED);
	}

	/** XNFR-1088 **/
	public String getVideoPath(Integer videoId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "select videouri from xt_video_files where id =:videoId ";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("videoId", videoId));
		return (String) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

	public boolean isPartnerCampaignLaunched(Integer loggedInUserCompanyId, Integer vendorCompanyId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlQueryString = "select count(*) > 0 from xt_campaign where created_for_company = :vendorCompanyId "
				+ "and customer_id in (select user_id from xt_user_profile where company_id = :companyId)";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlQueryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(VENDOR_COMPANY_ID, vendorCompanyId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(COMPANY_ID, loggedInUserCompanyId));
		return hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

	public ModuleAccess getModuleAccess(Integer companyId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			org.hibernate.Criteria criteria = session.createCriteria(ModuleAccess.class);
			return (ModuleAccess) criteria.add(Restrictions.eq("companyProfile.id", companyId)).uniqueResult();
		} catch (HibernateException | XamplifyDataAccessException e) {
			throw new XamplifyDataAccessException(e.getMessage());
		}
	}

	/********* XNFR-81 *************/
	public boolean isPartnerAddedToActivePartnerList(Integer partnerId, Integer companyId) {
		return isPartnerAddedToList(partnerId, companyId, activePartnerListName);
	}

	/********* XNFR-81 *************/
	private boolean isPartnerAddedToList(Integer partnerId, Integer companyId, String name) {
		Session session = sessionFactory.getCurrentSession();
		String sql = " select case when count(*)>0 then true else false end as result from xt_user_userlist where user_id = :partnerId and"
				+ " user_list_id = (select user_list_id from xt_user_list where company_id = :companyId and  user_list_name = :name)";
		Query query = session.createSQLQuery(sql).setParameter(PARTNER_ID, partnerId)
				.setParameter("companyId", companyId).setParameter("name", name.trim());
		return (boolean) query.uniqueResult();
	}

	/********* XNFR-81 *************/
	public boolean isPartnerAddedToInActivePartnerList(Integer partnerId, Integer companyId) {
		return isPartnerAddedToList(partnerId, companyId, inActivePartnerListName);
	}

	/********* XNFR-81 *************/
	public void updateActivePartnerToInActivePartnerList(Integer partnerId, Integer companyId) {
		String sql = "update xt_user_userlist set user_list_id = (select user_list_id from xt_user_list where company_id = :companyId and"
				+ " user_list_name = :InactiveMasterPartnerList) where user_list_id = (select user_list_id from xt_user_list where company_id = :companyId and "
				+ " user_list_name = :activeMasterPartnerList) and user_id = :partnerId";
		updateActiveAndInActivePartners(partnerId, companyId, sql, "i");
	}

	/********* XNFR-81 *************/
	public void updateInActivePartnerToActivePartnerList(Integer partnerId, Integer companyId) {
		String sql = "update xt_user_userlist set user_list_id = (select user_list_id from xt_user_list where company_id = :companyId and"
				+ " user_list_name = :activeMasterPartnerList) where user_list_id = (select user_list_id from xt_user_list where company_id = :companyId and "
				+ " user_list_name = :InactiveMasterPartnerList) and user_id = :partnerId";

		updateActiveAndInActivePartners(partnerId, companyId, sql, "a");
	}

	/********* XNFR-81 *************/
	private void updateActiveAndInActivePartners(Integer partnerId, Integer companyId, String sql, String updateType) {
		Session session = sessionFactory.openSession();
		try {
			Query query = session.createSQLQuery(sql);
			query.setParameter(PARTNER_ID, partnerId);
			query.setParameter("companyId", companyId);
			query.setParameter("InactiveMasterPartnerList", inActivePartnerListName);
			query.setParameter("activeMasterPartnerList", activePartnerListName);
			query.executeUpdate();
			session.flush();
			session.clear();
		} catch (ConstraintViolationException constraintViolationException) {
		} catch (HibernateException hibernateException) {
			hibernateException.printStackTrace();
		} catch (DataIntegrityViolationException dataIntegrityViolationException) {
		} finally {
			session.close();
		}
	}

	public boolean changeActivePartnerToInactivePartner(Integer vendorOrganizationId, Integer partnerId) {
		if (partnerId != null && partnerId > 0 && vendorOrganizationId != null && vendorOrganizationId > 0) {
			boolean isUpdated = false;
			boolean isActivePartner = isActivePartner(partnerId, vendorOrganizationId);
			boolean isPartnerExistsInActivePartnerList = isPartnerAddedToActivePartnerList(partnerId,
					vendorOrganizationId);
			if (!isActivePartner && isPartnerExistsInActivePartnerList) {
				updateActivePartnerToInActivePartnerList(partnerId, vendorOrganizationId);
				isUpdated = true;
			}
			return isUpdated;
		} else {
			return false;
		}

	}

	public boolean isActivePartner(Integer partnerId, Integer vendorCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlQueryString = "select case when count(campaign_id)>0 then true else false end as \"activePartner\" "
				+ " from xt_campaign c where c.customer_id in (select user_id from xt_user_profile where company_id = "
				+ " (select company_id from xt_user_profile where user_id=:partnerId)) "
				+ " and c.is_launched = :launched and c.vendor_organization_id = :vendorCompanyId and c.is_nurture_campaign = :nurtureCampaign";
		Query query = session.createSQLQuery(sqlQueryString).setParameter(VENDOR_COMPANY_ID, vendorCompanyId)
				.setParameter(PARTNER_ID, partnerId).setParameter("nurtureCampaign", true)
				.setParameter("launched", true);
		return (boolean) query.uniqueResult();
	}

	public boolean changeInActivePartnerToActivePartner(Integer companyId, Integer partnerId) {
		if (companyId != null && companyId > 0 && partnerId != null && partnerId > 0) {
			boolean isUpdated = false;
			if (XamplifyUtils.isValidInteger(companyId)) {
				boolean isPartnerExistsInInActivePartnerList = isPartnerAddedToInActivePartnerList(partnerId,
						companyId);
				if (isPartnerExistsInInActivePartnerList) {
					updateInActivePartnerToActivePartnerList(partnerId, companyId);
					isUpdated = true;
				}
			}
			return isUpdated;
		} else {
			return false;
		}

	}

	public Integer findMaxAdminsCountByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlString = "select max_admins from xt_module_access where company_id = :companyId";
		Query query = session.createSQLQuery(sqlString);
		query.setParameter("companyId", companyId);
		return (Integer) query.uniqueResult();
	}

	public DownloadDataInfo getDownloadDataInfo(Integer userId, DownloadItem type) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery(
				"FROM DownloadDataInfo cd WHERE cd.userId = :userId AND cd.type = :type ORDER BY cd.clickedTime DESC");
		query.setParameter("userId", userId);
		query.setParameter("type", type);
		query.setMaxResults(1);
		return (DownloadDataInfo) query.uniqueResult();
	}

	public DownloadDataInfo updateDownloadDataInfo(Integer userId, DownloadDataInfo downloadDataInfo,
			DownloadItem type) {
		Session session = sessionFactory.getCurrentSession();
		downloadDataInfo = new DownloadDataInfo();
		downloadDataInfo.setUserId(userId);
		downloadDataInfo.setDownloadInProgress(true);
		downloadDataInfo.setClickedTime(new Date());
		if (DownloadItem.CAMPAIGNS_DATA.equals(type)) {
			downloadDataInfo.setType(DownloadItem.CAMPAIGNS_DATA);
		} else if (DownloadItem.LEADS_DATA.equals(type)) {
			downloadDataInfo.setType(DownloadItem.LEADS_DATA);
		} else if (DownloadItem.DEALS_DATA.equals(type)) {
			downloadDataInfo.setType(DownloadItem.DEALS_DATA);
		} else if (DownloadItem.MASTER_PARTNER_LIST.equals(type)) {
			downloadDataInfo.setType(DownloadItem.MASTER_PARTNER_LIST);
		} else if (DownloadItem.PARTNER_LIST.equals(type)) {
			downloadDataInfo.setType(DownloadItem.PARTNER_LIST);
		} else if (DownloadItem.CONTACT_LIST.equals(type)) {
			downloadDataInfo.setType(DownloadItem.CONTACT_LIST);
		} else if (DownloadItem.SHARE_LEADS.equals(type)) {
			downloadDataInfo.setType(DownloadItem.SHARE_LEADS);
		} else if (DownloadItem.SHARED_LEADS.equals(type)) {
			downloadDataInfo.setType(DownloadItem.SHARED_LEADS);
		}
		session.save(downloadDataInfo);
		return downloadDataInfo;
	}

	public void updateDownloadDataInfo(Integer userId, String completeAmazonFilePath, DownloadItem dataType,
			Integer downloadDataInfoId) {
		Session session = sessionFactory.getCurrentSession();
		String hql = "update DownloadDataInfo cd set cd.downloadInProgress = :downloadInProgress, cd.amazonUrl = :amazonUrl, cd.downloadCompletedTime = :downloadCompletedTime where cd.id = :id";
		Query query = session.createQuery(hql);
		query.setParameter("id", downloadDataInfoId);
		query.setParameter("downloadInProgress", false);
		query.setParameter("amazonUrl", completeAmazonFilePath);
		query.setParameter("downloadCompletedTime", new Date());
		query.executeUpdate();
	}

	public String getPrmCompanyProfileName() {
		Session session = sessionFactory.getCurrentSession();
		String sqlString = "select LOWER(TRIM(cp.company_profile_name)) from xt_company_profile cp,xt_user_profile up,xt_user_role ur\r\n"
				+ "where up.company_id = cp.company_id and up.user_id = ur.user_id and ur.role_id = "
				+ Role.PRM_ROLE.getRoleId();
		Query query = session.createSQLQuery(sqlString);
		return (String) query.uniqueResult();
	}
	
	public Integer getPrmCompanyId() {
		Session session = sessionFactory.getCurrentSession();
		String sqlString = "select distinct cp.company_id from xt_company_profile cp,xt_user_profile up,xt_user_role ur\r\n"
				+ "where up.company_id = cp.company_id and up.user_id = ur.user_id and ur.role_id = "
				+ Role.PRM_ROLE.getRoleId();
		Query query = session.createSQLQuery(sqlString);
		return (Integer) query.uniqueResult();
	}

}
