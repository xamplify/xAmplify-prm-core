package com.xtremand.dam.dto;

import java.math.BigInteger;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DamPartnerAnalyticsDTO extends DamPartnerDetailsDTO {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4364918476154438755L;

	private BigInteger viewCount;
	
	private BigInteger downloadCount;
	
	private Integer damPartnerId;

}
