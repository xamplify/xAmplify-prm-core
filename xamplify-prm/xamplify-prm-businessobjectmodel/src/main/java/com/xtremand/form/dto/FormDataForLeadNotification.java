package com.xtremand.form.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class FormDataForLeadNotification implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7692375507306849818L;

	private String labelName;
	
	private String labelValue;
	
	private boolean required;
	
	private boolean file;
	

}
