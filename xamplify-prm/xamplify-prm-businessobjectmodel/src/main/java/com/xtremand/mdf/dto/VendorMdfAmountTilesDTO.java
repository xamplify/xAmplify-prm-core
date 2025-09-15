package com.xtremand.mdf.dto;

import java.math.BigInteger;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class VendorMdfAmountTilesDTO extends MdfAmountTilesDTO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7118644487539894983L;
	private BigInteger totalPartners;
	
	private String totalPartnersInString;
	
	public  static VendorMdfAmountTilesDTO setDefaultData() {
		VendorMdfAmountTilesDTO vendorMdfAmountTilesDTO = new VendorMdfAmountTilesDTO();
		vendorMdfAmountTilesDTO.setAvailableBalance(Double.valueOf(0));
		vendorMdfAmountTilesDTO.setTotalBalance(Double.valueOf(0));
		vendorMdfAmountTilesDTO.setTotalPartners(BigInteger.valueOf(0));
		vendorMdfAmountTilesDTO.setUsedBalance(Double.valueOf(0));
		vendorMdfAmountTilesDTO.setTotalBalanceInString("$ 0");
		vendorMdfAmountTilesDTO.setUsedBalanceInString("$ 0");
		vendorMdfAmountTilesDTO.setAvailableBalanceInString("$ 0");
		vendorMdfAmountTilesDTO.setTotalPartnersInString("0");
		return vendorMdfAmountTilesDTO;
		
	}

}
