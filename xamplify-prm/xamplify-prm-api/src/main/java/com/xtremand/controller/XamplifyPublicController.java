
package com.xtremand.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xtremand.common.bom.Criteria;
import com.xtremand.common.bom.Criteria.OPERATION_NAME;
import com.xtremand.common.bom.FindLevel;
import com.xtremand.form.dto.FormDTO;
import com.xtremand.form.service.FormService;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.log.bom.XtremandLog;
import com.xtremand.log.service.XamplifyLogService;
import com.xtremand.mail.service.AsyncComponent;
import com.xtremand.mail.service.AsyncService;
import com.xtremand.partner.bom.PartnerDataAccessException;
import com.xtremand.partnership.service.PartnershipService;
import com.xtremand.signup.dto.SignUpRequestDTO;
import com.xtremand.team.member.dto.TeamMemberDTO;
import com.xtremand.team.service.TeamService;
import com.xtremand.unsubscribe.service.UnsubscribeService;
import com.xtremand.user.exception.TeamMemberDataAccessException;
import com.xtremand.user.service.UserService;
import com.xtremand.util.BadRequestException;
import com.xtremand.util.CustomValidatonException;
import com.xtremand.util.XamplifyUtils;
import com.xtremand.util.dto.UserListOperationsAsyncDTO;
import com.xtremand.util.service.UtilService;
import com.xtremand.video.bom.VideoFile;
import com.xtremand.video.service.VideoService;

@Controller
public class XamplifyPublicController {

	private static final Logger logger = LoggerFactory.getLogger(XamplifyPublicController.class);

	@Autowired
	private UserService userService;
	@Autowired
	VideoService videoService;
	@Autowired
	XamplifyLogService xamplifyLogService;

	@Autowired
	private FormService formService;

	@Autowired
	private UnsubscribeService unsubscribeService;

	@Value("${images_path}")
	String imagesPath;

	@Autowired
	private UtilService utilService;

	@Autowired
	private TeamService teamService;

	@Autowired
	private AsyncComponent asyncComponent;

	@Autowired
	private PartnershipService partnershipService;

	@Autowired
	private AsyncService asyncService;

