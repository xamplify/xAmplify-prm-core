package com.xtremand.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.xtremand.partner.bom.PartnerDataAccessException;
@ControllerAdvice
public class PartnerAnalyticsExceptionHandler extends ResponseEntityExceptionHandler {
	@ExceptionHandler(PartnerDataAccessException.class)
	protected ResponseEntity<Object> handleXamplifyDataAccessException(PartnerDataAccessException ex) {
		return buildResponseEntity(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex));
	}
	
	
	private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
		return new ResponseEntity<>(apiError, apiError.getStatus());
	}
}
