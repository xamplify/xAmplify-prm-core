package com.xtremand.formbeans;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ExternalEmailTemplateDTO implements Serializable{

	private static final long serialVersionUID = 1L;

	private Long id;
	
	private String name;
	
	private Integer userId;
	
	private String description;
	
	private String content; 	
	
	private String createdAt; 
	
	private String updatedAt; 
	
	private int statusCode;
	
	private String message; 
	
	//private String type;
	
}
