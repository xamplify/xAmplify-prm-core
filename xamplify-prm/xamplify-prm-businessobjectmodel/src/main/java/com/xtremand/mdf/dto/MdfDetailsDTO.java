package com.xtremand.mdf.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class MdfDetailsDTO implements Serializable {/**
	 * 
	 */
	private static final long serialVersionUID = -1275344054227501688L;
	
	private Integer partnershipId;
	
	private Double mdfAmount;
	
	private String allocationDateInString;
	
	private String expirationDateInString;
	
	private String description;
	
	private String mdfAmountTypeInString;
	
	private Integer createdBy;
	
	
	

}
