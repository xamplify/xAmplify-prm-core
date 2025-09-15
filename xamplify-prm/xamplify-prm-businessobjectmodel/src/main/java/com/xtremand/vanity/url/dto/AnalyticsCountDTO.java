package com.xtremand.vanity.url.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class AnalyticsCountDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7057616110703506748L;

	private Integer total;

	private Integer availed;

	private Integer available;

}
