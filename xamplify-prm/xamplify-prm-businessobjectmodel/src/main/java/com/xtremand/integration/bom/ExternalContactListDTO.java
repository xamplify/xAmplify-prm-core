package com.xtremand.integration.bom;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ExternalContactListDTO implements Serializable{
	private Long id;
	private String name;
	private String type;
	private String createdAt; 
}
