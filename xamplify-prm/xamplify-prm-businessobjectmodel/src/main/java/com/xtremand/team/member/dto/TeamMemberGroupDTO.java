package com.xtremand.team.member.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.xtremand.util.dto.CreatedTimeConverter;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonInclude(value = Include.NON_NULL)
public class TeamMemberGroupDTO extends CreatedTimeConverter implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3450422745407487902L;

	private Integer id;

	private String name;

	private String createdBy;

	private Set<Integer> roleIds;

	private Integer userId;

	private Integer mappingId;

	private Integer creatorId;

	private String roleIdsInString;

	private Integer teamMembersCount;

	private boolean defaultGroup;

	private Integer companyIdFromApi;

	private Integer companyId;

	@Getter(value = AccessLevel.NONE)
	private List<Integer> assignedRoleIds;

	private List<TeamMemberModuleDTO> teamMemberModuleDTOs = new ArrayList<>();

	private List<TeamMemberModuleDTO> marketingModuleDTOs = new ArrayList<>();

	private boolean defaultSsoGroup;

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public List<Integer> getAssignedRoleIds() {
		if (StringUtils.hasText(roleIdsInString)) {
			List<String> roleIdsArray = Arrays.asList(roleIdsInString.split(","));
			List<Integer> roleIdsInInteger = new ArrayList<>(roleIdsArray.size());
			for (String role : roleIdsArray) {
				roleIdsInInteger.add(Integer.valueOf(role));
			}
			return roleIdsInInteger;
		} else {
			return new ArrayList<>();
		}

	}

	private boolean approvalManager;
	
	private List<Integer> whiteLabeledReApprovalDamIds;

	private String alias;

	private String signUpUrl;

	private boolean marketingModulesAccessToTeamMemberGroup;

}
