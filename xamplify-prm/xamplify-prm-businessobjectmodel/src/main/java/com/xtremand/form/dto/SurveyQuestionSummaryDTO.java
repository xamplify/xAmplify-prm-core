package com.xtremand.form.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SurveyQuestionSummaryDTO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5653353952411283836L;
	private String question;
	private Integer answeredCount = 0;
	private Integer skippedCount = 0;
	private Integer responseCount = 0;
	private Integer questionType;
	private List<SurveyChoiceSummaryDTO> choiceSummaries = new ArrayList<SurveyChoiceSummaryDTO>();
	private List<SurveyTextResponseDTO> textResponses = new ArrayList<SurveyTextResponseDTO>();
}
