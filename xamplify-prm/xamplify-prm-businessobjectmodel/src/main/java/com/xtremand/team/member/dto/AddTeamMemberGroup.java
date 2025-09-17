package com.xtremand.team.member.dto;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class AddTeamMemberGroup {

	private BigInteger rowId;

	private Integer teamMemberId;

	private Integer teamMemberUserId;

	private String roleIdsInString;

	private Integer companyId;

	private String companyProfileName;

	private Integer orgAdminId;

	@Getter(value = AccessLevel.NONE)
	private List<Integer> roleIds;

	public List<Integer> getRoleIds() {
		List<String> roleIdsArray = Arrays.asList(roleIdsInString.split(","));
		List<Integer> roleIdsInInteger = new ArrayList<>(roleIdsArray.size());
		for (String role : roleIdsArray) {
			if (!"3".equals(role)) {
				roleIdsInInteger.add(Integer.valueOf(role));
			}
		}
		return roleIdsInInteger;
	}

}
