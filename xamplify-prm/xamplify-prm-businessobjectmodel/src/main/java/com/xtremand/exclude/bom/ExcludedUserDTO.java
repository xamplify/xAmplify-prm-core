package com.xtremand.exclude.bom;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ExcludedUserDTO {	
	private Integer userId;
	private String firstName;
	private String lastName;
	private String emailId;
	private Date time;
	private String utcTimeString;
	private String domainName;
}
