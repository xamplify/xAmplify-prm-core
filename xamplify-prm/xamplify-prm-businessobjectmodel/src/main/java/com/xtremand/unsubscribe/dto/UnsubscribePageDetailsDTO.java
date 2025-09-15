package com.xtremand.unsubscribe.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class UnsubscribePageDetailsDTO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3796597407124094684L;

	private Integer id;
	
	private String headerText;
	
	private String footerText;
	
	private boolean hideHeaderText;
	
	private boolean hideFooterText;
	
	private Integer userId;

}
