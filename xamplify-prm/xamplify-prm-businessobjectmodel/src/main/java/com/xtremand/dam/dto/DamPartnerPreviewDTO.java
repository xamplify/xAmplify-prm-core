package com.xtremand.dam.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class DamPartnerPreviewDTO implements Serializable {/**
	 * 
	 */
	private static final long serialVersionUID = 8648320812235657379L;
	
	private Integer id;
	
	private boolean beeTemplate;
	
	private Integer createdBy;
	
	private String htmlBody;
	
	private String assestPath;

}
