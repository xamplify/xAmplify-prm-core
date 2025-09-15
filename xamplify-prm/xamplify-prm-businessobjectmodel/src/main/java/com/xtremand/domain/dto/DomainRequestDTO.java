package com.xtremand.domain.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class DomainRequestDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private List<String> domainNames = new ArrayList<>();

	private Integer createdUserId;

	private String moduleName;
	
	private Integer id;

	private Boolean isDomainAllowedToAddToSamePartnerAccount;

}
