package com.xtremand.dashboard.buttons.dto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Data;

@Data
public class DashboardButtonSharedWithSSOPartnerEmailNotificationDTO {

	private String firstName;

	private Set<String> dashboardButtonTitles = new HashSet<>();

	private List<String> partnerEmailAddresses = new ArrayList<>();

	private String bodyPrefixText;

	private boolean partnerOnboardedThroughSSO;

}
