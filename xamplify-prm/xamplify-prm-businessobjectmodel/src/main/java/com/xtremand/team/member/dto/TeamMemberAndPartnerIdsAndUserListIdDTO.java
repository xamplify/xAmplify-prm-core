package com.xtremand.team.member.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class TeamMemberAndPartnerIdsAndUserListIdDTO {

	private Integer teamMemberId;

	private Integer userListId;

	private String partnerIdsInString;

	@Getter(value = AccessLevel.NONE)
	private List<Integer> partnerIds;

	public List<Integer> getPartnerIds() {
		if (StringUtils.hasText(partnerIdsInString)) {
			List<String> partnerIdsArray = Arrays.asList(partnerIdsInString.split(","));
			List<Integer> partnerIdsArrayList = new ArrayList<>(partnerIdsArray.size());
			for (String partnerId : partnerIdsArray) {
				if (StringUtils.hasText(partnerId)) {
					partnerIdsArrayList.add(Integer.valueOf(partnerId));
				}
			}
			return partnerIdsArrayList;
		} else {
			return new ArrayList<>();
		}

	}

}
