package com.xtremand.lms.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class LearningTrackContentResponseDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6537185568979538050L;

	private Integer damId;

	private Integer displayIndex;

	private Integer quizId;

}
