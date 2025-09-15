package com.xtremand.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.xtremand.campaign.exception.XamplifyDataAccessException;

@ControllerAdvice
public class EmailTemplateExceptionHandler extends ResponseEntityExceptionHandler {

	
	@ExceptionHandler(XamplifyDataAccessException.class)
	protected ResponseEntity<Object> handleXamplifyDataAccessException(XamplifyDataAccessException ex) {
		return buildResponseEntity(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex));
	}
	
	
	private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
		return new ResponseEntity<>(apiError, apiError.getStatus());
	}
}
