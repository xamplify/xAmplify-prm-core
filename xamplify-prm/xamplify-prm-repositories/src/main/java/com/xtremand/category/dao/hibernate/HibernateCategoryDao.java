package com.xtremand.category.dao.hibernate;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xtremand.campaign.bom.FormCategoryView;
import com.xtremand.category.bom.Category;
import com.xtremand.category.bom.CategoryModule;
import com.xtremand.category.bom.CategoryModuleEnum;
import com.xtremand.category.bom.CategoryView;
import com.xtremand.category.dao.CategoryDao;
import com.xtremand.category.dto.CategoryDTO;
import com.xtremand.category.dto.CategoryItemsCountDTO;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.dam.bom.DamCategoryView;
import com.xtremand.exception.CategoryDataAccessException;
import com.xtremand.exception.DuplicateEntryException;
import com.xtremand.form.bom.FormTypeEnum;
import com.xtremand.lms.bom.LearningTrackCategoryView;
import com.xtremand.lms.bom.LearningTrackType;
import com.xtremand.lms.bom.PlaybookCategoryView;
import com.xtremand.user.bom.Role;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.XamplifyUtils;

@Repository
@Transactional
public class HibernateCategoryDao implements CategoryDao {

	private static final String MODULE_TYPE = "moduleType";

	private static final String AND_CAST_CM_CATEGORY_MODULE_TYPE_AS_TEXT_MODULE_TYPE = "and cast(cm.category_module_type as text) = :moduleType";

	private static final String CAT_COMPANY_ID_D_COMPANY_ID_AND_CAT_COMPANY_ID_CM_COMPANY_ID = "cat.company_id = d.company_id and cat.company_id = cm.company_id \r\n";

	private static final String AND_D_ID_DP_DAM_ID_AND_CAT_ID_CM_CATEGORY_ID_AND_CM_DAM_ID_D_ID_AND = "and d.id = dp.dam_id and cat.id = cm.category_id and cm.dam_id = d.id and \r\n";

	private static final String WHERE_A_ID_DP_PARTNERSHIP_ID_AND = "where a.id = dp.partnership_id and \r\n";

	private static final String SELECT_DISTINCT_CAT_ID_FROM_XT_PARTNERSHIP_A_XT_DAM_PARTNER_DP_XT_DAM_D = "select distinct cat.id from xt_partnership a,xt_dam_partner dp,xt_dam d,\r\n";

	private static final String DISTINCT_CAT_ID = " distinct cat.id ";

	private static final String USER_IDS = "userIds";

	private static final String UPDATED_USER_ID = "updatedUserId";

	private static final String UPDATED_TIME = "updatedTime";

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private PaginationUtil paginationUtil;

	@Autowired
	private UserDAO userDao;

	private static final String COMPANY_ID = "companyId";

	private static final String DESCRIPTION = "description";

	private static final String USER_ID = "userId";

	private static final String VENDOR_COMPANY_ID = "vendorCompanyId";

	private static final String ITEM_ID = "itemId";

	@Value("${categoryName.cast.query}")
	private String categoryNameCastQuery;

	@Value("${foldersByVanityFilterQuery}")
	private String foldersByVanityUrlFilterQuery;

	@Value("${partnerCampaignCountQuery}")
	private String partnerCampaignCountQueryString;

	private static final String MODULE_QUERY_STRING = " and m.category_module_type=";

	private static final String PARTNER_COMPANY_ID = "partnerCompanyId";

	private static final String CATEGORY_ID = "categoryId";

	private static final String CATEGORY_ITEMS_COUNT = "select count(*) from xt_category_module where category_id  = :categoryId and category_module_type = ";

	private static final String CATEGORY_MODULE_TYPE = " and cm.category_module_type = ";

	private static final String CATEGORY_COMPANY_QUERY = " and cm.category_id = cat.id and cat.company_id = cm.company_id ";

	private static final String CREATED_FOR_COMPANY = "createdForCompany";

	@SuppressWarnings("unchecked")
	@Override
	public List<CategoryView> findByCompanyId(Integer companyId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			return session.createCriteria(CategoryView.class).add(Restrictions.eq(COMPANY_ID, companyId)).list();
		} catch (HibernateException e) {
			throw new CategoryDataAccessException(e);
		} catch (Exception ex) {
			throw new CategoryDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> findAllCategories() {
		try {
			return sessionFactory.getCurrentSession().createSQLQuery(
					"select id,company_id,created_user_id,created_time from xt_category where is_default = true order by id desc")
					.list();
		} catch (HibernateException e) {
			throw new CategoryDataAccessException(e);
		} catch (Exception ex) {
			throw new CategoryDataAccessException(ex);
		}
	}

	@Override
	public Category findByPrimaryKey(Serializable pk, FindLevel[] levels) {
		return null;
	}

	@Override
	public Collection<Category> find(List<Criteria> criterias, FindLevel[] levels) {
		return Collections.emptyList();
	}

	@Override
	public Map<String, Object> find(List<Criteria> criterias, FindLevel[] levels, Pagination pagination) {
		if ("f".equals(pagination.getCategoryType())
				|| CategoryModuleEnum.FORM.name().equalsIgnoreCase(pagination.getCategoryType())) {
			return categoryListUtility(pagination, criterias, FormCategoryView.class);
		} else if (CategoryModuleEnum.DAM.name().equalsIgnoreCase(pagination.getCategoryType())) {
			return categoryListUtility(pagination, criterias, DamCategoryView.class);
		} else if (CategoryModuleEnum.LEARNING_TRACK.name().equalsIgnoreCase(pagination.getCategoryType())) {
			return categoryListUtility(pagination, criterias, LearningTrackCategoryView.class);
		} else if (CategoryModuleEnum.PLAY_BOOK.name().equalsIgnoreCase(pagination.getCategoryType())) {
			return categoryListUtility(pagination, criterias, PlaybookCategoryView.class);
		} else {
			return categoryListUtility(pagination, criterias, CategoryView.class);
		}

	}

	@Override
	public void save(Category category) {
		try {
			Session session = sessionFactory.getCurrentSession();
			session.save(category);
		} catch (HibernateException e) {
			throw new CategoryDataAccessException(e);
		} catch (Exception ex) {
			throw new CategoryDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CategoryDTO> listAvailableCategories(Integer companyId) {
		try {
			return sessionFactory.getCurrentSession().createSQLQuery(
					"select id as \"id\", LOWER(TRIM(name)) as \"name\" from xt_category where company_id=:companyId order by name asc")
					.setParameter(COMPANY_ID, companyId)
					.setResultTransformer(Transformers.aliasToBean(CategoryDTO.class)).list();
		} catch (HibernateException e) {
			throw new CategoryDataAccessException(e);
		} catch (Exception ex) {
			throw new CategoryDataAccessException(ex);
		}

	}

	@Override
	public CategoryView findById(Integer id) {
		try {
			Session session = sessionFactory.getCurrentSession();
			org.hibernate.Criteria criteria = session.createCriteria(CategoryView.class);
			criteria.add(Restrictions.eq("id", id));
			criteria.setProjection(Projections.projectionList().add(Projections.property("id"), "id")
					.add(Projections.property("name"), "name").add(Projections.property(DESCRIPTION), DESCRIPTION))
					.setResultTransformer(Transformers.aliasToBean(CategoryView.class));
			return (CategoryView) criteria.uniqueResult();
		} catch (HibernateException e) {
			throw new CategoryDataAccessException(e);
		} catch (Exception ex) {
			throw new CategoryDataAccessException(ex);
		}
	}

	@Override
	public void update(CategoryDTO categoryDTO) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery(
					"update Category set name = :name, description=:description,updatedTime=:updatedTime,updatedUserId=:updatedUserId where id = :id");
			query.setParameter("name", categoryDTO.getName());
			query.setParameter(DESCRIPTION, categoryDTO.getDescription());
			query.setParameter(UPDATED_TIME, new Date());
			query.setParameter(UPDATED_USER_ID, categoryDTO.getCreatedUserId());
			query.setParameter("id", categoryDTO.getId());
			query.executeUpdate();
		} catch (ConstraintViolationException con) {
			throw new DuplicateEntryException("name Already Exists");
		} catch (HibernateException e) {
			throw new CategoryDataAccessException(e);
		} catch (Exception ex) {
			throw new CategoryDataAccessException(ex);
		}
	}

	@Override
	public void deleteById(Integer categoryId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Query query = session.createQuery("delete from  Category  where id = :id");
			query.setParameter("id", categoryId);
			query.executeUpdate();
		} catch (HibernateException e) {
			throw new CategoryDataAccessException(e);
		} catch (Exception ex) {
			throw new CategoryDataAccessException(ex);
		}
	}

	@Override
	public void moveAndDeleteCategory(Integer categoryIdToDelete, Integer categoryIdToMove) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Query moveCategoryQuery = session.createQuery(
					"update CategoryModule c  set c.categoryId=:categoryIdToMove where c.categoryId=:categoryIdToDelete");
			moveCategoryQuery.setParameter("categoryIdToMove", categoryIdToMove);
			moveCategoryQuery.setParameter("categoryIdToDelete", categoryIdToDelete);
			moveCategoryQuery.executeUpdate();

