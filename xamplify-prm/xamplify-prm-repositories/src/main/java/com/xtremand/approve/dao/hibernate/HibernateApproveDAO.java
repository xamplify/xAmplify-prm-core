package com.xtremand.approve.dao.hibernate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionException;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xtremand.approve.dao.ApproveDAO;
import com.xtremand.approve.dto.MultiSelectApprovalDTO;
import com.xtremand.approve.dto.PendingApprovalDamAndLmsDTO;
import com.xtremand.approve.dto.TeamMemberApprovalPrivilegesDTO;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.Criteria.OPERATION_NAME;
import com.xtremand.common.bom.Pagination;
import com.xtremand.company.dto.ApprovalSettingsDTO;
import com.xtremand.dam.bom.ApprovalStatusType;
import com.xtremand.dam.exception.DamDataAccessException;
import com.xtremand.lms.dao.LMSDAO;
import com.xtremand.user.bom.Role;
import com.xtremand.user.dao.hibernate.HibernateUserDAO;
import com.xtremand.util.FileUtil;
import com.xtremand.util.XamplifyUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.bom.ModuleType;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.ApprovalStatisticsDTO;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.LeftSideNavigationBarItem;
import com.xtremand.util.dto.PaginatedDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.QueryParameterListDTO;
import com.xtremand.util.dto.XamplifyConstants;

@Repository("approveDAO")
@Transactional
public class HibernateApproveDAO implements ApproveDAO {

	private static final Logger logger = LoggerFactory.getLogger(HibernateApproveDAO.class);

	@Autowired
	SessionFactory sessionFactory;

	@Autowired
	HibernateUserDAO userDao;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private LMSDAO lmsDao;

	@Autowired
	private FileUtil fileUtil;

	@Autowired
	private XamplifyUtil xamplifyUtil;

	@Value("${damApprovalListQuery}")
	private String damApprovalListQuery;

	@Value("${damStatusCountQuery}")
	private String damStatusCountQuery;

	@Value("${trackApprovalListQuery}")
	private String trackApprovalListQuery;

	@Value("${allStatusListQuery}")
	private String allStatusListQuery;

	@Value("${trackStatusCountQuery}")
	private String trackStatusCountQuery;

	@Value("${playBookApprovalListQuery}")
	private String playBookApprovalListQuery;

	@Value("${playBookStatusCountQuery}")
	private String playBookStatusCountQuery;

	@Value("${allApprovalListQuery}")
	private String allApprovalListQuery;

	@Value("${nameColumnOrderByQuery}")
	private String nameColumnOrderByQuery;

	@Value("${createdOnOrderByQuery}")
	private String createdOnOrderByQuery;

	@Value("${sql.query.update.approval.company.settings}")
	private String updateApprovalConfigurationSettingsForCompanySqlQuery;

	@Value("${sql.query.update.approval.team.member.settings}")
	private String updateApprovalConfigurationSettingsForAdminsAndSuperVisorsSqlQuery;

	@Value("${sql.query.list.team.member.approval.control.management}")
	private String listTeamMembersForApprovalControlManagementSqlQuery;

	@Value("${sql.search.query.list.team.member.approval.control.management}")
	private String listTeamMembersForApprovalControlManagementSqlSearchQuery;

	@Value("${dev.host}")
	String devHost;

	@Value("${prod.host}")
	String productionHost;

	@Value("${image.file.types}")
	String imageFileTypes;

	@Value("${content.preview.fileTypes}")
	String contentPreviewSupportedFileFormats;

	@Value("${content.preview.fileTypes.text}")
	String contentPreviewForTextView;

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	private static final String ASSET_TYPE = "Assets";

	private static final String TRACK_TYPE = "Tracks";

	private static final String ALL_TYPE = "All";

	private static final String SEARCH_QUERY_STRING = "{searchQueryString}";

	private static final String ALL_STATUS_STRING = "{allStatusListString}";

	private static final String APPROVE_STATUS = "Approved";

	private static final String PENDING_STATUS = "Pending";

	private static final String REJECTED_STATUS = "Rejected";

	private static final String DRAFT_STATUS = "DRAFT";

	private static final String PLAYBOOK_TYPE = "PlayBooks";

	private static final String COMPANY_ID = "companyId";

	private static final String TEAM_MEMBER_ID = "teamMemberId";

	private static final String UPDATE_APPROVAL_CONFIGURATION_SETTINGS_ERROR_STRING = "Exception occured in updating approval configuration settings. Message: {}. TimeStamp: {}";

	private static final String ALL_APPROVAL_LIST_STRING = "{allApprovalListString}";

	private static final String DESC = "DESC";

	private static final String NAME = "name";

	private static final String CREATED_TIME = "createdTime";

	private static final String ASSET_DATE_FILTER = "d.created_time";

	private static final String LMS_DATE_FILTER = "xlt.created_time";

	private static final String ENTITY_ID = "entityId";

	private static final String MODULE_TYPE = "moduleType";

	private static final String TRACK = "Track";

	private static final String PLAYBOOK = "PlayBook";

	private static final String ASSET = "Asset";

	private static final String CREATED_BY = "createdBy";

	private static final String MODULE_TYPE_QUERY_REPLACE_KEY = "${moduleTypeQueryCondition}";

	private static final String UPDATED_TIME = "updatedTime";

	private static final String CREATED_BY_IDS = "createdByIds";

	private static final String HOST = "http://localhost:8080/";

        private static final String XAMPLIFY_PRM = "xamplify-prm-api";

	private static final String APPROVED_AND_UNPUBLISHED_FILTER_CONDITION = "{approvedAndUnpublishedFilterCondition}";

	private static final String APPROVED_AND_UNPUBLISHED_FILTER_JOIN_QUERY = "{approvedAndUnpublishedFilterJoinQuery}";

	private static final String APPROVED_AND_UNPUBLISHED_FOLDER_CONDITION = "{approvedAndUnpublishedFolderCondition}";

