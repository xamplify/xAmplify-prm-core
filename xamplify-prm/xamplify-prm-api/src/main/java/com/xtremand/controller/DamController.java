package com.xtremand.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.html2pdf.HtmlConverter;
import com.xtremand.approve.dto.PendingApprovalDamAndLmsDTO;
import com.xtremand.common.bom.Pagination;
import com.xtremand.dam.dto.AssetPdfPreviewRequestDTO;
import com.xtremand.dam.dto.DamAnalyticsPostDTO;
import com.xtremand.dam.dto.DamAwsDTO;
import com.xtremand.dam.dto.DamDownloadPostDTO;
import com.xtremand.dam.dto.DamPostDTO;
import com.xtremand.dam.dto.DamUploadPostDTO;
import com.xtremand.dam.dto.SharedAssetDetailsViewDTO;
import com.xtremand.dam.exception.DamDataAccessException;
import com.xtremand.dam.service.DamService;
import com.xtremand.exception.DuplicateEntryException;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.landing.page.analytics.bom.GeoLocationAnalytics;
import com.xtremand.mail.service.AsyncComponent;
import com.xtremand.util.BadRequestException;
import com.xtremand.util.FileUtil;
import com.xtremand.util.XamplifyUtil;
import com.xtremand.util.dto.Pageable;
import com.xtremand.util.dto.ShareContentRequestDTO;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;
import com.xtremand.video.formbeans.VideoFileUploadForm;

@RestController
@RequestMapping(value = "/dam/")
public class DamController {

	private static final String DAM_ASSET_NAME_UNIQUE_INDEX = "dam_asset_name_unique_index";

	private static final String DUPLICATENAME = "Name Already Exists";

	private static final String FOLDER_CANNOT_CHANGED_FOR_HISTORY_TEMPLATES_MESSAGE = "Folder cannot be changed for history templates";

	@Autowired
	private DamService damService;

	/*** XNFR-930 ***/
	@Autowired
	private XamplifyUtil xamplifyUtil;
	/*** XNFR-930 ***/

	@Autowired
	private FileUtil fileUtil;

	@Value("${image.format.types}")
	String imageFormats;

	@Autowired
	private AsyncComponent asyncComponent;

	@PostMapping(value = "save")
	public ResponseEntity<XtremandResponse> save(@RequestBody DamPostDTO damPostDTO) {
		boolean constraint = false;
		XtremandResponse response = new XtremandResponse();
		try {
			response = damService.save(damPostDTO);
			return ResponseEntity.ok(response);
		} catch (BadRequestException e) {
			constraint = true;
			throw new BadRequestException(e.getMessage());
		} catch (DataIntegrityViolationException e) {
			constraint = true;
			if (e.getMessage().indexOf(DAM_ASSET_NAME_UNIQUE_INDEX) > -1) {
				throw new DuplicateEntryException(DUPLICATENAME);
			} else {
				throw new DamDataAccessException(e);
			}
		} catch (Exception e) {
			constraint = true;
			throw new DamDataAccessException(e);
		} finally {
			if (!constraint && response.getStatusCode() == 200) {
				asyncComponent.generateThumbnailAndPublishToPartners(damPostDTO);

			}
		}
	}

	@PostMapping(value = "update", consumes = { "multipart/form-data" })
	public ResponseEntity<XtremandResponse> update(
			@RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImageFile,
			@RequestPart(value = "uploadedFile", required = false) MultipartFile uploadedFile,
			@RequestPart("damUploadPostDTO") DamUploadPostDTO damUploadPostDTO) {
		XtremandResponse response = new XtremandResponse();
		boolean constraint = false;
		PendingApprovalDamAndLmsDTO pendingApprovalDamAndLmsDTO = new PendingApprovalDamAndLmsDTO();
		try {
			response = damService.updateAssetDetailsAndReplaceAsset(damUploadPostDTO, thumbnailImageFile, uploadedFile,
					pendingApprovalDamAndLmsDTO);
			return ResponseEntity.ok(response);
		} catch (BadRequestException e) {
			constraint = true;
			throw new BadRequestException(e.getMessage());
		} catch (DataIntegrityViolationException | DuplicateEntryException e) {
			constraint = true;
			if (e.getMessage().indexOf(DAM_ASSET_NAME_UNIQUE_INDEX) > -1) {
				throw new DuplicateEntryException(DUPLICATENAME);
			} else {
				throw new DamDataAccessException(e);
			}
		} catch (Exception e) {
			constraint = true;
			throw new DamDataAccessException(e);
		} finally {
			setDamPropertiesAndPublishAsset(damUploadPostDTO, response, constraint);
			if (!constraint && response.getStatusCode() == 200 && !damUploadPostDTO.isDraft()
					&& (damUploadPostDTO.isSendForApproval() || damUploadPostDTO.isSendForReApproval())) {
				pendingApprovalDamAndLmsDTO.setSendForApproval(damUploadPostDTO.isSendForApproval());
				pendingApprovalDamAndLmsDTO.setSendForReApproval(damUploadPostDTO.isSendForReApproval());
				pendingApprovalDamAndLmsDTO.setStatus(damUploadPostDTO.getApprovalStatusInString());
				asyncComponent.sendNotificationForSendForApprovalAndReApprovalAssets(pendingApprovalDamAndLmsDTO);
			}
		}
	}

