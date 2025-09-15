package com.xtremand.mdf.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class MdfRequestTimeLineDTO extends TimeLineUserMappedDTO implements Serializable {/**
	 * 
	 */
	private static final long serialVersionUID = 5502031318788295334L;
	
	
	private String status;
	
	private Double allocationAmount;
	
	private Double reimburseAmount;
	
	private Date allocationDate;
	
	private Date allocationExpirationDate;
	
	private String allocationDateInString;
	
	private String expirationDateInString;
	
	private String description;
	
	private Integer statusInInteger;
	
	private Integer userId;
	
	private Integer partnershipId;
	

}
