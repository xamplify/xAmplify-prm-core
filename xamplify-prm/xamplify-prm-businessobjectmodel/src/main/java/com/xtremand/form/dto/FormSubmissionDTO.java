package com.xtremand.form.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.xtremand.landing.page.analytics.dto.GeoLocationAnalyticsDTO;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FormSubmissionDTO implements Serializable {/**
	 * 
	 */
	private static final long serialVersionUID = 3964502255188897515L;
	
	List<SurveyTextResponseDTO> questionAndAnswers;
	
	GeoLocationAnalyticsDTO geoLocationAnalytics;
	

}
