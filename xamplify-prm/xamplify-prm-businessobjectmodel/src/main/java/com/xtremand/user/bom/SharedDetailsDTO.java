package com.xtremand.user.bom;

import java.util.Date;

import lombok.Data;

@Data
public class SharedDetailsDTO {
	private String firstName;
	private String lastName;
	private String emailId;
	private String contactCompany;
	private String company;
	private String name;
	private String sharedTime;
	private Date sharedDate;
	private String createdTime;
	private Date createdDate;
	
}
