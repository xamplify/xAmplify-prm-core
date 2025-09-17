package com.xtremand.form.dto;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class FormDataDto {
	
	private List<FormValue> values;
	
	private boolean expanded;
	
	private boolean checkedInForEvent;
	
	private Integer formSubmittedId;

	/*****XNFER-583*****/
	private Integer masterLandingPageId;
	
	private String masterLandingPageName;
	
	private String partnerCompanyName;
	
	private String partnerEmailId;
	
	private Date submittedOn;
	
	private Integer partnerId;
	
}
