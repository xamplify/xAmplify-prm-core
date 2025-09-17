package com.xtremand.custom.exception;

import java.util.Date;
import java.util.List;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(value = Include.NON_NULL)
public class XamplifyApiErrorResponse {

	@JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "Asia/Kolkata", pattern = "dd-MM-yyyy HH:mm:ss zzz")
	private Date timestamp;

	private HttpStatus status;

	private String message;

	private String description;

	private List<ErrorMessage> errorMessages;

	public XamplifyApiErrorResponse() {
		super();
	}

	public XamplifyApiErrorResponse(HttpStatus status, String message, Date timestamp) {
		super();
		this.status = status;
		this.message = message;
		this.timestamp = timestamp;
	}

	public XamplifyApiErrorResponse(HttpStatus status, String message, String description, Date timestamp) {
		super();
		this.status = status;
		this.message = message;
		this.description = description;
		this.timestamp = timestamp;
	}

	public XamplifyApiErrorResponse(HttpStatus status, List<ErrorMessage> errorMessages, String message,
			String description, Date timestamp) {
		super();
		this.status = status;
		this.message = message;
		this.errorMessages = errorMessages;
		this.timestamp = timestamp;
		this.description = description;
	}

}
