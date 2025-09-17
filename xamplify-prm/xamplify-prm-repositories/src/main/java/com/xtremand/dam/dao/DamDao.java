package com.xtremand.dam.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;

import com.xtremand.approve.dto.ContentReApprovalDTO;
import com.xtremand.category.bom.CategoryModule;
import com.xtremand.common.bom.Pagination;
import com.xtremand.dam.bom.Dam;
import com.xtremand.dam.bom.DamPartner;
import com.xtremand.dam.bom.DamPartnerGroupMapping;
import com.xtremand.dam.bom.DamPartnerMapping;
import com.xtremand.dam.bom.DamTag;
import com.xtremand.dam.dto.ContentPreviewDetailsDTO;
import com.xtremand.dam.dto.DamAnalyticsTilesDTO;
import com.xtremand.dam.dto.DamBasicInfo;
import com.xtremand.dam.dto.DamDTO;
import com.xtremand.dam.dto.DamDownloadDTO;
import com.xtremand.dam.dto.DamDownloadPostDTO;
import com.xtremand.dam.dto.DamListDTO;
import com.xtremand.dam.dto.DamPartnerDetailsDTO;
import com.xtremand.dam.dto.DamPartnerPreviewDTO;
import com.xtremand.dam.dto.DamPatchDTO;
import com.xtremand.dam.dto.DamPostDTO;
import com.xtremand.dam.dto.DamPreviewDTO;
import com.xtremand.dam.dto.DamPublishGetDTO;
import com.xtremand.dam.dto.DamPublishPostDTO;
import com.xtremand.dam.dto.DamUploadPostDTO;
import com.xtremand.dam.dto.PublishedContentIdAndUserListIdDetailsDTO;
import com.xtremand.dam.dto.SharedAssetDetailsViewDTO;
import com.xtremand.dam.dto.WhiteLabeledContentSharedByVendorCompaniesDTO;
import com.xtremand.dao.util.FinderDAO;
import com.xtremand.util.dto.PaginatedDTO;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;
import com.xtremand.video.formbeans.VideoFileUploadForm;
import com.xtremand.white.labeled.dto.DamVideoDTO;

public interface DamDao extends FinderDAO<Dam> {

	public void save(Object clazz);

	Map<String, Object> listAssets(Pagination pagination);

	String getHtmlBodyByAlias(String alias);

	String getHtmlBodyById(Integer id);

	Dam getById(Integer id);

	Dam getAssetDetailsById(Integer id);

	DamPublishGetDTO getPublishedAssetById(Integer id);

	Map<String, Object> listAssetsHistory(Pagination pagination);

	Integer getParentIdByChildParentId(Integer id);

	Integer getMaxVersionByParentId(Integer id);

	Integer getChildTemplatesCountByParentId(Integer id);

	void updateVersion(Integer id, Integer version);

	Integer getIdForUpdatingVersion(Integer id);

	DamDownloadDTO getDownloadContent(String alias, boolean isPartnerDownloading);

	void publish(DamPublishPostDTO damPublishPostDto, Dam dam);

	List<Integer> listPublishedPartnershipIdsByDamId(Integer damId);

	public Map<String, Object> listPublishedAssets(Pagination pagination);

	public void updatePublishedAsset(DamPostDTO damPostDTO);

	public Map<String, Object> listPublishedPartners(Pagination pagination);

	public Map<String, Object> listPartners(Pagination pagination);

	public void updateDownloadOptions(DamDownloadPostDTO damDownloadPostDTO);

	public Dam getDownloadOptionsByAlias(String alias);

	public void updateAssetPathAndThumbnailPath(Integer id, String assetPath, String thumbnailPath);

	public String getAssetPathByAlias(String alias, boolean isPartnerContent);

	public void delete(Integer id);

	public void deletePartner(Integer id);

	SharedAssetDetailsViewDTO getSharedAssetDetailsById(Integer id);

	Integer getDamPartnerIdByAlias(String alias);

	public Map<String, Object> listDamAnalytics(Pagination pagination);

	public DamAnalyticsTilesDTO findPartnerDetailsAndViewAndDownloadsCount(Integer damPartnerId, Integer partnerId);

	public Map<String, Object> listPublishedPartnersAnalytics(Pagination pagination);

	public void update(DamUploadPostDTO damUploadPostDTO);

	public Integer getCompanyIdById(Integer id);

	String getAssetNameByDamPartnerId(Integer damPartnerId);

