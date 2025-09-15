package com.xtremand.dashboard.buttons.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class PublishedDashboardButtonDetailsDTO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = -3092469985940579781L;

	private Integer id;

	private String buttonTitle;

	private String partnerGroupIdsString;

	@Getter(value = AccessLevel.NONE)
	private List<Integer> partnerGroupIds;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public List<Integer> getPartnerGroupIds() {
		if (StringUtils.hasText(partnerGroupIdsString)) {
			List<String> partnerGroupIdsStringArray = Arrays.asList(partnerGroupIdsString.split(","));
			List<Integer> updatedPartnerGroupIds = new ArrayList<>(partnerGroupIdsStringArray.size());
			for (String partnerGroupId : partnerGroupIdsStringArray) {
				updatedPartnerGroupIds.add(Integer.valueOf(partnerGroupId));
			}
			return updatedPartnerGroupIds;
		} else {
			return new ArrayList<>();
		}

	}

}
