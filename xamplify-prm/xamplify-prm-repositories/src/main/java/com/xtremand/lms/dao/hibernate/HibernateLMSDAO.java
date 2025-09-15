package com.xtremand.lms.dao.hibernate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.hibernate.sql.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xtremand.category.bom.Category;
import com.xtremand.common.bom.Pagination;
import com.xtremand.common.bom.Pagination.SORTINGORDER;
import com.xtremand.dam.bom.ApprovalStatusType;
import com.xtremand.dam.bom.Dam;
import com.xtremand.dam.dto.DamListDTO;
import com.xtremand.dam.dto.PublishedContentIdAndUserListIdDetailsDTO;
import com.xtremand.dam.exception.DamDataAccessException;
import com.xtremand.form.bom.Form;
import com.xtremand.form.dto.FormDTO;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.lms.bom.LearningTrack;
import com.xtremand.lms.bom.LearningTrackContent;
import com.xtremand.lms.bom.LearningTrackPartnerActivity;
import com.xtremand.lms.bom.LearningTrackType;
import com.xtremand.lms.bom.LearningTrackVisibility;
import com.xtremand.lms.bom.LearningTrackVisibilityGroup;
import com.xtremand.lms.dao.LMSDAO;
import com.xtremand.lms.dto.LearningTrackContentDto;
import com.xtremand.lms.dto.LearningTrackContentResponseDTO;
import com.xtremand.lms.dto.PlaybookAssetResponseDTO;
import com.xtremand.lms.dto.PlaybookContentCategoryListDTO;
import com.xtremand.lms.dto.PreviewPlaybookResponseDTO;
import com.xtremand.lms.dto.ShareLearningTrackResponseDTO;
import com.xtremand.util.DateUtils;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.ResponseUtilException;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.ExceptionHandlerUtil;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.CompanyDTO;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.QueryParameterListDTO;
import com.xtremand.util.dto.SortColumnDTO;
import com.xtremand.util.dto.TeamMemberFilterDTO;
import com.xtremand.util.dto.XamplifyConstants;

@Repository("LMSDAO")
@Transactional
public class HibernateLMSDAO implements LMSDAO {

	private static final Logger logger = LoggerFactory.getLogger(HibernateLMSDAO.class);

	private static final String VISIBILITY_ID = "visibilityId";

	private static final String COMPANY_ENTITY_ID_PARAMETER = "company.id";

	private static final String VISIBILITY_LEARNING_TRACK_PARAMETER = "VC.learningTrack";

	private static final String LEARNING_TRACK_ENTITY_ID_PARAMETER = "learningTrack.id";

	private static final String LEARNING_TRACK_PARAMETER_ID = XamplifyConstants.LEARNING_TRACK_ID;

	private static final String TOTAL_RECORDS = XamplifyConstants.TOTAL_RECORDS;

	private static final String USER_ENTITY_ID_PARAMETER = "user.id";

	private static final String PARTNERSHIP_ENTITY_ID_PARAMETER = "partnership.id";

	private static final String VISIBILITY_IDS_PARAMETER = "visibilityIds";

	private static final String USER_LIST_ID = "userListId";
	
	private static final String ALL_COUNT = "allCount";
	
	private static final String PUBLISHED_COUNT = "publishedCount";
	
	private static final String UNPUBLISHED_COUNT = "unPublishedCount";
	
	private static final String MANAGE_FOLDER_COUNT = "manageFolderCount";
	
	private static final String LOGGEDIN_USER_ID = "loggedInUserId";
	
	private static final String ALL_SHARED_COUNT = "allSharedCount";
	
	private static final String IN_PROGRESS_COUNT = "inProgressCount";

	private static final String COMPLETED_COUNT = "completedCount";

	private static final String NOT_VIEWED_COUNT = "notViewedCount";
	
	private static final String INTERACTED = "interacted";

	private static final String NON_INTERACTED = "nonInteracted";
	
	private static final String MANAGE_SHARED_FOLDERS_COUNT = "managerSharedFoldersCount";

	private static final String AND_XD_COMPANY_ID = "and xd. company_id = ";

	private static final String AND_XLT_COMPANY_ID = "and xlt.company_id = ";

	private static final String COMPANY_ID = "companyId";

	private static final String VENDOR_COMPANY_ID = "vendorCompanyId";

	private static final String VC_USER_ID = "VC.user.id";

	private static final String COMPANY_NAME = "companyName";

	private static final String COUNT = "count";

	private static final String AND_LOWER_UUL_CONTACT_COMPANY_LIKE_LOWER = "and LOWER(uul.contact_company) like LOWER('%";

	private static final String VC_PROGRESS = "VC.progress";

	private static final String L_COMPANY_ID = "L.company.id";

	private static final String CM_CATEGORY_ID = "CM.categoryId";

	private static final String L_APPROVAL_STATUS = "L.approvalStatus";

	private static final String L_PUBLISHED = "L.published";

	private static final String LEARNING_TRACK_ID = "learningTrackId";

	private static final String FORM_ID = "formId";

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	@Autowired
	private PaginationUtil paginationUtil;

	@Value("${createdTime.property.name}")
	private String createdTimePropertyName;
	
	@Value("${deleteData.Query}")
	private boolean deleteDataQuery;

	
	//XNFR-1032	
	@Value("${content.tracks.count}")
    private String tracksCountQuery;
	
    @Value("${content.tracks.published.count}")
    private String tracksPublishedCountQuery;
    
    @Value("${content.tracks.unpublished.count}")
    private String tracksUnpublishedCountQuery;
    
    @Value("${content.tracks.manage.tarcks.folder.count}")
    private String manageTracksFolderCount;
    
    @Value("${content.tracks.access.shared.tracks.folder}")
    private String sharedTracksFolderCount;
    
    @Value("${content.tracks.shared.count}")
    private String tracksSharedCountQuery;
    
    @Value("${manage.shared.track.all.count}")
    private String manageSharedTrackAllCount;
    
    @Value("${manage.shared.track.inprogress.count}")
    private String manageSharedTrackInprogressCount;
    
    @Value("${manage.shared.track.completed.tracks.count}")
    private String manageSharedTrackCompletedTracksCount;
    
    @Value("${manage.shared.track.not.viewed.tracks.count}")
    private String manageSharedTrackNotViewedTracksCount;
    
    @Value("${manage.shared.folder.tracks.count}")
    private String manageSharedFolderTracksCount;
    
    @Value("${content.playbooks.count}")
    private String playbooksCountQuery;
    
    @Value("${content.playbooks.published.count}")
    private String playbooksPublishedCountQuery;
    
    @Value("${content.playbooks.unpublished.count}")
    private String playbooksUnpublishedCountQuery;
    
    @Value("${content.playbook.manage.playbook.folder.count}")
    private String managePlaybookFolderCount;
    
    @Value("${content.playbook.access.shared.playbook.folder}")
    private String sharedPlaybookFolderCount;
    
    @Value("${content.playbooks.shared.count}")
    private String playbooksSharedCountQuery;
    
    @Value("${manage.shared.playbook.all.count}")
    private String manageSharedPlaybookAllCount;
    
    @Value("${manage.shared.playbook.inprogress.count}")
    private String manageSharedPlaybookInprogressCount;
    
    @Value("${manage.shared.playbook.completed.playbook.count}")
    private String managesharedPlaybookCompletedPlaybookCount;
    
    @Value("${manage.shared.playbook.not.viewed.playbook.count}")
    private String manageSharedPlaybookNotViewedPlaybookCount;
    
    @Value("${manage.shared.folder.playbbok.count}")
    private String manageSharedFolderPlaybookCount;
    
    @Value("${content.assets.count}")
    private String assetsCountQuery;
    
    @Value("${content.asset.white.labeled.count}")
    private String assetsWhiteLabeledCount;
    
    @Value("${content.asset.design.count}")
    private String assetsDesignCount;
    
    @Value("${content.asset.upload.count}")
    private String assetsUploadCount;
    
    @Value("${content.assets.published.count}")
    private String assetsPublishedCountQuery;
    
    @Value("${content.assets.unpublished.count}")
    private String assetsUnpublishedCountQuery;
    
    @Value("${content.assets.manage.assets.folder.count}")
    private String manageAssetsFolderCount;
    
    @Value("${content.assets.access.assets.folder}")
    private String sharedAssetsFolderCount;
    
    @Value("${content.assets.shared.count}")
    private String assetsSharedCountQuery;
    
    @Value("${manage.shared.asset.all.count}")
    private String manageSharedAssetAllCount;
    
    @Value("${manage.shared.asset.interacted.count}")
    private String manageSharedAssetInteractedCount;
    
    @Value("${manage.shared.asset.non.interacted.count}")
    private String manageSharedAssetNonInteractedCount;
    
    @Value("${manage.shared.asset.folder.count}")
    private String manageSharedAssetFolderCount;

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> getLearningTracksForVendor(Pagination pagination) {
    	Integer totalRecords = 0;
    	Session session = sessionFactory.getCurrentSession();
    	Criteria criteria = session.createCriteria(LearningTrack.class, "L");
    	criteria.createAlias("L.categoryModule", "CM", JoinType.INNER_JOIN);
    	criteria.add(Restrictions.eq(COMPANY_ENTITY_ID_PARAMETER, pagination.getCompanyId()));
    	criteria.add(Restrictions.eq("type", pagination.getLmsType()));

    	String approvalCategory = pagination.getSelectedApprovalStatusCategory();

    	if (XamplifyUtils.isValidString(approvalCategory) && !"ALL".equalsIgnoreCase(approvalCategory)) {
    		switch (approvalCategory.toUpperCase()) {
    		case "PUBLISHED":
    			criteria.add(Restrictions.eq(L_PUBLISHED, true));
    			break;
    		case "UNPUBLISHED":
    			criteria.add(Restrictions.eq(L_PUBLISHED, false));
    			break;
    		case "APPROVED":
    			criteria.add(Restrictions.eq(L_APPROVAL_STATUS, ApprovalStatusType.APPROVED));
    			criteria.add(Restrictions.eq(L_PUBLISHED, false));
    			break;
    		case "REJECTED":
    			criteria.add(Restrictions.eq(L_APPROVAL_STATUS, ApprovalStatusType.REJECTED));
    			break;
    		case "DRAFT":
    			criteria.add(Restrictions.eq(L_APPROVAL_STATUS, ApprovalStatusType.DRAFT));
    			break;
    		default:
    			break;
    		}
    	}

    	addCategoryFilter(pagination, criteria);

    	if (pagination.getSearchKey() != null && !pagination.getSearchKey().isEmpty()) {
    		criteria.createAlias("L.tags", "LT", JoinType.LEFT_OUTER_JOIN);
    		criteria.createAlias("LT.tag", "T", JoinType.LEFT_OUTER_JOIN);
    		Disjunction disjunction = Restrictions.disjunction();
    		disjunction.add(Restrictions.ilike("title", pagination.getSearchKey(), MatchMode.ANYWHERE));
    		disjunction.add(Restrictions.ilike("T.tagName", pagination.getSearchKey(), MatchMode.ANYWHERE));
    		addUserAndCategoryClassForSearch(pagination, disjunction);
    		criteria.add(disjunction);
    		criteria.setProjection(Projections.projectionList().add(Projections.groupProperty("L.id")));
    	}

    	ScrollableResults scrollableResults = criteria.scroll();
    	scrollableResults.last();
    	totalRecords = scrollableResults.getRowNumber() + 1;

    	List<LearningTrack> ltList = new ArrayList<>();
    	if (pagination.getSearchKey() != null && !pagination.getSearchKey().isEmpty()) {
    		List<Integer> trackIds = criteria.list();
    		if (trackIds != null && !trackIds.isEmpty()) {
    			Criteria criteriaForEntityList = session.createCriteria(LearningTrack.class, "L");
    			criteriaForEntityList.add(Restrictions.in("L.id", trackIds));
    			addSortOption(pagination, criteriaForEntityList);
    			ltList = criteriaForEntityList.list();
    		}
    	} else {
    		addSortOption(pagination, criteria);
    		ltList = criteria.list();
    	}

    	Map<String, Object> resultMap = new HashMap<>();
    	resultMap.put(XamplifyConstants.TOTAL_RECORDS, totalRecords);
    	resultMap.put("data", ltList);
    	return resultMap;
    }

