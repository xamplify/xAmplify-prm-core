package com.xtremand.approve.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.approve.dao.ApproveDAO;
import com.xtremand.approve.dto.ApprovalPrivilegesEmailNotificationDTO;
import com.xtremand.approve.dto.ContentReApprovalDTO;
import com.xtremand.approve.dto.MultiSelectApprovalDTO;
import com.xtremand.approve.dto.PendingApprovalDamAndLmsDTO;
import com.xtremand.approve.dto.TeamMemberApprovalPrivilegesDTO;
import com.xtremand.approve.service.ApproveService;
import com.xtremand.category.bom.CategoryModuleEnum;
import com.xtremand.category.dao.CategoryDao;
import com.xtremand.comments.dao.CommentDao;
import com.xtremand.common.bom.Pagination;
import com.xtremand.company.dto.ApprovalSettingsDTO;
import com.xtremand.dam.bom.ApprovalStatusHistory;
import com.xtremand.dam.bom.ApprovalStatusType;
import com.xtremand.dam.bom.Dam;
import com.xtremand.dam.bom.DamPartner;
import com.xtremand.dam.bom.DamTag;
import com.xtremand.dam.dao.DamDao;
import com.xtremand.dam.dto.DamUploadPostDTO;
import com.xtremand.dam.dto.SharedAssetDetailsViewDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.lms.bom.LearningTrack;
import com.xtremand.team.member.dto.RoleDisplayDTO;
import com.xtremand.user.bom.User;
import com.xtremand.user.dao.hibernate.HibernateUserDAO;
import com.xtremand.util.XamplifyUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.bom.ModuleType;
import com.xtremand.util.dao.XamplifyUtilDao;
import com.xtremand.util.dto.ApprovalStatisticsDTO;
import com.xtremand.util.dto.LeftSideNavigationBarItem;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.dto.PaginatedDTO;
import com.xtremand.util.service.UtilService;
import com.xtremand.video.bom.VideoTag;
import com.xtremand.video.dao.VideoDao;
import com.xtremand.white.labeled.dto.DamVideoDTO;

@Service("approveService")
@Transactional
public class ApproveServiceImpl implements ApproveService {
	
	private static final Logger logger = LoggerFactory.getLogger(ApproveServiceImpl.class);
	
	@Autowired
	private UtilService utilService;
	
	@Autowired
	private ApproveDAO approveDao;
	
	@Autowired
	private HibernateUserDAO userDao;
	
	@Autowired
	private XamplifyUtilDao xamplifyUtilDao;
	
	@Autowired
	private CommentDao commentDao;

	@Autowired
	private CategoryDao categoryDao;

	@Autowired
	private DamDao damDao;
	
	@Autowired
	private VideoDao videoDAO;
	
	@Autowired
	private XamplifyUtil xamplifyUtil;
	
	private static final String INVALID_DATA = "Invalid input parameters";
	
	private static final String SETTINGS_UPDATED_SUCCESSFULLY_MESSAGE = "Settings Updated Successfully";

	private static final String VIDEO_TAGS_TO_SAVE_MAP_KEY = "videoTagsToSave";
	
	private static final String VIDEO_IDS_TO_DELETE_MAP_KEY = "videoIdsToDelete";
	
	@Override
	public XtremandResponse getAllApprovalList(Pagination pagination, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		if (XamplifyUtils.isValidInteger(loggedInUserId)) {
			pagination.setUserId(loggedInUserId);
			Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
			pagination.setCompanyId(companyId);
			String searchkey = pagination.getSearchKey();
			utilService.setDateFilters(pagination);
			LeftSideNavigationBarItem leftSideNavigationBarItem = new LeftSideNavigationBarItem();
			frameContentAccessForUser(loggedInUserId, leftSideNavigationBarItem);
			PaginatedDTO paginatedDTO = approveDao.getAllApprovalList(pagination, searchkey, leftSideNavigationBarItem);
			TeamMemberApprovalPrivilegesDTO loggedInUserPrivileges = fetchApprovalPrivileges(loggedInUserId, companyId);
			response.setStatusCode(200);
			Map<String, Object> map = new HashMap<>();
			map.put("paginatedDTO", paginatedDTO);
			map.put("loggedInUserPrivileges", loggedInUserPrivileges);
			response.setMap(map);
		} else {
			XamplifyUtils.addErorMessageWithStatusCode(response, INVALID_DATA, 400);
			return response;
		}
		return response;
	}

	private void frameContentAccessForUser(Integer loggedInUserId,
			LeftSideNavigationBarItem leftSideNavigationBarItem) {
		leftSideNavigationBarItem.setUserId(loggedInUserId);
		RoleDisplayDTO roleDisplayDto = utilService.getRoleDetailsByUserId(loggedInUserId);
		List<Integer> roleIds = roleDisplayDto.getRoleIds();
		boolean isAnyAdmin = roleDisplayDto.isAnyAdmin();
		utilService.setDAMAccess(leftSideNavigationBarItem, loggedInUserId, roleIds, isAnyAdmin);
		utilService.setLearningTrackAccess(leftSideNavigationBarItem, loggedInUserId, roleIds, isAnyAdmin);
		utilService.setPlayBookAccess(leftSideNavigationBarItem, loggedInUserId, roleIds, isAnyAdmin);
	}

	@Override
	public XtremandResponse getStatusTileCounts(Integer loggedInUserId, Pageable pageable) {
		XtremandResponse response = new XtremandResponse();
		if (XamplifyUtils.isValidInteger(loggedInUserId)) {
			Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
			Pagination pagination = utilService.setPageableParameters(pageable, loggedInUserId);
			pagination.setFilterBy(pageable.getFilterBy());
			framePaginationValues(pageable, pagination);
			LeftSideNavigationBarItem leftSideNavigationBarItem = new LeftSideNavigationBarItem();
			frameContentAccessForUser(loggedInUserId, leftSideNavigationBarItem);
			ApprovalStatisticsDTO approvalStatisticsDTO = approveDao.getStatusTileCounts(companyId, pagination,leftSideNavigationBarItem);
			response.setData(approvalStatisticsDTO);
			XamplifyUtils.addSuccessStatus(response);
		} else {
			XamplifyUtils.addErorMessageWithStatusCode(response, INVALID_DATA, 400);
			return response;
		}
		return response;
	}

