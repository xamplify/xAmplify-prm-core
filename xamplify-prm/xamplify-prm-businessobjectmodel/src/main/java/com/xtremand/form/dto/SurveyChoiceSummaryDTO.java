package com.xtremand.form.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SurveyChoiceSummaryDTO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6209077090607786115L;
	private String choice;
	private Float responsePercentage = 0f;
	private Integer responseCount = 0;
}
