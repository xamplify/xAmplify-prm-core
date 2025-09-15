package com.xtremand.mdf.dto;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import com.xtremand.util.bom.DateUtils;
import com.xtremand.util.dto.NumberFormatterString;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class MdfRequestDeatailsDTO {

	private static final long serialVersionUID = 1L;
	
	private String emailId;
	
	private String companyName;
	
	private String partnerEmailId;

	private BigInteger totalRequests;

	private BigDecimal averageRequestSize;

	private BigDecimal totalValue;
	
	private String fullName;
	
	private Date allocationTime;
	private String allocationTimeInUTC;
	
	private Date createdTime;
	private String createdTimeInUTC;
	
	private DateUtils dateUtils = new DateUtils();
	

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
	

	
	public void setAllocationTime(Date allocationTime) {
		this.allocationTime = allocationTime;
		if (allocationTime != null) {
			setAllocationTimeInUTC(dateUtils.getUTCString(allocationTime));
		}
	}
	
	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
		if (createdTime != null) {
			setCreatedTimeInUTC(dateUtils.getUTCString(createdTime));
		}
	}
	
}
