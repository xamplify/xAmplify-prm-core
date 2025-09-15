package com.xtremand.form.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SurveyTextResponseDTO implements Serializable {/**
	 * 
	 */
	private static final long serialVersionUID = 5776328798117734995L;
	
	private String question;
	
	private String value;
	
	private String createdDateUTC;
	
	private Integer formSubmitId;
	
	private String filePath;
	
	private String questionType;

}
