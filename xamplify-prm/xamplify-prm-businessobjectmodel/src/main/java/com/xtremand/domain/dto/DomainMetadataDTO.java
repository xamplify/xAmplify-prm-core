package com.xtremand.domain.dto;

import java.io.Serializable;
import java.util.List;

import com.xtremand.util.dto.CreatedTimeConverter;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DomainMetadataDTO extends CreatedTimeConverter implements Serializable {

	private static final long serialVersionUID = -2378435876317211831L;

	private Integer id;
	
	private String domainName;
	
	private String companyInfoMetadata;
	
	private List<DomainMediaResourceDTO> domainMediaResourceDTOs;

}
