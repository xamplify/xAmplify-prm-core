package com.xtremand.account.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class ResendEmailDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 9197025103945496479L;

	private String emailId;

	private String vendorCompanyProfileName;

}
