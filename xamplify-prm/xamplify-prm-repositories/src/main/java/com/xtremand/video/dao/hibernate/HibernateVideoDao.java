package com.xtremand.video.dao.hibernate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.common.bom.Pagination.SORTINGORDER;
import com.xtremand.dam.bom.ApprovalStatusType;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.VideoDTO;
import com.xtremand.video.bom.VideoCategory;
import com.xtremand.video.bom.VideoDefaultSettings;
import com.xtremand.video.bom.VideoDetails;
import com.xtremand.video.bom.VideoFile;
import com.xtremand.video.bom.VideoFile.TYPE;
import com.xtremand.video.dao.VideoDao;
import com.xtremand.video.exception.VideoDataAccessException;

@Repository("videoDao")
@Transactional
public class HibernateVideoDao implements VideoDao {

	private static final Logger logger = LoggerFactory.getLogger(HibernateVideoDao.class);

	@Resource
	SessionFactory sessionFactory;

	@Value("${server_path}")
	String server_path;

	@Autowired
	private PaginationUtil paginationUtil;

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	private static final String MAXIMUM_UPLOADS_REACHED = "You can not upload videos maximum uploads reached";
	private static final String MAXIMUM_PUBLIC_UPLOADS_REACHED = "You can not upload Public videos, please try with Private";
	private static final String MAXIMUM_PRIVATE_UPLOADS_REACHED = "You can not upload Private videos please try with Public";
	private static final String MAXIMUM_UNLISTED_UPLOADS_REACHED = "You can not upload Protected videos";
	private static final String MAXIMUM_DISK_SPACE_REACHED = "Maximum Disk Space Reached for you subscription";
	private static final String MAXIMUM_LENGTH_REACHED = "Maximum Length of Video Reached for you subscription";

