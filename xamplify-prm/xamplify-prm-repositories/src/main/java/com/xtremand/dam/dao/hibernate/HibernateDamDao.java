package com.xtremand.dam.dao.hibernate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.xtremand.approve.dao.ApproveDAO;
import com.xtremand.approve.dto.ContentReApprovalDTO;
import com.xtremand.aws.AmazonWebService;
import com.xtremand.aws.FilePathAndThumbnailPath;
import com.xtremand.category.bom.CategoryModule;
import com.xtremand.category.bom.CategoryModuleEnum;
import com.xtremand.category.dao.CategoryDao;
import com.xtremand.comments.dao.CommentDao;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Criteria.OPERATION_NAME;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.dam.bom.ApprovalStatusType;
import com.xtremand.dam.bom.Dam;
import com.xtremand.dam.bom.DamPartner;
import com.xtremand.dam.bom.DamPartnerAnalyticsView;
import com.xtremand.dam.bom.DamPartnerGroupAnalyticsView;
import com.xtremand.dam.bom.DamPartnerGroupMapping;
import com.xtremand.dam.bom.DamPartnerGroupUserMapping;
import com.xtremand.dam.bom.DamPartnerMapping;
import com.xtremand.dam.bom.DamStatusEnum;
import com.xtremand.dam.bom.DamTag;
import com.xtremand.dam.bom.PublishedAssetsView;
import com.xtremand.dam.dao.DamDao;
import com.xtremand.dam.dto.CompanyPartnershipDTO;
import com.xtremand.dam.dto.ContentPreviewDetailsDTO;
import com.xtremand.dam.dto.DamAnalyticsDTO;
import com.xtremand.dam.dto.DamAnalyticsTilesDTO;
import com.xtremand.dam.dto.DamAnalyticsViewDTO;
import com.xtremand.dam.dto.DamBasicInfo;
import com.xtremand.dam.dto.DamDTO;
import com.xtremand.dam.dto.DamDownloadDTO;
import com.xtremand.dam.dto.DamDownloadPostDTO;
import com.xtremand.dam.dto.DamListDTO;
import com.xtremand.dam.dto.DamPartnerCompanyUsersAnalyticsDTO;
import com.xtremand.dam.dto.DamPartnerDetailsDTO;
import com.xtremand.dam.dto.DamPartnerPreviewDTO;
import com.xtremand.dam.dto.DamPatchDTO;
import com.xtremand.dam.dto.DamPostDTO;
import com.xtremand.dam.dto.DamPreviewDTO;
import com.xtremand.dam.dto.DamPublishGetDTO;
import com.xtremand.dam.dto.DamPublishPostDTO;
import com.xtremand.dam.dto.DamUploadPostDTO;
import com.xtremand.dam.dto.PublishedContentIdAndUserListIdDetailsDTO;
import com.xtremand.dam.dto.ShareAssetsResponseDTO;
import com.xtremand.dam.dto.SharedAssetDetailsViewDTO;
import com.xtremand.dam.dto.WhiteLabeledContentSharedByVendorCompaniesDTO;
import com.xtremand.dam.exception.DamDataAccessException;
import com.xtremand.exception.DuplicateEntryException;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.VideoFileDTO;
import com.xtremand.partnership.bom.Partnership;
import com.xtremand.partnership.bom.PartnershipDTO;
import com.xtremand.partnership.dao.PartnershipDAO;
import com.xtremand.tag.bom.Tag;
import com.xtremand.user.bom.User;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.userlist.dao.UserListDAO;
import com.xtremand.util.BadRequestException;
import com.xtremand.util.DateUtils;
import com.xtremand.util.FileUtil;
import com.xtremand.util.GenerateRandomPassword;
import com.xtremand.util.PaginationUtil;
import com.xtremand.util.XamplifyUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.bom.ModuleType;
import com.xtremand.util.dao.HibernateSQLQueryResultUtilDao;
import com.xtremand.util.dao.UtilDao;
import com.xtremand.util.dto.HibernateSQLQueryResultRequestDTO;
import com.xtremand.util.dto.PaginatedDTO;
import com.xtremand.util.dto.PartnerCompanyDTO;
import com.xtremand.util.dto.QueryParameterDTO;
import com.xtremand.util.dto.QueryParameterListDTO;
import com.xtremand.util.dto.SortColumnDTO;
import com.xtremand.util.dto.TeamMemberFilterDTO;
import com.xtremand.util.dto.UserListAndUserId;
import com.xtremand.util.dto.XamplifyConstants;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;
import com.xtremand.video.bom.VideoFile;
import com.xtremand.video.formbeans.VideoFileUploadForm;
import com.xtremand.white.labeled.bom.WhiteLabeledAsset;
import com.xtremand.white.labeled.dao.WhiteLabeledAssetDao;
import com.xtremand.white.labeled.dto.DamVideoDTO;
import com.xtremand.white.labeled.dto.WhiteLabeledContentDTO;

@Repository
@Transactional
public class HibernateDamDao implements DamDao {

	private static final Logger logger = LoggerFactory.getLogger(HibernateDamDao.class);

	private static final String ASSET_NAME = "assetName";

	private static final String HTML_BODY = "htmlBody";

	private static final String ALIAS = "alias";

	private static final String PAGE_SIZE = "pageSize";

	private static final String PAGE_ORIENTATION = "pageOrientation";

	private static final String ASSET_PATH = "assetPath";

	private static final String SEARCH_KEY = "searchKey";

	private static final String DESCRIPTION = "description";

	private static final String THUMBNAIL_PATH = "thumbnailPath";

	private static final String DESC = " desc";

	private static final String CREATED_TIME = "createdTime";

	private static final String PUBLISHED_TIME = "publishedTime";

	private static final String DAM_ID = "damId";

	private static final String UPDATED_BY = "updatedBy";

	private static final String UPDATED_TIME = "updatedTime";

	private static final String DAM_PARTNER_ID = "damPartnerId";

	private static final String PARENT_ID = "parentId";

	private static final String PARTNER_ID = "partnerId";

	private static final String VENDOR_COMPANY_ID = "vendorCompanyId";

	private static final String COMPANY_ID = "companyId";

	private static final String ASSET_FILTER_TYPE_MERGE_TAG = "{asset_filter_type_merge_tag}";

	private static final String EXCLUDE_BEE_TEMPLATE = "{exclude_bee_template}";

	private static final String ASSET_ID = "assetId";

	private static final String CATEGORY_ID = "categoryId";

	private static final String CUSTOM_FILTER_OPTION_QUERY = "{custom_filter_option_query}";

	private static final String CUSTOM_JOIN_CONDITION = "{custom_join_codition}";

	private static final String VIDEO_ID = "videoId";

	private static final String PARTNERSHIP_ID = "partnershipId";

	private static final String USER_ID = "userId";

	private static final String USER_LIST_ID = "userListId";

	private static final String VERSION = "version";

	private static final String RECIVED_FROM_FILTER_QUERY = "{recived_from_filter_query}";

	private static final String APPROVAL_STATUS_TYPE_MERGE_TAG = "{asset_approval_status_type_merge_tag}";

	private static final String UPDATE_APPROVAL_STATUS_FOR_DRAFTS_MERGE_TAG = "{updateApprovalStatusForDraftAssetMergeTag}";

	private static final String ADDED_TO_QUICK_LINKS = "addedToQuickLinks";

	private static final String PARTNER_SIGNED = "SIGNED";

	private static final String PARTNER_NOT_SIGNED = "NOTSIGNED";
	
	private static final String HOST = "http://localhost:8080/";
	
	private static final String XAMPLIFY_PRM = "xamplify-prm-api";

	@Autowired
	private SessionFactory sessionFactory;

	@Value("${listAssetsQueryExcludeBeePDF}")
	private String listAssetsQueryExcludeBeePDF;

	@Value("${listAssetsQuerySuffix}")
	private String listAssetsQuerySuffix;

	@Value("${listAssetsHistoryQuery}")
	private String listAssetsHistoryQuery;

	@Value("${listAssetsSearchQuery}")
	private String listAssetsSearchQuery;

	@Value("${listAssetQuery}")
	private String listAssetQuery;

	@Value("${listSearchQuery}")
	private String listSearchQuery;

	@Value("${createdTimeOrderByQuery}")
	private String createdTimeOrderByQuery;

	@Value("${assetNameOrderByQuery}")
	private String assetNameOrderByQuery;

	@Value("${totalRecords}")
	private String totalRecordsStringKey;

	@Value("${hasHistoryQuery}")
	private String hasHistoryQuery;

	@Value("${updatedTimeOrderByQuery}")
	private String updatedTimeOrderByQuery;

	@Value("${getParentIdByChildParentIdQuery}")
	private String getParentIdByChildParentIdQuery;

	@Value("${downloadAsPdfQuery}")
	private String downloadAsPdfQuery;

	@Value("${downloadPublishedAssetAsPdfQuery}")
	private String downloadPublishedAssetAsPdfQuery;

	@Value("${publishedTimeOrderByQuery}")
	private String publishedTimeOrderByQuery;

	@Value("${listPublishedAssetsQuery}")
	private String listPublishedAssetsQuery;

	@Value("${listPublishedAssetsGroupByQuery}")
	private String listPublishedAssetsGroupByQuery;

	@Value("${getPublishedAssetByIdQuery}")
	private String getPublishedAssetByIdQuery;

	@Value("${listPartnersUtilQuery}")
	private String listPartnersUtilQuery;

	@Value("${listPublishedPartnersQuery}")
	private String listPublishedPartnersQuery;

	@Value("${listPublishedPartnersWithAnalyticsCountQuery}")
	private String listPublishedPartnersWithAnalyticsCountQuery;

	@Value("${listPublishedPartnersWithAnalyticsCountGroupQuery}")
	private String listPublishedPartnersWithAnalyticsCountGroupQuery;

	@Value("${listPartnersUtilSearchQuery}")
	private String listPartnersUtilSearchQuery;

	@Value("${partnershipIdNotInQuery}")
	private String partnershipIdNotInQuery;

	@Value("${partnershipIdInQuery}")
	private String partnershipIdInQuery;

	@Value("${getSharedAssetDetailsByIdQuery}")
	private String getSharedAssetDetailsByIdQuery;

	@Value("${listDamAnalyticsQuery}")
	private String listDamAnalyticsQuery;

	@Value("${listPartnerGroupDamAnalyticsQuery}")
	private String listPartnerGroupDamAnalyticsQuery;

	@Value("${listDamAnalyticsSearchQuery}")
	private String listDamAnalyticsSearchQuery;

	@Value("${listDamAnalyticsSortQuery}")
	private String listDamAnalyticsSortQuery;

	@Value("${showPartnerDetailsWithAnalyticsCountQuery}")
	private String showPartnerDetailsWithAnalyticsCountQuery;

	@Value("${showPartnerDetailsWithAnalyticsCountForPartnerCompanyQuery}")
	private String showPartnerDetailsWithAnalyticsCountForPartnerCompanyQuery;

	@Value("${image.format.types}")
	String imageFormats;

	@Value("${preview.image.format.types}")
	String previewImageFormats;

	@Value("${pdfs.content.path}")
	private String pdfThumbnailPath;

	@Value("${listPublishedAssetsParnterGroupQueryPrefix}")
	private String listPublishedAssetsParnterGroupQueryPrefix;

	@Value("${listPublishedAssetsParnterGroupQuerySuffix}")
	private String listPublishedAssetsParnterGroupQuerySuffix;

	@Value("${listPublishedAssetsParnterGroupVanityQuerySuffix}")
	private String listPublishedAssetsParnterGroupVanityQuerySuffix;

	@Value("${listPublishedAssetsParnterQueryPrefix}")
	private String listPublishedAssetsParnterQueryPrefix;

	@Value("${listPublishedAssetsParnterVanityQuerySuffix}")
	private String listPublishedAssetsParnterVanityQuerySuffix;

	@Value("${listPublishedAssetsParnterQuerySuffix}")
	private String listPublishedAssetsParnterQuerySuffix;

	@Value("${listPublishedAssetsSearchQuery}")
	private String listPublishedAssetsSearchQuery;

	@Value("${listPublishedAssetsSearchQueryWithTags}")
	private String listPublishedAssetsSearchQueryWithTags;

	@Value("${listPublishedAssetsCompleteQuery}")
	private String listPublishedAssetsCompleteQuery;

	@Value("${server_path}")
	String serverPath;

	@Value("${createdTime.property.name}")
	private String createdTimePropertyName;

	@Value("${findAssetsByIdsQueryString}")
	private String findAssetsByIdsQueryString;

	@Value("${media_base_path}")
	String mediaBasePath;

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

	@Value("${damViewAnalyticsForCompaniesQuery}")
	private String damViewAnalyticsForCompaniesQuery;

	@Value("${damViewAnalyticsForGroupsQuery}")
	private String damViewAnalyticsForGroupsQuery;

	@Value("${damPartnerAnalyticsForCompaniesQuery}")
	private String damPartnerAnalyticsForCompaniesQuery;

	@Value("${damPartnerAnalyticsForGroupsQuery}")
	private String damPartnerAnalyticsForGroupsQuery;

	@Value("${damViewAnalyticsForCompaniesSheet2Query}")
	private String damViewAnalyticsForCompaniesSheet2Query;

	@Value("${damViewAnalyticsForGroupsSheet2Query}")
	private String damViewAnalyticsForGroupsSheet2Query;

	@Autowired
	private PaginationUtil paginationUtil;

	@Autowired
	private XamplifyUtil xamplifyUtil;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private PartnershipDAO partnershipDao;

	@Autowired
	private UserListDAO userListDao;

	@Autowired
	private UtilDao utilDao;

	@Autowired
	private FileUtil fileUtil;

	@Autowired
	private CategoryDao categoryDao;

	/***** XNFR-255 *****/
	@Autowired
	private WhiteLabeledAssetDao whiteLabeledAssetDao;

	@Autowired
	private AmazonWebService amazonWebService;

	@Autowired
	private HibernateSQLQueryResultUtilDao hibernateSQLQueryResultUtilDao;

	@Autowired
	private CommentDao commentDao;

	@Autowired
	private ApproveDAO approveDao;

	@Override
	public void save(Object clazz) {
		try {
			sessionFactory.getCurrentSession().save(clazz);
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}

	}

