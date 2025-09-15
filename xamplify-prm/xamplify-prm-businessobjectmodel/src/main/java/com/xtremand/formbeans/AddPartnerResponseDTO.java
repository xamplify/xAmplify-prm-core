package com.xtremand.formbeans;

import java.io.Serializable;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.xtremand.util.dto.ModuleCustomDTO;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter @Setter
public class AddPartnerResponseDTO implements Serializable {
	private String emailId;
	private String status; 
	private String message; 
	private String userAlias;
	private Integer userId;
	private Integer partnershipId;
	private Set<ModuleCustomDTO> moduleDTOs;
}
