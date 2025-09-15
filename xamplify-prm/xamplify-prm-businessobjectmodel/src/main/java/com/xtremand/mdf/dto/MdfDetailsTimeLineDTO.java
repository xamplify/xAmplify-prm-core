package com.xtremand.mdf.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class MdfDetailsTimeLineDTO extends TimeLineUserMappedDTO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 720812156163538024L;
	
	private Double mdfAmount;
	
	private Date allocationDate;
	
	private String allocationDateInString;
	
	private Date expirationDate;
	
	private String expirationDateInString;
	
	private String mdfAmountType;
	
	private String description;
	
	
	
}
