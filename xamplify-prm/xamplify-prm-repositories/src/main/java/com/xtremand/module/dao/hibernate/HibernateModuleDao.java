package com.xtremand.module.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.xtremand.common.bom.Module;
import com.xtremand.common.bom.ModuleCustom;
import com.xtremand.formbeans.RoleDTO;
import com.xtremand.module.dao.ModuleDao;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.ModuleCustomDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.QueryParameterListDTO;

@Repository
public class HibernateModuleDao implements ModuleDao {

	private static final String MODULE_ID = "moduleId";

	private static final String PARTNERSHIP_ID = "partnershipId";

	private static final String COMPANY_ID = "companyId";

	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	@SuppressWarnings("unchecked")
	@Override
	public List<RoleDTO> getRoleDetailsByUserId(Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select  c.role,c.role_id,a.company_id from xt_user_profile a,xt_user_role b,xt_role c "
				+ " where b.user_id = a.user_id and b.role_id = c.role_id and a.user_id=:userId";
		List<Object[]> list = session.createSQLQuery(queryString).setParameter("userId", userId).list();
		List<RoleDTO> roleDtos = new ArrayList<>();
		if (!list.isEmpty()) {
			for (Object[] object : list) {
				RoleDTO roleDTO = new RoleDTO();
				roleDTO.setRole((String) object[0]);
				roleDTO.setSuperiorId((int) object[1]);
				if (object[2] != null) {
					roleDTO.setTotalRoles((int) object[2]);
				}
				roleDtos.add(roleDTO);
			}

		}
		return roleDtos;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Module> findModuleNames() {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Module.class);
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ModuleCustom> findModuleCustomNamesByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(ModuleCustom.class);
		criteria.add(Restrictions.eq(COMPANY_ID, companyId));
		return criteria.list();
	}

	@Override
	public ModuleCustomDTO findPartnerModuleByCompanyId(Integer companyId) {
		String queryString = "select mcn.id as \"id\",mn.module_id as \"moduleId\", mn.module_name as \"moduleName\",mcn.custom_name as \"customName\" from xt_module_custom mcn,xt_module mn where mcn.company_id = :companyId and"
				+ " mn.module_id = mcn.module_id and mn.module_id = 11 and mcn.partnership_id is null";
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter(COMPANY_ID, companyId);
		return (ModuleCustomDTO) query.setResultTransformer(Transformers.aliasToBean(ModuleCustomDTO.class))
				.uniqueResult();
	}

	/*** XNFR-127 *****/
	@Override
	public String findPartnersModuleCustomNameByUserId(Integer userId) {
		String queryString = "select  mcn.custom_name as \"customName\" from xt_module_custom mcn,xt_module mn where mcn.company_id = (select company_id from xt_user_profile where user_id = :userId) and"
				+ " mn.module_id = mcn.module_id and mn.module_id = 11 and mcn.partnership_id is null";
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
		query.setParameter("userId", userId);
		return (String) query.uniqueResult();
	}

	/*** XNFR-276 *****/

	@Override
	@SuppressWarnings("unchecked")
	public List<ModuleCustomDTO> getLeftSideItemsByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select custom_name,display_index,module_id from xt_module_custom where company_id=:companyId order by display_index ";
		List<Object[]> list = session.createSQLQuery(queryString).setParameter(COMPANY_ID, companyId).list();
		List<ModuleCustomDTO> moduleCustomNameDtos = new ArrayList<>();
		for (Object[] object : list) {
			ModuleCustomDTO moduleCustomNameDto = new ModuleCustomDTO();
			moduleCustomNameDto.setCustomName((String) object[0]);
			moduleCustomNameDto.setDisplayIndex((Integer) object[1]);
			moduleCustomNameDto.setModuleId((Integer) object[2]);
			moduleCustomNameDtos.add(moduleCustomNameDto);
		}

		return moduleCustomNameDtos;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ModuleCustom> findModuleCustomInOrder(Integer loggedInUserCompanyId, Integer partnershipId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(ModuleCustom.class);
		criteria.add(Restrictions.eq(COMPANY_ID, loggedInUserCompanyId));
		if (partnershipId != null && partnershipId > 0) {
			criteria.add(Restrictions.eq("partnership.id", partnershipId));
			criteria.add(Restrictions.eq("canPartnerAccessModule", true));
		} else {
			criteria.add(Restrictions.isNull("partnership.id"));
		}
		criteria.addOrder(Order.asc("displayIndex"));
		return criteria.list();
	}

