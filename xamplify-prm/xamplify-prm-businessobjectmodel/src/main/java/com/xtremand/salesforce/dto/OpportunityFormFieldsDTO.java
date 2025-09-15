package com.xtremand.salesforce.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.xtremand.form.bom.FormDefaultFieldTypeEnum;
import com.xtremand.form.bom.FormFieldTypeEnum;
import com.xtremand.form.bom.FormLookUpDefaultFieldTypeEnum;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpportunityFormFieldsDTO {
	
	private Integer id;
	private String name;
	private String label;
	private String type;
	private Boolean custom;
	private Integer length;
	private List<PicklistValues> picklistValues;
	private boolean selected = false;
	private boolean required = false;
	private boolean defaultField = false;
	private String placeHolder;
	private boolean canUnselect = true;
	private boolean canEditRequired = true;
	private String entryType;
	private boolean defaultColumn;
	private String displayName;
	private FormDefaultFieldTypeEnum formDefaultFieldType;
	private String halopsaTicketTypeName;
	private List<PicklistValues> options = new ArrayList<>();
	private String originalCRMType;
	private Integer order;
	private boolean nonInteractive;
	private boolean dependentPicklist;
	private String controllerName;
	private Integer defaultChoiceId;
    private String defaultChoiceLabel;
    private List<String> referenceTo;
    private boolean isPrivate;
    private FormLookUpDefaultFieldTypeEnum formLookUpDefaultFieldType;
    private boolean isActive;
    private FormFieldTypeEnum formFieldType;
    private boolean emailNotificationEnabledOnUpdate;

}