	@RequestMapping(value = { "/user/log_embedvideo_action" }, method = RequestMethod.POST)
	public ResponseEntity<?> logEmbedVideoActions(@RequestBody XtremandLog log) {
		ResponseEntity<?> response = null;
		try {
			if (StringUtils.hasText(log.getVideoAlias())) {
				Collection<VideoFile> videoFiles = videoService.find(
						Arrays.asList(new Criteria("alias", OPERATION_NAME.eq, log.getVideoAlias())),
						new FindLevel[] { FindLevel.SHALLOW });
				if (!videoFiles.isEmpty()) {
					VideoFile videoFile = videoService
							.find(Arrays.asList(new Criteria("alias", OPERATION_NAME.eq, log.getVideoAlias())),
									new FindLevel[] { FindLevel.SHALLOW })
							.iterator().next();
					log.setVideoId(videoFile.getId());
					Integer id = xamplifyLogService.logEmbedVideoActions(log);
					response = ResponseEntity.status(HttpStatus.OK).body(Collections.singletonMap("id", id));
				}
			}
		} catch (Exception ve) {
			logger.error("error occurred in logEmbedVideoActions(" + log.toString() + ") :" + ve);
			response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ve.getMessage());
		}
		return response;
	}

	@RequestMapping(value = "/user-video-heat-map-by-unique-session", method = RequestMethod.GET)
	public ResponseEntity<?> getHeatMapByUniqueSession(@RequestParam String sessionId) {
		try {
			return new ResponseEntity<>(xamplifyLogService.getHeatMapByUniqueSession(sessionId), HttpStatus.OK);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
	}

	@RequestMapping(value = "/user/{alias}", method = RequestMethod.GET)
	public ResponseEntity<?> getSignUpDetails(@PathVariable String alias) {
		logger.debug("entered into loadUserByAlias() with alias:  " + alias);
		try {
			UserDTO user = userService.getSignUpDetails(alias);
			return ResponseEntity.status(HttpStatus.OK).body(user);
		} catch (Exception e) {
			logger.error("error occured in loadUserByAlias(): " + e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonMap("message", e.getMessage()));
		}
	}

	@GetMapping(value = "getByFormAlias")
	@ResponseBody
	public XtremandResponse getByAlias(FormDTO formDto) {
		return formService.getByAlias(formDto.getAlias(), formDto.isVendorJourney(), formDto.isPartnerJourneyPage());
	}

	@RequestMapping(value = { "/user/smsLogEmbedVideoActions" }, method = RequestMethod.POST)
	public ResponseEntity<?> smsLogEmbedVideoActions(@RequestBody XtremandLog log) {

		ResponseEntity<?> response = null;
		try {
			VideoFile videoFile = videoService
					.find(Arrays.asList(new Criteria("alias", OPERATION_NAME.eq, log.getVideoAlias())),
							new FindLevel[] { FindLevel.SHALLOW })
					.iterator().next();
			log.setVideoId(videoFile.getId());
			Integer id = xamplifyLogService.logEmbedVideoActions(log);
			response = ResponseEntity.status(HttpStatus.OK).body(Collections.singletonMap("id", id));
		} catch (Exception ve) {
			logger.error("error occurred in logEmbedVideoActions() :" + ve.getMessage());
			response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ve.getMessage());
		}
		return response;
	}

	@GetMapping("/getUserByAlias/{alias}")
	public ResponseEntity<XtremandResponse> getUserByAlias(@PathVariable String alias,
			@RequestParam String companyProfileName) {
		return ResponseEntity.ok(userService.getUserByAlias(alias, companyProfileName));
	}

	@PostMapping("/accessAccount/updatePassword")
	public ResponseEntity<XtremandResponse> saveAccountPassword(@RequestBody UserDTO user) {
		return ResponseEntity.ok(userService.saveAccountPassword(user));
	}

	@GetMapping("validate-captcha/{response}")
	@ResponseBody
	public boolean getValidation(@PathVariable String response) {
		return xamplifyLogService.executePost(response);
	}

	@GetMapping("/findAllUnsubscribeReasons/{companyId}")
	public ResponseEntity<XtremandResponse> findAllUnsubscribedReasons(@PathVariable Integer companyId) {
		return ResponseEntity.ok(unsubscribeService.findAll(companyId));
	}

	@GetMapping(value = "/findUnsubscribePageContentByCompanyId/{companyId}")
	public ResponseEntity<XtremandResponse> findUnsubscribePageContentByCompanyId(@PathVariable Integer companyId) {
		return ResponseEntity.ok(unsubscribeService.findUnsubscribePageContentByCompanyId(companyId));
	}

	@GetMapping("/getFirstNameLastNameAndEmailIdByUserId/{userId}")
	public ResponseEntity<XtremandResponse> getFirstNameLastNameAndEmailIdByUserId(@PathVariable Integer userId) {
		return ResponseEntity.ok(userService.getFirstNameLastNameAndEmailIdByUserId(userId));
	}

	@GetMapping("/findCompanyDetails/{companyProfileName}")
	public ResponseEntity<XtremandResponse> findCompanyDetails(@PathVariable String companyProfileName) {
		return new ResponseEntity<>(utilService.findCompanyDetailsByCompanyProfileName(companyProfileName),
				HttpStatus.OK);
	}

	@PostMapping("/signUpAsTeamMember")
	public ResponseEntity<XtremandResponse> signUpAsTeamMember(@RequestBody SignUpRequestDTO signUpRequestDTO) {
		boolean constraint = false;
		try {
			return new ResponseEntity<>(teamService.addTeamMemberUsingSignUpLink(signUpRequestDTO), HttpStatus.OK);
		} catch (BadRequestException e) {
			constraint = true;
			throw new BadRequestException(e.getMessage());
		} catch (CustomValidatonException e) {
			constraint = true;
			throw new CustomValidatonException(e.getMessage());
		} catch (Exception e) {
			constraint = true;
			throw new TeamMemberDataAccessException(e);
		} finally {
			if (!constraint) {
				asyncService
						.sendTeamMemberSignedUpEmailNotificationsToAdminsAndPublishTracksOrPlayBooks(signUpRequestDTO);
			}
		}
	}

	/***** XNFR-506 ********/
	@PostMapping("/signUpAsPartner")
	public ResponseEntity<XtremandResponse> signUpAsPartner(@RequestBody SignUpRequestDTO signUpRequestDTO) {
		boolean constraint = false;
		UserListOperationsAsyncDTO userListOperationsAsyncDTO = new UserListOperationsAsyncDTO();
		try {
			return new ResponseEntity<>(utilService.addAsPartner(signUpRequestDTO, userListOperationsAsyncDTO),
					HttpStatus.OK);
		} catch (BadRequestException e) {
			constraint = true;
			throw new BadRequestException(e.getMessage());
		} catch (Exception e) {
			constraint = true;
			throw new PartnerDataAccessException(e);
		} finally {
			if (!constraint) {
				boolean isPartnerCreatedSuccessfully = userListOperationsAsyncDTO.isPartnerList()
						&& userListOperationsAsyncDTO.getStatusCode() != null
						&& userListOperationsAsyncDTO.getStatusCode().equals(200);

				boolean isTeamMemberCreatedSuccessfully = userListOperationsAsyncDTO.getTeamMembers() != null
						&& XamplifyUtils.isValidInteger(userListOperationsAsyncDTO.getVendorAdminId());

				if (isPartnerCreatedSuccessfully) {
					Integer userId = userListOperationsAsyncDTO.getVendorAdminId();
					asyncComponent.sendPartnerSignedUpEmailNotifications(signUpRequestDTO);
					Set<UserDTO> partners = userListOperationsAsyncDTO.getPartners();
					Set<Integer> partnerListIds = userListOperationsAsyncDTO.getPartnerListIds();
					partnershipService.publishDAMAndLMSToNewlyAddedPartners(partnerListIds, userId, partners);
					/*** XNFR-597 ****/
					asyncComponent.publishDashboardButtonsToNewlyAddedPartners(userListOperationsAsyncDTO, userId);
				} else if (isTeamMemberCreatedSuccessfully) {
					publishLmsToOnboardedTeamMember(userListOperationsAsyncDTO);
				}

			}

		}

	}

	@PostMapping("/signUpAsPrm")
	public ResponseEntity<XtremandResponse> signUpAsPrm(@RequestBody SignUpRequestDTO signUpRequestDTO) {
		XtremandResponse response = userService.registerPrmAccount(signUpRequestDTO);
		HttpStatus status = response.getStatusCode() == 400 ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
		return ResponseEntity.status(status).body(response);
	}

	private void publishLmsToOnboardedTeamMember(UserListOperationsAsyncDTO userListOperationsAsyncDTO) {
		Integer primaryAdminId = userListOperationsAsyncDTO.getVendorAdminId();
		List<TeamMemberDTO> teamMemberDTOs = new ArrayList<>();
		teamMemberDTOs.addAll(userListOperationsAsyncDTO.getTeamMembers());
		asyncComponent.publishLMSToNewTeamMembers(teamMemberDTOs, primaryAdminId);
	}

	// XNFR-889
	@GetMapping(value = "/getPartnerCompanyByEmailDomain/{emailId:.+}/{companyProfileName}")
	public ResponseEntity<XtremandResponse> getPartnerCompanyByEmailDomain(@PathVariable String emailId,
			@PathVariable String companyProfileName) {
		return new ResponseEntity<>(utilService.getPartnerCompanyByEmailDomain(emailId, companyProfileName),
				HttpStatus.OK);
	}

}