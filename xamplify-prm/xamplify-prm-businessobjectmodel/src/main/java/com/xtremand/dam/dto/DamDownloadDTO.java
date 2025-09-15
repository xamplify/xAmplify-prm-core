package com.xtremand.dam.dto;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class DamDownloadDTO extends DamDownloadMappedDTO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8070786444150860279L;

	private String companyLogo;
	
	private String fileName;
	
	private String htmlBody;
	
	private String partnerCompanyLogo;
	
	private Integer createdBy;
	


}
