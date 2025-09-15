package com.xtremand.mdf.dto;

import java.io.Serializable;

import com.xtremand.util.dto.NumberFormatterString;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class MdfAmountTilesDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5858331495484885073L;

	private String companyName;
	
	private String emailId;
	
	private Integer companyId;
	
	private Double totalBalance;

	private Double usedBalance;

	private Double availableBalance;
	
	private String fullName;

	@Getter(value = AccessLevel.NONE)
	private String totalBalanceInString;

	@Getter(value = AccessLevel.NONE)
	private String usedBalanceInString;

	@Getter(value = AccessLevel.NONE)
	private String availableBalanceInString;

	public static MdfAmountTilesDTO setDefaultData() {
		MdfAmountTilesDTO mdfAmountTilesDTO = new MdfAmountTilesDTO();
		mdfAmountTilesDTO.setAvailableBalance(Double.valueOf(0));
		mdfAmountTilesDTO.setTotalBalance(Double.valueOf(0));
		mdfAmountTilesDTO.setUsedBalance(Double.valueOf(0));
		mdfAmountTilesDTO.setTotalBalanceInString("$ 0");
		mdfAmountTilesDTO.setUsedBalanceInString("$ 0");
		mdfAmountTilesDTO.setAvailableBalanceInString("$ 0");
		return mdfAmountTilesDTO;
	}

	public String getTotalBalanceInString() {
		return "$ "+NumberFormatterString.formatValueInTrillionsOrBillions(totalBalance);
	}

	public String getUsedBalanceInString() {
		return "$ "+NumberFormatterString.formatValueInTrillionsOrBillions(usedBalance);
	}

	public String getAvailableBalanceInString() {
		return "$ "+NumberFormatterString.formatValueInTrillionsOrBillions(availableBalance);
	}

}
