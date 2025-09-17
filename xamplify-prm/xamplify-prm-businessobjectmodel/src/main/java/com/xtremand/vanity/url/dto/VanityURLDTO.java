package com.xtremand.vanity.url.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class VanityURLDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String companyName;
	private String companyLogoImagePath;
	private String companyBgImagePath;
	private String companyProfileName;
	private String vanityURLink;
	private boolean showVendorCompanyLogo;
	private String companyFavIconPath;
	private String loginScreenDirection;
	private boolean enableVanityURL;
	private Integer companyId;

	/***** XNFR-233 *****/
	private String backgroundLogoStyle2;
	private String loginType;
	/***** XNFR-233 *****/

	/**** XNFR-416 ****/
	private String backgroundColorStyle1;
	private String backgroundColorStyle2;
	private boolean styleOneBgColor;
	private boolean styleTwoBgColor;
	/**** XNFR-416 ***/
	/*** XBI-2016 **/
	private String loginFormDirectionStyleOne;
	private String companyUrl;

	/***** XNFR-534 *****/

	/***** XNFR-603 *****/
	private String supportEmailId;
}
