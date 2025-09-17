package com.xtremand.formbeans;

import java.io.Serializable;

import lombok.Data;

@Data
public class ErrorResponse implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4413912984512812004L;
	
	private String field;
	
	private String message;

	public ErrorResponse(String field, String message) {
		super();
		this.field = field;
		this.message = message;
	}

	

}
