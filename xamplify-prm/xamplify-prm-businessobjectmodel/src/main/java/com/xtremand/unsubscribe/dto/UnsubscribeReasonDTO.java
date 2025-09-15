package com.xtremand.unsubscribe.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.xtremand.util.dto.CreatedTimeConverter;

import lombok.Data;
import lombok.EqualsAndHashCode;

@JsonInclude(value = Include.NON_NULL)
@Data
@EqualsAndHashCode(callSuper = false)
public class UnsubscribeReasonDTO extends CreatedTimeConverter {
	
	private Integer id;
	
	private String reason;
	
	private Integer createdUserId;
	
	private String createdBy;
	
	private boolean customReason;
	
	

}
