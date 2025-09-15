package com.xtremand.team.member.dto;

import java.util.ArrayList;
import java.util.List;

import com.xtremand.social.formbeans.TeamMemberDTO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class TeamMemberPostDTO extends TeamMemberVanityUrlPostDTO {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5593303884279801546L;
	
	private List<TeamMemberDTO> teamMemberDTOs = new ArrayList<>();
	
	

}
