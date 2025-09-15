package com.xtremand.custom.field.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.xtremand.salesforce.dto.OpportunityFormFieldsDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomFieldsDTO {
	Integer loggedInUserId;
	List<OpportunityFormFieldsDTO> selectedFields;
	ObjectType objectType;
	
}