			Query deleteCategoryQuery = session.createQuery("delete from  Category  where id = :id");
			deleteCategoryQuery.setParameter("id", categoryIdToDelete);
			deleteCategoryQuery.executeUpdate();
		} catch (HibernateException e) {
			throw new CategoryDataAccessException(e.getMessage());
		} catch (Exception ex) {
			throw new CategoryDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> listAllCompanyDetails(String type) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String queryString = "";
			Query query;
			if ("v".equalsIgnoreCase(type)) {
				queryString = "select company_id,company_profile_name from xt_company_profile "
						+ " where company_id in (select distinct a.company_id from "
						+ " xt_user_profile a,xt_user_role b where b.user_id = a.user_id and  b.role_id in ("
						+ Role.getAllAdminRolesInString() + ")  and a.company_id is not null order by company_id desc)"
						+ " and company_id not in (select company_id from xt_category where is_default = true)";

			} else if ("p".equalsIgnoreCase(type)) {
				queryString = "select company_id,company_profile_name from xt_company_profile "
						+ " where company_id in (select distinct a.company_id from "
						+ " xt_user_profile a,xt_user_role b where b.user_id = a.user_id and  b.role_id in (12)  and a.company_id is not null order by company_id desc)"
						+ " and company_id not in (select company_id from xt_category where is_default = true)";
			}
			query = session.createSQLQuery(queryString);
			return query.list();
		} catch (HibernateException e) {
			throw new CategoryDataAccessException(e.getMessage());
		} catch (Exception ex) {
			throw new CategoryDataAccessException(ex);
		}
	}

	@Override
	public Integer getUserIdByCompanyId(Integer companyId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String queryString = "select min(u.user_id) from xt_user_profile u,xt_user_role ur where u.company_id = :companyId and ur.role_id in ("
					+ Role.getAllAdminRolesAndPartnerRoleInString() + ") and ur.user_id = u.user_id";
			Query query = session.createSQLQuery(queryString);
			query.setParameter(COMPANY_ID, companyId);
			return (Integer) query.uniqueResult();
		} catch (HibernateException e) {
			throw new CategoryDataAccessException(e.getMessage());
		} catch (Exception ex) {
			throw new CategoryDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> listEmailTemplateIdsByCompanyId(Integer companyId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String queryString = "select id from xt_email_templates where user_id in (select user_id from xt_user_profile where company_id = :companyId)";
			Query query = session.createSQLQuery(queryString);
			query.setParameter(COMPANY_ID, companyId);
			return query.list();
		} catch (HibernateException e) {
			throw new CategoryDataAccessException(e.getMessage());
		} catch (Exception ex) {
			throw new CategoryDataAccessException(ex);
		}
	}

	@Override
	public void addCategoryModules(List<CategoryModule> categoryModules) {
		Session session = sessionFactory.getCurrentSession();
		for (CategoryModule categoryModule : categoryModules) {
			session.save(categoryModule);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> listLandingPagesByCompanyId(Integer companyId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String queryString = "select id from xt_landing_page where company_id = :companyId";
			Query query = session.createSQLQuery(queryString);
			query.setParameter(COMPANY_ID, companyId);
			return query.list();
		} catch (HibernateException e) {
			throw new CategoryDataAccessException(e.getMessage());
		} catch (Exception ex) {
			throw new CategoryDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> listCampaignIdsByCompanyId(Integer companyId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String queryString = "select campaign_id from xt_campaign where  customer_id in (select user_id from xt_user_profile where company_id = :companyId) and is_nurture_campaign = false";
			Query query = session.createSQLQuery(queryString);
			query.setParameter(COMPANY_ID, companyId);
			return query.list();
		} catch (HibernateException e) {
			throw new CategoryDataAccessException(e.getMessage());
		} catch (Exception ex) {
			throw new CategoryDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CategoryDTO> listAvailableCategoriesByUserId(Integer userId) {
		try {
			String queryString = "select id as \"id\", TRIM(name) as \"name\", is_default as \"defaultCategory\" \n"
					+ "from xt_category where company_id= (select company_id from xt_user_profile where user_id=:userId) \n"
					+ "order by name asc";
			return sessionFactory.getCurrentSession().createSQLQuery(queryString).setParameter(USER_ID, userId)
					.setResultTransformer(Transformers.aliasToBean(CategoryDTO.class)).list();
		} catch (HibernateException e) {
			throw new CategoryDataAccessException(e);
		} catch (Exception ex) {
			throw new CategoryDataAccessException(ex);
		}
	}

	@Override
	public void saveCategoryModule(CategoryModule categoryModule) {
		try {
			Session session = sessionFactory.getCurrentSession();
			session.save(categoryModule);
		} catch (HibernateException e) {
			throw new CategoryDataAccessException(e);
		} catch (Exception ex) {
			throw new CategoryDataAccessException(ex);
		}
	}

	@Override
	public Integer getCategoryIdByType(Integer itemId, String type) {
		if (itemId != null && itemId > 0) {
			Session session = sessionFactory.getCurrentSession();
			String query = "select category_id from xt_category_module where ";
			if ("E".equals(type)) {
				query += " email_template_id=:itemId";
			} else if ("L".equals(type)) {
				query += " landing_page_id=:itemId";
			} else if ("F".equals(type)) {
				query += " form_id=:itemId";
			} else if ("C".equals(type)) {
				query += " campaign_id=:itemId";
			} else if (CategoryModuleEnum.DAM.name().equals(type)) {
				query += " dam_id=:itemId";
			} else if (CategoryModuleEnum.LEARNING_TRACK.name().equals(type)) {
				query += " learning_track_id=:itemId";
			}
			return (Integer) session.createSQLQuery(query).setParameter(ITEM_ID, itemId).uniqueResult();
		} else {
			return 0;
		}

	}

	@Override
	public void updateCategoryIdByType(Integer itemId, Integer categoryId, Integer updatedUserId, String type) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "update xt_category_module  ";
		if ("E".equals(type)) {
			queryString += " set category_id=:categoryId,updated_time = :updatedTime,updated_user_id=:updatedUserId where email_template_id=:itemId";
		} else if ("L".equals(type)) {
			queryString += " set category_id=:categoryId,updated_time = :updatedTime,updated_user_id=:updatedUserId where landing_page_id=:itemId";
		} else if ("F".equals(type)) {
			queryString += " set category_id=:categoryId,updated_time = :updatedTime,updated_user_id=:updatedUserId where form_id=:itemId";
		} else if ("C".equals(type)) {
			queryString += " set category_id=:categoryId,updated_time = :updatedTime,updated_user_id=:updatedUserId where campaign_id=:itemId";
		} else if (CategoryModuleEnum.DAM.name().equals(type)) {
			queryString += " set category_id=:categoryId,updated_time = :updatedTime,updated_user_id=:updatedUserId where dam_id=:itemId";
		}
		Query query = session.createSQLQuery(queryString);
		query.setParameter(CATEGORY_ID, categoryId);
		query.setParameter(UPDATED_TIME, new Date());
		query.setParameter(UPDATED_USER_ID, updatedUserId);
		query.setParameter(ITEM_ID, itemId);
		query.executeUpdate();
	}

	@Override
	public String getCategoryName(Integer inputId, CategoryModuleEnum categoryModuleEnum) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "SELECT " + categoryNameCastQuery
				+ " from xt_category cat,xt_category_module m where cat.id = m.category_id and ";
		if (categoryModuleEnum.equals(CategoryModuleEnum.EMAIL_TEMPLATE)) {
			sql += " email_template_id=" + inputId;
		} else if (categoryModuleEnum.equals(CategoryModuleEnum.LANDING_PAGE)) {
			sql += " landing_page_id=" + inputId;
		} else if (categoryModuleEnum.equals(CategoryModuleEnum.CAMPAIGN)) {
			sql += " campaign_id=" + inputId;
		} else if (categoryModuleEnum.equals(CategoryModuleEnum.DAM)) {
			sql += " dam_id=" + inputId;
		}
		String categoryName = (String) session.createSQLQuery(sql).uniqueResult();
		if (StringUtils.hasText(categoryName)) {
			return categoryName;
		} else {
			return "";
		}
	}

	private Map<String, Object> categoryListUtility(Pagination pagination, List<Criteria> criterias, Class<?> clazz) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(clazz);
		List<Criterion> criterions = generateCriteria(criterias);
		Integer companyId = pagination.getCompanyId();
		if (companyId == null || companyId == 0) {
			companyId = userDao.getCompanyIdByUserId(pagination.getUserId());
		}
		boolean isVanityUrlFilterApplicable = pagination.isVanityUrlFilterApplicable();
		boolean isVanityUrl = pagination.isVanityUrlFilter();
		Integer teamMemberId = pagination.getTeamMemberId();
		Integer partnerCompanyId = pagination.getPartnerCompanyId();
		partnerCompanyId = setPartnerCompanyId(pagination, partnerCompanyId);
		if (teamMemberId != null && teamMemberId > 0) {
			/********
			 * This method will be called when a vendor navigates through team member
			 * campaign analytics
			 ***************/
			List<Integer> categoryIds = new ArrayList<>();
			getCategoryIdsForVanityUrlAndTeamMember(pagination, teamMemberId, categoryIds);
			if (!categoryIds.isEmpty()) {
				addCategoryIdsCriteria(criteria, categoryIds);
				if (pagination.isArchived()) {
					criteria.add(Restrictions.gt("archivedCount", 0));
				} else {
					criteria.add(Restrictions.gt("count", 0));
				}
			} else {
				return addEmptyList();
			}

		} else if ("c".equals(pagination.getCategoryType())
				|| CategoryModuleEnum.CAMPAIGN.name().equalsIgnoreCase(pagination.getCategoryType())) {
			List<Integer> categoryIds = new ArrayList<>();
			if (pagination.isArchived()) {
				criteria.add(Restrictions.gt("archivedCount", 0));
			} else {
				criteria.add(Restrictions.gt("count", 0));
			}
			if (pagination.isShowPartnerCreatedCampaigns()) {
				categoryIds = findCampaignCategoryIds(companyId);
				if (XamplifyUtils.isNotEmptyList(categoryIds)) {
					addCategoryIdsCriteria(criteria, categoryIds);
				} else {
					return addEmptyList();
				}

			} else {
				if (partnerCompanyId != null && partnerCompanyId > 0) {
					if (isVanityUrlFilterApplicable) {
						categoryIds.addAll(getVanityUrlRedistributedCampaignCategoryIds(pagination.getVendorCompanyId(),
								partnerCompanyId));
					} else {
						categoryIds.addAll(getRedistributedCampaignCategoryIdsByPartnerCompanyId(partnerCompanyId));
					}
					if (!categoryIds.isEmpty()) {
						addCategoryIdsCriteria(criteria, categoryIds);
					} else {
						return addEmptyList();
					}
				} else {
					if (isVanityUrlFilterApplicable) {
						List<Integer> userIds = userDao.listAllUserIdsByLoggedInUserId(pagination.getUserId());
						/*** Vendor Company Id Filter ****/
						List<Integer> filteredCategoryIds = getVanityUrlCampaignCategoryIdsByPartner(userIds,
								pagination.getVendorCompanyId());
						categoryIds.addAll(filteredCategoryIds);

						/********** XNFR-252 *******/
						Integer loginAsUserId = pagination.getLoginAsUserId();
						boolean loginAsPartner = XamplifyUtils.isLoginAsPartner(loginAsUserId);
						if (loginAsPartner) {
							List<Integer> vendorCompanyCategoryIdsForLoginAsPartner = getVendorCompanyCategoryIdsForLoginAsPartner(
									userIds);
							categoryIds.addAll(vendorCompanyCategoryIdsForLoginAsPartner);
						}

						if (!categoryIds.isEmpty()) {
							addCategoryIdsCriteria(criteria, categoryIds);
						} else {
							return addEmptyList();
						}
					} else if (isVanityUrl) {
						/******
						 * Exclude Redistributed Campaigns From CategoryList
						 ***************************/
						List<Integer> userIds = userDao.listAllUserIdsByLoggedInUserId(pagination.getUserId());
						List<Integer> filteredCategoryIds = getCategoryIdsExcludingRedistributingCampaings(userIds);
						categoryIds.addAll(filteredCategoryIds);
						if (!categoryIds.isEmpty()) {
							addCategoryIdsCriteria(criteria, categoryIds);
						} else {
							return addEmptyList();
						}
					} else {
						criteria.add(Restrictions.eq(COMPANY_ID, companyId));
					}
				}
			}
		} else if ("l".equals(pagination.getCategoryType())
				|| CategoryModuleEnum.LANDING_PAGE.name().equalsIgnoreCase(pagination.getCategoryType())) {
			if (partnerCompanyId != null && partnerCompanyId > 0) {
				List<Integer> categoryIds = new ArrayList<>();
				if (isVanityUrlFilterApplicable) {
					categoryIds.addAll(getVanityUrlFoldersIds(pagination.getVendorCompanyId(), partnerCompanyId));
				} else {
					categoryIds.addAll(getSharedPageCategoryIdsByPartnerCompanyId(partnerCompanyId));
				}
				if (!categoryIds.isEmpty()) {
					addCategoryIdsCriteria(criteria, categoryIds);
				} else {
					return addEmptyList();
				}
			} else {
				addCompanyIdCriteria(pagination, criteria);
			}
		} else if (CategoryModuleEnum.DAM.name().equalsIgnoreCase(pagination.getCategoryType())) {
			if (XamplifyUtils.isValidInteger(partnerCompanyId)) {
				List<Integer> categoryIds = new ArrayList<>();
				if (isVanityUrlFilterApplicable) {
					categoryIds.addAll(getVanityUrlFoldersIdsForSharedAssets(pagination.getVendorCompanyId(),
							partnerCompanyId, pagination.getUserId()));
				} else {
					categoryIds.addAll(
							getSharedAssetsCategoryIdsByPartnerCompanyId(partnerCompanyId, pagination.getUserId()));
				}
				if (!categoryIds.isEmpty()) {
					addCategoryIdsCriteria(criteria, categoryIds);
				} else {
					return addEmptyList();
				}
			} else {
				addCompanyIdCriteria(pagination, criteria);
			}

		} else if (CategoryModuleEnum.LEARNING_TRACK.name().equalsIgnoreCase(pagination.getCategoryType())) {
			if (partnerCompanyId != null && partnerCompanyId > 0) {
				List<Integer> categoryIds = new ArrayList<>();
				if (isVanityUrlFilterApplicable) {
					categoryIds.addAll(getVanityUrlFoldersIdsForSharedTracks(pagination.getVendorCompanyId(),
							partnerCompanyId, pagination.getUserId()));
				} else {
					categoryIds.addAll(
							getSharedTracksCategoryIdsByPartnerCompanyId(partnerCompanyId, pagination.getUserId()));
				}
				if (!categoryIds.isEmpty()) {
					addCategoryIdsCriteria(criteria, categoryIds);
				} else {
					return addEmptyList();
				}
			} else {
				criteria.add(Restrictions.eq(COMPANY_ID, companyId));
			}

		} else if (CategoryModuleEnum.PLAY_BOOK.name().equalsIgnoreCase(pagination.getCategoryType())) {
			if (partnerCompanyId != null && partnerCompanyId > 0) {
				List<Integer> categoryIds = new ArrayList<>();
				if (isVanityUrlFilterApplicable) {
					categoryIds.addAll(getVanityUrlFoldersIdsForSharedPlayBooks(pagination.getVendorCompanyId(),
							partnerCompanyId, pagination.getUserId()));
				} else {
					categoryIds.addAll(
							getSharedPlayBooksCategoryIdsByPartnerCompanyId(partnerCompanyId, pagination.getUserId()));
				}
				if (!categoryIds.isEmpty()) {
					addCategoryIdsCriteria(criteria, categoryIds);
				} else {
					return addEmptyList();
				}
			} else {
				criteria.add(Restrictions.eq(COMPANY_ID, companyId));
			}

		} else if (CategoryModuleEnum.EMAIL_TEMPLATE.name().equalsIgnoreCase(pagination.getCategoryType())) {
			List<Integer> categoryIds = findEmailTemplateCategoryIds(pagination);
			if (XamplifyUtils.isNotEmptyList(categoryIds)) {
				addCategoryIdsCriteria(criteria, categoryIds);
			} else {
				return addEmptyList();
			}
		} else if ("f".equals(pagination.getCategoryType())) {
			addCompanyIdCriteria(pagination, criteria);
		} else {
			criteria.add(Restrictions.eq(COMPANY_ID, companyId));
		}
		return addColumnNamesAndGetList(pagination, criteria, criterions);
	}

	private void addCompanyIdCriteria(Pagination pagination, org.hibernate.Criteria criteria) {
		criteria.add(Restrictions.eq(COMPANY_ID, pagination.getCompanyId()));
		criteria.add(Restrictions.isNull(CREATED_FOR_COMPANY));
	}

	private Integer setPartnerCompanyId(Pagination pagination, Integer partnerCompanyId) {
		if (pagination.isPartnerView() && (partnerCompanyId == null || partnerCompanyId == 0)) {
			partnerCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
			pagination.setPartnerCompanyId(partnerCompanyId);
		}
		return partnerCompanyId;
	}

	private void getCategoryIdsForVanityUrlAndTeamMember(Pagination pagination, Integer teamMemberId,
			List<Integer> categoryIds) {
		if (pagination.isVanityUrlFilterApplicable()) {
			categoryIds.addAll(
					getCampaignCategoryIdsByTeamMemberIdForVanityUrl(teamMemberId, pagination.getVendorCompanyId()));
		} else if (pagination.isVanityUrlFilter()) {
			List<Integer> teamMemberIds = new ArrayList<>();
			teamMemberIds.add(teamMemberId);
			categoryIds.addAll(getCategoryIdsExcludingRedistributingCampaings(teamMemberIds));
		} else {
			categoryIds.addAll(getCampaignCategoryIdsByTeamMemberId(teamMemberId));
		}
	}

	private Map<String, Object> addColumnNamesAndGetList(Pagination pagination, org.hibernate.Criteria criteria,
			List<Criterion> criterions) {
		List<String> columnNames = new ArrayList<>();
		columnNames.add("name");
		columnNames.add(DESCRIPTION);
		columnNames.add("emailId");
		columnNames.add("firstName");
		columnNames.add("lastName");
		columnNames.add("companyName");
		return paginationUtil.addSearchAndPaginationAndSort(pagination, criteria, criterions, columnNames,
				"createdTime", "desc");
	}

	private void addCategoryIdsCriteria(org.hibernate.Criteria criteria, List<Integer> categoryIds) {
		criteria.add(Restrictions.in("id", categoryIds));
	}

	private Map<String, Object> addEmptyList() {
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("totalRecords", 0);
		resultMap.put("list", new ArrayList<>());
		return resultMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> listEmailTemplateIdsByCategoryId(Integer categoryId) {
		Session session = sessionFactory.getCurrentSession();
		String moduleType = "'" + CategoryModuleEnum.EMAIL_TEMPLATE.name() + "'";
		String sql = "SELECT m.email_template_id from xt_category c,xt_category_module m where c.id = m.category_id and c.id="
				+ categoryId + MODULE_QUERY_STRING + moduleType;
		return session.createSQLQuery(sql).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> listFormIdsByCompanyId(Integer companyId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String queryString = "select id from xt_form where company_id=:companyId";
			Query query = session.createSQLQuery(queryString);
			query.setParameter(COMPANY_ID, companyId);
			return query.list();
		} catch (HibernateException e) {
			throw new CategoryDataAccessException(e.getMessage());
		} catch (Exception ex) {
			throw new CategoryDataAccessException(ex);
		}
	}

	@Override
	public String getCategoryNameForLandingPage(Integer inputId, CategoryModuleEnum categoryModuleEnum) {
		Session session = sessionFactory.getCurrentSession();
		String moduleType = "'" + CategoryModuleEnum.LANDING_PAGE.name() + "'";
		String sql = "SELECT " + categoryNameCastQuery
				+ " from xt_category cat,xt_category_module m where cat.id = m.category_id and landing_page_id="
				+ inputId + "" + " and cat.company_id = m.company_id  and m.category_module_type=" + moduleType;
		return (String) session.createSQLQuery(sql).uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> listLandingPageIdsByCategoryId(Integer categoryId) {
		Session session = sessionFactory.getCurrentSession();
		String moduleType = "'" + CategoryModuleEnum.LANDING_PAGE.name() + "'";
		String sql = "SELECT m.landing_page_id from xt_category c,xt_category_module m where c.id = m.category_id and c.id="
				+ categoryId + MODULE_QUERY_STRING + moduleType;
		return session.createSQLQuery(sql).list();
	}

	@Override
	public Integer getDefaultCategoryIdByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String query = "select id from xt_category where company_id=:companyId and is_default = :defaultCategory ";
		return (Integer) session.createSQLQuery(query).setParameter(COMPANY_ID, companyId)
				.setParameter("defaultCategory", true).uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> listFoldersByType(Integer userId, CategoryModuleEnum categoryModuleEnum) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String type = getModuleTypeInString(categoryModuleEnum);
			String queryString = "SELECT distinct cat.id, " + categoryNameCastQuery
					+ " from xt_category cat,xt_category_module m where cat.id = m.category_id  and cat.company_id = m.company_id  and m.category_module_type="
					+ type + " and cat.company_id=(select company_id from xt_user_profile where user_id=" + userId
					+ ")";
			Query query = session.createSQLQuery(queryString);
			return query.list();
		} catch (HibernateException e) {
			throw new CategoryDataAccessException(e.getMessage());
		} catch (Exception ex) {
			throw new CategoryDataAccessException(ex);
		}
	}

	private String getModuleTypeInString(CategoryModuleEnum categoryModuleEnum) {
		String type = "";
		if (categoryModuleEnum.equals(CategoryModuleEnum.EMAIL_TEMPLATE)) {
			type = "'" + CategoryModuleEnum.EMAIL_TEMPLATE.name() + "'";
		} else if (categoryModuleEnum.equals(CategoryModuleEnum.LANDING_PAGE)) {
			type = "'" + CategoryModuleEnum.LANDING_PAGE.name() + "'";
		} else if (categoryModuleEnum.equals(CategoryModuleEnum.FORM)) {
			type = "'" + CategoryModuleEnum.FORM.name() + "'";
		} else if (categoryModuleEnum.equals(CategoryModuleEnum.CAMPAIGN)) {
			type = "'" + CategoryModuleEnum.CAMPAIGN.name() + "'";
		} else if (categoryModuleEnum.equals(CategoryModuleEnum.DAM)) {
			type = "'" + CategoryModuleEnum.DAM.name() + "'";
		} else if (categoryModuleEnum.equals(CategoryModuleEnum.LEARNING_TRACK)) {
			type = "'" + CategoryModuleEnum.LEARNING_TRACK.name() + "'";
		} else if (categoryModuleEnum.equals(CategoryModuleEnum.PLAY_BOOK)) {
			type = "'" + CategoryModuleEnum.PLAY_BOOK.name() + "'";
		}
		return type;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> listEmailTemplateIdsByCategoryIds(List<Integer> categoryIds) {
		List<Integer> emailTemplateIds = new ArrayList<>();
		if (categoryIds != null && !categoryIds.isEmpty()) {
			Session session = sessionFactory.getCurrentSession();
			String moduleType = "'" + CategoryModuleEnum.EMAIL_TEMPLATE.name() + "'";
			String sql = "SELECT m.email_template_id from xt_category c,xt_category_module m where c.id = m.category_id and c.id in (:categoryIds) and m.category_module_type="
					+ moduleType;
			Query query = session.createSQLQuery(sql);
			query.setParameterList("categoryIds", categoryIds);
			emailTemplateIds = query.list();
		}
		return emailTemplateIds;

	}

	@Override
	public CategoryItemsCountDTO getItemsCountDetailsByCategoryId(Integer categoryId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select cast(count(email_template_id) as int) as \"emailTemplatesCount\",cast(count(form_id) as int) as \"formsCount\","
				+ "cast(count(landing_page_id) as int) as \"pagesCount\",cast(count(campaign_id) as int) as \"campaignsCount\", "
				+ "cast(count(dam_id) as int) as \"damCount\" from xt_category_module where category_id = :categoryId";
		Query query = session.createSQLQuery(queryString);
		query.setParameter(CATEGORY_ID, categoryId);
		CategoryItemsCountDTO categoryItemsCountDTO = (CategoryItemsCountDTO) paginationUtil
				.getDto(CategoryItemsCountDTO.class, query);

		String tracksOrPlayBooksQuery = "select distinct  cast(count(cm.id) as int) from xt_category_module cm,xt_learning_track lt where\r\n"
				+ "lt.id = cm.learning_track_id and lt.company_id = cm.company_id\r\n"
				+ " and cm.category_id = :categoryId and cast(lt.type as text) = :type";

		getTracksCount(categoryId, session, categoryItemsCountDTO, tracksOrPlayBooksQuery);

		getPlayBooksCount(categoryId, session, categoryItemsCountDTO, tracksOrPlayBooksQuery);

		/** XBI-1798 **/
		getFormCount(categoryId, session, categoryItemsCountDTO);

		return categoryItemsCountDTO;
	}

	private void getFormCount(Integer categoryId, Session session, CategoryItemsCountDTO categoryItemsCountDTO) {
		String formsCountQueryString = "select cast(count(xcm.*) as int) from xt_category_module xcm,xt_form xf  where xcm.category_id  = :categoryId\r\n"
				+ "and cast(xcm.\"category_module_type\" as text)  = :moduleType and xcm.company_id  = xf.company_id and xf.id  = xcm.form_id and cast(xf.\"form_type\" as text)  = :formType";
		Query formQuery = session.createSQLQuery(formsCountQueryString).setParameter(CATEGORY_ID, categoryId)
				.setParameter(MODULE_TYPE, CategoryModuleEnum.FORM.name())
				.setParameter("formType", FormTypeEnum.XAMPLIFY_FORM.name());
		Integer formsCount = (Integer) formQuery.uniqueResult();
		categoryItemsCountDTO.setFormsCount(formsCount);
	}

	private void getPlayBooksCount(Integer categoryId, Session session, CategoryItemsCountDTO categoryItemsCountDTO,
			String tracksOrPlayBooksQuery) {
		Query playBooksQuery = session.createSQLQuery(tracksOrPlayBooksQuery).setParameter(CATEGORY_ID, categoryId)
				.setParameter("type", LearningTrackType.PLAYBOOK.name());
		categoryItemsCountDTO.setPlayBooksCount((Integer) playBooksQuery.uniqueResult());
	}

	private void getTracksCount(Integer categoryId, Session session, CategoryItemsCountDTO categoryItemsCountDTO,
			String tracksQueryString) {
		Query tracksQuery = session.createSQLQuery(tracksQueryString).setParameter(CATEGORY_ID, categoryId)
				.setParameter("type", LearningTrackType.TRACK.name());
		categoryItemsCountDTO.setLmsCount((Integer) tracksQuery.uniqueResult());
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> listCampaignIdsByCategoryId(Integer categoryId) {
		Session session = sessionFactory.getCurrentSession();
		String moduleType = "'" + CategoryModuleEnum.CAMPAIGN.name() + "'";
		String sql = "SELECT m.campaign_id from xt_category c,xt_category_module m where c.id = m.category_id and c.id="
				+ categoryId + MODULE_QUERY_STRING + moduleType;
		return session.createSQLQuery(sql).list();
	}

	@Override
	public Integer getDefaultCategoryIdByUserId(Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		String query = "select id from xt_category where company_id=(select company_id from xt_user_profile where user_id =:userId) and is_default = :defaultCategory ";
		return (Integer) session.createSQLQuery(query).setParameter(USER_ID, userId)
				.setParameter("defaultCategory", true).uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getCampaignCategoryIdsByTeamMemberId(Integer teamMemberId) {
		if (teamMemberId != null && teamMemberId > 0) {
			Session session = sessionFactory.getCurrentSession();
			String moduleType = "'" + CategoryModuleEnum.CAMPAIGN.name() + "'";
			String sql = "select distinct cat.id from xt_category cat,xt_category_module cm where cm.campaign_id in (select campaign_id from xt_campaign where customer_id = "
					+ teamMemberId + ")" + CATEGORY_MODULE_TYPE + moduleType + CATEGORY_COMPANY_QUERY;
			return session.createSQLQuery(sql).list();
		} else {
			return new ArrayList<>();
		}

	}

	@Override
	public Integer getCampaignItemsCountByCategoryIdAndUserId(Integer userId, Integer categoryId,
			CategoryModuleEnum categoryModuleEnum, boolean archived) {
		Session session = sessionFactory.getCurrentSession();
		String type = getModuleTypeInString(categoryModuleEnum);
		String sql = CATEGORY_ITEMS_COUNT + type
				+ " and campaign_id in(select campaign_id from xt_campaign where customer_id = :userId and is_archived="
				+ archived + ")";
		Query query = session.createSQLQuery(sql);
		query.setParameter(USER_ID, userId);
		query.setParameter(CATEGORY_ID, categoryId);
		return query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getSharedPageCategoryIdsByPartnerCompanyId(Integer partnerCompanyId) {
		if (partnerCompanyId != null && partnerCompanyId > 0) {
			Session session = sessionFactory.getCurrentSession();
			String moduleType = "'" + CategoryModuleEnum.LANDING_PAGE.name() + "'";
			String sql = "select distinct cat.id from xt_partnership a,xt_partner_landing_page b,xt_landing_page c,"
					+ " xt_category cat,xt_category_module cm where a.id = b.partner_ship_fkey_id and a.partner_company_id = :partnerCompanyId "
					+ " and c.id = b.landing_page_fkey_id and cat.id = cm.category_id and cm.landing_page_id = c.id and "
					+ " cat.company_id = c.company_id and cat.company_id = cm.company_id " + CATEGORY_MODULE_TYPE
					+ moduleType;
			Query query = session.createSQLQuery(sql);
			query.setParameter(PARTNER_COMPANY_ID, partnerCompanyId);
			return query.list();
		} else {
			return new ArrayList<>();
		}

	}

	@Override
	public Integer getPageItemsCountByCategoryIdAndPartnerCompanyId(Integer categoryId, Integer partnerCompanyId,
			CategoryModuleEnum categoryModuleEnum) {
		Session session = sessionFactory.getCurrentSession();
		String type = getModuleTypeInString(categoryModuleEnum);
		String sql = CATEGORY_ITEMS_COUNT + type
				+ " and landing_page_id in (select c.id from xt_partnership a,xt_partner_landing_page b,xt_landing_page c where a.id = b.partner_ship_fkey_id and "
				+ " a.partner_company_id = :partnerCompanyId and c.id = b.landing_page_fkey_id)";
		Query query = session.createSQLQuery(sql);
		query.setParameter(PARTNER_COMPANY_ID, partnerCompanyId);
		query.setParameter(CATEGORY_ID, categoryId);
		return query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> listRedistributedCampaignIdsByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select campaign_id from xt_campaign where  customer_id in (select user_id from xt_user_profile where company_id = :companyId) and is_nurture_campaign = true";
		Query query = session.createSQLQuery(queryString);
		query.setParameter(COMPANY_ID, companyId);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getRedistributedCampaignCategoryIdsByPartnerCompanyId(Integer partnerCompanyId) {
		if (partnerCompanyId != null && partnerCompanyId > 0) {
			Session session = sessionFactory.getCurrentSession();
			String moduleType = "'" + CategoryModuleEnum.CAMPAIGN.name() + "'";
			String sql = "select distinct cat.id from xt_category cat,xt_category_module cm where cm.campaign_id in "
					+ partnerCampaignCountQueryString + CATEGORY_MODULE_TYPE + moduleType + CATEGORY_COMPANY_QUERY;
			Query query = session.createSQLQuery(sql);
			query.setParameter(PARTNER_COMPANY_ID, partnerCompanyId);
			return query.list();
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public Integer getRedistributedCampaignItemsCountByCategoryIdAndPartnerCompanyId(Integer categoryId,
			Integer partnerCompanyId, CategoryModuleEnum categoryModuleEnum) {
		Session session = sessionFactory.getCurrentSession();
		String type = getModuleTypeInString(categoryModuleEnum);
		String sql = CATEGORY_ITEMS_COUNT + type + " and campaign_id in" + partnerCampaignCountQueryString;
		Query query = session.createSQLQuery(sql);
		query.setParameter(PARTNER_COMPANY_ID, partnerCompanyId);
		query.setParameter(CATEGORY_ID, categoryId);
		return query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
	}

	@Override
	@SuppressWarnings("unchecked")
	public CategoryDTO getCategoryIdAndNameByType(Integer moduleTypeId, String type) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select cat.id, " + categoryNameCastQuery
				+ " from xt_category cat,xt_category_module cm where cat.id = cm.category_id and cat.company_id = cm.company_id and ";
		if ("E".equals(type)) {
			queryString += " cm.email_template_id=:itemId";
		} else if ("L".equals(type)) {
			queryString += " cm.landing_page_id=:itemId";
		} else if ("F".equals(type)) {
			queryString += " cm.form_id=:itemId";
		} else if ("C".equals(type)) {
			queryString += " cm.campaign_id=:itemId";
		}
		Query query = session.createSQLQuery(queryString);
		query.setParameter(ITEM_ID, moduleTypeId);
		List<Object[]> list = query.list();
		CategoryDTO categoryDTO = new CategoryDTO();
		for (Object[] object : list) {
			Integer categoryId = (Integer) object[0];
			String categoryName = (String) object[1];
			categoryDTO.setId(categoryId);
			categoryDTO.setName(categoryName);
		}
		return categoryDTO;
	}

	@Override
	public Integer getCampaignCategoryCountByCampaignId(Integer campaignId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select count(*) from xt_category_module where campaign_id=:campaignId";
		Query query = session.createSQLQuery(sql);
		query.setParameter("campaignId", campaignId);
		return query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getVanityUrlFoldersIds(Integer vendorCompanyId, Integer partnerCompanyId) {
		if (vendorCompanyId != null && partnerCompanyId != null && vendorCompanyId > 0 && partnerCompanyId > 0) {
			Session session = sessionFactory.getCurrentSession();
			String moduleType = "'" + CategoryModuleEnum.LANDING_PAGE.name() + "'";
			Query query = session
					.createSQLQuery(foldersByVanityUrlFilterQuery + " cm.category_module_type=" + moduleType);
			query.setParameter(PARTNER_COMPANY_ID, partnerCompanyId);
			query.setParameter(VENDOR_COMPANY_ID, vendorCompanyId);
			return query.list();
		} else {
			return new ArrayList<>();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getVanityUrlRedistributedCampaignCategoryIds(Integer vendorCompanyId,
			Integer partnerCompanyId) {
		if (XamplifyUtils.isValidInteger(partnerCompanyId) && XamplifyUtils.isValidInteger(vendorCompanyId)) {
			Session session = sessionFactory.getCurrentSession();
			String moduleType = "'" + CategoryModuleEnum.CAMPAIGN.name() + "'";
			String sql = "select distinct cat.id from xt_category cat, xt_category_module cm,xt_campaign xc,xt_campaign_partner xcp where cm.campaign_id = xc.campaign_id and xcp.campaign_id = xc.campaign_id\r\n"
					+ "and xcp.partner_company_id = :partnerCompanyId and xcp.company_id = :vendorCompanyId and cat.company_id = :vendorCompanyId and cm.category_module_type = "
					+ moduleType + CATEGORY_COMPANY_QUERY;
			Query query = session.createSQLQuery(sql);
			query.setParameter(PARTNER_COMPANY_ID, partnerCompanyId);
			query.setParameter(VENDOR_COMPANY_ID, vendorCompanyId);
			return query.list();
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public Integer getPageCategoryCountByPageId(Integer pageId) {
		Session session = sessionFactory.getCurrentSession();
		String moduleType = "'" + CategoryModuleEnum.LANDING_PAGE + "'";
		String sql = "select count(*) from xt_category_module where landing_page_id=:landingPageId and category_module_type ="
				+ moduleType;
		Query query = session.createSQLQuery(sql);
		query.setParameter("landingPageId", pageId);
		return query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
	}

	@Override
	public Integer getFormsCategroyCountByFormId(Integer formId) {
		Session session = sessionFactory.getCurrentSession();
		String moduleType = "'" + CategoryModuleEnum.FORM + "'";
		String sql = "select count(*) from xt_category_module where form_id=:formId and category_module_type ="
				+ moduleType;
		Query query = session.createSQLQuery(sql);
		query.setParameter("formId", formId);
		return query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
	}

	@Override
	public Integer getEmailTemplateCategroyCountByEmailTemplateId(Integer emailTemplateId) {
		Session session = sessionFactory.getCurrentSession();
		String moduleType = "'" + CategoryModuleEnum.EMAIL_TEMPLATE + "'";
		String sql = "select count(*) from xt_category_module where email_template_id=:emailTemplateId and category_module_type ="
				+ moduleType;
		Query query = session.createSQLQuery(sql);
		query.setParameter("emailTemplateId", emailTemplateId);
		return query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getVanityUrlCampaignCategoryIdsByPartner(List<Integer> userIds, Integer vendorCompanyId) {
		if (!userIds.isEmpty() && vendorCompanyId != null) {
			Session session = sessionFactory.getCurrentSession();
			String moduleType = "'" + CategoryModuleEnum.CAMPAIGN.name() + "'";
			String sql = " select distinct cat.id from xt_category cat,xt_category_module cm where cm.campaign_id in "
					+ " (select campaign_id from xt_campaign where customer_id in (:userIds) and "
					+ "(vendor_organization_id=:vendorCompanyId or created_for_company = :vendorCompanyId))  "
					+ CATEGORY_MODULE_TYPE + moduleType
					+ " and cm.category_id = cat.id and cat.company_id = cm.company_id";
			Query query = session.createSQLQuery(sql);
			query.setParameterList(USER_IDS, userIds);
			query.setParameter(VENDOR_COMPANY_ID, vendorCompanyId);
			return query.list();
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public Integer getCampaignItemsCountByCategoryIdAndVendorCompanyIdAndUserIds(List<Integer> userIds,
			Integer vendorCompanyId, Integer categoryId) {
		if (!userIds.isEmpty() && vendorCompanyId != null && categoryId != null) {
			Session session = sessionFactory.getCurrentSession();
			String moduleType = "'" + CategoryModuleEnum.CAMPAIGN + "'";
			String sql = CATEGORY_ITEMS_COUNT + moduleType
					+ " and campaign_id in(select campaign_id from xt_campaign where customer_id in(:userIds) "
					+ "and vendor_organization_id = :vendorOrganizationId and created_for_company is null)";
			Query query = session.createSQLQuery(sql);
			query.setParameter("vendorOrganizationId", vendorCompanyId);
			query.setParameterList(USER_IDS, userIds);
			query.setParameter(CATEGORY_ID, categoryId);
			return query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
		} else {
			return 0;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getCampaignCategoryIdsByTeamMemberIdForVanityUrl(Integer teamMemberId,
			Integer vendorCompanyId) {
		if (teamMemberId != null && teamMemberId > 0) {
			Session session = sessionFactory.getCurrentSession();
			String moduleType = "'" + CategoryModuleEnum.CAMPAIGN.name() + "'";
			String sql = "select distinct cat.id from xt_category cat,xt_category_module cm where cm.campaign_id in"
					+ " (select campaign_id from xt_campaign where customer_id = " + teamMemberId
					+ " and vendor_organization_id=" + vendorCompanyId + " and parent_campaign_id is not null)"
					+ CATEGORY_MODULE_TYPE + moduleType + CATEGORY_COMPANY_QUERY;
			return session.createSQLQuery(sql).list();
		} else {
			return new ArrayList<>();
		}

	}

	@Override
	public Integer getCampaignItemsCountByCategoryIdAndUserIdAndVendorCompanyId(Integer userId, Integer categoryId,
			CategoryModuleEnum categoryModuleEnum, Integer vendorCompanyId, boolean archived) {
		Session session = sessionFactory.getCurrentSession();
		String type = getModuleTypeInString(categoryModuleEnum);
		String sql = CATEGORY_ITEMS_COUNT + type
				+ " and campaign_id in(select campaign_id from xt_campaign where customer_id = :userId and vendor_organization_id=:vendorCompanyId and parent_campaign_id is not null and is_archived="
				+ archived + ")";
		Query query = session.createSQLQuery(sql);
		query.setParameter(USER_ID, userId);
		query.setParameter(CATEGORY_ID, categoryId);
		query.setParameter(VENDOR_COMPANY_ID, vendorCompanyId);
		return query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
	}

	@Override
	public Integer getItemsCountExcludingRedistributedCampaigns(Integer categoryId, Integer companyId,
			List<Integer> campaignIds) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select count(*) from xt_category_module where category_id = :categoryId and "
				+ "(campaign_id not in (:campaignIds) or campaign_id is null) and company_id = :companyId";
		Query query = session.createSQLQuery(sql);
		query.setParameter(COMPANY_ID, companyId);
		query.setParameter(CATEGORY_ID, categoryId);
		query.setParameterList("campaignIds", campaignIds);
		return query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
	}

	@Override
	public Integer getCampaignsCountExcludingRedistributedCampaigns(Integer categoryId, Integer companyId,
			List<Integer> campaignIds) {
		Session session = sessionFactory.getCurrentSession();
		String moduleType = "'" + CategoryModuleEnum.CAMPAIGN.name() + "'";
		String sql = "select count(*) from xt_category_module where category_id = :categoryId "
				+ "and campaign_id not in (:campaignIds)  and category_module_type = " + moduleType
				+ " and company_id = :companyId";
		Query query = session.createSQLQuery(sql);
		query.setParameter(COMPANY_ID, companyId);
		query.setParameter(CATEGORY_ID, categoryId);
		query.setParameterList("campaignIds", campaignIds);
		return query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getCategoryIdsExcludingRedistributingCampaings(List<Integer> userIds) {
		Session session = sessionFactory.getCurrentSession();
		String moduleType = "'" + CategoryModuleEnum.CAMPAIGN.name() + "'";
		String sql = "select distinct cat.id from xt_category cat,xt_category_module cm where cm.campaign_id in "
				+ " (select campaign_id from xt_campaign where customer_id in (:userIds) "
				+ "and is_nurture_campaign = false and created_for_company is null) " + CATEGORY_MODULE_TYPE
				+ moduleType + CATEGORY_COMPANY_QUERY;
		Query query = session.createSQLQuery(sql);
		query.setParameterList(USER_IDS, userIds);
		return query.list();
	}

	@Override
	public Integer getCampaignItemsCountByCategoryIdAndTeamMemberId(Integer teamMemberId, Integer categoryId,
			boolean archived) {
		Session session = sessionFactory.getCurrentSession();
		String moduleType = "'" + CategoryModuleEnum.CAMPAIGN.name() + "'";
		String sql = CATEGORY_ITEMS_COUNT + moduleType
				+ " and campaign_id in(select campaign_id from xt_campaign where customer_id = :userId and is_nurture_campaign = false and is_archived="
				+ archived + ")";
		Query query = session.createSQLQuery(sql);
		query.setParameter(USER_ID, teamMemberId);
		query.setParameter(CATEGORY_ID, categoryId);
		return query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
	}

	@Override
	public CategoryModule getCategoryModuleByLearningTrackId(Integer learningTrackId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(CategoryModule.class);
		criteria.add(Restrictions.eq("learningTrack.id", learningTrackId));
		return (CategoryModule) criteria.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getSharedAssetsCategoryIdsByPartnerCompanyId(Integer partnerCompanyId, Integer partnerId) {
		if (partnerCompanyId != null && partnerCompanyId > 0) {
			Session session = sessionFactory.getCurrentSession();
			String damPartnerMappingQueryString = SELECT_DISTINCT_CAT_ID_FROM_XT_PARTNERSHIP_A_XT_DAM_PARTNER_DP_XT_DAM_D
					+ "xt_category cat,xt_category_module cm,xt_dam_partner_mapping dpm\r\n"
					+ WHERE_A_ID_DP_PARTNERSHIP_ID_AND
					+ "a.partner_company_id = :partnerCompanyId and dpm.partner_id = :partnerId and \r\n"
					+ "dpm.dam_partner_id = dp.id\r\n"
					+ AND_D_ID_DP_DAM_ID_AND_CAT_ID_CM_CATEGORY_ID_AND_CM_DAM_ID_D_ID_AND
					+ CAT_COMPANY_ID_D_COMPANY_ID_AND_CAT_COMPANY_ID_CM_COMPANY_ID
					+ AND_CAST_CM_CATEGORY_MODULE_TYPE_AS_TEXT_MODULE_TYPE;
			String damPartnerGroupMappingQueryString = SELECT_DISTINCT_CAT_ID_FROM_XT_PARTNERSHIP_A_XT_DAM_PARTNER_DP_XT_DAM_D
					+ "xt_category cat,xt_category_module cm,xt_dam_partner_group_mapping dpgm\r\n"
					+ WHERE_A_ID_DP_PARTNERSHIP_ID_AND
					+ "a.partner_company_id = :partnerCompanyId and dpgm.dam_partner_id = dp.id\r\n"
					+ AND_D_ID_DP_DAM_ID_AND_CAT_ID_CM_CATEGORY_ID_AND_CM_DAM_ID_D_ID_AND
					+ CAT_COMPANY_ID_D_COMPANY_ID_AND_CAT_COMPANY_ID_CM_COMPANY_ID
					+ AND_CAST_CM_CATEGORY_MODULE_TYPE_AS_TEXT_MODULE_TYPE;

			String queryString = damPartnerMappingQueryString + " UNION " + damPartnerGroupMappingQueryString;
			Query query = session.createSQLQuery(queryString);
			setPartnerCompanyIdAndPartnerIdAndModuleTypeParameters(partnerCompanyId, partnerId, query);
			return query.list();
		} else {
			return new ArrayList<>();
		}

	}

	@Override
	public Integer getSharedAssetsCountByCategoryIdAndPartnerCompanyId(Integer categoryId, Integer partnerCompanyId,
			CategoryModuleEnum categoryModuleEnum, Integer loggedInUserId) {
		Session session = sessionFactory.getCurrentSession();
		Integer totalCount = 0;
		Integer damPartnerMappingCount = 0;
		Integer damPartnerGroupMappingCount = 0;
		String type = getModuleTypeInString(categoryModuleEnum);
		damPartnerMappingCount = getDamPartnerMappingCount(categoryId, partnerCompanyId, loggedInUserId, session, type);
		damPartnerGroupMappingCount = getDamPartnerGroupMappingCount(categoryId, partnerCompanyId, session, type);
		totalCount = damPartnerMappingCount + damPartnerGroupMappingCount;
		return totalCount;

	}

	private Integer getDamPartnerMappingCount(Integer categoryId, Integer partnerCompanyId, Integer loggedInUserId,
			Session session, String type) {
		Integer damPartnerMappingCount;
		String damPartnerMappingQueryString = "select cast(count(*) as int) from xt_category_module where category_id  = :categoryId and category_module_type = "
				+ type
				+ " and dam_id in (select d.id from xt_partnership a,xt_dam_partner dp,xt_dam d,xt_dam_partner_mapping dpm where a.id = dp.partnership_id and "
				+ " a.partner_company_id = :partnerCompanyId and d.id = dp.dam_id and dpm.dam_partner_id = dp.id  and dpm.partner_id = :loggedInUserId)";
		Query query = session.createSQLQuery(damPartnerMappingQueryString);
		query.setParameter(PARTNER_COMPANY_ID, partnerCompanyId);
		query.setParameter(CATEGORY_ID, categoryId);
		query.setParameter("loggedInUserId", loggedInUserId);
		damPartnerMappingCount = query.uniqueResult() != null ? ((Integer) query.uniqueResult()).intValue() : 0;
		return damPartnerMappingCount;
	}

	private Integer getDamPartnerGroupMappingCount(Integer categoryId, Integer partnerCompanyId, Session session,
			String type) {
		Integer damPartnerMappingCount;
		String damPartnerMappingQueryString = "select cast(count(*) as int) from xt_category_module where category_id  = :categoryId and category_module_type = "
				+ type
				+ " and dam_id in (select d.id from xt_partnership a,xt_dam_partner dp,xt_dam d,xt_dam_partner_group_mapping dpgm where a.id = dp.partnership_id and "
				+ " a.partner_company_id = :partnerCompanyId and d.id = dp.dam_id and dpgm.dam_partner_id = dp.id)";
		Query query = session.createSQLQuery(damPartnerMappingQueryString);
		query.setParameter(PARTNER_COMPANY_ID, partnerCompanyId);
		query.setParameter(CATEGORY_ID, categoryId);
		damPartnerMappingCount = query.uniqueResult() != null ? ((Integer) query.uniqueResult()).intValue() : 0;
		return damPartnerMappingCount;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getVanityUrlFoldersIdsForSharedAssets(Integer vendorCompanyId, Integer partnerCompanyId,
			Integer partnerId) {
		if (vendorCompanyId != null && partnerCompanyId != null && vendorCompanyId > 0 && partnerCompanyId > 0) {
			Session session = sessionFactory.getCurrentSession();
			String damPartnerMappingQueryString = SELECT_DISTINCT_CAT_ID_FROM_XT_PARTNERSHIP_A_XT_DAM_PARTNER_DP_XT_DAM_D
					+ "xt_category cat,xt_category_module cm,xt_dam_partner_mapping dpm\r\n"
					+ WHERE_A_ID_DP_PARTNERSHIP_ID_AND
					+ "a.partner_company_id = :partnerCompanyId and a.vendor_company_id = :vendorCompanyId and dpm.partner_id = :partnerId and \r\n"
					+ "dpm.dam_partner_id = dp.id\r\n"
					+ AND_D_ID_DP_DAM_ID_AND_CAT_ID_CM_CATEGORY_ID_AND_CM_DAM_ID_D_ID_AND
					+ CAT_COMPANY_ID_D_COMPANY_ID_AND_CAT_COMPANY_ID_CM_COMPANY_ID
					+ AND_CAST_CM_CATEGORY_MODULE_TYPE_AS_TEXT_MODULE_TYPE;
			String damPartnerGroupMappingQueryString = SELECT_DISTINCT_CAT_ID_FROM_XT_PARTNERSHIP_A_XT_DAM_PARTNER_DP_XT_DAM_D
					+ "xt_category  cat,xt_category_module cm,xt_dam_partner_group_mapping dpgm\r\n"
					+ WHERE_A_ID_DP_PARTNERSHIP_ID_AND
					+ "a.partner_company_id = :partnerCompanyId and a.vendor_company_id = :vendorCompanyId and dpgm.dam_partner_id = dp.id\r\n"
					+ AND_D_ID_DP_DAM_ID_AND_CAT_ID_CM_CATEGORY_ID_AND_CM_DAM_ID_D_ID_AND
					+ CAT_COMPANY_ID_D_COMPANY_ID_AND_CAT_COMPANY_ID_CM_COMPANY_ID
					+ AND_CAST_CM_CATEGORY_MODULE_TYPE_AS_TEXT_MODULE_TYPE;

			String queryString = damPartnerMappingQueryString + " UNION " + damPartnerGroupMappingQueryString;
			Query query = session.createSQLQuery(queryString);
			query.setParameter(VENDOR_COMPANY_ID, vendorCompanyId);
			setPartnerCompanyIdAndPartnerIdAndModuleTypeParameters(partnerCompanyId, partnerId, query);
			return query.list();
		} else {
			return new ArrayList<>();
		}
	}

	private void setPartnerCompanyIdAndPartnerIdAndModuleTypeParameters(Integer partnerCompanyId, Integer partnerId,
			Query query) {
		query.setParameter(PARTNER_COMPANY_ID, partnerCompanyId);
		query.setParameter("partnerId", partnerId);
		query.setParameter(MODULE_TYPE, CategoryModuleEnum.DAM.name());
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findCategoryModuleIdsForPlayBooksPatch() {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select cm.id from xt_category_module cm,xt_learning_track lt \r\n"
				+ "where lt.id = cm.learning_track_id and cast(lt.type as text) = :type";
		Query query = session.createSQLQuery(queryString);
		query.setParameter("type", LearningTrackType.PLAYBOOK.name());
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getSharedTracksCategoryIdsByPartnerCompanyId(Integer partnerCompanyId, Integer partnerId) {
		Query query = categoryIdsForTracksOrPlayBooksUtilQuery(partnerCompanyId, partnerId, DISTINCT_CAT_ID, "",
				LearningTrackType.TRACK.name(), CategoryModuleEnum.LEARNING_TRACK.name());
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getSharedPlayBooksCategoryIdsByPartnerCompanyId(Integer partnerCompanyId, Integer partnerId) {
		Query query = categoryIdsForTracksOrPlayBooksUtilQuery(partnerCompanyId, partnerId, DISTINCT_CAT_ID, "",
				LearningTrackType.PLAYBOOK.name(), CategoryModuleEnum.PLAY_BOOK.name());
		return query.list();
	}

	private Query categoryIdsForTracksOrPlayBooksUtilQuery(Integer partnerCompanyId, Integer partnerId,
			String queryPrefix, String querySuffix, String trackOrPlayBookType, String categoryModuleType) {
		Session session = sessionFactory.getCurrentSession();
		String sqlString = "select " + queryPrefix
				+ " from xt_partnership a,xt_learning_track_visibility ltv,xt_learning_track lt,\r\n"
				+ "xt_category cat,xt_category_module cm where a.id = ltv.partnership_id and\r\n"
				+ "a.partner_company_id = :partnerCompanyId and a.id = ltv.partnership_id and ltv.user_id = :partnerId\r\n"
				+ "and lt.id = ltv.learning_track_id and cat.id = cm.category_id and cm.learning_track_id = lt.id and \r\n"
				+ "cat.company_id = lt.company_id and cat.company_id = cm.company_id\r\n"
				+ "and cast(cm.category_module_type as text) = :categoryModuleType and cast(lt.type as text) = :type and ltv.is_published"
				+ querySuffix;
		Query query = session.createSQLQuery(sqlString);
		query.setParameter("type", trackOrPlayBookType);
		query.setParameter(PARTNER_COMPANY_ID, partnerCompanyId);
		query.setParameter("partnerId", partnerId);
		query.setParameter("categoryModuleType", categoryModuleType);
		return query;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getVanityUrlFoldersIdsForSharedTracks(Integer vendorCompanyId, Integer partnerCompanyId,
			Integer partnerId) {
		Query query = categoryIdsForTracksOrPlayBooksUtilQuery(partnerCompanyId, partnerId, DISTINCT_CAT_ID,
				" and a.vendor_company_id=:vendorCompanyId", LearningTrackType.TRACK.name(),
				CategoryModuleEnum.LEARNING_TRACK.name());
		query.setParameter(VENDOR_COMPANY_ID, vendorCompanyId);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getVanityUrlFoldersIdsForSharedPlayBooks(Integer vendorCompanyId, Integer partnerCompanyId,
			Integer partnerId) {
		Query query = categoryIdsForTracksOrPlayBooksUtilQuery(partnerCompanyId, partnerId, DISTINCT_CAT_ID,
				" and a.vendor_company_id=:vendorCompanyId", LearningTrackType.PLAYBOOK.name(),
				CategoryModuleEnum.PLAY_BOOK.name());
		query.setParameter(VENDOR_COMPANY_ID, vendorCompanyId);
		return query.list();
	}

	@Override
	public Integer getSharedTracksCountByCategoryIdAndPartnerCompanyId(Integer categoryId, Integer partnerCompanyId,
			Integer loggedInUserId) {
		Query query = categoryIdsForTracksOrPlayBooksUtilQuery(partnerCompanyId, loggedInUserId,
				" cast(count(cm.*) as int) ", " and cat.id = :categoryId", LearningTrackType.TRACK.name(),
				CategoryModuleEnum.LEARNING_TRACK.name());
		query.setParameter(CATEGORY_ID, categoryId);
		return (Integer) query.uniqueResult();
	}

	@Override
	public Integer getSharedPlayBooksCountByCategoryIdAndPartnerCompanyId(Integer categoryId, Integer partnerCompanyId,
			Integer loggedInUserId) {
		Query query = categoryIdsForTracksOrPlayBooksUtilQuery(partnerCompanyId, loggedInUserId,
				" cast(count(cm.*) as int) ", " and cat.id = :categoryId", LearningTrackType.PLAYBOOK.name(),
				CategoryModuleEnum.PLAY_BOOK.name());
		query.setParameter(CATEGORY_ID, categoryId);
		return (Integer) query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void updateHistoryTemplatesCategoryId(Integer categoryId, Integer damId, Integer updatedUserId) {
		Session session = sessionFactory.getCurrentSession();
		String historyTemplateQueryString = "select distinct id from xt_dam where parent_id = :damId";
		List<Integer> historyIds = session.createSQLQuery(historyTemplateQueryString).setParameter("damId", damId)
				.list();
		if (historyIds != null && !historyIds.isEmpty()) {
			String queryString = "update xt_category_module set category_id=:categoryId,updated_time = :updatedTime,updated_user_id=:updatedUserId"
					+ " where dam_id in (:historyIds) and cast(category_module_type as text) = :damType ";
			Query query = session.createSQLQuery(queryString);
			query.setParameter(CATEGORY_ID, categoryId);
			query.setParameter(UPDATED_TIME, new Date());
			query.setParameter(UPDATED_USER_ID, updatedUserId);
			query.setParameterList("historyIds", historyIds);
			query.setParameter("damType", CategoryModuleEnum.DAM.name());
			query.executeUpdate();
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getVendorCompanyCategoryIdsForLoginAsPartner(List<Integer> userIds) {
		if (!userIds.isEmpty()) {
			Session session = sessionFactory.getCurrentSession();
			String moduleType = "'" + CategoryModuleEnum.CAMPAIGN.name() + "'";
			String sql = " select distinct cat.id from xt_category cat,xt_category_module cm where cm.campaign_id in "
					+ " (select campaign_id from xt_campaign where customer_id in (:userIds) and is_nurture_campaign = false)  "
					+ CATEGORY_MODULE_TYPE + moduleType
					+ " and cm.category_id = cat.id and cat.company_id = cm.company_id";
			Query query = session.createSQLQuery(sql);
			query.setParameterList(USER_IDS, userIds);
			return query.list();
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public Integer getVendorCompanyCampaignItemsCountByUserIdsAndCategoryId(List<Integer> userIds, Integer categoryId) {
		if (!userIds.isEmpty() && categoryId != null) {
			Session session = sessionFactory.getCurrentSession();
			String moduleType = "'" + CategoryModuleEnum.CAMPAIGN + "'";
			String sql = CATEGORY_ITEMS_COUNT + moduleType
					+ " and campaign_id in(select campaign_id from xt_campaign where customer_id in(:userIds) "
					+ "and is_nurture_campaign = false and created_for_company is null)";
			Query query = session.createSQLQuery(sql);
			query.setParameterList(USER_IDS, userIds);
			query.setParameter(CATEGORY_ID, categoryId);
			return query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
		} else {
			return 0;
		}

	}

	@Override
	public Integer isDefaultFolderExistsByCompanyId(Integer companyId) {
		String sqlString = "select cast(count(*) as integer) from xt_category where company_id = :companyId and is_default";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sqlString);
		query.setParameter(COMPANY_ID, companyId);
		return query.uniqueResult() != null ? (Integer) query.uniqueResult() : 0;
	}

	@SuppressWarnings("unchecked")
	private List<Integer> findEmailTemplateCategoryIds(Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		List<Integer> partnerIds = userDao.getCompanyUsers(pagination.getCompanyId());
		String queryString = "select cm.category_id from xt_category_module cm \n where cm.email_template_id in ";

		queryString += "(SELECT id AS template_id FROM xt_email_templates WHERE user_id IN (:userIds)"
				+ " AND created_for_company_id is null) \n";

		queryString += AND_CAST_CM_CATEGORY_MODULE_TYPE_AS_TEXT_MODULE_TYPE;

		Query query = session.createSQLQuery(queryString);
		query.setParameterList(USER_IDS, partnerIds);
		query.setParameter(MODULE_TYPE, CategoryModuleEnum.EMAIL_TEMPLATE.name());
		return query.list();
	}

	@Override
	public Integer findEmailTemplateCategoryCount(Integer categoryId, Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		List<Integer> partnerIds = userDao.getCompanyUsers(pagination.getCompanyId());
		String queryString = "SELECT cast(count(xet.*) as int) FROM xt_email_templates xet \n"
				+ "LEFT JOIN xt_category_module xcm ON xcm.email_template_id = COALESCE(xet.parent_id, xet.id) \n"
				+ "LEFT JOIN xt_category xc ON xc.id = xcm.category_id \n"
				+ "WHERE xc.id = :categoryId and xet.user_id IN (:partnerIds) \n";
		queryString = " AND xet.created_for_company_id is null ";

		Query query = session.createSQLQuery(queryString);
		query.setParameter(CATEGORY_ID, categoryId);
		query.setParameterList("partnerIds", partnerIds);
		return (Integer) query.uniqueResult();
	}

	// XNFR-1073
	@SuppressWarnings("unchecked")
	private List<Integer> findCampaignCategoryIds(Integer vendorCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select cm.category_id from xt_category_module cm \n"
				+ "where cm.campaign_id in (SELECT campaign_id \n"
				+ "FROM xt_campaign WHERE created_for_company = :vendorCompanyId and is_launched = true ) \n"
				+ AND_CAST_CM_CATEGORY_MODULE_TYPE_AS_TEXT_MODULE_TYPE;
		Query query = session.createSQLQuery(queryString);
		query.setParameter(VENDOR_COMPANY_ID, vendorCompanyId);
		query.setParameter(MODULE_TYPE, CategoryModuleEnum.CAMPAIGN.name());
		return query.list();
	}

	@Override
	public Integer findPartnerCreatedCampaignsCountByCategoryId(Integer vendorCompanyId, Integer categoryId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlString = "SELECT cast(count(xca.*) as int) FROM xt_campaign xca \n"
				+ "LEFT JOIN xt_category_module cm ON cm.campaign_id = xca.campaign_id \n"
				+ "LEFT JOIN xt_category xc ON xc.id = cm.category_id \n"
				+ "WHERE xc.id = :categoryId and xca.created_for_company = :vendorCompanyId and xca.is_launched = true  \n"
				+ AND_CAST_CM_CATEGORY_MODULE_TYPE_AS_TEXT_MODULE_TYPE;
		Query query = session.createSQLQuery(sqlString);
		query.setParameter(CATEGORY_ID, categoryId);
		query.setParameter(VENDOR_COMPANY_ID, vendorCompanyId);
		query.setParameter(MODULE_TYPE, CategoryModuleEnum.CAMPAIGN.name());
		return (Integer) query.uniqueResult();
	}

	@Override
	public CategoryItemsCountDTO findCategoryCountDetails(Integer categoryId, Integer vendorCompanyId,
			boolean hasPartnerAccess) {
		String queryString = hasPartnerAccess ? "= :vendorCompanyId" : "is null";
		String sqlString = "SELECT CAST(COUNT(DISTINCT CASE WHEN xcm.category_module_type = 'DAM' "
				+ "AND xd.parent_id IS NULL AND xd.created_for_company {queryString} THEN xd.id END) AS INT) AS \"damCount\", \n"
				+ "CAST(COUNT(DISTINCT CASE WHEN xcm.category_module_type = 'EMAIL_TEMPLATE' "
				+ "AND xet.created_for_company_id {queryString} THEN xet.id END) AS INT) AS \"emailTemplatesCount\", \n"
				+ "CAST(COUNT(DISTINCT CASE WHEN xcm.category_module_type = 'CAMPAIGN' "
				+ "AND xca.created_for_company {queryString} THEN xca.campaign_id END) AS INT) AS \"campaignsCount\", \n"
				+ "CAST(COUNT(DISTINCT CASE WHEN xcm.category_module_type = 'FORM' "
				+ "AND xfm.created_for_company {queryString} THEN xfm.id END) AS INT) AS \"formsCount\", \n"
				+ "CAST(COUNT(DISTINCT CASE WHEN xcm.category_module_type = 'LANDING_PAGE' "
				+ "AND xlp.created_for_company {queryString} THEN xlp.id END) AS INT) AS \"pagesCount\" \n"
				+ "FROM xt_category xc \n" + "LEFT JOIN xt_category_module xcm ON xcm.category_id = xc.id \n"
				+ "LEFT JOIN xt_dam xd ON xd.id = xcm.dam_id \n"
				+ "LEFT JOIN xt_email_templates xet ON xet.id = xcm.email_template_id \n"
				+ "LEFT JOIN xt_campaign xca ON xca.campaign_id = xcm.campaign_id \n"
				+ "LEFT JOIN xt_form xfm ON xfm.id = xcm.form_id \n"
				+ "LEFT JOIN xt_landing_page xlp ON xlp.id = xcm.landing_page_id \n" + "WHERE xc.id = :categoryId";

		String finalQueryString = sqlString.replace("{queryString}", queryString);
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(finalQueryString);
		query.setParameter(CATEGORY_ID, categoryId);
		if (hasPartnerAccess && XamplifyUtils.isValidInteger(vendorCompanyId)) {
			query.setParameter(VENDOR_COMPANY_ID, vendorCompanyId);
		}
		CategoryItemsCountDTO categoryItemsCountDTO = (CategoryItemsCountDTO) paginationUtil
				.getDto(CategoryItemsCountDTO.class, query);

		String tracksOrPlayBooksQuery = "select distinct  cast(count(cm.id) as int) from xt_category_module cm,xt_learning_track lt where\r\n"
				+ "lt.id = cm.learning_track_id and lt.company_id = cm.company_id\r\n"
				+ " and cm.category_id = :categoryId and cast(lt.type as text) = :type";

		getTracksCount(categoryId, session, categoryItemsCountDTO, tracksOrPlayBooksQuery);

		getPlayBooksCount(categoryId, session, categoryItemsCountDTO, tracksOrPlayBooksQuery);

		return categoryItemsCountDTO;
	}

}
