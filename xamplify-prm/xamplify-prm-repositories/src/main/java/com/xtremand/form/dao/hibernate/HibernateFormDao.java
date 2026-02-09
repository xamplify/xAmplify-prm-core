package com.xtremand.form.dao.hibernate;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.sql.JoinType;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xtremand.category.bom.CategoryModuleEnum;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.exception.DuplicateEntryException;
import com.xtremand.form.bom.Form;
import com.xtremand.form.bom.FormLabel;
import com.xtremand.form.bom.FormLabelChoice;
import com.xtremand.form.bom.FormLabelType;
import com.xtremand.form.bom.FormSubTypeEnum;
import com.xtremand.form.bom.FormTeamGroupMapping;
import com.xtremand.form.bom.FormTypeEnum;
import com.xtremand.form.dao.FormDao;
import com.xtremand.form.dto.FormChoiceDTO;
import com.xtremand.form.dto.FormDTO;
import com.xtremand.form.dto.FormLabelDTO;
import com.xtremand.form.exception.FormDataAccessException;
import com.xtremand.form.submit.bom.FormSubmit;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.integration.dto.CustomChoicesDTO;
import com.xtremand.lms.bom.LearningTrackType;
import com.xtremand.lms.dao.LMSDAO;
import com.xtremand.salesforce.dto.OpportunityFormFieldsDTO;
import com.xtremand.user.bom.User;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.util.DateUtils;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.XamplifyConstants;
import com.xtremand.white.labeled.dao.WhiteLabeledFormDao;
import com.xtremand.white.labeled.dto.WhiteLabeledContentDTO;

@Repository
@Transactional
public class HibernateFormDao implements FormDao {

	private static final String FORM_ENTITY_ID = "form.id";

	private static final String LEARNING_TRACK_ID = "learningTrackId";

	private static final String COMPANY_ID = "companyId";

	private static final String USER_ID = "userId";

	private static final String LANDING_PAGE_ID = "landingPageId";

	private static final String ALIAS_PARAMETER = "alias";

	private static final String FORMS = "forms";

	private static final String AND_LOWER_F_FORM_NAME_LIKE_LOWER = " and  LOWER(f.form_name) like LOWER(";

	private static final String AND = " and ";

	private static final String IS_FORM_SUB_TYPE_SURVEY = "f.form_sub_type = 'SURVEY'";

	private static final String SURVEY = "survey";

	static final Logger logger = LoggerFactory.getLogger(HibernateFormDao.class);

	private static final String IS_QUIZ_FORM_IS_TRUE = "f.is_quiz_form is true";

	private static final String CATEGORY_ID_AND_COMPANY_ID = " where  cat.id = cm.category_id and cat.company_id = ";

	private static final String CATEGORY_MODULE_TYPE = " and cm.category_module_type = ";

	private static final String FORM_ID_AND_USER_ID_AND_COMPANY_ID = " and cm.form_id = f.id and  u.user_id = f.created_user_id  and  u.company_id = cp.company_id and f.company_id = cp.company_id and f.company_id=";

	private static final String REGULAR = "regular";

	private static final String IS_REGULAR_FORM = "f.is_quiz_form is false and f.form_sub_type = 'REGULAR'";

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private GenericDAO genericDao;

	@Autowired
	private LMSDAO lmsDao;

	@Autowired
	private WhiteLabeledFormDao whiteLabeledFormDao;

	@Autowired
	private PaginationUtil paginationUtil;

	private static final String FORM_ID = "formId";

	@Value("${categoryName.cast.query}")
	private String categoryNameCastQuery;

	@Value("${web_url}")
	private String webUrl;
	
	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	@Override
	public void save(Form form) {
		sessionFactory.getCurrentSession().save(form);
	}

