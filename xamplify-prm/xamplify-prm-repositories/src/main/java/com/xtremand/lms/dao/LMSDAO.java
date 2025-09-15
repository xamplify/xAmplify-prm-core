package com.xtremand.lms.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.xtremand.common.bom.Pagination;
import com.xtremand.dam.bom.Dam;
import com.xtremand.dam.dto.DamListDTO;
import com.xtremand.dam.dto.PublishedContentIdAndUserListIdDetailsDTO;
import com.xtremand.form.bom.Form;
import com.xtremand.form.dto.FormDTO;
import com.xtremand.lms.bom.LearningTrack;
import com.xtremand.lms.bom.LearningTrackContent;
import com.xtremand.lms.bom.LearningTrackPartnerActivity;
import com.xtremand.lms.bom.LearningTrackType;
import com.xtremand.lms.bom.LearningTrackVisibility;
import com.xtremand.lms.dto.LearningTrackContentDto;
import com.xtremand.lms.dto.LearningTrackContentResponseDTO;
import com.xtremand.lms.dto.PlaybookAssetResponseDTO;
import com.xtremand.lms.dto.PlaybookContentCategoryListDTO;
import com.xtremand.lms.dto.PreviewPlaybookResponseDTO;

public interface LMSDAO {

	Map<String, Object> getLearningTracksForVendor(Pagination pagination);

	LearningTrack getLearningTrackBySlug(String slug, Integer companyId, LearningTrackType learningTrackType);

	LearningTrack getLearningTrackByTitle(String title, Integer companyId, LearningTrackType learningTrackType);

	Map<String, Object> getLearningTracksForPartner(Pagination pagination);

	Map<String, Object> getAnalytics(Pagination pagination);

	Map<String, Object> getPartnerAnalytics(Pagination pagination);

	List<LearningTrackPartnerActivity> getPartnerActivity(Integer damId, Integer loggedInUserId);

	List<LearningTrackContent> getContentsInOrder(Integer learningTrackId);

	void retainGroups(Set<Integer> newGroupIds, Integer learningTrackId);

	void clearDisplayIndex(Integer learningTrackId);

	List<Integer> getPartnerFinishedContent(Integer learningTrackId, Integer userId);

	List<LearningTrackContent> getLearningTrackContent(Integer learningTrackId);

	Map<String, Object> getPartnerActivities(Pagination pagination);

	LearningTrackVisibility getVisibilityUser(Integer userId, Integer partnershipId, Integer learningTrackId);

	List<Integer> getAllVisibilityUsersIds(Integer partnershipId, Integer learningTrackId);

	List<Integer> getAllPartnershipIds(Integer learningTrackId);

	List<Integer> getAllGroupIds(Integer learningTrackId);

	List<LearningTrack> getLearningTracksByGroupId(Integer userListId);

	LearningTrackVisibility getVisibilityUser(Integer userId, Integer learningTrackId);

	void deleteVisibilityForDeletedUsersFromUserList(Integer userListId, List<Integer> userIds);

	void clearVisbilityOrphans();

	List<Object[]> getLearningTracksForTeamMember(Integer companyId);

	List<Integer> getUserListsForTeamMember(Integer companyId);

	void unPublishLearningTracksWithEmptyVisbility();

	int getTracksCountByFormId(Integer formId);

	public boolean isLMSSharedToPartnerCompanyByPartnerId(Integer partnerId, LearningTrackType lmsType,
			Integer vendorCompanyId);

	/* XNFR-223 */
	public LearningTrackContent getContentByQuizID(Integer learningTrackId, Integer quizId);

	public LearningTrackContent getContentByDamID(Integer learningTrackId, Integer contentId);

	public Integer getRecentlyFinishedQuizId(Integer learningTrackId, Integer userId, Integer partnershipId);

	/*
	 * LearningTrackContent getContent(Integer learningTrackId, Integer contentId);
	 */

	public List<LearningTrack> getAllTracksWithQuiz();

	public List<LearningTrack> getAllTracksWithQuizByTrackID(Integer learningTrackId);

	public Integer getMaxDisplayIndex(Integer learningTrackId);

	public void clearPartnersActivity(Set<Integer> visibilityIds);

	public int getDameCountByLearningId(Integer learningTrackId);

	public void saveAllContent(List<LearningTrackContent> learningTrackContents);

	public void saveAllPartnerActivity(List<LearningTrackPartnerActivity> learningTrackContents);

	/********** XNFR-342 *********/

	List<Integer> findUnPublishedTrackOrPlayBookIdsByCompanyId(Integer companyId, String type);

	List<PublishedContentIdAndUserListIdDetailsDTO> findAllPublishedTracksOrPlayBooksByUserListId(Integer userListId,
			String type);