	private void addSortOption(Pagination pagination, Criteria criteria) {
		if ("assetName".equals(pagination.getSortcolumn())) {
			pagination.setSortcolumn("L.title");
		}
		Optional<Pagination> paginationObj = Optional.ofNullable(pagination);

		Optional<Integer> maxResultsObj = Optional.ofNullable(paginationObj.get().getMaxResults());
		Optional<Integer> pageIndexObj = Optional.ofNullable(paginationObj.get().getPageIndex());
		Optional<String> sortcolumnObj = Optional.ofNullable(paginationObj.get().getSortcolumn());

		if (maxResultsObj.isPresent() && pageIndexObj.isPresent()) {
			criteria.setFirstResult((pageIndexObj.get() * maxResultsObj.get()) - maxResultsObj.get());
			criteria.setMaxResults(maxResultsObj.get());
		}
		boolean isPartnerView = pagination.isPartnerView();
		if (sortcolumnObj.isPresent()) {
			boolean isCreatedTime = "createdTime".equalsIgnoreCase(sortcolumnObj.get());
			String sortColumn = sortcolumnObj.get();
			if (isPartnerView && isCreatedTime) {
				sortColumn = "VC.publishedOn";
			}
			if (SORTINGORDER.ASC == SORTINGORDER.valueOf(paginationObj.get().getSortingOrder())) {
				criteria.addOrder(Order.asc(sortColumn));
			} else if (SORTINGORDER.DESC == SORTINGORDER.valueOf(paginationObj.get().getSortingOrder())) {
				criteria.addOrder(Order.desc(sortColumn));
			}
		} else {
			if (pagination.isPartnerView()) {
				criteria.addOrder(Order.desc("VC.publishedOn"));
			} else {
				criteria.addOrder(Order.desc("createdTime"));
			}

		}

	}

	private void addCategoryFilter(Pagination pagination, Criteria criteria) {
		if (pagination.getCategoryId() != null && pagination.getCategoryId() > 0) {
			criteria.add(Restrictions.eq(CM_CATEGORY_ID, pagination.getCategoryId()));
		}
	}

