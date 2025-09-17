package com.xtremand.exception;

import java.util.Date;
import java.util.List;

import org.springframework.http.HttpStatus;

public class ApiError {

	private HttpStatus status;
	private Integer statusCode;
	private Date timestamp;
	private String message;
	private String debugMessage;
	private List<ApiSubError> subErrors;

	public HttpStatus getStatus() {
		return status;
	}

	public void setStatus(HttpStatus status) {
		this.status = status;
		this.statusCode = status.value();
	}

	public Integer getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(Integer statusCode) {
		this.statusCode = statusCode;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getDebugMessage() {
		return debugMessage;
	}

	public void setDebugMessage(String debugMessage) {
		this.debugMessage = debugMessage;
	}

	public List<ApiSubError> getSubErrors() {
		return subErrors;
	}

	public void setSubErrors(List<ApiSubError> subErrors) {
		this.subErrors = subErrors;
	}

	private ApiError() {
		timestamp = new Date();
	}

	ApiError(HttpStatus status) {
		this();
		this.setStatus(status);
	}

	ApiError(HttpStatus status, Throwable ex) {
		this();
		this.setStatus(status);
		this.message = ex.getMessage();
		this.debugMessage = ex.getLocalizedMessage();
	}

	ApiError(HttpStatus status, String message, Throwable ex) {
		this();
		this.setStatus(status);
		this.message = message;
		this.debugMessage = ex.getLocalizedMessage();
	}
}