	private void framePaginationValues(Pageable pageable, Pagination pagination) {
		String toDateFilterString = pageable.getToDateFilterString();
		String fromDateFilterString = pageable.getFromDateFilterString();
		String timeZone = pageable.getTimeZone();
		if (XamplifyUtils.isValidString(toDateFilterString) && XamplifyUtils.isValidString(fromDateFilterString)
				&& XamplifyUtils.isValidString(timeZone)) {
			pagination.setFromDateFilterString(fromDateFilterString);
			pagination.setToDateFilterString(toDateFilterString);
			pagination.setTimeZone(timeZone);
			utilService.setDateFilters(pagination);
		}
	}

	@Override
	public XtremandResponse updateApprovalStatusByTypeForMultiSelect(Integer loggedInUserId,
			MultiSelectApprovalDTO multiSelectApprovalDto, XtremandResponse response) {
		List<ApprovalStatusHistory> approvalStatusHistoryList = new ArrayList<>();
		if (XamplifyUtils.isValidInteger(loggedInUserId)) {
			String approvalStatus = multiSelectApprovalDto.getStatus();
			List<Integer> damIds = multiSelectApprovalDto.getDamIds();
			List<Integer> trackIds = multiSelectApprovalDto.getTrackIds();
			List<Integer> playBooksIds = multiSelectApprovalDto.getPlayBooksIds();

			Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
			TeamMemberApprovalPrivilegesDTO loggedInUserPrivileges = fetchApprovalPrivileges(loggedInUserId, companyId);

			if (validateRejectedAssets(damIds, approvalStatus) || validateRejectedTracks(trackIds, approvalStatus)
					|| validateRejectedPlayBooks(playBooksIds, approvalStatus)) {
				returnNonRejectedRecords(response, damIds, trackIds, playBooksIds);
				return response;
			}

			updateStatusByTypeAndApprovalAccessForMultiSelect(loggedInUserId, multiSelectApprovalDto, approvalStatusHistoryList,
					approvalStatus, loggedInUserPrivileges, companyId);

			XamplifyUtils.addSuccessStatus(response);

		} else {
			XamplifyUtils.addErorMessageWithStatusCode(response, INVALID_DATA, 400);
			return response;
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	private void updateStatusByTypeAndApprovalAccessForMultiSelect(Integer loggedInUserId,
			MultiSelectApprovalDTO multiSelectApprovalDto, List<ApprovalStatusHistory> approvalStatusHistoryList,
			String approvalStatus, TeamMemberApprovalPrivilegesDTO loggedInUserPrivileges, Integer companyId) {
		List<Integer> damIds = multiSelectApprovalDto.getDamIds();
		List<Integer> trackIds = multiSelectApprovalDto.getTrackIds();
		List<Integer> playBooksIds = multiSelectApprovalDto.getPlayBooksIds();
		List<Integer> reApprovalDamVersionIds = new ArrayList<>();
		
		if (XamplifyUtils.isNotEmptyList(damIds) && loggedInUserPrivileges.isAssetApprover()) {
			if (ApprovalStatusType.APPROVED.name().equals(approvalStatus)) {
				reApprovalDamVersionIds = approveDao.getReApprovalVersionDamIdsForMultiSelect(damIds, companyId);
				reApprovalDamVersionIds.removeIf(Objects::isNull);
				damIds.removeAll(reApprovalDamVersionIds);
			}
			updateDamApprovalAndSaveComments(multiSelectApprovalDto.getComment(), damIds, approvalStatus, loggedInUserId,
					approvalStatusHistoryList);
		}

		if (XamplifyUtils.isNotEmptyList(trackIds) && loggedInUserPrivileges.isTrackApprover()) {
			updateTrackApprovalAndSaveComments(multiSelectApprovalDto, approvalStatus, loggedInUserId,
					approvalStatusHistoryList);
		}
		if (XamplifyUtils.isNotEmptyList(playBooksIds) && loggedInUserPrivileges.isPlaybookApprover()) {
			updatePlayBookApprovalAndSaveComments(multiSelectApprovalDto, approvalStatus, loggedInUserId,
					approvalStatusHistoryList);
		}
		/** XNFR-885 **/
		List<DamTag> allDamTagsToSave = new ArrayList<>();
		List<VideoTag> videoTagsToSave = new ArrayList<>();
		List<Integer> videoIdsToDelete = new ArrayList<>();
		List<Integer> damIdsToDelete = new ArrayList<>();
		
		if (XamplifyUtils.isNotEmptyList(reApprovalDamVersionIds)) {
			List<ContentReApprovalDTO> contentReApprovalDTOs = damDao.getAssetDetailsForReApproval(reApprovalDamVersionIds);
			
			List<Integer> whiteLabeledReApprovalDamIds = contentReApprovalDTOs.stream()
				    .filter(ContentReApprovalDTO::isWhiteLabeledAssetSharedWithPartners)
				    .map(ContentReApprovalDTO::getApprovalReferenceId)
				    .collect(Collectors.toList());
			multiSelectApprovalDto.setWhiteLabeledReApprovalDamIds(whiteLabeledReApprovalDamIds);
			multiSelectApprovalDto.setLoggedInUserId(loggedInUserId);
			multiSelectApprovalDto.setCompanyId(companyId);
			
			 List<ContentReApprovalDTO> pdfTypeAssetContentDetails = contentReApprovalDTOs.stream()
					    .filter(dto -> "pdf".equalsIgnoreCase(dto.getAssetType()))
					    .collect(Collectors.toList());
			
			contentReApprovalDTOs.forEach(dto -> dto.setLoggedInUserId(loggedInUserId));
			List<ContentReApprovalDTO> videoTypeAssetContentDetails = contentReApprovalDTOs.stream()
				        .filter(dto -> dto.getVideoId() != null) 
				        .collect(Collectors.toList());  
		    List<ContentReApprovalDTO> nonVideoTypeAssetContentDetails = contentReApprovalDTOs.stream()
		        .filter(dto -> dto.getVideoId() == null)
		        .collect(Collectors.toList());

		    damIdsToDelete = processNonVideoAssetReApprovalAndGetIds(loggedInUserId, allDamTagsToSave,
					nonVideoTypeAssetContentDetails, approvalStatusHistoryList, multiSelectApprovalDto.getComment());
		    
		    Map<String, Object> resultMap = handleReApprovalVersionForVideoTypeAsset(loggedInUserId, videoTypeAssetContentDetails, approvalStatusHistoryList, multiSelectApprovalDto.getComment());
	        videoTagsToSave = (List<VideoTag>) resultMap.get(VIDEO_TAGS_TO_SAVE_MAP_KEY);
	        videoIdsToDelete = (List<Integer>) resultMap.get(VIDEO_IDS_TO_DELETE_MAP_KEY);
	  
	        handleSharedAssetPathForPdfTypeAssets(pdfTypeAssetContentDetails);

		}
		
		finalizeReApprovalAssetChanges(allDamTagsToSave, videoTagsToSave, videoIdsToDelete, damIdsToDelete);
		
		if (XamplifyUtils.isNotEmptyList(approvalStatusHistoryList)) {
			xamplifyUtilDao.saveAll(approvalStatusHistoryList, "Approval Status History");
		}
	}

	private void finalizeReApprovalAssetChanges(List<DamTag> allDamTagsToSave, List<VideoTag> videoTagsToSave,
			List<Integer> videoIdsToDelete, List<Integer> damIdsToDelete) {
		if (XamplifyUtils.isNotEmptyList(videoIdsToDelete)) {
			videoDAO.deleteVideoRecordsByIds(videoIdsToDelete);
		}
		
		if (XamplifyUtils.isNotEmptyList(damIdsToDelete)) {
			damDao.deleteByDamIds(damIdsToDelete);
		}
		
		if (XamplifyUtils.isNotEmptyList(allDamTagsToSave)) {
			xamplifyUtilDao.saveAll(allDamTagsToSave, "Dam Tag for Re-Approval");
		}
		
		if (XamplifyUtils.isNotEmptyList(videoTagsToSave)) {
			xamplifyUtilDao.saveAll(videoTagsToSave, "VideoTag for Re-Approval");
		}
	}
	/** XNFR-885 **/
	@Override
	public Map<String, Object> handleReApprovalVersionForVideoTypeAsset(Integer loggedInUserId,
			List<ContentReApprovalDTO> videoTypeAssetContentDetails, List<ApprovalStatusHistory> approvalHistoryList, String comment) {
		List<VideoTag> videoTagsToSave = new ArrayList<>();
		List<Integer> videoIdsToDelete = new ArrayList<>();
		if (XamplifyUtils.isNotEmptyList(videoTypeAssetContentDetails)) {
			for (ContentReApprovalDTO videoTypeAssetContent: videoTypeAssetContentDetails) {
				DamVideoDTO videoFileDetails = damDao.getReApprovalVersionVideoAssetDetails(videoTypeAssetContent.getVideoId());
				Integer parentVideoId = damDao.getVideoIdByDamId(videoTypeAssetContent.getApprovalReferenceId());
	            List<String> tagToRemove = processTags(videoFileDetails, parentVideoId, videoTagsToSave);
				updateCategory(videoTypeAssetContent.getCategoryId(), videoTypeAssetContent.getApprovalReferenceId(), loggedInUserId);
				damDao.replaceParentAssetMetadataAfterReApproval(videoTypeAssetContent, videoTypeAssetContent.getApprovalReferenceId());
				damDao.replaceParentVideoFileDetailsWithChildForReApproval(videoFileDetails, parentVideoId);
				damDao.replaceImagesAndGifsForReApproval(videoFileDetails, parentVideoId);
				if (XamplifyUtils.isNotEmptyList(tagToRemove)) {
					approveDao.deleteVideoTagsAfterReApprovalByNames(tagToRemove, parentVideoId);
				}
				videoIdsToDelete.add(videoTypeAssetContent.getVideoId());
				commentDao.replaceReApprovalVersionAssetCommentsToParentAsset(videoTypeAssetContent.getId(), videoTypeAssetContent.getApprovalReferenceId());
				generateApprovalStatusHistoryForReApprovalVersion(approvalHistoryList, comment, loggedInUserId, videoTypeAssetContent.getApprovalReferenceId());
			}
		}
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put(VIDEO_TAGS_TO_SAVE_MAP_KEY, videoTagsToSave);
		resultMap.put(VIDEO_IDS_TO_DELETE_MAP_KEY, videoIdsToDelete);
		return resultMap;
	}
	
	
	private List<String> processTags(DamVideoDTO videoFileDetails, Integer parentVideoId, List<VideoTag> videoTagsToSave) {
	    List<String> childTags = XamplifyUtils.isValidString(videoFileDetails.getTagsInString()) 
	        ? XamplifyUtils.convertStringToArrayList(videoFileDetails.getTagsInString()) 
	        : new ArrayList<>();
	    
	    List<String> parentTags = videoDAO.getTagNamesByVideoId(parentVideoId);
	    List<String> tagsToRemove = new ArrayList<>();
	    List<String> tags = new ArrayList<>();
	    
	    for (String parentTag : parentTags) {
			tags.add(parentTag);
			if (childTags == null || childTags.isEmpty() || !childTags.contains(parentTag)) {
				tagsToRemove.add(parentTag);
			}
		}

	    populateVideoTags(childTags, parentVideoId, videoTagsToSave, tags);
	    return tagsToRemove;
	}
	
	private void populateVideoTags(List<String> childVideoTags, Integer parentVideoId, List<VideoTag> videoTags, List<String> tags) {
		if (childVideoTags != null && XamplifyUtils.isNotEmptyList(childVideoTags)) {
			childVideoTags.removeAll(tags);
			for (String tagId : childVideoTags) {
				VideoTag videoTag = new VideoTag();
				videoTag.setVideoId(parentVideoId);
				videoTag.setTag(tagId);
				videoTags.add(videoTag);
			}
		}
	}
	
	@Override
	public List<Integer> processNonVideoAssetReApprovalAndGetIds(Integer loggedInUserId, List<DamTag> allDamTagsToSave,
			List<ContentReApprovalDTO> nonVideoTypeAssetContentDetails, List<ApprovalStatusHistory> approvalHistoryList, String comment) {
		List<Integer> damIdsToDelete = new ArrayList<>();
		if (XamplifyUtils.isNotEmptyList(nonVideoTypeAssetContentDetails)) {
			for (ContentReApprovalDTO contentReApprovalDTO: nonVideoTypeAssetContentDetails) {
				damDao.replaceParentAssetMetadataAfterReApproval(contentReApprovalDTO, contentReApprovalDTO.getApprovalReferenceId());
				updateCategory(contentReApprovalDTO.getCategoryId(), contentReApprovalDTO.getApprovalReferenceId(), loggedInUserId);
				List<DamTag> damTagsToSave = damDao.updateTagsAfterReApprovalAndReturnTagsToSave(
						contentReApprovalDTO,  contentReApprovalDTO.getApprovalReferenceId(), contentReApprovalDTO.getId(), loggedInUserId);
				damIdsToDelete.add(contentReApprovalDTO.getId());
				allDamTagsToSave.addAll(damTagsToSave);
				commentDao.replaceReApprovalVersionAssetCommentsToParentAsset(contentReApprovalDTO.getId(), contentReApprovalDTO.getApprovalReferenceId());
				generateApprovalStatusHistoryForReApprovalVersion(approvalHistoryList, comment, loggedInUserId, contentReApprovalDTO.getApprovalReferenceId());
			}
		}
		return damIdsToDelete;
	}
	
	/** XNFR-885 **/
	private void updateCategory(Integer categoryId, Integer approvalReferenceId, Integer userId) {
	    if (XamplifyUtils.isValidInteger(categoryId) && XamplifyUtils.isValidInteger(approvalReferenceId) && XamplifyUtils.isValidInteger(userId)) {
	        categoryDao.updateCategoryIdByType(approvalReferenceId, categoryId, userId, CategoryModuleEnum.DAM.name());
	    }
	}

	private boolean validateRejectedTracks(List<Integer> trackIds, String status) {
		return ApprovalStatusType.REJECTED.name().equals(status) && XamplifyUtils.isNotEmptyList(trackIds)
				&& approveDao.isPublished(trackIds);
	}

	private boolean validateRejectedPlayBooks(List<Integer> playBooksIds, String status) {
		return ApprovalStatusType.REJECTED.name().equals(status) && XamplifyUtils.isNotEmptyList(playBooksIds)
				&& approveDao.isPublished(playBooksIds);
	}

	private boolean validateRejectedAssets(List<Integer> damIds, String status) {
		return ApprovalStatusType.REJECTED.name().equals(status) && XamplifyUtils.isNotEmptyList(damIds)
				&& (approveDao.isAssociatedWithLMS(damIds) || damDao.hasAnyReApprovalVersionsCreated(damIds));
	}

	private void updateDamApprovalAndSaveComments(String comment, List<Integer> damIds  , String approvalStatus,
			Integer loggedInUserId, List<ApprovalStatusHistory> approvalStatusHistoryList) {
		if (XamplifyUtils.isNotEmptyList(damIds) && XamplifyUtils.isValidInteger(loggedInUserId) && XamplifyUtils.isValidString(approvalStatus)) {
			Integer updateCount = approveDao.updateApprovalStatus(damIds, approvalStatus, loggedInUserId,"DAM");
			if (XamplifyUtils.isNotEmptyList(damIds) && updateCount > 0) {
				for (Integer damId : damIds) {
					ApprovalStatusHistory approvalStatusHistory = new ApprovalStatusHistory();
					setApprovalHistoryValues(approvalStatus, approvalStatusHistory,comment,loggedInUserId);
					approvalStatusHistory.setModuleType(ModuleType.DAM);
					Dam dam = new Dam();
					dam.setId(damId);
					approvalStatusHistory.setDam(dam);
					approvalStatusHistoryList.add(approvalStatusHistory);
				}
			}
		}
	}

	private void setApprovalHistoryValues(String approvalStatus, ApprovalStatusHistory approvalStatusHistory,
			String comment, Integer loggedInUserId) {
		approvalStatusHistory.setComment(comment);
		approvalStatusHistory.setCreatedTime(new Date());
		User user = new User();
		user.setUserId(loggedInUserId);
		approvalStatusHistory.setCreatedBy(user);
		if (XamplifyUtils.isValidString(approvalStatus)) {
			if (ApprovalStatusType.APPROVED.name().equals(approvalStatus)) {
				approvalStatusHistory.setStatus(ApprovalStatusType.APPROVED);
			} else if (ApprovalStatusType.REJECTED.name().equals(approvalStatus)) {
				approvalStatusHistory.setStatus(ApprovalStatusType.REJECTED);
			}
		}
	}
	
	private void updateTrackApprovalAndSaveComments(MultiSelectApprovalDTO multiSelectApprovalDto,
			String approvalStatus, Integer loggedInUserId, List<ApprovalStatusHistory> approvalStatusHistoryList) {
		List<Integer> trackIds = multiSelectApprovalDto.getTrackIds();
		String comment = multiSelectApprovalDto.getComment();
		Integer updateCount = approveDao.updateApprovalStatus(trackIds, approvalStatus, loggedInUserId,"TRACK");
		if (updateCount > 0) {
			for (Integer trackId : trackIds) {
				ApprovalStatusHistory approvalStatusHistory = new ApprovalStatusHistory();
				setApprovalHistoryValues(approvalStatus, approvalStatusHistory, comment, loggedInUserId);
				approvalStatusHistory.setModuleType(ModuleType.TRACK);
				LearningTrack learningTrack = new LearningTrack();
				learningTrack.setId(trackId);
				approvalStatusHistory.setLearningTrack(learningTrack);
				approvalStatusHistoryList.add(approvalStatusHistory);
			}
		}
	}
	
	private void updatePlayBookApprovalAndSaveComments(MultiSelectApprovalDTO multiSelectApprovalDto,
			String approvalStatus, Integer loggedInUserId, List<ApprovalStatusHistory> approvalStatusHistoryList) {
		List<Integer> playBookIds = multiSelectApprovalDto.getPlayBooksIds();
		String comment = multiSelectApprovalDto.getComment();
		Integer updateCount = approveDao.updateApprovalStatus(playBookIds, approvalStatus, loggedInUserId, "PLAYBOOK");
		if (updateCount > 0) {
			for (Integer playBookId : playBookIds) {
				ApprovalStatusHistory approvalStatusHistory = new ApprovalStatusHistory();
				setApprovalHistoryValues(approvalStatus, approvalStatusHistory, comment, loggedInUserId);
				approvalStatusHistory.setModuleType(ModuleType.PLAYBOOK);
				LearningTrack learningTrack = new LearningTrack();
				learningTrack.setId(playBookId);
				approvalStatusHistory.setLearningTrack(learningTrack);
				approvalStatusHistoryList.add(approvalStatusHistory);
			}
		}
	}
	
	private void returnNonRejectedRecords(XtremandResponse response, List<Integer> damIds, List<Integer> trackIds,
			List<Integer> playBooksIds) {
		response.setStatusCode(401);
		List<String> assetNames = approveDao.findNamesByAssetId(damIds);
		List<String> trackNames = approveDao.findNamesBylmsId(trackIds);
		List<String> playBookNames = approveDao.findNamesBylmsId(playBooksIds);
		trackNames.addAll(playBookNames);
		assetNames.addAll(trackNames);
		String message = "The below record(s) cannot be rejected";
		response.setMessage(message);
		response.setData(assetNames);
		response.setAccess(true);
	}

	/** XNFR-821 **/
	@Override
	public XtremandResponse listTeamMembersForApprovalControlManagement(Pageable pageable, Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		if (!XamplifyUtils.isValidInteger(loggedInUserId)) {
			XamplifyUtils.addErorMessageWithStatusCode(response, INVALID_DATA, 400);
			return response;
		}
		Pagination pagination = utilService.setPageableParameters(pageable, loggedInUserId);
		PaginatedDTO paginatedDTO = approveDao.listTeamMembersForApprovalControlManagement(pagination, pageable.getSearch());
		XamplifyUtils.addPaginatedDTO(response, paginatedDTO);
		XamplifyUtils.addSuccessStatus(response);
		return response;
	}
	
	/** XNFR-821 **/
	@SuppressWarnings("unchecked")
	@Override
	public XtremandResponse saveOrUpdateApprovalControlManagementSettings(Integer loggedInUserId, List<TeamMemberApprovalPrivilegesDTO> teamMemberApprovalPrivilegesDTOs,
			List<ApprovalPrivilegesEmailNotificationDTO> approvalPrivilegesEmailNotificationDTOs, MultiSelectApprovalDTO multiSelectApprovalDTO, XtremandResponse response) {
		Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
		List<ApprovalStatusHistory> approvalHistoryList = new ArrayList<>();
		List<DamTag> allDamTagsToSave = new ArrayList<>();
		List<VideoTag> videoTagsToSave = new ArrayList<>();
		List<Integer> videoIdsToDelete = new ArrayList<>();
		List<Integer> damIdsToDelete = new ArrayList<>();
		List<Integer> reApprovalVersionDamIds = new ArrayList<>();
		Integer updatedRowCount = null;
		if (!validateApprovalSettingsInputs(loggedInUserId, companyId, teamMemberApprovalPrivilegesDTOs)) {
			XamplifyUtils.addErorMessageWithStatusCode(response, INVALID_DATA, 400);
			return response;
		}
				
		for (TeamMemberApprovalPrivilegesDTO teamMemberApprovalPrivilegesDTO : teamMemberApprovalPrivilegesDTOs) {
			if (teamMemberApprovalPrivilegesDTO == null || !XamplifyUtils.isValidInteger(teamMemberApprovalPrivilegesDTO.getId())) {
				continue;
			}

			TeamMemberApprovalPrivilegesDTO existingTeamMemberApprovalSettings = approveDao
					.getTeamMemberApprovalPrivilegeSettingsByTeamMemberId(teamMemberApprovalPrivilegesDTO.getId(), companyId);
			ApprovalPrivilegesEmailNotificationDTO approvalPrivilegesEmailNotificationDTO = new ApprovalPrivilegesEmailNotificationDTO();
			approvalPrivilegesEmailNotificationDTO.setId(teamMemberApprovalPrivilegesDTO.getId());
			approvalPrivilegesEmailNotificationDTO.setPrivilegesUpdatedBy(loggedInUserId);
			if (checkApprovalPrivilegeChangesAndUpdateNotification(teamMemberApprovalPrivilegesDTO,
					existingTeamMemberApprovalSettings, approvalPrivilegesEmailNotificationDTO)) {
				updatedRowCount = approveDao.updateTeamMemberApprovalPrivilegeSettings(teamMemberApprovalPrivilegesDTO,
						companyId);
				if (XamplifyUtils.isValidInteger(updatedRowCount)) {
					autoApprovePendingContentForTheNewlyAssignedApprover(teamMemberApprovalPrivilegesDTO, loggedInUserId, companyId, approvalHistoryList);
					approvalPrivilegesEmailNotificationDTOs.add(approvalPrivilegesEmailNotificationDTO);
				}
			}
		}
		
		if(XamplifyUtils.isValidInteger(updatedRowCount)) {
			List<Integer> createdByIds = teamMemberApprovalPrivilegesDTOs.stream().map(TeamMemberApprovalPrivilegesDTO::getId).collect(Collectors.toList());
			reApprovalVersionDamIds = approveDao.getReApprovalVersionDamIdsByUserIds(createdByIds, companyId);
			
		}

		if (XamplifyUtils.isNotEmptyList(reApprovalVersionDamIds)) {
			List<ContentReApprovalDTO> contentReApprovalDTOs = damDao.getAssetDetailsForReApproval(reApprovalVersionDamIds);
			
			List<Integer> whiteLabeledReApprovalDamIds = contentReApprovalDTOs.stream()
				    .filter(ContentReApprovalDTO::isWhiteLabeledAssetSharedWithPartners)
				    .map(ContentReApprovalDTO::getApprovalReferenceId)
				    .collect(Collectors.toList());
			
			multiSelectApprovalDTO.setWhiteLabeledReApprovalDamIds(whiteLabeledReApprovalDamIds);
			multiSelectApprovalDTO.setLoggedInUserId(loggedInUserId);
			multiSelectApprovalDTO.setCompanyId(companyId);

			List<ContentReApprovalDTO> pdfTypeAssetContentDetails = contentReApprovalDTOs.stream()
				    .filter(dto -> "pdf".equalsIgnoreCase(dto.getAssetType()))
				    .collect(Collectors.toList());
					
			contentReApprovalDTOs.forEach(dto -> dto.setLoggedInUserId(loggedInUserId));
			List<ContentReApprovalDTO> videoTypeAssetContentDetails = contentReApprovalDTOs.stream()
				        .filter(dto -> dto.getVideoId() != null) 
				        .collect(Collectors.toList());  
		    List<ContentReApprovalDTO> nonVideoTypeAssetContentDetails = contentReApprovalDTOs.stream()
		        .filter(dto -> dto.getVideoId() == null)
		        .collect(Collectors.toList());
		    
		    damIdsToDelete = processNonVideoAssetReApprovalAndGetIds(loggedInUserId, allDamTagsToSave,
					nonVideoTypeAssetContentDetails, approvalHistoryList, "");
		    
		    Map<String, Object> resultMap = handleReApprovalVersionForVideoTypeAsset(loggedInUserId, videoTypeAssetContentDetails, approvalHistoryList, "");
	        videoTagsToSave = (List<VideoTag>) resultMap.get(VIDEO_TAGS_TO_SAVE_MAP_KEY);
	        videoIdsToDelete = (List<Integer>) resultMap.get(VIDEO_IDS_TO_DELETE_MAP_KEY);
	        
	        handleSharedAssetPathForPdfTypeAssets(pdfTypeAssetContentDetails);
		}
		
		finalizeReApprovalAssetChanges(allDamTagsToSave, videoTagsToSave, videoIdsToDelete, damIdsToDelete);
		
		if (XamplifyUtils.isNotEmptyList(approvalHistoryList)) {
			xamplifyUtilDao.saveAll(approvalHistoryList, "Auto approve - Approval Status History");
		}
		
		XamplifyUtils.addSuccessStatusWithMessage(response, SETTINGS_UPDATED_SUCCESSFULLY_MESSAGE);
		return response;		
	}
	
	private void autoApprovePendingContentForTheNewlyAssignedApprover(TeamMemberApprovalPrivilegesDTO teamMemberApprovalPrivilegesDTO, Integer loggedInUserId,
			Integer companyId, List<ApprovalStatusHistory> approvalHistoryList) {

		List<Integer> createdByIds = new ArrayList<>();
		createdByIds.add(teamMemberApprovalPrivilegesDTO.getId());

		if (teamMemberApprovalPrivilegesDTO.isAssetApproverFieldUpdated() && teamMemberApprovalPrivilegesDTO.isAssetApprover()) {
			appendGeneratedApprovalHistory(createdByIds, ModuleType.DAM, approvalHistoryList);
			approveDao.autoApprovePendingAssetsCreatedByNewlyAssignedApprover(teamMemberApprovalPrivilegesDTO, loggedInUserId, companyId);
	       
		}

		if (teamMemberApprovalPrivilegesDTO.isTrackApproverFieldUpdated() && teamMemberApprovalPrivilegesDTO.isTrackApprover()) {
			appendGeneratedApprovalHistory(createdByIds, ModuleType.TRACK, approvalHistoryList);
			approveDao.autoApprovePendingTracksOrPlaybooksCreatedByNewlyAssignedApprover(teamMemberApprovalPrivilegesDTO, companyId, loggedInUserId, ModuleType.TRACK.name());
		}

		if (teamMemberApprovalPrivilegesDTO.isPlaybookApproverFieldUpdated() && teamMemberApprovalPrivilegesDTO.isPlaybookApprover()) {
			appendGeneratedApprovalHistory(createdByIds, ModuleType.PLAYBOOK, approvalHistoryList);
			approveDao.autoApprovePendingTracksOrPlaybooksCreatedByNewlyAssignedApprover(teamMemberApprovalPrivilegesDTO, companyId, loggedInUserId, ModuleType.PLAYBOOK.name());
		}
		
	}
	
	
	private void appendGeneratedApprovalHistory(List<Integer> createdByIds, ModuleType moduleType, List<ApprovalStatusHistory> approvalHistoryList) {
		List<ApprovalStatusHistory> historyList = iterateAndBuildApprovalStatusHistoryList(createdByIds, moduleType);
		if (XamplifyUtils.isNotEmptyList(historyList)) {
			approvalHistoryList.addAll(historyList);
		}
	}
	
	@Override
	public List<ApprovalStatusHistory> processAndSaveApprovalTimelineHistory(List<Integer> createdByIds, Integer loggedInUserId) {

	    List<ApprovalStatusHistory> approvalHistoryList = new ArrayList<>();

	    List<ApprovalStatusHistory> damHistory = iterateAndBuildApprovalStatusHistoryList(createdByIds, ModuleType.DAM);
	    approvalHistoryList.addAll(damHistory);

	    List<ApprovalStatusHistory> trackHistory = iterateAndBuildApprovalStatusHistoryList(createdByIds, ModuleType.TRACK);
	    approvalHistoryList.addAll(trackHistory);

	    List<ApprovalStatusHistory> playbookHistory = iterateAndBuildApprovalStatusHistoryList(createdByIds, ModuleType.PLAYBOOK);
	    approvalHistoryList.addAll(playbookHistory);

	    return approvalHistoryList;
	}

	private List<ApprovalStatusHistory> iterateAndBuildApprovalStatusHistoryList(List<Integer> createdByIds, ModuleType moduleType) {
		List<MultiSelectApprovalDTO> multiSelectApprovalDTOs  = approveDao.getPendingApprovalEntityIdsByCreatorAndModuleType(createdByIds,  moduleType.name().toUpperCase());
		List<ApprovalStatusHistory> approvalStatusHistoryList = new ArrayList<>();
		if (XamplifyUtils.isNotEmptyList(multiSelectApprovalDTOs)) {
			for (MultiSelectApprovalDTO multiSelectApprovalDTO : multiSelectApprovalDTOs) {
				ApprovalStatusHistory approvalStatusHistory = new ApprovalStatusHistory();
				approvalStatusHistory.setCreatedTime(new Date());
				User user = new User();
				user.setUserId(multiSelectApprovalDTO.getCreatedById());
				approvalStatusHistory.setCreatedBy(user);
				approvalStatusHistory.setStatus(ApprovalStatusType.APPROVED);
				approvalStatusHistory.setModuleType(moduleType);
				commentDao.setApprovalStatusHistoryEntityIdByModuleType(multiSelectApprovalDTO.getEntityId(), moduleType, approvalStatusHistory);
				approvalStatusHistoryList.add(approvalStatusHistory);
			}
		}
		return approvalStatusHistoryList;
	}
	
	private boolean checkApprovalPrivilegeChangesAndUpdateNotification(TeamMemberApprovalPrivilegesDTO teamMemberApprovalPrivilegesDTO,
			TeamMemberApprovalPrivilegesDTO existingTeamMemberApprovalSettings,
			ApprovalPrivilegesEmailNotificationDTO approvalPrivilegesEmailNotificationDTO) {
		boolean isUpdated = false;
		if (existingTeamMemberApprovalSettings != null) {
			if (existingTeamMemberApprovalSettings.isAssetApprover() != teamMemberApprovalPrivilegesDTO
					.isAssetApprover()) {
				approvalPrivilegesEmailNotificationDTO.setAssetApproverFieldUpdated(true);
				teamMemberApprovalPrivilegesDTO.setAssetApproverFieldUpdated(true);
				isUpdated = true;
			}
			if (existingTeamMemberApprovalSettings.isTrackApprover() != teamMemberApprovalPrivilegesDTO
					.isTrackApprover()) {
				approvalPrivilegesEmailNotificationDTO.setTrackApproverFieldUpdated(true);
				teamMemberApprovalPrivilegesDTO.setTrackApproverFieldUpdated(true);
				isUpdated = true;
			}
			if (existingTeamMemberApprovalSettings.isPlaybookApprover() != teamMemberApprovalPrivilegesDTO
					.isPlaybookApprover()) {
				approvalPrivilegesEmailNotificationDTO.setPlaybookApproverFieldUpdated(true);
				teamMemberApprovalPrivilegesDTO.setPlaybookApproverFieldUpdated(true);
				isUpdated = true;
			}
		}
		return isUpdated;
	}
	
	private boolean validateApprovalSettingsInputs(Integer loggedInUserId, Integer companyId, List<TeamMemberApprovalPrivilegesDTO> teamMemberApprovalPrivilegesDTOs) {
	    return XamplifyUtils.isValidInteger(loggedInUserId) && XamplifyUtils.isValidInteger(companyId)
	            && teamMemberApprovalPrivilegesDTOs != null && XamplifyUtils.isNotEmptyList(teamMemberApprovalPrivilegesDTOs);
	}

	/** XNFR-821 **/
	@Override
	public XtremandResponse getApprovalPrivileges(Integer loggedInUserId) {
	    XtremandResponse response = new XtremandResponse();
	    
	    Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
	    if (!XamplifyUtils.isValidInteger(loggedInUserId) || !XamplifyUtils.isValidInteger(companyId)) {
	        XamplifyUtils.addErorMessageWithStatusCode(response, INVALID_DATA, 400);
	        return response;
	    }
	    
	    TeamMemberApprovalPrivilegesDTO loggedInUserPrivileges = fetchApprovalPrivileges(loggedInUserId, companyId);
	    response.setData(loggedInUserPrivileges);
	    XamplifyUtils.addSuccessStatus(response);
	    return response;
	}

	private TeamMemberApprovalPrivilegesDTO fetchApprovalPrivileges(Integer userId, Integer companyId) {
	    TeamMemberApprovalPrivilegesDTO privilegesDTO = new TeamMemberApprovalPrivilegesDTO();
	    if (!approveDao.isApprovalPrivilegeManager(userId)) {
	        privilegesDTO = approveDao.getTeamMemberApprovalPrivilegeSettingsByTeamMemberId(userId, companyId);
	    } else {
	        ApprovalSettingsDTO approvalSettingsDTO = userDao.getApprovalConfigurationSettingsByCompanyId(companyId);
	        if (approvalSettingsDTO != null) {
	            privilegesDTO.setAssetApprover(approvalSettingsDTO.isApprovalRequiredForAssets());
	            privilegesDTO.setTrackApprover(approvalSettingsDTO.isApprovalRequiredForTracks());
	            privilegesDTO.setPlaybookApprover(approvalSettingsDTO.isApprovalRequiredForPlaybooks());
	        }
	    }
	    return privilegesDTO;
	}

	/** XNFR-821 **/
	@Override
	public XtremandResponse isApprovalPrivilegeManager(Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
		boolean isApprovalPrivilegeManager = false;
		if (XamplifyUtils.isValidInteger(loggedInUserId)) {
			isApprovalPrivilegeManager = approveDao.isApprovalPrivilegeManager(loggedInUserId);
			response.setData(isApprovalPrivilegeManager);
		}
		return response;
	}

	@Override
	public XtremandResponse sendReminderToApprovers(XtremandResponse response, Integer loggedInUserId, Integer entityId, String moduleType) {
		if(XamplifyUtils.isValidInteger(loggedInUserId) && XamplifyUtils.isValidInteger(entityId) && XamplifyUtils.isValidString(moduleType)) {
			PendingApprovalDamAndLmsDTO pendingApprovalDamAndLmsDTO = new PendingApprovalDamAndLmsDTO();
			Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
			if (ModuleType.DAM.name().equals(moduleType)) {
				pendingApprovalDamAndLmsDTO = approveDao.fetchPendingApprovalAssetDetails(entityId);
				pendingApprovalDamAndLmsDTO.setModuleType(ModuleType.DAM.name());
			} else if (ModuleType.TRACK.name().equals(moduleType)) {
				pendingApprovalDamAndLmsDTO = approveDao.fetchPendingApprovalLMSDetails(entityId,ModuleType.TRACK.name());
				pendingApprovalDamAndLmsDTO.setModuleType(ModuleType.TRACK.name());
			} else if (ModuleType.PLAYBOOK.name().equals(moduleType)) {
				pendingApprovalDamAndLmsDTO = approveDao.fetchPendingApprovalLMSDetails(entityId, ModuleType.PLAYBOOK.name());
				pendingApprovalDamAndLmsDTO.setModuleType(ModuleType.PLAYBOOK.name());
			}
			
			if (loggedInUserId.equals(pendingApprovalDamAndLmsDTO.getCreatedById())	&& companyId.equals(pendingApprovalDamAndLmsDTO.getCreatedByCompanyId())
					&& ApprovalStatusType.CREATED.name().equals(pendingApprovalDamAndLmsDTO.getStatus())) {
				List<Integer> allApproversIds = approveDao.findAllApproversByModuleTypeAndCompanyId(companyId, moduleType);
				if (XamplifyUtils.isNotEmptyList(allApproversIds)) {
					Map<String, Object> approversMap = new HashMap<>();
					approversMap.put("allApproversIds", allApproversIds);
					approversMap.put("pendingApprovalDamAndLmsDTO", pendingApprovalDamAndLmsDTO);
					response.setData(approversMap);
					XamplifyUtils.addSuccessStatus(response);
				} else {
					XamplifyUtils.addErorMessageWithStatusCode(response, "Approvers not found this time. Please try again later.", 400);
				}
			} else {
				XamplifyUtils.addErorMessageWithStatusCode(response, "You are not authorized to send a reminder for this content.", 400);
			}
		} else {
			XamplifyUtils.addErorMessageWithStatusCode(response, INVALID_DATA, 400);
		}
		return response;
	}
	
	@Override
	public XtremandResponse checkIsAssetApprover(Integer loggedInUserId) {
		XtremandResponse response = new XtremandResponse();
	    Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
	    if (!XamplifyUtils.isValidInteger(loggedInUserId) || !XamplifyUtils.isValidInteger(companyId)) {
	        XamplifyUtils.addErorMessageWithStatusCode(response, INVALID_DATA, 400);
	        response.setData(false);
	        return response;
	    }
	    
	    boolean isApprovalPrivilegeManager = approveDao.isApprovalPrivilegeManager(loggedInUserId);
		boolean isAssetApprover = approveDao.checkIsAssetApproverByTeamMemberIdAndCompanyId(loggedInUserId, companyId);
		response.setData(isApprovalPrivilegeManager || isAssetApprover);
	    XamplifyUtils.addSuccessStatus(response);
	    return response;
	}
	
	/** XNFR-813 **/
	@Override
	public XtremandResponse getStatusTileCountsByModuleType(Integer loggedInUserId, String moduleType, boolean showTiles, Integer categoryId) {
		XtremandResponse response = new XtremandResponse();
		if (XamplifyUtils.isValidInteger(loggedInUserId) && XamplifyUtils.isValidString(moduleType)) {
			Integer companyId = userDao.getCompanyIdByUserId(loggedInUserId);
			ApprovalStatisticsDTO approvalStatisticsDTO = approveDao.getApprovalStatusTileCountsByModuleType(companyId,
					moduleType, showTiles, categoryId);
			if (approvalStatisticsDTO != null) {
				XamplifyUtils.addSuccessStatus(response);
				response.setData(approvalStatisticsDTO);
			}
		}
		return response;
	}
	
	/** XNFR-885 **/
	public void generateApprovalStatusHistoryForReApprovalVersion(List<ApprovalStatusHistory> approvalStatusHistoryList,
			String comment, Integer loggedInUserId, Integer damId) {
		ApprovalStatusHistory approvalStatusHistory = new ApprovalStatusHistory();
		setApprovalHistoryValues(ApprovalStatusType.APPROVED.name(), approvalStatusHistory, comment, loggedInUserId);
		approvalStatusHistory.setModuleType(ModuleType.DAM);
		Dam dam = new Dam();
		dam.setId(damId);
		approvalStatusHistory.setDam(dam);
		approvalStatusHistoryList.add(approvalStatusHistory);
	}
	
	/** XNFR-928 **/
	@Override
	public void handleSharedAssetPathForPdfTypeAssets(List<ContentReApprovalDTO> pdfTypeAssetContentDetails) {
		logger.debug("Started updating shared asset path while re-approving Timestamp: {}",new Date());
		if (XamplifyUtils.isNotEmptyList(pdfTypeAssetContentDetails)) {
			for (ContentReApprovalDTO pdfTypeAssetContent: pdfTypeAssetContentDetails) {
				boolean isParentAssetPublished = damDao.isAssetPublished(pdfTypeAssetContent.getApprovalReferenceId());
				if (isParentAssetPublished && !pdfTypeAssetContent.isBeeTemplate() && pdfTypeAssetContent.getAssetType().equalsIgnoreCase("pdf")) {
					DamPartner damPartner = new  DamPartner();
					List<Integer> publishedPartnershipIds = damDao.listPublishedPartnershipIdsByDamId(pdfTypeAssetContent.getApprovalReferenceId());
					iteratePartnershipIdsAndUpdateSharedAssetPath(pdfTypeAssetContent, damPartner, publishedPartnershipIds);
				}
				logger.debug("Finished for Asset: {} Timestamp: {}",pdfTypeAssetContent.getAssetName(), new Date());
			}
		}
		logger.debug("Finished updating shared asset path while re-approving Timestamp: {}",new Date());
	}

	private void iteratePartnershipIdsAndUpdateSharedAssetPath(ContentReApprovalDTO pdfTypeAssetContent, DamPartner damPartner,
			List<Integer> publishedPartnershipIds) {
		if (XamplifyUtils.isNotEmptyList(publishedPartnershipIds)) {
			DamUploadPostDTO damUploadPostDTO = new DamUploadPostDTO();
			for (Integer partnershipId: publishedPartnershipIds) {
				SharedAssetDetailsViewDTO sharedAssetDetailsViewDTO = damDao.getDamPartnerIdAndShareAssetPathByDamIdAndPartnershipId(pdfTypeAssetContent.getApprovalReferenceId(), partnershipId);
				if (sharedAssetDetailsViewDTO != null && XamplifyUtils.isValidInteger(sharedAssetDetailsViewDTO.getId()) && XamplifyUtils.isValidString(sharedAssetDetailsViewDTO.getSharedAssetPath())) {
					xamplifyUtil.shareAssestToPartner(pdfTypeAssetContent.getApprovalReferenceId(), damPartner, damUploadPostDTO, pdfTypeAssetContent.getAssetPath());
					damDao.updateSharedAssetPathAndPartnerSignatureCompletedFalseForPartner(damPartner.getSharedAssetPath(), sharedAssetDetailsViewDTO.getId());
				}
			}
		}
	}

}