	@Override
	public LearningTrack getLearningTrackBySlug(String slug, Integer companyId, LearningTrackType learningTrackType) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(LearningTrack.class);
		criteria.add(Restrictions.eq("slug", slug).ignoreCase());
		criteria.add(Restrictions.eq(COMPANY_ENTITY_ID_PARAMETER, companyId));
		criteria.add(Restrictions.eq("type", learningTrackType));
		return (LearningTrack) criteria.uniqueResult();
	}
	 
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getLearningTracksForPartner(Pagination pagination) {
		Integer totalRecords = 0;
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(LearningTrackVisibility.class, "VC");
		criteria.createAlias(VISIBILITY_LEARNING_TRACK_PARAMETER, "L", JoinType.LEFT_OUTER_JOIN);
		criteria.createAlias("L.categoryModule", "CM", JoinType.INNER_JOIN);
		criteria.add(Restrictions.eq(VC_USER_ID, pagination.getUserId()));
		criteria.add(Restrictions.eq("VC.published", true));
		criteria.add(Restrictions.eq(L_PUBLISHED, true));
		criteria.add(Restrictions.eq("L.type", pagination.getLmsType()));
		addCategoryFilter(pagination, criteria);
		if (pagination.getVendorCompanyId() != null && pagination.getVendorCompanyId() > 0) {
			criteria.add(Restrictions.eq(L_COMPANY_ID, pagination.getVendorCompanyId()));
		}

		if (XamplifyUtils.isValidString(pagination.getSelectedApprovalStatusCategory())) {
			String progressFilter = pagination.getSelectedApprovalStatusCategory();
			if (progressFilter.equalsIgnoreCase("in-progress")) {
				criteria.add(Restrictions.gt(VC_PROGRESS, 0));
				criteria.add(Restrictions.ne(VC_PROGRESS, 100));
			} else if (progressFilter.equalsIgnoreCase("completed")) {
				criteria.add(Restrictions.eq(VC_PROGRESS, 100));
			} else if (progressFilter.equalsIgnoreCase("not-viewd")) {
				criteria.add(Restrictions.or(
						Restrictions.isNull(VC_PROGRESS),
						Restrictions.eq(VC_PROGRESS, 0)
						));
			}
		}

		if (pagination.getSearchKey() != null && !pagination.getSearchKey().isEmpty()) {
			criteria.createAlias("L.tags", "LT", JoinType.LEFT_OUTER_JOIN);
			criteria.createAlias("LT.tag", "T", JoinType.LEFT_OUTER_JOIN);
			Disjunction disjunction = Restrictions.disjunction();
			disjunction.add(Restrictions.ilike("L.title", pagination.getSearchKey(), MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("T.tagName", pagination.getSearchKey(), MatchMode.ANYWHERE));
			/*** XNFR-758 ***/
			addUserAndCategoryClassForSearch(pagination, disjunction);
			/*** XNFR-758 ***/
			criteria.add(disjunction);
			criteria.setProjection(Projections.projectionList().add(Projections.groupProperty("VC.id")));
		}

		ScrollableResults scrollableResults = criteria.scroll();
		scrollableResults.last();
		totalRecords = scrollableResults.getRowNumber() + 1;
		pagination.setPartnerView(true);
		List<LearningTrack> ltList = new ArrayList<>();
		if (pagination.getSearchKey() != null && !pagination.getSearchKey().isEmpty()) {
			List<Integer> visibilityIds = criteria.list();
			if (visibilityIds != null && !visibilityIds.isEmpty()) {
				Criteria criteriaForEntityList = session.createCriteria(LearningTrackVisibility.class, "VC");
				criteriaForEntityList.createAlias(VISIBILITY_LEARNING_TRACK_PARAMETER, "L", JoinType.LEFT_OUTER_JOIN);
				criteriaForEntityList.add(Restrictions.in("VC.id", visibilityIds));
				addSortOption(pagination, criteriaForEntityList);
				ltList = criteriaForEntityList.list();
			}
		} else {
			/*********** Sorting ************/
			addSortOption(pagination, criteria);
			ltList = criteria.list();
		}

		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(TOTAL_RECORDS, totalRecords);
		resultMap.put("data", ltList);
		return resultMap;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getAnalytics(Pagination pagination) {
		Map<String, Object> map = new HashMap<>();
		Session session = sessionFactory.getCurrentSession();
		String queryString = "";
		queryString = "select c.company_id as \"id\", c.company_name as \"companyName\", count(*) as \"count\", \n"
				+ "c.company_logo as \"companyLogo\",cast(count(v.progress>0) as int) as \"viewCount\", \n"
				+ "p.status as \"partnerStatus\" from xt_learning_track_visibility v, xt_partnership p, \n"
				+ "xt_user_list ul, xt_user_userlist uul, xt_user_profile u, xt_company_profile c \n"
				+ "where v.learning_track_id = " + pagination.getLearningTrackId() + "  and v.partnership_id = p.id "
				+ " and p.vendor_company_id = ul.company_id " + " and ul.is_default_partnerlist "
				+ " and uul.user_list_id = ul.user_list_id " + " and uul.user_id = u.user_id "
				+ " and u.company_id = c.company_id " + " and c.company_id = p.partner_company_id  ";
		/******** XNFR-85 **********/
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		boolean applyPartnershipIdsFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();

		if (!teamMemberFilterDTO.isEmptyFilter() && applyPartnershipIdsFilter) {
			String partnershipIdsOrPartnerCompanyIds = teamMemberFilterDTO.getPartnershipIdsOrPartnerCompanyIds()
					.stream().map(String::valueOf).collect(Collectors.joining(","));
			queryString += "and  v.partnership_id in ( " + partnershipIdsOrPartnerCompanyIds + " )";
		} else if (teamMemberFilterDTO.isEmptyFilter()) {
			map.put(TOTAL_RECORDS, 0);
			map.put("data", new ArrayList<>());
			return map;
		}

		if (pagination.getSearchKey() != null && !pagination.getSearchKey().isEmpty()) {
			queryString += "and LOWER(c.company_name) like LOWER('%" + pagination.getSearchKey() + "%') ";
		}
		queryString += "group by (c.company_name, c.company_id), p.status ";
		if (XamplifyUtils.isValidString(pagination.getSortcolumn()) && XamplifyUtils.isValidString(pagination.getSortingOrder())) {
			if (COMPANY_NAME.equalsIgnoreCase(pagination.getSortcolumn())) {
				queryString += "order by 2 " + pagination.getSortingOrder();
			} else if (COUNT.equalsIgnoreCase(pagination.getSortcolumn())) {
				queryString += "order by 5 " + pagination.getSortingOrder();
			}
		}
		Query query = session.createSQLQuery(queryString);

		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		List<CompanyDTO> companyDTOList = (List<CompanyDTO>) paginationUtil.getListDTO(CompanyDTO.class, query);
		map.put(TOTAL_RECORDS, totalRecords);
		map.put("data", companyDTOList);
		return map;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getPartnerAnalytics(Pagination pagination) {
		List<UserDTO> userDTOList = new ArrayList<>();
		Session session = sessionFactory.getCurrentSession();
		String sql = "";
		sql = "select u.firstname, u.lastname, u.email_id as email_id, c.company_name, COALESCE(v.progress, 0) as progress, u.user_id, v.updated_time, "
				+" CONCAT(u.firstname, ' ', u.lastname) AS \"createdBy\" "
				+ " from xt_learning_track_visibility v, xt_user_profile u, xt_company_profile c "
				+ " where v.learning_track_id = " + pagination.getLearningTrackId() + " "
				+ " and u.user_id = v.user_id " + " and u.company_id = c.company_id " + " and c.company_id = "
				+ pagination.getPartnerCompanyId() + " ";

		String searchKey = pagination.getSearchKey();
		
		if (pagination.getSearchKey() != null && !pagination.getSearchKey().isEmpty()) {
			sql += "and ( LOWER(u.firstname) like LOWER('%" + searchKey + "%') "
					+ "or LOWER(u.lastname) like LOWER('%" + searchKey + "%') "
					+ "or LOWER(u.email_id) like LOWER('%" + searchKey + "%') "
					+ "or LOWER(CONCAT(u.firstname,' ',u.lastname)) LIKE LOWER('%" + searchKey + "%')) ";
		}

		if ("partnerName".equalsIgnoreCase(pagination.getSortcolumn())) {
			sql += "order by \"createdBy\" " + pagination.getSortingOrder();
		} else if ("progress".equalsIgnoreCase(pagination.getSortcolumn())) {
			sql += "order by \"progress\" " + pagination.getSortingOrder();
		} else if ("emailId".equalsIgnoreCase(pagination.getSortcolumn())) {
			sql += "order by \"email_id\" " + pagination.getSortingOrder();
		}
		Query query = session.createSQLQuery(sql);
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		List<Object[]> responseList = query.list();
		for (Object[] item : responseList) {
			UserDTO userDTO = new UserDTO();
			userDTO.setFirstName((String) item[0]);
			userDTO.setLastName((String) item[1]);
			userDTO.setEmailId((String) item[2]);
			userDTO.setCompanyName((String) item[3]);
			userDTO.setLearningTrackProgress((Integer) item[4]);
			userDTO.setId((Integer) item[5]);
			userDTO.setUpdatedTime(DateUtils.getUtcString((Date) item[6]));
			userDTOList.add(userDTO);
		}

		Map<String, Object> map = new HashMap<>();
		map.put(TOTAL_RECORDS, totalRecords);
		map.put("data", userDTOList);
		return map;
	}
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getPartnerTrackAnalyticsForDownload(Pagination pagination) {
		List<UserDTO> userDTOList = new ArrayList<>();
		Session session = sessionFactory.getCurrentSession();
		String sql = "";
		sql = "SELECT DISTINCT " +
			    "t.title AS \"trackName\", " +
			    "CONCAT(up.firstname, ' ', up.lastname) AS \"createdBy\", " +
			    "c.company_name AS \"companyName\", " +
			    "u.email_id AS \"emailId\", " +
			    "CONCAT(u.firstname, '', u.lastname) AS \"partnerName\", " +
			    "COALESCE(v.progress, 0) AS \"progress\", " +
			    "v.updated_time, " +
			    "MAX(v.published_on) AS \"publishedOn\", " +
			    "u.firstname, " +
			    "u.lastname, " +
			    "u.user_id, " +
			    "max(case when xfs.submitted_on is not null and xfs.form_submit_type='LMS_FORM' then coalesce (xfs.score, 0) || '  out of  ' ||coalesce(xf.max_score, 0) " + 
			    "when coalesce (xfs.score, 0)=0 or coalesce(xf.max_score, 0) =0 then 'Quiz Not Available' " + 
			    "else coalesce (xfs.score, 0) || '  out of  ' ||coalesce(xf.max_score, 0) end )  as \"score\"  " +
			    "from xt_learning_track_visibility v join xt_learning_track t on t.id = v.learning_track_id " +
			    "join xt_user_profile up on up.user_id = t.created_by " +
			    "join xt_user_profile u on u.user_id = v.user_id " +
			    "join xt_company_profile c on u.company_id = c.company_id " +
			    "left join xt_learning_track_content xltc on xltc.learning_track_id=t.id " +
			    "left join xt_form xf on  xltc.quiz_id= xf.id " +
			    "left join xt_form_submit xfs on xf.id=xfs.form_id and xfs.learning_track_id = t.id and xfs.user_id =u.user_id " +
			    "where v.learning_track_id = " + pagination.getLearningTrackId() + " and c.company_id = " + pagination.getPartnerCompanyId() + " ";
			 
		if (pagination.getSearchKey() != null && !pagination.getSearchKey().isEmpty() && !"-".equalsIgnoreCase(pagination.getSearchKey())) {
			sql += "and ( LOWER(u.firstname) like LOWER('%" + pagination.getSearchKey() + "%') "
					+ "or LOWER(u.lastname) like LOWER('%" + pagination.getSearchKey() + "%') "
					+ "or LOWER(u.email_id) like LOWER('%" + pagination.getSearchKey() + "%')) ";
		}
		sql += "GROUP BY 1,2,3,4,5,6,7,9,10,11 ";
		if ("partnerName".equalsIgnoreCase(pagination.getSortcolumn())) {
			sql += "order by \"partnerName\" " + pagination.getSortingOrder();
		} else if ("progress".equalsIgnoreCase(pagination.getSortcolumn())) {
			sql += "order by \"progress\" " + pagination.getSortingOrder();
		} else if ("emailId".equalsIgnoreCase(pagination.getSortcolumn())) {
			sql += "order by \"emailId\" " + pagination.getSortingOrder();
		}
		Query query = session.createSQLQuery(sql);
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		List<Object[]> responseList = query.list();
		for (Object[] item : responseList) {
		    UserDTO userDTO = new UserDTO();

		    userDTO.setTrackName((String) item[0]);                         
		    userDTO.setCreatedBy((String) item[1]);                         
		    userDTO.setCompanyName((String) item[2]);                    
		    userDTO.setEmailId((String) item[3]);                           
		    userDTO.setPartnerName((String) item[4]);                       
		    userDTO.setProgress(((Integer) item[5]));
		    userDTO.setUpdatedTime(DateUtils.getUtcString((Date) item[6]));   
		    userDTO.setPublishedOn((Date) item[7]);                            
		    userDTO.setFirstName((String) item[8]);                         
		    userDTO.setLastName((String) item[9]);                            
		    userDTO.setId(((Integer) item[10]));  
		    userDTO.setScore((String)item[11]);
		    userDTO.setHasLearningTrackQuiz(false); 

		    userDTOList.add(userDTO);
		}
		
		Map<String, Object> map = new HashMap<>();
		map.put(TOTAL_RECORDS, totalRecords);
		map.put("data", userDTOList);
		return map;
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<LearningTrackPartnerActivity> getPartnerActivity(Integer contentId, Integer loggedInUserId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(LearningTrackPartnerActivity.class, "PA");
		criteria.createAlias("PA.visibilityUser", "V", JoinType.LEFT_OUTER_JOIN);
		criteria.add(Restrictions.eq("PA.learningTrackContent.id", contentId));
		criteria.add(Restrictions.eq("V.user.userId", loggedInUserId));
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getPartnerFinishedContent(Integer learningTrackId, Integer visibilityUserId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select c.id from xt_learning_track_content_partner_activity pa, "
				+ " xt_learning_track_content c, xt_learning_track l " + " where pa.learning_track_content_id = c.id "
				+ " and c.learning_track_id = l.id " + " and l.id = :learningTrackId "
				+ " and pa.learning_track_visibility_id = :visibilityUserId "
				+ " and pa.type IN ('VIEWED', 'DOWNLOADED', 'SUBMITTED')" + " group by c.id";
		SQLQuery query = session.createSQLQuery(sql);
		query.setInteger(LEARNING_TRACK_PARAMETER_ID, learningTrackId);
		query.setInteger("visibilityUserId", visibilityUserId);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<LearningTrackContent> getContentsInOrder(Integer learningTrackId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(LearningTrackContent.class);
		criteria.add(Restrictions.eq(LEARNING_TRACK_ENTITY_ID_PARAMETER, learningTrackId));
		criteria.addOrder(Order.asc("displayIndex"));
		return criteria.list();
	}

	@Override
	public void retainGroups(Set<Integer> newGroupIds, Integer learningTrackId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "delete from xt_learning_track_visibility_group where ";
		if (newGroupIds != null && !newGroupIds.isEmpty()) {
			sql += "user_list_id not IN (:newGroupIds) and ";
		}
		sql += "visibility_id IN (select id from xt_learning_track_visibility where learning_track_id = :learningTrackId)";
		SQLQuery query = session.createSQLQuery(sql);
		if (newGroupIds != null && !newGroupIds.isEmpty()) {
			query.setParameterList("newGroupIds", newGroupIds);
		}
		query.setInteger(LEARNING_TRACK_PARAMETER_ID, learningTrackId);
		query.executeUpdate();
	}

	@Override
	public void clearDisplayIndex(Integer learningTrackId) {
		Session session = sessionFactory.getCurrentSession();
		String hql = "update LearningTrackContent c set displayIndex = displayIndex*(-1) where c.learningTrack.id = "
				+ learningTrackId;
		Query query = session.createQuery(hql);
		query.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<LearningTrackContent> getLearningTrackContent(Integer learningTrackId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(LearningTrackContent.class);
		criteria.add(Restrictions.eq(LEARNING_TRACK_ENTITY_ID_PARAMETER, learningTrackId));
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getPartnerActivities(Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(LearningTrackPartnerActivity.class, "L");
		criteria.createAlias("L.visibilityUser", "V", JoinType.LEFT_OUTER_JOIN);
		criteria.createAlias("L.learningTrackContent", "LC", JoinType.LEFT_OUTER_JOIN);
		criteria.add(Restrictions.eq("LC.learningTrack.id", pagination.getLearningTrackId()));
		criteria.add(Restrictions.eq("V.user.userId", pagination.getPartnerId()));

		if (pagination.getSearchKey() != null && !pagination.getSearchKey().isEmpty()) {
			criteria.createAlias("LC.dam", "D", JoinType.LEFT_OUTER_JOIN);
			criteria.createAlias("LC.quiz", "Q", JoinType.LEFT_OUTER_JOIN);
			Disjunction disjunction = Restrictions.disjunction();
			disjunction.add(Restrictions.ilike("D.assetName", pagination.getSearchKey(), MatchMode.ANYWHERE));
			disjunction.add(Restrictions.ilike("Q.formName", pagination.getSearchKey(), MatchMode.ANYWHERE));
			criteria.add(disjunction);
		}
		criteria.addOrder(Order.desc("id"));

		ScrollableResults scrollableResults = criteria.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;

		criteria.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		criteria.setMaxResults(pagination.getMaxResults());

		List<LearningTrackPartnerActivity> ltList = criteria.list();
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("data", ltList);
		return resultMap;
	}

	@Override
	public LearningTrack getLearningTrackByTitle(String title, Integer companyId, LearningTrackType learningTrackType) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(LearningTrack.class);
		criteria.add(Restrictions.eq("title", title).ignoreCase());
		criteria.add(Restrictions.eq(COMPANY_ENTITY_ID_PARAMETER, companyId));
		criteria.add(Restrictions.eq("type", learningTrackType));
		return (LearningTrack) criteria.uniqueResult();
	}

	@Override
	public LearningTrackVisibility getVisibilityUser(Integer userId, Integer partnershipId, Integer learningTrackId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(LearningTrackVisibility.class);
		criteria.add(Restrictions.eq(USER_ENTITY_ID_PARAMETER, userId));
		criteria.add(Restrictions.eq(PARTNERSHIP_ENTITY_ID_PARAMETER, partnershipId));
		criteria.add(Restrictions.eq(LEARNING_TRACK_ENTITY_ID_PARAMETER, learningTrackId));
		return (LearningTrackVisibility) criteria.uniqueResult();
	}

	@Override
	public LearningTrackVisibility getVisibilityUser(Integer userId, Integer learningTrackId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(LearningTrackVisibility.class);
		criteria.add(Restrictions.eq(USER_ENTITY_ID_PARAMETER, userId));
		criteria.add(Restrictions.eq(LEARNING_TRACK_ENTITY_ID_PARAMETER, learningTrackId));
		return (LearningTrackVisibility) criteria.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getAllVisibilityUsersIds(Integer partnershipId, Integer learningTrackId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(LearningTrackVisibility.class);
		criteria.add(Restrictions.eq(PARTNERSHIP_ENTITY_ID_PARAMETER, partnershipId));
		criteria.add(Restrictions.eq(LEARNING_TRACK_ENTITY_ID_PARAMETER, learningTrackId));
		criteria.add(Restrictions.eq("associatedThroughCompany", true));
		criteria.setProjection(Projections.property(USER_ENTITY_ID_PARAMETER));
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getAllPartnershipIds(Integer learningTrackId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(LearningTrackVisibility.class);
		criteria.add(Restrictions.eq(LEARNING_TRACK_ENTITY_ID_PARAMETER, learningTrackId));
		criteria.add(Restrictions.eq("associatedThroughCompany", true));
		criteria.setProjection(Projections.distinct(Projections.property(PARTNERSHIP_ENTITY_ID_PARAMETER)));
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getAllGroupIds(Integer learningTrackId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(LearningTrackVisibilityGroup.class, "VG");
		criteria.createAlias("VG.learningTrackVisibility", "V", JoinType.LEFT_OUTER_JOIN);
		criteria.add(Restrictions.eq("V.learningTrack.id", learningTrackId));
		criteria.setProjection(Projections.distinct(Projections.property("userList.id")));
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<LearningTrack> getLearningTracksByGroupId(Integer userListId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(LearningTrackVisibilityGroup.class, "VG");
		criteria.createAlias("VG.learningTrackVisibility", "V", JoinType.LEFT_OUTER_JOIN);
		criteria.add(Restrictions.eq("VG.userList.id", userListId));
		criteria.setProjection(Projections.distinct(Projections.property("V.learningTrack")));
		/*** XBI-4309 *****/
		criteria.createAlias("V.learningTrack", "learningTrack");
		criteria.add(
		    Restrictions.or(
		        Restrictions.isNull("learningTrack.expireDate"),
		        Restrictions.gt("learningTrack.expireDate", new Date())
		    )
		);
		/*** XBI-4309 *****/
		return criteria.list();
	}

	@Override
	public void deleteVisibilityForDeletedUsersFromUserList(Integer userListId, List<Integer> userIds) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "delete from xt_learning_track_visibility where id in "
				+ " (select v.id from xt_learning_track_visibility v, xt_learning_track_visibility_group vg, "
				+ " xt_user_profile u, xt_company_profile c "
				+ " where v.is_associated_through_company = false and v.id = vg.visibility_id and vg.user_list_id = :userListId "
				+ " and v.user_id = u.user_id and u.company_id = c.company_id "
				+ " and c.company_id in (select distinct(company_id) from xt_user_profile where user_id in (:userIds)))";
		Query query = session.createSQLQuery(sql);
		query.setParameterList("userIds", userIds);
		query.setInteger(XamplifyConstants.USER_LIST_ID, userListId);
		query.executeUpdate();
	}

	@Override
	public void clearVisbilityOrphans() {
		Session session = sessionFactory.getCurrentSession();
		String sql = null;
		if(deleteDataQuery) {
			sql = "DELETE FROM xt_learning_track_visibility v USING xt_learning_track_visibility_group g \r\n" + 
					"WHERE v.is_associated_through_company = false AND v.id = g.visibility_id AND g.visibility_id IS NULL  ";
		}else {
			sql = "delete from xt_learning_track_visibility where is_associated_through_company = false and id not in "
					+ "(select distinct(visibility_id) from xt_learning_track_visibility_group)";
		}
		Query query = session.createSQLQuery(sql);
		query.executeUpdate();

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getLearningTracksForTeamMember(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select distinct(l.id), vg.user_list_id from xt_learning_track l, xt_learning_track_visibility v, "
				+ " xt_learning_track_visibility_group vg, xt_user_userlist uul "
				+ " where l.id = v.learning_track_id and v.id = vg.visibility_id "
				+ " and vg.user_list_id = uul.user_list_id "
				+ " and uul.user_id in (select user_id from xt_user_profile where company_id = :companyId) ";
		Query query = session.createSQLQuery(sql);
		query.setInteger(XamplifyConstants.COMPANY_ID, companyId);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getUserListsForTeamMember(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select distinct(uul.user_list_id) from xt_user_userlist uul, xt_learning_track_visibility_group vg "
				+ " where vg.user_list_id = uul.user_list_id "
				+ " and uul.user_id in (select user_id from xt_user_profile where company_id = :companyId) ";
		Query query = session.createSQLQuery(sql);
		query.setInteger(XamplifyConstants.COMPANY_ID, companyId);
		return query.list();
	}

	@Override
	public void unPublishLearningTracksWithEmptyVisbility() {
		Session session = sessionFactory.getCurrentSession();
		String sql = "update xt_learning_track set is_published = false where id not in "
				+ "(select distinct(learning_track_id) from xt_learning_track_visibility)";
		Query query = session.createSQLQuery(sql);
		query.executeUpdate();
	}

	@Override
	public int getTracksCountByFormId(Integer formId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select cast(count(*) as int) from xt_learning_track_content  where quiz_id = :formId";
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter(FORM_ID, formId);
		return (int) query.uniqueResult();
	}

	@Override
	public boolean isLMSSharedToPartnerCompanyByPartnerId(Integer partnerId, LearningTrackType lmsType,
			Integer vendorCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(LearningTrackVisibility.class, "VC");
		criteria.createAlias(VISIBILITY_LEARNING_TRACK_PARAMETER, "L", JoinType.LEFT_OUTER_JOIN);
		criteria.add(Restrictions.eq(VC_USER_ID, partnerId));
		criteria.add(Restrictions.eq(L_PUBLISHED, true));
		criteria.add(Restrictions.eq("L.type", lmsType));

		if (vendorCompanyId != null && vendorCompanyId > 0) {
			criteria.add(Restrictions.eq(L_COMPANY_ID, vendorCompanyId));
		}
		criteria.setProjection(Projections.rowCount());
		return (Long) criteria.uniqueResult() > 0;
	}

	@Override
	public LearningTrackContent getContentByQuizID(Integer learningTrackId, Integer quizId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(LearningTrackContent.class);
		criteria.add(Restrictions.eq("quiz.id", quizId));
		criteria.add(Restrictions.eq(LEARNING_TRACK_ENTITY_ID_PARAMETER, learningTrackId));
		return (LearningTrackContent) criteria.uniqueResult();
	}

	@Override
	public LearningTrackContent getContentByDamID(Integer learningTrackId, Integer contentId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(LearningTrackContent.class);
		criteria.add(Restrictions.eq("dam.id", contentId));
		criteria.add(Restrictions.eq(LEARNING_TRACK_ENTITY_ID_PARAMETER, learningTrackId));
		return (LearningTrackContent) criteria.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Integer getRecentlyFinishedQuizId(Integer learningTrackId, Integer userId, Integer partnershipId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select c.quiz_id from xt_learning_track_content_partner_activity pa, "
				+ " xt_learning_track_content c, xt_learning_track l, xt_learning_track_visibility xltv  "
				+ " where pa.learning_track_content_id = c.id " + " and c.learning_track_id = l.id "
				+ " and l.id = :learningTrackId " + " and pa.type IN ('SUBMITTED') "
				+ " and pa.learning_track_visibility_id = xltv.id " + " and xltv.partnership_id= :partnershipId "
				+ " and xltv.user_id = :userId " + " order by pa.created_time desc";
		SQLQuery query = session.createSQLQuery(sql);
		query.setInteger(LEARNING_TRACK_PARAMETER_ID, learningTrackId);
		query.setInteger("userId", userId);
		query.setInteger("partnershipId", partnershipId);
		List<Integer> quizIDs = query.list();
		if (quizIDs != null && !quizIDs.isEmpty()) {
			return quizIDs.get(0);
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<LearningTrack> getAllTracksWithQuiz() {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(LearningTrack.class);
		criteria.add(Restrictions.isNotNull("quiz"));
		criteria.add(Restrictions.eq("type", LearningTrackType.TRACK));
		return criteria.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<LearningTrack> getAllTracksWithQuizByTrackID(Integer learningTrackId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(LearningTrack.class);
		criteria.add(Restrictions.isNotNull("quiz"));
		criteria.add(Restrictions.eq("type", LearningTrackType.TRACK));
		criteria.add(Restrictions.eq("id", learningTrackId));
		return criteria.list();
	}

	@Override
	public Integer getMaxDisplayIndex(Integer learningTrackId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select max(xltc.display_index) from xt_learning_track_content xltc "
				+ " where learning_track_id =:learningTrackId";
		SQLQuery query = session.createSQLQuery(sql);
		query.setInteger(LEARNING_TRACK_ID, learningTrackId);
		return (Integer) query.uniqueResult();
	}

	@Override
	public void clearPartnersActivity(Set<Integer> visibilityIds) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "delete from xt_learning_track_content_partner_activity where learning_track_visibility_id in (:visibilityIds)";
		Query query = session.createSQLQuery(sql);
		query.setParameterList(VISIBILITY_IDS_PARAMETER, visibilityIds);
		query.executeUpdate();

	}

	@Override
	public int getDameCountByLearningId(Integer learningTrackId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select cast(count(*) as int) from xt_learning_track_content where dam_id is not null and learning_track_id=:learningTrackId";
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter(LEARNING_TRACK_ID, learningTrackId);
		return (Integer) query.uniqueResult();
	}

	@Override
	public void saveAllContent(List<LearningTrackContent> learningTrackContents) {
		try {
			Session session = sessionFactory.getCurrentSession();
			for (int i = 0; i < learningTrackContents.size(); i++) {
				LearningTrackContent learningTrackContent = learningTrackContents.get(i);
				session.save(learningTrackContent);
				if (i % 30 == 0) {
					session.flush();
					session.clear();
				}
			}
		} catch (ResponseUtilException e) {
			throw new ResponseUtilException(e.getMessage());
		}
	}

	@Override
	public void saveAllPartnerActivity(List<LearningTrackPartnerActivity> learningTrackContents) {
		try {
			Session session = sessionFactory.getCurrentSession();
			for (int i = 0; i < learningTrackContents.size(); i++) {
				LearningTrackPartnerActivity learningTrackContent = learningTrackContents.get(i);
				session.save(learningTrackContent);
				if (i % 30 == 0) {
					session.flush();
					session.clear();
				}
			}
		} catch (ResponseUtilException e) {
			throw new ResponseUtilException(e.getMessage());
		}
	}

	/**** XNFR-342 ****/
	@Override
	public List<Integer> findUnPublishedTrackOrPlayBookIdsByCompanyId(Integer companyId, String type) {
		return findUnPublishedAndApprovedTrackOrPlayBookIds(companyId, type);
	}

	/**** XNFR-342 ****/
	@SuppressWarnings("unchecked")
	private List<Integer> findUnPublishedAndApprovedTrackOrPlayBookIds(Integer companyId, String type) {
		String queryString = "select distinct l.id from xt_learning_track l left join \r\n"
				+ "xt_learning_track_visibility lv\r\n"
				+ "on lv.learning_track_id = l.id where l.company_id= :companyId\r\n" + " and l.is_published = false"
				+ " and l.is_publishing_or_white_labeling_in_progress = false and is_publishing_to_partner_list = false and cast(l.type as text) = :type"
				+ " and approval_status = 'APPROVED'";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		QueryParameterDTO companyIdParameterDTO = new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId);
		QueryParameterDTO typeParameterDTO = new QueryParameterDTO("type", type);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(companyIdParameterDTO);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(typeParameterDTO);
		return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

	/**** XNFR-342 ****/
	@Override
	public List<PublishedContentIdAndUserListIdDetailsDTO> findAllPublishedTracksOrPlayBooksByUserListId(
			Integer userListId, String type) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		return findAllPublishedTracksOrPlayBooksByUserListId(userListId, hibernateSQLQueryResultRequestDTO, type);
	}

	/**** XNFR-342 ****/
	@SuppressWarnings("unchecked")
	private List<PublishedContentIdAndUserListIdDetailsDTO> findAllPublishedTracksOrPlayBooksByUserListId(
			Integer userListId, HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO, String type) {
		String queryString = "select distinct lt.id,string_agg(distinct cast(ltv.user_id as text), ',') as \"userIdsAsString\"  from xt_learning_track lt,\n"
				+ "xt_learning_track_visibility ltv,xt_learning_track_visibility_group ltvg\n"
				+ "where lt.id = ltv.learning_track_id and ltv.learning_track_id = lt.id\n"
				+ "and ltvg.visibility_id = ltv.id \n"
				+ "and ltv.is_associated_through_company = false and lt.is_published = true and\n"
				+ "cast(lt.type as text) = :type and ltvg.user_list_id = :userListId\n" + "group by lt.id\n";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		QueryParameterDTO queryParameterDTO = new QueryParameterDTO(XamplifyConstants.USER_LIST_ID, userListId);
		QueryParameterDTO typeParameterDTO = new QueryParameterDTO("type", type);
		List<QueryParameterDTO> queryParameterDTOs = new ArrayList<>();
		queryParameterDTOs.add(queryParameterDTO);
		queryParameterDTOs.add(typeParameterDTO);
		hibernateSQLQueryResultRequestDTO.setQueryParameterDTOs(queryParameterDTOs);
		hibernateSQLQueryResultRequestDTO.setClassInstance(PublishedContentIdAndUserListIdDetailsDTO.class);
		return (List<PublishedContentIdAndUserListIdDetailsDTO>) hibernateSQLQueryResultUtilDao
				.returnDTOList(hibernateSQLQueryResultRequestDTO);
	}

	/**** XNFR-342 ****/
	@Override
	public Map<String, Object> findAllUnPublishedAndFilteredPublishedTracksOrPlayBooks(Pagination pagination,
			String search, String type) {
		String findTracksByIdQueryString = "select xlt.id as \"id\", xlt.title as \"title\",xlt.created_time as \"createdTime\",\n"
				+ "CASE WHEN length(trim(concat(trim(xup.firstname), ' ', trim(xup.lastname)))) >\n"
				+ "0 THEN trim(concat(trim(xup.firstname), ' ', trim(xup.lastname))) ELSE xup.email_id  END AS \"createdBy\"\n"
				+ "from xt_learning_track xlt,xt_user_profile xup\n"
				+ "where xlt.company_id  = :companyId  and xlt.created_by  = xup.user_id\n"
				+ "and xup.company_id  = xlt.company_id and xlt.id in (:ids) and cast(xlt.type as text) = :type and (xlt.expire_date IS NULL OR xlt.expire_date > NOW()) \n"; //XNFR-938
		String sortQueryString = addSortColumns(pagination);
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(findTracksByIdQueryString);
		QueryParameterDTO queryParameterDTO = new QueryParameterDTO(XamplifyConstants.COMPANY_ID,
				pagination.getCompanyId());
		QueryParameterDTO typeParameterDTO = new QueryParameterDTO("type", type);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(queryParameterDTO);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(typeParameterDTO);
		QueryParameterListDTO queryParameterListDTO = new QueryParameterListDTO("ids", pagination.getIds());
		hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs().add(queryParameterListDTO);
		hibernateSQLQueryResultRequestDTO.setSortQueryString(sortQueryString);
		hibernateSQLQueryResultRequestDTO.setClassInstance(ShareLearningTrackResponseDTO.class);
		List<String> searchColumns = new ArrayList<>();
		searchColumns.add("xup.email_id");
		searchColumns.add("xup.firstname");
		searchColumns.add("xup.lastname");
		searchColumns.add("xlt.title");
		hibernateSQLQueryResultRequestDTO.setSearchColumns(searchColumns);
		return hibernateSQLQueryResultUtilDao.returnPaginatedDTOList(hibernateSQLQueryResultRequestDTO, pagination,
				search);
	}

	private String addSortColumns(Pagination pagination) {
		List<SortColumnDTO> sortColumnDTOs = new ArrayList<>();
		SortColumnDTO createdTimeSortOption = new SortColumnDTO(createdTimePropertyName, "xlt.created_time", true, true,
				false);
		SortColumnDTO titleSortOption = new SortColumnDTO("assetName", "xlt.title", false, true, false);
		sortColumnDTOs.add(createdTimeSortOption);
		sortColumnDTOs.add(titleSortOption);
		return paginationUtil.generateSortQuery(pagination, sortColumnDTOs, "desc");
	}

	/**** XNFR-342 ****/
	@SuppressWarnings("unchecked")
	@Override
	public List<LearningTrack> findByIds(Set<Integer> ids) {
		try {
			if (XamplifyUtils.isNotEmptySet(ids)) {
				Session session = sessionFactory.getCurrentSession();
				Criteria criteria = session.createCriteria(LearningTrack.class);
				criteria.add(Restrictions.in("id", ids));
				return criteria.list();
			} else {
				return Collections.emptyList();
			}
		} catch (HibernateException e) {
			throw new DamDataAccessException(e);
		}

	}

	/**** XNFR-342 ****/
	@Override
	public void updatePublishedStatusByIds(Set<Integer> ids) {
		if (XamplifyUtils.isNotEmptySet(ids)) {
			String queryString = "update xt_learning_track set is_published = true where id in (:ids)";
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			QueryParameterListDTO queryParameterListDTO = new QueryParameterListDTO("ids",
					XamplifyUtils.convertSetToList(ids));
			hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs().add(queryParameterListDTO);
			hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);

		}

	}

	/**** XNFR-342 ****/
	@Override
	public boolean isDataExistsInLearningVisibilityGroupByUserListAndVisibilityId(Integer userListId,
			Integer learningTrackVisibilityId) {
		String queryString = "select case when count(*)>0 then true else false end as rowExists\n"
				+ "from xt_learning_track_visibility_group where visibility_id = :visibilityId and user_list_id = :userListId \n";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		List<QueryParameterDTO> queryParameterDTOs = new ArrayList<>();
		queryParameterDTOs.add(new QueryParameterDTO(XamplifyConstants.USER_LIST_ID, userListId));
		queryParameterDTOs.add(new QueryParameterDTO(VISIBILITY_ID, learningTrackVisibilityId));
		hibernateSQLQueryResultRequestDTO.setQueryParameterDTOs(queryParameterDTOs);
		return hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

	/**** XNFR-342 ****/
	@Override
	public void updateIsPublishedToPartnerListByIds(Set<Integer> ids, boolean value) {
		if (XamplifyUtils.isNotEmptySet(ids)) {
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(
					"update xt_learning_track set is_publishing_to_partner_list = :value where id in(:ids)");
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("value", value));
			hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
					.add(new QueryParameterListDTO("ids", XamplifyUtils.convertSetToList(ids)));
			hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
		}

	}

	/**** XNFR-342 ****/
	@Override
	public void deleteUnPublishedPartnerCompaniesOrPartnerLists(Set<Integer> userListIds,
			Set<Integer> trackOrPlayBookIds) {
		deletePartnerCompanies(trackOrPlayBookIds);

		List<Integer> visibilityIdsByTrackOrPlayBookIds = findVisibilityIdsByTrackOrPlayBookIds(trackOrPlayBookIds);

		List<Integer> visibilityIdsByUserListIdsAndTrackOrPlayBookIds = findVisibilityIdsByUserListIdsAndTrackOrPlayBookIds(
				userListIds, trackOrPlayBookIds);

		deleteVisibilityGroupIds(userListIds, visibilityIdsByUserListIdsAndTrackOrPlayBookIds);

		visibilityIdsByTrackOrPlayBookIds.removeAll(visibilityIdsByUserListIdsAndTrackOrPlayBookIds);
		deleteVisibilityIds(visibilityIdsByTrackOrPlayBookIds);

	}

	private void deleteVisibilityGroupIds(Set<Integer> userListIds,
			List<Integer> visibilityIdsByUserListIdsAndTrackOrPlayBookIds) {
		if (XamplifyUtils.isNotEmptyList(visibilityIdsByUserListIdsAndTrackOrPlayBookIds)
				&& XamplifyUtils.isNotEmptySet(userListIds)) {
			String deleteVisibilityGroupIdsQueryString = "delete from xt_learning_track_visibility_group where\n"
					+ "visibility_id in (:visibilityIds)\n" + "and user_list_id not in (:userListIds)\n";
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(deleteVisibilityGroupIdsQueryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs().add(new QueryParameterListDTO(
					VISIBILITY_IDS_PARAMETER, visibilityIdsByUserListIdsAndTrackOrPlayBookIds));
			hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
					.add(new QueryParameterListDTO("userListIds", XamplifyUtils.convertSetToList(userListIds)));
			hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);

		}
	}

	private void deleteVisibilityIds(List<Integer> visibilityIds) {
		if (XamplifyUtils.isNotEmptyList(visibilityIds)) {
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO
					.setQueryString("delete from xt_learning_track_visibility where id in (:visibilityIds)");
			hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
					.add(new QueryParameterListDTO(VISIBILITY_IDS_PARAMETER, visibilityIds));
			hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
		}
	}

	@SuppressWarnings("unchecked")
	private List<Integer> findVisibilityIdsByUserListIdsAndTrackOrPlayBookIds(Set<Integer> userListIds,
			Set<Integer> trackOrPlayBookIds) {
		String findVisibilityIdsQueryString = "select\r\n" + "distinct xltv.id\r\n" + "from\r\n"
				+ "xt_learning_track_visibility xltv,\r\n" + "xt_learning_track_visibility_group xltvg\r\n"
				+ "where\r\n" + "xltv.learning_track_id in(:ids)\r\n" + "and\r\n" + "xltv.id = xltvg.visibility_id\r\n"
				+ "and xltvg.user_list_id in (:userListIds)";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(findVisibilityIdsQueryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
				.add(new QueryParameterListDTO("ids", XamplifyUtils.convertSetToList(trackOrPlayBookIds)));
		hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
				.add(new QueryParameterListDTO("userListIds", XamplifyUtils.convertSetToList(userListIds)));
		return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

	@SuppressWarnings({ "unchecked" })
	private List<Integer> findVisibilityIdsByTrackOrPlayBookIds(Set<Integer> trackOrPlayBookIds) {
		String findVisibilityIdsQueryString = "select\r\n" + "distinct xltv.id\r\n" + "from\r\n "
				+ "xt_learning_track_visibility xltv,xt_learning_track_visibility_group xltvg where\r\n "
				+ "xltv.learning_track_id in(:ids)\r\n" + "and\r\n" + "xltv.id = xltvg.visibility_id\r\n";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(findVisibilityIdsQueryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
				.add(new QueryParameterListDTO("ids", XamplifyUtils.convertSetToList(trackOrPlayBookIds)));
		return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

	private void deletePartnerCompanies(Set<Integer> trackOrPlayBookIds) {
		String sqlString = "delete from xt_learning_track_visibility where is_associated_through_company\r\n"
				+ "and learning_track_id in (:ids)";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
				.add(new QueryParameterListDTO("ids", XamplifyUtils.convertSetToList(trackOrPlayBookIds)));
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
	}

	/**** XNFR-342 ****/
	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findPublishedPartnerIdsByUserListIdAndId(Integer userListId, Integer id) {
		if (XamplifyUtils.isValidInteger(id) && XamplifyUtils.isValidInteger(userListId)) {
			String sqlString = "select distinct xltv.user_id  from xt_learning_track_visibility xltv,\r\n"
					+ "xt_learning_track xlt,xt_learning_track_visibility_group xltvg \r\n"
					+ "where xlt.id  = xltv.learning_track_id \r\n"
					+ "and xlt.is_published and xltv.is_published and xlt.id  = :id\r\n"
					+ "and xltvg.visibility_id  = xltv.id and xltvg.user_list_id  = :userListId";
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(sqlString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("id", id));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(XamplifyConstants.USER_LIST_ID, userListId));
			return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
		} else {
			return Collections.emptyList();
		}

	}

	/**** XNFR-342 ****/
	@SuppressWarnings("unchecked")
	@Override
	public List<PublishedContentIdAndUserListIdDetailsDTO> findAllPublishedTrackOrPlayBooksByUserListId(
			Integer userListId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "select distinct xlt.id as \"id\",string_agg(distinct cast(xltv.user_id as text), ',') as \"userIdsAsString\"\r\n"
				+ "from xt_learning_track xlt,xt_learning_track_visibility xltv,\r\n"
				+ "xt_learning_track_visibility_group xltvg,xt_user_list xul ,xt_user_userlist xuu \r\n"
				+ "where xlt.id = xltv.learning_track_id \r\n"
				+ "and xltv.id = xltvg.visibility_id and xlt.is_published and \r\n"
				+ "xltv.is_published and xltv.is_associated_through_company  = false and\r\n"
				+ "xltvg.user_list_id  = :userListId and xul.user_list_id  = xltvg.user_list_id \r\n"
				+ "and xul.user_list_id  = xuu.user_list_id and xuu.user_id  = xltv.user_id  \r\n" + "group by xlt.id ";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		QueryParameterDTO queryParameterDTO = new QueryParameterDTO(XamplifyConstants.USER_LIST_ID, userListId);
		List<QueryParameterDTO> queryParameterDTOs = new ArrayList<>();
		queryParameterDTOs.add(queryParameterDTO);
		hibernateSQLQueryResultRequestDTO.setQueryParameterDTOs(queryParameterDTOs);
		hibernateSQLQueryResultRequestDTO.setClassInstance(PublishedContentIdAndUserListIdDetailsDTO.class);
		return (List<PublishedContentIdAndUserListIdDetailsDTO>) hibernateSQLQueryResultUtilDao
				.returnDTOList(hibernateSQLQueryResultRequestDTO);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<LearningTrackVisibility> findVisibilityUsersById(Integer learningTrackId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(LearningTrackVisibility.class);
		criteria.add(Restrictions.eq(LEARNING_TRACK_ENTITY_ID_PARAMETER, learningTrackId));
		return criteria.list();
	}

	@Override
	public List<String> findTrackNamesByAssetId(Integer id) {
		String type = LearningTrackType.TRACK.name();
		return findTrackOrPlayBookNamesByDamIdAndType(id, type);
	}

	@Override
	public List<String> findPlayBookNamesByAssetId(Integer id) {
		String type = LearningTrackType.PLAYBOOK.name();
		return findTrackOrPlayBookNamesByDamIdAndType(id, type);
	}

	@SuppressWarnings("unchecked")
	private List<String> findTrackOrPlayBookNamesByDamIdAndType(Integer id, String type) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sqlString = "select distinct l.title from xt_learning_track l, \r\n"
				+ "xt_learning_track_content ltc where l.id = ltc.learning_track_id and ltc.dam_id =:id and cast(l.type as text) = :type";
		hibernateSQLQueryResultRequestDTO.setQueryString(sqlString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("id", id));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("type", type));
		return (List<String>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public void updateTrackDescriptionWithReplacedVideoUri(String replacedVideoUri, String previousVideoUri) {
		String queryString = "update xt_learning_track\r\n" + "set description = replace(description,'"
				+ previousVideoUri + "',\r\n" + "'" + replacedVideoUri + "')\r\n" + "where description like '%"
				+ previousVideoUri + "%'";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public LearningTrack findById(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(LearningTrack.class);
		criteria.add(Restrictions.eq("id", id));
		return (LearningTrack) criteria.uniqueResult();
	}

	@Override
	public LearningTrackContentDto getLearningTrackIdByContentId(Integer learningTrackContentId) {
		String queryString = "select learning_track_id as \"id\", dam_id as \"damId\"  from xt_learning_track_content where id =:learningTrackContentId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("learningTrackContentId", learningTrackContentId));
		return (LearningTrackContentDto) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO,
				LearningTrackContentDto.class);
	}

	@Override
	public List<Integer> findContentIdsByTrackOrPlayBookId(Integer trackOrPlayBookId) {
		String queryString = "select distinct dam_id from xt_learning_track_content xltc where learning_track_id  = :id and dam_id is not null";
		return addIdParameterAndReturnList(trackOrPlayBookId, queryString);
	}

	@Override
	public List<Integer> findVisibilityUserIdsByTrackOrPlayBookId(Integer trackOrPlayBookId) {
		String queryString = "select distinct u.user_id from xt_learning_track_visibility v, xt_user_profile u, xt_company_profile c where v.learning_track_id = :id  and u.user_id = v.user_id  \r\n"
				+ "and u.company_id = c.company_id";
		return addIdParameterAndReturnList(trackOrPlayBookId, queryString);
	}

	@Override
	public List<Integer> findProgressedVisibilityUserIdsByTrackOrPlayBookId(Integer trackOrPlayBookId) {
		String queryString = "select distinct u.user_id from xt_learning_track_visibility v, xt_user_profile u, xt_company_profile c where v.learning_track_id = :id  and v.progress > 0  and u.user_id = v.user_id  \r\n"
				+ "and u.company_id = c.company_id";
		return addIdParameterAndReturnList(trackOrPlayBookId, queryString);
	}

	@Override
	public List<Integer> findQuizIdsByTrackOrPlayBookId(Integer trackOrPlayBookId) {
		String queryString = "select distinct quiz_id from xt_learning_track_content xltc where learning_track_id  = :id and quiz_id is not null";
		return addIdParameterAndReturnList(trackOrPlayBookId, queryString);
	}

	@SuppressWarnings("unchecked")
	private List<Integer> addIdParameterAndReturnList(Integer trackOrPlayBookId, String queryString) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("id", trackOrPlayBookId));
		return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<LearningTrackContentResponseDTO> findLearningTrackContentsByTrackOrPlayBookId(
			Integer trackOrPlayBookId) {
		String queryString = "select dam_id as \"damId\", display_index as \"displayIndex\", quiz_id as \"quizId\"  from xt_learning_track_content where learning_track_id =:trackOrPlayBookId order by display_index asc";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("trackOrPlayBookId", trackOrPlayBookId));
		hibernateSQLQueryResultRequestDTO.setClassInstance(LearningTrackContentResponseDTO.class);
		return (List<LearningTrackContentResponseDTO>) hibernateSQLQueryResultUtilDao
				.returnDTOList(hibernateSQLQueryResultRequestDTO);

	}

	/** XNFR-745 **/
	@Override
	public PreviewPlaybookResponseDTO getPlaybookBySlug(String type, Integer companyId, String slug) {
		String queryString = "SELECT xlt.id AS \"id\", xlt.title AS \"title\", xlt.description AS \"description\", xlt.slug AS \"slug\", "
				+ "xlt.featured_image AS \"featuredImage\", cast(xlt.created_time as text) AS \"createdTime\", xlt.created_by AS \"createdBy\", "
				+ "CAST(xlt.type AS TEXT) AS \"type\", CONCAT(xup.firstname, ' ', xup.lastname) AS \"createdByName\", TO_CHAR(xlt.expire_date, 'MM/DD/YYYY HH12:MI AM') AS \"expireDate\" "
				+ "FROM xt_learning_track xlt INNER JOIN xt_user_profile xup ON xlt.created_by = xup.user_id "
				+ "WHERE xlt.slug = :slug AND cast(xlt.type as text) = :type AND xlt.company_id = :companyId "
				+ "ORDER BY 1 DESC";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("type", type));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("slug", slug));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(COMPANY_ID, companyId));
		return (PreviewPlaybookResponseDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO,
				PreviewPlaybookResponseDTO.class);
	}

	/** XNFR-745 **/
	@SuppressWarnings("unchecked")
	@Override
	public List<PlaybookContentCategoryListDTO> getCategoryNamesWithDamIdsForPalybookById(Integer id, String sortKey) {
		String queryString = "SELECT cast(xc.name as text) AS \"categoryName\", string_agg(cast(xd.id as text), ',') as \"damIds\", cast(COUNT(xd.id) as int) AS \"count\" "
				+ "FROM xt_learning_track_content xltc INNER JOIN xt_dam xd ON xd.id = xltc.dam_id "
				+ "INNER JOIN xt_category_module xcm ON xd.id = xcm.dam_id "
				+ "INNER JOIN xt_category xc ON xcm.category_id = xc.id WHERE xltc.learning_track_id = :id "
				+ "GROUP BY xc.name ORDER BY xc.name";
		if (sortKey.equalsIgnoreCase("folderName-DESC")) {
			queryString += " desc";
		}
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("id", id));
		hibernateSQLQueryResultRequestDTO.setClassInstance(PlaybookContentCategoryListDTO.class);
		return (List<PlaybookContentCategoryListDTO>) hibernateSQLQueryResultUtilDao
				.returnDTOList(hibernateSQLQueryResultRequestDTO);
	}

	/** XNFR-745 **/
	@SuppressWarnings("unchecked")
	@Override
	public List<PlaybookAssetResponseDTO> getAssetsWithCategoryIdForPlaybooksByDamId(List<Integer> ids, String serverPath, Integer playbookId) {
		String queryString = "SELECT xd.id AS \"id\", xd.asset_name AS \"assetName\", "
				+ "CASE WHEN (xd.asset_path IS NULL OR xd.asset_path = '') and xd.video_id is not null THEN concat(:serverPath , xvf.videouri) ELSE xd.asset_path "
				+ "END AS \"assetPath\", xd.is_bee_template as \"beeTemplate\", xd.description as \"description\", "
				+ "CASE WHEN (xd.thumbnail_path IS NULL OR xd.thumbnail_path = '') and xd.video_id is not null THEN concat(:serverPath , xvf.imageuri) "
				+ "ELSE xd.thumbnail_path end as \"thumbnailPath\", CAST(xd.asset_type AS TEXT) AS \"assetType\", xd.video_id as \"videoId\", xltc.id as \"learningTrackContentMappingId\" "
				+ "FROM xt_dam xd left join xt_video_files as xvf on xd.video_id = xvf.id "
				+ "inner join xt_learning_track_content xltc on xd.id = xltc.dam_id and xltc.learning_track_id = :playbookId WHERE xd.id IN (:ids) order by xd.id desc";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("serverPath", serverPath));
		hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs().add(new QueryParameterListDTO("ids", ids));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("playbookId", playbookId));
		hibernateSQLQueryResultRequestDTO.setClassInstance(PlaybookAssetResponseDTO.class);
		return (List<PlaybookAssetResponseDTO>) hibernateSQLQueryResultUtilDao
				.returnDTOList(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public boolean checkGroupByAssetsEnabled(String type, Integer companyId, String slug) {
		String queryString = "select is_group_by_assets from xt_learning_track where company_id = :companyId and slug = :slug and lower(cast(type as text)) = lower(:type)";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(COMPANY_ID, companyId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("slug", slug));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("type", type));
		return hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public boolean isRowPresentInLearningTrackVisiblity(Integer learningTrackId, Integer partnershipId) {
		String queryString = "select case when count(*)>0 then true else false end from xt_learning_track_visibility where learning_track_id = :learningTrackId and partnership_id = :partnershipId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		addLearningTrackAndPartnershipParameters(learningTrackId, partnershipId, hibernateSQLQueryResultRequestDTO);
		return hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public void updatePublishedStatusByLearningTrackIdAndPartnershipId(Integer learningTrackId, Integer partnershipId) {
		String queryString = "update xt_learning_track_visibility set is_published = :published where partnership_id = :partnershipId and learning_track_id = :learningTrackId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		addLearningTrackAndPartnershipParameters(learningTrackId, partnershipId, hibernateSQLQueryResultRequestDTO);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("published", true));
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public boolean isRowPresentInLearningTrackVisibilityGroup(Integer learningTrackId, Integer partnershipId,
			Integer userListId) {
		String queryString = "select case when count(xltvg.*)>0 then true else false end from xt_learning_track_visibility xltv,\r\n"
				+ "xt_learning_track_visibility_group xltvg where xltv.learning_track_id = :learningTrackId and xltv.partnership_id = :partnershipId\r\n"
				+ "and xltvg.visibility_id  = xltv.id and xltvg.user_list_id  = :userListId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		addLearningTrackAndPartnershipParameters(learningTrackId, partnershipId, hibernateSQLQueryResultRequestDTO);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(USER_LIST_ID, userListId));
		return hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

	private void addLearningTrackAndPartnershipParameters(Integer learningTrackId, Integer partnershipId,
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO) {
		addLearningTrackIdParameter(learningTrackId, hibernateSQLQueryResultRequestDTO);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("partnershipId", partnershipId));
	}

	private void addLearningTrackIdParameter(Integer learningTrackId,
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO) {
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(LEARNING_TRACK_ID, learningTrackId));
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findVisiblityIds(Integer learningTrackId, Integer partnershipId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(
				"select id from xt_learning_track_visibility where learning_track_id = :learningTrackId and partnership_id = :partnershipId order by id asc");
		addLearningTrackAndPartnershipParameters(learningTrackId, partnershipId, hibernateSQLQueryResultRequestDTO);
		return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findPublishedPartnershipIds(Integer learningTrackId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(
				"select distinct partnership_id from xt_learning_track_visibility xltv where xltv.learning_track_id  = :learningTrackId and is_published order by xltv.partnership_id asc");
		addLearningTrackIdParameter(learningTrackId, hibernateSQLQueryResultRequestDTO);
		return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findVisibilityIdsByPartnershipIds(List<Integer> partnershipIds, Integer userListId) {
		if (XamplifyUtils.isNotEmptyList(partnershipIds) && XamplifyUtils.isValidInteger(userListId)) {
			List<Integer> visbilityIds = new ArrayList<>();
			String queryString = "select distinct xltv.id from xt_learning_track_visibility xltv, xt_learning_track_visibility_group xltvg\r\n"
					+ "where xltv.id  = xltvg.visibility_id and xltvg.user_list_id  = :userListId and xltv.partnership_id in (:partnershipIds) order  by  1 asc";
			Session session = sessionFactory.getCurrentSession();
			List<List<Integer>> chunkedIds = XamplifyUtils.getChunkedList(partnershipIds);
			for (List<Integer> splittedIds : chunkedIds) {
				visbilityIds.addAll(session.createSQLQuery(queryString).setParameterList("partnershipIds", splittedIds)
						.setParameter(USER_LIST_ID, userListId).list());
			}
			XamplifyUtils.removeNullsFromList(visbilityIds);
			return visbilityIds;
		} else {
			return Collections.emptyList();
		}

	}

	@Override
	public void deleteLearningTrackVisibilityGroups(List<Integer> visibilityIds, Integer userListId) {
		if (XamplifyUtils.isNotEmptyList(visibilityIds) && XamplifyUtils.isValidInteger(userListId)) {
			String deleteLearningTrackVisibilityGroupQueryString = "delete from xt_learning_track_visibility_group where visibility_id = :visibilityId and user_list_id = :userListId";
			String deleteLearningTrackVisibility = "delete from xt_learning_track_visibility where id = :visibilityId";
			int counter = 1;
			int total = visibilityIds.size();
			for (Integer visibilityId : visibilityIds) {
				deleteVisibilityGroup(deleteLearningTrackVisibilityGroupQueryString, visibilityId, userListId);
				boolean isVisbilityRowEmpty = checkIfVisibilityRowExists(visibilityId);
				if (isVisbilityRowEmpty) {
					deleteVisibility(deleteLearningTrackVisibility, visibilityId);
				}
				String debugMessage = counter + "/" + total + " deleted from visbility group at " + new Date();
				logger.debug(debugMessage);
				counter++;
			}

		}

	}

	private boolean checkIfVisibilityRowExists(Integer visibilityId) {
		boolean isVisbilityRowCanBeDeleted = false;
		try (Session session = sessionFactory.openSession()) {
			boolean isVisbilityRowEmpty = (boolean) session.createSQLQuery(
					"select case when count(*) > 0 then false else true end from xt_learning_track_visibility_group where visibility_id = :visibilityId")
					.setParameter(VISIBILITY_ID, visibilityId).uniqueResult();

			boolean isAssociatedThroughCompany = (boolean) session.createSQLQuery(
					"select is_associated_through_company from  xt_learning_track_visibility where id = :visibilityId")
					.setParameter(VISIBILITY_ID, visibilityId).uniqueResult();

			isVisbilityRowCanBeDeleted = isVisbilityRowEmpty && !isAssociatedThroughCompany;
		} catch (HibernateException | DataIntegrityViolationException e) {
			ExceptionHandlerUtil.handleException(e);
			return false; // Handle gracefully by returning a default value
		} catch (Exception ex) {
			ExceptionHandlerUtil.handleException(ex);
			return false; // Handle gracefully by returning a default value
		}
		return isVisbilityRowCanBeDeleted;
	}

	private void deleteVisibilityGroup(String sqlQueryString, Integer visibilityId, Integer userListId) {
		try (Session session = sessionFactory.openSession()) {
			session.createSQLQuery(sqlQueryString).setParameter(VISIBILITY_ID, visibilityId)
					.setParameter(USER_LIST_ID, userListId).executeUpdate();
			session.flush();
			session.clear();
		} catch (HibernateException | DataIntegrityViolationException e) {
			ExceptionHandlerUtil.handleException(e);
		} catch (Exception ex) {
			ExceptionHandlerUtil.handleException(ex);
		}
	}

	private void deleteVisibility(String sqlQueryString, Integer visibilityId) {
		try (Session session = sessionFactory.openSession()) {
			session.createSQLQuery(sqlQueryString).setParameter(VISIBILITY_ID, visibilityId).executeUpdate();
			session.flush();
			session.clear();
			String debugMessage = "row (" + visibilityId + ") has been  deleted from visbility at " + new Date();
			logger.debug(debugMessage);
		} catch (HibernateException | DataIntegrityViolationException e) {
			ExceptionHandlerUtil.handleException(e);
		} catch (Exception ex) {
			ExceptionHandlerUtil.handleException(ex);
		}
	}

	@Override
	public boolean isPublished(Integer learningTrackId) {
		String queryString = "select is_published from xt_learning_track where id = :learningTrackId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		addLearningTrackIdParameter(learningTrackId, hibernateSQLQueryResultRequestDTO);
		return hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

	/*** XNFR-758 *****/
	private void addUserAndCategoryClassForSearch(Pagination pagination, Disjunction disjunction) {
		String searchKey = pagination.getSearchKey();
		if (!StringUtils.hasText(searchKey)) {
			return; // Exit early if searchKey is empty
		}
		searchKey = XamplifyUtils.addBackSlashToSpecialCharacters(searchKey);
		searchKey = XamplifyUtils.escapeSingleQuotesForSearchQuery(searchKey.replaceAll("['\"]", "_"));

		// Add category search
		DetachedCriteria categoryQuery = DetachedCriteria.forClass(Category.class, "category")
				.add(Restrictions.eqProperty(CM_CATEGORY_ID, "category.id"))
				.add(Restrictions.ilike("category.name", searchKey, MatchMode.ANYWHERE))
				.setProjection(Projections.distinct(Projections.property("category.id")));
		applyCompanyFilters(categoryQuery, pagination);
		disjunction.add(Subqueries.propertyIn(CM_CATEGORY_ID, categoryQuery));
	}

	private void applyCompanyFilters(DetachedCriteria query, Pagination pagination) {
		if (pagination.isPartnerView()) {
			if (XamplifyUtils.isValidInteger(pagination.getVendorCompanyId())) {
				query.add(Restrictions.eq(L_COMPANY_ID, pagination.getVendorCompanyId()));
			}
			query.add(Restrictions.eq(VC_USER_ID, pagination.getUserId()));
		} else {
			query.add(Restrictions.eq(L_COMPANY_ID, pagination.getCompanyId()));
		}
	}

	/*** XNFR-758 *****/

	/** XNFR-824 **/
	public ApprovalStatusType getApprovalStatusEnumType(String selectedTileCategory) {
		switch (selectedTileCategory.toUpperCase()) {
		case "APPROVED":
			return ApprovalStatusType.APPROVED;
		case "REJECTED":
			return ApprovalStatusType.REJECTED;
		case "CREATED":
			return ApprovalStatusType.CREATED;
		default:
			return ApprovalStatusType.APPROVED;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<LearningTrackContentDto> getTrackContentByLearningTrackId(Integer learningTrackId) {
		String queryString = "SELECT id as \"id\", dam_id as \"damId\", quiz_id as \"quizId\" \n"
				+ "FROM xt_learning_track_content WHERE learning_track_id = :id \n order by display_index";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("id", learningTrackId));
		return (List<LearningTrackContentDto>) hibernateSQLQueryResultUtilDao
				.getListDto(hibernateSQLQueryResultRequestDTO, LearningTrackContentDto.class);
	}

	@Override
	public DamListDTO getAssetDetailsByDamIdAndTrackId(Integer damId, String serverPath, Integer trackId) {
		String queryString = "SELECT xd.id AS \"id\", xd.asset_name AS \"assetName\", "
				+ "CASE WHEN (xd.asset_path IS NULL OR xd.asset_path = '') and xd.video_id is not null THEN concat(:serverPath , xvf.videouri) ELSE xd.asset_path "
				+ "END AS \"assetPath\", xd.is_bee_template as \"beeTemplate\", cast(xc.name as text) as \"categoryName\", xd.description as \"description\", "
				+ "CASE WHEN (xd.thumbnail_path IS NULL OR xd.thumbnail_path = '') and xd.video_id is not null THEN concat(:serverPath , xvf.imageuri) "
				+ "ELSE xd.thumbnail_path end as \"thumbnailPath\", CAST(xd.asset_type AS TEXT) AS \"assetType\", xd.video_id as \"videoId\", xltc.id as \"learningTrackContentMappingId\" "
				+ "FROM xt_dam xd left join xt_video_files as xvf on xd.video_id = xvf.id INNER JOIN xt_category_module xcm ON xd.id = xcm.dam_id "
				+ "INNER JOIN xt_category xc ON xcm.category_id = xc.id inner join xt_learning_track_content xltc on xd.id = xltc.dam_id "
				+ "and xltc.learning_track_id = :trackId WHERE xd.id = :damId order by xltc.display_index";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("serverPath", serverPath));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("damId", damId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("trackId", trackId));
		hibernateSQLQueryResultRequestDTO.setClassInstance(DamListDTO.class);
		return (DamListDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO, DamListDTO.class);
	}

	@Override
	public FormDTO findQuizDetailsByQuizId(Integer quizId) {
		String queryString = "select id as \"id\", form_name as \"name\", alias as \"alias\" \n from xt_form where id = :id";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("id", quizId));
		return (FormDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO, FormDTO.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getPartnerActivityType(Integer trackContentId) {
		String queryString = "select type from xt_learning_track_content_partner_activity where learning_track_content_id = :id";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("id", trackContentId));
		return (List<String>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public boolean getLearningTrackFormSubmissionByFormID(Integer learningTrackId, Integer loggedInUserId,
			Integer quizId) {
		String queryString = "select id from xt_form_submit where form_id = :formId and user_id = :userId "
				+ "and learning_track_id = :learningTrackId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(FORM_ID, quizId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("userId", loggedInUserId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(LEARNING_TRACK_ID, learningTrackId));
		Integer id = (Integer) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
		return (id != null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getVisibilityByLearningTrackId(Integer learningTrackId) {
		String queryString = "SELECT DISTINCT CASE WHEN v.is_associated_through_company THEN v.user_id ELSE NULL "
				+ "END AS userIds, \n CASE WHEN v.is_associated_through_company THEN v.partnership_id ELSE NULL "
				+ "END AS partnershipIds, \n g.user_list_id AS groupIds \n FROM xt_learning_track_visibility v "
				+ "LEFT JOIN xt_learning_track_visibility_group g ON g.visibility_id = v.id \n"
				+ "WHERE v.learning_track_id = :learningTrackId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(LEARNING_TRACK_ID, learningTrackId));
		return (List<Object[]>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}
	
	public String getCategoryNameByDamId(Dam dam,Form form) {
		Session session = sessionFactory.openSession();
		String categoryName = null;
		try {
			String sql = "SELECT cast(cat.name as text) FROM xt_category cat " +
					"JOIN xt_category_module cm ON cm.category_id = cat.id " ;

			if(dam!= null && XamplifyUtils.isValidInteger(dam.getId())) {
				sql = sql + "WHERE cm.dam_id = :damId";
			} else if(form!= null && XamplifyUtils.isValidInteger(form.getId())) {
				sql = sql + "WHERE cm.form_id = :formId";
			}
			Query query = session.createSQLQuery(sql);
			if(dam!= null && XamplifyUtils.isValidInteger(dam.getId())) {
				query.setParameter("damId", dam.getId());
			} else if(form!= null && XamplifyUtils.isValidInteger(form.getId())) {
				query.setParameter(FORM_ID, form.getId());
			}

			categoryName = (String) query.uniqueResult();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			session.close();
		}
		return categoryName;
	}
	
	// XNFR-1032
	@Override
	public Map<String, Object> getContentCounts(Integer companyId, Integer loggedInUserId, Integer vendorCompanyId,
			boolean hasAccesssToPartner) {
		Session session = sessionFactory.getCurrentSession();
		Map<String, Object> map = new HashMap<>();
		String finalPlaybooksSharedCountQuery = playbooksSharedCountQuery;
		String finalAssetsSharedCountQuery = assetsSharedCountQuery;
		String finalTracksSharedCountQuery = tracksSharedCountQuery;

		if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
			finalPlaybooksSharedCountQuery += AND_XLT_COMPANY_ID + vendorCompanyId + " ";
			finalTracksSharedCountQuery += AND_XLT_COMPANY_ID + vendorCompanyId + " ";
			finalAssetsSharedCountQuery += "and xd.company_id = " + vendorCompanyId + " ";
		}

		SQLQuery query = session.createSQLQuery(tracksCountQuery);
		query.setInteger(COMPANY_ID, companyId);
		map.put("tracksCount", query.uniqueResult());

		query = session.createSQLQuery(playbooksCountQuery);
		query.setInteger(COMPANY_ID, companyId);
		map.put("playbooksCount", query.uniqueResult());

		String partnerAssetCountQuery = hasAccesssToPartner 
				? " and xd.created_for_company = :vendorCompanyId"
				: " and xd.created_for_company is null";
		query = session.createSQLQuery(assetsCountQuery + partnerAssetCountQuery);
		query.setInteger(COMPANY_ID, companyId);
		if (hasAccesssToPartner) { query.setInteger(VENDOR_COMPANY_ID, vendorCompanyId); }
		map.put("assetsCount", query.uniqueResult());

		query = session.createSQLQuery(finalTracksSharedCountQuery);
		query.setInteger(LOGGEDIN_USER_ID, loggedInUserId);
		map.put("sharedTracksCount", query.uniqueResult());

		query = session.createSQLQuery(finalPlaybooksSharedCountQuery);
		query.setInteger(LOGGEDIN_USER_ID, loggedInUserId);
		map.put("sharedPlaybooksCount", query.uniqueResult());

		query = session.createSQLQuery(finalAssetsSharedCountQuery);
		query.setInteger(COMPANY_ID, companyId);
		query.setInteger(LOGGEDIN_USER_ID, loggedInUserId);
		map.put("sharedAssetsCount", query.uniqueResult());

		return map;
	}

	@Override
	public Map<String, Object> getManageContentCounts(Integer companyId, Integer loggedInUserId,
			String moduleContentType) {

		Session session = sessionFactory.getCurrentSession();
		Map<String, Object> map = new HashMap<>();

		if (moduleContentType.equals("TRACK")) {
			SQLQuery tracksQuery = session.createSQLQuery(tracksCountQuery);
			tracksQuery.setInteger(COMPANY_ID, companyId);
			map.put(ALL_COUNT, tracksQuery.uniqueResult());

			tracksQuery = session.createSQLQuery(tracksPublishedCountQuery);
			tracksQuery.setInteger(COMPANY_ID, companyId);
			map.put(PUBLISHED_COUNT, tracksQuery.uniqueResult());

			tracksQuery = session.createSQLQuery(tracksUnpublishedCountQuery);
			tracksQuery.setInteger(COMPANY_ID, companyId);
			map.put(UNPUBLISHED_COUNT, tracksQuery.uniqueResult());

			tracksQuery = session.createSQLQuery(manageTracksFolderCount);
			tracksQuery.setInteger(COMPANY_ID, companyId);
			map.put(MANAGE_FOLDER_COUNT, tracksQuery.uniqueResult());
		}

		if (moduleContentType.equals("PLAYBOOK")) {
			SQLQuery playbookQuery = session.createSQLQuery(playbooksCountQuery);
			playbookQuery.setInteger(COMPANY_ID, companyId);
			map.put(ALL_COUNT, playbookQuery.uniqueResult());

			playbookQuery = session.createSQLQuery(playbooksPublishedCountQuery);
			playbookQuery.setInteger(COMPANY_ID, companyId);
			map.put(PUBLISHED_COUNT, playbookQuery.uniqueResult());

			playbookQuery = session.createSQLQuery(playbooksUnpublishedCountQuery);
			playbookQuery.setInteger(COMPANY_ID, companyId);
			map.put(UNPUBLISHED_COUNT, playbookQuery.uniqueResult());

			playbookQuery = session.createSQLQuery(managePlaybookFolderCount);
			playbookQuery.setInteger(COMPANY_ID, companyId);
			map.put(MANAGE_FOLDER_COUNT, playbookQuery.uniqueResult());

		}

		if (moduleContentType.equals("DAM")) {
			String partnerAssetsQuery = " and xd.created_for_company is null ";
			SQLQuery asetsQuery = session.createSQLQuery(assetsCountQuery + partnerAssetsQuery);
			asetsQuery.setInteger(COMPANY_ID, companyId);
			map.put(ALL_COUNT, asetsQuery.uniqueResult());

			asetsQuery = session.createSQLQuery(assetsPublishedCountQuery);
			asetsQuery.setInteger(COMPANY_ID, companyId);
			map.put(PUBLISHED_COUNT, asetsQuery.uniqueResult());

			asetsQuery = session.createSQLQuery(assetsUnpublishedCountQuery);
			asetsQuery.setInteger(COMPANY_ID, companyId);
			map.put(UNPUBLISHED_COUNT, asetsQuery.uniqueResult());

			asetsQuery = session.createSQLQuery(manageAssetsFolderCount);
			asetsQuery.setInteger(COMPANY_ID, companyId);
			map.put(MANAGE_FOLDER_COUNT, asetsQuery.uniqueResult());
		}

		return map;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> downloadTrackAnalytics(Pagination pagination) {
		Map<String, Object> map = new HashMap<>();
		Session session = sessionFactory.getCurrentSession();
		String queryString = "";
				
		queryString = "SELECT c.company_id AS \"id\", t.title AS \"trackName\", " +
			    "c.company_name AS \"companyName\", CONCAT(up.firstname, ' ', up.lastname) AS \"createdBy\", " +
			    "COUNT(*) AS \"count\", c.company_logo AS \"companyLogo\", " +
			    "CAST(COUNT(v.progress > 0) AS INT) AS \"viewCount\", p.status AS \"partnerStatus\",max(v.published_on) as \"publishedOn\" " +
			    "FROM xt_learning_track_visibility v, xt_learning_track t, xt_user_profile up, xt_partnership p, " +
			    "xt_user_list ul, xt_user_userlist uul, xt_user_profile u, xt_company_profile c " +
			    "WHERE v.learning_track_id = " + pagination.getLearningTrackId() + " AND v.partnership_id = p.id " +
			    "AND t.id = v.learning_track_id " +
			    "AND up.user_id = t.created_by " +
			    "AND p.vendor_company_id = ul.company_id " +
			    "AND ul.is_default_partnerlist " +
			    "AND uul.user_list_id = ul.user_list_id " +
			    "AND uul.user_id = u.user_id " +
			    "AND u.company_id = c.company_id " +
			    "AND c.company_id = p.partner_company_id ";
		
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		boolean applyPartnershipIdsFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();

		if (!teamMemberFilterDTO.isEmptyFilter() && applyPartnershipIdsFilter) {
			String partnershipIdsOrPartnerCompanyIds = teamMemberFilterDTO.getPartnershipIdsOrPartnerCompanyIds()
					.stream().map(String::valueOf).collect(Collectors.joining(","));
			queryString += "and  v.partnership_id in ( " + partnershipIdsOrPartnerCompanyIds + " )";
		} else if (teamMemberFilterDTO.isEmptyFilter()) {
			map.put(TOTAL_RECORDS, 0);
			map.put("data", new ArrayList<>());
			return map;
		}

		if (pagination.getSearchKey() != null && !pagination.getSearchKey().isEmpty() && !"-".equalsIgnoreCase(pagination.getSearchKey())) {
			queryString += "and LOWER(c.company_name) like LOWER('%" + pagination.getSearchKey() + "%') ";
		}
		queryString += "GROUP BY (c.company_name, c.company_id, t.title, CONCAT(up.firstname, ' ', up.lastname)), p.status ";
		if (XamplifyUtils.isValidString(pagination.getSortcolumn()) && XamplifyUtils.isValidString(pagination.getSortingOrder())) {
			String sortColumn = pagination.getSortcolumn();
			String sortOrder = pagination.getSortingOrder();
			String nullsHandling = "desc".equalsIgnoreCase(sortOrder) ? "NULLS LAST" : "NULLS FIRST";
			if (COMPANY_NAME.equalsIgnoreCase(sortColumn)) {
				queryString += "order by \"companyName\" " + sortOrder + " " + nullsHandling;
			} else if (COUNT.equalsIgnoreCase(sortColumn)) {
				queryString += "order by \"viewCount\" " + sortOrder + " " + nullsHandling;
			}
		}
		Query query = session.createSQLQuery(queryString);

		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		List<CompanyDTO> companyDTOList = (List<CompanyDTO>) paginationUtil.getListDTO(CompanyDTO.class, query);
		map.put(TOTAL_RECORDS, totalRecords);
		map.put("data", companyDTOList);
		return map;
	}


	@Override
	public Map<String, Object> downloadDetailedTrackAnalytics(Pagination pagination) {
		Map<String, Object> map = new HashMap<>();
		Session session = sessionFactory.getCurrentSession();
		String queryString = "";
		
		String lmsType = pagination.getLmsType().getType();
		queryString = "SELECT xlt.title AS \"trackName\", xcp.company_id AS \"id\", " +
			    	    "CONCAT(xup.firstname, xup.lastname) AS \"createdBy\", xcp.company_name AS \"companyName\", " +
			    	    "xup1.email_id AS \"emailId\", CONCAT(xup1.firstname, xup1.lastname) AS \"partnerName\", " +
			    	    "MAX(xltv.published_on) AS \"publishedOn\", MAX(xltv.progress) AS \"progress\", " +
			    	    "MAX(sd.form_name) AS \"formName\", MAX(sd.submitted_on) AS \"submittedOn\", " +
			    	    "CASE WHEN MAX(sd.score) IS NOT NULL THEN MAX(sd.score) " +
			    	    "WHEN MAX(qz.quiz_exists) = 1 AND MAX(sd.score) IS NULL THEN 'Quiz Not Yet Finished' " +
			    	    "ELSE 'Quiz Not Available' END AS \"score\" " +
			    	    "FROM xt_learning_track xlt " +
			    	    "JOIN xt_learning_track_visibility xltv ON xltv.learning_track_id = xlt.id " +
			    	    "JOIN xt_user_profile xup ON xup.user_id = xlt.created_by " +
			    	    "LEFT JOIN xt_partnership p ON p.id = xltv.partnership_id " +
			    	    "LEFT JOIN xt_user_profile xup1 ON xup1.user_id = xltv.user_id " +
			    	    "JOIN xt_company_profile xcp ON xcp.company_id = xup1.company_id " +
			    	    "LEFT JOIN (SELECT learning_track_id, 1 AS quiz_exists FROM xt_learning_track_content " +
			    	    "WHERE quiz_id IS NOT NULL GROUP BY learning_track_id) qz ON qz.learning_track_id = xlt.id " +
			    	    "LEFT JOIN (SELECT xfs.user_id, xfs.learning_track_id, xf.form_name, xfs.submitted_on, " +
			    	    "CASE WHEN xfs.score IS NOT NULL THEN xfs.score || ' out of ' || xf.max_score END AS score " +
			    	    "FROM xt_form_submit xfs JOIN xt_form xf ON xf.id = xfs.form_id) sd " +
			    	    "ON sd.user_id = xup1.user_id AND sd.learning_track_id = xlt.id " +
			    	    "WHERE xlt.id = " + pagination.getLearningTrackId() + " AND xup1.company_id IN (:companyIdsList) " +
			    	    "AND xlt.type = '" + lmsType + "' AND xlt.is_published = TRUE " ;
			    	 				
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		boolean applyPartnershipIdsFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
		if (!teamMemberFilterDTO.isEmptyFilter() && applyPartnershipIdsFilter) {
			String partnershipIdsOrPartnerCompanyIds = teamMemberFilterDTO.getPartnershipIdsOrPartnerCompanyIds()
					.stream().map(String::valueOf).collect(Collectors.joining(","));
			queryString += "and  xltv.partnership_id in ( " + partnershipIdsOrPartnerCompanyIds + " )";
		} else if (teamMemberFilterDTO.isEmptyFilter()) {
			map.put(TOTAL_RECORDS, 0);
			map.put("data", new ArrayList<>());
			return map;
		}
		if (pagination.getSearchKey() != null && !pagination.getSearchKey().isEmpty() && !"-".equalsIgnoreCase(pagination.getSearchKey())) {
			queryString += "and LOWER(xcp.company_name) like LOWER('%" + pagination.getSearchKey() + "%') ";
		}
		queryString += " GROUP BY xlt.title, xcp.company_id, CONCAT(xup.firstname, xup.lastname), xcp.company_name, xup1.email_id, CONCAT(xup1.firstname, xup1.lastname)  ";
		if (XamplifyUtils.isValidString(pagination.getSortcolumn()) && XamplifyUtils.isValidString(pagination.getSortingOrder())) {
			if (COMPANY_NAME.equalsIgnoreCase(pagination.getSortcolumn())) {
				queryString += "order by \"companyName\" " + pagination.getSortingOrder();
			} else if (COUNT.equalsIgnoreCase(pagination.getSortcolumn())) {
				String nullsHandling = "desc".equalsIgnoreCase(pagination.getSortingOrder()) ? "NULLS LAST" : "NULLS FIRST";
			    queryString += " ORDER BY \"progress\" " + pagination.getSortingOrder() + " " + nullsHandling;
			}
		}
		Query query = session.createSQLQuery(queryString);
		if (XamplifyUtils.isNotEmptyList(pagination.getCompanyIds())) {
			query.setParameterList("companyIdsList", pagination.getCompanyIds());
		}
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		@SuppressWarnings("unchecked")
		List<CompanyDTO> companyDTOList = (List<CompanyDTO>) paginationUtil.getListDTO(CompanyDTO.class, query);
		map.put(TOTAL_RECORDS, totalRecords);
		map.put("data", companyDTOList);
		return map;
	}
  
  
	@Override
	public Map<String, Object> getManageSharedContentCounts(Integer companyId, Integer loggedInUserId,
			String moduleContentType, Integer vendorCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		Map<String, Object> map = new HashMap<>();
		
	    String manageSharedTrackAllCountSQL = manageSharedTrackAllCount;
        String manageSharedTrackInprogressCountSQL = manageSharedTrackInprogressCount;
        String manageSharedTrackCompletedTracksCountSQL = manageSharedTrackCompletedTracksCount;
        String manageSharedTrackNotViewedTracksCountSQL = manageSharedTrackNotViewedTracksCount;
        String manageSharedFolderTracksCountSQL = manageSharedFolderTracksCount;
        String manageSharedPlaybookAllCountSQL = manageSharedPlaybookAllCount;
        String manageSharedPlaybookInprogressCountSQL = manageSharedPlaybookInprogressCount;
        String managesharedPlaybookCompletedPlaybookCountSQL = managesharedPlaybookCompletedPlaybookCount;
        String manageSharedPlaybookNotViewedPlaybookCountSQL = manageSharedPlaybookNotViewedPlaybookCount;
        String manageSharedFolderPlaybookCountSQL = manageSharedFolderPlaybookCount;
        String manageSharedAssetAllCountSQL = manageSharedAssetAllCount;
        String manageSharedAssetInteractedCountSQL = manageSharedAssetInteractedCount;
        String manageSharedAssetNonInteractedCountSQL = manageSharedAssetNonInteractedCount;
        String manageSharedAssetFolderCountSQL = manageSharedAssetFolderCount;


        
        if (XamplifyUtils.isValidInteger(vendorCompanyId)) {
			manageSharedTrackAllCountSQL += AND_XLT_COMPANY_ID+ vendorCompanyId + " ";
			manageSharedTrackInprogressCountSQL += AND_XLT_COMPANY_ID+ vendorCompanyId + " ";
			manageSharedTrackCompletedTracksCountSQL += AND_XLT_COMPANY_ID+ vendorCompanyId + " ";		
			manageSharedTrackNotViewedTracksCountSQL += AND_XLT_COMPANY_ID+ vendorCompanyId + " ";
			manageSharedFolderTracksCountSQL += AND_XLT_COMPANY_ID+ vendorCompanyId + " ";
			manageSharedPlaybookAllCountSQL += AND_XLT_COMPANY_ID+ vendorCompanyId + " ";
			manageSharedPlaybookInprogressCountSQL += AND_XLT_COMPANY_ID+ vendorCompanyId + " ";
			managesharedPlaybookCompletedPlaybookCountSQL += AND_XLT_COMPANY_ID+ vendorCompanyId + " ";
			manageSharedPlaybookNotViewedPlaybookCountSQL += AND_XLT_COMPANY_ID+ vendorCompanyId + " ";
			manageSharedFolderPlaybookCountSQL += AND_XLT_COMPANY_ID+ vendorCompanyId + " ";
			manageSharedAssetAllCountSQL += AND_XD_COMPANY_ID+ vendorCompanyId + " ";
			manageSharedAssetInteractedCountSQL += AND_XD_COMPANY_ID+ vendorCompanyId + " ";
			manageSharedAssetNonInteractedCountSQL += AND_XD_COMPANY_ID+ vendorCompanyId + " ";
			manageSharedAssetFolderCountSQL += AND_XD_COMPANY_ID+ vendorCompanyId + " ";
		}

		if (moduleContentType.equals("TRACK")) {
			SQLQuery tracksQuery = session.createSQLQuery(manageSharedTrackAllCountSQL);
			tracksQuery.setInteger(LOGGEDIN_USER_ID, loggedInUserId);
			map.put(ALL_SHARED_COUNT, tracksQuery.uniqueResult());

			tracksQuery = session.createSQLQuery(manageSharedTrackInprogressCountSQL);
			tracksQuery.setInteger(LOGGEDIN_USER_ID, loggedInUserId);
			map.put(IN_PROGRESS_COUNT, tracksQuery.uniqueResult());

			tracksQuery = session.createSQLQuery(manageSharedTrackCompletedTracksCountSQL);
			tracksQuery.setInteger(LOGGEDIN_USER_ID, loggedInUserId);
			map.put(COMPLETED_COUNT, tracksQuery.uniqueResult());

			tracksQuery = session.createSQLQuery(manageSharedTrackNotViewedTracksCountSQL);
			tracksQuery.setInteger(LOGGEDIN_USER_ID, loggedInUserId);
			map.put(NOT_VIEWED_COUNT, tracksQuery.uniqueResult());
			
			tracksQuery = session.createSQLQuery(manageSharedFolderTracksCountSQL);
			tracksQuery.setInteger(LOGGEDIN_USER_ID, loggedInUserId);
			map.put(MANAGE_SHARED_FOLDERS_COUNT, tracksQuery.uniqueResult());
		}

		if (moduleContentType.equals("PLAYBOOK")) {
			SQLQuery playbookQuery = session.createSQLQuery(manageSharedPlaybookAllCountSQL);
			playbookQuery.setInteger(LOGGEDIN_USER_ID, loggedInUserId);
			map.put(ALL_SHARED_COUNT, playbookQuery.uniqueResult());

			playbookQuery = session.createSQLQuery(manageSharedPlaybookInprogressCountSQL);
			playbookQuery.setInteger(LOGGEDIN_USER_ID, loggedInUserId);
			map.put(IN_PROGRESS_COUNT, playbookQuery.uniqueResult());
			
			playbookQuery = session.createSQLQuery(managesharedPlaybookCompletedPlaybookCountSQL);
			playbookQuery.setInteger(LOGGEDIN_USER_ID, loggedInUserId);
			map.put(COMPLETED_COUNT, playbookQuery.uniqueResult());


			playbookQuery = session.createSQLQuery(manageSharedPlaybookNotViewedPlaybookCountSQL);
			playbookQuery.setInteger(LOGGEDIN_USER_ID, loggedInUserId);
			map.put(NOT_VIEWED_COUNT, playbookQuery.uniqueResult());

			playbookQuery = session.createSQLQuery(manageSharedFolderPlaybookCountSQL);
			playbookQuery.setInteger(LOGGEDIN_USER_ID, loggedInUserId);
			map.put(MANAGE_SHARED_FOLDERS_COUNT, playbookQuery.uniqueResult());

		}
		
		if (moduleContentType.equals("DAM")) {
			SQLQuery asetsQuery = session.createSQLQuery(manageSharedAssetAllCountSQL);
			asetsQuery.setInteger(COMPANY_ID, companyId);
			asetsQuery.setInteger(LOGGEDIN_USER_ID, loggedInUserId);
			map.put(ALL_SHARED_COUNT, asetsQuery.uniqueResult());

			asetsQuery = session.createSQLQuery(manageSharedAssetInteractedCountSQL);
			asetsQuery.setInteger(COMPANY_ID, companyId);
			asetsQuery.setInteger(LOGGEDIN_USER_ID, loggedInUserId);
			map.put(INTERACTED, asetsQuery.uniqueResult());

			asetsQuery = session.createSQLQuery(manageSharedAssetNonInteractedCountSQL);
			asetsQuery.setInteger(COMPANY_ID, companyId);
			asetsQuery.setInteger(LOGGEDIN_USER_ID, loggedInUserId);
			map.put(NON_INTERACTED, asetsQuery.uniqueResult());

			asetsQuery = session.createSQLQuery(manageSharedAssetFolderCountSQL);
			asetsQuery.setInteger(COMPANY_ID, companyId);
			map.put(MANAGE_SHARED_FOLDERS_COUNT, asetsQuery.uniqueResult());
		}
		
		return map;
	}
	
}