	Map<String, Object> findAllUnPublishedAndFilteredPublishedTracksOrPlayBooks(Pagination pagination, String search,
			String type);

	List<LearningTrack> findByIds(Set<Integer> ids);

	void updatePublishedStatusByIds(Set<Integer> ids);

	boolean isDataExistsInLearningVisibilityGroupByUserListAndVisibilityId(Integer userListId,
			Integer learningTrackVisibilityId);

	void updateIsPublishedToPartnerListByIds(Set<Integer> ids, boolean value);

	void deleteUnPublishedPartnerCompaniesOrPartnerLists(Set<Integer> userListIds, Set<Integer> trackOrPlayBookIds);

	List<Integer> findPublishedPartnerIdsByUserListIdAndId(Integer userListId, Integer id);

	List<PublishedContentIdAndUserListIdDetailsDTO> findAllPublishedTrackOrPlayBooksByUserListId(Integer userListId);

	List<LearningTrackVisibility> findVisibilityUsersById(Integer learningTrackId);

	/**** XNFR-342 ****/

	List<String> findTrackNamesByAssetId(Integer id);

	List<String> findPlayBookNamesByAssetId(Integer id);

	void updateTrackDescriptionWithReplacedVideoUri(String replacedVideoUri, String previousVideoUri);

	LearningTrack findById(Integer id);

	LearningTrackContentDto getLearningTrackIdByContentId(Integer learningTrackContentId);

	/**** XNFR-523 ****/
	List<Integer> findContentIdsByTrackOrPlayBookId(Integer trackOrPlayBookId);

	List<Integer> findQuizIdsByTrackOrPlayBookId(Integer trackOrPlayBookId);

	List<Integer> findVisibilityUserIdsByTrackOrPlayBookId(Integer trackOrPlayBookId);

	List<Integer> findProgressedVisibilityUserIdsByTrackOrPlayBookId(Integer trackOrPlayBookId);

	List<LearningTrackContentResponseDTO> findLearningTrackContentsByTrackOrPlayBookId(Integer trackOrPlayBookId);

	/** XNFR-745 **/
	PreviewPlaybookResponseDTO getPlaybookBySlug(String type, Integer companyId, String slug);

	List<PlaybookContentCategoryListDTO> getCategoryNamesWithDamIdsForPalybookById(Integer id, String sortKey);

	List<PlaybookAssetResponseDTO> getAssetsWithCategoryIdForPlaybooksByDamId(List<Integer> damIds, String serverPath, Integer playbookId);

	boolean checkGroupByAssetsEnabled(String name, Integer companyId, String slug);

	boolean isRowPresentInLearningTrackVisiblity(Integer learningTrackId, Integer partnershipId);

	void updatePublishedStatusByLearningTrackIdAndPartnershipId(Integer learningTrackId, Integer partnershipId);

	boolean isRowPresentInLearningTrackVisibilityGroup(Integer learningTrackId, Integer partnershipId,
			Integer userListId);

	List<Integer> findVisiblityIds(Integer learningTrackId, Integer partnershipId);

	List<Integer> findPublishedPartnershipIds(Integer learningTrackId);

	List<Integer> findVisibilityIdsByPartnershipIds(List<Integer> partnershipIds, Integer userListId);

	void deleteLearningTrackVisibilityGroups(List<Integer> visibilityIds, Integer userListId);
	
	boolean isPublished(Integer learningTrackId);

	List<LearningTrackContentDto> getTrackContentByLearningTrackId(Integer learningTrackId);

	DamListDTO getAssetDetailsByDamIdAndTrackId(Integer damId, String serverPath, Integer trackId);

	FormDTO findQuizDetailsByQuizId(Integer quizId);

	List<String> getPartnerActivityType(Integer trackContentId);

	public boolean getLearningTrackFormSubmissionByFormID(Integer learningTrackId, Integer loggedInUserId,
			Integer quizId);

	List<Object[]> getVisibilityByLearningTrackId(Integer learningTrackId);

	String getCategoryNameByDamId(Dam dam, Form form);

	// XNFR-1032
	Map<String, Object> getContentCounts(Integer companyId, Integer loggedInUserId, Integer vendorCompanyId,
			boolean hasAccesssToPartner);

	Map<String, Object> getManageContentCounts(Integer companyId, Integer loggedInUserId, String moduleContentType);

	Map<String, Object> getManageSharedContentCounts(Integer companyId, Integer loggedInUserId,
			String moduleContentType, Integer vendorCompanyId);
  
	Map<String, Object> downloadTrackAnalytics(Pagination pagination);
	
	Map<String, Object> downloadDetailedTrackAnalytics(Pagination pagination);

	Map<String, Object> getPartnerTrackAnalyticsForDownload(Pagination pagination);

}