	@Override
	public VideoFile findByPrimaryKey(Serializable pk, FindLevel[] levels) {
		logger.debug("VideoDao findByPrimaryKey " + pk);
		VideoFile videoFile = (VideoFile) sessionFactory.getCurrentSession().get(VideoFile.class, pk);
		if (videoFile != null) {
			loadAssociations(videoFile, levels);
		}
		return videoFile;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<VideoFile> find(List<Criteria> criterias, FindLevel[] levels) {
		logger.debug("HibernateVideoDao find ");

		Session session = sessionFactory.getCurrentSession();

		org.hibernate.Criteria criteria = session.createCriteria(VideoFile.class);
		List<Criterion> criterions = generateCriteria(criterias);

		for (Criterion criterion : criterions) {
			criteria.add(criterion);
		}
		List<VideoFile> videoFiles = criteria.list();

		// Initializing Associations for one Object. Since fetch is set as SUBSELECT,
		//
		if (!videoFiles.isEmpty()) {
			for (VideoFile videoFile : videoFiles) {
				loadAssociations(videoFile, levels);
			}
		}
		return videoFiles;
	}

	private void loadAssociations(VideoFile videoFile, FindLevel[] levels) {
		for (FindLevel level : levels) {
			switch (level) {
			case VIDEO_TAGS:
				Hibernate.initialize(videoFile.getVideoTags());
				break;

			case VIDEO_IMAGE:
				Hibernate.initialize(videoFile.getVideoImage());
				break;

			case VIDEO_CONTROL:
				Hibernate.initialize(videoFile.getVideoControl());
				break;

			case VIDEO_PATH:
				videoFile.setFullVideoPath(server_path + videoFile.getUri());
				videoFile.setFullImagePath(videoFile.getImageUri() != null && videoFile.getImageUri().length() > 0
						? server_path + videoFile.getImageUri()
						: "");
				videoFile.setFullGifPath(server_path + videoFile.getGifUri());
				break;
			case INCREMENT_VIEW:
				videoFile.setViews(videoFile.getViews().intValue() + 1);
				break;

			case CALL_ACTION:
				Hibernate.initialize(videoFile.getCallAction());
				break;
			default:
				break;
			}
		}
	}

	public List<String> getViewByOptions(Integer userId) {
		List<String> values = new ArrayList<String>();
		Session session = sessionFactory.getCurrentSession();
		Integer totalPublicVideosCount = ((BigInteger) session
				.createSQLQuery(
						"select count(1) from xt_video_files where customer_id = " + userId + " and view_by='PUBLIC'")
				.list().get(0)).intValue();
		Integer totalPrivateVideosCount = ((BigInteger) session
				.createSQLQuery(
						"select count(1) from xt_video_files where customer_id = " + userId + " and view_by='PRIVATE'")
				.list().get(0)).intValue();
		Integer totalProtectedVideosCount = ((BigInteger) session
				.createSQLQuery(
						"select count(1) from xt_video_files where customer_id = " + userId + " and view_by='Unlisted'")
				.list().get(0)).intValue();

		logger.debug("totalPublicVideosCount # " + totalPublicVideosCount + " totalPrivateVideosCount # "
				+ totalPrivateVideosCount + "totalProtectedVideosCount # " + totalProtectedVideosCount);

		/*
		 * if(totalProtectedVideosCount < subscription.getNoOfProtectedVideos()){
		 * values.add(VideoFile.TYPE.PROTECTED.name()); }
		 */

		return values;
	}

	@SuppressWarnings("unchecked")
	public Integer getVideoUsedLength(List<Integer> subAdminUserIds, Integer userId) {
		subAdminUserIds.add(userId);
		String subAdminUserIdsWithCustomerIds = StringUtils.join(subAdminUserIds, ",");

		List<String> values = sessionFactory.getCurrentSession().createSQLQuery(
				"select video_length from xt_video_files where customer_id in (" + subAdminUserIdsWithCustomerIds + ")")
				.list();
		int hours = 0, minutes = 0, seconds = 0;

		for (String v : values) {
			String[] sp = v.split(":");
			seconds = seconds + Integer.parseInt(sp[2]);
			if (seconds > 60) {
				minutes++;
				seconds = seconds - 60;
			}
			minutes = minutes + Integer.parseInt(sp[1]);
			if (minutes > 60) {
				hours++;
				minutes = minutes - 60;
			}
			hours = hours + Integer.parseInt(sp[0]);
		}
		minutes = minutes + hours * 60;
		logger.debug("getVideoUsedLength ## " + minutes);
		return minutes;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> find(List<Criteria> criterias, FindLevel[] levels, Pagination pagination) {
		logger.debug("HibernateVideoDao find ");

		Session session = sessionFactory.getCurrentSession();

		org.hibernate.Criteria criteria = session.createCriteria(VideoFile.class);
		List<Criterion> criterions = generateCriteria(criterias);

		for (Criterion criterion : criterions) {
			criteria.add(criterion);
		}

		criteria.createAlias("customer.companyProfile", "children");
		if (pagination != null && pagination.getSearchKey() != null) {
			Criterion name1 = Restrictions.ilike("title", pagination.getSearchKey(), MatchMode.ANYWHERE);
			Criterion description1 = Restrictions.ilike("description", pagination.getSearchKey(), MatchMode.ANYWHERE);
			// criteria.createAlias("customer.companyProfile", "children");
			Criterion vendorCompany1 = Restrictions.ilike("children.companyName", pagination.getSearchKey(),
					MatchMode.ANYWHERE);
			criteria.add(Restrictions.or(name1, description1, vendorCompany1));
		}

		if (pagination != null && pagination.getFilterBy() != null) {
			criteria.add(Restrictions.eq("viewBy", VideoFile.TYPE.valueOf(pagination.getFilterBy().toUpperCase())));
		}

		if (pagination != null && pagination.getShowDraftContent() != null
				&& pagination.getShowDraftContent() == false) {
			criteria.add(Restrictions.ne("viewBy", VideoFile.TYPE.valueOf(TYPE.DRAFT.name())));
		}

		ScrollableResults scrollableResults = criteria.scroll();
		scrollableResults.last();

		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		logger.info("totalRecords : " + totalRecords);
		scrollableResults.close();

		Optional<Pagination> paginationObj = Optional.ofNullable(pagination);

		if (paginationObj.isPresent()) {
			Optional<Integer> maxResultsObj = Optional.ofNullable(paginationObj.get().getMaxResults());
			Optional<Integer> pageIndexObj = Optional.ofNullable(paginationObj.get().getPageIndex());
			Optional<String> sortcolumnObj = Optional.ofNullable(paginationObj.get().getSortcolumn());
			Optional<String> searchKeyObj = Optional.ofNullable(paginationObj.get().getSearchKey());

			if (maxResultsObj.isPresent() && pageIndexObj.isPresent()) {
				criteria.setFirstResult((pageIndexObj.get() * maxResultsObj.get()) - maxResultsObj.get());
				criteria.setMaxResults(maxResultsObj.get());
			}

			if (sortcolumnObj.isPresent()) {
				if (sortcolumnObj.get().equalsIgnoreCase("companyName")) {
					// criteria.createAlias("customer.companyProfile", "childrenObj");
					if (SORTINGORDER.ASC == SORTINGORDER.valueOf(paginationObj.get().getSortingOrder())) {
						criteria.addOrder(Order.asc("children.companyName"));
					} else if (SORTINGORDER.DESC == SORTINGORDER.valueOf(paginationObj.get().getSortingOrder())) {
						criteria.addOrder(Order.desc("children.companyName"));

					}

				} else {
					if (SORTINGORDER.ASC == SORTINGORDER.valueOf(paginationObj.get().getSortingOrder())) {
						criteria.addOrder(Order.asc(sortcolumnObj.get()));
					} else if (SORTINGORDER.DESC == SORTINGORDER.valueOf(paginationObj.get().getSortingOrder())) {
						criteria.addOrder(Order.desc(sortcolumnObj.get()));

					}
				}
			} else {
				criteria.addOrder(Order.desc("createdTime"));
			}
		}

		List<VideoFile> videoFiles = criteria.list();

		// Initializing Associations for one Object. Since fetch is set as SUBSELECT,
		//
		if (!videoFiles.isEmpty()) {
			for (VideoFile videoFile : videoFiles) {
				loadAssociations(videoFile, levels);
			}
		}
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("totalRecords", totalRecords);
		resultMap.put("videoFiles", videoFiles);
		return resultMap;
	}

	@Override
	public void deleteByPrimaryKey(Integer id) {
		logger.debug("delete video called " + id);
		Session session = sessionFactory.getCurrentSession();
		session.createSQLQuery("delete from xt_video_leads where video_id =  " + id).executeUpdate();
		session.createSQLQuery("delete from xt_video_tags where video_id =  " + id).executeUpdate();
		session.createSQLQuery("delete from xt_video_control where id =  " + id).executeUpdate();
		session.createSQLQuery("delete from xt_video_image where id =  " + id).executeUpdate();
		session.createSQLQuery("delete from xt_call_action where id =  " + id).executeUpdate();
		session.createSQLQuery("delete from xt_video_files where id =  " + id).executeUpdate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getVideoTitles(Integer[] userIdArray) {
		List<String> titles = new ArrayList<String>();
		logger.debug("entered in HibernateVideoDao getVideoTitles() mehtod");
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(VideoFile.class);
		criteria.setProjection(Projections.property("title"));
		criteria.add(Restrictions.in("customer.userId", userIdArray));
		criteria.add(Restrictions.ne("viewBy", TYPE.DRAFT));
		List<String> videoTitles = criteria.list();

		for (String title : videoTitles) {
			titles.add(title.toLowerCase());
		}
		return titles;
	}

	@Override
	public VideoDefaultSettings getVideoDefaultSettings(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(VideoDefaultSettings.class);
		criteria.add(Restrictions.eq("companyProfile.id", companyId));
		VideoDefaultSettings videoDefaultSettings = (VideoDefaultSettings) criteria.uniqueResult();
		return videoDefaultSettings;
	}

	@Override
	public Integer getVideosCount(Integer[] userIdArray) {
		logger.debug("entered in HibernateVideoDao getVideosCount() mehtod");
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(VideoFile.class);
		criteria.add(Restrictions.in("customer.userId", userIdArray));
		criteria.setProjection(Projections.rowCount());
		Integer count = criteria.uniqueResult() != null ? ((Long) criteria.uniqueResult()).intValue() : 0;
		return count;
	}

	@Override
	public Integer getVideosViewsCount(Integer[] userIdArray) {
		logger.debug("entered in HibernateVideoDao getVideosViewsCount() mehtod");
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(VideoFile.class);
		criteria.add(Restrictions.in("customer.userId", userIdArray));
		criteria.setProjection(Projections.sum("views"));
		Integer count = criteria.uniqueResult() != null ? ((Long) criteria.uniqueResult()).intValue() : 0;
		return count;
	}

	@Override
	public Integer getChannelVideosViewsCount(Integer[] videoIdArray) {
		logger.debug("entered in HibernateVideoDao getVideosViewsCount() mehtod");
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(VideoFile.class);
		criteria.add(Restrictions.in("id", videoIdArray));
		criteria.setProjection(Projections.sum("views"));
		Integer count = criteria.uniqueResult() != null ? ((Long) criteria.uniqueResult()).intValue() : 0;
		return count;
	}

	@Override
	public Integer monthWiseVideoViewsCount(Integer videoId, String startDateOfMonth, String endDateOfMonth) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select sum(rows) as total_rows  from (SELECT (coalesce(COUNT(DISTINCT session_id),0)+ SUM(CASE WHEN action_id = 10 THEN 1 ELSE 0 END)) as rows  FROM  xt_xtremand_log where video_id="
				+ videoId + " and start_time>='" + startDateOfMonth + "' and end_time<='" + endDateOfMonth + "') as u ";
		SQLQuery query = session.createSQLQuery(sql);
		Integer count = query.uniqueResult() != null ? ((BigDecimal) query.uniqueResult()).intValue() : 0;
		return count;
	}

	@Override
	public List countryWiseVideoViewsCount(Integer videoId) throws VideoDataAccessException {
		Session session = sessionFactory.getCurrentSession();
		String sql = " SELECT country_code, coalesce(COUNT(DISTINCT session_id),0) + (SUM(CASE WHEN action_id = 10 THEN 1 ELSE 0 END)) AS count FROM xt_xtremand_log WHERE video_id="
				+ videoId + " and country_code IS NOT NULL GROUP BY country_code";
		SQLQuery query = session.createSQLQuery(sql);
		return query.list();
	}

	@Override
	public Integer watchedFullyVideoViewsCount(Integer videoId) throws VideoDataAccessException {
		Session session = sessionFactory.getCurrentSession();
		String sql = " (select coalesce(count(*),0) from xt_xtremand_log where video_id =" + videoId
				+ " and action_id=9) ";
		SQLQuery query = session.createSQLQuery(sql);
		Integer count = query.uniqueResult() != null ? ((java.math.BigInteger) query.uniqueResult()).intValue() : 0;
		return count;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getChannelVideos(Integer userId, List<Integer> companyIds, boolean isPartner,
			Integer loggedInUsercompanyId) throws VideoDataAccessException {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select distinct v.video_id  from  xt_campaign_videos v,xt_campaign  c,xt_campaign_user_userlist u where v.campaign_id=c.campaign_id "
				+ " and u.campaign_id=c.campaign_id and  c.is_launched=true and c.is_channel_campaign=true and u.user_id in (select user_id from xt_user_profile where company_id ="
				+ loggedInUsercompanyId + ")";

		String modifiedSql = sql
				+ "  and c.customer_id in (select user_id from xt_user_profile where company_id in (:companyIds)) ";
		SQLQuery query = session.createSQLQuery(isPartner ? modifiedSql : sql);
		if (isPartner) {
			query.setParameterList("companyIds", companyIds);
		}
		List<Integer> videoIdsList = query.list();
		return videoIdsList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> listPublicVideosByCompany(Integer[] userIdArray) {
		logger.debug("entered in listPublicVideosByCompany() mehtod");
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(VideoFile.class);
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("id"));
		proList.add(Projections.property("title"));
		criteria.add(Restrictions.in("customer.userId", userIdArray));
		criteria.add(Restrictions.eq("viewBy", TYPE.PUBLIC));
		criteria.setProjection(proList);
		return criteria.list();
	}

	@Override
	public Map<String, Object> listVideos(List<Criteria> criterias, Pagination pagination) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(VideoDetails.class);
		List<Criterion> criterions = generateCriteria(criterias);
		Map<String, Object> map = addColumnNamesAndGetList(pagination, criteria, criterions);
		return map;
	}

	private Map<String, Object> addColumnNamesAndGetList(Pagination pagination, org.hibernate.Criteria criteria,
			List<Criterion> criterions) {
		List<String> columnNames = new ArrayList<>();
		columnNames.add("title");
		return paginationUtil.addSearchAndPaginationAndSort(pagination, criteria, criterions, columnNames, "id",
				"desc");
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<VideoCategory> listVideoCategories(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = " select distinct categories_id as \"id\", category_name as \"name\" from v_manage_videos where company_id= :companyId ";
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameter("companyId", companyId);
		query.setResultTransformer(Transformers.aliasToBean(VideoCategory.class));
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<VideoCategory> listVideoCategories(List<Integer> videoIds) {
		Session session = sessionFactory.getCurrentSession();
		String sql = " select distinct categories_id as \"id\", category_name as \"name\" from v_manage_videos where video_id in :videoIds ";
		SQLQuery query = session.createSQLQuery(sql);
		query.setParameterList("videoIds", videoIds);
		query.setResultTransformer(Transformers.aliasToBean(VideoCategory.class));
		return query.list();
	}

	@Override
	public String getVideoPath(Integer videoId) {
		Session session = sessionFactory.getCurrentSession();
		String hql = "select uri from VideoFile v where v.id = :videoId";
		Query query = session.createQuery(hql);
		query.setParameter("videoId", videoId);
		return (String) query.uniqueResult();
	}

	@Override
	public Integer getVideoTitleCount(String title, Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select cast(count(*) as integer) from xt_video_files where lower(title) ='" + title.toLowerCase()
				+ "' and " + " customer_id in (select distinct user_id from xt_user_profile where company_id="
				+ companyId + ") and view_by!='DRAFT'";
		SQLQuery query = session.createSQLQuery(sql);
		Integer count = query.uniqueResult() != null ? (Integer) query.uniqueResult() : 0;
		return count;
	}

	@Override
	public VideoFile findByAlias(String alias) {
		return (VideoFile) sessionFactory.getCurrentSession().createCriteria(VideoFile.class)
				.add(Restrictions.eq("alias", alias)).uniqueResult();
	}

	@Override
	public Map<String, Object> findVideos(Pagination pagination) {
		Map<String, Object> map = new HashMap<String, Object>();
		String searchKey = pagination.getSearchKey();
		boolean hasSearchKey = searchKey != null && org.springframework.util.StringUtils.hasText(searchKey);
		StringBuilder findVideosQueryStringBuilder = new StringBuilder();
		String sortQueryString = getSortOptionQueryString(pagination);
		Integer categoryId = pagination.getCategoryId();
		boolean isValidCategoryId = categoryId != null && categoryId > 0;
		StringBuilder queryStringBuilder = new StringBuilder();

		String queryString = "select xv.video_id AS \"id\", xv.company_id AS \"companyId\", xv.video_alias AS \"alias\", xv.title AS \"title\", "
				+ "xv.customer_id AS \"uploadedUserId\", xv.categories_id AS \"categoryId\", xv.category_name AS \"categoryName\", "
				+ "xv.uploaded_time AS \"createdTime\", xv.video_length AS \"videoLength\", xv.view_by AS \"viewBy\", xv.views AS \"views\", "
				+ "xv.image_uri AS \"imagePath\", xv.videouri AS \"videoPath\", xv.is_processed AS \"isProcessed\", "
				+ "xv.full_name AS \"uploadedUserName\", xv.company_profile_name AS \"companyName\", xv.gifimagepath AS \"gifImagePath\", "
				+ "xv.description AS \"description\" " + "FROM v_manage_videos xv {fetchApprovedVideoAssets} "
				+ "WHERE xv.company_id = :companyId AND xv.is_processed AND xv.view_by != :viewBy {fetchApprovedVideoAssetsCondition}";

		if (pagination.getSelectedApprovalStatusCategory().equals(ApprovalStatusType.APPROVED.name())) {
			queryString = queryString.replace("{fetchApprovedVideoAssets}",
					"INNER JOIN xt_dam xd ON xv.video_id = xd.video_id");
			queryString = queryString.replace("{fetchApprovedVideoAssetsCondition}",
					"AND xd.approval_status = 'APPROVED'");
		} else {
			queryString = queryString.replace("{fetchApprovedVideoAssets}", "");
			queryString = queryString.replace("{fetchApprovedVideoAssetsCondition}", "");
		}

		queryString += " and xv.created_for_company is null ";

		queryStringBuilder.append(queryString);
		if (isValidCategoryId) {
			queryStringBuilder.append(" and xv.categories_id = :categoryId");
		}
		String searchQueryString = " and (LOWER(xv.title) like LOWER('%searchKey%') OR LOWER(xv.category_name) like LOWER('%searchKey%') OR LOWER(xv.description) like LOWER('%searchKey%')"
				+ " OR LOWER(xv.view_by) like LOWER('%searchKey%'))";
		findVideosQueryStringBuilder.append(queryStringBuilder);
		if (hasSearchKey) {
			findVideosQueryStringBuilder.append(searchQueryString.replace("searchKey", searchKey))
					.append(sortQueryString);
		} else {
			findVideosQueryStringBuilder.append(sortQueryString);
		}
		Session session = sessionFactory.getCurrentSession();
		SQLQuery query = session.createSQLQuery(String.valueOf(findVideosQueryStringBuilder));
		query.setParameter("companyId", pagination.getCompanyId());
		query.setParameter("viewBy", "DRAFT");
		if (isValidCategoryId) {
			query.setParameter("categoryId", categoryId);
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, VideoDTO.class);

	}

	private String getSortOptionQueryString(Pagination pagination) {
		String sortOder = pagination.getSortingOrder();
		String sortColumn = pagination.getSortcolumn();

		boolean isValidSortOrder = sortOder != null && org.springframework.util.StringUtils.hasText(sortOder);
		boolean isValidSortColumn = sortColumn != null && org.springframework.util.StringUtils.hasText(sortColumn);
		if (!isValidSortOrder) {
			pagination.setSortingOrder(" desc");
		}
		if (isValidSortColumn) {
			if ("title".equalsIgnoreCase(sortColumn)) {
				pagination.setSortcolumn("xv.title");
			} else if ("uploadedDate".equalsIgnoreCase(sortColumn)) {
				pagination.setSortcolumn("xv.uploaded_time");
			} else if ("selectedVideo".equals(pagination.getSortcolumn())) {
				Integer selectedVideoId = pagination.getSelectedVideoId();
				if (selectedVideoId != null && selectedVideoId > 0) {
					pagination.setSortcolumn("array_position(array[" + selectedVideoId + "], xv.video_id)");
					pagination.setSortingOrder(",xv.uploaded_time desc");
				} else {
					pagination.setSortcolumn("1");
				}
			}
		} else {
			pagination.setSortcolumn("xv.uploaded_time");
		}

		return " order by " + pagination.getSortcolumn() + " " + pagination.getSortingOrder();

	}

	/** XNFR-885 **/
	@SuppressWarnings("unchecked")
	@Override
	public List<String> getTagNamesByVideoId(Integer videoId) {
		if (!XamplifyUtils.isValidInteger(videoId)) {
			return Collections.emptyList();
		}
		String queryString = "select video_tags from xt_video_tags where video_id = :videoId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("videoId", videoId));
		return (List<String>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

	/** XNFR-885 **/
	@Override
	public void deleteVideoRecordsByIds(List<Integer> ids) {
		if (XamplifyUtils.isNotEmptyList(ids)) {
			Map<String, String> tablePrimaryKeyMap = new LinkedHashMap<>();
			tablePrimaryKeyMap.put("xt_video_leads", "video_id");
			tablePrimaryKeyMap.put("xt_video_tags", "video_id");
			tablePrimaryKeyMap.put("xt_video_control", "id");
			tablePrimaryKeyMap.put("xt_video_image", "id");
			tablePrimaryKeyMap.put("xt_call_action", "id");
			tablePrimaryKeyMap.put("xt_video_files", "id");
			Session session = sessionFactory.getCurrentSession();
			try {
				for (Map.Entry<String, String> entry : tablePrimaryKeyMap.entrySet()) {
					String tableName = entry.getKey();
					String primaryKey = entry.getValue();
					Integer deletedRows = session
							.createSQLQuery("delete from " + tableName + " where " + primaryKey + " in (:ids)")
							.setParameterList("ids", ids).executeUpdate();
					logger.debug("Deleted rows: {} from table: {} time stamp: {}", deletedRows, tableName, new Date());
				}
				session.flush();
				session.clear();
			} catch (Exception e) {
				logger.error("Error occurred while deleting video records", e);
			}
		}
	}

}