	/************** User Guide ****************/
	@Override
	public Integer getModuleIdByModuleName(String name) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select id from xt_module where  module_name = :moduleName ";
		return (Integer) session.createSQLQuery(queryString).setParameter("moduleName", name).uniqueResult();
	}

	@Override
	public String getModuleNameById(Integer moduleId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select module_name from xt_module where  id = :moduleId ";
		return (String) session.createSQLQuery(queryString).setParameter(MODULE_ID, moduleId).uniqueResult();
	}

	/************ end user guide **************/

	@Override
	public boolean checkModuleCustom(Integer partnershipId, Integer companyId, Integer moduleId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "";
		if (partnershipId != null) {
			sql = "select CASE WHEN  count(*) > 0 THEN true ELSE false END from xt_module_custom where partnership_id=:partnershipId and company_id=:companyId and module_id=:moduleId";
		} else {
			sql = "select CASE WHEN  count(*) > 0 THEN true ELSE false END from xt_module_custom where company_id=:companyId and module_id=:moduleId and partnership_id is null;";
		}
		Query query = session.createSQLQuery(sql);
		if (partnershipId != null) {
			query.setParameter(PARTNERSHIP_ID, partnershipId);
		}
		query.setParameter(COMPANY_ID, companyId);
		query.setParameter(MODULE_ID, moduleId);
		return (boolean) query.uniqueResult();
	}

	@Override
	public void updateModulesAccess(List<Integer> restrictedModuleIds, Integer partnershipId, boolean isPartnerAccessModule) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlQueryString = "update xt_module_custom set can_partner_access_module = "+isPartnerAccessModule+" where partnership_id = :partnershipId and module_id in (:restrictedModuleIds)";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlQueryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(PARTNERSHIP_ID, partnershipId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs().add(new QueryParameterListDTO("restrictedModuleIds", restrictedModuleIds));
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> fetchRestrictedModuleIds(Integer companyId, Integer partnershipId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlQueryString = "select module_id from xt_module_custom where company_id = :companyId and partnership_id = :partnershipId and can_partner_access_module = 'false'";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlQueryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(COMPANY_ID, companyId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(PARTNERSHIP_ID, partnershipId));
		return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ModuleCustomDTO> fetchModuleCustomDTOs(List<Integer> moduleIds, Integer partnershipId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlQueryString = "select module_id as \"moduleId\", module_name as \"moduleName\", true as \"partnerAccessModule\", angular_icon as \"angularIcon\" "
				+ "from xt_module where module_id in (:moduleIds) order by module_id";
		if (XamplifyUtils.isValidInteger(partnershipId)) {
			sqlQueryString = "select m.module_id as \"moduleId\", m.module_name as \"moduleName\", coalesce(mc.can_partner_access_module, true) as \"partnerAccessModule\", m.angular_icon as \"angularIcon\" "
					+ "from xt_module m left join xt_module_custom mc on m.module_id = mc.module_id and mc.partnership_id = :partnershipId where m.module_id in (:moduleIds) order by mc.module_id";
		}
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlQueryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
				.add(new QueryParameterListDTO("moduleIds", moduleIds));
		if (XamplifyUtils.isValidInteger(partnershipId)) {
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
			.add(new QueryParameterDTO(PARTNERSHIP_ID, partnershipId));
		}
		return (List<ModuleCustomDTO>) hibernateSQLQueryResultUtilDao.getListDto(hibernateSQLQueryResultRequestDTO,
				ModuleCustomDTO.class);
	}

	@Override
	public boolean fetchModuleAccessForPartner(Integer vendorCompanyId, Integer partnerCompanyId, Integer moduleId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlQueryString = "select coalesce(mc.can_partner_access_module, true) "
				+ "from xt_module_custom mc join xt_partnership p on mc.partnership_id = p.id "
				+ "where p.partner_company_id = :partnerCompanyId and p.vendor_company_id = :vendorCompanyId and mc.module_id = :moduleId";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlQueryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("partnerCompanyId", partnerCompanyId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("vendorCompanyId", vendorCompanyId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(MODULE_ID, moduleId));
		return hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public void updateMarketingModulesAccess(List<Integer> moduleIds, Integer partnershipId,
			boolean isMarketingModule) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlQueryString = "update xt_module_custom set is_marketing_module = :isMarketingModule \n"
				+ "where partnership_id = :partnershipId and module_id in (:moduleIds)";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlQueryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("isMarketingModule", isMarketingModule));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(PARTNERSHIP_ID, partnershipId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
				.add(new QueryParameterListDTO("moduleIds", moduleIds));
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ModuleCustom> findMarketingModulesCustomInOrder(Integer loggedInUserCompanyId, Integer partnershipId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(ModuleCustom.class);
		criteria.add(Restrictions.eq(COMPANY_ID, loggedInUserCompanyId));
		if (partnershipId != null && partnershipId > 0) {
			criteria.add(Restrictions.eq("partnership.id", partnershipId));
			Criterion partnerCriteria = Restrictions.eq("canPartnerAccessModule", true);
			Criterion marketingCriteria = Restrictions.eq("marketingModule", true);
			criteria.add(Restrictions.or(partnerCriteria, marketingCriteria));
		} else {
			criteria.add(Restrictions.isNull("partnership.id"));
		}
		criteria.addOrder(Order.asc("displayIndex"));
		return criteria.list();
	}

}
