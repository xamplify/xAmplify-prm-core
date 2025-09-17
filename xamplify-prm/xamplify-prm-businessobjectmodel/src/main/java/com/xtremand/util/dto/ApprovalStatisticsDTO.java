package com.xtremand.util.dto;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class ApprovalStatisticsDTO implements Serializable{
	
	private static final long serialVersionUID = 9218316822680009088L;
	
	private String moduleTypeInString;
	
	private Integer approvedCount;
	
	private Integer rejectedCount;
	
	private Integer pendingCount;
	
	private Integer totalCount;
	
	private Integer draftCount;
	
}
