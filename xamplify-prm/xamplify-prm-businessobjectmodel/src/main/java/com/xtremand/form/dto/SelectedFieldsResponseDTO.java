package com.xtremand.form.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class SelectedFieldsResponseDTO {
	
	private List<SelectedFieldsDTO> propertiesList = new ArrayList<>();
	private String companyProfileName;
	private Integer loggedInUserId;
	private boolean myPreferances;
	private boolean defaultField;
	private boolean integation;
	private String opportunityType;
}