	@Override
	public FormLabelType findByType(String type) {
		Session session = sessionFactory.getCurrentSession();
		return (FormLabelType) session.createCriteria(FormLabelType.class).add(Restrictions.eq("labelType", type))
				.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> listFormNamesByCompanyId(Integer userId, String companyProfileName) {
//		String query = "select LOWER(TRIM(form_name)) from xt_form where company_id=:companyId";
//		Integer companyId = userDao.getCompanyIdByUserId(userId);
//		if (companyId != null) {
//			return sessionFactory.getCurrentSession().createSQLQuery(query)
//					.setParameter(XamplifyConstants.COMPANY_ID, companyId).list();
//		} else {
//			return Collections.emptyList();
//		}

		HibernateSQLQueryResultRequestDTO dto = new HibernateSQLQueryResultRequestDTO();
		String queryString = "select LOWER(TRIM(form_name)) from xt_form f join xt_user_profile up"
				+ " on f.company_id = up.company_id where up.user_id=:userId";
		dto.setQueryString(queryString);
		dto.getQueryParameterDTOs().add(new QueryParameterDTO("userId", userId));
		return (List<String>) hibernateSQLQueryResultUtilDao.returnList(dto);
	}

	@Override
	public Form findByPrimaryKey(Serializable pk, FindLevel[] levels) {
		return null;
	}

	@Override
	public Collection<Form> find(List<Criteria> criterias, FindLevel[] levels) {
		return Collections.emptyList();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> find(List<Criteria> criterias, FindLevel[] levels, Pagination pagination) {
		Map<String, Object> map = new HashMap<>();
		Session session = sessionFactory.getCurrentSession();
		String finalQueryString;
		boolean isNurtureCampaign = false;
		String sortQueryString = getSelectedSortOption(pagination);
		Integer companyId = userDao.getCompanyIdByUserId(pagination.getUserId());
		Integer loggedInUserCompanyId = companyId;
		Integer campaignId = pagination.getCampaignId();
		String finalQuery = "";
		String filterQuery = "";
		String categoryModuleType = "'" + CategoryModuleEnum.FORM + "'";
		String formType = setFormType(pagination);
		finalQuery = generateQuery(pagination, companyId, campaignId, categoryModuleType, formType);
		filterQuery = addFilterQuery(pagination, filterQuery);
		if (StringUtils.hasText(filterQuery)) {
			finalQuery = finalQuery + AND + filterQuery;
		}
		finalQueryString = addSearchQueryString(pagination, sortQueryString, finalQuery);

		SQLQuery query = session.createSQLQuery(finalQueryString);
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		List<Object[]> list = query.list();
		List<FormDTO> formDtos = new ArrayList<>();
		updateList(pagination, isNurtureCampaign, loggedInUserCompanyId, campaignId, list, formDtos);
		map.put(XamplifyConstants.TOTAL_RECORDS, totalRecords);
		map.put(FORMS, formDtos);
		return map;

	}

	private String generateQuery(Pagination pagination, Integer companyId, Integer campaignId,
			String categoryModuleType, String formType) {
		String finalQuery;
		String formQuery = "select distinct f.id,f.form_name,f.created_time,f.updated_time,u.firstname,u.lastname,u.email_id,"
				+ " cp.company_name,f.alias,f.description,u.user_id,f.thumbnail_image,f.is_quiz_form, "
				+ categoryNameCastQuery + ",f.form_sub_type"
				+ " from xt_user_profile u,xt_form f,xt_company_profile cp,xt_category cat,xt_category_module cm ";
		String whereQuery = CATEGORY_ID_AND_COMPANY_ID + companyId + CATEGORY_MODULE_TYPE + categoryModuleType
				+ FORM_ID_AND_USER_ID_AND_COMPANY_ID + +companyId + " and f.is_default = false and f.form_type in ("
				+ formType + " )";
		if (pagination.getCategoryId() != null && pagination.getCategoryId() > 0) {
			whereQuery += " and cat.id = " + pagination.getCategoryId();
		}
		if (pagination.isCampaignForm()) {
			finalQuery = formQuery + "," + "xt_form_campaign fc " + whereQuery
					+ " and f.id = fc.form_id and fc.campaign_id=" + campaignId;
		} else if (pagination.isLandingPageForm() || pagination.isPartnerLandingPageForm()) {
			finalQuery = formQuery + "," + "xt_form_landing_page fl " + whereQuery
					+ " and f.id = fl.form_id and fl.landing_page_id=" + pagination.getLandingPageId();
		} else if (pagination.isLandingPageCampaignForm()) {
			finalQuery = formQuery + "," + "xt_campaign c,xt_form_landing_page fl" + whereQuery + " and c.campaign_id ="
					+ campaignId + " and c.landing_page_id = fl.landing_page_id and f.id = fl.form_id";
		} else if (pagination.isSurveyCampaignForm()) {
			finalQuery = formQuery + "," + "xt_campaign_url cu " + whereQuery
					+ " and f.id = cu.form_id and f.form_sub_type = 'SURVEY' and cu.campaign_id=" + campaignId;
		} else {
			finalQuery = formQuery + whereQuery;
		}

		finalQuery += " and f.created_for_company is null ";

		return finalQuery;
	}

	private String setFormType(Pagination pagination) {
		String formType = "'" + FormTypeEnum.XAMPLIFY_FORM + "'";
		return formType;
	}

	private String addSearchQueryString(Pagination pagination, String sortQueryString, String finalQuery) {
		String finalQueryString;
		if (StringUtils.hasText(pagination.getSearchKey())) {
			String searchKey = "'%" + pagination.getSearchKey() + "%'";
			String searchQueryString = AND_LOWER_F_FORM_NAME_LIKE_LOWER + searchKey + ") ";
			finalQueryString = finalQuery + searchQueryString + sortQueryString;
		} else {
			finalQueryString = finalQuery + sortQueryString;
		}
		return finalQueryString;
	}

	private String addFilterQuery(Pagination pagination, String filterQuery) {
		filterQuery = setFilterQuery(pagination, filterQuery);
		return filterQuery;
	}

	private void updateList(Pagination pagination, boolean isNurtureCampaign, Integer loggedInUserCompanyId,
			Integer campaignId, List<Object[]> list, List<FormDTO> formDtos) {
		for (Object[] row : list) {
			FormDTO formDto = new FormDTO();
			formDto.setId((Integer) row[0]);
			formDto.setName((String) row[1]);
			formDto.setCreatedDateString(DateUtils.getUtcString((Date) row[2]));
			formDto.setUpdatedDateString(DateUtils.getUtcString((Date) row[3]));
			String firstName = (String) row[4];
			String lastName = (String) row[5];
			String emailId = (String) row[6];
			User user = new User();
			user.setFirstName(firstName);
			user.setLastName(lastName);
			user.setEmailId(emailId);
			formDto.setCreatedName(XamplifyUtils.setDisplayName(user));
			formDto.setCompanyName((String) row[7]);
			formDto.setAlias((String) row[8]);
			formDto.setDescription((String) row[9]);
			formDto.setUserId((Integer) row[10]);
			formDto.setThumbnailImage((String) row[11]);
			formDto.setQuizForm((boolean) row[12]);
			formDto.setCategoryName((String) row[13]);
			formDto.setFormSubType(FormSubTypeEnum.valueOf((String) row[14]));
			String formAlias = formDto.getAlias();
			setFormAliasAndEmbedUrl(pagination, formDto, formAlias);
			formDto.setCount(getSubmittedCountByFormId(formDto.getId()));
			Integer trackCount = lmsDao.getTracksCountByFormId(formDto.getId());
			if (trackCount > 0) {
				formDto.setAssociatedWithTrack(true);
			}
			/********** XNFR-255 *******/
			WhiteLabeledContentDTO whiteLabeledContentDTO = whiteLabeledFormDao
					.findSharedByVendorCompanyNameByForm(formDto.getId());
			if (whiteLabeledContentDTO != null) {
				formDto.setWhiteLabeledFormReceivedFromVendor(true);
				formDto.setWhiteLabeledFormSharedByVendorCompanyName(
						whiteLabeledContentDTO.getWhiteLabeledContentSharedByVendorCompanyName());
			}
			/********** XNFR-255 *******/
			formDtos.add(formDto);
		}
	}

	private void setFormAliasAndEmbedUrl(Pagination pagination, FormDTO formDto, String formAlias) {
		String updatedFormAlias = "";

		if (pagination.isVanityUrlFilter()) {
			updatedFormAlias = XamplifyUtils.frameVanityURL(webUrl,
					userDao.getCompanyProfileNameByUserId(pagination.getUserId()));
		} else {
			updatedFormAlias = webUrl;
		}

		updatedFormAlias = webUrl + "f/" + formAlias;
		formDto.setAilasUrl(updatedFormAlias);
		formDto.setEmbedUrl("<iframe width=\"1000\" height=\"720\" src=" + updatedFormAlias
				+ " frameborder=\"0\" allowfullscreen ></iframe>");
	}

	private String getSelectedSortOption(Pagination pagination) {
		String sortOptionQueryString = " order by ";
		if (StringUtils.hasText(pagination.getSortcolumn())) {
			if ("name".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += " f.form_name " + pagination.getSortingOrder();
			} else if ("createdTime".equals(pagination.getSortcolumn())) {
				sortOptionQueryString += "f.created_time " + pagination.getSortingOrder();
			} else if ("updatedTime".equals(pagination.getSortcolumn())) {
				if ("DESC".equals(pagination.getSortingOrder())) {
					sortOptionQueryString += "f.updated_time " + pagination.getSortingOrder() + " nulls last";
				} else {
					sortOptionQueryString += "f.updated_time " + pagination.getSortingOrder() + " nulls first";
				}
			} else {
				sortOptionQueryString += "f.created_time desc";
			}
		} else {
			sortOptionQueryString += "f.created_time desc";
		}
		return sortOptionQueryString;
	}

	@Override
	@SuppressWarnings("unchecked")
	public XtremandResponse deleteById(Integer id, XtremandResponse response) {
		Session session = sessionFactory.getCurrentSession();
		Form form = genericDao.get(Form.class, id);
		if (form != null) {
			try {
				String formSqlString = "select l.name from xt_landing_page l,xt_form_landing_page fl where l.id = fl.landing_page_id and fl.form_id=:formId";
				List<String> assoicatedLandingPages = session.createSQLQuery(formSqlString)
						.setParameter(FORM_ID, form.getId()).list();
				if (assoicatedLandingPages.isEmpty()) {
					String campaignFormSqlString = "select distinct campaign_name from xt_campaign xc where campaign_id in (select campaign_id from xt_form_campaign where form_id = :formId)";
					List<String> campaignNames = session.createSQLQuery(campaignFormSqlString).setParameter(FORM_ID, id)
							.list();
					if (campaignNames.isEmpty()) {
						checkTracksOrPlayBooksFormOrDeleteForms(id, response, session, form);
					} else {
						setErrorMessageData(response, campaignNames, "Campaign(s)");
					}

				} else {
					setErrorMessageData(response, assoicatedLandingPages, "Page(s)");
				}
			} catch (Exception ex) {
				throw new FormDataAccessException(ex);
			}
		} else {
			response.setStatusCode(404);
			response.setMessage("Form does not exists");

		}
		return response;

	}

	private void checkTracksOrPlayBooksFormOrDeleteForms(Integer id, XtremandResponse response, Session session,
			Form form) {
		Integer count = 0;
		count = lmsDao.getTracksCountByFormId(form.getId());
		if (count < 1) {
			checkSurveyCampaignFormOrDelete(id, response, session, form);
		} else {
			checkTracksOrPlayBookForms(id, response, session);
		}
	}

	@SuppressWarnings("unchecked")
	private void checkTracksOrPlayBookForms(Integer id, XtremandResponse response, Session session) {
		String trackNamesSQLString = "select distinct xlt.title from xt_learning_track xlt,xt_learning_track_content xltc\r\n"
				+ "where xltc.quiz_id =:formId and xlt.id = xltc.learning_track_id and cast(xlt.type as text) = :type";

		List<String> trackNames = session.createSQLQuery(trackNamesSQLString).setParameter(FORM_ID, id)
				.setParameter("type", LearningTrackType.TRACK.name()).list();

		List<String> playBookNames = session.createSQLQuery(trackNamesSQLString).setParameter(FORM_ID, id)
				.setParameter("type", LearningTrackType.PLAYBOOK.name()).list();
		if (XamplifyUtils.isNotEmptyList(trackNames)) {
			setErrorMessageData(response, trackNames, "Track(s)");
		} else if (XamplifyUtils.isNotEmptyList(playBookNames)) {
			setErrorMessageData(response, trackNames, "Play book(s)");
		}
	}

	private void checkSurveyCampaignFormOrDelete(Integer id, XtremandResponse response, Session session, Form form) {
		String surveySqlString = "select cast(count(campaign_id) as int) from xt_campaign_url where form_id=:formId";
		int surveyCampaignCount = (int) session.createSQLQuery(surveySqlString).setParameter(FORM_ID, form.getId())
				.uniqueResult();
		if (surveyCampaignCount < 1) {
			deleteFromUserListAndFormTables(response, session, form);
		} else {
			addSurveyCampaignNamesErrorMessage(id, response, session);
		}
	}

	@SuppressWarnings("unchecked")
	private void addSurveyCampaignNamesErrorMessage(Integer id, XtremandResponse response, Session session) {
		List<String> surveyCampaignNames = session
				.createSQLQuery("select  distinct xc.campaign_name from xt_campaign xc,xt_campaign_url xcu\r\n"
						+ "where xc.campaign_id = xcu.campaign_id and xcu.form_id = :formId")
				.setParameter(FORM_ID, id).list();
		setErrorMessageData(response, surveyCampaignNames, "Survey Campaign(s)");
		response.setStatusCode(400);
	}

	private void deleteFromUserListAndFormTables(XtremandResponse response, Session session, Form form) {
		String deleteULSqlString = "delete from xt_user_list where user_list_id not in "
				+ "(select distinct(user_list_id) from xt_user_userlist) and form_id = :formId";
		session.createSQLQuery(deleteULSqlString).setParameter(FORM_ID, form.getId()).executeUpdate();
		String deleteSqlString = "delete from xt_form where id=:formId";
		session.createSQLQuery(deleteSqlString).setParameter(FORM_ID, form.getId()).executeUpdate();
		response.setMessage(form.getFormName() + " is deleted successfully");
		response.setStatusCode(200);
	}

	private void setErrorMessageData(XtremandResponse response, List<String> assoicatedLandingPages,
			String errorMessage) {
		String message = "This form is being used in following " + errorMessage;
		response.setMessage(message);
		response.setStatusCode(403);
		List<String> allData = new ArrayList<>();
		allData.addAll(assoicatedLandingPages);
		response.setData(allData);
	}

	@Override
	public Form getById(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		Form form = session.get(Form.class, id);
		return getFormChilds(form);

	}

	private Form getFormChilds(Form form) {
		if (form != null) {
			Hibernate.initialize(form.getFormLabels());
			Collections.sort(form.getFormLabels());
			return form;
		}
		return form;
	}

	@Override
	public Form getByAlias(String alias) {
		Session session = sessionFactory.getCurrentSession();
		Form form = (Form) session.createCriteria(Form.class).add(Restrictions.eq(ALIAS_PARAMETER, alias))
				.uniqueResult();
		return getFormChilds(form);

	}

	@Override
	public FormLabel getFormLabelId(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		return (FormLabel) session.createCriteria(FormLabel.class).add(Restrictions.eq("id", id)).uniqueResult();
	}

	@Override
	public void update(Form form) {
		sessionFactory.getCurrentSession().update(form);
	}

	@Override
	public void deleteFormLabelByIds(List<Integer> ids) {
		if (!ids.isEmpty()) {
			Session session = sessionFactory.getCurrentSession();
			String queryString = "delete from xt_form_label where  id in (:ids)";
			Query query = session.createSQLQuery(queryString);
			query.setParameterList("ids", ids);
			query.executeUpdate();
		}
	}

	@Override
	public void save(FormLabel formLabel) {
		sessionFactory.getCurrentSession().save(formLabel);
	}

	@Override
	public void update(FormLabel formLabel) {
		sessionFactory.getCurrentSession().update(formLabel);
	}

	@Override
	public void deleteFormChoiceByIds(List<Integer> ids) {
		if (!ids.isEmpty()) {
			Session session = sessionFactory.getCurrentSession();
			String queryString = "delete from xt_form_label_choice where  id in (:ids)";
			Query query = session.createSQLQuery(queryString);
			query.setParameterList("ids", ids);
			query.executeUpdate();
		}

	}

	@Override
	public void update(FormLabelChoice formLabelChoice) {
		sessionFactory.getCurrentSession().update(formLabelChoice);
	}

	@Override
	public FormLabelChoice getFormLabelChoiceById(Integer choiceId) {
		Session session = sessionFactory.getCurrentSession();
		return (FormLabelChoice) session.createCriteria(FormLabelChoice.class).add(Restrictions.eq("id", choiceId))
				.uniqueResult();
	}

	@Override
	public void save(FormLabelChoice formLabelChoice) {
		sessionFactory.getCurrentSession().save(formLabelChoice);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FormLabelDTO> listFormLabelsById(Integer id, boolean totalLeads, boolean mdfForm,
			Integer additionalColumnsSize) {
		List<FormLabelDTO> formLabelDTOs = new ArrayList<>();
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(
				"select label_name,label_order,is_required,column_order,id from xt_form_label where form_id = :id order by label_order,column_order asc");
		query.setParameter("id", id);
		List<Object[]> listFormLabelsByFormId = query.list();
		if (totalLeads) {
			addPartnerOrVendorColumns(formLabelDTOs, 1, "Email");
			addPartnerOrVendorColumns(formLabelDTOs, 2, "First Name");
			addPartnerOrVendorColumns(formLabelDTOs, 3, "Last Name");
			addPartnerOrVendorColumns(formLabelDTOs, 4, "Company Name");
		}
		for (Object[] row : listFormLabelsByFormId) {
			String labelName = (String) row[0];
			boolean isRequired = (boolean) row[2];
			FormLabelDTO formLabelDTO = new FormLabelDTO();
			formLabelDTO.setLabelName(labelName);
			Integer order = (Integer) row[1];
			if (mdfForm) {
				order = order + additionalColumnsSize;
			}
			if (totalLeads) {
				formLabelDTO.setOrder(XamplifyUtils.getOrderForTotalLeads(order));
			} else {
				formLabelDTO.setOrder(order);
			}
			formLabelDTO.setColumnOrder((Integer) row[3]);
			formLabelDTO.setRequired(isRequired);
			formLabelDTO.setId((Integer) row[4]);
			formLabelDTOs.add(formLabelDTO);
		}
		return formLabelDTOs;
	}

	private void addPartnerOrVendorColumns(List<FormLabelDTO> formLabelDTOs, int order, String labelName) {
		FormLabelDTO vendorOrPartnerDetailsColumn = new FormLabelDTO();
		vendorOrPartnerDetailsColumn.setLabelName(labelName);
		vendorOrPartnerDetailsColumn.setOrder(order);
		formLabelDTOs.add(vendorOrPartnerDetailsColumn);
	}

	@Override
	public Form getFormIdByAlias(String alias) {
		return (Form) sessionFactory.getCurrentSession().createCriteria(Form.class)
				.setProjection(Projections.projectionList().add(Projections.property("id"), "id")
						.add(Projections.property("formName"), "formName"))
				.add(Restrictions.eq(ALIAS_PARAMETER, alias)).setResultTransformer(Transformers.aliasToBean(Form.class))
				.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> listAliasesByUserId(Integer userId) {
		List<Integer> userIds = userDao.listAllUserIdsByLoggedInUserId(userId);
		return sessionFactory.getCurrentSession()
				.createSQLQuery("select alias from xt_form where created_user_id in (:userIds)")
				.setParameterList("userIds", userIds).list();

	}

	@Override
	public boolean isFormExists(String alias) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select count(*) from xt_form where alias = :alias";
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter(ALIAS_PARAMETER, alias);
		Integer count = ((BigInteger) query.uniqueResult()).intValue();
		return count > 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FormDTO> listFormIdAndAliasesByUserId(Integer userId) {
		List<FormDTO> formDtos = new ArrayList<>();
		List<Integer> userIds = userDao.listAllUserIdsByLoggedInUserId(userId);
		Session session = sessionFactory.getCurrentSession();
		String sql = "select id,alias from xt_form where created_user_id in(:userIds)";
		List<Object[]> forms = session.createSQLQuery(sql).setParameterList("userIds", userIds).list();
		copyListToFormDTO(formDtos, forms);
		return formDtos;
	}

	@Override
	public Integer getFormCountByCampaignId(Integer campaignId) {
		Session session = sessionFactory.getCurrentSession();
		SQLQuery query = session
				.createSQLQuery("select count(*) from xt_form_campaign where campaign_id=" + campaignId);
		return query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
	}

	@Override
	public Integer getSubmittedCountByFormId(Integer formId) {
		Session session = sessionFactory.getCurrentSession();
		SQLQuery query = session.createSQLQuery("select count(*) from xt_form_submit where form_id=" + formId);
		return query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
	}

	@Override
	public Integer getSubmittedCountByFormIdAndCampaignId(Integer formId, Integer campaignId) {
		Session session = sessionFactory.getCurrentSession();
		SQLQuery query = session.createSQLQuery(
				"select count(*) from xt_form_campaign_submit fcs,xt_form_submit fs where fcs.id = fs.form_campaign_submit_id and fcs.campaign_id = :campaignId and fs.form_id = :formId");
		query.setParameter("campaignId", campaignId);
		query.setParameter(FORM_ID, formId);
		return query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
	}

	@Override
	public Integer getSubmittedCountByFormIdAndLandingPageId(Integer formId, Integer landingPageId) {
		Session session = sessionFactory.getCurrentSession();
		SQLQuery query = session.createSQLQuery(
				"select count(*) from xt_form_submit fs where fs.landing_page_id = :landingPageId and fs.form_id = :formId");
		query.setParameter(LANDING_PAGE_ID, landingPageId);
		query.setParameter(FORM_ID, formId);
		return query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
	}

	@Override
	public Integer getLandingPageCampaignFormSubmitCount(Integer formId, Integer campaignId, Integer landingPageId) {
		Session session = sessionFactory.getCurrentSession();
		SQLQuery query = session.createSQLQuery(
				"select count(*) from xt_form_submit where form_id=:formId and landing_page_id=:landingPageId ");
		query.setParameter(FORM_ID, formId);
		query.setParameter(LANDING_PAGE_ID, landingPageId);
		return query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
	}

	public Integer getPageCampaignFormSubmitCountByPartnerIdOrContactId(Integer formId, Integer partnerId,
			Integer landingPageId, Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "";
		SQLQuery query = session.createSQLQuery(queryString);
		query.setParameter(FORM_ID, formId);
		query.setParameter(LANDING_PAGE_ID, landingPageId);
		query.setParameter(USER_ID, partnerId);
		return query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
	}

	@Override
	public String getFormAliasByCampaignId(Integer campaignId) {
		Session session = sessionFactory.getCurrentSession();
		SQLQuery query = session.createSQLQuery(
				"select f.alias from xt_form f,xt_form_campaign fc where fc.form_id = f.id and fc.campaign_id ="
						+ campaignId);
		return query.uniqueResult() != null ? ((String) query.uniqueResult()) : "";
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getLeadEmailIdsByCampaignFormSubmittedIds(List<Integer> formCampaignSubmitIds) {
		Session session = sessionFactory.getCurrentSession();
		SQLQuery query = session.createSQLQuery(
				"select LOWER(TRIM(b.value)) from xt_form_submit a,xt_form_submit_single_choice b,xt_form_label c,xt_campaign_event_rsvp d where a.form_campaign_submit_id in (:ids) and a.id = b.form_submit_id and "
						+ " b.form_label_id = c.id and c.form_id = a.form_id and c.label_type = 3 and d.form_submit_id = a.id and d.user_id is null  group by LOWER(TRIM(b.value))");
		query.setParameterList("ids", formCampaignSubmitIds);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getLeadEmailIdsForVendorAndPartnerCampaigns(Integer campaignId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select LOWER(TRIM(b.value)) from xt_form_submit a,xt_form_submit_single_choice b,xt_form_label c where a.form_campaign_submit_id in "
				+ " (select id from xt_form_campaign_submit where campaign_id in (select campaign_id from xt_campaign where (parent_campaign_id = :campaignId or campaign_id = :campaignId)))"
				+ " and a.id = b.form_submit_id and b.form_label_id = c.id and c.form_id = a.form_id and c.label_type = 3 group by LOWER(b.value)";
		SQLQuery query = session.createSQLQuery(queryString);
		query.setParameter("campaignId", campaignId);
		return query.list();
	}

	@Override
	public Integer getSfCustomFormIdByCompanyIdAndUserIdAndFormType(Integer companyId, Integer userId,
			FormTypeEnum formType) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select id from xt_form where company_id =" + companyId + " and created_user_id = " + userId
				+ " and form_type= '" + formType + "'";
		return (Integer) session.createSQLQuery(sql).uniqueResult();
	}

	@Override
	public Integer getSfCustomFormIdByCompanyIdAndFormType(Integer companyId, FormTypeEnum formType) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select id from xt_form where company_id =" + companyId + " and form_type= '" + formType + "'";
		return (Integer) session.createSQLQuery(sql).uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FormLabel> listSfCustomLabelsByFormId(Form form) {
		try {
			Session session = sessionFactory.getCurrentSession();
			org.hibernate.Criteria criteria = session.createCriteria(FormLabel.class);
			criteria.add(Restrictions.eq("form", form));
			return criteria.list();
		} catch (Exception exception) {
			String errorMessage = String.format("An error occurred: %s", exception.getMessage());
			logger.error(errorMessage);
			throw exception;
		}
	}

	@Override
	public void deleteSfCustomLabelById(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "delete from xt_form_label where id = " + id + "";
		session.createSQLQuery(sql).executeUpdate();
	}

	@Override
	public FormLabel getFormLabelByFormIdAndLabelId(Form form, String labelId) {
		Session session = sessionFactory.getCurrentSession();
		return (FormLabel) session.createCriteria(FormLabel.class).add(Restrictions.eq("form", form))
				.add(Restrictions.eq("labelId", labelId)).uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FormDTO> listFormIdAndAliasesByCompanyId(Integer companyId) {
		List<FormDTO> formDtos = new ArrayList<>();
		Session session = sessionFactory.getCurrentSession();
		String sql = "select id,alias from xt_form where company_id=:companyId";
		List<Object[]> forms = session.createSQLQuery(sql).setParameter(COMPANY_ID, companyId).list();
		copyListToFormDTO(formDtos, forms);
		return formDtos;
	}

	private void copyListToFormDTO(List<FormDTO> formDtos, List<Object[]> forms) {
		for (Object[] form : forms) {
			FormDTO formDTO = new FormDTO();
			Integer id = (Integer) form[0];
			String alias = (String) form[1];
			formDTO.setId(id);
			formDTO.setAlias(alias);
			formDtos.add(formDTO);
		}
	}

	@Override
	public String getFormAliasByPageFormAlias(String pageFormAlias) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select f.alias from xt_form f,xt_form_landing_page fl where fl.form_id = f.id and fl.alias = :alias";
		Query query = session.createSQLQuery(queryString).setParameter(ALIAS_PARAMETER, pageFormAlias);
		String result = (String) query.uniqueResult();
		return result != null ? result : "";
	}

	@Override
	public Form getMDFFormByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		Form form = (Form) session.createCriteria(Form.class).add(Restrictions.eq("companyProfile.id", companyId))
				.add(Restrictions.eq("formTypeEnum", FormTypeEnum.MDF_REQUEST_FORM)).uniqueResult();
		return getFormChilds(form);
	}

	@Override
	public Date getDefaultMdfFormCreatedDate(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String mdfRequestFormType = "'" + FormTypeEnum.MDF_REQUEST_FORM + "'";
		String queryString = "select created_time from xt_form where company_id = " + companyId + " and form_type ="
				+ mdfRequestFormType;
		Query query = session.createSQLQuery(queryString);
		Date result = (Date) query.uniqueResult();
		return result != null ? result : null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> findQuizList(Pagination pagination) {
		Map<String, Object> map = new HashMap<>();
		Session session = sessionFactory.getCurrentSession();
		String finalQueryString;
		String sortQueryString = getSelectedSortOption(pagination);
		Integer loggedInUserCompanyId = userDao.getCompanyIdByUserId(pagination.getUserId());
		String finalQuery = "";
		String categoryModuleType = "'" + CategoryModuleEnum.FORM + "'";
		String formQuery = "select distinct f.id,f.form_name,f.created_time,f.updated_time,u.firstname,u.lastname,u.email_id,cp.company_name,f.alias,f.description,u.user_id,f.is_quiz_form,f.thumbnail_image, "
				+ categoryNameCastQuery
				+ " from xt_user_profile u,xt_form f,xt_company_profile cp,xt_category cat,xt_category_module cm";
		String whereQuery = CATEGORY_ID_AND_COMPANY_ID + loggedInUserCompanyId + CATEGORY_MODULE_TYPE
				+ categoryModuleType + FORM_ID_AND_USER_ID_AND_COMPANY_ID + loggedInUserCompanyId
				+ " and f.is_default = false and f.is_quiz_form = true and f.form_type=" + "'"
				+ FormTypeEnum.XAMPLIFY_FORM + "'";
		finalQuery = formQuery + whereQuery;
		if (StringUtils.hasText(pagination.getSearchKey())) {
			String searchKey = "'%" + pagination.getSearchKey() + "%'";
			String searchQueryString = AND_LOWER_F_FORM_NAME_LIKE_LOWER + searchKey + ")";
			finalQueryString = finalQuery + searchQueryString;
		} else {
			finalQueryString = finalQuery + sortQueryString;
		}
		SQLQuery query = session.createSQLQuery(finalQueryString);
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		List<Object[]> list = query.list();
		List<FormDTO> formDtos = new ArrayList<>();
		updateQuizList(list, formDtos);
		map.put("totalRecords", totalRecords);
		map.put(FORMS, formDtos);
		return map;

	}

	private void updateQuizList(List<Object[]> list, List<FormDTO> formDtos) {
		for (Object[] row : list) {
			FormDTO formDto = new FormDTO();
			formDto.setId((Integer) row[0]);
			formDto.setName((String) row[1]);
			formDto.setCreatedDateString(DateUtils.getUtcString((Date) row[2]));
			formDto.setUpdatedDateString(DateUtils.convertDateToStringWithOutSec((Date) row[3]));
			String firstName = (String) row[4];
			String lastName = (String) row[5];
			String emailId = (String) row[6];
			User user = new User();
			user.setFirstName(firstName);
			user.setLastName(lastName);
			user.setEmailId(emailId);
			formDto.setCreatedName(XamplifyUtils.setDisplayName(user));
			formDto.setCompanyName((String) row[7]);
			formDto.setAlias((String) row[8]);
			formDto.setDescription((String) row[9]);
			formDto.setUserId((Integer) row[10]);
			formDto.setQuizForm((boolean) row[11]);
			formDto.setThumbnailImage((String) row[12]);
			formDto.setCategoryName((String) row[13]);
			formDtos.add(formDto);
		}
	}

	@Override
	public void deleteLearningTrackQuizSubmissions(Integer learningTrackId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "delete from xt_form_submit where learning_track_id = :learningTrackId";
		SQLQuery query = session.createSQLQuery(sql);
		query.setInteger(LEARNING_TRACK_ID, learningTrackId);
		query.executeUpdate();

	}

	@Override
	public FormSubmit getLearningTrackFormSubmission(Integer learningTrackId, Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(FormSubmit.class);
		criteria.add(Restrictions.eq("user.id", userId));
		criteria.add(Restrictions.eq("learningTrack.id", learningTrackId));
		return (FormSubmit) criteria.uniqueResult();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> listDefaultForms(Pagination pagination) {
		Map<String, Object> map = new HashMap<>();
		String finalQueryString;
		String filterQuery = "";
		String sortQueryString = getSelectedSortOption(pagination);
		Session session = sessionFactory.getCurrentSession();
		String formQuery = "select distinct f.id,f.form_name,f.created_time,f.updated_time,f.thumbnail_image,f.is_quiz_form,f.alias,f.form_sub_type from xt_form f where f.form_type="
				+ "'" + FormTypeEnum.XAMPLIFY_DEFAULT_FORM + "'";
		filterQuery = setFilterQuery(pagination, filterQuery);
		if (StringUtils.hasText(filterQuery)) {
			formQuery = formQuery + AND + filterQuery;
		}
		if (StringUtils.hasText(pagination.getSearchKey())) {
			String searchKey = "'%" + pagination.getSearchKey() + "%'";
			String searchQueryString = AND_LOWER_F_FORM_NAME_LIKE_LOWER + searchKey + ")";
			finalQueryString = formQuery + searchQueryString;
		} else {
			finalQueryString = formQuery + sortQueryString;
		}
		SQLQuery query = session.createSQLQuery(finalQueryString);
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		List<Object[]> list = query.list();
		List<FormDTO> formDtos = new ArrayList<>();
		updateDefaultFormList(list, formDtos);
		map.put("totalRecords", totalRecords);
		map.put(FORMS, formDtos);
		return map;

	}

	private void updateDefaultFormList(List<Object[]> list, List<FormDTO> formDtos) {
		for (Object[] row : list) {
			FormDTO formDto = new FormDTO();
			formDto.setId((Integer) row[0]);
			formDto.setName((String) row[1]);
			formDto.setCreatedDateString(DateUtils.getUtcString((Date) row[2]));
			formDto.setUpdatedDateString(DateUtils.convertDateToStringWithOutSec((Date) row[3]));
			formDto.setThumbnailImage((String) row[4]);
			formDto.setQuizForm((boolean) row[5]);
			String updatedFormAlias = webUrl + "f/" + row[6];
			formDto.setAilasUrl(updatedFormAlias);
			formDto.setFormSubType(FormSubTypeEnum.valueOf((String) row[7]));
			formDto.setEmbedUrl("<iframe width=\"1000\" height=\"720\" src=" + updatedFormAlias
					+ " frameborder=\"0\" allowfullscreen ></iframe>");
			formDtos.add(formDto);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> findDefaultFormAliases() {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select distinct alias from xt_form where created_user_id = 1 and is_default";
		SQLQuery query = session.createSQLQuery(queryString);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FormDTO> findDefaultFormIdsAndAliases() {
		List<FormDTO> formDtos = new ArrayList<>();
		Session session = sessionFactory.getCurrentSession();
		String sql = "select id,alias from xt_form where created_user_id = 1 and is_default";
		List<Object[]> forms = session.createSQLQuery(sql).list();
		copyListToFormDTO(formDtos, forms);
		return formDtos;
	}

	@Override
	public void delete(Integer formId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String queryString = "delete from Form where id=:id";
			Query query = session.createQuery(queryString);
			query.setParameter("id", formId);
			query.executeUpdate();
		} catch (ConstraintViolationException e) {
			if (String.valueOf(e.getCause())
					.indexOf("update or delete on table \"xt_form\" violates foreign key constraint") > -1) {
				throw new DuplicateEntryException("This form cannot be deleted");
			} else {
				throw new FormDataAccessException(e);
			}

		} catch (HibernateException | FormDataAccessException e) {
			throw new FormDataAccessException(e);
		} catch (Exception ex) {
			throw new FormDataAccessException(ex);
		}

	}

	@Override
	public Integer findFormsCountByUserId(Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		String defaultFormType = "'" + FormTypeEnum.XAMPLIFY_FORM.name() + "'";
		Integer companyId = userDao.getCompanyIdByUserId(userId);
		SQLQuery query = session.createSQLQuery(
				"select count(*) from xt_form where company_id=" + companyId + " and form_type = " + defaultFormType);
		return query.uniqueResult() != null ? ((BigInteger) query.uniqueResult()).intValue() : 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Form> getAllForms() {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(Form.class);
		criteria.add(Restrictions.eq("formTypeEnum", FormTypeEnum.XAMPLIFY_FORM));
		return criteria.list();
	}

	@Override
	public Integer getSubmissionsCount(Integer formId, Integer campaignId, Integer partnerCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(FormSubmit.class, "FS");
		criteria.createAlias("FS.form", "F", JoinType.LEFT_OUTER_JOIN);
		criteria.add(Restrictions.eq("F.id", formId));
		if (campaignId != null && campaignId > 0) {
			criteria.createAlias("FS.formCampaignSubmit", "FCS", JoinType.LEFT_OUTER_JOIN);
			criteria.createAlias("FCS.campaign", "FCSC", JoinType.LEFT_OUTER_JOIN);
			Disjunction disjunction = Restrictions.disjunction();
			disjunction.add(Restrictions.eq("FCSC.id", campaignId));
			disjunction.add(Restrictions.eq("FCSC.parentCampaignId", campaignId));
			criteria.add(disjunction);
			if (partnerCompanyId != null && partnerCompanyId > 0) {
				criteria.createAlias("FCSC.user", "FCSCU", JoinType.LEFT_OUTER_JOIN);
				criteria.add(Restrictions.eq("FCSCU.companyProfile.id", partnerCompanyId));
			}
		}
		criteria.setProjection(Projections.rowCount());
		return ((Long) criteria.uniqueResult()).intValue();
	}

	@Override
	public Integer getFormSubmitByFormCampaignAndUser(Integer formId, Integer campaignId, Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(FormSubmit.class, "FS");
		criteria.createAlias("FS.form", "F", JoinType.LEFT_OUTER_JOIN);
		criteria.add(Restrictions.eq("F.id", formId));
		criteria.createAlias("FS.formCampaignSubmit", "FCS", JoinType.LEFT_OUTER_JOIN);
		criteria.add(Restrictions.eq("FCS.campaign.id", campaignId));
		criteria.add(Restrictions.eq("FCS.user.id", userId));
		criteria.addOrder(Order.desc("id"));
		criteria.setMaxResults(1);
		criteria.setProjection(Projections.id());
		return (Integer) criteria.uniqueResult();
	}

	/******* XNFR-108 *********/

	@Override
	public void findAndDeleteFormTeamMemberGroupMappingByFormIdAndTeamMemberGroupMappingIds(Integer formId,
			List<Integer> teamMemberGroupMappingIds) {
		if (formId != null && teamMemberGroupMappingIds != null && !teamMemberGroupMappingIds.isEmpty()) {
			String sqlString = "delete from xt_form_team_group_mapping where form_id = :formId and team_member_group_user_mapping_id in (:teamMemberGroupMappingIds)";
			Session session = sessionFactory.getCurrentSession();
			session.createSQLQuery(sqlString).setParameter(FORM_ID, formId)
					.setParameterList("teamMemberGroupMappingIds", teamMemberGroupMappingIds).executeUpdate();
		}
	}

	@Override
	public boolean isFormTeamMemberGroupMappingExists(Integer formId, Integer teamMemberGroupUserMappingId) {
		String sqlString = "select case when count(*)>0 then true else false end as row_exists from xt_form_team_group_mapping where form_id = :formId and team_member_group_user_mapping_id = :mappingId";
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(sqlString).setParameter(FORM_ID, formId).setParameter("mappingId",
				teamMemberGroupUserMappingId);
		return (boolean) query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getAllSelecetedGroupIds(Integer formId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(FormTeamGroupMapping.class, "formTeamGroupMapping");
		criteria.add(Restrictions.eq(FORM_ENTITY_ID, formId));
		criteria.createAlias("formTeamGroupMapping.teamMemberGroupUserMapping", "teamMemberGroupUserMapping",
				JoinType.LEFT_OUTER_JOIN);
		criteria.createAlias("teamMemberGroupUserMapping.teamMemberGroup", "group", JoinType.LEFT_OUTER_JOIN);
		criteria.setProjection(Projections.distinct(Projections.property("group.id")));
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getAllSelecetedTeamMemberIds(Integer formId, Integer groupId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(FormTeamGroupMapping.class, "formTeamGroupMapping");
		criteria.add(Restrictions.eq(FORM_ENTITY_ID, formId));
		criteria.createAlias("formTeamGroupMapping.teamMemberGroupUserMapping", "teamMemberGroupUserMapping",
				JoinType.LEFT_OUTER_JOIN);
		criteria.createAlias("teamMemberGroupUserMapping.teamMemberGroup", "group", JoinType.LEFT_OUTER_JOIN);
		criteria.add(Restrictions.eq("group.id", groupId));
		criteria.setProjection(Projections.distinct(Projections.property("teamMemberGroupUserMapping.id")));
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getSelectedTeamMemberIdsByFormId(Integer formId) {
		List<Integer> list = new ArrayList<>();
		if (formId != null) {
			String sqlString = "SELECT DISTINCT t.team_member_id from xt_team_member t,xt_team_member_group_user_mapping tmg,xt_form_team_group_mapping ftmg where t.id=tmg.team_member_id and ftmg.team_member_group_user_mapping_id=tmg.id and ftmg.form_id="
					+ formId;
			Session session = sessionFactory.getCurrentSession();
			list = session.createSQLQuery(sqlString).list();
		}
		return list;
	}

	/******* XNFR-108 ENDS *********/

	@Override
	public void updateMaxScore(Integer formId) {
		if (formId != null && formId > 0) {
			Session session = sessionFactory.getCurrentSession();

			String s = "select count(*) from xt_form_label fl, xt_form f, xt_form_label_type flt where "
					+ " fl.form_id = f.id and fl.label_type = flt.id  "
					+ " and (flt.label_type = 'quiz_radio' or flt.label_type = 'quiz_checkbox') and f.id = :formId";
			Query query1 = session.createSQLQuery(s);
			query1.setInteger(FORM_ID, formId);
			Integer maxScore = ((BigInteger) query1.uniqueResult()).intValue();
			String queryString = "update xt_form set max_score = :maxScore where id = :formId";
			Query query = session.createSQLQuery(queryString);
			query.setInteger(FORM_ID, formId);
			query.setInteger("maxScore", maxScore);
			query.executeUpdate();
		}
	}

	@Override
	public FormSubmit getLearningTrackFormSubmissionByFormID(Integer learningTrackId, Integer userId, Integer formId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(FormSubmit.class);
		criteria.add(Restrictions.eq("user.id", userId));
		criteria.add(Restrictions.eq("learningTrack.id", learningTrackId));
		criteria.add(Restrictions.eq(FORM_ENTITY_ID, formId));
		return (FormSubmit) criteria.uniqueResult();
	}

	@Override
	public void deleteLearningTrackQuizSubmissionsByUserId(Integer learningTrackId, Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "delete from xt_form_submit where learning_track_id = :learningTrackId and user_id = :userId";
		SQLQuery query = session.createSQLQuery(sql);
		query.setInteger(LEARNING_TRACK_ID, learningTrackId);
		query.setInteger(USER_ID, userId);
		query.executeUpdate();

	}

	@Override
	public void deleteLearningTrackQuizSubmissionsByFormId(Integer learningTrackId, Integer formId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "delete from xt_form_submit where learning_track_id = :learningTrackId and form_id = :formId";
		SQLQuery query = session.createSQLQuery(sql);
		query.setInteger(LEARNING_TRACK_ID, learningTrackId);
		query.setInteger(FORM_ID, formId);
		query.executeUpdate();

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FormLabelChoice> findFormLabelChoicesByFormLabelId(Integer formLabelId) {
		Session session = sessionFactory.getCurrentSession();
		return session.createCriteria(FormLabelChoice.class).add(Restrictions.eq("formLabel.id", formLabelId)).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> findFormAliasesByFormIds(List<Integer> formIds) {
		return sessionFactory.getCurrentSession().createSQLQuery("select alias from xt_form where id in (:formIds)")
				.setParameterList("formIds", formIds).list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> findFormLandingPageAliasesByFormIdsAndLandingPageId(List<Integer> formIds,
			Integer landingPageId) {
		String sqlString = "select distinct alias from xt_form_landing_page where \r\n"
				+ " form_id in (:formIds) and landing_page_id = :landingPageId";
		return sessionFactory.getCurrentSession().createSQLQuery(sqlString).setParameter(LANDING_PAGE_ID, landingPageId)
				.setParameterList("formIds", formIds).list();
	}

	@Override
	public String getFormAliasByFormId(Integer formId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select alias from xt_form where id = :formId";
		Query query = session.createSQLQuery(queryString).setParameter(FORM_ID, formId);
		String result = (String) query.uniqueResult();
		return result != null ? result : "";
	}

	/***** XBI-2332 *****/
	@Override
	public String getFromSubType(String alias) {
		if (StringUtils.hasText(alias)) {
			Session session = sessionFactory.getCurrentSession();
			String queryString = "select form_sub_type from xt_form where alias = :alias";
			Query query = session.createSQLQuery(queryString);
			query.setParameter(ALIAS_PARAMETER, alias);
			String formSubType = (String) query.uniqueResult();
			return formSubType != null ? formSubType : "";
		} else {
			return "";
		}

	}

	@Override
	public boolean checkIfRecordExists(Integer formLabelId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select count(*) from xt_form_label where id = :formLabelId";
		BigInteger count = (BigInteger) session.createSQLQuery(sql).setParameter("formLabelId", formLabelId)
				.uniqueResult();
		return count.intValue() > 0;
	}

	@Override
	public Integer getDefaulatVendorJourneyForm(boolean isVendorJourney) {
		Session session = sessionFactory.getCurrentSession();
		String hql = null;
		Query query = session.createQuery(hql);
		return (Integer) query.uniqueResult();
	}

	@Override
	public boolean checkFormNameExists(String formname) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select count(*) from xt_form xf where xf.form_name = '" + formname + "'";
		BigInteger count = (BigInteger) session.createSQLQuery(sql).uniqueResult();
		return count.intValue() > 0;
	}

	private String setFilterQuery(Pagination pagination, String filterQuery) {
		if (StringUtils.hasText(pagination.getFilterKey())) {
			if (pagination.getFilterKey().equalsIgnoreCase(REGULAR)) {
				filterQuery = IS_REGULAR_FORM;
			} else if (pagination.getFilterKey().equalsIgnoreCase("quiz")) {
				filterQuery = IS_QUIZ_FORM_IS_TRUE;
			} else if (pagination.getFilterKey().equalsIgnoreCase(SURVEY)) {
				filterQuery = IS_FORM_SUB_TYPE_SURVEY;
			}

		}
		return filterQuery;
	}

	@Override
	public String getDefaultDependentChoicesJson() {
		Session session = sessionFactory.getCurrentSession();
		String sql = " select dependent_choices_json from xt_default_dependent_choices_json xd where xd.id = 1 ";
		return (String) session.createSQLQuery(sql).uniqueResult();
	}

	@Override
	public Integer getIdByParentChoice(String parentChoice, OpportunityFormFieldsDTO parentLabel) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "";
		if (parentLabel.getName().equals("Region_State__c")) {
			sql = " select id from xt_crm_custom_region xcr where xcr.choice_id = :parentChoice ";
		} else if (parentLabel.getName().equals("State_P__c")) {
			sql = " select id from xt_crm_custom_state xcs where xcs.choice_id = :parentChoice";
		} else if (parentLabel.getName().equals("Country_P__c")) {
			sql = " select id from xt_crm_custom_country xcc where xcc.choice_id = :parentChoice";
		}
		Query query = session.createSQLQuery(sql).setParameter("parentChoice", parentChoice);
		return (Integer) query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CustomChoicesDTO> getCustomChoicesData() {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select choice_name as \"choiceName\" from xt_crm_custom_region";
		Query query = session.createSQLQuery(sql);
		return (List<CustomChoicesDTO>) paginationUtil.getListDTO(CustomChoicesDTO.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CustomChoicesDTO> getCustomChoicesDataByParentChoice(String parentLabelId, String parentChoice) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "";
		if (parentLabelId.equals("Region_State__c")) {
			sql = "select choice_name as \"choiceName\" from xt_crm_custom_country where region_id in "
					+ " (select id from xt_crm_custom_region where choice_id = :parentChoice) ";
		} else if (parentLabelId.equals("Country_P__c")) {
			sql = "select choice_name as \"choiceName\" from xt_crm_custom_state where country_id in "
					+ " (select id from xt_crm_custom_country where choice_id = :parentChoice) ";
		} else if (parentLabelId.equals("State_P__c")) {
			sql = "select choice_name as \"choiceName\" from xt_crm_custom_city where state_id in "
					+ " (select id from xt_crm_custom_state where choice_id = :parentChoice) ";
		}
		Query query = session.createSQLQuery(sql).setParameter("parentChoice", parentChoice);
		return (List<CustomChoicesDTO>) paginationUtil.getListDTO(CustomChoicesDTO.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FormLabelDTO> getFormLabelDtoByFormId(Integer formId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = frameFormLabelDtoQueryString();
		sql += "order by label_order";
		Query query = session.createSQLQuery(sql).setParameter(FORM_ID, formId);
		return (List<FormLabelDTO>) paginationUtil.getListDTO(FormLabelDTO.class, query);
	}

	private String frameFormLabelDtoQueryString() {
		return " SELECT  xfl.id AS \"id\",xfl.label_name AS \"labelName\", xfl.label_id AS \"labelId\",xfl.is_required AS \"required\",cast(xfl.form_default_field_type as text) AS \"formLabelDefaultFieldType\",\r\n"
				+ " xfl.display_name AS \"displayName\",xfl.is_non_interactive AS \"nonInteractive\",\r\n"
				+ " xfl.is_private AS \"isPrivate\", xfl.lookup_external_reference AS \"lookUpReferenceTo\",\r\n"
				+ " xft.label_type AS \"labelType\" from xt_form_label xfl JOIN  xt_form_label_type xft ON xfl.label_type = xft.id where xfl.form_id = :formId ";
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FormDTO> getFormIdAndFormNamesByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "SELECT xf.id as \"id\",xf.form_name as \"name\" FROM xt_form xf,xt_form_label xfl WHERE xf.id = xfl.form_id\n"
				+ "AND xf.company_id = :companyId AND xfl.label_type = 3 AND xf.form_type in('XAMPLIFY_FORM') ORDER BY xf.id DESC\n";
		Query query = session.createSQLQuery(sql).setParameter(COMPANY_ID, companyId);
		return (List<FormDTO>) paginationUtil.getListDTO(FormDTO.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FormSubmit> findSubmittedDataByFormId(Integer formId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(FormSubmit.class);
		criteria.add(Restrictions.eq(FORM_ENTITY_ID, formId));
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FormChoiceDTO> getFormLabelChoices(Integer formLabelId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select id as \"id\",label_choice_name as \"name\",label_choice_id as \"labelId\",\r\n"
				+ "label_choice_hidden_id as \"hiddenLabelId\",is_default_column as \"defaultColumn\"\r\n"
				+ "from xt_form_label_choice where form_label_id = :formLabelId";
		Query query = session.createSQLQuery(sql).setParameter("formLabelId", formLabelId);
		return (List<FormChoiceDTO>) paginationUtil.getListDTO(FormChoiceDTO.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FormLabelDTO> getFormLabelDtoByFormIdForCSV(Integer formId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = frameFormLabelDtoQueryString();
		sql += "order by label_order desc";
		Query query = session.createSQLQuery(sql).setParameter(FORM_ID, formId);
		return (List<FormLabelDTO>) paginationUtil.getListDTO(FormLabelDTO.class, query);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FormLabelDTO> getFormLabelDtoByFormIdForCSVXamplifyIntegration(Integer formId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = frameFormLabelDtoQueryString();
		sql += "and is_active = true order by label_order desc";
		Query query = session.createSQLQuery(sql).setParameter(FORM_ID, formId);
		return (List<FormLabelDTO>) paginationUtil.getListDTO(FormLabelDTO.class, query);
	}

}
