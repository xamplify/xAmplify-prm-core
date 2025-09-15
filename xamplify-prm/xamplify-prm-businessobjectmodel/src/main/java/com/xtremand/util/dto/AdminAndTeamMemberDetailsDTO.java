package com.xtremand.util.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminAndTeamMemberDetailsDTO implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5621552893374728420L;

	private Integer userId;
	
	private String emailId;
	
	private String firstName;
	
	private String lastName;
	
	private Integer companyId;
	
	private String status;
	
	private Integer id;
	
	private String fullName;
	
	

}
