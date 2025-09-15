package com.xtremand.dashboard.buttons.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class DashboardButtonsEmailNotificationRequestDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6665319960238514708L;
	private String dashboardTitle;
	private Integer loggedInUserCompanyId;
	private Integer loggedInUserId;
	private boolean dashboardButtonPublished;
	private List<Integer> partnerGroupIds;
	private List<Integer> partnerIds;
	private Integer dashboardButtonId;
	private boolean atLeastOnePartnerOrGroupSelected;
	private List<Integer> publishedPartnerUserIds;
	

}
