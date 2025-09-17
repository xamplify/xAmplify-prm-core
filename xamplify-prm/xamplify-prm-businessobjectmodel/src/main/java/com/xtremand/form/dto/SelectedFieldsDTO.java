package com.xtremand.form.dto;

import lombok.Data;

@Data
public class SelectedFieldsDTO {
	
	private Integer id;
	
	private String labelName;
	
	private String displayName;
	
	private String labelId;
	
	private Integer formId;
	
	private Integer integrationId;
	
	private Integer loggedInUserId;
	
	private Integer columnOrder;
	
	private boolean selectedColumn;
	
	private String companyProfileName;
	
	private Integer formLabelId;
	
	private boolean myPreferencesEnabled;
	
	private boolean defaultColumn;
	
	private String formDefaultFieldType;
	
	private boolean required;
	
	private boolean privateField;
}