	@Override
	public PaginatedDTO getAllApprovalList(Pagination pagination, String searchKey,
			LeftSideNavigationBarItem leftSideNavigationBarItem) {
		Integer companyId = pagination.getCompanyId();
		if (XamplifyUtils.isValidInteger(companyId)) {
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = frameQueryStringForApproveList(
					pagination, searchKey, companyId, leftSideNavigationBarItem);
			if (StringUtils.hasText(hibernateSQLQueryResultRequestDTO.getQueryString())) {
				List<QueryParameterDTO> queryParameterDTOs = new ArrayList<>();
				List<QueryParameterListDTO> queryParameterListDTOs = new ArrayList<>();
				queryParameterDTOs.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, pagination.getCompanyId()));
				hibernateSQLQueryResultRequestDTO.setQueryParameterDTOs(queryParameterDTOs);
				hibernateSQLQueryResultRequestDTO.setQueryParameterListDTOs(queryParameterListDTOs);
				hibernateSQLQueryResultRequestDTO.setClassInstance(PendingApprovalDamAndLmsDTO.class);
				PaginatedDTO pagintedDTO = hibernateSQLQueryResultUtilDao
						.returnPaginatedDTO(hibernateSQLQueryResultRequestDTO, pagination, searchKey);
				return iterateAndSetProperties(pagintedDTO, pagination);
			} else {
				return new PaginatedDTO();
			}
		} else {
			return new PaginatedDTO();
		}
	}

	@SuppressWarnings("unchecked")
	private PaginatedDTO iterateAndSetProperties(PaginatedDTO pagintedDTO, Pagination pagination) {
		List<PendingApprovalDamAndLmsDTO> updatedApprovalList = new ArrayList<>();
		List<Integer> listAllApprovers = userDao.findAllAdminAndSuperVisorUserIdsByCompanyId(pagination.getCompanyId());
		Integer totalRecords = pagintedDTO.getTotalRecords();
		PaginatedDTO updatedPaginatedDto = new PaginatedDTO();
		boolean isSuperVisorOrAdmin = utilDao.isSuperVisorOrAdmin(pagination.getUserId());
		List<PendingApprovalDamAndLmsDTO> approvalList = (List<PendingApprovalDamAndLmsDTO>) pagintedDTO.getList();
		List<Integer> assetApprovers = updateApprovalListForAssets(pagination.getCompanyId(), listAllApprovers);
		List<Integer> playbookApprovers = updateApprovalListForPlayBooks(pagination.getCompanyId(), listAllApprovers);
		List<Integer> trackApprovers = updateApprovalListForTracks(pagination.getCompanyId(), listAllApprovers);
		for (PendingApprovalDamAndLmsDTO content : approvalList) {
			PendingApprovalDamAndLmsDTO updatedContent = new PendingApprovalDamAndLmsDTO();
			BeanUtils.copyProperties(content, updatedContent);
			boolean hasAccess = isSuperVisorOrAdmin || pagination.getUserId().equals(content.getCreatedById());
			updatedContent.setCanEdit(hasAccess);
			updatedContent.setCanDelete(hasAccess);
			if (TRACK.equalsIgnoreCase(content.getType()) || PLAYBOOK.equalsIgnoreCase(content.getType())) {
				boolean canPublish = false;
				if (content.isHasVisibility()) {
					canPublish = true;
				}
				updatedContent.setCanPublish(hasAccess && canPublish);
				updatedContent.setCanUnPublish(hasAccess && canPublish);
			}

			if (PLAYBOOK.equalsIgnoreCase(content.getType())) {
				Integer damCount = lmsDao.getDameCountByLearningId(content.getId());
				if (damCount <= 0) {
					updatedContent.setHasDamContent(false);
				}
			}

			checkIfCreatedByAnyApprovalManagerOrApprover(assetApprovers, trackApprovers, playbookApprovers,
					content.getType(), updatedContent);

			if (XamplifyUtils.isValidString(content.getAssetPath())) {
				addProxyToPDFAssetPath(updatedContent);
			}

			/** XNFR-885 **/
			setReApprovalProperties(content, updatedContent);
			updatedApprovalList.add(updatedContent);
		}
		updatedPaginatedDto.setList(updatedApprovalList);
		updatedPaginatedDto.setTotalRecords(totalRecords);
		return updatedPaginatedDto;
	}

	/** XNFR-885 **/
	private void setReApprovalProperties(PendingApprovalDamAndLmsDTO content,
			PendingApprovalDamAndLmsDTO updatedContent) {
		Session session = sessionFactory.getCurrentSession();
		updatedContent.setHasAnyReApprovalVersion((boolean) session.createSQLQuery(
				"select case when count(*) > 0 then true else false end from xt_dam where approval_reference_id = "
						+ content.getId())
				.uniqueResult());

		if (XamplifyUtils.isValidInteger(content.getApprovalReferenceId())) {
			updatedContent.setParentAssetName((String) session
					.createSQLQuery(
							"select trim(asset_name) from xt_dam where id = " + content.getApprovalReferenceId())
					.uniqueResult());
		}
	}

	private void checkIfCreatedByAnyApprovalManagerOrApprover(List<Integer> assetApprovers,
			List<Integer> trackApprovers, List<Integer> playbookApprovers, String type,
			PendingApprovalDamAndLmsDTO updatedContent) {
		boolean isCreatedByApprover = false;
		switch (type) {
		case ASSET:
			isCreatedByApprover = assetApprovers.contains(updatedContent.getCreatedById());
			break;
		case TRACK:
			isCreatedByApprover = trackApprovers.contains(updatedContent.getCreatedById());
			break;
		case PLAYBOOK:
			isCreatedByApprover = playbookApprovers.contains(updatedContent.getCreatedById());
			break;
		default:
			isCreatedByApprover = false;
			break;
		}
		updatedContent.setCreatedByAnyApprovalManagerOrApprover(isCreatedByApprover);
	}

	private HibernateSQLQueryResultRequestDTO frameQueryStringForApproveList(Pagination pagination, String searchKey,
			Integer companyId, LeftSideNavigationBarItem leftSideNavigationBarItem) {
		String queryString = "";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		searchKey = XamplifyUtils.escapeSingleQuotesForSearchQuery(searchKey);
		searchKey = XamplifyUtils.addBackSlashToSpecialCharacters(searchKey);
		boolean isApprovalRequiredForAssets = userDao.checkIfAssetApprovalRequiredByCompanyId(companyId);
		boolean isApprovalRequiredForTracks = userDao.checkIfTracksApprovalRequiredByCompanyId(companyId);
		boolean isApprovalRequiredForPlaybooks = userDao.checkIfPlaybooksApprovalRequiredByCompanyId(companyId);
		String filterBy = pagination.getFilterBy();
		filterBy = StringUtils.hasText(filterBy) ? filterBy : ALL_TYPE;
		String filterKey = pagination.getFilterKey();
		boolean isAssetFilterApplied = checkIfAssetFilterApplied(filterBy, isApprovalRequiredForAssets,
				leftSideNavigationBarItem);
		boolean isTrackFilterApplied = checkIfTrackFilterApplied(filterBy, isApprovalRequiredForTracks,
				leftSideNavigationBarItem);
		boolean isPlayBookFilterApplied = checkIfPlayBookFilterApplied(filterBy, isApprovalRequiredForPlaybooks,
				leftSideNavigationBarItem);
		queryString = frameDamApprovalListQuery(pagination, queryString, isAssetFilterApplied, filterKey, searchKey);
		queryString = frameTrackApprovalListQuery(pagination, queryString, isTrackFilterApplied, filterKey, searchKey);
		queryString = framePlayBookApprovalListQuery(pagination, queryString, isPlayBookFilterApplied, filterKey,
				searchKey);
		if (XamplifyUtils.isValidString(queryString)) {
			updateQueryString(queryString, hibernateSQLQueryResultRequestDTO, pagination);
		}
		return hibernateSQLQueryResultRequestDTO;
	}

	private void updateQueryString(String queryString,
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO, Pagination pagination) {
		StringBuilder queryStringBuilder = new StringBuilder(queryString);
		if (queryString.endsWith(XamplifyConstants.UNION)) {
			queryStringBuilder.replace(queryString.lastIndexOf(XamplifyConstants.UNION),
					queryString.lastIndexOf(XamplifyConstants.UNION) + XamplifyConstants.UNION.length(), "");
		}
		String updatedQueryString = String.valueOf(queryStringBuilder);
		String finalQueryString = allApprovalListQuery.replace(ALL_APPROVAL_LIST_STRING, updatedQueryString);
		String sortQueryString = getSelectedSortOptionQuery(pagination);
		hibernateSQLQueryResultRequestDTO.setQueryString(finalQueryString);
		hibernateSQLQueryResultRequestDTO.setSortQueryString(sortQueryString);
	}

	private String frameDamApprovalListQuery(Pagination pagination, String queryString, boolean isAssetFilterApplied,
			String filterKey, String searchKey) {
		if (isAssetFilterApplied) {
			String searchQuery = "";
			String approveStatusFilterQuery = "";
			String dateFilterQueryString = XamplifyUtils.frameDateFilterQuery(pagination, ASSET_DATE_FILTER);
			String additionalCriteriaQuery = frameCritreaBasedFilterQuery(pagination);
			if (StringUtils.hasText(searchKey)) {
				searchQuery = "and (LOWER(d.asset_name) like LOWER('%" + searchKey + "%') "
						+ "or LOWER(cat.name) like LOWER('%" + searchKey + "%'))";
			}
			if (StringUtils.hasText(filterKey)) {
				if (APPROVE_STATUS.equalsIgnoreCase(filterKey)) {
					approveStatusFilterQuery = " and d.approval_status = 'APPROVED' ";
				} else if (PENDING_STATUS.equalsIgnoreCase(filterKey)) {
					approveStatusFilterQuery = " and d.approval_status = 'CREATED' ";
				} else if (REJECTED_STATUS.equalsIgnoreCase(filterKey)) {
					approveStatusFilterQuery = " and d.approval_status = 'REJECTED' ";
				} else if (DRAFT_STATUS.equalsIgnoreCase(filterKey)) {
					approveStatusFilterQuery = " and d.approval_status = 'DRAFT' ";
				}
			}

			queryString += damApprovalListQuery.replace(SEARCH_QUERY_STRING, searchQuery) + " "
					+ approveStatusFilterQuery + additionalCriteriaQuery + dateFilterQueryString
					+ XamplifyConstants.UNION;
		}
		return queryString;
	}

	private boolean checkIfAssetFilterApplied(String filterBy, boolean isApprovalRequiredForAssets,
			LeftSideNavigationBarItem leftSideNavigationBarItem) {
		boolean hasDamAccess = leftSideNavigationBarItem.isDam();
		return hasDamAccess && isApprovalRequiredForAssets
				&& (ASSET_TYPE.equalsIgnoreCase(filterBy) || ALL_TYPE.equalsIgnoreCase(filterBy));
	}

	@Override
	public ApprovalStatisticsDTO getStatusTileCounts(Integer companyId, Pagination pagination,
			LeftSideNavigationBarItem leftSideNavigationBarItem) {
		if (XamplifyUtils.isValidInteger(companyId)) {
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = frameQueryStringForStatusTileCounts(
					pagination, companyId, leftSideNavigationBarItem);
			if (StringUtils.hasText(hibernateSQLQueryResultRequestDTO.getQueryString())) {
				List<QueryParameterDTO> queryParameterDTOs = new ArrayList<>();
				queryParameterDTOs.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
				hibernateSQLQueryResultRequestDTO.setQueryParameterDTOs(queryParameterDTOs);
				return (ApprovalStatisticsDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO,
						ApprovalStatisticsDTO.class);
			} else {
				return new ApprovalStatisticsDTO();
			}
		} else {
			return new ApprovalStatisticsDTO();
		}
	}

	private HibernateSQLQueryResultRequestDTO frameQueryStringForStatusTileCounts(Pagination pagination,
			Integer companyId, LeftSideNavigationBarItem leftSideNavigationBarItem) {
		String queryString = "";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		boolean isApprovalRequiredForAssets = userDao.checkIfAssetApprovalRequiredByCompanyId(companyId);
		boolean isApprovalRequiredForTracks = userDao.checkIfTracksApprovalRequiredByCompanyId(companyId);
		boolean isApprovalRequiredForPlaybooks = userDao.checkIfPlaybooksApprovalRequiredByCompanyId(companyId);
		String filterType = pagination.getFilterBy();
		boolean isAssetFilterApplied = checkIfAssetFilterApplied(filterType, isApprovalRequiredForAssets,
				leftSideNavigationBarItem);
		boolean isTrackFilterApplied = checkIfTrackFilterApplied(filterType, isApprovalRequiredForTracks,
				leftSideNavigationBarItem);
		boolean isPlayBookFilterApplied = checkIfPlayBookFilterApplied(filterType, isApprovalRequiredForPlaybooks,
				leftSideNavigationBarItem);
		queryString = frameAssetStatusCountQuery(pagination, queryString, isAssetFilterApplied);
		queryString = frameTrackStatusCountQuery(pagination, queryString, isTrackFilterApplied);
		queryString = framePlayBookStatusCountQuery(pagination, queryString, isPlayBookFilterApplied);
		StringBuilder queryStringBuilder = new StringBuilder(queryString);
		if (queryString.endsWith(XamplifyConstants.UNION)) {
			queryStringBuilder.replace(queryString.lastIndexOf(XamplifyConstants.UNION),
					queryString.lastIndexOf(XamplifyConstants.UNION) + XamplifyConstants.UNION.length(), "");
		}
		if (XamplifyUtils.isValidString(queryString)) {
			String updatedQueryString = String.valueOf(queryStringBuilder);
			String finalQueryString = allStatusListQuery.replace(ALL_STATUS_STRING, updatedQueryString);
			hibernateSQLQueryResultRequestDTO.setQueryString(finalQueryString);
		}
		return hibernateSQLQueryResultRequestDTO;
	}

	private String frameAssetStatusCountQuery(Pagination pagination, String queryString, boolean isAssetFilterApplied) {
		if (isAssetFilterApplied) {
			String dateFilterQuery = XamplifyUtils.frameDateFilterQuery(pagination, ASSET_DATE_FILTER);
			queryString += damStatusCountQuery + dateFilterQuery + " " + XamplifyConstants.UNION;
		}
		return queryString;
	}

	private boolean checkIfTrackFilterApplied(String filterBy, boolean isApprovalRequiredForTracks,
			LeftSideNavigationBarItem leftSideNavigationBarItem) {
		boolean hasLMSAccess = leftSideNavigationBarItem.isLms();
		return isApprovalRequiredForTracks && hasLMSAccess
				&& (TRACK_TYPE.equalsIgnoreCase(filterBy) || ALL_TYPE.equalsIgnoreCase(filterBy));
	}

	private String frameTrackApprovalListQuery(Pagination pagination, String queryString, boolean isTrackFilterApplied,
			String filterKey, String searchKey) {
		if (isTrackFilterApplied) {
			String searchQuery = "";
			String approveStatusFilterQuery = "";
			String dateFilterQueryString = XamplifyUtils.frameDateFilterQuery(pagination, LMS_DATE_FILTER);
			if (StringUtils.hasText(searchKey)) {
				searchQuery = "and (LOWER(title) like LOWER('%" + searchKey + "%') "
						+ "or LOWER(cat.name) like LOWER('%" + searchKey + "%'))";
			}

			if (StringUtils.hasText(filterKey)) {
				if (APPROVE_STATUS.equalsIgnoreCase(filterKey)) {
					approveStatusFilterQuery = " and xlt.approval_status = 'APPROVED' ";
				} else if (PENDING_STATUS.equalsIgnoreCase(filterKey)) {
					approveStatusFilterQuery = " and xlt.approval_status = 'CREATED' ";
				} else if (REJECTED_STATUS.equalsIgnoreCase(filterKey)) {
					approveStatusFilterQuery = " and xlt.approval_status = 'REJECTED' ";
				} else if (DRAFT_STATUS.equalsIgnoreCase(filterKey)) {
					approveStatusFilterQuery = " and xlt.approval_status = 'DRAFT' ";
				}
			}

			queryString += trackApprovalListQuery.replace(SEARCH_QUERY_STRING, searchQuery) + " "
					+ approveStatusFilterQuery + dateFilterQueryString + XamplifyConstants.UNION;
		}
		return queryString;
	}

	private String frameTrackStatusCountQuery(Pagination pagination, String queryString, boolean isTrackFilterApplied) {
		if (isTrackFilterApplied) {
			String dateFilterQuery = XamplifyUtils.frameDateFilterQuery(pagination, LMS_DATE_FILTER);
			queryString += trackStatusCountQuery + dateFilterQuery + " " + XamplifyConstants.UNION;
		}
		return queryString;
	}

	private boolean checkIfPlayBookFilterApplied(String filterBy, boolean isApprovalRequiredForPlaybooks,
			LeftSideNavigationBarItem leftSideNavigationBarItem) {
		boolean hasPlayBookAccess = leftSideNavigationBarItem.isPlaybook();
		return isApprovalRequiredForPlaybooks && hasPlayBookAccess
				&& (PLAYBOOK_TYPE.equalsIgnoreCase(filterBy) || ALL_TYPE.equalsIgnoreCase(filterBy));
	}

	private String framePlayBookApprovalListQuery(Pagination pagination, String queryString,
			boolean isPlayBookFilterApplied, String filterKey, String searchKey) {
		if (isPlayBookFilterApplied) {
			String searchQuery = "";
			String approveStatusFilterQuery = "";
			String dateFilterQueryString = XamplifyUtils.frameDateFilterQuery(pagination, LMS_DATE_FILTER);
			if (StringUtils.hasText(searchKey)) {
				searchQuery = "and (LOWER(title) like LOWER('%" + searchKey + "%') "
						+ "or LOWER(cat.name) like LOWER('%" + searchKey + "%'))";
			}

			if (StringUtils.hasText(filterKey)) {
				if (APPROVE_STATUS.equalsIgnoreCase(filterKey)) {
					approveStatusFilterQuery = " and xlt.approval_status = 'APPROVED' ";
				} else if (PENDING_STATUS.equalsIgnoreCase(filterKey)) {
					approveStatusFilterQuery = " and xlt.approval_status = 'CREATED' ";
				} else if (REJECTED_STATUS.equalsIgnoreCase(filterKey)) {
					approveStatusFilterQuery = " and xlt.approval_status = 'REJECTED' ";
				} else if (DRAFT_STATUS.equalsIgnoreCase(filterKey)) {
					approveStatusFilterQuery = " and xlt.approval_status = 'DRAFT' ";
				}
			}

			queryString += playBookApprovalListQuery.replace(SEARCH_QUERY_STRING, searchQuery) + " "
					+ approveStatusFilterQuery + dateFilterQueryString + XamplifyConstants.UNION;
		}
		return queryString;
	}

	private String framePlayBookStatusCountQuery(Pagination pagination, String queryString,
			boolean isPlayBookFilterApplied) {
		if (isPlayBookFilterApplied) {
			String dateFilterQuery = XamplifyUtils.frameDateFilterQuery(pagination, LMS_DATE_FILTER);
			queryString += playBookStatusCountQuery + dateFilterQuery + " " + XamplifyConstants.UNION;
		}
		return queryString;
	}

	@Override
	public Integer updateApprovalStatus(List<Integer> ids, String status, Integer loggedInUserId, String moduleType) {
		String fromTable = getFromTableByModuleType(moduleType);
		if (!XamplifyUtils.isValidString(fromTable) || !XamplifyUtils.isNotEmptyList(ids)) {
			return null;
		}

		String queryString = " UPDATE " + fromTable
				+ " SET approval_status = cast(:status as approval_status_type), approval_status_updated_by = :updatedBy, "
				+ "approval_status_updated_time = :updatedTime WHERE id in (:ids)";
		Session session = sessionFactory.getCurrentSession();
		Integer updateCount = 0;
		List<List<Integer>> chunkedIds = XamplifyUtils.getChunkedList(ids);
		for (List<Integer> splittedIds : chunkedIds) {
			SQLQuery query = session.createSQLQuery(queryString);
			query.setParameterList("ids", splittedIds);
			query.setParameter("status", status);
			query.setParameter("updatedBy", loggedInUserId);
			query.setParameter(UPDATED_TIME, new Date());
			updateCount += query.executeUpdate();
		}
		return updateCount;
	}

	private String getFromTableByModuleType(String moduleType) {
		if (ModuleType.DAM.name().equals(moduleType)) {
			return "xt_dam";
		} else if (ModuleType.TRACK.name().equals(moduleType) || ModuleType.PLAYBOOK.name().equals(moduleType)) {
			return "xt_learning_track";
		}
		return "";
	}

	@Override
	public boolean isAssociatedWithLMS(List<Integer> damIds) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select case when count(*)>0 then true else false end as lmsCount from xt_learning_track l, xt_learning_track_content ltc "
				+ " where l.id = ltc.learning_track_id and ltc.dam_id in (:damIds) ";
		return (boolean) session.createSQLQuery(sql).setParameterList("damIds", damIds).uniqueResult();
	}

	@Override
	public boolean isPublished(List<Integer> lmsIds) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select case when count(*)>0 then true else false end as lmsCount from xt_learning_track l "
				+ " where l.is_published = true and l.id in (:lmsIds) ";
		return (boolean) session.createSQLQuery(sql).setParameterList("lmsIds", lmsIds).uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> findNamesByAssetId(List<Integer> damIds) {
		List<String> records = new ArrayList<>();
		if (XamplifyUtils.isNotEmptyList(damIds)) {
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			String sqlString = "select distinct d.asset_name from xt_learning_track l \r\n"
					+ "join xt_learning_track_content ltc on l.id = ltc.learning_track_id join xt_dam d on  d.id = ltc.dam_id "
					+ " where ltc.dam_id in (:damIds)";
			hibernateSQLQueryResultRequestDTO.setQueryString(sqlString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
					.add(new QueryParameterListDTO("damIds", damIds));
			return (List<String>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
		} else {
			return records;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> findNamesBylmsId(List<Integer> lmsIds) {
		List<String> records = new ArrayList<>();
		if (XamplifyUtils.isNotEmptyList(lmsIds)) {
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			String sqlString = "select distinct title from xt_learning_track l \r\n"
					+ " where l.is_published = true and l.id in (:lmsIds) ";
			hibernateSQLQueryResultRequestDTO.setQueryString(sqlString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
					.add(new QueryParameterListDTO("lmsIds", lmsIds));
			return (List<String>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
		} else {
			return records;
		}
	}

	private String getSelectedSortOptionQuery(Pagination pagination) {
		String sortOptionQueryString = "";
		if (StringUtils.hasText(pagination.getSortcolumn())) {
			if (NAME.equals(pagination.getSortcolumn())) {
				sortOptionQueryString += nameColumnOrderByQuery + " " + pagination.getSortingOrder();
			} else if (CREATED_TIME.equals(pagination.getSortcolumn())) {
				sortOptionQueryString += createdOnOrderByQuery + " " + pagination.getSortingOrder();
			}
		} else {
			sortOptionQueryString += createdOnOrderByQuery + DESC;
		}
		return sortOptionQueryString;
	}

//    XNFR-837
	private String frameCritreaBasedFilterQuery(Pagination pagination) {
		String additionalQuery = "";
		Criteria[] criterias = pagination.getCriterias();
		if (ArrayUtils.isNotEmpty(criterias) && !"Field Name*".equals(criterias[0].getProperty())) {
			StringBuilder criteriaStringBuilder = new StringBuilder();
			criteriaStringBuilder.append(" and (");
			for (int i = 0; i < criterias.length; i++) {
				Criteria criteriaQuery = criterias[i];
				criteriaQuery.setOperationName(Criteria.getOperationNameEnum(criteriaQuery.getOperation()));
				if (i != 0) {
					criteriaStringBuilder.append(" and ");
				}
				String criteriaPropertyValue = " ";
				if (criteriaQuery.getValue1() != null) {
					criteriaPropertyValue = String.valueOf(criteriaQuery.getValue1()).toLowerCase().trim();

				}
				appendQueryString(criteriaStringBuilder, criteriaQuery, criteriaPropertyValue);
			}
			criteriaStringBuilder.append(" ) ");
			additionalQuery = criteriaStringBuilder.toString();
		}
		return additionalQuery;
	}

	private void appendQueryString(StringBuilder criteriaStringBuilder, Criteria criteriaQuery,
			String criteriaPropertyValue) {
		if (criteriaPropertyValue != null) {
			criteriaPropertyValue = criteriaPropertyValue.replace("'", "''");
			if (criteriaQuery.getProperty().equalsIgnoreCase("assetsname")) {
				addAssetNameFilterQuery(criteriaQuery, criteriaStringBuilder, criteriaPropertyValue);
			} else if (criteriaQuery.getProperty().equalsIgnoreCase("folder")) {
				addFolderFilterQuery(criteriaQuery, criteriaStringBuilder, criteriaPropertyValue);
			} else if (criteriaQuery.getProperty().equalsIgnoreCase("type")) {
				addTypeFilterQuery(criteriaQuery, criteriaStringBuilder, criteriaPropertyValue);
			} else if (criteriaQuery.getProperty().equalsIgnoreCase(CREATED_BY)) {
				addCreatedByFilterQuery(criteriaQuery, criteriaStringBuilder, criteriaPropertyValue);
			}
		}
	}

	private void addCreatedByFilterQuery(Criteria criteriaQuery, StringBuilder criteriaStringBuilder,
			String criteriaPropertyValue) {
		if (criteriaQuery.getOperationName() == OPERATION_NAME.eq) {
			appendCreatedByEqualsQueryString(criteriaStringBuilder, criteriaPropertyValue);
		} else if (criteriaQuery.getOperationName() == OPERATION_NAME.like) {
			appendCreatedByLikeQueryString(criteriaStringBuilder, criteriaPropertyValue);
		}
	}

	private void addTypeFilterQuery(Criteria criteriaQuery, StringBuilder criteriaStringBuilder,
			String criteriaPropertyValue) {
		if (criteriaQuery.getOperationName() == OPERATION_NAME.eq) {
			criteriaStringBuilder.append(" LOWER(d.asset_type) = '" + criteriaPropertyValue + "' ");
		}
	}

	private void addFolderFilterQuery(Criteria criteriaQuery, StringBuilder criteriaStringBuilder,
			String criteriaPropertyValue) {
		if (criteriaQuery.getOperationName() == OPERATION_NAME.eq) {
			criteriaStringBuilder.append(" LOWER(cast(cat.name as text)) = '" + criteriaPropertyValue + "' ");
		} else if (criteriaQuery.getOperationName() == OPERATION_NAME.like) {
			criteriaStringBuilder.append(" LOWER(cast(cat.name as text)) like'%" + criteriaPropertyValue + "%' ");
		}
	}

	private void addAssetNameFilterQuery(Criteria criteriaQuery, StringBuilder criteriaStringBuilder,
			String criteriaPropertyValue) {
		if (criteriaQuery.getOperationName() == OPERATION_NAME.eq) {
			criteriaStringBuilder.append(" lower(d.asset_name)='" + criteriaPropertyValue + "' ");
		} else if (criteriaQuery.getOperationName() == OPERATION_NAME.like) {
			criteriaStringBuilder.append(" lower(d.asset_name) like'%" + criteriaPropertyValue + "%' ");
		}
	}

	private void appendCreatedByEqualsQueryString(StringBuilder criteriaStringBuilder, String criteriaPropertyValue) {
		criteriaStringBuilder
				.append(" (LOWER(xup.firstname) = '" + criteriaPropertyValue + "' " + " OR LOWER(xup.lastname) = '"
						+ criteriaPropertyValue + "' " + " OR LOWER(xup.email_id) = '" + criteriaPropertyValue + "' "
						+ "OR LOWER(xup.firstname|| ' ' || xup.lastname ) = '" + criteriaPropertyValue + "') ");
	}

	private void appendCreatedByLikeQueryString(StringBuilder criteriaStringBuilder, String criteriaPropertyValue) {
		criteriaStringBuilder.append(" (LOWER(xup.firstname) like '%" + criteriaPropertyValue + "%' "
				+ " OR LOWER(xup.lastname) like '%" + criteriaPropertyValue + "%' " + " OR LOWER(xup.email_id) like '%"
				+ criteriaPropertyValue + "%' " + "OR LOWER(xup.firstname|| ' ' || xup.lastname ) like '%"
				+ criteriaPropertyValue + "%') ");
	}

	/** XNFR-821 **/
	@Override
	public PaginatedDTO listTeamMembersForApprovalControlManagement(Pagination pagination, String searchKey) {
		Integer companyId = pagination.getCompanyId();
		if (XamplifyUtils.isValidInteger(companyId)) {
			String sqlQueryString = listTeamMembersForApprovalControlManagementSqlQuery;
			if (XamplifyUtils.isValidString(searchKey)) {
				sqlQueryString = sqlQueryString.replace("${approvalManagementSearchQuery}",
						listTeamMembersForApprovalControlManagementSqlSearchQuery);
				sqlQueryString = sqlQueryString.replace("${searchKey}", searchKey);
			} else {
				sqlQueryString = sqlQueryString.replace("${approvalManagementSearchQuery}", "");
			}
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(sqlQueryString);
			List<QueryParameterDTO> queryParameterDTOs = new ArrayList<>();
			queryParameterDTOs.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, pagination.getCompanyId()));
			hibernateSQLQueryResultRequestDTO.setQueryParameterDTOs(queryParameterDTOs);
			hibernateSQLQueryResultRequestDTO
					.setSortQueryString("order by \"anyApprover\" desc, \"role\", xtm.created_time desc");
			hibernateSQLQueryResultRequestDTO.setClassInstance(TeamMemberApprovalPrivilegesDTO.class);
			return hibernateSQLQueryResultUtilDao.returnPaginatedDTO(hibernateSQLQueryResultRequestDTO, pagination,
					searchKey);
		} else {
			return new PaginatedDTO();
		}
	}

	/** XNFR-821 **/
	@Override
	public TeamMemberApprovalPrivilegesDTO getTeamMemberApprovalPrivilegeSettingsByTeamMemberId(Integer teamMemberId,
			Integer companyId) {

		if (!XamplifyUtils.isValidInteger(teamMemberId) || !XamplifyUtils.isValidInteger(companyId)) {
			return new TeamMemberApprovalPrivilegesDTO();
		}

		String queryString = "select is_asset_approver as \"assetApprover\", is_track_approver as \"trackApprover\", is_playbook_approver "
				+ "as \"playbookApprover\" from xt_team_member where team_member_id = :teamMemberId and company_id = :companyId";

		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add((new QueryParameterDTO(COMPANY_ID, companyId)));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add((new QueryParameterDTO(TEAM_MEMBER_ID, teamMemberId)));
		return (TeamMemberApprovalPrivilegesDTO) hibernateSQLQueryResultUtilDao
				.getDto(hibernateSQLQueryResultRequestDTO, TeamMemberApprovalPrivilegesDTO.class);
	}

	/** XNFR-821 **/
	@Override
	public Integer updateTeamMemberApprovalPrivilegeSettings(
			TeamMemberApprovalPrivilegesDTO teamMemberApprovalPrivilegesDTO, Integer companyId) {
		Integer updatedRowCount = null;
		if (XamplifyUtils.isValidInteger(teamMemberApprovalPrivilegesDTO.getId())
				&& XamplifyUtils.isValidInteger(companyId)) {
			String queryString = "update xt_team_member set is_asset_approver = :isAssetApprover, is_track_approver = :isTrackApprover,"
					+ "is_playbook_approver = :isPlaybookApprover where company_id = :companyId and team_member_id = :teamMemberId";
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(TEAM_MEMBER_ID, teamMemberApprovalPrivilegesDTO.getId()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("isAssetApprover", teamMemberApprovalPrivilegesDTO.isAssetApprover()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("isTrackApprover", teamMemberApprovalPrivilegesDTO.isTrackApprover()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(
					new QueryParameterDTO("isPlaybookApprover", teamMemberApprovalPrivilegesDTO.isPlaybookApprover()));
			updatedRowCount = hibernateSQLQueryResultUtilDao.updateAndReturnCount(hibernateSQLQueryResultRequestDTO);
		}
		return updatedRowCount;
	}

	/** XNFR-821 **/
	@Override
	public void autoApprovePendingAssetsCreatedByNewlyAssignedApprover(
			TeamMemberApprovalPrivilegesDTO teamMemberApprovalPrivilegesDTO, Integer loggedInUserId,
			Integer companyId) {
		Integer cretedById = teamMemberApprovalPrivilegesDTO.getId();
		if (XamplifyUtils.isValidInteger(teamMemberApprovalPrivilegesDTO.getId())
				&& XamplifyUtils.isValidInteger(companyId) && XamplifyUtils.isValidInteger(loggedInUserId)
				&& teamMemberApprovalPrivilegesDTO.isAssetApprover() && XamplifyUtils.isValidInteger(cretedById)) {
			List<Integer> createdByIds = new ArrayList<>();
			createdByIds.add(cretedById);
			autoApprovePendingAssets(loggedInUserId, companyId, createdByIds);
		}

	}

	@Override
	public void autoApprovePendingAssets(Integer loggedInUserId, Integer companyId, List<Integer> createdByIds) {
		if (XamplifyUtils.isValidInteger(loggedInUserId) && XamplifyUtils.isValidInteger(companyId)
				&& XamplifyUtils.isNotEmptyList(createdByIds)) {
			String queryString = "update xt_dam set approval_status = 'APPROVED', approval_status_updated_by = :userId, "
					+ "approval_status_updated_time = :updatedTime where company_id = :companyId and created_by in (:createdByIds) "
					+ "and (approval_status = 'CREATED' or approval_status = 'REJECTED') and approval_reference_id is null";
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(XamplifyConstants.USER_ID, loggedInUserId));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(UPDATED_TIME, new Date()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
			hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
					.add(new QueryParameterListDTO(CREATED_BY_IDS, createdByIds));
			hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
		}
	}

	/** XNFR-821 **/
	@Override
	public void autoApprovePendingTracksOrPlaybooksCreatedByNewlyAssignedApprover(
			TeamMemberApprovalPrivilegesDTO teamMemberApprovalPrivilegesDTO, Integer companyId, Integer loggedInUserId,
			String moduleType) {
		if (XamplifyUtils.isValidInteger(teamMemberApprovalPrivilegesDTO.getId())
				&& XamplifyUtils.isValidInteger(companyId) && XamplifyUtils.isValidInteger(loggedInUserId)
				&& XamplifyUtils.isValidString(moduleType)) {
			List<Integer> createdByIds = new ArrayList<>();
			createdByIds.add(teamMemberApprovalPrivilegesDTO.getId());
			autoApprovePendingLMS(createdByIds, companyId, loggedInUserId, moduleType);
		}
	}

	@Override
	public void autoApprovePendingLMS(List<Integer> createdByIds, Integer companyId, Integer loggedInUserId,
			String moduleType) {
		if (XamplifyUtils.isValidInteger(loggedInUserId) && XamplifyUtils.isValidInteger(companyId)
				&& XamplifyUtils.isNotEmptyList(createdByIds)) {
			String queryString = "update xt_learning_track set approval_status = 'APPROVED', approval_status_updated_by = :userId, "
					+ "approval_status_updated_time = :updatedTime where company_id = :companyId and created_by in (:createdByIds) "
					+ "${moduleTypeQueryCondition} and (approval_status = 'CREATED' or approval_status = 'REJECTED')";

			if (XamplifyUtils.isValidString(moduleType)) {
				queryString = queryString.replace(MODULE_TYPE_QUERY_REPLACE_KEY,
						"and type = cast(:moduleType as learning_track_type)");
			} else {
				queryString = queryString.replace(MODULE_TYPE_QUERY_REPLACE_KEY, "");
			}

			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(XamplifyConstants.USER_ID, loggedInUserId));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(UPDATED_TIME, new Date()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
			hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
					.add(new QueryParameterListDTO(CREATED_BY_IDS, createdByIds));
			if (XamplifyUtils.isValidString(moduleType)) {
				hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
						.add(new QueryParameterDTO(MODULE_TYPE, moduleType.toUpperCase()));
			}
			hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
		}
	}

	/** XNFR-821 **/
	@Override
	public boolean isApprovalPrivilegeManager(Integer loggedInUserId) {
		List<Integer> roleIds = userDao.getRoleIdsByUserId(loggedInUserId);
		boolean isPrm = roleIds.indexOf(Role.PRM_ROLE.getRoleId()) > -1;
		boolean isSuperVisor = roleIds.indexOf(Role.ALL_ROLES.getRoleId()) > -1;
		return isSuperVisor || isPrm;
	}

	/** XNFR-821 **/
	@Override
	public boolean checkIsAssetApproverByTeamMemberIdAndCompanyId(Integer teamMemberId, Integer companyId) {
		String queryString = "select is_asset_approver from xt_team_member where team_member_id = :userId and company_id = :companyId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.USER_ID, teamMemberId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
		return hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

	/** XNFR-821 **/
	@Override
	public boolean checkIsTrackApproverByTeamMemberIdAndCompanyId(Integer teamMemberId, Integer companyId) {
		String queryString = "select is_track_approver from xt_team_member where team_member_id = :userId and company_id = :companyId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.USER_ID, teamMemberId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
		return hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

	/** XNFR-821 **/
	@Override
	public boolean checkIsPlaybookApproverByTeamMemberIdAndCompanyId(Integer teamMemberId, Integer companyId) {
		String queryString = "select is_playbook_approver from xt_team_member where team_member_id = :userId and company_id = :companyId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.USER_ID, teamMemberId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
		return hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

	/** XNFR-821 **/
	@Override
	public Integer updateApprovalConfigurationSettingsForCompany(ApprovalSettingsDTO approvalSettingsDTO) {
		Session session = sessionFactory.openSession();
		Integer updateCount = 0;
		try {
			if (XamplifyUtils.isValidInteger(approvalSettingsDTO.getCompanyId())) {
				SQLQuery query = session.createSQLQuery(updateApprovalConfigurationSettingsForCompanySqlQuery);
				query.setParameter("assetApprovalEnabledForCompany",
						approvalSettingsDTO.isAssetApprovalEnabledForCompany());
				query.setParameter("tracksApprovalEnabledForCompany",
						approvalSettingsDTO.isTracksApprovalEnabledForCompany());
				query.setParameter("playbooksApprovalEnabledForCompany",
						approvalSettingsDTO.isPlaybooksApprovalEnabledForCompany());
				query.setParameter(XamplifyConstants.COMPANY_ID, approvalSettingsDTO.getCompanyId());
				updateCount = query.executeUpdate();
			}
			session.flush();
			session.clear();
		} catch (SessionException se) {
			logger.debug(UPDATE_APPROVAL_CONFIGURATION_SETTINGS_ERROR_STRING, se.getMessage(), new Date());
		} catch (HibernateException e) {
			logger.debug(UPDATE_APPROVAL_CONFIGURATION_SETTINGS_ERROR_STRING, e.getMessage(), new Date());
		} catch (Exception ex) {
			logger.debug(UPDATE_APPROVAL_CONFIGURATION_SETTINGS_ERROR_STRING, ex.getMessage(), new Date());
		} finally {
			session.close();
		}
		return updateCount;
	}

	/** XNFR-821 **/
	@Override
	public Integer updateApprovalConfigurationSettingsForTeamMembers(
			TeamMemberApprovalPrivilegesDTO teamMemberApprovalPrivilegesDTO, Integer companyId,
			List<Integer> teamMemberIds) {
		Session session = sessionFactory.openSession();
		Integer updateCount = 0;
		try {
			if (XamplifyUtils.isValidInteger(companyId) && XamplifyUtils.isNotEmptyList(teamMemberIds)) {
				List<String> columnsToUpdate = new ArrayList<>();
				frameColumnNamesToUpdate(teamMemberApprovalPrivilegesDTO, columnsToUpdate);
				if (XamplifyUtils.isNotEmptyList(columnsToUpdate)) {
					String queryString = "update xt_team_member SET " + String.join(", ", columnsToUpdate)
							+ " where team_member_id IN (:teamMemberIds) and company_id = :companyId";
					List<List<Integer>> chunkedIds = XamplifyUtils.getChunkedList(teamMemberIds);
					for (List<Integer> splittedIds : chunkedIds) {
						SQLQuery query = session.createSQLQuery(queryString);
						query.setParameterList("teamMemberIds", splittedIds);
						frameSqlParametersToUpdate(teamMemberApprovalPrivilegesDTO, query);
						query.setParameter(XamplifyConstants.COMPANY_ID, companyId);
						updateCount += query.executeUpdate();
					}
				}
			}
			session.flush();
			session.clear();
		} catch (SessionException se) {
			logger.debug(UPDATE_APPROVAL_CONFIGURATION_SETTINGS_ERROR_STRING, se.getMessage(), new Date());
		} catch (HibernateException e) {
			logger.debug(UPDATE_APPROVAL_CONFIGURATION_SETTINGS_ERROR_STRING, e.getMessage(), new Date());
		} catch (Exception ex) {
			logger.debug(UPDATE_APPROVAL_CONFIGURATION_SETTINGS_ERROR_STRING, ex.getMessage(), new Date());
		} finally {
			session.close();
		}
		return updateCount;
	}

	private void frameSqlParametersToUpdate(TeamMemberApprovalPrivilegesDTO teamMemberApprovalPrivilegesDTO,
			SQLQuery query) {
		if (teamMemberApprovalPrivilegesDTO.isAssetApproverFieldUpdated()) {
			query.setParameter("assetApprover", teamMemberApprovalPrivilegesDTO.isAssetApprover());
		}
		if (teamMemberApprovalPrivilegesDTO.isTrackApproverFieldUpdated()) {
			query.setParameter("trackApprover", teamMemberApprovalPrivilegesDTO.isTrackApprover());
		}
		if (teamMemberApprovalPrivilegesDTO.isPlaybookApproverFieldUpdated()) {
			query.setParameter("playbookApprover", teamMemberApprovalPrivilegesDTO.isPlaybookApprover());
		}
	}

	private void frameColumnNamesToUpdate(TeamMemberApprovalPrivilegesDTO teamMemberApprovalPrivilegesDTO,
			List<String> columnsToUpdate) {
		if (teamMemberApprovalPrivilegesDTO.isAssetApproverFieldUpdated())
			columnsToUpdate.add("is_asset_approver = :assetApprover");
		if (teamMemberApprovalPrivilegesDTO.isTrackApproverFieldUpdated())
			columnsToUpdate.add("is_track_approver = :trackApprover");
		if (teamMemberApprovalPrivilegesDTO.isPlaybookApproverFieldUpdated())
			columnsToUpdate.add("is_playbook_approver = :playbookApprover");
	}

	/** XNFR-821 **/
	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> listAssetApproverTeamMembers(Integer companyId) {
		if (XamplifyUtils.isValidInteger(companyId)) {
			String queryString = "select team_member_id from xt_team_member where is_asset_approver = true and company_id = :companyId and cast(status as text) = 'APPROVE'";
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
			return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
		}
		return Collections.emptyList();
	}

	/** XNFR-821 **/
	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> listTrackApproverTeamMembers(Integer companyId) {
		if (XamplifyUtils.isValidInteger(companyId)) {
			String queryString = "select team_member_id from xt_team_member where is_track_approver = true and company_id = :companyId and cast(status as text) = 'APPROVE'";
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
			return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
		}
		return Collections.emptyList();
	}

	/** XNFR-821 **/
	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> listPlaybookApproverTeamMembers(Integer companyId) {
		if (XamplifyUtils.isValidInteger(companyId)) {
			String queryString = "select team_member_id from xt_team_member where is_playbook_approver = true and company_id = :companyId and cast(status as text) = 'APPROVE'";
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
			return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
		}
		return Collections.emptyList();
	}

	/** XNFR-821 **/
	@Override
	public List<Integer> findAllApproversByModuleTypeAndCompanyId(Integer companyId, String moduleType) {
		List<Integer> allApproverList = userDao.findAllAdminAndSuperVisorUserIdsByCompanyId(companyId);
		if (ModuleType.DAM.name().equals(moduleType)) {
			allApproverList = updateApprovalListForAssets(companyId, allApproverList);
		} else if (ModuleType.TRACK.name().equals(moduleType)) {
			allApproverList = updateApprovalListForTracks(companyId, allApproverList);
		} else if (ModuleType.PLAYBOOK.name().equals(moduleType)) {
			allApproverList = updateApprovalListForPlayBooks(companyId, allApproverList);
		}
		return allApproverList;
	}

	@Override
	public PendingApprovalDamAndLmsDTO fetchPendingApprovalAssetDetails(Integer entityId) {
		if (XamplifyUtils.isValidInteger(entityId)) {
			String sqlQueryString = "select xd.id AS \"id\", xd.asset_name AS \"name\", cast(xd.asset_type as text) as \"assetType\", xd.company_id as \"createdByCompanyId\", "
					+ "xd.created_time AS \"createdTime\", xd.created_by AS \"createdById\", cast(xd.approval_status as text) as \"status\" FROM xt_dam xd "
					+ "WHERE cast(xd.approval_status as text) = 'CREATED' AND xd.id = :entityId";
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(sqlQueryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(ENTITY_ID, entityId));
			return (PendingApprovalDamAndLmsDTO) hibernateSQLQueryResultUtilDao
					.getDto(hibernateSQLQueryResultRequestDTO, PendingApprovalDamAndLmsDTO.class);
		}
		return new PendingApprovalDamAndLmsDTO();
	}

	@Override
	public PendingApprovalDamAndLmsDTO fetchPendingApprovalLMSDetails(Integer entityId, String moduleType) {
		if (XamplifyUtils.isValidInteger(entityId) && XamplifyUtils.isValidString(moduleType)) {
			String sqlQueryString = "SELECT xlt.id AS \"id\", xlt.title AS \"name\", xlt.created_time AS \"createdTime\", xlt.created_by AS \"createdById\", "
					+ "cast(xlt.type AS text) AS \"type\", xlt.approval_status AS \"status\", xlt.company_id as \"createdByCompanyId\" "
					+ "FROM xt_learning_track xlt WHERE cast(xlt.approval_status as text) = 'CREATED' AND xlt.id = :entityId AND cast(xlt.type as text) = :moduleType";
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(sqlQueryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(ENTITY_ID, entityId));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(MODULE_TYPE, moduleType));
			return (PendingApprovalDamAndLmsDTO) hibernateSQLQueryResultUtilDao
					.getDto(hibernateSQLQueryResultRequestDTO, PendingApprovalDamAndLmsDTO.class);
		}
		return new PendingApprovalDamAndLmsDTO();
	}

	private List<Integer> updateApprovalListForPlayBooks(Integer companyId, List<Integer> allApproverList) {
		List<Integer> playbookApproverList = listPlaybookApproverTeamMembers(companyId);
		allApproverList = Stream.concat(allApproverList.stream(), playbookApproverList.stream()).distinct()
				.collect(Collectors.toList());
		return allApproverList;
	}

	private List<Integer> updateApprovalListForTracks(Integer companyId, List<Integer> allApproverList) {
		List<Integer> trackApproverList = listTrackApproverTeamMembers(companyId);
		allApproverList = Stream.concat(allApproverList.stream(), trackApproverList.stream()).distinct()
				.collect(Collectors.toList());
		return allApproverList;
	}

	private List<Integer> updateApprovalListForAssets(Integer companyId, List<Integer> allApproverList) {
		List<Integer> assetApproverList = listAssetApproverTeamMembers(companyId);
		allApproverList = Stream.concat(allApproverList.stream(), assetApproverList.stream()).distinct()
				.collect(Collectors.toList());
		return allApproverList;
	}

	/** XNFR-884 **/
	@SuppressWarnings("unchecked")
	@Override
	public List<MultiSelectApprovalDTO> getPendingApprovalEntityIdsByCreatorAndModuleType(List<Integer> createdByIds,
			String moduleType) {
		if (XamplifyUtils.isNotEmptyList(createdByIds) && XamplifyUtils.isValidString(moduleType)) {
			String fromTableName = getFromTableByModuleType(moduleType);
			if (XamplifyUtils.isValidString(fromTableName)) {
				String queryString = "select t.id as \"entityId\", t.created_by as \"createdById\" " + "from "
						+ fromTableName
						+ " as t where t.created_by in (:createdByIds) and (t.approval_status = 'CREATED' or t.approval_status = 'REJECTED')${reApprovalVersionMergeTag}";

				if (moduleType.equals(ModuleType.TRACK.name())) {
					queryString += " and t.type = 'TRACK'";
				} else if (moduleType.equals(ModuleType.PLAYBOOK.name())) {
					queryString += " and t.type = 'PLAYBOOK'";
				}
				/** XNFR-885 **/
				if (fromTableName.equals("xt_dam")) {
					queryString = queryString.replace("${reApprovalVersionMergeTag}",
							" and t.approval_reference_id is null");
				} else {
					queryString = queryString.replace("${reApprovalVersionMergeTag}", "");
				}

				HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
				hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
				hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
						.add(new QueryParameterListDTO(CREATED_BY_IDS, createdByIds));
				return (List<MultiSelectApprovalDTO>) hibernateSQLQueryResultUtilDao
						.getListDto(hibernateSQLQueryResultRequestDTO, MultiSelectApprovalDTO.class);
			}
			return Collections.emptyList();
		}
		return Collections.emptyList();
	}

	/** XNFR-884 **/
	@Override
	public ApprovalStatisticsDTO getApprovalStatusTileCountsByModuleType(Integer companyId, String moduleType, boolean showTiles, Integer categoryId) {

	    if (!XamplifyUtils.isValidInteger(companyId) || !XamplifyUtils.isValidString(moduleType)) {
	        return new ApprovalStatisticsDTO();
	    }

	    String fromTable = getFromTableByModuleType(moduleType);
	    if (!XamplifyUtils.isValidString(fromTable)) {
	        return new ApprovalStatisticsDTO();
	    }
	    
	    String queryString;
	    if (XamplifyUtils.isValidInteger(categoryId)) {
	        queryString = buildQueryForNoTiles(fromTable, moduleType);
	    } else {
	        queryString = buildQueryForTiles(fromTable, moduleType);
	    }

	    HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
	    hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
	    hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(COMPANY_ID, companyId));
	    if (XamplifyUtils.isValidInteger(categoryId)) {
	    	if (queryString.contains(":category_id")) {
	    	    hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("category_id", categoryId));
	    	}
	    	if (queryString.contains(":id")) {
	    	    hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("id", categoryId));
	    	}

	    }
	    return (ApprovalStatisticsDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO, ApprovalStatisticsDTO.class);
	}
	
	private String buildQueryForNoTiles(String fromTable, String moduleType) {
		String query = "select cast(count(distinct case when t.approval_status = 'APPROVED' {approvedAndUnpublishedFilterCondition} then t.id end) as integer) as \"approvedCount\", "
				+ "cast(count(distinct case when t.approval_status = 'REJECTED'  {approvedAndUnpublishedFilterCondition} then t.id end) as integer) as \"rejectedCount\", "
				+ "cast(count(distinct case when t.approval_status = 'CREATED' {approvedAndUnpublishedFilterCondition} then t.id end) as integer) as \"pendingCount\", "
				+ "cast(count(distinct case when t.approval_status = 'DRAFT' {approvedAndUnpublishedFilterCondition} then t.id end) as integer) as \"draftCount\", "
				+ "cast(count(distinct t.id) as integer) as \"totalCount\" from " + fromTable
				+ " t {approvedAndUnpublishedFilterJoinQuery} {approvedAndUnpublishedFolderCondition}";

		if (ModuleType.DAM.name().equals(moduleType)) {
			query += " and parent_id is null and created_for_company is null ";
			query = query.replace(APPROVED_AND_UNPUBLISHED_FILTER_JOIN_QUERY,
					"left JOIN xt_dam_partner dp ON t.id = dp.dam_id");
			query = query.replace(APPROVED_AND_UNPUBLISHED_FILTER_CONDITION, "and dp.dam_id IS null");
			query = query.replace(APPROVED_AND_UNPUBLISHED_FOLDER_CONDITION,
					"left join xt_category_module cm on cm.dam_id=t.id left join xt_category c on c.id= cm.category_id where t.company_id=:companyId and c.id=:id");
		} else if (ModuleType.TRACK.name().equals(moduleType)) {
			query += " and type = 'TRACK'";
			query = query.replace(APPROVED_AND_UNPUBLISHED_FILTER_JOIN_QUERY, "");
			query = query.replace(APPROVED_AND_UNPUBLISHED_FILTER_CONDITION, "and is_published = false");
			query = query.replace(APPROVED_AND_UNPUBLISHED_FOLDER_CONDITION,
					"left JOIN xt_category_module cm  ON t.id = cm.learning_track_id where cm.category_id=:category_id and t.company_id=:companyId");
		} else if (ModuleType.PLAYBOOK.name().equals(moduleType)) {
			query += " and type = 'PLAYBOOK'";
			query = query.replace(APPROVED_AND_UNPUBLISHED_FILTER_JOIN_QUERY, "");
			query = query.replace(APPROVED_AND_UNPUBLISHED_FILTER_CONDITION, "and is_published = false");
			query = query.replace(APPROVED_AND_UNPUBLISHED_FOLDER_CONDITION,
					"left JOIN xt_category_module cm  ON t.id = cm.learning_track_id where cm.category_id=:category_id and t.company_id=:companyId");
		} else {
			query = query.replace(APPROVED_AND_UNPUBLISHED_FILTER_JOIN_QUERY, "");
			query = query.replace(APPROVED_AND_UNPUBLISHED_FILTER_CONDITION, "");
		}
		return query;
	}

	private String buildQueryForTiles(String fromTable, String moduleType) {
		String query = "select cast(count(distinct case when t.approval_status = 'APPROVED' {approvedAndUnpublishedFilterCondition} then t.id end) as integer) as \"approvedCount\", "
				+ "cast(count(distinct case when t.approval_status = 'REJECTED'  {approvedAndUnpublishedFilterCondition} then t.id end) as integer) as \"rejectedCount\", "
				+ "cast(count(distinct case when t.approval_status = 'CREATED' {approvedAndUnpublishedFilterCondition} then t.id end) as integer) as \"pendingCount\", "
				+ "cast(count(distinct case when t.approval_status = 'DRAFT' {approvedAndUnpublishedFilterCondition} then t.id end) as integer) as \"draftCount\", "
				+ "cast(count(distinct t.id) as integer) as \"totalCount\" from " + fromTable
				+ " t {approvedAndUnpublishedFilterJoinQuery}   where company_id = :companyId ";

		if (ModuleType.DAM.name().equals(moduleType)) {
			query += " and parent_id is null and created_for_company is null ";
			query = query.replace(APPROVED_AND_UNPUBLISHED_FILTER_JOIN_QUERY,
					"left JOIN xt_dam_partner dp ON t.id = dp.dam_id");
			query = query.replace(APPROVED_AND_UNPUBLISHED_FILTER_CONDITION, "and dp.dam_id IS null");
		} else if (ModuleType.TRACK.name().equals(moduleType)) {
			query += " and type = 'TRACK'";
			query = query.replace(APPROVED_AND_UNPUBLISHED_FILTER_JOIN_QUERY, "");
			query = query.replace(APPROVED_AND_UNPUBLISHED_FILTER_CONDITION, "and is_published = false");
		} else if (ModuleType.PLAYBOOK.name().equals(moduleType)) {
			query += " and type = 'PLAYBOOK'";
			query = query.replace(APPROVED_AND_UNPUBLISHED_FILTER_JOIN_QUERY, "");
			query = query.replace(APPROVED_AND_UNPUBLISHED_FILTER_CONDITION, "and is_published = false");
		} else {
			query = query.replace(APPROVED_AND_UNPUBLISHED_FILTER_JOIN_QUERY, "");
			query = query.replace(APPROVED_AND_UNPUBLISHED_FILTER_CONDITION, "");
		}
		return query;
	}

	/** XNFR-884 **/
	@Override
	public void updateApprovalStatusForAssetInDraft(Integer damId, Integer updatedBy,
			ApprovalStatusType approvalStatusType) {
		if (XamplifyUtils.isValidInteger(damId) && XamplifyUtils.isValidInteger(updatedBy)
				&& XamplifyUtils.isValidString(approvalStatusType.name())) {
			String queryString = "update xt_dam set approval_status = cast(:approvalStatus as approval_status_type), approval_status_updated_by = :userId, "
					+ "approval_status_updated_time = :updatedTime where id = :id";
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(XamplifyConstants.USER_ID, updatedBy));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(UPDATED_TIME, new Date()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("approvalStatus", approvalStatusType.name()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(XamplifyConstants.ID, damId));
			hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
		}
	}

	@Override
	public boolean checkIsAssetApproverByTeamMemberId(Integer teamMemberId) {
		Integer companyId = userDao.getCompanyIdByUserId(teamMemberId);
		if (!XamplifyUtils.isValidInteger(teamMemberId) || !XamplifyUtils.isValidInteger(companyId)) {
			return false;
		}

		boolean isApprovalPrivilegeManager = isApprovalPrivilegeManager(teamMemberId);
		boolean isAssetApprover = checkIsAssetApproverByTeamMemberIdAndCompanyId(teamMemberId, companyId);
		return isAssetApprover || isApprovalPrivilegeManager;
	}

	/** XNFR-885 **/
	@Override
	@SuppressWarnings("unchecked")
	public List<Integer> getApprovalReferenceIdsByDamIds(List<Integer> damIds) {
		if (!XamplifyUtils.isNotEmptyList(damIds)) {
			return Collections.emptyList();
		}
		String queryString = "select approval_reference_id from xt_dam where id in (:damIds)";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs().add(new QueryParameterListDTO("damIds", damIds));
		return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

	/** XNFR-885 **/
	@Override
	@SuppressWarnings("unchecked")
	public List<Integer> getReApprovalVersionDamIdsByUserIds(List<Integer> createdByIds, Integer companyId) {
		if (!XamplifyUtils.isNotEmptyList(createdByIds) || !XamplifyUtils.isValidInteger(companyId)) {
			return Collections.emptyList();
		}
		String queryString = "select id from xt_dam where created_by in (:createdByIds) and approval_status = 'CREATED' "
				+ "and company_id = :companyId and approval_reference_id IS NOT NULL";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
				.add(new QueryParameterListDTO(CREATED_BY_IDS, createdByIds));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(COMPANY_ID, companyId));
		return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Integer> getReApprovalVersionDamIdsForMultiSelect(List<Integer> damIds, Integer companyId) {
		if (!XamplifyUtils.isNotEmptyList(damIds) || !XamplifyUtils.isValidInteger(companyId)) {
			return Collections.emptyList();
		}
		String queryString = "select id from xt_dam where id in (:damIds) and approval_status = 'CREATED' "
				+ "and company_id = :companyId and approval_reference_id IS NOT NULL";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs().add(new QueryParameterListDTO("damIds", damIds));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(COMPANY_ID, companyId));
		return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public void deleteVideoTagsAfterReApprovalByNames(List<String> tagNames, Integer videoId) {
		if (XamplifyUtils.isValidInteger(videoId) && XamplifyUtils.isNotEmptyList(tagNames)) {
			try {
				Session session = sessionFactory.getCurrentSession();
				String sqlQueryString = "delete from xt_video_tags where video_id = :videoId and video_tags in (:tagNames)";
				Query query = session.createSQLQuery(sqlQueryString);
				query.setParameter("videoId", videoId);
				query.setParameterList("tagNames", tagNames);
				query.executeUpdate();
			} catch (HibernateException | DamDataAccessException e) {
				throw new DamDataAccessException(e);
			} catch (Exception ex) {
				throw new DamDataAccessException(ex);
			}
		}
	}

	/** XNFR-885 **/
	@Override
	@SuppressWarnings("unchecked")
	public List<Integer> listReApprovalVersionDamIdsByCreatedByIds(List<Integer> createdByIds) {
		if (XamplifyUtils.isNotEmptyList(createdByIds)) {
			String queryString = "select xd.id from xt_dam as xd where xd.created_by in (:createdByIds) and xt.approval_status = 'CREATED' and xd.approval_reference_id is not null";
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
					.add(new QueryParameterListDTO(CREATED_BY_IDS, createdByIds));
			return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
		}
		return Collections.emptyList();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Integer> listWhiteLabeledParentDamIdsForReApproval(List<Integer> damIds, Integer companyId) {
		if (!XamplifyUtils.isNotEmptyList(damIds) || !XamplifyUtils.isValidInteger(companyId)) {
			return Collections.emptyList();
		}
		String queryString = "select id from xt_dam where is_white_labeled_asset_shared_with_partners and "
				+ "approval_reference_id is null and company_id = :companyId and id in (:damIds)";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs().add(new QueryParameterListDTO("damIds", damIds));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(COMPANY_ID, companyId));
		return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Integer> getPendingStateParentVersionDamIdsByCompanyId(Integer companyId) {
		if (XamplifyUtils.isValidInteger(companyId)) {
			String queryString = "select id from xt_dam where approval_status = 'CREATED' and approval_reference_id is null and company_id = :companyId";
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(COMPANY_ID, companyId));
			return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Integer> getPendingStateReApprovalVersionDamIdsByCompanyId(Integer companyId) {
		if (XamplifyUtils.isValidInteger(companyId)) {
			String queryString = "select id from xt_dam where approval_status = 'CREATED' and approval_reference_id is not null and company_id = :companyId";
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(COMPANY_ID, companyId));
			return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public void approvePendingAssetsByCompanyIdAndDamIds(Integer companyId, Integer loggedInUserId,
			List<Integer> damIds) {
		if (XamplifyUtils.isValidInteger(companyId) && XamplifyUtils.isValidInteger(loggedInUserId)
				&& XamplifyUtils.isNotEmptyList(damIds)) {
			String queryString = "UPDATE xt_dam SET approval_status = 'APPROVED', "
					+ "approval_status_updated_by = :userId, approval_status_updated_time = :updatedTime "
					+ "WHERE company_id = :companyId AND approval_status = 'CREATED' and id in (:damIds)";
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(XamplifyConstants.COMPANY_ID, companyId));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(XamplifyConstants.USER_ID, loggedInUserId));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(XamplifyConstants.UPDATED_TIME, new Date()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
					.add(new QueryParameterListDTO("damIds", damIds));
			hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
		}
	}

	@Override
	public ApprovalStatusType getApprovalStatusByString(String approvalStatusInString) {
		if (XamplifyUtils.isValidString(approvalStatusInString)) {
			switch (approvalStatusInString.toUpperCase()) {
			case "APPROVED":
				return ApprovalStatusType.APPROVED;
			case "REJECTED":
				return ApprovalStatusType.REJECTED;
			case DRAFT_STATUS:
				return ApprovalStatusType.DRAFT;
			case "CREATED":
				return ApprovalStatusType.CREATED;
			default:
				return ApprovalStatusType.APPROVED;
			}
		} else {
			return ApprovalStatusType.APPROVED;
		}
	}

	private void addProxyToPDFAssetPath(PendingApprovalDamAndLmsDTO damDto) {
		List<String> availableImageFileTypes = fileUtil.getArrayList(imageFileTypes);
		List<String> availabletextFileTypes = fileUtil.getArrayList(contentPreviewForTextView);
		List<String> availableContentPreviewTypes = fileUtil.getArrayList(contentPreviewSupportedFileFormats);
		damDto.setTextFileType(availabletextFileTypes.contains(damDto.getSlug()));
		damDto.setImageFileType(availableImageFileTypes.contains(damDto.getSlug()));
		damDto.setContentPreviewType(availableContentPreviewTypes.contains(damDto.getSlug()));
		if (XamplifyUtils.isValidString(damDto.getAssetPath())
				&& (damDto.getSlug().equals("pdf") || availableImageFileTypes.contains(damDto.getSlug())
						|| availabletextFileTypes.contains(damDto.getSlug()) || damDto.getSlug().equals("csv")
						|| damDto.getSlug().equals("mp3"))) {
			String baseUrl;
			if (xamplifyUtil.isDev()) {
				baseUrl = HOST;
			} else if (xamplifyUtil.isQA()) {
				baseUrl = devHost;
			} else if (xamplifyUtil.isProduction()) {
				baseUrl = productionHost;
			} else {
				baseUrl = HOST;
			}
			String proxyUrl = baseUrl + XAMPLIFY_PRM + "/api/pdf/proxy?pdfUrl=";
			damDto.setAssetProxyPath(proxyUrl);
		}
	}

}
