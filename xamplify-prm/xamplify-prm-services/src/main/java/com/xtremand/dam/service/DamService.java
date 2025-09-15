package com.xtremand.dam.service;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.xtremand.approve.dto.PendingApprovalDamAndLmsDTO;
import com.xtremand.common.bom.Pagination;
import com.xtremand.dam.bom.Dam;
import com.xtremand.dam.dto.AssetPdfPreviewRequestDTO;
import com.xtremand.dam.dto.DamAnalyticsPostDTO;
import com.xtremand.dam.dto.DamAwsDTO;
import com.xtremand.dam.dto.DamDTO;
import com.xtremand.dam.dto.DamDownloadPostDTO;
import com.xtremand.dam.dto.DamPostDTO;
import com.xtremand.dam.dto.DamPublishPostDTO;
import com.xtremand.dam.dto.DamUploadPostDTO;
import com.xtremand.dam.dto.SharedAssetDetailsViewDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.landing.page.analytics.bom.GeoLocationAnalytics;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.dto.ShareContentRequestDTO;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;
import com.xtremand.video.formbeans.VideoFileUploadForm;

public interface DamService {

	XtremandResponse save(DamPostDTO damPostDTO);

	XtremandResponse update(DamUploadPostDTO damUploadPostDTO, MultipartFile thumbnailFile);

	XtremandResponse listAssets(Pagination pagination);

	void downloadAsset(String alias, String pageSize, String pageOrientation) throws IOException;

	void downloadPartnerAsset(String alias, GeoLocationAnalytics geoLocationAnalytics) throws IOException;

	XtremandResponse getById(Integer id, Integer loggedInUserId);

	XtremandResponse getAssetDetailsById(Integer id, Integer userId);

	XtremandResponse getPublishedAssetById(Integer id, Integer loggedInUserId);

	XtremandResponse listAssetsHistory(Pagination pagination);

	XtremandResponse publish(DamPublishPostDTO damPublishPostDTO);

	List<Integer> listPublishedPartneshipIdsByDamId(Integer damId);

	XtremandResponse listPublishedAssets(Pagination pagination);

	XtremandResponse updatePublishedAsset(DamPostDTO damPostDTO);

	XtremandResponse listPartners(Pagination pagination);

	XtremandResponse listPublishedPartners(Pagination pagination);

	XtremandResponse updateDownloadOptions(DamDownloadPostDTO damDownloadPostDTO);

	XtremandResponse getDownloadOptionsByAlias(String alias);

	XtremandResponse upload(MultipartFile uploadedFile, DamUploadPostDTO damUploadPostDTO, MultipartFile thumbnailFile,
			XtremandResponse response);

	String getAssetPathByAlias(GeoLocationAnalytics geoLocationAnalytics, String alias, Integer id,
			boolean isPartnerContent, OAuth2Authentication auth, Integer userId);

	XtremandResponse delete(DamUploadPostDTO dto);

	XtremandResponse getSharedAssetDetailsById(Integer id, Integer loggedInUserId);

	XtremandResponse saveDamAnalytics(DamAnalyticsPostDTO damAnalyticsPostDTO);

	XtremandResponse listDamAnalytics(Pagination pagination);

	XtremandResponse showPartnerDetailsWithAnalyticsCount(Integer damPartnerId, Integer partnerId);

	XtremandResponse listPublishedPartnersAnalytics(Pagination pagination);

	XtremandResponse deletePartner(Integer id);

	void uploadAsset(DamAwsDTO damAwsDTO, DamUploadPostDTO damUploadPostDTO);

	XtremandResponse previewAssetById(Integer id);

	XtremandResponse checkVendorAccessForDamPartnerAnalytics(Integer damId, Integer partnerId);

	XtremandResponse checkDamPartnerId(Integer damPartnerId, Integer loggedInUserId);

	XtremandResponse findPublishedPartnershipIds(Integer damId);

	XtremandResponse findPublishedPartnerIdsByDamId(Integer damId);

	XtremandResponse findPublishedPartnerGroupIdsByDamId(Integer damId);

	void findAndDeleteUnMappedRowsFromDamPartnerOrDamPartnerGroupMapping(boolean isPartnerGroupSelected);

	XtremandResponse insertIntoDamPartnerMapping();

	XtremandResponse isPublishedToPartnerGroups(Integer damId);

	public XtremandResponse insertVideoIdsIntoDam();

	public XtremandResponse uploadContent(MultipartFile uploadedFile, DamUploadPostDTO damUploadPostDTO,
			MultipartFile thumbnailFile);

	public XtremandResponse uploadCloudContent(DamUploadPostDTO damUploadPostDTO, MultipartFile thumbnailFile);