	DamPreviewDTO previewAssetById(Integer id);

	List<String> getUsersByDamId(Integer damId);

	List<Integer> getUsersByDamAlias(Integer damId, Integer userId);

	List<String> getPartnerEmailIdsByDamPartnerId(Integer damPartnerId);

	List<Integer> findPartnershipIdsForPartnerCompaniesOptionByDamId(Integer damId);

	List<Integer> findPartnershipIdsForPartnerGroupOptionByDamId(Integer damId);

	List<Integer> findPublishedPartnerIdsByDamIdAndPartnershipId(Integer damId, Integer partnershipId);

	List<Integer> findPublishedPartnerIdsByDamId(Integer damId);

	List<Integer> findPublishedPartnerGroupIdsByDamId(Integer damId);

	void findAndDeleteDamPartnerMappingIds(Integer damId, List<Integer> partnerIds);

	void deleteDamPartnerByDamIdAndUnpublishDam(Integer damId);

	void findAndDeleteUnMappedRowsFromDamPartnerOrDamPartnerGroupMapping(boolean isPartnerGroupSelected);

	void insertIntoDamPartnerMapping();

	void updateBeeTemplateImagePath(List<Integer> ids, String path);

	public void findAndDeleteDamPartnerGroupMappingIds(Integer damId, List<Integer> publishedPartnerGroupIds);

	DamPartnerMapping findByPartnerIdAndDamPartnerId(Integer partnerId, Integer damPartnerId);

	boolean checkVendorAccessForDamPartnerAnalytics(Integer damId, Integer partnerId);

	boolean isPublishedToPartnerGroups(Integer damId);

	boolean isPublishedToPartnerGroupsByDamPartnerId(Integer damPartnerId);

	void deleteFromDamPartnerGroupMappingAndDamPartnerByUserListIdAndUserIds(List<Integer> userIds, Integer userListId,
			Integer loggedInUserId);

	void deleteFromDamPartnerGroupMappingAndDamPartnerByUserListId(Integer userListId, Integer loggedInUserId);

	List<DamBasicInfo> findAssociatedDamBasicInfoByUserListId(Integer userListId);

	boolean isPublishedToPartnerByDamIdAndPartnershipId(Integer damId, Integer partnershipId);

	Integer findDamPartnerIdByDamIdAndPartnershipId(Integer damId, Integer partnershipId);

	public boolean isAssetSharedToPartnerCompanyByPartnerId(Integer partnerId);

	public boolean isAssetSharedToPartnerCompanyByPartnerIdAndVendorCompany(Integer partnerId, Integer vendorCompanyId);

	public DamPartner findByPartnershipIdAndDamId(Integer partnershipId, Integer damId);

	public DamPartnerGroupMapping findByUserListIdAndUserIdAndDamPartnerId(Integer userListId, Integer userId,
			Integer damPartnerId);

	public boolean isRowExistsByUserListId(Integer userListId);

	public boolean isAssociatedWithLMS(Integer damId, boolean checkIsPublishedCondition);

	public boolean isDuplicateAssetName(String assetName, Integer companyId);

	public void updateDamProcessingStatus(Integer damId, Integer userId);

	public boolean isDuplicateAssetName(String assetName, Integer companyId, Integer damId);

	public Map<String, Object> listChannelAssets(Pagination pagination, List<Integer> videoIds);

	public List<Integer> getContentAccessCompanyIds();

	public void updateAssetDetails(Integer id, String assetName, Integer updatedBy, Date updatedTime,
			boolean shareAsWhiteLabeledAsset, boolean isAddedToQuickLinks);

	public List<Object[]> getVideoTagsDetails();

	public boolean isTagExists(String tagValue, Integer companyId);

	public List<String> getDamTags(Integer companyId);

	public Object[] getAssetValuesById(Integer id);

	public List<Integer> getUsersByDamId(Integer damId, Integer userId);

	/******** XNFR-169 ******/
	public List<DamPatchDTO> findDamContentForPatch();

	Integer getDamIdByVideoId(Integer videoId);

	Integer getParentTemplateCategoryIdByHistoryTemplateId(Integer historyTemplateId);

	Integer getBeeTemplateCategoryIdByDamId(Integer damId);

	List<String> findFileTypes(Integer companyId, Integer categoryId);

	List<String> findFileTypesForSharedAssetsByPartnerCompanyId(Integer partnerCompanyId, Integer categoryId);

