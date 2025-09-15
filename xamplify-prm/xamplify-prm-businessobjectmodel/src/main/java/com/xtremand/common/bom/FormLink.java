package com.xtremand.common.bom;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FormLink implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4051568548377819155L;

	private Integer userId;

	private String body;

	private String type;

	private String updatedHtmlBodyForLandingPage;

	private String companyProfileName;

	/**** XNFR-428 ***/
	private Integer vendorFormId;

	private String vendorFormAlias;

}
