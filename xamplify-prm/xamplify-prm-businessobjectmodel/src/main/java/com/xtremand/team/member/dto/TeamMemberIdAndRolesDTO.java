package com.xtremand.team.member.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class TeamMemberIdAndRolesDTO {

	private Integer userId;

	private String rolesInString;

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

}
