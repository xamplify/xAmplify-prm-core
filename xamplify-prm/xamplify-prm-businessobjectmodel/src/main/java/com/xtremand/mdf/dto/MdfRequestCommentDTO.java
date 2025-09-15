package com.xtremand.mdf.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class MdfRequestCommentDTO extends MdfUserMappedDTO  implements Serializable {/**
	 * 
	 */
	private static final long serialVersionUID = 1292701317840908795L;
	
	
	private Integer requestId;
	
	private String comment;
	
	private Integer commentedBy;
	
	private Date createdTime;
	
	private String commentedOnInUTCString;
	
	private Integer companyId;
	
	private Integer partnershipId;
	
	private Integer requestCreatedBy;
	
	private Integer userId;
	
	

}
