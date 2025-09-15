package com.xtremand.white.labeled.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class WhiteLabeledFormDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer vendorCompanyFormId;
	
	private String vendorCompanyFormName;
	
	private String vendorCompanyFormAlias;

	private Integer receivedWhiteLabeledFormId;
	
	private String receivedWhiteLabeledFormName;
	
	private String receivedWhiteLabeledFormAlias;

}
