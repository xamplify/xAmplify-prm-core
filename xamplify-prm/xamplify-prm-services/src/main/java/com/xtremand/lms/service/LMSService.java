package com.xtremand.lms.service;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.xtremand.common.bom.Pagination;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.lms.bom.LearningTrackType;
import com.xtremand.lms.dto.LearningTrackDto;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.dto.ShareContentRequestDTO;

public interface LMSService {

	public XtremandResponse save(MultipartFile featuredImage, LearningTrackDto learningTrackDto);

	public XtremandResponse getLearningTracksForVendor(Pagination pagination);

	public XtremandResponse getPartnerList(Integer loggedInUserId);

	public XtremandResponse validateSlug(LearningTrackDto learningTrackDto);

	public XtremandResponse getLearningTracksForPartner(Pagination pagination);

	public XtremandResponse publishLearningTrack(boolean publish, Integer learningTrackId, Integer loggedInUserId);

	public XtremandResponse getBySlug(LearningTrackType type, Integer companyId, String slug, Integer loggedInUserId);

	public XtremandResponse edit(MultipartFile featuredImage, LearningTrackDto learningTrackDto);

	public XtremandResponse delete(LearningTrackDto learningTrackDto);

	public XtremandResponse updatePartnerProgress(LearningTrackDto learningTrackDto);

	public XtremandResponse getAnalytics(Pagination pagination);

	public XtremandResponse getPartnerAnalytics(Pagination pagination);

	public XtremandResponse getById(Integer learningTrackId, Integer loggedInUserId);

	public XtremandResponse getPartnerActivities(Pagination pagination);

	public XtremandResponse downloadPDF(Integer learningTrackId, Integer contentId, Integer userId) throws IOException;

	public XtremandResponse getPartnerList(Pagination pagination);

	public XtremandResponse saveAsPlaybook(LearningTrackDto learningTrackDto);

	public XtremandResponse validateTitle(LearningTrackDto learningTrackDto);

	public XtremandResponse getPartnerQuizAnalytics(LearningTrackDto learningTrackDto);

	public XtremandResponse addExistingQuiz();

	/******** XNFR-342 ****/
	public XtremandResponse findAllUnPublishedTracksOrPlayBooks(Pageable pageable, BindingResult result,
			Integer loggedInUserId, Integer userListId, Integer userListUserId, String type);

	public XtremandResponse shareSelectedTracksOrPlayBooks(ShareContentRequestDTO shareContentRequestDTO, String type);

	public XtremandResponse findPublishedPartnerIdsByUserListIdAndId(Integer userListId, Integer id);

	public XtremandResponse updateLmsPublishedValues();

	/******** XNFR-342 ****/

	public XtremandResponse getGroupedAssetsBySlug(LearningTrackType playbook, Integer companyId, String slug,
			String sortKey);

	public XtremandResponse checkGroupByAssetsEnabled(String type, Integer companyId, String slug);

	/******** XNFR-750 ****/
	public XtremandResponse publishTrackOrPlayBookToPartnerCompany(Integer userListId, Integer partnerUserId,
			Integer trackOrPlayBookId, Integer loggedInUserId);

	public XtremandResponse addPartnerGroup(Integer userListId, Integer partnershipId, Integer trackOrPlayBookId,
			Integer loggedInUserId);
	
	public XtremandResponse getPreviewBySlug(LearningTrackType type, Integer companyId, String slug, Integer loggedInUserId);

	//XNFR-1032
	public XtremandResponse getContentCounts(Integer loggedInUserId, String vendorCompanyProfileName);

	public XtremandResponse getManageContentCounts(Integer loggedInUserId, String moduleContentType);
	
	public HttpServletResponse downloadTrackAnalytics(Pageable pageable,Integer userId, Integer learningTrackId,String lmsType,HttpServletResponse response);

	public XtremandResponse getManageSharedContentCounts(Integer loggedInUserId, String moduleContentType, String vendorCompanyProfileName);

	public HttpServletResponse downloadPartnerTrackAnalytics(Pageable pageable,Integer userId, Integer learningTrackId, Integer partnerCompanyId, String lmsType,HttpServletResponse response);

	public XtremandResponse checkAWSCredentials();
	
}
