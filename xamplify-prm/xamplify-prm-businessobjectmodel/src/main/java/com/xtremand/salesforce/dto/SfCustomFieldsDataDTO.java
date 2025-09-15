package com.xtremand.salesforce.dto;

import com.xtremand.form.dto.FormLabelDTO;

import lombok.Data;

@Data
public class SfCustomFieldsDataDTO {
	
	private String sfCfLabelId;
	private String value;
	private String type;
	private String dateTimeIsoValue;	
	private String selectedChoiceValue;
	private FormLabelDTO formLabel; 
}
