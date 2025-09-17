package com.xtremand.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xtremand.account.dto.ResendEmailDTO;
import com.xtremand.formbeans.UserDTO;
import com.xtremand.formbeans.XtremandResponse;
import com.xtremand.mail.service.StatusCodeConstants;
import com.xtremand.user.exception.UserDataAccessException;
import com.xtremand.user.service.UserService;
import com.xtremand.util.ResponseUtil;
import com.xtremand.util.XamplifyUtils;

@RestController
@RequestMapping("/register")
public class RegisterController {
	private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

	@Value("${invalidVendorEmailId}")
	String invalidVendorEmailId;

	@Autowired
	UserService userService;

	@RequestMapping(value = "/forgotpassword", method = RequestMethod.GET)
	public ResponseEntity forgotPassword(@RequestParam String emailId, @RequestParam String companyProfileName) {
		ResponseEntity response = null;
		String modifiedEmailId = XamplifyUtils.replaceSpacesWithPlus(emailId);
		userService.forgotPassword(modifiedEmailId, companyProfileName);
		response = ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.FORGOTPWD_SUCCESS, null);
		return response;
	}

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = { "/signup/user" }, method = RequestMethod.POST)
	public ResponseEntity registerUser(@RequestBody UserDTO user, @RequestParam String companyProfileName) {
		ResponseEntity responseEntity = null;
		try {
			return ResponseEntity.ok(userService.registerUser(user));
		} catch (UserDataAccessException ude) {
			logger.error("error occured in user signup: " + ude.getMessage());
			if (("Username is already existing").equals(ude.getMessage())) {
				responseEntity = ResponseUtil.getResponse(HttpStatus.BAD_REQUEST,
						StatusCodeConstants.DUPLICATE_USERNAME, null);
			} else if (("User is already existing with this email").equals(ude.getMessage())) {
				responseEntity = ResponseUtil.getResponse(HttpStatus.BAD_REQUEST, StatusCodeConstants.DUPLICATE_EMAIL,
						null);
			}
		} catch (Exception e) {
			logger.error("error occured in user signup: " + e.getMessage());
			responseEntity = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
		return responseEntity;
	}

	@RequestMapping(value = "/verifyemail/user", method = RequestMethod.GET)
	public ResponseEntity verifyEmail(@RequestParam String alias, @RequestParam String companyProfileName)
			throws UserDataAccessException {
		userService.approveUser(alias);
		return ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.USER_VERIFIED, null);
	}

	@RequestMapping(value = "/embed_video/saveuser", method = RequestMethod.GET)
	public ResponseEntity verifyEmail(@RequestBody UserDTO userDTO) throws UserDataAccessException {
		try {
			userService.embedVideoSaveUser(userDTO);
			return ResponseUtil.getResponse(HttpStatus.OK, StatusCodeConstants.USER_VERIFIED, null);
		} catch (Exception e) {
			logger.error("error occured in verifyEmail: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	/********* XNFR-334 *****************/
	@PostMapping(value = "/resend/activationemail")
	public ResponseEntity<XtremandResponse> resendActivationEmail(@RequestBody ResendEmailDTO resendEmailDTO) {
		return new ResponseEntity<XtremandResponse>(userService.resendActivationEmail(resendEmailDTO), HttpStatus.OK);

	}

	/********* XNFR-334 *****************/

}