	public List<String> findFileTypesForSharedAssetsByVendorCompanyIdAndPartnerCompanyId(Integer vendorCompanyId,
			Integer partnerCompanyId, Integer categoryId);

	/*********** XNFR-255 **********/
	public DamVideoDTO findDamAndVideoDetailsByVideoId(Integer videoId);

	public DamDTO findCreatedUserIdAndWhiteLabeledOptionsByDamId(Integer damId);

	public void publishAsset(DamUploadPostDTO damUploadPostDTO);

	public void publishVideoAsset(Dam dam, DamUploadPostDTO damUploadPostDTO);

	boolean isAssetPublished(Integer damId);

	public void updatePublishingOrWhiteLabeledStatus(Integer damId);

	public Integer getDamPartnerIdByDamIdAndPartnershipId(Integer damId, Integer partnershipId);

	public void updateWhiteLabelStatus(Integer videoId);

	List<Integer> findPublishedPartnerUserIdsByDamId(Integer damId);

	void changeAsParentAsset(Integer assetId, Integer loggedInUserId, Dam dam);

	/*********** XNFR-255 **********/

	boolean isParentTemplate(Integer damId);

	Map<String, Object> findAllUnPublishedAndFilteredPublishedAssets(Pagination pagination, String searchKey);

	List<PublishedContentIdAndUserListIdDetailsDTO> findAllPublishedAssetsByUserListId(Integer userListId);

	List<Integer> findUnPublishedAndApprovedAssetIdsByCompanyId(Integer companyId);

	/***** XNFR-342 *****/
	List<DamBasicInfo> findAssociatedDamBasicInfoByIds(Set<Integer> damIds);

	void updatePublishedTimeById(Integer damId);

	void updateIsPublishedToPartnerListByIds(Set<Integer> ids, boolean value);

	List<Integer> findPublishedPartnerIdsByUserListIdAndDamId(Integer userListId, Integer damId);

	/***** XNFR-434 *****/
	void updateStatusToProgessByDamId(Integer damId);

	void updateStatusToProgessByVideoId(Integer videoId);

	boolean isAssetWhiteLabeled(Integer damId);

	Dam getByVideoId(Integer videoId);

	boolean isPublished(Integer damId);

	/***** XNFR-434 *****/

	/***** XBI-2133 *****/
	List<WhiteLabeledContentSharedByVendorCompaniesDTO> fetchWhiteLabeledContentSharedByVendorCompanies(
			Integer companyId);

	List<WhiteLabeledContentSharedByVendorCompaniesDTO> findSharedAssetsByCompaniesForPartnerView(
			VanityUrlDetailsDTO vanityUrlDetailsDTO);

	ContentPreviewDetailsDTO findContentDetails(Integer id);

	public Map<String, Object> findAssetAnalytics(Pagination pagination);

	/******* XNFR-543 *****/
	public PaginatedDTO findAllPartnersByPartnerCompanies(Pagination pagination, String search);

	public boolean isVideoFile(Integer damId);

	public PaginatedDTO findAllPartnerCompanyUsersByDamPartnerId(Pagination pagination, String search);

	DamPartnerPreviewDTO findHtmlBodyByDamPartnerId(Integer damPartnerId);

	boolean isIdMatchedByCompanyIdAndId(Integer companyId, Integer id);

	public boolean isIdMatchedByCompanyIdAndDamPartnerId(Integer companyId, Integer damPartnerId);

	public boolean isVideoIdMatchedByCompanyIdAndVideoId(Integer companyId, Integer videoId);

	public boolean isAssetPublishedToPartner(Integer damId, Integer userListId, Integer partnerUserId);

	DamPublishGetDTO findHtmlAndJsonBodyById(Integer id);

	/**** XNFR-748 ****/
	void handleWhiteLabeledAssetSharing(DamUploadPostDTO damUploadPostDTO, Session session, Dam dam,
			Integer partnershipId);

	public List<Integer> findPublishedPartnershipIds(Integer damId);

	/** XNFR-781 **/
	public void setAssetApprovalStatus(Integer loggedInUserId, Integer companyId, Dam dam, boolean isDraft);

	public void updateSharedAssetPathForPartner(String updatedSharedAssetPath, Integer damPartnerId);

	public String fetchAssestPath(Integer damId);

	public void updateAssetPathForVendor(String updatedSharedAssetPath, Integer id);

	public ContentPreviewDetailsDTO findContentDetailsForPartner(Integer id, Integer damPartnerId);