	private void setDamPropertiesAndPublishAsset(DamUploadPostDTO damUploadPostDTO, XtremandResponse response,
			boolean constraint) {
		if (!constraint) {
			Map<String, Object> map = response.getMap();
			if (map != null && response.getStatusCode() == 200) {
				getUpdatedPartnerListIdsOrPartnershipIds(damUploadPostDTO, map);
				/*** XNFR-434 ****/
				if (damUploadPostDTO.isReplaceAsset()) {
					DamAwsDTO damAwsDTO = (DamAwsDTO) response.getMap().get("dto");
					asyncComponent.replaceAndPublishAsset(damAwsDTO, damUploadPostDTO);
				} else {
					/***** XNFR-949 *****/
					asyncComponent.uploadDesignedPdfToAws(damUploadPostDTO.getId());
				}
			}
		}
	}

	private void getUpdatedPartnerListIdsOrPartnershipIds(DamUploadPostDTO damUploadPostDTO, Map<String, Object> map) {
		boolean publishAsset = (boolean) map.get("publishAsset");
		boolean shareAsWhiteLabeledAsset = (boolean) map.get("shareAsWhiteLabeledAsset");
		if (publishAsset || shareAsWhiteLabeledAsset) {
			damUploadPostDTO.setDamId(damUploadPostDTO.getId());
			damUploadPostDTO.setAssetSharedEmailNotification(publishAsset);
			if (map.containsKey("publishingToPartnersInsidePartnerList")) {
				boolean publishingToPartnersInsidePartnerList = (boolean) map
						.get("publishingToPartnersInsidePartnerList");
				damUploadPostDTO.setPublishingToPartnersInsidePartnerList(publishingToPartnersInsidePartnerList);
			}
			/***** XBI-1829 ****/
			setPublishedPartnerUserIds(damUploadPostDTO, map, publishAsset);
			if (!damUploadPostDTO.isReplaceAsset()) {
				asyncComponent.publishAsset(damUploadPostDTO);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void setPublishedPartnerUserIds(DamUploadPostDTO damUploadPostDTO, Map<String, Object> map,
			boolean publishAsset) {
		if (publishAsset) {
			List<Integer> publishedPartnerUserIds = (List<Integer>) map.get("publishedPartnerUserIds");
			damUploadPostDTO.setPublishedPartnerUserIds(publishedPartnerUserIds);
		}
	}

	@PostMapping(value = "updatePublishedAsset")
	public ResponseEntity<XtremandResponse> updatePublishedAsset(@RequestBody DamPostDTO damPostDTO) {
		try {
			return ResponseEntity.ok(damService.updatePublishedAsset(damPostDTO));
		} catch (DataIntegrityViolationException e) {
			if (e.getMessage().indexOf(DAM_ASSET_NAME_UNIQUE_INDEX) > -1) {
				throw new DuplicateEntryException(DUPLICATENAME);
			} else {
				throw new DamDataAccessException(e);
			}
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}
	}

	@GetMapping(value = "getById/{id}/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> getById(@PathVariable Integer id, @PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(damService.getById(id, loggedInUserId));
	}

	@GetMapping(value = "getAssetDetailsById/{id}/{userId}")
	public ResponseEntity<XtremandResponse> getAssetDetailsById(@PathVariable Integer id,
			@PathVariable Integer userId) {
		return ResponseEntity.ok(damService.getAssetDetailsById(id, userId));
	}

	@GetMapping(value = "previewAssetById/{id}")
	public ResponseEntity<XtremandResponse> previewAssetById(@PathVariable Integer id) {
		return ResponseEntity.ok(damService.previewAssetById(id));
	}

	/***** XNFR-501 ****/
	@GetMapping(value = "preview")
	public ResponseEntity<XtremandResponse> preview(AssetPdfPreviewRequestDTO assetPdfPreviewRequestDTO,
			BindingResult result) {
		return ResponseEntity.ok(damService.preview(assetPdfPreviewRequestDTO, result));
	}

	@GetMapping(value = "getPublishedAssetById/{id}/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> getPublishedAssetById(@PathVariable Integer id,
			@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(damService.getPublishedAssetById(id, loggedInUserId));
	}

	@PostMapping(value = "list")
	public ResponseEntity<XtremandResponse> listAssets(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(damService.listAssets(pagination));
	}

	@PostMapping(value = "listPublishedAssets")
	public ResponseEntity<XtremandResponse> listPublishedAssets(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(damService.listPublishedAssets(pagination));
	}

	@PostMapping(value = "listHistory")
	public ResponseEntity<XtremandResponse> listHistory(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(damService.listAssetsHistory(pagination));
	}

	@PostMapping(value = "listPartners")
	public ResponseEntity<XtremandResponse> listPartners(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(damService.listPartners(pagination));
	}

	@PostMapping(value = "listPublishedPartners")
	public ResponseEntity<XtremandResponse> listPublishedPartners(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(damService.listPublishedPartners(pagination));
	}

	@GetMapping(value = "/download/{alias}/{size}/{orientation}")
	public void downloadAssetPdf(@PathVariable String alias, @PathVariable String size,
			@PathVariable String orientation) throws IOException {
		damService.downloadAsset(alias, size, orientation);
	}

	@PostMapping(value = "/downloadp")
	public void downloadPublishedAssetPdf(@RequestParam String alias, @RequestParam Integer loggedInUserId,
			@RequestParam String deviceType, @RequestParam String os, @RequestParam String city,
			@RequestParam String country, @RequestParam String isp, @RequestParam String ipAddress,
			@RequestParam String state, @RequestParam String zip, @RequestParam String latitude,
			@RequestParam String longitude, @RequestParam String countryCode, @RequestParam String timezone)
			throws IOException {
		GeoLocationAnalytics geoLocationAnalytics = setFirstFourParams(loggedInUserId, deviceType, os, city);
		setMiddleParams(country, isp, ipAddress, state, geoLocationAnalytics);
		setLastParams(zip, latitude, longitude, countryCode, timezone, geoLocationAnalytics);
		damService.downloadPartnerAsset(alias, geoLocationAnalytics);
	}

	@PostMapping(value = "downloadpc")
	public ResponseEntity<Void> getPartnerContentAndRedirect(@RequestParam String alias, @RequestParam Integer id,
			@RequestParam Integer loggedInUserId, @RequestParam String deviceType, @RequestParam String os,
			@RequestParam String city, @RequestParam String country, @RequestParam String isp,
			@RequestParam String ipAddress, @RequestParam String state, @RequestParam String zip,
			@RequestParam String latitude, @RequestParam String longitude, @RequestParam String countryCode,
			@RequestParam String timezone, OAuth2Authentication auth) {
		GeoLocationAnalytics geoLocationAnalytics = setFirstFourParams(loggedInUserId, deviceType, os, city);
		setMiddleParams(country, isp, ipAddress, state, geoLocationAnalytics);
		setLastParams(zip, latitude, longitude, countryCode, timezone, geoLocationAnalytics);
		String url = damService.getAssetPathByAlias(geoLocationAnalytics, alias, id, true, auth, loggedInUserId);
		/*** XNFR-930 ***/
		String downloadUrl = xamplifyUtil.replaceS3WithCloudfrontViceVersa(url);
		/*** XNFR-930 ***/
		return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(downloadUrl)).build();
	}

	private GeoLocationAnalytics setFirstFourParams(Integer loggedInUserId, String deviceType, String os, String city) {
		GeoLocationAnalytics geoLocationAnalytics = new GeoLocationAnalytics();
		firstMethod(loggedInUserId, deviceType, os, city, geoLocationAnalytics);
		return geoLocationAnalytics;
	}

	private void firstMethod(Integer loggedInUserId, String deviceType, String os, String city,
			GeoLocationAnalytics geoLocationAnalytics) {
		geoLocationAnalytics.setUserId(loggedInUserId);
		geoLocationAnalytics.setDeviceType(deviceType);
		geoLocationAnalytics.setOs(os);
		geoLocationAnalytics.setCity(city);
	}

	private void setMiddleParams(String country, String isp, String ipAddress, String state,
			GeoLocationAnalytics geoLocationAnalytics) {
		geoLocationAnalytics.setCountry(country);
		geoLocationAnalytics.setIsp(isp);
		geoLocationAnalytics.setIpAddress(ipAddress);
		geoLocationAnalytics.setState(state);
	}

	private void setLastParams(String zip, String latitude, String longitude, String countryCode, String timezone,
			GeoLocationAnalytics geoLocationAnalytics) {
		geoLocationAnalytics.setZip(zip);
		geoLocationAnalytics.setLatitude(latitude);
		geoLocationAnalytics.setLongitude(longitude);
		geoLocationAnalytics.setCountryCode(countryCode);
		geoLocationAnalytics.setTimezone(timezone);
	}

	@GetMapping(value = "downloadc/{alias}/{id}/{userId}")
	public ResponseEntity<Void> getContentAndRedirect(@PathVariable String alias, @PathVariable Integer id,
			OAuth2Authentication auth, @PathVariable Integer userId) {
		String url = damService.getAssetPathByAlias(null, alias, id, false, auth, userId);
		/*** XNFR-930 ***/
		String downloadUrl = xamplifyUtil.replaceS3WithCloudfrontViceVersa(url);
		/*** XNFR-930 ***/
		return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(downloadUrl)).build();
	}

	@GetMapping(value = "listPublishedPartneshipIdsByDamId/{damId}")
	public ResponseEntity<List<Integer>> listPublishedPartneshipIdsByDamId(@PathVariable Integer damId) {
		return ResponseEntity.ok(damService.listPublishedPartneshipIdsByDamId(damId));
	}

	@PostMapping(value = "updateDownloadOptions")
	public ResponseEntity<XtremandResponse> updateDownloadOptions(@RequestBody DamDownloadPostDTO damDownloadPostDTO) {
		return ResponseEntity.ok(damService.updateDownloadOptions(damDownloadPostDTO));
	}

	@GetMapping(value = "getDownloadOptions/{alias}")
	public ResponseEntity<XtremandResponse> getDownloadOptions(@PathVariable String alias) {
		return ResponseEntity.ok(damService.getDownloadOptionsByAlias(alias));
	}

	@PostMapping(value = "delete")
	public ResponseEntity<XtremandResponse> delete(@RequestBody DamUploadPostDTO dto) {
		return ResponseEntity.ok(damService.delete(dto));
	}

	@GetMapping(value = "deletePartner/{id}")
	public ResponseEntity<XtremandResponse> deletePartner(@PathVariable Integer id) {
		return ResponseEntity.ok(damService.deletePartner(id));
	}

	@GetMapping(value = "getSharedAssetDetailsById/{id}/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> getSharedAssetDetailsById(@PathVariable Integer id,
			@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(damService.getSharedAssetDetailsById(id, loggedInUserId));
	}

	@PostMapping(value = "saveDamAnalytics")
	public ResponseEntity<XtremandResponse> saveDamAnalytics(@RequestBody DamAnalyticsPostDTO damAnalyticsPostDTO) {
		return ResponseEntity.ok(damService.saveDamAnalytics(damAnalyticsPostDTO));
	}

	@PostMapping(value = "listDamAnalytics")
	public ResponseEntity<XtremandResponse> listDamAnalytics(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(damService.listDamAnalytics(pagination));
	}

	@GetMapping(value = "showPartnerDetailsWithAnalyticsCount/{damPartnerId}/{partnerId}")
	public ResponseEntity<XtremandResponse> showPartnerDetailsWithAnalyticsCount(@PathVariable Integer damPartnerId,
			@PathVariable Integer partnerId) {
		return ResponseEntity.ok(damService.showPartnerDetailsWithAnalyticsCount(damPartnerId, partnerId));
	}

	@PostMapping(value = "listPublishedPartnersAnalytics")
	public ResponseEntity<XtremandResponse> listPublishedPartnersAnalytics(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(damService.listPublishedPartnersAnalytics(pagination));
	}

	@GetMapping(value = "checkDamAndPartnerId/{damId}/{partnerId}")
	public ResponseEntity<XtremandResponse> checkVendorAccessForDamPartnerAnalytics(@PathVariable Integer damId,
			@PathVariable Integer partnerId) {
		return ResponseEntity.ok(damService.checkVendorAccessForDamPartnerAnalytics(damId, partnerId));
	}

	@GetMapping(value = "checkDamPartnerId/{damPartnerId}/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> checkDamPartnerId(@PathVariable Integer damPartnerId,
			@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(damService.checkDamPartnerId(damPartnerId, loggedInUserId));
	}

	@GetMapping(value = "findPublishedPartnershipIds/{damId}")
	public ResponseEntity<XtremandResponse> findPublishedPartnershipIds(@PathVariable Integer damId) {
		return ResponseEntity.ok(damService.findPublishedPartnershipIds(damId));
	}

	@GetMapping(value = "findPublishedPartnerIds/{damId}")
	public ResponseEntity<XtremandResponse> findPublishedPartnerIdsByDamId(@PathVariable Integer damId) {
		return ResponseEntity.ok(damService.findPublishedPartnerIdsByDamId(damId));
	}

	@GetMapping(value = "findPublishedPartnerGroupIds/{damId}")
	public ResponseEntity<XtremandResponse> findPublishedPartnerGroupIdsByDamId(@PathVariable Integer damId) {
		return ResponseEntity.ok(damService.findPublishedPartnerGroupIdsByDamId(damId));
	}

	@GetMapping(value = "insertIntoDamPartnerMapping")
	public ResponseEntity<XtremandResponse> insertIntoDamPartnerMapping() {
		return ResponseEntity.ok(damService.insertIntoDamPartnerMapping());
	}

	@GetMapping(value = "isPublishedToPartnerGroups/{damId}")
	public ResponseEntity<XtremandResponse> isPublishedToPartnerGroups(@PathVariable Integer damId) {
		return ResponseEntity.ok(damService.isPublishedToPartnerGroups(damId));
	}

	@GetMapping(value = "insertVideoIdsIntoDam")
	public ResponseEntity<XtremandResponse> insertVideoIdsIntoDam() {
		return ResponseEntity.ok(damService.insertVideoIdsIntoDam());
	}

	@PostMapping(value = "upload-content", consumes = { "multipart/form-data" })
	@ResponseBody
	public ResponseEntity<XtremandResponse> uploadContent(
			@RequestPart(value = "uploadedFile") MultipartFile uploadedFile,
			@RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImageFile,
			@RequestPart("damUploadPostDTO") DamUploadPostDTO damUploadPostDTO) {
		boolean constraint = false;
		XtremandResponse response = new XtremandResponse();
		boolean isVideoFile = fileUtil.isVideoFile(uploadedFile.getOriginalFilename());
		try {
			response = damService.uploadContent(uploadedFile, damUploadPostDTO, thumbnailImageFile);
			return ResponseEntity.ok(response);
		} catch (DataIntegrityViolationException e) {
			constraint = e.getMessage().indexOf(DAM_ASSET_NAME_UNIQUE_INDEX) > -1;
			if (constraint) {
				throw new DuplicateEntryException(DUPLICATENAME);
			} else {
				throw new DamDataAccessException(e);
			}
		} catch (BadRequestException e) {
			constraint = e.getMessage().indexOf(FOLDER_CANNOT_CHANGED_FOR_HISTORY_TEMPLATES_MESSAGE) > -1;
			if (constraint) {
				throw new BadRequestException(FOLDER_CANNOT_CHANGED_FOR_HISTORY_TEMPLATES_MESSAGE);
			} else {
				throw new DamDataAccessException(e);
			}
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		} finally {
			if (response.getStatusCode() != 401 && !constraint && !isVideoFile && response.getMap() != null) {
				DamAwsDTO damAwsDTO = (DamAwsDTO) response.getMap().get("dto");
				damService.uploadAsset(damAwsDTO, damUploadPostDTO);
			}

			if (response.getStatusCode() != 401 && !constraint && !isVideoFile && response.getMap() != null
					&& !damUploadPostDTO.isDraft()
					&& (damUploadPostDTO.isSendForApproval() || damUploadPostDTO.isSendForReApproval())) {
				Map<String, Object> map = new HashMap<>();
				map = response.getMap();
				PendingApprovalDamAndLmsDTO pendingApprovalDamAndLmsDTO = (PendingApprovalDamAndLmsDTO) map
						.get("pendingApprovalDamAndLmsDTO");
				if (pendingApprovalDamAndLmsDTO != null) {
					pendingApprovalDamAndLmsDTO.setSendForApproval(damUploadPostDTO.isSendForApproval());
					pendingApprovalDamAndLmsDTO.setSendForReApproval(damUploadPostDTO.isSendForReApproval());
					asyncComponent.sendNotificationForSendForApprovalAndReApprovalAssets(pendingApprovalDamAndLmsDTO);
				}
			}
		}
	}

	@PostMapping(value = "upload-cloud-content", consumes = { "multipart/form-data" })
	@ResponseBody
	public ResponseEntity<XtremandResponse> uploadCloudContent(
			@RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImageFile,
			@RequestPart("damUploadPostDTO") DamUploadPostDTO damUploadPostDTO) {
		boolean constraint = false;
		XtremandResponse response = new XtremandResponse();
		try {
			response = damService.uploadCloudContent(damUploadPostDTO, thumbnailImageFile);
			return ResponseEntity.ok(response);
		} catch (DataIntegrityViolationException e) {
			constraint = e.getMessage().indexOf(DAM_ASSET_NAME_UNIQUE_INDEX) > -1;
			if (constraint) {
				throw new DuplicateEntryException(DUPLICATENAME);
			} else {
				throw new DamDataAccessException(e);
			}
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		} finally {
			if (response.getStatusCode() != 401 && response.getMap() != null) {
				boolean isVideoFile = (boolean) response.getMap().get("isVideoFile");
				if (!constraint && !isVideoFile) {
					DamAwsDTO damAwsDTO = (DamAwsDTO) response.getMap().get("dto");
					damService.uploadAsset(damAwsDTO, damUploadPostDTO);
				}
			}
		}
	}

	@PostMapping(value = "/update-video")
	public ResponseEntity<XtremandResponse> updateVideo(@RequestBody VideoFileUploadForm videoFileUploadForm,
			@RequestParam Integer userId) {
		XtremandResponse response = new XtremandResponse();
		boolean constraint = false;
		PendingApprovalDamAndLmsDTO pendingApprovalDamAndLmsDTO = new PendingApprovalDamAndLmsDTO();
		try {
			response = damService.updateVideo(videoFileUploadForm, userId, pendingApprovalDamAndLmsDTO);
			return ResponseEntity.ok(response);
		} catch (BadRequestException e) {
			constraint = true;
			throw new BadRequestException(e.getMessage());
		} catch (DataIntegrityViolationException | DuplicateEntryException e) {
			constraint = true;
			if (e.getMessage().indexOf(DAM_ASSET_NAME_UNIQUE_INDEX) > -1) {
				throw new DuplicateEntryException(DUPLICATENAME);
			} else {
				throw new DamDataAccessException(e);
			}
		} catch (Exception e) {
			constraint = true;
			throw new DamDataAccessException(e);
		} finally {
			setDamPropertiesAndPublishVideoAsset(videoFileUploadForm, userId, response, constraint);

			if (!constraint && response.getStatusCode() == 200 && videoFileUploadForm.isSendForApproval()
					&& !videoFileUploadForm.isDraft()) {
				pendingApprovalDamAndLmsDTO.setSendForApproval(videoFileUploadForm.isSendForApproval());
				asyncComponent.sendNotificationForSendForApprovalAndReApprovalAssets(pendingApprovalDamAndLmsDTO);
			}
		}

	}

	private void setDamPropertiesAndPublishVideoAsset(VideoFileUploadForm videoFileUploadForm, Integer userId,
			XtremandResponse response, boolean constraint) {
		if (!constraint) {
			Map<String, Object> map = response.getMap();
			if (map != null && response.getStatusCode() != 401) {
				boolean publishAsset = (boolean) map.get("publishAsset");
				boolean shareAsWhiteLabeledAsset = (boolean) map.get("shareAsWhiteLabeledAsset");
				if (publishAsset || shareAsWhiteLabeledAsset) {
					DamUploadPostDTO damUploadPostDTO = videoFileUploadForm.getDamUploadPostDTO();
					damUploadPostDTO.setDamId(videoFileUploadForm.getDamId());
					damUploadPostDTO.setAssetSharedEmailNotification(publishAsset);
					damUploadPostDTO.setVideoId(videoFileUploadForm.getId());
					damUploadPostDTO.setLoggedInUserId(userId);
					setPublishedPartnerUserIds(damUploadPostDTO, map, publishAsset);
					/***** XBI-1829 ****/
					asyncComponent.publishVideoAsset(damUploadPostDTO);
				}
			}
		}
	}

	@PostMapping(value = "process-video", consumes = { "multipart/form-data" })
	@ResponseBody
	public ResponseEntity<XtremandResponse> processVideo(
			@RequestPart("damUploadPostDTO") DamUploadPostDTO damUploadPostDTO,
			@RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImageFile,
			@RequestParam String path) {
		PendingApprovalDamAndLmsDTO pendingApprovalDamAndLmsDTO = new PendingApprovalDamAndLmsDTO();
		boolean constraint = false;
		try {
			damUploadPostDTO.setAssetPath(path);
			return ResponseEntity
					.ok(damService.processVideo(damUploadPostDTO, thumbnailImageFile, pendingApprovalDamAndLmsDTO));
		} catch (Exception e) {
			constraint = true;
			throw new DamDataAccessException(e);
		} finally {
			if (!constraint && !damUploadPostDTO.isDraft()
					&& (damUploadPostDTO.isSendForApproval() || damUploadPostDTO.isSendForReApproval())) {
				pendingApprovalDamAndLmsDTO.setSendForApproval(damUploadPostDTO.isSendForApproval());
				pendingApprovalDamAndLmsDTO.setSendForReApproval(damUploadPostDTO.isSendForReApproval());
				asyncComponent.sendNotificationForSendForApprovalAndReApprovalAssets(pendingApprovalDamAndLmsDTO);
			}
		}
	}

	/* This method needs to be removed */
	@GetMapping(value = "insertTags")
	public ResponseEntity<XtremandResponse> insertTags() {
		return ResponseEntity.ok(damService.insertTags());
	}

	@GetMapping(value = "/findFileTypes/{companyId}/{categoryId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> findFileTypes(@PathVariable Integer companyId,
			@PathVariable Integer categoryId) {
		return ResponseEntity.ok(damService.findFileTypes(companyId, categoryId));
	}

	@PostMapping(value = "/findFileTypesForPartnerView/{categoryId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> findFileTypesForPartnerView(
			@RequestBody VanityUrlDetailsDTO vanityUrlDetailsDTO, @PathVariable Integer categoryId) {
		return ResponseEntity.ok(damService.findFileTypesForPartnerView(vanityUrlDetailsDTO, categoryId));
	}

	/**** XNFR-379 ****/
	@PostMapping(value = "/downloadPdfPreview")
	public void downloadPublishedAssetPdf(@RequestParam String size, @RequestParam String orientation,
			@RequestParam String htmlBody, @RequestParam Integer loggedInUserId, @RequestParam String title)
			throws IOException {
		damService.downloadPdf(size, orientation, htmlBody, loggedInUserId, title);
	}

	/**** XNFR-381 ****/
	@PutMapping(value = "changeAsParentAsset/{assetId}/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> changeAsParentAsset(@PathVariable Integer assetId,
			@PathVariable Integer loggedInUserId) {
		try {
			return ResponseEntity.ok(damService.changeAsParentAsset(assetId, loggedInUserId));
		} catch (BadRequestException e) {
			throw new BadRequestException(e.getMessage());
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}

	}

	/**** XNFR-342 ****/
	@GetMapping("/findAllUnPublishedAndFilteredPublishedAssets/{loggedInUserId}/{userListId}/{userListUserId}")
	public ResponseEntity<XtremandResponse> findAllUnPublishedAndFilteredPublishedAssets(
			@PathVariable Integer loggedInUserId, @PathVariable Integer userListId,
			@PathVariable Integer userListUserId, @Valid Pageable pageable, BindingResult result) {
		return new ResponseEntity<>(damService.findAllUnPublishedAndFilteredPublishedAssets(pageable, result,
				loggedInUserId, userListId, userListUserId), HttpStatus.OK);
	}

	/**** XNFR-342 ****/
	@PutMapping(value = "/shareSelectedAssets")
	public ResponseEntity<XtremandResponse> shareSelectedAssets(
			@RequestBody ShareContentRequestDTO shareContentRequestDTO) {
		boolean hasError = false;
		try {
			hasError = false;
			return ResponseEntity.ok(damService.updateDamStatus(shareContentRequestDTO));
		} catch (DamDataAccessException e) {
			hasError = true;
			throw new DamDataAccessException(e);
		} catch (Exception ex) {
			hasError = true;
			throw new DamDataAccessException(ex);
		} finally {
			if (!hasError) {
				asyncComponent.publishDamToNewlyAddedPartners(shareContentRequestDTO, true);
			}

		}

	}

	@GetMapping("/findPublishedPartnerIds/{userListId}/{damId}")
	public ResponseEntity<XtremandResponse> findPublishedPartnerIds(@PathVariable Integer userListId,
			@PathVariable Integer damId) {
		return new ResponseEntity<>(damService.findPublishedPartnerIds(userListId, damId), HttpStatus.OK);
	}

	/**** XNFR-381 ****/
	@PutMapping(value = "updatePDFData")
	public ResponseEntity<XtremandResponse> updatePDFData(@RequestBody DamPostDTO damPostDTO) {
		try {
			return ResponseEntity.ok(damService.updatePDFData(damPostDTO));
		} catch (BadRequestException e) {
			throw new BadRequestException(e.getMessage());
		} catch (Exception e) {
			throw new DamDataAccessException(e);
		}

	}

	/***** XBI-2133 ****/
	@GetMapping(value = "/sharedByVendorCompany/{companyId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> fetchWhiteLabeledContentSharedByVendorCompanies(
			@PathVariable Integer companyId) {
		return ResponseEntity.ok(damService.fetchWhiteLabeledContentSharedByVendorCompanies(companyId));
	}

	@PostMapping(value = "/findSharedAssetsByCompaniesForPartnerView/")
	@ResponseBody
	public ResponseEntity<XtremandResponse> findSharedAssetsByCompaniesForPartnerView(
			@RequestBody VanityUrlDetailsDTO vanityUrlDetailsDTO) {
		return ResponseEntity.ok(damService.findSharedAssetsByCompaniesForPartnerView(vanityUrlDetailsDTO));
	}

	@GetMapping(value = "findContentDetails/{id}")
	public ResponseEntity<XtremandResponse> findContentDetails(@PathVariable Integer id) {
		return ResponseEntity.ok(damService.findContentDetails(id));
	}

	@GetMapping(value = "findContentDetailsForPartnerView/{damPartnerId}/{loggedInPartnerId}")
	public ResponseEntity<XtremandResponse> findContentDetailsForPartnerView(@PathVariable Integer damPartnerId,
			@PathVariable Integer loggedInPartnerId) {
		return ResponseEntity.ok(damService.findContentDetailsForPartnerView(damPartnerId, loggedInPartnerId));
	}

	/**** XNFR-543 ****/
	@GetMapping("findAllPartners/damId/{damId}/userId/{userId}")
	public ResponseEntity<XtremandResponse> findAllPartners(@PathVariable Integer damId, @PathVariable Integer userId,
			@Valid Pageable pageable, BindingResult result) {
		return new ResponseEntity<>(damService.findAllPartners(pageable, damId, userId, result), HttpStatus.OK);
	}

	/** XNFR-1035 **/
	@GetMapping("downloadDamAnalytics/damId/{damId}/userId/{userId}")
	public void findAllPartnersByDamId(@PathVariable Integer damId, @PathVariable Integer userId,
			@Valid Pageable pageable, HttpServletResponse response) {
		damService.findAllPartnersByDamId(pageable, damId, userId, response);
	}

	/**** XNFR-543 ****/
	@GetMapping("findAllPartnerCompanyUsers/id/{id}")
	public ResponseEntity<XtremandResponse> findAllPartnerCompanyUsers(@PathVariable Integer id,
			@RequestParam boolean isExportToExcel, @Valid Pageable pageable, BindingResult result) {
		return new ResponseEntity<>(damService.findAllPartnerCompanyUsers(pageable, id, isExportToExcel, result),
				HttpStatus.OK);
	}

	/**** XNFR-694 ****/
	@GetMapping("authorizeDamUrlAccess/users/{userId}/routerUrls/{routerUrl}")
	public ResponseEntity<XtremandResponse> authorizeDamUrlAccess(@PathVariable("userId") Integer userId,
			@PathVariable("routerUrl") String routerUrl,
			@RequestParam(value = "subDomain", required = false) String subDomain,
			@RequestParam(value = "id", required = false) Integer id) {
		return new ResponseEntity<>(damService.authorizeDamUrlAccess(subDomain, userId, routerUrl, id), HttpStatus.OK);
	}

	@GetMapping("validateDamId/damId/{damId}/loggedInUserId/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> validateDamId(@PathVariable Integer damId,
			@PathVariable Integer loggedInUserId) {
		return new ResponseEntity<>(damService.validateDamId(damId, loggedInUserId), HttpStatus.OK);
	}

	/** XNFR-694 **/
	@GetMapping("validateVideoId/videoId/{videoId}/loggedInUserId/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> validateVideoId(@PathVariable Integer videoId,
			@PathVariable Integer loggedInUserId) {
		return new ResponseEntity<>(damService.validateVideoId(videoId, loggedInUserId), HttpStatus.OK);
	}

	/** XNFR-748 **/
	@GetMapping("publish/userListId/{userListId}/partnerUserId/{partnerUserId}/id/{damId}/loggedInUserId/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> publishAssetToPartnerCompany(@PathVariable Integer userListId,
			@PathVariable Integer partnerUserId, @PathVariable Integer damId, @PathVariable Integer loggedInUserId) {
		return new ResponseEntity<>(
				damService.publishAssetToPartnerCompany(userListId, partnerUserId, damId, loggedInUserId),
				HttpStatus.OK);
	}

	@GetMapping("addPartnerGroup/userListId/{userListId}/partnershipId/{partnershipId}/id/{damId}/loggedInUserId/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> addPartnerGroup(@PathVariable Integer userListId,
			@PathVariable Integer partnershipId, @PathVariable Integer damId, @PathVariable Integer loggedInUserId) {
		return new ResponseEntity<>(damService.addPartnerGroup(userListId, partnershipId, damId, loggedInUserId),
				HttpStatus.OK);
	}

	/** XNFR-833 **/
	@PostMapping(value = "/p/upload/signature")
	@ResponseBody
	public ResponseEntity<XtremandResponse> uploadSignature(
			@RequestPart(value = "uploadedFile", required = false) MultipartFile uploadedFile,
			@RequestPart("assetDetailsViewDto") SharedAssetDetailsViewDTO sharedAssetDetailsViewDTO) {
		return ResponseEntity.ok(damService.uploadPartnerSignature(sharedAssetDetailsViewDTO, uploadedFile));
	}

	@PostMapping(value = "generatePdf", produces = MediaType.APPLICATION_PDF_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<byte[]> generatePdf(@RequestBody Map<String, String> requestBody) {
		String htmlBody = requestBody.get("htmlBody");

		if (htmlBody == null || htmlBody.trim().isEmpty()) {
			return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
					.body("{\"error\": \"HTML body cannot be null or empty\"}".getBytes(StandardCharsets.UTF_8));
		}

		try {
			MultipartFile pdfFile = damService.convertToHtmlAndPushToAws(htmlBody);

			if (pdfFile == null || pdfFile.getSize() == 0) {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON)
						.body("{\"error\": \"Failed to generate PDF\"}".getBytes(StandardCharsets.UTF_8));
			}

			byte[] pdfBytes = pdfFile.getBytes();

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_PDF);
			headers.setContentDispositionFormData("attachment", "output.pdf");
			headers.setContentLength(pdfBytes.length);

			return ResponseEntity.ok().headers(headers).body(pdfBytes);
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(
					"{\"error\": \"An error occurred while generating the PDF\"}".getBytes(StandardCharsets.UTF_8));
		}
	}

