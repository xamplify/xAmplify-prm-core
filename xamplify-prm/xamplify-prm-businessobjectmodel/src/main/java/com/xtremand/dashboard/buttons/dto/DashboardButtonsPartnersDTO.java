package com.xtremand.dashboard.buttons.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class DashboardButtonsPartnersDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = -336655772307555027L;

	private Integer userListId;
	
	private Integer userUserListId;

	private Integer partnerId;

	private Integer partnershipId;

}