	public String getApprovalStatusById(Integer damId);

	public void updateVideoAssetDetails(VideoFileUploadForm videoFileUploadForm, Date updatedTime, Integer updatedBy,
			boolean updateApprovalStatus, String approvalStatusTypeInString);

	public DamUploadPostDTO getVideoAssetDetailsForReApproval(Integer videoId);

	public Integer getApprovalReferenceIdByDamId(Integer damId);

	public void replaceParentAssetMetadataAfterReApproval(ContentReApprovalDTO contentReApprovalDTO, Integer damId);

	public List<Integer> getTagIdsByDamId(Integer damId);

	public void deleteParentTagsAfterReApprvoal(List<Integer> tagIdsToDelete, Integer damId);

	public DamVideoDTO getReApprovalVersionVideoAssetDetails(Integer videoId);

	public Integer getVideoIdByDamId(Integer damId);

	public void replaceParentVideoFileDetailsWithChildForReApproval(DamVideoDTO damVideoDTO, Integer videoId);

	public boolean hasAnyReApprovalVersionsCreated(List<Integer> damIds);

	public List<ContentReApprovalDTO> getAssetDetailsForReApproval(List<Integer> damIds);

	public List<DamTag> updateTagsAfterReApprovalAndReturnTagsToSave(ContentReApprovalDTO contentReApprovalDTO,
			Integer approvalReferenceId, Integer childDamId, Integer loggedInUserId);

	public void deleteByDamIds(List<Integer> damIds);

	public void replaceImagesAndGifsForReApproval(DamVideoDTO damVideoDTO, Integer videoId);

	public void updateSharedAssetPathForPartnerAndVendorSignatureCompleted(String updatedSharedAssetPath,
			Integer damPartnerId);

	public boolean isVendorSignatureRequiredAfterPartnerSignatureCompleted(Integer damId);

	public void handleWhiteLabeledAssetsAfterReApproval(List<Integer> whiteLabeledParentDamIds, Integer companyId,
			Integer loggedInUserId);

	public boolean isPartnerSignatureRequired(Integer id);

	public DamAnalyticsTilesDTO getPartnerSignatureCount(Integer id);

	public void updateSharedAssetPathAndPartnerSignatureCompletedFalseForPartner(String updatedSharedAssetPath,
			Integer damPartnerId);

	public SharedAssetDetailsViewDTO getDamPartnerIdAndShareAssetPathByDamIdAndPartnershipId(Integer damId,
			Integer partnershipId);

	public void updateAssetPath(Integer damId, String assetPath);

	public Integer findAssetIdByAlias(String alias);

	DamUploadPostDTO findDamByPartnerId(Integer damPartnerId);

	public DamDTO getVendorAndPartnerIdAssets(Integer id);

	public boolean isDamPartnerGroupRowExists(Integer userListId, Integer damPartnerId, Integer userId);

	/** XNFR-973 **/
	void updateOpenAIFileIdByAssetId(Integer assetId, String fileId);

	String fetchOpenAIFileIdByAssetId(Integer assetId);

	Integer findDamIdByDamPartnerId(Integer damPartnerId);

	public boolean isAssetExistBySlugAndCompany(String slug, Integer companyId);

	public Integer getDamIdBySlugAndCopmanyId(String slug, Integer companyId);

	public DamListDTO findAssetDetailsBySlugAndCompanyId(String slug, Integer companyId);

	/** XNFR-981 **/
	void updateRAGITFileIdByAssetId(Integer assetId, String fileId);

	String fetchRagitFileIdByAssetId(Integer id);

	public CategoryModule findCategoryByDamId(Integer damId);

	public List<DamPartnerDetailsDTO> findPublishedGroupAssetsByPartnerId(Integer loggedInUserId);

	DamDTO damDetailsByDamId(Integer damId);

	/******* XNFR-1035 *****/
	public PaginatedDTO findAllPartnersByPartnerCompaniesDetails(Pagination pagination, String search);

	public void unPublishDamAssets();

	public Object trackDetailsByTrackId(Integer learningTrackId);

	public Object playbookDetailsByPlaybookId(Integer learningTrackId);

	public void deleteDamPartnerGroupMappingsByUserListIdsAndUserIds(List<Integer> removeUserIdsList,
			List<Integer> teamMemberParnterListIds);

	public void delateDamPartnersByCompanyId(Integer companyId);

}
