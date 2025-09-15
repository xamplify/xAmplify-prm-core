package com.xtremand.comments.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.xtremand.approve.dao.ApproveDAO;
import com.xtremand.approve.dto.ContentReApprovalDTO;
import com.xtremand.category.bom.CategoryModuleEnum;
import com.xtremand.category.dao.CategoryDao;
import com.xtremand.comments.dao.CommentDao;
import com.xtremand.comments.dto.CommentRequestDTO;
import com.xtremand.comments.dto.CommentResponseDTO;
import com.xtremand.comments.service.CommentService;
import com.xtremand.dam.bom.ApprovalStatusHistory;
import com.xtremand.dam.bom.ApprovalStatusType;
import com.xtremand.dam.bom.Dam;
import com.xtremand.dam.bom.DamPartner;
import com.xtremand.dam.bom.DamTag;
import com.xtremand.dam.dao.DamDao;
import com.xtremand.dam.dto.ApprovalStatusHistoryDTO;
import com.xtremand.dam.dto.DamUploadPostDTO;
import com.xtremand.dam.dto.SharedAssetDetailsViewDTO;
import com.xtremand.dam.service.DamService;
import com.xtremand.dao.util.GenericDAO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.lms.bom.LearningTrack;
import com.xtremand.lms.dao.LMSDAO;
import com.xtremand.user.bom.User;
import com.xtremand.user.dao.UserDAO;
import com.xtremand.util.XamplifyUtil;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.bom.ModuleType;
import com.xtremand.util.dao.XamplifyUtilDao;
import com.xtremand.video.bom.VideoTag;
import com.xtremand.video.dao.VideoDao;
import com.xtremand.white.labeled.dto.DamVideoDTO;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

	@Autowired
	private CommentDao commentDao;

	@Autowired
	private GenericDAO genericDao;

	@Autowired
	private XamplifyUtil xamplifyUtil;

	@Autowired
	private DamDao damDao;

	@Autowired
	private VideoDao videoDAO;

	@Autowired
	private DamService damService;

	@Autowired
	private LMSDAO lmsDao;

	@Autowired
	private XamplifyUtilDao xamplifyUtilDao;

	@Autowired
	private CategoryDao categoryDao;

	@Autowired
	private ApproveDAO approveDao;

	@Autowired
	private UserDAO userDao;

	@Value("${asset.rejection.associated_with_campaigns.message}")
	private String assetRejectionAssociatedWithCampaignsMessage;

	@Override
	public XtremandResponse findCommentsByModuleNameAndId(Integer id, String moduleName) {
		XtremandResponse response = new XtremandResponse();
		List<CommentResponseDTO> comments = commentDao.findCommentsByModuleNameAndId(id, moduleName);
		for (CommentResponseDTO comment : comments) {
			String commentedUserProfilePicture = comment.getCommentedUserProfilePicture();
			String updatedProfilePicture = xamplifyUtil.getProfilePicturePrefixPath(commentedUserProfilePicture);
			comment.setCommentedUserProfilePicture(updatedProfilePicture + commentedUserProfilePicture);
		}
		response.setData(comments);
		XamplifyUtils.addSuccessStatusWithMessage(response, "");
		return response;
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public XtremandResponse save(CommentRequestDTO commentRequestDTO) {
		return new XtremandResponse();
	}



	/** XNFR-824 start **/
	@Override
	public XtremandResponse loadUserDetailsWithApprovalStatus(Integer entityId, String moduleType) {
		XtremandResponse response = new XtremandResponse();
		if (XamplifyUtils.isValidInteger(entityId) && XamplifyUtils.isValidString(moduleType)) {
			response.setData(commentDao.loadUserDetailsWithApprovalStatus(entityId, moduleType));
			XamplifyUtils.addSuccessStatusWithMessage(response, "");
		}
		return response;
	}

	@Override
	public XtremandResponse loadCommentsAndTimelineHistory(Integer entityId, String moduleType) {
		XtremandResponse response = new XtremandResponse();
		if (XamplifyUtils.isValidInteger(entityId) && XamplifyUtils.isValidString(moduleType)) {
			response.setData(commentDao.loadCommentsAndTimelineHistory(entityId, moduleType));
			XamplifyUtils.addSuccessStatusWithMessage(response, "");
		}
		return response;
	}

	@Override
	public XtremandResponse updateApprovalStatusAndSaveComment(ApprovalStatusHistoryDTO approvalStatusHistoryDTO) {
		XtremandResponse response = new XtremandResponse();

		if (!isValidInputParametersForUpdatingTheAssetStatus(approvalStatusHistoryDTO)) {
			XamplifyUtils.addErorMessageWithStatusCode(response, "Invalid input parameters", 400);
			return response;
		}

		Integer approvalReferenceId = getApprovalReferenceId(approvalStatusHistoryDTO);
		boolean isReApprovalVersionAsset = XamplifyUtils.isValidInteger(approvalReferenceId);

		validateAssetRejection(approvalStatusHistoryDTO, response);
		if (response.getStatusCode() == 401 || response.getStatusCode() == 403) {
			return response;
		}

		if (validateTrackOrPlaybookRejection(approvalStatusHistoryDTO)) {
			String message = ModuleType.TRACK.name().equals(approvalStatusHistoryDTO.getModuleType())
					? "Published tracks cannot be rejected."
					: "Published playbooks cannot be rejected.";
			response.setMessage(message);
			response.setStatusCode(403);
			return response;
		}
		/** XNFR-885 **/
		List<VideoTag> videoTags = new ArrayList<>();
		List<DamTag> damTagsToSave = new ArrayList<>();
		if (isReApprovalVersionAsset
				&& ApprovalStatusType.APPROVED.name().equals(approvalStatusHistoryDTO.getStatusInString())) {
			List<Integer> entityIds = new ArrayList<>();
			entityIds.add(approvalStatusHistoryDTO.getEntityId());
			List<ContentReApprovalDTO> contentReApprovalDTOs = damDao.getAssetDetailsForReApproval(entityIds);
			ContentReApprovalDTO contentReApprovalDTO = contentReApprovalDTOs.get(0);
			contentReApprovalDTO.setLoggedInUserId(approvalStatusHistoryDTO.getLoggedInUserId());
			Integer videoId = contentReApprovalDTO.getVideoId();
			approvalStatusHistoryDTO
					.setCompanyId(userDao.getCompanyIdByUserId(approvalStatusHistoryDTO.getLoggedInUserId()));
			checkAndSetWhiteLabeleDamId(approvalStatusHistoryDTO, approvalReferenceId, contentReApprovalDTO);
			commentDao.replaceReApprovalVersionAssetCommentsToParentAsset(approvalStatusHistoryDTO.getEntityId(),
					approvalReferenceId);
			if (XamplifyUtils.isValidInteger(videoId)) {
				processVideoReApproval(approvalReferenceId, videoTags, contentReApprovalDTO, videoId);
			} else {
				damTagsToSave = processNonVideoReApproval(approvalStatusHistoryDTO, approvalReferenceId,
						contentReApprovalDTO);
			}

			approvalStatusHistoryDTO.setEntityId(approvalReferenceId);
			createAndSaveApprovalStatusHistory(approvalStatusHistoryDTO);
			iterateAndUpdateSharedAssetPath(approvalReferenceId, contentReApprovalDTO);
		} else {
			ApprovalStatusHistory approvalStatusHistory = createAndSaveApprovalStatusHistory(approvalStatusHistoryDTO);
			if (approvalStatusHistoryDTO.isStatusUpdated()) {
				updateApprovalStatusByModuleType(approvalStatusHistory.getModuleType().name(), approvalStatusHistoryDTO,
						response);
			}
		}
		saveTags(videoTags, damTagsToSave);
		XamplifyUtils.addSuccessStatus(response);
		return response;
	}

	private ApprovalStatusHistory createAndSaveApprovalStatusHistory(
			ApprovalStatusHistoryDTO approvalStatusHistoryDTO) {
		ApprovalStatusHistory approvalStatusHistory = new ApprovalStatusHistory();
		approvalStatusHistory.setComment(approvalStatusHistoryDTO.getComment());
		approvalStatusHistory.setCreatedTime(new Date());
		User user = new User();
		user.setUserId(approvalStatusHistoryDTO.getLoggedInUserId());
		approvalStatusHistory.setCreatedBy(user);
		checkAndUpdateTheAssetApprovalStatus(approvalStatusHistoryDTO, approvalStatusHistory);
		setApprovalStatusHistoryModuleType(approvalStatusHistoryDTO, approvalStatusHistory);
		setApprovalStatusHistoryEntityId(approvalStatusHistoryDTO, approvalStatusHistory);
		genericDao.save(approvalStatusHistory);
		return approvalStatusHistory;
	}

	private void checkAndSetWhiteLabeleDamId(ApprovalStatusHistoryDTO approvalStatusHistoryDTO,
			Integer approvalReferenceId, ContentReApprovalDTO contentReApprovalDTO) {
		if (contentReApprovalDTO.isWhiteLabeledAssetSharedWithPartners()) {
			List<Integer> approvalReferenceIds = new ArrayList<>();
			approvalReferenceIds.add(approvalReferenceId);
			approvalStatusHistoryDTO.setWhiteLabeledParentDamIds(approvalReferenceIds);
		}
	}

	private List<DamTag> processNonVideoReApproval(ApprovalStatusHistoryDTO approvalStatusHistoryDTO,
			Integer approvalReferenceId, ContentReApprovalDTO contentReApprovalDTO) {
		List<DamTag> damTagsToSave;
		damDao.replaceParentAssetMetadataAfterReApproval(contentReApprovalDTO, approvalReferenceId);
		updateCategory(contentReApprovalDTO.getCategoryId(), approvalReferenceId,
				approvalStatusHistoryDTO.getLoggedInUserId());
		damTagsToSave = damDao.updateTagsAfterReApprovalAndReturnTagsToSave(contentReApprovalDTO, approvalReferenceId,
				approvalStatusHistoryDTO.getEntityId(), approvalStatusHistoryDTO.getLoggedInUserId());
		damDao.delete(approvalStatusHistoryDTO.getEntityId());
		return damTagsToSave;
	}

	private void processVideoReApproval(Integer approvalReferenceId, List<VideoTag> videoTags,
			ContentReApprovalDTO contentReApprovalDTO, Integer videoId) {
		DamVideoDTO videoFileDetails = damDao.getReApprovalVersionVideoAssetDetails(videoId);
		Integer parentVideoId = damDao.getVideoIdByDamId(approvalReferenceId);

		List<String> childVideoTags = XamplifyUtils.isValidString(videoFileDetails.getTagsInString())
				? XamplifyUtils.convertStringToArrayList(videoFileDetails.getTagsInString())
				: new ArrayList<>();

		List<String> parentVideoTags = videoDAO.getTagNamesByVideoId(parentVideoId);
		List<String> tags = new ArrayList<>();
		List<String> tagToRemove = new ArrayList<>();

		for (String parentTag : parentVideoTags) {
			tags.add(parentTag);
			if (childVideoTags == null || childVideoTags.isEmpty() || !childVideoTags.contains(parentTag)) {
				tagToRemove.add(parentTag);
			}
		}
		populateVideoTags(childVideoTags, parentVideoId, videoTags, tags);
		updateCategory(contentReApprovalDTO.getCategoryId(), approvalReferenceId,
				contentReApprovalDTO.getLoggedInUserId());
		damDao.replaceParentAssetMetadataAfterReApproval(contentReApprovalDTO, approvalReferenceId);
		damDao.replaceParentVideoFileDetailsWithChildForReApproval(videoFileDetails, parentVideoId);
		damDao.replaceImagesAndGifsForReApproval(videoFileDetails, parentVideoId);
		if (XamplifyUtils.isNotEmptyList(tagToRemove)) {
			approveDao.deleteVideoTagsAfterReApprovalByNames(tagToRemove, parentVideoId);
		}
		videoDAO.deleteByPrimaryKey(videoId);
	}

	private void populateVideoTags(List<String> childVideoTags, Integer parentVideoId, List<VideoTag> videoTags,
			List<String> tags) {
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

	private Integer getApprovalReferenceId(ApprovalStatusHistoryDTO approvalStatusHistoryDTO) {
		return ModuleType.DAM.name().equals(approvalStatusHistoryDTO.getModuleType())
				? damDao.getApprovalReferenceIdByDamId(approvalStatusHistoryDTO.getEntityId())
				: null;
	}

	private void updateCategory(Integer categoryId, Integer approvalReferenceId, Integer userId) {
		if (XamplifyUtils.isValidInteger(categoryId) && XamplifyUtils.isValidInteger(approvalReferenceId)
				&& XamplifyUtils.isValidInteger(userId)) {
			categoryDao.updateCategoryIdByType(approvalReferenceId, categoryId, userId, CategoryModuleEnum.DAM.name());
		}
	}

	private void saveTags(List<VideoTag> videoTags, List<DamTag> damTagsToSave) {
		if (XamplifyUtils.isNotEmptyList(videoTags)) {
			xamplifyUtilDao.saveAll(videoTags, "Video tags for Re-Approval");
		}
		if (XamplifyUtils.isNotEmptyList(damTagsToSave)) {
			xamplifyUtilDao.saveAll(damTagsToSave, "Dam Tags for Re-Approval");
		}
	}

	private void setApprovalStatusHistoryEntityId(ApprovalStatusHistoryDTO approvalStatusHistoryDTO,
			ApprovalStatusHistory approvalStatusHistory) {
		String moduleType = approvalStatusHistoryDTO.getModuleType();
		Integer entityId = approvalStatusHistoryDTO.getEntityId();

		if (ModuleType.DAM.name().equals(moduleType) && XamplifyUtils.isValidInteger(entityId)) {
			Dam dam = new Dam();
			dam.setId(entityId);
			approvalStatusHistory.setDam(dam);
		} else if ((ModuleType.TRACK.name().equals(moduleType) || ModuleType.PLAYBOOK.name().equals(moduleType))
				&& XamplifyUtils.isValidInteger(entityId)) {
			LearningTrack learningTrack = new LearningTrack();
			learningTrack.setId(entityId);
			approvalStatusHistory.setLearningTrack(learningTrack);
		}
	}

	public void setApprovalStatusHistoryModuleType(ApprovalStatusHistoryDTO approvalStatusHistoryDTO,
			ApprovalStatusHistory approvalStatusHistory) {
		String moduleType = approvalStatusHistoryDTO.getModuleType();

		if (ModuleType.DAM.name().equals(moduleType)) {
			approvalStatusHistory.setModuleType(ModuleType.DAM);
		} else if (ModuleType.TRACK.name().equals(moduleType)) {
			approvalStatusHistory.setModuleType(ModuleType.TRACK);
		} else if (ModuleType.PLAYBOOK.name().equals(moduleType)) {
			approvalStatusHistory.setModuleType(ModuleType.PLAYBOOK);
		}
	}

	private XtremandResponse validateAssetRejection(ApprovalStatusHistoryDTO approvalStatusHistoryDTO,
			XtremandResponse response) {
		if (ApprovalStatusType.REJECTED.name().equals(approvalStatusHistoryDTO.getStatusInString())
				&& ModuleType.DAM.name().equals(approvalStatusHistoryDTO.getModuleType())
				&& XamplifyUtils.isValidInteger(approvalStatusHistoryDTO.getEntityId())) {
			boolean isAssetPublished = damDao.isPublished(approvalStatusHistoryDTO.getEntityId());
			List<Integer> damIds = new ArrayList<>();
			damIds.add(approvalStatusHistoryDTO.getEntityId());
			boolean hasAnyReApprovalVersionsCreated = damDao.hasAnyReApprovalVersionsCreated(damIds);
			if (isAssetPublished) {
				response.setStatusCode(403);
				response.setMessage("Published assets cannot be rejected.");
			} else if (hasAnyReApprovalVersionsCreated) {
				response.setStatusCode(403);
				response.setMessage("Rejecting this asset is not allowed as re-approval version is already created");
			} else {
				handleLMSAssociationCheckForAssetRejection(approvalStatusHistoryDTO, response);
			}
		}
		return response;
	}

	private void handleLMSAssociationCheckForAssetRejection(ApprovalStatusHistoryDTO approvalStatusHistoryDTO,
			XtremandResponse response) {
		if (damDao.isAssociatedWithLMS(approvalStatusHistoryDTO.getEntityId(), false)) {
			response.setData(401);
			damService.returnTrackOrPlayBookNamesAssociatedWithDamId(response, approvalStatusHistoryDTO.getEntityId(),
					false);
		}
	}

	public boolean validateTrackOrPlaybookRejection(ApprovalStatusHistoryDTO approvalStatusHistoryDTO) {
		return ApprovalStatusType.REJECTED.name().equals(approvalStatusHistoryDTO.getStatusInString())
				&& (ModuleType.TRACK.name().equals(approvalStatusHistoryDTO.getModuleType())
						|| ModuleType.PLAYBOOK.name().equals(approvalStatusHistoryDTO.getModuleType()))
				&& XamplifyUtils.isValidInteger(approvalStatusHistoryDTO.getEntityId())
				&& lmsDao.isPublished(approvalStatusHistoryDTO.getEntityId());
	}

	private void updateApprovalStatusByModuleType(String moduleType, ApprovalStatusHistoryDTO approvalStatusHistoryDTO,
			XtremandResponse response) {
		Integer updatedCount = null;
		Integer entityId = approvalStatusHistoryDTO.getEntityId();
		if (XamplifyUtils.isValidInteger(entityId)) {
			updatedCount = commentDao.updateApprovalStatusByEntityIdAndModuleType(moduleType, entityId,
					approvalStatusHistoryDTO.getLoggedInUserId(), approvalStatusHistoryDTO.getStatusInString());
		}

		if (XamplifyUtils.isValidInteger(updatedCount)) {
			approvalStatusHistoryDTO.setStatusUpdated(true);
			response.setData(true);
		}
	}

	private void checkAndUpdateTheAssetApprovalStatus(ApprovalStatusHistoryDTO approvalStatusHistoryDTO,
			ApprovalStatusHistory approvalStatusHistory) {
		if (approvalStatusHistoryDTO.isStatusUpdated()
				&& XamplifyUtils.isValidString(approvalStatusHistoryDTO.getStatusInString())) {
			if (ApprovalStatusType.APPROVED.name().equals(approvalStatusHistoryDTO.getStatusInString())) {
				approvalStatusHistory.setStatus(ApprovalStatusType.APPROVED);
			} else if (ApprovalStatusType.REJECTED.name().equals(approvalStatusHistoryDTO.getStatusInString())) {
				approvalStatusHistory.setStatus(ApprovalStatusType.REJECTED);
			}
		} else {
			approvalStatusHistory.setStatus(ApprovalStatusType.COMMENTED);
		}
	}

	private boolean isValidInputParametersForUpdatingTheAssetStatus(ApprovalStatusHistoryDTO approvalStatusHistoryDTO) {
		return approvalStatusHistoryDTO != null && XamplifyUtils.isValidInteger(approvalStatusHistoryDTO.getEntityId())
				&& XamplifyUtils.isValidInteger(approvalStatusHistoryDTO.getLoggedInUserId())
				&& XamplifyUtils.isValidString(approvalStatusHistoryDTO.getStatusInString())
				&& XamplifyUtils.isValidString(approvalStatusHistoryDTO.getComment())
				&& XamplifyUtils.isValidString(approvalStatusHistoryDTO.getModuleType());
	}

	/** XNFR-824 end **/

	/** XNFR-928 **/
	private void iterateAndUpdateSharedAssetPath(Integer approvalReferenceId,
			ContentReApprovalDTO contentReApprovalDTO) {
		boolean isParentAssetPublished = damDao.isAssetPublished(approvalReferenceId);
		if (isParentAssetPublished && !contentReApprovalDTO.isBeeTemplate()
				&& contentReApprovalDTO.getAssetType().equalsIgnoreCase("pdf")) {
			DamPartner damPartner = new DamPartner();
			List<Integer> publishedPartnershipIds = damDao.listPublishedPartnershipIdsByDamId(approvalReferenceId);
			if (XamplifyUtils.isNotEmptyList(publishedPartnershipIds)) {
				DamUploadPostDTO damUploadPostDTO = new DamUploadPostDTO();
				for (Integer partnershipId : publishedPartnershipIds) {
					SharedAssetDetailsViewDTO sharedAssetDetailsViewDTO = damDao
							.getDamPartnerIdAndShareAssetPathByDamIdAndPartnershipId(approvalReferenceId,
									partnershipId);
					if (sharedAssetDetailsViewDTO != null
							&& XamplifyUtils.isValidInteger(sharedAssetDetailsViewDTO.getId())
							&& XamplifyUtils.isValidString(sharedAssetDetailsViewDTO.getSharedAssetPath())) {
						xamplifyUtil.shareAssestToPartner(approvalReferenceId, damPartner, damUploadPostDTO,
								contentReApprovalDTO.getAssetPath());
						damDao.updateSharedAssetPathAndPartnerSignatureCompletedFalseForPartner(
								damPartner.getSharedAssetPath(), sharedAssetDetailsViewDTO.getId());
					}
				}
			}
		}
	}

}
