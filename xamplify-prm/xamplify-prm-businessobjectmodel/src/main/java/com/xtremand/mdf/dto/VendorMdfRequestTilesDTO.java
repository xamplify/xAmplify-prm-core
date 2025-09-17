package com.xtremand.mdf.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class VendorMdfRequestTilesDTO extends MdfRequestTilesDTO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3244769201109181294L;
	private BigInteger totalPartners;
	
	public static VendorMdfRequestTilesDTO setDefaultData() {
		VendorMdfRequestTilesDTO vendorMdfRequestTilesDto = new VendorMdfRequestTilesDTO();
		vendorMdfRequestTilesDto.setTotalRequests(BigInteger.valueOf(0));
		vendorMdfRequestTilesDto.setAverageRequestSize(BigDecimal.valueOf(0));
		vendorMdfRequestTilesDto.setTotalValue(BigDecimal.valueOf(0));
		vendorMdfRequestTilesDto.setTotalPartners(BigInteger.valueOf(0));
		vendorMdfRequestTilesDto.setTotalRequestsInString("0");
		vendorMdfRequestTilesDto.setAverageRequestSizeInString("0");
		vendorMdfRequestTilesDto.setTotalValueInString("0");
		return vendorMdfRequestTilesDto;
	}

}
