package com.xtremand.gdpr.setting.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class LegalBasisDTO implements Serializable{

	private static final long serialVersionUID = 1955272911302242935L;
	
	private Integer id;
	
	private String name;
	
	private String description;
	
	private boolean isDefault;
	
}
