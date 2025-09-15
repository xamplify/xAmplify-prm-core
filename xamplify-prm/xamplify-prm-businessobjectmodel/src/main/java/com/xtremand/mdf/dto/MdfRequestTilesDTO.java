package com.xtremand.mdf.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.xtremand.util.dto.NumberFormatterString;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class MdfRequestTilesDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private BigInteger totalRequests;

	private BigDecimal averageRequestSize;

	private BigDecimal totalValue;

	@Getter(value = AccessLevel.NONE)
	private String totalRequestsInString;

	@Getter(value = AccessLevel.NONE)
	private String averageRequestSizeInString;

	@Getter(value = AccessLevel.NONE)
	private String totalValueInString;

	public static MdfRequestTilesDTO setDefaultData() {
		MdfRequestTilesDTO mdfRequestTilesDTO = new MdfRequestTilesDTO();
		mdfRequestTilesDTO.setTotalRequests(BigInteger.valueOf(0));
		mdfRequestTilesDTO.setAverageRequestSize(BigDecimal.valueOf(0));
		mdfRequestTilesDTO.setTotalValue(BigDecimal.valueOf(0));
		mdfRequestTilesDTO.setTotalRequestsInString("0");
		mdfRequestTilesDTO.setAverageRequestSizeInString("$ 0");
		mdfRequestTilesDTO.setTotalValueInString("$ 0");
		return mdfRequestTilesDTO;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getTotalRequestsInString() {
		if (totalRequests != null) {
			return NumberFormatterString.formatValueInTrillionsOrBillions(totalRequests.doubleValue());
		} else {
			return "0";
		}

	}

	public String getAverageRequestSizeInString() {
		if (averageRequestSize != null) {
			return "$ " + NumberFormatterString.formatValueInTrillionsOrBillions(averageRequestSize.doubleValue());
		} else {
			return "0";
		}
	}

	public String getTotalValueInString() {
		if (totalValue != null) {
			return "$ " + NumberFormatterString.formatValueInTrillionsOrBillions(totalValue.doubleValue());
		} else {
			return "0";
		}
	}

}