	@PostMapping(value = "/generate-pdf", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_PDF_VALUE)
	public void generatePdf(@RequestBody String html, HttpServletResponse response) throws IOException {

		html = html.replace("VENDOR_COMPANY_WEBSITE_URL", "javascript:void(0)");

		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=generated-pdf.pdf");

		try (OutputStream outputStream = response.getOutputStream()) {
			HtmlConverter.convertToPdf(html, outputStream);
		}
	}

	@PostMapping(value = "/v/upload/signature")
	@ResponseBody
	public ResponseEntity<XtremandResponse> uploadVendorSignatureForPartner(
			@RequestPart(value = "uploadedFile", required = false) MultipartFile uploadedFile,
			@RequestPart("assetDetailsViewDto") SharedAssetDetailsViewDTO sharedAssetDetailsViewDTO) {
		return ResponseEntity.ok(damService.uploadVendorSignatureForPartner(sharedAssetDetailsViewDTO, uploadedFile));
	}

	@GetMapping(value = "getSharedAssetDetailsById/v/{id}")
	public ResponseEntity<XtremandResponse> getSharedAssetDetailsByIdForVendor(@PathVariable Integer id) {
		return ResponseEntity.ok(damService.getSharedAssetDetailsByIdForVendor(id));
	}

	@GetMapping(value = "getIsVendorSignatureRequiredAfterPartnerSignatureCompleted/{id}")
	public ResponseEntity<XtremandResponse> getIsVendorSignatureRequiredAfterPartnerSignatureCompleted(
			@PathVariable Integer id) {
		return ResponseEntity.ok(damService.getIsVendorSignatureRequiredAfterPartnerSignatureCompleted(id));
	}

