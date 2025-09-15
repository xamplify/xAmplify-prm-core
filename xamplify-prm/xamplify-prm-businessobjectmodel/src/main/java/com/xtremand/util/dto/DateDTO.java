package com.xtremand.util.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class DateDTO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6527698273065601074L;

	private Date date;
	
	private boolean validDate;

}
