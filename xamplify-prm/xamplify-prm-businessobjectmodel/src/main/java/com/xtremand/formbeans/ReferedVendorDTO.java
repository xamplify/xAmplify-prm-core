package com.xtremand.formbeans;

import lombok.Data;

@Data
public class ReferedVendorDTO {
	private String emailId;
	private String companyName;
	private String status;
	private String referredDateUTC;
	private String firstName;
	private String lastName;
	private String invitedBy;
}
