package com.xtremand.form.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FormChoiceDTO implements Serializable {
	
	private static final long serialVersionUID = -2829074509085079250L;
	
	private Integer id;
	
	private String labelId;
	
	private String hiddenLabelId;
	
	private String name;
	
	private boolean defaultColumn;
	
	private String itemName;
	
	private boolean correct;
	
	private Integer parentChoiceId;
	
	private List<String> parentChoiceLabelIds = new ArrayList<String>();
	
	private List<FormChoiceDTO> parentChoices  = new ArrayList<>();

}
