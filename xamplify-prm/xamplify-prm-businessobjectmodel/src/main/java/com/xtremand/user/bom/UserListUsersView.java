package com.xtremand.user.bom;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.xtremand.flexi.fields.dto.FlexiFieldRequestDTO;

import lombok.Data;

@Data
public class UserListUsersView implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 1L;

	private Integer userListId;

	private Integer companyId;

	private String emailId;

	private String firstName;

	private String lastName;

	private String companyName;

	private String mobileNumber;

	private String jobTitle;

	private String address;

	private boolean validEmail;

	private Integer userId;

	private boolean partnerGroup;

	private Integer partnershipId;
	
	private Integer contactId;
	
	private String zipCode;
	
	private String state;
	
	private String city;
	
	private String country;
	
	private List<Integer> legalBasis = new ArrayList<>();
	
	private List<FlexiFieldRequestDTO> flexiFields = new ArrayList<>();
	
	private boolean showDetails = false;

	private String contactStatus;

	private String partnerStatus;

}