package com.xtremand.util.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class BarChartDualAxesDTO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7354743762557673499L;

	
	/****These columns for json*********/
	private List<String> xaxis;
	
	private List<Integer> yaxis1;
	
	private List<Integer> yaxis2;
	
	


}
