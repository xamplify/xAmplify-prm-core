package com.xtremand.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.xtremand.campaign.bom.ModuleAccessDTO;
import com.xtremand.campaign.exception.XamplifyDataAccessException;
import com.xtremand.common.bom.CompanyProfile;
import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.Criteria.OPERATION_NAME;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.common.bom.Pagination;
import com.xtremand.company.dto.ApprovalSettingsDTO;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.UserListDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.log.service.XamplifyLogService;
import com.xtremand.mail.service.AsyncComponent;
import com.xtremand.mail.service.StatusCodeConstants;
import com.xtremand.social.formbeans.UserPassword;
import com.xtremand.user.bom.User;
import com.xtremand.user.exception.UserDataAccessException;
import com.xtremand.user.service.UserService;
import com.xtremand.userlist.service.UserListService;
import com.xtremand.util.BadRequestException;
import com.xtremand.util.ResponseUtil;
import com.xtremand.util.ResponseUtilException;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.bom.ModuleType;
import com.xtremand.util.dto.AngularUrl;
import com.xtremand.util.dto.LoginAsEmailNotificationDTO;
import com.xtremand.util.service.UtilService;
import com.xtremand.util.service.UtilServiceWithOutTransactional;
import com.xtremand.vanity.url.dto.VanityUrlDetailsDTO;
import com.xtremand.video.bom.VideoCategory;
import com.xtremand.video.exception.VideoDataAccessException;
import com.xtremand.video.service.VideoService;
import com.xtremand.videoencoding.service.FFMPEGStatus;

@RestController
@RequestMapping("/admin")
public class AdminController {
	private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

	@Autowired
	UserService userService;

	@Autowired
	UserListService userListService;

	@Autowired
	com.xtremand.user.validation.UserListValidator userListValidator;

	@Autowired
	VideoService videoService;

	@Autowired
	XamplifyLogService xamplifyLogService;

	@Autowired
	private UtilService utilService;


	@Autowired
	private AsyncComponent asyncComponent;


	Map<String, FFMPEGStatus> statusMap = new HashMap<>();

	@Autowired
	private UtilServiceWithOutTransactional utilServiceWithOutTransactional;

