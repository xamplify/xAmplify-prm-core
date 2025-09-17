package com.xtremand.form.submit.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class FormSubmitFieldsValuesDTO {

	private Integer labelId;
	private String value;
	private List<Integer> dropdownIds = new ArrayList<>();
	private String labelName;
	private Integer labelTypeId;
	private String labelType;
	private boolean required;
	
	
}
