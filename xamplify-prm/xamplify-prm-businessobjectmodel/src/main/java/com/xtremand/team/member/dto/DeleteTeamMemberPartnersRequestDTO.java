package com.xtremand.team.member.dto;

import java.util.List;

import lombok.Data;

@Data
public class DeleteTeamMemberPartnersRequestDTO {

	private Integer loggedInUserId;

	private List<Integer> partnerTeamGroupMappingIds;

}
