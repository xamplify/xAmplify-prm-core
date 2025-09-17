package com.xtremand.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.xtremand.mdf.exception.DuplicateRequestTitleException;

@ControllerAdvice
public class MdfExceptionHandler extends ResponseEntityExceptionHandler {
	
	@ExceptionHandler(DuplicateRequestTitleException.class)
	protected ResponseEntity<Object> handleDuplicateRequestTitleException(DuplicateRequestTitleException ex) {
		return buildResponseEntity(new ApiError(HttpStatus.BAD_REQUEST, ex));
	}
	
	
	private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
		return new ResponseEntity<>(apiError, apiError.getStatus());
	}

}
