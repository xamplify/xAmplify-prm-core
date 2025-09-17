package com.xtremand.lms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.xtremand.lms.bom.PartnerActivityType;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LearningTrackPartnerActivityDto {
	private String assetName;
	private String assetThumbnail;
	private PartnerActivityType type;
	private String createdTime;
	private boolean typeQuiz;
	private Integer learningTrackScore;
	private Integer learningTrackMaxScore;
	private Integer quizId;
	private Integer learningTrackId;
	private String featuredImage;

}
