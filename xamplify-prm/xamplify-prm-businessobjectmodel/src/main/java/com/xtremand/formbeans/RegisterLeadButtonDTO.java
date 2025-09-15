package com.xtremand.formbeans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class RegisterLeadButtonDTO {
	
	private boolean nurtureCampaign;
	
	private Integer campaignCreatedBy;
	
	private Integer partentCampaignId;
	
	private boolean enableLeads;
	
	private boolean channelCampaign;
	
	private String rolesInString;
	
	private Integer vendorCompanyId;

	@Getter(value = AccessLevel.NONE)
	private List<Integer> roleIds;

	public List<Integer> getRoleIds() {
		List<String> roleIdsArray = Arrays.asList(rolesInString.split(","));
		List<Integer> roleIdsInInteger = new ArrayList<>(roleIdsArray.size());
		for (String role : roleIdsArray) {
			roleIdsInInteger.add(Integer.valueOf(role));
		}
		return roleIdsInInteger;
	}

	private Integer createdForCompanyId;
	
	private Integer companyId;
	

}
