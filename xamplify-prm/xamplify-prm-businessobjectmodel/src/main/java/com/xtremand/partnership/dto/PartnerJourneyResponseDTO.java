package com.xtremand.partnership.dto;

import java.io.Serializable;

import com.xtremand.util.dto.CreatedTimeConverter;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class PartnerJourneyResponseDTO extends CreatedTimeConverter implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 5690921225262811443L;

	private Integer order;

	private String journeyType;

	private String companyNameAddedByVendor;

	private String companyName;

	private Integer companyId;

}
