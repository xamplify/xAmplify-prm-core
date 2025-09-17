package com.xtremand.util.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import com.xtremand.custom.exception.ErrorMessage;
import com.xtremand.custom.exception.XamplifyApiErrorResponse;
import com.xtremand.formbeans.XtremandResponse;

@Component
public class XamplifyUtilValidator {

	private static final String ID_PARAMETER = XamplifyConstants.ID;

	private static final String USER_ID_PARAMETER = XamplifyConstants.USER_ID;

	@Value("${text.pattern}")
	private String textPattern;

	@Value("${url.pattern}")
	private String urlPattern;

	public XamplifyApiErrorResponse addCustomErrorMessages(BindingResult result) {
		List<ErrorMessage> errorMessages = new ArrayList<>();
		for (FieldError error : result.getFieldErrors()) {
			ErrorMessage errorMessage = new ErrorMessage();
			errorMessage.setField(error.getField());
			errorMessage.setMessage(error.getDefaultMessage());
			errorMessages.add(errorMessage);
		}
		return new XamplifyApiErrorResponse(HttpStatus.BAD_REQUEST, errorMessages, HttpStatus.BAD_REQUEST.name(), null,
				new Date());
	}

	public void addErrorResponse(BindingResult result, XtremandResponse response) {
		XamplifyApiErrorResponse xamplifyApiErrorResponse = addCustomErrorMessages(result);
		response.setData(xamplifyApiErrorResponse);
		response.setStatusCode(xamplifyApiErrorResponse.getStatus().value());
		response.setAccess(true);
	}

	public boolean isValidText(String text) {
		if (StringUtils.hasText(text)) {
			Pattern pattern = Pattern.compile(textPattern);
			return pattern.matcher(text).matches();
		} else {
			return true;
		}
	}

	public void addFieldError(Errors errors, String field, String errorMessage) {
		errors.rejectValue(field, "", errorMessage);
	}

	public void validatePattern(Errors errors, String propertyName, String parameter, String errorMessage) {
		boolean isValidPattern = isValidText(propertyName);
		if (!isValidPattern) {
			addFieldError(errors, parameter, errorMessage);
		}
	}

	public void validateUrlPattern(Errors errors, String propertyName, String parameter, String errorMessage) {
		boolean isValidPattern = isValidUrlPattern(propertyName);
		if (!isValidPattern) {
			addFieldError(errors, parameter, errorMessage);
		}
	}

	public boolean isValidUrlPattern(String url) {
		if (StringUtils.hasText(url)) {
			Pattern pattern = Pattern.compile(urlPattern);
			return pattern.matcher(url).matches();
		} else {
			return true;
		}
	}

	public void validateIdParameter(Errors errors, Integer id) {
		if (id == null) {
			addFieldError(errors, ID_PARAMETER, ID_PARAMETER + XamplifyConstants.PARAMETER_IS_MISSING);
		} else if (id.equals(0)) {
			addFieldError(errors, ID_PARAMETER, XamplifyConstants.INVALID + ID_PARAMETER);
		}
	}

	public void validateUserIdParameter(Errors errors, Integer userId) {
		if (userId == null) {
			addFieldError(errors, USER_ID_PARAMETER, USER_ID_PARAMETER + XamplifyConstants.PARAMETER_IS_MISSING);
		} else if (userId.equals(0)) {
			addFieldError(errors, USER_ID_PARAMETER, XamplifyConstants.INVALID + USER_ID_PARAMETER);
		}
	}

}
