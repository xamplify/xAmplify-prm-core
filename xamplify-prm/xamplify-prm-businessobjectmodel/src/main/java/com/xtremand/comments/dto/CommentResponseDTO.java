package com.xtremand.comments.dto;

import java.io.Serializable;
import java.util.Date;

import com.xtremand.util.dto.DateInString;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class CommentResponseDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1000730198266779597L;

	private String commentedBy;

	private Integer commentedByUserId;

	private String comment;

	private Date commentedOn;

	@Getter(value = AccessLevel.NONE)
	private String commentedOnUTCString;

	private String commentedUserProfilePicture;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getCommentedOnUTCString() {
		if (commentedOn != null) {
			return DateInString.getUtcString(commentedOn);
		} else {
			return "";
		}
	}

}
