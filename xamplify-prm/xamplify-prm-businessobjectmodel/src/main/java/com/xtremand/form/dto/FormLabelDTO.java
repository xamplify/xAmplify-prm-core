package com.xtremand.form.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.xtremand.form.bom.FormDefaultFieldTypeEnum;
import com.xtremand.form.bom.FormFieldTypeEnum;
import com.xtremand.form.bom.FormLookUpDefaultFieldTypeEnum;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FormLabelDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 862078035437551884L;
	
	private Integer id;
	
	private String labelName;
	
	private String labelId;
	
	private String hiddenLabelId;
	
	private String labelType;
	
	private String placeHolder;
	
	private Integer order;
	
	private boolean required;
	
	private List<FormChoiceDTO> radioButtonChoices = new ArrayList<>();
	
	private List<FormChoiceDTO> checkBoxChoices  = new ArrayList<>();
	
	private List<FormChoiceDTO> dropDownChoices  = new ArrayList<>();
	
	
	private String value = "";
	
	private Integer selectedValue;
	
	private List<Integer> dropdownIds = new ArrayList<>();
	
	private boolean sfCustomField;
	
	private String labelLength;
	
	private String priceType;
	
	private String priceSymbol;
	
	private boolean defaultColumn;
	
	private String description;

	private Set<Integer> correctAnswerLabelChoiceIds = new HashSet<>();
	
	private FormDefaultFieldTypeEnum formDefaultFieldType;
	
	private String displayName;

	private int index;
	
	private int columnOrder;
	
	private String originalCRMType;
	
	private List<LookupChoiceDTO> lookupDropDownChoices  = new ArrayList<>();
	
	private List<FormChoiceDTO> dependentDropDownChoices  = new ArrayList<>();
	
	private Integer parentLabelId;
	
	private boolean nonInteractive;

	private String parentFormLabelId;

	private Integer defaultChoiceId;

	private String defaultChoiceLabel;
	
	private boolean isColumnDisable;
	
	private String lookUpReferenceTo;
	
	private boolean isPrivate;
	
	private FormLookUpDefaultFieldTypeEnum formLookUpDefaultFieldType;
	
	//XNFR-710
	private String selectedChoiceValue = "";
	
	private String formLabelDefaultFieldType;

	private boolean isActive;
	
	private FormFieldTypeEnum formFieldType;
	
	private boolean emailNotificationEnabledOnUpdate;


}
