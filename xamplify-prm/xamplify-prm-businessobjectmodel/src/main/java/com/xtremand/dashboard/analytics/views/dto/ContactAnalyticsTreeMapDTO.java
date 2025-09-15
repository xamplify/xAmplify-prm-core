package com.xtremand.dashboard.analytics.views.dto;

import java.math.BigInteger;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;


@Data
public class ContactAnalyticsTreeMapDTO {
	
	private String name;
	
	private BigInteger value;
	
	@Getter(value = AccessLevel.NONE)
	private BigInteger colorValue;
	
	@Getter(value = AccessLevel.NONE)
	private BigInteger y;

	public BigInteger getColorValue() {
		colorValue = value;
		return colorValue;
	}

	public BigInteger getY() {
		y = value;
		return y;
	}

	
	

}