	@Override
	public Map<String, Object> listAssets(Pagination pagination) {
		try {
			return getAssetsOrAssetsHistoryDtos(pagination, listAssetQuery, listSearchQuery, false);
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getAssetsOrAssetsHistoryDtos(Pagination pagination, String listQuery,
			String searchQuery, boolean isHistory) {
		boolean isFilterApplied = StringUtils.hasText(pagination.getFilterBy());
		if (isFilterApplied) {
			listQuery = listQuery.replace(ASSET_FILTER_TYPE_MERGE_TAG, " and d.asset_type = :assetType ")
					.replace(RECIVED_FROM_FILTER_QUERY, "");
		} else if (pagination.isCustomFilterOption()) {
			/*** XNFR-409 ****/
			String additionalQuery = getFilterParamertesString(pagination, true);
			if (additionalQuery.contains("e.shared_by_vendor_company_id")) {
				String sharedByVendorCompanyIdQuery = " left join xt_white_labeled_assets e on d.id = e.received_white_labeled_asset_id  ";
				listQuery = listQuery.replace(RECIVED_FROM_FILTER_QUERY, sharedByVendorCompanyIdQuery);
			} else {
				listQuery = listQuery.replace(RECIVED_FROM_FILTER_QUERY, "");
			}
			listQuery = listQuery.replace(ASSET_FILTER_TYPE_MERGE_TAG, additionalQuery);
			/**** XNFR-409 ***/
		} else {
			listQuery = listQuery.replace(ASSET_FILTER_TYPE_MERGE_TAG, "").replace(RECIVED_FROM_FILTER_QUERY, "");
		}

		/** XNFR-813 **/
		listQuery = applyApprovalStatusFilter(pagination, listQuery);

		HashMap<String, Object> map = new HashMap<>();
		String finalQueryString = "";
		String sortQueryString = getSelectedSortOptionForAssets(pagination);
		String searchKey = pagination.getSearchKey();
		boolean hasSearchKey = StringUtils.hasText(searchKey);
		String groupByQueryString = " group by d.id,u.user_id,cat.name ";
		String groupByAndSortQueryString = "";
		if (isHistory) {
			groupByAndSortQueryString = groupByQueryString + " " + sortQueryString;
		} else {
			groupByAndSortQueryString = sortQueryString;
		}

		if (hasSearchKey) {
			/*** XNFR-758 ***/
			searchKey = XamplifyUtils.addBackSlashToSpecialCharacters(searchKey);
			searchKey = XamplifyUtils.escapeSingleQuotesForSearchQuery(searchKey.replaceAll("['\"]", "_"));
			/*** XNFR-758 ***/
			if (isHistory) {
				finalQueryString = listQuery + " " + searchQuery.replace(SEARCH_KEY, searchKey) + " "
						+ groupByAndSortQueryString;
			} else {
				finalQueryString = listQuery.replace("{list_search_query}", searchQuery.replace(SEARCH_KEY, searchKey))
						+ " " + groupByAndSortQueryString;
			}
		} else {
			if (!isHistory) {
				listQuery = listQuery.replace("{list_search_query}", "");
			}
			finalQueryString = listQuery + " " + groupByAndSortQueryString;
		}
		finalQueryString = finalQueryString.replace("{partner_query}", "and d.created_for_company is null");
		finalQueryString = finalQueryString.replace("{exclude_bee_template}", "");

		Session session = sessionFactory.getCurrentSession();
		/******** XNFR-169 ******/
		finalQueryString = addCategoryFilter(pagination, finalQueryString);
		finalQueryString = addNewTileConditions(pagination, finalQueryString);

		SQLQuery query = session.createSQLQuery(finalQueryString);
		
		query.setParameter(COMPANY_ID, pagination.getCompanyId());
		if (isHistory) {
			query.setParameter("parentDamId", pagination.getCampaignId());
		}
		if (isFilterApplied) {
			query.setParameter("assetType", pagination.getFilterBy());
		}
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		List<DamListDTO> assets = query.setResultTransformer(Transformers.aliasToBean(DamListDTO.class)).list();
		List<DamListDTO> updatedAssets = new ArrayList<>();
		boolean isSuperVisorOrAdmin = utilDao.isSuperVisorOrAdmin(pagination.getUserId());
		iterateAssetsAndSetProperties(pagination, isHistory, session, assets, updatedAssets, isSuperVisorOrAdmin);
		/***** XNFR-930 ***/
		updatedAssets.forEach(item -> {
			if (XamplifyUtils.isValidString(item.getAssetPath())) {
				String updatedAssetPath = xamplifyUtil.replaceS3WithCloudfrontViceVersa(item.getAssetPath());
				item.setCdnAssetPath(updatedAssetPath);

			}
			if (XamplifyUtils.isValidString(item.getThumbnailPath())) {
				String updatedThumbnailPathPath = xamplifyUtil
						.replaceS3WithCloudfrontViceVersa(item.getThumbnailPath());
				item.setCdnThumbnailPath(updatedThumbnailPathPath);
			}
		});
		/***** XNFR-930 ***/
		map.put(totalRecordsStringKey, totalRecords);
		map.put("assets", updatedAssets);
		return map;
	}

	private void iterateAssetsAndSetProperties(Pagination pagination, boolean isHistory, Session session,
			List<DamListDTO> assets, List<DamListDTO> updatedAssets, boolean isSuperVisorOrAdmin) {
		List<Integer> listAllApprovers = approveDao.findAllApproversByModuleTypeAndCompanyId(pagination.getCompanyId(),
				ModuleType.DAM.name());
		for (DamListDTO asset : assets) {
			DamListDTO updatedAsset = new DamListDTO();
			VideoFileDTO videoFileDTO = new VideoFileDTO();
			BeanUtils.copyProperties(asset, updatedAsset);
			String createdTimeInUTCString = DateUtils.getUtcString(asset.getCreatedTime());
			updatedAsset.setCreatedDateInUTCString(createdTimeInUTCString);
			if (!isHistory) {
				SQLQuery historyQuery = sessionFactory.getCurrentSession().createSQLQuery(hasHistoryQuery);
				historyQuery.setParameter("id", asset.getId());
				updatedAsset.setHistory((boolean) historyQuery.uniqueResult());
			}
			updatedAsset.setPublished((boolean) session.createSQLQuery(
					"select case when count(*)>0 then true else false end from xt_dam_partner where dam_id="
							+ asset.getId())
					.uniqueResult());
			if (asset.isBeeTemplate()) {
				updatedAsset.setShowPreviewIcon(true);
			} else {
				boolean imageType = (xamplifyUtil.convertStringToArrayListWithCommaSeperator(previewImageFormats)
						.indexOf(asset.getAssetType()) > -1) || (fileUtil.isVideoFileByType(asset.getAssetType()));
				updatedAsset.setShowPreviewIcon(imageType);
			}
			boolean hasAccess = isSuperVisorOrAdmin || pagination.getUserId().equals(asset.getCreatedBy());
			updatedAsset.setEdit(hasAccess);
			updatedAsset.setDelete(hasAccess);
			String thumbnailPath = asset.getVideoId() != null ? serverPath + asset.getThumbnailPath()
					: asset.getThumbnailPath();
			String assetPath = asset.getVideoId() != null ? serverPath + asset.getAssetPath() : asset.getAssetPath();
			setVideoFileDTOProperties(asset, updatedAsset, videoFileDTO, thumbnailPath, assetPath);
			updatedAsset.setThumbnailPath(thumbnailPath);
			updatedAsset.setAssetPath(assetPath);
			if (XamplifyUtils.isValidString(updatedAsset.getAssetPath())) {
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
				updatedAsset.setAssetProxyPath(proxyUrl);
			}
			/********** XNFR-255 *******/
			WhiteLabeledContentDTO whiteLabeledContentDTO = whiteLabeledAssetDao
					.findSharedByVendorCompanyNameByAssetId(asset.getId());
			if (whiteLabeledContentDTO != null) {
				updatedAsset.setWhiteLabeledAssetReceivedFromVendor(true);
				updatedAsset.setWhiteLabeledAssetSharedByVendorCompanyName(
						whiteLabeledContentDTO.getWhiteLabeledContentSharedByVendorCompanyName());
			}

			/** XNFR-781 **/
			if (listAllApprovers.contains(asset.getCreatedBy())) {
				updatedAsset.setCreatedByAnyApprovalManagerOrApprover(true);
			}

			/** XNFR-885 **/
			updatedAsset.setHasAnyReApprovalVersion((boolean) session.createSQLQuery(
					"select case when count(*) > 0 then true else false end from xt_dam where approval_reference_id = "
							+ asset.getId())
					.uniqueResult());
			/** XNFR-885 **/
			if (XamplifyUtils.isValidInteger(asset.getApprovalReferenceId())) {
				updatedAsset.setParentAssetName((String) session
						.createSQLQuery(
								"select trim(asset_name) from xt_dam where id = " + asset.getApprovalReferenceId())
						.uniqueResult());
			}
			updateAssetDetailsByAssetType(asset, updatedAsset);

			/********** XNFR-255 *******/
			updatedAssets.add(updatedAsset);
		}
	}

	private void updateAssetDetailsByAssetType(DamListDTO asset, DamListDTO updatedAsset) {
		List<String> availableImageFileTypes = fileUtil.getArrayList(imageFileTypes);
		List<String> availabletextFileTypes = fileUtil.getArrayList(contentPreviewForTextView);
		List<String> availableContentPreviewTypes = fileUtil.getArrayList(contentPreviewSupportedFileFormats);
		updatedAsset.setTextFileType(availabletextFileTypes.contains(asset.getAssetType()));
		updatedAsset.setImageFileType(availableImageFileTypes.contains(asset.getAssetType()));
		updatedAsset.setContentPreviewType(availableContentPreviewTypes.contains(asset.getAssetType()));
	}

	private void setVideoFileDTOProperties(DamListDTO asset, DamListDTO updatedAsset, VideoFileDTO videoFileDTO,
			String thumbnailPath, String assetPath) {
		if (asset.getVideoId() != null) {
			videoFileDTO.setId(asset.getVideoId());
			videoFileDTO.setTitle(asset.getAssetName());
			videoFileDTO.setImagePath(thumbnailPath);
			videoFileDTO.setVideoLength(asset.getVideoLength());
			videoFileDTO.setViews(asset.getTotalViews());
			videoFileDTO.setViewBy(asset.getViewBy());
			videoFileDTO.setUploadedUserName(asset.getDisplayName());
			videoFileDTO.setAlias(asset.getAlias());
			videoFileDTO.setProcessed(asset.isProcessed());
			videoFileDTO.setVideoPath(assetPath);
			updatedAsset.setVideoFileDTO(videoFileDTO);
		}
	}

	/******** XNFR-169 ******/
	/***** XNFR-409 ****/
	private String getFilterParamertesString(Pagination pagination, boolean isVendor) {
		String additionalQuery = "";
		com.xtremand.common.bom.Criteria[] criterias = pagination.getCriterias();
		if (pagination.isFilterOptionEnable() && ArrayUtils.isNotEmpty(criterias)
				&& !"Field Name*".equals(criterias[0].getProperty())) {
			StringBuilder sb = new StringBuilder();
			sb.append(" and (");
			for (int i = 0; i < criterias.length; i++) {
				com.xtremand.common.bom.Criteria criteriaQuery = criterias[i];
				criteriaQuery.setOperationName(
						com.xtremand.common.bom.Criteria.getOperationNameEnum(criteriaQuery.getOperation()));
				if (i != 0) {
					sb.append(" and ");
				}
				String criteriaPropertyValue = " ";
				if (criteriaQuery.getValue1() != null) {
					criteriaPropertyValue = String.valueOf(criteriaQuery.getValue1()).toLowerCase().trim();

				}
				appendQueryString(isVendor, sb, criteriaQuery, criteriaPropertyValue);
			}
			sb.append(" ) ");
			additionalQuery = sb.toString();
		}
		if (pagination.getFromDateFilter() != null && pagination.getToDateFilter() != null
				&& pagination.isDateFilterOpionEnable()) {
			if (isVendor) {
				additionalQuery = additionalQuery + " and  d.created_time  between  TO_TIMESTAMP('"
						+ pagination.getFromDateFilter() + "', 'Dy Mon DD HH24:MI:SS ZZZ YYYY') and TO_TIMESTAMP('"
						+ pagination.getToDateFilter() + "', 'Dy Mon DD HH24:MI:SS ZZZ YYYY')";
			} else {
				additionalQuery = additionalQuery + " and  d.published_time  between  TO_TIMESTAMP('"
						+ pagination.getFromDateFilter() + "', 'Dy Mon DD HH24:MI:SS ZZZ YYYY') and TO_TIMESTAMP('"
						+ pagination.getToDateFilter() + "', 'Dy Mon DD HH24:MI:SS ZZZ YYYY')";
			}
		}
		return additionalQuery;
	}

	private void appendQueryString(boolean isVendor, StringBuilder sb, com.xtremand.common.bom.Criteria criteriaQuery,
			String criteriaPropertyValue) {
		if (criteriaPropertyValue != null) {
			criteriaPropertyValue = criteriaPropertyValue.replace("'", "''");
			if (criteriaQuery.getProperty().equalsIgnoreCase("assetsname")
					&& criteriaQuery.getOperationName() == OPERATION_NAME.eq) {
				sb.append(" lower(d.asset_name)='" + criteriaPropertyValue + "' ");
			} else if (criteriaQuery.getProperty().equalsIgnoreCase("assetsname")
					&& criteriaQuery.getOperationName() == OPERATION_NAME.like) {
				sb.append(" lower(d.asset_name) like'%" + criteriaPropertyValue + "%' ");
			} else if (criteriaQuery.getProperty().equalsIgnoreCase("folder")
					&& criteriaQuery.getOperationName() == OPERATION_NAME.like) {
				appendFolderLikeQueryString(isVendor, sb, criteriaPropertyValue);
			} else if (criteriaQuery.getProperty().equalsIgnoreCase("folder")
					&& criteriaQuery.getOperationName() == OPERATION_NAME.eq) {
				appendFolderEqualsQueryString(isVendor, sb, criteriaPropertyValue);
			} else if (criteriaQuery.getProperty().equals("type")
					&& criteriaQuery.getOperationName() == OPERATION_NAME.eq) {
				sb.append(" LOWER(d.asset_type) = '" + criteriaPropertyValue + "' ");
			} else if (criteriaQuery.getProperty().equals("tags")
					&& criteriaQuery.getOperationName() == OPERATION_NAME.like) {
				String tagsString = "";
				String[] tagsList = criteriaPropertyValue.split(",");
				for (int i = 0; i < tagsList.length; i++) {
					String tag = tagsList[i];
					if (i == 0) {
						tagsString = tagsString + " LOWER(t.tag_names_string) LIKE '%" + tag + "%' ";
					} else {
						tagsString = tagsString + " and LOWER(t.tag_names_string) LIKE '%" + tag + "%' ";
					}
				}

				sb.append(tagsString);
			} else if (criteriaQuery.getProperty().equals("tags")
					&& criteriaQuery.getOperationName() == OPERATION_NAME.eq) {
				sb.append(" LOWER(t.tag_names_string) = '" + criteriaPropertyValue + "' ");
			} else if (criteriaQuery.getProperty().equals("from")
					&& criteriaQuery.getOperationName() == OPERATION_NAME.eq) {
				if (isVendor) {
					sb.append("  e.shared_by_vendor_company_id  = " + criteriaPropertyValue + " ");
				} else {
					sb.append(" d.vendor_company_id = " + criteriaPropertyValue + " ");
				}
			} else if ((criteriaQuery.getProperty().equals("createdby")
					|| criteriaQuery.getProperty().equals("publishedby"))
					&& criteriaQuery.getOperationName() == OPERATION_NAME.like) {
				appendCreatedByOrPublishedByLikeQueryString(sb, criteriaPropertyValue);
			} else if ((criteriaQuery.getProperty().equals("createdby")
					|| criteriaQuery.getProperty().equals("publishedby"))
					&& criteriaQuery.getOperationName() == OPERATION_NAME.eq) {
				appendCreatedByOrPublishedByEqualsQueryString(sb, criteriaPropertyValue);
			}
		}
	}

	private void appendFolderEqualsQueryString(boolean isVendor, StringBuilder sb, String criteriaPropertyValue) {
		if (isVendor) {
			sb.append(" LOWER(cast(cat.name as text)) = '" + criteriaPropertyValue + "' ");
		} else {
			sb.append(" LOWER(d.name) = '" + criteriaPropertyValue + "' ");
		}
	}

	private void appendFolderLikeQueryString(boolean isVendor, StringBuilder sb, String criteriaPropertyValue) {
		if (isVendor) {
			sb.append(" LOWER(cast(cat.name as text)) like'%" + criteriaPropertyValue + "%' ");
		} else {
			sb.append(" LOWER(d.name) like'%" + criteriaPropertyValue + "%' ");
		}
	}

	private void appendCreatedByOrPublishedByEqualsQueryString(StringBuilder sb, String criteriaPropertyValue) {
		sb.append(" (LOWER(d.firstname) = '" + criteriaPropertyValue + "' " + " OR LOWER(d.lastname) = '"
				+ criteriaPropertyValue + "' " + " OR LOWER(d.email_id) = '" + criteriaPropertyValue + "' "
				+ "OR LOWER(d.firstname|| ' ' || d.lastname ) = '" + criteriaPropertyValue + "') ");
	}

	private void appendCreatedByOrPublishedByLikeQueryString(StringBuilder sb, String criteriaPropertyValue) {
		sb.append(" (LOWER(d.firstname) like '%" + criteriaPropertyValue + "%' " + " OR LOWER(d.lastname) like '%"
				+ criteriaPropertyValue + "%' " + " OR LOWER(d.email_id) like '%" + criteriaPropertyValue + "%' "
				+ "OR LOWER(d.firstname|| ' ' || d.lastname ) like '%" + criteriaPropertyValue + "%') ");
	}

	/******* XNFR-409 ******/
	private String addCategoryFilter(Pagination pagination, String finalQueryString) {
		if (pagination.getCategoryId() != null && pagination.getCategoryId() > 0) {
			finalQueryString = finalQueryString.replace("{category_filter}",
					" and  cat.id =" + pagination.getCategoryId());
		} else {
			finalQueryString = finalQueryString.replace("{category_filter}", "");
		}
		return finalQueryString;
	}

	private String getSelectedSortOptionForAssets(Pagination pagination) {
		String sortOptionQueryString = "";
		if (StringUtils.hasText(pagination.getSortcolumn())) {
			if (ASSET_NAME.equals(pagination.getSortcolumn())) {
				sortOptionQueryString += assetNameOrderByQuery + " " + pagination.getSortingOrder();
			} else if (CREATED_TIME.equals(pagination.getSortcolumn())) {
				sortOptionQueryString += createdTimeOrderByQuery + " " + pagination.getSortingOrder();
			}
		} else {
			sortOptionQueryString += createdTimeOrderByQuery + DESC;
		}
		return sortOptionQueryString;
	}

	private String getSelectedSortOptionForPublishedAssets(Pagination pagination) {
		String sortOptionQueryString = "";
		if (StringUtils.hasText(pagination.getSortcolumn())) {
			if (ASSET_NAME.equals(pagination.getSortcolumn())) {
				sortOptionQueryString += " order by \"assetName\" " + " " + pagination.getSortingOrder();
			} else if (PUBLISHED_TIME.equals(pagination.getSortcolumn())) {
				sortOptionQueryString += " order by \"publishedTime\"  " + " " + pagination.getSortingOrder();
			}
		} else {
			sortOptionQueryString += " order by \"publishedTime\"  " + " " + DESC;
		}
		return sortOptionQueryString;
	}

	@Override
	public String getHtmlBodyByAlias(String alias) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Criteria criteria = session.createCriteria(Dam.class);
			criteria.add(Restrictions.eq(ALIAS, alias));
			criteria.setProjection(Projections.projectionList().add(Projections.property(HTML_BODY), HTML_BODY))
					.setResultTransformer(Transformers.aliasToBean(Dam.class));
			Dam dam = (Dam) criteria.uniqueResult();
			if (dam != null) {
				return dam.getHtmlBody();
			} else {
				return "";
			}
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public Dam getById(Integer id) {
		try {
			Session session = sessionFactory.getCurrentSession();
			return getByIdAndSession(id, session);
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	private Dam getByIdAndSession(Integer id, Session session) {
		org.hibernate.Criteria criteria = session.createCriteria(Dam.class);
		criteria.add(Restrictions.eq("id", id));
		return (Dam) criteria.uniqueResult();
	}

	@Override
	public Map<String, Object> listAssetsHistory(Pagination pagination) {
		try {
			return getAssetsOrAssetsHistoryDtos(pagination, listAssetsHistoryQuery, listAssetsSearchQuery, true);
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public Integer getParentIdByChildParentId(Integer id) {
		try {
			Session session = sessionFactory.getCurrentSession();
			return (Integer) session.createSQLQuery(getParentIdByChildParentIdQuery).setParameter("id", id)
					.uniqueResult();
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public String getHtmlBodyById(Integer id) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Criteria criteria = session.createCriteria(Dam.class);
			criteria.add(Restrictions.eq("id", id));
			criteria.setProjection(Projections.projectionList().add(Projections.property(HTML_BODY), HTML_BODY))
					.setResultTransformer(Transformers.aliasToBean(Dam.class));
			Dam dam = (Dam) criteria.uniqueResult();
			if (dam != null) {
				return dam.getHtmlBody();
			} else {
				return "";
			}
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public DamDownloadDTO getDownloadContent(String alias, boolean isPartnerDownloading) {
		try {
			String queryString = downloadAsPdfQuery;
			if (isPartnerDownloading) {
				queryString = downloadPublishedAssetAsPdfQuery;
			}
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(queryString);
			query.setParameter(ALIAS, alias);
			return (DamDownloadDTO) query.setResultTransformer(Transformers.aliasToBean(DamDownloadDTO.class))
					.uniqueResult();
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public void publish(DamPublishPostDTO damPublishPostDto, Dam dam) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Integer loggedInUserCompanyId = userDao.getCompanyIdByUserId(damPublishPostDto.getPublishedBy());
			Set<Integer> uniquePartnerIds = new HashSet<>();
			Set<Integer> uniquePartnerShipIds = new HashSet<>();
			List<Integer> partnerIds = new ArrayList<>();
			List<Integer> partnershipIdsFromDamPartnerByDamId = new ArrayList<>();
			List<UserListAndUserId> userListAndUserIdDtos = new ArrayList<>();
			if (damPublishPostDto.isPartnerGroupSelected()) {
				userListAndUserIdDtos = findPartnersAndPartnershipIds(damPublishPostDto, loggedInUserCompanyId,
						uniquePartnerIds, uniquePartnerShipIds, partnerIds);
				partnershipIdsFromDamPartnerByDamId.addAll(findPartnershipIdsForPartnerGroupOptionByDamId(dam.getId()));
			} else {
				findPartnershipIds(damPublishPostDto, loggedInUserCompanyId, uniquePartnerIds, uniquePartnerShipIds);
				partnershipIdsFromDamPartnerByDamId
						.addAll(findPartnershipIdsForPartnerCompaniesOptionByDamId(dam.getId()));
			}
			iterateAndSave(damPublishPostDto, dam, session, uniquePartnerIds, loggedInUserCompanyId,
					uniquePartnerShipIds, userListAndUserIdDtos, partnershipIdsFromDamPartnerByDamId);
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	private void findPartnershipIds(DamPublishPostDTO damPublishPostDto, Integer loggedInUserCompanyId,
			Set<Integer> uniquePartnerIds, Set<Integer> uniquePartnerShipIds) {
		uniquePartnerIds.addAll(XamplifyUtils.convertListToSetElements(damPublishPostDto.getPartnerIds()));
		List<Integer> partnershipIds = partnershipDao
				.getPartnershipIdsByPartnerCompanyUserIds(damPublishPostDto.getPartnerIds(), loggedInUserCompanyId);
		uniquePartnerShipIds.addAll(XamplifyUtils.convertListToSetElements(partnershipIds));
	}

	private List<UserListAndUserId> findPartnersAndPartnershipIds(DamPublishPostDTO damPublishPostDto,
			Integer loggedInUserCompanyId, Set<Integer> uniquePartnerIds, Set<Integer> uniquePartnerShipIds,
			List<Integer> partnerIds) {
		List<UserListAndUserId> userListAndUserIdDtos;
		userListAndUserIdDtos = userListDao.findUserIdsAndUserListIds(damPublishPostDto.getPartnerGroupIds());
		partnerIds
				.addAll(userListAndUserIdDtos.stream().map(UserListAndUserId::getUserId).collect(Collectors.toList()));
		uniquePartnerIds.addAll(XamplifyUtils.convertListToSetElements(partnerIds));
		List<Integer> partnershipIds = partnershipDao.findPartnershipIdsByPartnerIdsAndVendorCompanyId(partnerIds,
				loggedInUserCompanyId);
		uniquePartnerShipIds.addAll(XamplifyUtils.convertListToSetElements(partnershipIds));
		return userListAndUserIdDtos;
	}

	private void iterateAndSave(DamPublishPostDTO damPublishPostDto, Dam dam, Session session,
			Set<Integer> uniquePartnerIds, Integer loggedInUserCompanyId, Set<Integer> uniquePartnerShipIds,
			List<UserListAndUserId> userListAndUserIdDtos, List<Integer> partnershipIdsFromDamPartnerByDamId) {
		int i = 0;
		List<Integer> updatedPartnerIds = new ArrayList<>();
		for (Integer partnershipId : uniquePartnerShipIds) {
			DamPartner damPartner = new DamPartner();
			findDamPartnerIdOrInsertIntoDamPartner(damPublishPostDto, dam, session, partnershipIdsFromDamPartnerByDamId,
					partnershipId, damPartner);
			insertIntoDamPartnerOrDamPartnerGroupMapping(damPublishPostDto, session, uniquePartnerIds,
					loggedInUserCompanyId, userListAndUserIdDtos, partnershipId, damPartner, updatedPartnerIds);
			if (i % 30 == 0) {
				session.flush();
				session.clear();
			}
			i++;
		}
	}

	private void findDamPartnerIdOrInsertIntoDamPartner(DamPublishPostDTO damPublishPostDto, Dam dam, Session session,
			List<Integer> partnershipIdsByDamId, Integer partnershipId, DamPartner damPartner) {
		if (partnershipIdsByDamId != null && partnershipIdsByDamId.indexOf(partnershipId) > -1) {
			Integer damPartnerId = (Integer) session.createSQLQuery(" select id from xt_dam_partner where dam_id="
					+ dam.getId() + " and partnership_id=" + partnershipId).uniqueResult();
			damPartner.setId(damPartnerId);
		} else {
			insertIntoDamPartner(damPublishPostDto, dam, session, partnershipId, damPartner);
		}
	}

	private void insertIntoDamPartnerOrDamPartnerGroupMapping(DamPublishPostDTO damPublishPostDto, Session session,
			Set<Integer> uniquePartnerIds, Integer loggedInUserCompanyId, List<UserListAndUserId> userListAndUserIdDtos,
			Integer partnershipId, DamPartner damPartner, List<Integer> updatedPartnerIds) {
		if (damPublishPostDto.isPartnerGroupSelected()) {
			insertIntoDamPartnerGroupMapping(session, loggedInUserCompanyId, partnershipId, userListAndUserIdDtos,
					damPartner, damPublishPostDto, updatedPartnerIds);
		} else {
			saveDamPartnerMapping(session, uniquePartnerIds, loggedInUserCompanyId, partnershipId, damPartner,
					damPublishPostDto, updatedPartnerIds);
		}
	}

	private void insertIntoDamPartner(DamPublishPostDTO damPublishPostDto, Dam dam, Session session,
			Integer partnershipId, DamPartner damPartner) {
		Partnership partnership = new Partnership();
		partnership.setId(partnershipId);
		damPartner.setPartnership(partnership);
		damPartner.setDam(dam);
		GenerateRandomPassword password = new GenerateRandomPassword();
		damPartner.setAlias(password.getPassword());
		damPartner.setJsonBody(dam.getJsonBody());
		damPartner.setHtmlBody(dam.getHtmlBody());
		damPartner.setPartnerGroupSelected(damPublishPostDto.isPartnerGroupSelected());
		damPartner.setPublishedTime(new Date());
		damPartner.setPublishedBy(damPublishPostDto.getPublishedBy());
		session.save(damPartner);
	}

	private void insertIntoDamPartnerGroupMapping(Session session, Integer loggedInUserCompanyId, Integer partnershipId,
			List<UserListAndUserId> userListAndUserIdDtos, DamPartner damPartner, DamPublishPostDTO damPublishPostDto,
			List<Integer> updatedPartnerIds) {
		for (UserListAndUserId userListAndUserId : userListAndUserIdDtos) {
			Integer userId = userListAndUserId.getUserId();
			Integer userListId = userListAndUserId.getUserListId();
			Integer partnerShipIdByPartnerId = partnershipDao.findPartnershipIdByPartnerIdAndVendorCompanyId(userId,
					loggedInUserCompanyId);
			boolean isRowExistsInDamPartnerGroupMapping = isRowExistsInDamPartnerGroupMapping(session,
					damPartner.getId(), userListId, userId);
			if (partnerShipIdByPartnerId != null && partnerShipIdByPartnerId.equals(partnershipId)
					&& !isRowExistsInDamPartnerGroupMapping) {
				DamPartnerGroupMapping damPartnerGroupMapping = new DamPartnerGroupMapping();
				damPartnerGroupMapping.setDamPartner(damPartner);
				damPartnerGroupMapping.setUserId(userId);
				damPartnerGroupMapping.setUserListId(userListId);
				damPartnerGroupMapping.setCreatedTime(new Date());
				updatedPartnerIds.add(userId);
				Integer damPartnerGroupId = (Integer) session.save(damPartnerGroupMapping);

				insertIntoDamPartnerGroupUserMapping(session, partnershipId, userId, damPartnerGroupId);
			}

		}
		damPublishPostDto.setUpdatedPartnerIds(updatedPartnerIds);
	}

	private void saveDamPartnerMapping(Session session, Set<Integer> partnerIds, Integer loggedInUserCompanyId,
			int partnershipId, DamPartner damPartner, DamPublishPostDTO damPublishPostDTO,
			List<Integer> updatedPartnerIds) {
		for (Integer partnerId : partnerIds) {
			Integer partnershipIdByPartnerId = partnershipDao.getPartnershipIdByPartnerCompanyUserId(partnerId,
					loggedInUserCompanyId);
			boolean isRowExistsInDamPartnerMapping = isAssetPublishedByDamPartnerIdAndPartnerId(session,
					damPartner.getId(), partnerId);
			if (partnershipIdByPartnerId != null && partnershipIdByPartnerId.equals(partnershipId)
					&& !isRowExistsInDamPartnerMapping) {
				DamPartnerMapping damPartnerMapping = new DamPartnerMapping();
				damPartnerMapping.setCreatedTime(new Date());
				damPartnerMapping.setDamPartner(damPartner);
				damPartnerMapping.setPartnerId(partnerId);
				updatedPartnerIds.add(partnerId);
				session.save(damPartnerMapping);
			}
		}
		damPublishPostDTO.setUpdatedPartnerIds(updatedPartnerIds);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> listPublishedPartnershipIdsByDamId(Integer damId) {
		try {
			SQLQuery query = sessionFactory.getCurrentSession()
					.createSQLQuery("select distinct partnership_id from xt_dam_partner where dam_id = :damId");
			query.setParameter(DAM_ID, damId);
			return query.list();
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public Map<String, Object> listPublishedAssets(Pagination pagination) {
		try {
			HashMap<String, Object> map = new HashMap<>();
			String listPublishedAssets = listPublishedAssetsCompleteQuery;
			boolean isFilterApplied = StringUtils.hasText(pagination.getFilterBy());
			if (isFilterApplied) {
				listPublishedAssets = listPublishedAssets
						.replace(ASSET_FILTER_TYPE_MERGE_TAG, " and d.asset_type = :assetType ")
						.replace(CUSTOM_JOIN_CONDITION, "LEFT").replace(CUSTOM_FILTER_OPTION_QUERY, "");
			} else if (pagination.isCustomFilterOption()) {
				/*** XNFR-409 ****/
				String additionalQuery = getFilterParamertesString(pagination, false);
				listPublishedAssets = listPublishedAssets.replace(CUSTOM_FILTER_OPTION_QUERY, additionalQuery)
						.replace(ASSET_FILTER_TYPE_MERGE_TAG, "").replace(CUSTOM_JOIN_CONDITION, "INNER");
				/**** XNFR-409 ***/
			} else {
				listPublishedAssets = listPublishedAssets.replace(ASSET_FILTER_TYPE_MERGE_TAG, "")
						.replace(CUSTOM_FILTER_OPTION_QUERY, "").replace(CUSTOM_JOIN_CONDITION, "LEFT");
			}
			String finalQueryString = "";
			if (pagination.isVanityUrlFilterApplicable()) {
				listPublishedAssets = listPublishedAssets
						.replace("{listPublishedAssetsParnterGroupVanityQuerySuffix}",
								listPublishedAssetsParnterGroupVanityQuerySuffix)
						.replace("{listPublishedAssetsParnterGroupQuerySuffix}", "")
						.replace("{listPublishedAssetsParnterVanityQuerySuffix}",
								listPublishedAssetsParnterVanityQuerySuffix)
						.replace("{listPublishedAssetsParnterQuerySuffix}", "");
			} else {
				listPublishedAssets = listPublishedAssets
						.replace("{listPublishedAssetsParnterGroupVanityQuerySuffix}", "")
						.replace("{listPublishedAssetsParnterGroupQuerySuffix}",
								listPublishedAssetsParnterGroupQuerySuffix)
						.replace("{listPublishedAssetsParnterVanityQuerySuffix}", "")
						.replace("{listPublishedAssetsParnterQuerySuffix}", listPublishedAssetsParnterQuerySuffix);
			}
			String sortQueryString = getSelectedSortOptionForPublishedAssets(pagination);
			String searchKey = pagination.getSearchKey();
			boolean hasSearchKey = StringUtils.hasText(searchKey);
			if (hasSearchKey) {
				/***** XNFR-758 ***/
				searchKey = XamplifyUtils.escapeSingleQuotesForSearchQuery(searchKey);
				searchKey = XamplifyUtils.addBackSlashToSpecialCharacters(searchKey);
				/***** XNFR-758 ***/
				finalQueryString = listPublishedAssets.replace("{listPublishedAssetsSearchQueryWithTags}",
						listPublishedAssetsSearchQueryWithTags.replace(SEARCH_KEY, searchKey)) + " " + sortQueryString;

			} else {
				finalQueryString = listPublishedAssets.replace("{listPublishedAssetsSearchQueryWithTags}", "") + " "
						+ sortQueryString;
			}
			/***** XNFR-169 *****/
			finalQueryString = addCategoryFilter(pagination, finalQueryString);

			finalQueryString = addNewTileConditionsAccessSharedAssets(pagination, finalQueryString);

			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(finalQueryString);
			if (pagination.isVanityUrlFilterApplicable()) {
				query.setParameter(VENDOR_COMPANY_ID, pagination.getVendorCompanyId());
			}
			if (isFilterApplied) {
				query.setParameter("assetType", pagination.getFilterBy());
			}
			query.setParameter("partnerCompanyId", pagination.getCompanyId());
			query.setParameter(PARTNER_ID, pagination.getUserId());

			return paginationUtil.setScrollableAndGetList(pagination, map, query, PublishedAssetsView.class);
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public DamPublishGetDTO getPublishedAssetById(Integer id) {
		try {
			Session session = sessionFactory.getCurrentSession();
			return (DamPublishGetDTO) session.createSQLQuery(getPublishedAssetByIdQuery).setParameter("id", id)
					.setResultTransformer(Transformers.aliasToBean(DamPublishGetDTO.class)).uniqueResult();
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public void updatePublishedAsset(DamPostDTO damPostDTO) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String hql = "update DamPartner set  htmlBody=:htmlBody,jsonBody=:jsonBody, updatedBy=:updatedBy,updatedTime=:updatedTime where id=:id";
			Query query = session.createQuery(hql);
			query.setParameter("id", damPostDTO.getId());
			query.setParameter(HTML_BODY, damPostDTO.getHtmlBody());
			query.setParameter("jsonBody", damPostDTO.getJsonBody());
			query.setParameter(UPDATED_BY, damPostDTO.getCreatedBy());
			query.setParameter(UPDATED_TIME, new Date());
			query.executeUpdate();
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public Map<String, Object> listPublishedPartners(Pagination pagination) {
		try {
			return listPublishedOrUnPublishedPartners(pagination, false);
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public Map<String, Object> listPartners(Pagination pagination) {
		try {
			return listPublishedOrUnPublishedPartners(pagination, true);
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	private Map<String, Object> listPublishedOrUnPublishedPartners(Pagination pagination,
			boolean viewUnpublishedPartners) {
		Map<String, Object> map = new HashMap<>();
		Session session = sessionFactory.getCurrentSession();
		String finalQueryString = "";
		String sortQueryString = xamplifyUtil.getSelectedSortOptionForPartners(pagination);
		String searchKey = pagination.getSearchKey();
		boolean hasSearchKey = StringUtils.hasText(searchKey);
		List<Integer> publishedPartnershipIds = listPublishedPartnershipIdsByDamId(pagination.getFormId());
		boolean isPublishedToAtleastOnePartner = publishedPartnershipIds != null && !publishedPartnershipIds.isEmpty();
		boolean condition1 = viewUnpublishedPartners && isPublishedToAtleastOnePartner;
		boolean condition2 = !viewUnpublishedPartners && isPublishedToAtleastOnePartner;
		boolean condition3 = !viewUnpublishedPartners && !isPublishedToAtleastOnePartner;
		if (condition1) {
			finalQueryString = getFinalQueryString(listPartnersUtilQuery, sortQueryString, searchKey, hasSearchKey,
					partnershipIdNotInQuery);
		} else if (condition2) {
			finalQueryString = getFinalQueryString(listPublishedPartnersQuery, sortQueryString, searchKey, hasSearchKey,
					partnershipIdInQuery);
		} else if (condition3) {
			map.put("totalRecords", 0);
			map.put("list", new ArrayList<>());
			return map;
		} else {
			finalQueryString = getFinalQueryString(listPartnersUtilQuery, sortQueryString, searchKey, hasSearchKey, "");
		}
		SQLQuery query = session.createSQLQuery(finalQueryString);
		query.setParameter(VENDOR_COMPANY_ID, pagination.getVendorCompanyId());
		if (condition1 || condition2) {
			query.setParameterList("partnershipIds", publishedPartnershipIds);
			if (condition2) {
				query.setParameter(DAM_ID, pagination.getFormId());
			}
		}
		return paginationUtil.setScrollableAndGetList(pagination, map, query, DamPartnerDetailsDTO.class);
	}

	private String getFinalQueryString(String listBaseQuery, String sortQueryString, String searchKey,
			boolean hasSearchKey, String partnerQuery) {
		String finalQueryString;
		if (hasSearchKey) {
			finalQueryString = listBaseQuery + partnerQuery + " "
					+ listPartnersUtilSearchQuery.replace(SEARCH_KEY, searchKey) + " " + sortQueryString;
		} else {
			finalQueryString = listBaseQuery + partnerQuery + " " + sortQueryString;
		}
		return finalQueryString;
	}

	@Override
	public void updateDownloadOptions(DamDownloadPostDTO damDownloadPostDTO) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String hql = "update Dam set pageSize=:pageSize,pageOrientation=:pageOrientation,updatedTime=:updatedTime,updatedBy=:updatedBy where alias=:alias";
			Query query = session.createQuery(hql);
			query.setParameter(ALIAS, damDownloadPostDTO.getAlias());
			query.setParameter(PAGE_SIZE, damDownloadPostDTO.getPageSize());
			query.setParameter(PAGE_ORIENTATION, damDownloadPostDTO.getPageOrientation());
			query.setParameter(UPDATED_TIME, new Date());
			query.setParameter(UPDATED_BY, damDownloadPostDTO.getUserId());
			query.executeUpdate();
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public Dam getDownloadOptionsByAlias(String alias) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Criteria criteria = session.createCriteria(Dam.class);
			criteria.add(Restrictions.eq(ALIAS, alias));
			criteria.setProjection(Projections.projectionList().add(Projections.property(PAGE_SIZE), PAGE_SIZE)
					.add(Projections.property(PAGE_ORIENTATION), PAGE_ORIENTATION))
					.setResultTransformer(Transformers.aliasToBean(Dam.class));
			return (Dam) criteria.uniqueResult();
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public void updateAssetPathAndThumbnailPath(Integer id, String assetPath, String thumbnailPath) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String status = "'" + DamStatusEnum.COMPLETED.name() + "'";
			String modifiedAssetPath = assetPath.replaceAll("'", "''");
			thumbnailPath = thumbnailPath.replaceAll("'", "''");
			String updatedAssetPath = "'" + modifiedAssetPath + "'";
			String updatedThumbnailPath = "'" + thumbnailPath + "'";
			String queryString = "";
			if (StringUtils.hasText(thumbnailPath)) {
				queryString = "update xt_dam set asset_path=" + updatedAssetPath + ",thumbnail_path="
						+ updatedThumbnailPath + ",asset_status=" + status + " where id=" + id;
			} else {
				queryString = "update xt_dam set asset_path=" + updatedAssetPath + ",asset_status=" + status
						+ " where id=" + id;
			}
			Query query = session.createSQLQuery(queryString);
			query.executeUpdate();
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public String getAssetPathByAlias(String alias, boolean isPartnerContent) {
		try {
			Session session = sessionFactory.getCurrentSession();
			if (isPartnerContent) {
				Criteria criteria = session.createCriteria(DamPartner.class, "damPartner").createAlias("damPartner.dam",
						"dam");
				criteria.add(Restrictions.eq(ALIAS, alias));
				String assetType = (String) criteria.setProjection(Projections.property("dam.assetType"))
						.uniqueResult();
				Projection assetProjection = Projections.property("dam.assetPath");
				if ("pdf".equals(assetType)) {
					String sharedAssetPath = (String) criteria
							.setProjection(Projections.property("damPartner.sharedAssetPath")).uniqueResult();
					if (sharedAssetPath != null) {
						assetProjection = Projections.property("damPartner.sharedAssetPath");
					} else {
						assetProjection = Projections.property("dam.assetPath");
					}
				}
				criteria.setProjection(Projections.projectionList().add((assetProjection), ASSET_PATH));
				String assetPath = (String) criteria.uniqueResult();
				return assetPath != null ? assetPath : "";
			} else {
				Criteria criteria = session.createCriteria(Dam.class);
				criteria.add(Restrictions.eq(ALIAS, alias));
				criteria.setProjection(Projections.projectionList().add(Projections.property(ASSET_PATH), ASSET_PATH));
				String assetPath = (String) criteria.uniqueResult();
				return assetPath != null ? assetPath : "";
			}

		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public void delete(Integer id) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String hql = "delete from Dam  where id=:id";
			Query query = session.createQuery(hql);
			query.setParameter("id", id);
			query.executeUpdate();
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public SharedAssetDetailsViewDTO getSharedAssetDetailsById(Integer id) {
		try {
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(getSharedAssetDetailsByIdQuery);
			query.setParameter("id", id);
			return (SharedAssetDetailsViewDTO) query
					.setResultTransformer(Transformers.aliasToBean(SharedAssetDetailsViewDTO.class)).uniqueResult();
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public Integer getDamPartnerIdByAlias(String alias) {
		try {
			Session session = sessionFactory.getCurrentSession();
			return (Integer) session.createSQLQuery("select id from xt_dam_partner where alias=:alias")
					.setParameter(ALIAS, alias).uniqueResult();
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> listDamAnalytics(Pagination pagination) {
		try {
			HashMap<String, Object> map = new HashMap<>();
			String finalQueryString = "";
			String searchKey = pagination.getSearchKey();
			boolean hasSearchKey = StringUtils.hasText(searchKey);
			String updatedAnalyticsQuery = listDamAnalyticsQuery;
			boolean isPublishedToPartnerGroup = isPublishedToPartnerGroupsByDamPartnerId(pagination.getCampaignId());
			if (isPublishedToPartnerGroup) {
				updatedAnalyticsQuery = listPartnerGroupDamAnalyticsQuery;
			}
			if (!StringUtils.hasText(pagination.getFilterKey()) || "NONE".equals(pagination.getFilterKey())) {
				updatedAnalyticsQuery += " and da.action_type in ('DOWNLOAD', 'VIEW')";
			} else if (StringUtils.hasText(pagination.getCampaignType())) {
				updatedAnalyticsQuery += " and  da.action_type in ('" + pagination.getFilterKey().toUpperCase() + "')";
			}
			if (hasSearchKey) {
				finalQueryString = updatedAnalyticsQuery + " "
						+ listDamAnalyticsSearchQuery.replace(SEARCH_KEY, searchKey) + " " + listDamAnalyticsSortQuery;
			} else {
				finalQueryString = updatedAnalyticsQuery + " " + listDamAnalyticsSortQuery;
			}
			SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(finalQueryString);
			query.setParameter(DAM_PARTNER_ID, pagination.getCampaignId());
			/*** XBI-2788 ***/
			query.setParameter(PARTNER_ID, pagination.getPartnerId());
			ScrollableResults scrollableResults = query.scroll();
			scrollableResults.last();
			Integer totalRecords = scrollableResults.getRowNumber() + 1;
			query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
			query.setMaxResults(pagination.getMaxResults());
			List<DamAnalyticsViewDTO> list = query
					.setResultTransformer(Transformers.aliasToBean(DamAnalyticsViewDTO.class)).list();
			List<DamAnalyticsViewDTO> updatedList = new ArrayList<>();
			for (DamAnalyticsViewDTO damAnalyticsViewDTO : list) {
				DamAnalyticsViewDTO updatedamAnalyticsViewDTO = new DamAnalyticsViewDTO();
				BeanUtils.copyProperties(damAnalyticsViewDTO, updatedamAnalyticsViewDTO);
				updatedamAnalyticsViewDTO
						.setActionTimeInUTCString(DateUtils.getUtcString(damAnalyticsViewDTO.getActionTime()));
				updatedList.add(updatedamAnalyticsViewDTO);
			}
			map.put(totalRecordsStringKey, totalRecords);
			map.put("list", updatedList);
			return map;
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public DamAnalyticsTilesDTO findPartnerDetailsAndViewAndDownloadsCount(Integer damPartnerId, Integer partnerId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			DamAnalyticsTilesDTO damAnalyticsTilesDTO;
			boolean isPublishedToPartnerGroup = isPublishedToPartnerGroupsByDamPartnerId(damPartnerId);
			if (isPublishedToPartnerGroup) {
				damAnalyticsTilesDTO = (DamAnalyticsTilesDTO) session
						.createSQLQuery(showPartnerDetailsWithAnalyticsCountForPartnerCompanyQuery)
						.setParameter(DAM_PARTNER_ID, damPartnerId).setParameter(PARTNER_ID, partnerId)
						.setResultTransformer(Transformers.aliasToBean(DamAnalyticsTilesDTO.class)).uniqueResult();
			} else {
				damAnalyticsTilesDTO = (DamAnalyticsTilesDTO) session
						.createSQLQuery(showPartnerDetailsWithAnalyticsCountQuery)
						.setParameter(DAM_PARTNER_ID, damPartnerId).setParameter(PARTNER_ID, partnerId)
						.setResultTransformer(Transformers.aliasToBean(DamAnalyticsTilesDTO.class)).uniqueResult();
			}

			findPartnerDetails(damPartnerId, partnerId, session, damAnalyticsTilesDTO);
			return damAnalyticsTilesDTO;
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	private void findPartnerDetails(Integer damPartnerId, Integer partnerId, Session session,
			DamAnalyticsTilesDTO damAnalyticsTilesDTO) {
		String queryString = "select p.partner_id as \"partnerId\",p.partner_company_id as \"partnerCompanyId\",p.vendor_company_id as \"vendorCompanyId\" from xt_partnership p, xt_dam_partner dp "
				+ " where dp.id = " + damPartnerId + " and dp.partnership_id = p.id";
		PartnerCompanyDTO partnerCompanyDTO = (PartnerCompanyDTO) session.createSQLQuery(queryString)
				.setResultTransformer(Transformers.aliasToBean(PartnerCompanyDTO.class)).uniqueResult();
		if (partnerCompanyDTO != null && partnerCompanyDTO.getVendorCompanyId() != null) {
			findDetailsFromPartnership(partnerId, session, damAnalyticsTilesDTO, partnerCompanyDTO);
		}
	}

	private void findDetailsFromPartnership(Integer partnerId, Session session,
			DamAnalyticsTilesDTO damAnalyticsTilesDTO, PartnerCompanyDTO partnerCompanyDTO) {
		String partnerDetailsQuery = " select  u.email_id as \"emailId\",coalesce(uul.firstname,'')  as \"firstName\", "
				+ " coalesce(uul.lastname,'')  as \"lastName\", coalesce(uul.contact_company,'')  as \"contactCompany\", "
				+ " concat(uul.firstname, ' ', uul.lastname) as \"fullName\" from xt_user_list ul,xt_user_profile u, "
				+ " xt_user_userlist uul where ul.company_id = :vendorCompanyId and uul.user_list_id = ul.user_list_id "
				+ " and uul.user_id = :partnerId and ul.is_default_partnerlist and u.user_id = uul.user_id ";

		DamAnalyticsTilesDTO partnerDetails = (DamAnalyticsTilesDTO) session.createSQLQuery(partnerDetailsQuery)
				.setParameter(VENDOR_COMPANY_ID, partnerCompanyDTO.getVendorCompanyId())
				.setParameter(PARTNER_ID, partnerCompanyDTO.getPartnerId())
				.setResultTransformer(Transformers.aliasToBean(DamAnalyticsTilesDTO.class)).uniqueResult();

		if (partnerDetails != null) {
			damAnalyticsTilesDTO.setEmailId(partnerDetails.getEmailId());
			damAnalyticsTilesDTO.setFirstName(partnerDetails.getFirstName());
			damAnalyticsTilesDTO.setLastName(partnerDetails.getLastName());
			damAnalyticsTilesDTO.setContactCompany(partnerDetails.getContactCompany());
		}

		if (!partnerId.equals(partnerCompanyDTO.getPartnerId())) {
			User user = userDao.getFirstNameLastNameAndEmailIdByUserId(partnerId);
			damAnalyticsTilesDTO.setEmailId(user.getEmailId());
			damAnalyticsTilesDTO.setFirstName(user.getFirstName());
			damAnalyticsTilesDTO.setLastName(user.getLastName());
		}
	}

	@Override
	public Map<String, Object> listPublishedPartnersAnalytics(Pagination pagination) {
		Map<String, Object> map = new HashMap<>();
		try {
			Session session = sessionFactory.getCurrentSession();
			org.hibernate.Criteria criteria;
			boolean publishedToPartnerGroups = isPublishedToPartnerGroups(pagination.getFormId());
			if (publishedToPartnerGroups) {
				criteria = session.createCriteria(DamPartnerGroupAnalyticsView.class);
			} else {
				criteria = session.createCriteria(DamPartnerAnalyticsView.class);
			}
			criteria.add(Restrictions.eq(DAM_ID, pagination.getFormId()));
			/******** XNFR-85 **********/
			TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
					pagination.isPartnerTeamMemberGroupFilter(), true);
			boolean applyPartnershipIdsFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();

			if (teamMemberFilterDTO.isEmptyFilter()) {
				return paginationUtil.returnEmptyList(map,
						publishedToPartnerGroups ? new ArrayList<DamPartnerGroupAnalyticsView>()
								: new ArrayList<DamPartnerAnalyticsView>());
			} else {
				if (applyPartnershipIdsFilter) {
					criteria.add(Restrictions.in(PARTNERSHIP_ID,
							teamMemberFilterDTO.getPartnershipIdsOrPartnerCompanyIds()));
				}
			}
			List<com.xtremand.common.bom.Criteria> criterias = new ArrayList<>();
			List<Criterion> criterions = generateCriteria(criterias);
			List<String> columnNames = new ArrayList<>();
			columnNames.add("emailId");
			columnNames.add("firstName");
			columnNames.add("lastName");
			columnNames.add("contactCompany");
			map = paginationUtil.addSearchAndPaginationAndSort(pagination, criteria, criterions, columnNames, "emailId",
					"asc");
			return map;

		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public Integer getMaxVersionByParentId(Integer id) {
		try {
			Session session = sessionFactory.getCurrentSession();
			return (Integer) session
					.createSQLQuery("select coalesce(max(version),1) from xt_dam where parent_id=:parentId")
					.setParameter(PARENT_ID, id).uniqueResult();
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public Integer getChildTemplatesCountByParentId(Integer id) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String queryString = "SELECT CAST (count(id) AS INTEGER) from xt_dam where parent_id = :parentId";
			return (Integer) session.createSQLQuery(queryString).setParameter(PARENT_ID, id).uniqueResult();
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public void updateVersion(Integer id, Integer version) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String hql = "update Dam set version=:version where id=:id";
			Query query = session.createQuery(hql);
			query.setParameter("id", id);
			query.setParameter(VERSION, version);
			query.executeUpdate();
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public Integer getIdForUpdatingVersion(Integer id) {
		try {
			Session session = sessionFactory.getCurrentSession();
			return (Integer) session
					.createSQLQuery("select id from xt_dam where parent_id=:parentId and version is null")
					.setParameter(PARENT_ID, id).uniqueResult();
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public Dam getAssetDetailsById(Integer id) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Criteria criteria = session.createCriteria(Dam.class);
			criteria.add(Restrictions.eq("id", id));

			criteria.setProjection(Projections.projectionList().add(Projections.property(ASSET_NAME), ASSET_NAME)
					.add(Projections.property(DESCRIPTION), DESCRIPTION)
					.add(Projections.property(THUMBNAIL_PATH), THUMBNAIL_PATH)
					.add(Projections.property("beeTemplate"), "beeTemplate")
					.add(Projections.property(PUBLISHED_TIME), PUBLISHED_TIME)
					.add(Projections.property(PARENT_ID), PARENT_ID)
					.add(Projections.property("childParentId"), "childParentId")
					.add(Projections.property(VERSION), VERSION).add(Projections.property(ASSET_PATH), ASSET_PATH))
					.setResultTransformer(Transformers.aliasToBean(Dam.class));

			return criteria.uniqueResult() != null ? (Dam) criteria.uniqueResult() : new Dam();
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public void update(DamUploadPostDTO damUploadPostDTO) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String queryString = "update xt_dam set asset_name = :assetName, description = :description, updated_by = :updatedBy, "
					+ "updated_time = :updatedTime, is_white_labeled_asset_shared_with_partners = :whiteLabeledAssetSharedWithPartners, "
					+ "is_added_to_quick_links = :addedToQuickLinks, is_partner_signature_required = :partnerSignatureRequired, "
					+ "is_vendor_signature_required = :vendorSignatureRequired, "
					+ "is_vendor_signature_required_after_partner_signature = :vendorSignatureRequiredAfterPartnerSignature, "
					+ " slug = :slug";
			if (StringUtils.hasText(damUploadPostDTO.getThumbnailPath())) {
				queryString += ", thumbnail_path = :thumbnailPath";
			}
			if (damUploadPostDTO.isUpdateApprovalStatus()
					&& XamplifyUtils.isValidString(damUploadPostDTO.getApprovalStatusInString())) {
				queryString += ", approval_status = :approvalStatus, approval_status_updated_by = :approvalStatusUpdatedBy, "
						+ "approval_status_updated_time = :approvalStatusUpdatedTime";
			}
			queryString += " where id=:id limit 1";

			Query query = session.createSQLQuery(queryString);
			query.setParameter("id", damUploadPostDTO.getId());
			query.setParameter(ASSET_NAME, damUploadPostDTO.getAssetName());
			query.setParameter(DESCRIPTION, damUploadPostDTO.getDescription());
			query.setParameter(UPDATED_BY, damUploadPostDTO.getLoggedInUserId());
			query.setParameter(UPDATED_TIME, new Date());
			query.setParameter("whiteLabeledAssetSharedWithPartners", damUploadPostDTO.isShareAsWhiteLabeledAsset());
			query.setParameter(ADDED_TO_QUICK_LINKS, damUploadPostDTO.isAddedToQuickLinks());
			query.setParameter("partnerSignatureRequired", damUploadPostDTO.isPartnerSignatureRequired());
			query.setParameter("vendorSignatureRequired", damUploadPostDTO.isVendorSignatureRequired());
			query.setParameter("vendorSignatureRequiredAfterPartnerSignature",
					damUploadPostDTO.isVendorSignatureRequiredAfterPartnerSignature());
			query.setParameter("slug", damUploadPostDTO.getSlug());

			if (StringUtils.hasText(damUploadPostDTO.getThumbnailPath())) {
				query.setParameter(THUMBNAIL_PATH, damUploadPostDTO.getThumbnailPath());
			}
			if (damUploadPostDTO.isUpdateApprovalStatus()
					&& XamplifyUtils.isValidString(damUploadPostDTO.getApprovalStatusInString())) {
				query.setParameter("approvalStatus",
						approveDao.getApprovalStatusByString(damUploadPostDTO.getApprovalStatusInString()));
				query.setParameter("approvalStatusUpdatedBy", damUploadPostDTO.getApprovalStatusUpdatedby());
				query.setParameter("approvalStatusUpdatedTime", new Date());
			}
			query.executeUpdate();
		} catch (ConstraintViolationException e) {
			if ("dam_asset_name_unique_index".equalsIgnoreCase(e.getConstraintName())) {
				throw new DuplicateEntryException(e.getConstraintName());
			} else {
				throw new DamDataAccessException(e);
			}
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	@Transactional
	public void deletePartner(Integer id) {
		try {
			Session session = sessionFactory.getCurrentSession();
			/******* Get Dam Id *********/
			Integer damId = (Integer) session.createSQLQuery("select dam_id from xt_dam_partner where id=:id")
					.setParameter("id", id).uniqueResult();
			String hql = "delete from DamPartner  where id=:id";
			Query query = session.createQuery(hql);
			query.setParameter("id", id);
			query.executeUpdate();
			if (damId != null) {
				String publishedPartnersCountQuery = "select count(*) from xt_dam_partner where dam_id=:damId";
				Integer publishedPartnersCount = ((BigInteger) session.createSQLQuery(publishedPartnersCountQuery)
						.setParameter(DAM_ID, damId).uniqueResult()).intValue();
				if (publishedPartnersCount == 0) {
					session.createQuery("update Dam set published = :published where id=:id")
							.setParameter("published", false).setParameter("id", damId).executeUpdate();
				}

			}
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public Integer getCompanyIdById(Integer id) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String sql = "select company_id from xt_dam where id=:id";
			Query query = session.createSQLQuery(sql);
			query.setParameter("id", id);
			return query.uniqueResult() != null ? (int) query.uniqueResult() : 0;
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public String getAssetNameByDamPartnerId(Integer damPartnerId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String sql = "select d.asset_name from xt_dam d,xt_dam_partner dp where dp.dam_id = d.id and dp.id=:damPartnerId";
			Query query = session.createSQLQuery(sql);
			query.setParameter(DAM_PARTNER_ID, damPartnerId);
			return query.uniqueResult() != null ? (String) query.uniqueResult() : "";
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public DamPreviewDTO previewAssetById(Integer id) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String sql = "select asset_name as \"name\",asset_path as \"assetPath\",is_bee_template as \"beeTemplate\",html_body as \"htmlBody\","
					+ "created_by as \"createdBy\",alias \"alias\" from xt_dam where id=:id";
			Query query = session.createSQLQuery(sql);
			query.setParameter("id", id);
			return (DamPreviewDTO) query.setResultTransformer(Transformers.aliasToBean(DamPreviewDTO.class))
					.uniqueResult();
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getUsersByDamId(Integer damId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String sql = "select distinct LOWER(TRIM(u.email_id)) from xt_user_profile u left join xt_dam d on d.company_id = u.company_id where d.id  =:damId  or u.user_id = 1";
			Query query = session.createSQLQuery(sql);
			query.setParameter(DAM_ID, damId);
			return query.list();
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getPartnerEmailIdsByDamPartnerId(Integer damPartnerId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String sql = "select LOWER(TRIM(u.email_id)) from xt_user_profile u,xt_dam_partner dp,xt_partnership p where dp.id = :damPartnerId and dp.partnership_id = p.id and (u.company_id = p.partner_company_id or u.user_id = 1)";
			Query query = session.createSQLQuery(sql);
			query.setParameter(DAM_PARTNER_ID, damPartnerId);
			return query.list();
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getUsersByDamAlias(Integer damId, Integer userId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String sql = "select distinct u.user_id from xt_user_profile u left join xt_dam d on d.company_id = u.company_id "
					+ " where d.id  =:damId and u.company_id = (select company_id from xt_user_profile where user_id ="
					+ userId + ") or u.user_id = 1";
			Query query = session.createSQLQuery(sql);
			query.setParameter(DAM_ID, damId);
			return query.list();
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public List<Integer> findPartnershipIdsForPartnerCompaniesOptionByDamId(Integer damId) {
		try {
			if (damId != null && damId > 0) {
				boolean isPartnerGroupSelected = false;
				return findPartnershipIdsByDamId(damId, isPartnerGroupSelected);
			} else {
				return Collections.emptyList();
			}

		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findPublishedPartnerIdsByDamIdAndPartnershipId(Integer damId, Integer partnershipId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String sql = "select distinct dpm.partner_id from xt_dam_partner dp,xt_dam_partner_mapping dpm where dp.dam_id = :damId and dpm.dam_partner_id = dp.id and dp.partnership_id=:partnershipId";
			Query query = session.createSQLQuery(sql);
			query.setParameter(DAM_ID, damId);
			query.setParameter(PARTNERSHIP_ID, partnershipId);
			return query.list();
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public List<Integer> findPublishedPartnerIdsByDamId(Integer damId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String sql = "select distinct dpm.partner_id from xt_dam_partner dp,xt_dam_partner_mapping dpm where dp.dam_id = :damId and dpm.dam_partner_id = dp.id";
			return findPublishedPartnerIdsOrPartnerGroupIds(damId, session, sql);
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void findAndDeleteDamPartnerMappingIds(Integer damId, List<Integer> partnerIds) {
		try {
			if (partnerIds != null && !partnerIds.isEmpty()) {
				List<Integer> damPartnerMappingIds = new ArrayList<>();
				Session session = sessionFactory.getCurrentSession();
				String sql = "select distinct dpm.id from xt_dam_partner dp,xt_dam_partner_mapping dpm where dp.dam_id  = :damId and dpm.partner_id in (:partnerIds) "
						+ " and dpm.dam_partner_id = dp.id";
				Query query = session.createSQLQuery(sql);
				query.setParameter(DAM_ID, damId);
				query.setParameterList("partnerIds", partnerIds);
				if (query.list() != null && !query.list().isEmpty()) {
					damPartnerMappingIds = query.list();
				}
				deleteDamPartnerMappingIds(damPartnerMappingIds, session);
			}
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	private void deleteDamPartnerMappingIds(List<Integer> damPartnerMappingIds, Session session) {
		if (damPartnerMappingIds != null && !damPartnerMappingIds.isEmpty()) {
			String sql = "delete from xt_dam_partner_mapping where id in(:ids)";
			Query query = session.createSQLQuery(sql);
			query.setParameterList("ids", damPartnerMappingIds);
			query.executeUpdate();
		}

	}

	private boolean isAssetPublishedByDamPartnerIdAndPartnerId(Session session, Integer damPartnerId,
			Integer partnerId) {
		String sql = "SELECT CASE WHEN  count(*) > 0 THEN true ELSE false END from xt_dam_partner_mapping where dam_partner_id = :damPartnerId and partner_id = :partnerId";
		Query query = session.createSQLQuery(sql);
		query.setParameter(DAM_PARTNER_ID, damPartnerId);
		query.setParameter(PARTNER_ID, partnerId);
		return (boolean) query.uniqueResult();
	}

	@Override
	public void deleteDamPartnerByDamIdAndUnpublishDam(Integer damId) {
		try {
			deleteDamPartner(damId);
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	private Session deleteDamPartner(Integer damId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "delete from xt_dam_partner where dam_id=:damId";
		Query query = session.createSQLQuery(sql);
		query.setParameter(DAM_ID, damId);
		query.executeUpdate();
		return session;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void insertIntoDamPartnerMapping() {
		try {
			Session session = sessionFactory.getCurrentSession();
			String partnerIdsQueryString = "select distinct dp.id,p.partner_id from xt_dam_partner dp,xt_partnership p where dp.partnership_id = p.id";
			List<Object[]> rows = session.createSQLQuery(partnerIdsQueryString).list();
			for (Object[] row : rows) {
				Integer damPartnerId = (Integer) row[0];
				Integer partnerId = (Integer) row[1];
				DamPartner damPartner = new DamPartner();
				damPartner.setId(damPartnerId);
				DamPartnerMapping damPartnerMapping = new DamPartnerMapping();
				damPartnerMapping.setCreatedTime(new Date());
				damPartnerMapping.setDamPartner(damPartner);
				damPartnerMapping.setPartnerId(partnerId);
				session.save(damPartnerMapping);
			}
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}

	}

	@Override
	public void findAndDeleteUnMappedRowsFromDamPartnerOrDamPartnerGroupMapping(boolean isPartnerGroupSelected) {
		try {
			Session session = sessionFactory.getCurrentSession();
			List<Integer> unmappedIds = findUnMappedIds(session, isPartnerGroupSelected);
			deleteUnMappedRowsFromDamPartner(session, unmappedIds);
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}

	}

	@SuppressWarnings("unchecked")
	private List<Integer> findUnMappedIds(Session session, boolean isPartnerGroupSelected) {
		String tableName = isPartnerGroupSelected ? "xt_dam_partner_group_mapping" : "xt_dam_partner_mapping";
		String unMappedIdsQueryString = "select id from xt_dam_partner where id not in (select dam_partner_id from "
				+ tableName + ") and is_partner_group_selected=" + isPartnerGroupSelected;
		return session.createSQLQuery(unMappedIdsQueryString).list();
	}

	private void deleteUnMappedRowsFromDamPartner(Session session, List<Integer> unmappedIds) {
		if (unmappedIds != null && !unmappedIds.isEmpty()) {
			String deleteQuery = "delete from xt_dam_partner where id in (:ids)";
			session.createSQLQuery(deleteQuery).setParameterList("ids", unmappedIds).executeUpdate();
		}
	}

	@Override
	public void updateBeeTemplateImagePath(List<Integer> ids, String path) {
		try {
			if (ids != null && !ids.isEmpty()) {
				Session session = sessionFactory.getCurrentSession();
				String querystr = "update Dam set thumbnailPath = :thumbnailPath,imageGeneratedOn=:date,imageGeneratedSuccessfully=:isImageGenerated  where id in (:ids)";
				Query query = session.createQuery(querystr);
				if (StringUtils.hasText(path)) {
					query.setParameter(THUMBNAIL_PATH, path);
					query.setParameter("isImageGenerated", true);
				} else {
					query.setParameter(THUMBNAIL_PATH, pdfThumbnailPath);
					query.setParameter("isImageGenerated", false);
				}
				query.setParameter("date", new Date());
				query.setParameterList("ids", ids);
				query.executeUpdate();
			}
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}

	}

	@Override
	public DamPartnerMapping findByPartnerIdAndDamPartnerId(Integer partnerId, Integer damPartnerId) {
		try {
			if (partnerId != null && partnerId > 0 && damPartnerId != null && damPartnerId > 0) {
				Session session = sessionFactory.getCurrentSession();
				org.hibernate.Criteria criteria = session.createCriteria(DamPartnerMapping.class);
				criteria.add(Restrictions.eq("damPartner.id", damPartnerId));
				criteria.add(Restrictions.eq(PARTNER_ID, partnerId));
				return (DamPartnerMapping) criteria.uniqueResult();
			} else {
				return null;
			}

		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public Dam findByPrimaryKey(Serializable pk, FindLevel[] levels) {
		return null;
	}

	@Override
	public Collection<Dam> find(List<com.xtremand.common.bom.Criteria> criterias, FindLevel[] levels) {
		return Collections.emptyList();
	}

	@Override
	public Map<String, Object> find(List<com.xtremand.common.bom.Criteria> criterias, FindLevel[] levels,
			Pagination pagination) {
		return Collections.emptyMap();
	}

	@Override
	public boolean checkVendorAccessForDamPartnerAnalytics(Integer damId, Integer partnerId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String viewName = isPublishedToPartnerGroups(damId) ? "v_dam_partner_group_analytics"
					: "v_dam_partner_analytics";
			String querystr = "select case when count(*)>0 then true else false end as count  from " + viewName
					+ " where dam_id = " + damId + " and user_id = " + partnerId;
			return (boolean) session.createSQLQuery(querystr).uniqueResult();
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	public List<Integer> findPublishedPartnerGroupIdsByDamId(Integer damId) {
		try {
			if (damId != null && damId > 0) {
				Session session = sessionFactory.getCurrentSession();
				String sql = "select distinct dpgm.user_list_id from xt_dam_partner_group_mapping dpgm, xt_dam_partner dp,xt_dam d where d.id = dp.dam_id and dp.id = dpgm.dam_partner_id and d.id = :damId";
				return findPublishedPartnerIdsOrPartnerGroupIds(damId, session, sql);
			} else {
				return Collections.emptyList();
			}

		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	private List<Integer> findPublishedPartnerIdsOrPartnerGroupIds(Integer damId, Session session, String sql) {
		Query query = session.createSQLQuery(sql);
		query.setParameter(DAM_ID, damId);
		if (query.list() != null && !query.list().isEmpty()) {
			return query.list();
		} else {
			return new ArrayList<>();
		}
	}

	private boolean isRowExistsInDamPartnerGroupMapping(Session session, Integer damPartnerId, Integer userListId,
			Integer userId) {
		String sql = "SELECT CASE WHEN  count(*) > 0 THEN true ELSE false END from xt_dam_partner_group_mapping where dam_partner_id = :damPartnerId and user_list_id = :userListId and user_id = :userId";
		Query query = session.createSQLQuery(sql);
		query.setParameter(DAM_PARTNER_ID, damPartnerId);
		query.setParameter(USER_ID, userId);
		query.setParameter(USER_LIST_ID, userListId);
		return (boolean) query.uniqueResult();
	}

	@Override
	public void findAndDeleteDamPartnerGroupMappingIds(Integer damId, List<Integer> publishedPartnerGroupIds) {
		try {
			if (publishedPartnerGroupIds != null && !publishedPartnerGroupIds.isEmpty()) {
				List<Integer> damPartnerGroupMappingIds = new ArrayList<>();
				Session session = sessionFactory.getCurrentSession();
				damPartnerGroupMappingIds = findDamPartnerGroupMappingIds(damId, publishedPartnerGroupIds,
						damPartnerGroupMappingIds, session);
				deleteFromDamPartnerGroupMappingByIds(damPartnerGroupMappingIds, session);
			}
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	private List<Integer> findDamPartnerGroupMappingIds(Integer damId, List<Integer> publishedPartnerGroupIds,
			List<Integer> damPartnerGroupMappingIds, Session session) {
		String sql = "select distinct dpgm.id from xt_dam_partner_group_mapping dpgm,xt_dam_partner dp,xt_dam d where d.id = dp.dam_id and dp.id = dpgm.dam_partner_id and d.id = :damId and dpgm.user_list_id in (:publishedPartnerGroupIds)";
		Query query = session.createSQLQuery(sql);
		query.setParameter(DAM_ID, damId);
		query.setParameterList("publishedPartnerGroupIds", publishedPartnerGroupIds);
		if (query.list() != null && !query.list().isEmpty()) {
			damPartnerGroupMappingIds = query.list();
		}
		return damPartnerGroupMappingIds;
	}

	private void deleteFromDamPartnerGroupMappingByIds(List<Integer> damPartnerGroupMappingIds, Session session) {
		if (damPartnerGroupMappingIds != null && !damPartnerGroupMappingIds.isEmpty()) {
			String sql = "delete from xt_dam_partner_group_mapping where id in(:ids)";
			Query query = session.createSQLQuery(sql);
			query.setParameterList("ids", damPartnerGroupMappingIds);
			query.executeUpdate();
		}
	}

	@Override
	public List<Integer> findPartnershipIdsForPartnerGroupOptionByDamId(Integer damId) {
		try {
			boolean isPartnerGroupSelected = true;
			return findPartnershipIdsByDamId(damId, isPartnerGroupSelected);
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	private List<Integer> findPartnershipIdsByDamId(Integer damId, boolean isPartnerGroupSelected) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String sql = "select distinct  dp.partnership_id from xt_dam_partner dp,xt_dam d where dp.dam_id = d.id and dp.is_partner_group_selected =:isPartnerGroupSelected  and dp.dam_id = :damId";
			Query query = session.createSQLQuery(sql);
			query.setParameter(DAM_ID, damId);
			query.setParameter("isPartnerGroupSelected", isPartnerGroupSelected);
			return query.list();
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}

	}

	@Override
	public boolean isPublishedToPartnerGroups(Integer damId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String sql = "select distinct is_partner_group_selected from xt_dam_partner where dam_id=" + damId;
			Query query = session.createSQLQuery(sql);
			if (query.uniqueResult() != null) {
				return (boolean) query.uniqueResult();
			} else {
				return false;
			}
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}

	}

	@Override
	public boolean isPublishedToPartnerGroupsByDamPartnerId(Integer damPartnerId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String sql = "select is_partner_group_selected from xt_dam_partner where id=" + damPartnerId;
			Query query = session.createSQLQuery(sql);
			if (query.uniqueResult() != null) {
				return (boolean) query.uniqueResult();
			} else {
				return false;
			}
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}

	}

	@Override
	public void deleteFromDamPartnerGroupMappingAndDamPartnerByUserListIdAndUserIds(List<Integer> userIds,
			Integer userListId, Integer userId) {
		if (XamplifyUtils.isNotEmptyList(userIds) && XamplifyUtils.isValidInteger(userListId)
				&& XamplifyUtils.isValidInteger(userId)) {
			Session session = sessionFactory.getCurrentSession();
			deleteFromDamPartnerGroupMapping(userIds, userListId, session);
			findDamPartnerIdsAndDelete(userId, session);
		}
	}

	@SuppressWarnings("unchecked")
	private void findDamPartnerIdsAndDelete(Integer userId, Session session) {
		String sql = "select distinct dp.id from xt_dam d,xt_dam_partner dp where d.id = dp.dam_id and d.company_id = (select company_id from xt_user_profile where user_id=:userId)";
		List<Integer> damPartnerIds = session.createSQLQuery(sql).setParameter(USER_ID, userId).list();
		if (damPartnerIds != null && !damPartnerIds.isEmpty()) {
			for (Integer damPartnerId : damPartnerIds) {
				boolean isPublishedToPartnerGroup = isPublishedToPartnerGroupsByDamPartnerId(damPartnerId);
				if (isPublishedToPartnerGroup) {
					boolean isUserListExists = (boolean) session.createSQLQuery(
							"select case when count(*)>0 then true else false end as c1 from xt_dam_partner_group_mapping where dam_partner_id = :damPartnerId")
							.setParameter(DAM_PARTNER_ID, damPartnerId).uniqueResult();
					if (!isUserListExists) {
						session.createSQLQuery("delete from xt_dam_partner where id=" + damPartnerId).executeUpdate();
					}
				}

			}
		}
	}

	private void deleteFromDamPartnerGroupMapping(List<Integer> userIds, Integer userListId, Session session) {
		String sql = "delete from xt_dam_partner_group_mapping where user_id in (:userIds) and user_list_id = :userListId";
		Query query = session.createSQLQuery(sql);
		query.setParameterList("userIds", userIds);
		query.setParameter(USER_LIST_ID, userListId);
		query.executeUpdate();
	}

	@Override
	public void deleteFromDamPartnerGroupMappingAndDamPartnerByUserListId(Integer userListId, Integer loggedInUserId) {
		if (userListId != null && loggedInUserId != null && userListId > 0 && loggedInUserId > 0) {
			Session session = sessionFactory.getCurrentSession();
			deleteFromDamPartnerGroupMappingByUserListId(userListId, session);
			findDamPartnerIdsAndDelete(loggedInUserId, session);
		}
	}

	private void deleteFromDamPartnerGroupMappingByUserListId(Integer userListId, Session session) {
		String sql = "delete from xt_dam_partner_group_mapping where user_list_id = :userListId";
		Query query = session.createSQLQuery(sql);
		query.setParameter(USER_LIST_ID, userListId);
		query.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DamBasicInfo> findAssociatedDamBasicInfoByUserListId(Integer userListId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String sql = " select  distinct d.id as \"damId\",d.asset_name as \"assetName\",d.video_id as \"videoId\", d.is_white_labeled_asset_shared_with_partners as \"whiteLabeledAssetSharedWithPartners\",c.company_name as \"companyName\", "
					+ " d.published_time as \"publishedTime\" from xt_dam d,xt_dam_partner dp,xt_dam_partner_group_mapping dpgm,xt_company_profile c "
					+ " where dp.dam_id = d.id and dp.id = dpgm.dam_partner_id "
					+ " and user_list_id = :userListId and dp.is_partner_group_selected  and c.company_id = d.company_id";
			return session.createSQLQuery(sql).setParameter(USER_LIST_ID, userListId)
					.setResultTransformer(Transformers.aliasToBean(DamBasicInfo.class)).list();
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public boolean isPublishedToPartnerByDamIdAndPartnershipId(Integer damId, Integer partnershipId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String sql = "select case when count(*)>0 then true else false end as isPublished from xt_dam_partner where dam_id = :damId and partnership_id = :partnershipId";
			return (boolean) session.createSQLQuery(sql).setParameter(DAM_ID, damId)
					.setParameter(PARTNERSHIP_ID, partnershipId).uniqueResult();
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public Integer findDamPartnerIdByDamIdAndPartnershipId(Integer damId, Integer partnershipId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String sql = "select id from xt_dam_partner where dam_id = :damId and partnership_id = :partnershipId";
			return (Integer) session.createSQLQuery(sql).setParameter(DAM_ID, damId)
					.setParameter(PARTNERSHIP_ID, partnershipId).uniqueResult();
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public boolean isAssetSharedToPartnerCompanyByPartnerId(Integer partnerId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select case when count(dpm.id)>0 then true else false end as asset_shared_to_partner_company from xt_dam d,"
				+ "xt_dam_partner dp,xt_dam_partner_mapping dpm where dp.id = dpm.dam_partner_id and dpm.partner_id = :partnerId and d.id = dp.dam_id";
		return (boolean) session.createSQLQuery(sql).setParameter(PARTNER_ID, partnerId).uniqueResult();
	}

	@Override
	public boolean isAssetSharedToPartnerCompanyByPartnerIdAndVendorCompany(Integer partnerId,
			Integer vendorCompanyId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select case when count(dpm.id)>0 then true else false end as asset_shared_to_partner_company from xt_dam d,"
				+ "xt_dam_partner dp,xt_dam_partner_mapping dpm where dp.id = dpm.dam_partner_id and dpm.partner_id = :partnerId and d.company_id = :companyId and d.id = dp.dam_id";
		return (boolean) session.createSQLQuery(sql).setParameter(PARTNER_ID, partnerId)
				.setParameter(COMPANY_ID, vendorCompanyId).uniqueResult();
	}

	@Override
	public DamPartner findByPartnershipIdAndDamId(Integer partnershipId, Integer damId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			org.hibernate.Criteria criteria = session.createCriteria(DamPartner.class);
			criteria.add(Restrictions.eq("partnership.id", partnershipId));
			criteria.add(Restrictions.eq("dam.id", damId));
			return (DamPartner) criteria.uniqueResult();
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public DamPartnerGroupMapping findByUserListIdAndUserIdAndDamPartnerId(Integer userListId, Integer userId,
			Integer damPartnerId) {
		try {
			boolean validUserListId = userListId != null && userListId > 0;
			boolean validUserId = userId != null && userId > 0;
			boolean validDamPartnerId = damPartnerId != null && damPartnerId > 0;
			if (validUserListId && validUserId && validDamPartnerId) {
				Session session = sessionFactory.getCurrentSession();
				org.hibernate.Criteria criteria = session.createCriteria(DamPartnerGroupMapping.class);
				criteria.add(Restrictions.eq(USER_LIST_ID, userListId));
				criteria.add(Restrictions.eq(USER_ID, userId));
				criteria.add(Restrictions.eq("damPartner.id", damPartnerId));
				return (DamPartnerGroupMapping) criteria.uniqueResult();
			} else {
				return null;
			}

		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public boolean isRowExistsByUserListId(Integer userListId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select case when count(*)>0 then true else false end as rowExists	from xt_dam_partner_group_mapping  where user_list_id =:userListId";
		return (boolean) session.createSQLQuery(queryString).setParameter(USER_LIST_ID, userListId).uniqueResult();
	}

	@Override
	public boolean isDuplicateAssetName(String assetName, Integer companyId) {
		boolean isDuplicateAssetName = false;
		Session session = sessionFactory.getCurrentSession();
		String sql = "select cast(count(*) as integer) from xt_dam d where lower(TRIM(d.asset_name)) = LOWER(TRIM(:assetName)) and company_id=:companyId";
		Integer count = (Integer) session.createSQLQuery(sql).setParameter(COMPANY_ID, companyId)
				.setParameter(ASSET_NAME, assetName).uniqueResult();
		if (count > 0) {
			isDuplicateAssetName = true;
		}
		return isDuplicateAssetName;
	}

	@Override
	public boolean isDuplicateAssetName(String assetName, Integer companyId, Integer damId) {
		boolean isDuplicateAssetName = false;
		Session session = sessionFactory.getCurrentSession();
		String sql = "select cast(count(*) as integer) from xt_dam d where lower(TRIM(d.asset_name)) = LOWER(TRIM(:assetName)) and d.company_id=:companyId and d.id!=:damId";
		Integer count = (Integer) session.createSQLQuery(sql).setParameter(COMPANY_ID, companyId)
				.setParameter(ASSET_NAME, assetName).setParameter(DAM_ID, damId).uniqueResult();
		if (count > 0) {
			isDuplicateAssetName = true;
		}
		return isDuplicateAssetName;
	}

	@Override
	public void updateDamProcessingStatus(Integer id, Integer userId) {
		Session session = sessionFactory.getCurrentSession();
		String status = "'" + DamStatusEnum.COMPLETED.name() + "'";
		String queryString = "update xt_dam set asset_status=" + status
				+ ", updated_time = :updatedTime, updated_by = :updatedBy where id=:id";
		Query query = session.createSQLQuery(queryString);
		query.setParameter(UPDATED_BY, userId);
		query.setParameter("id", id);
		query.setParameter(UPDATED_TIME, new Date());
		query.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> listChannelAssets(Pagination pagination, List<Integer> videoIds) {
		HashMap<String, Object> map = new HashMap<>();
		Session session = sessionFactory.getCurrentSession();
		String searchSql = "";
		String searchKey = pagination.getSearchKey();
		String sortQueryString = getSelectedSortOptionForAssets(pagination);
		if (searchKey != null && searchKey.length() > 0) {
			searchSql = " and ( LOWER(d.asset_name) like LOWER('%" + searchKey
					+ "%') or LOWER(u.email_id) like LOWER('%" + searchKey + "%') or "
					+ " LOWER(u.firstname) like LOWER('%" + searchKey + "%')  or LOWER(u.lastname) like LOWER('%"
					+ searchKey + "%') or " + " LOWER(d.asset_type) like LOWER('%" + searchKey
					+ "%') or   LOWER(t.tag_name) like LOWER('%" + searchKey + "%')  ) ";
		}
		String sql = "WITH tag AS (SELECT d.id as \"dam_id\", case when d.video_id is null then string_agg(DISTINCT t.tag_name, ',') else "
				+ " string_agg(DISTINCT vt.video_tags, ',') end AS \"tag_names_string\" FROM xt_dam d LEFT JOIN public.xt_dam_tag dt on "
				+ " dt.dam_id=d.id LEFT JOIN xt_tag t on t.id=dt.tag_id LEFT JOIN  xt_video_files v on v.id =d.video_id LEFT JOIN "
				+ " xt_video_tags vt on v.id = vt.video_id GROUP BY d.id ), dam AS(select distinct d.id, case when d.video_id is null "
				+ " then d.asset_name else v.title end as asset_name, case when d.video_id is null then d.created_time else v.uploaded_time end "
				+ " as created_time, case when d.video_id is null then d.updated_time else v.uploaded_time end as updated_time, case when d.video_id is null "
				+ " then d.thumbnail_path else v.image_uri end as thumbnail_path, case when d.video_id is null then d.asset_path  else v.videouri end "
				+ " as asset_path, case when d.video_id is null then d.created_by  else v.customer_id end as created_by, case when d.video_id is null "
				+ " then d.alias  else v.video_alias end as alias, case when d.video_id is null then null else v.view_by end as view_by, "
				+ " case when d.video_id is null then null else v.video_id end as video_id, "
				+ " case when d.video_id is null then null else v.video_length end as video_length, "
				+ " case when d.video_id is null then null else v.views end as total_views, "
				+ " case when d.video_id is null then false else v.is_processed end as is_processed, "
				+ " case when d.video_id is null then null else v.company_profile_name end as company_name, "
				+ "d.asset_type, d.page_size, "
				+ " d.page_orientation, d.is_bee_template, d.version, d.asset_status,  u.firstname, u.lastname,u.middle_name, u.email_id from xt_dam d JOIN xt_user_profile u "
				+ " ON d.created_by = u.user_id and  d.parent_id is null LEFT JOIN  xt_dam_tag dt ON dt.dam_id=d.id LEFT JOIN xt_tag t on t.id=dt.tag_id "
				+ " LEFT JOIN v_manage_videos v on v.video_id =d.video_id WHERE d.video_id in  (:videoIds)" + searchSql
				+ ") " + " SELECT d.id as \"id\",d.asset_name as \"assetName\", "
				+ " d.created_time as \"createdTime\",d.updated_time as  \"updatedTime\", d.alias as \"alias\", d.asset_type as \"assetType\", d.thumbnail_path as \"thumbnailPath\", "
				+ " d.asset_path as \"assetPath\",  d.page_size as \"pageSize\", d.page_orientation as \"pageOrientation\", d.is_bee_template as \"beeTemplate\", "
				+ " d.version as \"templateVersion\", d.asset_status as \"status\",  concat(d.firstname, ' ', d.lastname, ' ',d.middle_name) as \"fullName\", d.email_id as \"emailId\", "
				+ " d.video_id as \"videoId\", d.video_length as \"videoLength\", d.total_views as \"totalViews\", d.is_processed as \"isProcessed\", d.company_name as \"companyName\","
				+ " d.created_by as \"createdBy\",d.view_by as \"viewBy\" ,t.tag_names_string as \"tagNamesString\" FROM dam d LEFT JOIN tag t on d.id=t.dam_id  "
				+ sortQueryString;
		Query query = session.createSQLQuery(sql);
		query.setParameterList("videoIds", videoIds);
		ScrollableResults scrollableResults = query.scroll();
		scrollableResults.last();
		Integer totalRecords = scrollableResults.getRowNumber() + 1;
		query.setFirstResult((pagination.getPageIndex() - 1) * pagination.getMaxResults());
		query.setMaxResults(pagination.getMaxResults());
		List<DamListDTO> assets = query.setResultTransformer(Transformers.aliasToBean(DamListDTO.class)).list();
		List<DamListDTO> updatedAssets = new ArrayList<>();
		for (DamListDTO asset : assets) {
			DamListDTO updatedAsset = new DamListDTO();
			VideoFileDTO videoFileDTO = new VideoFileDTO();
			BeanUtils.copyProperties(asset, updatedAsset);
			String createdTimeInUTCString = DateUtils.getUtcString(asset.getCreatedTime());
			updatedAsset.setCreatedDateInUTCString(createdTimeInUTCString);
			String thumbnailPath = asset.getVideoId() != null ? serverPath + asset.getThumbnailPath()
					: asset.getThumbnailPath();
			if (asset.getVideoId() != null) {
				videoFileDTO.setId(asset.getVideoId());
				videoFileDTO.setTitle(asset.getAssetName());
				videoFileDTO.setImagePath(thumbnailPath);
				videoFileDTO.setVideoLength(asset.getVideoLength());
				videoFileDTO.setViews(asset.getTotalViews());
				videoFileDTO.setViewBy(asset.getViewBy());
				videoFileDTO.setUploadedUserName(asset.getDisplayName());
				videoFileDTO.setAlias(asset.getAlias());
				videoFileDTO.setProcessed(asset.isProcessed());
				videoFileDTO.setCompanyName(asset.getCompanyName());
				updatedAsset.setVideoFileDTO(videoFileDTO);
			}
			updatedAsset.setThumbnailPath(thumbnailPath);
			updatedAssets.add(updatedAsset);
		}
		map.put(totalRecordsStringKey, totalRecords);
		map.put("assets", updatedAssets);
		return map;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getContentAccessCompanyIds() {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select distinct ma.company_id from xt_user_profile up, xt_user_role ur, xt_module_access ma where "
				+ " ma.video = true and ma.company_id= up.company_id and ur.role_id in (2, 13, 18, 20) "
				+ " and up.user_id=ur.user_id order by ma.company_id ";
		return session.createSQLQuery(sql).list();

	}

	@Override
	public void updateAssetDetails(Integer id, String assetName, Integer updatedBy, Date updatedTime,
			boolean shareAsWhiteLabeledAsset, boolean isAddedToQuickLinks) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String queryString = "update xt_dam set asset_name=TRIM(:assetName), is_white_labeled_asset_shared_with_partners=:shareAsWhiteLabeledAsset, updated_by="
					+ updatedBy + ", is_added_to_quick_links = :addedToQuickLinks where id=" + id;
			Query query = session.createSQLQuery(queryString).setParameter(ASSET_NAME, assetName)
					.setParameter("shareAsWhiteLabeledAsset", shareAsWhiteLabeledAsset)
					.setParameter("addedToQuickLinks", isAddedToQuickLinks);
			query.executeUpdate();
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	/** XNFR-884 **/
	@Override
	public void updateVideoAssetDetails(VideoFileUploadForm videoFileUploadForm, Date updatedTime, Integer updatedBy,
			boolean updateApprovalStatus, String approvalStatusTypeInString) {
		if (XamplifyUtils.isValidInteger(videoFileUploadForm.getDamId())) {
			String sqlQueryString = "update xt_dam set asset_name=TRIM(:assetName), is_white_labeled_asset_shared_with_partners = :shareAsWhiteLabeledAsset, "
					+ "updated_by = :updatedBy, is_added_to_quick_links = :addedToQuickLinks {updateApprovalStatusForDraftAssetMergeTag}, slug = TRIM(:slug)  where id = :damId";

			if (updateApprovalStatus && XamplifyUtils.isValidString(approvalStatusTypeInString)) {
				sqlQueryString = sqlQueryString.replace(UPDATE_APPROVAL_STATUS_FOR_DRAFTS_MERGE_TAG,
						", approval_Status = cast(:approvalStatusType as approval_status_type), approval_status_updated_by = :updatedBy, approval_status_updated_time = :updatedTime");
			} else {
				sqlQueryString = sqlQueryString.replace(UPDATE_APPROVAL_STATUS_FOR_DRAFTS_MERGE_TAG, "");
			}

			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(sqlQueryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(ASSET_NAME, videoFileUploadForm.getTitle()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("slug", videoFileUploadForm.getSlug()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(
					"shareAsWhiteLabeledAsset", videoFileUploadForm.isShareAsWhiteLabeledAsset()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(UPDATED_BY, updatedBy));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("addedToQuickLinks", videoFileUploadForm.isAddedToQuickLinks()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(DAM_ID, videoFileUploadForm.getDamId()));
			if (updateApprovalStatus && XamplifyUtils.isValidString(approvalStatusTypeInString)) {
				hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
						.add(new QueryParameterDTO(UPDATED_TIME, updatedTime));
				hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
						.add(new QueryParameterDTO("approvalStatusType", approvalStatusTypeInString));
			}
			hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
		}
	}

	@Override
	public String getApprovalStatusById(Integer damId) {
		if (!XamplifyUtils.isValidInteger(damId)) {
			return "";
		}

		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "select approval_status from xt_dam where id = :damId";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(DAM_ID, damId));
		String approvalStatus = (String) hibernateSQLQueryResultUtilDao
				.getUniqueResult(hibernateSQLQueryResultRequestDTO);
		return XamplifyUtils.isValidString(approvalStatus) ? approvalStatus : "";
	}

	public boolean isAssociatedWithLMS(Integer damId, boolean checkIsPublishedCondition) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select case when count(*)>0 then true else false end as lmsCount from xt_learning_track l, xt_learning_track_content ltc "
				+ " where l.id = ltc.learning_track_id and ltc.dam_id =:damId ";
		if (checkIsPublishedCondition) {
			sql += " and l.is_published = true ";
		}
		return (boolean) session.createSQLQuery(sql).setParameter(DAM_ID, damId).uniqueResult();

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Object[]> getVideoTagsDetails() {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select distinct v.company_id, string_agg(distinct vt.video_tags, ', '), max(v.customer_id) as customerId, max(v.uploaded_time) as uploadedTime "
				+ " from v_manage_videos v, xt_video_tags vt where  v.video_id=vt.video_id "
				+ " and length(vt.video_tags) > 0 group by v.company_id order by v.company_id";
		return session.createSQLQuery(sql).list();
	}

	public boolean isTagExists(String tagValue, Integer companyId) {
		tagValue = tagValue.replaceAll("'", "''");
		boolean isTagExists = false;
		Session session = sessionFactory.getCurrentSession();
		String sql = "select cast(count(*) as integer) from xt_tag where LOWER(tag_name)='" + tagValue.toLowerCase()
				+ "' and company_id=" + companyId;
		Query query = session.createSQLQuery(sql);
		Integer count = (Integer) query.uniqueResult() != null ? (Integer) query.uniqueResult() : 0;
		if (count > 0) {
			isTagExists = true;
		}
		return isTagExists;
	}

	@SuppressWarnings("unchecked")
	public List<String> getDamTags(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String hql = "select tagName from Tag where companyProfile.id=" + companyId;
		return session.createQuery(hql).list();
	}

	public Object[] getAssetValuesById(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select d.asset_name,d.description,d.thumbnail_path,d.asset_path,d.is_bee_template, d.published_time, "
				+ " string_agg(cast(t.id as text), ','), case when d.parent_id is not null then true else false end,"
				+ " d.is_white_labeled_asset_shared_with_partners,d.asset_type,d.is_added_to_quick_links,d.is_partner_signature_required,d.is_vendor_signature_required,d.is_vendor_signature_completed,d.approval_status, d.created_by,d.json_body,d.html_body, d.is_vendor_signature_required_after_partner_signature, d.slug from xt_dam d left join xt_dam_tag dt on dt.dam_id=d.id LEFT JOIN "
				+ " xt_tag t on t.id=dt.tag_id where d.id =" + id + " group by d.id";
		Query query = session.createSQLQuery(sql);
		return (Object[]) query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getUsersByDamId(Integer damId, Integer userId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String sql = "select distinct u.user_id from xt_user_profile u left join xt_dam d on d.company_id = u.company_id where d.id  =:damId and u.company_id = (select company_id from xt_user_profile where user_id ="
					+ userId + ") or u.user_id = 1";
			Query query = session.createSQLQuery(sql);
			query.setParameter(DAM_ID, damId);
			return query.list();
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DamPatchDTO> findDamContentForPatch() {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select distinct d.id as \"damId\",d.asset_name as \"assetName\",d.company_id as \"companyId\",d.created_by as \"createdBy\",c.id as \"categoryId\",cast(c.name as text) as \"categoryName\""
				+ " from xt_dam d left join xt_category c on c.company_id = d.company_id and c.is_default order by d.company_id asc ";
		Query query = session.createSQLQuery(queryString);
		return (List<DamPatchDTO>) paginationUtil.getListDTO(DamPatchDTO.class, query);
	}

	@Override
	public Integer getDamIdByVideoId(Integer videoId) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select id from xt_dam where video_id = :videoId";
		return (Integer) session.createSQLQuery(sql).setParameter(VIDEO_ID, videoId).uniqueResult();
	}

	@Override
	public Integer getParentTemplateCategoryIdByHistoryTemplateId(Integer historyTemplateId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlString = "select distinct cm.category_id from xt_category_module cm,xt_dam d where d.parent_id = cm.dam_id and d.id = :historyTemplateId and d.parent_id is not null";
		return (Integer) session.createSQLQuery(sqlString).setParameter("historyTemplateId", historyTemplateId)
				.uniqueResult();
	}

	@Override
	public Integer getBeeTemplateCategoryIdByDamId(Integer damId) {
		Integer categoryId = 0;
		Session session = sessionFactory.getCurrentSession();
		String parentTemplateQueryString = "select parent_id from xt_dam where id = :damId";
		Integer parentId = (Integer) session.createSQLQuery(parentTemplateQueryString).setParameter(DAM_ID, damId)
				.uniqueResult();
		if (parentId != null) {
			categoryId = categoryDao.getCategoryIdByType(parentId, CategoryModuleEnum.DAM.name());
		} else {
			categoryId = categoryDao.getCategoryIdByType(damId, CategoryModuleEnum.DAM.name());
		}
		return categoryId;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> findFileTypes(Integer companyId, Integer categoryId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlString = "";
		boolean validCategoryId = categoryId != null && categoryId > 0;
		if (validCategoryId) {
			sqlString = "select distinct d.asset_type from xt_dam d,xt_category_module cm where d.company_id = :companyId and d.id = cm.dam_id and cm.category_id = :categoryId"
					+ " order by d.asset_type asc";

		} else {
			sqlString = "select distinct asset_type from xt_dam where company_id = :companyId order by asset_type asc";
		}
		Query query = session.createSQLQuery(sqlString);
		query.setParameter(COMPANY_ID, companyId);
		setValidCategoryIdQueryParameter(categoryId, validCategoryId, query);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> findFileTypesForSharedAssetsByPartnerCompanyId(Integer partnerCompanyId, Integer categoryId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlString = "";
		boolean validCategoryId = categoryId != null && categoryId > 0;
		if (validCategoryId) {
			sqlString = "select distinct d.asset_type from xt_dam d,xt_partnership p,xt_dam_partner dp,xt_category_module cm \r\n"
					+ " where d.company_id = p.vendor_company_id and p.vendor_company_id = d.company_id\r\n"
					+ " and p.partner_company_id = :partnerCompanyId and dp.dam_id = d.id and dp.partnership_id = p.id \r\n"
					+ " and p.vendor_company_id is not null and d.id = cm.dam_id and cm.category_id = :categoryId  order by 1 asc";
		} else {
			sqlString = "select distinct d.asset_type from xt_dam d,xt_partnership p,xt_dam_partner dp\r\n"
					+ " where d.company_id = p.vendor_company_id and p.vendor_company_id = d.company_id\r\n"
					+ " and p.partner_company_id = :partnerCompanyId and dp.dam_id = d.id and dp.partnership_id = p.id \r\n"
					+ " and p.vendor_company_id is not null   order by 1 asc";
		}

		Query query = session.createSQLQuery(sqlString);
		setPartnerCompanyIdQueryParameter(partnerCompanyId, query);
		setValidCategoryIdQueryParameter(categoryId, validCategoryId, query);
		return query.list();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> findFileTypesForSharedAssetsByVendorCompanyIdAndPartnerCompanyId(Integer vendorCompanyId,
			Integer partnerCompanyId, Integer categoryId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlString = "";
		boolean validCategoryId = categoryId != null && categoryId > 0;
		if (validCategoryId) {
			sqlString = "select distinct d.asset_type from xt_dam d,xt_partnership p,xt_dam_partner dp,xt_category_module cm \r\n"
					+ "where d.company_id = p.vendor_company_id and p.vendor_company_id = d.company_id\r\n"
					+ " and p.partner_company_id = :partnerCompanyId and p.vendor_company_id = :vendorCompanyId and dp.dam_id = d.id and dp.partnership_id = p.id \r\n"
					+ " and p.vendor_company_id is not null and d.id = cm.dam_id and cm.category_id = :categoryId  order by 1 asc";
		} else {
			sqlString = "select distinct d.asset_type from xt_dam d,xt_partnership p,xt_dam_partner dp\r\n"
					+ "where d.company_id = p.vendor_company_id and p.vendor_company_id = d.company_id\r\n"
					+ " and p.partner_company_id = :partnerCompanyId and p.vendor_company_id = :vendorCompanyId and dp.dam_id = d.id and dp.partnership_id = p.id \r\n"
					+ " and p.vendor_company_id is not null   order by 1 asc";
		}

		Query query = session.createSQLQuery(sqlString);
		setPartnerCompanyIdQueryParameter(partnerCompanyId, query);
		query.setParameter(VENDOR_COMPANY_ID, vendorCompanyId);
		setValidCategoryIdQueryParameter(categoryId, validCategoryId, query);
		return query.list();
	}

	private void setPartnerCompanyIdQueryParameter(Integer partnerCompanyId, Query query) {
		query.setParameter("partnerCompanyId", partnerCompanyId);
	}

	private void setValidCategoryIdQueryParameter(Integer categoryId, boolean validCategoryId, Query query) {
		if (validCategoryId) {
			query.setParameter(CATEGORY_ID, categoryId);
		}
	}

	/********** XNFR-255 ********/
	@Override
	public DamVideoDTO findDamAndVideoDetailsByVideoId(Integer videoId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlString = "select d.id as \"damId\", d.is_added_to_quick_links as \"addedToQuickLinks\", d.company_id as \"companyId\",d.asset_name as \"assetName\",d.asset_type as \"assetType\",cast(d.asset_status as text) as \"assetStatus\",d.is_white_labeled_asset_shared_with_partners as \"shareAsWhiteLabeledAsset\",  vf.id as \"videoId\",vf.title as \"videoTitle\",vf.videouri as \"videoUri\",vf.description as \"description\",cast(vf.video_status as text) as \"videoStatus\",cast(vf.view_by as text) as \"viewBy\",\r\n"
				+ "vf.big_thumbnail_image as \"bigThumbnailImage\", vf.video_id as \"videoIdAsString\", vf.imageuri as \"imageUri\",vf.video_length as \"videoLength\",vf.video_size as \"videoSize\",vf.bitrate as \"bitRate\",vf.gifuri as \"gifUri\",vf.is_processed as \"processed\",vf.is_custom_thumbnail_uploaded as \"customThumbnailUploaded\", \r\n"
				+ "vc.id as \"videoControlId\", vc.player_color as \"playerColor\", vc.enable_videocontroller as \"enableVideoController\", vc.controller_color as \"controllerColor\",vc.allow_sharing as \"allowSharing\", vc.enable_settings as \"enableSettings\",vc.allow_fullscreen as \"allowFullScreen\", "
				+ "vc.allow_comments as \"allowComments\", vc.allow_likes as \"allowLikes\", vc.enable_casting \"enableCasting\",vc.allow_embed as \"allowEmbed\", vc.transparency as \"transparency\",vc.is_360video as \"is360Video\","
				+ " vc.default_setting as \"defaultSetting\",vc.branding_logo_uri as \"brandingLogoUri\", vc.branding_logo_desc_uri as \"brandingLogoDescUri\","
				+ " vc.enable_video_cobranding_logo as \"enableVideoCobrandingLogo\", vi.image1 as \"image1\", vi.image2 as \"image2\", vi.image3 as \"image3\","
				+ " vi.gif1 as \"gif1\", vi.gif2 as \"gif2\", vi.gif3 as \"gif3\",string_agg(vt.video_tags, ',') as \"tagsInString\","
				+ "ca.id as \"callToActionId\", ca.name as \"callToActionName\",  ca.skip as \"skip\",ca.uppertext as \"upperText\", ca.lowertext as \"lowerText\", ca.start_of_video as \"startOfVideo\","
				+ "ca.end_of_video as \"endOfVideo\",ca.call_action as \"callAction\", d.approval_status as \"approvalStatus\", d.created_by as \"createdBy\", d.slug as \"slug\"  from xt_dam d,xt_video_control vc,xt_video_image vi,xt_video_files vf \r\n"
				+ " left join xt_video_tags vt on vt.video_id = vf.id left join xt_call_action ca on ca.id =vf.id"
				+ " where vf.id = d.video_id and vc.id = vf.id and vi.id = vf.id and vf.id = :videoId\r\n"
				+ " group by d.id,vf.id,vc.id,vi.id,ca.id";
		Query query = session.createSQLQuery(sqlString);
		query.setParameter(VIDEO_ID, videoId);
		DamVideoDTO damVideoDTO = (DamVideoDTO) paginationUtil.getDto(DamVideoDTO.class, query);
		addTags(damVideoDTO);
		return damVideoDTO;
	}

	/********** XNFR-255 ********/
	private void addTags(DamVideoDTO damVideoDTO) {
		if (damVideoDTO != null && StringUtils.hasText(damVideoDTO.getTagsInString())) {
			List<String> tagsArray = Arrays.asList(damVideoDTO.getTagsInString().split(","));
			List<String> tags = new ArrayList<>();
			for (String tag : tagsArray) {
				tags.add(tag);
			}
			damVideoDTO.setTags(tags);
		}
	}

	@Override
	public DamDTO findCreatedUserIdAndWhiteLabeledOptionsByDamId(Integer damId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlString = "select ma.share_white_labeled_content as \"shareWhiteLabelContentAccess\",d.is_white_labeled_asset_shared_with_partners as \"sharedWithPartnersAsAWhiteLabeledAsset\" \r\n"
				+ "from xt_dam d,xt_module_access ma where ma.company_id = d.company_id and d.id=:damId";
		Query query = session.createSQLQuery(sqlString);
		query.setParameter(DAM_ID, damId);
		return (DamDTO) paginationUtil.getDto(DamDTO.class, query);
	}

	/******* XNFR-255 *****/
	@Override
	public void publishAsset(DamUploadPostDTO damUploadPostDTO) {
		Integer damId = damUploadPostDTO.getDamId();
		Session session = sessionFactory.getCurrentSession();
		Dam dam = getByIdAndSession(damId, session);
		CompanyProfile companyProfile = dam.getCompanyProfile();
		String companyName = companyProfile != null ? companyProfile.getCompanyName() : "";
		damUploadPostDTO.setCompanyName(companyName);
		publishAndShareWhiteLabelAsset(dam, damUploadPostDTO, session);

	}

	/******* XNFR-255 *****/
	private void iteratePartnersAndPublishAndWhiteLabelTheAsset(DamUploadPostDTO damUploadPostDTO, Session session,
			Dam dam, Integer loggedInUserCompanyId, List<UserListAndUserId> userListAndUserIdDtos,
			List<Integer> partnerIds, List<Integer> uniquePartnerShipIds) {
		int i = 0;
		List<Integer> updatedPartnerIds = new ArrayList<>();
		int total = uniquePartnerShipIds.size();
		Path filePathToDelete = null;
		Map<Integer, LinkedList<Integer>> damPartnerIds = new LinkedHashMap<>();
		Transaction tx = session.beginTransaction();
		UserDTO vendorData = getSenderCompanyInfo(damUploadPostDTO.getLoggedInUserId(), session);
		for (Integer partnershipId : uniquePartnerShipIds) {
			if (!damUploadPostDTO.isNewlyAddedPartnerFromPartnerList() && !damUploadPostDTO.isReplaceVideoAsset()) {
				DamPartner damPartner = new DamPartner();
				Integer existingDamPartnerId = getDamPartnerIdByDamIdAndPartnershipId(dam.getId(), partnershipId);
				if (existingDamPartnerId != null) {
					damPartner.setId(existingDamPartnerId);
				}

				if (filePathToDelete == null && dam.getAssetType().equals("pdf") && !dam.isBeeTemplate()
						&& (!XamplifyUtils.isValidString(damUploadPostDTO.getAssetPath())
								|| damUploadPostDTO.isReplaceAsset())) {
					filePathToDelete = Paths.get(damUploadPostDTO.getCopiedAssetFilePath());
				}

				insertIntoDamPartner(damUploadPostDTO, session, dam, partnershipId, damPartner, existingDamPartnerId,
						damPartnerIds, vendorData);

				if (damUploadPostDTO.isPartnerGroupSelected()) {
					insertIntoDamPartnerGroupMapping(session, loggedInUserCompanyId, userListAndUserIdDtos,
							updatedPartnerIds, partnershipId, damPartner, existingDamPartnerId);
				} else {
					insertIntoDamPartnerMapping(session, loggedInUserCompanyId, partnerIds, updatedPartnerIds,
							partnershipId, damPartner, existingDamPartnerId);
				}

			}
			handleWhiteLabeledAssetSharing(damUploadPostDTO, session, dam, partnershipId);
//			if (i % 30 == 0) {
			session.flush();
			session.clear();
			tx.commit();
			tx = session.beginTransaction();
//			}
			i++;
			int itemsLeft = total - i;
			String message = "Dam:-" + dam.getAssetName() + "(" + dam.getId() + ") Partnership Id:-" + partnershipId
					+ " Partners Left:-" + itemsLeft + "-" + new Date();
			logger.debug(message);

		}
		if (filePathToDelete != null) {
			try {
				Files.delete(filePathToDelete);
				String debugMessage = filePathToDelete + " deleted successfully";
				logger.debug(debugMessage);
			} catch (IOException e) {
				logger.error("! Error Caught In Deleting File As The File Is Not Present !", e);
			}
		}
	}

	/**** XNFR-255 ****/
	@Override
	public void handleWhiteLabeledAssetSharing(DamUploadPostDTO damUploadPostDTO, Session session, Dam dam,
			Integer partnershipId) {
		boolean isSharedWithPartnerAsAWhiteLabeledAssetOptionEnabled = damUploadPostDTO.isShareAsWhiteLabeledAsset();
		if (isSharedWithPartnerAsAWhiteLabeledAssetOptionEnabled) {
			PartnershipDTO partnershipDTO = partnershipDao
					.findPartnerIdAndPartnerCompanyIdByPartnershipId(partnershipId);
			if (partnershipDTO != null) {
				Integer partnerId = partnershipDTO.getId();
				boolean isAnyAdminCompany = utilDao.isAnyVendorAdminCompany(partnerId);
				Integer partnerCompanyId = partnershipDTO.getPartnerCompanyId();

				if (isAnyAdminCompany && partnerCompanyId != null) {
					Integer vendorCompanyId = dam.getCompanyProfile().getId();
					boolean isAssetSharedWithPartner = whiteLabeledAssetDao.isWhiteLabeledAssetSharedWithPartnerCompany(
							dam.getId(), vendorCompanyId, partnerCompanyId);
					if (!isAssetSharedWithPartner || damUploadPostDTO.isReplaceAsset()) {

						/** XNFR-748 ***/
						session = getSession(session);

						CompanyProfile partnerCompany = new CompanyProfile();
						partnerCompany.setId(partnerCompanyId);

						Dam whiteLabeledDam = saveIntoDam(damUploadPostDTO, session, dam, partnerId, partnerCompanyId,
								partnerCompany);

						saveIntoCategory(partnerId, partnerCompanyId, whiteLabeledDam);

						saveWhiteLabeledAsset(dam, session, vendorCompanyId, whiteLabeledDam, partnerCompany);

						/** XNFR-781 **/
						commentDao.createApprovalStatusHistory(whiteLabeledDam.getId(), partnerId, ModuleType.DAM);

					}

				}
			}
		}

	}

	private Session getSession(Session session) {
		if (session == null) {
			session = sessionFactory.getCurrentSession();
		}
		return session;
	}

	private Dam saveIntoDam(DamUploadPostDTO damUploadPostDTO, Session session, Dam dam, Integer partnerId,
			Integer partnerCompanyId, CompanyProfile partnerCompany) {
		Dam whiteLabeledDam = new Dam();
		/**** XBI-1910 ****/
		String vendorCompanyProfileName = dam.getCompanyProfile().getCompanyProfileName();
		String whiteLabledAssetName = XamplifyUtils.getWhiteLabeledName(dam.getAssetName(), vendorCompanyProfileName);
		whiteLabeledDam.setAssetName(whiteLabledAssetName);
		whiteLabeledDam.setSlug(whiteLabledAssetName.replace(" ", "_"));
		/**** XBI-1910 ****/
		User partner = new User();
		partner.setUserId(partnerId);

		/****** XBI-1762 *****/
		setWhiteLabeledVideoDTOProperties(damUploadPostDTO, session, whiteLabeledDam, partner);
		/****** XBI-1762 *****/

		setWhiteLabeledDamProperties(dam, partnerId, partnerCompanyId, whiteLabeledDam, partnerCompany);

		/** XNFR-781 **/
		setAssetApprovalStatus(partnerId, partnerCompanyId, whiteLabeledDam, damUploadPostDTO.isDraft());

		session.save(whiteLabeledDam);

		return whiteLabeledDam;
	}

	private void saveIntoCategory(Integer partnerId, Integer partnerCompanyId, Dam whiteLabeledDam) {
		Integer categoryId = categoryDao.getDefaultCategoryIdByCompanyId(partnerCompanyId);
		saveIntoCategory(partnerId, whiteLabeledDam, partnerCompanyId, categoryId);
	}

	private void setWhiteLabeledDamProperties(Dam dam, Integer partnerId, Integer partnerCompanyId, Dam whiteLabeledDam,
			CompanyProfile partnerCompany) {
		whiteLabeledDam.setCompanyProfile(partnerCompany);
		String whiteLabeledAssetPath = amazonWebService.copyAssetImageFromOneFolderToAnotherFolder(dam.getAssetPath(),
				partnerCompanyId);
		whiteLabeledDam.setAssetPath(whiteLabeledAssetPath);
		setWhiteLabeledThumbnailPath(dam, partnerCompanyId, whiteLabeledDam);
		whiteLabeledDam.setDescription(dam.getDescription());
		whiteLabeledDam.setAssetType(dam.getAssetType());
		whiteLabeledDam.setAlias(XamplifyUtils.generateAlias());
		whiteLabeledDam.setBeeTemplate(dam.isBeeTemplate());
		whiteLabeledDam.setJsonBody(dam.getJsonBody());
		whiteLabeledDam.setHtmlBody(dam.getHtmlBody());
		whiteLabeledDam.setPageSize(dam.getPageSize());
		whiteLabeledDam.setPageOrientation(dam.getPageOrientation());
		whiteLabeledDam.setCreatedTime(new Date());
		whiteLabeledDam.setCreatedBy(partnerId);
		whiteLabeledDam.setDamStatusEnum(DamStatusEnum.COMPLETED);
		whiteLabeledDam.setImageGeneratedSuccessfully(dam.isImageGeneratedSuccessfully());
		if (whiteLabeledDam.isImageGeneratedSuccessfully()) {
			whiteLabeledDam.setImageGeneratedOn(new Date());
		}
	}

	private void setWhiteLabeledThumbnailPath(Dam dam, Integer partnerCompanyId, Dam whiteLabeledDam) {
		String whiteLabeledThumbnailPath = "";
		String assetThumbnailPath = dam.getThumbnailPath();
		if (StringUtils.hasText(assetThumbnailPath)) {
			if (dam.getThumbnailPath().contains("assets/images")) {
				whiteLabeledThumbnailPath = dam.getThumbnailPath();
			} else {
				whiteLabeledThumbnailPath = amazonWebService
						.copyAssetThumbnailImageFromOneFolderToAnotherFolder(dam.getThumbnailPath(), partnerCompanyId);
			}
		}
		whiteLabeledDam.setThumbnailPath(whiteLabeledThumbnailPath);
	}

	private void setWhiteLabeledVideoDTOProperties(DamUploadPostDTO damUploadPostDTO, Session session,
			Dam whiteLabeledDam, User partner) {
		DamVideoDTO damVideoDTO = damUploadPostDTO.getDamVideoDTO();
		if (damVideoDTO != null) {
			damVideoDTO.setAssetName(whiteLabeledDam.getAssetName());
			VideoFile videoFile = xamplifyUtil.setWhiteLabeledVideoFileProperties(damVideoDTO, partner);
			session.save(videoFile);
			whiteLabeledDam.setVideoFile(videoFile);
		}
	}

	/************ XNFR-255 **********/
	private void saveWhiteLabeledAsset(Dam dam, Session session, Integer vendorCompanyId, Dam whiteLabeledDam,
			CompanyProfile partnerCompany) {
		WhiteLabeledAsset whiteLabeledAsset = new WhiteLabeledAsset();
		CompanyProfile sharedByVendorCompany = new CompanyProfile();
		sharedByVendorCompany.setId(vendorCompanyId);
		whiteLabeledAsset.setSharedByCompany(sharedByVendorCompany);
		whiteLabeledAsset.setSharedWithCompany(partnerCompany);
		Dam vendorCompanyAsset = new Dam();
		vendorCompanyAsset.setId(dam.getId());
		whiteLabeledAsset.setVendorCompanyAsset(vendorCompanyAsset);
		Dam receivedWhiteLabeledAsset = new Dam();
		receivedWhiteLabeledAsset.setId(whiteLabeledDam.getId());
		whiteLabeledAsset.setReceivedAsset(receivedWhiteLabeledAsset);
		whiteLabeledAsset.setSharedOn(new Date());
		session.save(whiteLabeledAsset);
	}

	/************ XNFR-255 **********/
	private void saveIntoCategory(Integer partnerId, Dam whiteLabeledDam, Integer partnerCompanyId,
			Integer categoryId) {
		CategoryModule categoryModule = new CategoryModule();
		categoryModule.setDamId(whiteLabeledDam.getId());
		categoryModule.setCategoryModuleEnum(CategoryModuleEnum.DAM);
		categoryModule.setCategoryId(categoryId);
		categoryModule.setCreatedTime(new Date());
		categoryModule.setCreatedUserId(partnerId);
		categoryModule.setCompanyId(partnerCompanyId);
		categoryDao.saveCategoryModule(categoryModule);
	}

	/*******
	 * XNFR-255
	 * 
	 * @param damPartnerIds
	 * @param vendorData    TODO
	 *****/
	private void insertIntoDamPartner(DamUploadPostDTO damUploadPostDTO, Session session, Dam dam,
			Integer partnershipId, DamPartner damPartner, Integer exisitingDamPartnerId,
			Map<Integer, LinkedList<Integer>> damPartnerIds, UserDTO vendorData) {
		if (exisitingDamPartnerId == null) {
			Partnership partnership = new Partnership();
			partnership.setId(partnershipId);
			damPartner.setPartnership(partnership);
			damPartner.setDam(dam);
			GenerateRandomPassword password = new GenerateRandomPassword();
			damPartner.setAlias(password.getPassword());
			damPartner.setJsonBody(dam.getJsonBody());
			damPartner.setHtmlBody(dam.getHtmlBody());
			damPartner.setPartnerGroupSelected(damUploadPostDTO.isPartnerGroupSelected());
			damPartner.setPublishedTime(new Date());
			damPartner.setPublishedBy(damUploadPostDTO.getLoggedInUserId());
			if (dam.getAssetType().equals("pdf") && (!XamplifyUtils.isValidString(damUploadPostDTO.getAssetPath())
					|| damUploadPostDTO.isReplaceAsset())) {
//				FilePathAndThumbnailPath filePaths = xamplifyUtil.uploadAssetAndThumbnail(damUploadPostDTO);
//				damPartner.setSharedAssetPath(filePaths.getFilePath());
				handleBeeTemplateSharing(damUploadPostDTO, session, dam, damPartner, vendorData);

			} else if (dam.getAssetType().equals("pdf") && XamplifyUtils.isValidString(damUploadPostDTO.getAssetPath())
					&& !damUploadPostDTO.isReplaceAsset()) {
//				assignAssestPath(damUploadPostDTO, dam, damPartner);
				handleBeeTemplateSharing(damUploadPostDTO, session, dam, damPartner, vendorData);

			}
			session.save(damPartner);
			Integer partnerCompanyId = partnershipDao.getPartnerCompanyIdByPartnershipId(partnershipId);
			if (XamplifyUtils.isValidInteger(damPartner.getId())) {
				if (!damPartnerIds.containsKey(partnerCompanyId)) {
					LinkedList<Integer> damIds = new LinkedList<>();
					damIds.add(damPartner.getId());
					damPartnerIds.put(partnerCompanyId, damIds);
				} else {
					LinkedList<Integer> damIds = damPartnerIds.get(partnerCompanyId);
					damIds.add(damPartner.getId());
				}
				damUploadPostDTO.setDamPartnerIds(damPartnerIds);
			}
		} else {
			if (dam.getAssetType().equals("pdf") && damUploadPostDTO.isReplaceAsset()) {
//				FilePathAndThumbnailPath filePaths = xamplifyUtil.uploadAssetAndThumbnail(damUploadPostDTO);
				updateSharedAssetPathAndPartnerSignatureCompletedFalseForPartner(dam.getAssetPath(),
						exisitingDamPartnerId);
			}
		}
	}

	private void handleBeeTemplateSharing(DamUploadPostDTO damUploadPostDTO, Session session, Dam dam,
			DamPartner damPartner, UserDTO vendorData) {
		if (damUploadPostDTO.isBeeTemplate()) {
			// xamplifyUtil.shareAssestToPartner(dam.getId(),damPartner,damUploadPostDTO,dam.getAssetPath());
			assignAssestPath(damUploadPostDTO, session, dam, damPartner, vendorData);
			damPartner.setSharedAssetPath(damPartner.getSharedAssetPath());
		} else {
			damPartner.setSharedAssetPath(dam.getAssetPath());
		}
	}

	private void assignAssestPath(DamUploadPostDTO damUploadPostDTO, Session session, Dam dam, DamPartner damPartner,
			UserDTO vendorData) {
		try {
			Integer partnershipId = damPartner.getPartnership().getId();
			UserDTO partnerData = getPartnerInfo(partnershipId, session);
			String tempDir = mediaBasePath + "shared-assest/" + partnershipId + "-" + System.currentTimeMillis() + "/";
			String inputPdfPath = tempDir + "input.pdf";
			String outputPdfPath = tempDir + "merged.pdf";
			Files.createDirectories(Paths.get(tempDir));
			xamplifyUtil.downloadAssestPath(dam.getAssetPath(), inputPdfPath);
			xamplifyUtil.replaceAllMergeTagsForDam(inputPdfPath, vendorData, partnerData, outputPdfPath);
			File mergedFile = new File(outputPdfPath);
			if (!mergedFile.exists()) {
				throw new FileNotFoundException("Failed to generate merged PDF: " + outputPdfPath);
			}
			damUploadPostDTO.setCopiedAssetFilePath(mergedFile.getAbsolutePath());
			damUploadPostDTO.setDamId(dam.getId());
			damUploadPostDTO
					.setCompleteAssetFileName(dam.getAssetPath().replace("https://s3.amazonaws.com/xamplify/", ""));
			FilePathAndThumbnailPath filePaths = xamplifyUtil.uploadAssetAndThumbnail(damUploadPostDTO);
			damPartner.setSharedAssetPath(filePaths.getFilePath());
			Files.deleteIfExists(Paths.get(inputPdfPath));
			Files.deleteIfExists(Paths.get(outputPdfPath));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public UserDTO getSenderCompanyInfo(Integer userId, Session session) {
		String queryString = "SELECT xcp.company_name AS \"companyName\", \n"
				+ "CONCAT_WS(', ', COALESCE(xcp.street, ''), COALESCE(xcp.city, ''), COALESCE(xcp.state, ''), COALESCE(xcp.zip, ''), COALESCE(xcp.country, '')) AS \"companyAddress\", \n"
				+ "xcp.email_id AS \"companyEmail\", \n" + "xcp.phone AS \"companyMobile\", \n"
				+ "xup.firstname AS \"firstName\", \n" + "xup.middle_name AS \"middleName\", \n"
				+ "xup.lastname AS \"lastName\", \n"
				+ "CONCAT_WS(' ', COALESCE(xup.firstname, ''), COALESCE(xup.middle_name, ''), COALESCE(xup.lastname, '')) AS \"fullName\", \n"
				+ "xup.job_title AS \"jobTitle\", \n" + "xup.email_id AS \"emailId\", \n"
				+ "xup.mobile_number AS \"mobileNumber\", \n" + "xcp.company_logo AS \"companyLogo\", "
				+ "xcp.website AS \"websiteUrl\", \n" + "xcp.instagram_link AS \"instagramUrl\", \n"
				+ "xcp.twitter_link AS \"twitterUrl\", \n" + "xcp.google_plus_link AS \"googlePlusLink\", \n"
				+ "xcp.facebook_link AS \"facebookLink\", \n" + "xcp.linked_in_link AS \"linkedInLink\", \n"
				+ "CONCAT_WS(', ', COALESCE(xcp.street, ''), COALESCE(xcp.city, ''), COALESCE(xcp.state, ''), COALESCE(xcp.zip, ''), COALESCE(xcp.country, '')) AS \"address\", \n"
				+ "xcp.event_url AS \"eventUrl\", \n" + "xcp.about_us AS \"aboutUs\", \n"
				+ "xcp.privacy_policy AS \"privacyPolicy\" \n" + "FROM xt_user_profile xup \n"
				+ "LEFT JOIN xt_company_profile xcp ON xup.company_id = xcp.company_id \n"
				+ "WHERE xup.user_id = :userId";

		SQLQuery query = session.createSQLQuery(queryString);
		query.setParameter("userId", userId);
		UserDTO userDTO = (UserDTO) query.setResultTransformer(Transformers.aliasToBean(UserDTO.class)).uniqueResult();
		return userDTO;

	}

	public UserDTO getPartnerInfo(Integer partnershipId, Session session) {
		String queryString = "SELECT " + "xcp.company_name AS \"companyName\", " + "xcp.email_id AS \"emailId\", "
				+ "xcp.phone AS \"companyMobile\", " + "xcp.website AS \"websiteUrl\", "
				+ "xcp.about_us AS \"partnerAboutUs\", " + "xcp.company_logo AS \"companyLogo\", " + "CONCAT_WS(', ', "
				+ "COALESCE(xcp.street, ''), " + "COALESCE(xcp.city, ''), " + "COALESCE(xcp.state, ''), "
				+ "COALESCE(xcp.zip, ''), " + "COALESCE(xcp.country, '')) AS \"companyAddress\" "
				+ "FROM xt_partnership xp " + "JOIN xt_user_profile xup ON xp.partner_id = xup.user_id "
				+ "JOIN xt_company_profile xcp ON xup.company_id = xcp.company_id " + "WHERE xp.id = :id";
		SQLQuery query = session.createSQLQuery(queryString);
		query.setParameter("id", partnershipId);
		UserDTO userDTO = (UserDTO) query.setResultTransformer(Transformers.aliasToBean(UserDTO.class)).uniqueResult();
		return userDTO;

	}

	/************ XNFR-255 **********/
	private void insertIntoDamPartnerMapping(Session session, Integer loggedInUserCompanyId, List<Integer> partnerIds,
			List<Integer> updatedPartnerIds, Integer partnershipId, DamPartner damPartner,
			Integer existingDamPartnerId) {
		for (Integer partnerId : partnerIds) {
			Integer partnershipIdByPartnerId = partnershipDao.getPartnershipIdByPartnerCompanyUserId(partnerId,
					loggedInUserCompanyId);
			if (partnershipIdByPartnerId != null && partnershipIdByPartnerId.equals(partnershipId)) {
				DamPartnerMapping existingDamPartnerMapping = findByPartnerIdAndDamPartnerId(partnerId,
						existingDamPartnerId);
				if (existingDamPartnerMapping == null) {
					DamPartnerMapping damPartnerMapping = new DamPartnerMapping();
					damPartnerMapping.setCreatedTime(new Date());
					damPartnerMapping.setDamPartner(damPartner);
					damPartnerMapping.setPartnerId(partnerId);
					updatedPartnerIds.add(partnerId);
					session.save(damPartnerMapping);
				}

			}
		}
	}

	/******* XNFR-255 *****/
	private void insertIntoDamPartnerGroupMapping(Session session, Integer loggedInUserCompanyId,
			List<UserListAndUserId> userListAndUserIdDtos, List<Integer> updatedPartnerIds, Integer partnershipId,
			DamPartner damPartner, Integer existingDamPartnerId) {
		for (UserListAndUserId userListAndUserId : userListAndUserIdDtos) {
			Integer userId = userListAndUserId.getUserId();
			Integer userListId = userListAndUserId.getUserListId();
			Integer partnerShipIdByPartnerId = partnershipDao.findPartnershipIdByPartnerIdAndVendorCompanyId(userId,
					loggedInUserCompanyId);
			if (partnerShipIdByPartnerId != null && partnerShipIdByPartnerId.equals(partnershipId)) {
				DamPartnerGroupMapping existingDamPartnerGroupMapping = findByUserListIdAndUserIdAndDamPartnerId(
						userListId, userId, existingDamPartnerId);
				if (existingDamPartnerGroupMapping == null) {
					DamPartnerGroupMapping damPartnerGroupMapping = new DamPartnerGroupMapping();
					damPartnerGroupMapping.setDamPartner(damPartner);
					damPartnerGroupMapping.setUserId(userId);
					damPartnerGroupMapping.setUserListId(userListId);
					damPartnerGroupMapping.setCreatedTime(new Date());
					updatedPartnerIds.add(userId);
					Integer damPartnerGroupId = (Integer) session.save(damPartnerGroupMapping);

					insertIntoDamPartnerGroupUserMapping(session, partnershipId, userId, damPartnerGroupId);
				}

			}

		}
	}

	private void insertIntoDamPartnerGroupUserMapping(Session session, Integer partnershipId, Integer partnerId,
			Integer damPartnerGroupId) {
		Integer partnerCompanyId = partnershipDao.getPartnerCompanyIdByPartnershipId(partnershipId);
		List<User> partnerCompanyUsers = XamplifyUtils.isValidInteger(partnerCompanyId)
				? userDao.getAllUsersByCompanyId(partnerCompanyId)
				: Collections.singletonList(createPartnerUser(partnerId));
		for (User companyUser : partnerCompanyUsers) {
			DamPartnerGroupUserMapping partnerGroupMapping = new DamPartnerGroupUserMapping();
			DamPartnerGroupMapping damPartnerGroup = new DamPartnerGroupMapping();
			damPartnerGroup.setId(damPartnerGroupId);
			partnerGroupMapping.setDamPartnerGroupMapping(damPartnerGroup);
			Partnership partnership = new Partnership();
			partnership.setId(partnershipId);
			partnerGroupMapping.setPartnership(partnership);
			partnerGroupMapping.setUser(companyUser);
			if (XamplifyUtils.isNotEmptyList(companyUser.getTeamMembers())) {
				partnerGroupMapping.setTeamMember(companyUser.getTeamMembers().get(0));
			}
			partnerGroupMapping.setCreatedTime(new Date());
			session.save(partnerGroupMapping);
		}
	}

	private User createPartnerUser(Integer partnerId) {
		User partnerUser = new User();
		partnerUser.setUserId(partnerId);
		return partnerUser;
	}

	@Override
	public void publishVideoAsset(Dam dam, DamUploadPostDTO damUploadPostDTO) {
		Session session = sessionFactory.getCurrentSession();
		publishAndShareWhiteLabelAsset(dam, damUploadPostDTO, session);

	}

	private void publishAndShareWhiteLabelAsset(Dam dam, DamUploadPostDTO damUploadPostDTO, Session session) {
		Integer loggedInUserCompanyId = userDao.getCompanyIdByUserId(damUploadPostDTO.getLoggedInUserId());
		damUploadPostDTO.setCompanyId(loggedInUserCompanyId);
		List<UserListAndUserId> userListAndUserIdDtos = new ArrayList<>();
		List<Integer> partnerIds = new ArrayList<>();
		List<Integer> uniquePartnerShipIds = new ArrayList<>();
		List<Integer> uniquePartnerIds = new ArrayList<>();
		if (damUploadPostDTO.isPartnerGroupSelected()) {
			List<Integer> partnerGroupIds = XamplifyUtils.convertSetToList(damUploadPostDTO.getPartnerGroupIds());
			if (!damUploadPostDTO.isNewlyAddedPartnerFromPartnerList()) {
				userListAndUserIdDtos = userListDao.findUserIdsAndUserListIds(partnerGroupIds);
				partnerIds.addAll(
						userListAndUserIdDtos.stream().map(UserListAndUserId::getUserId).collect(Collectors.toList()));
			} else {
				partnerIds.addAll(damUploadPostDTO.getPartnerIds());
			}
			if (XamplifyUtils.isNotEmptyList(partnerIds)) {
				uniquePartnerIds.addAll(XamplifyUtils.convertListToSetElements(partnerIds));
				List<Integer> deactivatedPartners = utilDao.findDeactivedPartnersByCompanyId(loggedInUserCompanyId);
				if (XamplifyUtils.isNotEmptyList(deactivatedPartners)) {
					Set<Integer> deactivatedPartnerSet = new HashSet<>(deactivatedPartners);
					uniquePartnerIds.removeIf(deactivatedPartnerSet::contains);
				}
				if (XamplifyUtils.isNotEmptyList(uniquePartnerIds)) {
					List<Integer> partnershipIds = partnershipDao
							.findPartnershipIdsByPartnerIdsAndVendorCompanyId(uniquePartnerIds, loggedInUserCompanyId);
					uniquePartnerShipIds.addAll(XamplifyUtils.convertListToSetElements(partnershipIds));
				}
			}
		} else {
			List<Integer> selectedPartnerIds = XamplifyUtils.convertSetToList(damUploadPostDTO.getPartnerIds());
			if (XamplifyUtils.isNotEmptyList(selectedPartnerIds)) {
				uniquePartnerIds.addAll(XamplifyUtils.convertListToSetElements(selectedPartnerIds));
				List<Integer> deactivatedPartners = utilDao.findDeactivedPartnersByCompanyId(loggedInUserCompanyId);
				if (XamplifyUtils.isNotEmptyList(deactivatedPartners)) {
					Set<Integer> deactivatedPartnerSet = new HashSet<>(deactivatedPartners);
					uniquePartnerIds.removeIf(deactivatedPartnerSet::contains);
				}
				if (XamplifyUtils.isNotEmptyList(uniquePartnerIds)) {
					List<Integer> partnershipIds = partnershipDao
							.getPartnershipIdsByPartnerCompanyUserIds(uniquePartnerIds, loggedInUserCompanyId);
					uniquePartnerShipIds.addAll(XamplifyUtils.convertListToSetElements(partnershipIds));
				}
			}

		}
		damUploadPostDTO.setUpdatedPartnerIds(uniquePartnerIds);
		/********* Insert into related tables *****/
		iteratePartnersAndPublishAndWhiteLabelTheAsset(damUploadPostDTO, session, dam, loggedInUserCompanyId,
				userListAndUserIdDtos, uniquePartnerIds, uniquePartnerShipIds);
		String baseQuery = "update xt_dam set published_time=:publishedOn,is_publishing_or_white_labeling_in_progress= false";
		if (!damUploadPostDTO.isNewlyAddedPartnerFromPartnerList()
				&& XamplifyUtils.isNotEmptyList(uniquePartnerShipIds)) {
			baseQuery += " where id = :damId";
			updateDamPublishStatus(dam.getId(), baseQuery, session);
		} else {
			baseQuery += " ,is_published = false where id = :damId";
			updateDamPublishStatus(dam.getId(), baseQuery, session);
		}
	}

	private void updateDamPublishStatus(Integer damId, String queryString, Session session) {
		Query query = session.createSQLQuery(queryString);
		query.setParameter("publishedOn", new Date());
		query.setParameter(DAM_ID, damId);
		query.executeUpdate();
	}

	@Override
	public boolean isAssetPublished(Integer damId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String sql = "select case when count(*)>0 then true else false end from xt_dam_partner where dam_id = :damId";
			Query query = session.createSQLQuery(sql);
			query.setParameter(DAM_ID, damId);
			if (query.uniqueResult() != null) {
				return (boolean) query.uniqueResult();
			} else {
				return false;
			}
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}

	}

	@Override
	public void updatePublishingOrWhiteLabeledStatus(Integer damId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(
				"update xt_dam set is_publishing_or_white_labeling_in_progress= true where id = :damId");
		query.setParameter(DAM_ID, damId);
		query.executeUpdate();

	}

	@Override
	public Integer getDamPartnerIdByDamIdAndPartnershipId(Integer damId, Integer partnershipId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String sql = "select id from xt_dam_partner where dam_id = :damId and partnership_id = :partnershipId";
			return (Integer) session.createSQLQuery(sql).setParameter(DAM_ID, damId)
					.setParameter(PARTNERSHIP_ID, partnershipId).uniqueResult();
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public void updateWhiteLabelStatus(Integer videoId) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createSQLQuery(
				"update xt_dam set is_white_labeled_asset_shared_with_partners= true where video_id = :videoId");
		query.setParameter(VIDEO_ID, videoId);
		query.executeUpdate();

	}

	/***** XBI-1829 ****/
	@Override
	public List<Integer> findPublishedPartnerUserIdsByDamId(Integer damId) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String sql = "select distinct dpgm.user_id from xt_dam_partner dp,xt_dam_partner_group_mapping dpgm where dpgm.dam_partner_id = dp.id and dp.dam_id = :damId";
			return findPublishedPartnerIdsOrPartnerGroupIds(damId, session, sql);
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	@Override
	public void changeAsParentAsset(Integer assetId, Integer loggedInUserId, Dam dam) {
		try {
			Session session = sessionFactory.getCurrentSession();
			Integer parentId = dam.getParentId();
			Integer version = dam.getVersion();

			String updateParentIdsQueryString = "update xt_dam set parent_id = :assetId where parent_id = :parentId";
			session.createSQLQuery(updateParentIdsQueryString).setParameter(ASSET_ID, assetId)
					.setParameter(PARENT_ID, parentId).executeUpdate();

			if (version != null) {
				String updateParentAsChildQueryString = "update xt_dam set parent_id = :assetId,version = :version where id=:id";
				session.createSQLQuery(updateParentAsChildQueryString).setParameter(ASSET_ID, assetId)
						.setParameter(VERSION, version).setParameter("id", parentId).executeUpdate();
			} else {
				String updateParentAsChildQueryString = "update xt_dam set parent_id = :assetId,version = NULL where id=:id";
				session.createSQLQuery(updateParentAsChildQueryString).setParameter(ASSET_ID, assetId)
						.setParameter("id", parentId).executeUpdate();
			}

			String updateChildAsParentQueryString = "update xt_dam set parent_id = NULL,child_parent_id = NULL, version = 1 where id = :assetId";
			session.createSQLQuery(updateChildAsParentQueryString).setParameter(ASSET_ID, assetId).executeUpdate();
		} catch (HibernateException e) {
			if (e instanceof ConstraintViolationException) {
				String errorMessage = dam.getAssetName()
						+ " is a duplicate asset name.Please change the name and try again.";
				throw new BadRequestException(errorMessage);
			} else {
				throw new DamDataAccessException(e);
			}

		}
	}

	@Override
	public boolean isParentTemplate(Integer damId) {
		String queryString = "select case when (is_bee_template = true and parent_id is null and child_parent_id is null) then true\r\n"
				+ "else false end as parentTemplate from xt_dam where id = :id";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		QueryParameterDTO queryParameterDTO = new QueryParameterDTO("id", damId);
		List<QueryParameterDTO> queryParameterDTOs = new ArrayList<>();
		queryParameterDTOs.add(queryParameterDTO);
		hibernateSQLQueryResultRequestDTO.setQueryParameterDTOs(queryParameterDTOs);
		return hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public Map<String, Object> findAllUnPublishedAndFilteredPublishedAssets(Pagination pagination, String searchKey) {
		String sortQueryString = addSortColumns(pagination);
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(findAssetsByIdsQueryString);
		QueryParameterDTO queryParameterDTO = new QueryParameterDTO(COMPANY_ID, pagination.getCompanyId());
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(queryParameterDTO);
		QueryParameterListDTO queryParameterListDTO = new QueryParameterListDTO("ids", pagination.getIds());
		hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs().add(queryParameterListDTO);
		hibernateSQLQueryResultRequestDTO.setSortQueryString(sortQueryString);
		hibernateSQLQueryResultRequestDTO.setClassInstance(ShareAssetsResponseDTO.class);
		List<String> searchColumns = new ArrayList<>();
		searchColumns.add("u.email_id");
		searchColumns.add("u.firstname");
		searchColumns.add("u.lastname");
		searchColumns.add("d.asset_name");
		searchColumns.add("d.asset_type");
		hibernateSQLQueryResultRequestDTO.setSearchColumns(searchColumns);
		return hibernateSQLQueryResultUtilDao.returnPaginatedDTOList(hibernateSQLQueryResultRequestDTO, pagination,
				searchKey);

	}

	private String addSortColumns(Pagination pagination) {
		List<SortColumnDTO> sortColumnDTOs = new ArrayList<>();
		SortColumnDTO createdTimeSortOption = new SortColumnDTO(createdTimePropertyName, "d.created_time", true, true,
				false);
		SortColumnDTO titleSortOption = new SortColumnDTO(ASSET_NAME, "d.asset_name", false, true, false);
		sortColumnDTOs.add(createdTimeSortOption);
		sortColumnDTOs.add(titleSortOption);
		return paginationUtil.generateSortQuery(pagination, sortColumnDTOs, "desc");
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<PublishedContentIdAndUserListIdDetailsDTO> findAllPublishedAssetsByUserListId(Integer userListId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "select  d.id as \"id\",string_agg(distinct cast(dpgm.user_id as text), ',') as \"userIdsAsString\" \r"
				+ " from xt_dam_partner_group_mapping dpgm,xt_dam d,xt_dam_partner dp \r"
				+ " where d.id = dp.dam_id and dp.id = dpgm.dam_partner_id \r"
				+ " and dpgm.user_list_id = :userListId and d.is_publishing_or_white_labeling_in_progress = false group by d.id";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		QueryParameterDTO queryParameterDTO = new QueryParameterDTO(USER_LIST_ID, userListId);
		List<QueryParameterDTO> queryParameterDTOs = new ArrayList<>();
		queryParameterDTOs.add(queryParameterDTO);
		hibernateSQLQueryResultRequestDTO.setQueryParameterDTOs(queryParameterDTOs);
		hibernateSQLQueryResultRequestDTO.setClassInstance(PublishedContentIdAndUserListIdDetailsDTO.class);
		return (List<PublishedContentIdAndUserListIdDetailsDTO>) hibernateSQLQueryResultUtilDao
				.returnDTOList(hibernateSQLQueryResultRequestDTO);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findUnPublishedAndApprovedAssetIdsByCompanyId(Integer companyId) {
		String queryString = "select distinct d.id from xt_dam d left join xt_dam_partner dp on dp.dam_id = d.id where d.company_id= :companyId"
				+ " and d.is_publishing_to_partner_list = false and d.is_publishing_or_white_labeling_in_progress = false and d.approval_status = 'APPROVED' group by d.id having count(dp.dam_id)=0";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		QueryParameterDTO queryParameterDTO = new QueryParameterDTO(COMPANY_ID, companyId);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(queryParameterDTO);
		return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DamBasicInfo> findAssociatedDamBasicInfoByIds(Set<Integer> damIds) {
		if (XamplifyUtils.isNotEmptySet(damIds)) {
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			String queryString = " select  distinct d.id as \"damId\",d.asset_name as \"assetName\",d.video_id as \"videoId\", d.is_white_labeled_asset_shared_with_partners as \"whiteLabeledAssetSharedWithPartners\",c.company_name as \"companyName\", "
					+ " d.published_time as \"publishedTime\" from xt_dam d,xt_company_profile c "
					+ " where c.company_id = d.company_id and d.id in (:ids)";
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			QueryParameterListDTO queryParameterListDTO = new QueryParameterListDTO("ids",
					XamplifyUtils.convertSetToList(damIds));
			hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs().add(queryParameterListDTO);
			hibernateSQLQueryResultRequestDTO.setClassInstance(DamBasicInfo.class);
			return (List<DamBasicInfo>) hibernateSQLQueryResultUtilDao.returnDTOList(hibernateSQLQueryResultRequestDTO);
		} else {
			return Collections.emptyList();
		}

	}

	@Override
	public void updatePublishedTimeById(Integer damId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO
				.setQueryString("update xt_dam set published_time = :publishedTime where id = :damId");
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(PUBLISHED_TIME, new Date()));
		addDamIdQueryParameter(damId, hibernateSQLQueryResultRequestDTO);
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);

	}

	@Override
	public void updateIsPublishedToPartnerListByIds(Set<Integer> damIds, boolean value) {
		if (XamplifyUtils.isNotEmptySet(damIds)) {
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO
					.setQueryString("update xt_dam set is_publishing_to_partner_list = :value where id in(:ids)");
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("value", value));
			hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs()
					.add(new QueryParameterListDTO("ids", XamplifyUtils.convertSetToList(damIds)));
			hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findPublishedPartnerIdsByUserListIdAndDamId(Integer userListId, Integer damId) {
		if (XamplifyUtils.isValidInteger(userListId) && XamplifyUtils.isValidInteger(damId)) {
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString("\r\n"
					+ "select distinct xdpgm.user_id from xt_dam_partner_group_mapping xdpgm,xt_dam_partner xdp,\r\n"
					+ "xt_dam xd where xd.id  = xdp.dam_id and xdp.id  = xdpgm.dam_partner_id \r\n"
					+ "and xd.id  = :damId and xdpgm.user_list_id  = :userListId");
			addDamIdQueryParameter(damId, hibernateSQLQueryResultRequestDTO);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(USER_LIST_ID, userListId));
			return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
		} else {
			return Collections.emptyList();
		}

	}

	/***** XNFR-434 *****/
	@Override
	public void updateStatusToProgessByDamId(Integer damId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String processingStatus = "'" + DamStatusEnum.PROCESSING.name() + "'";
		hibernateSQLQueryResultRequestDTO
				.setQueryString("update xt_dam set asset_status = " + processingStatus + " where id = :id");
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("id", damId));
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public void updateStatusToProgessByVideoId(Integer videoId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String processingStatus = "'" + DamStatusEnum.PROCESSING.name() + "'";
		hibernateSQLQueryResultRequestDTO
				.setQueryString("update xt_dam set asset_status = " + processingStatus + " where video_id = :videoId");
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(VIDEO_ID, videoId));
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public boolean isAssetWhiteLabeled(Integer damId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO
				.setQueryString("select is_white_labeled_asset_shared_with_partners from xt_dam where id = :damId");
		addDamIdQueryParameter(damId, hibernateSQLQueryResultRequestDTO);
		return hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public Dam getByVideoId(Integer videoId) {
		Session session = sessionFactory.getCurrentSession();
		org.hibernate.Criteria criteria = session.createCriteria(Dam.class);
		criteria.add(Restrictions.eq("videoFile.id", videoId));
		return (Dam) criteria.uniqueResult();
	}

	@Override
	public boolean isPublished(Integer damId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(
				"select case when count(*)>0 then true else false end from xt_dam_partner where dam_id=:damId");
		addDamIdQueryParameter(damId, hibernateSQLQueryResultRequestDTO);
		return hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<WhiteLabeledContentSharedByVendorCompaniesDTO> fetchWhiteLabeledContentSharedByVendorCompanies(
			Integer companyId) {
		if (XamplifyUtils.isValidInteger(companyId)) {
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			String queryString = " select distinct c.company_name as \"sharedByVendorCompanyName\", e.shared_by_vendor_company_id as \"sharedByVendorCompanyId\" from \r\n"
					+ "xt_white_labeled_assets e,xt_company_profile c,xt_dam d where e.shared_by_vendor_company_id = c.company_id and \r\n"
					+ "d.id = e.received_white_labeled_asset_id  and d.company_id = :companyId ";
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(COMPANY_ID, companyId));
			hibernateSQLQueryResultRequestDTO.setClassInstance(WhiteLabeledContentSharedByVendorCompaniesDTO.class);
			return (List<WhiteLabeledContentSharedByVendorCompaniesDTO>) hibernateSQLQueryResultUtilDao
					.returnDTOList(hibernateSQLQueryResultRequestDTO);
		} else {
			return Collections.emptyList();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<WhiteLabeledContentSharedByVendorCompaniesDTO> findSharedAssetsByCompaniesForPartnerView(
			VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		if (XamplifyUtils.isValidInteger(vanityUrlDetailsDTO.getUserId())) {
			Integer partnerCompanyId = userDao.getCompanyIdByUserId(vanityUrlDetailsDTO.getUserId());
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			String queryString = "select distinct xcp.company_name as \"sharedByVendorCompanyName\" , d.company_id as \"sharedByVendorCompanyId\" \r\n"
					+ "from xt_dam d left join xt_dam_partner dp on d.id = dp.dam_id \r\n"
					+ "join xt_partnership p  on  p.id = dp.partnership_id \r\n"
					+ "join xt_company_profile xcp on d.company_id = xcp.company_id \r\n"
					+ "where p.partner_company_id = :companyId";
			if (vanityUrlDetailsDTO.isVanityUrlFilter()) {
				Integer vendorCompanyId = vanityUrlDetailsDTO.getVendorCompanyId();
				queryString = queryString + " and p.vendor_company_id= " + vendorCompanyId;
			}
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(COMPANY_ID, partnerCompanyId));
			hibernateSQLQueryResultRequestDTO.setClassInstance(WhiteLabeledContentSharedByVendorCompaniesDTO.class);
			return (List<WhiteLabeledContentSharedByVendorCompaniesDTO>) hibernateSQLQueryResultUtilDao
					.returnDTOList(hibernateSQLQueryResultRequestDTO);
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public ContentPreviewDetailsDTO findContentDetails(Integer id) {
		String queryString = "select asset_name as \"fileName\", \r\n"
				+ "asset_path as \"filePath\",asset_type as \"fileType\",xvf.videouri as \"videoUri\" from xt_dam d\r\n"
				+ "left join xt_video_files xvf on xvf.id = d.video_id\r\n" + "where d.id  = :id";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("id", id));
		return (ContentPreviewDetailsDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO,
				ContentPreviewDetailsDTO.class);
	}

	@Override
	public Map<String, Object> findAssetAnalytics(Pagination pagination) {
		Map<String, Object> map = new HashMap<>();
		return map;
	}

	/******* XNFR-543 *****/
	@Override
	public PaginatedDTO findAllPartnersByPartnerCompanies(Pagination pagination, String search) {
		Integer damId = pagination.getId();
		boolean publishedToPartnerGroups = isPublishedToPartnerGroups(damId);
		String queryString = "";
		List<String> searchColumns = new ArrayList<>();
		searchColumns.add("cp.company_name");
		searchColumns.add("uul.contact_company");
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		boolean applyPartnershipIdsFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
		if (teamMemberFilterDTO.isEmptyFilter()) {
			return hibernateSQLQueryResultUtilDao.returnEmptyPaginatedDTO();
		}

		if (publishedToPartnerGroups) {
			queryString = queryForFindingPartnerGroups(applyPartnershipIdsFilter);
		} else {
			queryString = queryForFindingPartnerCompanies(applyPartnershipIdsFilter);
		}
		String partnerSignatureCondition = "";
		if (XamplifyUtils.isValidString(pagination.getPartnerSignatureType())) {
			if (pagination.getPartnerSignatureType().equals(PARTNER_SIGNED)) {
				partnerSignatureCondition = " and dp.is_partner_signature_completed = true ";
			} else if (pagination.getPartnerSignatureType().equals(PARTNER_NOT_SIGNED)) {
				partnerSignatureCondition = " and dp.is_partner_signature_completed = false ";
			}
		}
		queryString = queryString + partnerSignatureCondition;
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.setSearchColumns(searchColumns);
		hibernateSQLQueryResultRequestDTO.setGroupByQueryString(
				" group by cp.company_id,dp.partnership_id,dp.id,uul.contact_company,dp.dam_id,xp.partner_id,xp.status,d.asset_name,d.asset_type,concat(xup.firstname, ' ', xup.lastname),dp.published_time,cast(xc.name as text) ");
		hibernateSQLQueryResultRequestDTO.setClassInstance(DamAnalyticsDTO.class);
		addDamIdQueryParameter(damId, hibernateSQLQueryResultRequestDTO);
		if (applyPartnershipIdsFilter) {
			hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs().add(new QueryParameterListDTO(
					"partnershipIds", teamMemberFilterDTO.getPartnershipIdsOrPartnerCompanyIds()));
		}
		return hibernateSQLQueryResultUtilDao.returnPaginatedDTO(hibernateSQLQueryResultRequestDTO, pagination, search);

	}

	private String queryForFindingPartnerCompanies(boolean applyPartnershipIdsFilter) {
		String queryString = damViewAnalyticsForCompaniesQuery;
		queryString = applyPartnershipIdsQuery(queryString, applyPartnershipIdsFilter);
		return queryString;
	}

	private String queryForFindingPartnerGroups(boolean applyPartnershipIdsFilter) {
		String queryString = damViewAnalyticsForGroupsQuery;
		queryString = applyPartnershipIdsQuery(queryString, applyPartnershipIdsFilter);
		return queryString;
	}

	private String applyPartnershipIdsQuery(String queryString, boolean applyPartnershipIdsFilter) {
		if (applyPartnershipIdsFilter) {
			queryString += " and dp.partnership_id in (:partnershipIds) \n";
		}
		return queryString;
	}

	@Override
	public boolean isVideoFile(Integer damId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(
				"select case when video_id is not null then true else false end  from xt_dam where id = :damId");
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add((new QueryParameterDTO(DAM_ID, damId)));
		return hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public PaginatedDTO findAllPartnerCompanyUsersByDamPartnerId(Pagination pagination, String search) {
		Integer id = pagination.getId();
		boolean isPublishedToPartnerGroups = isPublishedToPartnerGroupsByDamPartnerId(id);
		String queryString = "";
		if (isPublishedToPartnerGroups) {
			queryString += damPartnerAnalyticsForGroupsQuery;

		} else {
			queryString += damPartnerAnalyticsForCompaniesQuery;
		}
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO
				.setSearchColumns(Arrays.asList("uul.contact_company", "u.email_id", "u.firstname", "u.lastname"));
		if (isPublishedToPartnerGroups) {
			hibernateSQLQueryResultRequestDTO.setGroupByQueryString(
					" GROUP BY d.id, dp.id, p.partner_id, p.id, u.email_id, d.asset_name, d.asset_type, dp.published_time, u.firstname, u.lastname,up.firstname ,up.lastname, uul.firstname, uul.lastname,xcp.company_name, u.user_id ");
		} else {
			hibernateSQLQueryResultRequestDTO.setGroupByQueryString(
					" GROUP BY d.id, dp.id, p.partner_id, p.id, u.email_id, d.asset_name, d.asset_type, dp.published_time, u.firstname, u.lastname,u1.firstname ,u1.lastname, uul.firstname, uul.lastname,xcp.company_name, u.user_id ");
		}
		hibernateSQLQueryResultRequestDTO.setClassInstance(DamPartnerCompanyUsersAnalyticsDTO.class);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(DAM_PARTNER_ID, id));
		return hibernateSQLQueryResultUtilDao.returnPaginatedDTO(hibernateSQLQueryResultRequestDTO, pagination, search);
	}

	@Override
	public DamPartnerPreviewDTO findHtmlBodyByDamPartnerId(Integer damPartnerId) {
		String queryString = " select xdp.dam_id as \"id\",xdp.html_body as \"htmlBody\",xd.is_bee_template as \"beeTemplate\",xd.created_by as \"createdBy\",xdp.shared_asset_path as \"assestPath\" from xt_dam_partner xdp,xt_dam xd where xd.id = xdp.dam_id \r\n"
				+ " and xdp.id = :damPartnerId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(DAM_PARTNER_ID, damPartnerId));
		return (DamPartnerPreviewDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO,
				DamPartnerPreviewDTO.class);

	}

	@Override
	public boolean isIdMatchedByCompanyIdAndId(Integer companyId, Integer id) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(
				"select case when count(id)>0 then true else false end from xt_dam where company_id = :companyId and id = :damId");
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add((new QueryParameterDTO(DAM_ID, id)));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(COMPANY_ID, companyId));
		return hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public boolean isIdMatchedByCompanyIdAndDamPartnerId(Integer companyId, Integer damPartnerId) {
		boolean isIdMatched = false;
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select d.company_id as \"companyId\",dp.partnership_id as \"partnershipId\" from xt_dam d,xt_dam_partner dp where d.id = dp.dam_id and dp.id = :damPartnerId";
		Query query = session.createSQLQuery(queryString).setParameter(DAM_PARTNER_ID, damPartnerId);
		CompanyPartnershipDTO companyPartnershipDTO = (CompanyPartnershipDTO) paginationUtil
				.getDto(CompanyPartnershipDTO.class, query);
		if (companyPartnershipDTO != null) {
			Integer vendorCompanyId = companyPartnershipDTO.getCompanyId();
			Integer partnershipIdByDamPartnerId = companyPartnershipDTO.getPartnershipId();
			String partnershipQueryString = "select id from xt_partnership where vendor_company_id = :vendorCompanyId and partner_company_id = :partnerCompanyId";
			Query partnershipQuery = session.createSQLQuery(partnershipQueryString);
			partnershipQuery.setParameter(VENDOR_COMPANY_ID, vendorCompanyId);
			partnershipQuery.setParameter("partnerCompanyId", companyId);
			Integer partnershipId = (Integer) partnershipQuery.uniqueResult();
			isIdMatched = XamplifyUtils.isValidInteger(partnershipIdByDamPartnerId)
					&& XamplifyUtils.isValidInteger(partnershipId) && partnershipIdByDamPartnerId.equals(partnershipId);
		}
		return isIdMatched;
	}

	@Override
	public boolean isVideoIdMatchedByCompanyIdAndVideoId(Integer companyId, Integer videoId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "select company_id from xt_dam where video_id = :videoId";
		Query query = session.createSQLQuery(queryString).setParameter(VIDEO_ID, videoId);
		Integer companyIdByVideoId = (Integer) query.uniqueResult();
		return companyId.equals(companyIdByVideoId);
	}

	@Override
	public boolean isAssetPublishedToPartner(Integer damId, Integer userListId, Integer partnerUserId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(
				"select case when count(xdpgm.*)>0 then true else false end from xt_dam_partner_group_mapping xdpgm, xt_dam_partner xdp ,xt_dam xd  where xd.id  = xdp.dam_id and xdp.id = xdpgm.dam_partner_id and xdpgm.user_list_id  = :userListId"
						+ " and xdpgm.user_id = :partnerUserId and xd.id  = :damId");
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add((new QueryParameterDTO(DAM_ID, damId)));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(USER_LIST_ID, userListId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("partnerUserId", partnerUserId));
		return hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public DamPublishGetDTO findHtmlAndJsonBodyById(Integer id) {
		String queryString = "select html_body as \"htmlBody\", json_body as \"jsonBody\" from xt_dam where id =:damId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add((new QueryParameterDTO(DAM_ID, id)));
		return (DamPublishGetDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO,
				DamPublishGetDTO.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> findPublishedPartnershipIds(Integer damId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(
				"select distinct partnership_id from xt_dam_partner where dam_id = :damId order by partnership_id asc");
		addDamIdQueryParameter(damId, hibernateSQLQueryResultRequestDTO);
		return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

	private void addDamIdQueryParameter(Integer damId,
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO) {
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(DAM_ID, damId));
	}

	/** XNFR-824 start **/
	private String applyApprovalStatusFilter(Pagination pagination, String listQuery) {
		if (XamplifyUtils.isValidString(pagination.getSelectedApprovalStatusCategory())) {
			String approvalStatusCondition = "";
			String selectedStatus = pagination.getSelectedApprovalStatusCategory();

			if (selectedStatus.equals(ApprovalStatusType.APPROVED.name())) {
				if (!pagination.isPublishedFilter()) {
					approvalStatusCondition = " and d.approval_status = 'APPROVED' ";
					listQuery = listQuery.replace("{approvedAndUnPublsihedFilterCondition}", "");
					listQuery = listQuery.replace("{approvedAndUnPublsihedFilterJoinQuery}", "");
				} else {
					approvalStatusCondition = " and d.approval_status = 'APPROVED' ";
					listQuery = listQuery.replace("{approvedAndUnPublsihedFilterCondition}", " and dp.dam_id IS null ");
					listQuery = listQuery.replace("{approvedAndUnPublsihedFilterJoinQuery}",
							" left JOIN xt_dam_partner dp ON d.id = dp.dam_id ");
				}

			} else if (selectedStatus.equals(ApprovalStatusType.DRAFT.name())
					|| selectedStatus.equals(ApprovalStatusType.REJECTED.name())
					|| selectedStatus.equals(ApprovalStatusType.CREATED.name())) {
				approvalStatusCondition = " and d.approval_status = '" + selectedStatus + "' ";
				listQuery = listQuery.replace("{approvedAndUnPublsihedFilterCondition}", " and dp.dam_id IS null ");
				listQuery = listQuery.replace("{approvedAndUnPublsihedFilterJoinQuery}",
						" left JOIN xt_dam_partner dp ON d.id = dp.dam_id ");

			} else if ("ALL".equalsIgnoreCase(selectedStatus)) {
				listQuery = listQuery.replace("{approvedAndUnPublsihedFilterCondition}", "");
				listQuery = listQuery.replace("{approvedAndUnPublsihedFilterJoinQuery}", "");

			} else {
				listQuery = listQuery.replace("{approvedAndUnPublsihedFilterCondition}", "");
				listQuery = listQuery.replace("{approvedAndUnPublsihedFilterJoinQuery}", "");
			}

			listQuery = listQuery.replace(APPROVAL_STATUS_TYPE_MERGE_TAG, approvalStatusCondition);

		} else {
			listQuery = listQuery.replace(APPROVAL_STATUS_TYPE_MERGE_TAG, "");
			listQuery = listQuery.replace("{approvedAndUnPublsihedFilterCondition}", "");
			listQuery = listQuery.replace("{approvedAndUnPublsihedFilterJoinQuery}", "");
		}

		return listQuery;
	}

	@Override
	public void setAssetApprovalStatus(Integer loggedInUserId, Integer companyId, Dam dam, boolean isDraft) {
		if (XamplifyUtils.isValidInteger(loggedInUserId) && XamplifyUtils.isValidInteger(companyId) && dam != null) {
			ApprovalStatusType approvalStatus = determineApprovalStatus(loggedInUserId, companyId, isDraft);
			dam.setApprovalStatus(approvalStatus);
			dam.setApprovalStatusUpdatedBy(loggedInUserId);
			dam.setApprovalStatusUpdatedTime(new Date());
		}
	}

	private ApprovalStatusType determineApprovalStatus(Integer loggedInUserId, Integer companyId, boolean isDraft) {
		if (isDraft) {
			return ApprovalStatusType.CREATED;
		}

		boolean isApprovalRequiredForAssets = userDao.checkIfAssetApprovalRequiredByCompanyId(companyId);
		if (!isApprovalRequiredForAssets) {
			return ApprovalStatusType.APPROVED;
		}

		boolean isApprovalPrivilegeManager = approveDao.isApprovalPrivilegeManager(loggedInUserId);
		boolean isAssetApprover = approveDao.checkIsAssetApproverByTeamMemberIdAndCompanyId(loggedInUserId, companyId);

		return (isApprovalPrivilegeManager || isAssetApprover) ? ApprovalStatusType.APPROVED
				: ApprovalStatusType.CREATED;
	}

	/** XNFR-824 end **/

	@Override
	public void updateSharedAssetPathForPartner(String updatedSharedAssetPath, Integer damPartnerId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		updatedSharedAssetPath = "'" + updatedSharedAssetPath + "'";
		hibernateSQLQueryResultRequestDTO.setQueryString("update xt_dam_partner set shared_asset_path = "
				+ updatedSharedAssetPath + ",  is_partner_signature_completed = true where id = :damPartnerId");
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(DAM_PARTNER_ID, damPartnerId));
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);

	}

	@Override
	public String fetchAssestPath(Integer damId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "SELECT asset_path FROM xt_dam where id =:damId";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("damId", damId));
		String path = (String) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
		if (XamplifyUtils.isValidString(path)) {
			return path;
		} else {
			return null;
		}
	}

	@Override
	public void updateSharedAssetPathAndPartnerSignatureCompletedFalseForPartner(String updatedSharedAssetPath,
			Integer damPartnerId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		updatedSharedAssetPath = "'" + updatedSharedAssetPath + "'";
		hibernateSQLQueryResultRequestDTO.setQueryString("update xt_dam_partner set shared_asset_path = "
				+ updatedSharedAssetPath + ",  is_partner_signature_completed = false where id = :damPartnerId");
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(DAM_PARTNER_ID, damPartnerId));
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);

	}

	@Override
	public void updateAssetPathForVendor(String updatedSharedAssetPath, Integer id) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		updatedSharedAssetPath = "'" + updatedSharedAssetPath + "'";
		hibernateSQLQueryResultRequestDTO.setQueryString("update xt_dam set asset_path = " + updatedSharedAssetPath
				+ ",  is_vendor_signature_completed = true where id = :damId");
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(DAM_ID, id));
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public ContentPreviewDetailsDTO findContentDetailsForPartner(Integer id, Integer damPartnerId) {
		String queryString = "SELECT asset_name AS \"fileName\", \r\n" + "CASE \r\n"
				+ "   WHEN d.asset_type = 'pdf' AND dp.shared_asset_path IS NOT NULL THEN dp.shared_asset_path \r\n"
				+ "   ELSE d.asset_path \r\n" + "END AS \"filePath\", \r\n" + "d.asset_type AS \"fileType\", \r\n"
				+ "xvf.videouri AS \"videoUri\" \r\n" + "FROM xt_dam d \r\n"
				+ "LEFT JOIN xt_video_files xvf ON xvf.id = d.video_id \r\n"
				+ "LEFT JOIN xt_dam_partner dp ON dp.dam_id = d.id \r\n" + "WHERE d.id = :id and dp.id = :damPartnerId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("id", id));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(DAM_PARTNER_ID, damPartnerId));
		return (ContentPreviewDetailsDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO,
				ContentPreviewDetailsDTO.class);
	}

	/** XNFR-885 start **/
	@Override
	public DamUploadPostDTO getVideoAssetDetailsForReApproval(Integer videoId) {
		if (!XamplifyUtils.isValidInteger(videoId)) {
			return new DamUploadPostDTO();
		}
		String queryString = "select xd.id as \"id\", xd.asset_name as \"assetName\", xvf.description as \"description\" from xt_dam xd "
				+ "inner join xt_video_files xvf on xd.video_id = xvf.id where xd.video_id = :videoId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(VIDEO_ID, videoId));
		return (DamUploadPostDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO,
				DamUploadPostDTO.class);
	}

	@Override
	public Integer getApprovalReferenceIdByDamId(Integer damId) {
		if (!XamplifyUtils.isValidInteger(damId)) {
			return null;
		}
		String queryString = "select approval_reference_id from xt_dam where id = :damId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(DAM_ID, damId));
		return (Integer) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public boolean hasAnyReApprovalVersionsCreated(List<Integer> damIds) {
		if (!XamplifyUtils.isNotEmptyList(damIds)) {
			return false;
		}
		String queryString = "select case when count(*) > 0 then true else false end from xt_dam where approval_reference_id in (:damIds)";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs().add(new QueryParameterListDTO("damIds", damIds));
		return hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Integer> getTagIdsByDamId(Integer damId) {
		if (!XamplifyUtils.isValidInteger(damId)) {
			return Collections.emptyList();
		}
		String queryString = "select tag_id from xt_dam_tag where dam_id = :damId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(DAM_ID, damId));
		return (List<Integer>) hibernateSQLQueryResultUtilDao.returnList(hibernateSQLQueryResultRequestDTO);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ContentReApprovalDTO> getAssetDetailsForReApproval(List<Integer> damIds) {
		if (!XamplifyUtils.isNotEmptyList(damIds)) {
			return Collections.emptyList();
		}
		String queryString = "select xd.id AS \"id\", xd.asset_name AS \"assetName\", xd.asset_path AS \"assetPath\", xd.thumbnail_path AS \"thumbnailPath\", "
				+ "xd.description AS \"description\", xd.asset_type AS \"assetType\", xd.json_body AS \"jsonBody\", xd.html_body AS \"htmlBody\", "
				+ "xd.is_published AS \"published\", cast(xd.asset_status as text) AS \"assetStatus\", xd.video_id AS \"videoId\", "
				+ "cast(xd.approval_status as text) AS \"approvalStatus\", xd.is_added_to_quick_links as \"addedToQuickLinks\", xd.alias as \"alias\", xd.is_bee_template as \"beeTemplate\", "
				+ "xd.parent_id as \"parentId\", xd.child_parent_id as \"childParentId\", cast(xd.created_time as text) as \"createdTime\", xd.created_by as \"createdBy\", xd.is_partner_signature_required as \"partnerSignatureRequired\", "
				+ "xd.is_vendor_signature_required as \"vendorSignatureRequired\", xd.approval_reference_id as \"approvalReferenceId\", xd.is_white_labeled_asset_shared_with_partners as \"whiteLabeledAssetSharedWithPartners\", "
				+ "xcm.category_id as \"categoryId\", xd.page_size as \"pageSize\", xd.page_orientation \"pageOrientation\", xd.is_image_generated_successfully as \"imageGeneratedSuccessfully\", xd.company_id as \"companyId\", "
				+ "xd.is_vendor_signature_required_after_partner_signature as \"vendorSignatureRequiredAfterPartnerSignature\" FROM xt_dam xd inner join xt_category_module xcm on xcm.dam_id = xd.id where xd.id in (:damIds)";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs().add(new QueryParameterListDTO("damIds", damIds));
		return (List<ContentReApprovalDTO>) hibernateSQLQueryResultUtilDao.getListDto(hibernateSQLQueryResultRequestDTO,
				ContentReApprovalDTO.class);
	}

	@Override
	public void replaceParentAssetMetadataAfterReApproval(ContentReApprovalDTO contentReApprovalDTO, Integer damId) {
		if (XamplifyUtils.isValidInteger(damId) && contentReApprovalDTO != null
				&& XamplifyUtils.isValidInteger(contentReApprovalDTO.getLoggedInUserId())) {

			String queryString = "update xt_dam set asset_name = :assetName, ${assetPathAndThumbnailPathMergeQuery}description = :description, "
					+ "is_added_to_quick_links = :addedToQuickLinks, updated_by = :updatedBy, updated_time = :updatedTime, is_white_labeled_asset_shared_with_partners = :whiteLabeledAssetSharedWithPartners, "
					+ "is_vendor_signature_required = :vendorSignatureRequired, is_partner_signature_required = :partnerSignatureRequired, is_vendor_signature_required_after_partner_signature = :vendorSignatureRequiredAfterPartnerSignature where id = :damId";

			if (!XamplifyUtils.isValidInteger(contentReApprovalDTO.getVideoId())) {
				queryString = queryString.replace("${assetPathAndThumbnailPathMergeQuery}",
						"asset_path = :assetPath, thumbnail_path = :thumbnailPath,");
			} else {
				queryString = queryString.replace("${assetPathAndThumbnailPathMergeQuery}", "");
			}

			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(ASSET_NAME, contentReApprovalDTO.getAssetName()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(DESCRIPTION, contentReApprovalDTO.getDescription()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("addedToQuickLinks", contentReApprovalDTO.isAddedToQuickLinks()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(UPDATED_TIME, new Date()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(UPDATED_BY, contentReApprovalDTO.getLoggedInUserId()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(DAM_ID, damId));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("whiteLabeledAssetSharedWithPartners",
							contentReApprovalDTO.isWhiteLabeledAssetSharedWithPartners()));

			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(
					new QueryParameterDTO("vendorSignatureRequired", contentReApprovalDTO.isVendorSignatureRequired()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(
					"partnerSignatureRequired", contentReApprovalDTO.isPartnerSignatureRequired()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("vendorSignatureRequiredAfterPartnerSignature",
							contentReApprovalDTO.isVendorSignatureRequiredAfterPartnerSignature()));

			if (!XamplifyUtils.isValidInteger(contentReApprovalDTO.getVideoId())) {
				hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
						.add(new QueryParameterDTO(ASSET_PATH, contentReApprovalDTO.getAssetPath()));
				hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
						.add(new QueryParameterDTO(THUMBNAIL_PATH, contentReApprovalDTO.getThumbnailPath()));
			}
			hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
		}
	}

	@Override
	public void deleteParentTagsAfterReApprvoal(List<Integer> tagIdsToDelete, Integer damId) {
		if (XamplifyUtils.isValidInteger(damId) && XamplifyUtils.isNotEmptyList(tagIdsToDelete)) {
			try {
				Session session = sessionFactory.getCurrentSession();
				Query query = session
						.createSQLQuery("delete from xt_dam_tag where dam_id = :damId and tag_id in (:tagIds)");
				query.setParameter(DAM_ID, damId);
				query.setParameterList("tagIds", tagIdsToDelete);
				query.executeUpdate();
			} catch (HibernateException | DamDataAccessException e) {
				throw new DamDataAccessException(e);
			} catch (Exception ex) {
				throw new DamDataAccessException(ex);
			}
		}
	}

	@Override
	public DamVideoDTO getReApprovalVersionVideoAssetDetails(Integer videoId) {
		if (!XamplifyUtils.isValidInteger(videoId)) {
			return new DamVideoDTO();
		}
		String queryString = "SELECT xvf.id AS \"id\", xvf.categories_id AS \"categoriesId\", xvf.title AS \"videoTitle\", xvf.videouri AS \"videoUri\", "
				+ "xvf.description AS \"description\", xvf.customer_id AS \"customerId\", CAST(xvf.video_status AS TEXT) AS \"videoStatus\", "
				+ "xvf.view_by AS \"viewBy\", xvf.big_thumbnail_image AS \"bigThumbnailImage\", xvf.imageuri AS \"imageUri\", "
				+ "CAST(xvf.created_time AS TEXT) AS \"createdTime\", CAST(xvf.updated_time AS TEXT) AS \"updatedTime\", xvf.updated_by AS \"updatedBy\", "
				+ "xvf.alias AS \"alias\", xvf.video_length AS \"videoLength\", xvf.video_size AS \"videoSize\", xvf.bitrate AS \"bitRate\", xvf.gifuri AS \"gifUri\", "
				+ "xvf.video_id AS \"videoIdAsString\", xvf.views AS \"views\", xvf.is_processed AS \"processed\", xvf.is_custom_thumbnail_uploaded AS \"customThumbnailUploaded\", "
				+ "STRING_AGG(xvt.video_tags, ',') AS \"tagsInString\", xvi.image1 as \"image1\", xvi.image2 as \"image2\", xvi.image3 as \"image3\", xvi.gif1 as \"gif1\", xvi.gif2 as \"gif2\", xvi.gif3 as \"gif3\" "
				+ "FROM xt_video_files AS xvf inner join xt_video_image as xvi on xvf.id = xvi.id "
				+ "LEFT JOIN xt_video_tags AS xvt ON xvf.id = xvt.video_id "
				+ "WHERE xvf.id = :videoId GROUP BY xvf.id, xvi.id ";

		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(VIDEO_ID, videoId));
		return (DamVideoDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO,
				DamVideoDTO.class);
	}

	@Override
	public void replaceParentVideoFileDetailsWithChildForReApproval(DamVideoDTO damVideoDTO, Integer videoId) {
		if (XamplifyUtils.isValidInteger(videoId) && damVideoDTO != null) {
			String queryString = "UPDATE xt_video_files SET categories_id = :categoriesId, title = :videoTitle, videouri = :videoUri, description = :description, "
					+ "customer_id = :customerId, big_thumbnail_image = :bigThumbnailImage, imageuri = :imageUri, updated_by = :updatedBy, "
					+ "video_length = :videoLength, video_size = :videoSize, bitrate = :bitRate, gifuri = :gifUri, video_id = :videoIdAsString, "
					+ "is_custom_thumbnail_uploaded = :customThumbnailUploaded WHERE id = :videoId";

			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("categoriesId", damVideoDTO.getCategoriesId()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("videoTitle", damVideoDTO.getVideoTitle()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("videoUri", damVideoDTO.getVideoUri()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(DESCRIPTION, damVideoDTO.getDescription()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("customerId", damVideoDTO.getCustomerId()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("bigThumbnailImage", damVideoDTO.getBigThumbnailImage()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("imageUri", damVideoDTO.getImageUri()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(UPDATED_BY, damVideoDTO.getUpdatedBy()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("videoLength", damVideoDTO.getVideoLength()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("videoSize", damVideoDTO.getVideoSize()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("bitRate", damVideoDTO.getBitRate()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("gifUri", damVideoDTO.getGifUri()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("videoIdAsString", damVideoDTO.getVideoIdAsString()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("customThumbnailUploaded", damVideoDTO.isCustomThumbnailUploaded()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(VIDEO_ID, videoId));

			hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
		}
	}

	@Override
	public void replaceImagesAndGifsForReApproval(DamVideoDTO damVideoDTO, Integer videoId) {
		if (XamplifyUtils.isValidInteger(videoId) && damVideoDTO != null) {
			String queryString = "UPDATE xt_video_image SET image1 = :image1, image2 = :image2, image3 = :image3, "
					+ "gif1 = :gif1, gif2 = :gif2, gif3 = :gif3 where id = :videoId";

			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("image1", damVideoDTO.getImage1()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("image2", damVideoDTO.getImage2()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("image3", damVideoDTO.getImage3()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("gif1", damVideoDTO.getGif1()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("gif2", damVideoDTO.getGif2()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO("gif3", damVideoDTO.getGif3()));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(VIDEO_ID, videoId));
			hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
		}
	}

	@Override
	public Integer getVideoIdByDamId(Integer damId) {
		if (!XamplifyUtils.isValidInteger(damId)) {
			return null;
		}
		String queryString = "select video_id from xt_dam where id = :damId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(DAM_ID, damId));
		return (Integer) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public List<DamTag> updateTagsAfterReApprovalAndReturnTagsToSave(ContentReApprovalDTO contentReApprovalDTO,
			Integer approvalReferenceId, Integer childDamId, Integer loggedInUserId) {
		List<Integer> childTagIds = getTagIdsByDamId(childDamId);
		List<Integer> parentTagIds = getTagIdsByDamId(approvalReferenceId);
		List<Integer> tagIds = new ArrayList<>();
		List<Integer> idsToDelete = new ArrayList<>();
		for (Integer parentTagId : parentTagIds) {
			tagIds.add(parentTagId);
			if (childTagIds == null || childTagIds.isEmpty() || !childTagIds.contains(parentTagId)) {
				idsToDelete.add(parentTagId);
			}
		}

		List<DamTag> damTags = new ArrayList<>();
		buildDamTag(contentReApprovalDTO, approvalReferenceId, loggedInUserId, childTagIds, tagIds, damTags);

		if (XamplifyUtils.isNotEmptyList(idsToDelete)) {
			deleteParentTagsAfterReApprvoal(idsToDelete, approvalReferenceId);
		}
		return damTags;
	}

	private void buildDamTag(ContentReApprovalDTO contentReApprovalDTO, Integer approvalReferenceId,
			Integer loggedInUserId, List<Integer> childTagIds, List<Integer> tagIds, List<DamTag> damTags) {
		if (childTagIds != null) {
			childTagIds.removeAll(tagIds);
			for (Integer tagId : childTagIds) {
				DamTag damTag = new DamTag();
				damTag.setCreatedBy(contentReApprovalDTO.getCreatedBy());
				damTag.setUpdatedBy(loggedInUserId);
				Dam dam = new Dam();
				dam.setId(approvalReferenceId);
				damTag.setDam(dam);
				Tag tag = new Tag();
				tag.setId(tagId);
				damTag.setTag(tag);
				damTag.setCreatedTime(new Date());
				damTags.add(damTag);
			}
		}
	}

	@Override
	public void deleteByDamIds(List<Integer> damIds) {
		try {
			Session session = sessionFactory.getCurrentSession();
			String hql = "delete from Dam  where id in (:damIds)";
			Query query = session.createQuery(hql);
			query.setParameterList("damIds", damIds);
			query.executeUpdate();
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	/** XNFR-885 end **/

	@Override
	public void updateSharedAssetPathForPartnerAndVendorSignatureCompleted(String updatedSharedAssetPath,
			Integer damPartnerId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		updatedSharedAssetPath = "'" + updatedSharedAssetPath + "'";
		hibernateSQLQueryResultRequestDTO.setQueryString("update xt_dam_partner set shared_asset_path = "
				+ updatedSharedAssetPath + ",  is_vendor_signature_completed = true where id = :damPartnerId");
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(DAM_PARTNER_ID, damPartnerId));
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);

	}

	@Override
	public boolean isVendorSignatureRequiredAfterPartnerSignatureCompleted(Integer id) {
		Session session = sessionFactory.getCurrentSession();
		String sql = "select is_vendor_signature_required_after_partner_signature from xt_dam where id = :id";
		Query query = session.createSQLQuery(sql);
		query.setParameter("id", id);
		Boolean result = (Boolean) query.uniqueResult();
		return result != null && result;
	}

	/** XNFR-885 **/
	@Override
	public void handleWhiteLabeledAssetsAfterReApproval(List<Integer> whiteLabeledParentDamIds, Integer companyId,
			Integer loggedInUserId) {
		if (XamplifyUtils.isNotEmptyList(whiteLabeledParentDamIds)) {
			List<ContentReApprovalDTO> whiteLabeledReApprovalDamDTOs = getAssetDetailsForReApproval(
					whiteLabeledParentDamIds);
			String companyProfileName = userDao.getCompanyProfileNameByUserId(loggedInUserId);
			for (ContentReApprovalDTO whiteLabeledReApprovalDamDTO : whiteLabeledReApprovalDamDTOs) {
				processAndSaveWhiteLabeledDamForReApproval(companyId, companyProfileName, whiteLabeledReApprovalDamDTO);
			}
		}
	}

	private void processAndSaveWhiteLabeledDamForReApproval(Integer companyId, String companyProfileName,
			ContentReApprovalDTO whiteLabeledReApprovalDamDTO) {
		DamUploadPostDTO damUploadPostDTO = new DamUploadPostDTO();
		List<Integer> publishedPartnerIds = findPublishedPartnerIdsByDamId(whiteLabeledReApprovalDamDTO.getId());
		List<Integer> publishedPartnerGroupIds = findPublishedPartnerGroupIdsByDamId(
				whiteLabeledReApprovalDamDTO.getId());
		if (XamplifyUtils.isNotEmptyList(publishedPartnerIds)
				|| XamplifyUtils.isNotEmptyList(publishedPartnerGroupIds)) {
			List<Integer> uniquePartnerShipIds = determinePartnershipIdsForApproval(companyId, publishedPartnerIds,
					publishedPartnerGroupIds);
			if (XamplifyUtils.isNotEmptyList(uniquePartnerShipIds)) {
				Dam dam = copyAndPrepareWhiteLabeledDamForReApproval(companyId, companyProfileName,
						whiteLabeledReApprovalDamDTO);
				if (dam.getCompanyProfile() != null
						&& XamplifyUtils.isValidString(dam.getCompanyProfile().getCompanyProfileName())) {
					if (XamplifyUtils.isValidInteger(whiteLabeledReApprovalDamDTO.getVideoId())) {
						DamVideoDTO damVideoDTO = findDamAndVideoDetailsByVideoId(
								whiteLabeledReApprovalDamDTO.getVideoId());
						damUploadPostDTO.setDamVideoDTO(damVideoDTO);
					}
					iterateAndSaveWhiteLabeledDamAfterReplacingReApprovalAssets(whiteLabeledReApprovalDamDTO,
							uniquePartnerShipIds, damUploadPostDTO, dam);
				}
			}
		}
	}

	private Dam copyAndPrepareWhiteLabeledDamForReApproval(Integer companyId, String companyProfileName,
			ContentReApprovalDTO whiteLabeledReApprovalDamDTO) {
		Dam dam = new Dam();
		BeanUtils.copyProperties(whiteLabeledReApprovalDamDTO, dam);
		if (XamplifyUtils.isValidString(companyProfileName)) {
			if (dam.getCompanyProfile() == null) {
				dam.setCompanyProfile(new CompanyProfile());
			}
			dam.getCompanyProfile().setId(companyId);
			dam.getCompanyProfile().setCompanyProfileName(companyProfileName);
		}
		return dam;
	}

	private List<Integer> determinePartnershipIdsForApproval(Integer companyId, List<Integer> publishedPartnerIds,
			List<Integer> publishedPartnerGroupIds) {
		List<Integer> partnerIds = new ArrayList<>();
		List<Integer> uniquePartnerIds = new ArrayList<>();
		List<Integer> uniquePartnerShipIds = new ArrayList<>();
		boolean isPartnerGroupSelected = publishedPartnerGroupIds != null && publishedPartnerIds != null
				&& publishedPartnerIds.isEmpty() && !publishedPartnerGroupIds.isEmpty();
		if (isPartnerGroupSelected) {
			List<UserListAndUserId> userListAndUserIdDtos = userListDao
					.findUserIdsAndUserListIds(publishedPartnerGroupIds);
			partnerIds.addAll(
					userListAndUserIdDtos.stream().map(UserListAndUserId::getUserId).collect(Collectors.toList()));
			if (XamplifyUtils.isNotEmptyList(partnerIds)) {
				uniquePartnerIds.addAll(XamplifyUtils.convertListToSetElements(partnerIds));
				List<Integer> partnershipIds = partnershipDao
						.findPartnershipIdsByPartnerIdsAndVendorCompanyId(uniquePartnerIds, companyId);
				uniquePartnerShipIds.addAll(XamplifyUtils.convertListToSetElements(partnershipIds));
			}
		} else {
			if (XamplifyUtils.isNotEmptyList(publishedPartnerIds)) {
				uniquePartnerIds.addAll(XamplifyUtils.convertListToSetElements(publishedPartnerIds));
				List<Integer> partnershipIds = partnershipDao.getPartnershipIdsByPartnerCompanyUserIds(uniquePartnerIds,
						companyId);
				uniquePartnerShipIds.addAll(XamplifyUtils.convertListToSetElements(partnershipIds));
			}
		}
		return uniquePartnerShipIds;
	}

	private void iterateAndSaveWhiteLabeledDamAfterReplacingReApprovalAssets(
			ContentReApprovalDTO whiteLabeledReApprovalDamDTO, List<Integer> uniquePartnerShipIds,
			DamUploadPostDTO damUploadPostDTO, Dam dam) {
		Session session = sessionFactory.getCurrentSession();
		int i = 0;
		int total = uniquePartnerShipIds.size();
		for (Integer partnershipId : uniquePartnerShipIds) {
			PartnershipDTO partnershipDTO = partnershipDao
					.findPartnerIdAndPartnerCompanyIdByPartnershipId(partnershipId);
			if (partnershipDTO != null) {
				Integer partnerId = partnershipDTO.getId();
				boolean isAnyAdminCompany = utilDao.isAnyVendorAdminCompany(partnerId);
				Integer partnerCompanyId = partnershipDTO.getPartnerCompanyId();
				Integer vendorCompanyId = whiteLabeledReApprovalDamDTO.getCompanyId();
				if (isAnyAdminCompany && XamplifyUtils.isValidInteger(partnerCompanyId)
						&& XamplifyUtils.isValidInteger(partnerId)) {
					CompanyProfile partnerCompany = new CompanyProfile();
					partnerCompany.setId(partnerCompanyId);
					Dam whiteLabeledDam = saveIntoDam(damUploadPostDTO, session, dam, partnerId, partnerCompanyId,
							partnerCompany);
					saveIntoCategory(partnerId, partnerCompanyId, whiteLabeledDam);
					saveWhiteLabeledAsset(dam, session, vendorCompanyId, whiteLabeledDam, partnerCompany);
				}
			}
			if (++i % 30 == 0) {
				session.flush();
				session.clear();
			}
			int itemsLeft = total - i;
			logger.debug(
					"Re Approval White Labeled DAM : {} , Id:{}, Partnership Id: {}, Partners Left: {}, Timestamp: {}",
					dam.getAssetName(), dam.getId(), partnershipId, itemsLeft, new Date());
		}
	}

	/** XNFR-885 end **/

	/** XNFR-923 **/
	@Override
	public boolean isPartnerSignatureRequired(Integer id) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();

		String sql = " select is_partner_signature_required from xt_dam where id = :damId ";

		hibernateSQLQueryResultRequestDTO.setQueryString(sql);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("damId", id));

		return hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

	public DamAnalyticsTilesDTO getPartnerSignatureCount(Integer id) {

		String queryString = " select  CAST(COUNT(dp.id) AS INTEGER) AS \"allCount\", "
				+ " CAST(COUNT(CASE WHEN dp.is_partner_signature_completed THEN 1 END) AS INTEGER) AS \"signedCount\", "
				+ " CAST(COUNT(CASE WHEN dp.is_partner_signature_completed = false THEN 1 END) AS INTEGER) AS \"notSignedCount\" "
				+ " FROM xt_dam_partner dp "
				+ " join xt_partnership xp on xp.id = dp.partnership_id and xp.partner_company_id notnull "
				+ " where dp.dam_id =:dam_id group by dp.dam_id ;";

		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("dam_id", id));
		return (DamAnalyticsTilesDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO,
				DamAnalyticsTilesDTO.class);
	}

	/** XNFR-928 **/
	@Override
	public SharedAssetDetailsViewDTO getDamPartnerIdAndShareAssetPathByDamIdAndPartnershipId(Integer damId,
			Integer partnershipId) {
		try {
			String queryString = "select id as \"id\", shared_asset_path as \"sharedAssetPath\" from xt_dam_partner where dam_id = :damId and partnership_id = :partnershipId";
			HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
			hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(DAM_ID, damId));
			hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
					.add(new QueryParameterDTO(PARTNERSHIP_ID, partnershipId));
			return (SharedAssetDetailsViewDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO,
					SharedAssetDetailsViewDTO.class);
		} catch (HibernateException | DamDataAccessException e) {
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			throw new DamDataAccessException(ex);
		}
	}

	public DamDTO getVendorAndPartnerIdAssets(Integer damId) {
		if (!XamplifyUtils.isValidInteger(damId)) {
			return null;
		}
		String queryString = "select company_id as \"companyId\", created_by as \"createdBy\" from xt_dam where id = :damId ";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("damId", damId));
		return (DamDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO, DamDTO.class);
	}

	@Override
	public boolean isDamPartnerGroupRowExists(Integer userListId, Integer damPartnerId, Integer userId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String sql = "SELECT CASE WHEN  count(*) > 0 THEN true ELSE false END from xt_dam_partner_group_mapping where dam_partner_id = :damPartnerId and user_list_id = :userListId and user_id = :userId";
		hibernateSQLQueryResultRequestDTO.setQueryString(sql);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("damPartnerId", damPartnerId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("userListId", userListId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("userId", userId));
		return hibernateSQLQueryResultUtilDao.returnBoolean(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public void updateAssetPath(Integer damId, String assetPath) {
		String queryString = "update xt_dam set asset_path ='" + assetPath + "' where id = :damId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO(DAM_ID, damId));
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public Integer findAssetIdByAlias(String alias) {
		String queryString = "select id from xt_dam where alias ='" + alias + "'";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		return (Integer) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public DamUploadPostDTO findDamByPartnerId(Integer damPartnerId) {
		String queryString = "select xd.id as \"id\", xd.asset_name as \"assetName\", xd.asset_path as \"assetPath\", \n"
				+ "xd.is_bee_template as \"beeTemplate\", xd.is_partner_signature_required as \"partnerSignatureRequired\" \n"
				+ "from xt_dam xd, xt_dam_partner xdp \n where xd.id = xdp.dam_id and xdp.id = :damPartnerId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(DAM_PARTNER_ID, damPartnerId));
		return (DamUploadPostDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO,
				DamUploadPostDTO.class);
	}

	/** XNFR-973 **/
	@Override
	public void updateOpenAIFileIdByAssetId(Integer assetId, String fileId) {
		String queryString = "update xt_dam set open_ai_file_id = :openAIFileId where id = :id";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("id", assetId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("openAIFileId", fileId));
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public String fetchOpenAIFileIdByAssetId(Integer assetId) {
		String queryString = "select open_ai_file_id from xt_dam where id = :id";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("id", assetId));
		return (String) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public Integer findDamIdByDamPartnerId(Integer damPartnerId) {
		String queryString = "select dam_id from xt_dam_partner where id = :damPartnerId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO("damPartnerId", damPartnerId));
		return (Integer) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public boolean isAssetExistBySlugAndCompany(String slug, Integer companyId) {
		Integer id = getDamIdBySlugAndCopmanyId(slug, companyId);
		return (id != null);
	}

	@Override
	public Integer getDamIdBySlugAndCopmanyId(String slug, Integer companyId) {
		String queryString = "select id from xt_dam where slug = :slug and company_id = :companyId ";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("slug", slug));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("companyId", companyId));
		Integer id = (Integer) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
		return id;
	}

	@Override
	public DamListDTO findAssetDetailsBySlugAndCompanyId(String slug, Integer companyId) {
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		String queryString = "select " + "	id as \"id\", " + "	video_id as \"videoId\" , "
				+ "	is_bee_template as \"beeTemplate\" , " + "	asset_path as \"assetPath\", "
				+ "	asset_type as \"assetType\", " + "	asset_name as \"assetName\" " + "from " + "	xt_dam " + "where "
				+ "	slug =:slug " + "	and company_id =:companyId ; ";
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("slug", slug));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("companyId", companyId));
		hibernateSQLQueryResultRequestDTO.setClassInstance(DamListDTO.class);
		DamListDTO damDto = (DamListDTO) hibernateSQLQueryResultUtilDao.getDto(hibernateSQLQueryResultRequestDTO,
				DamListDTO.class);
		if (damDto != null) {
			updateAssetDetailsByAssetType(damDto, damDto);
		}
		return damDto;
	}

	/** XNFR-981 **/
	@Override
	public void updateRAGITFileIdByAssetId(Integer assetId, String fileId) {
		String queryString = "update xt_dam set ragit_file_id = :ragitFileId where id = :id";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("id", assetId));
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("ragitFileId", fileId));
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public String fetchRagitFileIdByAssetId(Integer id) {
		String queryString = "select ragit_file_id from xt_dam where id = :id";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs().add(new QueryParameterDTO("id", id));
		return (String) hibernateSQLQueryResultUtilDao.getUniqueResult(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public CategoryModule findCategoryByDamId(Integer damId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(CategoryModule.class);
		criteria.add(Restrictions.eq("damId", damId));
		return (CategoryModule) criteria.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DamPartnerDetailsDTO> findPublishedGroupAssetsByPartnerId(Integer loggedInUserId) {
		String queryString = "select xd.asset_name as \"assetName\", xdpg.id as \"damPartnerGroupId\", "
				+ "xdp.partnership_id as \"partnershipId\" \n from xt_dam xd "
				+ "join xt_dam_partner xdp on xdp.dam_id = xd.id \n"
				+ "join xt_dam_partner_group_mapping xdpg on xdpg.dam_partner_id = xdp.id "
				+ "and xdp.is_partner_group_selected is true \n where xdpg.user_id = :userId";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.getQueryParameterDTOs()
				.add(new QueryParameterDTO(XamplifyConstants.USER_ID, loggedInUserId));
		return (List<DamPartnerDetailsDTO>) hibernateSQLQueryResultUtilDao.getListDto(hibernateSQLQueryResultRequestDTO,
				DamPartnerDetailsDTO.class);
	}

	private String addNewTileConditions(Pagination pagination, String finalQueryString) {
		String publicationFilter = pagination.getSelectedApprovalStatusCategory();
		if (XamplifyUtils.isValidString(publicationFilter)) {
			if (publicationFilter.equalsIgnoreCase("published")) {
				finalQueryString = finalQueryString.replace("unPublishedCondition", "");
				finalQueryString = finalQueryString.replace("publishedCondition",
						"join xt_dam_partner dp on dp.dam_id= d.id");
			} else if (publicationFilter.equalsIgnoreCase("unpublished")) {
				finalQueryString = finalQueryString.replace("unPublishedCondition", " and dp.dam_id is null ");
				finalQueryString = finalQueryString.replace("publishedCondition",
						"left join xt_dam_partner dp on dp.dam_id= d.id");
			} else {
				finalQueryString = finalQueryString.replace("unPublishedCondition", "");
				finalQueryString = finalQueryString.replace("publishedCondition", "");
			}
		} else {
			finalQueryString = finalQueryString.replace("unPublishedCondition", "");
			finalQueryString = finalQueryString.replace("publishedCondition", "");
		}
		return finalQueryString;
	}

	@Override
	public DamDTO damDetailsByDamId(Integer damId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "SELECT d.asset_name AS \"assetName\", " + "d.asset_type AS \"assetType\", "
				+ "CAST(cat.name AS TEXT) AS \"folder\", "
				+ "CONCAT(xup.firstname, ' ', xup.lastname) AS \"fullName\",max(xdp.published_time) as \"publishedOn\" "
				+ "FROM xt_category cat " + "JOIN xt_category_module cm ON cm.category_id = cat.id "
				+ "JOIN xt_dam d ON d.id = cm.dam_id " + "left join xt_dam_partner xdp on xdp.dam_id= d.id "
				+ "join xt_user_profile xup on xup.user_id = d.created_by " + "WHERE d.id = :damId "
				+ "group by 1,2,3,4 ";
		Query query = session.createSQLQuery(queryString);
		query.setParameter(DAM_ID, damId);
		return (DamDTO) paginationUtil.getDto(DamDTO.class, query);
	}

//XNFR-1035
	@Override
	public PaginatedDTO findAllPartnersByPartnerCompaniesDetails(Pagination pagination, String search) {
		Integer damId = pagination.getId();
		boolean publishedToPartnerGroups = isPublishedToPartnerGroups(damId);
		String queryString = "";
		List<String> searchColumns = new ArrayList<>();
		searchColumns.add("cp.company_name");
		searchColumns.add("uul.contact_company");
		TeamMemberFilterDTO teamMemberFilterDTO = utilDao.applyFilterConditions(pagination.getUserId(),
				pagination.isPartnerTeamMemberGroupFilter(), true);
		boolean applyPartnershipIdsFilter = teamMemberFilterDTO.isApplyTeamMemberFilter();
		if (teamMemberFilterDTO.isEmptyFilter()) {
			return hibernateSQLQueryResultUtilDao.returnEmptyPaginatedDTO();
		}

		if (publishedToPartnerGroups) {
			queryString = queryForFindingPartnerGroupsDetails(applyPartnershipIdsFilter);
		} else {
			queryString = queryForFindingPartnerCompaniesDetails(applyPartnershipIdsFilter);
		}
		String partnerSignatureCondition = "";
		if (XamplifyUtils.isValidString(pagination.getPartnerSignatureType())) {
			if (pagination.getPartnerSignatureType().equals(PARTNER_SIGNED)) {
				partnerSignatureCondition = " and dp.is_partner_signature_completed = true ";
			} else if (pagination.getPartnerSignatureType().equals(PARTNER_NOT_SIGNED)) {
				partnerSignatureCondition = " and dp.is_partner_signature_completed = false ";
			}
		}
		queryString = queryString + partnerSignatureCondition;
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultRequestDTO.setSearchColumns(searchColumns);
		hibernateSQLQueryResultRequestDTO.setGroupByQueryString(
				" group by cp.company_id,dp.partnership_id,dp.id,uul.contact_company,dp.dam_id,xp.partner_id,xp.status,d.asset_name,d.asset_type,concat(xup.firstname, ' ', xup.lastname),dp.published_time,xup1.email_id,concat(xup1.firstname, ' ', xup1.lastname),cast(xc.name as text) ");
		hibernateSQLQueryResultRequestDTO.setClassInstance(DamAnalyticsDTO.class);
		addDamIdQueryParameter(damId, hibernateSQLQueryResultRequestDTO);
		if (applyPartnershipIdsFilter) {
			hibernateSQLQueryResultRequestDTO.getQueryParameterListDTOs().add(new QueryParameterListDTO(
					"partnershipIds", teamMemberFilterDTO.getPartnershipIdsOrPartnerCompanyIds()));
		}
		return hibernateSQLQueryResultUtilDao.returnPaginatedDTO(hibernateSQLQueryResultRequestDTO, pagination, search);

	}

	private String queryForFindingPartnerCompaniesDetails(boolean applyPartnershipIdsFilter) {
		String queryString = damViewAnalyticsForCompaniesSheet2Query;
		queryString = applyPartnershipIdsQuery(queryString, applyPartnershipIdsFilter);
		return queryString;
	}

	private String queryForFindingPartnerGroupsDetails(boolean applyPartnershipIdsFilter) {
		String queryString = damViewAnalyticsForGroupsSheet2Query;
		queryString = applyPartnershipIdsQuery(queryString, applyPartnershipIdsFilter);
		return queryString;
	}

	@Override
	public void unPublishDamAssets() {
		String queryString = "UPDATE xt_dam d SET is_published = false FROM (SELECT d.id FROM "
				+ "xt_dam d LEFT JOIN xt_dam_partner p ON d.id = p.dam_id "
				+ "WHERE p.dam_id IS NULL AND d.is_published = true) sub WHERE d.id = sub.id";
		HibernateSQLQueryResultRequestDTO hibernateSQLQueryResultRequestDTO = new HibernateSQLQueryResultRequestDTO();
		hibernateSQLQueryResultRequestDTO.setQueryString(queryString);
		hibernateSQLQueryResultUtilDao.update(hibernateSQLQueryResultRequestDTO);
	}

	@Override
	public DamDTO trackDetailsByTrackId(Integer learningTrackId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "SELECT distinct xlt.title as \"assetName\", cat.name as \"folder\", concat(xup.firstname, '', xup.lastname) as \"fullName\", "
				+ "max(xltv.published_on) as \"publishedOn\" from xt_learning_track xlt "
				+ "left join xt_learning_track_visibility xltv on xlt.id= xltv.learning_track_id "
				+ "left join xt_user_profile xup on xup.user_id= xlt.created_by "
				+ "left JOIN xt_category_module cm ON xlt.id = cm.learning_track_id "
				+ "left join xt_category cat on cat.id = cm.category_id "
				+ "where type='TRACK' and xlt.id= :learningTrackId group by 1,2,3";

		Query query = session.createSQLQuery(queryString).addScalar("assetName", StandardBasicTypes.STRING)
				.addScalar("folder", StandardBasicTypes.STRING).addScalar("fullName", StandardBasicTypes.STRING)
				.addScalar("publishedOn", StandardBasicTypes.TIMESTAMP);
		query.setParameter("learningTrackId", learningTrackId);
		return (DamDTO) paginationUtil.getDto(DamDTO.class, query);
	}

	@Override
	public DamDTO playbookDetailsByPlaybookId(Integer learningTrackId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "SELECT distinct xlt.title as \"assetName\", cat.name as \"folder\", concat(xup.firstname, '', xup.lastname) as \"fullName\", "
				+ "max(xltv.published_on) as \"publishedOn\" from xt_learning_track xlt "
				+ "left join xt_learning_track_visibility xltv on xlt.id= xltv.learning_track_id "
				+ "left join xt_user_profile xup on xup.user_id= xlt.created_by "
				+ "left JOIN xt_category_module cm ON xlt.id = cm.learning_track_id "
				+ "left join xt_category cat on cat.id = cm.category_id "
				+ "where type='PLAYBOOK' and xlt.id= :learningTrackId group by 1,2,3";

		Query query = session.createSQLQuery(queryString).addScalar("assetName", StandardBasicTypes.STRING)
				.addScalar("folder", StandardBasicTypes.STRING).addScalar("fullName", StandardBasicTypes.STRING)
				.addScalar("publishedOn", StandardBasicTypes.TIMESTAMP);
		query.setParameter("learningTrackId", learningTrackId);
		return (DamDTO) paginationUtil.getDto(DamDTO.class, query);
	}

	@Override
	public void deleteDamPartnerGroupMappingsByUserListIdsAndUserIds(List<Integer> userIds, List<Integer> userListIds) {
		if (XamplifyUtils.isNotEmptyList(userIds) && XamplifyUtils.isNotEmptyList(userListIds)) {
			Session session = sessionFactory.getCurrentSession();
			String queryString = "delete from xt_dam_partner_group_mapping where user_id in (:userIds) and user_list_id in (:userListIds)";
			Query query = session.createSQLQuery(queryString);
			query.setParameterList("userIds", userIds);
			query.setParameterList("userListIds", userListIds);
			query.executeUpdate();
		}
	}

	@Override
	public void delateDamPartnersByCompanyId(Integer companyId) {
		Session session = sessionFactory.getCurrentSession();
		String queryString = "DELETE FROM xt_dam_partner WHERE is_partner_group_selected = true \n"
				+ "AND id IN (SELECT dp.id FROM xt_dam d JOIN xt_dam_partner dp ON d.id = dp.dam_id \n"
				+ "LEFT JOIN xt_dam_partner_group_mapping g ON g.dam_partner_id = dp.id \n"
				+ "WHERE d.company_id = :companyId AND dp.is_partner_group_selected = true \n"
				+ "AND g.dam_partner_id IS NULL)";
		session.createSQLQuery(queryString).setParameter("companyId", companyId).executeUpdate();
	}

	private String addNewTileConditionsAccessSharedAssets(Pagination pagination, String finalQueryString) {
		String publicationFilter = pagination.getSelectedApprovalStatusCategory();

		if (XamplifyUtils.isValidString(publicationFilter)) {
			if (publicationFilter.equalsIgnoreCase("interacted")) {
				finalQueryString = finalQueryString.replace("notInteracted", "");

				finalQueryString = finalQueryString
						.replaceAll("interacted1", " LEFT JOIN xt_dam_analytics xda ON xda.dam_partner_fk_id = dp.id ")

						.replaceAll("interacted2",
								" LEFT JOIN xt_dam_analytics xda ON xda.dam_partner_mapping_fk_id = dpm.id ")

						.replaceAll("filterConditionWhereFirst",
								" and ( xda.action_type= 'VIEW' or  xda.action_type= 'DOWNLOAD')");

			} else if (publicationFilter.equalsIgnoreCase("notInteracted")) {
				finalQueryString = finalQueryString
						.replaceAll("interacted1", " LEFT JOIN xt_dam_analytics xda ON xda.dam_partner_fk_id = dp.id ")

						.replaceAll("interacted2",
								" LEFT JOIN xt_dam_analytics xda ON xda.dam_partner_mapping_fk_id = dpm.id ")

						.replaceAll("filterConditionWhereFirst", " AND xda.id IS NULL");
				finalQueryString = finalQueryString.replace("interacted", "");

			} else {
				finalQueryString = finalQueryString.replaceAll("interacted1", "").replaceAll("interacted2", "")

						.replaceAll("filterConditionWhereFirst", "");
			}
		} else {
			finalQueryString = finalQueryString.replaceAll("interacted1", "").replaceAll("interacted2", "")
					.replace("filterConditionWhereFirst", "");
		}

		return finalQueryString;
	}

}