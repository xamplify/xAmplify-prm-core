package com.xtremand.comments.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class CommentRequestDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8467981407879315749L;

	private Integer id;

	private String comment;

	private Integer commentedBy;

	private String moduleType;

	private String statusInString;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
