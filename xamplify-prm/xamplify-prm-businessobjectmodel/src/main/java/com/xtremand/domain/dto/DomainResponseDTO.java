package com.xtremand.domain.dto;

import java.io.Serializable;

import com.xtremand.util.dto.CreatedTimeConverter;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DomainResponseDTO extends CreatedTimeConverter implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2378435876317211831L;

	private Integer id;

	private String domainName;
	
	private boolean isDomainAllowedToAddToSamePartnerAccount;
	
	private boolean domainDeactivated;
	
	private String deactivatedOn;

}
