package com.xtremand.form.emailtemplate.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class FormEmailTemplateDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3687426624121221342L;
	
	
	private Integer id;
	
	private Integer formId;
	
	private Integer emailTemplateId;
	

}
