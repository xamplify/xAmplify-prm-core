package com.xtremand.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Pagination;
import com.xtremand.formbeans.EmailLogReport;
import com.xtremand.formbeans.VideoFileDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.log.service.XamplifyLogService;
import com.xtremand.mail.service.StatusCodeConstants;
import com.xtremand.user.service.UserService;
import com.xtremand.util.ResponseUtil;
import com.xtremand.video.bom.CloudContent;
import com.xtremand.video.bom.VideoDefaultSettings;
import com.xtremand.video.bom.VideoFile;
import com.xtremand.video.exception.VideoDataAccessException;
import com.xtremand.video.formbeans.VideoFileUploadForm;
import com.xtremand.video.service.VideoService;
import com.xtremand.videoencoding.service.FFMPEGStatus;

@RestController
@RequestMapping("/videos")
public class VideoController {

	private static final Logger logger = LoggerFactory.getLogger(VideoController.class);

	@Autowired
	UserService userService;

	@Autowired
	VideoService videoService;

	@Autowired
	XamplifyLogService xamplifyLogService;

	@RequestMapping(value = { "/loadVideos/{categoryId}" }, method = RequestMethod.POST)
	public ResponseEntity<?> loadVideos(@RequestBody Pagination pagination, @PathVariable Integer categoryId,
			@RequestParam Integer userId) {
		logger.debug("entered into loadVideos()");
		Map<String, Object> resultMap = videoService.loadVideos(userId, pagination, categoryId);
		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}

