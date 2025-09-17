package com.xtremand.lms.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.xtremand.util.dto.CreatedTimeConverter;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonPropertyOrder({ "id", "title" })
public class ShareLearningTrackResponseDTO extends CreatedTimeConverter implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1549155180357741525L;

	private Integer id;

	private String title;

	private String createdBy;

}