	@RequestMapping(value = "/videos_count", method = RequestMethod.GET)
	public ResponseEntity<?> getVideosCount(@RequestParam Integer userId) {
		try {
			Integer count = videoService.getVideosCount(userId);
			return ResponseEntity.status(HttpStatus.OK).body(Collections.singletonMap("videos_count", count));
		} catch (VideoDataAccessException e) {
			logger.error("Error In saving own Thumbnail image", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
	}

	@RequestMapping(value = "/videos/views_count", method = RequestMethod.GET)
	public ResponseEntity<?> getVideosViewsCount(@RequestParam Integer userId) {
		try {
			Integer count = videoService.getVideosViewsCount(userId);
			return ResponseEntity.status(HttpStatus.OK).body(Collections.singletonMap("videos_views_count", count));
		} catch (VideoDataAccessException e) {
			logger.error("Error In getVideosViewsCount()", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
	}

	@RequestMapping(value = "/videos/monthwise_countrywise_views/{alias}", method = RequestMethod.GET)
	public ResponseEntity<?> getMonthWiseCountryWiseVideoViews(@PathVariable String alias) {
		try {
			Map<String, Object> resultMap = videoService.getMonthWiseCountryWiseVideoViews(alias);
			return ResponseEntity.status(HttpStatus.OK)
					.body(Collections.singletonMap("video_views_count_data", resultMap));
		} catch (VideoDataAccessException e) {
			logger.error("Error In getMonthWiseCountryWiseVideoViews()", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
	}

	/*********** Compare Password ********************/
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/comparePassword", method = RequestMethod.POST)
	public ResponseEntity comparePassword(@RequestBody UserPassword userPassword) {
		logger.debug("Comparing Password" + userPassword.toString());
		try {
			if (userService.comparePassword(userPassword.getOldPassword(), userPassword.getUserId())) {
				return ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.PASSWORD_MATCHED, null);
			} else {
				return ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.PASSWORD_MISMATCH, null);
			}

		} catch (Exception e) {
			logger.error("Error In Comparing Password", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
	}

	/*********** Update Password ********************/
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
	public ResponseEntity updatePassword(@RequestBody UserPassword userPassword) {
		logger.debug("Updating Password" + userPassword.toString());
		try {
			if (userPassword.getOldPassword() != null) {
				if (userService.comparePassword(userPassword.getOldPassword(), userPassword.getUserId())) {
					userService.updatePassword(userPassword);
					return ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.PASSWORD_UPDATED, null);
				} else {
					return ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.PASSWORD_MISMATCH, null);
				}
			} else {
				userService.updatePassword(userPassword);
				return ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.PASSWORD_UPDATED, null);
			}

		} catch (Exception e) {
			logger.error("Error In Updating Password", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
	}

	/*********** Update User ********************/
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/updateUser/{userId}", method = RequestMethod.POST)
	public ResponseEntity updateUser(@PathVariable Integer userId, @RequestBody User user) {
		try {
			user.setUserId(userId);
			userService.updateUser(user);
			return ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.USER_UPDATED, null);
		} catch (Exception e) {
			logger.error("Error In Updating User Details", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
	}

	/*********** Get User Details ********************/
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/getUserByUserName", method = RequestMethod.POST)
	public ResponseEntity getUserByUserName(@RequestParam String userName,
			@RequestParam(required = false, defaultValue = "false") boolean isSuperAdmin) {
		logger.debug("Finding User By getUserByUserName");
		try {
			return ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.USER_FOUND,
					userService.getUserDTO(userName, isSuperAdmin));
		} catch (Exception e) {
			logger.error("Error In Finding User", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
	}

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "uploadProfilePicture/{userId}", method = RequestMethod.POST, consumes = {
			"multipart/form-data" })
	public ResponseEntity uploadProfilePicture(@PathVariable Integer userId, UserDTO userProfile) {
		logger.debug("Uploading Profile Picture" + userProfile.toString());
		try {
			logger.debug("got Profile Pic :" + userProfile.getFile().getOriginalFilename() + " For User Id:::::::::"
					+ userId);
			userProfile.setId(userId);
			String path = userService.uploadProfilePicture(userProfile);
			return ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.USER_IMAGE_UPLOADED,
					Collections.singletonMap("message", path));
		} catch (Exception e) {
			logger.error("Error In Uploading Profile Picutre", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", String.valueOf(e.getCause())));
		}
	}

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/categories", method = RequestMethod.GET)
	public ResponseEntity getCategories() {
		logger.debug("loading categories");
		List<VideoCategory> categories = videoService.getCategories();
		return ResponseEntity.status(HttpStatus.OK).body(categories);
	}

	@RequestMapping(value = { "/video_report/{categoryId}" }, method = RequestMethod.POST)
	public ResponseEntity<?> videoReport(@RequestBody Pagination pagination, @PathVariable Integer categoryId,
			@RequestParam Integer userId) {
		try {
			User user = userService.loadUser(Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, userId)),
					new FindLevel[] { FindLevel.COMPANY_PROFILE });

			Map<String, Object> resultMap = videoService.getVideoReportData(user, pagination, categoryId);
			logger.debug("video Files size :" + resultMap.size());
			return resultMap.isEmpty()
					? (ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.NO_MOB_FOUND, null))
					: (ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.LISTMOB_FOUND, resultMap));
		} catch (UserDataAccessException e) {
			logger.error("error occurred in listVideos() :" + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@GetMapping("/get-user-default-page")
	public String getUserDefaultPage(@RequestParam("userId") Integer userId) {
		return userService.getUserDefaultPage(userId);
	}

	@GetMapping("/set-user-default-page")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void setUserDefaultPage(@RequestParam("userId") Integer userId,
			@RequestParam("defaultPage") String defaultPage) {
		userService.setUserDefaultPage(userId, defaultPage);
	}

	@GetMapping("/get-user-gridview/{userId}")
	public boolean isGridView(@PathVariable Integer userId) {
		return userService.isGridView(userId);
	}

	@GetMapping("/set-user-gridview/{userId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void setGridView(@PathVariable Integer userId, @RequestParam("isGridView") boolean isGridView) {
		userService.setGridView(userId, isGridView);
	}

	@SuppressWarnings("rawtypes")
	@GetMapping(value = "/listAllPartnerEmailIds")
	public ResponseEntity listAllPartnerEmailIds() {
		try {
			logger.debug("In listAllPartnerEmailIds()");
			return ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.ORG_ADMINS_EMAIL_IDS_LIST,
					userService.listAllPartnerEmailIds());
		} catch (UserDataAccessException | ResponseUtilException e) {
			logger.error("In listAllPartnerEmailIds()", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}

	}

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/company-profile/get/{userId}", method = RequestMethod.GET)
	public ResponseEntity getCompanyProfileById(@PathVariable Integer userId) {
		try {
			logger.debug("In getCompanyProfileById(" + userId + ")");
			return ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.CAMPAIGN_FOUND,
					userService.getCompanyProfileByUserId(userId));
		} catch (UserDataAccessException | ResponseUtilException e) {
			logger.error("Error In getCompanyProfileById(" + userId + ")", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}

	}

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/get-company-id/{userId}", method = RequestMethod.GET)
	public ResponseEntity getCompanyIdByUserId(@PathVariable Integer userId) {
		try {
			logger.debug("In getCompanyIdByUserId(" + userId + ")");
			return ResponseEntity.status(HttpStatus.OK).body(userService.getCompanyIdByUserId(userId));
		} catch (UserDataAccessException | ResponseUtilException e) {
			logger.error("Error In getCompanyIdByUserId(" + userId + ")", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}

	}

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/company-profile/save/{userId}", method = RequestMethod.POST)
	public ResponseEntity saveCompanyProfile(@PathVariable Integer userId, @RequestBody CompanyProfile companyProfile) {
		try {
			logger.debug("In saveCompanyProfile(" + companyProfile.toString() + ")");
			return ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.CAMPAIGN_FOUND,
					userService.save(companyProfile, userId));
		} catch (UserDataAccessException | ResponseUtilException e) {
			logger.error("Error In saveCompanyProfile(" + companyProfile.toString() + ")", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
	}

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/company-profile/update/{userId}", method = RequestMethod.POST)
	public ResponseEntity updateCompanyProfile(@PathVariable Integer userId,
			@RequestBody CompanyProfile companyProfile) {
		try {
			logger.debug("In updateCompanyProfile(" + companyProfile.toString() + ")");
			return ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.CAMPAIGN_FOUND,
					userService.update(companyProfile, userId));
		} catch (UserDataAccessException | ResponseUtilException e) {
			logger.error("Error In updateCompanyProfile(" + companyProfile.toString() + ")", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
	}

	@PostMapping("/company-profile/saveFavIcon/{userId}")
	public XtremandResponse uploadFavIconFile(@PathVariable Integer userId,
			@RequestParam("favIconFile") MultipartFile file) {
		return userService.getCompanyFavIconPath(userId, file);
	}

	@PostMapping("/company-profile/saveBgImage/{userId}")
	public XtremandResponse uploadBgImageFile(@PathVariable Integer userId,
			@RequestParam("bgImageFile") MultipartFile file) {
		return userService.getCompanyBgImagePath(userId, file);
	}

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/company-profile/company-names")
	public ResponseEntity listAllCompanyNames() {
		try {
			logger.debug("In listAllCompanyNames()");
			return ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.CAMPAIGN_FOUND,
					userService.listAllCompanyNames());
		} catch (UserDataAccessException | ResponseUtilException e) {
			logger.error("Error In listAllCompanyNames()", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
	}

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/company-profile/company-profile-names")
	public ResponseEntity listAllCompanyProfileNames() {
		try {
			logger.debug("In listAllCompanyProfileNames()");
			return ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.CAMPAIGN_FOUND,
					userService.listAllCompanyProfileNames());
		} catch (UserDataAccessException | ResponseUtilException e) {
			logger.error("Error In listAllCompanyProfileNames()", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
	}

	@PostMapping("/partners/{userId}")
	public Object listPartners(@RequestBody Pagination pagination, @PathVariable Integer userId) {
		return userListService.listPartners(userId, pagination);
	}

	@GetMapping("/default-partner-list/{userId}")
	public UserListDTO defaultPartnerList(@PathVariable Integer userId) {
		return userListService.getOrCreateDefaultPartnerList(userId);
	}

	/********************* All Team Members List **************************/
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/listAllTeamMemberEmailIds/{userId}")
	public ResponseEntity listAllTeamMemberEmailIds(@PathVariable Integer userId) {
		try {
			logger.debug("listAllTeamMemberEmailIds(" + userId + ")");
			return ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.CAMPAIGN_LIST_NAMES_FOUND,
					userService.listTeamMembers(userId));
		} catch (XamplifyDataAccessException e) {
			logger.error("Error In listAllTeamMemberEmailIds", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}

	}

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/partner-team-members/{userId}")
	public ResponseEntity listPartnerAndTeamMembers(@PathVariable Integer userId) {
		try {
			logger.debug("listPartnerAndTeamMembers(" + userId + ")");
			return ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.CAMPAIGN_LIST_NAMES_FOUND,
					userService.listPartnerAndHisTeamMembers(userId));
		} catch (XamplifyDataAccessException e) {
			logger.error("Error In listPartnerAndTeamMembers(" + userId + ")", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}

	}

	@RequestMapping(value = "/company-profile/upload-logo", method = RequestMethod.POST, consumes = {
			"multipart/form-data" })
	public ResponseEntity<?> uploadLogo(MultipartFile file, @RequestParam Integer userId) {
		try {
			logger.debug("In uploadLogo()");
			String path = userService.getCompanyLogoPath(file, userId);
			return ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.UPLOAD_OWN_THUMBNAIL_IMAGE,
					Collections.singletonMap("path", path));
		} catch (VideoDataAccessException e) {
			logger.error("Error In uploadLogo()", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
	}

	@RequestMapping(value = "/company-profile/upload-background-image", method = RequestMethod.POST, consumes = {
			"multipart/form-data" })
	public ResponseEntity<?> uploadBackgroundLogo(MultipartFile file, @RequestParam Integer userId) {
		try {
			logger.debug("In uploadBackgroundLogo()");
			String path = userService.getCompanyBackGroundImagePath(file, userId);
			return ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.UPLOAD_OWN_THUMBNAIL_IMAGE,
					Collections.singletonMap("path", path));
		} catch (VideoDataAccessException e) {
			logger.error("Error In uploadBackgroundLogo()", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
	}

	@PostMapping(value = "/getCompanyIdsByEmailIds")
	public ResponseEntity<List<UserDTO>> listCollections(@RequestBody List<String> emailIds) {
		return ResponseEntity.ok(userService.getCompanyIdsByEmailIds(emailIds));
	}


	@GetMapping("/listAllCompanyProfileImages/{userId}")
	public List<String> listAllCompanyProfileImages(@PathVariable Integer userId) {
		return userService.listAllCompanyProfileImages(userId);
	}

	@GetMapping("/getUserOppertunityModule/{userId}")
	public XtremandResponse getUserOppertunityModule(@PathVariable Integer userId) {
		return userService.getUserOppertunityModule(userId);
	}

	@GetMapping("/getAccess/{companyId}")
	public ModuleAccessDTO getAccessByCompanyId(@PathVariable Integer companyId) {
		return userService.getAccessByCompanyId(companyId);
	}

	@PostMapping("/updateAccess")
	public XtremandResponse updateAccess(@RequestBody ModuleAccessDTO moduleAccessDto) {
		return userService.updateModulesAccess(moduleAccessDto);
	}

	// SMS
	@GetMapping("/getSMSServiceModule/{userId}")
	public XtremandResponse getSMSServiceModule(@PathVariable Integer userId) {
		return userService.getSMSServiceModule(userId);
	}

	@PostMapping("/validatePartnerOrganizations/{userId}/{userListId}")
	public ResponseEntity<Map<String, Object>> validatePartnerOrganizations(@PathVariable Integer userId,
			@PathVariable Integer userListId, @RequestBody List<String> emailIds) {
		return ResponseEntity.ok(utilService.validatePartnerOrganizationByEmailIds(emailIds, userId, userListId));
	}

	@GetMapping("/getRolesByUserId/{userId}")
	public ResponseEntity<XtremandResponse> getRolesByUserId(@PathVariable Integer userId) {
		return ResponseEntity.ok(userService.getRolesByUserId(userId));
	}

	@PostMapping("/getByEmailId")
	public ResponseEntity<XtremandResponse> getUserAndCompanyProfileByEmailId(@RequestBody UserDTO userDto) {
		return ResponseEntity.ok(userService.getUserAndCompanyProfileByEmailId(userDto.getEmailId()));
	}

	@GetMapping("/getTeamMemberRoles/{userId}")
	public ResponseEntity<XtremandResponse> getTeamMemberRoles(@PathVariable Integer userId) {
		return ResponseEntity.ok(userService.getRoleDtosByUserId(userId));
	}

	@GetMapping("/getModulesDisplayDefaultView/{userId}")
	public ResponseEntity<XtremandResponse> getModulesDisplayDefaultView(@PathVariable Integer userId) {
		return ResponseEntity.ok(userService.getModulesDisplayDefaultView(userId));
	}

	@GetMapping("/updateDefaultDisplayView/{userId}/{type}")
	public ResponseEntity<XtremandResponse> updateDefaultDisplayView(@PathVariable Integer userId,
			@PathVariable String type) {
		return ResponseEntity.ok(userService.updateDefaultDisplayView(userId, type));
	}

	/* -- XNFR-415 -- */
	@GetMapping("/updateDefaultDashboardForPartner/{companyId}/{type}")
	public ResponseEntity<Object> updateDefaultDashboardForPartner(@PathVariable Integer companyId,
			@PathVariable String type) {
		return ResponseEntity.ok(userService.updateDefaultDashboardForPartner(companyId, type));
	}

	/* -- XNFR-415 -- */
	@GetMapping("/getDefaultDashboardForPartner/{companyId}")
	public ResponseEntity<Object> getDefaultDashboardForPartner(@PathVariable Integer companyId) {
		return ResponseEntity.ok(userService.getDefaultDashboardForPartner(companyId));
	}

	/* -- XNFR-415 -- */
	@PostMapping("/getDefaultDashboardPageForPartner")
	public ResponseEntity<XtremandResponse> getDefaultDashboardPageForPartner(
			@RequestBody VanityUrlDetailsDTO vanityUrlDetails) {
		return ResponseEntity.ok(userService.getDefaultDashboardPageForPartner(vanityUrlDetails));
	}

	@GetMapping("/getRolesCount/{userId}")
	public int getRolesCount(@PathVariable Integer userId) {
		return userService.getRolesCountByUserId(userId);
	}

	@GetMapping("/hasPartnerAccess/{userId}")
	public ResponseEntity<XtremandResponse> hasPartnerAccess(@PathVariable Integer userId) {
		return ResponseEntity.ok(utilService.hasPartnerAccess(userId));
	}

	@GetMapping("/getSenderMergeTagsData/{userId}")
	public ResponseEntity<XtremandResponse> getSenderMergeTagsData(@PathVariable Integer userId) {
		return ResponseEntity.ok(utilService.getSenderMergeTagsData(userId));
	}

	@PostMapping("authorizeUrl")
	@ResponseBody
	public ResponseEntity<XtremandResponse> authorizeUrl(@RequestBody AngularUrl angularUrl) {
		return ResponseEntity.ok(utilService.authorizeUrl(angularUrl));
	}

	@PostMapping("findAdminsAndTeamMembers")
	@ResponseBody
	public ResponseEntity<XtremandResponse> findAdminsAndTeamMembers(@RequestBody Pagination pagination) {
		return ResponseEntity.ok(userService.findAdminsAndTeamMembers(pagination));
	}

	@GetMapping("updateSpfConfiguration/{companyId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> updateSpfConfiguration(@PathVariable Integer companyId) {
		return ResponseEntity.ok(userService.updateSpfConfiguration(companyId));
	}

	@GetMapping("isSpfConfigured/{companyId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> isSpfConfigured(@PathVariable Integer companyId) {
		return ResponseEntity.ok(userService.isSpfConfigured(companyId));
	}

	@GetMapping("findNotifyPartnersOption/{companyId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> findNotifyPartnersOption(@PathVariable Integer companyId) {
		return ResponseEntity.ok(userService.findNotifyPartnersOption(companyId));
	}

	@GetMapping("updateNotifyPartnersOption/{companyId}/{status}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> updateNotifyPartnersOption(@PathVariable Integer companyId,
			@PathVariable boolean status) {
		return ResponseEntity.ok(userService.updateNotifyPartnersOption(companyId, status));
	}

	@GetMapping("showPartnersFilter/{loggedInUserId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> showPartnersFilter(@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(utilService.showPartnersFilter(loggedInUserId));
	}

	@GetMapping("partnershipOnlyWithPrm/{loggedInUserId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> partnershipOnlyWithPrm(@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(utilService.partnershipEstablishedOnlyWithPrm(loggedInUserId));
	}

	/******* XNFR-224 *******/
	@PostMapping(value = "sendLoginAsPartnerEmailNotification")
	public String sendLoginAsPartnerEmailNotification(
			@RequestBody LoginAsEmailNotificationDTO loginAsEmailNotificationDTO) {
		asyncComponent.sendLoginAsPartnerEmailNotification(loginAsEmailNotificationDTO);
		return "Email sent successfully";
	}

	/******* XNFR-255 *******/
	@GetMapping("shareWhiteLabelContentAccess/loggedInUserId/{loggedInUserId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> findShareWhiteLabelContentAccessByLoggedInUserId(
			@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(utilService.findShareWhiteLabelContentAccessByLoggedInUserId(loggedInUserId));
	}

	/******* XNFR-255 *******/
	@GetMapping("shareWhiteLabelContentAccess/companyProfileName/{companyProfileName}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> findShareWhiteLabelContentAccessByCompanyProfileName(
			@PathVariable String companyProfileName) {
		return ResponseEntity.ok(utilService.findShareWhiteLabelContentAccessByCompanyProfileName(companyProfileName));
	}

	/******* XNFR-362 *******/
	@GetMapping("assetPublishedEmailNotification/loggedInUserId/{loggedInUserId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> assetPublishedEmailNotification(@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(utilService.assetPublishedEmailNotification(loggedInUserId));
	}

	/******* XNFR-362 *******/
	@GetMapping("assetPublishedEmailNotification/companyProfileName/{companyProfileName}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> assetPublishedEmailNotification(@PathVariable String companyProfileName) {
		return ResponseEntity.ok(utilService.assetPublishedEmailNotification(companyProfileName));
	}

	/******* XNFR-362 *******/
	@GetMapping("trackPublishedEmailNotification/loggedInUserId/{loggedInUserId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> trackPublishedEmailNotification(@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(utilService.trackPublishedEmailNotification(loggedInUserId));
	}

	/******* XNFR-362 *******/
	@GetMapping("trackPublishedEmailNotification/companyProfileName/{companyProfileName}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> trackPublishedEmailNotification(@PathVariable String companyProfileName) {
		return ResponseEntity.ok(utilService.trackPublishedEmailNotification(companyProfileName));
	}

	/******* XNFR-362 *******/
	@GetMapping("playbookPublishedEmailNotification/loggedInUserId/{loggedInUserId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> playbookPublishedEmailNotification(@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(utilService.playbookPublishedEmailNotification(loggedInUserId));
	}

	/******* XNFR-362 *******/
	@GetMapping("playbookPublishedEmailNotification/companyProfileName/{companyProfileName}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> playbookPublishedEmailNotification(
			@PathVariable String companyProfileName) {
		return ResponseEntity.ok(utilService.playbookPublishedEmailNotification(companyProfileName));
	}

	/*** XBI-1968 ***/
	@GetMapping("isSpfConfiguredOrDomainConnected/{companyId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> isSpfConfiguredOrDomainConnected(@PathVariable Integer companyId) {
		return ResponseEntity.ok(userService.isSpfConfiguredOrDomainConnected(companyId));
	}

	/*** XNFR-423 ***/
	@GetMapping("countryNames")
	@ResponseBody
	public ResponseEntity<XtremandResponse> getCountryNames() {
		return ResponseEntity.ok(utilService.getCountryNames());
	}

	/******** XNFR-426 *******/
	@GetMapping("/updateLeadApprovalOrRejectionStatus/{companyId}/{leadApprovalStatus}")
	public ResponseEntity<Object> updateLeadApprovalOrRejectionStatus(@PathVariable Integer companyId,
			@PathVariable Boolean leadApprovalStatus) {
		return ResponseEntity.ok(userService.updateLeadApprovalOrRejectionStatus(companyId, leadApprovalStatus));
	}

	@GetMapping("/getLeadApprovalStatus/{companyId}")
	public ResponseEntity<Object> getLeadApprovalStatus(@PathVariable Integer companyId) {
		return ResponseEntity.ok(userService.getLeadApprovalStatus(companyId));
	}

	@RequestMapping(value = { "validatePartnerCompany/{loggedInUserId}" }, method = RequestMethod.POST)
	public ResponseEntity<XtremandResponse> validatePartnerCompany(@PathVariable Integer loggedInUserId,
			@RequestBody CompanyProfile companyProfile) {
		User loggedInUser = userService.loadUser(
				Arrays.asList(new Criteria("userId", OPERATION_NAME.eq, loggedInUserId)),
				new FindLevel[] { FindLevel.COMPANY_PROFILE });
		return ResponseEntity.ok(userService.validateCompany(companyProfile, loggedInUser));
	}

	/******* XNFR-571 *******/
	@GetMapping("dashboardButtonPublishedEmailNotification/loggedInUserId/{loggedInUserId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> dashboardButtonPublishedEmailNotification(
			@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(utilService.dashboardButtonPublishedEmailNotification(loggedInUserId));
	}

	/******* XNFR-571 *******/
	@GetMapping("dashboardButtonPublishedEmailNotification/companyProfileName/{companyProfileName}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> dashboardButtonPublishedEmailNotification(
			@PathVariable String companyProfileName) {
		return ResponseEntity.ok(utilService.dashboardButtonPublishedEmailNotification(companyProfileName));
	}

	@GetMapping("/getDisplayViewType/{userId}/{companyProfileName}")
	public ResponseEntity<XtremandResponse> getDisplayViewType(@PathVariable Integer userId,
			@PathVariable String companyProfileName) {
		return ResponseEntity.ok(userService.getDisplayViewType(userId, companyProfileName));
	}

	@GetMapping("/updateDisplayViewType/{userId}/{type}")
	public ResponseEntity<XtremandResponse> updateDisplaytViewType(@PathVariable Integer userId,
			@PathVariable String type, @RequestParam String companyProfileName) {
		return ResponseEntity.ok(userService.updateDisplayViewType(userId, type, companyProfileName));
	}

	@GetMapping("/setViewTypeForExistingUsers")
	public ResponseEntity<XtremandResponse> setViewTypeForExistingUsers() {
		return ResponseEntity.ok(userService.setViewTypeForExistingUsers());
	}

	@GetMapping("/updatePartnerViewTypes")
	public ResponseEntity<XtremandResponse> updatePartnerViewTypes() {
		return ResponseEntity.ok(utilServiceWithOutTransactional.updatePartnerViewTypes());
	}

	@GetMapping("/isPaymentOverDue/{loggedInUserId}/")
	public ResponseEntity<XtremandResponse> isPaymentOverDue(@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(userService.isPaymentOverDue(loggedInUserId, null));
	}

	@GetMapping("/isPaymentOverDue/{loggedInUserId}/{companyProfileName}")
	public ResponseEntity<XtremandResponse> isPaymentOverDue(@PathVariable Integer loggedInUserId,
			@PathVariable String companyProfileName) {
		return ResponseEntity.ok(userService.isPaymentOverDue(loggedInUserId, companyProfileName));
	}

	@GetMapping("/loadUserDefaultPage/{loggedInUserId}")
	public ResponseEntity<Map<String, Object>> loadUserDefaultPage(@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(userService.loadUserDefaultPage(loggedInUserId, ""));
	}

	@GetMapping("/loadUserDefaultPage/{loggedInUserId}/{companyProfileName}")
	public ResponseEntity<Map<String, Object>> loadUserDefaultPageWithCompanyProfileName(
			@PathVariable Integer loggedInUserId, @PathVariable String companyProfileName) {
		return ResponseEntity.ok(userService.loadUserDefaultPage(loggedInUserId, companyProfileName));
	}

	@GetMapping("/importPartnersFromExternalCSV/{companyId}")
	public ResponseEntity<XtremandResponse> importPartnersFromExternalCSV(@PathVariable Integer companyId) {
		return ResponseEntity.ok(userService.importPartnersFromExternalCSV(companyId));
	}

	@GetMapping("/updateExistingLeadsData/{companyId}")
	public ResponseEntity<XtremandResponse> updateExistingLeadsData(@PathVariable Integer companyId) {
		return ResponseEntity.ok(userService.updateExistingLeadsData(companyId));
	}

	@GetMapping("/getNonExistingUsers/{companyId}")
	public ResponseEntity<XtremandResponse> getNonExistingUsersfromCSV(@PathVariable Integer companyId) {
		return ResponseEntity.ok(userService.getNonExistingUsersfromCSV(companyId));
	}

	/** XNFR-824 start **/
	@GetMapping("/getApprovalConfigurationSettingsByUserId/{userId}")
	public ResponseEntity<XtremandResponse> getApprovalConfigurationSettingsByUserId(@PathVariable Integer userId) {
		return ResponseEntity.ok(userService.getApprovalConfigurationSettingsByUserId(userId));
	}

	@PutMapping("/updateApprovalConfigurationSettings")
	public ResponseEntity<XtremandResponse> updateApprovalConfigurationSettings(
			@RequestBody ApprovalSettingsDTO approvalSettingsDTO) {

		XtremandResponse response = new XtremandResponse();
		boolean constraint = false;
		try {
			response = userService.updateApprovalConfigurationSettings(approvalSettingsDTO);
			return ResponseEntity.ok(response);
		} catch (BadRequestException e) {
			constraint = true;
			throw new BadRequestException(e.getMessage());
		} catch (Exception e) {
			constraint = true;
			throw new XamplifyDataAccessException(e);
		} finally {
			Map<String, Object> approvalsMap = new HashMap<>();
			checkAndApprovePendingDamAndLms(approvalSettingsDTO, response, constraint, approvalsMap);
			@SuppressWarnings("unchecked")
			List<Integer> whiteLabeledReApprovalDamIds = approvalsMap.containsKey("whiteLabeledReApprovalDamIds")
					? (List<Integer>) approvalsMap.get("whiteLabeledReApprovalDamIds")
					: Collections.emptyList();
			if (!constraint && response.getStatusCode() == 200
					&& XamplifyUtils.isNotEmptyList(whiteLabeledReApprovalDamIds)
					&& XamplifyUtils.isValidInteger(approvalSettingsDTO.getLoggedInUserId())
					&& XamplifyUtils.isValidInteger(approvalSettingsDTO.getCompanyId())) {
				asyncComponent.handleWhiteLabeledAssetsAfterReApproval(whiteLabeledReApprovalDamIds,
						approvalSettingsDTO.getCompanyId(), approvalSettingsDTO.getLoggedInUserId());
			}

		}
	}

	private void checkAndApprovePendingDamAndLms(ApprovalSettingsDTO approvalSettingsDTO, XtremandResponse response,
			boolean constraint, Map<String, Object> approvalsMap) {
		if (!constraint && response.getStatusCode() == 200 && !approvalSettingsDTO.isTracksApprovalEnabledForCompany()
				&& XamplifyUtils.isValidInteger(approvalSettingsDTO.getCompanyId())) {
			userService.approvePendingTracksOrPlaybooksByModuleTypeAndTimeLineStatusHistory(approvalSettingsDTO,
					ModuleType.TRACK);

		}

		if (!constraint && response.getStatusCode() == 200
				&& !approvalSettingsDTO.isPlaybooksApprovalEnabledForCompany()
				&& XamplifyUtils.isValidInteger(approvalSettingsDTO.getCompanyId())) {
			userService.approvePendingTracksOrPlaybooksByModuleTypeAndTimeLineStatusHistory(approvalSettingsDTO,
					ModuleType.PLAYBOOK);

		}

		if (!constraint && response.getStatusCode() == 200 && !approvalSettingsDTO.isAssetApprovalEnabledForCompany()
				&& XamplifyUtils.isValidInteger(approvalSettingsDTO.getCompanyId())) {
			userService.approvePendingAssetsAndTimeLineStatusHistory(approvalSettingsDTO, approvalsMap);

		}
	}

	@GetMapping("/createTeamMembersByCompanyId/{companyId}")
	public ResponseEntity<XtremandResponse> createTeamMembersByCompanyId(@PathVariable Integer companyId) {
		return ResponseEntity.ok(userService.createTeamMembersByCompanyId(companyId));
	}

	@PostMapping("/savePartnerFilter/{userId}/{filterOption}")
	public ResponseEntity<XtremandResponse> savePartnerFilter(@PathVariable Integer filterOption,
			@PathVariable Integer userId) {
		try {
			return ResponseEntity.ok(userService.saveTeamMemberFilter(userId, filterOption));
		} catch (BadRequestException e) {
			throw new BadRequestException(e.getMessage());
		} catch (Exception e) {
			throw new XamplifyDataAccessException(e);
		}
	}

	@GetMapping("getTeamMemberPartnerFilter/{loggedInUserId}")
	@ResponseBody
	public ResponseEntity<XtremandResponse> getTeamMemberPartnersFilter(@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(utilService.getTeamMemberFilter(loggedInUserId));
	}

	/** XNFR-891 **/
	@GetMapping("/hasVanityAccess/{loggedInUserId}")
	public ResponseEntity<XtremandResponse> hasVanityAccess(@PathVariable Integer loggedInUserId) {
		return ResponseEntity.ok(utilService.checkVanityAccess(loggedInUserId));
	}

}
