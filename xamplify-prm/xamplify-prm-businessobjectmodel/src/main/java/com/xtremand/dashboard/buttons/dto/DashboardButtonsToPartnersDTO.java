package com.xtremand.dashboard.buttons.dto;

import java.util.Set;

import lombok.Data;

@Data
public class DashboardButtonsToPartnersDTO {
	private Set<Integer> ids;
	private Integer partnerId;
	private Integer vendorId;
	private Set<String> titles;
	private Integer userListId;
	private Integer partnershipId;

}
