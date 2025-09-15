package com.xtremand.util.dto;

import java.io.Serializable;
import java.math.BigInteger;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class PartnerGroupDTO extends CreatedTimeConverter implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 610509782864075141L;

	private Integer id;
	
	private String groupName;

	private BigInteger numberOfPartners;
	
	private String uploadedBy;

}