	@GetMapping(value = "getIsPartnerSignatureRequiredAndGetPartnerSignatureCount/{id}")
	public ResponseEntity<XtremandResponse> getIsPartnerSignatureRequiredAndGetPartnerSignatureCount(
			@PathVariable Integer id) {
		return ResponseEntity.ok(damService.getIsPartnerSignatureRequiredAndGetPartnerSignatureCount(id));
	}

	@PostMapping("getPartnersByDamIdAndCompanyIds")
	public XtremandResponse getPartnersByDamIdAndCompanyIds(@RequestBody Pagination pagination) {
		return damService.getPartnersByDamIdAndCompanyIds(pagination.getSelectedPartnerCompanyIds(),
				pagination.getDamId());
	}

	@GetMapping(value = "getPartnerSignatureCountDetails/{id}")
	public ResponseEntity<XtremandResponse> getPartnerSignatureCountDetails(@PathVariable Integer id) {
		return ResponseEntity.ok(damService.getPartnerSignatureCountDetails(id));
	}

	/** XNFR-955 **/
	@GetMapping(value = "validateSlug/{companyId}")
	public ResponseEntity<XtremandResponse> validateSlug(DamUploadPostDTO damUploadPostDTO,
			@PathVariable Integer companyId) {
		return ResponseEntity.ok(damService.validateSlug(damUploadPostDTO.getSlug(), companyId));
	}

	@GetMapping(value = "getAssetDetailBySlug/{companyId}")
	public ResponseEntity<XtremandResponse> getAssetDetailBySlug(DamUploadPostDTO damUploadPostDTO,
			@PathVariable Integer companyId) {
		return ResponseEntity.ok(damService.getAssetDetailBySlug(damUploadPostDTO.getSlug(), companyId,
				damUploadPostDTO.getLoggedInUserId()));
	}

	/** XNFR-1035 **/
	@GetMapping(value = "damDetailsByDamId/{contentId}/contentType/{contentType}")
	public ResponseEntity<XtremandResponse> damDetailsByDamId(@PathVariable Integer contentId,
			@PathVariable String contentType) {
		return ResponseEntity.ok(damService.damDetailsByDamId(contentId, contentType));
	}
}
