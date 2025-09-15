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
public class SurveyAnalyticsDTO implements Serializable {
	
	private static final long serialVersionUID = 7001906914232148042L;
	
	private String formName;
	private String description;
	private Integer responseCount = 0;
	private List<SurveyQuestionSummaryDTO> questionSummaries = new ArrayList<SurveyQuestionSummaryDTO>();

}
