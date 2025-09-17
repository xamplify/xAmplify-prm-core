package com.xtremand.custom.exception;

import lombok.Data;

@Data
public class ErrorMessage {

	public ErrorMessage() {

	}

	public ErrorMessage(String field, String message) {
		super();
		this.field = field;
		this.message = message;
	}

	private String field;

	private String message;

}