	@RequestMapping(value = "video-by-alias/{alias}/{userId}", method = RequestMethod.GET)
	public ResponseEntity<?> getVideoByAlias(@PathVariable("alias") String alias,
			@PathVariable("userId") Integer userId, @RequestParam String viewBy,
			@RequestParam(required = false, defaultValue = "") String companyProfileName) throws Exception {
		logger.info("got video alias :" + alias);
		try {
			VideoFileDTO videoFileDTO = videoService.getVideoByAlias(userId, alias,
					VideoFile.TYPE.valueOf(viewBy.toUpperCase()), companyProfileName);
			logger.info("video found :" + videoFileDTO.getId());
			return videoFileDTO.getId() == null
					? (ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.NO_MOB_FOUND_ID, null))
					: (ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.MOB_FOUND_ID, videoFileDTO));
		} catch (VideoDataAccessException ve) {
			logger.error("error occurred in getVideoByAlias() :" + ve.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ve.getMessage());
		}
	}

	@RequestMapping(value = { "upload-video" }, method = RequestMethod.POST, consumes = { "multipart/form-data" })
	public ResponseEntity<?> uploadVideo(MultipartFile file, @RequestParam Integer userId) {
		logger.debug("got video :" + file.getOriginalFilename());
		try {
			XtremandResponse response = videoService.saveVideo(file, userId);
			logger.info("video uploading done..!! ");
			return ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.UPLOAD_VIDEO, response);
		} catch (VideoDataAccessException e) {
			logger.error("error occurred in uploadVideo() :" + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@RequestMapping(value = { "upload-cloud-video" }, method = RequestMethod.POST)
	public ResponseEntity<?> uploadCloudVideo(String downloadLink, String fileName,
			@RequestParam(value = "oauthToken", required = false) String oauthToken, @RequestParam Integer userId) {
		try {
			logger.debug("got video :" + fileName);
			XtremandResponse xtremandResponse = videoService.saveVideo(downloadLink, fileName, oauthToken, userId);
			logger.info("video uploading done..!! ");
			return ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.UPLOAD_VIDEO, xtremandResponse);
		} catch (VideoDataAccessException e) {
			logger.error("error occurred in uploadCloudVideo() :" + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@RequestMapping(value = { "upload-cloud-content" }, method = RequestMethod.POST)
	public ResponseEntity<?> uploadCloudContent(@RequestBody List<CloudContent> files, @RequestParam Integer userId) {
		try {
			logger.debug("got content files :" + files.size());
			return ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.EMAIL_TEMPLATE_NAMES_FOUND,
					videoService.uploadFile(files, userId));
		} catch (VideoDataAccessException e) {
			logger.error("error occurred in uploadCloudContent() :" + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@RequestMapping(value = "/upload-own-thumbnail", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	public ResponseEntity uploadOwnThumbnail(MultipartFile file, @RequestParam Integer userId) {
		try {
			logger.debug("Got uploadOwnThumbnail image File" + file.getOriginalFilename());
			String path = videoService.uploadOwnThumbnail(file, userId);
			logger.info("Thumbnail image File Saved in " + path);
			return ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.UPLOAD_OWN_THUMBNAIL_IMAGE,
					Collections.singletonMap("path", path));
		} catch (VideoDataAccessException e) {
			logger.error("Error In uploadOwnThumbnail()", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
	}

	@RequestMapping(value = "/upload-branding-logo", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	public ResponseEntity uploadBrandingLogo(MultipartFile file, @RequestParam Integer userId,
			@RequestParam boolean videoDefaultSetting) {
		try {
			logger.debug("Got uploadBrandingLogo image File" + file.getOriginalFilename());
			String path = videoService.uploadBrandingLogo(file, userId, videoDefaultSetting);
			logger.info("BrandingLogo image File Saved in " + path);
			return ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.UPLOAD_OWN_THUMBNAIL_IMAGE,
					Collections.singletonMap("path", path));
		} catch (VideoDataAccessException e) {
			logger.error("Error In uploadOwnThumbnail()", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
	}

	@RequestMapping(value = "/save-branding-logo", method = RequestMethod.GET)
	public ResponseEntity saveBrandingLogo(String logoPath, String LogoDescUri, Integer userId) {
		try {
			logger.debug("from saveBrandingLogo() ");
			Map map = videoService.saveBrandingLogo(logoPath, LogoDescUri, userId);
			return ResponseEntity.status(HttpStatus.OK).body(map);
		} catch (VideoDataAccessException e) {
			logger.error("Error In uploadOwnThumbnail()", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
	}

	@RequestMapping(value = "process-video", method = RequestMethod.POST)
	public ResponseEntity<?> processVideo(@RequestParam String path, @RequestParam Integer userId) {
		logger.debug("got uploaded video path :" + path);
		try {
			FFMPEGStatus status = new FFMPEGStatus();
			VideoFileUploadForm form = videoService.processVideo(path, userId, status);
			logger.info("Video processing complete! ");
			return ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.PROCESS_VIDEO, form);
		} catch (Exception e) {
			logger.error("error occurred in processVideo() :" + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public ResponseEntity<?> save(@RequestBody VideoFileUploadForm videoFileUploadForm, @RequestParam Integer userId) {
		logger.debug("entered into save()");
		try {
			VideoFile videoFile = videoService.updateVideo(videoFileUploadForm, userId);
			VideoFileDTO videoFileDTO = videoService.getVideoFileDTO(videoFile, userId);
			return ("save".equalsIgnoreCase(videoFileUploadForm.getAction()))
					? ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.SAVE_VIDEO, videoFileDTO)
					: ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.EDIT_VIDEO, videoFileDTO);
		} catch (Exception e) {
			logger.error("error occurred " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@RequestMapping(value = "/video-default-settings", method = RequestMethod.POST)
	public ResponseEntity<?> videoDefaultSettings(@RequestParam Integer userId,
			@RequestBody(required = false) VideoDefaultSettings videoDefaultSettings) {
		try {
			logger.debug("entered into the VideoDefaultSettings() method");
			videoService.updateVideoDefaultSettings(
					((CompanyProfile) ((XtremandResponse) userService.getCompanyProfileByUserId(userId)).getData()),
					videoDefaultSettings);
			return ResponseEntity.status(HttpStatus.OK).body(Collections.singletonMap("message", "success"));
		} catch (VideoDataAccessException e) {
			logger.error("Error In videoDefaultSettings", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
	}

	@RequestMapping(value = "/video-default-settings", method = RequestMethod.GET)
	public ResponseEntity<?> getVideoDefaultSettings(@RequestParam Integer userId) {
		try {
			logger.debug("entered into the VideoDefaultSettings() method");
			VideoDefaultSettings videoDefaultSettings = videoService.getVideoDefaultSettings(
					((CompanyProfile) ((XtremandResponse) userService.getCompanyProfileByUserId(userId)).getData())
							.getId());
			return ResponseEntity.status(HttpStatus.OK).body(videoDefaultSettings);
		} catch (VideoDataAccessException e) {
			logger.error("Error In getVideoDefaultSettings() method", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
	}

	@RequestMapping(value = "/video-titles", method = RequestMethod.GET)
	public ResponseEntity<?> listVideoTitles(@RequestParam Integer userId) {
		try {
			logger.debug("entered into the listVideoTitles() method");
			List<String> titles = videoService.getVideoTitles(userId);
			return ResponseEntity.status(HttpStatus.OK).body(Collections.singletonMap("titles", titles));
		} catch (VideoDataAccessException e) {
			logger.error("Error In listVideoTitles()", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
	}

	/***************** Save Recorded Video *************************/
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/save-recorded-video", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	public ResponseEntity saveRecordedVideo(MultipartFile file, @RequestParam Integer userId) {
		try {
			logger.debug("Got Recorded Video File" + file.getName());
			XtremandResponse response = videoService.saveRecordedVideo(file, userId);
			logger.info("Recorded Video File Saved in " + response.getData());
			return ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.UPLOAD_VIDEO, response);
		} catch (VideoDataAccessException e) {
			logger.error("Error In Saving Recorded Video", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
	}

	@RequestMapping(value = { "/channel-videos/{categoryId}" }, method = RequestMethod.POST)
	public ResponseEntity<?> listChannelVideos(@RequestBody Pagination pagination, @PathVariable Integer categoryId,
			@RequestParam Integer userId) {
		logger.debug("entered into listChannelVideos()");
		try {
			Map<String, Object> resultMap = videoService.getChannelVideos(pagination, categoryId, userId);
			return resultMap.isEmpty()
					? (ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.NO_MOB_FOUND, null))
					: (ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.LISTMOB_FOUND, resultMap));
		} catch (VideoDataAccessException e) {
			logger.error("error occurred in listChannelVideos() :" + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@RequestMapping(value = { "{videoId}/user/save-call-action-user" }, method = RequestMethod.POST)
	public ResponseEntity<?> logSaveCallToActionUser(@RequestBody EmailLogReport emailLogReport,
			@PathVariable Integer videoId) {

		ResponseEntity<?> response = null;
		try {
			logger.debug("user : " + emailLogReport);
			Map<String, Object> resultMap = userService.logSaveCallToActionUser(emailLogReport, videoId);
			response = ResponseEntity.status(HttpStatus.OK).body(resultMap);
		} catch (Exception ex) {
			logger.error("error occurred in log save call to action user() :" + ex.getMessage());
			response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
		}
		return response;
	}



	@RequestMapping(value = "public/{companyId}", method = RequestMethod.GET)
	public ResponseEntity<?> listPublicVideosByCompany(@PathVariable Integer companyId) {
		try {
			List<VideoFileDTO> videos = videoService.listPublicVideosByCompany(companyId);
			return ResponseEntity.status(HttpStatus.OK).body(videos);
		} catch (Exception e) {
			logger.error("Error In getURLShortenerByAlias method ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
	}

	@RequestMapping(value = "video-by-id/{videoId}/{userId}", method = RequestMethod.GET)
	public ResponseEntity<?> getVideoById(@PathVariable("videoId") Integer videoId,
			@PathVariable("userId") Integer userId, @RequestParam String viewBy) throws Exception {
		logger.info("got video videoId :" + videoId);
		try {
			VideoFileDTO videoFileDTO = videoService.getVideoById(userId, videoId,
					VideoFile.TYPE.valueOf(viewBy.toUpperCase()));
			logger.info("video found :" + videoFileDTO.getId());
			return videoFileDTO.getId() == null
					? (ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.NO_MOB_FOUND_ID, null))
					: (ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.MOB_FOUND_ID, videoFileDTO));
		} catch (VideoDataAccessException ve) {
			logger.error("error occurred in getVideoByAlias() :" + ve.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ve.getMessage());
		}
	}

	/******** XNFR-318 ********/
	@PostMapping(value = "/findVideos")
	public ResponseEntity<XtremandResponse> findVideos(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(videoService.findVideos(pagination));
	}

	/******** XNFR-543 ********/
	@GetMapping("findVideoById/{damId}/{userId}")
	public ResponseEntity<XtremandResponse> findVideoByDamId(@PathVariable Integer damId,
			@PathVariable Integer userId) {
		return new ResponseEntity<>(videoService.findVideoFileByDamIdAndUserId(damId, userId), HttpStatus.OK);
	}

}