	public XtremandResponse updateVideo(VideoFileUploadForm videoFileUploadForm, Integer loggedInUserId, PendingApprovalDamAndLmsDTO pendingApprovalDamAndLmsDTO);

	public XtremandResponse updateAssetDetailsAndReplaceAsset(DamUploadPostDTO damUploadPostDTO,
			MultipartFile thumbnailFile, MultipartFile uploadedFile, PendingApprovalDamAndLmsDTO pendingApprovalDamAndLmsDTO);

	public DamDTO getDamDTO(Dam dam);

	public XtremandResponse processVideo(DamUploadPostDTO damUploadPostDTO, MultipartFile thumbnailFile, PendingApprovalDamAndLmsDTO pendingApprovalDamAndLmsDTO);

	public XtremandResponse insertTags();

	XtremandResponse findFileTypes(Integer companyId, Integer categoryId);

	XtremandResponse findFileTypesForPartnerView(VanityUrlDetailsDTO vanityUrlDetailsDTO, Integer categoryId);

	void downloadPdf(String size, String orientation, String htmlBody, Integer loggedInUserId, String title)
			throws IOException;

	XtremandResponse changeAsParentAsset(Integer assetId, Integer loggedInUserId);

	XtremandResponse findAllUnPublishedAndFilteredPublishedAssets(Pageable pageable, BindingResult result,
			Integer loggedInUserId, Integer userListId, Integer userListUserId);

	XtremandResponse updateDamStatus(ShareContentRequestDTO shareContentRequestDTO);

	XtremandResponse findPublishedPartnerIds(Integer userListId, Integer damId);

	XtremandResponse updatePDFData(DamPostDTO damPostDTO);

	/**** XBI-2133 ****/
	XtremandResponse fetchWhiteLabeledContentSharedByVendorCompanies(Integer companyId);

	XtremandResponse findSharedAssetsByCompaniesForPartnerView(VanityUrlDetailsDTO vanityUrlDetailsDTO);

	XtremandResponse preview(AssetPdfPreviewRequestDTO assetPdfPreviewRequestDTO, BindingResult result);

	XtremandResponse findContentDetails(Integer id);

	XtremandResponse findAssetAnalytics(Pagination pagination);

	/**** XNFR-543 ***/
	XtremandResponse findAllPartners(Pageable pageable, Integer damId, Integer userId, BindingResult result);

	XtremandResponse findAllPartnerCompanyUsers(Pageable pageable, Integer damPartnerId, boolean isExportToExcel, BindingResult result);

	XtremandResponse findContentDetailsForPartnerView(Integer damPartnerId, Integer loggedInPartnerId);

	XtremandResponse authorizeDamUrlAccess(String subDomain, Integer loggedInUserId, String routerUrl, Integer id);

	XtremandResponse validateDamId(Integer damId, Integer loggedInUserId);

	XtremandResponse validateVideoId(Integer videoId, Integer loggedInUserId);

	/** XNFR-748 **/
	XtremandResponse publishAssetToPartnerCompany(Integer userListId, Integer partnerUserId, Integer damId,
			Integer loggedInUserId);

	XtremandResponse addPartnerGroup(Integer userListId, Integer partnershipId, Integer damId, Integer loggedInUserId);

	public void returnTrackOrPlayBookNamesAssociatedWithDamId(XtremandResponse response, Integer damId,
			boolean isDeleteAction);

	public XtremandResponse uploadPartnerSignature(SharedAssetDetailsViewDTO sharedAssetDetailsViewDTO, MultipartFile uploadedFile);
	
	public MultipartFile convertToHtmlAndPushToAws(String htmlBody);

	public XtremandResponse uploadVendorSignatureForPartner(SharedAssetDetailsViewDTO sharedAssetDetailsViewDTO,
			MultipartFile uploadedFile);

	XtremandResponse getSharedAssetDetailsByIdForVendor(Integer id);

	XtremandResponse getIsVendorSignatureRequiredAfterPartnerSignatureCompleted(Integer id);
	
	/** XNFR-923 **/
	public XtremandResponse getIsPartnerSignatureRequiredAndGetPartnerSignatureCount(Integer id);
	
	public XtremandResponse getPartnersByDamIdAndCompanyIds(List<Integer> companyIds, Integer damId);
	
	/** XNFR-950 **/
	public XtremandResponse getPartnerSignatureCountDetails(Integer id);
	
	/** XNFR-955 **/
	public XtremandResponse validateSlug(String slug, Integer companyId);

	public XtremandResponse getAssetDetailBySlug(String slug, Integer companyId, Integer loggedInUserId);
	
	public XtremandResponse damDetailsByDamId(Integer contentId, String contentType);

	public HttpServletResponse findAllPartnersByDamId(Pageable pageable, Integer damId, Integer userId, HttpServletResponse response);
}
